package com.oplus.dynamicframerate;

import android.animation.ValueAnimator;

public class AnimationVelocityCalculator {
    private float mLastValue;

    public AnimationVelocityCalculator(ValueAnimator animator) {
        if (animator != null && animator.getAnimatedValue() instanceof Number) {
            this.mLastValue = ((Number) animator.getAnimatedValue()).floatValue();
        }
    }

    public float calculator(int height, ValueAnimator animator) {
        if (animator == null || !(animator.getAnimatedValue() instanceof Number)) {
            return 0.0f;
        }
        float value = ((Number) animator.getAnimatedValue()).floatValue();
        float velocity = Math.abs(value - this.mLastValue);
        this.mLastValue = value;
        return velocity;
    }
}
