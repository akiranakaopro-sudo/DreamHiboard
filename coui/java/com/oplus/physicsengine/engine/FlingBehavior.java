package com.oplus.physicsengine.engine;

import android.graphics.RectF;

import com.oplus.physicsengine.common.Compat;

public final class FlingBehavior extends ConstraintBehavior {
    public float mCustomLinearDamping;
    public boolean mHasSetVelocity;
    public float mOriginLinearDamping;

    public FlingBehavior() {
        this(Float.MAX_VALUE, 0);
    }

    public FlingBehavior(int collisionMode, float minValue, float maxValue) {
        this(new RectF(minValue, minValue, maxValue, maxValue), collisionMode);
    }

    public FlingBehavior(int collisionMode, RectF rectF) {
        this(rectF, collisionMode);
    }

    public FlingBehavior(float size, int collisionMode) {
        this(new RectF(0.0f, 0.0f, size, size), collisionMode);
    }

    public FlingBehavior(RectF rectF, int collisionMode) {
        super(rectF, collisionMode);
        mOriginLinearDamping = 0.0f;
        mCustomLinearDamping = 0.0f;
        mHasSetVelocity = false;
    }

    public int getType() {
        return 2;
    }

    public void setActiveFrame(RectF activeFrame) {
        super.setConstraintRect(activeFrame);
    }

    public void setValueRange(float minValue, float maxValue) {
        setActiveFrame(new RectF(minValue, minValue, maxValue, maxValue));
    }

    public FlingBehavior setLinearDamping(float linearDamping) {
        mCustomLinearDamping = linearDamping;
        return this;
    }

    public void setFriction(float friction) {
        setLinearDamping(friction);
    }

    public void setMaxValue(float maxValue) {
        mConstraintRect.right = maxValue;
    }

    public void setMinValue(float minValue) {
        mConstraintRect.left = minValue;
    }

    public void setValueThreshold(float threshold) {
        mValueThreshold = threshold;
        if (mFirstProperty != null) {
            mFirstProperty.setValueThreshold(threshold);
        }
    }

    public void start() {
        super.startBehavior();
        if (mCustomLinearDamping != 0.0f && mPropertyBody != null) {
            mOriginLinearDamping = mPropertyBody.mLinearDamping;
            mPropertyBody.setLinearDamping(mCustomLinearDamping);
            if (mAssistBody != null) {
                mAssistBody.setLinearDamping(mCustomLinearDamping);
            }
        }
    }

    public void start(float velocity) {
        mHasSetVelocity = true;
        verifyBodyProperty();
        mPropertyBody.getLinearVelocity().mX = velocity / Compat.sPhysicalSizeToPixelsRatio;
        mPropertyBody.getLinearVelocity().mY = 0.0f;
        start();
        mHasSetVelocity = false;
    }

    public void stop() {
        stopBehavior();
    }

    public boolean stopBehavior() {
        if (mOriginLinearDamping != 0.0f && mPropertyBody != null) {
            mPropertyBody.setLinearDamping(mOriginLinearDamping);
            if (mAssistBody != null) {
                mAssistBody.setLinearDamping(mOriginLinearDamping);
            }
        }
        return super.stopBehavior();
    }

    public void updateStartVelocity() {
        if (!mHasSetVelocity) {
            super.updateStartVelocity();
        }
    }
}
