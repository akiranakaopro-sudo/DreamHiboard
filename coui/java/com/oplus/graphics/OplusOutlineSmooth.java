package com.oplus.graphics;

import android.graphics.Outline;
import android.graphics.Rect;

class OplusOutlineSmooth implements IOplusOutline {
    private static final float FULL_WEIGHT_DEFAULT = 1.1f;

    private final Outline mOutline;
    private final OplusOutline.OplusOutlineReflector mReflector;

    OplusOutlineSmooth(Outline outline) {
        mOutline = outline;
        mReflector = new OplusOutline.OplusOutlineReflector(outline);
    }

    @Override
    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius, float weight) {
        if (!mReflector.callSmoothRoundRect("setSmoothRoundRect16", left, top, right, bottom,
                radius, weight)) {
            mOutline.setRoundRect(left, top, right, bottom, radius);
        }
    }

    @Override
    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius) {
        setSmoothRoundRect(left, top, right, bottom, radius, FULL_WEIGHT_DEFAULT);
    }

    @Override
    public void setSmoothRoundRect(Rect rect, float radius, float weight) {
        setSmoothRoundRect(rect.left, rect.top, rect.right, rect.bottom, radius, weight);
    }

    @Override
    public void setSmoothRoundRect(Rect rect, float radius) {
        setSmoothRoundRect(rect.left, rect.top, rect.right, rect.bottom, radius);
    }
}
