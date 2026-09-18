package com.oplus.physicsengine.common;

public abstract class MathUtils {
    public static float abs(float value) {
        return value > 0.0f ? value : -value;
    }

    public static boolean floatEquals(float left, float right) {
        return Math.abs(left - right) < 1.0E-7f;
    }
}
