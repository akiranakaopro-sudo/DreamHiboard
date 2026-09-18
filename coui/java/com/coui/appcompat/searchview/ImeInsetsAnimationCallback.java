package com.coui.appcompat.searchview;

import android.view.WindowInsets;

import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public final class ImeInsetsAnimationCallback extends WindowInsetsAnimationCompat.Callback {
    private OnImeAnimationListener imeListener;

    public interface OnImeAnimationListener {
        void onImeAnimStart();
    }

    public ImeInsetsAnimationCallback() {
        this(DISPATCH_MODE_STOP);
    }

    public ImeInsetsAnimationCallback(int dispatchMode) {
        super(dispatchMode);
    }

    public OnImeAnimationListener getImeListener() {
        return imeListener;
    }

    @Override
    public WindowInsetsCompat onProgress(
            WindowInsetsCompat insets,
            List<WindowInsetsAnimationCompat> runningAnimations
    ) {
        return insets;
    }

    @Override
    public WindowInsetsAnimationCompat.BoundsCompat onStart(
            WindowInsetsAnimationCompat animation,
            WindowInsetsAnimationCompat.BoundsCompat bounds
    ) {
        if ((animation.getTypeMask() & WindowInsets.Type.ime()) != 0 && imeListener != null) {
            imeListener.onImeAnimStart();
        }
        return super.onStart(animation, bounds);
    }

    public void setImeAnimationListener(OnImeAnimationListener listener) {
        this.imeListener = listener;
    }

    public void setImeListener(OnImeAnimationListener listener) {
        this.imeListener = listener;
    }
}
