package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import com.coui.appcompat.scrollview.COUIScrollView;


public class COUIMaxHeightScrollView extends COUIScrollView {
    private int mMinHeight;
    private int maxHeight;

    public COUIMaxHeightScrollView(Context context) {
        this(context, null);
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = View.MeasureSpec.getSize(heightMeasureSpec);
        int index = this.maxHeight;
        if (index > 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.min(index, size), Integer.MIN_VALUE);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        int index_2 = this.mMinHeight;
        if (measuredHeight < index_2) {
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(index_2, 1073741824));
        }
    }

    public void setMaxHeight(int maxHeight_2) {
        this.maxHeight = maxHeight_2;
        requestLayout();
    }

    public void setMinHeight(int minHeight) {
        this.mMinHeight = minHeight;
        requestLayout();
    }

    public COUIMaxHeightScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIMaxHeightScrollView(Context context, AttributeSet attributeSet, int index) {
        super(context, attributeSet, index);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUIMaxHeightScrollView);
        this.maxHeight = typedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.COUIMaxHeightScrollView_scrollViewMaxHeight, 0);
        this.mMinHeight = typedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.COUIMaxHeightScrollView_scrollViewMinHeight, 0);
        typedArrayObtainStyledAttributes.recycle();
    }
}
