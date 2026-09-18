package com.coui.appcompat.preference;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.coui.appcompat.roundRect.COUIRoundRectUtil;
import com.google.android.material.shape.MaterialShapeDrawable;


public class COUIRecommendedDrawable extends MaterialShapeDrawable {
    private int mColor;
    private Paint mPaint = new Paint(1);
    private Path mPath = new Path();
    private float mRadius;

    public COUIRecommendedDrawable(float radius, int color) {
        mRadius = radius;
        mColor = color;
        mPaint.setColor(mColor);
    }

    @Override
    public void draw(Canvas canvas) {
        mPath.reset();
        Path path = COUIRoundRectUtil.getInstance().getPath(getBounds(), mRadius);
        mPath = path;
        canvas.drawPath(path, mPaint);
    }
}
