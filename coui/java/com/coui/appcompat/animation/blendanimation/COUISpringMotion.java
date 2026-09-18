package com.coui.appcompat.animation.blendanimation;

import com.coui.appcompat.animation.dynamicanimation.SpringMotion;

public class COUISpringMotion extends SpringMotion {
    public <K> COUISpringMotion(COUIProperty<K> property) {
        this((K) null, property);
    }

    public <K> COUISpringMotion(K target, COUIProperty<K> property) {
        super(target, property);
        super.setMinimumVisibleChange(property.getMinimumVisibleChange());
    }

    @Override
    public SpringMotion setMinimumVisibleChange(float minimumVisibleChange) {
        throw new UnsupportedOperationException("setMinimumVisibleChange is not support for this type of animation.");
    }

    public <K> COUISpringMotion(COUIProperty<K> property, float finalPosition) {
        this((K) null, property, finalPosition);
    }

    public <K> COUISpringMotion(K target, COUIProperty<K> property, float finalPosition) {
        super(target, property, finalPosition);
        super.setMinimumVisibleChange(property.getMinimumVisibleChange());
    }
}
