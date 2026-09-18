package com.coui.appcompat.checklayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableLayout extends RelativeLayout implements Checkable {
    private Checkable mCheckable;

    public CheckableLayout(Context context) {
        super(context);
    }

    public CheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setCheckbleView(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        if (childCount <= 0) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                setCheckbleView((ViewGroup) child);
            } else if (child instanceof Checkable) {
                mCheckable = (Checkable) child;
                return;
            }
        }
    }

    @Override
    public boolean isChecked() {
        return mCheckable != null && mCheckable.isChecked();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setCheckbleView(this);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mCheckable != null) {
            mCheckable.setChecked(checked);
        }
    }

    @Override
    public void toggle() {
        if (mCheckable != null) {
            mCheckable.toggle();
        }
    }
}
