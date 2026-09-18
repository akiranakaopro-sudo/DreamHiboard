package com.coui.appcompat.animation.dynamicanimation;

import android.os.Looper;
import android.util.AndroidRuntimeException;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;

public final class COUISpringAnimation extends COUIDynamicAnimation<COUISpringAnimation> {
    private static final int LOGIC_END_EXTRA_TIME_MS = 50;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final float UNSET = Float.MAX_VALUE;

    private boolean mEndRequested;
    private float mPendingPosition = UNSET;
    private COUISpringForce mSpring;

    public COUISpringAnimation(FloatValueHolder valueHolder) {
        super(valueHolder);
    }

    public <K> COUISpringAnimation(K target, FloatPropertyCompat<K> property) {
        super(target, property);
    }

    public <K> COUISpringAnimation(K target, FloatPropertyCompat<K> property, float finalPosition) {
        super(target, property);
        mSpring = new COUISpringForce(finalPosition);
    }

    private void onStartAnimation(COUISpringAnimation previous) {
        if (previous == null || !previous.isRunning()) {
            getSpring().setBlendDuration(0.0f);
            return;
        }
        setStartValue(previous.mValue);
        setStartVelocity(previous.mVelocity);
        float response = previous.getSpring().getResponse();
        getSpring().updateResponse(response);
        if (response == getSpring().getFinalResponse()) {
            getSpring().setBlendDuration(0.0f);
        } else if (getSpring().getBlendDuration() == 0.0f) {
            getSpring().updateResponse(getSpring().getFinalResponse());
        } else if (getSpring().getFinalResponse() == previous.getSpring().getFinalResponse()) {
            getSpring().setBlendDuration(previous.getSpring().getBlendDuration());
        }
    }

    private void sanityCheck() {
        if (mSpring == null) {
            throw new UnsupportedOperationException(
                    "Incomplete SpringAnimation: Either final position or a spring force needs to be set.");
        }
        double finalPosition = mSpring.getFinalPosition();
        if (finalPosition > mMaxValue) {
            throw new UnsupportedOperationException(
                    "Final position of the spring cannot be greater than the max value.");
        }
        if (finalPosition < mMinValue) {
            throw new UnsupportedOperationException(
                    "Final position of the spring cannot be less than the min value.");
        }
    }

    public void animateToFinalPosition(float finalPosition) {
        if (isRunning()) {
            mPendingPosition = finalPosition;
            return;
        }
        if (mSpring == null) {
            mSpring = new COUISpringForce(finalPosition);
        }
        mSpring.setFinalPosition(finalPosition);
        start();
    }

    public boolean canSkipToEnd() {
        return mSpring.mDampingRatio > 0.0d;
    }

    public void cancelComplete() {
        super.cancel();
        mPendingPosition = UNSET;
        mVelocity = 0.0f;
        mEndRequested = false;
    }

    @Override
    public float getAcceleration(float value, float velocity) {
        return mSpring.getAcceleration(value, velocity);
    }

    public COUISpringForce getSpring() {
        return mSpring;
    }

    @Override
    public boolean isAtEquilibrium(float value, float velocity) {
        return mSpring.isAtEquilibrium(value, velocity);
    }

    @Override
    public boolean isLogicEnd(long elapsedMillis) {
        return elapsedMillis >= (getSpring().getFinalResponse() * MILLISECONDS_PER_SECOND)
                + LOGIC_END_EXTRA_TIME_MS;
    }

    @Override
    public void onAnimate(long deltaMillis) {
        super.onAnimate(deltaMillis);
        float blendDuration = getSpring().getBlendDuration();
        if (blendDuration > 0.0f) {
            float delta = deltaMillis;
            setStartValue(mValue);
            setStartVelocity(mVelocity);
            if (delta >= blendDuration) {
                getSpring().updateResponse(getSpring().getFinalResponse());
                getSpring().setBlendDuration(0.0f);
                return;
            }
            float response = getSpring().getResponse();
            getSpring().updateResponse(
                    response + ((getSpring().getFinalResponse() - response)
                            * (delta / blendDuration)));
            getSpring().setBlendDuration(blendDuration - delta);
        }
    }

    public void reset() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException(
                    "Spring animations can only come to an end when there is damping");
        }
        cancel();
        if (mPendingPosition != UNSET) {
            mSpring.setFinalPosition(mPendingPosition);
            mPendingPosition = UNSET;
        }
        mValue = mSpring.getFinalPosition();
        mVelocity = 0.0f;
        mEndRequested = false;
    }

    public COUISpringAnimation setSpring(COUISpringForce spring) {
        mSpring = spring;
        return this;
    }

    @Override
    public void setValueThreshold(float threshold) {
    }

    public void skipToEnd() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException(
                    "Spring animations can only come to an end when there is damping");
        }
        if (!mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        }
        if (mRunning) {
            mEndRequested = true;
        }
    }

    @Override
    public void start() {
        sanityCheck();
        mSpring.setValueThreshold(getValueThreshold());
        super.start();
    }

    public void start(COUISpringAnimation previous) {
        start();
        if (!isRunning()) {
            onStartAnimation(previous);
        }
    }

    @Override
    public boolean updateValueAndVelocity(long deltaMillis) {
        if (mEndRequested) {
            if (mPendingPosition != UNSET) {
                mSpring.setFinalPosition(mPendingPosition);
                mPendingPosition = UNSET;
            }
            mValue = mSpring.getFinalPosition();
            mVelocity = 0.0f;
            mEndRequested = false;
            return true;
        }
        if (mPendingPosition != UNSET) {
            long halfDelta = deltaMillis / 2L;
            MassState first = mSpring.updateValues(mValue, mVelocity, halfDelta);
            mSpring.setFinalPosition(mPendingPosition);
            mPendingPosition = UNSET;
            MassState second = mSpring.updateValues(first.mValue, first.mVelocity, halfDelta);
            mValue = second.mValue;
            mVelocity = second.mVelocity;
        } else {
            MassState state = mSpring.updateValues(mValue, mVelocity, deltaMillis);
            mValue = state.mValue;
            mVelocity = state.mVelocity;
        }
        mValue = Math.min(Math.max(mValue, mMinValue), mMaxValue);
        if (!isAtEquilibrium(mValue, mVelocity)) {
            return false;
        }
        mValue = mSpring.getFinalPosition();
        mVelocity = 0.0f;
        return true;
    }
}
