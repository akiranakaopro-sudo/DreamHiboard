package com.coui.appcompat.viewpager;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class COUIViewPager2 extends ViewGroup {
    public static final int OFFSCREEN_PAGE_LIMIT_DEFAULT = -1;
    public static final int ORIENTATION_HORIZONTAL = ViewPager2.ORIENTATION_HORIZONTAL;
    public static final int ORIENTATION_VERTICAL = ViewPager2.ORIENTATION_VERTICAL;
    public static final int SCROLL_STATE_IDLE = ViewPager2.SCROLL_STATE_IDLE;
    public static final int SCROLL_STATE_DRAGGING = ViewPager2.SCROLL_STATE_DRAGGING;
    public static final int SCROLL_STATE_SETTLING = ViewPager2.SCROLL_STATE_SETTLING;

    private AnimationConfig mAnimationConfig = new AnimationConfig();
    int mCurrentItem;
    private RecyclerView.AdapterDataObserver mCurrentItemDataSetChangeObserver;
    boolean mCurrentItemDirty;
    private int mOffscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT;
    private final COUICompositeOnPageChangeCallback mExternalPageChangeCallbacks =
            new COUICompositeOnPageChangeCallback(3);
    private final COUICompositeOnPageChangeCallback mPageChangeEventDispatcher =
            new COUICompositeOnPageChangeCallback(3);
    private COUIPageTransformerAdapter mPageTransformerAdapter;
    private final PagerSnapHelper mPagerSnapHelper = new PagerSnapHelper();
    private final Rect mTmpChildRect = new Rect();
    private final Rect mTmpContainerRect = new Rect();
    private boolean mUserInputEnabled = true;
    LinearLayoutManager mLayoutManager;
    RecyclerViewImpl mRecyclerView;
    COUIScrollEventAdapter mScrollEventAdapter;
    private COUIFakeDrag mFakeDragger;

    @Retention(RetentionPolicy.SOURCE)
    public @interface OffscreenPageLimit {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    public COUIViewPager2(Context context) {
        this(context, null);
    }

    public COUIViewPager2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIViewPager2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mRecyclerView = new RecyclerViewImpl(context);
        mRecyclerView.setId(ViewCompat.generateViewId());
        mRecyclerView.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        mLayoutManager = new LinearLayoutManagerImpl(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        mPagerSnapHelper.attachToRecyclerView(mRecyclerView);
        mScrollEventAdapter = new COUIScrollEventAdapter(this);
        mRecyclerView.addOnScrollListener(mScrollEventAdapter);
        mFakeDragger = new COUIFakeDrag(this, mScrollEventAdapter, mRecyclerView);
        mPageTransformerAdapter = new COUIPageTransformerAdapter(mLayoutManager);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mScrollSelectedUpdater);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mPageTransformerAdapter);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mExternalPageChangeCallbacks);
        mScrollEventAdapter.setOnPageChangeCallback(mPageChangeEventDispatcher);
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (params.width != LayoutParams.MATCH_PARENT || params.height != LayoutParams.MATCH_PARENT) {
                    throw new IllegalStateException("Pages must fill the whole ViewPager2 (use match_parent)");
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
            }
        });
        mCurrentItemDataSetChangeObserver = new DataSetChangeObserver() {
            @Override
            public void onChanged() {
                mCurrentItemDirty = true;
                mScrollEventAdapter.notifyDataSetChangeHappened();
            }
        };
        addView(mRecyclerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private final ViewPager2.OnPageChangeCallback mScrollSelectedUpdater =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == SCROLL_STATE_IDLE) {
                        updateCurrentItem();
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (mCurrentItem != position) {
                        mCurrentItem = position;
                    }
                }
            };

    private int getPageSize() {
        RecyclerView recyclerView = mRecyclerView;
        if (getOrientation() == ORIENTATION_HORIZONTAL) {
            return recyclerView.getWidth() - recyclerView.getPaddingLeft() - recyclerView.getPaddingRight();
        }
        return recyclerView.getHeight() - recyclerView.getPaddingTop() - recyclerView.getPaddingBottom();
    }

    private void updateCurrentItem() {
        View snapView = mPagerSnapHelper.findSnapView(mLayoutManager);
        if (snapView == null) {
            return;
        }
        int snapPosition = mLayoutManager.getPosition(snapView);
        if (snapPosition != mCurrentItem && getScrollState() == SCROLL_STATE_IDLE) {
            mPageChangeEventDispatcher.onPageSelected(snapPosition);
        }
        mCurrentItemDirty = false;
    }

    private void setCurrentItemInternal(int item, boolean smoothScroll) {
        RecyclerView.Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            mCurrentItem = Math.max(item, 0);
            return;
        }
        int itemCount = adapter.getItemCount();
        if (itemCount <= 0) {
            return;
        }
        int target = Math.max(0, Math.min(item, itemCount - 1));
        if (target == mCurrentItem && mScrollEventAdapter.isIdle()) {
            return;
        }
        mCurrentItem = target;
        mScrollEventAdapter.notifyProgrammaticScroll(target, smoothScroll);
        if (smoothScroll) {
            mRecyclerView.smoothScrollToPosition(target);
        } else {
            mRecyclerView.scrollToPosition(target);
            mScrollEventAdapter.notifyProgrammaticScroll(target, false);
        }
    }

    static COUIViewPager2 findOwner(View page) {
        ViewParent parent = page.getParent();
        while (parent != null) {
            if (parent instanceof COUIViewPager2) {
                return (COUIViewPager2) parent;
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException("Expected the page view to be managed by a ViewPager2 instance.");
    }

    void snapToPageOpen() {
        View snapView = mPagerSnapHelper.findSnapView(mLayoutManager);
        if (snapView == null) {
            return;
        }
        int[] distance = mPagerSnapHelper.calculateDistanceToFinalSnap(mLayoutManager, snapView);
        if (distance != null && (distance[0] != 0 || distance[1] != 0)) {
            mRecyclerView.smoothScrollBy(distance[0], distance[1]);
        }
    }

    public boolean beginFakeDrag() {
        return mFakeDragger.beginFakeDrag();
    }

    public boolean endFakeDrag() {
        return mFakeDragger.endFakeDrag();
    }

    public boolean fakeDragBy(float offsetPxFloat) {
        return mFakeDragger.fakeDragBy(offsetPxFloat);
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public AnimationConfig getAnimationConfig() {
        return mAnimationConfig;
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public int getItemDecorationCount() {
        return mRecyclerView.getItemDecorationCount();
    }

    public int getOffscreenPageLimit() {
        return mOffscreenPageLimit;
    }

    public int getOrientation() {
        return mLayoutManager.getOrientation();
    }

    public int getScrollState() {
        return mScrollEventAdapter.getScrollState();
    }

    public boolean isFakeDragging() {
        return mFakeDragger.isFakeDragging();
    }

    public boolean isRtl() {
        return mLayoutManager.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public boolean isUserInputEnabled() {
        return mUserInputEnabled;
    }

    public void registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback callback) {
        mExternalPageChangeCallbacks.addOnPageChangeCallback(callback);
    }

    public void removeItemDecoration(RecyclerView.ItemDecoration decoration) {
        mRecyclerView.removeItemDecoration(decoration);
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        RecyclerView.Adapter<?> oldAdapter = mRecyclerView.getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(mCurrentItemDataSetChangeObserver);
        }
        mRecyclerView.setAdapter(adapter);
        mCurrentItem = 0;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mCurrentItemDataSetChangeObserver);
        }
    }

    public void setAnimationConfig(AnimationConfig animationConfig) {
        mAnimationConfig = animationConfig != null ? animationConfig : new AnimationConfig();
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (isFakeDragging()) {
            throw new IllegalStateException("Cannot change current item when ViewPager2 is fake dragging");
        }
        setCurrentItemInternal(item, smoothScroll);
    }

    public void setCurrentItemWithoutAnimation(int item) {
        setCurrentItem(item, false);
    }

    public void setOffscreenPageLimit(@OffscreenPageLimit int limit) {
        if (limit < 1 && limit != OFFSCREEN_PAGE_LIMIT_DEFAULT) {
            throw new IllegalArgumentException("Offscreen page limit must be OFFSCREEN_PAGE_LIMIT_DEFAULT or a number > 0");
        }
        mOffscreenPageLimit = limit;
        mRecyclerView.requestLayout();
    }

    public void setOrientation(@Orientation int orientation) {
        mLayoutManager.setOrientation(orientation);
    }

    public void setPageTransformer(ViewPager2.PageTransformer transformer) {
        if (transformer != null) {
            if (mRecyclerView.getItemAnimator() != null) {
                mRecyclerView.setItemAnimator(null);
            }
        }
        mPageTransformerAdapter.setPageTransformer(transformer);
        requestTransform();
    }

    public void setUserInputEnabled(boolean enabled) {
        mUserInputEnabled = enabled;
    }

    public void unregisterOnPageChangeCallback(ViewPager2.OnPageChangeCallback callback) {
        mExternalPageChangeCallbacks.removeOnPageChangeCallback(callback);
    }

    public void requestTransform() {
        if (mPageTransformerAdapter.getPageTransformer() == null) {
            return;
        }
        double relativePosition = mScrollEventAdapter.getRelativeScrollPosition();
        int position = (int) relativePosition;
        float offset = (float) (relativePosition - position);
        int offsetPx = Math.round(getPageSize() * offset);
        mPageTransformerAdapter.onPageScrolled(position, offset, offsetPx);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();
        mTmpContainerRect.left = getPaddingLeft();
        mTmpContainerRect.right = (right - left) - getPaddingRight();
        mTmpContainerRect.top = getPaddingTop();
        mTmpContainerRect.bottom = (bottom - top) - getPaddingBottom();
        mTmpChildRect.left = mTmpContainerRect.left;
        mTmpChildRect.top = mTmpContainerRect.top;
        mTmpChildRect.right = mTmpChildRect.left + width;
        mTmpChildRect.bottom = mTmpChildRect.top + height;
        mRecyclerView.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
        if (mCurrentItemDirty) {
            updateCurrentItem();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mRecyclerView, widthMeasureSpec, heightMeasureSpec);
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();
        int childState = mRecyclerView.getMeasuredState();
        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();
        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    public static abstract class DataSetChangeObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public abstract void onChanged();

        @Override public final void onItemRangeChanged(int positionStart, int itemCount) { onChanged(); }
        @Override public final void onItemRangeChanged(int positionStart, int itemCount, Object payload) { onChanged(); }
        @Override public final void onItemRangeInserted(int positionStart, int itemCount) { onChanged(); }
        @Override public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) { onChanged(); }
        @Override public final void onItemRangeRemoved(int positionStart, int itemCount) { onChanged(); }
    }

    public class LinearLayoutManagerImpl extends LinearLayoutManager {
        public LinearLayoutManagerImpl(Context context) {
            super(context);
        }

        @Override
        protected void calculateExtraLayoutSpace(RecyclerView.State state, int[] extraLayoutSpace) {
            int offscreenPageLimit = getOffscreenPageLimit();
            if (offscreenPageLimit == OFFSCREEN_PAGE_LIMIT_DEFAULT) {
                super.calculateExtraLayoutSpace(state, extraLayoutSpace);
                return;
            }
            int pageSize = getPageSize() * offscreenPageLimit;
            extraLayoutSpace[0] = pageSize;
            extraLayoutSpace[1] = pageSize;
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect,
                boolean immediate, boolean focusedChildVisible) {
            return false;
        }
    }

    public class RecyclerViewImpl extends COUIRecyclerView {
        public RecyclerViewImpl(Context context) {
            super(context);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            return isUserInputEnabled() && super.onInterceptTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return isUserInputEnabled() && super.onTouchEvent(event);
        }
    }

    public static class AnimationConfig {
        private boolean mEnableVelocityTracking;
        private ProgramScrollConfig mProgramScrollConfig;
        private FlingScrollConfig mFlingScrollConfig;

        public FlingScrollConfig getFlingScrollConfig() {
            return mFlingScrollConfig;
        }

        public ProgramScrollConfig getProgramScrollConfig() {
            return mProgramScrollConfig;
        }

        public boolean isVelocityTrackingEnabled() {
            return mEnableVelocityTracking;
        }

        public AnimationConfig setFlingScrollConfig(FlingScrollConfig config) {
            mFlingScrollConfig = config;
            mEnableVelocityTracking = config != null;
            return this;
        }

        public AnimationConfig setProgramScrollConfig(ProgramScrollConfig config) {
            mProgramScrollConfig = config;
            return this;
        }

        public class ProgramScrollConfig {
            public final int mDurationMs;
            public final int mDuration;
            public final Interpolator mInterpolator;

            public ProgramScrollConfig(int durationMs, Interpolator interpolator) {
                mDurationMs = durationMs;
                mDuration = durationMs;
                mInterpolator = interpolator != null ? interpolator : new LinearInterpolator();
            }
        }

        public class FlingScrollConfig {
            public final int mMinDurationMs;
            public final int mMaxDurationMs;
            public final Interpolator mInterpolator;
            public final float mVelocityThreshold;
            public final int mMaxVelocity;
            public final float mVelocityDecayFactor;
            public final int mMinDuration;
            public final int mMaxDuration;
            public final float mMaxFlingThreshold;
            public final int mMaxFlingExcess;
            public final float mTimeInVelocityRatio;

            public FlingScrollConfig(int minDurationMs, int maxDurationMs,
                    Interpolator interpolator, float velocityThreshold, int maxVelocity,
                    float velocityDecayFactor) {
                mMinDurationMs = minDurationMs;
                mMaxDurationMs = maxDurationMs;
                mInterpolator = interpolator != null ? interpolator : new LinearInterpolator();
                mVelocityThreshold = velocityThreshold;
                mMaxVelocity = maxVelocity;
                mVelocityDecayFactor = velocityDecayFactor;
                mMinDuration = minDurationMs;
                mMaxDuration = maxDurationMs;
                mMaxFlingThreshold = velocityThreshold;
                mMaxFlingExcess = maxVelocity;
                mTimeInVelocityRatio = velocityDecayFactor;
            }
        }
    }
}
