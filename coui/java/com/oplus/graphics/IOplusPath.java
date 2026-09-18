package com.oplus.graphics;

import android.graphics.Path;
import android.graphics.RectF;

interface IOplusPath {
    void addSmoothRoundRect(float left, float top, float right, float bottom, float rx, float ry,
            float weight, Path.Direction dir);

    void addSmoothRoundRect(float left, float top, float right, float bottom, float rx, float ry,
            Path.Direction dir);

    void addSmoothRoundRect(RectF rect, float rx, float ry, Path.Direction dir);

    void addSmoothRoundRect(RectF rect, float rx, float ry, float weight, Path.Direction dir);

    void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir, float weight);

    void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir);
}
