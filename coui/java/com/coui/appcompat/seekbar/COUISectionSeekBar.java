package com.coui.appcompat.seekbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.statelistutil.COUIStateListUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class COUISectionSeekBar extends COUISeekBar {
    private static final float DEFORMATION_RELEASE_SPRING_RESPONSE = 0.35f;
    private static final int DEFORMATION_SCALE_FACTOR = 1000;
    private static final long MOVE_ANIMATOR_DURATION = 100L;
    private static final float MOVE_RATIO = 0.4f;

    private static final FloatPropertyCompat<COUISectionSeekBar> DEFORMED_RELEASE_PROPERTY =
            new FloatPropertyCompat<COUISectionSeekBar>("deformedReleaseTransition") {
                @Override
                public float getValue(COUISectionSeekBar seekBar) {
                    return seekBar.getSectionScale();
                }

                @Override
                public void setValue(COUISectionSeekBar seekBar, float value) {
                    seekBar.setSectionScale(value);
                }
            };

    private int mActionMoveDirection;
    private int mActiveMarkColor;
    private ColorStateList mActiveMarkColorStateList;
    private float mCurrentOffset;
    private COUISpringAnimation mDeformedReleaseAnim;
    private int mInactiveMarkColor;
    private ColorStateList mInactiveMarkColorStateList;
    private boolean mIsFastMoving;
    private float mMarkRadius;
    private float mMoveAnimationEndThumbX;
    private float mMoveAnimationStartThumbX;
    private float mMoveAnimationValue;
    private float mClickAnimationStartThumbX = -1.0f;
    private ValueAnimator mMoveAnimator;
    private boolean mOnStopTrackingMask;
    private final PorterDuffXfermode mPorterDuffXfermode;
    private float mThumbX = -1.0f;
    private int mTouchDownPos = -1;
    private float mTouchDownThumbX;

    public COUISectionSeekBar(Context context) {
        this(context, null);
    }

    public COUISectionSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiSectionSeekBarStyle);
    }

    public COUISectionSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.COUISectionSeekBar);
    }

    public COUISectionSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        mMarkRadius = getResources().getDimensionPixelSize(
                R.dimen.coui_section_seekbar_tick_mark_radius);
        mInactiveMarkColorStateList = COUIStateListUtil.createColorStateList(
                COUIContextUtil.getColor(context, R.color.coui_seekbar_tick_mark_color),
                COUIContextUtil.getColor(context, R.color.coui_seekbar_inactive_mark_disable_color));
        mActiveMarkColorStateList = COUIStateListUtil.createColorStateList(
                COUIContextUtil.getAttrColor(context, R.attr.couiColorLabelOnColor),
                COUIContextUtil.getColor(context, R.color.coui_seekbar_active_mark_disable_color));
        updateMarkColors();
        initDeformedReleaseAnim();
    }

    private void initDeformedReleaseAnim() {
        if (mDeformedReleaseAnim != null) {
            return;
        }
        mDeformedReleaseAnim = new COUISpringAnimation(this, DEFORMED_RELEASE_PROPERTY);
        COUISpringForce springForce = new COUISpringForce();
        springForce.setBounce(0.0f);
        springForce.setResponse(DEFORMATION_RELEASE_SPRING_RESPONSE);
        mDeformedReleaseAnim.setSpring(springForce);
    }

    private float getSectionScale() {
        return mScale * DEFORMATION_SCALE_FACTOR;
    }

    private void setSectionScale(float scale) {
        mScale = scale / DEFORMATION_SCALE_FACTOR;
        calculateTouchDeformationValue();
    }

    private void updateMarkColors() {
        mInactiveMarkColor = getColor(this, mInactiveMarkColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_tick_mark_color));
        mActiveMarkColor = getColor(this, mActiveMarkColorStateList,
                COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorLabelOnColor));
    }

    private void calculateCurIndex() {
        int nextProgress = mProgress;
        boolean changed = true;
        if (mMoveAnimationEndThumbX - mMoveAnimationStartThumbX > 0.0f) {
            nextProgress = Math.round(mThumbX
                    / (mIsDragging ? getMoveSectionWidth() : getSectionWidth()));
        } else if (mMoveAnimationEndThumbX - mMoveAnimationStartThumbX < 0.0f) {
            nextProgress = (int) Math.ceil(((int) mThumbX)
                    / (mIsDragging ? getMoveSectionWidth() : getSectionWidth()));
        } else {
            changed = false;
        }
        if (isLayoutRtl() && changed) {
            nextProgress = mMax - nextProgress;
        }
        setSectionProgressFromUser(nextProgress);
    }

    private void calculateThumbPositionByIndex() {
        int seekBarWidth = getSeekBarWidth();
        int range = Math.max(1, mMax - mMin);
        mThumbX = ((mProgress - mMin) * seekBarWidth) / (float) range;
        if (isLayoutRtl()) {
            mThumbX = seekBarWidth - mThumbX;
        }
    }

    private void clearDeformationValue(MotionEvent event) {
        float x = (event.getX() - getStart()) - mProgressPaddingHorizontal;
        if (x > 0.0f && x < getSeekBarWidth()) {
            resetDeformationValue();
        }
    }

    private void drawMark(Canvas canvas, int centerY, float deformationOffset) {
        float start = getStart() + mProgressPaddingHorizontal;
        float end = (getWidth() - getEnd()) - mProgressPaddingHorizontal;
        float width = end - start;
        float thumbStart = mThumbPosition - mCurThumbRadius;
        float thumbEnd = mThumbPosition + mCurThumbRadius;
        int saveLayer = canvas.saveLayer(null, null);
        mPaint.setXfermode(mPorterDuffXfermode);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor((!mShowProgress || isLayoutRtl()) ? mInactiveMarkColor : mActiveMarkColor);
        boolean switchedColor = false;
        int sections = Math.max(1, mMax - mMin);
        for (int i = 0; i <= sections; i++) {
            float markX = start + ((i * width) / sections);
            if (mShowProgress && !switchedColor
                    && markX > start + Math.min(mThumbX, getSeekBarWidth())) {
                mPaint.setColor(isLayoutRtl() ? mActiveMarkColor : mInactiveMarkColor);
                switchedColor = true;
            }
            markX += isLayoutRtl() ? -deformationOffset : deformationOffset;
            if (thumbStart > markX - mMarkRadius || thumbEnd < markX + mMarkRadius) {
                canvas.drawCircle(markX, centerY, mMarkRadius, mPaint);
            }
        }
        mPaint.setXfermode(null);
        canvas.restoreToCount(saveLayer);
    }

    private float getMoveSectionWidth() {
        return getSeekBarMoveWidth() / (float) Math.max(1, mMax - mMin);
    }

    private float getMoveThumbXByIndex(int progress) {
        int range = Math.max(1, mMax - mMin);
        float x = ((progress - mMin) * getSeekBarMoveWidth()) / (float) range;
        float width = getSeekBarMoveWidth();
        float clamped = Math.max(0.0f, Math.min(x, width));
        return isLayoutRtl() ? width - clamped : clamped;
    }

    private float getSectionWidth() {
        return getSeekBarNormalWidth() / (float) Math.max(1, mMax - mMin);
    }

    private int getSeekBarMoveWidth() {
        return Math.max(0, (int) (((getWidth() - getStart()) - getEnd())
                - (mProgressPaddingHorizontal * 2.0f)));
    }

    private int getSeekBarNormalWidth() {
        return Math.max(0, (int) (((getWidth() - getStart()) - getEnd())
                - (mProgressPaddingHorizontal * 2.0f)));
    }

    private int getThumbPosByX(float x) {
        int seekBarWidth = getSeekBarWidth();
        if (isLayoutRtl()) {
            x = seekBarWidth - x;
        }
        int range = Math.max(1, mMax - mMin);
        return Math.max(mMin, Math.min(Math.round((x * range) / seekBarWidth) + mMin, mMax));
    }

    private float getThumbXByIndex(int progress) {
        int range = Math.max(1, mMax - mMin);
        float x = ((progress - mMin) * getSeekBarNormalWidth()) / (float) range;
        float width = getSeekBarNormalWidth();
        float clamped = Math.max(0.0f, Math.min(x, width));
        return isLayoutRtl() ? width - clamped : clamped;
    }

    private float getTouchXOfDrawArea(MotionEvent event) {
        return Math.min(Math.max(0.0f, (event.getX() - getStart()) - mProgressPaddingHorizontal),
                getSeekBarWidth());
    }

    private void invalidateProgress(float targetX, boolean animate) {
        float thumbXByIndex = getThumbXByIndex(mProgress);
        float delta = subtract(targetX, thumbXByIndex);
        float sectionWidth = getSectionWidth();
        int sectionOffset = mIsDragging
                ? (int) (delta / sectionWidth)
                : Math.round(delta / sectionWidth);
        ValueAnimator animator = mMoveAnimator;
        if (animator != null && animator.isRunning()
                && Float.compare(mMoveAnimationEndThumbX,
                (sectionOffset * sectionWidth) + thumbXByIndex) == 0) {
            return;
        }
        float offset = sectionOffset * sectionWidth;
        mCurrentOffset = offset;
        float startOffset = mThumbX - thumbXByIndex;
        mOnStopTrackingMask = true;
        startMoveAnimation(thumbXByIndex, offset + thumbXByIndex, startOffset, animate);
    }

    private float subtract(float value, float base) {
        return value - base;
    }

    private void setSectionProgressFromUser(int progress) {
        int clamped = Math.max(mMin, Math.min(progress, mMax));
        int oldRealProgress = mRealProgress;
        if (mProgress != clamped || mRealProgress != getRealProgress(clamped)) {
            setLocalProgress(clamped);
            updateScale();
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
            }
            if (oldRealProgress != mRealProgress) {
                performFeedback();
            }
        }
    }

    private void startMoveAnimation(float startThumbX, float endThumbX, float startValue,
            boolean animate) {
        ValueAnimator animator;
        if (Float.compare(mThumbX, endThumbX) == 0
                || ((animator = mMoveAnimator) != null && animator.isRunning()
                && Float.compare(mMoveAnimationEndThumbX, endThumbX) == 0)) {
            if (mOnStopTrackingMask) {
                stopTrackingTouch(true, true);
                mOnStopTrackingMask = false;
            }
            return;
        }
        mMoveAnimationEndThumbX = endThumbX;
        mMoveAnimationStartThumbX = startThumbX;
        if (!animate) {
            mClickAnimationStartThumbX = mThumbX;
            mThumbX = endThumbX;
            calculateCurIndex();
            mOnStopTrackingMask = false;
            return;
        }
        if (mMoveAnimator == null) {
            mMoveAnimator = new ValueAnimator();
            mMoveAnimator.setInterpolator(new COUIInEaseInterpolator());
            mMoveAnimator.addUpdateListener(animation -> {
                mMoveAnimationValue = (Float) animation.getAnimatedValue();
                mThumbX = mMoveAnimationStartThumbX + (mMoveAnimationValue * MOVE_RATIO)
                        + (mCurrentOffset * (1.0f - MOVE_RATIO));
                invalidate();
                calculateCurIndex();
            });
            mMoveAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mOnStopTrackingMask) {
                        stopTrackingTouch(true, true);
                        mOnStopTrackingMask = false;
                    }
                    if (mIsFastMoving) {
                        mIsFastMoving = false;
                        invalidateProgress(mLastX, true);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (mOnStopTrackingMask) {
                        stopTrackingTouch(true, true);
                        mOnStopTrackingMask = false;
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        mMoveAnimator.cancel();
        mMoveAnimator.setDuration(MOVE_ANIMATOR_DURATION);
        mMoveAnimator.setFloatValues(startValue, endThumbX - startThumbX);
        mMoveAnimator.start();
    }

    private void trackTouchEvent(MotionEvent event, float touchX) {
        setTouchScale(isLayoutRtl()
                ? (((getWidth() - event.getX()) - getEnd()) - mProgressPaddingHorizontal)
                / getSeekBarWidth()
                : ((event.getX() - getStart()) - mProgressPaddingHorizontal)
                / getSeekBarWidth(), false);
        executeTouchGlitterEffectAnim();
        float delta = subtract(touchX, mTouchDownThumbX);
        float correctedDelta = delta < 0.0f ? delta - 0.1f : delta + 0.1f;
        float moveSectionWidth = getMoveSectionWidth();
        int sectionOffset = (int) new BigDecimal(Float.toString(correctedDelta))
                .divide(new BigDecimal(Float.toString(moveSectionWidth)),
                        RoundingMode.FLOOR)
                .floatValue();
        float snappedOffset = sectionOffset * moveSectionWidth;
        if (isLayoutRtl()) {
            sectionOffset = -sectionOffset;
        }
        mCurrentOffset = correctedDelta;
        if (Math.abs((mTouchDownPos + sectionOffset) - mProgress) > 0) {
            startMoveAnimation(mTouchDownThumbX, snappedOffset + mTouchDownThumbX,
                    mMoveAnimationValue, true);
        } else {
            mThumbX = mTouchDownThumbX + snappedOffset
                    + ((mCurrentOffset - snappedOffset) * (1.0f - MOVE_RATIO));
            invalidate();
        }
        mLastX = touchX;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mThumbX == -1.0f) {
            calculateThumbPositionByIndex();
        }
        super.draw(canvas);
    }

    @Override
    public void drawActiveTrack(Canvas canvas, float width) {
        int centerY = getSeekBarCenterY();
        float deformationOffset = mHeightTopDeformedUpValue - mHeightBottomDeformedDownValue;
        mThumbPosition = getStart() + mProgressPaddingHorizontal
                + Math.min(mThumbX, getSeekBarWidth())
                + (isLayoutRtl() ? -deformationOffset : deformationOffset);
        drawProgress(canvas);
        drawGlitterEffect(canvas);
        drawMark(canvas, centerY, deformationOffset);
        drawSectionThumb(canvas, centerY);
    }

    private void drawSectionThumb(Canvas canvas, int centerY) {
        if (!mShowThumb) {
            return;
        }
        if (mThumbShadowRadius > 0.0f && isEnabled()) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShadowLayer(mThumbShadowRadius, 0.0f, mThumbShadowOffsetY, mThumbShadowColor);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mThumbColor);
        canvas.drawCircle(mThumbPosition, centerY, mCurThumbRadius, mPaint);
        if (mThumbShadowRadius > 0.0f && isEnabled()) {
            mPaint.clearShadowLayer();
        }
    }

    @Override
    public void handleMotionEventCancel() {
        super.handleMotionEventCancel();
        mIsFastMoving = false;
    }

    @Override
    public void handleMotionEventDown(MotionEvent event) {
        float touchX = getTouchXOfDrawArea(event);
        mTouchDownX = touchX;
        mLastX = touchX;
        mIsBumpingEdges = false;
        executeThumbScaleAnim(event);
        setPressed(true);
        if (mShowText) {
            ensureLabelsAdded();
        }
    }

    @Override
    public void handleMotionEventMove(MotionEvent event) {
        resetBumpingEdges();
        clearDeformationValue(event);
        float touchX = getTouchXOfDrawArea(event);
        if (mIsDragging) {
            int direction = 0;
            float delta = touchX - mLastX;
            if (delta > 0.0f) {
                direction = 1;
            } else if (delta < 0.0f) {
                direction = -1;
            }
            if (direction == -mActionMoveDirection) {
                mActionMoveDirection = direction;
                if (mTouchDownPos != mProgress) {
                    mTouchDownPos = mProgress;
                    mTouchDownThumbX = getMoveThumbXByIndex(mProgress);
                    mMoveAnimationValue = 0.0f;
                }
                if (mMoveAnimator != null) {
                    mMoveAnimator.cancel();
                }
            }
            trackTouchEvent(event, touchX);
            return;
        }
        if (!isToucheInSeekBar(event)) {
            return;
        }
        if (Math.abs(event.getX() - ((mTouchDownX + getStart()) + mProgressPaddingHorizontal))
                > mTouchSlop) {
            if (mClickAnim != null) {
                mClickAnim.cancel();
            }
            if (mDeformedReleaseAnim != null) {
                mDeformedReleaseAnim.cancel();
            }
            startDrag();
            touchAnim();
            int thumbPos = getThumbPosByX(mTouchDownX);
            mTouchDownPos = thumbPos;
            setSectionProgressFromUser(thumbPos);
            mTouchDownThumbX = getMoveThumbXByIndex(mTouchDownPos);
            mMoveAnimationValue = 0.0f;
            mThumbX = mTouchDownThumbX;
            invalidate();
            trackTouchEvent(event, touchX);
            mActionMoveDirection = touchX - mTouchDownX > 0.0f ? 1 : -1;
        }
        mLastX = touchX;
    }

    @Override
    public void handleMotionEventUp(MotionEvent event) {
        releaseThumbScaleAnim();
        float touchX = getTouchXOfDrawArea(event);
        if (!mIsDragging) {
            if (isEnabled() && touchInSeekBar(event, this)) {
                if (mMoveAnimator != null && mMoveAnimator.isRunning()) {
                    mOnStopTrackingMask = false;
                    mIsFastMoving = false;
                    mMoveAnimator.cancel();
                }
                invalidateProgress(touchX, false);
                animForClick(touchX);
                releaseAnim();
            }
            return;
        }
        if (mMoveAnimator != null && mMoveAnimator.isRunning()) {
            mIsFastMoving = true;
        }
        if (mScale < 0.0f) {
            mDeformedReleaseAnim.setStartValue(mScale * DEFORMATION_SCALE_FACTOR);
            mDeformedReleaseAnim.animateToFinalPosition(0.0f);
            stopTrackingTouch(true, true);
        } else if (mScale > 1.0f) {
            mDeformedReleaseAnim.setStartValue(mScale * DEFORMATION_SCALE_FACTOR);
            mDeformedReleaseAnim.animateToFinalPosition(DEFORMATION_SCALE_FACTOR);
            stopTrackingTouch(true, true);
        } else if (!mIsFastMoving) {
            invalidateProgress(touchX, true);
        }
        stopTrackingTouch(false, false);
        setPressed(false);
        releaseAnim();
    }

    @Override
    public void onClickAnimationUpdate(float value) {
        mThumbX = (int) value;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mThumbX = -1.0f;
    }

    public boolean performAdaptiveFeedback() {
        return false;
    }

    @Override
    public void performFeedback() {
        if (!mEnableVibrator) {
            return;
        }
        if (performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE_SYNC)) {
            return;
        }
        performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateMarkColors();
    }

    @Override
    public synchronized void setMax(int max) {
        if (max < getMin()) {
            max = getMin();
        }
        if (max != mMax) {
            setLocalMax(max);
            if (mProgress > max) {
                setProgress(max);
            }
            calculateThumbPositionByIndex();
        }
        invalidate();
    }

    @Override
    public void setProgress(int progress, boolean animate, boolean fromUser) {
        int clamped = Math.max(mMin, Math.min(progress, mMax));
        if (mProgress == clamped) {
            return;
        }
        if (animate) {
            float oldThumbX = mThumbX;
            setLocalProgress(clamped);
            updateScale();
            calculateThumbPositionByIndex();
            mClickAnimationStartThumbX = oldThumbX;
            startTransitionAnim(clamped, fromUser, false);
        } else {
            setLocalProgress(clamped);
            updateScale();
            if (getWidth() != 0) {
                calculateThumbPositionByIndex();
                mMoveAnimationEndThumbX = mThumbX;
                invalidate();
            }
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, fromUser);
        }
    }

    @Override
    public void setProgressRect() {
        float seekBarWidth = getSeekBarWidth();
        int centerY = getSeekBarCenterY();
        if (isLayoutRtl()) {
            float end = getStart() + mProgressPaddingHorizontal + seekBarWidth;
            float start = getStart() + mProgressPaddingHorizontal + mThumbX;
            mProgressRect.set(start - mHeightTopDeformedUpValue + mHeightBottomDeformedDownValue,
                    (centerY - (mProgressHeight / 2.0f)) + mWidthDeformedValue,
                    end - mHeightBottomDeformedUpValue + mHeightBottomDeformedDownValue,
                    (centerY + (mProgressHeight / 2.0f)) - mWidthDeformedValue);
        } else {
            float start = getStart() + mProgressPaddingHorizontal;
            float end = mThumbX + start;
            mProgressRect.set((start - mHeightBottomDeformedDownValue)
                            + mHeightBottomDeformedUpValue,
                    (centerY - (mProgressHeight / 2.0f)) + mWidthDeformedValue,
                    (end + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue,
                    (centerY + (mProgressHeight / 2.0f)) - mWidthDeformedValue);
        }
        RectF rect = mProgressRect;
        rect.left -= mProgressHeight / 2.0f;
        rect.right += mProgressHeight / 2.0f;
    }

    @Override
    public void startTransitionAnim(int progress, final boolean fromUser,
            final boolean clearDragging) {
        COUIDynamicAnimation.OnAnimationEndListener endListener =
                (animation, canceled, value, velocity) -> stopTrackingTouch(fromUser, clearDragging);
        int startX = (int) (mClickAnimationStartThumbX >= 0.0f
                ? mClickAnimationStartThumbX
                : mThumbX);
        int endX = (int) getThumbXByIndex(progress);
        mClickAnimationStartThumbX = -1.0f;
        if (mClickAnim == null) {
            setProgress(progress, false, fromUser);
            stopTrackingTouch(fromUser, clearDragging);
            return;
        }
        mClickAnim.cancel();
        if (mLastEndClickListener != null) {
            mClickAnim.removeEndListener(mLastEndClickListener);
        }
        mClickAnim.addEndListener(endListener);
        mClickAnim.setStartValue(startX);
        startTrackingTouch(fromUser, clearDragging);
        mClickAnim.animateToFinalPosition(endX);
        mLastEndClickListener = endListener;
    }
}
