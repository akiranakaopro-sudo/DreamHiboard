package com.coui.appcompat.cardview;

import android.graphics.drawable.Drawable;
import android.view.View;

interface CardViewDelegate {
    Drawable getCardBackground();

    View getCardView();

    boolean getPreventCornerOverlap();

    boolean getUseCompatPadding();

    void setCardBackground(Drawable drawable);

    void setMinWidthHeightInternal(int width, int height);

    void setShadowPadding(int left, int top, int right, int bottom);
}
