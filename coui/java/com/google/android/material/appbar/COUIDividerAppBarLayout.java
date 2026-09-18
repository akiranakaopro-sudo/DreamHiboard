package com.google.android.material.appbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.scrollview.COUINestedScrollView;

/**
 * Leapy added 2026-07-20: Source-compatible port of decoded OPPO
 * com.google.android.material.appbar.COUIDividerAppBarLayout.
 *
 * The decompiler-only SystemUI R and synthetic outline references were replaced
 * with the COUI library's own generated R class and ordinary Java logging/math.
 */
public class COUIDividerAppBarLayout extends AppBarLayout {
    private static final String TAG = "COUIDividerAppBarLayout";
    private static final String SUPER_STATE_KEY = "SUPER_STATE_KEY";
    private static final String OFFSET_DY_SCROLL_STATE_KEY = "OFFSET_DY_SCROLL_STATE_KEY";
    private static final String SCROLL_DY_SCROLL_STATE_KEY = "SCROLL_DY_SCROLL_STATE_KEY";
    private static final String OVERSCROLL_DY_SCROLL_STATE_KEY =
            "OVERSCROLL_DY_SCROLL_STATE_KEY";
    private static final String DIVIDER_FRACTION_STATE_KEY = "DIVIDER_FRACTION_STATE_KEY";

    private static boolean sDebug;

    protected boolean mCollapsable;
    protected int mScrollDyByOffset;
    protected int mScrollDyByOverScroll;
    protected int mScrollDyByScroll;
    protected RecyclerView mTargetView;
    protected int mTargetViewState;

    private float mDividerEndAlpha;
    private int mDividerEndMarginHorizontal;
    private float mDividerFraction;
    private float mDividerStartAlpha;
    private int mDividerStartMarginHorizontal;
    private View mDividerView;
    private boolean mHasDivider;
    private OnDividerProgressChangedListener mOnDividerProgressChangedListener;
    private View.OnLayoutChangeListener mOnLayoutChangeListener;
    private RecyclerView.OnScrollListener mOnScrollListener;

    public interface OnDividerProgressChangedListener {
        void onDividerProgressChanged(float fraction);
    }

    private final class DividerAppBarOnScrollChangedListener
            implements ViewTreeObserver.OnScrollChangedListener {
        private AppBarLayout mAppBar;
        private View mTarget;

        @Override
        public void onScrollChanged() {
            if (mTarget instanceof COUIRecyclerView) {
                mScrollDyByOverScroll = Math.max(0, mTarget.getScrollY());
                onDividerChanged();
                if (mScrollDyByOverScroll == 0 && mTarget.getViewTreeObserver().isAlive()) {
                    mTarget.getViewTreeObserver().removeOnScrollChangedListener(this);
                }
            }
        }

        void setAppBar(AppBarLayout appBar) {
            mAppBar = appBar;
        }

        void setTarget(View target) {
            mTarget = target;
        }
    }

    private final class DividerBehavior extends AppBarLayout.Behavior {
        private DividerAppBarOnScrollChangedListener mScrollChangedListener;
        private boolean mStopAfterFling;

        // Leapy added 2026-07-30: Preserve the decoded OPPO divider behavior.
        @Override
        public boolean onNestedPreFling(@NonNull CoordinatorLayout parent,
                @NonNull AppBarLayout child, @NonNull View target,
                float velocityX, float velocityY) {
            mStopAfterFling = true;
            return super.onNestedPreFling(parent, child, target, velocityX, velocityY);
        }

        @Override
        public void onNestedScroll(@NonNull CoordinatorLayout parent,
                @NonNull AppBarLayout child, @NonNull View target, int dxConsumed,
                int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type,
                @NonNull int[] consumed) {
            if (target instanceof COUIRecyclerView) {
                int oldOverScroll = mScrollDyByOverScroll;
                mScrollDyByOverScroll = Math.max(0, target.getScrollY());
                if (oldOverScroll != mScrollDyByOverScroll) {
                    onDividerChanged();
                }
            }
            super.onNestedScroll(parent, child, target, dxConsumed, dyConsumed,
                    dxUnconsumed, dyUnconsumed, type, consumed);
        }

        @Override
        public boolean onStartNestedScroll(@NonNull CoordinatorLayout parent,
                @NonNull AppBarLayout child, @NonNull View directTargetChild,
                @NonNull View target, int axes, int type) {
            return super.onStartNestedScroll(parent, child, directTargetChild, target, axes, type)
                    || isDividerAnimEnable();
        }

        @Override
        public void onStopNestedScroll(@NonNull CoordinatorLayout parent,
                @NonNull AppBarLayout child, @NonNull View target, int type) {
            if (target instanceof COUIRecyclerView) {
                if (mScrollChangedListener == null) {
                    mScrollChangedListener = new DividerAppBarOnScrollChangedListener();
                }
                mScrollChangedListener.setTarget(target);
                mScrollChangedListener.setAppBar(child);
                target.getViewTreeObserver().addOnScrollChangedListener(mScrollChangedListener);
            }
            if (!mStopAfterFling) {
                super.onStopNestedScroll(parent, child, target, type);
            }
            mStopAfterFling = false;
            // Leapy end
        }
    }

    public COUIDividerAppBarLayout(Context context) {
        this(context, null);
    }

    public COUIDividerAppBarLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIDividerAppBarLayout(Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCollapsable = false;
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(
                attrs, R.styleable.COUIDividerAppBarLayout);
        mHasDivider = array.getBoolean(
                R.styleable.COUIDividerAppBarLayout_hasDivider, true);
        mDividerStartAlpha = array.getFloat(
                R.styleable.COUIDividerAppBarLayout_dividerStartAlpha, 0.0f);
        mDividerEndAlpha = array.getFloat(
                R.styleable.COUIDividerAppBarLayout_dividerEndAlpha,
                mHasDivider ? 1.0f : 0.0f);
        mDividerStartMarginHorizontal = array.getDimensionPixelOffset(
                R.styleable.COUIDividerAppBarLayout_dividerStartMarginHorizontal,
                getResources().getDimensionPixelOffset(
                        R.dimen.coui_appbar_divider_expanded_margin_horizontal));
        mDividerEndMarginHorizontal = array.getDimensionPixelOffset(
                R.styleable.COUIDividerAppBarLayout_dividerEndMarginHorizontal,
                getResources().getDimensionPixelOffset(
                        R.dimen.coui_appbar_divider_collapsed_margin_horizontal));
        array.recycle();

        mDividerStartAlpha = clamp(mDividerStartAlpha);
        mDividerEndAlpha = clamp(mDividerEndAlpha);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                mTargetViewState = newState;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                mScrollDyByScroll = recyclerView.computeVerticalScrollOffset();
                onDividerChanged();
            }
        };
        mOnLayoutChangeListener = (view, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> refreshAppBar(view);
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(value, 1.0f));
    }

    @Override
    @NonNull
    public CoordinatorLayout.Behavior<AppBarLayout> getBehavior() {
        return new DividerBehavior();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDividerView == null) {
            mDividerView = LayoutInflater.from(getContext()).inflate(
                    R.layout.coui_appbar_divider_layout, this, false);
            addView(mDividerView, getChildCount());
            mDividerView.setAlpha(mDividerStartAlpha);
        }
        mDividerView.setBackgroundColor(
                COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDivider));
        // Leapy modified 2026-07-30: Decoded OPPO applies the restored/start
        // fraction before discovering and binding the scrolling child.
        refreshDivider();
        mDividerView.setVisibility(mHasDivider ? VISIBLE : GONE);
        mDividerView.setForceDarkAllowed(false);
        findRecyclerView();
        bindListener();
        // Leapy end
        setTouchscreenBlocksFocus(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unbindListener();
    }

    private void findRecyclerView() {
        View parent = (View) getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup parentGroup = (ViewGroup) parent;
        for (int i = 0; i < parentGroup.getChildCount(); i++) {
            View child = parentGroup.getChildAt(i);
            if (child instanceof RecyclerView) {
                mTargetView = (RecyclerView) child;
                return;
            }
        }
    }

    private void bindListener() {
        if (mTargetView == null) {
            if (sDebug) {
                Log.d(TAG, "Can not find RecyclerView");
            }
            return;
        }
        mTargetView.addOnScrollListener(mOnScrollListener);
        mTargetView.addOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    private void unbindListener() {
        if (mTargetView != null) {
            mTargetView.removeOnScrollListener(mOnScrollListener);
            mTargetView.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        }
    }

    public void bindRecyclerView(@Nullable RecyclerView recyclerView) {
        unbindListener();
        mTargetView = recyclerView;
        bindListener();
    }

    private boolean isDividerAnimEnable() {
        return mDividerView != null && mHasDivider
                && (mDividerStartAlpha != mDividerEndAlpha
                || mDividerStartMarginHorizontal != mDividerEndMarginHorizontal);
    }

    public void onDividerChanged() {
        int totalScroll = Math.max(0, mScrollDyByScroll)
                + mScrollDyByOffset + mScrollDyByOverScroll;
        if (totalScroll < 0 || !isDividerAnimEnable()) {
            return;
        }
        float oldFraction = mDividerFraction;
        int range = getDividerScrollRange();
        mDividerFraction = range == 0 ? 0.0f : Math.min((float) totalScroll / range, 1.0f);
        applyDividerFraction(oldFraction != mDividerFraction);
    }

    private void applyDividerFraction(boolean notifyProgress) {
        float alpha = mDividerStartAlpha
                + ((mDividerEndAlpha - mDividerStartAlpha) * mDividerFraction);
        int margin = mDividerStartMarginHorizontal
                + (int) ((mDividerEndMarginHorizontal - mDividerStartMarginHorizontal)
                * mDividerFraction);
        if (mDividerView != null) {
            mDividerView.setAlpha(alpha);
            setDividerHorizontalMargin(margin);
        }
        if (notifyProgress && mOnDividerProgressChangedListener != null) {
            mOnDividerProgressChangedListener.onDividerProgressChanged(mDividerFraction);
        }
    }

    private void setDividerHorizontalMargin(int margin) {
        if (mDividerView == null || !mDividerView.isAttachedToWindow()
                || mDividerView.getMeasuredWidth() <= 0) {
            return;
        }
        mDividerView.setPivotX(mDividerView.getMeasuredWidth() / 2.0f);
        mDividerView.setScaleX((float) (getMeasuredWidth() - (margin * 2))
                / mDividerView.getMeasuredWidth());
    }

    public boolean refreshAppBar(View view) {
        final int scrollY;
        if (view instanceof RecyclerView) {
            scrollY = ((RecyclerView) view).computeVerticalScrollOffset();
        } else if (view instanceof COUINestedScrollView) {
            scrollY = view.getScrollY();
        } else {
            return false;
        }
        if (scrollY == mScrollDyByScroll) {
            return false;
        }
        mScrollDyByScroll = scrollY;
        // Leapy modified 2026-07-30: Decoded OPPO only records layout-time
        // position here. onScrolled() performs the visible divider update.
        // Leapy end
        return true;
    }

    public void refreshDivider() {
        if (mDividerView != null) {
            mDividerView.setBackgroundColor(
                    COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDivider));
        }
        applyDividerFraction(false);
        onDividerChanged();
        if (mOnDividerProgressChangedListener != null) {
            mOnDividerProgressChangedListener.onDividerProgressChanged(mDividerFraction);
        }
    }

    public int getDividerScrollRange() {
        return getMeasuredHeight();
    }

    public boolean hasDivider() {
        return mHasDivider;
    }

    public void setHasDivider(boolean hasDivider) {
        mHasDivider = hasDivider;
        if (mDividerView != null) {
            mDividerView.setVisibility(hasDivider ? VISIBLE : GONE);
        }
    }

    public float getDividerStartAlpha() {
        return mDividerStartAlpha;
    }

    public void setDividerStartAlpha(float alpha) {
        mDividerStartAlpha = alpha;
    }

    public float getDividerEndAlpha() {
        return mDividerEndAlpha;
    }

    public void setDividerEndAlpha(float alpha) {
        mDividerEndAlpha = alpha;
    }

    public int getDividerStartMarginHorizontal() {
        return mDividerStartMarginHorizontal;
    }

    public void setDividerStartMarginHorizontal(int margin) {
        mDividerStartMarginHorizontal = margin;
    }

    public int getDividerEndMarginHorizontal() {
        return mDividerEndMarginHorizontal;
    }

    public void setDividerEndMarginHorizontal(int margin) {
        mDividerEndMarginHorizontal = margin;
    }

    public void setOnDividerProgressChangedListener(
            @Nullable OnDividerProgressChangedListener listener) {
        mOnDividerProgressChangedListener = listener;
    }

    public void translateDivider(int translationY) {
        if (mDividerView != null && mDividerView.isAttachedToWindow()) {
            mDividerView.setTranslationY(translationY);
        }
    }

    public void reset() {
        mScrollDyByScroll = 0;
        mScrollDyByOffset = 0;
        mScrollDyByOverScroll = 0;
        // Leapy modified 2026-07-30: Keep reset state-only, matching decoded
        // OPPO COUIDividerAppBarLayout.
        // Leapy end
    }

    public void setDebug(boolean debug) {
        sDebug = debug;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState());
        state.putInt(OFFSET_DY_SCROLL_STATE_KEY, mScrollDyByOffset);
        state.putInt(SCROLL_DY_SCROLL_STATE_KEY, mScrollDyByScroll);
        state.putInt(OVERSCROLL_DY_SCROLL_STATE_KEY, mScrollDyByOverScroll);
        state.putFloat(DIVIDER_FRACTION_STATE_KEY, mDividerFraction);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mScrollDyByOffset = bundle.getInt(OFFSET_DY_SCROLL_STATE_KEY);
            mScrollDyByScroll = bundle.getInt(SCROLL_DY_SCROLL_STATE_KEY);
            mScrollDyByOverScroll = bundle.getInt(OVERSCROLL_DY_SCROLL_STATE_KEY);
            mDividerFraction = bundle.getFloat(DIVIDER_FRACTION_STATE_KEY);
            state = bundle.getParcelable(SUPER_STATE_KEY);
        }
        super.onRestoreInstanceState(state);
    }
}
