package com.oplus.graphics;

import android.graphics.Outline;
import android.graphics.Rect;

public class OplusOutlineAdapter {
    public static final int NEW_OUTLINE_SMOOTH = 1;
    public static final int OLD_OUTLINE_SMOOTH = 0;

    private final IOplusOutline mOutline;

    public OplusOutlineAdapter(Outline outline, int styleType) {
        if (styleType == OLD_OUTLINE_SMOOTH) {
            mOutline = new OplusOutline(outline);
        } else if (styleType == NEW_OUTLINE_SMOOTH) {
            mOutline = new OplusOutlineSmooth(outline);
        } else {
            throw new IllegalArgumentException("Invalid flag: " + styleType);
        }
    }

    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius, float weight) {
        mOutline.setSmoothRoundRect(left, top, right, bottom, radius, weight);
    }

    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius) {
        mOutline.setSmoothRoundRect(left, top, right, bottom, radius);
    }

    public void setSmoothRoundRect(Rect rect, float radius, float weight) {
        mOutline.setSmoothRoundRect(rect, radius, weight);
    }

    public void setSmoothRoundRect(Rect rect, float radius) {
        mOutline.setSmoothRoundRect(rect, radius);
    }
}
