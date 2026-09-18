package com.coui.appcompat.searchview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Insets;
import android.view.WindowInsetsAnimationControlListener;
import android.view.WindowInsetsAnimationController;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

public final class CustomWindowInsetsAnimationControlListener
        implements WindowInsetsAnimationControlListener {
    private static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR =
            new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);

    private static final TypeEvaluator<Insets> INSETS_EVALUATOR = (fraction, start, end) ->
            Insets.of(
                    (int) (start.left + ((end.left - start.left) * fraction)),
                    (int) (start.top + ((end.top - start.top) * fraction)),
                    (int) (start.right + ((end.right - start.right) * fraction)),
                    (int) (start.bottom + ((end.bottom - start.bottom) * fraction))
            );

    public static final class Companion {
        private Companion() {
        }

        public TypeEvaluator<Insets> getINSETS_EVALUATOR() {
            return INSETS_EVALUATOR;
        }
    }

    public static final Companion Companion = new Companion();

    private Animator mAnimator;
    private final int mDuration;
    private final Interpolator mInsetsInterpolator;
    private final boolean mShow;

    public CustomWindowInsetsAnimationControlListener(
            boolean show,
            int duration,
            Interpolator insetsInterpolator
    ) {
        this.mShow = show;
        this.mDuration = duration;
        this.mInsetsInterpolator = insetsInterpolator;
    }

    private Interpolator getAlphaInterpolator() {
        return mShow ? fraction -> Math.min(1.0f, 2.0f * fraction)
                : FAST_OUT_LINEAR_IN_INTERPOLATOR;
    }

    private ValueAnimator runTransition(
            final WindowInsetsAnimationController controller,
            final boolean show
    ) {
        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(mDuration);
        animator.setInterpolator(new LinearInterpolator());

        final Interpolator alphaInterpolator = getAlphaInterpolator();
        final Insets start = show ? controller.getHiddenStateInsets()
                : controller.getShownStateInsets();
        final Insets end = show ? controller.getShownStateInsets()
                : controller.getHiddenStateInsets();

        animator.addUpdateListener(animation -> {
            if (!controller.isReady()) {
                animator.cancel();
                return;
            }
            float fraction = animation.getAnimatedFraction();
            controller.setInsetsAndAlpha(
                    INSETS_EVALUATOR.evaluate(
                            mInsetsInterpolator.getInterpolation(fraction),
                            start,
                            end
                    ),
                    alphaInterpolator.getInterpolation(mShow ? fraction : 1.0f - fraction),
                    fraction
            );
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!controller.isCancelled()) {
                    controller.finish(show);
                }
            }
        });
        animator.start();
        return animator;
    }

    @Override
    public void onCancelled(WindowInsetsAnimationController controller) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    @Override
    public void onFinished(WindowInsetsAnimationController controller) {
    }

    @Override
    public void onReady(WindowInsetsAnimationController controller, int types) {
        mAnimator = runTransition(controller, mShow);
    }
}
