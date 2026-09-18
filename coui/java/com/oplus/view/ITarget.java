package com.oplus.view;

public interface ITarget {
    void cancel();

    boolean doFrame(long frameTime);

    void end();

    boolean isRunning();

    void start();

    default void skipToEnd() {
        throw new RuntimeException("Only SpringAnimation can skipToEnd, but " + getClass().getName());
    }

    default void animateToFinalPosition(float finalPosition) {
        throw new RuntimeException("Only SpringAnimation can skipToEnd, but " + getClass().getName());
    }

    default void setAnimationHandler() {
        throw new RuntimeException("animationHandler should be provided by target");
    }
}
