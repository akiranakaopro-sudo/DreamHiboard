package com.coui.appcompat.animation;

import android.view.animation.PathInterpolator;

public class COUIMoveEaseInterpolator extends PathInterpolator {
    public COUIMoveEaseInterpolator() {
        super(0.3f, 0.0f, 0.1f, 1.0f);
    }
}
