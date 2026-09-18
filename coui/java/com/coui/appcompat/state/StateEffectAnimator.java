package com.coui.appcompat.state;

import android.animation.ArgbEvaluator;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;

public class StateEffectAnimator {
    public static final float DEFAULT_ANIMATE_FACTOR = 10000.0f;
    private static final ArgbEvaluator EVALUATOR = new ArgbEvaluator();
    private static final float UNSET = Float.MAX_VALUE;

    private float mCurrentAnimatedValue;
    private int mCurrentMaskColor;
    private int mEndMaskColor;
    private Drawable mHostDrawable;
    private View mHostView;
    private StateEffectAnimatorListener mListener;
    private final FloatPropertyCompat<StateEffectAnimator> mMaskTransition;
    private float mPendingThresholdValue;
    private final COUIDynamicAnimation.OnAnimationEndListener mResetEndListener;
    private COUISpringAnimation mSpringAnimation;
    private int mStartMaskColor;
    private final String mTag;

    public interface StateEffectAnimatorListener {
        void onValueUpdateListener(float value);
    }

    public StateEffectAnimator(Drawable drawable, String tag, int startColor, int endColor) {
        this(drawable, null, tag, startColor, endColor);
    }

    public StateEffectAnimator(View view, String tag, int startColor, int endColor) {
        this(null, view, tag, startColor, endColor);
    }

    public StateEffectAnimator(Drawable drawable, View view, String tag, int startColor,
            int endColor) {
        // Leapy added: Match OPPO's reset listener and threshold-return spring behavior.
        mResetEndListener = new COUIDynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(COUIDynamicAnimation animation, boolean canceled,
                    float value, float velocity) {
                StateEffectAnimator.this.animateToProgress(0.0f, true);
                animation.removeEndListener(StateEffectAnimator.this.mResetEndListener);
            }
        };
        mCurrentAnimatedValue = 0.0f;
        mPendingThresholdValue = UNSET;
        mListener = null;
        mHostDrawable = drawable;
        mHostView = view;
        mTag = tag;
        mMaskTransition = new FloatPropertyCompat<StateEffectAnimator>(tag) {
            @Override
            public float getValue(StateEffectAnimator animator) {
                return animator.getProgress();
            }

            @Override
            public void setValue(StateEffectAnimator animator, float value) {
                animator.setProgress(value);
            }
        };
        ensureSpringAnimation();
        mStartMaskColor = startColor;
        mEndMaskColor = endColor;
    }

    private void ensureSpringAnimation() {
        if (mSpringAnimation != null) {
            return;
        }
        // Leapy modified: OPPO uses COUI's spring engine, not an AndroidX approximation.
        mSpringAnimation = new COUISpringAnimation(this, mMaskTransition);
        mSpringAnimation.setSpring(new COUISpringForce());
    }

    private float getProgress() {
        return mCurrentAnimatedValue;
    }

    private void setProgress(float value) {
        mCurrentAnimatedValue = value;
        mCurrentMaskColor = (Integer) EVALUATOR.evaluate(
                value / DEFAULT_ANIMATE_FACTOR, mStartMaskColor, mEndMaskColor);
        if (mListener != null) {
            mListener.onValueUpdateListener(value);
        }
        if (mHostDrawable != null) {
            mHostDrawable.invalidateSelf();
        }
        if (mHostView != null) {
            mHostView.invalidate();
        }
        if (mCurrentAnimatedValue > mPendingThresholdValue) {
            mPendingThresholdValue = UNSET;
            if (mCurrentAnimatedValue >= DEFAULT_ANIMATE_FACTOR) {
                mSpringAnimation.addEndListener(mResetEndListener);
            } else {
                animateToProgress(0.0f, true);
            }
        }
    }

    public void animateToProgress(float progress, boolean animated) {
        ensureSpringAnimation();
        mSpringAnimation.removeEndListener(mResetEndListener);
        if (animated) {
            mSpringAnimation.setStartValue(mCurrentAnimatedValue);
            mSpringAnimation.animateToFinalPosition(progress);
        } else {
            if (mSpringAnimation.isRunning()) {
                mSpringAnimation.animateToFinalPosition(progress);
                mSpringAnimation.reset();
            }
            setProgress(progress);
        }
        mPendingThresholdValue = UNSET;
    }

    public void animateToProgressUntil(float progress, float threshold) {
        ensureSpringAnimation();
        mSpringAnimation.removeEndListener(mResetEndListener);
        if (!mSpringAnimation.isRunning()) {
            mSpringAnimation.setStartValue(mCurrentAnimatedValue);
            mSpringAnimation.animateToFinalPosition(progress);
            mPendingThresholdValue = threshold;
        } else if (mCurrentAnimatedValue <= threshold) {
            mPendingThresholdValue = threshold;
        } else {
            mSpringAnimation.setStartValue(mCurrentAnimatedValue);
            mSpringAnimation.animateToFinalPosition(progress);
        }
    }

    public int getCurrentMaskColor() {
        return mCurrentMaskColor;
    }

    public void setEndMaskColor(int color) {
        mEndMaskColor = color;
    }

    public void setHostDrawable(Drawable drawable) {
        mHostDrawable = drawable;
    }

    public void setHostView(View view) {
        mHostView = view;
    }

    public void setSpringBounce(float bounce) {
        ensureSpringAnimation();
        mSpringAnimation.getSpring().setBounce(bounce);
    }

    public void setSpringResponse(float response) {
        ensureSpringAnimation();
        mSpringAnimation.getSpring().setResponse(response);
    }

    public void setStartMaskColor(int color) {
        mStartMaskColor = color;
    }

    public void setStateEffectAnimatorListener(StateEffectAnimatorListener listener) {
        mListener = listener;
    }
}
