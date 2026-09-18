package com.coui.appcompat.dialog.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.coui.appcompat.R;

public class COUIAlertDialogMaxScrollView extends ScrollView {
    private int mMaxHeight;
    private int mMaxWidth;

    public COUIAlertDialogMaxScrollView(Context context) {
        this(context, null);
    }

    public COUIAlertDialogMaxScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIAlertDialogMaxScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIAlertDialogMaxScrollView);
        mMaxHeight = a.getDimensionPixelOffset(R.styleable.COUIAlertDialogMaxScrollView_maxHeight, 0);
        mMaxWidth = a.getDimensionPixelOffset(R.styleable.COUIAlertDialogMaxScrollView_maxWidth, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMaxWidth > 0) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    Math.min(MeasureSpec.getSize(widthMeasureSpec), mMaxWidth), MeasureSpec.getMode(widthMeasureSpec));
        }
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    Math.min(MeasureSpec.getSize(heightMeasureSpec), mMaxHeight), MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
        requestLayout();
    }
}
