package com.coui.appcompat.reddot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;

public class COUIHintRedDot extends View {
    public static final int NO_POINT_MODE = 0;
    public static final int POINT_ONLY_MODE = 1;
    public static final int POINT_WITH_NUM_MODE = 2;
    public static final int POINT_NAVI_WITH_NUM = 3;
    public static final int POINT_ONLY_MODE_STROKE = 4;
    public static final int POINT_NUM_MODE_STROKE = 5;
    public static final long NUM_CHANGE_ALPHA_ANIM_DURATION = 150;
    public static final long NUM_CHANGE_WIDTH_ANIM_DURATION = 517;
    public static final long RED_POINT_ANIM_DURATION = 520;
    public static final Interpolator NUM_CHANGE_WIDTH_ANIM_INTERPOLATOR = new COUIMoveEaseInterpolator();

    private ValueAnimator mAlphaAnim;
    private COUIHintRedDotHelper mCOUIHintRedDotHelper;
    private boolean mIsExecutingAlphaAnim;
    private boolean mIsExecutingWidthAnim;
    private boolean mIsLaidOut;
    private String mMoreText;
    private int mPointMode;
    private int mPointNumber;
    private String mPointText;
    private RectF mRectF;
    private String mRedDotDescription;
    private int mRedDotWithNumberDescriptionId;
    private Drawable mStrokeBackground;
    private int mTempPointNumber;
    private int mTempWidth;
    private int mTextPaintAlpha;
    private ValueAnimator mWidthAnim;

    public COUIHintRedDot(Context context) {
        this(context, null);
    }

    public COUIHintRedDot(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiHintRedDotStyle);
    }

    public COUIHintRedDot(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_COUIHintRedDot);
    }

    public COUIHintRedDot(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mPointMode = NO_POINT_MODE;
        mPointNumber = 0;
        mPointText = "";
        mTempPointNumber = 0;
        mTextPaintAlpha = 255;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIHintRedDot, defStyleAttr, defStyleRes);
        mPointMode = a.getInteger(R.styleable.COUIHintRedDot_couiHintRedPointMode, NO_POINT_MODE);
        setPointNumber(a.getInteger(R.styleable.COUIHintRedDot_couiHintRedPointNum, 0));
        mPointText = a.getString(R.styleable.COUIHintRedDot_couiHintRedPointText);
        a.recycle();
        mCOUIHintRedDotHelper = new COUIHintRedDotHelper(context, attrs, R.styleable.COUIHintRedDot, defStyleAttr, defStyleRes);
        mRectF = new RectF();
        mRedDotDescription = getResources().getString(R.string.red_dot_description);
        mRedDotWithNumberDescriptionId = R.plurals.red_dot_with_number_description;
        mStrokeBackground = context.getResources().getDrawable(R.drawable.red_dot_stroke_circle, context.getTheme());
        if (mPointMode == POINT_ONLY_MODE_STROKE) {
            setBackground(mStrokeBackground);
        }
        mMoreText = context.getString(R.string.red_dot_more);
    }

    private void cancelAnim() {
        if (mWidthAnim != null && mWidthAnim.isRunning()) {
            mWidthAnim.end();
        }
        if (mAlphaAnim != null && mAlphaAnim.isRunning()) {
            mAlphaAnim.end();
        }
    }

    private void executeAlphaAnim() {
        if (mAlphaAnim == null) {
            mAlphaAnim = ValueAnimator.ofInt(255, 0);
            mAlphaAnim.setDuration(NUM_CHANGE_ALPHA_ANIM_DURATION);
            mAlphaAnim.addUpdateListener(animation -> mTextPaintAlpha = (Integer) animation.getAnimatedValue());
            mAlphaAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    finishAlphaAnim();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    finishAlphaAnim();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mIsExecutingAlphaAnim = true;
                }
            });
        }
        mAlphaAnim.start();
    }

    private void finishAlphaAnim() {
        mIsExecutingAlphaAnim = false;
        mPointNumber = mTempPointNumber;
        mPointText = String.valueOf(mPointNumber);
        mTempPointNumber = 0;
    }

    private void executeWidthAnim(int oldNumber, int newNumber) {
        mWidthAnim = ValueAnimator.ofInt(
                mCOUIHintRedDotHelper.getViewWidth(mPointMode, oldNumber),
                mCOUIHintRedDotHelper.getViewWidth(mPointMode, newNumber));
        mWidthAnim.setDuration(NUM_CHANGE_WIDTH_ANIM_DURATION);
        mWidthAnim.setInterpolator(NUM_CHANGE_WIDTH_ANIM_INTERPOLATOR);
        mWidthAnim.addUpdateListener(animation -> {
            mTempWidth = (Integer) animation.getAnimatedValue();
            requestLayout();
        });
        mWidthAnim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationCancel(Animator animation) { mIsExecutingWidthAnim = false; }
            @Override public void onAnimationEnd(Animator animation) { mIsExecutingWidthAnim = false; }
            @Override public void onAnimationStart(Animator animation) {
                mIsExecutingWidthAnim = true;
                executeAlphaAnim();
            }
        });
        mWidthAnim.start();
    }

    public void changePointNumber(int number) {
        if (getVisibility() == GONE || mPointMode == NO_POINT_MODE || mPointMode == POINT_ONLY_MODE
                || mPointMode == POINT_ONLY_MODE_STROKE || mPointMode == POINT_NUM_MODE_STROKE
                || mPointNumber == number || number <= 0 || mCOUIHintRedDotHelper == null) {
            return;
        }
        cancelAnim();
        if (!mIsLaidOut) {
            setPointNumber(number);
        } else {
            mTempPointNumber = number;
            executeWidthAnim(mPointNumber, number);
        }
    }

    public void executeScaleAnim(final boolean show) {
        ValueAnimator animator = ValueAnimator.ofFloat(show ? 0.0f : 1.0f, show ? 1.0f : 0.0f);
        animator.setDuration(RED_POINT_ANIM_DURATION);
        animator.setInterpolator(NUM_CHANGE_WIDTH_ANIM_INTERPOLATOR);
        animator.addUpdateListener(animation -> {
            Float value = (Float) animation.getAnimatedValue();
            if (getVisibility() != GONE) {
                setScaleX(value);
                setScaleY(value);
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationCancel(Animator animation) { if (!show) setPointMode(NO_POINT_MODE); }
            @Override public void onAnimationRepeat(Animator animation) { }
            @Override public void onAnimationStart(Animator animation) {
                if (show) {
                    setVisibility(VISIBLE);
                    requestLayout();
                }
            }
            @Override public void onAnimationEnd(Animator animation) {
                if (!show) {
                    setVisibility(GONE);
                    setPointMode(NO_POINT_MODE);
                }
            }
        });
        animator.start();
    }

    public boolean getIsLaidOut() { return mIsLaidOut; }
    public int getPointMode() { return mPointMode; }
    public int getPointNumber() { return mPointNumber; }
    public String getPointText() { return mPointText; }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnim();
        super.onDetachedFromWindow();
        mIsLaidOut = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRectF.left = 0.0f;
        mRectF.top = 0.0f;
        mRectF.right = getWidth();
        mRectF.bottom = getHeight();
        if (mIsExecutingAlphaAnim && (mPointNumber < 1000 || mTempPointNumber < 1000)) {
            mCOUIHintRedDotHelper.drawPointWithFadeNumber(canvas, mPointNumber, mTextPaintAlpha,
                    mTempPointNumber, 255 - mTextPaintAlpha, mRectF);
        } else if (mPointNumber == 0 || mPointNumber < 1000) {
            mCOUIHintRedDotHelper.drawRedPoint(canvas, mPointMode, mPointText, mRectF);
        } else {
            mCOUIHintRedDotHelper.drawRedPoint(canvas, mPointMode, mMoreText, mRectF);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mIsLaidOut = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mIsExecutingWidthAnim ? mTempWidth : mCOUIHintRedDotHelper.getViewWidth(mPointMode, mPointText),
                mCOUIHintRedDotHelper.getViewHeight(mPointMode));
    }

    public COUIHintRedDotMemento saveMemento() {
        COUIHintRedDotMemento memento = new COUIHintRedDotMemento();
        memento.setPointMode(getPointMode());
        memento.setPointNumber(getPointNumber());
        memento.setPointText(getPointText());
        return memento;
    }

    public void setBgColor(int color) { mCOUIHintRedDotHelper.setBgColor(color); }
    public void setCornerRadius(int radius) { mCOUIHintRedDotHelper.setCornerRadius(radius); }
    public void setDotDiameter(int diameter) { mCOUIHintRedDotHelper.setDotDiameter(diameter); }
    public void setEllipsisDiameter(int diameter) { mCOUIHintRedDotHelper.setEllipsisDiameter(diameter); }
    public void setLaidOut() { mIsLaidOut = true; }
    public void setLargeWidth(int width) { mCOUIHintRedDotHelper.setLargeWidth(width); }
    public void setMediumWidth(int width) { mCOUIHintRedDotHelper.setMediumWidth(width); }

    public void setPointMode(int mode) {
        if (mPointMode != mode) {
            mPointMode = mode;
            if (mode == POINT_ONLY_MODE_STROKE) {
                setBackground(mStrokeBackground);
            }
            requestLayout();
            if (mPointMode == POINT_ONLY_MODE || mPointMode == POINT_ONLY_MODE_STROKE) {
                setContentDescription(mRedDotDescription);
            } else if (mPointMode == NO_POINT_MODE) {
                setContentDescription("");
            }
        }
    }

    public void setPointNumber(int number) {
        mPointNumber = number;
        setPointText(number != 0 ? String.valueOf(number) : "");
        if (number > 0) {
            Resources resources = getResources();
            setContentDescription(COUIAccessibilityUtil.PAUSE_STRING
                    + resources.getQuantityString(mRedDotWithNumberDescriptionId, mPointNumber, mPointNumber));
        }
    }

    public void setPointText(String text) {
        mPointText = text;
        requestLayout();
    }

    public void setSmallWidth(int width) { mCOUIHintRedDotHelper.setSmallWidth(width); }
    public void setTextColor(int color) { mCOUIHintRedDotHelper.setTextColor(color); }
    public void setTextSize(int size) { mCOUIHintRedDotHelper.setTextSize(size); }
    public void setViewHeight(int height) { mCOUIHintRedDotHelper.setViewHeight(height); }
}
