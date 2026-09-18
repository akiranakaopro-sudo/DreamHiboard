package com.coui.appcompat.panel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;


public class COUIPanelAdjustResizeHelper {
    private final COUIAbsPanelAdjustResizeHelper mAdjustHelper = new COUIPanelAdjustResizeHelperAfterR();

    public void adjustResize(Context context, ViewGroup viewGroup, WindowInsets windowInsets, View view, boolean isShowKeyboard) {
        mAdjustHelper.adjustResize(context, viewGroup, windowInsets, view, isShowKeyboard);
    }

    public int getMarginBottomValue() {
        return mAdjustHelper.getMarginBottomValue();
    }

    public int getPaddingBottomOffset() {
        return mAdjustHelper.getPaddingBottomOffset();
    }

    public float getTranslateOffset() {
        return mAdjustHelper.getTranslateOffset();
    }

    public int getWindowType() {
        return mAdjustHelper.getWindowType();
    }

    public void recoveryScrollingParentViewPaddingBottom(COUIPanelContentLayout contentLayout) {
        mAdjustHelper.recoveryScrollingParentViewPaddingBottom(contentLayout);
    }

    public boolean releaseData() {
        return mAdjustHelper.releaseData();
    }

    public void resetInnerStatus() {
        mAdjustHelper.resetInnerStatus();
    }

    public void setCouiPanelEdgeToEdgeEnable(boolean couiPanelEdgeToEdgeEnable) {
        mAdjustHelper.setCouiPanelEdgeToEdgeEnable(couiPanelEdgeToEdgeEnable);
    }

    public void setIgnoreHideKeyboardAnim(boolean ignoreHideKeyboardAnim) {
        mAdjustHelper.setIgnoreHideKeyboardAnim(ignoreHideKeyboardAnim);
    }

    public void setWindowType(int windowType) {
        mAdjustHelper.setWindowType(windowType);
    }
}
