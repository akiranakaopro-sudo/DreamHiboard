package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;

import com.coui.appcompat.R;

public class COUISpannablePreference extends COUIPreference {
    public COUISpannablePreference(Context context) {
        this(context, null);
    }

    public COUISpannablePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiSpannablePreferenceStyle);
    }

    public COUISpannablePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.style.Preference_COUI_COUISpannablePreference);
    }
}
