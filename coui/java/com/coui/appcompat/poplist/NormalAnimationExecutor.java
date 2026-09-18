package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.animation.Animator;
import android.view.View;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;


class NormalAnimationExecutor implements AnimationExecutor {
    static final NormalAnimationExecutor INSTANCE = new NormalAnimationExecutor();

    private NormalAnimationExecutor() {
    }

    @Override
    public boolean isAsynchronous() {
        return false;
    }

    @Override
    public void runOnMainThread(View view, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void setAlpha(View view, float alpha) {
        view.setAlpha(alpha);
    }

    @Override
    public void setScaleX(View view, float scaleX) {
        view.setScaleX(scaleX);
    }

    @Override
    public void setScaleY(View view, float scaleY) {
        view.setScaleY(scaleY);
    }

    @Override
    public Animator startAnimation(COUISpringAnimation cOUISpringAnimation, View view, float forward) {
        cOUISpringAnimation.animateToFinalPosition(forward);
        return null;
    }
}
