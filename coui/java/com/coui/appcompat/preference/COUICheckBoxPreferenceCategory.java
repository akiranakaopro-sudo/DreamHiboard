package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.checkbox.COUICheckBox;

public class COUICheckBoxPreferenceCategory extends COUIPreferenceCategory {
    private static final String TAG = "CheckBoxCategory";

    private int mDefaultCheckboxState;
    private COUICheckBox.OnStateChangeListener mOnStateChangeListener;

    public COUICheckBoxPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefaultCheckboxState = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICheckBoxPreferenceCategory, 0, 0);
        mDefaultCheckboxState = a.getInteger(
                R.styleable.COUICheckBoxPreferenceCategory_default_checkbox_state,
                mDefaultCheckboxState
        );
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        setWidgetLayoutRes(R.layout.coui_preference_category_widget_layout_checkbox);
        super.onBindViewHolder(holder);
        COUICheckBox checkBox = (COUICheckBox) getWidgetLayout().findViewById(android.R.id.checkbox);
        if (checkBox != null) {
            int defaultState = mDefaultCheckboxState;
            if (defaultState != 0) {
                checkBox.setState(defaultState);
            }
            COUICheckBox.OnStateChangeListener listener = mOnStateChangeListener;
            if (listener != null) {
                checkBox.setOnStateChangeListener(listener);
            }
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    public void setOnStateChangeListener(COUICheckBox.OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    @Override
    public void setWidgetLayoutClickListener(View.OnClickListener listener) {
        Log.e(TAG, "set Widget Layout Click Listener does not take effect in the COUICheckBoxPreferenceCategory setting, please set setOnStateChangeListener");
    }
}
