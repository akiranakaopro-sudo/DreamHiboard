package com.coui.appcompat.tablayout;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.searchview.COUISearchBar;
import com.coui.appcompat.toolbar.COUIToolbar;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.component.responsiveui.ResponsiveUIModel;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import com.coui.appcompat.R;


public class COUIPercentTabWithSearchView extends FrameLayout {
    private static final int MAX_CHILD_COUNT = 2;
    private static final int RESPONSIVE_WIDTH_TYPE_COMPAT = 0;
    private static final int RESPONSIVE_WIDTH_TYPE_EXPANDED = 2;
    private static final int RESPONSIVE_WIDTH_TYPE_MEDIUM = 1;
    private int mHorizontalPaddingInLargerScreen;
    private ResponsiveUIModel mResponsiveUIModel;
    private int mResponsiveWidthSize;
    private final int[] mSearchBarPaddingEnd;
    private final int[] mSearchBarPaddingStart;

    public COUIPercentTabWithSearchView(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        initAttr();
        this.mResponsiveUIModel = new ResponsiveUIModel(context, 0, 0);
    }

    private void initAttr() {
        if (getContext() != null) {
            this.mHorizontalPaddingInLargerScreen = getContext().getResources().getDimensionPixelSize(R.dimen.coui_tab_search_horizontal_padding);
        }
    }

    private void layoutSearchBarAndTabLayout(View view, View view_2) {
        int iCalculateWidth;
        int iGutter;
        int iCalculateWidth2;
        int iGutter2;
        int height;
        boolean layoutDirection = ViewCompat.getLayoutDirection(this) == 1;
        if (COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth())) {
            if (view != null) {
                height = view.getHeight();
                view.layout(0, 0, view.getWidth(), height);
            } else {
                height = 0;
            }
            if (view_2 != null) {
                view_2.layout(0, height, view_2.getWidth(), view_2.getHeight() + height);
                return;
            }
            return;
        }
        if (layoutDirection) {
            if (view_2 != null) {
                if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                    view_2.layout(getMeasuredWidth() - view_2.getWidth(), (getMeasuredHeight() - view_2.getHeight()) / 2, getMeasuredWidth(), view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
                } else {
                    view_2.layout((getMeasuredWidth() - view_2.getWidth()) - this.mHorizontalPaddingInLargerScreen, (getMeasuredHeight() - view_2.getHeight()) / 2, getMeasuredWidth() - this.mHorizontalPaddingInLargerScreen, view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
                }
            }
            if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                iCalculateWidth2 = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
                iGutter2 = this.mResponsiveUIModel.gutter();
            } else {
                iCalculateWidth2 = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 8, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
                iGutter2 = this.mResponsiveUIModel.gutter();
            }
            int measuredWidth = getMeasuredWidth() - (iCalculateWidth2 + iGutter2);
            if (view != null) {
                view.layout(measuredWidth - view.getWidth(), 0, measuredWidth, view.getHeight());
                return;
            }
            return;
        }
        if (view_2 != null) {
            if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                view_2.layout(0, (getMeasuredHeight() - view_2.getHeight()) / 2, view_2.getWidth(), view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
            } else {
                view_2.layout(this.mHorizontalPaddingInLargerScreen, (getMeasuredHeight() - view_2.getHeight()) / 2, view_2.getWidth() + this.mHorizontalPaddingInLargerScreen, view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
            }
        }
        if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
            iCalculateWidth = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
            iGutter = this.mResponsiveUIModel.gutter();
        } else {
            iCalculateWidth = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 8, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
            iGutter = this.mResponsiveUIModel.gutter();
        }
        int index = iCalculateWidth + iGutter;
        if (view != null) {
            view.layout(index, 0, view.getWidth() + index, view.getHeight());
        }
    }

    private void layoutToolbarAndTabLayout(View view, View view_2) {
        int iCalculateWidth;
        int iGutter;
        int iCalculateWidth2;
        int iGutter2;
        int height;
        boolean layoutDirection = ViewCompat.getLayoutDirection(this) == 1;
        if (COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth())) {
            if (view != null) {
                height = view.getHeight();
                if (layoutDirection) {
                    view.layout(0, 0, view.getWidth(), height);
                } else {
                    view.layout(this.mResponsiveUIModel.margin(), 0, this.mResponsiveUIModel.margin() + view.getWidth(), height);
                }
            } else {
                height = 0;
            }
            if (view_2 != null) {
                view_2.layout(0, height, view_2.getWidth(), view_2.getHeight() + height);
                return;
            }
            return;
        }
        if (layoutDirection) {
            if (view_2 != null) {
                if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                    view_2.layout(getMeasuredWidth() - view_2.getWidth(), (getMeasuredHeight() - view_2.getHeight()) / 2, getMeasuredWidth(), view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
                } else {
                    view_2.layout((getMeasuredWidth() - view_2.getWidth()) - this.mHorizontalPaddingInLargerScreen, (getMeasuredHeight() - view_2.getHeight()) / 2, getMeasuredWidth() - this.mHorizontalPaddingInLargerScreen, view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
                }
            }
            if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                iCalculateWidth2 = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
                iGutter2 = this.mResponsiveUIModel.gutter();
            } else {
                iCalculateWidth2 = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 8, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
                iGutter2 = this.mResponsiveUIModel.gutter();
            }
            int measuredWidth = getMeasuredWidth() - (iCalculateWidth2 + iGutter2);
            if (view != null) {
                view.layout(measuredWidth - view.getWidth(), 0, measuredWidth, view.getHeight());
                return;
            }
            return;
        }
        if (view_2 != null) {
            if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                view_2.layout(0, (getMeasuredHeight() - view_2.getHeight()) / 2, view_2.getWidth(), view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
            } else {
                view_2.layout(this.mHorizontalPaddingInLargerScreen, (getMeasuredHeight() - view_2.getHeight()) / 2, view_2.getWidth() + this.mHorizontalPaddingInLargerScreen, view_2.getHeight() + ((getMeasuredHeight() - view_2.getHeight()) / 2));
            }
        }
        if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
            iCalculateWidth = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
            iGutter = this.mResponsiveUIModel.gutter();
        } else {
            iCalculateWidth = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 8, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
            iGutter = this.mResponsiveUIModel.gutter();
        }
        int index = iCalculateWidth + iGutter;
        if (view != null) {
            view.layout(index, 0, view.getWidth() + index, view.getHeight());
        }
    }

    private void measureSearchBar(int index, int index_2, View view) {
        int measuredWidth = COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth()) ? getMeasuredWidth() : ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
        int[] iArr = this.mSearchBarPaddingStart;
        int index_3 = this.mResponsiveWidthSize;
        view.setPaddingRelative(iArr[index_3], 0, this.mSearchBarPaddingEnd[index_3], 0);
        measureChild(view, ViewGroup.getChildMeasureSpec(index, 0, Math.min(getMeasuredWidth(), measuredWidth)), ViewGroup.getChildMeasureSpec(index_2, 0, getMeasuredHeight()));
    }

    private void measureTabLayout(int index, int index_2, View view) {
        int iCalculateWidth;
        if (COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth())) {
            iCalculateWidth = (int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 1, 0, getContext());
        } else {
            iCalculateWidth = (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext())) ? (int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext()) : COUIResponsiveUtils.isLargeScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext())) ? ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 8, 0, 0, getContext())) - this.mHorizontalPaddingInLargerScreen : 0) + this.mResponsiveUIModel.margin();
        }
        measureChild(view, ViewGroup.getChildMeasureSpec(index, 0, Math.min(getMeasuredWidth(), iCalculateWidth)), ViewGroup.getChildMeasureSpec(index_2, 0, getMeasuredHeight()));
    }

    private void measureToolbar(int index, int index_2, View view) {
        int iCalculateWidth;
        if (COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth())) {
            iCalculateWidth = ((int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext())) + this.mResponsiveUIModel.margin();
        } else {
            iCalculateWidth = (int) COUIResponsiveUtils.calculateWidth(getMeasuredWidth(), 4, 0, 0, getContext());
            if (COUIResponsiveUtils.isLargeScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
                iCalculateWidth -= this.mHorizontalPaddingInLargerScreen;
            }
        }
        measureChild(view, ViewGroup.getChildMeasureSpec(index, 0, Math.min(getMeasuredWidth(), iCalculateWidth + this.mResponsiveUIModel.margin())), ViewGroup.getChildMeasureSpec(index_2, 0, getMeasuredHeight()));
    }

    private void updateResponsiveUI() {
        int measuredWidth = getMeasuredWidth();
        this.mResponsiveUIModel.rebuild(measuredWidth, getMeasuredHeight()).chooseMargin(MarginType.MARGIN_SMALL);
        if (COUIResponsiveUtils.isSmallScreen(getContext(), measuredWidth)) {
            this.mResponsiveWidthSize = 0;
        } else if (COUIResponsiveUtils.isMediumScreen(getContext(), measuredWidth, UIUtil.getScreenHeightMetrics(getContext()))) {
            this.mResponsiveWidthSize = RESPONSIVE_WIDTH_TYPE_MEDIUM;
        } else if (COUIResponsiveUtils.isLargeScreen(getContext(), measuredWidth, UIUtil.getScreenHeightMetrics(getContext()))) {
            this.mResponsiveWidthSize = RESPONSIVE_WIDTH_TYPE_EXPANDED;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mResponsiveUIModel.onConfigurationChanged(configuration);
        this.mResponsiveUIModel.chooseMargin(MarginType.MARGIN_SMALL);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int childCount = getChildCount();
        View view = null;
        View view_2 = null;
        View view_3 = null;
        for (int index = 0; index < childCount && index < MAX_CHILD_COUNT; index++) {
            View childAt = getChildAt(index);
            if (childAt instanceof COUIToolbar) {
                view = childAt;
            } else if (childAt instanceof COUITabLayout) {
                view_3 = childAt;
            } else if (childAt instanceof COUISearchBar) {
                view_2 = childAt;
            }
        }
        if (view_2 == null) {
            layoutToolbarAndTabLayout(view, view_3);
        } else {
            layoutSearchBarAndTabLayout(view_2, view_3);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateResponsiveUI();
        int childCount = getChildCount();
        int measuredHeight = 0;
        boolean flag = false;
        int measuredHeight2 = 0;
        int measuredHeight3 = 0;
        for (int index = 0; index < childCount && index < MAX_CHILD_COUNT; index++) {
            View childAt = getChildAt(index);
            if (childAt instanceof COUIToolbar) {
                measureToolbar(widthMeasureSpec, heightMeasureSpec, childAt);
                measuredHeight2 = childAt.getMeasuredHeight();
            } else if (childAt instanceof COUITabLayout) {
                measureTabLayout(widthMeasureSpec, heightMeasureSpec, childAt);
                measuredHeight = childAt.getMeasuredHeight();
            } else if (childAt instanceof COUISearchBar) {
                ((COUISearchBar) childAt).setUseResponsivePadding(false);
                measureSearchBar(widthMeasureSpec, heightMeasureSpec, childAt);
                measuredHeight3 = childAt.getMeasuredHeight();
                flag = true;
            }
        }
        if (COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth())) {
            int iResolveSizeAndState = View.resolveSizeAndState(View.MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec, 0);
            if (flag) {
                measuredHeight2 = measuredHeight3;
            }
            setMeasuredDimension(iResolveSizeAndState, measuredHeight2 + measuredHeight);
            return;
        }
        int iResolveSizeAndState2 = View.resolveSizeAndState(View.MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec, 0);
        if (flag) {
            measuredHeight2 = measuredHeight3;
        }
        setMeasuredDimension(iResolveSizeAndState2, Math.max(measuredHeight, measuredHeight2));
    }

    public COUIPercentTabWithSearchView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIPercentTabWithSearchView(Context context, AttributeSet attributeSet, int index) {
        super(context, attributeSet, index);
        this.mResponsiveWidthSize = 0;
        this.mSearchBarPaddingStart = new int[]{getContext().getResources().getDimensionPixelOffset(R.dimen.coui_tab_search_bar_padding_start_compat), getContext().getResources().getDimensionPixelOffset(R.dimen.coui_tab_search_bar_padding_start_medium), getContext().getResources().getDimensionPixelOffset(R.dimen.coui_tab_search_bar_padding_start_expanded)};
        this.mSearchBarPaddingEnd = new int[]{getContext().getResources().getDimensionPixelOffset(R.dimen.coui_tab_search_bar_padding_end_compat), getContext().getResources().getDimensionPixelOffset(R.dimen.coui_tab_search_bar_padding_end_medium), getContext().getResources().getDimensionPixelOffset(R.dimen.coui_tab_search_bar_padding_end_expanded)};
        init(context);
    }
}






