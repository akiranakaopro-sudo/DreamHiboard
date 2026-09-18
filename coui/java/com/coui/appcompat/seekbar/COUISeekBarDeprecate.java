package com.coui.appcompat.seekbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

import com.coui.appcompat.R;

@Deprecated
public class COUISeekBarDeprecate extends COUISeekBar {
    public COUISeekBarDeprecate(Context context) {
        this(context, null);
    }

    public COUISeekBarDeprecate(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiSeekBarStyle);
    }

    public COUISeekBarDeprecate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public COUISeekBarDeprecate(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void onStartTrackingTouch() {
        startTrackingTouch(true, true);
    }

    public void onStopTrackingTouch() {
        stopTrackingTouch(true, true);
    }

    public void checkThumbPosChange(int progress) {
        setProgress(progress, false, true);
    }

    public void checkThumbPosChange(int progress, boolean fromUser, boolean notify) {
        setProgress(progress, false, fromUser);
    }

    public void refresh() {
        updateScale();
        invalidate();
    }

    @Override
    public void releaseAnim() {
        super.releaseAnim();
    }

    @Override
    public void touchAnim() {
        super.touchAnim();
    }

    public void setBackgroundEnlargeScale(float scale) {
        mBackgroundEnlargeScale = scale;
        invalidate();
    }

    public void setBackgroundHeight(float height) {
        mBackgroundHeight = height;
        mCurBackgroundHeight = height;
        invalidate();
    }

    public void setBackgroundRadius(float radius) {
        mBackgroundRadius = radius;
        invalidate();
    }

    public void setCustomProgressAnimDuration(float duration) {
        // The current COUISeekBar keeps the progress duration internal.
    }

    public void setCustomProgressAnimInterpolator(Interpolator interpolator) {
        // The current COUISeekBar keeps the progress interpolator internal.
    }

    public void setDeformedParams(DeformedValueBean bean) {
        if (bean == null) {
            return;
        }
        mHeightBottomDeformedUpValue = bean.getHeightBottomDeformedUpValue();
        mHeightTopDeformedUpValue = bean.getHeightTopDeformedUpValue();
        mWidthDeformedValue = bean.getWidthDeformedValue();
        mHeightBottomDeformedDownValue = bean.getHeightBottomDeformedDownValue();
        mHeightTopDeformedDownValue = bean.getHeightTopDeformedDownValue();
        mScale = bean.getScale();
        mDrawProgressScale = bean.getDrawProgressScale();
        setLocalProgress(bean.getProgress());
        invalidate();
    }

    public void setEnableAdaptiveVibrator(boolean enable) {
        setEnableVibrator(enable);
    }

    public boolean performAdaptiveFeedback() {
        performFeedback();
        return true;
    }

    public void setEnableVibrator(boolean enable) {
        mEnableVibrator = enable;
    }

    public void setInterpolator(Interpolator interpolator) {
        // Preserved for source compatibility with original deprecated API.
    }

    @Override
    public void setLocalProgress(int progress) {
        super.setLocalProgress(progress);
    }

    public void setMaxMovingDistance(int distance) {
        mMaxMovingDistance = distance;
    }

    public void setPaddingHorizontal(float padding) {
        mProgressPaddingHorizontal = padding;
        mAnimatedProgressPaddingHorizontal = padding;
        invalidate();
    }

    public void setProgressContentDescription(String description) {
        setContentDescription(description);
    }

    public void setProgressEnlargeScale(float scale) {
        mBackgroundEnlargeScale = scale;
    }

    public void setProgressFull() {
        setProgress(getMax(), false);
    }

    public void setProgressHeight(float height) {
        mProgressHeight = height;
        mCurProgressHeight = height;
        invalidate();
    }

    public void setProgressRadius(float radius) {
        mProgressRadius = radius;
        invalidate();
    }

    public void setText(String text) {
        setSeekBarText(text);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
    }

    public void showText(boolean showText) {
        setShowText(showText);
    }

    public float subtract(float value, float base) {
        return value - base;
    }
}
