package com.oplus.graphics;

import android.graphics.Paint;
import android.graphics.RectF;

interface IOplusCanvas {
    void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx, float ry,
            Paint paint, float weight);

    void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx, float ry,
            Paint paint);

    void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint, float weight);

    void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint);
}
