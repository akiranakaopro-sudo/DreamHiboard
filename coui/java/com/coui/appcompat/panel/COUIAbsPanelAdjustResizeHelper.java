package com.coui.appcompat.panel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;


public abstract class COUIAbsPanelAdjustResizeHelper {
    private boolean mCouiPanelEdgeToEdgeEnable;

    public void adjustResize(Context context, ViewGroup viewGroup, WindowInsets windowInsets, View view, boolean isShowKeyboard) {
    }

    public int getMarginBottomValue() {
        return -1;
    }

    public int getPaddingBottomOffset() {
        return -1;
    }

    public float getTranslateOffset() {
        return -1.0f;
    }

    public int getWindowType() {
        return -1;
    }

    public boolean isCouiPanelEdgeToEdgeEnable() {
        return this.mCouiPanelEdgeToEdgeEnable;
    }

    public void recoveryScrollingParentViewPaddingBottom(COUIPanelContentLayout contentLayout) {
    }

    public boolean releaseData() {
        return false;
    }

    public void resetInnerStatus() {
    }

    public void setCouiPanelEdgeToEdgeEnable(boolean couiPanelEdgeToEdgeEnable) {
        this.mCouiPanelEdgeToEdgeEnable = couiPanelEdgeToEdgeEnable;
    }

    public void setIgnoreHideKeyboardAnim(boolean ignoreHideKeyboardAnim) {
    }

    public void setWindowType(int windowType) {
    }
}
