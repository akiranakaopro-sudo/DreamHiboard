package com.coui.appcompat.animation.dynamicanimation;

public final class COUISpringForce implements COUIForce {
    public static final float DAMPING_RATIO_HIGH_BOUNCY = 0.2f;
    public static final float DAMPING_RATIO_LOW_BOUNCY = 0.75f;
    public static final float DAMPING_RATIO_MEDIUM_BOUNCY = 0.5f;
    public static final float DAMPING_RATIO_NO_BOUNCY = 1.0f;
    public static final float STIFFNESS_HIGH = 10000.0f;
    public static final float STIFFNESS_LOW = 200.0f;
    public static final float STIFFNESS_MEDIUM = 1500.0f;
    public static final float STIFFNESS_VERY_LOW = 50.0f;
    private static final double UNSET = Double.MAX_VALUE;
    private static final double VELOCITY_THRESHOLD_MULTIPLIER = 62.5d;

    private float mBlendDuration;
    private double mDampedFreq;
    double mDampingRatio = DAMPING_RATIO_MEDIUM_BOUNCY;
    private double mFinalPosition = UNSET;
    private float mFinalResponse;
    private double mGammaMinus;
    private double mGammaPlus;
    private boolean mInitialized;
    private final COUIDynamicAnimation.MassState mMassState =
            new COUIDynamicAnimation.MassState();
    double mNaturalFreq = Math.sqrt(STIFFNESS_MEDIUM);
    private float mResponse;
    private float mStiffness = STIFFNESS_MEDIUM;
    private double mValueThreshold;
    private double mVelocityThreshold;

    public COUISpringForce() {
    }

    public COUISpringForce(float finalPosition) {
        mFinalPosition = finalPosition;
    }

    private void init() {
        if (mInitialized) {
            return;
        }
        if (mFinalPosition == UNSET) {
            throw new IllegalStateException(
                    "Error: Final position of the spring must be set before the animation starts");
        }
        if (mDampingRatio > 1.0d) {
            mGammaPlus = (-mDampingRatio * mNaturalFreq)
                    + (mNaturalFreq * Math.sqrt((mDampingRatio * mDampingRatio) - 1.0d));
            mGammaMinus = (-mDampingRatio * mNaturalFreq)
                    - (mNaturalFreq * Math.sqrt((mDampingRatio * mDampingRatio) - 1.0d));
        } else if (mDampingRatio >= 0.0d && mDampingRatio < 1.0d) {
            mDampedFreq = mNaturalFreq * Math.sqrt(1.0d - (mDampingRatio * mDampingRatio));
        }
        mInitialized = true;
    }

    private float stiffnessToResponse(float stiffness) {
        return (float) (Math.PI * 2.0d / Math.sqrt(stiffness));
    }

    @Override
    public float getAcceleration(float value, float velocity) {
        float displacement = value - getFinalPosition();
        return (float) ((-(mNaturalFreq * mNaturalFreq) * displacement)
                - (((mNaturalFreq * 2.0d) * mDampingRatio) * velocity));
    }

    public float getBlendDuration() {
        return mBlendDuration;
    }

    public float getDampingRatio() {
        return (float) mDampingRatio;
    }

    public float getFinalPosition() {
        return (float) mFinalPosition;
    }

    public float getFinalResponse() {
        return mFinalResponse;
    }

    public float getResponse() {
        return mResponse;
    }

    public float getStiffness() {
        return (float) (mNaturalFreq * mNaturalFreq);
    }

    @Override
    public boolean isAtEquilibrium(float value, float velocity) {
        return Math.abs(velocity) < mVelocityThreshold
                && Math.abs(value - getFinalPosition()) < mValueThreshold;
    }

    public float responseToStiffness(float response) {
        return (float) Math.pow((Math.PI * 2.0d) / response, 2.0d);
    }

    public COUISpringForce setBlendDuration(float blendDuration) {
        mBlendDuration = blendDuration;
        return this;
    }

    public COUISpringForce setBounce(float bounce) {
        float dampingRatio = 1.0f - bounce;
        if (dampingRatio < 0.0f) {
            throw new IllegalArgumentException("Damping ratio must be non-negative");
        }
        return setDampingRatio(dampingRatio);
    }

    public COUISpringForce setDampingRatio(float dampingRatio) {
        if (dampingRatio < 0.0f) {
            throw new IllegalArgumentException("Damping ratio must be non-negative");
        }
        mDampingRatio = dampingRatio;
        mInitialized = false;
        return this;
    }

    public COUISpringForce setFinalPosition(float finalPosition) {
        mFinalPosition = finalPosition;
        return this;
    }

    public COUISpringForce setResponse(float response) {
        if (response == 0.0f) {
            response = 1.0f;
        }
        float stiffness = (float) Math.pow((Math.PI * 2.0d) / response, 2.0d);
        if (stiffness <= 0.0f) {
            throw new IllegalArgumentException("Spring stiffness constant must be positive.");
        }
        return setStiffness(stiffness);
    }

    public COUISpringForce setStiffness(float stiffness) {
        if (stiffness <= 0.0f) {
            throw new IllegalArgumentException("Spring stiffness constant must be positive.");
        }
        mNaturalFreq = Math.sqrt(stiffness);
        mStiffness = stiffness;
        float response = stiffnessToResponse(stiffness);
        mResponse = response;
        mFinalResponse = response;
        mInitialized = false;
        return this;
    }

    public void setValueThreshold(double threshold) {
        mValueThreshold = Math.abs(threshold);
        mVelocityThreshold = mValueThreshold * VELOCITY_THRESHOLD_MULTIPLIER;
    }

    public COUISpringForce updateResponse(float response) {
        mResponse = response;
        float stiffness = responseToStiffness(response);
        mNaturalFreq = Math.sqrt(stiffness);
        mStiffness = stiffness;
        mInitialized = false;
        return this;
    }

    public COUIDynamicAnimation.MassState updateValues(double value, double velocity,
            long deltaMillis) {
        init();
        double dt = deltaMillis / 1000.0d;
        double displacement = value - mFinalPosition;
        double newDisplacement;
        double newVelocity;
        if (mDampingRatio > 1.0d) {
            double coeffA = displacement - (((mGammaMinus * displacement) - velocity)
                    / (mGammaMinus - mGammaPlus));
            double coeffB = ((mGammaMinus * displacement) - velocity)
                    / (mGammaMinus - mGammaPlus);
            newDisplacement = (Math.exp(mGammaMinus * dt) * coeffA)
                    + (Math.exp(mGammaPlus * dt) * coeffB);
            newVelocity = (coeffA * mGammaMinus * Math.exp(mGammaMinus * dt))
                    + (coeffB * mGammaPlus * Math.exp(mGammaPlus * dt));
        } else if (mDampingRatio == 1.0d) {
            double coeff = velocity + (mNaturalFreq * displacement);
            double partial = displacement + (coeff * dt);
            newDisplacement = Math.exp(-mNaturalFreq * dt) * partial;
            newVelocity = (coeff * Math.exp(-mNaturalFreq * dt))
                    + (partial * Math.exp(-mNaturalFreq * dt) * -mNaturalFreq);
        } else {
            double coeff = (1.0d / mDampedFreq)
                    * ((mDampingRatio * mNaturalFreq * displacement) + velocity);
            newDisplacement = Math.exp(-mDampingRatio * mNaturalFreq * dt)
                    * ((Math.cos(mDampedFreq * dt) * displacement)
                    + (Math.sin(mDampedFreq * dt) * coeff));
            newVelocity = (-mNaturalFreq * newDisplacement * mDampingRatio)
                    + (Math.exp(-mDampingRatio * mNaturalFreq * dt)
                    * ((-mDampedFreq * displacement * Math.sin(mDampedFreq * dt))
                    + (coeff * mDampedFreq * Math.cos(mDampedFreq * dt))));
        }
        mMassState.mValue = (float) (newDisplacement + mFinalPosition);
        mMassState.mVelocity = (float) newVelocity;
        return mMassState;
    }
}
