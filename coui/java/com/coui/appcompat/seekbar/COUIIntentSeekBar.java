package com.coui.appcompat.seekbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;

public class COUIIntentSeekBar extends COUISeekBarDeprecate {
    private boolean mIsFollowThumb;
    private int mSecondaryProgress;
    private int mSecondaryProgressColor;
    private float mThumbOutShadeRadius;

    public COUIIntentSeekBar(Context context) {
        this(context, null);
    }

    public COUIIntentSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiIntentSeekBarStyle);
    }

    public COUIIntentSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,
                COUIContextUtil.isCOUIDarkTheme(context) ? R.style.COUIIntentSeekBar_Dark
                        : R.style.COUIIntentSeekBar);
    }

    public COUIIntentSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIIntentSeekBar, defStyleAttr,
                defStyleRes);
        ColorStateList secondaryColor =
                a.getColorStateList(R.styleable.COUIIntentSeekBar_couiSeekBarSecondaryProgressColor);
        mIsFollowThumb = a.getBoolean(R.styleable.COUIIntentSeekBar_couiSeekBarIsFollowThumb, false);
        a.recycle();
        mSecondaryProgressColor = getColor(this, secondaryColor,
                COUIContextUtil.getColor(context, R.color.coui_seekbar_progress_color_normal));
        mThumbOutShadeRadius = getResources()
                .getDimensionPixelSize(R.dimen.coui_seekbar_intent_thumb_out_shade_radius);
    }

    @Override
    public void drawActiveTrack(Canvas canvas, float width) {
        if (!mShowProgress) {
            return;
        }
        float centerY = getSeekBarCenterY();
        int range = mMax - mMin;
        if (range <= 0) {
            return;
        }

        float progressStart;
        float progressEnd;
        float secondaryStart;
        float secondaryEnd;
        float progressRadius = mCurProgressHeight / 2.0f;
        if (isLayoutRtl()) {
            secondaryEnd = getStart() + mAnimatedProgressPaddingHorizontal + width;
            progressEnd = secondaryEnd - (((mOldProgress - mMin) * width) / range);
            secondaryStart = secondaryEnd - (((mSecondaryProgress - mMin) * width) / range);
            progressStart = progressEnd;
        } else {
            progressStart = getStart() + mAnimatedProgressPaddingHorizontal;
            progressEnd = progressStart + (((mOldProgress - mMin) * width) / range);
            secondaryEnd = progressStart + (((mSecondaryProgress - mMin) * width) / range);
            secondaryStart = progressStart;
        }

        mPaint.setColor(mSecondaryProgressColor);
        mProgressRect.set(secondaryStart - progressRadius, centerY - progressRadius,
                secondaryEnd + progressRadius, centerY + progressRadius);
        canvas.drawRoundRect(mProgressRect, progressRadius, progressRadius, mPaint);

        if (mIsFollowThumb) {
            super.drawActiveTrack(canvas, width);
            return;
        }

        mPaint.setColor(mProgressColor);
        mProgressRect.set(progressStart - progressRadius, centerY - progressRadius,
                progressEnd + progressRadius, centerY + progressRadius);
        canvas.drawRoundRect(mProgressRect, progressRadius, progressRadius, mPaint);
        drawThumbs(canvas);
    }

    private void drawThumbs(Canvas canvas) {
        float seekBarWidth = getSeekBarWidth();
        float centerY = getSeekBarCenterY();
        float thumbCenter = isLayoutRtl()
                ? ((getStart() + mAnimatedProgressPaddingHorizontal) + seekBarWidth)
                        - (mScale * seekBarWidth)
                : getStart() + mAnimatedProgressPaddingHorizontal + (mScale * seekBarWidth);
        float left = thumbCenter - mCurThumbRadius;
        float right = thumbCenter + mCurThumbRadius;
        mPaint.setColor(mThumbColor);
        if (!mIsDragging || mIsFollowThumb) {
            canvas.drawRoundRect(left, centerY - mCurThumbRadius, right, centerY + mCurThumbRadius,
                    mCurThumbRadius, mCurThumbRadius, mPaint);
        } else {
            canvas.drawRoundRect(left - mThumbOutShadeRadius,
                    (centerY - mCurThumbRadius) - mThumbOutShadeRadius,
                    right + mThumbOutShadeRadius,
                    centerY + mCurThumbRadius + mThumbOutShadeRadius,
                    mCurThumbRadius + mThumbOutShadeRadius,
                    mCurThumbRadius + mThumbOutShadeRadius, mPaint);
        }
        mThumbPosition = left + ((right - left) / 2.0f);
    }

    @Override
    public void setSecondaryProgress(int progress) {
        if (progress >= 0) {
            mSecondaryProgress = Math.max(getMin(), Math.min(getMax(), progress));
            invalidate();
        }
    }

    @Override
    public int getSecondaryProgress() {
        return mSecondaryProgress;
    }

    @Override
    public void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        mOldProgress = mProgress;
    }

    public void setSecondaryProgressColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mSecondaryProgressColor = getColor(this, colorStateList,
                    COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_secondary_progress_color));
            invalidate();
        }
    }

    public void setFollowThumb(boolean followThumb) {
        mIsFollowThumb = followThumb;
        invalidate();
    }

    public boolean isFollowThumb() {
        return mIsFollowThumb;
    }
}
