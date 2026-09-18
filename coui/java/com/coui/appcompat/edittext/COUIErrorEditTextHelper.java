package com.coui.appcompat.edittext;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.view.animation.Interpolator;
import android.widget.EditText;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

import java.util.ArrayList;

public class COUIErrorEditTextHelper {
    private static final int DELAY_MASK_ANIMATOR = 80;
    private static final int DURATION_HINT_ANIMATOR = 217;
    private static final int DURATION_MASK_ANIMATOR = 133;
    private static final int MAX_COLOR_VALUE = 255;
    private static final float SELECTION_MASK_ALPHA_MAX = 0.3f;
    private static final Rect TEMP_RECT = new Rect();

    private boolean mAnimating;
    private COUICutoutDrawable mBoxBackground;
    private final COUICutoutDrawable.COUICollapseTextHelper mCOUICollapseTextHelper;
    private ColorStateList mCollapsedTextColor;
    private final EditText mEditText;
    private int mErrorColor;
    private Paint mErrorPaint;
    private boolean mErrorState;
    private AnimatorSet mErrorTrueAnimatorSet;
    private ColorStateList mExpandedTextColor;
    private float mHintColorChangeProgress;
    private boolean mIsFocusedAtAnimateBeginning;
    private ArrayList<COUIEditText.OnErrorStateChangedListener> mOnErrorStateChangedListeners;
    private int mOriginalHighlightColor;
    private ColorStateList mOriginalTextColors;
    private float mSelectionMaskAlpha;
    private Paint mSelectionMaskPaint;
    private float mSingleCOUIEditTextHeight;
    private int mStrokeWidth;
    private float mTextShakeOffset;
    private float mTextWidth;

    public COUIErrorEditTextHelper(EditText editText) {
        this(editText, 1);
    }

    public COUIErrorEditTextHelper(EditText editText, int hintLines) {
        mEditText = editText;
        mCOUICollapseTextHelper = new COUICutoutDrawable.COUICollapseTextHelper(editText);
        mCOUICollapseTextHelper.setHintLines(hintLines);
        mCOUICollapseTextHelper.setTextSizeInterpolator(new COUILinearInterpolator());
        mCOUICollapseTextHelper.setPositionInterpolator(new COUILinearInterpolator());
        mCOUICollapseTextHelper.setCollapsedTextGravity(8388659);
    }

    private void cancelAnimation() {
        if (mErrorTrueAnimatorSet != null && mErrorTrueAnimatorSet.isStarted()) {
            mErrorTrueAnimatorSet.cancel();
        }
    }

    private Layout.Alignment getAlignment() {
        switch (mEditText.getTextAlignment()) {
            case 1:
                int gravity = mEditText.getGravity() & 8388615;
                if (gravity == 1) {
                    return Layout.Alignment.ALIGN_CENTER;
                }
                if (gravity == 3) {
                    return isRtlMode() ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL;
                }
                if (gravity == 5) {
                    return isRtlMode() ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE;
                }
                if (gravity == 8388613) {
                    return Layout.Alignment.ALIGN_OPPOSITE;
                }
                return Layout.Alignment.ALIGN_NORMAL;
            case 2:
            case 5:
                return Layout.Alignment.ALIGN_NORMAL;
            case 3:
            case 6:
                return Layout.Alignment.ALIGN_OPPOSITE;
            case 4:
                return Layout.Alignment.ALIGN_CENTER;
            default:
                return Layout.Alignment.ALIGN_NORMAL;
        }
    }

    private CharSequence getFullText() {
        return !isPassword() ? mEditText.getText() : getMaskChars();
    }

    private int getGradientColor(int from, int to, float fraction) {
        if (fraction <= 0.0f) {
            return from;
        }
        if (fraction >= 1.0f) {
            return to;
        }
        float inverse = 1.0f - fraction;
        int alpha = clampColor((int) ((Color.alpha(from) * inverse) + (Color.alpha(to) * fraction)));
        int red = clampColor((int) ((Color.red(from) * inverse) + (Color.red(to) * fraction)));
        int green = clampColor((int) ((Color.green(from) * inverse) + (Color.green(to) * fraction)));
        int blue = clampColor((int) ((Color.blue(from) * inverse) + (Color.blue(to) * fraction)));
        return Color.argb(alpha, red, green, blue);
    }

    int blendColor(int from, int to) {
        return getGradientColor(from, to, mHintColorChangeProgress);
    }

    private int clampColor(int color) {
        return Math.min(MAX_COLOR_VALUE, Math.max(0, color));
    }

    private CharSequence getMaskChars() {
        TransformationMethod transformationMethod = mEditText.getTransformationMethod();
        return transformationMethod != null
                ? transformationMethod.getTransformation(mEditText.getText(), mEditText)
                : mEditText.getText();
    }

    private int getSelectionMaskColor(float alpha) {
        return Color.argb((int) (alpha * 255.0f), Color.red(mErrorColor), Color.green(mErrorColor),
                Color.blue(mErrorColor));
    }

    private void initAnimator() {
        float shakeAmplitude = mEditText.getResources().getDimension(R.dimen.coui_edit_text_shake_amplitude);
        COUIEaseInterpolator easeInterpolator = new COUIEaseInterpolator();

        ValueAnimator hintAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        hintAnimator.setInterpolator(easeInterpolator);
        hintAnimator.setDuration(DURATION_HINT_ANIMATOR);
        hintAnimator.addUpdateListener(animation ->
                mHintColorChangeProgress = (Float) animation.getAnimatedValue());

        ValueAnimator shakeAnimator = ValueAnimator.ofFloat(0.0f, shakeAmplitude);
        shakeAnimator.setInterpolator(new ShakeInterpolator());
        shakeAnimator.setDuration(ShakeInterpolator.TOTAL_DURATION);
        shakeAnimator.addUpdateListener(animation -> {
            if (mIsFocusedAtAnimateBeginning) {
                mTextShakeOffset = (Float) animation.getAnimatedValue();
            }
            mEditText.invalidate();
        });

        ValueAnimator maskAnimator = ValueAnimator.ofFloat(0.0f, SELECTION_MASK_ALPHA_MAX);
        maskAnimator.setInterpolator(easeInterpolator);
        maskAnimator.setDuration(DURATION_MASK_ANIMATOR);
        maskAnimator.setStartDelay(DELAY_MASK_ANIMATOR);
        maskAnimator.addUpdateListener(animation -> {
            if (mIsFocusedAtAnimateBeginning) {
                mSelectionMaskAlpha = (Float) animation.getAnimatedValue();
            }
        });

        mErrorTrueAnimatorSet = new AnimatorSet();
        mErrorTrueAnimatorSet.playTogether(hintAnimator, shakeAnimator, maskAnimator);
        mErrorTrueAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mEditText.setSelection(mEditText.length());
                if (mSingleCOUIEditTextHeight <= 0.0f) {
                    mEditText.post(() -> mSingleCOUIEditTextHeight = mEditText.getHeight());
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setErrorStateEnd(true, true, true);
                performOnErrorStateChangeAnimationEnd(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private boolean isPassword() {
        return (mEditText.getInputType() & 128) == 128 || (mEditText.getInputType() & 16) == 16;
    }

    private boolean isRtlMode() {
        return mEditText.getLayoutDirection() == EditText.LAYOUT_DIRECTION_RTL;
    }

    private void performOnErrorStateChanged(boolean error) {
        if (mOnErrorStateChangedListeners == null) {
            return;
        }
        for (int i = 0; i < mOnErrorStateChangedListeners.size(); i++) {
            mOnErrorStateChangedListeners.get(i).onErrorStateChanged(error);
        }
    }

    private void performOnErrorStateChangeAnimationEnd(boolean error) {
        if (mOnErrorStateChangedListeners == null) {
            return;
        }
        for (int i = 0; i < mOnErrorStateChangedListeners.size(); i++) {
            mOnErrorStateChangedListeners.get(i).onErrorStateChangeAnimationEnd(error);
        }
    }

    private void setErrorStateEnd(boolean error, boolean restoreTextColor, boolean selectAll) {
        mAnimating = false;
        if (!error) {
            mEditText.setTextColor(mOriginalTextColors);
            mEditText.setHighlightColor(mOriginalHighlightColor);
            return;
        }
        if (restoreTextColor) {
            mEditText.setTextColor(mOriginalTextColors);
        }
        mEditText.setHighlightColor(getSelectionMaskColor(SELECTION_MASK_ALPHA_MAX));
        if (selectAll) {
            mEditText.setSelection(0, mEditText.getText().length());
        }
    }

    private void setErrorStateWithAnimation(boolean error, boolean selectAllOnEnd) {
        if (!error) {
            cancelAnimation();
            setErrorStateEnd(false, false, selectAllOnEnd);
            return;
        }
        cancelAnimation();
        mEditText.setTextColor(Color.TRANSPARENT);
        mEditText.setHighlightColor(Color.TRANSPARENT);
        mHintColorChangeProgress = 0.0f;
        mTextShakeOffset = 0.0f;
        mSelectionMaskAlpha = 0.0f;
        mAnimating = true;
        mIsFocusedAtAnimateBeginning = mEditText.isFocused();
        mErrorTrueAnimatorSet.start();
    }

    private void setErrorStateWithoutAnimation(boolean error, boolean selectAllOnEnd) {
        if (!error) {
            setErrorStateEnd(false, false, selectAllOnEnd);
            return;
        }
        mHintColorChangeProgress = 1.0f;
        mTextShakeOffset = 0.0f;
        mSelectionMaskAlpha = 0.0f;
        setErrorStateEnd(true, false, selectAllOnEnd);
    }

    public void addOnErrorStateChangedListener(COUIEditText.OnErrorStateChangedListener listener) {
        if (mOnErrorStateChangedListeners == null) {
            mOnErrorStateChangedListeners = new ArrayList<>();
        }
        if (!mOnErrorStateChangedListeners.contains(listener)) {
            mOnErrorStateChangedListeners.add(listener);
        }
    }

    public void drawCollapseText(Canvas canvas, COUICutoutDrawable.COUICollapseTextHelper helper) {
        mCOUICollapseTextHelper.setCollapsedTextColor(ColorStateList.valueOf(
                getGradientColor(mCollapsedTextColor.getDefaultColor(), mErrorColor,
                        mHintColorChangeProgress)));
        mCOUICollapseTextHelper.setExpandedTextColor(ColorStateList.valueOf(
                getGradientColor(mExpandedTextColor.getDefaultColor(), mErrorColor,
                        mHintColorChangeProgress)));
        mCOUICollapseTextHelper.setExpansionFraction(helper.getExpandedFraction());
        mCOUICollapseTextHelper.draw(canvas);
    }

    public void drawModeBackgroundLine(Canvas canvas, int lineY, int width, int focusedWidth, Paint normalPaint,
            Paint focusedPaint) {
        mErrorPaint.setColor(getGradientColor(normalPaint.getColor(), mErrorColor,
                mHintColorChangeProgress));
        float y = lineY;
        canvas.drawRect(0.0f, lineY - mStrokeWidth, width, y, mErrorPaint);
        mErrorPaint.setColor(getGradientColor(focusedPaint.getColor(), mErrorColor,
                mHintColorChangeProgress));
        canvas.drawRect(0.0f, lineY - mStrokeWidth, focusedWidth, y, mErrorPaint);
    }

    public void drawModeBackgroundRect(Canvas canvas, GradientDrawable background, int color) {
        mBoxBackground.setBounds(background.getBounds());
        if (background instanceof COUICutoutDrawable) {
            mBoxBackground.setCutout(((COUICutoutDrawable) background).getCutout());
        }
        mBoxBackground.setStroke(mStrokeWidth, getGradientColor(color, mErrorColor,
                mHintColorChangeProgress));
        mBoxBackground.draw(canvas);
    }

    public void drawableStateChanged(int[] state) {
        mCOUICollapseTextHelper.setState(state);
    }

    public void init(int errorColor) {
        init(errorColor, 1, 0, new float[8], new COUICutoutDrawable.COUICollapseTextHelper(mEditText));
    }

    public void init(int errorColor, int strokeWidth, int backgroundMode, float[] cornerRadii,
            COUICutoutDrawable.COUICollapseTextHelper helper) {
        mOriginalTextColors = mEditText.getTextColors();
        mOriginalHighlightColor = mEditText.getHighlightColor();
        mErrorColor = errorColor;
        mStrokeWidth = strokeWidth;
        if (backgroundMode == COUIEditText.MODE_BACKGROUND_RECT) {
            mCOUICollapseTextHelper.setTypefaces(Typeface.create(COUIChangeTextUtil.MEDIUM_FONT, 0));
        }
        mCOUICollapseTextHelper.setExpandedTextSize(helper.getExpandedTextSize());
        mCOUICollapseTextHelper.setCollapsedTextGravity(helper.getCollapsedTextGravity());
        mCOUICollapseTextHelper.setExpandedTextGravity(helper.getExpandedTextGravity());
        mBoxBackground = new COUICutoutDrawable();
        mBoxBackground.setCornerRadii(cornerRadii);
        mErrorPaint = new Paint();
        mSelectionMaskPaint = new Paint();
        initAnimator();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mSingleCOUIEditTextHeight <= 0.0f) {
                    mSingleCOUIEditTextHeight = mEditText.getHeight();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setErrorState(false, false, false);
                Editable text = mEditText.getText();
                int length = text.length();
                mTextWidth = mEditText.getPaint().measureText(text, 0, length);
            }
        });
        setHintInternal(helper);
        updateLabelState(helper);
    }

    public boolean isErrorState() {
        return mErrorState;
    }

    public void onDraw(Canvas canvas) {
        if (!mAnimating || !mErrorState) {
            return;
        }
        int save = canvas.save();
        canvas.translate(isRtlMode() ? -mTextShakeOffset : mTextShakeOffset, 0.0f);
        int compoundPaddingStart = mEditText.getCompoundPaddingStart();
        int compoundPaddingEnd = mEditText.getCompoundPaddingEnd();
        int contentRight = mEditText.getWidth() - compoundPaddingEnd;
        int contentWidth = contentRight - compoundPaddingStart;
        float textRight = contentRight + mEditText.getX() + mEditText.getScrollX();
        float overflow = (mTextWidth - mEditText.getScrollX()) - contentWidth;
        mEditText.getLineBounds(0, TEMP_RECT);

        int textSave = canvas.save();
        canvas.translate(isRtlMode() ? compoundPaddingEnd : compoundPaddingStart, TEMP_RECT.top);
        int clipSave = canvas.save();
        if (mEditText.getBottom() - mEditText.getTop() == mSingleCOUIEditTextHeight
                && mTextWidth > contentWidth) {
            if (isRtlMode()) {
                canvas.clipRect(mEditText.getScrollX() + contentWidth, 0.0f, mEditText.getScrollX(),
                        mSingleCOUIEditTextHeight);
            } else {
                canvas.translate(-overflow, 0.0f);
                canvas.clipRect(mEditText.getScrollX(), 0.0f, textRight,
                        mSingleCOUIEditTextHeight);
            }
        }
        Layout layout = mEditText.getLayout();
        if (layout != null && mOriginalTextColors != null) {
            layout.getPaint().setColor(mOriginalTextColors.getDefaultColor());
            layout.draw(canvas);
        }
        canvas.restoreToCount(clipSave);
        canvas.restoreToCount(textSave);

        Layout.Alignment alignment = getAlignment();
        mSelectionMaskPaint.setColor(getSelectionMaskColor(mSelectionMaskAlpha));
        float left;
        float right;
        if ((alignment == Layout.Alignment.ALIGN_NORMAL && !isRtlMode())
                || (alignment == Layout.Alignment.ALIGN_OPPOSITE && isRtlMode())) {
            left = compoundPaddingStart;
            right = left;
        } else if ((alignment == Layout.Alignment.ALIGN_NORMAL && isRtlMode())
                || (alignment == Layout.Alignment.ALIGN_OPPOSITE && !isRtlMode())) {
            left = compoundPaddingStart;
            right = left;
        } else {
            float center = ((compoundPaddingStart + contentRight) - compoundPaddingEnd) / 2.0f;
            left = center - (mTextWidth / 2.0f);
            right = left + mTextWidth;
        }
        canvas.drawRect(left, TEMP_RECT.top, right, TEMP_RECT.bottom, mSelectionMaskPaint);
        canvas.restoreToCount(save);
    }

    public void onLayout(COUICutoutDrawable.COUICollapseTextHelper helper) {
        Rect expandedBounds = helper.getExpandedBounds();
        Rect collapsedBounds = helper.getCollapsedBounds();
        mCOUICollapseTextHelper.setExpandedBounds(expandedBounds.left, expandedBounds.top,
                expandedBounds.right, expandedBounds.bottom);
        mCOUICollapseTextHelper.setCollapsedBounds(collapsedBounds.left, collapsedBounds.top,
                collapsedBounds.right, collapsedBounds.bottom);
        mCOUICollapseTextHelper.recalculate();
    }

    public void removeOnErrorStateChangedListener(COUIEditText.OnErrorStateChangedListener listener) {
        if (mOnErrorStateChangedListeners != null) {
            mOnErrorStateChangedListeners.remove(listener);
        }
    }

    public void setCollapsedTextAppearance(int textSize, ColorStateList colorStateList) {
        mCOUICollapseTextHelper.setCollapsedTextAppearance(textSize, colorStateList);
    }

    public void setErrorColor(int color) {
        mErrorColor = color;
    }

    public void setErrorState(boolean error) {
        setErrorState(error, true);
    }

    private void setErrorState(boolean error, boolean animate) {
        setErrorState(error, animate, true);
    }

    private void setErrorState(boolean error, boolean animate, boolean selectAllOnEnd) {
        if (mErrorState == error) {
            return;
        }
        mErrorState = error;
        performOnErrorStateChanged(error);
        if (animate) {
            setErrorStateWithAnimation(error, selectAllOnEnd);
        } else {
            setErrorStateWithoutAnimation(error, selectAllOnEnd);
        }
    }

    public void setHintInternal(COUICutoutDrawable.COUICollapseTextHelper helper) {
        mCOUICollapseTextHelper.setText(helper.getText());
    }

    public void setOriginalTextColors(ColorStateList colorStateList) {
        mOriginalTextColors = colorStateList;
    }

    public void updateLabelState(COUICutoutDrawable.COUICollapseTextHelper helper) {
        mCollapsedTextColor = helper.getCollapsedTextColor();
        mExpandedTextColor = helper.getExpandedTextColor();
        mCOUICollapseTextHelper.setCollapsedTextColor(mCollapsedTextColor);
        mCOUICollapseTextHelper.setExpandedTextColor(mExpandedTextColor);
    }

    static class ShakeInterpolator implements Interpolator {
        static final int TOTAL_DURATION = 450;
        private static final int[] DURATIONS = {83, DURATION_MASK_ANIMATOR, 117, 117};
        private static final float[] OFFSETS = {0.0f, -1.0f, 0.5f, -0.5f, 0.0f};
        private static final float[] PROGRESSES = new float[DURATIONS.length + 1];
        private final Interpolator mBetweenInterpolator = new COUIEaseInterpolator();

        static {
            int total = 0;
            for (int i = 0; i < DURATIONS.length; i++) {
                total += DURATIONS[i];
                PROGRESSES[i + 1] = total / (float) TOTAL_DURATION;
            }
        }

        @Override
        public float getInterpolation(float input) {
            for (int i = 1; i < PROGRESSES.length; i++) {
                float end = PROGRESSES[i];
                if (input <= end) {
                    int startIndex = i - 1;
                    float start = PROGRESSES[startIndex];
                    float interpolation = mBetweenInterpolator.getInterpolation((input - start)
                            / (end - start));
                    return (OFFSETS[startIndex] * (1.0f - interpolation))
                            + (OFFSETS[i] * interpolation);
                }
            }
            return 0.0f;
        }
    }
}
