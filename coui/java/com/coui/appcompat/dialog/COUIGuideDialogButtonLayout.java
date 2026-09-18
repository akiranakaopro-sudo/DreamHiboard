package com.coui.appcompat.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.button.COUIButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class COUIGuideDialogButtonLayout extends ViewGroup {
    static final int MULTIPLE_BUTTONS = 0;
    static final int SINGLE_BUTTON = 1;
    private static final Interpolator PAGE_SCROLL_INTERPOLATOR = new COUIEaseInterpolator();
    private static final float SECONDARY_BUTTON_ALPHA_END_PROGRESS = 0.6f;
    private static final float SPRING_MINIMUM_VISIBLE_CHANGE = 0.001f;
    private static final float SPRING_RESPONSE = 0.15f;
    private static final String TEXT_EMPTY = "";
    private static final float TEXT_FADE_END_PROGRESS = 0.85f;
    private static final float TEXT_FADE_RANGE = 0.35f;
    private static final float TEXT_FADE_START_PROGRESS = 0.5f;
    private static final float WIDTH_CHANGE_START_PROGRESS = 0.2f;

    private COUIGuideLayoutContentView.OnButtonClickListener mButtonClickListener;
    private int mButtonHeight;
    private final int mButtonSpacing;
    private int mButtonWidth;
    private float mCurrOffset;
    private int mCurrPage;
    private boolean mHasInitialized;
    private boolean mMultipleType;
    private COUITwoTextButton mNextButton;
    private COUIGuideLayoutContentView.OnButtonClickListener mNextButtonClickListener;
    private int mPagerCount;
    private float mPreOffset;
    private float mRawOffset;
    private int mScrollState;
    private COUIButton mSkipButton;
    private int mStartButtonWidth;
    private int mStartDraggingPage;
    private COUISpringAnimation mWidthSpringAnimation;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ButtonLayoutType {
    }

    public COUIGuideDialogButtonLayout(Context context) {
        this(context, null);
    }

    public COUIGuideDialogButtonLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIGuideDialogButtonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIGuideDialogButtonLayout(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mMultipleType = true;
        mButtonSpacing = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_guide_button_layout_space);
        initAnimation();
        initButtons();
    }

    private void animateToPosition(float position) {
        mWidthSpringAnimation.animateToFinalPosition(position);
    }

    private void dispatchOnNextClick() {
        if (mButtonClickListener != null) {
            mButtonClickListener.onNextClick();
            if (mNextButtonClickListener != null) {
                mNextButtonClickListener.onNextClick();
            }
        }
    }

    private void dispatchOnSkipClick() {
        if (mButtonClickListener != null) {
            mButtonClickListener.onSkipClick();
        }
    }

    private void dispatchOnStartClick() {
        if (mButtonClickListener != null) {
            mButtonClickListener.onStartClick();
        }
    }

    private void initAnimation() {
        mWidthSpringAnimation = new COUISpringAnimation(new FloatValueHolder());
        COUISpringForce springForce = new COUISpringForce(0.0f);
        springForce.setResponse(SPRING_RESPONSE);
        springForce.setBounce(0.0f);
        mWidthSpringAnimation.setSpring(springForce);
        mWidthSpringAnimation.setMinValue(0.0f);
        mWidthSpringAnimation.setMaxValue(1.0f);
        mWidthSpringAnimation.setMinimumVisibleChange(SPRING_MINIMUM_VISIBLE_CHANGE);
        mWidthSpringAnimation.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(COUIDynamicAnimation animation, float value,
                    float velocity) {
                widthChange(value);
                mRawOffset = value;
            }
        });
    }

    private void initButtons() {
        mSkipButton = new COUIButton(getContext(), null, R.attr.couiButtonColorfulWhiteStyle);
        mSkipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSkipButton.getAlpha() == 1.0f) {
                    dispatchOnSkipClick();
                }
            }
        });
        mNextButton = new COUITwoTextButton(getContext(), null, R.attr.couiButtonColorfulLargeStyle);
        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mMultipleType) {
                    dispatchOnStartClick();
                    return;
                }
                if (mScrollState == 1) {
                    return;
                }
                if (mScrollState == 2) {
                    if (mNextButton.getStartTextAlpha() > mNextButton.getNextTextAlpha()) {
                        dispatchOnStartClick();
                    } else {
                        dispatchOnNextClick();
                        mStartDraggingPage = mPagerCount - 2;
                    }
                    return;
                }
                if (mNextButton.getNextTextAlpha() < mNextButton.getStartTextAlpha()) {
                    dispatchOnStartClick();
                } else {
                    dispatchOnNextClick();
                    mStartDraggingPage = mPagerCount - 2;
                }
            }
        });
        if (!mMultipleType) {
            mSkipButton.setVisibility(GONE);
            addView(mNextButton);
        } else {
            mSkipButton.setVisibility(VISIBLE);
            addView(mSkipButton);
            addView(mNextButton);
        }
    }

    private void layoutButtonTransition(COUITwoTextButton button, boolean isRtl) {
        int centerStart;
        if (isRtl) {
            centerStart = getPaddingLeft() + (mButtonWidth / 2);
        } else {
            centerStart = getPaddingLeft() + mButtonWidth + mButtonSpacing + (mButtonWidth / 2);
        }
        float targetCenter = getWidth() / 2.0f;
        float center = centerStart + (mRawOffset * (targetCenter - centerStart));
        float halfWidth = button.getLayoutParams().width / 2.0f;
        int top = getPaddingTop();
        button.layout((int) (center - halfWidth), top, (int) (center + halfWidth),
                mButtonHeight + top);
    }

    private void textAlphaAnimate(float progress) {
        float nextAlpha = progress > TEXT_FADE_START_PROGRESS
                ? Math.max(0.0f, Math.min((TEXT_FADE_END_PROGRESS - progress) / TEXT_FADE_RANGE,
                1.0f)) : 1.0f;
        float startAlpha = Math.max(0.0f, Math.min((progress - TEXT_FADE_START_PROGRESS)
                / TEXT_FADE_START_PROGRESS, 1.0f));
        if (mStartDraggingPage == mPagerCount - 1 && progress < 1.0f) {
            mNextButton.setNextTextAlpha(startAlpha);
            mNextButton.setStartTextAlpha(nextAlpha);
        } else if (mStartDraggingPage == mPagerCount - 2 && progress > 0.0f) {
            mNextButton.setNextTextAlpha(nextAlpha);
            mNextButton.setStartTextAlpha(startAlpha);
        }
        if (mNextButton.getStartTextAlpha() == 1.0f) {
            mNextButton.setNextTextAlpha(0.0f);
        } else if (mNextButton.getNextTextAlpha() == 1.0f) {
            mNextButton.setStartTextAlpha(0.0f);
        } else {
            mNextButton.setText(TEXT_EMPTY);
        }
        mNextButton.invalidate();
    }

    private void widthChange(float progress) {
        float widthProgress = Math.max(0.0f, Math.min(1.0f,
                (progress - WIDTH_CHANGE_START_PROGRESS) / (1.0f - WIDTH_CHANGE_START_PROGRESS)));
        mNextButton.getLayoutParams().width = (int) (mButtonWidth
                + ((mButtonWidth + mButtonSpacing) * widthProgress));
        float alpha = Math.max(0.0f, Math.min(
                (SECONDARY_BUTTON_ALPHA_END_PROGRESS - progress)
                        / SECONDARY_BUTTON_ALPHA_END_PROGRESS, 1.0f));
        mSkipButton.setAlpha(alpha);
        mSkipButton.setVisibility(alpha == 0.0f ? GONE : VISIBLE);
        requestLayout();
    }

    public void addButtonClickListener(COUIGuideLayoutContentView.OnButtonClickListener listener) {
        mButtonClickListener = listener;
    }

    public COUITwoTextButton getNextButton() {
        return mNextButton;
    }

    public COUIButton getSkipButton() {
        return mSkipButton;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        mButtonWidth = (((width - getPaddingLeft()) - getPaddingRight()) - mButtonSpacing) / 2;
        mButtonHeight = (height - getPaddingTop()) - getPaddingBottom();
        if (!mMultipleType) {
            if (mNextButton.getVisibility() == VISIBLE) {
                int childTop = getPaddingTop();
                mNextButton.layout(getPaddingLeft(), childTop, getWidth() - getPaddingRight(),
                        mButtonHeight + childTop);
            }
            return;
        }
        if (!isRtl) {
            if (mSkipButton.getVisibility() == VISIBLE) {
                int childLeft = getPaddingLeft();
                int childTop = getPaddingTop();
                mSkipButton.layout(childLeft, childTop, mButtonWidth + childLeft,
                        mButtonHeight + childTop);
            }
            if (mNextButton.getVisibility() == VISIBLE) {
                layoutButtonTransition(mNextButton, false);
            }
            return;
        }
        if (mSkipButton.getVisibility() == VISIBLE) {
            int childRight = getWidth() - getPaddingRight();
            int childLeft = childRight - mButtonWidth;
            int childTop = getPaddingTop();
            mSkipButton.layout(childLeft, childTop, childRight, mButtonHeight + childTop);
        }
        if (mNextButton.getVisibility() == VISIBLE) {
            layoutButtonTransition(mNextButton, true);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        mButtonWidth = (((size - getPaddingLeft()) - getPaddingRight()) - mButtonSpacing) / 2;
        mStartButtonWidth = (size - getPaddingLeft()) - getPaddingRight();
        if (!mHasInitialized) {
            mHasInitialized = true;
            if (mSkipButton.getVisibility() != GONE) {
                mSkipButton.getLayoutParams().width = mButtonWidth;
            }
            if (mNextButton.getVisibility() != GONE) {
                mNextButton.getLayoutParams().width = mButtonWidth;
            }
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(size, Math.max(mSkipButton.getMeasuredHeight(),
                mNextButton.getMeasuredHeight()) + getPaddingTop() + getPaddingBottom());
    }

    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
        if (state == 1 || state == 0) {
            mStartDraggingPage = mCurrPage;
        }
    }

    public void onPageScrolled(int position, float offset, int offsetPixels) {
        if (mPagerCount <= 1) {
            return;
        }
        mPreOffset = mCurrOffset;
        float lastTwoPagesOffset = (position + offset) - (mPagerCount - 2);
        mCurrOffset = Math.max(0.0f, Math.min(lastTwoPagesOffset, 1.0f));
        mCurrOffset = PAGE_SCROLL_INTERPOLATOR.getInterpolation(mCurrOffset);
        if (mPreOffset == mCurrOffset) {
            return;
        }
        animateToPosition(mCurrOffset);
        if (mStartDraggingPage == mPagerCount - 1 && mCurrOffset < 1.0f) {
            textAlphaAnimate(1.0f - mCurrOffset);
        } else if (mStartDraggingPage == mPagerCount - 2 && mCurrOffset > 0.0f) {
            textAlphaAnimate(mCurrOffset);
        }
    }

    public void onPageSelected(int position) {
        mCurrPage = position;
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width == oldWidth) {
            return;
        }
        if (mRawOffset == 0.0f && mNextButton.getLayoutParams().width != mButtonWidth) {
            mNextButton.getLayoutParams().width = mButtonWidth;
        }
        if (mRawOffset == 1.0f && mNextButton.getStartTextAlpha() == 1.0f
                && mNextButton.getLayoutParams().width != mStartButtonWidth) {
            mNextButton.getLayoutParams().width = mStartButtonWidth;
        }
    }

    public void removeButtonClickListener(COUIGuideLayoutContentView.OnButtonClickListener listener) {
        if (mButtonClickListener == listener) {
            mButtonClickListener = null;
        }
    }

    public void setButtonLayoutType(@ButtonLayoutType int type) {
        mMultipleType = type == MULTIPLE_BUTTONS;
    }

    public void setNextButtonClickListener(COUIGuideLayoutContentView.OnButtonClickListener listener) {
        mNextButtonClickListener = listener;
    }

    public void setPagerCount(int count) {
        mPagerCount = count;
    }
}
