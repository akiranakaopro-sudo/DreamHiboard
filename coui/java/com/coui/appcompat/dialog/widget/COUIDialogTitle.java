package com.coui.appcompat.dialog.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;

public class COUIDialogTitle extends AppCompatTextView {
    public COUIDialogTitle(Context context) {
        super(context);
    }

    public COUIDialogTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public COUIDialogTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Layout layout = getLayout();
        if (layout != null && layout.getLineCount() > 0
                && layout.getEllipsisCount(layout.getLineCount() - 1) > 0) {
            setSingleLine(false);
            setMaxLines(3);
            TypedArray textAppearance = getContext().obtainStyledAttributes(null,
                    androidx.appcompat.R.styleable.TextAppearance,
                    android.R.attr.textAppearanceMedium,
                    android.R.style.TextAppearance_Medium);
            int textSize = textAppearance.getDimensionPixelSize(
                    androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0);
            if (textSize != 0) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            textAppearance.recycle();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
