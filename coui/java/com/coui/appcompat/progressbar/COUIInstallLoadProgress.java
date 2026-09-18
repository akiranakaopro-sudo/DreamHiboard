package com.coui.appcompat.progressbar;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.oplus.graphics.OplusPathAdapter;

import java.util.Locale;

public class COUIInstallLoadProgress extends COUILoadProgress {
    private static final String CIRCLE_BRIGHTNESS_HOLDER = "circleBrightnessHolder";
    private static final String CIRCLE_IN_ALPHA_HOLDER = "circleInAlphaHolder";
    private static final String CIRCLE_OUT_ALPHA_HOLDER = "circleOutAlphaHolder";
    private static final String CIRCLE_RADIUS_HOLDER = "circleRadiusHolder";
    private static final float DEFAULT_BRIGHTNESS_MAX_VALUE = 0.8f;
    private static final float DEFAULT_MIN_PRESS_FEEDBACK = 0.005f;
    private static final float DEFAULT_NARROW_FINAL_VALUE = 0.92f;
    private static final float DEFAULT_SCALE_PARAMETER = 0.05f;
    private static final String HOLDER_BRIGHTNESS = "brightnessHolder";
    private static final String HOLDER_NARROW_FONT = "narrowHolderFont";
    private static final String HOLDER_NARROW_X = "narrowHolderX";
    private static final String HOLDER_NARROW_Y = "narrowHolderY";
    public static final int LOAD_STYLE_DEFAULT = 0;
    public static final int LOAD_STYLE_BIG_ROUND = 1;
    public static final int LOAD_STYLE_CIRCLE = 2;
    private static final int NORMAL_ANIMATOR_DURATION = 340;
    private static final int PRESS_ANIMATOR_DURATION = 200;
    private static final float ONE_POINT_FIVE = 1.5f;

    private String mApostrophe;
    private int mBtnTextColor;
    private boolean mBtnTextColorChanged;
    private ColorStateList mBtnTextColorStateList;
    private Drawable mCircleLoadDrawable;
    private Drawable mCirclePauseDrawable;
    private Drawable mCircleReloadDrawable;
    private Paint mCirclePaint;
    private int mColorPrimary;
    private int mColorSecondary;
    private int mColorWhite;
    private float mCurrentBrightness = 1f;
    private float mCurrentCircleRadius;
    private int mCurrentInBitmapAlpha = 255;
    private int mCurrentOutBitmapAlpha;
    private int mCurrentRoundBorderRadius;
    private int mDefaultCircleRadius;
    private ColorStateList mDefaultTextColor;
    private int mDefaultTextSize;
    private int mDefaultWidth;
    private int mDisabledColor;
    private String mDownloadingContentDecrpition;
    private int mExpandOffsetX;
    private int mExpandOffsetY;
    private Paint.FontMetricsInt mFmi;
    private boolean mHasBrightness;
    private Paint mInBitmapPaint;
    private boolean mIsChangeTextColor;
    private boolean mIsNeedVibrate;
    private int mLoadStyle = LOAD_STYLE_DEFAULT;
    private Locale mLocale = Locale.getDefault();
    private float mMaxBrightness = DEFAULT_BRIGHTNESS_MAX_VALUE;
    private float mNarrowOffsetFont = 1f;
    private Interpolator mNormalAnimationInterpolator;
    private ValueAnimator mNormalAnimator;
    private Paint mOutBitmapPaint;
    private Interpolator mPressAnimationInterpolator;
    private ValueAnimator mPressedAnimator;
    private float mRadiusOffset;
    private Paint mRoundRectPaint;
    private final Path mRoundRectPath = new Path();
    private final OplusPathAdapter mOplusPath = new OplusPathAdapter(mRoundRectPath,
            OplusPathAdapter.NEW_PATH_SMOOTH);
    private int mSpace;
    private int mStyle;
    private int mSurpassProgressColor;
    private int mTextColor;
    private int mTextPadding;
    private TextPaint mTextPaint;
    private String mTextView;
    private int mThemeColor;
    private ColorStateList mThemeColorStateList;
    private int mThemeSecondaryColor;
    private ColorStateList mThemeSecondaryColorStateList;
    private int mTouchModeCircleRadius;
    private int mTouchModeHeight;
    private int mTouchModeWidth;
    private int mUserTextColor = -1;
    private int mUserTextSize;

    public COUIInstallLoadProgress(Context context) {
        this(context, null);
    }

    public COUIInstallLoadProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiInstallLoadProgressStyle);
    }

    public COUIInstallLoadProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_COUILoadProgress_InstallDownload);
    }

    public COUIInstallLoadProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mStyle = attrs == null || attrs.getStyleAttribute() == 0 ? defStyleAttr : attrs.getStyleAttribute();
        mColorPrimary = COUIContextUtil.getAttrColor(context, R.attr.couiColorPrimary, 0);
        mColorSecondary = COUIContextUtil.getAttrColor(context, R.attr.couiColorSecondary, 0);
        mColorWhite = getResources().getColor(R.color.coui_install_load_progress_text_color_in_progress);
        mSurpassProgressColor = mColorWhite;
        mPressAnimationInterpolator = new COUIMoveEaseInterpolator();
        mNormalAnimationInterpolator = new COUIMoveEaseInterpolator();
        TypedArray loadAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUILoadProgress,
                defStyleAttr, defStyleRes);
        mIsNeedVibrate = loadAttrs.getBoolean(
                R.styleable.COUILoadProgress_loadingButtonNeedVibrate, false);
        Drawable buttonDrawable = loadAttrs.getDrawable(R.styleable.COUILoadProgress_couiDefaultDrawable);
        if (buttonDrawable != null) {
            setButtonDrawable(buttonDrawable);
        }
        setState(loadAttrs.getInteger(R.styleable.COUILoadProgress_couiState, DEFAULT_UP_OR_DOWN));
        setProgress(loadAttrs.getInt(R.styleable.COUILoadProgress_couiProgress, mProgress), false);
        loadAttrs.recycle();

        int defaultTextSize = getResources().getDimensionPixelSize(
                R.dimen.coui_install_download_progress_textsize);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIInstallLoadProgress,
                defStyleAttr, defStyleRes);
        setLoadStyle(a.getInteger(R.styleable.COUIInstallLoadProgress_couiStyle, LOAD_STYLE_DEFAULT));
        mTouchModeHeight = a.getDimensionPixelSize(
                R.styleable.COUIInstallLoadProgress_couiInstallViewHeight,
                getResources().getDimensionPixelSize(R.dimen.coui_install_download_progress_height));
        mTouchModeWidth = a.getDimensionPixelOffset(
                R.styleable.COUIInstallLoadProgress_couiInstallViewWidth,
                getResources().getDimensionPixelOffset(R.dimen.coui_install_download_progress_width));
        mDefaultWidth = getDefaultSize(mTouchModeWidth, ONE_POINT_FIVE, false);
        mMaxBrightness = a.getFloat(R.styleable.COUIInstallLoadProgress_brightness,
                DEFAULT_BRIGHTNESS_MAX_VALUE);
        mDisabledColor = a.getColor(R.styleable.COUIInstallLoadProgress_disabledColor,
                getResources().getColor(R.color.coui_button_neutral_color_disable));
        if (mLoadStyle != LOAD_STYLE_CIRCLE) {
            mCurrentRoundBorderRadius = getResources().getDimensionPixelSize(
                    mLoadStyle == LOAD_STYLE_BIG_ROUND
                            ? R.dimen.coui_install_download_progress_round_border_radius
                            : R.dimen.coui_install_download_progress_round_border_radius_small);
            if (mLoadStyle == LOAD_STYLE_DEFAULT && !isZhLanguage(mLocale)) {
                int extra = getResources().getDimensionPixelSize(
                        R.dimen.coui_install_download_progress_width_in_foreign_language);
                mTouchModeWidth += extra;
                mDefaultWidth += extra;
            }
            mDefaultTextColor = a.getColorStateList(
                    R.styleable.COUIInstallLoadProgress_couiInstallDefaultColor);
            mTextPadding = a.getDimensionPixelOffset(
                    R.styleable.COUIInstallLoadProgress_couiInstallPadding, 0);
            mTextView = a.getString(R.styleable.COUIInstallLoadProgress_couiInstallTextview);
            if (mTextView == null) {
                mTextView = getResources().getString(R.string.coui_install_download_progress_textview);
            }
            mDefaultTextSize = a.getDimensionPixelSize(
                    R.styleable.COUIInstallLoadProgress_couiInstallTextsize, defaultTextSize);
            mDefaultTextSize = (int) COUIChangeTextUtil.getSuitableFontSize(mDefaultTextSize,
                    getResources().getConfiguration().fontScale, 2);
            mApostrophe = getResources().getString(R.string.coui_install_load_progress_apostrophe);
        } else {
            mCurrentRoundBorderRadius = getResources().getDimensionPixelSize(
                    R.dimen.coui_install_download_progress_circle_round_border_radius);
        }
        setThemeColorStateList(a.getColorStateList(R.styleable.COUIInstallLoadProgress_couiThemeColor));
        setThemeSecondaryColorStateList(a.getColorStateList(
                R.styleable.COUIInstallLoadProgress_couiThemeColorSecondary));
        setBtnTextColorStateList(a.getColorStateList(
                R.styleable.COUIInstallLoadProgress_couiThemeTextColor));
        a.recycle();
        mRadiusOffset = getResources().getDimension(
                R.dimen.coui_install_download_progress_round_border_radius_offset);
        initText();
    }

    private void addApostrophe() {
        if (mTextView == null || mTextPaint == null) {
            return;
        }
        String displayText = getDisplayText(mTextView, mDefaultWidth);
        if (displayText.length() > 0 && displayText.length() < mTextView.length()) {
            int width = (mDefaultWidth - (mTextPadding * 2))
                    - ((int) mTextPaint.measureText(mApostrophe));
            mTextView = isEnglish(getDisplayText(displayText, width)) + mApostrophe;
        }
    }

    private void cancelAnim(ValueAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private int dip2px(float dp) {
        return (int) ((dp * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int getCurrentColor(int color) {
        if (!isEnabled()) {
            return mDisabledColor;
        }
        return Color.argb(Color.alpha(color),
                Math.min(255, (int) (Color.red(color) * mCurrentBrightness)),
                Math.min(255, (int) (Color.green(color) * mCurrentBrightness)),
                Math.min(255, (int) (Color.blue(color) * mCurrentBrightness)));
    }

    private int getDefaultSize(int size, float offsetDp, boolean singleSide) {
        return size - (singleSide ? dip2px(offsetDp) : dip2px(offsetDp) * 2);
    }

    private String getDisplayText(String text, int width) {
        int count = mTextPaint.breakText(text, true, width, null);
        return count == 0 || count == text.length() ? text : text.substring(0, count - 1);
    }

    private void initText() {
        if (mLoadStyle == LOAD_STYLE_CIRCLE) {
            return;
        }
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        int textSize = mUserTextSize == 0 ? mDefaultTextSize : mUserTextSize;
        mTextColor = mUserTextColor;
        if (mTextColor == -1) {
            mTextColor = mDefaultTextColor == null ? mColorPrimary
                    : mDefaultTextColor.getColorForState(getDrawableState(), mColorPrimary);
        }
        mTextPaint.setTextSize(textSize);
        COUIChangeTextUtil.adaptBoldAndMediumFont(mTextPaint, true);
        mFmi = mTextPaint.getFontMetricsInt();
        addApostrophe();
    }

    private static boolean isChinese(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.toString(text.charAt(i)).matches("^[\\u4e00-\\u9fa5]{1}$")) {
                return true;
            }
        }
        return false;
    }

    private String isEnglish(String text) {
        int lastSpace = text.lastIndexOf(' ');
        return isChinese(text) || lastSpace <= 0 ? text : text.substring(0, lastSpace);
    }

    private boolean isRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private boolean isZhLanguage(Locale locale) {
        return "zh".equalsIgnoreCase(locale.getLanguage());
    }

    private void onDrawButtonDrawable(Canvas canvas, float left, float top, float width, float height) {
        if (mButtonDrawable == null) {
            return;
        }
        int intrinsicWidth = mButtonDrawable.getIntrinsicWidth();
        int intrinsicHeight = mButtonDrawable.getIntrinsicHeight();
        int drawableLeft = ((int) (width - intrinsicWidth)) / 2;
        int drawableTop = ((int) (height - intrinsicHeight)) / 2;
        int drawableRight = drawableLeft + intrinsicWidth;
        int drawableBottom = drawableTop + intrinsicHeight;
        mButtonDrawable.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        mButtonDrawable.setColorFilter(mTextColor, PorterDuff.Mode.SRC_IN);
        mButtonDrawable.draw(canvas);
        if (mIsChangeTextColor && mButtonDrawableReverseColor != null) {
            canvas.save();
            mButtonDrawableReverseColor.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
            mButtonDrawableReverseColor.setColorFilter(mSurpassProgressColor, PorterDuff.Mode.SRC_IN);
            if (isRtl()) {
                canvas.clipRect(width - mSpace, top, width, height);
            } else {
                canvas.clipRect(left, top, mSpace, height);
            }
            mButtonDrawableReverseColor.draw(canvas);
            canvas.restore();
            mIsChangeTextColor = false;
        }
    }

    private void onDrawCircle(Canvas canvas, boolean primary, Drawable inDrawable, Drawable outDrawable) {
        if (mCirclePaint == null) {
            return;
        }
        mCirclePaint.setColor(primary
                ? getCurrentColor(mThemeColorStateList == null ? mColorPrimary : mThemeColor)
                : getCurrentColor(mThemeSecondaryColorStateList == null ? mColorSecondary : mThemeSecondaryColor));
        canvas.save();
        canvas.clipPath(mRoundRectPath);
        canvas.drawColor(mCirclePaint.getColor());
        drawCenteredDrawable(canvas, inDrawable, mCurrentInBitmapAlpha, primary ? mColorWhite : mColorPrimary);
        drawCenteredDrawable(canvas, outDrawable, mCurrentOutBitmapAlpha, mColorWhite);
        canvas.restore();
    }

    private void drawCenteredDrawable(Canvas canvas, Drawable drawable, int alpha, int tint) {
        if (drawable == null) {
            return;
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        int left = (mTouchModeWidth - width) / 2;
        int top = (mTouchModeHeight - height) / 2;
        drawable.setBounds(left, top, left + width, top + height);
        drawable.setAlpha(alpha);
        drawable.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        drawable.draw(canvas);
        drawable.setAlpha(255);
    }

    private void onDrawRoundRect(Canvas canvas, float left, float top, float right, float bottom,
            boolean primary, float translateX, float translateY) {
        canvas.translate(translateX, translateY);
        mRoundRectPaint.setColor(primary
                ? getCurrentColor(mThemeColorStateList == null ? mColorPrimary : mThemeColor)
                : getCurrentColor(mThemeSecondaryColorStateList == null ? mColorSecondary : mThemeSecondaryColor));
        canvas.drawPath(mRoundRectPath, mRoundRectPaint);
        canvas.translate(-translateX, -translateY);
    }

    private void onDrawText(Canvas canvas, float left, float top, float width, float height) {
        if (mTextView == null || mTextPaint == null) {
            return;
        }
        mTextPaint.setTextSize(mDefaultTextSize * mNarrowOffsetFont);
        float measured = mTextPaint.measureText(mTextView);
        float x = mTextPadding + (((width - measured) - (mTextPadding * 2f)) / 2f);
        float y = ((height - mFmi.descent) - mFmi.ascent) / 2f;
        canvas.drawText(mTextView, x, y, mTextPaint);
        if (mIsChangeTextColor) {
            mTextPaint.setColor(mSurpassProgressColor);
            canvas.save();
            if (isRtl()) {
                canvas.clipRect(width - mSpace, top, width, height);
            } else {
                canvas.clipRect(left, top, mSpace, height);
            }
            canvas.drawText(mTextView, x, y, mTextPaint);
            canvas.restore();
            mIsChangeTextColor = false;
        }
    }

    private void performHapticFeedbackIfNeeded() {
        if (mIsNeedVibrate) {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
        }
    }

    private void performTouchEndAnim(final boolean click) {
        performHapticFeedbackIfNeeded();
        if (!mHasBrightness) {
            return;
        }
        cancelAnim(mPressedAnimator);
        if (mLoadStyle == LOAD_STYLE_CIRCLE) {
            mNormalAnimator = ValueAnimator.ofPropertyValuesHolder(
                    PropertyValuesHolder.ofFloat(CIRCLE_RADIUS_HOLDER, mCurrentCircleRadius,
                            mDefaultCircleRadius),
                    PropertyValuesHolder.ofFloat(CIRCLE_BRIGHTNESS_HOLDER, mCurrentBrightness, 1f),
                    PropertyValuesHolder.ofInt(CIRCLE_IN_ALPHA_HOLDER, 0, 255),
                    PropertyValuesHolder.ofInt(CIRCLE_OUT_ALPHA_HOLDER, 255, 0));
        } else {
            mNormalAnimator = ValueAnimator.ofPropertyValuesHolder(
                    PropertyValuesHolder.ofFloat(HOLDER_BRIGHTNESS, mCurrentBrightness, 1f),
                    PropertyValuesHolder.ofFloat(HOLDER_NARROW_X, mExpandOffsetX, 0f),
                    PropertyValuesHolder.ofFloat(HOLDER_NARROW_Y, mExpandOffsetY, 0f),
                    PropertyValuesHolder.ofFloat(HOLDER_NARROW_FONT, mNarrowOffsetFont, 1f));
        }
        mNormalAnimator.setInterpolator(mNormalAnimationInterpolator);
        mNormalAnimator.setDuration(NORMAL_ANIMATOR_DURATION);
        mNormalAnimator.addUpdateListener(animation -> {
            if (mLoadStyle == LOAD_STYLE_CIRCLE) {
                mCurrentCircleRadius = (Float) animation.getAnimatedValue(CIRCLE_RADIUS_HOLDER);
                mCurrentBrightness = (Float) animation.getAnimatedValue(CIRCLE_BRIGHTNESS_HOLDER);
                mCurrentInBitmapAlpha = (Integer) animation.getAnimatedValue(CIRCLE_IN_ALPHA_HOLDER);
                mCurrentOutBitmapAlpha = (Integer) animation.getAnimatedValue(CIRCLE_OUT_ALPHA_HOLDER);
            } else {
                mCurrentBrightness = (Float) animation.getAnimatedValue(HOLDER_BRIGHTNESS);
                mExpandOffsetX = Math.round((Float) animation.getAnimatedValue(HOLDER_NARROW_X));
                mExpandOffsetY = Math.round((Float) animation.getAnimatedValue(HOLDER_NARROW_Y));
                mNarrowOffsetFont = (Float) animation.getAnimatedValue(HOLDER_NARROW_FONT);
                updateRoundRectPath();
            }
            invalidate();
        });
        mNormalAnimator.start();
        if (click) {
            super.performClick();
        }
        mHasBrightness = false;
    }

    private void performTouchStartAnim() {
        if (mHasBrightness) {
            return;
        }
        cancelAnim(mNormalAnimator);
        if (mLoadStyle == LOAD_STYLE_CIRCLE) {
            mPressedAnimator = ValueAnimator.ofPropertyValuesHolder(
                    PropertyValuesHolder.ofFloat(CIRCLE_RADIUS_HOLDER, mCurrentCircleRadius,
                            mDefaultCircleRadius * 0.9f),
                    PropertyValuesHolder.ofFloat(CIRCLE_BRIGHTNESS_HOLDER, mCurrentBrightness,
                            mMaxBrightness));
        } else {
            mPressedAnimator = ValueAnimator.ofPropertyValuesHolder(
                    PropertyValuesHolder.ofFloat(HOLDER_BRIGHTNESS, 1f, mMaxBrightness),
                    PropertyValuesHolder.ofFloat(HOLDER_NARROW_X, 0f,
                            getMeasuredWidth() * DEFAULT_SCALE_PARAMETER),
                    PropertyValuesHolder.ofFloat(HOLDER_NARROW_Y, 0f,
                            getMeasuredHeight() * DEFAULT_SCALE_PARAMETER),
                    PropertyValuesHolder.ofFloat(HOLDER_NARROW_FONT, 1f, DEFAULT_NARROW_FINAL_VALUE));
        }
        mPressedAnimator.setInterpolator(mPressAnimationInterpolator);
        mPressedAnimator.setDuration(PRESS_ANIMATOR_DURATION);
        mPressedAnimator.addUpdateListener(animation -> {
            if (mLoadStyle == LOAD_STYLE_CIRCLE) {
                mCurrentCircleRadius = (Float) animation.getAnimatedValue(CIRCLE_RADIUS_HOLDER);
                mCurrentBrightness = (Float) animation.getAnimatedValue(CIRCLE_BRIGHTNESS_HOLDER);
            } else {
                mCurrentBrightness = (Float) animation.getAnimatedValue(HOLDER_BRIGHTNESS);
                float x = (Float) animation.getAnimatedValue(HOLDER_NARROW_X);
                float y = (Float) animation.getAnimatedValue(HOLDER_NARROW_Y);
                if (x < getMeasuredWidth() * DEFAULT_MIN_PRESS_FEEDBACK
                        && y < getMeasuredHeight() * DEFAULT_MIN_PRESS_FEEDBACK) {
                    x = getMeasuredWidth() * DEFAULT_MIN_PRESS_FEEDBACK;
                    y = getMeasuredHeight() * DEFAULT_MIN_PRESS_FEEDBACK;
                }
                mExpandOffsetX = Math.round(x);
                mExpandOffsetY = Math.round(y);
                mNarrowOffsetFont = (Float) animation.getAnimatedValue(HOLDER_NARROW_FONT);
                updateRoundRectPath();
            }
            invalidate();
        });
        mPressedAnimator.start();
        mHasBrightness = true;
    }

    private void updateRoundRectPath() {
        if (mLoadStyle == LOAD_STYLE_CIRCLE) {
            float cx = mTouchModeWidth / 2f;
            float cy = mTouchModeHeight / 2f;
            COUIShapePath.getRoundRectPath(mRoundRectPath,
                    new RectF(cx - mCurrentCircleRadius, cy - mCurrentCircleRadius,
                            cx + mCurrentCircleRadius, cy + mCurrentCircleRadius),
                    mCurrentRoundBorderRadius);
            return;
        }
        float left = mExpandOffsetX;
        float top = mExpandOffsetY;
        float right = getWidth() - mExpandOffsetX;
        float bottom = getHeight() - mExpandOffsetY;
        RectF rect = new RectF(left, top, right, bottom);
        mRoundRectPath.reset();
        float radius = ((bottom - top) / 2f) - mRadiusOffset;
        mOplusPath.addSmoothRoundRect(rect, radius, radius, Path.Direction.CCW);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return ProgressBar.class.getName();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mLoadStyle == LOAD_STYLE_CIRCLE) {
            mCircleLoadDrawable = getContext().getDrawable(R.drawable.coui_install_load_progress_circle_load);
            mCircleReloadDrawable = getContext().getDrawable(R.drawable.coui_install_load_progress_circle_reload);
            mCirclePauseDrawable = getContext().getDrawable(R.drawable.coui_install_load_progress_circle_pause);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Locale locale = Locale.getDefault();
        if (mLoadStyle != LOAD_STYLE_DEFAULT || mLocale.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
            return;
        }
        mLocale = locale;
        int extra = getResources().getDimensionPixelSize(
                R.dimen.coui_install_download_progress_width_in_foreign_language);
        if (isZhLanguage(mLocale)) {
            mTouchModeWidth -= extra;
            mDefaultWidth -= extra;
        } else {
            mTouchModeWidth += extra;
            mDefaultWidth += extra;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = mExpandOffsetX;
        float top = mExpandOffsetY;
        float right = getWidth() - mExpandOffsetX;
        float bottom = getHeight() - mExpandOffsetY;
        if (mState == UP_OR_DOWN_FAIL) {
            if (mLoadStyle == LOAD_STYLE_CIRCLE) {
                onDrawCircle(canvas, true, mCircleReloadDrawable, mCirclePauseDrawable);
                return;
            }
            onDrawRoundRect(canvas, left, top, right, bottom, true, 0f, 0f);
            mTextPaint.setColor(mBtnTextColorChanged ? mBtnTextColor : mColorWhite);
            mIsChangeTextColor = false;
            if (mButtonDrawable == null) {
                onDrawText(canvas, left, top, mTouchModeWidth, mTouchModeHeight);
            } else {
                onDrawButtonDrawable(canvas, left, top, mTouchModeWidth, mTouchModeHeight);
            }
            return;
        }
        if (mState == DEFAULT_UP_OR_DOWN) {
            if (mLoadStyle == LOAD_STYLE_CIRCLE) {
                onDrawCircle(canvas, false, mCircleLoadDrawable, mCirclePauseDrawable);
            } else if (mLoadStyle == LOAD_STYLE_BIG_ROUND) {
                onDrawRoundRect(canvas, left, top, right, bottom, true, 0f, 0f);
            } else {
                onDrawRoundRect(canvas, left, top, right, bottom, false, 0f, 0f);
            }
            if (mLoadStyle == LOAD_STYLE_BIG_ROUND) {
                mTextPaint.setColor(mBtnTextColorChanged ? mBtnTextColor : mColorWhite);
            } else if (mLoadStyle == LOAD_STYLE_DEFAULT) {
                mTextPaint.setColor(mBtnTextColorStateList == null ? mColorPrimary : mBtnTextColor);
            }
        }
        if (mState == UPING_OR_DOWNING || mState == UP_OR_DOWN_WAIT) {
            if (mLoadStyle == LOAD_STYLE_CIRCLE) {
                if (mState == UPING_OR_DOWNING) {
                    onDrawCircle(canvas, true, mCirclePauseDrawable, mCircleReloadDrawable);
                } else {
                    onDrawCircle(canvas, true, mCircleReloadDrawable, mCirclePauseDrawable);
                }
                return;
            }
            float progress = (mIsUpdateWithAnimation ? mVisualProgress : mProgress)
                    / Math.max(1f, mMax);
            mSpace = ((int) (progress * (mTouchModeWidth - (mExpandOffsetX * 2)))) + mExpandOffsetX;
            onDrawRoundRect(canvas, left, top, right, bottom, false, 0f, 0f);
            canvas.save();
            if (isRtl()) {
                canvas.clipRect(right - mSpace, top, right, mTouchModeHeight);
            } else {
                canvas.clipRect(left, top, mSpace, mTouchModeHeight);
            }
            onDrawRoundRect(canvas, left, top, right, bottom, true, 0f, 0f);
            canvas.restore();
            mIsChangeTextColor = true;
            mTextPaint.setColor(mBtnTextColorStateList == null ? mColorPrimary : mBtnTextColor);
        }
        if (mLoadStyle != LOAD_STYLE_CIRCLE) {
            if (mButtonDrawable == null) {
                onDrawText(canvas, left, top, mTouchModeWidth, mTouchModeHeight);
            } else {
                onDrawButtonDrawable(canvas, left, top, mTouchModeWidth, mTouchModeHeight);
            }
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setItemCount(mMax);
        event.setCurrentItemIndex(mProgress);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if ((mState == DEFAULT_UP_OR_DOWN || mState == UP_OR_DOWN_FAIL || mState == UP_OR_DOWN_WAIT)
                && mTextView != null) {
            info.setContentDescription(mTextView);
        } else if (mState == UPING_OR_DOWNING && mDownloadingContentDecrpition != null) {
            info.setContentDescription(mDownloadingContentDecrpition);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mTouchModeWidth, mTouchModeHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateRoundRectPath();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            performHapticFeedbackIfNeeded();
            performTouchStartAnim();
        } else if (action == MotionEvent.ACTION_UP) {
            boolean inside = event.getX() >= 0f && event.getX() <= mTouchModeWidth
                    && event.getY() >= 0f && event.getY() <= mTouchModeHeight;
            performTouchEndAnim(inside);
        } else if (action == MotionEvent.ACTION_CANCEL) {
            performTouchEndAnim(false);
        }
        return true;
    }

    public void refresh() {
        mColorPrimary = COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPrimary, 0);
        mColorSecondary = COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorSecondary, 0);
        TypedArray a = null;
        String type = getResources().getResourceTypeName(mStyle);
        if ("attr".equals(type)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUIInstallLoadProgress, mStyle, 0);
        } else if ("style".equals(type)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUIInstallLoadProgress, 0, mStyle);
        }
        if (a != null) {
            mDisabledColor = a.getColor(R.styleable.COUIInstallLoadProgress_disabledColor, 0);
            a.recycle();
        }
        invalidate();
    }

    @Deprecated
    public void setBtnTextColor(int color) {
        mBtnTextColor = color;
        mBtnTextColorChanged = true;
        invalidate();
    }

    public void setBtnTextColorBySurpassProgress(int color) {
        mSurpassProgressColor = color;
        invalidate();
    }

    public void setBtnTextColorStateList(ColorStateList colorStateList) {
        mBtnTextColorStateList = colorStateList;
        setBtnTextColor(colorStateList == null ? -1 : colorStateList.getDefaultColor());
    }

    public void setDefaultTextSize(int defaultTextSize) {
        mDefaultTextSize = defaultTextSize;
    }

    public void setDisabledColor(int disabledColor) {
        mDisabledColor = disabledColor;
    }

    public void setDownloadingContentDecrpition(String description) {
        mDownloadingContentDecrpition = description;
    }

    public void setFail() {
        setState(UP_OR_DOWN_FAIL);
    }

    public void setIsChangeTextColor(boolean changeTextColor) {
        mIsChangeTextColor = changeTextColor;
    }

    public void setIsCircleMode(boolean circleMode) {
        setLoadStyle(circleMode ? LOAD_STYLE_CIRCLE : LOAD_STYLE_DEFAULT);
        requestLayout();
    }

    public void setIsNeedToShowCircle(boolean showCircle) {
        setIsCircleMode(showCircle);
    }

    public void setIsNeedVibrate(boolean needVibrate) {
        mIsNeedVibrate = needVibrate;
    }

    public void setLoadStyle(int loadStyle) {
        mLoadStyle = loadStyle;
        if (loadStyle == LOAD_STYLE_CIRCLE) {
            mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOutBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTouchModeCircleRadius = getResources().getDimensionPixelSize(
                    R.dimen.coui_install_download_progress_default_circle_radius);
            mDefaultCircleRadius = getDefaultSize(mTouchModeCircleRadius, ONE_POINT_FIVE, true);
            mCurrentCircleRadius = mDefaultCircleRadius;
            mCircleLoadDrawable = getContext().getDrawable(R.drawable.coui_install_load_progress_circle_load);
            mCircleReloadDrawable = getContext().getDrawable(R.drawable.coui_install_load_progress_circle_reload);
            mCirclePauseDrawable = getContext().getDrawable(R.drawable.coui_install_load_progress_circle_pause);
        } else {
            mRoundRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
    }

    public void setLoading() {
        setState(UPING_OR_DOWNING);
    }

    public void setMaxBrightness(int maxBrightness) {
        mMaxBrightness = maxBrightness;
    }

    public void setSuccess() {
        setState(INSTALL_HAVE_GIFT);
    }

    public void setText(String text) {
        if (text == null || text.equals(mTextView)) {
            return;
        }
        mTextView = text;
        if (mTextPaint != null) {
            addApostrophe();
        }
        invalidate();
    }

    @Override
    public void setTextColor(int color) {
        if (color != 0) {
            mUserTextColor = color;
        }
    }

    public void setTextId(int resId) {
        setText(getResources().getString(resId));
    }

    public void setTextPadding(int padding) {
        mTextPadding = padding;
    }

    public void setTextSize(int textSize) {
        if (textSize != 0) {
            mUserTextSize = textSize;
        }
    }

    @Deprecated
    public void setThemeColor(int color) {
        mThemeColor = color;
        invalidate();
    }

    public void setThemeColorStateList(ColorStateList colorStateList) {
        mThemeColorStateList = colorStateList;
        setThemeColor(colorStateList == null ? mColorPrimary : colorStateList.getDefaultColor());
    }

    @Deprecated
    public void setThemeSecondaryColor(int color) {
        mThemeSecondaryColor = color;
        invalidate();
    }

    public void setThemeSecondaryColorStateList(ColorStateList colorStateList) {
        mThemeSecondaryColorStateList = colorStateList;
        setThemeSecondaryColor(colorStateList == null ? mColorSecondary : colorStateList.getDefaultColor());
    }

    public void setTouchModeHeight(int height) {
        mTouchModeHeight = height;
    }

    public void setTouchModeWidth(int width) {
        mTouchModeWidth = width;
        mDefaultWidth = getDefaultSize(width, ONE_POINT_FIVE, false);
        if (mTextPaint != null) {
            addApostrophe();
        }
        requestLayout();
    }
}
