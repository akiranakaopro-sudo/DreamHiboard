package com.coui.appcompat.panel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;


public class COUIPanelBarView extends View {
    private static final long ANIMATOR_DURATION = 167;
    private static final int ANIMATOR_RESPONSE_THRESHOLD = 5;
    private int continuousMove;
    private int directTo;
    private int mBarColor;
    private int mBarHeight;
    private int mBarMarginTop;
    private int mBarWidth;
    private int mCurrentPosition;
    private boolean mIsBeingDragged;
    private boolean mIsFixed;
    private float mMaxOffset;
    private float mOffset;
    private Paint mPaint;
    private Path mPath;
    private int mSpecialThreshold;
    private float mTopLeftPointX;
    private float mTopLeftPointY;
    private float mTopMiddlePointX;
    private float mTopMiddlePointY;
    private float mTopRightPointX;
    private float mTopRightPointY;
    private ValueAnimator translationAnimator;

    public COUIPanelBarView(Context context) {
        super(context);
        this.mIsFixed = false;
        this.mIsBeingDragged = false;
        this.mOffset = 0.0f;
        this.mTopLeftPointX = 0.0f;
        this.mTopLeftPointY = 0.0f;
        this.mTopMiddlePointX = 0.0f;
        this.mTopMiddlePointY = 0.0f;
        this.mTopRightPointX = 0.0f;
        this.mTopRightPointY = 0.0f;
        this.mMaxOffset = 0.0f;
        this.continuousMove = 0;
        this.mCurrentPosition = 0;
        this.mSpecialThreshold = 0;
        this.directTo = -1;
        init(context);
    }

    private void drawBar(Canvas canvas) {
        setPoint();
        this.mPath.reset();
        this.mPath.moveTo(this.mTopLeftPointX, this.mTopLeftPointY);
        this.mPath.lineTo(this.mTopMiddlePointX, this.mTopMiddlePointY);
        this.mPath.lineTo(this.mTopRightPointX, this.mTopRightPointY);
        canvas.drawPath(this.mPath, this.mPaint);
    }

    private void init(Context context) {
        this.mBarWidth = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_panel_bar_width);
        this.mBarHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_panel_bar_height);
        this.mBarMarginTop = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_panel_bar_margin_top);
        this.mMaxOffset = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_panel_drag_bar_max_offset);
        this.mSpecialThreshold = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_panel_normal_padding_top_tiny_screen);
        this.mBarColor = ContextCompat.getColor(context, R.color.coui_panel_bar_view_color);
        this.mPaint = new Paint();
        this.mPath = new Path();
        Paint paint = new Paint(1);
        this.mPaint = paint;
        paint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setDither(true);
        this.mPaint.setStrokeWidth(this.mBarHeight);
        this.mPaint.setColor(this.mBarColor);
    }

    private void playResetAnimator() {
        if (this.mIsFixed) {
            return;
        }
        ValueAnimator animator = this.translationAnimator;
        if (animator != null && animator.isRunning()) {
            this.translationAnimator.cancel();
        }
        animator = createBarOffsetAnimator(this.mOffset, 0.0f);
        this.translationAnimator = animator;
        animator.setDuration((long) ((Math.abs(this.mOffset) / (this.mMaxOffset * 2.0f)) * ANIMATOR_DURATION));
        this.translationAnimator.setInterpolator(new COUIEaseInterpolator());
        this.translationAnimator.start();
        this.directTo = 0;
    }

    private void playTowardsDownAnimator() {
        if (this.mIsFixed) {
            return;
        }
        ValueAnimator animator = this.translationAnimator;
        if (animator != null && animator.isRunning()) {
            this.translationAnimator.cancel();
        }
        animator = createBarOffsetAnimator(this.mOffset, this.mMaxOffset);
        this.translationAnimator = animator;
        animator.setDuration((long) ((Math.abs(this.mMaxOffset - this.mOffset) / (this.mMaxOffset * 2.0f)) * ANIMATOR_DURATION));
        this.translationAnimator.setInterpolator(new COUIEaseInterpolator());
        this.translationAnimator.start();
        this.directTo = 1;
    }

    private void playTowardsUpAnimator() {
        if (this.mIsFixed) {
            return;
        }
        ValueAnimator animator = this.translationAnimator;
        if (animator != null && animator.isRunning()) {
            this.translationAnimator.cancel();
        }
        animator = createBarOffsetAnimator(this.mOffset, -this.mMaxOffset);
        this.translationAnimator = animator;
        animator.setDuration((long) ((Math.abs(this.mMaxOffset + this.mOffset) / (this.mMaxOffset * 2.0f)) * ANIMATOR_DURATION));
        this.translationAnimator.setInterpolator(new LinearInterpolator());
        this.translationAnimator.start();
        this.directTo = -1;
    }

    private void setBarOffset(float offset) {
        this.mOffset = offset;
        invalidate();
    }

    private ValueAnimator createBarOffsetAnimator(float startOffset, float endOffset) {
        ValueAnimator animator = ValueAnimator.ofFloat(startOffset, endOffset);
        animator.addUpdateListener(animation -> setBarOffset((Float) animation.getAnimatedValue()));
        return animator;
    }

    private void setPoint() {
        float halfOffset = this.mOffset / 2.0f;
        int barHeight = this.mBarHeight;
        this.mTopLeftPointX = barHeight / 2.0f;
        float sidePointY = (barHeight / 2.0f) - halfOffset;
        this.mTopLeftPointY = sidePointY;
        int barWidth = this.mBarWidth;
        this.mTopMiddlePointX = (barWidth / 2.0f) + (barHeight / 2.0f);
        this.mTopMiddlePointY = (barHeight / 2.0f) + halfOffset;
        this.mTopRightPointX = barWidth + (barHeight / 2.0f);
        this.mTopRightPointY = sidePointY;
    }

    private void startAnimator() {
        if (this.mIsBeingDragged) {
            int continuousMove = this.continuousMove;
            if (continuousMove > 0 && this.mOffset <= 0.0f && this.directTo != 1) {
                playTowardsDownAnimator();
            } else {
                if (continuousMove >= 0 || this.mOffset < 0.0f || this.directTo == -1 || this.mCurrentPosition < this.mSpecialThreshold) {
                    return;
                }
                playTowardsUpAnimator();
            }
        }
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0.0f, this.mBarMarginTop);
        drawBar(canvas);
    }

    public void releaseDrag() {
        playResetAnimator();
    }

    public void setBarColor(int color) {
        this.mBarColor = color;
        this.mPaint.setColor(color);
        invalidate();
    }

    public void setIsBeingDragged(boolean isBeingDragged) {
        if (this.mIsBeingDragged != isBeingDragged) {
            this.mIsBeingDragged = isBeingDragged;
            if (isBeingDragged) {
                return;
            }
            releaseDrag();
        }
    }

    public void setIsFixed(boolean isFixed) {
        this.mIsFixed = isFixed;
    }

    public void setPanelOffset(int offset) {
        if (this.mIsFixed) {
            return;
        }
        int previousContinuousMove = this.continuousMove;
        if (previousContinuousMove * offset > 0) {
            this.continuousMove = previousContinuousMove + offset;
        } else {
            this.continuousMove = offset;
        }
        this.mCurrentPosition += offset;
        if (Math.abs(this.continuousMove) > ANIMATOR_RESPONSE_THRESHOLD || (this.continuousMove > 0 && this.mCurrentPosition < this.mSpecialThreshold)) {
            startAnimator();
        }
    }

    public COUIPanelBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsFixed = false;
        this.mIsBeingDragged = false;
        this.mOffset = 0.0f;
        this.mTopLeftPointX = 0.0f;
        this.mTopLeftPointY = 0.0f;
        this.mTopMiddlePointX = 0.0f;
        this.mTopMiddlePointY = 0.0f;
        this.mTopRightPointX = 0.0f;
        this.mTopRightPointY = 0.0f;
        this.mMaxOffset = 0.0f;
        this.continuousMove = 0;
        this.mCurrentPosition = 0;
        this.mSpecialThreshold = 0;
        this.directTo = -1;
        init(context);
    }

    public COUIPanelBarView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mIsFixed = false;
        this.mIsBeingDragged = false;
        this.mOffset = 0.0f;
        this.mTopLeftPointX = 0.0f;
        this.mTopLeftPointY = 0.0f;
        this.mTopMiddlePointX = 0.0f;
        this.mTopMiddlePointY = 0.0f;
        this.mTopRightPointX = 0.0f;
        this.mTopRightPointY = 0.0f;
        this.mMaxOffset = 0.0f;
        this.continuousMove = 0;
        this.mCurrentPosition = 0;
        this.mSpecialThreshold = 0;
        this.directTo = -1;
        init(context);
    }

    public COUIPanelBarView(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mIsFixed = false;
        this.mIsBeingDragged = false;
        this.mOffset = 0.0f;
        this.mTopLeftPointX = 0.0f;
        this.mTopLeftPointY = 0.0f;
        this.mTopMiddlePointX = 0.0f;
        this.mTopMiddlePointY = 0.0f;
        this.mTopRightPointX = 0.0f;
        this.mTopRightPointY = 0.0f;
        this.mMaxOffset = 0.0f;
        this.continuousMove = 0;
        this.mCurrentPosition = 0;
        this.mSpecialThreshold = 0;
        this.directTo = -1;
        init(context);
    }
}
