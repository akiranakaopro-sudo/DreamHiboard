package com.coui.appcompat.couiswitch;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Switch;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.soundloadutil.COUIAsyncSoundUtil;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.state.COUIStrokeDrawable;
import com.coui.appcompat.state.StateEffectAnimator;
import com.coui.appcompat.uiutil.ShadowUtils;
import com.oplus.graphics.OplusOutlineAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class COUISwitch extends SwitchCompat {
    private static final int ALPHA_VALUE_30 = 0x4DFFFFFF;
    private static final String TAG = "COUISwitch";

    private int mBarCheckedColor;
    private int mBarCheckedDisabledColor;
    private int mBarHeight;
    private int mBarTrackCurrentColor;
    private int mBarUnCheckedColor;
    private int mBarUncheckedDisabledColor;
    private Drawable mCheckedDrawable;
    private int mCirclePadding;
    private float mCircleScale = 1.0f;
    private float mCircleScaleX = 1.0f;
    private int mCircleTranslation;
    private int mDefaultTranslation;
    private boolean mEnableHapticFeedback;
    // Leapy added: OPPO guards delayed switch feedback with an atomic flag.
    private AtomicBoolean mFeedBackSwitch;
    private StateEffectAnimator mHoverAnimator;
    private float mInnerCircleAlpha;
    private int mInnerCircleCheckedDisabledColor;
    private int mInnerCircleColor;
    private Paint mInnerCirclePaint;
    private RectF mInnerCircleRectF;
    private int mInnerCircleUncheckedDisabledColor;
    private int mInnerCircleWidth;
    private boolean mIsAttachedToWindow;
    private boolean mIsLoading;
    private boolean mIsLoadingStyle;
    private boolean mIsMeasured;
    private boolean mIsThemedEnabled;
    private float mLoadingAlpha;
    private Drawable mLoadingDrawable;
    private float mLoadingRotation;
    private float mLoadingScale;
    private AccessibilityManager mManager;
    private OnLoadingStateChangedListener mOnLoadingStateChangedListener;
    private int mOuterCircleCheckedDisabledColor;
    private int mOuterCircleColor;
    private Paint mOuterCirclePaint;
    private RectF mOuterCircleRectF;
    private int mOuterCircleStrokeWidth;
    private int mOuterCircleUnCheckedColor;
    private int mOuterCircleUncheckedDisabledColor;
    private int mOuterCircleWidth;
    private int mPadding;
    private StateEffectAnimator mPressAnimator;
    private boolean mShouldPlaySound;
    private AnimatorSet mStartLoadingAnimator;
    private COUIStateEffectDrawable mStateEffectBackground;
    private AnimatorSet mStopLoadingAnimator;
    private COUIStrokeDrawable mStrokeDrawable;
    private int mStyle;
    private String mSwitchLoadingStr;
    private String mSwitchOffStr;
    private String mSwitchOnStr;
    private final RectF mSwitchRect = new RectF();
    private AnimatorSet mThemedLoadingAnimator;
    private Drawable mThemedLoadingCheckedBackground;
    private Drawable mThemedLoadingDrawable;
    private Drawable mThemedLoadingUncheckedBackground;
    private AnimatorSet mToggleAnimator = new AnimatorSet();
    private Drawable mUncheckedDrawable;
    private ExecutorService mVibratorExecutor;

    public interface OnLoadingStateChangedListener {
        void onStartLoading();
        void onStopLoading();
    }

    public COUISwitch(Context context) {
        this(context, null);
    }

    public COUISwitch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiSwitchStyle);
    }

    public COUISwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOuterCircleRectF = new RectF();
        mInnerCircleRectF = new RectF();
        mFeedBackSwitch = new AtomicBoolean(false);
        setSoundEffectsEnabled(false);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        // Leapy modified: Preserve OPPO's attribute-based style and exact lookup path.
        mStyle = attrs != null && attrs.getStyleAttribute() != 0
                ? attrs.getStyleAttribute() : defStyleAttr;
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.COUISwitch, defStyleAttr, 0);
        initAttr(a, context);
        a.recycle();
        initAnimator();
        initPaint();
        initResValue(context);
        initStateEffectBackground();
        configStateEffectAnimator();
        initOutLine();
    }

    private void animateWhenStateChanged(boolean checked) {
        if (mToggleAnimator == null) {
            mToggleAnimator = new AnimatorSet();
        }
        Interpolator interpolator = new PathInterpolator(0.3f, 0.0f, 0.1f, 1.0f);
        int targetTranslation;
        if (isRtlMode()) {
            targetTranslation = checked ? 0 : mDefaultTranslation;
        } else {
            targetTranslation = checked ? mDefaultTranslation : 0;
        }
        mToggleAnimator.setInterpolator(interpolator);
        // Leapy modified: OPPO smali stretches the thumb to 1.3f during a toggle.
        ValueAnimator scaleUp = ValueAnimator.ofFloat(1.0f, 1.3f);
        scaleUp.addUpdateListener(animation -> setCircleScaleX((Float) animation.getAnimatedValue()));
        scaleUp.setDuration(133L);
        ValueAnimator scaleDown = ValueAnimator.ofFloat(1.3f, 1.0f);
        scaleDown.addUpdateListener(animation -> setCircleScaleX((Float) animation.getAnimatedValue()));
        scaleDown.setStartDelay(133L);
        scaleDown.setDuration(250L);
        ValueAnimator translation = ValueAnimator.ofInt(mCircleTranslation, targetTranslation);
        translation.addUpdateListener(animation -> setCircleTranslation((Integer) animation.getAnimatedValue()));
        translation.setDuration(383L);
        ValueAnimator innerAlpha = ValueAnimator.ofFloat(mInnerCircleAlpha, checked ? 0.0f : 1.0f);
        innerAlpha.addUpdateListener(animation -> setInnerCircleAlpha((Float) animation.getAnimatedValue()));
        innerAlpha.setDuration(100L);
        ValueAnimator barColor = ValueAnimator.ofArgb(getBarColor(),
                checked ? mBarCheckedColor : mBarUnCheckedColor);
        barColor.addUpdateListener(animation -> setBarColor((Integer) animation.getAnimatedValue()));
        barColor.setDuration(450L);
        mToggleAnimator.play(scaleUp).with(scaleDown).with(translation).with(innerAlpha).with(barColor);
        mToggleAnimator.start();
    }

    private Drawable backgroundDrawable() {
        if (isLoading()) {
            return isChecked() ? mThemedLoadingCheckedBackground : mThemedLoadingUncheckedBackground;
        }
        return isChecked() ? mCheckedDrawable : mUncheckedDrawable;
    }

    private void configStateEffectAnimator() {
        mHoverAnimator = new StateEffectAnimator(this, "hover", 0, COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorHover));
        mPressAnimator = new StateEffectAnimator(this, "press", 0, COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPress));
        mHoverAnimator.setSpringResponse(0.3f);
        mHoverAnimator.setSpringBounce(0.0f);
        mPressAnimator.setSpringResponse(0.3f);
        mPressAnimator.setSpringBounce(0.0f);
    }

    private void drawBar() {
        Drawable trackDrawable = getTrackDrawable();
        if (trackDrawable == null) {
            return;
        }
        if (isEnabled()) {
            int hoverColor = ColorUtils.compositeColors(mHoverAnimator.getCurrentMaskColor(), mBarTrackCurrentColor);
            trackDrawable.setTint(ColorUtils.compositeColors(mPressAnimator.getCurrentMaskColor(), hoverColor));
        } else {
            trackDrawable.setTint(isChecked() ? mBarCheckedDisabledColor : mBarUncheckedDisabledColor);
        }
    }

    private void drawLoading(Canvas canvas) {
        if (!mIsLoading || mLoadingDrawable == null) {
            return;
        }
        canvas.save();
        canvas.scale(mLoadingScale, mLoadingScale, mOuterCircleRectF.centerX(), mOuterCircleRectF.centerY());
        canvas.rotate(mLoadingRotation, mOuterCircleRectF.centerX(), mOuterCircleRectF.centerY());
        mLoadingDrawable.setBounds((int) mOuterCircleRectF.left, (int) mOuterCircleRectF.top,
                (int) mOuterCircleRectF.right, (int) mOuterCircleRectF.bottom);
        mLoadingDrawable.setAlpha((int) (mLoadingAlpha * 255.0f));
        mLoadingDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawOuterCircle(Canvas canvas) {
        canvas.save();
        canvas.scale(mCircleScale, mCircleScale, mOuterCircleRectF.centerX(), mOuterCircleRectF.centerY());
        mOuterCirclePaint.setColor(isChecked() ? mOuterCircleColor : mOuterCircleUnCheckedColor);
        if (!isEnabled()) {
            mOuterCirclePaint.setColor(isChecked() ? mOuterCircleCheckedDisabledColor : mOuterCircleUncheckedDisabledColor);
        }
        float radius = mOuterCircleWidth / 2.0f;
        canvas.drawRoundRect(mOuterCircleRectF, radius, radius, mOuterCirclePaint);
        canvas.restore();
    }

    private void drawThemedBackground(Canvas canvas) {
        Drawable drawable = backgroundDrawable();
        if (drawable == null) {
            return;
        }
        canvas.save();
        drawable.setAlpha(drawableAlpha());
        drawable.setBounds(mPadding, mPadding, getSwitchMinWidth() + mPadding, mBarHeight + mPadding);
        drawable.draw(canvas);
        canvas.restore();
    }

    private void drawThemedLoading(Canvas canvas) {
        if (!mIsLoading || mThemedLoadingDrawable == null) {
            return;
        }
        int left = (getWidth() - mOuterCircleWidth) / 2;
        int right = (getWidth() + mOuterCircleWidth) / 2;
        int top = (getHeight() - mOuterCircleWidth) / 2;
        int bottom = (getHeight() + mOuterCircleWidth) / 2;
        canvas.save();
        canvas.rotate(mLoadingRotation, getWidth() / 2.0f, getHeight() / 2.0f);
        mThemedLoadingDrawable.setBounds(left, top, right, bottom);
        mThemedLoadingDrawable.draw(canvas);
        canvas.restore();
    }

    private int drawableAlpha() {
        return (int) ((isEnabled() ? 1.0f : 0.5f) * 255.0f);
    }

    private int getBarColor() {
        return mBarTrackCurrentColor;
    }

    private void initAnimator() {
        initStartLoadingAnimator();
        initStopLoadingAnimator();
        initThemedLoadingAnimator();
    }

    private void initAttr(TypedArray a, Context context) {
        mLoadingDrawable = a.getDrawable(R.styleable.COUISwitch_loadingDrawable);
        mBarHeight = a.getDimensionPixelSize(R.styleable.COUISwitch_barHeight, 0);
        mOuterCircleStrokeWidth = a.getDimensionPixelSize(R.styleable.COUISwitch_outerCircleStrokeWidth, 0);
        mOuterCircleWidth = a.getDimensionPixelOffset(R.styleable.COUISwitch_outerCircleWidth, 0);
        mInnerCircleWidth = a.getDimensionPixelSize(R.styleable.COUISwitch_innerCircleWidth, 0);
        mCirclePadding = a.getDimensionPixelSize(R.styleable.COUISwitch_circlePadding, 0);
        mInnerCircleColor = a.getColor(R.styleable.COUISwitch_innerCircleColor, 0);
        mOuterCircleColor = a.getColor(R.styleable.COUISwitch_outerCircleColor, 0);
        mInnerCircleUncheckedDisabledColor = a.getColor(R.styleable.COUISwitch_innerCircleUncheckedDisabledColor, 0);
        mOuterCircleUnCheckedColor = a.getColor(R.styleable.COUISwitch_outerUnCheckedCircleColor, 0);
        mInnerCircleCheckedDisabledColor = a.getColor(R.styleable.COUISwitch_innerCircleCheckedDisabledColor, 0);
        mOuterCircleUncheckedDisabledColor = a.getColor(R.styleable.COUISwitch_outerCircleUncheckedDisabledColor, 0);
        mOuterCircleCheckedDisabledColor = a.getColor(R.styleable.COUISwitch_outerCircleCheckedDisabledColor, 0);
        mBarCheckedDisabledColor = a.getColor(R.styleable.COUISwitch_barUncheckedDisabledColor,
                COUIContextUtil.getAttrColor(context, R.attr.couiColorPrimary) & ALPHA_VALUE_30);
        mIsThemedEnabled = getContext().getResources().getBoolean(R.bool.coui_switch_theme_enable);
        if (mIsThemedEnabled) {
            mThemedLoadingDrawable = a.getDrawable(R.styleable.COUISwitch_themedLoadingDrawable);
            mThemedLoadingCheckedBackground = a.getDrawable(R.styleable.COUISwitch_themedLoadingCheckedBackground);
            mThemedLoadingUncheckedBackground = a.getDrawable(R.styleable.COUISwitch_themedLoadingUncheckedBackground);
            mCheckedDrawable = a.getDrawable(R.styleable.COUISwitch_themedCheckedDrawable);
            mUncheckedDrawable = a.getDrawable(R.styleable.COUISwitch_themedUncheckedDrawable);
        }
    }

    private void initOutLine() {
        if (!isOs16() || mIsThemedEnabled) {
            return;
        }
        setOutlineProvider(new ViewOutlineProvider() {
            private final Rect mOplusOutLineRect = new Rect();

            @Override
            public void getOutline(View view, Outline outline) {
                OplusOutlineAdapter adapter = new OplusOutlineAdapter(outline, 1);
                mOplusOutLineRect.left = (int) mSwitchRect.left;
                mOplusOutLineRect.top = (int) mSwitchRect.top;
                mOplusOutLineRect.right = (int) mSwitchRect.right;
                mOplusOutLineRect.bottom = (int) mSwitchRect.bottom;
                adapter.setSmoothRoundRect(mOplusOutLineRect, (mOplusOutLineRect.height() * getScaleY()) / 2.0f);
            }
        });
        setClipToOutline(true);
        ShadowUtils.clearShadow(this);
    }

    private void initPaint() {
        mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setPaintShadowLayer();
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void initResValue(Context context) {
        mPadding = context.getResources().getDimensionPixelSize(R.dimen.coui_switch_padding);
        mSwitchOnStr = getResources().getString(R.string.switch_on);
        mSwitchOffStr = getResources().getString(R.string.switch_off);
        mSwitchLoadingStr = getResources().getString(R.string.switch_loading);
        mDefaultTranslation = (getSwitchMinWidth() - (mCirclePadding * 2)) - mOuterCircleWidth;
        mBarCheckedColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorPrimary);
        mBarUnCheckedColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorControls);
        mBarTrackCurrentColor = isChecked() ? mBarCheckedColor : mBarUnCheckedColor;
        mBarUncheckedDisabledColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorPressBackground);
        setTrackTintMode(PorterDuff.Mode.SRC);
    }

    private Interpolator switchInterpolator() {
        return new PathInterpolator(0.3f, 0.0f, 0.1f, 1.0f);
    }

    private void initStartLoadingAnimator() {
        Interpolator interpolator = switchInterpolator();
        mStartLoadingAnimator = new AnimatorSet();
        ValueAnimator circleScale = ValueAnimator.ofFloat(1.0f, 0.0f);
        circleScale.addUpdateListener(animation -> setCircleScale((Float) animation.getAnimatedValue()));
        circleScale.setInterpolator(interpolator);
        circleScale.setDuration(433L);
        ValueAnimator loadingScale = ValueAnimator.ofFloat(0.5f, 1.0f);
        loadingScale.addUpdateListener(animation -> setLoadingScale((Float) animation.getAnimatedValue()));
        loadingScale.setInterpolator(interpolator);
        loadingScale.setDuration(550L);
        ValueAnimator loadingAlpha = ValueAnimator.ofFloat(0.0f, 1.0f);
        loadingAlpha.addUpdateListener(animation -> setLoadingAlpha((Float) animation.getAnimatedValue()));
        loadingAlpha.setInterpolator(interpolator);
        loadingAlpha.setDuration(550L);
        ValueAnimator rotation = ValueAnimator.ofFloat(0.0f, 360.0f);
        rotation.addUpdateListener(animation -> setLoadingRotation((Float) animation.getAnimatedValue()));
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setDuration(800L);
        rotation.setInterpolator(new COUILinearInterpolator());
        mStartLoadingAnimator.play(circleScale).with(loadingAlpha).with(loadingScale).with(rotation);
    }

    private void initStateEffectBackground() {
        Drawable background = getBackground();
        mStrokeDrawable = new COUIStrokeDrawable(getContext());
        Resources res = getContext().getResources();
        int radius = res.getDimensionPixelOffset(R.dimen.bar_radius);
        mStrokeDrawable.setStrokeRect(mSwitchRect, radius, radius);
        setDefaultFocusHighlightEnabled(false);
        mStateEffectBackground = new COUIStateEffectDrawable(new Drawable[]{
                background == null ? new ColorDrawable(Color.TRANSPARENT) : background, mStrokeDrawable});
        super.setBackground(mStateEffectBackground);
    }

    private void initStopLoadingAnimator() {
        mStopLoadingAnimator = new AnimatorSet();
        ValueAnimator alpha = ValueAnimator.ofFloat(1.0f, 0.0f);
        alpha.addUpdateListener(animation -> setLoadingAlpha((Float) animation.getAnimatedValue()));
        alpha.setInterpolator(switchInterpolator());
        alpha.setDuration(100L);
        mStopLoadingAnimator.play(alpha);
    }

    private void initThemedLoadingAnimator() {
        mThemedLoadingAnimator = new AnimatorSet();
        ValueAnimator rotation = ValueAnimator.ofFloat(0.0f, 360.0f);
        rotation.addUpdateListener(animation -> setLoadingRotation((Float) animation.getAnimatedValue()));
        rotation.setRepeatCount(ValueAnimator.INFINITE);
        rotation.setDuration(800L);
        rotation.setInterpolator(new COUILinearInterpolator());
        mThemedLoadingAnimator.play(rotation);
    }

    private boolean isOs16() {
        return RoundCornerUtil.getSmoothStyleType() == 1;
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    private void performFeedBack() {
        if (!isTactileFeedbackEnabled()) {
            return;
        }
        if (mVibratorExecutor == null) {
            mVibratorExecutor = Executors.newSingleThreadExecutor();
        }
        // Leapy modified: Match OPPO's 263 ms animator-synchronized feedback path.
        mFeedBackSwitch.set(true);
        mVibratorExecutor.execute(() -> {
            try {
                Thread.sleep(263L);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
            if (mToggleAnimator != null && mToggleAnimator.isRunning()
                    && mFeedBackSwitch.get()) {
                mFeedBackSwitch.set(false);
                performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
            }
        });
        setTactileFeedbackEnabled(false);
    }

    private void playSoundEffect(boolean checked) {
        COUIAsyncSoundUtil.play(getContext(), checked ? R.raw.coui_switch_sound_on : R.raw.coui_switch_sound_off,
                1.0f, 1.0f, 0, 0, 1.0f);
    }

    private void setBarColor(int color) {
        mBarTrackCurrentColor = color;
        invalidate();
    }

    private void setInnerCircleRectF() {
        mInnerCircleRectF.set(mOuterCircleRectF.left + mOuterCircleStrokeWidth,
                mOuterCircleRectF.top + mOuterCircleStrokeWidth,
                mOuterCircleRectF.right - mOuterCircleStrokeWidth,
                mOuterCircleRectF.bottom - mOuterCircleStrokeWidth);
    }

    private void setOuterCircleRectF() {
        float left;
        float right;
        if (isChecked()) {
            if (isRtlMode()) {
                left = mCirclePadding + mCircleTranslation + mPadding;
                right = left + (mOuterCircleWidth * mCircleScaleX);
            } else {
                right = ((getSwitchMinWidth() - mCirclePadding) - (mDefaultTranslation - mCircleTranslation)) + mPadding;
                left = right - (mOuterCircleWidth * mCircleScaleX);
            }
        } else if (isRtlMode()) {
            right = ((getSwitchMinWidth() - mCirclePadding) - (mDefaultTranslation - mCircleTranslation)) + mPadding;
            left = right - (mOuterCircleWidth * mCircleScaleX);
        } else {
            left = mCirclePadding + mCircleTranslation + mPadding;
            right = left + (mOuterCircleWidth * mCircleScaleX);
        }
        float top = ((mBarHeight - mOuterCircleWidth) / 2.0f) + mPadding;
        mOuterCircleRectF.set(left, top, right, top + mOuterCircleWidth);
    }

    private void setPaintShadowLayer() {
        mOuterCirclePaint.setShadowLayer(8.0f, 0.0f, 4.0f, Color.argb(25, 0, 0, 0));
    }

    public void disableThemed() { mIsThemedEnabled = false; }
    public void enableThemed() { mIsThemedEnabled = true; }

    @Override
    public CharSequence getAccessibilityClassName() {
        return Switch.class.getName();
    }

    public final int getOuterCircleUncheckedColor() {
        return mOuterCircleUnCheckedColor;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttachedToWindow = true;
        COUIAsyncSoundUtil.register(getContext(), R.raw.coui_switch_sound_on, R.raw.coui_switch_sound_off);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttachedToWindow = false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mIsThemedEnabled) {
            drawThemedBackground(canvas);
            drawThemedLoading(canvas);
            return;
        }
        drawBar();
        setOuterCircleRectF();
        setInnerCircleRectF();
        super.onDraw(canvas);
        drawOuterCircle(canvas);
        drawLoading(canvas);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (!mIsLoadingStyle) {
            info.setText(isChecked() ? mSwitchOnStr : mSwitchOffStr);
        } else {
            info.setCheckable(false);
            info.setText(isChecked() ? mSwitchOnStr : mSwitchOffStr);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getSwitchMinWidth() + (mPadding * 2), mBarHeight + (mPadding * 2));
        if (!mIsMeasured) {
            mIsMeasured = true;
            mCircleTranslation = isRtlMode()
                    ? (isChecked() ? 0 : mDefaultTranslation)
                    : (isChecked() ? mDefaultTranslation : 0);
            mInnerCircleAlpha = isChecked() ? 0.0f : 1.0f;
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSwitchRect.set(0.0f, 0.0f, w, h);
        if (isOs16()) {
            invalidateOutline();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClickable() || isFocusable()) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                mPressAnimator.animateToProgress(StateEffectAnimator.DEFAULT_ANIMATE_FACTOR, true);
            } else if (action == MotionEvent.ACTION_UP) {
                mShouldPlaySound = true;
                mEnableHapticFeedback = true;
                mPressAnimator.animateToProgress(0.0f, true);
                if (mIsLoadingStyle && isEnabled()) {
                    startLoading();
                    return false;
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                mPressAnimator.animateToProgress(0.0f, true);
            }
        }
        if (mIsLoading) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            if (mIsThemedEnabled && mThemedLoadingAnimator != null && mThemedLoadingAnimator.isPaused()) {
                mThemedLoadingAnimator.resume();
            } else if (!mIsThemedEnabled && mStartLoadingAnimator != null && mStartLoadingAnimator.isPaused()) {
                mStartLoadingAnimator.resume();
            }
        } else if (mIsThemedEnabled && mThemedLoadingAnimator != null && mThemedLoadingAnimator.isRunning()) {
            mThemedLoadingAnimator.pause();
        } else if (!mIsThemedEnabled && mStartLoadingAnimator != null && mStartLoadingAnimator.isRunning()) {
            mStartLoadingAnimator.pause();
        }
    }

    public void refresh() {
        String type = getResources().getResourceTypeName(mStyle);
        TypedArray a = null;
        if ("attr".equals(type)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUISwitch, mStyle, 0);
        } else if ("style".equals(type)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUISwitch, 0, mStyle);
        }
        if (a != null) {
            initAttr(a, getContext());
            a.recycle();
            initResValue(getContext());
        }
        if (mPressAnimator != null) {
            mPressAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPress));
        }
        if (mHoverAnimator != null) {
            mHoverAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorHover));
        }
        if (mStateEffectBackground != null) {
            mStateEffectBackground.refresh(getContext());
        }
        invalidate();
    }

    @Override
    public void setBackground(Drawable background) {
        if (mStateEffectBackground == null) {
            super.setBackground(background);
        } else {
            mStateEffectBackground.setViewBackground(background == null ? new ColorDrawable(Color.TRANSPARENT) : background);
        }
    }

    public final void setBarCheckedColor(int color) {
        mBarCheckedColor = color;
        if (isChecked()) {
            mBarTrackCurrentColor = mBarCheckedColor;
        }
        setBarStateListDrawable();
        invalidate();
    }

    public final void setBarCheckedDisabledColor(int color) {
        mBarCheckedDisabledColor = color;
        setBarStateListDrawable();
        invalidate();
    }

    public void setBarStateListDrawable() {
        Drawable on = ContextCompat.getDrawable(getContext(), R.drawable.switch_custom_track_on);
        Drawable off = ContextCompat.getDrawable(getContext(), R.drawable.switch_custom_track_off);
        Drawable onDisabled = ContextCompat.getDrawable(getContext(), R.drawable.switch_custom_track_on_disable);
        Drawable offDisabled = ContextCompat.getDrawable(getContext(), R.drawable.switch_custom_track_off_disable);
        StateListDrawable state = new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_checked, android.R.attr.state_enabled}, tintShape(on, mBarCheckedColor));
        state.addState(new int[]{-android.R.attr.state_checked, android.R.attr.state_enabled}, tintShape(off, mBarUnCheckedColor));
        state.addState(new int[]{-android.R.attr.state_enabled, android.R.attr.state_checked}, tintShape(onDisabled, mBarCheckedDisabledColor));
        state.addState(new int[]{-android.R.attr.state_enabled, -android.R.attr.state_checked}, tintShape(offDisabled, mBarUncheckedDisabledColor));
        setTrackDrawable(state);
    }

    private Drawable tintShape(Drawable drawable, int color) {
        if (drawable instanceof GradientDrawable && color != 0) {
            GradientDrawable gradient = (GradientDrawable) drawable.mutate();
            gradient.setColor(color);
            return gradient;
        }
        return drawable;
    }

    public final void setBarUnCheckedColor(int color) {
        mBarUnCheckedColor = color;
        if (!isChecked()) {
            mBarTrackCurrentColor = mBarUnCheckedColor;
        }
        setBarStateListDrawable();
        invalidate();
    }

    public final void setBarUncheckedDisabledColor(int color) {
        mBarUncheckedDisabledColor = color;
        setBarStateListDrawable();
        invalidate();
    }

    @Override
    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (checked == isChecked()) {
            return;
        }
        super.setChecked(checked);
        if (!mIsThemedEnabled) {
            boolean currentChecked = isChecked();
            if (mToggleAnimator != null) {
                mToggleAnimator.removeAllListeners();
                mToggleAnimator.cancel();
                mToggleAnimator.end();
            }
            if (!mIsAttachedToWindow || !animate || getHeight() <= 0 || getWidth() <= 0) {
                setCircleTranslation(isRtlMode()
                        ? (currentChecked ? 0 : mDefaultTranslation)
                        : (currentChecked ? mDefaultTranslation : 0));
                setInnerCircleAlpha(currentChecked ? 0.0f : 1.0f);
                setBarColor(currentChecked ? mBarCheckedColor : mBarUnCheckedColor);
            } else {
                animateWhenStateChanged(currentChecked);
            }
        }
        if (mShouldPlaySound && mIsAttachedToWindow) {
            playSoundEffect(checked);
            mShouldPlaySound = false;
        }
        performFeedBack();
        invalidate();
    }

    public void setCheckedDrawable(Drawable drawable) { mCheckedDrawable = drawable; }
    public void setCircleScale(float scale) { mCircleScale = scale; invalidate(); }
    public void setCircleScaleX(float scaleX) { mCircleScaleX = scaleX; invalidate(); }
    public void setCircleTranslation(int translation) { mCircleTranslation = translation; invalidate(); }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mOuterCirclePaint == null) {
            mOuterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        if (enabled) {
            setPaintShadowLayer();
        } else {
            mOuterCirclePaint.clearShadowLayer();
        }
    }

    @Override
    public void setHovered(boolean hovered) {
        super.setHovered(hovered);
        if (isEnabled()) {
            mHoverAnimator.animateToProgress(hovered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, true);
        }
    }

    public void setInnerCircleAlpha(float alpha) { mInnerCircleAlpha = alpha; invalidate(); }
    public void setInnerCircleColor(int color) { mInnerCircleColor = color; }
    public void setLoadingAlpha(float alpha) { mLoadingAlpha = alpha; invalidate(); }
    public void setLoadingDrawable(Drawable drawable) { mLoadingDrawable = drawable; }
    public void setLoadingRotation(float rotation) { mLoadingRotation = rotation; invalidate(); }
    public void setLoadingScale(float scale) { mLoadingScale = scale; invalidate(); }
    public void setLoadingStyle(boolean loadingStyle) { mIsLoadingStyle = loadingStyle; }
    public void setOnLoadingStateChangedListener(OnLoadingStateChangedListener listener) { mOnLoadingStateChangedListener = listener; }
    public void setOuterCircleColor(int color) { mOuterCircleColor = color; }
    public void setOuterCircleStrokeWidth(int width) { mOuterCircleStrokeWidth = width; }
    public final void setOuterCircleUncheckedColor(int color) { mOuterCircleUnCheckedColor = color; invalidate(); }
    public void setShouldPlaySound(boolean shouldPlaySound) { mShouldPlaySound = shouldPlaySound; }
    public void setTactileFeedbackEnabled(boolean enabled) { mEnableHapticFeedback = enabled; }
    public void setThemedLoadingCheckedBackground(Drawable drawable) { mThemedLoadingCheckedBackground = drawable; }
    public void setThemedLoadingUncheckedBackground(Drawable drawable) { mThemedLoadingUncheckedBackground = drawable; }
    public void setUncheckedDrawable(Drawable drawable) { mUncheckedDrawable = drawable; }

    public void startLoading() {
        if (mIsLoading) {
            return;
        }
        if (mManager != null && mManager.isEnabled()) {
            announceForAccessibility(mSwitchLoadingStr);
        }
        mIsLoading = true;
        if (mIsThemedEnabled) {
            mThemedLoadingAnimator.start();
        } else {
            mStartLoadingAnimator.start();
        }
        if (mOnLoadingStateChangedListener != null) {
            mOnLoadingStateChangedListener.onStartLoading();
        }
        invalidate();
    }

    public void stopLoading() {
        if (mIsLoadingStyle && mManager != null && mManager.isEnabled()) {
            announceForAccessibility(isChecked() ? mSwitchOffStr : mSwitchOnStr);
        }
        if (mStartLoadingAnimator != null && mStartLoadingAnimator.isRunning()) {
            mStartLoadingAnimator.cancel();
        }
        if (mThemedLoadingAnimator != null && mThemedLoadingAnimator.isRunning()) {
            mThemedLoadingAnimator.cancel();
        }
        if (mIsLoading) {
            if (!mIsThemedEnabled) {
                mStopLoadingAnimator.start();
            }
            setCircleScale(1.0f);
            mIsLoading = false;
            toggle();
            if (mOnLoadingStateChangedListener != null) {
                mOnLoadingStateChangedListener.onStopLoading();
            }
        }
    }
}
