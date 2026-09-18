package com.coui.appcompat.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.COUISpringInterpolator;
import com.coui.appcompat.indicator.COUIPageIndicator;
import com.coui.appcompat.viewpager.COUIViewPager2;
import com.coui.appcompat.viewpager.COUIViewPager2.AnimationConfig.FlingScrollConfig;
import com.coui.appcompat.viewpager.COUIViewPager2.AnimationConfig.ProgramScrollConfig;

import java.util.List;

public class COUIGuideLayoutContentView extends LinearLayout {
    private static final int FLING_MAX_VELOCITY = 6000;
    private static final int FLING_SCROLL_MAX_DURATION_MS = 1000;
    private static final int FLING_SCROLL_MIN_DURATION_MS = 500;
    private static final float FLING_VELOCITY_DECAY_FACTOR = 2.0f;
    private static final float FLING_VELOCITY_THRESHOLD = 2500.0f;
    private static final int PROGRAM_SCROLL_DURATION_MS = 650;
    private static final double SPRING_DAMPING_RATIO = 0.6000000238418579d;

    private COUIGuideDialogButtonLayout mButtonLayout;
    private boolean mButtonTextSetByUser;
    private ImageHeightStyle mImageHeightStyle;
    private int mImagePaddingBottomDp;
    private int mImagePaddingLeftDp;
    private int mImagePaddingRightDp;
    private int mImagePaddingTopDp;
    private final OnButtonClickListener mNextPageClickListener;
    private COUIPageIndicator mPageIndicator;
    private COUIViewPager2 mViewPager;

    public enum ImageHeightStyle {
        DEFAULT(328),
        HEIGHT_240DP(240);

        private final int mHeightDp;

        ImageHeightStyle(int heightDp) {
            mHeightDp = heightDp;
        }

        public int getHeightDp() {
            return mHeightDp;
        }
    }

    public interface OnButtonClickListener {
        default void onNextClick() {
        }

        default void onSkipClick() {
        }

        default void onStartClick() {
        }
    }

    public COUIGuideLayoutContentView(Context context) {
        this(context, null);
    }

    public COUIGuideLayoutContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIGuideLayoutContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNextPageClickListener = new OnButtonClickListener() {
            @Override
            public void onNextClick() {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            }
        };
        mImageHeightStyle = ImageHeightStyle.DEFAULT;
        initLayout();
        initViewPager();
    }

    private int dpToPx(int dp) {
        return Math.round(getContext().getResources().getDisplayMetrics().density * dp);
    }

    private void initAfterAdapterSet() {
        RecyclerView.Adapter<?> adapter = mViewPager.getAdapter();
        int itemCount = adapter != null ? adapter.getItemCount() : 0;
        mPageIndicator.setDotsCount(itemCount);
        mPageIndicator.setOnDotClickListener(new COUIPageIndicator.OnIndicatorDotClickListener() {
            @Override
            public void onClick(int position) {
                mViewPager.setCurrentItem(position);
            }
        });
        mButtonLayout.setPagerCount(itemCount);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mViewPager
                .getLayoutParams();
        if (layoutParams != null) {
            int bottomMargin;
            if (itemCount <= 1) {
                mViewPager.setUserInputEnabled(false);
                mPageIndicator.setVisibility(GONE);
                bottomMargin = getResources().getDimensionPixelSize(
                        R.dimen.coui_dialog_guide_viewpager2_margin_single_bottom);
            } else {
                mViewPager.setUserInputEnabled(true);
                mPageIndicator.setVisibility(VISIBLE);
                bottomMargin = getResources().getDimensionPixelSize(
                        R.dimen.coui_dialog_guide_viewpager2_margin_bottom);
            }
            layoutParams.setMargins(0, 0, 0, bottomMargin);
            mViewPager.setLayoutParams(layoutParams);
        }
        if (!mButtonTextSetByUser) {
            setDefaultButtonText(itemCount);
        }
    }

    private void initLayout() {
        setOrientation(VERTICAL);
        int horizontalPadding = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_guide_button_layout_padding_horizontal);
        int viewPagerMarginBottom = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_guide_viewpager2_margin_bottom);
        int indicatorMarginBottom = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_guide_indicator_margin_bottom);
        int buttonMarginBottom = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_guide_button_layout_margin_bottom);
        mViewPager = new COUIViewPager2(getContext());
        mViewPager.setClickable(false);
        mViewPager.setAnimationConfig(createGuideDialogConfig());
        LinearLayout.LayoutParams viewPagerParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 0, 1.0f);
        viewPagerParams.setMargins(0, 0, 0, viewPagerMarginBottom);
        addView(mViewPager, viewPagerParams);
        mPageIndicator = new COUIPageIndicator(getContext());
        mPageIndicator.setIsClickable(false);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorParams.setMargins(0, 0, 0, indicatorMarginBottom);
        indicatorParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        addView(mPageIndicator, indicatorParams);
        mButtonLayout = new COUIGuideDialogButtonLayout(getContext());
        mButtonLayout.setClipToPadding(false);
        mButtonLayout.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        mButtonLayout.setNextButtonClickListener(mNextPageClickListener);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 0, 0, buttonMarginBottom);
        addView(mButtonLayout, buttonParams);
    }

    private void initViewPager() {
        mViewPager.setClickable(false);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                mPageIndicator.onPageScrollStateChanged(state);
                mButtonLayout.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                mPageIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
                mButtonLayout.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mPageIndicator.onPageSelected(position);
                mButtonLayout.onPageSelected(position);
            }
        });
    }

    private void setDefaultButtonText(int itemCount) {
        String skip = getResources().getString(R.string.coui_guide_dialog_skip_text);
        String next = getResources().getString(R.string.coui_guide_dialog_next_text);
        String known = getResources().getString(R.string.coui_guide_dialog_known_text);
        if (itemCount <= 1) {
            mButtonLayout.getNextButton().setStartText(known);
            mButtonLayout.getNextButton().setText(known);
            mButtonLayout.setButtonLayoutType(COUIGuideDialogButtonLayout.SINGLE_BUTTON);
        } else {
            mButtonLayout.getSkipButton().setText(skip);
            mButtonLayout.getNextButton().setNextText(next);
            mButtonLayout.getNextButton().setStartText(known);
            mButtonLayout.getNextButton().setText(next);
            mButtonLayout.setButtonLayoutType(COUIGuideDialogButtonLayout.MULTIPLE_BUTTONS);
        }
    }

    public void addButtonClickListener(OnButtonClickListener listener) {
        mButtonLayout.addButtonClickListener(listener);
    }

    public COUIViewPager2.AnimationConfig createGuideDialogConfig() {
        new COUIInEaseInterpolator();
        COUISpringInterpolator interpolator = new COUISpringInterpolator(SPRING_DAMPING_RATIO,
                0.0d);
        COUIViewPager2.AnimationConfig animationConfig = new COUIViewPager2.AnimationConfig();
        ProgramScrollConfig programScrollConfig = animationConfig.new ProgramScrollConfig(
                PROGRAM_SCROLL_DURATION_MS, interpolator);
        FlingScrollConfig flingScrollConfig = animationConfig.new FlingScrollConfig(
                FLING_SCROLL_MIN_DURATION_MS, FLING_SCROLL_MAX_DURATION_MS, interpolator,
                FLING_VELOCITY_THRESHOLD, FLING_MAX_VELOCITY, FLING_VELOCITY_DECAY_FACTOR);
        return animationConfig.setProgramScrollConfig(programScrollConfig)
                .setFlingScrollConfig(flingScrollConfig);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeButtonClickListener(mNextPageClickListener);
    }

    public void removeButtonClickListener(OnButtonClickListener listener) {
        mButtonLayout.removeButtonClickListener(listener);
    }

    public void setButtonText(CharSequence skip, CharSequence next, CharSequence start) {
        mButtonTextSetByUser = true;
        mButtonLayout.getSkipButton().setText(skip);
        mButtonLayout.getNextButton().setNextText(next);
        mButtonLayout.getNextButton().setStartText(start);
        mButtonLayout.getNextButton().setText(next);
        mButtonLayout.setButtonLayoutType(COUIGuideDialogButtonLayout.MULTIPLE_BUTTONS);
    }

    public void setGuidePages(List<COUIGuidePageItem> list) {
        COUIGuidePageAdapter adapter = new COUIGuidePageAdapter(getContext(), list);
        adapter.setImagePadding(mImagePaddingLeftDp, mImagePaddingTopDp, mImagePaddingRightDp,
                mImagePaddingBottomDp);
        adapter.setImageHeightDp(mImageHeightStyle.getHeightDp());
        setViewPagerAdapter(adapter);
    }

    public void setImageHeightStyle(ImageHeightStyle imageHeightStyle) {
        mImageHeightStyle = imageHeightStyle;
        RecyclerView.Adapter<?> adapter = mViewPager != null ? mViewPager.getAdapter() : null;
        if (adapter instanceof COUIGuidePageAdapter) {
            ((COUIGuidePageAdapter) adapter).setImageHeightDp(imageHeightStyle.getHeightDp());
        }
    }

    public void setImagePadding(int left, int top, int right, int bottom) {
        mImagePaddingLeftDp = dpToPx(left);
        mImagePaddingTopDp = dpToPx(top);
        mImagePaddingRightDp = dpToPx(right);
        mImagePaddingBottomDp = dpToPx(bottom);
        RecyclerView.Adapter<?> adapter = mViewPager != null ? mViewPager.getAdapter() : null;
        if (adapter instanceof COUIGuidePageAdapter) {
            ((COUIGuidePageAdapter) adapter).setImagePadding(left, top, right, bottom);
        }
    }

    public void setViewPagerAdapter(RecyclerView.Adapter<?> adapter) {
        mViewPager.setAdapter(adapter);
        if (adapter instanceof COUIGuidePageAdapter) {
            ((COUIGuidePageAdapter) adapter).setImageHeightDp(mImageHeightStyle.getHeightDp());
        }
        initAfterAdapterSet();
    }

    public void setButtonText(CharSequence text) {
        mButtonTextSetByUser = true;
        mButtonLayout.getNextButton().setStartText(text);
        mButtonLayout.getNextButton().setText(text);
        mButtonLayout.setButtonLayoutType(COUIGuideDialogButtonLayout.SINGLE_BUTTON);
    }
}
