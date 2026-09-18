package com.coui.appcompat.animation;

import android.view.animation.PathInterpolator;

public class COUIInEaseInterpolator extends PathInterpolator {
    public COUIInEaseInterpolator() {
        // Leapy modified: Exact control points from OPPO COUIInEaseInterpolator smali.
        super(0.0f, 0.0f, 0.1f, 1.0f);
    }
}
