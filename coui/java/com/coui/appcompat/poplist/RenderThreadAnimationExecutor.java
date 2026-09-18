package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.animation.Animator;
import android.os.Looper;
import android.view.View;
import com.coui.appcompat.animation.dynamicanimation.COUIRtAnimationImpl;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.oplus.animation.OplusAsyncAnimatorUtils;
import com.oplus.view.OplusRenderNodeAnimator;


class RenderThreadAnimationExecutor implements AnimationExecutor {
    static final RenderThreadAnimationExecutor INSTANCE = new RenderThreadAnimationExecutor();

    private RenderThreadAnimationExecutor() {
    }

    @Override
    public boolean isAsynchronous() {
        return true;
    }

    @Override
    public void runOnMainThread(View view, Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            view.post(runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void setAlpha(View view, float alpha) {
        OplusAsyncAnimatorUtils.setAlpha(view, alpha);
    }

    @Override
    public void setScaleX(View view, float scaleX) {
        OplusAsyncAnimatorUtils.setScaleX(view, scaleX);
    }

    @Override
    public void setScaleY(View view, float scaleY) {
        OplusAsyncAnimatorUtils.setScaleY(view, scaleY);
    }

    @Override
    public Animator startAnimation(COUISpringAnimation cOUISpringAnimation, View view, float forward) {
        Animator animatorCreateRtAnimator = OplusRenderNodeAnimator.createRtAnimator(new COUIRtAnimationImpl(cOUISpringAnimation), view);
        OplusRenderNodeAnimator.animateToFinalPosition(animatorCreateRtAnimator, forward);
        return animatorCreateRtAnimator;
    }
}
