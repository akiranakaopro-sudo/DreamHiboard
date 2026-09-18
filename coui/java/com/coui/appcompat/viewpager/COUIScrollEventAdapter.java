package com.coui.appcompat.viewpager;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Locale;

public class COUIScrollEventAdapter extends RecyclerView.OnScrollListener {
    private static final int NO_POSITION = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_IN_PROGRESS_MANUAL_DRAG = 1;
    private static final int STATE_IN_PROGRESS_SMOOTH_SCROLL = 2;
    private static final int STATE_IN_PROGRESS_IMMEDIATE_SCROLL = 3;
    private static final int STATE_IN_PROGRESS_FAKE_DRAG = 4;

    private int mAdapterState;
    private ViewPager2.OnPageChangeCallback mCallback;
    private boolean mDataSetChangeHappened;
    private boolean mDispatchSelected;
    private int mDragStartPosition;
    private boolean mFakeDragging;
    private final LinearLayoutManager mLayoutManager;
    private final RecyclerView mRecyclerView;
    private boolean mScrollHappened;
    private int mScrollState;
    private final ScrollEventValues mScrollValues = new ScrollEventValues();
    private int mTarget;
    private final COUIViewPager2 mViewPager;

    public static final class ScrollEventValues {
        int mPosition;
        float mOffset;
        int mOffsetPx;

        public void reset() {
            mPosition = NO_POSITION;
            mOffset = 0.0f;
            mOffsetPx = 0;
        }
    }

    public COUIScrollEventAdapter(COUIViewPager2 viewPager) {
        mViewPager = viewPager;
        mRecyclerView = viewPager.mRecyclerView;
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        resetState();
    }

    private void dispatchScrolled(int position, float offset, int offsetPx) {
        if (mCallback != null) {
            mCallback.onPageScrolled(position, offset, offsetPx);
        }
    }

    private void dispatchSelected(int position) {
        if (mCallback != null) {
            mCallback.onPageSelected(position);
        }
    }

    private void dispatchStateChanged(int state) {
        if ((mAdapterState == STATE_IN_PROGRESS_IMMEDIATE_SCROLL && mScrollState == ViewPager2.SCROLL_STATE_IDLE)
                || mScrollState == state) {
            return;
        }
        mScrollState = state;
        if (mCallback != null) {
            mCallback.onPageScrollStateChanged(state);
        }
    }

    private int getPosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private boolean isInAnyDraggingState() {
        return mAdapterState == STATE_IN_PROGRESS_MANUAL_DRAG
                || mAdapterState == STATE_IN_PROGRESS_FAKE_DRAG;
    }

    private void resetState() {
        mAdapterState = STATE_IDLE;
        mScrollState = ViewPager2.SCROLL_STATE_IDLE;
        mScrollValues.reset();
        mDragStartPosition = NO_POSITION;
        mTarget = NO_POSITION;
        mDispatchSelected = false;
        mScrollHappened = false;
        mFakeDragging = false;
        mDataSetChangeHappened = false;
    }

    private void startDrag(boolean fake) {
        mFakeDragging = fake;
        mAdapterState = fake ? STATE_IN_PROGRESS_FAKE_DRAG : STATE_IN_PROGRESS_MANUAL_DRAG;
        if (mTarget != NO_POSITION) {
            mDragStartPosition = mTarget;
            mTarget = NO_POSITION;
        } else if (mDragStartPosition == NO_POSITION) {
            mDragStartPosition = getPosition();
        }
        dispatchStateChanged(ViewPager2.SCROLL_STATE_DRAGGING);
    }

    private void updateScrollEventValues() {
        ScrollEventValues values = mScrollValues;
        values.mPosition = mLayoutManager.findFirstVisibleItemPosition();
        if (values.mPosition == NO_POSITION) {
            values.reset();
            return;
        }
        View firstVisible = mLayoutManager.findViewByPosition(values.mPosition);
        if (firstVisible == null) {
            values.reset();
            return;
        }
        int leftDecorations = mLayoutManager.getLeftDecorationWidth(firstVisible);
        int rightDecorations = mLayoutManager.getRightDecorationWidth(firstVisible);
        int topDecorations = mLayoutManager.getTopDecorationHeight(firstVisible);
        int bottomDecorations = mLayoutManager.getBottomDecorationHeight(firstVisible);
        ViewGroup.LayoutParams params = firstVisible.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
            leftDecorations += margins.leftMargin;
            rightDecorations += margins.rightMargin;
            topDecorations += margins.topMargin;
            bottomDecorations += margins.bottomMargin;
        }
        int height = firstVisible.getHeight() + topDecorations + bottomDecorations;
        int width = firstVisible.getWidth() + leftDecorations + rightDecorations;
        int start;
        int sizePx;
        if (mLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
            start = (firstVisible.getLeft() - leftDecorations) - mRecyclerView.getPaddingLeft();
            if (mViewPager.isRtl()) {
                start = -start;
            }
            sizePx = width;
        } else {
            start = (firstVisible.getTop() - topDecorations) - mRecyclerView.getPaddingTop();
            sizePx = height;
        }
        values.mOffsetPx = -start;
        if (values.mOffsetPx < 0) {
            if (new COUIAnimateLayoutChangeDetector(mLayoutManager).mayHaveInterferingAnimations()) {
                throw new IllegalStateException("Page(s) contain a ViewGroup with a LayoutTransition (or animateLayoutChanges=\"true\"), which interferes with the scrolling animation. Make sure to call getLayoutTransition().setAnimateParentHierarchy(false) on all ViewGroups with a LayoutTransition before an animation is started.");
            }
            throw new IllegalStateException(String.format(Locale.US,
                    "Page can only be offset by a positive amount, not by %d", values.mOffsetPx));
        }
        values.mOffset = sizePx == 0 ? 0.0f : (float) values.mOffsetPx / sizePx;
    }

    public double getRelativeScrollPosition() {
        updateScrollEventValues();
        return mScrollValues.mPosition + mScrollValues.mOffset;
    }

    public int getScrollState() {
        return mScrollState;
    }

    public boolean isDragging() {
        return mScrollState == ViewPager2.SCROLL_STATE_DRAGGING;
    }

    public boolean isFakeDragging() {
        return mFakeDragging;
    }

    public boolean isIdle() {
        return mScrollState == ViewPager2.SCROLL_STATE_IDLE;
    }

    public void notifyBeginFakeDrag() {
        mAdapterState = STATE_IN_PROGRESS_FAKE_DRAG;
        startDrag(true);
    }

    public void notifyDataSetChangeHappened() {
        mDataSetChangeHappened = true;
    }

    public void notifyEndFakeDrag() {
        if (!isDragging() || mFakeDragging) {
            mFakeDragging = false;
            updateScrollEventValues();
            if (mScrollValues.mOffsetPx != 0) {
                dispatchStateChanged(ViewPager2.SCROLL_STATE_SETTLING);
                return;
            }
            if (mScrollValues.mPosition != mDragStartPosition) {
                dispatchSelected(mScrollValues.mPosition);
            }
            dispatchStateChanged(ViewPager2.SCROLL_STATE_IDLE);
            resetState();
        }
    }

    public void notifyProgrammaticScroll(int target, boolean smooth) {
        mAdapterState = smooth ? STATE_IN_PROGRESS_SMOOTH_SCROLL : STATE_IN_PROGRESS_IMMEDIATE_SCROLL;
        mFakeDragging = false;
        boolean hasNewTarget = mTarget != target;
        mTarget = target;
        dispatchStateChanged(ViewPager2.SCROLL_STATE_SETTLING);
        if (hasNewTarget) {
            dispatchSelected(target);
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (!(mAdapterState == STATE_IN_PROGRESS_MANUAL_DRAG
                && mScrollState == ViewPager2.SCROLL_STATE_DRAGGING)
                && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            startDrag(false);
            return;
        }
        if (isInAnyDraggingState() && newState == RecyclerView.SCROLL_STATE_SETTLING) {
            if (mScrollHappened) {
                dispatchStateChanged(ViewPager2.SCROLL_STATE_SETTLING);
                mDispatchSelected = true;
            }
            return;
        }
        if (isInAnyDraggingState() && newState == RecyclerView.SCROLL_STATE_IDLE) {
            updateScrollEventValues();
            if (mScrollHappened) {
                if (mScrollValues.mOffsetPx == 0 && mDragStartPosition != mScrollValues.mPosition) {
                    dispatchSelected(mScrollValues.mPosition);
                }
            } else if (mScrollValues.mPosition != NO_POSITION) {
                dispatchScrolled(mScrollValues.mPosition, 0.0f, 0);
            }
            dispatchStateChanged(ViewPager2.SCROLL_STATE_IDLE);
            resetState();
        }
        if (mAdapterState == STATE_IN_PROGRESS_SMOOTH_SCROLL
                && newState == RecyclerView.SCROLL_STATE_IDLE
                && mDataSetChangeHappened) {
            updateScrollEventValues();
            if (mScrollValues.mOffsetPx == 0) {
                if (mTarget != mScrollValues.mPosition) {
                    dispatchSelected(mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition);
                }
                dispatchStateChanged(ViewPager2.SCROLL_STATE_IDLE);
                resetState();
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        mScrollHappened = true;
        updateScrollEventValues();
        if (mDispatchSelected) {
            mDispatchSelected = false;
            boolean scrollingForward = dy > 0
                    || (dy == 0 && ((dx < 0) == mViewPager.isRtl()));
            if (scrollingForward && mScrollValues.mOffsetPx != 0) {
                mTarget = mScrollValues.mPosition + 1;
            } else {
                mTarget = mScrollValues.mPosition;
            }
            if (mDragStartPosition != mTarget) {
                dispatchSelected(mTarget);
            }
        } else if (mAdapterState == STATE_IDLE) {
            dispatchSelected(mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition);
        }
        int position = mScrollValues.mPosition == NO_POSITION ? 0 : mScrollValues.mPosition;
        dispatchScrolled(position, mScrollValues.mOffset, mScrollValues.mOffsetPx);
        if ((mScrollValues.mPosition == mTarget || mTarget == NO_POSITION)
                && mScrollValues.mOffsetPx == 0
                && mScrollState != ViewPager2.SCROLL_STATE_DRAGGING) {
            dispatchStateChanged(ViewPager2.SCROLL_STATE_IDLE);
            resetState();
        }
    }

    public void setOnPageChangeCallback(ViewPager2.OnPageChangeCallback callback) {
        mCallback = callback;
    }
}
