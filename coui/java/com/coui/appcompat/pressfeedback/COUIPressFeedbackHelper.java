package com.coui.appcompat.pressfeedback;

import android.content.Context;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.log.COUILog;

public class COUIPressFeedbackHelper {
    public static final int CARD_PRESS_FEEDBACK = 0;
    public static final int UNJUMPABLE_CARD_PRESS_FEEDBACK = 0;
    public static final int BORDERLESS_BUTTON_PRESS_FEEDBACK = 1;
    public static final int FILL_BUTTON_PRESS_FEEDBACK = 2;
    private static final float DEFAULT_SCALE_END_RATIO = 0.92f;
    private static final float DEFAULT_SCALE_FACTOR = 10000.0f;
    private static final float DEFAULT_SCALE_START_RATIO = 1.0f;
    public static final float DEFAULT_SPRING_BOUNCE = 0.0f;
    public static final float DEFAULT_SPRING_RESPONSE = 0.3f;
    private static final float MAX_SCALE_END_RATIO = 0.98f;
    private static final float MIN_SCALE_END_RATIO = 0.92f;
    private static final String TAG = "COUIPressFeedbackHelper";
    private static final Interpolator DEFAULT_SCALE_MAPPING_INTERPOLATOR = new COUIInEaseInterpolator();

    private static final FloatPropertyCompat<COUIPressFeedbackHelper> SCALE_TRANSITION =
            new FloatPropertyCompat<COUIPressFeedbackHelper>("viewScaleTransition") {
                @Override
                public float getValue(COUIPressFeedbackHelper object) {
                    return object.mCurrentScaleValue;
                }

                @Override
                public void setValue(COUIPressFeedbackHelper object, float value) {
                    object.setProgress(value);
                }
            };

    public interface COUIPressFeedbackHelperCallback {
        default int getTargetHeight() { return 0; }
        default int getTargetWidth() { return 0; }
        default void onScaleUpdate(float scale) { }
    }

    private COUIPressFeedbackHelperCallback mCallback;
    private float mCurrentScaleValue;
    private float mMaxCardViewSize;
    private float mMinCardViewSize;
    private boolean mScaleEnable = true;
    private COUISpringAnimation mSpringAnimation;
    private View mTarget;
    private float mUserViewHeight;
    private float mUserViewWidth;
    private int mViewType;

    public COUIPressFeedbackHelper(View view) {
        this(view, CARD_PRESS_FEEDBACK);
    }

    public COUIPressFeedbackHelper(View view, int viewType) {
        mViewType = viewType;
        setTargetView(view);
        updateConfig(view.getContext());
    }

    public COUIPressFeedbackHelper(Context context) {
        this(context, CARD_PRESS_FEEDBACK);
    }

    public COUIPressFeedbackHelper(Context context, int viewType) {
        mViewType = viewType;
        updateConfig(context);
    }

    private void ensureSpringAnimation() {
        if (mSpringAnimation != null) {
            return;
        }
        COUISpringForce force = new COUISpringForce();
        force.setBounce(DEFAULT_SPRING_BOUNCE);
        force.setResponse(DEFAULT_SPRING_RESPONSE);
        mSpringAnimation = new COUISpringAnimation(this, SCALE_TRANSITION);
        mSpringAnimation.setSpring(force);
        mSpringAnimation.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(COUIDynamicAnimation animation, float value, float velocity) {
                setProgress(value);
            }
        });
    }

    private float getCardScaleRatio() {
        int targetWidth;
        int targetHeight;
        if (mTarget != null) {
            targetWidth = mTarget.getWidth();
            targetHeight = mTarget.getHeight();
        } else if (mCallback != null) {
            targetWidth = mCallback.getTargetWidth();
            targetHeight = mCallback.getTargetHeight();
        } else {
            return 1.0f;
        }
        float area = mUserViewWidth * mUserViewHeight;
        if (area <= 0.0f) {
            area = targetWidth * targetHeight;
        }
        if (area <= mMinCardViewSize) {
            return DEFAULT_SCALE_END_RATIO;
        }
        if (area >= mMaxCardViewSize) {
            return MAX_SCALE_END_RATIO;
        }
        return (DEFAULT_SCALE_MAPPING_INTERPOLATOR.getInterpolation((area - mMinCardViewSize) / (mMaxCardViewSize - mMinCardViewSize)) * 0.060000002f)
                + DEFAULT_SCALE_END_RATIO;
    }

    private float getScaledRatioByAnimatedValue() {
        return DEFAULT_SCALE_START_RATIO - ((DEFAULT_SCALE_START_RATIO - getCardScaleRatio()) * (mCurrentScaleValue / DEFAULT_SCALE_FACTOR));
    }

    private void setProgress(float value) {
        if (mTarget == null && mCallback == null) {
            COUILog.w(TAG, "press effect target is null!");
            return;
        }
        // Leapy modified: OPPO calculates this frame before storing the next spring value.
        float scale = getScaledRatioByAnimatedValue();
        mCurrentScaleValue = value;
        if (mTarget == null) {
            mCallback.onScaleUpdate(scale);
            return;
        }
        mTarget.setPivotX(mTarget.getWidth() / 2.0f);
        mTarget.setPivotY(mTarget.getHeight() / 2.0f);
        mTarget.setScaleX(scale);
        mTarget.setScaleY(scale);
    }

    private void updateConfig(Context context) {
        int min = context.getResources().getDimensionPixelOffset(R.dimen.coui_min_end_value_size);
        mMinCardViewSize = min * min;
        mMaxCardViewSize = context.getResources().getDimensionPixelOffset(R.dimen.coui_max_end_value_width)
                * context.getResources().getDimensionPixelOffset(R.dimen.coui_max_end_value_height);
    }

    public void executeFeedbackAnimator(boolean pressed) {
        if (!mScaleEnable) {
            return;
        }
        ensureSpringAnimation();
        mSpringAnimation.animateToFinalPosition(pressed ? DEFAULT_SCALE_FACTOR : 0.0f);
    }

    public COUISpringAnimation getSpringAnimation() {
        return mSpringAnimation;
    }

    public void setCallback(COUIPressFeedbackHelperCallback callback) {
        mCallback = callback;
    }

    public void setScaleEnable(boolean scaleEnable) {
        mScaleEnable = scaleEnable;
    }

    public void setTargetView(View view) {
        mTarget = view;
    }

    public void setUserViewHeight(int height) {
        mUserViewHeight = height;
    }

    public void setUserViewWidth(int width) {
        mUserViewWidth = width;
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
    }
}
