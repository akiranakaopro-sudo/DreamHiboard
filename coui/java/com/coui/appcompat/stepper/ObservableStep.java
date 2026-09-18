package com.coui.appcompat.stepper;

import java.util.Observable;

public class ObservableStep extends Observable {
    public static final int MAX_VALUE = 9999;
    public static final int MIN_VALUE = -999;

    private int mMax = MAX_VALUE;
    private int mMini = MIN_VALUE;
    private int mStep;

    public int getMaximum() {
        return mMax;
    }

    public int getMinimum() {
        return mMini;
    }

    public int getStep() {
        return mStep;
    }

    public void setMaximum(int maximum) {
        if (maximum < mMini) {
            throw new IllegalArgumentException("maximum cannot be smaller than mMini");
        }
        if (maximum > MAX_VALUE) {
            throw new IllegalArgumentException("maximum cannot be bigger than '9999'");
        }
        mMax = maximum;
        if (mStep > maximum) {
            setStep(maximum);
        }
    }

    public void setMinimum(int minimum) {
        if (minimum > mMax) {
            throw new IllegalArgumentException("minimum cannot be bigger than mMini");
        }
        if (minimum < MIN_VALUE) {
            throw new IllegalArgumentException("minimum cannot be smaller than '-999'");
        }
        mMini = minimum;
        if (mStep < minimum) {
            setStep(minimum);
        }
    }

    public void setStep(int step) {
        int clampedStep = Math.min(Math.max(step, getMinimum()), getMaximum());
        int oldStep = mStep;
        mStep = clampedStep;
        setChanged();
        notifyObservers(Integer.valueOf(oldStep));
    }
}
