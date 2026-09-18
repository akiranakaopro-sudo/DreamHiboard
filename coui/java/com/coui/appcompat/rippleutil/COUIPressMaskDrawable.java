package com.coui.appcompat.rippleutil;

import android.graphics.Canvas;

import com.coui.appcompat.roundRect.COUIRoundDrawable;

public class COUIPressMaskDrawable extends COUIRoundDrawable {
    private int mCornerRadius;

    public COUIPressMaskDrawable(int cornerRadius) {
        mCornerRadius = cornerRadius;
    }

    @Override
    public void draw(Canvas canvas) {
        setRadius(mCornerRadius);
        super.draw(canvas);
    }
}
