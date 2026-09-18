package com.coui.appcompat.input;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

class InnerShadowHelper {
    List<Paint> mShadowLayerPaints = new ArrayList<>();
    List<Path> mShadowLayerPaths = new ArrayList<>();
    int mViewHeight;
    int mViewWidth;

    public InnerShadowHelper(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
    }

    public void addInnerShadowLayer(float radius, float dx, float dy, int shadowColor, int color, float strokeWidth, Path path) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setShadowLayer(radius, dx, dy, shadowColor);
        mShadowLayerPaints.add(paint);
        mShadowLayerPaths.add(path);
    }

    public Bitmap createInnerShadowBitmap() {
        int width = mViewWidth;
        if (width <= 0 || mViewHeight <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, mViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0);
        int saveLayer = canvas.saveLayer(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight(), null);
        for (int i = 0; i < mShadowLayerPaths.size(); i++) {
            if (mShadowLayerPaths.get(i) != null && mShadowLayerPaints.get(i) != null) {
                canvas.clipPath(mShadowLayerPaths.get(i));
                canvas.drawPath(mShadowLayerPaths.get(i), mShadowLayerPaints.get(i));
            }
        }
        canvas.restoreToCount(saveLayer);
        return bitmap;
    }

    public void reset() {
        if (mShadowLayerPaints != null) {
            mShadowLayerPaints.clear();
        }
        if (mShadowLayerPaths != null) {
            mShadowLayerPaths.clear();
        }
    }

    public void setInnerShadowBitmapSize(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
    }
}
