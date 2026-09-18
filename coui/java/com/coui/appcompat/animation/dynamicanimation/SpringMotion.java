package com.coui.appcompat.animation.dynamicanimation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;

public abstract class SpringMotion extends COUIDynamicAnimation<SpringMotion> {
    private static final int LOGIC_END_EXTRA_TIME_MS = 50;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final float UNSET = Float.MAX_VALUE;
    private boolean mEndRequested;
    private float mPendingPosition;
    private COUISpringForce mSpring;

    public SpringMotion(FloatValueHolder valueHolder) {
        super(valueHolder);
        this.mSpring = null;
        this.mPendingPosition = UNSET;
        this.mEndRequested = false;
    }

    private void onStartAnimation(SpringMotion springMotion) {
        if (springMotion == null || !springMotion.isRunning()) {
            getSpring().setBlendDuration(0.0f);
            return;
        }
        setStartValue(springMotion.mValue);
        setStartVelocity(springMotion.mVelocity);
        float response = springMotion.getSpring().getResponse();
        getSpring().updateResponse(response);
        if (response == getSpring().getFinalResponse()) {
            getSpring().setBlendDuration(0.0f);
        } else if (getSpring().getBlendDuration() == 0.0f) {
            getSpring().updateResponse(getSpring().getFinalResponse());
        } else if (getSpring().getFinalResponse() == springMotion.getSpring().getFinalResponse()) {
            getSpring().setBlendDuration(springMotion.getSpring().getBlendDuration());
        }
    }

    private void sanityCheck() {
        COUISpringForce spring = this.mSpring;
        if (spring == null) {
            throw new UnsupportedOperationException("Incomplete SpringAnimation: Either final position or a spring force needs to be set.");
        }
        double finalPosition = spring.getFinalPosition();
        if (finalPosition > this.mMaxValue) {
            throw new UnsupportedOperationException("Final position of the spring cannot be greater than the max value.");
        }
        if (finalPosition < this.mMinValue) {
            throw new UnsupportedOperationException("Final position of the spring cannot be less than the min value.");
        }
    }

    public void animateToFinalPosition(float finalPosition) {
        if (isRunning()) {
            this.mPendingPosition = finalPosition;
            return;
        }
        if (this.mSpring == null) {
            this.mSpring = new COUISpringForce(finalPosition);
        }
        this.mSpring.setFinalPosition(finalPosition);
        start();
    }

    public boolean canSkipToEnd() {
        return this.mSpring.mDampingRatio > 0.0d;
    }

    public void cancelComplete() {
        super.cancel();
        this.mPendingPosition = UNSET;
        this.mVelocity = 0.0f;
        this.mEndRequested = false;
    }

    @Override
    public float getAcceleration(float value, float velocity) {
        return this.mSpring.getAcceleration(value, velocity);
    }

    public COUISpringForce getSpring() {
        return this.mSpring;
    }

    @Override
    public boolean isAtEquilibrium(float value, float velocity) {
        return this.mSpring.isAtEquilibrium(value, velocity);
    }

    @Override
    public boolean isLogicEnd(long elapsedMillis) {
        return ((float) elapsedMillis) >= (getSpring().getFinalResponse() * MILLISECONDS_PER_SECOND) + LOGIC_END_EXTRA_TIME_MS;
    }

    @Override
    public void onAnimate(long elapsedMillis) {
        super.onAnimate(elapsedMillis);
        float blendDuration = getSpring().getBlendDuration();
        if (blendDuration > 0.0f) {
            float elapsed = elapsedMillis;
            setStartValue(this.mValue);
            setStartVelocity(this.mVelocity);
            if (elapsed >= blendDuration) {
                getSpring().updateResponse(getSpring().getFinalResponse());
                getSpring().setBlendDuration(0.0f);
                return;
            }
            float response = getSpring().getResponse();
            getSpring().updateResponse(response + ((getSpring().getFinalResponse() - response) * (elapsed / blendDuration)));
            getSpring().setBlendDuration(blendDuration - elapsed);
        }
    }

    public void reset() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException("Spring animations can only come to an end when there is damping");
        }
        cancel();
        float pendingPosition = this.mPendingPosition;
        if (pendingPosition != UNSET) {
            this.mSpring.setFinalPosition(pendingPosition);
            this.mPendingPosition = UNSET;
        }
        this.mValue = this.mSpring.getFinalPosition();
        this.mVelocity = 0.0f;
        this.mEndRequested = false;
    }

    public SpringMotion setSpring(COUISpringForce spring) {
        this.mSpring = spring;
        return this;
    }

    @Override
    public void setValueThreshold(float threshold) {
    }

    public void skipToEnd() {
        if (!canSkipToEnd()) {
            throw new UnsupportedOperationException("Spring animations can only come to an end when there is damping");
        }
        if (!this.mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        }
        if (this.mRunning) {
            this.mEndRequested = true;
        }
    }

    @Override
    public void start() {
        sanityCheck();
        this.mSpring.setValueThreshold(getValueThreshold());
        super.start();
    }

    @Override
    public boolean updateValueAndVelocity(long deltaMillis) {
        if (this.mEndRequested) {
            float pendingPosition = this.mPendingPosition;
            if (pendingPosition != UNSET) {
                this.mSpring.setFinalPosition(pendingPosition);
                this.mPendingPosition = UNSET;
            }
            this.mValue = this.mSpring.getFinalPosition();
            this.mVelocity = 0.0f;
            this.mEndRequested = false;
            return true;
        }
        if (this.mPendingPosition != UNSET) {
            this.mSpring.getFinalPosition();
            long halfDeltaMillis = deltaMillis / 2;
            COUIDynamicAnimation.MassState firstHalfState = this.mSpring.updateValues(this.mValue, this.mVelocity, halfDeltaMillis);
            this.mSpring.setFinalPosition(this.mPendingPosition);
            this.mPendingPosition = UNSET;
            COUIDynamicAnimation.MassState secondHalfState = this.mSpring.updateValues(firstHalfState.mValue, firstHalfState.mVelocity, halfDeltaMillis);
            this.mValue = secondHalfState.mValue;
            this.mVelocity = secondHalfState.mVelocity;
        } else {
            COUIDynamicAnimation.MassState updatedState = this.mSpring.updateValues(this.mValue, this.mVelocity, deltaMillis);
            this.mValue = updatedState.mValue;
            this.mVelocity = updatedState.mVelocity;
        }
        float minBoundedValue = Math.max(this.mValue, this.mMinValue);
        this.mValue = minBoundedValue;
        float boundedValue = Math.min(minBoundedValue, this.mMaxValue);
        this.mValue = boundedValue;
        if (!isAtEquilibrium(boundedValue, this.mVelocity)) {
            return false;
        }
        this.mValue = this.mSpring.getFinalPosition();
        this.mVelocity = 0.0f;
        return true;
    }

    public <K> SpringMotion(K target, FloatPropertyCompat<K> property) {
        super(target, property);
        this.mSpring = null;
        this.mPendingPosition = UNSET;
        this.mEndRequested = false;
    }

    public <K> SpringMotion(K target, FloatPropertyCompat<K> property, float finalPosition) {
        super(target, property);
        this.mSpring = null;
        this.mPendingPosition = UNSET;
        this.mEndRequested = false;
        this.mSpring = new COUISpringForce(finalPosition);
    }

    public void start(SpringMotion springMotion) {
        start();
        if (isRunning()) {
            return;
        }
        onStartAnimation(springMotion);
    }
}

