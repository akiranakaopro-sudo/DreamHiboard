package com.coui.appcompat.dialog.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatTextView;

public class COUIAlertDialogMessageView extends AppCompatTextView
        implements ViewTreeObserver.OnGlobalLayoutListener {
    public COUIAlertDialogMessageView(Context context) {
        super(context);
    }

    public COUIAlertDialogMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public COUIAlertDialogMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onGlobalLayout() {
        setGravity(getLineCount() > 1 ? Gravity.START : Gravity.CENTER);
    }
}
