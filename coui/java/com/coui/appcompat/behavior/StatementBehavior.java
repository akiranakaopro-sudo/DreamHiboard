package com.coui.appcompat.behavior;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.coui.appcompat.R;

public class StatementBehavior extends CoordinatorLayout.Behavior<View> {
    private View mChild;
    private int mCurrentOffset;
    private View mDivider;
    private int mDividerAlphaChangeEndY;
    private int mDividerAlphaChangeOffset;
    private float mDividerAlphaRange;
    public int mDividerInitWidth;
    private ViewGroup.LayoutParams mDividerParams;
    private int mDividerWidthChangeEndY;
    private int mDividerWidthChangeInitY;
    private int mDividerWidthChangeOffset;
    private float mDividerWidthRange;
    private int mListFirstChildInitY;
    private final int[] mLocation;
    private int mLocationY;
    private int mMarginLeftRight;
    private int mNewOffset;
    private Resources mResources;
    private View mScrollView;

    public StatementBehavior() {
        mLocation = new int[2];
    }

    public StatementBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLocation = new int[2];
        init(context);
    }

    private void init(Context context) {
        mResources = context.getResources();
        mMarginLeftRight = mResources.getDimensionPixelOffset(
                R.dimen.preference_divider_margin_horizontal) * 2;
        mDividerAlphaChangeOffset = mResources.getDimensionPixelOffset(
                R.dimen.preference_line_alpha_range_change_offset);
        mDividerWidthChangeOffset = mResources.getDimensionPixelOffset(
                R.dimen.preference_divider_width_change_offset);
    }

    private void onScroll() {
        mChild = null;
        View scrollView = mScrollView;
        if (scrollView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) scrollView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mChild = viewGroup.getChildAt(i);
                    break;
                }
            }
        }
        if (mChild == null) {
            mChild = mScrollView;
        }
        if (mChild == null || mDivider == null || mDividerParams == null) {
            return;
        }
        mChild.getLocationOnScreen(mLocation);
        mLocationY = mLocation[1];
        mNewOffset = 0;
        if (mLocationY < mDividerAlphaChangeEndY) {
            mNewOffset = mDividerAlphaChangeOffset;
        } else if (mLocationY > mListFirstChildInitY) {
            mNewOffset = 0;
        } else {
            mNewOffset = mListFirstChildInitY - mLocationY;
        }
        mCurrentOffset = mNewOffset;
        if (mDividerAlphaRange <= 1.0f) {
            float alphaRange = Math.abs(mCurrentOffset) / (float) mDividerAlphaChangeOffset;
            mDividerAlphaRange = alphaRange;
            mDivider.setAlpha(alphaRange);
        }
        if (mLocationY < mDividerWidthChangeEndY) {
            mNewOffset = mDividerWidthChangeOffset;
        } else if (mLocationY > mDividerWidthChangeInitY) {
            mNewOffset = 0;
        } else {
            mNewOffset = mDividerWidthChangeInitY - mLocationY;
        }
        mCurrentOffset = mNewOffset;
        float widthRange = Math.abs(mCurrentOffset) / (float) mDividerWidthChangeOffset;
        mDividerWidthRange = widthRange;
        mDividerParams.width = (int) (mDividerInitWidth
                - (mMarginLeftRight * (1.0f - widthRange)));
        mDivider.setLayoutParams(mDividerParams);
    }

    @Override
    public boolean onStartNestedScroll(
            CoordinatorLayout coordinatorLayout,
            View child,
            View directTargetChild,
            View target,
            int axes,
            int type
    ) {
        if (mListFirstChildInitY <= 0) {
            child.getLocationOnScreen(mLocation);
            mListFirstChildInitY = mLocation[1];
            mScrollView = target;
            mDivider = child.findViewById(R.id.divider_line);
            if (mDivider != null) {
                mDividerInitWidth = mDivider.getWidth();
                mDividerParams = mDivider.getLayoutParams();
            }
            mDividerAlphaChangeEndY = mListFirstChildInitY - mDividerAlphaChangeOffset;
            int widthChangeInitY = mListFirstChildInitY - mResources.getDimensionPixelOffset(
                    R.dimen.preference_divider_width_start_count_offset);
            mDividerWidthChangeInitY = widthChangeInitY;
            mDividerWidthChangeEndY = widthChangeInitY - mDividerWidthChangeOffset;
        }
        target.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> onScroll());
        return false;
    }
}
