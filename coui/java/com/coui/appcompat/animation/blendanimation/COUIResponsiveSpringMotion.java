package com.coui.appcompat.animation.blendanimation;

import com.coui.appcompat.animation.dynamicanimation.SpringMotion;

public class COUIResponsiveSpringMotion extends SpringMotion {
    private static final float DEFAULT_BLEND_DURATION = 250.0f;
    private static final float MIN_VISIBLE_CHANGE_DIVISOR = 10000.0f;

    public <K> COUIResponsiveSpringMotion(K target, COUIProperty<K> property) {
        super(target, property);
    }

    @Override
    public void animateToFinalPosition(float finalPosition) {
        float minimumVisibleChange = (isStartValueSet()
                ? Math.abs(getSpring().getFinalPosition() - this.mValue)
                : Math.abs(getSpring().getFinalPosition() - getTargetPropertyValue()))
                / MIN_VISIBLE_CHANGE_DIVISOR;
        if (minimumVisibleChange == 0.0f) {
            minimumVisibleChange = COUIProperty.DEFAULT_MIN_VISIBLE_CHANGE;
        }
        super.setMinimumVisibleChange(minimumVisibleChange);
        super.animateToFinalPosition(finalPosition);
    }

    public <K> COUIResponsiveSpringMotion(K target, COUIProperty<K> property, float finalPosition) {
        super(target, property, finalPosition);
        getSpring().setBlendDuration(DEFAULT_BLEND_DURATION);
    }

    @Override
    public SpringMotion setMinimumVisibleChange(float minimumVisibleChange) {
        throw new UnsupportedOperationException("setMinimumVisibleChange is not support for this type of animation.");
    }
}
