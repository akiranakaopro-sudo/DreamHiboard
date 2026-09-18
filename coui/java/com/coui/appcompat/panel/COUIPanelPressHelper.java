package com.coui.appcompat.panel;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.coui.appcompat.animation.COUIEaseInterpolator;


public class COUIPanelPressHelper {
    private static final String BG_ALPHA = "bgAlpha";
    private static final PathInterpolator COUI_EASE_INTERPOLATOR = new COUIEaseInterpolator();
    private static final int LOAD_BG_DURATION = 200;
    private Float bgAlpha = 0f;
    private ValueAnimator pressAnim;
    private ValueAnimator releaseAnim;

    private void cancelAnim(ValueAnimator animator) {
        if (animator == null || !animator.isRunning()) {
            return;
        }
        animator.cancel();
    }

    private void updateBackgroundAlpha(View targetView, ValueAnimator animator) {
        Float animatedAlpha = (Float) animator.getAnimatedValue(BG_ALPHA);
        this.bgAlpha = animatedAlpha;
        targetView.setAlpha(animatedAlpha);
    }

    public void endAnim(final View view) {
        cancelAnim(this.pressAnim);
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(BG_ALPHA, this.bgAlpha, 0.0f));
        this.releaseAnim = animator;
        animator.setInterpolator(COUI_EASE_INTERPOLATOR);
        this.releaseAnim.setDuration(LOAD_BG_DURATION);
        this.releaseAnim.addUpdateListener(animator1 -> COUIPanelPressHelper.this.updateBackgroundAlpha(view, animator1));
        this.releaseAnim.start();
    }

    public void startAnim(final View view) {
        cancelAnim(this.releaseAnim);
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(BG_ALPHA, 0.0f, 1.0f));
        this.pressAnim = animator;
        animator.setInterpolator(COUI_EASE_INTERPOLATOR);
        this.pressAnim.setDuration(LOAD_BG_DURATION);
        this.pressAnim.addUpdateListener(animator1 -> COUIPanelPressHelper.this.updateBackgroundAlpha(view, animator1));
        this.pressAnim.start();
    }
}
