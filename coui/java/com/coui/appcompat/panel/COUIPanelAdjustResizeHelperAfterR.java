package com.coui.appcompat.panel;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import androidx.dynamicanimation.animation.FloatValueHolder;


public class COUIPanelAdjustResizeHelperAfterR extends COUIAbsPanelAdjustResizeHelper {
    private static final float DEFAULT_SPRING_BOUNCE = 0.0f;
    private static final float DEFAULT_SPRING_RESPONSE = 0.3f;
    private static final String TAG = "AdjustResizeAfterR";
    private int mAnimMaxMargin;
    private int mAnimStartBottomMargin;
    private int mAnimStartOffset;
    private int mAnimStartPaddingBottom;
    private int mAnimTargetBottomMargin;
    private int mAnimTargetOffset;
    private int mAnimTargetPaddingBottom;
    private View mLastBottomDesignSheetOrPanelContentLayout;
    private View mLastCouiPanelContentLayout;
    private COUISpringAnimation mOffsetSpringAnimation;
    private FloatValueHolder mOffsetValueHolder;
    private int mWindowType = 2;
    private int mCurrentKeyboardHeight = 0;
    private int mBottomMarginOfDesignBottomSheet = 0;

    private void doMarginBottomAnim(int maxMargin, int startOffset, int targetOffset) {
        this.mAnimMaxMargin = maxMargin;
        this.mAnimStartOffset = startOffset;
        this.mAnimTargetOffset = targetOffset;
        int paddingBottom = this.mLastCouiPanelContentLayout.getPaddingBottom();
        int startBottomMargin = Math.max(0, startOffset - paddingBottom);
        int targetBottomMargin = Math.min(targetOffset, maxMargin);
        int targetPaddingBottom = Math.max(targetOffset - maxMargin, 0);
        this.mAnimStartBottomMargin = startBottomMargin;
        this.mAnimStartPaddingBottom = paddingBottom;
        this.mAnimTargetBottomMargin = targetBottomMargin;
        this.mAnimTargetPaddingBottom = targetPaddingBottom;
        FloatValueHolder offsetValueHolder = this.mOffsetValueHolder;
        if (offsetValueHolder == null) {
            this.mOffsetValueHolder = new FloatValueHolder(startOffset);
        } else {
            offsetValueHolder.setValue(startOffset);
        }
        COUISpringAnimation offsetSpringAnimation = this.mOffsetSpringAnimation;
        if (offsetSpringAnimation == null) {
            COUISpringAnimation startValue = new COUISpringAnimation(this.mOffsetValueHolder).setSpring(new COUISpringForce(targetOffset).setBounce(DEFAULT_SPRING_BOUNCE).setResponse(DEFAULT_SPRING_RESPONSE)).setStartValue(startOffset);
            this.mOffsetSpringAnimation = startValue;
            startValue.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public final void onAnimationUpdate(COUIDynamicAnimation animation, float value, float velocity) {
                    COUIPanelAdjustResizeHelperAfterR.this.onMarginBottomAnimUpdate(animation, value, velocity);
                }
            });
        } else {
            offsetSpringAnimation.setStartValue(startOffset);
            this.mOffsetSpringAnimation.getSpring().setFinalPosition(targetOffset);
            this.mAnimStartOffset = startOffset;
            this.mAnimTargetOffset = targetOffset;
        }
        if (this.mOffsetSpringAnimation.isRunning()) {
            return;
        }
        this.mOffsetSpringAnimation.start();
    }

    private int getCurrentBottomMargin(View view) {
        if (view == null) {
            return 0;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            return ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return 0;
    }

    private int getCurrentBottomOffset(View view, View contentView) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        return (layoutParams instanceof ViewGroup.MarginLayoutParams ? ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin : 0) + contentView.getPaddingBottom();
    }


    public void onMarginBottomAnimUpdate(COUIDynamicAnimation animation, float value, float velocity) {
        View contentLayout = this.mLastCouiPanelContentLayout;
        View bottomSheetOrPanelContentLayout = this.mLastBottomDesignSheetOrPanelContentLayout;
        if (bottomSheetOrPanelContentLayout == null || contentLayout == null) {
            return;
        }
        int targetOffset = this.mAnimTargetOffset;
        int startOffset = this.mAnimStartOffset;
        float progress = targetOffset != startOffset ? Math.max(0.0f, Math.min(1.0f, (value - startOffset) / (targetOffset - startOffset))) : 1.0f;
        int bottomMargin = Math.max(0, Math.min(this.mAnimStartBottomMargin + ((int) ((this.mAnimTargetBottomMargin - this.mAnimStartBottomMargin) * progress)), this.mAnimMaxMargin));
        contentLayout.setPadding(0, 0, 0, Math.max(0, this.mAnimStartPaddingBottom + ((int) ((this.mAnimTargetPaddingBottom - this.mAnimStartPaddingBottom) * progress))));
        ViewGroup.LayoutParams layoutParams = bottomSheetOrPanelContentLayout.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = bottomMargin;
            bottomSheetOrPanelContentLayout.setLayoutParams(layoutParams);
        }
    }

    private void resetPaddingAndMargin() {
        View view = this.mLastBottomDesignSheetOrPanelContentLayout;
        if (view != null) {
            if (this.mAnimTargetPaddingBottom == 0 && this.mAnimTargetBottomMargin == 0) {
                return;
            }
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = this.mBottomMarginOfDesignBottomSheet;
                this.mLastBottomDesignSheetOrPanelContentLayout.setLayoutParams(layoutParams);
            }
            View contentLayout = this.mLastCouiPanelContentLayout;
            if (contentLayout != null) {
                contentLayout.setPadding(0, 0, 0, 0);
            }
        }
    }

    private void setMarginBottomTo(View bottomSheetOrPanelContentLayout, int keyboardHeight, WindowInsets windowInsets, View rootView) {
        int expectedContentHeight;
        if (bottomSheetOrPanelContentLayout != null) {
            View couiPanelContentLayout = rootView.findViewById(com.coui.appcompat.R.id.coui_panel_content_layout);
            if (couiPanelContentLayout == null) {
                Log.e(TAG, "couiPanelContentLayout is null");
                return;
            }
            int availableHeight = rootView.getMeasuredHeight();
            int contentHeight = bottomSheetOrPanelContentLayout.getMeasuredHeight() - couiPanelContentLayout.getPaddingBottom();
            if (keyboardHeight > availableHeight * 0.9f) {
                Log.e(TAG, "KeyboardHeight > availableHeight * 0.9f, so not elevated");
                return;
            }
            Context context = bottomSheetOrPanelContentLayout.getContext();
            View designBottomSheet = rootView.findViewById(com.coui.appcompat.R.id.design_bottom_sheet);
            this.mBottomMarginOfDesignBottomSheet = COUIPanelMultiWindowUtils.getPanelMarginBottom(context, context.getResources().getConfiguration(), windowInsets, designBottomSheet instanceof COUIPanelPercentFrameLayout ? ((COUIPanelPercentFrameLayout) designBottomSheet).isIsHandlePanel() : false, isCouiPanelEdgeToEdgeEnable());
            int maxMargin = Math.max(keyboardHeight > 0 ? (availableHeight <= 0 || contentHeight <= 0 || (expectedContentHeight = contentHeight + keyboardHeight) <= availableHeight) ? keyboardHeight : keyboardHeight - (expectedContentHeight - availableHeight) : getCurrentBottomMargin(bottomSheetOrPanelContentLayout), this.mBottomMarginOfDesignBottomSheet);
            if (keyboardHeight == 0) {
                keyboardHeight = this.mBottomMarginOfDesignBottomSheet;
            }
            int currentBottomOffset = getCurrentBottomOffset(bottomSheetOrPanelContentLayout, couiPanelContentLayout);
            if (currentBottomOffset == keyboardHeight && this.mLastBottomDesignSheetOrPanelContentLayout == bottomSheetOrPanelContentLayout && this.mLastCouiPanelContentLayout == couiPanelContentLayout) {
                Log.w(TAG, "currentBottomOffset == targetBottomOffset, skip animation");
                return;
            }
            this.mLastBottomDesignSheetOrPanelContentLayout = bottomSheetOrPanelContentLayout;
            this.mLastCouiPanelContentLayout = couiPanelContentLayout;
            COUISpringAnimation offsetSpringAnimation = this.mOffsetSpringAnimation;
            if (offsetSpringAnimation == null || !offsetSpringAnimation.isRunning()) {
                doMarginBottomAnim(maxMargin, currentBottomOffset, keyboardHeight);
                return;
            }
            FloatValueHolder offsetValueHolder = this.mOffsetValueHolder;
            int currentAnimatedOffset = (int) (offsetValueHolder != null ? offsetValueHolder.getValue() : currentBottomOffset);
            this.mAnimMaxMargin = maxMargin;
            this.mAnimStartOffset = currentAnimatedOffset;
            this.mAnimTargetOffset = keyboardHeight;
            this.mAnimStartBottomMargin = getCurrentBottomMargin(bottomSheetOrPanelContentLayout);
            this.mAnimStartPaddingBottom = couiPanelContentLayout.getPaddingBottom();
            this.mAnimTargetBottomMargin = Math.min(keyboardHeight, maxMargin);
            this.mAnimTargetPaddingBottom = Math.max(keyboardHeight - maxMargin, 0);
            FloatValueHolder updatedOffsetValueHolder = this.mOffsetValueHolder;
            if (updatedOffsetValueHolder != null) {
                updatedOffsetValueHolder.setValue(currentAnimatedOffset);
            }
            this.mOffsetSpringAnimation.animateToFinalPosition(keyboardHeight);
        }
    }

    @Override
    public void adjustResize(Context context, ViewGroup viewGroup, WindowInsets windowInsets, View view, boolean isShowKeyboard) {
        if (isShowKeyboard) {
            int keyboardHeight = Math.max(0, windowInsets.getInsets(WindowInsets.Type.ime()).bottom - windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom);
            if (keyboardHeight != this.mCurrentKeyboardHeight) {
                this.mCurrentKeyboardHeight = keyboardHeight;
                setMarginBottomTo(viewGroup, keyboardHeight, windowInsets, view);
            } else {
                Log.w(TAG, "keyboardHeight is the same size, keyboardHeight :" + keyboardHeight);
            }
        }
    }

    @Override
    public int getMarginBottomValue() {
        return -1;
    }

    @Override
    public int getPaddingBottomOffset() {
        return -1;
    }

    @Override
    public float getTranslateOffset() {
        return -1.0f;
    }

    @Override
    public int getWindowType() {
        return this.mWindowType;
    }

    @Override
    public void recoveryScrollingParentViewPaddingBottom(COUIPanelContentLayout contentLayout) {
        if (contentLayout != null) {
            contentLayout.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public boolean releaseData() {
        resetInnerStatus();
        COUISpringAnimation offsetSpringAnimation = this.mOffsetSpringAnimation;
        if (offsetSpringAnimation != null && offsetSpringAnimation.isRunning()) {
            this.mOffsetSpringAnimation.cancel();
            this.mOffsetSpringAnimation = null;
        }
        this.mOffsetValueHolder = null;
        resetPaddingAndMargin();
        this.mCurrentKeyboardHeight = 0;
        this.mBottomMarginOfDesignBottomSheet = 0;
        this.mLastBottomDesignSheetOrPanelContentLayout = null;
        this.mLastCouiPanelContentLayout = null;
        return true;
    }

    @Override
    public void resetInnerStatus() {
    }

    @Override
    public void setIgnoreHideKeyboardAnim(boolean ignoreHideKeyboardAnim) {
    }

    @Override
    public void setWindowType(int windowType) {
        this.mWindowType = windowType;
    }
}
