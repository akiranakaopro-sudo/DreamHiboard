package com.coui.appcompat.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.coui.appcompat.animation.COUILinearInterpolator;

public class COUIBackgroundAnimationUtil {
    public static final int ACTION_IS_FROM_TOUCH_LISTVIEW = -1;
    public static final int APPEAR_DURATION = 150;
    public static final int DISAPPEAR_DURATION = 367;
    public static final int STATE_BACKGROUND_APPEAR = 1;
    public static final int STATE_BACKGROUND_DISAPPEAR = 2;

    private boolean mActionIsFromTouchListView;
    private int mAppearDuration = APPEAR_DURATION;
    public Interpolator mAppearInterpolator = new COUILinearInterpolator();
    private ValueAnimator mBackgroundAppearAnimator;
    private ValueAnimator mBackgroundDisappearAnimator;
    private int mDefaultColor;
    private int mDisappearDuration = DISAPPEAR_DURATION;
    public Interpolator mDisappearInterpolator = new PathInterpolator(0.17f, 0.17f, 0.67f, 1.0f);
    private boolean mIfDisappearWhenGetCancelEvent = true;
    private boolean mIsNeedVibrate;
    private boolean mIsSelected;
    private boolean mNeedAutoStartDisAppear;
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (view.isEnabled() && view.isClickable()) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (event.getSource() == ACTION_IS_FROM_TOUCH_LISTVIEW) {
                        mActionIsFromTouchListView = true;
                    }
                    startAppearAnimation();
                } else if (action == MotionEvent.ACTION_UP) {
                    startDisAppearAnimationOrNot();
                    mActionIsFromTouchListView = false;
                } else if (action == MotionEvent.ACTION_CANCEL) {
                    if (mIfDisappearWhenGetCancelEvent) {
                        startDisAppearAnimationOrNot();
                    }
                    mActionIsFromTouchListView = false;
                }
            }
            return false;
        }
    };
    private int mPressColor;
    public int mState = STATE_BACKGROUND_DISAPPEAR;
    private View mTargetView;

    private void cancelAnimation() {
        if (mBackgroundDisappearAnimator != null && mBackgroundDisappearAnimator.isRunning()) {
            mBackgroundDisappearAnimator.cancel();
        }
        if (mBackgroundAppearAnimator != null && mBackgroundAppearAnimator.isRunning()) {
            mBackgroundAppearAnimator.cancel();
        }
    }

    private void performHapticFeedback() {
        if (mIsNeedVibrate && mTargetView != null && mActionIsFromTouchListView) {
            mTargetView.performHapticFeedback(302);
        }
    }

    public void init(View targetView, int defaultColor, int pressColor) {
        init(targetView, defaultColor, pressColor, false);
    }

    @SuppressLint("ObjectAnimatorBinding")
    public void init(View targetView, int defaultColor, int pressColor, boolean needVibrate) {
        mIsNeedVibrate = needVibrate;
        mTargetView = targetView;
        mPressColor = pressColor;
        mDefaultColor = defaultColor;
        if (mBackgroundAppearAnimator != null && mBackgroundAppearAnimator.isRunning()) {
            mBackgroundAppearAnimator.end();
            mBackgroundAppearAnimator = null;
        }
        if (mBackgroundDisappearAnimator != null && mBackgroundDisappearAnimator.isRunning()) {
            mBackgroundDisappearAnimator.end();
            mBackgroundDisappearAnimator = null;
        }
        mBackgroundAppearAnimator = ObjectAnimator.ofInt(targetView, "backgroundColor",
                defaultColor, pressColor);
        mBackgroundDisappearAnimator = ObjectAnimator.ofInt(targetView, "backgroundColor",
                pressColor, defaultColor);
        mBackgroundAppearAnimator.setDuration(mAppearDuration);
        mBackgroundAppearAnimator.setInterpolator(mAppearInterpolator);
        mBackgroundAppearAnimator.setEvaluator(new ArgbEvaluator());
        mBackgroundAppearAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_BACKGROUND_APPEAR;
                if (mNeedAutoStartDisAppear) {
                    mNeedAutoStartDisAppear = false;
                    if (!mIsSelected) {
                        mBackgroundDisappearAnimator.start();
                    }
                }
            }
        });
        mBackgroundDisappearAnimator.setDuration(mDisappearDuration);
        mBackgroundDisappearAnimator.setInterpolator(mDisappearInterpolator);
        mBackgroundDisappearAnimator.setEvaluator(new ArgbEvaluator());
        mBackgroundDisappearAnimator.addUpdateListener(animation -> {
            if (mIsSelected) {
                mBackgroundDisappearAnimator.cancel();
            }
        });
        mBackgroundDisappearAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_BACKGROUND_DISAPPEAR;
            }
        });
    }

    public View.OnTouchListener operateTouchListener(boolean onlyReturnListener) {
        if (mTargetView == null) {
            throw new IllegalArgumentException("Must be called after the init method");
        }
        if (onlyReturnListener) {
            return mOnTouchListener;
        }
        mTargetView.setOnTouchListener(mOnTouchListener);
        return null;
    }

    public void refreshCardBg(int color) {
        if (mTargetView != null) {
            mTargetView.setBackgroundColor(color);
        }
    }

    public void setAppearDuration(int appearDuration) {
        mAppearDuration = appearDuration;
    }

    public void setDisappearDuration(int disappearDuration) {
        mDisappearDuration = disappearDuration;
    }

    public void setIfDisappearWhenGetCancelEvent(boolean disappearWhenCancel) {
        mIfDisappearWhenGetCancelEvent = disappearWhenCancel;
    }

    public void setIsSelected(boolean selected) {
        setIsSelected(selected, false);
    }

    public void setIsSelected(boolean selected, boolean animate) {
        if (mIsSelected == selected) {
            return;
        }
        mIsSelected = selected;
        cancelAnimation();
        if (!selected) {
            if (animate) {
                if (mBackgroundDisappearAnimator != null) {
                    mBackgroundDisappearAnimator.start();
                }
            } else {
                refreshCardBg(mDefaultColor);
            }
        } else if (animate) {
            if (mBackgroundAppearAnimator != null) {
                mBackgroundAppearAnimator.start();
            }
        } else {
            refreshCardBg(mPressColor);
        }
    }

    public void startAppearAnimation() {
        if (!mIsSelected) {
            if (mBackgroundDisappearAnimator != null && mBackgroundDisappearAnimator.isRunning()) {
                mBackgroundDisappearAnimator.cancel();
            }
            if (mBackgroundAppearAnimator != null && mBackgroundAppearAnimator.isRunning()) {
                mBackgroundAppearAnimator.cancel();
            }
            if (mBackgroundAppearAnimator != null) {
                mBackgroundAppearAnimator.start();
            }
            performHapticFeedback();
        }
    }

    public void startDisAppearAnimationOrNot() {
        if (mBackgroundAppearAnimator != null && mBackgroundAppearAnimator.isRunning()) {
            mNeedAutoStartDisAppear = true;
        } else if (mBackgroundDisappearAnimator != null
                && !mBackgroundDisappearAnimator.isRunning()
                && mState == STATE_BACKGROUND_APPEAR
                && !mIsSelected) {
            mBackgroundDisappearAnimator.start();
        }
    }
}
