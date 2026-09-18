package com.coui.appcompat.viewpager;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import androidx.recyclerview.widget.RecyclerView;

public class COUIFakeDrag {
    private int mActualDraggedDistance;
    private final COUIScrollEventAdapter mCOUIScrollEventAdapter;
    private long mFakeDragBeginTime;
    private int mMaximumVelocity;
    private final RecyclerView mRecyclerView;
    private float mRequestedDragDistance;
    private VelocityTracker mVelocityTracker;
    private final COUIViewPager2 mViewPager;

    public COUIFakeDrag(COUIViewPager2 viewPager, COUIScrollEventAdapter scrollEventAdapter,
            RecyclerView recyclerView) {
        mViewPager = viewPager;
        mCOUIScrollEventAdapter = scrollEventAdapter;
        mRecyclerView = recyclerView;
    }

    private void addFakeMotionEvent(long eventTime, int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(mFakeDragBeginTime, eventTime, action, x, y, 0);
        mVelocityTracker.addMovement(event);
        event.recycle();
    }

    private void beginFakeVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        } else {
            mVelocityTracker = VelocityTracker.obtain();
            mMaximumVelocity = ViewConfiguration.get(mViewPager.getContext()).getScaledMaximumFlingVelocity();
        }
    }

    public boolean beginFakeDrag() {
        if (mCOUIScrollEventAdapter.isDragging()) {
            return false;
        }
        mActualDraggedDistance = 0;
        mRequestedDragDistance = 0.0f;
        mFakeDragBeginTime = SystemClock.uptimeMillis();
        beginFakeVelocityTracker();
        mCOUIScrollEventAdapter.notifyBeginFakeDrag();
        if (!mCOUIScrollEventAdapter.isIdle()) {
            mRecyclerView.stopScroll();
        }
        addFakeMotionEvent(mFakeDragBeginTime, MotionEvent.ACTION_DOWN, 0.0f, 0.0f);
        return true;
    }

    public boolean endFakeDrag() {
        if (!mCOUIScrollEventAdapter.isFakeDragging()) {
            return false;
        }
        mCOUIScrollEventAdapter.notifyEndFakeDrag();
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        if (mRecyclerView.fling((int) mVelocityTracker.getXVelocity(), (int) mVelocityTracker.getYVelocity())) {
            return true;
        }
        mViewPager.snapToPageOpen();
        return true;
    }

    public boolean fakeDragBy(float offsetPxFloat) {
        if (!mCOUIScrollEventAdapter.isFakeDragging()) {
            return false;
        }
        mRequestedDragDistance -= offsetPxFloat;
        int rounded = Math.round(mRequestedDragDistance - mActualDraggedDistance);
        mActualDraggedDistance += rounded;
        long time = SystemClock.uptimeMillis();
        boolean horizontal = mViewPager.getOrientation() == COUIViewPager2.ORIENTATION_HORIZONTAL;
        int dx = horizontal ? rounded : 0;
        int dy = horizontal ? 0 : rounded;
        float x = horizontal ? mRequestedDragDistance : 0.0f;
        float y = horizontal ? 0.0f : mRequestedDragDistance;
        mRecyclerView.scrollBy(dx, dy);
        addFakeMotionEvent(time, MotionEvent.ACTION_MOVE, x, y);
        return true;
    }

    public boolean isFakeDragging() {
        return mCOUIScrollEventAdapter.isFakeDragging();
    }
}
