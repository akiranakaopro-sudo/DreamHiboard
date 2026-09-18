package com.coui.component.responsiveui.window;

import android.content.Context;
import com.coui.component.responsiveui.unit.Dp;

public final class LayoutGridWindowSize {
    private int width;
    private int height;

    public LayoutGridWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public LayoutGridWindowSize(LayoutGridWindowSize windowSize) {
        this(windowSize.width, windowSize.height);
    }

    public LayoutGridWindowSize(Context context, Dp width, Dp height) {
        this((int) width.toPixel(context), (int) height.toPixel(context));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "(width = " + width + ", height = " + height + ')';
    }
}
