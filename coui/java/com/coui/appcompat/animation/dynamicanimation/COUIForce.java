package com.coui.appcompat.animation.dynamicanimation;

public interface COUIForce {
    float getAcceleration(float value, float velocity);

    boolean isAtEquilibrium(float value, float velocity);
}
