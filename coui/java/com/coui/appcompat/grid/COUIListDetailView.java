package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentContainerView;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.component.responsiveui.unit.Dp;
import com.coui.component.responsiveui.window.WindowSizeClass;
import com.coui.component.responsiveui.window.WindowTotalSizeClass;


public class COUIListDetailView extends FrameLayout {
    private static final float DEFAULT_MAIN_WIDTH_PERCENT = 0.4f;
    private View mDivider;
    private int mDividerColor;
    private FragmentContainerView mEmptyPageFragment;
    private int mGapWidth;
    private FragmentContainerView mMainFragment;
    private float mMainFragmentPercent;
    private int mMaxMainFragmentWidth;
    private int mMinMainFragmentWidth;
    private FragmentContainerView mSubFragment;

    public COUIListDetailView(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        this.mMainFragment = new FragmentContainerView(context);
        this.mSubFragment = new FragmentContainerView(context);
        this.mEmptyPageFragment = new FragmentContainerView(context);
        this.mDivider = new View(context);
        addView(this.mEmptyPageFragment);
        addView(this.mMainFragment);
        addView(this.mDivider);
        addView(this.mSubFragment);
        this.mEmptyPageFragment.setId(View.generateViewId());
        this.mMainFragment.setId(View.generateViewId());
        this.mSubFragment.setId(View.generateViewId());
        int attrColor = COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDivider);
        this.mDividerColor = attrColor;
        setDividerColor(attrColor);
        COUIDarkModeUtil.setForceDarkAllow(this.mDivider, false);
        this.mMaxMainFragmentWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_main_fragment_max_width);
        this.mMinMainFragmentWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_main_fragment_min_width);
        this.mGapWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_fragment_gap_width);
    }

    public FrameLayout getEmptyPageFragmentContainer() {
        return this.mEmptyPageFragment;
    }

    public FrameLayout getMainFragmentContainer() {
        return this.mMainFragment;
    }

    public FrameLayout getSubFragmentContainer() {
        return this.mSubFragment;
    }

    public boolean isInSplitMode() {
        WindowSizeClass.Companion companion = WindowSizeClass.Companion;
        Dp.Companion companion2 = Dp.Companion;
        return companion.calculateFromSize(companion2.pixel2Dp(getContext(), Math.abs(getWidth())), companion2.pixel2Dp(getContext(), Math.abs(getWidth()))).getWindowTotalSizeClass() != WindowTotalSizeClass.Compact;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int attrColor = COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDivider);
        this.mDividerColor = attrColor;
        setDividerColor(attrColor);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        WindowSizeClass.Companion companion = WindowSizeClass.Companion;
        Dp.Companion companion2 = Dp.Companion;
        WindowTotalSizeClass windowTotalSizeClass = companion.calculateFromSize(companion2.pixel2Dp(getContext(), Math.abs(getWidth())), companion2.pixel2Dp(getContext(), Math.abs(getWidth()))).getWindowTotalSizeClass();
        if (isRtl) {
            if (windowTotalSizeClass == WindowTotalSizeClass.Compact) {
                this.mEmptyPageFragment.setVisibility(8);
                FragmentContainerView fragmentContainerView = this.mMainFragment;
                fragmentContainerView.layout(0, 0, fragmentContainerView.getWidth(), this.mMainFragment.getHeight());
                FragmentContainerView fragmentContainerView2 = this.mSubFragment;
                fragmentContainerView2.layout(0, 0, fragmentContainerView2.getWidth(), this.mSubFragment.getHeight());
                return;
            }
            this.mEmptyPageFragment.setVisibility(0);
            this.mEmptyPageFragment.layout(0, 0, this.mSubFragment.getWidth(), this.mSubFragment.getHeight());
            FragmentContainerView fragmentContainerView3 = this.mSubFragment;
            fragmentContainerView3.layout(0, 0, fragmentContainerView3.getWidth(), this.mSubFragment.getHeight());
            this.mDivider.layout(this.mSubFragment.getWidth(), 0, this.mSubFragment.getWidth() + this.mDivider.getWidth(), this.mDivider.getHeight());
            this.mMainFragment.layout(this.mSubFragment.getWidth() + this.mDivider.getWidth(), 0, this.mSubFragment.getWidth() + this.mDivider.getWidth() + this.mMainFragment.getWidth(), this.mMainFragment.getHeight());
            return;
        }
        if (windowTotalSizeClass == WindowTotalSizeClass.Compact) {
            this.mEmptyPageFragment.setVisibility(8);
            FragmentContainerView fragmentContainerView4 = this.mMainFragment;
            fragmentContainerView4.layout(0, 0, fragmentContainerView4.getWidth(), this.mMainFragment.getHeight());
            FragmentContainerView fragmentContainerView5 = this.mSubFragment;
            fragmentContainerView5.layout(0, 0, fragmentContainerView5.getWidth(), this.mSubFragment.getHeight());
            return;
        }
        this.mEmptyPageFragment.setVisibility(0);
        this.mEmptyPageFragment.layout(this.mMainFragment.getWidth() + this.mDivider.getWidth(), 0, this.mMainFragment.getWidth() + this.mDivider.getWidth() + this.mSubFragment.getWidth(), this.mSubFragment.getHeight());
        FragmentContainerView fragmentContainerView6 = this.mMainFragment;
        fragmentContainerView6.layout(0, 0, fragmentContainerView6.getWidth(), this.mMainFragment.getHeight());
        this.mDivider.layout(this.mMainFragment.getWidth(), 0, this.mMainFragment.getWidth() + this.mDivider.getWidth(), this.mDivider.getHeight());
        this.mSubFragment.layout(this.mMainFragment.getWidth() + this.mDivider.getWidth(), 0, this.mMainFragment.getWidth() + this.mDivider.getWidth() + this.mSubFragment.getWidth(), this.mSubFragment.getHeight());
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mainFragmentWidth;
        int subFragmentWidth;
        int dividerWidth;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        WindowSizeClass.Companion companion = WindowSizeClass.Companion;
        Dp.Companion companion2 = Dp.Companion;
        WindowTotalSizeClass windowTotalSizeClass = companion.calculateFromSize(companion2.pixel2Dp(getContext(), Math.abs(measuredWidth)), companion2.pixel2Dp(getContext(), Math.abs(measuredHeight))).getWindowTotalSizeClass();
        int preferredMainFragmentWidth = (int) Math.max(Math.min(measuredWidth * this.mMainFragmentPercent, this.mMaxMainFragmentWidth), this.mMinMainFragmentWidth);
        if (windowTotalSizeClass == WindowTotalSizeClass.Compact) {
            mainFragmentWidth = measuredWidth;
            subFragmentWidth = mainFragmentWidth;
            dividerWidth = 0;
        } else {
            mainFragmentWidth = Math.min(Math.max(preferredMainFragmentWidth, this.mMinMainFragmentWidth), this.mMaxMainFragmentWidth);
            subFragmentWidth = measuredWidth - mainFragmentWidth;
            dividerWidth = this.mGapWidth;
        }
        measureChild(this.mMainFragment, ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, Math.min(measuredWidth, mainFragmentWidth)), heightMeasureSpec);
        int subFragmentMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, subFragmentWidth);
        measureChild(this.mSubFragment, subFragmentMeasureSpec, heightMeasureSpec);
        measureChild(this.mEmptyPageFragment, subFragmentMeasureSpec, heightMeasureSpec);
        measureChild(this.mDivider, ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, dividerWidth), heightMeasureSpec);
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        this.mDivider.setBackgroundColor(dividerColor);
    }

    public void setMainFragmentPercent(float mainFragmentPercent) {
        this.mMainFragmentPercent = mainFragmentPercent;
        requestLayout();
    }

    public COUIListDetailView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIListDetailView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mMainFragmentPercent = DEFAULT_MAIN_WIDTH_PERCENT;
        init(context);
    }
}
