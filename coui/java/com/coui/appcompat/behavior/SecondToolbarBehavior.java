package com.coui.appcompat.behavior;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.coui.appcompat.R;
import com.google.android.material.appbar.AppBarLayout;

public class SecondToolbarBehavior extends CoordinatorLayout.Behavior<AppBarLayout>
        implements AbsListView.OnScrollListener {
    private View mChild;
    private int mCountOffset;
    private int mDividerAlphaChangeEndY;
    private int mDividerAlphaChangeOffset;
    private int mDividerWidthChangeEndY;
    private int mDividerWidthChangeInitY;
    private int mDividerWidthChangeOffset;
    public int mDividerInitWidth;
    private View mDivider;
    private ViewGroup.LayoutParams mDividerParams;
    private boolean mIsImmerSiveTheme;
    private int mListFirstChildInitY;
    private final int[] mLocation;
    private int mMarginLeftRight;
    private Resources mResources;
    private View mScrollView;

    public SecondToolbarBehavior() {
        mLocation = new int[2];
    }

    public SecondToolbarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLocation = new int[2];
        init(context);
    }

    private void init(Context context) {
        mResources = context.getResources();
        mMarginLeftRight = mResources.getDimensionPixelOffset(R.dimen.preference_divider_margin_horizontal);
        mDividerAlphaChangeOffset = mResources.getDimensionPixelOffset(R.dimen.preference_line_alpha_range_change_offset);
        mDividerWidthChangeOffset = mResources.getDimensionPixelOffset(R.dimen.preference_divider_width_change_offset);
        mCountOffset = mResources.getDimensionPixelOffset(R.dimen.preference_divider_width_start_count_offset);
        mIsImmerSiveTheme = mResources.getBoolean(R.bool.is_dialog_preference_immersive);
    }

    private void onListScroll() {
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
        if (mChild == null || mScrollView == null || mDivider == null) {
            return;
        }
        mChild.getLocationOnScreen(mLocation);
        int childY = mLocation[1];
        int[] rootLocation = new int[2];
        mScrollView.getRootView().getLocationOnScreen(rootLocation);
        if (rootLocation[1] != 0) {
            childY -= rootLocation[1];
        }

        int alphaOffset;
        if (childY < mDividerAlphaChangeEndY) {
            alphaOffset = mDividerAlphaChangeOffset;
        } else if (childY > mListFirstChildInitY) {
            alphaOffset = 0;
        } else {
            alphaOffset = mListFirstChildInitY - childY;
        }
        float alphaRange = Math.abs(alphaOffset) / (float) mDividerAlphaChangeOffset;
        if (alphaRange <= 1.0f) {
            mDivider.setAlpha(alphaRange);
        }

        int widthOffset;
        if (childY < mDividerWidthChangeEndY) {
            widthOffset = mDividerWidthChangeOffset;
        } else if (childY > mDividerWidthChangeInitY) {
            widthOffset = 0;
        } else {
            widthOffset = mDividerWidthChangeInitY - childY;
        }
        float widthRange = Math.abs(widthOffset) / (float) mDividerWidthChangeOffset;
        if (mDividerParams instanceof ViewGroup.MarginLayoutParams) {
            int margin = (int) (mMarginLeftRight * (1.0f - widthRange));
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mDividerParams;
            marginParams.leftMargin = margin;
            marginParams.rightMargin = margin;
        }
        mDivider.setLayoutParams(mDividerParams);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        onListScroll();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public boolean onStartNestedScroll(
            CoordinatorLayout coordinatorLayout,
            AppBarLayout child,
            View directTargetChild,
            View target,
            int axes,
            int type
    ) {
        boolean nestedVertical = (axes & View.SCROLL_AXIS_VERTICAL) != 0
                && coordinatorLayout.getHeight() - directTargetChild.getHeight() <= child.getHeight();
        if (!mIsImmerSiveTheme && nestedVertical) {
            if (mListFirstChildInitY <= 0) {
                mScrollView = target;
                mDivider = child.findViewById(R.id.divider_line);
            }
            int measuredHeight = child.getMeasuredHeight();
            mListFirstChildInitY = measuredHeight;
            mDividerAlphaChangeEndY = measuredHeight - mDividerAlphaChangeOffset;
            int widthChangeInitY = measuredHeight - mCountOffset;
            mDividerWidthChangeInitY = widthChangeInitY;
            mDividerWidthChangeEndY = widthChangeInitY - mDividerWidthChangeOffset;
            if (mDivider != null) {
                mDividerInitWidth = mDivider.getWidth();
                mDividerParams = mDivider.getLayoutParams();
            }
            target.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> onListScroll());
        }
        return false;
    }
}
