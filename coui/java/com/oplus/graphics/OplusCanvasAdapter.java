package com.oplus.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class OplusCanvasAdapter {
    public static final int OLD_CANVAS_SMOOTH = 0;
    public static final int NEW_CANVAS_SMOOTH = 1;

    private final IOplusCanvas mCanvas;

    public OplusCanvasAdapter(Canvas canvas, int styleType) {
        if (styleType != OLD_CANVAS_SMOOTH && styleType != NEW_CANVAS_SMOOTH) {
            throw new IllegalArgumentException("Invalid flag: " + styleType);
        }
        mCanvas = new OplusCanvas(canvas);
    }

    public void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Paint paint, float weight) {
        mCanvas.drawSmoothRoundRect(left, top, right, bottom, rx, ry, paint, weight);
    }

    public void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Paint paint) {
        mCanvas.drawSmoothRoundRect(left, top, right, bottom, rx, ry, paint);
    }

    public void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint, float weight) {
        mCanvas.drawSmoothRoundRect(rect, rx, ry, paint, weight);
    }

    public void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint) {
        mCanvas.drawSmoothRoundRect(rect, rx, ry, paint);
    }
}
