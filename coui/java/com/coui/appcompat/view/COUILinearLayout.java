package com.coui.appcompat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.coui.appcompat.R;

public class COUILinearLayout extends LinearLayout {
    private int mMaxHeight;
    private int mMaxWidth;

    public COUILinearLayout(Context context) {
        this(context, null);
    }

    public COUILinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUILinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUILinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.COUILinearLayout);
        this.mMaxWidth = typedArray.getDimensionPixelOffset(R.styleable.COUILinearLayout_couiMaxWidth, -1);
        this.mMaxHeight = typedArray.getDimensionPixelOffset(R.styleable.COUILinearLayout_couiMaxHeight, -1);
        typedArray.recycle();
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getOrientation() == HORIZONTAL && this.mMaxWidth >= 0) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    Math.min(View.MeasureSpec.getSize(widthMeasureSpec), this.mMaxWidth),
                    View.MeasureSpec.AT_MOST);
        } else if (getOrientation() == VERTICAL && this.mMaxHeight >= 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    Math.min(View.MeasureSpec.getSize(heightMeasureSpec), this.mMaxHeight),
                    View.MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
        requestLayout();
    }

    public void setMaxWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
        requestLayout();
    }
}
