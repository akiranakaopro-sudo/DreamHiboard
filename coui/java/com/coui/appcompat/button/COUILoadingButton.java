package com.coui.appcompat.button;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.R;


public class COUILoadingButton extends COUIButton {
    public static final int DEFAULT_STATE = 0;
    private static final float DOT_END_ALPHA = 255.0f;
    private static final float DOT_MID_ALPHA = 127.5f;
    private static final float DOT_START_ALPHA = 51.0f;
    public static final int LOADING_STATE = 1;
    private int mButtonState;
    private final String mDots;
    private int mFirstLoadingDotAlpha;
    private AnimatorSet mLoadingAnim;
    private final float mLoadingCircleRadius;
    private final float mLoadingCircleSpacing;
    private final float mLoadingCircleTotalWidth;
    private String mLoadingText;
    private final Rect mLoadingTextBounds;
    private OnLoadingStateChangeListener mOnLoadingStateChangeListener;
    private String mOriginalText;
    private int mSecondLoadingDotAlpha;
    private boolean mShowLoadingText;
    private int mThirdLoadingDotAlpha;

    public interface OnLoadingStateChangeListener {
        void OnLoadingStateChanged(int state);
    }

    public COUILoadingButton(Context context) {
        this(context, null);
    }

    private void drawClipDot(Canvas canvas, float left, float right, float textX, float textY,
            TextPaint textPaint, int alpha) {
        textPaint.setAlpha(alpha);
        int saveCount = canvas.save();
        canvas.clipRect(left, 0.0f, right, getHeight());
        canvas.drawText(this.mDots, textX, textY, textPaint);
        canvas.restoreToCount(saveCount);
    }

    private void drawLoadingCircles(Canvas canvas, TextPaint textPaint) {
        int firstDotAlpha;
        int thirdDotAlpha;
        int secondDotAlpha = this.mSecondLoadingDotAlpha;
        if (isRtlMode()) {
            firstDotAlpha = this.mThirdLoadingDotAlpha;
            thirdDotAlpha = this.mFirstLoadingDotAlpha;
        } else {
            firstDotAlpha = this.mFirstLoadingDotAlpha;
            thirdDotAlpha = this.mThirdLoadingDotAlpha;
        }
        float centerY = getMeasuredHeight() / 2.0f;
        float firstCenterX = ((getMeasuredWidth() - this.mLoadingCircleTotalWidth) / 2.0f) + this.mLoadingCircleRadius;
        textPaint.setAlpha(firstDotAlpha);
        canvas.drawCircle(firstCenterX, centerY, this.mLoadingCircleRadius, textPaint);
        float secondCenterX = firstCenterX + (this.mLoadingCircleRadius * 2.0f) + this.mLoadingCircleSpacing;
        textPaint.setAlpha(secondDotAlpha);
        canvas.drawCircle(secondCenterX, centerY, this.mLoadingCircleRadius, textPaint);
        float thirdCenterX = secondCenterX + (this.mLoadingCircleRadius * 2.0f) + this.mLoadingCircleSpacing;
        textPaint.setAlpha(thirdDotAlpha);
        canvas.drawCircle(thirdCenterX, centerY, this.mLoadingCircleRadius, textPaint);
    }

    private ValueAnimator getAlphaAnimator(float startAlpha, float endAlpha, long duration,
            long startDelay, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator animator = ValueAnimator.ofFloat(startAlpha, endAlpha);
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        animator.addUpdateListener(updateListener);
        return animator;
    }

    private void initAnim() {
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUILoadingButton.this.mFirstLoadingDotAlpha = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
                COUILoadingButton.this.invalidate();
            }
        };
        ValueAnimator alphaAnimator = getAlphaAnimator(DOT_START_ALPHA, DOT_MID_ALPHA, 133L, 0L, animatorUpdateListener);
        ValueAnimator alphaAnimator2 = getAlphaAnimator(DOT_MID_ALPHA, DOT_END_ALPHA, 67L, 133L, animatorUpdateListener);
        ValueAnimator alphaAnimator3 = getAlphaAnimator(DOT_END_ALPHA, DOT_MID_ALPHA, 67L, 467L, animatorUpdateListener);
        ValueAnimator alphaAnimator4 = getAlphaAnimator(DOT_MID_ALPHA, DOT_START_ALPHA, 133L, 533L, animatorUpdateListener);
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener2 = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUILoadingButton.this.mSecondLoadingDotAlpha = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
                COUILoadingButton.this.invalidate();
            }
        };
        ValueAnimator alphaAnimator5 = getAlphaAnimator(DOT_START_ALPHA, DOT_MID_ALPHA, 133L, 333L, animatorUpdateListener2);
        ValueAnimator alphaAnimator6 = getAlphaAnimator(DOT_MID_ALPHA, DOT_END_ALPHA, 67L, 466L, animatorUpdateListener2);
        ValueAnimator alphaAnimator7 = getAlphaAnimator(DOT_END_ALPHA, DOT_MID_ALPHA, 67L, 800L, animatorUpdateListener2);
        ValueAnimator alphaAnimator8 = getAlphaAnimator(DOT_MID_ALPHA, DOT_START_ALPHA, 133L, 866L, animatorUpdateListener2);
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener3 = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUILoadingButton.this.mThirdLoadingDotAlpha = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
                COUILoadingButton.this.invalidate();
            }
        };
        ValueAnimator alphaAnimator9 = getAlphaAnimator(DOT_START_ALPHA, DOT_MID_ALPHA, 133L, 666L, animatorUpdateListener3);
        ValueAnimator alphaAnimator10 = getAlphaAnimator(DOT_MID_ALPHA, DOT_END_ALPHA, 67L, 799L, animatorUpdateListener3);
        ValueAnimator alphaAnimator11 = getAlphaAnimator(DOT_END_ALPHA, DOT_MID_ALPHA, 67L, 1133L, animatorUpdateListener3);
        ValueAnimator alphaAnimator12 = getAlphaAnimator(DOT_MID_ALPHA, DOT_START_ALPHA, 133L, 1199L, animatorUpdateListener3);
        AnimatorSet animatorSet = new AnimatorSet();
        this.mLoadingAnim = animatorSet;
        animatorSet.playTogether(alphaAnimator, alphaAnimator2, alphaAnimator3, alphaAnimator4, alphaAnimator5, alphaAnimator6, alphaAnimator7, alphaAnimator8, alphaAnimator9, alphaAnimator10, alphaAnimator11, alphaAnimator12);
        this.mLoadingAnim.setInterpolator(new COUILinearInterpolator());
        this.mLoadingAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (COUILoadingButton.this.mLoadingAnim == null || COUILoadingButton.this.mButtonState != LOADING_STATE) {
                    return;
                }
                COUILoadingButton.this.post(new Runnable() {
                    @Override
                    public void run() {
                        COUILoadingButton.this.mLoadingAnim.start();
                    }
                });
            }
        });
    }

    private void initTextChangeListener() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (COUILoadingButton.this.mButtonState != LOADING_STATE || text.toString().equals("")) {
                    return;
                }
                COUILoadingButton.this.mOriginalText = text.toString();
                COUILoadingButton.this.setText("");
            }
        });
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public int getButtonState() {
        return this.mButtonState;
    }

    public String getLoadingText() {
        return this.mLoadingText;
    }

    public OnLoadingStateChangeListener getOnLoadingStateChangeListener(OnLoadingStateChangeListener onLoadingStateChangeListener) {
        return this.mOnLoadingStateChangeListener;
    }

    public boolean getShowLoadingText() {
        return this.mShowLoadingText;
    }

    @Override
    public void onAttachedToWindow() {
        AnimatorSet animatorSet;
        super.onAttachedToWindow();
        if (this.mButtonState != LOADING_STATE || (animatorSet = this.mLoadingAnim) == null || animatorSet.isRunning()) {
            return;
        }
        this.mLoadingAnim.start();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mButtonState == LOADING_STATE) {
            this.mLoadingAnim.cancel();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int saveCount;
        float loadingTextX;
        float dotsX;
        int firstDotAlpha;
        int thirdDotAlpha;
        super.onDraw(canvas);
        if (this.mButtonState != LOADING_STATE || getPaint() == null) {
            return;
        }
        TextPaint paint = getPaint();
        int alpha = paint.getAlpha();
        int originalSaveCount = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        if (this.mShowLoadingText) {
            float loadingTextWidth = paint.measureText(this.mLoadingText);
            float dotsWidth = paint.measureText(this.mDots);
            if (loadingTextWidth + dotsWidth > (getMeasuredWidth() - getPaddingStart()) - getPaddingEnd()) {
                drawLoadingCircles(canvas, paint);
                saveCount = originalSaveCount;
            } else {
                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                float baseline = (((getMeasuredHeight() + (fontMetrics.bottom - fontMetrics.top)) / 2.0f) - fontMetrics.bottom) - ((getPaddingBottom() - getPaddingTop()) / 2);
                int secondDotAlpha = this.mSecondLoadingDotAlpha;
                if (isRtlMode()) {
                    loadingTextX = (((getMeasuredWidth() - loadingTextWidth) - dotsWidth) / 2.0f) + dotsWidth;
                    firstDotAlpha = this.mThirdLoadingDotAlpha;
                    thirdDotAlpha = this.mFirstLoadingDotAlpha;
                    dotsX = ((getMeasuredWidth() - loadingTextWidth) - dotsWidth) / 2.0f;
                } else {
                    loadingTextX = ((getMeasuredWidth() - loadingTextWidth) - dotsWidth) / 2.0f;
                    dotsX = loadingTextWidth + loadingTextX;
                    firstDotAlpha = this.mFirstLoadingDotAlpha;
                    thirdDotAlpha = this.mThirdLoadingDotAlpha;
                }
                canvas.drawText(this.mLoadingText, loadingTextX - ((getPaddingEnd() - getPaddingStart()) / 2), baseline, paint);
                paint.getTextBounds(this.mDots, 0, 1, this.mLoadingTextBounds);
                float dotsTextX = dotsX;
                saveCount = originalSaveCount;
                drawClipDot(canvas, dotsX, this.mLoadingTextBounds.right + dotsX, dotsTextX, baseline, paint, firstDotAlpha);
                int firstDotRight = this.mLoadingTextBounds.right;
                paint.getTextBounds(this.mDots, 0, 2, this.mLoadingTextBounds);
                drawClipDot(canvas, firstDotRight + dotsX, this.mLoadingTextBounds.right + dotsX, dotsTextX, baseline, paint, secondDotAlpha);
                drawClipDot(canvas, this.mLoadingTextBounds.right + dotsX, dotsX + dotsWidth, dotsTextX, baseline, paint, thirdDotAlpha);
            }
        } else {
            saveCount = originalSaveCount;
            drawLoadingCircles(canvas, paint);
        }
        paint.setAlpha(alpha);
        canvas.restoreToCount(saveCount);
    }

    public void resetButtonState() {
        if (this.mButtonState == LOADING_STATE) {
            this.mButtonState = DEFAULT_STATE;
            setText(this.mOriginalText);
            this.mLoadingAnim.cancel();
            this.mFirstLoadingDotAlpha = 51;
            this.mSecondLoadingDotAlpha = 51;
            this.mThirdLoadingDotAlpha = 51;
            OnLoadingStateChangeListener onLoadingStateChangeListener = this.mOnLoadingStateChangeListener;
            if (onLoadingStateChangeListener != null) {
                onLoadingStateChangeListener.OnLoadingStateChanged(this.mButtonState);
            }
        }
    }

    public void setLoadingText(String loadingText) {
        if (loadingText == null || !this.mShowLoadingText) {
            return;
        }
        this.mLoadingText = loadingText;
    }

    public void setOnLoadingStateChangeListener(OnLoadingStateChangeListener onLoadingStateChangeListener) {
        this.mOnLoadingStateChangeListener = onLoadingStateChangeListener;
    }

    public void setOriginalText(String originalText) {
        this.mOriginalText = originalText;
    }

    public void setShowLoadingText(boolean showLoadingText) {
        this.mShowLoadingText = showLoadingText;
    }

    public void switchToLoadingState() {
        if (this.mLoadingAnim == null) {
            initTextChangeListener();
            initAnim();
        }
        if (this.mButtonState == DEFAULT_STATE) {
            this.mButtonState = LOADING_STATE;
            setText("");
            this.mLoadingAnim.start();
            OnLoadingStateChangeListener onLoadingStateChangeListener = this.mOnLoadingStateChangeListener;
            if (onLoadingStateChangeListener != null) {
                onLoadingStateChangeListener.OnLoadingStateChanged(this.mButtonState);
            }
        }
    }

    public COUILoadingButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, androidx.appcompat.R.attr.buttonStyle);
    }

    public COUILoadingButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mButtonState = DEFAULT_STATE;
        this.mLoadingText = "";
        this.mLoadingTextBounds = new Rect();
        this.mFirstLoadingDotAlpha = 51;
        this.mSecondLoadingDotAlpha = 51;
        this.mThirdLoadingDotAlpha = 51;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUIButton, defStyleAttr, 0);
        boolean showLoadingText = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIButton_isShowLoadingText, false);
        this.mShowLoadingText = showLoadingText;
        if (showLoadingText) {
            String string = typedArrayObtainStyledAttributes.getString(R.styleable.COUIButton_loadingText);
            this.mLoadingText = string;
            if (string == null) {
                this.mLoadingText = "";
            }
        }
        typedArrayObtainStyledAttributes.recycle();
        this.mOriginalText = getText().toString();
        this.mDots = context.getString(R.string.loading_button_dots);
        float dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_loading_btn_circle_radius);
        this.mLoadingCircleRadius = dimensionPixelOffset;
        float dimensionPixelOffset2 = context.getResources().getDimensionPixelOffset(R.dimen.coui_loading_btn_circle_spacing);
        this.mLoadingCircleSpacing = dimensionPixelOffset2;
        this.mLoadingCircleTotalWidth = (dimensionPixelOffset * 6.0f) + (dimensionPixelOffset2 * 2.0f);
    }
}
