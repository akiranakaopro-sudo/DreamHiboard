package com.coui.appcompat.edittext;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.coui.appcompat.R;

public class COUICheckBoxPwd extends AppCompatCheckBox {
    private static final String ACCESSIBILITY_CLASSNAME = "androidx.appcompat.widget.AppCompatButton";

    public COUICheckBoxPwd(Context context) {
        this(context, null);
    }

    public COUICheckBoxPwd(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.checkboxStyle);
    }

    public COUICheckBoxPwd(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setStateDescription("");
        if (TextUtils.isEmpty(getContentDescription())) {
            setContentDescription(getContext().getString(R.string.coui_inputview_show_password_description));
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return ACCESSIBILITY_CLASSNAME;
    }
}
