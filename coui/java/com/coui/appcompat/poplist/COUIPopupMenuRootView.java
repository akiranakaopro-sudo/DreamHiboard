package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.poplist.BasePopupMenuAnimationController;


public class COUIPopupMenuRootView extends FrameLayout {
    private static final boolean DEBUG_DRAW;
    private static final String TAG = "COUIPopupMenuRootView";
    private BasePopupMenuAnimationController mController;
    private final Paint mDebugPaint;
    private BasePopupMenuAnimationController mDefaultScreenController;
    private PopupMenuDomain mDomain;
    private final Rect mDrawingRect;
    private int mMainMenuHeight;
    private ViewGroup mMainMenuRootView;
    private int mMainMenuWidth;
    private OnMenuStateChangedListener mMenuStateChangedListener;
    private boolean mNeedReDispatchDownForNextEvent;
    private final BasePopupMenuAnimationController.OnMenuStateChangedListener mOnSubMenuStateChangedListener;
    private Runnable mPendingDismissRunnable;
    private Runnable mShowSubMenuAfterAnimationSkipped;
    private BasePopupMenuAnimationController mSmallScreenController;
    private View.OnClickListener mSubMenuHeaderClickListener;
    private int mSubMenuHeight;
    private ViewGroup mSubMenuRootView;
    private int mSubMenuWidth;


    public class AnonymousClass1 implements BasePopupMenuAnimationController.OnMenuStateChangedListener {
        private final View.OnClickListener mCancelSubMenuEnterAndStartExit = new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIPopupMenuRootView.AnonymousClass1.this.onCancelSubMenuEnterAndStartExit(view);
            }
        };
        private final View.OnClickListener mCancelSubMenuExitAndStartEnter = new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIPopupMenuRootView.AnonymousClass1.this.onCancelSubMenuExitAndStartEnter(view);
            }
        };

        public AnonymousClass1() {
        }


        public void onCancelSubMenuEnterAndStartExit(View view) {
            COUIPopupMenuRootView.this.hideSubMenu(true);
            COUIPopupMenuRootView.this.mMainMenuRootView.setOnClickListener(null);
        }


        public void onCancelSubMenuExitAndStartEnter(View view) {
            COUIPopupMenuRootView.this.showSubMenu();
        }

        @Override
        public void onMainMenuAnimationCanceled() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onMainMenuAnimationCanceled();
            }
        }

        @Override
        public void onMainMenuEntered() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onMainMenuEntered();
            }
        }

        @Override
        public void onMainMenuExited() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onMainMenuExited();
            }
        }

        @Override
        public void onMainMenuStartToEnter() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onMainMenuStartToEnter();
            }
        }

        @Override
        public void onMainMenuStartToExit() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onMainMenuStartToExit();
            }
        }

        @Override
        public void onSubMenuAnimationCanceled() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onSubMenuAnimationCanceled();
            }
        }

        @Override
        public void onSubMenuEntered() {
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onSubMenuEntered();
            }
            if (COUIPopupMenuRootView.this.mSubMenuRootView instanceof RoundFrameLayout) {
                ((RoundFrameLayout) COUIPopupMenuRootView.this.mSubMenuRootView).clearOverrideOutline();
            }
        }

        @Override
        public void onSubMenuExited() {
            COUIPopupMenuRootView.this.mNeedReDispatchDownForNextEvent = false;
            if (COUIPopupMenuRootView.this.mSubMenuRootView instanceof RoundFrameLayout) {
                ((RoundFrameLayout) COUIPopupMenuRootView.this.mSubMenuRootView).setAllowDispatchEvent(true);
            }
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onSubMenuExited();
            }
            COUIPopupMenuRootView cOUIPopupMenuRootView = COUIPopupMenuRootView.this;
            cOUIPopupMenuRootView.allowListViewScroll(cOUIPopupMenuRootView.mMainMenuRootView, true);
            COUIPopupMenuRootView.this.configSubMenuHeaderOnClick(null);
            COUIPopupMenuRootView.this.removeSubMenuView();
            if (COUIPopupMenuRootView.this.mShowSubMenuAfterAnimationSkipped != null) {
                Runnable runnable = COUIPopupMenuRootView.this.mShowSubMenuAfterAnimationSkipped;
                COUIPopupMenuRootView.this.mShowSubMenuAfterAnimationSkipped = null;
                runnable.run();
            }
        }

        @Override
        public void onSubMenuStartToEnter() {
            COUIPopupMenuRootView.this.mNeedReDispatchDownForNextEvent = false;
            if (COUIPopupMenuRootView.this.mSubMenuRootView instanceof RoundFrameLayout) {
                ((RoundFrameLayout) COUIPopupMenuRootView.this.mSubMenuRootView).setAllowDispatchEvent(true);
            }
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onSubMenuStartToEnter();
            }
            if (COUIPopupMenuRootView.this.mMainMenuRootView != null) {
                COUIPopupMenuRootView cOUIPopupMenuRootView = COUIPopupMenuRootView.this;
                cOUIPopupMenuRootView.allowListViewDispatchTouchEvent(cOUIPopupMenuRootView.mMainMenuRootView, false);
                COUIPopupMenuRootView cOUIPopupMenuRootView2 = COUIPopupMenuRootView.this;
                cOUIPopupMenuRootView2.allowListViewScroll(cOUIPopupMenuRootView2.mMainMenuRootView, false);
                COUIPopupMenuRootView cOUIPopupMenuRootView3 = COUIPopupMenuRootView.this;
                cOUIPopupMenuRootView3.allowListViewScroll(cOUIPopupMenuRootView3.mSubMenuRootView, true);
                COUIPopupMenuRootView.this.configSubMenuHeaderOnClick(this.mCancelSubMenuEnterAndStartExit);
                COUIPopupMenuRootView.this.mMainMenuRootView.setOnClickListener(this.mCancelSubMenuEnterAndStartExit);
            }
        }

        @Override
        public void onSubMenuStartToExit() {
            COUIPopupMenuRootView.this.mNeedReDispatchDownForNextEvent = true;
            if (COUIPopupMenuRootView.this.mSubMenuRootView instanceof RoundFrameLayout) {
                ((RoundFrameLayout) COUIPopupMenuRootView.this.mSubMenuRootView).setAllowDispatchEvent(false);
            }
            if (COUIPopupMenuRootView.this.mMenuStateChangedListener != null) {
                COUIPopupMenuRootView.this.mMenuStateChangedListener.onSubMenuStartToExit();
            }
            if (COUIPopupMenuRootView.this.mMainMenuRootView != null) {
                COUIPopupMenuRootView.this.mMainMenuRootView.setFocusable(false);
                COUIPopupMenuRootView.this.mMainMenuRootView.setClickable(false);
                COUIPopupMenuRootView.this.mMainMenuRootView.setOnClickListener(null);
                COUIPopupMenuRootView cOUIPopupMenuRootView = COUIPopupMenuRootView.this;
                cOUIPopupMenuRootView.allowListViewDispatchTouchEvent(cOUIPopupMenuRootView.mMainMenuRootView, true);
                COUIPopupMenuRootView cOUIPopupMenuRootView2 = COUIPopupMenuRootView.this;
                cOUIPopupMenuRootView2.allowListViewScroll(cOUIPopupMenuRootView2.mSubMenuRootView, false);
                COUIPopupMenuRootView.this.configSubMenuHeaderOnClick(this.mCancelSubMenuExitAndStartEnter);
            }
        }
    }

    public interface OnMenuStateChangedListener {
        default void onMainMenuAnimationCanceled() {
        }

        default void onMainMenuEntered() {
        }

        default void onMainMenuExited() {
        }

        default void onMainMenuStartToEnter() {
        }

        default void onMainMenuStartToExit() {
        }

        default void onSubMenuAnimationCanceled() {
        }

        default void onSubMenuEntered() {
        }

        default void onSubMenuExited() {
        }

        default void onSubMenuStartToEnter() {
        }

        default void onSubMenuStartToExit() {
        }
    }

    static {
        DEBUG_DRAW = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public COUIPopupMenuRootView(Context context) {
        this(context, null);
    }


    public void allowListViewDispatchTouchEvent(ViewGroup viewGroup, boolean flag) {
        if (viewGroup != null) {
            View childAt = viewGroup.getChildAt(0);
            if (childAt instanceof COUITouchListView) {
                ((COUITouchListView) childAt).allowDispatchEvent(flag);
            }
        }
    }


    public void allowListViewScroll(ViewGroup viewGroup, boolean flag) {
        if (viewGroup != null) {
            View childAt = viewGroup.getChildAt(0);
            if (childAt instanceof COUITouchListView) {
                ((COUITouchListView) childAt).allowScroll(flag);
            }
        }
    }


    public void configSubMenuHeaderOnClick(View.OnClickListener onClickListener) {
        this.mSubMenuHeaderClickListener = onClickListener;
    }

    public void addMainMenuView(ViewGroup viewGroup) {
        ViewGroup view = this.mMainMenuRootView;
        if (view != null) {
            removeView(view);
        }
        if (this.mSubMenuRootView != null) {
            hideSubMenu(false);
        }
        this.mMainMenuRootView = viewGroup;
        addView(viewGroup, new ViewGroup.LayoutParams(-2, -2));
        allowListViewScroll(this.mMainMenuRootView, true);
        this.mController.setMainMenuView(this.mMainMenuRootView);
        this.mController.setMenuRootView(this);
        this.mController.setOnSubMenuStateChangedListener(this.mOnSubMenuStateChangedListener);
    }

    public void addSubMenuView(ViewGroup viewGroup) {
        ViewGroup view = this.mSubMenuRootView;
        if (view != null) {
            removeView(view);
        }
        this.mSubMenuRootView = viewGroup;
        viewGroup.setTranslationZ(1.0f);
        addView(this.mSubMenuRootView, new ViewGroup.LayoutParams(-2, -2));
        allowListViewScroll(this.mSubMenuRootView, true);
        this.mController.setSubMenuView(this.mSubMenuRootView);
        showSubMenu();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mNeedReDispatchDownForNextEvent) {
            this.mNeedReDispatchDownForNextEvent = false;
            MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
            motionEventObtain.setAction(3);
            super.dispatchTouchEvent(motionEventObtain);
            motionEventObtain.recycle();
            if (motionEvent.getActionMasked() == 0) {
                return super.dispatchTouchEvent(motionEvent);
            }
            MotionEvent motionEventObtain2 = MotionEvent.obtain(motionEvent);
            motionEventObtain2.setAction(0);
            super.dispatchTouchEvent(motionEventObtain2);
            motionEventObtain2.recycle();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public void hideMainMenu(boolean flag) {
        BasePopupMenuAnimationController basePopupMenuAnimationController = this.mController;
        if (basePopupMenuAnimationController == null) {
            return;
        }
        if (flag) {
            basePopupMenuAnimationController.startMainMenuExit();
        } else {
            basePopupMenuAnimationController.startMainMenuExit(false);
        }
    }

    public void hideSubMenu(boolean flag) {
        ViewGroup viewGroup = this.mSubMenuRootView;
        if (viewGroup != null) {
            if (!flag) {
                this.mController.startSubMenuExit(false);
                return;
            }
            View childAt = viewGroup.getChildAt(0);
            if (childAt instanceof COUITouchListView) {
                ((COUITouchListView) childAt).smoothScrollToPosition(0);
            }
            this.mController.startSubMenuExit();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setVisibility(0);
        ViewGroup viewGroup = this.mMainMenuRootView;
        if (viewGroup == null || this.mDomain == null) {
            return;
        }
        viewGroup.setVisibility(8);
        this.mController.startMainMenuEnter();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeDelayedDismiss();
        this.mController.stopAllAnimation();
        this.mMainMenuRootView.setFocusable(false);
        this.mMainMenuRootView.setClickable(false);
        this.mMainMenuRootView.setOnClickListener(null);
        allowListViewDispatchTouchEvent(this.mMainMenuRootView, true);
        allowListViewScroll(this.mSubMenuRootView, false);
        configSubMenuHeaderOnClick(null);
        removeSubMenuView();
        this.mShowSubMenuAfterAnimationSkipped = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (DEBUG_DRAW) {
            ViewGroup viewGroup = this.mMainMenuRootView;
            if (viewGroup != null) {
                viewGroup.setAlpha(0.5f);
            }
            ViewGroup view = this.mSubMenuRootView;
            if (view != null) {
                view.setAlpha(0.5f);
            }
            this.mDebugPaint.setColor(Color.parseColor("#33FF0000"));
            canvas.save();
            this.mDomain.getAvailableRect(this.mDrawingRect);
            canvas.clipOutRect(this.mDrawingRect);
            canvas.drawRect(this.mDomain.mWindow, this.mDebugPaint);
            canvas.restore();
            this.mDebugPaint.setColor(Color.parseColor("#330000FF"));
            canvas.save();
            this.mDrawingRect.set(this.mDomain.mAnchor);
            canvas.clipOutRect(this.mDrawingRect);
            this.mDomain.getAnchorRealRect(this.mDrawingRect);
            canvas.drawRect(this.mDrawingRect, this.mDebugPaint);
            canvas.restore();
            this.mDebugPaint.setColor(Color.parseColor("#3300FF00"));
            this.mDrawingRect.set(this.mDomain.mAnchor);
            canvas.drawRect(this.mDrawingRect, this.mDebugPaint);
            this.mDebugPaint.setColor(Color.parseColor("#33FF00FF"));
            this.mDrawingRect.set(this.mDomain.mMainMenu);
            canvas.drawRect(this.mDrawingRect, this.mDebugPaint);
            this.mDebugPaint.setColor(Color.parseColor("#33FFFF00"));
            this.mDrawingRect.set(this.mDomain.mSubMenuAnchor);
            canvas.drawRect(this.mDrawingRect, this.mDebugPaint);
            this.mDebugPaint.setColor(Color.parseColor("#3300FFFF"));
            this.mDrawingRect.set(this.mDomain.mMainMenuRelocated);
            canvas.drawRect(this.mDrawingRect, this.mDebugPaint);
            this.mDebugPaint.setColor(Color.parseColor("#33000000"));
            this.mDrawingRect.set(this.mDomain.mSubMenu);
            canvas.drawRect(this.mDrawingRect, this.mDebugPaint);
        }
    }

    @Override
    public void onLayout(boolean changed, int left_2, int top_2, int right_2, int bottom_2) {
        ViewGroup viewGroup = this.mMainMenuRootView;
        if (viewGroup != null) {
            Rect rect = this.mDomain.mMainMenu;
            viewGroup.layout(rect.left, rect.top, rect.right, rect.bottom);
        }
        ViewGroup view = this.mSubMenuRootView;
        if (view != null) {
            Rect rect_2 = this.mDomain.mSubMenu;
            view.layout(rect_2.left, rect_2.top, rect_2.right, rect_2.bottom);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup viewGroup = this.mMainMenuRootView;
        if (viewGroup != null) {
            viewGroup.measure(View.MeasureSpec.makeMeasureSpec(this.mMainMenuWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMainMenuHeight, 1073741824));
        }
        ViewGroup view = this.mSubMenuRootView;
        if (view != null) {
            view.measure(View.MeasureSpec.makeMeasureSpec(this.mSubMenuWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mSubMenuHeight, 1073741824));
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    public void performSubMenuHeader(View view) {
        View.OnClickListener onClickListener = this.mSubMenuHeaderClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public void postDelayedDismiss(Runnable runnable, long timestamp) {
        removeDelayedDismiss();
        this.mPendingDismissRunnable = runnable;
        if (runnable != null) {
            postDelayed(runnable, timestamp);
        }
    }

    public void postSkipExitAnimationAndShowSubMenu(Runnable runnable) {
        this.mShowSubMenuAfterAnimationSkipped = runnable;
    }

    public void removeDelayedDismiss() {
        Runnable runnable = this.mPendingDismissRunnable;
        if (runnable != null) {
            removeCallbacks(runnable);
            this.mPendingDismissRunnable = null;
        }
    }

    public void removeSubMenuView() {
        ViewGroup viewGroup = this.mSubMenuRootView;
        if (viewGroup != null) {
            removeView(viewGroup);
            this.mSubMenuRootView = null;
            this.mController.detach();
            this.mController.setSubMenuView(null);
            this.mNeedReDispatchDownForNextEvent = true;
        }
    }

    public void setDomain(PopupMenuDomain popupMenuDomain) {
        this.mDomain = popupMenuDomain;
        if (COUIResponsiveUtils.isSmallScreen(getContext(), this.mDomain.mWindow.width())) {
            if (this.mSmallScreenController == null) {
                this.mSmallScreenController = new SmallScreenAnimationController(getContext());
            }
            this.mController = this.mSmallScreenController;
        } else {
            if (this.mDefaultScreenController == null) {
                this.mDefaultScreenController = new DefaultScreenAnimationController();
            }
            this.mController = this.mDefaultScreenController;
        }
        this.mController.setDomain(this.mDomain);
        invalidate();
    }

    public void setEnableRenderThreadAnimation(boolean enableRenderThreadAnimation) {
        BasePopupMenuAnimationController basePopupMenuAnimationController = this.mController;
        if (basePopupMenuAnimationController != null) {
            basePopupMenuAnimationController.setEnableRenderThreadAnimation(enableRenderThreadAnimation);
        }
    }

    public void setMainMenuSize(int width, int height) {
        this.mMainMenuWidth = width;
        this.mMainMenuHeight = height;
    }

    public void setOnSubMenuStateChangedListener(OnMenuStateChangedListener onMenuStateChangedListener) {
        this.mMenuStateChangedListener = onMenuStateChangedListener;
    }

    public void setSubMenuSize(int width, int height) {
        this.mSubMenuWidth = width;
        this.mSubMenuHeight = height;
    }

    public void showMainMenu() {
        BasePopupMenuAnimationController basePopupMenuAnimationController = this.mController;
        if (basePopupMenuAnimationController == null) {
            return;
        }
        basePopupMenuAnimationController.startMainMenuEnter();
        if (this.mSubMenuRootView != null) {
            hideSubMenu(true);
        }
    }

    public void showSubMenu() {
        this.mController.startSubMenuEnter();
    }

    public COUIPopupMenuRootView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIPopupMenuRootView(Context context, AttributeSet attributeSet, int index) {
        this(context, attributeSet, index, 0);
    }

    public COUIPopupMenuRootView(Context context, AttributeSet attributeSet, int index, int index_2) {
        super(context, attributeSet, index, index_2);
        this.mNeedReDispatchDownForNextEvent = false;
        this.mShowSubMenuAfterAnimationSkipped = null;
        this.mPendingDismissRunnable = null;
        this.mSubMenuHeaderClickListener = null;
        this.mOnSubMenuStateChangedListener = new AnonymousClass1();
        this.mMainMenuRootView = null;
        this.mSubMenuRootView = null;
        this.mMainMenuWidth = 0;
        this.mMainMenuHeight = 0;
        this.mSubMenuWidth = 0;
        this.mSubMenuHeight = 0;
        this.mDebugPaint = new Paint(1);
        this.mDrawingRect = new Rect();
        if (DEBUG_DRAW) {
            setWillNotDraw(false);
        }
        setFocusable(false);
        setLayerType(2, null);
    }
}
