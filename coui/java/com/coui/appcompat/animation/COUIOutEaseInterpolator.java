package com.coui.appcompat.animation;

import android.view.animation.PathInterpolator;

public class COUIOutEaseInterpolator extends PathInterpolator {
    public COUIOutEaseInterpolator() {
        super(0.3f, 0.0f, 1.0f, 1.0f);
    }
}
