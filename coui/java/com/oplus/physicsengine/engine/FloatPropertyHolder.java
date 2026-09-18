package com.oplus.physicsengine.engine;

public abstract class FloatPropertyHolder {
    public boolean mIsStartValueSet;
    public String mPropertyName;
    public int mPropertyType;
    public float mStartValue;
    public float mValueThreshold;

    public FloatPropertyHolder setPropertyType(int propertyType) {
        mPropertyType = propertyType;
        return this;
    }

    public FloatPropertyHolder setStartValue(float startValue) {
        mStartValue = startValue;
        mIsStartValueSet = true;
        return this;
    }

    public FloatPropertyHolder setValueThreshold(float valueThreshold) {
        mValueThreshold = valueThreshold;
        return this;
    }
}
