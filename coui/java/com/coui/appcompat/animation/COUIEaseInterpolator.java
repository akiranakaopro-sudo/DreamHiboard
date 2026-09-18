package com.coui.appcompat.animation;

import android.view.animation.PathInterpolator;

public class COUIEaseInterpolator extends PathInterpolator {
    public COUIEaseInterpolator() {
        // Leapy modified: Exact control points from OPPO COUIEaseInterpolator smali.
        super(0.33f, 0.0f, 0.67f, 1.0f);
    }
}
