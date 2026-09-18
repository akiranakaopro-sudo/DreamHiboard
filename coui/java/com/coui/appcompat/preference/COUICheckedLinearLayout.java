package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class COUICheckedLinearLayout extends LinearLayout implements Checkable {
    public COUICheckedLinearLayout(Context context) {
        this(context, null);
    }

    public COUICheckedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUICheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isChecked() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            KeyEvent.Callback child = getChildAt(i);
            if (child instanceof Checkable) {
                return ((Checkable) child).isChecked();
            }
        }
        return false;
    }

    @Override
    public void setChecked(boolean checked) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            KeyEvent.Callback child = getChildAt(i);
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(checked);
            }
        }
    }

    @Override
    public void toggle() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            KeyEvent.Callback child = getChildAt(i);
            if (child instanceof Checkable) {
                ((Checkable) child).toggle();
            }
        }
    }
}
