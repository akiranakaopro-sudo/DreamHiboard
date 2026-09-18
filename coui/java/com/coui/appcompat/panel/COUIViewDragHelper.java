package com.coui.appcompat.panel;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import androidx.core.view.ViewCompat;
import java.util.Arrays;


public class COUIViewDragHelper {
    private static final int BASE_SETTLE_DURATION = 256;
    public static final int DIRECTION_ALL = 3;
    public static final int DIRECTION_HORIZONTAL = 1;
    public static final int DIRECTION_VERTICAL = 2;
    public static final int EDGE_ALL = 15;
    public static final int EDGE_BOTTOM = 8;
    public static final int EDGE_LEFT = 1;
    public static final int EDGE_RIGHT = 2;
    private static final int EDGE_SIZE = 20;
    public static final int EDGE_TOP = 4;
    public static final int INVALID_POINTER = -1;
    private static final int MAX_SETTLE_DURATION = 600;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_SETTLING = 2;
    private static final String TAG = "COUIViewDragHelper";
    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            float tMinusOne = t - 1.0f;
            return (tMinusOne * tMinusOne * tMinusOne * tMinusOne * tMinusOne) + 1.0f;
        }
    };
    private final Callback mCallback;
    private View mCapturedView;
    private final int mDefaultEdgeSize;
    private int mDragState;
    private int[] mEdgeDragsInProgress;
    private int[] mEdgeDragsLocked;
    private int mEdgeSize;
    private int[] mInitialEdgesTouched;
    private float[] mInitialMotionX;
    private float[] mInitialMotionY;
    private float[] mLastMotionX;
    private float[] mLastMotionY;
    private float mMaxVelocity;
    private float mMinVelocity;
    private final ViewGroup mParentView;
    private int mPointersDown;
    private boolean mReleaseInProgress;
    private OverScroller mScroller;
    private int mTouchSlop;
    private int mTrackingEdges;
    private VelocityTracker mVelocityTracker;
    private int mActivePointerId = INVALID_POINTER;
    private final Runnable mSetIdleRunnable = new Runnable() {
        @Override
        public void run() {
            COUIViewDragHelper.this.setDragState(STATE_IDLE);
        }
    };

    public static abstract class Callback {
        public int clampViewPositionHorizontal(View view, int left, int dx) {
            return 0;
        }

        public int clampViewPositionVertical(View view, int top, int dy) {
            return 0;
        }

        public int getOrderedChildIndex(int index) {
            return index;
        }

        public int getViewHorizontalDragRange(View view) {
            return 0;
        }

        public int getViewVerticalDragRange(View view) {
            return 0;
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
        }

        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }

        public void onEdgeTouched(int edgeFlags, int pointerId) {
        }

        public void onViewCaptured(View view, int activePointerId) {
        }

        public void onViewDragStateChanged(int state) {
        }

        public void onViewPositionChanged(View view, int left, int top, int dx, int dy) {
        }

        public void onViewReleased(View view, float xvel, float yvel) {
        }

        public abstract boolean tryCaptureView(View view, int pointerId);
    }

    private COUIViewDragHelper(Context context, ViewGroup viewGroup, Callback callback) {
        if (viewGroup == null) {
            throw new IllegalArgumentException("Parent view may not be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback may not be null");
        }
        this.mParentView = viewGroup;
        this.mCallback = callback;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        int edgeSize = (int) ((context.getResources().getDisplayMetrics().density * 20.0f) + 0.5f);
        this.mDefaultEdgeSize = edgeSize;
        this.mEdgeSize = edgeSize;
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMaxVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mMinVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    private boolean checkNewEdgeDrag(float delta, float odelta, int pointerId, int edge) {
        float fAbs = Math.abs(delta);
        float fAbs2 = Math.abs(odelta);
        if ((this.mInitialEdgesTouched[pointerId] & edge) != edge || (this.mTrackingEdges & edge) == 0 || (this.mEdgeDragsLocked[pointerId] & edge) == edge || (this.mEdgeDragsInProgress[pointerId] & edge) == edge) {
            return false;
        }
        int touchSlop = this.mTouchSlop;
        if (fAbs <= touchSlop && fAbs2 <= touchSlop) {
            return false;
        }
        if (fAbs >= fAbs2 * 0.5f || !this.mCallback.onEdgeLock(edge)) {
            return (this.mEdgeDragsInProgress[pointerId] & edge) == 0 && fAbs > ((float) this.mTouchSlop);
        }
        int[] iArr = this.mEdgeDragsLocked;
        iArr[pointerId] = iArr[pointerId] | edge;
        return false;
    }

    private boolean checkTouchSlop(View view, float dx, float dy) {
        if (view == null) {
            return false;
        }
        boolean checkHorizontal = this.mCallback.getViewHorizontalDragRange(view) > 0;
        boolean checkVertical = this.mCallback.getViewVerticalDragRange(view) > 0;
        if (!checkHorizontal || !checkVertical) {
            return checkHorizontal ? Math.abs(dx) > ((float) this.mTouchSlop) : checkVertical && Math.abs(dy) > ((float) this.mTouchSlop);
        }
        float distanceSq = (dx * dx) + (dy * dy);
        int touchSlop = this.mTouchSlop;
        return distanceSq > ((float) (touchSlop * touchSlop));
    }

    private int clampMag(int value, int absMin, int absMax) {
        int iAbs = Math.abs(value);
        if (iAbs < absMin) {
            return 0;
        }
        return iAbs > absMax ? value > 0 ? absMax : -absMax : value;
    }

    private void clearMotionHistory() {
        float[] fArr = this.mInitialMotionX;
        if (fArr == null) {
            return;
        }
        Arrays.fill(fArr, 0.0f);
        Arrays.fill(this.mInitialMotionY, 0.0f);
        Arrays.fill(this.mLastMotionX, 0.0f);
        Arrays.fill(this.mLastMotionY, 0.0f);
        Arrays.fill(this.mInitialEdgesTouched, 0);
        Arrays.fill(this.mEdgeDragsInProgress, 0);
        Arrays.fill(this.mEdgeDragsLocked, 0);
        this.mPointersDown = 0;
    }

    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        int iAbs;
        if (delta == 0) {
            return 0;
        }
        int width = this.mParentView.getWidth();
        float halfWidth = width / 2;
        float fDistanceInfluenceForSnapDuration = halfWidth + (distanceInfluenceForSnapDuration(Math.min(1.0f, Math.abs(delta) / width)) * halfWidth);
        int iAbs2 = Math.abs(velocity);
        if (iAbs2 > 0) {
            iAbs = Math.round(Math.abs(fDistanceInfluenceForSnapDuration / iAbs2) * 1000.0f) * 4;
        } else {
            iAbs = (int) (((Math.abs(delta) / (motionRange == 0 ? 1 : Math.abs(motionRange))) + 1.0f) * 256.0f);
        }
        return Math.min(iAbs, MAX_SETTLE_DURATION);
    }

    private int computeSettleDuration(View view, int dx, int dy, int xvel, int yvel) {
        float xWeight;
        float yWeight;
        float yDenom;
        float yNumer;
        float xNumer;
        float xDenom;
        int iClampMag = clampMag(xvel, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int iClampMag2 = clampMag(yvel, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int iAbs = Math.abs(dx);
        int iAbs2 = Math.abs(dy);
        int iAbs3 = Math.abs(iClampMag);
        int iAbs4 = Math.abs(iClampMag2);
        int addedVel = iAbs3 + iAbs4;
        int addedDistance = iAbs + iAbs2;
        if (addedVel > 0) {
            if (iClampMag != 0) {
                xNumer = iAbs3;
                xDenom = addedVel;
            } else {
                xNumer = iAbs;
                xDenom = addedDistance;
            }
            xWeight = xNumer / xDenom;
            if (iClampMag2 != 0) {
                yNumer = iAbs4;
                yDenom = addedVel;
            } else {
                yNumer = iAbs2;
                yDenom = addedDistance;
            }
        } else {
            if (addedDistance <= 0) {
                xWeight = 0.5f;
                yWeight = 0.5f;
                return (int) ((computeAxisDuration(dx, iClampMag, this.mCallback.getViewHorizontalDragRange(view)) * xWeight) + (computeAxisDuration(dy, iClampMag2, this.mCallback.getViewVerticalDragRange(view)) * yWeight));
            }
            yDenom = addedDistance;
            xWeight = iAbs / yDenom;
            yNumer = iAbs2;
        }
        yWeight = yNumer / yDenom;
        return (int) ((computeAxisDuration(dx, iClampMag, this.mCallback.getViewHorizontalDragRange(view)) * xWeight) + (computeAxisDuration(dy, iClampMag2, this.mCallback.getViewVerticalDragRange(view)) * yWeight));
    }

    public static COUIViewDragHelper create(ViewGroup viewGroup, Callback callback) {
        return new COUIViewDragHelper(viewGroup.getContext(), viewGroup, callback);
    }

    private void dispatchViewReleased(float xvel, float yvel) {
        this.mReleaseInProgress = true;
        this.mCallback.onViewReleased(this.mCapturedView, xvel, yvel);
        this.mReleaseInProgress = false;
        if (this.mDragState == STATE_DRAGGING) {
            setDragState(STATE_IDLE);
        }
    }

    private float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((f - 0.5f) * 0.47123894f);
    }

    private void dragTo(int left, int top, int dx, int dy) {
        int oldLeft = this.mCapturedView.getLeft();
        int oldTop = this.mCapturedView.getTop();
        if (dx != 0) {
            left = this.mCallback.clampViewPositionHorizontal(this.mCapturedView, left, dx);
            ViewCompat.offsetLeftAndRight(this.mCapturedView, left - oldLeft);
        }
        int clampedLeft = left;
        if (dy != 0) {
            top = this.mCallback.clampViewPositionVertical(this.mCapturedView, top, dy);
            ViewCompat.offsetTopAndBottom(this.mCapturedView, top - oldTop);
        }
        int clampedTop = top;
        if (dx == 0 && dy == 0) {
            return;
        }
        this.mCallback.onViewPositionChanged(this.mCapturedView, clampedLeft, clampedTop,
                clampedLeft - oldLeft, clampedTop - oldTop);
    }

    private void ensureMotionHistorySizeForId(int pointerId) {
        float[] fArr = this.mInitialMotionX;
        if (fArr == null || fArr.length <= pointerId) {
            int pointerCount = pointerId + 1;
            float[] fArr2 = new float[pointerCount];
            float[] fArr3 = new float[pointerCount];
            float[] fArr4 = new float[pointerCount];
            float[] fArr5 = new float[pointerCount];
            int[] iArr = new int[pointerCount];
            int[] iArr2 = new int[pointerCount];
            int[] iArr3 = new int[pointerCount];
            if (fArr != null) {
                System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
                float[] fArr6 = this.mInitialMotionY;
                System.arraycopy(fArr6, 0, fArr3, 0, fArr6.length);
                float[] fArr7 = this.mLastMotionX;
                System.arraycopy(fArr7, 0, fArr4, 0, fArr7.length);
                float[] fArr8 = this.mLastMotionY;
                System.arraycopy(fArr8, 0, fArr5, 0, fArr8.length);
                int[] iArr4 = this.mInitialEdgesTouched;
                System.arraycopy(iArr4, 0, iArr, 0, iArr4.length);
                int[] iArr5 = this.mEdgeDragsInProgress;
                System.arraycopy(iArr5, 0, iArr2, 0, iArr5.length);
                int[] iArr6 = this.mEdgeDragsLocked;
                System.arraycopy(iArr6, 0, iArr3, 0, iArr6.length);
            }
            this.mInitialMotionX = fArr2;
            this.mInitialMotionY = fArr3;
            this.mLastMotionX = fArr4;
            this.mLastMotionY = fArr5;
            this.mInitialEdgesTouched = iArr;
            this.mEdgeDragsInProgress = iArr2;
            this.mEdgeDragsLocked = iArr3;
        }
    }

    private boolean forceSettleCapturedViewAt(int finalLeft, int finalTop, int xvel, int yvel) {
        int left = finalLeft - this.mCapturedView.getLeft();
        int top = finalTop - this.mCapturedView.getTop();
        if (left == 0 && top == 0) {
            setDragState(STATE_IDLE);
            return false;
        }
        computeSettleDuration(this.mCapturedView, left, top, xvel, yvel);
        setDragState(STATE_SETTLING);
        return true;
    }

    private int getEdgesTouched(int x, int y) {
        int result = x < this.mParentView.getLeft() + this.mEdgeSize ? 1 : 0;
        if (y < this.mParentView.getTop() + this.mEdgeSize) {
            result |= 4;
        }
        if (x > this.mParentView.getRight() - this.mEdgeSize) {
            result |= 2;
        }
        return y > this.mParentView.getBottom() - this.mEdgeSize ? result | 8 : result;
    }

    private boolean isValidPointerForActionMove(int pointerId) {
        if (isPointerDown(pointerId)) {
            return true;
        }
        Log.e(TAG, "Ignoring pointerId=" + pointerId + " because ACTION_DOWN was not received for this pointer before ACTION_MOVE. It likely happened because  COUIViewDragHelper did not receive all the events in the event stream.");
        return false;
    }

    private void releaseViewForPointerUp() {
        this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaxVelocity);
        dispatchViewReleased(clampMag(this.mVelocityTracker.getXVelocity(this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity), clampMag(this.mVelocityTracker.getYVelocity(this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity));
    }













    private void reportNewEdgeDrags(float dx, float dy, int pointerId) {
        int edgeDrags = checkNewEdgeDrag(dx, dy, pointerId, EDGE_LEFT) ? 1 : 0;
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_TOP)) {
            edgeDrags |= EDGE_TOP;
        }
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_RIGHT)) {
            edgeDrags |= EDGE_RIGHT;
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_BOTTOM)) {
            edgeDrags |= EDGE_BOTTOM;
        }
        if (edgeDrags != 0) {
            int[] iArr = this.mEdgeDragsInProgress;
            iArr[pointerId] = iArr[pointerId] | edgeDrags;
            this.mCallback.onEdgeDragStarted(edgeDrags, pointerId);
        }
    }

    private void saveInitialMotion(float x, float y, int pointerId) {
        ensureMotionHistorySizeForId(pointerId);
        float[] fArr = this.mInitialMotionX;
        this.mLastMotionX[pointerId] = x;
        fArr[pointerId] = x;
        float[] fArr2 = this.mInitialMotionY;
        this.mLastMotionY[pointerId] = y;
        fArr2[pointerId] = y;
        this.mInitialEdgesTouched[pointerId] = getEdgesTouched((int) x, (int) y);
        this.mPointersDown |= 1 << pointerId;
    }

    private void saveLastMotion(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = motionEvent.getPointerId(i);
            if (isValidPointerForActionMove(pointerId)) {
                float x = motionEvent.getX(i);
                float y = motionEvent.getY(i);
                this.mLastMotionX[pointerId] = x;
                this.mLastMotionY[pointerId] = y;
            }
        }
    }

    public void abort() {
        cancel();
        if (this.mDragState == STATE_SETTLING) {
            int currX = this.mScroller.getCurrX();
            int currY = this.mScroller.getCurrY();
            this.mScroller.abortAnimation();
            int currX2 = this.mScroller.getCurrX();
            int currY2 = this.mScroller.getCurrY();
            this.mCallback.onViewPositionChanged(this.mCapturedView, currX2, currY2, currX2 - currX, currY2 - currY);
        }
        setDragState(STATE_IDLE);
    }

    public boolean canScroll(View view, boolean checkV, int dx, int dy, int x, int y) {
        int scrolledY;
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int scrollX = view.getScrollX();
            int scrollY = view.getScrollY();
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = viewGroup.getChildAt(childCount);
                int scrolledX = x + scrollX;
                if (scrolledX >= childAt.getLeft() && scrolledX < childAt.getRight() && (scrolledY = y + scrollY) >= childAt.getTop() && scrolledY < childAt.getBottom() && canScroll(childAt, true, dx, dy, scrolledX - childAt.getLeft(), scrolledY - childAt.getTop())) {
                    return true;
                }
            }
        }
        return checkV && (view.canScrollHorizontally(-dx) || view.canScrollVertically(-dy));
    }

    public void cancel() {
        this.mActivePointerId = INVALID_POINTER;
        clearMotionHistory();
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void captureChildView(View view, int activePointerId) {
        if (view.getParent() == this.mParentView) {
            this.mCapturedView = view;
            this.mActivePointerId = activePointerId;
            this.mCallback.onViewCaptured(view, activePointerId);
            setDragState(STATE_DRAGGING);
            return;
        }
        throw new IllegalArgumentException("captureChildView: parameter must be a descendant of the COUIViewDragHelper's tracked parent view (" + this.mParentView + ")");
    }

    public boolean continueSettling(boolean deferCallbacks) {
        if (this.mDragState == STATE_SETTLING) {
            boolean zComputeScrollOffset = this.mScroller.computeScrollOffset();
            int currX = this.mScroller.getCurrX();
            int currY = this.mScroller.getCurrY();
            int left = currX - this.mCapturedView.getLeft();
            int top = currY - this.mCapturedView.getTop();
            if (left != 0) {
                ViewCompat.offsetLeftAndRight(this.mCapturedView, left);
            }
            if (top != 0) {
                ViewCompat.offsetTopAndBottom(this.mCapturedView, top);
            }
            if (left != 0 || top != 0) {
                this.mCallback.onViewPositionChanged(this.mCapturedView, currX, currY, left, top);
            }
            if (zComputeScrollOffset && currX == this.mScroller.getFinalX() && currY == this.mScroller.getFinalY()) {
                this.mScroller.abortAnimation();
            } else if (!zComputeScrollOffset) {
            }
            if (deferCallbacks) {
                this.mParentView.post(this.mSetIdleRunnable);
            } else {
                setDragState(STATE_IDLE);
            }
        }
        return this.mDragState == 2;
    }

    public View findTopChildUnder(int x, int y) {
        for (int childCount = this.mParentView.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mParentView.getChildAt(this.mCallback.getOrderedChildIndex(childCount));
            if (x >= childAt.getLeft() && x < childAt.getRight() && y >= childAt.getTop() && y < childAt.getBottom()) {
                return childAt;
            }
        }
        return null;
    }

    public void flingCapturedView(int minLeft, int minTop, int maxLeft, int maxTop) {
        if (!this.mReleaseInProgress) {
            throw new IllegalStateException("Cannot flingCapturedView outside of a call to Callback#onViewReleased");
        }
        this.mScroller.fling(this.mCapturedView.getLeft(), this.mCapturedView.getTop(), (int) this.mVelocityTracker.getXVelocity(this.mActivePointerId), (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId), minLeft, maxLeft, minTop, maxTop);
        setDragState(STATE_SETTLING);
    }

    public int getActivePointerId() {
        return this.mActivePointerId;
    }

    public View getCapturedView() {
        return this.mCapturedView;
    }

    public int getDefaultEdgeSize() {
        return this.mDefaultEdgeSize;
    }

    public int getEdgeSize() {
        return this.mEdgeSize;
    }

    public float getMinVelocity() {
        return this.mMinVelocity;
    }

    public int getTouchSlop() {
        return this.mTouchSlop;
    }

    public int getViewDragState() {
        return this.mDragState;
    }

    public boolean isCapturedViewUnder(int x, int y) {
        return isViewUnder(this.mCapturedView, x, y);
    }

    public boolean isEdgeTouched(int edges) {
        int length = this.mInitialEdgesTouched.length;
        for (int i = 0; i < length; i++) {
            if (isEdgeTouched(edges, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPointerDown(int pointerId) {
        return (this.mPointersDown & (1 << pointerId)) != 0;
    }

    public boolean isViewUnder(View view, int x, int y) {
        return view != null && x >= view.getLeft() && x < view.getRight() && y >= view.getTop() && y < view.getBottom();
    }

    public void processTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        if (actionMasked == 0) {
            cancel();
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        int i = 0;
        if (actionMasked == 0) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            int pointerId = motionEvent.getPointerId(0);
            View toCapture = findTopChildUnder((int) x, (int) y);
            saveInitialMotion(x, y, pointerId);
            tryCaptureViewForDrag(toCapture, pointerId);
            int initialEdges = this.mInitialEdgesTouched[pointerId];
            int trackingEdges = this.mTrackingEdges;
            if ((initialEdges & trackingEdges) != 0) {
                this.mCallback.onEdgeTouched(initialEdges & trackingEdges, pointerId);
                return;
            }
            return;
        }
        if (actionMasked == 1) {
            if (this.mDragState == STATE_DRAGGING) {
                releaseViewForPointerUp();
            }
            cancel();
            return;
        }
        if (actionMasked == 2) {
            if (this.mDragState == STATE_DRAGGING) {
                if (isValidPointerForActionMove(this.mActivePointerId)) {
                    int pointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    float x = motionEvent.getX(pointerIndex);
                    float y = motionEvent.getY(pointerIndex);
                    float[] fArr = this.mLastMotionX;
                    int activePointerId = this.mActivePointerId;
                    int dx = (int) (x - fArr[activePointerId]);
                    int dy = (int) (y - this.mLastMotionY[activePointerId]);
                    dragTo(this.mCapturedView.getLeft() + dx, this.mCapturedView.getTop() + dy, dx, dy);
                    saveLastMotion(motionEvent);
                    return;
                }
                return;
            }
            int pointerCount = motionEvent.getPointerCount();
            while (i < pointerCount) {
                int pointerId = motionEvent.getPointerId(i);
                if (isValidPointerForActionMove(pointerId)) {
                    float x = motionEvent.getX(i);
                    float y = motionEvent.getY(i);
                    float dx = x - this.mInitialMotionX[pointerId];
                    float dy = y - this.mInitialMotionY[pointerId];
                    reportNewEdgeDrags(dx, dy, pointerId);
                    if (this.mDragState != 1) {
                        View toCapture = findTopChildUnder((int) x, (int) y);
                        if (checkTouchSlop(toCapture, dx, dy) && tryCaptureViewForDrag(toCapture, pointerId)) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                i++;
            }
            saveLastMotion(motionEvent);
            return;
        }
        if (actionMasked == 3) {
            if (this.mDragState == STATE_DRAGGING) {
                dispatchViewReleased(0.0f, 0.0f);
            }
            cancel();
            return;
        }
        if (actionMasked == 5) {
            int pointerId = motionEvent.getPointerId(actionIndex);
            float x = motionEvent.getX(actionIndex);
            float y = motionEvent.getY(actionIndex);
            saveInitialMotion(x, y, pointerId);
            if (this.mDragState != 0) {
                if (isCapturedViewUnder((int) x, (int) y)) {
                    tryCaptureViewForDrag(this.mCapturedView, pointerId);
                    return;
                }
                return;
            } else {
                tryCaptureViewForDrag(findTopChildUnder((int) x, (int) y), pointerId);
                int initialEdges2 = this.mInitialEdgesTouched[pointerId];
                int trackingEdges2 = this.mTrackingEdges;
                if ((initialEdges2 & trackingEdges2) != 0) {
                    this.mCallback.onEdgeTouched(initialEdges2 & trackingEdges2, pointerId);
                    return;
                }
                return;
            }
        }
        if (actionMasked != 6) {
            return;
        }
        int pointerId = motionEvent.getPointerId(actionIndex);
        if (this.mDragState == 1 && pointerId == this.mActivePointerId) {
            int pointerCount = motionEvent.getPointerCount();
            while (true) {
                if (i >= pointerCount) {
                    break;
                }
                int otherPointerId = motionEvent.getPointerId(i);
                if (otherPointerId != this.mActivePointerId) {
                    View toCapture = findTopChildUnder((int) motionEvent.getX(i), (int) motionEvent.getY(i));
                    View view = this.mCapturedView;
                    if (toCapture == view && tryCaptureViewForDrag(view, otherPointerId)) {
                        if (this.mActivePointerId == INVALID_POINTER) {
                            break;
                        }
                    }
                }
                i++;
            }
            releaseViewForPointerUp();
        }
        clearMotionHistory(pointerId);
    }

    public void setCapturedView(View view) {
        this.mCapturedView = view;
    }

    public void setDragState(int state) {
        this.mParentView.removeCallbacks(this.mSetIdleRunnable);
        if (this.mDragState != state) {
            this.mDragState = state;
            this.mCallback.onViewDragStateChanged(state);
            if (this.mDragState == 0) {
                this.mCapturedView = null;
            }
        }
    }

    public void setEdgeSize(int size) {
        this.mEdgeSize = size;
    }

    public void setEdgeTrackingEnabled(int edgeFlags) {
        this.mTrackingEdges = edgeFlags;
    }

    public void setMinVelocity(float minVel) {
        this.mMinVelocity = minVel;
    }

    public boolean settleCapturedViewAt(int finalLeft, int finalTop) {
        if (this.mReleaseInProgress) {
            return forceSettleCapturedViewAt(finalLeft, finalTop, (int) this.mVelocityTracker.getXVelocity(this.mActivePointerId), (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId));
        }
        throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to Callback#onViewReleased");
    }

    public boolean shouldInterceptTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            cancel();
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            int pointerId = motionEvent.getPointerId(0);
            saveInitialMotion(x, y, pointerId);
            View childUnder = findTopChildUnder((int) x, (int) y);
            if (childUnder == this.mCapturedView && this.mDragState == STATE_SETTLING) {
                tryCaptureViewForDrag(childUnder, pointerId);
            }
            int edgesTouched = this.mInitialEdgesTouched[pointerId];
            int trackingEdges = this.mTrackingEdges;
            if ((edgesTouched & trackingEdges) != 0) {
                this.mCallback.onEdgeTouched(edgesTouched & trackingEdges, pointerId);
            }
        } else if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
            cancel();
        } else if (actionMasked == MotionEvent.ACTION_MOVE) {
            if (this.mInitialMotionX != null && this.mInitialMotionY != null) {
                int pointerCount = motionEvent.getPointerCount();
                for (int pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++) {
                    int pointerId = motionEvent.getPointerId(pointerIndex);
                    if (!isValidPointerForActionMove(pointerId)) {
                        continue;
                    }
                    float x = motionEvent.getX(pointerIndex);
                    float y = motionEvent.getY(pointerIndex);
                    float dx = x - this.mInitialMotionX[pointerId];
                    float dy = y - this.mInitialMotionY[pointerId];
                    View childUnder = findTopChildUnder((int) x, (int) y);
                    boolean pastSlop = childUnder != null && checkTouchSlop(childUnder, dx, dy);
                    if (pastSlop) {
                        int oldLeft = childUnder.getLeft();
                        int targetLeft = oldLeft + ((int) dx);
                        int clampedX = this.mCallback.clampViewPositionHorizontal(childUnder, targetLeft, (int) dx);
                        int oldTop = childUnder.getTop();
                        int targetTop = oldTop + ((int) dy);
                        int clampedY = this.mCallback.clampViewPositionVertical(childUnder, targetTop, (int) dy);
                        int horizontalDragRange = this.mCallback.getViewHorizontalDragRange(childUnder);
                        int verticalDragRange = this.mCallback.getViewVerticalDragRange(childUnder);
                        if ((horizontalDragRange == 0 || (horizontalDragRange > 0 && clampedX == oldLeft)) && (verticalDragRange == 0 || (verticalDragRange > 0 && clampedY == oldTop))) {
                            break;
                        }
                    }
                    reportNewEdgeDrags(dx, dy, pointerId);
                    if (this.mDragState == STATE_DRAGGING) {
                        break;
                    }
                    if (pastSlop && tryCaptureViewForDrag(childUnder, pointerId)) {
                        break;
                    }
                }
                saveLastMotion(motionEvent);
            }
        } else if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            int pointerId = motionEvent.getPointerId(actionIndex);
            float x = motionEvent.getX(actionIndex);
            float y = motionEvent.getY(actionIndex);
            saveInitialMotion(x, y, pointerId);
            if (this.mDragState == STATE_IDLE) {
                int edgesTouched = this.mInitialEdgesTouched[pointerId];
                int trackingEdges = this.mTrackingEdges;
                if ((edgesTouched & trackingEdges) != 0) {
                    this.mCallback.onEdgeTouched(edgesTouched & trackingEdges, pointerId);
                }
            } else if (this.mDragState == STATE_SETTLING) {
                View childUnder = findTopChildUnder((int) x, (int) y);
                if (childUnder == this.mCapturedView) {
                    tryCaptureViewForDrag(childUnder, pointerId);
                }
            }
        } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
            clearMotionHistory(motionEvent.getPointerId(actionIndex));
        }
        return this.mDragState == STATE_DRAGGING;
    }

    public boolean smoothSlideViewTo(View view, int finalLeft, int finalTop) {
        this.mCapturedView = view;
        this.mActivePointerId = INVALID_POINTER;
        boolean zForceSettleCapturedViewAt = forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0);
        if (!zForceSettleCapturedViewAt && this.mDragState == 0 && this.mCapturedView != null) {
            this.mCapturedView = null;
        }
        return zForceSettleCapturedViewAt;
    }

    public boolean tryCaptureViewForDrag(View view, int pointerId) {
        if (view == this.mCapturedView && this.mActivePointerId == pointerId) {
            return true;
        }
        if (view == null || !this.mCallback.tryCaptureView(view, pointerId)) {
            return false;
        }
        this.mActivePointerId = pointerId;
        captureChildView(view, pointerId);
        return true;
    }

    private float clampMag(float value, float absMin, float absMax) {
        float fAbs = Math.abs(value);
        if (fAbs < absMin) {
            return 0.0f;
        }
        return fAbs > absMax ? value > 0.0f ? absMax : -absMax : value;
    }

    public static COUIViewDragHelper create(ViewGroup viewGroup, float sensitivity, Callback callback) {
        COUIViewDragHelper helper = create(viewGroup, callback);
        helper.mTouchSlop = (int) (helper.mTouchSlop * (1.0f / sensitivity));
        return helper;
    }

    public boolean isEdgeTouched(int edges, int pointerId) {
        return isPointerDown(pointerId) && (this.mInitialEdgesTouched[pointerId] & edges) != 0;
    }

    public boolean checkTouchSlop(int directions) {
        int length = this.mInitialMotionX.length;
        for (int i = 0; i < length; i++) {
            if (checkTouchSlop(directions, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTouchSlop(int directions, int pointerId) {
        if (!isPointerDown(pointerId)) {
            return false;
        }
        boolean checkHorizontal = (directions & 1) == 1;
        boolean checkVertical = (directions & 2) == 2;
        float dx = this.mLastMotionX[pointerId] - this.mInitialMotionX[pointerId];
        float dy = this.mLastMotionY[pointerId] - this.mInitialMotionY[pointerId];
        if (!checkHorizontal || !checkVertical) {
            return checkHorizontal ? Math.abs(dx) > ((float) this.mTouchSlop) : checkVertical && Math.abs(dy) > ((float) this.mTouchSlop);
        }
        float distanceSq = (dx * dx) + (dy * dy);
        int touchSlop = this.mTouchSlop;
        return distanceSq > ((float) (touchSlop * touchSlop));
    }

    private void clearMotionHistory(int pointerId) {
        if (this.mInitialMotionX == null || !isPointerDown(pointerId)) {
            return;
        }
        this.mInitialMotionX[pointerId] = 0.0f;
        this.mInitialMotionY[pointerId] = 0.0f;
        this.mLastMotionX[pointerId] = 0.0f;
        this.mLastMotionY[pointerId] = 0.0f;
        this.mInitialEdgesTouched[pointerId] = 0;
        this.mEdgeDragsInProgress[pointerId] = 0;
        this.mEdgeDragsLocked[pointerId] = 0;
        this.mPointersDown = (~(1 << pointerId)) & this.mPointersDown;
    }
}
