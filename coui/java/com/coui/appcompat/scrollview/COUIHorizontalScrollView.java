package com.coui.appcompat.scrollview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import com.coui.appcompat.animation.COUIPhysicalAnimationUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.scroll.COUIIOverScroller;
import com.coui.appcompat.scroll.SpringOverScroller;
import com.coui.appcompat.version.COUIVersionUtil;
import com.coui.appcompat.view.ViewNative;
import com.coui.appcompat.R;
import java.util.ArrayList;


public class COUIHorizontalScrollView extends HorizontalScrollView {
    static final int ANIMATED_SCROLL_GAP = 250;
    private static final float DEFAULT_INTERACTING_NESTED_SCROLL_ANGLE = 20.0f;
    private static final int DEFAULT_INTERACTING_NESTED_SCROLL_VELOCITY_THRESHOLD = 2500;
    private static final double DEGREE_TO_ARC_CONSTANT = 0.017453292519943295d;
    private static final int FLING_SCROLL_THRESHOLD = 1500;
    private static final float HORIZONTAL_SPRING_BACK_TENSION_MULTIPLE = 3.2f;
    private static final int INVALID_POINTER = -1;
    private static final int OVER_SCROLL_TOUCH_DURATION_THRESHOLD = 100;
    private static final int OVER_SCROLL_TOUCH_OFFSET_THRESHOLD = 10;
    private static final int SLOW_SCROLL_THRESHOLD = 250;
    private static final String TAG = "COUIHorScrollView";
    private float mAbortVelocityX;
    private int mActivePointerId;
    private boolean mAvoidAccidentalTouch;
    private View mChildToScrollTo;
    private int mDispatchEventVelocityThreshold;
    private boolean mEnableDispatchEventWhileOverScrolling;
    private boolean mEnableDispatchEventWhileScrolling;
    private boolean mEnableOptimizedScroll;
    private boolean mEnableVibrator;
    private float mEventFilterAngle;
    private float mFastFlingVelocity;
    private boolean mFillViewport;
    private boolean mFlingStrictSpan;
    private float mFlingVelocityX;
    private float mHorizontalScrollFactor;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private boolean mIsBeingDragged;
    private Boolean mIsColorDevice;
    private boolean mIsLayoutDirty;
    private boolean mIsTouchDownWhileOverScrolling;
    private boolean mIsTouchDownWhileSlowScrolling;
    private boolean mItemClickableWhileOverScrolling;
    private boolean mItemClickableWhileSlowScrolling;
    private int mLastMotionX;
    private long mLastScroll;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private COUIIOverScroller mOverScroller;
    private int mOverflingDistance;
    private int mOverscrollDistance;
    private int mScreenWidth;
    private boolean mScrollStrictSpan;
    private boolean mSmoothScrollingEnabled;
    private SpringOverScroller mSpringOverScroller;
    private final Rect mTempRect;
    private int mTouchSlop;
    private long mTouchTime;
    private VelocityTracker mVelocityTracker;

    public static class COUISavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<COUISavedState> CREATOR = new Parcelable.Creator<COUISavedState>() {

            @Override
            public COUISavedState createFromParcel(Parcel parcel) {
                return new COUISavedState(parcel, COUISavedState.class.getClassLoader());
            }


            @Override
            public COUISavedState[] newArray(int size) {
                return new COUISavedState[size];
            }
        };
        public int scrollOffsetFromStart;

        public COUISavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public String toString() {
            return "HorizontalScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + this.scrollOffsetFromStart + "}";
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(this.scrollOffsetFromStart);
        }

        public COUISavedState(Parcel parcel) {
            super(parcel);
            this.scrollOffsetFromStart = parcel.readInt();
        }

        public COUISavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.scrollOffsetFromStart = parcel.readInt();
        }
    }

    public COUIHorizontalScrollView(Context context) {
        this(context, null);
    }

    private boolean canScroll() {
        View childAt = getChildAt(0);
        if (childAt != null) {
            return getWidth() < (childAt.getWidth() + getPaddingLeft()) + getPaddingRight();
        }
        return false;
    }

    private boolean dispatchClickEvent(View view, MotionEvent motionEvent) {
        boolean zDispatchTouchEvent = true;
        int[] iArr = {0, 1};
        for (int i = 0; i < 2; i++) {
            motionEvent.setAction(iArr[i]);
            zDispatchTouchEvent &= view.dispatchTouchEvent(motionEvent);
        }
        return zDispatchTouchEvent;
    }

    private void doScrollX(int index) {
        if (index != 0) {
            if (this.mSmoothScrollingEnabled) {
                smoothCOUIScrollBy(index, 0);
            } else {
                scrollBy(index, 0);
            }
        }
    }

    private View findFocusableViewInBounds(boolean leftFocus, int left, int right) {
        ArrayList<View> focusables = getFocusables(View.FOCUS_FORWARD);
        int focusableCount = focusables.size();
        View focusCandidate = null;
        boolean candidateIsFullyContained = false;
        for (int index = 0; index < focusableCount; index++) {
            View view = focusables.get(index);
            int viewLeft = view.getLeft();
            int viewRight = view.getRight();
            if (left < viewRight && viewLeft < right) {
                boolean viewIsFullyContained = left < viewLeft && viewRight < right;
                if (focusCandidate == null) {
                    focusCandidate = view;
                    candidateIsFullyContained = viewIsFullyContained;
                } else {
                    boolean viewIsCloser = (leftFocus && viewLeft < focusCandidate.getLeft())
                            || (!leftFocus && viewRight > focusCandidate.getRight());
                    if (candidateIsFullyContained) {
                        if (viewIsFullyContained && viewIsCloser) {
                            focusCandidate = view;
                        }
                    } else if (viewIsFullyContained) {
                        focusCandidate = view;
                        candidateIsFullyContained = true;
                    } else if (viewIsCloser) {
                        focusCandidate = view;
                    }
                }
            }
        }
        return focusCandidate;
    }

    private View findFocusableViewInMyBounds(boolean enabled, int index, View view) {
        int horizontalFadingEdgeLength = getHorizontalFadingEdgeLength() / 2;
        int count = index + horizontalFadingEdgeLength;
        int width = (index + getWidth()) - horizontalFadingEdgeLength;
        return (view == null || view.getLeft() >= width || view.getRight() <= count) ? findFocusableViewInBounds(enabled, count, width) : view;
    }

    private View findViewToDispatchClickEvent(MotionEvent motionEvent) {
        View view = null;
        if (!isClickEvent(motionEvent)) {
            return null;
        }
        Rect rect = new Rect();
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (childAt.getVisibility() == 0 || childAt.getAnimation() != null) {
                childAt.getHitRect(rect);
                boolean zContains = rect.contains(((int) motionEvent.getX()) + getScrollX(), ((int) motionEvent.getY()) + getScrollY());
                MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
                motionEventObtain.offsetLocation(getScrollX() - childAt.getLeft(), getScrollY() - childAt.getTop());
                if (zContains && dispatchClickEvent(childAt, motionEventObtain)) {
                    view = childAt;
                }
                motionEventObtain.recycle();
            }
        }
        return view;
    }

    private int getScrollRange() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getWidth() - ((getWidth() - getPaddingLeft()) - getPaddingRight()));
        }
        return 0;
    }

    private float getVelocityAlongScrollableDirection() {
        if (this.mOverScroller == null || (getNestedScrollAxes() & 2) != 0) {
            return 0.0f;
        }
        return this.mOverScroller.getCurrVelocityX();
    }

    private boolean hookIfNeedInterceptMoveEvent(float fraction, float ratio) {
        return !(this.mEnableDispatchEventWhileScrolling || (this.mEnableDispatchEventWhileOverScrolling && isOverScrolling())) || ratio == 0.0f || ((double) Math.abs(fraction / ratio)) > Math.tan(((double) this.mEventFilterAngle) * DEGREE_TO_ARC_CONSTANT);
    }

    private boolean inChild(int index, int count) {
        if (getChildCount() <= 0) {
            return false;
        }
        int scrollX = getScrollX();
        View childAt = getChildAt(0);
        return count >= childAt.getTop() && count < childAt.getBottom() && index >= childAt.getLeft() - scrollX && index < childAt.getRight() - scrollX;
    }

    private void initCOUIHorizontalScrollView(Context context) {
        if (this.mOverScroller == null) {
            SpringOverScroller springOverScroller = new SpringOverScroller(context);
            this.mSpringOverScroller = springOverScroller;
            springOverScroller.setSpringBackTensionMultiple(HORIZONTAL_SPRING_BACK_TENSION_MULTIPLE);
            this.mSpringOverScroller.setIsScrollView(true);
            this.mOverScroller = this.mSpringOverScroller;
            setEnableFlingSpeedIncrease(true);
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        int overscrollDistance = displayMetrics.widthPixels;
        this.mOverscrollDistance = overscrollDistance;
        this.mOverflingDistance = overscrollDistance;
        this.mScreenWidth = overscrollDistance;
        this.mHorizontalScrollFactor = viewConfiguration.getScaledHorizontalScrollFactor();
        setOverScrollMode(0);
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private boolean isClickEvent(MotionEvent motionEvent) {
        int deltaX = (int) (motionEvent.getX() - this.mInitialTouchX);
        return System.currentTimeMillis() - this.mTouchTime < 100 && ((int) Math.sqrt((double) (deltaX * deltaX))) < 10;
    }

    private Boolean isColorDevice() {
        if (this.mIsColorDevice == null) {
            this.mIsColorDevice = Boolean.valueOf(COUIVersionUtil.isColorOS());
        }
        return this.mIsColorDevice;
    }

    private boolean isFastFling(float fraction, float ratio) {
        return !this.mAvoidAccidentalTouch || Math.abs(fraction) > this.mFastFlingVelocity || Math.abs(ratio) > this.mFastFlingVelocity;
    }

    private boolean isOffScreen(View view) {
        return !isWithinDeltaOfScreen(view, 0);
    }

    private boolean isOverScrolling() {
        return getScrollX() < 0 || getScrollX() > getScrollRange();
    }

    private static boolean isViewDescendantOf(View view, View otherView) {
        if (view == otherView) {
            return true;
        }
        Object parent = view.getParent();
        return (parent instanceof ViewGroup) && isViewDescendantOf((View) parent, otherView);
    }

    private boolean isWithinDeltaOfScreen(View view, int index) {
        view.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(view, this.mTempRect);
        return this.mTempRect.right + index >= getScrollX() && this.mTempRect.left - index <= getScrollX() + getWidth();
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            int index = action == 0 ? 1 : 0;
            int touchX = (int) motionEvent.getX(index);
            this.mLastMotionX = touchX;
            this.mInitialTouchX = touchX;
            this.mInitialTouchY = (int) motionEvent.getY(index);
            this.mActivePointerId = motionEvent.getPointerId(index);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    private void performFeedback() {
        if (this.mEnableVibrator) {
            performHapticFeedback(COUIHapticFeedbackConstants.EDGE_LIST_VIBRATE);
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private boolean scrollAndFocus(int index, int count, int value) {
        int width = getWidth();
        int scrollX = getScrollX();
        int offset = width + scrollX;
        boolean enabled = false;
        boolean flag = index == 17;
        View viewFindFocusableViewInBounds = findFocusableViewInBounds(flag, count, value);
        if (viewFindFocusableViewInBounds == null) {
            viewFindFocusableViewInBounds = this;
        }
        if (count < scrollX || value > offset) {
            doScrollX(flag ? count - scrollX : value - offset);
            enabled = true;
        }
        if (viewFindFocusableViewInBounds != findFocus()) {
            viewFindFocusableViewInBounds.requestFocus(index);
        }
        return enabled;
    }

    private void scrollToChild(View view) {
        view.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(view, this.mTempRect);
        int iComputeScrollDeltaToGetChildRectOnScreen = computeScrollDeltaToGetChildRectOnScreen(this.mTempRect);
        if (iComputeScrollDeltaToGetChildRectOnScreen != 0) {
            scrollBy(iComputeScrollDeltaToGetChildRectOnScreen, 0);
        }
    }

    private boolean scrollToChildRect(Rect rect, boolean enabled) {
        int iComputeScrollDeltaToGetChildRectOnScreen = computeScrollDeltaToGetChildRectOnScreen(rect);
        boolean flag = iComputeScrollDeltaToGetChildRectOnScreen != 0;
        if (flag) {
            if (enabled) {
                scrollBy(iComputeScrollDeltaToGetChildRectOnScreen, 0);
            } else {
                smoothCOUIScrollBy(iComputeScrollDeltaToGetChildRectOnScreen, 0);
            }
        }
        return flag;
    }

    @Override
    public boolean arrowScroll(int direction) {
        int right;
        View viewFindFocus = findFocus();
        if (viewFindFocus == this) {
            viewFindFocus = null;
        }
        View viewFindNextFocus = FocusFinder.getInstance().findNextFocus(this, viewFindFocus, direction);
        int maxScrollAmount = getMaxScrollAmount();
        if (viewFindNextFocus == null || !isWithinDeltaOfScreen(viewFindNextFocus, maxScrollAmount)) {
            if (direction == 17 && getScrollX() < maxScrollAmount) {
                maxScrollAmount = getScrollX();
            } else if (direction == 66 && getChildCount() > 0 && (right = getChildAt(0).getRight() - (getScrollX() + getWidth())) < maxScrollAmount) {
                maxScrollAmount = right;
            }
            if (maxScrollAmount == 0) {
                return false;
            }
            if (direction != 66) {
                maxScrollAmount = -maxScrollAmount;
            }
            doScrollX(maxScrollAmount);
        } else {
            viewFindNextFocus.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(viewFindNextFocus, this.mTempRect);
            doScrollX(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
            viewFindNextFocus.requestFocus(direction);
        }
        if (viewFindFocus == null || !viewFindFocus.isFocused() || !isOffScreen(viewFindFocus)) {
            return true;
        }
        int descendantFocusability = getDescendantFocusability();
        setDescendantFocusability(131072);
        requestFocus();
        setDescendantFocusability(descendantFocusability);
        return true;
    }

    @Override
    public void computeScroll() {
        COUIIOverScroller cOUIIOverScroller = this.mOverScroller;
        if (cOUIIOverScroller == null || !cOUIIOverScroller.computeScrollOffset()) {
            if (this.mFlingStrictSpan) {
                this.mFlingStrictSpan = false;
                return;
            }
            return;
        }
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int cOUICurrX = this.mOverScroller.getCOUICurrX();
        int cOUICurrY = this.mOverScroller.getCOUICurrY();
        if (scrollX != cOUICurrX || scrollY != cOUICurrY) {
            overScrollBy(cOUICurrX - scrollX, cOUICurrY - scrollY, scrollX, scrollY, getScrollRange(), 0, this.mOverflingDistance, 0, false);
            onScrollChanged(getScrollX(), getScrollY(), scrollX, scrollY);
        }
        if (awakenScrollBars()) {
            return;
        }
        postInvalidateOnAnimation();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return super.dispatchKeyEvent(keyEvent) || executeKeyEvent(keyEvent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        COUIIOverScroller cOUIIOverScroller;
        if (this.mEnableDispatchEventWhileScrolling || (this.mEnableDispatchEventWhileOverScrolling && isOverScrolling())) {
            float velocityAlongScrollableDirection = getVelocityAlongScrollableDirection();
            if (motionEvent.getActionMasked() == 0 && this.mDispatchEventVelocityThreshold >= Math.abs(velocityAlongScrollableDirection)) {
                COUIIOverScroller cOUIIOverScroller2 = this.mOverScroller;
                float abortVelocityX = 0.0f;
                if (cOUIIOverScroller2 != null && cOUIIOverScroller2.getCurrVelocityX() != 0.0f) {
                    abortVelocityX = this.mFlingVelocityX;
                }
                this.mAbortVelocityX = abortVelocityX;
                COUIIOverScroller cOUIIOverScroller3 = this.mOverScroller;
                if (cOUIIOverScroller3 != null) {
                    cOUIIOverScroller3.abortAnimation();
                }
                stopNestedScroll();
            }
            if ((motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) && (cOUIIOverScroller = this.mOverScroller) != null && cOUIIOverScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                postInvalidateOnAnimation();
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent keyEvent) {
        this.mTempRect.setEmpty();
        if (!canScroll()) {
            if (!isFocused()) {
                return false;
            }
            View viewFindFocus = findFocus();
            if (viewFindFocus == this) {
                viewFindFocus = null;
            }
            View viewFindNextFocus = FocusFinder.getInstance().findNextFocus(this, viewFindFocus, 66);
            return (viewFindNextFocus == null || viewFindNextFocus == this || !viewFindNextFocus.requestFocus(66)) ? false : true;
        }
        if (keyEvent.getAction() != 0) {
            return false;
        }
        int keyCode = keyEvent.getKeyCode();
        if (keyCode == 21) {
            return !keyEvent.isAltPressed() ? arrowScroll(17) : fullScroll(17);
        }
        if (keyCode == 22) {
            return !keyEvent.isAltPressed() ? arrowScroll(66) : fullScroll(66);
        }
        if (keyCode != 62) {
            return false;
        }
        pageScroll(keyEvent.isShiftPressed() ? 17 : 66);
        return false;
    }

    @Override
    public void fling(int flingVelocityX) {
        this.mFlingVelocityX = flingVelocityX;
        if (getChildCount() > 0) {
            int width = (getWidth() - getPaddingRight()) - getPaddingLeft();
            int iMax = Math.max(0, (getChildAt(0).getRight() - getPaddingLeft()) - width);
            COUIIOverScroller cOUIIOverScroller = this.mOverScroller;
            if (cOUIIOverScroller != null) {
                cOUIIOverScroller.fling(getScrollX(), getScrollY(), flingVelocityX, 0, 0, iMax, 0, 0, width / 2, 0);
            }
            if (!this.mFlingStrictSpan) {
                this.mFlingStrictSpan = true;
            }
            boolean enabled = flingVelocityX > 0;
            View viewFindFocus = findFocus();
            COUIIOverScroller cOUIIOverScroller2 = this.mOverScroller;
            View viewFindFocusableViewInMyBounds = findFocusableViewInMyBounds(enabled, cOUIIOverScroller2 != null ? cOUIIOverScroller2.getCOUIFinalX() : 0, viewFindFocus);
            if (viewFindFocusableViewInMyBounds == null) {
                viewFindFocusableViewInMyBounds = this;
            }
            if (viewFindFocusableViewInMyBounds != viewFindFocus) {
                viewFindFocusableViewInMyBounds.requestFocus(enabled ? 66 : 17);
            }
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean fullScroll(int direction) {
        boolean enabled = direction == 66;
        int width = getWidth();
        Rect rect = this.mTempRect;
        rect.left = 0;
        rect.right = width;
        if (enabled && getChildCount() > 0) {
            this.mTempRect.right = getChildAt(0).getRight();
            Rect rect2 = this.mTempRect;
            rect2.left = rect2.right - width;
        }
        Rect rect3 = this.mTempRect;
        return scrollAndFocus(direction, rect3.left, rect3.right);
    }

    public int getScrollableRange() {
        return (getWidth() - getPaddingLeft()) - getPaddingRight();
    }

    public void invalidateParentIfNeeded() {
        if (isHardwareAccelerated() && (getParent() instanceof View)) {
            ((View) getParent()).invalidate();
        }
    }

    public boolean isEnableFlingSpeedIncrease() {
        SpringOverScroller springOverScroller = this.mSpringOverScroller;
        if (springOverScroller != null) {
            return springOverScroller.isEnableFlingSpeedIncrease();
        }
        return false;
    }

    @Override
    public boolean isFillViewport() {
        return this.mFillViewport;
    }

    @Override
    public boolean isSmoothScrollingEnabled() {
        return this.mSmoothScrollingEnabled;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mScrollStrictSpan) {
            this.mScrollStrictSpan = false;
        }
        if (this.mFlingStrictSpan) {
            this.mFlingStrictSpan = false;
        }
        SpringOverScroller springOverScroller = this.mSpringOverScroller;
        if (springOverScroller != null) {
            springOverScroller.cancelCallback();
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 8 && !this.mIsBeingDragged) {
            int iRound = Math.round((motionEvent.isFromSource(2) ? (motionEvent.getMetaState() & 1) != 0 ? -motionEvent.getAxisValue(9) : motionEvent.getAxisValue(10) : motionEvent.isFromSource(4194304) ? motionEvent.getAxisValue(26) : 0.0f) * this.mHorizontalScrollFactor);
            if (iRound != 0) {
                int scrollRange = getScrollRange();
                int scrollX = getScrollX();
                int index = iRound + scrollX;
                if (index < 0) {
                    scrollRange = 0;
                } else if (index <= scrollRange) {
                    scrollRange = index;
                }
                if (scrollRange != scrollX) {
                    super.scrollTo(scrollRange, getScrollY());
                    return true;
                }
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE && this.mIsBeingDragged) {
            return true;
        }
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                COUIIOverScroller overScroller = this.mOverScroller;
                float currentVelocity = overScroller != null ? overScroller.getCurrVelocityX() : 0.0f;
                boolean isFastFling = Math.abs(this.mFlingVelocityX) > FLING_SCROLL_THRESHOLD;
                this.mIsTouchDownWhileSlowScrolling = Math.abs(currentVelocity) > 0.0f
                        && Math.abs(currentVelocity) < SLOW_SCROLL_THRESHOLD
                        && isFastFling;
                this.mIsTouchDownWhileOverScrolling = isOverScrolling();
                this.mTouchTime = System.currentTimeMillis();
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (!inChild(x, (int) event.getY())) {
                    this.mIsBeingDragged = false;
                    recycleVelocityTracker();
                    break;
                }
                this.mLastMotionX = x;
                this.mInitialTouchX = x;
                this.mInitialTouchY = y;
                this.mActivePointerId = event.getPointerId(0);
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(event);
                this.mIsBeingDragged = this.mOverScroller != null && !this.mOverScroller.isCOUIFinished();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.mIsBeingDragged = false;
                this.mActivePointerId = INVALID_POINTER;
                COUIIOverScroller scroller = this.mOverScroller;
                if (scroller != null && scroller.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                    postInvalidateOnAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int activePointerId = this.mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex == INVALID_POINTER) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                    break;
                }
                int moveX = (int) event.getX(pointerIndex);
                int deltaX = Math.abs(moveX - this.mInitialTouchX);
                int deltaY = Math.abs(((int) event.getY(pointerIndex)) - this.mInitialTouchY);
                if (deltaX > this.mTouchSlop
                        && (getNestedScrollAxes() & View.SCROLL_AXIS_HORIZONTAL) == 0
                        && hookIfNeedInterceptMoveEvent((float) deltaX, (float) deltaY)) {
                    this.mIsBeingDragged = true;
                    this.mLastMotionX = moveX;
                    initVelocityTrackerIfNotExists();
                    this.mVelocityTracker.addMovement(event);
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                int pointerDownIndex = event.getActionIndex();
                int pointerDownX = (int) event.getX(pointerDownIndex);
                this.mLastMotionX = pointerDownX;
                this.mInitialTouchX = pointerDownX;
                this.mInitialTouchY = (int) event.getY(pointerDownIndex);
                this.mActivePointerId = event.getPointerId(pointerDownIndex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                int newPointerIndex = event.findPointerIndex(this.mActivePointerId);
                if (newPointerIndex == INVALID_POINTER) {
                    Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onInterceptTouchEvent ACTION_POINTER_UP");
                    break;
                }
                int newPointerX = (int) event.getX(newPointerIndex);
                this.mLastMotionX = newPointerX;
                this.mInitialTouchX = newPointerX;
                this.mInitialTouchY = (int) event.getY(newPointerIndex);
                break;
            default:
                break;
        }
        return this.mIsBeingDragged;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mFillViewport && View.MeasureSpec.getMode(widthMeasureSpec) != 0 && getChildCount() > 0) {
            View childAt = getChildAt(0);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
            int paddingLeft = getPaddingLeft() + getPaddingRight() + layoutParams.leftMargin + layoutParams.rightMargin;
            int paddingTop = getPaddingTop() + getPaddingBottom() + layoutParams.topMargin + layoutParams.bottomMargin;
            int measuredWidth = getMeasuredWidth() - paddingLeft;
            if (childAt.getMeasuredWidth() < measuredWidth) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, 1073741824), ViewGroup.getChildMeasureSpec(heightMeasureSpec, paddingTop, layoutParams.height));
            }
        }
    }

    @Override
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (getScrollY() == scrollY && getScrollX() == scrollX) {
            return;
        }
        if ((scrollX < 0 || scrollX > getScrollRange()) && this.mFlingStrictSpan) {
            int scrollRange = scrollX >= getScrollRange() ? getScrollRange() : 0;
            scrollX = COUIPhysicalAnimationUtil.calcOverFlingDecelerateDist(scrollRange, scrollX - scrollRange, this.mScreenWidth);
        }
        if (getOverScrollMode() == 2 || (getOverScrollMode() == 1 && getChildAt(0).getWidth() <= getScrollableRange())) {
            scrollX = Math.min(Math.max(scrollX, 0), getScrollRange());
        }
        if (getScrollX() >= 0 && scrollX < 0 && this.mFlingStrictSpan) {
            performFeedback();
            SpringOverScroller springOverScroller = this.mSpringOverScroller;
            if (springOverScroller != null) {
                springOverScroller.notifyHorizontalEdgeReached(scrollX, 0, this.mOverflingDistance);
            }
        }
        if (getScrollX() <= getScrollRange() && scrollX > getScrollRange() && this.mFlingStrictSpan) {
            performFeedback();
            SpringOverScroller springOverScroller2 = this.mSpringOverScroller;
            if (springOverScroller2 != null) {
                springOverScroller2.notifyHorizontalEdgeReached(scrollX, getScrollRange(), this.mOverflingDistance);
            }
        }
        if (isColorDevice().booleanValue()) {
            ViewNative.setScrollX(this, scrollX);
            ViewNative.setScrollY(this, scrollY);
        } else {
            super.scrollTo(scrollX, scrollY);
        }
        invalidateParentIfNeeded();
        awakenScrollBars();
    }

    @Override
    public boolean onRequestFocusInDescendants(int direction, Rect rect) {
        if (direction == 2) {
            direction = 66;
        } else if (direction == 1) {
            direction = 17;
        }
        View viewFindNextFocus = rect == null ? FocusFinder.getInstance().findNextFocus(this, null, direction) : FocusFinder.getInstance().findNextFocusFromRect(this, rect, direction);
        if (viewFindNextFocus == null || isOffScreen(viewFindNextFocus)) {
            return false;
        }
        return viewFindNextFocus.requestFocus(direction, rect);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int overscrollDistance = getContext().getResources().getDisplayMetrics().widthPixels;
        this.mOverscrollDistance = overscrollDistance;
        this.mOverflingDistance = overscrollDistance;
        this.mScreenWidth = overscrollDistance;
        View viewFindFocus = findFocus();
        if (viewFindFocus == null || this == viewFindFocus || !isWithinDeltaOfScreen(viewFindFocus, getRight() - getLeft())) {
            return;
        }
        viewFindFocus.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(viewFindFocus, this.mTempRect);
        doScrollX(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        ViewParent parent;
        initVelocityTrackerIfNotExists();
        this.mVelocityTracker.addMovement(motionEvent);
        int action = motionEvent.getAction() & 255;
        if (action != 0) {
            if (action == 1) {
                boolean zIsOverScrolling = isOverScrolling();
                boolean enabled = this.mItemClickableWhileSlowScrolling && this.mIsTouchDownWhileSlowScrolling;
                boolean flag = this.mItemClickableWhileOverScrolling && this.mIsTouchDownWhileOverScrolling && zIsOverScrolling;
                if (enabled || flag) {
                    findViewToDispatchClickEvent(motionEvent);
                }
                if (this.mIsBeingDragged) {
                    initVelocityTrackerIfNotExists();
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                    int xVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                    if (Math.abs(xVelocity) <= this.mMinimumVelocity) {
                        COUIIOverScroller cOUIIOverScroller = this.mOverScroller;
                        if (cOUIIOverScroller != null && cOUIIOverScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                            postInvalidateOnAnimation();
                        }
                    } else if (getScrollX() < 0) {
                        if (xVelocity > -1500) {
                            COUIIOverScroller cOUIIOverScroller2 = this.mOverScroller;
                            if (cOUIIOverScroller2 != null) {
                                cOUIIOverScroller2.setCurrVelocityX(-xVelocity);
                            }
                            COUIIOverScroller cOUIIOverScroller3 = this.mOverScroller;
                            if (cOUIIOverScroller3 != null && cOUIIOverScroller3.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                                postInvalidateOnAnimation();
                            }
                        } else {
                            fling(-xVelocity);
                        }
                    } else if (getScrollX() > getScrollRange()) {
                        if (xVelocity < FLING_SCROLL_THRESHOLD) {
                            COUIIOverScroller cOUIIOverScroller4 = this.mOverScroller;
                            if (cOUIIOverScroller4 != null) {
                                cOUIIOverScroller4.setCurrVelocityX(-xVelocity);
                            }
                            COUIIOverScroller cOUIIOverScroller5 = this.mOverScroller;
                            if (cOUIIOverScroller5 != null && cOUIIOverScroller5.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                                postInvalidateOnAnimation();
                            }
                        } else {
                            fling(-xVelocity);
                        }
                    } else if (getScrollX() > 0 && getScrollX() < getScrollRange()) {
                        fling(-xVelocity);
                    }
                    if (getScrollX() < 0 || getScrollX() > getScrollRange()) {
                        performFeedback();
                    }
                    this.mActivePointerId = -1;
                    this.mIsBeingDragged = false;
                    recycleVelocityTracker();
                } else {
                    COUIIOverScroller cOUIIOverScroller6 = this.mOverScroller;
                    if (cOUIIOverScroller6 != null && cOUIIOverScroller6.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                        postInvalidateOnAnimation();
                    }
                }
            } else if (action == 2) {
                COUIIOverScroller cOUIIOverScroller7 = this.mOverScroller;
                if ((cOUIIOverScroller7 instanceof SpringOverScroller) && this.mEnableOptimizedScroll) {
                    ((SpringOverScroller) cOUIIOverScroller7).triggerCallback();
                }
                int iFindPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                if (iFindPointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                } else {
                    int touchX = (int) motionEvent.getX(iFindPointerIndex);
                    int iCalcRealOverScrollDist = this.mLastMotionX - touchX;
                    if (!this.mIsBeingDragged && Math.abs(iCalcRealOverScrollDist) > this.mTouchSlop) {
                        ViewParent parent2 = getParent();
                        if (parent2 != null) {
                            parent2.requestDisallowInterceptTouchEvent(true);
                        }
                        this.mIsBeingDragged = true;
                        iCalcRealOverScrollDist = iCalcRealOverScrollDist > 0 ? iCalcRealOverScrollDist - this.mTouchSlop : iCalcRealOverScrollDist + this.mTouchSlop;
                    }
                    if (this.mIsBeingDragged) {
                        this.mLastMotionX = touchX;
                        int scrollRange = getScrollRange();
                        if (getScrollX() < 0) {
                            iCalcRealOverScrollDist = COUIPhysicalAnimationUtil.calcRealOverScrollDist(iCalcRealOverScrollDist, getScrollX(), this.mOverscrollDistance);
                        } else if (getScrollX() > getScrollRange()) {
                            iCalcRealOverScrollDist = COUIPhysicalAnimationUtil.calcRealOverScrollDist(iCalcRealOverScrollDist, getScrollX() - getScrollRange(), this.mOverscrollDistance);
                        }
                        if (overScrollBy(iCalcRealOverScrollDist, 0, getScrollX(), 0, scrollRange, 0, this.mOverscrollDistance, 0, true) && !hasNestedScrollingParent()) {
                            this.mVelocityTracker.clear();
                        }
                    }
                }
            } else if (action != 3) {
                if (action == 6) {
                    onSecondaryPointerUp(motionEvent);
                }
            } else if (this.mIsBeingDragged && getChildCount() > 0) {
                COUIIOverScroller cOUIIOverScroller8 = this.mOverScroller;
                if (cOUIIOverScroller8 != null && cOUIIOverScroller8.springBack(getScrollX(), getScrollY(), 0, getScrollRange(), 0, 0)) {
                    postInvalidateOnAnimation();
                }
                this.mActivePointerId = -1;
                this.mIsBeingDragged = false;
                recycleVelocityTracker();
            }
        } else {
            if (getChildCount() == 0) {
                return false;
            }
            COUIIOverScroller cOUIIOverScroller9 = this.mOverScroller;
            if (cOUIIOverScroller9 != null && !cOUIIOverScroller9.isCOUIFinished() && (parent = getParent()) != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            COUIIOverScroller cOUIIOverScroller10 = this.mOverScroller;
            if (cOUIIOverScroller10 != null && !cOUIIOverScroller10.isCOUIFinished()) {
                this.mAbortVelocityX = this.mOverScroller.getCurrVelocityX() != 0.0f ? this.mFlingVelocityX : 0.0f;
                this.mOverScroller.abortAnimation();
                if (this.mFlingStrictSpan) {
                    this.mFlingStrictSpan = false;
                }
            }
            int touchX = (int) motionEvent.getX();
            this.mLastMotionX = touchX;
            this.mInitialTouchX = touchX;
            this.mInitialTouchY = (int) motionEvent.getY();
            this.mActivePointerId = motionEvent.getPointerId(0);
        }
        return true;
    }

    @Override
    public void onVisibilityChanged(View view, int index) {
        SpringOverScroller springOverScroller;
        super.onVisibilityChanged(view, index);
        if (index == 0 || (springOverScroller = this.mSpringOverScroller) == null) {
            return;
        }
        springOverScroller.abortAnimation();
        this.mSpringOverScroller.cancelCallback();
    }

    @Override
    public boolean overScrollBy(int index, int count, int value, int offset, int delta, int start, int end, int min, boolean enabled) {
        onOverScrolled(value + index, offset + count, false, false);
        return false;
    }

    @Override
    public boolean pageScroll(int direction) {
        boolean enabled = direction == 66;
        int width = getWidth();
        if (enabled) {
            this.mTempRect.left = getScrollX() + width;
            if (getChildCount() > 0) {
                View childAt = getChildAt(0);
                if (this.mTempRect.left + width > childAt.getRight()) {
                    this.mTempRect.left = childAt.getRight() - width;
                }
            }
        } else {
            this.mTempRect.left = getScrollX() - width;
            Rect rect = this.mTempRect;
            if (rect.left < 0) {
                rect.left = 0;
            }
        }
        Rect rect2 = this.mTempRect;
        int count = rect2.left;
        int value = width + count;
        rect2.right = value;
        return scrollAndFocus(direction, count, value);
    }

    @Override
    public void requestChildFocus(View view, View childToScrollTo) {
        if (childToScrollTo != null && childToScrollTo.getRevealOnFocusHint()) {
            if (this.mIsLayoutDirty) {
                this.mChildToScrollTo = childToScrollTo;
            } else {
                scrollToChild(childToScrollTo);
            }
        }
        super.requestChildFocus(view, childToScrollTo);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean enabled) {
        rect.offset(view.getLeft() - view.getScrollX(), view.getTop() - view.getScrollY());
        return scrollToChildRect(rect, enabled);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean enabled) {
        if (enabled) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(enabled);
    }

    @Override
    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
    }

    public void setAvoidAccidentalTouch(boolean avoidAccidentalTouch) {
        this.mAvoidAccidentalTouch = avoidAccidentalTouch;
    }

    public void setDispatchEventWhileOverScrolling(boolean enableDispatchEventWhileOverScrolling) {
        this.mEnableDispatchEventWhileOverScrolling = enableDispatchEventWhileOverScrolling;
    }

    public void setDispatchEventWhileScrolling(boolean enableDispatchEventWhileScrolling) {
        this.mEnableDispatchEventWhileScrolling = enableDispatchEventWhileScrolling;
    }

    public void setDispatchEventWhileScrollingThreshold(int dispatchEventVelocityThreshold) {
        this.mDispatchEventVelocityThreshold = dispatchEventVelocityThreshold;
    }

    public void setEnableFlingSpeedIncrease(boolean enabled) {
        SpringOverScroller springOverScroller = this.mSpringOverScroller;
        if (springOverScroller != null) {
            springOverScroller.setEnableFlingSpeedIncrease(enabled);
        }
    }

    public void setEnableVibrator(boolean enableVibrator) {
        this.mEnableVibrator = enableVibrator;
    }

    public void setEventFilterTangent(float eventFilterAngle) {
        this.mEventFilterAngle = eventFilterAngle;
    }

    public void setFastFlingThreshold(float fraction) {
        this.mFastFlingVelocity = Math.max(fraction, 0.0f);
    }

    @Override
    public void setFillViewport(boolean fillViewport) {
        if (fillViewport != this.mFillViewport) {
            this.mFillViewport = fillViewport;
            requestLayout();
        }
    }

    public void setIsUseOptimizedScroll(boolean enableOptimizedScroll) {
        this.mEnableOptimizedScroll = enableOptimizedScroll;
    }

    public void setItemClickableWhileOverScrolling(boolean itemClickableWhileOverScrolling) {
        this.mItemClickableWhileOverScrolling = itemClickableWhileOverScrolling;
    }

    public void setItemClickableWhileSlowScrolling(boolean itemClickableWhileSlowScrolling) {
        this.mItemClickableWhileSlowScrolling = itemClickableWhileSlowScrolling;
    }

    @Override
    public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
        this.mSmoothScrollingEnabled = smoothScrollingEnabled;
    }

    public void setSpringOverScrollerDebug(boolean enabled) {
        SpringOverScroller springOverScroller = this.mSpringOverScroller;
        if (springOverScroller != null) {
            springOverScroller.setDebug(enabled);
        }
    }

    public final void smoothCOUIScrollBy(int index, int count) {
        if (getChildCount() == 0) {
            return;
        }
        if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
            int iMax = Math.max(0, getChildAt(0).getWidth() - ((getWidth() - getPaddingRight()) - getPaddingLeft()));
            int scrollX = getScrollX();
            int iMax2 = Math.max(0, Math.min(index + scrollX, iMax)) - scrollX;
            COUIIOverScroller cOUIIOverScroller = this.mOverScroller;
            if (cOUIIOverScroller != null) {
                cOUIIOverScroller.startScroll(scrollX, getScrollY(), iMax2, 0);
            }
            postInvalidateOnAnimation();
        } else {
            COUIIOverScroller cOUIIOverScroller2 = this.mOverScroller;
            if (cOUIIOverScroller2 != null && !cOUIIOverScroller2.isCOUIFinished()) {
                this.mAbortVelocityX = this.mOverScroller.getCurrVelocityX() != 0.0f ? this.mFlingVelocityX : 0.0f;
                this.mOverScroller.abortAnimation();
                if (this.mFlingStrictSpan) {
                    this.mFlingStrictSpan = false;
                }
            }
            scrollBy(index, count);
        }
        this.mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    public final void smoothCOUIScrollTo(int index, int count) {
        smoothCOUIScrollBy(index - getScrollX(), count - getScrollY());
    }

    public COUIHorizontalScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIHorizontalScrollView(Context context, AttributeSet attributeSet, int index) {
        super(context, attributeSet, index);
        this.mScreenWidth = 0;
        this.mTempRect = new Rect();
        this.mOverScroller = null;
        this.mSpringOverScroller = null;
        this.mIsLayoutDirty = true;
        this.mChildToScrollTo = null;
        this.mIsBeingDragged = false;
        this.mSmoothScrollingEnabled = true;
        this.mActivePointerId = -1;
        this.mItemClickableWhileSlowScrolling = true;
        this.mItemClickableWhileOverScrolling = true;
        this.mEnableDispatchEventWhileScrolling = false;
        this.mEnableDispatchEventWhileOverScrolling = false;
        this.mDispatchEventVelocityThreshold = DEFAULT_INTERACTING_NESTED_SCROLL_VELOCITY_THRESHOLD;
        this.mEventFilterAngle = DEFAULT_INTERACTING_NESTED_SCROLL_ANGLE;
        this.mFastFlingVelocity = 1500.0f;
        this.mAvoidAccidentalTouch = true;
        this.mScrollStrictSpan = false;
        this.mFlingStrictSpan = false;
        this.mEnableOptimizedScroll = true;
        this.mEnableVibrator = true;
        this.mIsColorDevice = null;
        initCOUIHorizontalScrollView(context);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUIHorizontalScrollView, index, 0);
        this.mEnableVibrator = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIHorizontalScrollView_couiScrollViewEnableVibrator, true);
        typedArrayObtainStyledAttributes.recycle();
    }

    public COUIHorizontalScrollView(Context context, AttributeSet attributeSet, int index, int count) {
        super(context, attributeSet, index, count);
        this.mScreenWidth = 0;
        this.mTempRect = new Rect();
        this.mOverScroller = null;
        this.mSpringOverScroller = null;
        this.mIsLayoutDirty = true;
        this.mChildToScrollTo = null;
        this.mIsBeingDragged = false;
        this.mSmoothScrollingEnabled = true;
        this.mActivePointerId = -1;
        this.mItemClickableWhileSlowScrolling = true;
        this.mItemClickableWhileOverScrolling = true;
        this.mEnableDispatchEventWhileScrolling = false;
        this.mEnableDispatchEventWhileOverScrolling = false;
        this.mDispatchEventVelocityThreshold = DEFAULT_INTERACTING_NESTED_SCROLL_VELOCITY_THRESHOLD;
        this.mEventFilterAngle = DEFAULT_INTERACTING_NESTED_SCROLL_ANGLE;
        this.mFastFlingVelocity = 1500.0f;
        this.mAvoidAccidentalTouch = true;
        this.mScrollStrictSpan = false;
        this.mFlingStrictSpan = false;
        this.mEnableOptimizedScroll = true;
        this.mEnableVibrator = true;
        this.mIsColorDevice = null;
        initCOUIHorizontalScrollView(context);
    }
}


