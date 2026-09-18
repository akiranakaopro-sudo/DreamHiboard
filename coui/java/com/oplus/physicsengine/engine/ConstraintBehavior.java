package com.oplus.physicsengine.engine;

import android.graphics.RectF;

import com.oplus.physicsengine.common.Compat;
import com.oplus.physicsengine.common.MathUtils;
import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.Body;
import com.oplus.physicsengine.dynamics.spring.SpringDef;

public class ConstraintBehavior extends BaseBehavior {
    public Body mAssistBody;
    public int mCollisionMode;
    public float mConstraintPointX;
    public float mConstraintPointY;
    public final RectF mConstraintRect;
    public int mOverBoundsState;
    public boolean mShouldFixXSide;
    public boolean mShouldFixYSide;

    public ConstraintBehavior(RectF constraintRect, int collisionMode) {
        mConstraintRect = new RectF();
        mCollisionMode = collisionMode;
        setConstraintRect(constraintRect);
        // Leapy modified 2026-07-28: Match OPPO's decoded collision spring defaults.
        if (isCollisionLimitMode()) {
            mSpringDef = new SpringDef();
            mSpringDef.frequencyHz = 1.0f;
            mSpringDef.dampingRatio = 0.4f;
        }
        // Leapy end
    }

    public void applySizeChanged(float width, float height) {
        super.applySizeChanged(width, height);
        if (mAssistBody != null && mPropertyBody != null) {
            mAssistBody.setSize(mPropertyBody.mWidth, mPropertyBody.mHeight);
        }
    }

    public void calculateConstraintPosition() {
        if (mPropertyBody == null) {
            return;
        }
        // Leapy modified 2026-07-28: Match OPPO's physical-coordinate constraint state.
        mShouldFixXSide = isOverXBounds();
        mShouldFixYSide = isOverYBounds();
        mConstraintPointX = getFixedXInActive(mPropertyBody.getPosition().mX);
        mConstraintPointY = getFixedYInActive(mPropertyBody.getPosition().mY);
        // Leapy end
    }

    public void checkOverBoundsState(float x, float y) {
        // Leapy modified 2026-07-28: Preserve OPPO's four directional bound flags.
        mOverBoundsState = 0;
        RectF activeRect = mPropertyBody == null ? null : mPropertyBody.mActiveRect;
        if (activeRect == null || (!mIsStarted && activeRect.isEmpty())) {
            return;
        }
        if (x < activeRect.left) {
            mOverBoundsState |= 1;
        } else if (x > activeRect.right) {
            mOverBoundsState |= 4;
        }
        if (y < activeRect.top) {
            mOverBoundsState |= 2;
        } else if (y > activeRect.bottom) {
            mOverBoundsState |= 8;
        }
        // Leapy end
    }

    public void dispatchChanging() {
        // Leapy modified 2026-07-28: Refresh OPPO's bound flags before every transform.
        if (mPropertyBody != null && mPropertyBody.mActiveRect != null) {
            checkOverBoundsState(mPropertyBody.getPosition().mX,
                    mPropertyBody.getPosition().mY);
        }
        // Leapy end
        handlePositionChanging();
        super.dispatchChanging();
    }

    public float getFixedXInActive(float x) {
        // Leapy modified 2026-07-28: Clamp against OPPO's active physical rectangle.
        RectF activeRect = mPropertyBody == null ? null : mPropertyBody.mActiveRect;
        if (activeRect != null && (mIsStarted || !activeRect.isEmpty())) {
            if (x < activeRect.left) {
                return activeRect.left;
            }
            if (x > activeRect.right) {
                return activeRect.right;
            }
        }
        return x;
        // Leapy end
    }

    public float getFixedYInActive(float y) {
        // Leapy modified 2026-07-28: Clamp against OPPO's active physical rectangle.
        RectF activeRect = mPropertyBody == null ? null : mPropertyBody.mActiveRect;
        if (activeRect != null && (mIsStarted || !activeRect.isEmpty())) {
            if (y < activeRect.top) {
                return activeRect.top;
            }
            if (y > activeRect.bottom) {
                return activeRect.bottom;
            }
        }
        return y;
        // Leapy end
    }

    public int getType() {
        return 1;
    }

    public void handlePositionChanging() {
        if (mPropertyBody == null || mActiveUIItem == null) {
            return;
        }
        // Leapy modified 2026-07-28: Port OPPO's decoded collision-mode 4 path used
        // by COUIVerticalSeekBar. The assist body supplies elastic overscroll while
        // the spring target remains clamped to the active frame.
        if (mCollisionMode == 4 && mAssistBody != null) {
            Vector moveTarget = mActiveUIItem.mMoveTarget;
            moveTarget.set(mPropertyBody.getPosition());
            if (!mShouldFixXSide) {
                mConstraintPointX = getFixedXInActive(moveTarget.mX);
            } else {
                moveTarget.mX = mAssistBody.getPosition().mX;
            }
            mShouldFixXSide = isOverXBounds();
            if (!mShouldFixYSide) {
                mConstraintPointY = getFixedYInActive(moveTarget.mY);
            } else {
                moveTarget.mY = mAssistBody.getPosition().mY;
            }
            mShouldFixYSide = isOverYBounds();
            transform(moveTarget);
            return;
        }
        // Leapy end

        // Preserve the existing behavior for collision modes not used by the volume seekbar.
        float ratio = Compat.sPhysicalSizeToPixelsRatio;
        float x = mPropertyBody.mWorldCenter.mX * ratio;
        float y = mPropertyBody.mWorldCenter.mY * ratio;
        checkOverBoundsState(x, y);
        if (mShouldFixXSide) {
            mPropertyBody.mWorldCenter.mX = getFixedXInActive(x) / ratio;
            mPropertyBody.mLinearVelocity.mX = 0.0f;
        }
        if (mShouldFixYSide) {
            mPropertyBody.mWorldCenter.mY = getFixedYInActive(y) / ratio;
            mPropertyBody.mLinearVelocity.mY = 0.0f;
        }
    }

    public boolean isCollisionLimitMode() {
        // Leapy modified 2026-07-28: Match OPPO's decoded supported collision modes.
        return mCollisionMode == 1 || mCollisionMode == 2
                || mCollisionMode == 3 || mCollisionMode == 4;
        // Leapy end
    }

    public boolean isOverXBounds() {
        return (mOverBoundsState & 1) != 0 || (mOverBoundsState & 4) != 0;
    }

    public boolean isOverYBounds() {
        return (mOverBoundsState & 2) != 0 || (mOverBoundsState & 8) != 0;
    }

    public boolean isSteady() {
        if (mPropertyBody == null) {
            return true;
        }
        boolean velocitySteady =
                MathUtils.abs(mPropertyBody.mLinearVelocity.mX) < Compat.sSteadyAccuracy
                        && MathUtils.abs(mPropertyBody.mLinearVelocity.mY)
                        < Compat.sSteadyAccuracy;
        // Leapy modified 2026-07-28: OPPO waits for both velocity and spring distance.
        if (isCollisionLimitMode() && velocitySteady && mSpring != null) {
            Vector position = mPropertyBody.getPosition();
            Vector target = mSpring.getTarget();
            return MathUtils.abs(target.mX - position.mX)
                    + MathUtils.abs(target.mY - position.mY) < Compat.sSteadyAccuracy;
        }
        // Leapy end
        return velocitySteady;
    }

    public void linkGroundToSpring(Body body) {
        // Leapy modified 2026-07-28: Match OPPO's decoded ground spring endpoint.
        if (isCollisionLimitMode() && mSpringDef != null) {
            mSpringDef.bodyA = body;
            body.setAwake(true);
        }
        // Leapy end
    }

    public void moveToStartValue() {
        // Leapy modified 2026-07-28: Port OPPO's decoded start-value coordinate transform.
        if (mActiveUIItem != null && mPropertyBody != null) {
            Vector moveTarget = mActiveUIItem.mMoveTarget;
            moveTarget.mX = (mActiveUIItem.mStartPosition.mX
                    / Compat.sPhysicalSizeToPixelsRatio
                    + mPropertyBody.getHookPosition().mX) / mValueThreshold;
            moveTarget.mY = (mActiveUIItem.mStartPosition.mY
                    / Compat.sPhysicalSizeToPixelsRatio
                    + mPropertyBody.getHookPosition().mY) / mValueThreshold;
            mPropertyBody.setPosition(moveTarget);
            if (mAssistBody != null) {
                mAssistBody.setPosition(moveTarget);
            }
        }
        // Leapy end
    }

    public void onPropertyBodyCreated() {
        // Leapy modified 2026-07-28: Create OPPO's world-owned assist body and active frame.
        if (mPropertyBody != null && !mConstraintRect.isEmpty()) {
            mPropertyBody.setOriginActiveRect(mConstraintRect);
            mPropertyBody.updateActiveRect(this);
            if (isCollisionLimitMode()
                    && mPropertyBody.mActiveConstraintFrequency == 50.0f) {
                mPropertyBody.setActiveConstraintFrequency(mSpringDef.frequencyHz);
            }
        }
        if (mSpringDef != null) {
            mAssistBody = copyBodyFromPropertyBody("Assist", mAssistBody);
            mSpringDef.bodyB = mAssistBody;
        }
        // Leapy end
    }

    public void onRemove() {
        // Leapy modified 2026-07-28: Match OPPO's decoded constraint-body cleanup.
        super.onRemove();
        if (mPropertyBody != null && mPropertyBody.mOriginActiveRect != null
                && !mPropertyBody.mOriginActiveRect.isEmpty()
                && mPropertyBody.mActiveConstraintOwner == this) {
            mPropertyBody.mOriginActiveRect = null;
            mPropertyBody.mActiveRect = null;
            mPropertyBody.setActiveConstraintFrequency(50.0f);
        }
        if (isCollisionLimitMode()) {
            if (mIsSpringApplied && mSpring != null && mAnimator != null) {
                mAnimator.mWorld.destroySpring(mSpring);
                mSpring = null;
                mIsSpringApplied = false;
            }
            mOverBoundsState = 0;
            mShouldFixXSide = false;
            mShouldFixYSide = false;
            if (mAnimator != null && mAssistBody != null) {
                mAnimator.destroyBody(mAssistBody);
            }
        }
        mAssistBody = null;
        // Leapy end
    }

    public void setCollisionLimitMode(int collisionMode) {
        // Leapy modified 2026-07-29: Match OPPO; a non-collision behavior cannot be
        // converted into a collision behavior (or vice versa) after construction.
        if (mCollisionMode != 0 && collisionMode != 0) {
            mCollisionMode = collisionMode;
        }
        // Leapy end
    }

    public void setConstraintRect(RectF constraintRect) {
        if (constraintRect == null || constraintRect.isEmpty()) {
            return;
        }
        mConstraintRect.set(constraintRect);
        if (mPropertyBody != null) {
            mPropertyBody.setOriginActiveRect(mConstraintRect);
            // Leapy modified 2026-07-29: Match OPPO's immediate active-frame refresh.
            mPropertyBody.updateActiveRect(this);
            // Leapy end
        }
    }

    public ConstraintBehavior setSpringProperty(float frequency, float dampingRatio) {
        // Leapy modified 2026-07-29: Match OPPO's collision-body constraint frequency
        // update before changing the rebound spring definition.
        if (mPropertyBody != null && isCollisionLimitMode()
                && mPropertyBody.mActiveConstraintFrequency == 50.0f) {
            mPropertyBody.setActiveConstraintFrequency(frequency);
        }
        // Leapy end
        if (mSpringDef == null) {
            mSpringDef = new SpringDef();
        }
        mSpringDef.frequencyHz = frequency;
        mSpringDef.dampingRatio = dampingRatio;
        if (mSpring != null) {
            mSpring.setFrequency(frequency);
            mSpring.setDampingRatio(dampingRatio);
        }
        return this;
    }

    public void startBehavior() {
        super.startBehavior();
        // Leapy modified 2026-07-28: Port OPPO's decoded assist-body rebound setup.
        if (mPropertyBody == null || !mPropertyBody.updateActiveRect(this)
                || !isCollisionLimitMode() || mAssistBody == null) {
            return;
        }
        checkOverBoundsState(mPropertyBody.getPosition().mX,
                mPropertyBody.getPosition().mY);
        calculateConstraintPosition();
        mAssistBody.setAwake(true);
        mAssistBody.setLinearVelocity(mPropertyBody.getLinearVelocity());
        mAssistBody.setPosition(mPropertyBody.getPosition());
        if (!mIsSpringApplied) {
            mSpring = createSpring(mSpringDef, mPropertyBody);
            if (mSpring != null) {
                mIsSpringApplied = true;
                mSpring.mTarget.mX = mConstraintPointX;
                mSpring.mTarget.mY = mConstraintPointY;
            }
        }
        // Leapy end
    }

    public boolean stopBehavior() {
        // Leapy modified 2026-07-28: Tear down OPPO's active frame and assist spring.
        if (mPropertyBody != null && mPropertyBody.mActiveRect != null
                && mPropertyBody.mActiveConstraintOwner == this) {
            mPropertyBody.mActiveRect.setEmpty();
        }
        if (isCollisionLimitMode()) {
            if (mIsSpringApplied && mSpring != null && mAnimator != null) {
                mAnimator.mWorld.destroySpring(mSpring);
                mSpring = null;
                mIsSpringApplied = false;
            }
            mOverBoundsState = 0;
            mShouldFixXSide = false;
            mShouldFixYSide = false;
            if (mAssistBody != null) {
                mAssistBody.setAwake(false);
            }
        }
        // Leapy end
        return super.stopBehavior();
    }

    public String toString() {
        return "ConstraintBehavior{mConstraintRect=" + mConstraintRect + ", mCollisionMode="
                + mCollisionMode + ", mOverBoundsState=" + mOverBoundsState + "}@"
                + hashCode();
    }

    public void transform(Vector vector) {
        // Leapy modified 2026-07-28: Match OPPO's decoded transform and spring-target update.
        if (vector != null && mPropertyBody != null) {
            mPropertyBody.setPosition(vector);
            if (mSpring != null) {
                mSpring.mTarget.mX = mConstraintPointX;
                mSpring.mTarget.mY = mConstraintPointY;
                if (mAssistBody != null) {
                    mAssistBody.setPosition(vector);
                }
            }
        }
        // Leapy end
    }
}
