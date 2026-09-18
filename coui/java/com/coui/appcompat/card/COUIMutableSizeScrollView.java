package com.coui.appcompat.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.coui.appcompat.R;
import com.coui.appcompat.scrollview.COUIScrollView;

public class COUIMutableSizeScrollView extends COUIScrollView {
    protected final PointF curPoint;
    protected final PointF firstPoint;
    private int mMaxHeight;
    protected final int touchSlop;

    public COUIMutableSizeScrollView(Context context) {
        this(context, null);
    }

    public COUIMutableSizeScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIMutableSizeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        firstPoint = new PointF();
        curPoint = new PointF();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIMaxHeightScrollView);
        mMaxHeight = a.getDimensionPixelOffset(R.styleable.COUIMaxHeightScrollView_scrollViewMaxHeight, -1);
        a.recycle();
    }

    public boolean canScroll(int directionType, int delta) {
        if (directionType == 0) {
            return false;
        }
        return canScrollVertically((int) (-Math.signum(delta)));
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            firstPoint.x = event.getX();
            firstPoint.y = event.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() != 1) {
            return;
        }
        int childHeight = getChildAt(0).getMeasuredHeight();
        if (mMaxHeight >= 0 && childHeight > mMaxHeight) {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), mMaxHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            curPoint.x = event.getX();
            curPoint.y = event.getY();
            float dx = curPoint.x - firstPoint.x;
            float dy = curPoint.y - firstPoint.y;
            float weightedAbsDx = Math.abs(dx) * 0.5f;
            float absDy = Math.abs(dy);
            if (weightedAbsDx > touchSlop || absDy > touchSlop) {
                if (weightedAbsDx > absDy) {
                    getParent().requestDisallowInterceptTouchEvent(canScroll(0, (int) dx));
                } else {
                    getParent().requestDisallowInterceptTouchEvent(canScroll(1, (int) dy));
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
    }
}
