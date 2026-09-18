package com.oplus.physicsengine.engine;

import com.oplus.physicsengine.common.Compat;
import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.Body;
import com.oplus.physicsengine.dynamics.spring.Spring;
import com.oplus.physicsengine.dynamics.spring.SpringDef;

import java.util.HashMap;

public abstract class BaseBehavior {
    public UIItem mActiveUIItem;
    public PhysicalAnimator mAnimator;
    public FloatPropertyHolder mFirstProperty;
    public boolean mHasCustomStartVelocity;
    public boolean mIsSpringApplied;
    public boolean mIsStarted;
    public Body mPropertyBody;
    public HashMap<String, FloatPropertyHolder> mPropertyMap;
    public Spring mSpring;
    public SpringDef mSpringDef;
    public Runnable mStopAction;
    public Object mTarget;
    public float mValueThreshold = 1.0f;

    public void applySizeChanged(float width, float height) {
        if (mActiveUIItem != null) {
            mActiveUIItem.mWidth = width;
            mActiveUIItem.mHeight = height;
        }
        if (mPropertyBody != null) {
            float ratio = Compat.sPhysicalSizeToPixelsRatio;
            mPropertyBody.setSize(width / ratio, height / ratio);
            // Leapy modified 2026-07-29: Match OPPO by recalculating the active physical
            // frame immediately after a property-body size change.
            mPropertyBody.updateActiveRect(this);
            // Leapy end
        }
    }

    public Body copyBodyFromPropertyBody(String tag, Body body) {
        if (mPropertyBody == null) {
            verifyBodyProperty();
        }
        Body source = mPropertyBody;
        // Leapy modified 2026-07-28: Match OPPO's decoded assist-body creation path.
        // The assist body must belong to the same physics world or the constraint spring
        // cannot solve it. Reuse an existing body on subsequent property initialization.
        if (body == null) {
            body = mAnimator.createBody(source.mOriginPosition, source.getType(),
                    source.getProperty(), source.mWidth, source.mHeight, tag);
        } else {
            body.setSize(source.mWidth, source.mHeight);
        }
        body.setLinearVelocity(source.getLinearVelocity());
        body.setAwake(false);
        return body;
        // Leapy end
    }

    public Spring createSpring(SpringDef springDef, Body body) {
        if (mAnimator == null || springDef == null || body == null) {
            return null;
        }
        springDef.target.set(body.getWorldCenter());
        return mAnimator.createSpring(springDef);
    }

    public boolean applySpring(SpringDef springDef) {
        if (mIsSpringApplied) {
            return false;
        }
        mSpring = createSpring(springDef, mPropertyBody);
        if (mSpring == null) {
            return false;
        }
        mIsSpringApplied = true;
        return true;
    }

    public boolean removeSpring() {
        if (!mIsSpringApplied || mSpring == null || mAnimator == null) {
            return false;
        }
        mAnimator.mWorld.destroySpring(mSpring);
        mSpring = null;
        mIsSpringApplied = false;
        return true;
    }

    public void dispatchChanging() {
        // Leapy modified 2026-07-28: Match OPPO's decoded physics-to-UI transform.
        // PhysicalAnimator owns value/listener dispatch after this method; doing it here
        // skipped the body-position conversion and stranded max-level deformation.
        if (mActiveUIItem != null && mPropertyBody != null) {
            mActiveUIItem.mTransform.x =
                    (mPropertyBody.getPosition().mX - mPropertyBody.getHookPosition().mX)
                            * Compat.sPhysicalSizeToPixelsRatio;
            mActiveUIItem.mTransform.y =
                    (mPropertyBody.getPosition().mY - mPropertyBody.getHookPosition().mY)
                            * Compat.sPhysicalSizeToPixelsRatio;
        }
        // Leapy end
    }

    public Object getAnimatedValue() {
        if (mFirstProperty instanceof FloatValueHolder) {
            return ((FloatValueHolder) mFirstProperty).mValue;
        }
        if (mActiveUIItem != null) {
            return mActiveUIItem.mTransform.x;
        }
        return null;
    }

    public Vector getMoverTarget() {
        return mActiveUIItem == null ? null : mActiveUIItem.mMoveTarget;
    }

    public Body getPropertyBody() {
        return mPropertyBody;
    }

    public float getPropertyBodyLinearDamping() {
        return mPropertyBody == null ? 0.0f : mPropertyBody.mLinearDamping;
    }

    public Vector getPropertyBodyVelocity() {
        return mPropertyBody == null ? new Vector() : mPropertyBody.mLinearVelocity;
    }

    public Transform getTransform() {
        return mActiveUIItem == null ? null : mActiveUIItem.mTransform;
    }

    public abstract int getType();

    public abstract boolean isSteady();

    public abstract void linkGroundToSpring(Body body);

    public abstract void moveToStartValue();

    public abstract void onPropertyBodyCreated();

    public void onRemove() {
    }

    public BaseBehavior setStartVelocity(float velocity) {
        // Leapy modified 2026-07-29: Match OPPO's start-velocity contract. Constraint and
        // fling behaviors store a one-shot UI velocity; static behavior type 0 ignores it.
        if (getType() != 0) {
            mHasCustomStartVelocity = true;
            if (mActiveUIItem == null) {
                mActiveUIItem = new UIItem(mTarget);
            }
            mActiveUIItem.mStartVelocity.mX = velocity;
            mActiveUIItem.mStartVelocity.mY = 0.0f;
        }
        // Leapy end
        return this;
    }

    public BaseBehavior setSpringProperty(float frequency, float dampingRatio) {
        if (mSpringDef != null) {
            mSpringDef.frequencyHz = frequency;
            mSpringDef.dampingRatio = dampingRatio;
            if (mSpring != null) {
                mSpring.setFrequency(frequency);
                mSpring.setDampingRatio(dampingRatio);
            }
        }
        return this;
    }

    public BaseBehavior setTarget(Object target) {
        mTarget = target;
        verifyBodyProperty();
        return this;
    }

    public BaseBehavior addProperty(FloatPropertyHolder... properties) {
        withProperty(properties);
        return this;
    }

    public void startBehavior() {
        if (mIsStarted) {
            return;
        }
        verifyBodyProperty();
        updateStartVelocity();
        // Leapy added 2026-07-28: Restore the decoded OPPO physics start sequence.
        // COUI's seekbar rebound supplies a new FloatValueHolder start value for every
        // release, so the UI item and physics body must be moved to that value before
        // the first animation frame is dispatched.
        if (mPropertyMap == null) {
            mActiveUIItem.mStartPosition.mX = mActiveUIItem.mTransform.x;
            mActiveUIItem.mStartPosition.mY = mActiveUIItem.mTransform.y;
        } else {
            for (FloatPropertyHolder property : mPropertyMap.values()) {
                if (property != null) {
                    FloatValueHolder valueHolder = (FloatValueHolder) property;
                    if (!valueHolder.mIsStartValueSet) {
                        valueHolder.mStartValue = valueHolder.mValue;
                    }
                    mActiveUIItem.mStartPosition.mX = valueHolder.mStartValue;
                }
            }
        }
        moveToStartValue();
        dispatchChanging();
        if (mAnimator != null) {
            PhysicalAnimator.updateValue(this);
            mAnimator.startBehavior(this);
        } else {
            mIsStarted = true;
        }
        // Leapy end
    }

    public boolean stopBehavior() {
        if (!mIsStarted) {
            return false;
        }
        // Leapy modified 2026-07-29: Match OPPO's exact stop order. Do not zero the
        // physical velocity: OPPO lets the steady-state solver retain its terminal value,
        // then clears only the next-run UI start velocity.
        if (getType() != 0 && mActiveUIItem != null) {
            mActiveUIItem.mStartVelocity.setZero();
        }
        if (mAnimator != null) {
            mAnimator.mCurrentRunningBehaviors.remove(this);
            AnimationListener listener = mAnimator.mAnimationListeners == null
                    ? null : mAnimator.mAnimationListeners.get(this);
            if (listener != null) {
                listener.onAnimationEnd(this);
            }
        }
        mIsStarted = false;
        if (mStopAction != null) {
            mStopAction.run();
        }
        // Leapy end
        return true;
    }

    public String toString() {
        return getClass().getSimpleName() + "{mTarget=" + mTarget + ", mPropertyBody="
                + mPropertyBody + ", mIsStarted=" + mIsStarted + "}@" + hashCode();
    }

    public void updateStartVelocity() {
        // Leapy modified 2026-07-29: OPPO applies start velocity only when explicitly
        // requested. A zero-valued stale UI vector must not overwrite rebound body state.
        if (mHasCustomStartVelocity && mActiveUIItem != null && mPropertyBody != null) {
            mHasCustomStartVelocity = false;
            float ratio = Compat.sPhysicalSizeToPixelsRatio;
            mPropertyBody.mLinearVelocity.mX = mActiveUIItem.mStartVelocity.mX / ratio;
            mPropertyBody.mLinearVelocity.mY = mActiveUIItem.mStartVelocity.mY / ratio;
        }
        // Leapy end
    }

    public final void verifyBodyProperty() {
        if (mFirstProperty == null) {
            FloatValueHolder valueHolder = new FloatValueHolder();
            withProperty(valueHolder);
        }
        if (mActiveUIItem == null) {
            mActiveUIItem = new UIItem(mTarget);
        }
        if (mFirstProperty instanceof FloatValueHolder) {
            FloatValueHolder valueHolder = (FloatValueHolder) mFirstProperty;
            float startValue = valueHolder.mIsStartValueSet ? valueHolder.mStartValue : valueHolder.mValue;
            mActiveUIItem.mTransform.x = startValue / valueHolder.mValueThreshold;
            mActiveUIItem.mMoveTarget.mX = mActiveUIItem.mTransform.x;
            mValueThreshold = valueHolder.mValueThreshold;
        }
        if (mPropertyBody == null) {
            Vector position = new Vector(mActiveUIItem.mTransform.x / Compat.sPhysicalSizeToPixelsRatio,
                    mActiveUIItem.mTransform.y / Compat.sPhysicalSizeToPixelsRatio);
            mPropertyBody = mAnimator == null
                    ? new Body(position, 2, 0, 0.0f, 0.0f)
                    : mAnimator.createBody(position, 2, 0, 0.0f, 0.0f, "PropertyBody");
            onPropertyBodyCreated();
        }
    }

    public final void withProperty(FloatPropertyHolder... properties) {
        if (mPropertyMap == null) {
            mPropertyMap = new HashMap<>(properties.length);
        }
        for (FloatPropertyHolder property : properties) {
            if (property != null) {
                if (mFirstProperty == null) {
                    mFirstProperty = property;
                }
                mPropertyMap.put(property.mPropertyName, property);
            }
        }
    }

    public Object getAnimatedValueCompat() {
        return getAnimatedValue();
    }

    public Transform getTransformCompat() {
        return getTransform();
    }

    public int getTypeCompat() {
        return getType();
    }

    public boolean isSteadyCompat() {
        return isSteady();
    }

}
