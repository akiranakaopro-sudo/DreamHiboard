package com.coui.appcompat.stepper;

public interface IStepper {
    int getCurStep();

    int getMaximum();

    int getMinimum();

    int getUnit();

    void minus();

    void plus();

    void setCurStep(int step);

    void setMaximum(int maximum);

    void setMinimum(int minimum);

    void setOnStepChangeListener(OnStepChangeListener listener);

    void setUnit(int unit);
}
