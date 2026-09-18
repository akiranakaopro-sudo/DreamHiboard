package com.coui.appcompat.dialog.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;

import com.coui.appcompat.R;

public class COUIMaxHeightNestedScrollView extends NestedScrollView {
    private ConfigChangeListener mConfigChangeListener;
    private boolean mInterceptWhenCannotScroll;
    private int mMaxHeight;
    private int mMinHeight;

    public interface ConfigChangeListener {
        void onChange();
    }

    public COUIMaxHeightNestedScrollView(Context context) {
        this(context, null);
    }

    public COUIMaxHeightNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIMaxHeightNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIMaxHeightScrollView);
        mMaxHeight = a.getDimensionPixelOffset(R.styleable.COUIMaxHeightScrollView_scrollViewMaxHeight, 0);
        mMinHeight = a.getDimensionPixelOffset(R.styleable.COUIMaxHeightScrollView_scrollViewMinHeight, 0);
        mInterceptWhenCannotScroll = a.getBoolean(
                R.styleable.COUIMaxHeightScrollView_scrollViewInterceptWhenCannotScroll, false);
        a.recycle();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mConfigChangeListener != null) {
            mConfigChangeListener.onChange();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mInterceptWhenCannotScroll && !canScrollVertically(-1) && !canScrollVertically(1)) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(mMaxHeight, heightSize), MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        if (mMinHeight > 0) {
            measuredHeight = Math.max(measuredHeight, mMinHeight);
        }
        if (mMaxHeight > 0) {
            measuredHeight = Math.min(measuredHeight, mMaxHeight);
        }
        if (measuredHeight != getMeasuredHeight()) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
        }
    }

    public boolean isInterceptWhenCannotScroll() {
        return mInterceptWhenCannotScroll;
    }

    public void setConfigChangeListener(ConfigChangeListener listener) {
        mConfigChangeListener = listener;
    }

    public void setInterceptWhenCannotScroll(boolean interceptWhenCannotScroll) {
        mInterceptWhenCannotScroll = interceptWhenCannotScroll;
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
    }

    public void setMinHeight(int minHeight) {
        mMinHeight = minHeight;
        requestLayout();
    }
}
