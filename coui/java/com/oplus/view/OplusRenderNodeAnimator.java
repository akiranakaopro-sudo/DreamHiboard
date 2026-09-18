package com.oplus.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

public final class OplusRenderNodeAnimator {
    private OplusRenderNodeAnimator() {
    }

    public static Animator createRtAnimator(final IRtAnimationTarget animationImpl, View target) {
        if (animationImpl == null || target == null) {
            return null;
        }
        animationImpl.setAnimationHandler();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                animationImpl.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animationImpl.isRunning()) {
                    animationImpl.end();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animationImpl.cancel();
            }
        });
        return animator;
    }

    public static void animateToFinalPosition(Animator animator, float finalPosition) {
        if (animator instanceof ValueAnimator) {
            animator.start();
        }
    }
}
