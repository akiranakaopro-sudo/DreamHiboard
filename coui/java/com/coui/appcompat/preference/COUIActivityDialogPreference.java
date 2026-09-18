package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;

import com.coui.appcompat.R;

public class COUIActivityDialogPreference extends COUIListPreference {
    public COUIActivityDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public COUIActivityDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIActivityDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiActivityDialogPreferenceStyle);
    }

    public COUIActivityDialogPreference(Context context) {
        this(context, null);
    }
}
