package com.oplus.physicsengine.engine;

import com.oplus.physicsengine.common.Compat;
import com.oplus.physicsengine.common.MathUtils;
import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.Body;
import com.oplus.physicsengine.dynamics.spring.Spring;
import com.oplus.physicsengine.dynamics.spring.SpringDef;

public final class DragBehavior extends BaseBehavior {
    public boolean mIsDragging;
    public boolean mIsEnableOut;
    public Body mSimulateBody;
    public Spring mSimulateSpring;
    public final SpringDef mSimulateSpringDef;

    public DragBehavior() {
        mIsDragging = false;
        mIsEnableOut = true;
        mSpringDef = new SpringDef();
        mSpringDef.frequencyHz = 4.0f;
        mSpringDef.dampingRatio = 0.2f;
        mSimulateSpringDef = new SpringDef();
        mSimulateSpringDef.frequencyHz = 2000000.0f;
        mSimulateSpringDef.dampingRatio = 100.0f;
    }

    public void applySizeChanged(float width, float height) {
        super.applySizeChanged(width, height);
        if (mSimulateBody != null && mPropertyBody != null) {
            mSimulateBody.setSize(mPropertyBody.mWidth, mPropertyBody.mHeight);
        }
    }

    public void beginDrag(float x, float currentX) {
        beginDrag(x, 0.0f, currentX, 0.0f);
    }

    public void beginDrag(float x, float y, float currentX, float currentY) {
        verifyBodyProperty();
        float ratio = Compat.sPhysicalSizeToPixelsRatio;
        mPropertyBody.mHookPosition.mX = (x - currentX) / ratio;
        mPropertyBody.mHookPosition.mY = (y - currentY) / ratio;
        mPropertyBody.updateActiveRect(this);
        mPropertyBody.mLinearVelocity.setZero();
        if (mSimulateBody != null) {
            mSimulateBody.mLinearVelocity.setZero();
        }
        mActiveUIItem.mMoveTarget.mX = getFixedXInActive(x / ratio);
        mActiveUIItem.mMoveTarget.mY = getFixedYInActive(y / ratio);
        moveBodiesTo(mActiveUIItem.mMoveTarget);
        mIsDragging = true;
        startBehavior();
    }

    public void dragTo(float x) {
        dragTo(x, 0.0f);
    }

    public void dragTo(float x, float y) {
        verifyBodyProperty();
        if (mSpring != null) {
            float ratio = Compat.sPhysicalSizeToPixelsRatio;
            mActiveUIItem.mMoveTarget.mX = getFixedXInActive(x / ratio);
            mActiveUIItem.mMoveTarget.mY = getFixedYInActive(y / ratio);
            mSpring.setTarget(mActiveUIItem.mMoveTarget);
            if (mSimulateSpring != null) {
                mSimulateSpring.setTarget(mActiveUIItem.mMoveTarget);
            }
        }
    }

    public void endDrag(float xVelocity) {
        endDrag(xVelocity, 0.0f);
    }

    public void endDrag(float xVelocity, float yVelocity) {
        verifyBodyProperty();
        detachDragSprings();
        float ratio = Compat.sPhysicalSizeToPixelsRatio;
        xVelocity /= ratio;
        yVelocity /= ratio;
        if (mSimulateBody != null) {
            float bodyXVelocity = mSimulateBody.mLinearVelocity.mX;
            xVelocity = bodyXVelocity == 0.0f ? 0.0f
                    : (bodyXVelocity / MathUtils.abs(bodyXVelocity)) * MathUtils.abs(xVelocity);
            float bodyYVelocity = mSimulateBody.mLinearVelocity.mY;
            yVelocity = bodyYVelocity == 0.0f ? 0.0f
                    : (bodyYVelocity / MathUtils.abs(bodyYVelocity)) * MathUtils.abs(yVelocity);
        }
        mActiveUIItem.mStartVelocity.mX = xVelocity * ratio;
        mActiveUIItem.mStartVelocity.mY = yVelocity * ratio;
        mIsDragging = false;
        mPropertyBody.clearActiveRect(this);
    }

    public float getFixedXInActive(float x) {
        if (mIsEnableOut || mPropertyBody == null || mPropertyBody.mActiveRect == null
                || (!mIsStarted && mPropertyBody.mActiveRect.isEmpty())) {
            return x;
        }
        return Math.max(mPropertyBody.mActiveRect.left, Math.min(mPropertyBody.mActiveRect.right, x));
    }

    public float getFixedYInActive(float y) {
        if (mIsEnableOut || mPropertyBody == null || mPropertyBody.mActiveRect == null
                || (!mIsStarted && mPropertyBody.mActiveRect.isEmpty())) {
            return y;
        }
        return Math.max(mPropertyBody.mActiveRect.top, Math.min(mPropertyBody.mActiveRect.bottom, y));
    }

    public int getType() {
        return 0;
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean isSteady() {
        return !mIsDragging;
    }

    public void linkGroundToSpring(Body body) {
        mSpringDef.bodyA = mPropertyBody;
        mSpringDef.bodyB = body;
        mSimulateSpringDef.bodyA = body;
    }

    public void moveToStartValue() {
    }

    public void onPropertyBodyCreated() {
        mPropertyBody.setActiveConstraintFrequency(mSpringDef.frequencyHz);
        mSimulateBody = copyBodyFromPropertyBody("SimulateTouch", mSimulateBody);
        mSimulateSpringDef.bodyB = mSimulateBody;
    }

    public void onRemove() {
        if (mAnimator != null && mSimulateBody != null) {
            mAnimator.destroyBody(mSimulateBody);
        }
        mSimulateBody = null;
        mSimulateSpring = null;
    }

    public DragBehavior setEnableOut(boolean enableOut) {
        mIsEnableOut = enableOut;
        return this;
    }

    public DragBehavior setSpringProperty(float frequency, float dampingRatio) {
        super.setSpringProperty(frequency, dampingRatio);
        if (mPropertyBody != null) {
            mPropertyBody.setActiveConstraintFrequency(frequency);
        }
        return this;
    }

    public boolean stopBehavior() {
        detachDragSprings();
        mIsDragging = false;
        return super.stopBehavior();
    }

    public void startBehavior() {
        super.startBehavior();
        attachDragSprings();
    }

    public DragBehavior withProperty(FloatValueHolder valueHolder) {
        super.withProperty(valueHolder);
        return this;
    }

    private void attachDragSprings() {
        if (applySpring(mSpringDef)) {
            mSpring.setTarget(mActiveUIItem.mMoveTarget);
            mSimulateSpring = createSpring(mSimulateSpringDef, mSimulateBody);
            if (mSimulateSpring != null) {
                mSimulateSpring.setTarget(mActiveUIItem.mMoveTarget);
                mSimulateBody.setAwake(true);
            }
        }
    }

    private void detachDragSprings() {
        if (removeSpring() && mAnimator != null) {
            mAnimator.mWorld.destroySpring(mSimulateSpring);
            mSimulateSpring = null;
            if (mSimulateBody != null) {
                mSimulateBody.setAwake(false);
            }
        }
    }

    private void moveBodiesTo(Vector position) {
        mPropertyBody.setPosition(position);
        if (mSimulateBody != null) {
            mSimulateBody.setPosition(position);
        }
    }

}
