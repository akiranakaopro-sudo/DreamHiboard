package com.coui.appcompat.animation;

import android.view.animation.BaseInterpolator;

public class COUISpringInterpolator extends BaseInterpolator {
    private static final double DEFAULT_DAMPINGRATIO = 1.15d;
    private static final double DEFAULT_STIFFNESS = 40.0d;
    private static final float DEFAULT_VELOCITY_UNIT = 15000.0f;
    private static final double VELOCITY_MAX = 20000.0d;

    private double mAngularFreq;
    private final float mCutRatio;
    private final double mDampingRatio;
    private float mFinalValue;
    private final double mImpulse;
    private final double mInitialVel;
    private boolean mIsFling;
    private final double mUnDampedAngularFreq;

    public COUISpringInterpolator(double response, double bounce) {
        this(response, bounce, 0.0d, DEFAULT_VELOCITY_UNIT);
    }

    public COUISpringInterpolator(double response, double bounce, double velocity,
            float velocityUnit) {
        this(Math.pow(6.283185307179586d / (response == 0.0d ? 1.0d : response), 2.0d),
                1.0d - bounce,
                velocity,
                1.0f,
                velocityUnit);
    }

    public COUISpringInterpolator(double stiffness, double dampingRatio, double velocity,
            float cutRatio, float velocityUnit, boolean isFling) {
        this(stiffness, dampingRatio, velocity, cutRatio, velocityUnit);
        mIsFling = isFling;
    }

    public COUISpringInterpolator(double stiffness, double dampingRatio, double velocity,
            float cutRatio, float velocityUnit) {
        mFinalValue = -1.0f;
        double unDampedAngularFreq = Math.sqrt(stiffness <= 0.0d ? DEFAULT_STIFFNESS : stiffness);
        mUnDampedAngularFreq = unDampedAngularFreq;
        dampingRatio = dampingRatio <= 0.0d ? DEFAULT_DAMPINGRATIO : dampingRatio;
        mDampingRatio = dampingRatio;
        double initialVel = Math.min(Math.abs(velocity), VELOCITY_MAX)
                / (velocityUnit <= 0.0f ? DEFAULT_VELOCITY_UNIT : velocityUnit);
        mInitialVel = initialVel;
        mCutRatio = cutRatio <= 0.0f ? 1.0f : cutRatio;
        if (dampingRatio < 1.0d) {
            double angularFreq = Math.sqrt(1.0d - (dampingRatio * dampingRatio))
                    * unDampedAngularFreq;
            mAngularFreq = angularFreq;
            mImpulse = ((dampingRatio * unDampedAngularFreq) - initialVel) / angularFreq;
        } else if (Double.compare(1.0d, dampingRatio) == 0) {
            mImpulse = (-initialVel) + unDampedAngularFreq;
        } else {
            mImpulse = (-initialVel) + (dampingRatio * unDampedAngularFreq);
        }
    }

    private float getOriginInterpolation(float input) {
        double result;
        float cutInput = (input >= 0.0f ? input : 0.0f) * mCutRatio;
        double t = cutInput;
        double exp = Math.exp((-mDampingRatio) * mUnDampedAngularFreq * t);
        if (mDampingRatio >= 1.0d) {
            if (Double.compare(1.0d, mDampingRatio) == 0) {
                result = ((mImpulse * t) + 1.0d)
                        * Math.exp((-cutInput) * mUnDampedAngularFreq);
            } else {
                double dampedFreq = mUnDampedAngularFreq
                        * Math.sqrt((mDampingRatio * mDampingRatio) - 1.0d);
                if (mIsFling) {
                    exp /= dampedFreq;
                    result = exp * (((-mInitialVel) + (mUnDampedAngularFreq * mDampingRatio))
                            * Math.sinh(mDampingRatio * t)
                            + (Math.cosh(mDampingRatio * t) * dampedFreq));
                } else {
                    double dampedTime = input * dampedFreq;
                    result = (exp / dampedFreq)
                            * (((-mInitialVel) + (mDampingRatio * mUnDampedAngularFreq))
                            * Math.sinh(dampedTime)
                            + (dampedFreq * Math.cosh(dampedTime)));
                }
            }
            return (float) (1.0d - result);
        }
        result = exp * (Math.cos(mAngularFreq * t)
                + (mImpulse * Math.sin(mAngularFreq * t)));
        return (float) (1.0d - result);
    }

    public float getCutRatio() {
        return mCutRatio;
    }

    @Override
    public float getInterpolation(float input) {
        if (mFinalValue == -1.0f) {
            float finalValue = 1.0f;
            float originInterpolation = getOriginInterpolation(1.0f);
            if (originInterpolation != 0.0f && !Float.isNaN(originInterpolation)) {
                finalValue = originInterpolation;
            }
            mFinalValue = finalValue;
        }
        return getOriginInterpolation(input) / mFinalValue;
    }

    public float getSpeed(float input) {
        double t = input >= 0.0f ? input : 0.0f;
        double exp = Math.exp((-mCutRatio) * mDampingRatio * mUnDampedAngularFreq * t);
        double result;
        if (mDampingRatio < 1.0d) {
            result = Math.abs(exp
                    * (((-mCutRatio) * ((mImpulse * mDampingRatio * mUnDampedAngularFreq)
                    + mAngularFreq) * Math.sin(mCutRatio * mAngularFreq * t))
                    + (mCutRatio * ((mImpulse * mAngularFreq)
                    - (mDampingRatio * mUnDampedAngularFreq))
                    * Math.cos(mCutRatio * mAngularFreq * t))));
        } else if (Double.compare(1.0d, mDampingRatio) == 0) {
            result = Math.abs(mCutRatio
                    * ((mImpulse - mUnDampedAngularFreq)
                    - ((mImpulse * mCutRatio * mUnDampedAngularFreq) * t))
                    * Math.exp((-mCutRatio) * mUnDampedAngularFreq * t));
        } else {
            double dampedFreq = mUnDampedAngularFreq
                    * Math.sqrt((mDampingRatio * mDampingRatio) - 1.0d);
            double factor = mCutRatio
                    * (((dampedFreq * dampedFreq)
                    + ((mInitialVel * mDampingRatio) * mUnDampedAngularFreq))
                    - (((mDampingRatio * mDampingRatio) * mUnDampedAngularFreq)
                    * mUnDampedAngularFreq));
            if (mIsFling) {
                result = Math.abs((exp / dampedFreq)
                        * ((factor * Math.sinh(mCutRatio * mDampingRatio * t))
                        + (mCutRatio * mDampingRatio
                        * (((mDampingRatio * mUnDampedAngularFreq) - mInitialVel)
                        - (dampedFreq * mUnDampedAngularFreq))
                        * Math.cosh(mCutRatio * mDampingRatio * t))));
            } else {
                result = Math.abs((exp / dampedFreq)
                        * ((factor * Math.sinh(mCutRatio * dampedFreq * input))
                        + ((-mCutRatio) * mInitialVel * dampedFreq
                        * Math.cosh(mCutRatio * dampedFreq * input))));
            }
        }
        return (float) result;
    }
}
