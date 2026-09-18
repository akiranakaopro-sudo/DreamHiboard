package com.coui.appcompat.panel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.core.view.ScrollingView;

import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.buttonBar.COUIButtonBarLayout;
import com.coui.appcompat.uiutil.UIUtil;

import java.lang.ref.WeakReference;


public class COUIPanelAdjustResizeHelperBeforeR extends COUIAbsPanelAdjustResizeHelper {
    private static final float DISMISS_HEIGHT_ANIM_DURATION_COEFFICIENT = 50.0f;
    private static final float DISMISS_HEIGHT_ANIM_DURATION_INITIAL_VALUE = 200.0f;
    private static final int IME_ADJUST = 1;
    private static final int IME_HIDE = 2;
    private static final int IME_SHOW = 0;
    private static final float SHOW_HEIGHT_ANIM_DURATION_COEFFICIENT = 120.0f;
    private static final float SHOW_HEIGHT_ANIM_DURATION_INITIAL_VALUE = 300.0f;
    private ValueAnimator mBottomButtonBarAnim;
    private int mMarginBottomValue;
    private ValueAnimator mPaddingBottomAnim;
    private WeakReference<View> mPaddingBottomAnimView;
    private int mPaddingBottomOffset;
    private float mTranslateOffset;
    private static final Interpolator SHOW_HEIGHT_ANIM_INTERPOLATOR = new COUIInEaseInterpolator();
    private static final Interpolator DISMISS_HEIGHT_ANIM_INTERPOLATOR = new LinearInterpolator();
    private int mWindowType = 2;
    private int mAdjustResizeType = 2;
    private int mAdjustKeyboardStartHeight = 0;
    private int mAdjustKeyboardHeight = 0;
    private int mAdjustKeyboardOffset = 0;
    private int mFocusViewRawY = 0;
    private boolean mIsIgnoreHideKeyboardAnim = true;
    private boolean mIsKeyboardShow = false;
    private boolean mIsFocusViewDisplayInVerticalScrolledView = false;
    private View mFocusVerticalScrolledView = null;

    private void adjustResizeBeforeR(ViewGroup viewGroup, boolean isShowKeyboard, int keyboardHeight) {
        updateAdjustKeyboardType(isShowKeyboard);
        updateAdjustKeyboardData(viewGroup, keyboardHeight);
        updateAdjustKeyboardOffset(viewGroup, Boolean.valueOf(isShowKeyboard));
        doAdjustKeyboardAnim(viewGroup, isShowKeyboard);
        this.mIsIgnoreHideKeyboardAnim = false;
    }

    private void doAdjustKeyboardAnim(ViewGroup viewGroup, boolean isShowKeyboard) {
        if (viewGroup == null || this.mPaddingBottomAnimView == null) {
            return;
        }
        if (!(viewGroup instanceof COUIPanelContentLayout)) {
            int screenHeightRealSize = UIUtil.getScreenHeightRealSize(viewGroup.getContext());
            doMarginBottomAnim(viewGroup, this.mMarginBottomValue, (long) (isShowKeyboard ? Math.abs((this.mAdjustKeyboardOffset * SHOW_HEIGHT_ANIM_DURATION_COEFFICIENT) / screenHeightRealSize) + SHOW_HEIGHT_ANIM_DURATION_INITIAL_VALUE : Math.abs((this.mAdjustKeyboardOffset * DISMISS_HEIGHT_ANIM_DURATION_COEFFICIENT) / screenHeightRealSize) + DISMISS_HEIGHT_ANIM_DURATION_INITIAL_VALUE));
            return;
        }
        COUIPanelContentLayout contentLayout = (COUIPanelContentLayout) viewGroup;
        int maxHeight = contentLayout.getMaxHeight();
        long duration = (long) (isShowKeyboard ? Math.abs((this.mAdjustKeyboardOffset * SHOW_HEIGHT_ANIM_DURATION_COEFFICIENT) / maxHeight) + SHOW_HEIGHT_ANIM_DURATION_INITIAL_VALUE : Math.abs((this.mAdjustKeyboardOffset * DISMISS_HEIGHT_ANIM_DURATION_COEFFICIENT) / maxHeight) + DISMISS_HEIGHT_ANIM_DURATION_INITIAL_VALUE);
        doPaddingBottomAnim(this.mPaddingBottomAnimView.get(), this.mPaddingBottomOffset, duration);
        doBottomButtonTranslateAnim(contentLayout, this.mTranslateOffset, duration);
    }

    private void doBottomButtonTranslateAnim(final COUIPanelContentLayout contentLayout, float translateOffset, long duration) {
        if (translateOffset == 0.0f || contentLayout == null || contentLayout.getBtnBarLayout() == null) {
            return;
        }
        float translationY = contentLayout.getBtnBarLayout().getTranslationY();
        final float targetTranslationY = Math.min(0.0f, translateOffset + translationY);
        ValueAnimator bottomButtonBarAnim = ValueAnimator.ofFloat(translationY, targetTranslationY);
        this.mBottomButtonBarAnim = bottomButtonBarAnim;
        bottomButtonBarAnim.setDuration(duration);
        if (translationY < targetTranslationY) {
            this.mBottomButtonBarAnim.setInterpolator(SHOW_HEIGHT_ANIM_INTERPOLATOR);
        } else {
            this.mBottomButtonBarAnim.setInterpolator(DISMISS_HEIGHT_ANIM_INTERPOLATOR);
        }
        this.mBottomButtonBarAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                contentLayout.getBtnBarLayout().setTranslationY(targetTranslationY);
                contentLayout.getDivider().setTranslationY(targetTranslationY);
            }
        });
        this.mBottomButtonBarAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (contentLayout.isAttachedToWindow()) {
                    float animatedTranslationY = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    contentLayout.getBtnBarLayout().setTranslationY(animatedTranslationY);
                    contentLayout.getDivider().setTranslationY(animatedTranslationY);
                }
            }
        });
        this.mBottomButtonBarAnim.start();
    }

    private void doMarginBottomAnim(final View view, int marginBottomOffset, long duration) {
        if (marginBottomOffset == 0 || view == null) {
            return;
        }
        int startBottomMargin = Math.max(0, COUIViewMarginUtil.getMargin(view, 3));
        final int targetBottomMargin = Math.max(0, marginBottomOffset + startBottomMargin);
        ValueAnimator marginBottomAnim = ValueAnimator.ofInt(startBottomMargin, targetBottomMargin);
        marginBottomAnim.setDuration(duration);
        if (startBottomMargin < targetBottomMargin) {
            marginBottomAnim.setInterpolator(SHOW_HEIGHT_ANIM_INTERPOLATOR);
        } else {
            marginBottomAnim.setInterpolator(DISMISS_HEIGHT_ANIM_INTERPOLATOR);
        }
        marginBottomAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                COUIViewMarginUtil.setMargin(view, targetBottomMargin, 3);
            }
        });
        marginBottomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (view.isAttachedToWindow()) {
                    COUIViewMarginUtil.setMargin(view, ((Integer) valueAnimator.getAnimatedValue()).intValue(), 3);
                }
            }
        });
        marginBottomAnim.start();
    }

    private void doPaddingBottomAnim(final View view, int paddingBottomOffset, long duration) {
        if (paddingBottomOffset == 0 || view == null) {
            return;
        }
        final int paddingLeft = view.getPaddingLeft();
        final int paddingRight = view.getPaddingRight();
        final int paddingTop = view.getPaddingTop();
        int startPaddingBottom = Math.max(0, view.getPaddingBottom());
        final int targetPaddingBottom = Math.max(0, paddingBottomOffset + startPaddingBottom);
        ValueAnimator paddingBottomAnim = ValueAnimator.ofInt(startPaddingBottom, targetPaddingBottom);
        this.mPaddingBottomAnim = paddingBottomAnim;
        paddingBottomAnim.setDuration(duration);
        if (startPaddingBottom < targetPaddingBottom) {
            this.mPaddingBottomAnim.setInterpolator(SHOW_HEIGHT_ANIM_INTERPOLATOR);
        } else {
            this.mPaddingBottomAnim.setInterpolator(DISMISS_HEIGHT_ANIM_INTERPOLATOR);
        }
        this.mPaddingBottomAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setPadding(paddingLeft, paddingTop, paddingRight, targetPaddingBottom);
            }
        });
        this.mPaddingBottomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (view.isAttachedToWindow()) {
                    view.setPadding(paddingLeft, paddingTop, paddingRight, ((Integer) valueAnimator.getAnimatedValue()).intValue());
                }
            }
        });
        this.mPaddingBottomAnim.start();
    }

    private void findFocusView(ViewGroup viewGroup) {
        View focusView;
        if (viewGroup == null || (focusView = viewGroup.findFocus()) == null) {
            return;
        }
        this.mFocusViewRawY = 0;
        this.mIsFocusViewDisplayInVerticalScrolledView = false;
        this.mFocusVerticalScrolledView = null;
        if (isScrollable(focusView)) {
            this.mIsFocusViewDisplayInVerticalScrolledView = true;
            this.mFocusVerticalScrolledView = focusView;
        }
        this.mFocusViewRawY = getMeasureHeight(focusView) + focusView.getTop() + COUIViewMarginUtil.getMargin(focusView, 3);
        for (View view = (View) focusView.getParent(); view != null && view != viewGroup.getParent(); view = (View) view.getParent()) {
            if (isScrollable(view)) {
                this.mIsFocusViewDisplayInVerticalScrolledView = true;
                this.mFocusVerticalScrolledView = view;
            }
            this.mFocusViewRawY += view.getTop();
        }
    }

    private int getKeyboardHeightBeforeR(int systemWindowInsetBottom, int navigationBarHeight) {
        return this.mWindowType == 2038 ? systemWindowInsetBottom : systemWindowInsetBottom - navigationBarHeight;
    }

    private int getMeasureHeight(View view) {
        if (view == null || view.getVisibility() == View.GONE) {
            return 0;
        }
        int measuredHeight = view.getMeasuredHeight();
        if (measuredHeight != 0) {
            return measuredHeight;
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredHeight();
    }

    private boolean isScrollable(View view) {
        return (view instanceof ScrollView) || (view instanceof AbsListView) || (view instanceof ScrollingView);
    }

    private boolean updateAdjustKeyboardData(ViewGroup viewGroup, int keyboardHeight) {
        if (viewGroup == null) {
            return false;
        }
        releaseData();
        if (viewGroup instanceof COUIPanelContentLayout) {
            COUIPanelContentLayout contentLayout = (COUIPanelContentLayout) viewGroup;
            viewGroup.measure(View.MeasureSpec.makeMeasureSpec(viewGroup.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(contentLayout.getMaxHeight(), contentLayout.getLayoutAtMaxHeight() ? View.MeasureSpec.EXACTLY : View.MeasureSpec.AT_MOST));
            findFocusView(viewGroup);
        }
        int measuredHeight = viewGroup.getMeasuredHeight();
        this.mAdjustKeyboardStartHeight = measuredHeight;
        int adjustResizeType = this.mAdjustResizeType;
        if (adjustResizeType == 0) {
            this.mAdjustKeyboardHeight = keyboardHeight;
            this.mAdjustKeyboardOffset = keyboardHeight;
        } else if (adjustResizeType == 1) {
            this.mAdjustKeyboardStartHeight = measuredHeight - keyboardHeight;
            this.mAdjustKeyboardOffset = keyboardHeight - this.mAdjustKeyboardHeight;
            this.mAdjustKeyboardHeight = keyboardHeight;
        } else if (adjustResizeType == 2 && !this.mIsIgnoreHideKeyboardAnim) {
            this.mAdjustKeyboardHeight = keyboardHeight;
            this.mAdjustKeyboardOffset = keyboardHeight;
        }
        return true;
    }

    private void updateAdjustKeyboardOffset(ViewGroup viewGroup, Boolean isShowKeyboard) {
        this.mPaddingBottomAnimView = null;
        this.mPaddingBottomOffset = 0;
        this.mTranslateOffset = 0.0f;
        this.mMarginBottomValue = 0;
        if (viewGroup == null || this.mAdjustKeyboardOffset == 0) {
            return;
        }
        if (viewGroup instanceof COUIPanelContentLayout) {
            updateOffsetInConstraintLayout((COUIPanelContentLayout) viewGroup, isShowKeyboard);
        } else {
            updateOffsetInNormalLayout(viewGroup, isShowKeyboard);
        }
    }

    private void updateAdjustKeyboardType(boolean isShowKeyboard) {
        this.mAdjustResizeType = 2;
        boolean wasKeyboardShow = this.mIsKeyboardShow;
        if (!wasKeyboardShow && isShowKeyboard) {
            this.mAdjustResizeType = 0;
        } else if (wasKeyboardShow && isShowKeyboard) {
            this.mAdjustResizeType = 1;
        }
        this.mIsKeyboardShow = isShowKeyboard;
    }

    private void updateOffsetInConstraintLayout(COUIPanelContentLayout contentLayout, Boolean isShowKeyboard) {
        int offsetDirection = this.mAdjustResizeType == 2 ? -1 : 1;
        int maxHeight = contentLayout.getMaxHeight();
        int signedKeyboardOffset = this.mAdjustKeyboardOffset * offsetDirection;
        float translationY = contentLayout.getBtnBarLayout() != null ? contentLayout.getBtnBarLayout().getTranslationY() : 0.0f;
        this.mPaddingBottomAnimView = new WeakReference<>(contentLayout);
        if ((this.mIsFocusViewDisplayInVerticalScrolledView && maxHeight != 0) || (!COUIPanelMultiWindowUtils.isPortrait(contentLayout.getContext()) && translationY == 0.0f)) {
            View view = this.mFocusVerticalScrolledView;
            if (view != null) {
                View scrollingParent = (View) view.getParent();
                if (scrollingParent != null) {
                    this.mPaddingBottomAnimView = new WeakReference<>(scrollingParent);
                }
                this.mTranslateOffset = -signedKeyboardOffset;
            }
            this.mPaddingBottomOffset = signedKeyboardOffset;
            return;
        }
        int focusBottomSpace = this.mAdjustKeyboardStartHeight - this.mFocusViewRawY;
        int paddingBottom = contentLayout.getPaddingBottom();
        int buttonBarHeight = contentLayout.getBtnBarLayout() != null ? contentLayout.getBtnBarLayout().getHeight() : 0;
        int dividerHeight = contentLayout.getDivider() != null ? contentLayout.getDivider().getHeight() : 0;
        int adjustResizeType = this.mAdjustResizeType;
        if (adjustResizeType == 1) {
            focusBottomSpace += this.mAdjustKeyboardHeight;
        } else if (adjustResizeType == 2) {
            focusBottomSpace -= this.mAdjustKeyboardHeight;
        }
        int keyboardHeight = this.mAdjustKeyboardHeight;
        if (focusBottomSpace >= keyboardHeight + buttonBarHeight + dividerHeight && paddingBottom == 0) {
            this.mTranslateOffset = -signedKeyboardOffset;
            return;
        }
        int requiredPaddingOffset = offsetDirection * (((keyboardHeight + buttonBarHeight) + dividerHeight) - focusBottomSpace);
        this.mPaddingBottomOffset = Math.max(-paddingBottom, requiredPaddingOffset);
        if (this.mAdjustResizeType != 1) {
            this.mTranslateOffset = isShowKeyboard.booleanValue() ? -(signedKeyboardOffset - this.mPaddingBottomOffset) : -translationY;
            return;
        }
        int targetPaddingBottom = Math.max(0, paddingBottom + requiredPaddingOffset);
        int currentKeyboardHeight = this.mAdjustKeyboardHeight;
        this.mTranslateOffset = (-Math.min(currentKeyboardHeight, Math.max(-currentKeyboardHeight, currentKeyboardHeight - targetPaddingBottom))) - translationY;
    }

    private void updateOffsetInNormalLayout(ViewGroup viewGroup, Boolean isShowKeyboard) {
        int marginBottomOffset = (this.mAdjustResizeType == 2 ? -1 : 1) * this.mAdjustKeyboardOffset;
        this.mPaddingBottomAnimView = new WeakReference<>(viewGroup);
        this.mMarginBottomValue = marginBottomOffset;
    }

    @Override
    public void adjustResize(Context context, ViewGroup viewGroup, WindowInsets windowInsets, View view, boolean isShowKeyboard) {
        if (viewGroup == null || !isShowKeyboard) {
            return;
        }
        int keyboardHeightBeforeR = getKeyboardHeightBeforeR(windowInsets.getSystemWindowInsetBottom(), (!COUINavigationBarUtil.isNavigationBarShow(context) || context.getResources().getBoolean(com.coui.appcompat.R.bool.is_ignore_nav_height_in_panel_ime_adjust)) ? 0 : COUINavigationBarUtil.getNavigationBarHeight(context));
        if (keyboardHeightBeforeR > 0) {
            adjustResizeBeforeR(viewGroup, true, keyboardHeightBeforeR);
            return;
        }
        if (this.mAdjustResizeType != 2) {
            adjustResizeBeforeR(viewGroup, false, this.mAdjustKeyboardHeight);
        }
        View designBottomSheet = view.findViewById(com.coui.appcompat.R.id.design_bottom_sheet);
        int panelMarginBottom = COUIPanelMultiWindowUtils.getPanelMarginBottom(viewGroup.getContext(), viewGroup.getContext().getResources().getConfiguration(), windowInsets, designBottomSheet instanceof COUIPanelPercentFrameLayout ? ((COUIPanelPercentFrameLayout) designBottomSheet).isIsHandlePanel() : false, isCouiPanelEdgeToEdgeEnable());
        ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = panelMarginBottom;
            viewGroup.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getMarginBottomValue() {
        return this.mMarginBottomValue;
    }

    @Override
    public int getPaddingBottomOffset() {
        return this.mPaddingBottomOffset;
    }

    @Override
    public float getTranslateOffset() {
        return this.mTranslateOffset;
    }

    @Override
    public int getWindowType() {
        return this.mWindowType;
    }

    @Override
    public void recoveryScrollingParentViewPaddingBottom(COUIPanelContentLayout contentLayout) {
        if (contentLayout != null) {
            COUIButtonBarLayout btnBarLayout = contentLayout.getBtnBarLayout();
            View divider = contentLayout.getDivider();
            if (btnBarLayout != null) {
                btnBarLayout.setTranslationY(0.0f);
            }
            if (divider != null) {
                divider.setTranslationY(0.0f);
            }
            contentLayout.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public boolean releaseData() {
        ValueAnimator paddingBottomAnim = this.mPaddingBottomAnim;
        boolean canceledPaddingAnim = false;
        if (paddingBottomAnim != null) {
            if (paddingBottomAnim.isRunning()) {
                this.mPaddingBottomAnim.cancel();
                canceledPaddingAnim = true;
            }
            this.mPaddingBottomAnim = null;
        }
        ValueAnimator bottomButtonBarAnim = this.mBottomButtonBarAnim;
        if (bottomButtonBarAnim != null) {
            if (bottomButtonBarAnim.isRunning()) {
                this.mBottomButtonBarAnim.cancel();
            }
            this.mBottomButtonBarAnim = null;
        }
        return canceledPaddingAnim;
    }

    @Override
    public void resetInnerStatus() {
        this.mAdjustKeyboardHeight = 0;
    }

    @Override
    public void setIgnoreHideKeyboardAnim(boolean ignoreHideKeyboardAnim) {
        this.mIsIgnoreHideKeyboardAnim = ignoreHideKeyboardAnim;
    }

    @Override
    public void setWindowType(int windowType) {
        this.mWindowType = windowType;
    }
}
