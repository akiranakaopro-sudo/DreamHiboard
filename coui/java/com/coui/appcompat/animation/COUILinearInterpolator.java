package com.coui.appcompat.animation;

import android.view.animation.Interpolator;

public class COUILinearInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        return input;
    }
}
