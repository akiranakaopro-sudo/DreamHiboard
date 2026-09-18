package com.coui.appcompat.reddot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.coui.appcompat.R;

public class COUIRedDotDrawable extends Drawable {
    private final COUIHintRedDotHelper mCOUIHintRedDotHelper;
    private final int mPointMode;
    private final int mPointNumber;
    private final RectF mRectF;

    public COUIRedDotDrawable(int pointMode, int pointNumber, Context context, RectF rectF) {
        mPointMode = pointMode;
        mPointNumber = pointNumber;
        mRectF = rectF;
        mCOUIHintRedDotHelper = new COUIHintRedDotHelper(
                context,
                null,
                R.styleable.COUIHintRedDot,
                0,
                R.style.Widget_COUI_COUIHintRedDot_Small);
    }

    @Override
    public void draw(Canvas canvas) {
        mCOUIHintRedDotHelper.drawRedPoint(canvas, mPointMode, String.valueOf(mPointNumber), mRectF);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }
}
