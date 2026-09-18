package com.coui.appcompat.animation.dynamicanimation;

import com.oplus.view.IRtAnimationTarget;

public class COUIRtAnimationImpl implements IRtAnimationTarget {
    private final COUISpringAnimation mSpringAnimation;

    public COUIRtAnimationImpl(COUISpringAnimation springAnimation) {
        this.mSpringAnimation = springAnimation;
    }

    @Override
    public void animateToFinalPosition(float finalPosition) {
        this.mSpringAnimation.animateToFinalPosition(finalPosition);
    }

    @Override
    public void cancel() {
        this.mSpringAnimation.cancel();
    }

    @Override
    public boolean doFrame(long frameTime) {
        return this.mSpringAnimation.doAnimationFrame(frameTime);
    }

    @Override
    public void end() {
        this.mSpringAnimation.skipToEnd();
    }

    @Override
    public boolean isRunning() {
        return this.mSpringAnimation.isRunning();
    }

    @Override
    public void setAnimationHandler() {
        this.mSpringAnimation.setEnableNonMainThread(true);
        this.mSpringAnimation.setAnimationHandler(COUIRtAnimationHandler.getInstance());
    }

    @Override
    public void skipToEnd() {
        this.mSpringAnimation.skipToEnd();
    }

    @Override
    public void start() {
        this.mSpringAnimation.start();
    }
}
