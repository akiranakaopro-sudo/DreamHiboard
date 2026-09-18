package com.coui.appcompat.animation.dynamicanimation;

public class COUIRtAnimationHandler extends COUIAnimationHandler {
    public static final ThreadLocal<COUIRtAnimationHandler> sAnimatorHandler = new ThreadLocal<>();

    public static COUIRtAnimationHandler getInstance() {
        if (sAnimatorHandler.get() == null) {
            sAnimatorHandler.set(new COUIRtAnimationHandler());
        }
        return sAnimatorHandler.get();
    }

    @Override
    public void addAnimationFrameCallback(COUIAnimationHandler.AnimationFrameCallback callback, long delay) {
    }

    @Override
    public void removeCallback(COUIAnimationHandler.AnimationFrameCallback callback) {
    }
}
