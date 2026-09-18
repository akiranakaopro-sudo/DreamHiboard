package com.oplus.wrapper.view;

import android.graphics.Region;

public class ViewTreeObserver {
    private final android.view.ViewTreeObserver mViewTreeObserver;

    public ViewTreeObserver(android.view.ViewTreeObserver viewTreeObserver) {
        this.mViewTreeObserver = viewTreeObserver;
    }

    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
    }

    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
    }

    public interface OnComputeInternalInsetsListener {
        void onComputeInternalInsets(InternalInsetsInfo internalInsetsInfo);
    }

    public static class InternalInsetsInfo {
        public static final int TOUCHABLE_INSETS_REGION = 3;
        private final Region mTouchableRegion = new Region();

        public void setTouchableInsets(int val) {
        }

        public Region getTouchableRegion() {
            return this.mTouchableRegion;
        }
    }

    public interface OnPreDrawListener extends android.view.ViewTreeObserver.OnPreDrawListener {
    }
}
