package com.coui.appcompat.poplist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import androidx.dynamicanimation.animation.FloatValueHolder;
import com.coui.appcompat.R;
import com.coui.appcompat.talkbackutil.COUIAccessibilityUtil;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.list.COUIForegroundListView;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.scrollbar.COUIScrollBar;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class COUITouchListView extends COUIForegroundListView implements COUIScrollBar.COUIScrollable {
    public static final int ACTION_IS_FROM_TOUCH_LISTVIEW = -1;
    private static final int CAN_SCROLL_DOWN = 1;
    private static final int CAN_SCROLL_UP = -1;
    private static final boolean COUI_DEBUG;
    private static final String TAG = "COUITouchListView";
    private boolean mAllowDispatchEvent;
    private boolean mAllowScroll;
    private COUIScrollBar mCOUIScrollBar;
    private Rect mChildRectTemp;
    private DividerAnimationController mDividerAnimationController;
    private View mDownView;
    private int mDownY;
    private boolean mInTalkbackMode;
    private boolean mIsDynamicSelection;
    private boolean mIsNeedVibrate;
    private List<Integer> mItemHeightMap;
    private int mLastMotion;
    private int mLastTouchTarget;
    private Rect mParentRectTemp;
    private int mScrollY;
    private int mScrollbarVerticalPadding;
    private int mTotalHeight;

    public class DividerAnimationController {
        private static final float DIVIDER_ANIMATION_RATIO = 0.0f;
        private static final float DIVIDER_ANIMATION_RESPONSE = 0.25f;
        private static final float DIVIDER_DEFAULT_ALPHA = 1.0f;
        private static final int MOVE_FLAG_DOWN = 2;
        private static final int MOVE_FLAG_OUT = 0;
        private static final int MOVE_FLAG_UP = -2;
        private Map<View, COUISpringAnimation> mDividerAnimations;
        private boolean mForceStopDividerAnimation;
        private boolean mNoNeedDoDividerAnimation;
        private int mScrollState;

        private DividerAnimationController() {
            this.mScrollState = 0;
            this.mDividerAnimations = new HashMap();
            this.mNoNeedDoDividerAnimation = false;
            this.mForceStopDividerAnimation = false;
        }

        public void dividerAnim(boolean show, int moveDirection) {
            if (COUITouchListView.this.mLastTouchTarget == -1) {
                return;
            }
            COUITouchListView listView = COUITouchListView.this;
            if (listView.canSelect(listView.getChildAt(listView.mLastTouchTarget - listView.getFirstVisiblePosition()))) {
                float targetAlpha = show ? DIVIDER_DEFAULT_ALPHA : DIVIDER_ANIMATION_RATIO;
                if (listView.mLastTouchTarget > 0 && (!show || moveDirection != MOVE_FLAG_UP)) {
                    startDividerAnimation((listView.mLastTouchTarget - 1) - listView.getFirstVisiblePosition(), targetAlpha);
                }
                if (listView.mLastTouchTarget < listView.getCount() - 1) {
                    if (show && moveDirection == MOVE_FLAG_DOWN) {
                        return;
                    }
                    startDividerAnimation((listView.mLastTouchTarget + 1) - listView.getFirstVisiblePosition(), targetAlpha);
                }
            }
        }

        public void dividerAnimOut() {
            dividerAnim(false, 0);
        }

        private COUISpringAnimation getOrCreateDividerAnimation(final View view) {
            COUISpringAnimation animation = this.mDividerAnimations.get(view);
            if (animation != null) {
                return animation;
            }
            COUISpringAnimation newAnimation = new COUISpringAnimation(new FloatValueHolder());
            COUISpringForce spring = new COUISpringForce();
            spring.setBounce(DIVIDER_ANIMATION_RATIO);
            spring.setResponse(DIVIDER_ANIMATION_RESPONSE);
            newAnimation.setSpring(spring);
            newAnimation.setMinimumVisibleChange(0.002f);
            newAnimation.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public void onAnimationUpdate(COUIDynamicAnimation animation, float value, float velocity) {
                    view.setAlpha(value);
                }
            });
            newAnimation.setStartValue(DIVIDER_DEFAULT_ALPHA);
            this.mDividerAnimations.put(view, newAnimation);
            return newAnimation;
        }

        public void onScrollStateChanged(int scrollState) {
            if (this.mScrollState != scrollState) {
                if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                        || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
                        && !COUITouchListView.this.mIsDynamicSelection) {
                    resetDividerAnimation();
                    this.mNoNeedDoDividerAnimation = true;
                } else {
                    this.mNoNeedDoDividerAnimation = false;
                }
                this.mScrollState = scrollState;
            }
        }

        // Leapy modified 2026-07-29: Divider views are animated to alpha zero
        // while a popup item is pressed. Restore every tracked view before the
        // list detaches so a canceled/completed animation cannot leak an
        // invisible divider into the next popup through ListView recycling.
        public void release() {
            Iterator<Map.Entry<View, COUISpringAnimation>> it = this.mDividerAnimations.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<View, COUISpringAnimation> entry = it.next();
                entry.getValue().cancel();
                entry.getKey().setAlpha(DIVIDER_DEFAULT_ALPHA);
                it.remove();
            }
        }

        private void resetDividerAnimation() {
            for (Map.Entry<View, COUISpringAnimation> entry : this.mDividerAnimations.entrySet()) {
                COUISpringAnimation value = entry.getValue();
                if (value.isRunning()) {
                    value.cancelComplete();
                }
                // A completed hide animation is no longer running but may
                // still have alpha zero, so restoration must be unconditional.
                entry.getKey().setAlpha(DIVIDER_DEFAULT_ALPHA);
            }
        }
        // Leapy end

        private void startDividerAnimation(int childIndex, float targetAlpha) {
            View child = COUITouchListView.this.getChildAt(childIndex);
            if (child == null || this.mNoNeedDoDividerAnimation || this.mForceStopDividerAnimation || child.getAlpha() == targetAlpha) {
                return;
            }
            int adapterPosition = COUITouchListView.this.getFirstVisiblePosition() + childIndex;
            if (adapterPosition >= 0 && adapterPosition < COUITouchListView.this.getAdapter().getCount()) {
                if (COUITouchListView.this.getAdapter().getItemViewType(adapterPosition) == 2) {
                    return;
                }
                getOrCreateDividerAnimation(child).animateToFinalPosition(targetAlpha);
                return;
            }
            Log.e(TAG, "startDividerAnimation position out of range. count=" + getAdapter().getCount() + ", childIndex=" + childIndex + ", firstVisiblePosition=" + getFirstVisiblePosition());
        }
    }

    static {
        COUI_DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public COUITouchListView(Context context) {
        this(context, null);
    }

    private boolean canSelect(View view) {
        return view != null && (view.getBackground() instanceof COUIMaskEffectDrawable) && view.isEnabled();
    }

    private boolean cancelLastItemSelect(MotionEvent event, int moveDirection) {
        View selectedChild = getChildAt(this.mLastTouchTarget - getFirstVisiblePosition());
        if (canSelect(selectedChild)) {
            dispatchTargetEvent(selectedChild, event, MotionEvent.ACTION_CANCEL);
            ((COUIMaskEffectDrawable) selectedChild.getBackground()).setTouchSelectExited();
            this.mDividerAnimationController.dividerAnim(true, moveDirection);
        }
        this.mLastTouchTarget = -1;
        return true;
    }

    private void createCOUIScrollDelegate() {
        this.mCOUIScrollBar = new COUIScrollBar.Builder(this).marginTop(this.mScrollbarVerticalPadding).marginBottom(this.mScrollbarVerticalPadding).build();
    }

    private void dispatchTargetEvent(View view, MotionEvent sourceEvent, int action) {
        this.mChildRectTemp = new Rect();
        this.mParentRectTemp = new Rect();
        getChildVisibleRect(view, this.mChildRectTemp, null);
        getLocalVisibleRect(this.mParentRectTemp);
        int offsetX = this.mChildRectTemp.left - this.mParentRectTemp.left;
        int offsetY = this.mChildRectTemp.top - this.mParentRectTemp.top;
        MotionEvent childEvent = MotionEvent.obtain(sourceEvent);
        childEvent.setSource(ACTION_IS_FROM_TOUCH_LISTVIEW);
        childEvent.setLocation(sourceEvent.getX() - offsetX, sourceEvent.getY() - offsetY);
        childEvent.setAction(action);
        view.dispatchTouchEvent(childEvent);
        childEvent.recycle();
    }

    private void performHapticFeedback() {
        if (this.mIsNeedVibrate) {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
        }
    }

    public void allowDispatchEvent(boolean allow) {
        this.mAllowDispatchEvent = allow;
    }

    public void allowScroll(boolean allow) {
        this.mAllowScroll = allow;
    }

    @Override
    public boolean awakenScrollBars() {
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        return scrollBar != null ? scrollBar.awakenScrollBars(COUIScrollBar.SCROLLER_FADE_TIMEOUT) : super.awakenScrollBars();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar != null) {
            scrollBar.dispatchDrawOver(canvas);
        }
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        if (this.mAllowDispatchEvent) {
            return super.dispatchHoverEvent(motionEvent);
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View selectedChild;
        if (!this.mAllowDispatchEvent) {
            return false;
        }
        if (!this.mAllowScroll && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            return true;
        }
        if (canScrollVertically(CAN_SCROLL_DOWN) || canScrollVertically(CAN_SCROLL_UP)) {
            this.mIsDynamicSelection = false;
        } else {
            this.mIsDynamicSelection = true;
        }
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        if (COUI_DEBUG) {
            Log.d(TAG, "dispatchTouchEvent action=" + MotionEvent.actionToString(action)
                    + ", actionIndex=" + actionIndex + ", pointerCount=" + event.getPointerCount());
        }
        if (action == MotionEvent.ACTION_DOWN) {
            this.mDownY = touchY;
            this.mInTalkbackMode = COUIAccessibilityUtil.isTalkbackEnabled(getContext());
            int touchedPosition = pointToPosition(touchX, touchY);
            this.mLastTouchTarget = touchedPosition;
            this.mDownView = getChildAt(touchedPosition - getFirstVisiblePosition());
            if (canSelect(this.mDownView)) {
                this.mDividerAnimationController.dividerAnimOut();
                ((COUIMaskEffectDrawable) this.mDownView.getBackground()).setTouchEntered();
            }
        } else if (action == MotionEvent.ACTION_UP) {
            int touchedPosition = this.mLastTouchTarget;
            if ((touchedPosition != INVALID_POSITION && !this.mInTalkbackMode)
                    || this.mLastMotion == MotionEvent.ACTION_DOWN) {
                View touchedChild = getChildAt(touchedPosition - getFirstVisiblePosition());
                if (touchedChild != null) {
                    COUILog.d(TAG, "target=" + touchedChild + ", position=" + touchedPosition);
                    performItemClick(touchedChild, touchedPosition, getItemIdAtPosition(touchedPosition));
                    dispatchTargetEvent(touchedChild, event, MotionEvent.ACTION_UP);
                }
                this.mDividerAnimationController.dividerAnim(true, 0);
                this.mLastTouchTarget = -1;
                this.mLastMotion = action;
                return false;
            }
            this.mDividerAnimationController.dividerAnim(true, 0);
            this.mLastTouchTarget = -1;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (this.mLastTouchTarget != INVALID_POSITION && !this.mIsDynamicSelection
                    && Math.abs(touchY - this.mDownY) > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                    && canSelect(this.mDownView)) {
                ((COUIMaskEffectDrawable) this.mDownView.getBackground()).setTouchExited();
                this.mDividerAnimationController.dividerAnim(true, 0);
                this.mLastTouchTarget = -1;
            }
            int touchedPosition = pointToPosition(touchX, touchY);
            if (touchedPosition == INVALID_POSITION || event.getPointerCount() > 1 || this.mInTalkbackMode) {
                this.mLastMotion = action;
                return cancelLastItemSelect(event, DividerAnimationController.MOVE_FLAG_OUT);
            }
            selectedChild = getChildAt(touchedPosition - getFirstVisiblePosition());
            if (touchedPosition != this.mLastTouchTarget && DefaultAdapter.isDataIndex(touchedPosition)
                    && this.mIsDynamicSelection && selectedChild != null) {
                int moveDirection = canSelect(selectedChild) ? touchedPosition - this.mLastTouchTarget : 0;
                cancelLastItemSelect(event, moveDirection);
                if (canSelect(selectedChild)) {
                    dispatchTargetEvent(selectedChild, event, MotionEvent.ACTION_DOWN);
                    ((COUIMaskEffectDrawable) selectedChild.getBackground()).setTouchSelectEntered();
                    performHapticFeedback();
                    this.mLastTouchTarget = touchedPosition;
                    this.mDividerAnimationController.dividerAnimOut();
                }
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            cancelLastItemSelect(event, DividerAnimationController.MOVE_FLAG_OUT);
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            this.mLastMotion = action;
            return cancelLastItemSelect(event, DividerAnimationController.MOVE_FLAG_OUT);
        }
        this.mLastMotion = action;
        return super.dispatchTouchEvent(event);
    }

    @Override
    public COUIScrollBar getCOUIScrollDelegate() {
        return this.mCOUIScrollBar;
    }

    @Override
    public View getCOUIScrollableView() {
        return this;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar != null) {
            scrollBar.onAttachedToWindow();
        } else {
            createCOUIScrollDelegate();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar != null) {
            scrollBar.release();
            this.mCOUIScrollBar = null;
        }
        this.mDividerAnimationController.release();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar != null && scrollBar.onInterceptTouchEvent(motionEvent)) {
            return true;
        }
        if (this.mIsDynamicSelection) {
            return false;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar == null || !scrollBar.onTouchEvent(motionEvent)) {
            return super.onTouchEvent(motionEvent);
        }
        return true;
    }

    @Override
    public void onVisibilityChanged(View view, int visibility) {
        super.onVisibilityChanged(view, visibility);
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar != null) {
            scrollBar.onVisibilityChanged(view, visibility);
        }
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        COUIScrollBar scrollBar = this.mCOUIScrollBar;
        if (scrollBar != null) {
            scrollBar.onWindowVisibilityChanged(visibility);
        }
    }

    public void setForceStopDividerAnimation(boolean forceStop) {
        this.mDividerAnimationController.mForceStopDividerAnimation = forceStop;
    }

    public void setIsNeedVibrate(boolean needVibrate) {
        this.mIsNeedVibrate = needVibrate;
    }

    public void setItemHeightMap(List<Integer> itemHeightMap, int totalHeight) {
        this.mItemHeightMap = itemHeightMap;
        this.mTotalHeight = totalHeight;
    }

    @Override
    public void setNewCOUIScrollDelegate(COUIScrollBar scrollBar) {
        if (scrollBar == null) {
            throw new IllegalArgumentException("setNewFastScrollDelegate must NOT be NULL.");
        }
        this.mCOUIScrollBar = scrollBar;
        scrollBar.onAttachedToWindow();
    }

    @Override
    public int superComputeVerticalScrollExtent() {
        return getHeight();
    }

    @Override
    public int superComputeVerticalScrollOffset() {
        return this.mScrollY;
    }

    @Override
    public int superComputeVerticalScrollRange() {
        return this.mTotalHeight;
    }

    @Override
    public void superOnTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
    }

    public COUITouchListView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUITouchListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUITouchListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mAllowDispatchEvent = true;
        this.mAllowScroll = true;
        this.mIsNeedVibrate = true;
        this.mIsDynamicSelection = true;
        this.mTotalHeight = 0;
        this.mScrollY = 0;
        this.mLastMotion = -1;
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_fade_edge_length));
        this.mScrollbarVerticalPadding = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_scrollbar_vertical_padding);
        this.mDividerAnimationController = new DividerAnimationController();
        setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                if (COUITouchListView.this.mItemHeightMap != null) {
                    int scrollY = COUITouchListView.this.mItemHeightMap.get(firstVisibleItem);
                    View firstChild = listView.getChildAt(0);
                    if (firstChild != null) {
                        scrollY = (scrollY - firstChild.getHeight()) - firstChild.getTop();
                    }
                    COUITouchListView.this.mScrollY = scrollY;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView listView, int scrollState) {
                COUITouchListView.this.mDividerAnimationController.onScrollStateChanged(scrollState);
            }
        });
        createCOUIScrollDelegate();
        setDefaultFocusHighlightEnabled(false);
    }
}
