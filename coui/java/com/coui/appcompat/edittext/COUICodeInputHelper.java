package com.coui.appcompat.edittext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.PathInterpolator;

import com.coui.appcompat.animation.COUIMoveEaseInterpolator;

public class COUICodeInputHelper {
    private static final PathInterpolator ANIMATOR_INTERPOLATOR = new COUIMoveEaseInterpolator();
    public static final long INBOX_APPEAR_ANIMATOR_DURATION = 100;
    public static final long INBOX_DELAY_ANIMATOR_DURATION = 33;
    public static final long NUMBER_APPEAR_ANIMATOR_DURATION = 100;
    public static final long NUMBER_DELAY_ANIMATOR_DURATION = 33;
    public static final float NUMBER_SCALE_START = 0.6f;

    private final View codeItemView;
    private float endNumberAlpha;
    private boolean isInboxAnimatorRuning;
    private boolean isNumberAnimatorRuning;
    private String mAnimatorNumber;
    private boolean mCurrentInboxAppear;
    private boolean mCurrentNumberAppear;
    private ValueAnimator mInboxAlphaAnimator;
    private ValueAnimator mNumberScaleAnimator;
    private float startNumberAlpha;
    private float startNumberScale = NUMBER_SCALE_START;
    private float endNumberScale = 1.0f;
    private float mCurrentNumberAlpha = 1.0f;
    private float mCurrentNumberScale = 1.0f;
    private float mCurrentInboxAlpha = 1.0f;

    public COUICodeInputHelper(View view) {
        codeItemView = view;
    }

    private void cancelInboxAlphaAnimator() {
        if (mInboxAlphaAnimator != null && isInboxAnimatorRuning) {
            mInboxAlphaAnimator.cancel();
        }
    }

    private void cancelNumberScaleAnimator() {
        if (mNumberScaleAnimator != null && isNumberAnimatorRuning) {
            mNumberScaleAnimator.cancel();
        }
    }

    private void executeInboxAnimator(boolean appear) {
        if (appear) {
            startNumberAlpha = mCurrentInboxAlpha <= 0.0f || mCurrentInboxAlpha >= 1.0f
                    ? 0.0f : mCurrentInboxAlpha;
            endNumberAlpha = 1.0f;
        } else {
            startNumberAlpha = mCurrentInboxAlpha <= 0.0f || mCurrentInboxAlpha >= 1.0f
                    ? 1.0f : mCurrentInboxAlpha;
            endNumberAlpha = 0.0f;
        }
        mInboxAlphaAnimator = ValueAnimator.ofFloat(startNumberAlpha, endNumberAlpha);
        mInboxAlphaAnimator.setDuration(INBOX_APPEAR_ANIMATOR_DURATION);
        mInboxAlphaAnimator.setStartDelay(appear ? INBOX_DELAY_ANIMATOR_DURATION : 0L);
        mInboxAlphaAnimator.setInterpolator(ANIMATOR_INTERPOLATOR);
        mInboxAlphaAnimator.addUpdateListener(animation -> {
            mCurrentInboxAlpha = (Float) animation.getAnimatedValue();
            codeItemView.invalidate();
        });
        mInboxAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isInboxAnimatorRuning = false;
                codeItemView.invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isInboxAnimatorRuning = false;
                codeItemView.invalidate();
            }
        });
        mInboxAlphaAnimator.start();
        isInboxAnimatorRuning = true;
        mCurrentInboxAlpha = startNumberAlpha;
    }

    private void executeNumberAnimator(boolean appear) {
        if (appear) {
            startNumberAlpha = mCurrentNumberAlpha <= 0.0f || mCurrentNumberAlpha >= 1.0f
                    ? 0.0f : mCurrentNumberAlpha;
            endNumberAlpha = 1.0f;
            startNumberScale = NUMBER_SCALE_START;
        } else {
            startNumberAlpha = mCurrentNumberAlpha <= 0.0f || mCurrentNumberAlpha >= 1.0f
                    ? 1.0f : mCurrentNumberAlpha;
            endNumberAlpha = 0.0f;
            startNumberScale = 1.0f;
        }
        endNumberScale = 1.0f;
        mNumberScaleAnimator = ValueAnimator.ofPropertyValuesHolder(
                PropertyValuesHolder.ofFloat("scaleHolder", startNumberScale, endNumberScale),
                PropertyValuesHolder.ofFloat("alphaHolder", startNumberAlpha, endNumberAlpha));
        mNumberScaleAnimator.setDuration(NUMBER_APPEAR_ANIMATOR_DURATION);
        mNumberScaleAnimator.setStartDelay(appear ? 0L : NUMBER_DELAY_ANIMATOR_DURATION);
        mNumberScaleAnimator.setInterpolator(ANIMATOR_INTERPOLATOR);
        mNumberScaleAnimator.addUpdateListener(animation -> {
            mCurrentNumberAlpha = (Float) animation.getAnimatedValue("alphaHolder");
            mCurrentNumberScale = (Float) animation.getAnimatedValue("scaleHolder");
            codeItemView.invalidate();
        });
        mNumberScaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isNumberAnimatorRuning = false;
                codeItemView.invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isNumberAnimatorRuning = false;
                codeItemView.invalidate();
            }
        });
        mNumberScaleAnimator.start();
        isNumberAnimatorRuning = true;
        mCurrentNumberAlpha = startNumberAlpha;
        mCurrentNumberScale = startNumberScale;
    }

    private void setCurrentInboxAppear(boolean appear) {
        mCurrentInboxAppear = appear;
    }

    private void setCurrentNumberAppear(boolean appear) {
        mCurrentNumberAppear = appear;
    }

    public String getAnimatorNumber() {
        return mAnimatorNumber;
    }

    public float getCurrentInboxAlpha() {
        return mCurrentInboxAlpha;
    }

    public float getCurrentNumberAlpha() {
        return mCurrentNumberAlpha;
    }

    public float getCurrentNumberScale() {
        return mCurrentNumberScale;
    }

    public ValueAnimator getInboxAnimator() {
        return mInboxAlphaAnimator;
    }

    public ValueAnimator getNumberAnimator() {
        return mNumberScaleAnimator;
    }

    public boolean isCurrentNumberAppear() {
        return mCurrentNumberAppear;
    }

    public boolean isInboxAnimatorRuning() {
        return isInboxAnimatorRuning;
    }

    public boolean isNumberAnimatorRuning() {
        return isNumberAnimatorRuning;
    }

    public void startInboxAnimator(boolean appear) {
        setCurrentInboxAppear(appear);
        if (isInboxAnimatorRuning) {
            cancelInboxAlphaAnimator();
        }
        executeInboxAnimator(appear);
    }

    public void startNumberAnimator(boolean appear, String number) {
        mAnimatorNumber = number;
        setCurrentNumberAppear(appear);
        if (isNumberAnimatorRuning) {
            cancelNumberScaleAnimator();
        }
        executeNumberAnimator(appear);
    }
}
