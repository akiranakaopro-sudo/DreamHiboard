package com.oplus.graphics;

import android.graphics.Path;
import android.graphics.RectF;

class OplusPathFallback implements IOplusPath {
    private final Path mPath;

    OplusPathFallback(Path path) {
        mPath = path;
    }

    @Override
    public void addSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, float weight, Path.Direction dir) {
        mPath.addRoundRect(left, top, right, bottom, rx, ry, dir);
    }

    @Override
    public void addSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Path.Direction dir) {
        mPath.addRoundRect(left, top, right, bottom, rx, ry, dir);
    }

    @Override
    public void addSmoothRoundRect(RectF rect, float rx, float ry, Path.Direction dir) {
        mPath.addRoundRect(rect, rx, ry, dir);
    }

    @Override
    public void addSmoothRoundRect(RectF rect, float rx, float ry, float weight,
            Path.Direction dir) {
        mPath.addRoundRect(rect, rx, ry, dir);
    }

    @Override
    public void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir, float weight) {
        mPath.addRoundRect(rect, radii, dir);
    }

    @Override
    public void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir) {
        mPath.addRoundRect(rect, radii, dir);
    }
}
