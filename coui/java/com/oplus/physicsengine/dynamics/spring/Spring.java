package com.oplus.physicsengine.dynamics.spring;

import com.oplus.physicsengine.common.Mat22;
import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.Body;

public final class Spring {
    public float mBeta;
    public final Body mBodyA;
    public final Body mBodyB;
    public float mDampingRatio;
    public final Edge mEdgeA;
    public final Edge mEdgeB;
    public float mFrequencyHz;
    public float mGamma;
    public final Vector mImpulse;
    public final Vector mImpulseTemp;
    public float mInvMass;
    public boolean mIsSolved;
    public final Vector mLocalAnchor;
    public final Mat22 mMass;
    public final float mMaxForce;
    public Spring mNext;
    public final Vector mPositionCenter;
    public Spring mPrev;
    public final Vector mTarget;

    public Spring(Vector impulseTemp, SpringDef springDef) {
        mPrev = null;
        mNext = null;
        mBeta = 0.0f;
        mGamma = 0.0f;
        mLocalAnchor = new Vector();
        mPositionCenter = new Vector();
        mTarget = new Vector();
        mImpulse = new Vector();
        mMass = new Mat22();
        mImpulseTemp = impulseTemp;
        mBodyA = springDef.bodyA;
        mBodyB = springDef.bodyB;
        mIsSolved = false;
        mEdgeA = new Edge();
        mEdgeB = new Edge();
        mMaxForce = springDef.maxForce;
        mFrequencyHz = springDef.frequencyHz < 0.0f ? 0.0f : springDef.frequencyHz;
        mDampingRatio = springDef.dampingRatio < 0.0f ? 0.0f : springDef.dampingRatio;
        mTarget.set(springDef.target);
        if (mBodyB != null) {
            mLocalAnchor.set(mTarget);
            mLocalAnchor.subLocal(mBodyB.mOriginPosition);
        }
        mEdgeA.spring = this;
        mEdgeA.other = mBodyB;
        mEdgeB.spring = this;
        mEdgeB.other = mBodyA;
    }

    public Body getBodyA() {
        return mBodyA;
    }

    public Body getBodyB() {
        return mBodyB;
    }

    public Vector getTarget() {
        return mTarget;
    }

    public void setDampingRatio(float dampingRatio) {
        mDampingRatio = dampingRatio;
    }

    public void setFrequency(float frequencyHz) {
        mFrequencyHz = frequencyHz;
    }

    public void setTarget(Vector target) {
        mTarget.set(target);
    }

    public void setTarget(float x, float y) {
        mTarget.mX = x;
        mTarget.mY = y;
    }

    public void prepare(Body body, float timeStep) {
        mInvMass = body.mInvMass;
        float omega = mFrequencyHz * 6.2831855f;
        float damping = body.getMass() * 2.0f * mDampingRatio * omega;
        float stiffnessStep = body.getMass() * omega * omega * timeStep;
        float gammaDenominator = damping + stiffnessStep;
        if (gammaDenominator > 1.1920929E-7f) {
            mGamma = timeStep * gammaDenominator;
        }
        if (mGamma != 0.0f) {
            mGamma = 1.0f / mGamma;
        }
        mBeta = stiffnessStep * mGamma;
        mMass.ex.mX = mInvMass + mGamma;
        mMass.ey.mY = mInvMass + mGamma;
        mMass.invertLocal();
        mPositionCenter.set(body.mWorldCenter);
        mPositionCenter.subLocal(mLocalAnchor);
        mPositionCenter.subLocal(mTarget);
        mPositionCenter.mulLocal(mBeta);
        body.mLinearVelocity.mX += mImpulse.mX * mInvMass;
        body.mLinearVelocity.mY += mImpulse.mY * mInvMass;
    }

    public void solve(Body body) {
        mImpulseTemp.set(mImpulse);
        mImpulseTemp.mulLocal(mGamma);
        mImpulseTemp.addLocal(mPositionCenter);
        mImpulseTemp.addLocal(body.mLinearVelocity);
        mImpulseTemp.negate();
        Mat22.mulToOut(mMass, mImpulseTemp, mImpulseTemp);
        mImpulse.addLocal(mImpulseTemp);
        mImpulseTemp.mulLocal(mInvMass);
        body.mLinearVelocity.addLocal(mImpulseTemp);
    }

    public String toString() {
        return "Spring{mTarget=" + mTarget + ", mFrequencyHz=" + mFrequencyHz
                + ", mDampingRatio=" + mDampingRatio + "}@" + hashCode();
    }
}
