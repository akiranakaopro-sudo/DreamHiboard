package com.coui.appcompat.scrollview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;
import com.coui.appcompat.R;

public class COUIScrollView extends ScrollView {
    private boolean mEnableVibrator = true;
    private float mCustomOverScrollDistFactor;

    public COUIScrollView(Context context) {
        super(context);
    }

    public COUIScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIScrollView(Context context, AttributeSet attrs, int defStyleAttr, int screenHeight) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.COUIScrollView, defStyleAttr, 0);
        this.mEnableVibrator = array.getBoolean(R.styleable.COUIScrollView_couiScrollViewEnableVibrator, true);
        array.recycle();
    }

    public void setEnableVibrator(boolean enableVibrator) {
        this.mEnableVibrator = enableVibrator;
    }

    public boolean isEnableVibrator() {
        return this.mEnableVibrator;
    }

    public void setCustomOverScrollDistFactor(float customOverScrollDistFactor) {
        this.mCustomOverScrollDistFactor = customOverScrollDistFactor;
    }

    public float getCustomOverScrollDistFactor() {
        return this.mCustomOverScrollDistFactor;
    }

    public void setIsUseOptimizedScroll(boolean enableOptimizedScroll) {
    }

    public void setItemClickableWhileOverScrolling(boolean itemClickableWhileOverScrolling) {
    }

    public void setItemClickableWhileSlowScrolling(boolean itemClickableWhileSlowScrolling) {
    }

    public void setEventFilterTangent(float eventFilterAngle) {
    }

    public void setFastFlingThreshold(float fastFlingVelocity) {
    }

    public void setDispatchEventWhileScrolling(boolean enableDispatchEventWhileScrolling) {
    }

    public void setDispatchEventWhileOverScrolling(boolean enableDispatchEventWhileOverScrolling) {
    }

    public void setDispatchEventVelocityThreshold(int dispatchEventVelocityThreshold) {
    }
}
