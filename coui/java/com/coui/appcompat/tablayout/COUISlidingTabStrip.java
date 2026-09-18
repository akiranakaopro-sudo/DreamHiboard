package com.coui.appcompat.tablayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.R;


public class COUISlidingTabStrip extends LinearLayout {
    private final Paint mBottomDividerPaint;
    private COUITabLayout mCOUITabLayout;
    private int mContentMinWidth;
    private int mHorizontalLargeMargin;
    private int mHorizontalMediumMargin;
    private int mHorizontalSmallMargin;
    private int mIndicatorAnimTime;
    private ValueAnimator mIndicatorAnimator;
    private int mIndicatorBackgroundHeight;
    private int mIndicatorBackgroundPaddingLeft;
    private int mIndicatorBackgroundPaddingRight;
    private final Paint mIndicatorBackgroundPaint;
    private int mIndicatorLeft;
    private int mIndicatorRight;
    private float mIndicatorWidthRatio;
    float mLastOffset;
    private int mLastPosition;
    float mLastSelectionOffset;
    private int mLayoutDirection;
    protected int mSelectedIndicatorHeight;
    private final Paint mSelectedIndicatorPaint;
    int mSelectedPosition;
    float mSelectionOffset;
    private int mTabMediumSpacing;
    private int mTabSmallSpacing;

    public COUISlidingTabStrip(Context context, COUITabLayout cOUITabLayout) {
        super(context);
        this.mSelectedPosition = -1;
        this.mLayoutDirection = -1;
        this.mIndicatorLeft = -1;
        this.mIndicatorRight = -1;
        this.mLastPosition = 0;
        this.mIndicatorAnimTime = -1;
        this.mCOUITabLayout = cOUITabLayout;
        setWillNotDraw(false);
        this.mSelectedIndicatorPaint = new Paint();
        this.mBottomDividerPaint = new Paint();
        this.mIndicatorBackgroundPaint = new Paint();
        setGravity(17);
        this.mHorizontalLargeMargin = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_large_horizontal_margin);
        this.mHorizontalMediumMargin = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_medium_horizontal_margin);
        this.mHorizontalSmallMargin = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_small_horizontal_margin);
        this.mTabSmallSpacing = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_small_tab_spacing);
        this.mTabMediumSpacing = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_medium_tab_spacing);
        this.mContentMinWidth = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_content_min_width);
    }

    private boolean isLayoutRTL() {
        return ViewCompat.getLayoutDirection(this) == 1;
    }

    private void measureChildWithRedDot(COUITabView cOUITabView, int index, int index_2) {
        if (cOUITabView.getTextView() != null) {
            cOUITabView.getTextView().getLayoutParams().width = -2;
        }
        if (cOUITabView.getTextView() == null || cOUITabView.getHintRedDot() == null || cOUITabView.getHintRedDot().getVisibility() == 8) {
            cOUITabView.measure(index, index_2);
            return;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cOUITabView.getHintRedDot().getLayoutParams();
        layoutParams.gravity = 48;
        if (cOUITabView.getHintRedDot().getPointMode() == 0) {
            layoutParams.leftMargin = 0;
            layoutParams.rightMargin = 0;
            cOUITabView.measure(index, index_2);
            return;
        }
        if (isLayoutRTL()) {
            layoutParams.rightMargin = this.mCOUITabLayout.mDotHorizontalOffset;
        } else {
            layoutParams.leftMargin = this.mCOUITabLayout.mDotHorizontalOffset;
        }
        if (cOUITabView.getHintRedDot().getPointMode() == 2) {
            layoutParams.topMargin = this.mCOUITabLayout.mDotVerticalOffsetFromNumberRed;
        } else {
            layoutParams.topMargin = this.mCOUITabLayout.mDotVerticalOffsetFromOnlyRed;
        }
        cOUITabView.measure(View.MeasureSpec.makeMeasureSpec(0, 0), index_2);
        if (cOUITabView.getMeasuredWidth() > this.mCOUITabLayout.mRequestedTabMaxWidth) {
            cOUITabView.getTextView().getLayoutParams().width = ((this.mCOUITabLayout.mRequestedTabMaxWidth - cOUITabView.getHintRedDot().getMeasuredWidth()) - layoutParams.getMarginStart()) + layoutParams.getMarginEnd();
            cOUITabView.measure(index, index_2);
        }
    }

    private void measureShortChild(int index, int index_2, int index_3, int index_4) {
        int childCount = getChildCount();
        int index_6 = ((index - index_2) - (index_3 * childCount)) / 2;
        int index_7 = index_3 / 2;
        setLayoutPadding(index_6, index_6);
        for (int index_5 = 0; index_5 < childCount; index_5++) {
            View childAt = getChildAt(index_5);
            setMargin(childAt, index_7, index_7, childAt.getMeasuredWidth());
        }
    }

    private void measureSmallChild(int index, int index_2, int index_3) {
        int iMax;
        int index_7;
        int childCount = getChildCount();
        int index_5 = this.mContentMinWidth;
        if (index >= index_5) {
            iMax = Math.max((index_5 - index_2) / (childCount + 1), index_3);
            index_7 = ((index - this.mContentMinWidth) + iMax) / 2;
        } else {
            iMax = Math.max((index - index_2) / (childCount + 1), index_3);
            index_7 = iMax / 2;
        }
        int index_6 = iMax / 2;
        setLayoutPadding(index_7, index_7);
        for (int index_4 = 0; index_4 < childCount; index_4++) {
            View childAt = getChildAt(index_4);
            setMargin(childAt, index_6, index_6, childAt.getMeasuredWidth());
        }
    }

    private int parseMinDivider(int index) {
        if (index != -1) {
            return index;
        }
        int measuredWidth = ((COUITabLayout) getParent()).getMeasuredWidth();
        int measuredHeight = ((COUITabLayout) getParent()).getMeasuredHeight();
        return (COUIResponsiveUtils.isMediumScreen(getContext(), measuredWidth, measuredHeight) || COUIResponsiveUtils.isLargeScreen(getContext(), measuredWidth, measuredHeight)) ? this.mTabMediumSpacing : this.mTabSmallSpacing;
    }

    private int parseMinMargin(int index) {
        if (index != -1) {
            return index;
        }
        int measuredWidth = ((COUITabLayout) getParent()).getMeasuredWidth();
        return COUIResponsiveUtils.isLargeScreen(getContext(), measuredWidth, UIUtil.getScreenHeightMetrics(getContext())) ? this.mHorizontalLargeMargin : COUIResponsiveUtils.isMediumScreen(getContext(), measuredWidth, UIUtil.getScreenHeightMetrics(getContext())) ? this.mHorizontalMediumMargin : this.mHorizontalSmallMargin;
    }

    private void setLayoutPadding(int index, int index_2) {
        if (getParent() == null || !(getParent() instanceof COUITabLayout)) {
            return;
        }
        ((COUITabLayout) getParent()).setPaddingLeftAndRight(index, index_2);
    }

    private void setMargin(View view, int index, int index_2, int index_3) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = index_3 + index_2 + index;
        view.setPaddingRelative(index, view.getPaddingTop(), index_2, view.getPaddingBottom());
        view.measure(View.MeasureSpec.makeMeasureSpec(layoutParams.width, 1073741824), View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), 1073741824));
    }

    public void animateIndicatorToPosition(final int index, int index_2) {
        boolean flag;
        COUISlidingTabStrip cOUISlidingTabStrip;
        final int index_11;
        int index_12;
        ValueAnimator valueAnimator = this.mIndicatorAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            flag = false;
        } else if (index != this.mLastPosition) {
            this.mIndicatorAnimator.end();
            flag = false;
        } else {
            this.mIndicatorAnimator.cancel();
            flag = true;
        }
        boolean layoutDirection = ViewCompat.getLayoutDirection(this) == 1;
        View childAt = getChildAt(index);
        if (childAt == null) {
            updateIndicatorPosition();
            return;
        }
        final COUITabView cOUITabView = (COUITabView) childAt;
        final COUITabView cOUITabView2 = (COUITabView) getChildAt(this.mCOUITabLayout.getSelectedTabPosition());
        if (cOUITabView.getTextView() == null || cOUITabView.mCustomView != null) {
            cOUISlidingTabStrip = this;
            final int indicatorLeft = cOUISlidingTabStrip.getIndicatorLeft(cOUITabView.getLeft() + cOUITabView.mCustomView.getLeft());
            final int indicatorRight = cOUISlidingTabStrip.getIndicatorRight(cOUITabView.getLeft() + cOUITabView.mCustomView.getRight());
            if (Math.abs(index - cOUISlidingTabStrip.mSelectedPosition) <= 1) {
                index_11 = cOUISlidingTabStrip.mIndicatorLeft;
                index_12 = cOUISlidingTabStrip.mIndicatorRight;
            } else {
                int iDpToPx = cOUISlidingTabStrip.dpToPx(24);
                index_11 = (index >= cOUISlidingTabStrip.mSelectedPosition ? !layoutDirection : layoutDirection) ? indicatorLeft - iDpToPx : iDpToPx + indicatorRight;
                index_12 = index_11;
            }
            if (index_11 != indicatorLeft || index_12 != indicatorRight) {
                ValueAnimator valueAnimator_2 = new ValueAnimator();
                cOUISlidingTabStrip.mIndicatorAnimator = valueAnimator_2;
                valueAnimator_2.setInterpolator(COUIAnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                valueAnimator_2.setDuration(index_2);
                valueAnimator_2.setFloatValues(0.0f, 1.0f);
                final int index_3 = index_12;
                valueAnimator_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator indicatorAnimator) {
                        float animatedFraction = indicatorAnimator.getAnimatedFraction();
                        COUISlidingTabStrip.this.setIndicatorPosition(COUIAnimationUtils.lerp(index_11, indicatorLeft, animatedFraction), COUIAnimationUtils.lerp(index_3, indicatorRight, animatedFraction));
                    }
                });
                valueAnimator_2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        COUISlidingTabStrip cOUISlidingTabStrip2 = COUISlidingTabStrip.this;
                        cOUISlidingTabStrip2.mSelectedPosition = index;
                        cOUISlidingTabStrip2.mSelectionOffset = 0.0f;
                        if (cOUITabView.getTextView() != null) {
                            cOUITabView.getTextView().setTextColor(COUISlidingTabStrip.this.mCOUITabLayout.mSelectedTextColor);
                        }
                        if (cOUITabView2.getTextView() != null) {
                            cOUITabView2.getTextView().setTextColor(COUISlidingTabStrip.this.mCOUITabLayout.mNormalTextColor);
                        }
                    }
                });
                valueAnimator_2.start();
            }
        } else {
            final TextView textView = cOUITabView.getTextView();
            final int index_4 = this.mIndicatorLeft;
            final int index_5 = this.mIndicatorRight;
            int indicatorPadding = this.mCOUITabLayout.getIndicatorPadding();
            final int indicatorLeft2 = getIndicatorLeft((cOUITabView.getLeft() + textView.getLeft()) - indicatorPadding);
            final int indicatorRight2 = getIndicatorRight(cOUITabView.getLeft() + textView.getRight() + indicatorPadding);
            final int index_6 = (indicatorRight2 - indicatorLeft2) - (index_5 - index_4);
            final int index_7 = indicatorLeft2 - index_4;
            int indicatorAnimTime = this.mCOUITabLayout.getIndicatorAnimTime(index, this.mSelectedPosition);
            int index_8 = this.mIndicatorAnimTime;
            if (index_8 != -1) {
                indicatorAnimTime = index_8;
            }
            ValueAnimator indicatorAnimator = new ValueAnimator();
            this.mIndicatorAnimator = indicatorAnimator;
            indicatorAnimator.setDuration(indicatorAnimTime);
            indicatorAnimator.setInterpolator(new COUIEaseInterpolator());
            indicatorAnimator.setIntValues(0, 1);
            final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            final int currentTextColor = flag ? textView.getCurrentTextColor() : this.mCOUITabLayout.mNormalTextColor;
            final int currentTextColor2 = flag ? cOUITabView2.getTextView().getCurrentTextColor() : this.mCOUITabLayout.mSelectedTextColor;
            indicatorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator_3) {
                    int index_13;
                    int index_14;
                    float animatedFraction = valueAnimator_3.getAnimatedFraction();
                    textView.setTextColor(((Integer) argbEvaluator.evaluate(animatedFraction, Integer.valueOf(currentTextColor), Integer.valueOf(COUISlidingTabStrip.this.mCOUITabLayout.mSelectedTextColor))).intValue());
                    COUITabView cOUITabView3 = cOUITabView2;
                    if (cOUITabView3 != null && cOUITabView3.getTextView() != null) {
                        cOUITabView2.getTextView().setTextColor(((Integer) argbEvaluator.evaluate(animatedFraction, Integer.valueOf(currentTextColor2), Integer.valueOf(COUISlidingTabStrip.this.mCOUITabLayout.mNormalTextColor))).intValue());
                    }
                    COUISlidingTabStrip cOUISlidingTabStrip2 = COUISlidingTabStrip.this;
                    if (cOUISlidingTabStrip2.mLastOffset == 0.0f) {
                        cOUISlidingTabStrip2.mLastOffset = animatedFraction;
                    }
                    if (animatedFraction - cOUISlidingTabStrip2.mLastOffset > 0.0f) {
                        int index_9 = index_5;
                        index_13 = (int) ((index_9 - index_4) + (index_6 * animatedFraction));
                        index_14 = (int) (index_4 + (index_7 * animatedFraction));
                    } else {
                        int index_10 = indicatorRight2;
                        float value = 1.0f - animatedFraction;
                        index_13 = (int) ((index_10 - indicatorLeft2) - (index_6 * value));
                        index_14 = (int) (indicatorLeft2 - (index_7 * value));
                    }
                    cOUISlidingTabStrip2.setIndicatorPosition(index_14, index_13 + index_14);
                }
            });
            cOUISlidingTabStrip = this;
            indicatorAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    COUISlidingTabStrip cOUISlidingTabStrip2 = COUISlidingTabStrip.this;
                    cOUISlidingTabStrip2.mSelectedPosition = index;
                    cOUISlidingTabStrip2.mSelectionOffset = 0.0f;
                    cOUISlidingTabStrip2.updateIndicatorPosition();
                    COUISlidingTabStrip.this.mCOUITabLayout.resetTextColorAfterAnim();
                }
            });
            indicatorAnimator.start();
        }
        cOUISlidingTabStrip.mLastPosition = cOUISlidingTabStrip.mCOUITabLayout.getSelectedTabPosition();
    }

    public boolean childrenNeedLayout() {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            if (getChildAt(index).getWidth() <= 0) {
                return true;
            }
        }
        return false;
    }

    public int dpToPx(int index) {
        return Math.round(getResources().getDisplayMetrics().density * index);
    }

    public Paint getBottomDividerPaint() {
        return this.mBottomDividerPaint;
    }

    public int getIndicatorAnimTime() {
        return this.mIndicatorAnimTime;
    }

    public int getIndicatorBackgroundHeight() {
        return this.mIndicatorBackgroundHeight;
    }

    public int getIndicatorBackgroundPaddingLeft() {
        return this.mIndicatorBackgroundPaddingLeft;
    }

    public int getIndicatorBackgroundPaddingRight() {
        return this.mIndicatorBackgroundPaddingRight;
    }

    public Paint getIndicatorBackgroundPaint() {
        return this.mIndicatorBackgroundPaint;
    }

    public int getIndicatorLeft() {
        return this.mIndicatorLeft;
    }

    public float getIndicatorPosition() {
        return this.mSelectedPosition + this.mSelectionOffset;
    }

    public int getIndicatorRight() {
        return this.mIndicatorRight;
    }

    public float getIndicatorWidthRatio() {
        return this.mIndicatorWidthRatio;
    }

    public Paint getSelectedIndicatorPaint() {
        return this.mSelectedIndicatorPaint;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mCOUITabLayout.isUpdateindicatorposition()) {
            updateIndicatorPosition();
        }
        if (this.mCOUITabLayout.mTabAlreadyMeasure) {
            return;
        }
        ValueAnimator valueAnimator = this.mIndicatorAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mIndicatorAnimator.cancel();
            animateIndicatorToPosition(this.mSelectedPosition, Math.round((1.0f - this.mIndicatorAnimator.getAnimatedFraction()) * this.mIndicatorAnimator.getDuration()));
        }
        COUITabLayout cOUITabLayout = this.mCOUITabLayout;
        cOUITabLayout.mTabAlreadyMeasure = true;
        cOUITabLayout.setScrollPosition(this.mSelectedPosition, 0.0f, true, true);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (View.MeasureSpec.getMode(widthMeasureSpec) == 0) {
            return;
        }
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        if (childCount == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int minMargin = parseMinMargin(this.mCOUITabLayout.getTabMinMargin());
        int minDivider = parseMinDivider(this.mCOUITabLayout.getTabMinDivider());
        if (this.mCOUITabLayout.getTabMode() == 1) {
            this.mIndicatorWidthRatio = this.mCOUITabLayout.getDefaultIndicatoRatio();
            int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mCOUITabLayout.mRequestedTabMaxWidth, Integer.MIN_VALUE);
            int measuredWidth = 0;
            for (int index = 0; index < childCount; index++) {
                COUITabView cOUITabView = (COUITabView) getChildAt(index);
                setMargin(cOUITabView, 0, 0);
                measureChildWithRedDot(cOUITabView, iMakeMeasureSpec, heightMeasureSpec);
                measuredWidth += cOUITabView.getMeasuredWidth();
            }
            int index_5 = (minMargin * 2) + measuredWidth + ((childCount - 1) * minDivider);
            if (index_5 <= this.mContentMinWidth) {
                measureSmallChild(size, measuredWidth, minDivider);
            } else if (index_5 <= size) {
                measureShortChild(size, measuredWidth, minDivider, minMargin);
            } else {
                int index_6 = minDivider / 2;
                int index_7 = minMargin - index_6;
                setLayoutPadding(index_7, index_7);
                for (int index_2 = 0; index_2 < childCount; index_2++) {
                    View childAt = getChildAt(index_2);
                    setMargin(childAt, index_6, index_6, childAt.getMeasuredWidth());
                }
            }
        } else {
            int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mCOUITabLayout.mRequestedTabMaxWidth, Integer.MIN_VALUE);
            int index_8 = minDivider / 2;
            int index_9 = minMargin - index_8;
            setLayoutPadding(index_9, index_9);
            for (int index_3 = 0; index_3 < childCount; index_3++) {
                View childAt2 = getChildAt(index_3);
                setMargin(childAt2, 0, 0);
                measureChildWithRedDot((COUITabView) childAt2, iMakeMeasureSpec2, heightMeasureSpec);
                setMargin(childAt2, index_8, index_8, childAt2.getMeasuredWidth());
            }
        }
        int measuredWidth2 = 0;
        for (int index_4 = 0; index_4 < childCount; index_4++) {
            measuredWidth2 += getChildAt(index_4).getMeasuredWidth();
        }
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(measuredWidth2, 1073741824), heightMeasureSpec);
    }

    @Override
    public void onRtlPropertiesChanged(int index) {
        super.onRtlPropertiesChanged(index);
    }

    public void setBottomDividerColor(int bottomDividerColor) {
        this.mBottomDividerPaint.setColor(bottomDividerColor);
        ViewCompat.postInvalidateOnAnimation(this.mCOUITabLayout);
    }

    public void setIndicatorAnimTime(int indicatorAnimTime) {
        this.mIndicatorAnimTime = indicatorAnimTime;
    }

    public void setIndicatorBackgroundHeight(int indicatorBackgroundHeight) {
        this.mIndicatorBackgroundHeight = indicatorBackgroundHeight;
    }

    public void setIndicatorBackgroundPaddingLeft(int indicatorBackgroundPaddingLeft) {
        this.mIndicatorBackgroundPaddingLeft = indicatorBackgroundPaddingLeft;
    }

    public void setIndicatorBackgroundPaddingRight(int indicatorBackgroundPaddingRight) {
        this.mIndicatorBackgroundPaddingRight = indicatorBackgroundPaddingRight;
    }

    public void setIndicatorLeft(int indicatorLeft) {
        this.mIndicatorLeft = indicatorLeft;
    }

    public void setIndicatorPosition(int x, int y) {
        int index = (x + y) / 2;
        int iMax = Math.max(y - x, dpToPx(32)) / 2;
        int indicatorLeft = index - iMax;
        int indicatorRight = index + iMax;
        if (indicatorLeft == this.mIndicatorLeft && indicatorRight == this.mIndicatorRight) {
            return;
        }
        this.mIndicatorLeft = indicatorLeft;
        this.mIndicatorRight = indicatorRight;
        ViewCompat.postInvalidateOnAnimation(this.mCOUITabLayout);
    }

    public void setIndicatorPositionFromTabPosition(int x, float y) {
        ValueAnimator valueAnimator = this.mIndicatorAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mIndicatorAnimator.cancel();
        }
        this.mSelectedPosition = x;
        this.mSelectionOffset = y;
        updateIndicatorPosition();
    }

    public void setIndicatorRight(int indicatorRight) {
        this.mIndicatorRight = indicatorRight;
    }

    public void setIndicatorWidthRatio(float indicatorWidthRatio) {
        this.mIndicatorWidthRatio = indicatorWidthRatio;
    }

    public void setSelectedIndicatorColor(int selectedIndicatorColor) {
        this.mSelectedIndicatorPaint.setColor(selectedIndicatorColor);
        ViewCompat.postInvalidateOnAnimation(this.mCOUITabLayout);
    }

    public void setSelectedIndicatorHeight(int selectedIndicatorHeight) {
        if (this.mSelectedIndicatorHeight != selectedIndicatorHeight) {
            this.mSelectedIndicatorHeight = selectedIndicatorHeight;
            ViewCompat.postInvalidateOnAnimation(this.mCOUITabLayout);
        }
    }

    public void updateIndicatorPosition() {
        View selectedChild = getChildAt(this.mSelectedPosition);
        COUITabView selectedTabView = (COUITabView) getChildAt(this.mSelectedPosition);
        boolean hasTextViewContent = selectedTabView != null
                && selectedTabView.getTextView() != null
                && selectedTabView.mCustomView == null;
        boolean hasCustomViewContent = selectedTabView != null && selectedTabView.mCustomView != null;
        int left = -1;
        int right = -1;
        if (hasTextViewContent || hasCustomViewContent) {
            View contentView = hasTextViewContent ? selectedTabView.getTextView() : selectedTabView.mCustomView;
            if (contentView.getWidth() > 0) {
                left = (selectedTabView.getLeft() + contentView.getLeft()) - this.mCOUITabLayout.getIndicatorPadding();
                right = selectedTabView.getLeft() + contentView.getRight() + this.mCOUITabLayout.getIndicatorPadding();
                if (this.mSelectionOffset > 0.0f && this.mSelectedPosition < getChildCount() - 1) {
                    COUITabView nextTabView = (COUITabView) getChildAt(this.mSelectedPosition + 1);
                    View nextContentView = nextTabView.mCustomView != null ? nextTabView.mCustomView : nextTabView.getTextView();
                    int nextLeft;
                    int nextRight;
                    if (nextContentView != null) {
                        nextLeft = (nextTabView.getLeft() + nextContentView.getLeft()) - this.mCOUITabLayout.getIndicatorPadding();
                        nextRight = nextTabView.getLeft() + nextContentView.getRight() + this.mCOUITabLayout.getIndicatorPadding();
                    } else {
                        nextLeft = nextTabView.getLeft();
                        nextRight = nextTabView.getRight();
                    }
                    int nextWidth = nextRight - nextLeft;
                    int width = right - left;
                    int widthDiff = nextWidth - width;
                    int leftDiff = nextLeft - left;
                    if (this.mLastSelectionOffset == 0.0f) {
                        this.mLastSelectionOffset = this.mSelectionOffset;
                    }
                    float offset = this.mSelectionOffset;
                    if (offset - this.mLastSelectionOffset > 0.0f) {
                        width = (int) (width + (widthDiff * offset));
                        left = (int) (left + (leftDiff * offset));
                    } else {
                        width = (int) (nextWidth - (widthDiff * (1.0f - offset)));
                        left = (int) (nextLeft - (leftDiff * (1.0f - offset)));
                    }
                    right = left + width;
                    this.mLastSelectionOffset = offset;
                }
                left = getIndicatorLeft(left);
                right = getIndicatorRight(right);
            }
        } else if (selectedChild != null && selectedChild.getWidth() > 0) {
            left = selectedChild.getLeft();
            right = selectedChild.getRight();
            if (this.mSelectionOffset > 0.0f && this.mSelectedPosition < getChildCount() - 1) {
                View nextChild = getChildAt(this.mSelectedPosition + 1);
                float offset = this.mSelectionOffset;
                left = (int) ((offset * nextChild.getLeft()) + ((1.0f - offset) * left));
                right = (int) ((offset * nextChild.getRight()) + ((1.0f - offset) * right));
            }
        }
        setIndicatorPosition(left, right);
    }

    private int getIndicatorLeft(int index) {
        int width = ((this.mCOUITabLayout.getWidth() - this.mCOUITabLayout.getPaddingLeft()) - this.mCOUITabLayout.getPaddingRight()) - getWidth();
        return (!isLayoutRTL() || width <= 0) ? index : index + width;
    }

    private int getIndicatorRight(int index) {
        int width = ((this.mCOUITabLayout.getWidth() - this.mCOUITabLayout.getPaddingLeft()) - this.mCOUITabLayout.getPaddingRight()) - getWidth();
        return (!isLayoutRTL() || width <= 0) ? index : index + width;
    }

    private void setMargin(View view, int index, int index_2) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        ViewCompat.setPaddingRelative(view, 0, view.getPaddingTop(), 0, view.getPaddingBottom());
        layoutParams.setMarginStart(index);
        layoutParams.setMarginEnd(index_2);
    }
}






