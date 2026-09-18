package com.oplus.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class OplusCanvas implements IOplusCanvas {
    private final Canvas mCanvas;

    public OplusCanvas(Canvas canvas) {
        mCanvas = canvas;
    }

    @Override
    public void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Paint paint, float weight) {
        mCanvas.drawRoundRect(left, top, right, bottom, rx, ry, paint);
    }

    @Override
    public void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Paint paint) {
        mCanvas.drawRoundRect(left, top, right, bottom, rx, ry, paint);
    }

    @Override
    public void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint, float weight) {
        drawSmoothRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint, weight);
    }

    @Override
    public void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint) {
        drawSmoothRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint);
    }
}
