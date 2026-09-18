package com.coui.appcompat.edittext;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class COUIScrolledEditText extends COUIEditText {
    private int mMaxHeight;

    public COUIScrolledEditText(Context context) {
        super(context);
    }

    public COUIScrolledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public COUIScrolledEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mMaxHeight = (getLineHeight() * getMaxLines()) + getPaddingTop() + getPaddingBottom();
            if (getHeight() >= mMaxHeight && getLineCount() > 1 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.onTouchEvent(event);
    }
}
