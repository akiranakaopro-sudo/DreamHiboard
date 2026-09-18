package com.oplus.graphics;

import android.graphics.Path;
import android.graphics.RectF;

public class OplusPathAdapter {
    public static final int OLD_PATH_SMOOTH = 0;
    public static final int NEW_PATH_SMOOTH = 1;

    private final IOplusPath mPath;

    public OplusPathAdapter(Path path, int styleType) {
        if (styleType == OLD_PATH_SMOOTH || styleType == NEW_PATH_SMOOTH) {
            mPath = new OplusPathFallback(path);
            return;
        }
        throw new IllegalArgumentException("Invalid flag: " + styleType);
    }

    public void addSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, float weight, Path.Direction dir) {
        mPath.addSmoothRoundRect(left, top, right, bottom, rx, ry, weight, dir);
    }

    public void addSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Path.Direction dir) {
        mPath.addSmoothRoundRect(left, top, right, bottom, rx, ry, dir);
    }

    public void addSmoothRoundRect(RectF rect, float rx, float ry, Path.Direction dir) {
        mPath.addSmoothRoundRect(rect, rx, ry, dir);
    }

    public void addSmoothRoundRect(RectF rect, float rx, float ry, float weight, Path.Direction dir) {
        mPath.addSmoothRoundRect(rect, rx, ry, weight, dir);
    }

    public void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir, float weight) {
        mPath.addSmoothRoundRect(rect, radii, dir, weight);
    }

    public void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir) {
        mPath.addSmoothRoundRect(rect, radii, dir);
    }
}
