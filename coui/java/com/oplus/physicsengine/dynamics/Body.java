package com.oplus.physicsengine.dynamics;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.oplus.physicsengine.common.Compat;
import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.spring.Edge;
import com.oplus.physicsengine.engine.BaseBehavior;

public final class Body {
    public float mActiveConstraintFrequency = 50.0f;
    public BaseBehavior mActiveConstraintOwner;
    public RectF mActiveRect;
    public float mDensity;
    public Edge mEdgeList;
    public final Vector mForce = new Vector();
    public boolean mHasSetCenter = false;
    public float mHeight;
    public final Vector mHookPosition = new Vector(0.0f, 0.0f);
    public float mInvMass;
    public boolean mIsAwake = false;
    public boolean mIsSolved = false;
    public float mLinearDamping;
    public final Vector mLinearVelocity = new Vector();
    public float mMass;
    public final Vector mMassCenter = new Vector();
    public Body mNext;
    public RectF mOriginActiveRect;
    public final Vector mOriginPosition = new Vector();
    public Body mPrev;
    public int mProperty;
    public String mTag = "";
    public int mType;
    public float mWidth;
    public final Vector mWorldCenter = new Vector();

    public Body(Vector position, int type, int property, float width, float height) {
        setType(type);
        setProperty(property);
        mOriginPosition.set(position);
        mDensity = 1.0f;
        setSize(width, height);
        // Leapy modified 2026-07-29: Match OPPO Body constructor smali. The mass centre is
        // initialized by the first setSize only; later size changes preserve that centre
        // unless this is the position property body.
        mHasSetCenter = true;
        // Leapy end
    }

    private void setMass(float mass) {
        // Leapy modified 2026-07-29: Match OPPO's minimum unit mass and reciprocal mass.
        mMass = Math.max(1.0f, mass);
        mInvMass = 1.0f / mMass;
        // Leapy end
    }

    private void setProperty(int property) {
        mProperty = property;
    }

    private void setType(int type) {
        mType = type;
    }

    public Vector getHookPosition() {
        return mHookPosition;
    }

    public float getLinearDamping() {
        return mLinearDamping;
    }

    public Vector getLinearVelocity() {
        return mLinearVelocity;
    }

    public float getMass() {
        return mMass;
    }

    public Vector getPosition() {
        return mOriginPosition;
    }

    public int getProperty() {
        return mProperty;
    }

    public String getTag() {
        return mTag;
    }

    public int getType() {
        return mType;
    }

    public Vector getWorldCenter() {
        return mWorldCenter;
    }

    public void setActiveConstraintFrequency(float frequency) {
        mActiveConstraintFrequency = frequency;
    }

    public void setAwake(boolean awake) {
        mIsAwake = awake;
    }

    public void setDensity(float density) {
        // Leapy modified 2026-07-29: OPPO stores density here; mass is recalculated by
        // setSize, not immediately during a running rebound.
        mDensity = density;
        // Leapy end
    }

    public void setLinearDamping(float damping) {
        mLinearDamping = damping;
    }

    public void setLinearVelocity(Vector velocity) {
        // Leapy modified 2026-07-29: OPPO never assigns velocity to the static ground body.
        if (mType == 0) {
            return;
        }
        mLinearVelocity.set(velocity);
        // Leapy end
    }

    public void setOriginActiveRect(RectF rect) {
        if (rect == null || rect.isEmpty()) {
            return;
        }
        if (mOriginActiveRect == null) {
            mOriginActiveRect = new RectF();
        }
        float ratio = Compat.sPhysicalSizeToPixelsRatio;
        mOriginActiveRect.set(rect.left / ratio, rect.top / ratio, rect.right / ratio, rect.bottom / ratio);
    }

    public void setPosition(Vector position) {
        mOriginPosition.set(position);
        mWorldCenter.set(position).addLocal(mMassCenter);
    }

    public void setSize(float width, float height) {
        mWidth = width;
        mHeight = height;
        updateMass();
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public boolean updateActiveRect(BaseBehavior owner) {
        if (mOriginActiveRect == null || mOriginActiveRect.isEmpty()) {
            return false;
        }
        if (mActiveRect == null) {
            mActiveRect = new RectF();
        }
        mActiveConstraintOwner = owner;
        mActiveRect.set(
                mOriginActiveRect.left + mHookPosition.mX,
                mOriginActiveRect.top + mHookPosition.mY,
                mOriginActiveRect.right - (mWidth - mHookPosition.mX),
                mOriginActiveRect.bottom - (mHeight - mHookPosition.mY));
        return true;
    }

    public void clearActiveRect(BaseBehavior owner) {
        if (mActiveRect != null && mActiveConstraintOwner == owner) {
            mActiveRect.setEmpty();
        }
    }

    public void resetActiveRect(BaseBehavior owner) {
        if (mOriginActiveRect != null && mActiveConstraintOwner == owner) {
            mOriginActiveRect = null;
            mActiveRect = null;
            setActiveConstraintFrequency(50.0f);
        }
    }

    public void applyActiveRectForce() {
        if (mActiveRect == null || mActiveRect.isEmpty()
                || mActiveConstraintOwner == null || mActiveConstraintOwner.getType() != 0) {
            return;
        }
        float x = mOriginPosition.mX;
        if (x < mActiveRect.left) {
            mForce.mX = mActiveRect.left - x;
        } else if (x > mActiveRect.right) {
            mForce.mX = mActiveRect.right - x;
        }
        float y = mOriginPosition.mY;
        if (y < mActiveRect.top) {
            mForce.mY = mActiveRect.top - y;
        } else if (y > mActiveRect.bottom) {
            mForce.mY = mActiveRect.bottom - y;
        }
        float omega = mActiveConstraintFrequency * 6.2831855f;
        mForce.mulLocal(mMass * omega * omega);
    }

    public void syncOriginFromWorld() {
        mOriginPosition.mX = mWorldCenter.mX - mMassCenter.mX;
        mOriginPosition.mY = mWorldCenter.mY - mMassCenter.mY;
    }

    private void updateMass() {
        if (mType == 0) {
            setMass(1.0f);
            setLinearDamping(0.0f);
            return;
        }
        // Leapy modified 2026-07-29: Use OPPO's exact body mass and damping equation.
        // This restored damping is retained between COUIVerticalSeekBar rebound runs.
        setMass(mWidth * mHeight * mDensity);
        setLinearDamping(((float) StrictMath.sqrt(mMass) * 2.8600001f) + 2.2141f);
        if (!mHasSetCenter || mProperty == 1) {
            mMassCenter.mX = mWidth * 0.5f;
            mMassCenter.mY = mHeight * 0.5f;
            mWorldCenter.set(mOriginPosition).addLocal(mMassCenter);
        }
        // Leapy end
    }

    @NonNull
    public String toString() {
        return "Body{mTag=" + mTag + ", mType=" + mType + ", mProperty="
                + mProperty + ", mOriginPosition=" + mOriginPosition
                + ", mWorldCenter=" + mWorldCenter + ", mLinearVelocity="
                + mLinearVelocity + "}@" + hashCode();
    }
}
