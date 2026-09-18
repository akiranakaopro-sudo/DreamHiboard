package com.oplus.animation;

import android.view.View;

public final class OplusAsyncAnimatorUtils {
    private OplusAsyncAnimatorUtils() {
    }

    public static boolean setAlpha(View view, float alpha) {
        view.setAlpha(alpha);
        return true;
    }

    public static boolean setScaleX(View view, float scaleX) {
        view.setScaleX(scaleX);
        return true;
    }

    public static boolean setScaleY(View view, float scaleY) {
        view.setScaleY(scaleY);
        return true;
    }
}
