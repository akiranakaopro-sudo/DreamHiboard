package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;

import com.coui.appcompat.R;

public class COUIJumpPreference extends COUIPreference {
    public COUIJumpPreference(Context context) {
        this(context, null);
    }

    public COUIJumpPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiJumpPreferenceStyle);
    }

    public COUIJumpPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUIJumpPreference);
    }

    public COUIJumpPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
