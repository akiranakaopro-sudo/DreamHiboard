package com.oplus.physicsengine.engine;

import androidx.annotation.NonNull;

public final class FloatValueHolder extends FloatPropertyHolder {
    public float mValue;

    public FloatValueHolder() {
        this.mIsStartValueSet = false;
        this.mPropertyName = "floatValue";
        this.mValueThreshold = 1.0f;
        this.mPropertyType = 0;
        this.mValue = 0.0f;
    }

    public FloatValueHolder(float value) {
        this();
        this.mValue = value;
    }

    public float getValue() {
        return this.mValue;
    }

    public void setValue(float value) {
        this.mValue = value;
    }

    @NonNull
    public String toString() {
        return "FloatValueHolder{mValue=" + this.mValue + ", mPropertyType="
                + this.mPropertyType + ", mPropertyName=" + this.mPropertyName
                + ", mValueThreshold=" + this.mValueThreshold + ", mIsStartValueSet="
                + this.mIsStartValueSet + "}@" + hashCode();
    }
}
