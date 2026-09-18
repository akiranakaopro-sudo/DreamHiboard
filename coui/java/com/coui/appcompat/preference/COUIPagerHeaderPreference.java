package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;

import com.coui.appcompat.R;

public class COUIPagerHeaderPreference extends Preference {
    public COUIPagerHeaderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.coui_pager_header_preference);
    }
}
