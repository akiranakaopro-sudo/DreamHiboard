package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.stepper.COUIStepperView;
import com.coui.appcompat.stepper.IStepper;
import com.coui.appcompat.stepper.ObservableStep;
import com.coui.appcompat.stepper.OnStepChangeListener;

public class COUIStepperPreference extends COUIPreference implements IStepper, OnStepChangeListener {
    private COUIStepperView mCOUIStepperView;
    private final int mDefaultValue;
    private int mInitialMaximum;
    private int mInitialMinimum;
    private int mInitialValue;
    private OnStepChangeListener mOnStepChangeListener;
    private int mUnit;

    public COUIStepperPreference(Context context) {
        this(context, null);
    }

    public COUIStepperPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiStepperPreferenceStyle);
    }

    public COUIStepperPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUIStepperPreference);
    }

    public COUIStepperPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.COUIStepperPreference, defStyleAttr, defStyleRes);
        mInitialMaximum = typedArray.getInt(R.styleable.COUIStepperPreference_couiMaximum, ObservableStep.MAX_VALUE);
        mInitialMinimum = typedArray.getInt(R.styleable.COUIStepperPreference_couiMinimum, ObservableStep.MIN_VALUE);
        int defStep = typedArray.getInt(R.styleable.COUIStepperPreference_couiDefStep, 0);
        mInitialValue = defStep;
        mDefaultValue = defStep;
        mUnit = typedArray.getInt(R.styleable.COUIStepperPreference_couiUnit, 1);
        typedArray.recycle();
    }

    public void changePersistence(Boolean persistent) {
        if (persistent) {
            setPersistent(true);
            persistInt(mInitialValue);
        } else {
            persistInt(mDefaultValue);
            setPersistent(false);
        }
    }

    public COUIStepperView getCOUIStepperView() {
        return mCOUIStepperView;
    }

    @Override
    public int getCurStep() {
        return mCOUIStepperView.getCurStep();
    }

    @Override
    public int getMaximum() {
        return mCOUIStepperView.getMaximum();
    }

    @Override
    public int getMinimum() {
        return mCOUIStepperView.getMinimum();
    }

    @Override
    public int getUnit() {
        return mCOUIStepperView.getUnit();
    }

    @Override
    public void minus() {
        mCOUIStepperView.minus();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        COUIStepperView stepperView = (COUIStepperView) holder.findViewById(R.id.stepper);
        mCOUIStepperView = stepperView;
        if (stepperView != null) {
            setMaximum(mInitialMaximum);
            setMinimum(mInitialMinimum);
            setCurStep(mInitialValue);
            setUnit(mUnit);
            mCOUIStepperView.setOnStepChangeListener(this);
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        COUIStepperView stepperView = mCOUIStepperView;
        if (stepperView != null) {
            stepperView.release();
        }
    }

    @Override
    public Object onGetDefaultValue(TypedArray typedArray, int index) {
        return typedArray.getInt(index, 0);
    }

    @Override
    public void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = 0;
        }
        mInitialValue = getPersistedInt((Integer) defaultValue);
    }

    @Override
    public void onStepChanged(int step, int oldStep) {
        mInitialValue = step;
        persistInt(step);
        if (step != oldStep) {
            callChangeListener(step);
        }
        OnStepChangeListener listener = mOnStepChangeListener;
        if (listener != null) {
            listener.onStepChanged(step, oldStep);
        }
    }

    @Override
    public void plus() {
        mCOUIStepperView.plus();
    }

    @Override
    public void setCurStep(int step) {
        mCOUIStepperView.setCurStep(step);
    }

    @Override
    public void setMaximum(int maximum) {
        mInitialMaximum = maximum;
        mCOUIStepperView.setMaximum(maximum);
    }

    @Override
    public void setMinimum(int minimum) {
        mInitialMinimum = minimum;
        mCOUIStepperView.setMinimum(minimum);
    }

    @Override
    public void setOnStepChangeListener(OnStepChangeListener listener) {
        mOnStepChangeListener = listener;
    }

    @Override
    public void setUnit(int unit) {
        mUnit = unit;
        mCOUIStepperView.setUnit(unit);
    }
}
