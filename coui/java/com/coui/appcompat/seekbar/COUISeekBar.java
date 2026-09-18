package com.coui.appcompat.seekbar;

import android.animation.ValueAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.AbsSeekBar;

import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.statelistutil.COUIStateListUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.oplus.physicsengine.engine.AnimationListener;
import com.oplus.physicsengine.engine.AnimationUpdateListener;
import com.oplus.physicsengine.engine.BaseBehavior;
import com.oplus.physicsengine.engine.FlingBehavior;
import com.oplus.physicsengine.engine.PhysicalAnimator;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;
import com.coui.appcompat.view.DescendantOffsetUtil;
import com.coui.appcompat.view.ViewUtil;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.oplus.graphics.OplusCanvas;
import com.oplus.graphics.OplusPathAdapter;

import java.util.Locale;

public class COUISeekBar extends AbsSeekBar
        implements AnimationListener, AnimationUpdateListener {
    private static final int TOUCH_ANIMATION_DURATION = 183;
    private static final int PROGRESS_ANIMATION_DURATION = 150;
    private static final float FAST_MOVE_VELOCITY = 95.0f;
    private static final float MIN_FAST_MOVE_PERCENT = 0.05f;
    private static final float MAX_FAST_MOVE_PERCENT = 0.95f;
    private static final Interpolator THUMB_ANIMATE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final Interpolator PROGRESS_SCALE_INTERPOLATOR = new COUIEaseInterpolator();
    private static final FloatPropertyCompat<COUISeekBar> GLITTER_EFFECT_PROPERTY =
            new FloatPropertyCompat<COUISeekBar>("glitterEffectTransition") {
                @Override
                public float getValue(COUISeekBar seekBar) {
                    return seekBar.getCurGlitterEffectValue();
                }

                @Override
                public void setValue(COUISeekBar seekBar, float value) {
                    seekBar.setCurGlitterEffectValue(value);
                }
            };
    public interface OnSeekBarChangeListener {
        void onProgressChanged(COUISeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(COUISeekBar seekBar);

        void onStopTrackingTouch(COUISeekBar seekBar);
    }

    public interface OnDeformedListener {
        default void onHeightDeformedChanged(float top, float bottom) {
        }

        default void onScaleChanged(DeformedValueBean deformedValueBean) {
        }
    }

    protected final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final RectF mTrackRect = new RectF();
    protected final RectF mBackgroundRect = new RectF();
    protected final RectF mProgressRect = new RectF();
    private final Path mBackgroundPath = new Path();
    protected final Path mProgressPath = new Path();
    protected int mMin;
    protected int mMax = 100;
    protected int mProgress;
    protected int mOldProgress;
    protected int mRealProgress;
    protected int mIncrement = 1;
    protected boolean mIsDragging;
    protected boolean mShowProgress = true;
    protected boolean mShowThumb = true;
    protected boolean mShowText;
    protected boolean mStartFromMiddle;
    protected boolean mEnableVibrator = true;
    protected boolean mDeformation = true;
    protected boolean mPhysicsEnable = true;
    protected boolean mShowGlitterEffect = true;
    protected boolean mIsBumpingEdges;
    protected float mBackgroundEnlargeScale = 1.4f;
    protected int mBackgroundColor;
    protected int mProgressColor;
    protected int mThumbColor;
    protected int mThumbShadowColor;
    protected int mTextColor;
    protected int mGlitterEffectMinColor;
    protected int mGlitterEffectMaxColor;
    protected int mCurGlitterEffectAlpha;
    protected float mBackgroundHeight;
    protected float mProgressHeight;
    protected float mCurBackgroundHeight;
    protected float mCurProgressHeight;
    protected float mCurThumbRadius;
    protected float mBackgroundRadius;
    protected float mProgressRadius;
    protected float mBackgroundRoundCornerWeight;
    protected float mProgressRoundCornerWeight;
    protected float mThumbRadius;
    protected float mThumbPressedRadius;
    protected float mThumbShadowRadius;
    protected float mThumbShadowOffsetY;
    protected float mProgressPaddingHorizontal;
    protected float mProgressPressedPaddingHorizontal;
    protected float mAnimatedProgressPaddingHorizontal;
    protected float mVisualProgress;
    protected float mScale;
    protected float mDrawProgressScale;
    protected float mPixPerProgress;
    protected float mThumbPosition;
    protected float mTouchDownX;
    protected float mLastX;
    protected float mFlingVelocity;
    protected float mFlingFrequency = 2.8f;
    protected float mFlingDampingRatio = 1.0f;
    protected float mFlingLinearDamping = 15.0f;
    protected float mDamping;
    protected float mFastMoveScaleOffsetX;
    protected float mCurGlitterEffectValue;
    protected float mHeightBottomDeformedDownValue;
    protected float mHeightBottomDeformedUpValue;
    protected float mHeightTopDeformedDownValue;
    protected float mHeightTopDeformedUpValue;
    protected float mWidthDeformedValue;
    protected float mMaxHeightDeformedValue = 28.5f;
    protected float mMaxWidthDeformedValue = 4.7f;
    protected float mMaxBackgroundHeight;
    protected int mMaxMovingDistance = 30;
    protected int mMinHeight;
    protected int mMaxWidth;
    protected int mTouchSlop;
    protected int mMoveType = 1;
    protected String mText;
    protected OnSeekBarChangeListener mOnSeekBarChangeListener;
    private OnDeformedListener mOnDeformedListener;
    private String mSeekBarRoleDescription;
    private TextDrawable mTextDrawable;
    private VelocityTracker mVelocityTracker;

    private ColorStateList mBackgroundColorStateList;
    private ColorStateList mProgressColorStateList;
    private ColorStateList mThumbColorStateList;
    private ValueAnimator mTouchEnlargeAnimator;
    private ValueAnimator mTouchReleaseAnimator;
    private ValueAnimator mProgressAnimator;
    protected COUISpringAnimation mClickAnim;
    private COUISpringAnimation mFlexibleFollowHandAnim;
    private COUISpringAnimation mThumbScaleAnim;
    private COUISpringAnimation mDeformationAnim;
    private COUISpringAnimation mGlitterEffectAnim;
    private Spring mFastMoveSpring;
    private SpringConfig mFastMoveSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(500.0d, 30.0d);
    private PhysicalAnimator mPhysicalAnimator;
    private FlingBehavior mFlingBehavior;
    private com.oplus.physicsengine.engine.FloatValueHolder mFlingValueHolder;
    protected COUIDynamicAnimation.OnAnimationEndListener mLastEndClickListener;
    private LinearGradient mMaxToMinLinearGradient;
    private LinearGradient mMinToMaxLinearGradient;
    private Paint mGlitterEffectPaint;
    protected SmoothRoundCornerHelper mBackgroundSmoothRoundCornerHelper;
    protected SmoothRoundCornerHelper mProgressSmoothRoundCornerHelper;
    protected boolean mStartDragging;

    public COUISeekBar(Context context) {
        this(context, null);
    }

    public COUISeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiSeekBarStyle);
    }

    public COUISeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUISeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context = getContext();
        mBackgroundColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_background_color_normal);
        mProgressColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorContainerTheme);
        mThumbColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_thumb_color);
        mThumbShadowColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_thumb_shadow_color);
        mTextColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_text_color);
        mGlitterEffectMinColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_glitter_effect_min_color);
        mGlitterEffectMaxColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_glitter_effect_max_color);
        mBackgroundHeight = getResources().getDimension(R.dimen.coui_seekbar_background_height);
        mProgressHeight = getResources().getDimension(R.dimen.coui_seekbar_progress_height);
        mCurBackgroundHeight = mBackgroundHeight;
        mCurProgressHeight = mProgressHeight;
        mBackgroundRadius = getResources().getDimension(R.dimen.coui_seekbar_background_radius);
        mProgressRadius = getResources().getDimension(R.dimen.coui_seekbar_progress_radius);
        mThumbRadius = getResources().getDimension(R.dimen.coui_seekbar_thumb_out_radius);
        mThumbPressedRadius = getResources().getDimension(R.dimen.coui_seekbar_thumb_max_radius);
        mCurThumbRadius = mThumbRadius;
        mThumbShadowRadius = getResources().getDimension(R.dimen.coui_seekbar_thumb_shadow_size);
        mThumbShadowOffsetY = getResources().getDimension(R.dimen.coui_seekbar_shadow_offset_y);
        mProgressPaddingHorizontal = getResources().getDimension(R.dimen.coui_seekbar_progress_padding_horizontal);
        mProgressPressedPaddingHorizontal = getResources().getDimension(R.dimen.coui_seekbar_progress_pressed_padding_horizontal);
        mAnimatedProgressPaddingHorizontal = mProgressPaddingHorizontal;
        mMinHeight = getResources().getDimensionPixelSize(R.dimen.coui_seekbar_view_min_height);
        mMaxWidth = getResources().getDimensionPixelSize(R.dimen.coui_seekbar_view_max_width);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mText = "";
        mMaxBackgroundHeight = mBackgroundHeight * mBackgroundEnlargeScale;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUISeekBar, defStyleAttr, defStyleRes);
        mMin = a.getInt(R.styleable.COUISeekBar_android_min, mMin);
        mMax = a.getInt(R.styleable.COUISeekBar_android_max, mMax);
        mProgress = a.getInt(R.styleable.COUISeekBar_android_progress, mProgress);
        mEnableVibrator = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarEnableVibrator, true);
        mShowProgress = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarShowProgress, true);
        mShowThumb = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarShowThumb, true);
        mShowText = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarShowText, false);
        mShowGlitterEffect = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarShowGlitterEffect, true);
        mStartFromMiddle = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarStartMiddle, false);
        mDeformation = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarDeformation, true);
        mPhysicsEnable = a.getBoolean(R.styleable.COUISeekBar_couiSeekBarPhysicsEnable, true);
        mBackgroundEnlargeScale = a.getFloat(
                R.styleable.COUISeekBar_couiSeekBarBackGroundEnlargeScale,
                mBackgroundEnlargeScale);
        mBackgroundRoundCornerWeight = a.getFloat(
                R.styleable.COUISeekBar_couiSeekBarBackgroundRoundCornerWeight, 0f);
        mProgressRoundCornerWeight = a.getFloat(
                R.styleable.COUISeekBar_couiSeekBarProgressRoundCornerWeight, 0f);
        mBackgroundColorStateList = a.getColorStateList(R.styleable.COUISeekBar_couiSeekBarBackgroundColor);
        mProgressColorStateList = a.getColorStateList(R.styleable.COUISeekBar_couiSeekBarProgressColor);
        // Leapy modified 2026-08-14: Decoded OPPO COUISeekBar — light style omits
        // couiSeekBarProgressColor, so build enabled=theme / disabled=couiColorDisable.
        if (mProgressColorStateList == null) {
            mProgressColorStateList = COUIStateListUtil.createColorStateList(
                    COUIContextUtil.getAttrColor(context, R.attr.couiColorContainerTheme, 0),
                    COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDisable));
        }
        // Leapy end
        mThumbColorStateList = a.getColorStateList(R.styleable.COUISeekBar_couiSeekBarThumbColor);
        mBackgroundHeight = a.getDimension(R.styleable.COUISeekBar_couiSeekBarBackgroundHeight, mBackgroundHeight);
        mProgressHeight = a.getDimension(R.styleable.COUISeekBar_couiSeekBarProgressHeight, mProgressHeight);
        mCurBackgroundHeight = mBackgroundHeight;
        mCurProgressHeight = mProgressHeight;
        mMinHeight = a.getDimensionPixelSize(R.styleable.COUISeekBar_couiSeekBarMinHeight, mMinHeight);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.COUISeekBar_couiSeekBarMaxWidth, mMaxWidth);
        mThumbRadius = a.getDimension(R.styleable.COUISeekBar_couiSeekBarThumbOutRadius, mThumbRadius);
        mCurThumbRadius = mThumbRadius;
        mThumbShadowRadius = a.getDimension(R.styleable.COUISeekBar_couiSeekBarThumbShadowSize, mThumbShadowRadius);
        mMaxBackgroundHeight = mBackgroundHeight * mBackgroundEnlargeScale;
        mProgressPaddingHorizontal = mMaxBackgroundHeight / 2.0f;
        mAnimatedProgressPaddingHorizontal = mProgressPaddingHorizontal;
        mThumbShadowColor = a.getColor(R.styleable.COUISeekBar_couiSeekBarThumbShadowColor, mThumbShadowColor);
        mTextColor = a.getColor(R.styleable.COUISeekBar_couiSeekBarTextColor, mTextColor);
        mText = a.getString(R.styleable.COUISeekBar_couiSeekBarText);
        a.recycle();

        mBackgroundColor = getColor(this, mBackgroundColorStateList,
                COUIContextUtil.getColor(context, R.color.coui_seekbar_background_selector));
        mProgressColor = getColor(this, mProgressColorStateList,
                COUIContextUtil.getColor(context, R.color.coui_seekbar_progress_selector));
        mThumbColor = getColor(this, mThumbColorStateList,
                COUIContextUtil.getColor(context, R.color.coui_seekbar_thumb_selector));
        super.setMin(mMin);
        super.setMax(Math.max(mMin, mMax));
        int initialProgress = mProgress;
        mProgress = mMin;
        mRealProgress = getRealProgress(mProgress);
        setProgressInternal(initialProgress, false, false);
        mVisualProgress = mProgress;
        ensureSize();
        mSeekBarRoleDescription = getResources().getString(R.string.coui_seek_bar_role_description);
        mBackgroundSmoothRoundCornerHelper = new SmoothRoundCornerHelper(mBackgroundPath);
        mProgressSmoothRoundCornerHelper = new SmoothRoundCornerHelper(mProgressPath);
        mGlitterEffectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGlitterEffectPaint.setColor(0xff000000);
        mTextDrawable = new TextDrawable(getContext());
        initSpringAnimations();
        initAccessibility();
        setFocusable(true);
        setClickable(true);
    }

    private void ensureSize() {
        mCurBackgroundHeight = mBackgroundHeight;
        mCurThumbRadius = mThumbRadius;
        mCurProgressHeight = mProgressHeight;
        mProgressRadius = mProgressHeight / 2.0f;
        mProgressPaddingHorizontal = mMaxBackgroundHeight / 2.0f;
        mAnimatedProgressPaddingHorizontal = mProgressPaddingHorizontal;
        updatePixPerProgress();
        updateScale();
        updateBehavior();
    }

    private void initSpringAnimations() {
        COUISpringForce clickForce = new COUISpringForce();
        clickForce.setBounce(0f);
        clickForce.setResponse(0.3f);
        mClickAnim = new COUISpringAnimation(new FloatValueHolder(0f)).setSpring(clickForce);
        mClickAnim.addUpdateListener((animation, value, velocity) -> onClickAnimationUpdate(value));

        COUISpringForce followForce = new COUISpringForce();
        followForce.setBounce(0f);
        followForce.setResponse(0.1f);
        mFlexibleFollowHandAnim = new COUISpringAnimation(new FloatValueHolder(0f)).setSpring(followForce);
        mFlexibleFollowHandAnim.addUpdateListener((animation, value, velocity) -> {
            mDrawProgressScale = value / 1000f;
            invalidate();
        });

        COUISpringForce thumbForce = new COUISpringForce();
        thumbForce.setBounce(0f);
        thumbForce.setResponse(0.2f);
        mThumbScaleAnim = new COUISpringAnimation(new FloatValueHolder(mThumbRadius)).setSpring(thumbForce);
        mThumbScaleAnim.addUpdateListener((animation, value, velocity) -> {
            mCurThumbRadius = value;
            invalidate();
        });

        COUISpringForce deformationForce = new COUISpringForce();
        deformationForce.setBounce(0f);
        deformationForce.setResponse(0.1f);
        mDeformationAnim = new COUISpringAnimation(new FloatValueHolder(0f)).setSpring(deformationForce);
        mDeformationAnim.addUpdateListener((animation, value, velocity) -> {
            float deformScale = value / 100000f;
            updateSpringDeformationValues(deformScale);
        });

        COUISpringForce glitterForce = new COUISpringForce();
        glitterForce.setBounce(0f);
        glitterForce.setResponse(0.6f);
        mGlitterEffectAnim = new COUISpringAnimation(this, GLITTER_EFFECT_PROPERTY).setSpring(glitterForce);
        mGlitterEffectAnim.addEndListener((animation, canceled, value, velocity) -> {
            if (!canceled) {
                resetBumpingEdges();
            }
        });
    }

    private void initAccessibility() {
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(COUISeekBar.class.getName());
                info.setRangeInfo(AccessibilityNodeInfoCompat.RangeInfoCompat.obtain(
                        AccessibilityNodeInfoCompat.RangeInfoCompat.RANGE_TYPE_INT,
                        getMin(), getMax(), getProgress()));
                info.setRoleDescription(mSeekBarRoleDescription);
                info.setStateDescription(formatStateDescription(getProgress()));
                if (isEnabled()) {
                    if (getProgress() > getMin()) {
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                    }
                    if (getProgress() < getMax()) {
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
                    }
                }
            }

            @Override
            public boolean performAccessibilityAction(View host, int action, android.os.Bundle args) {
                if (!isEnabled()) {
                    return false;
                }
                if (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD) {
                    setProgress(getProgress() + mIncrement, false, true);
                    announceForAccessibility(formatStateDescription(getProgress()));
                    return true;
                }
                if (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD) {
                    setProgress(getProgress() - mIncrement, false, true);
                    announceForAccessibility(formatStateDescription(getProgress()));
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
    }

    private String formatStateDescription(int progress) {
        return String.format(Locale.getDefault(), "%d", progress);
    }

    public final class SmoothRoundCornerHelper {
        private final OplusPathAdapter mPathAdapter;
        private final int mSmoothStyleType;

        public SmoothRoundCornerHelper(Path path) {
            mSmoothStyleType = RoundCornerUtil.getSmoothStyleType();
            mPathAdapter = mSmoothStyleType == 1 ? new OplusPathAdapter(path, mSmoothStyleType) : null;
        }

        public OplusPathAdapter getPathAdapter() {
            return mPathAdapter;
        }

        public int getSmoothStyleType() {
            return mSmoothStyleType;
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateStateColors();
        invalidate();
    }

    private void updateStateColors() {
        // Leapy modified 2026-08-14: Match decoded OPPO setEnabled()/init color
        // refresh. Disabled progress is ?couiColorDisable (faded blue ring), not
        // #ebebeb (solid white thumb) and not the enabled theme blue.
        mBackgroundColor = getColor(this, mBackgroundColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_background_selector));
        mProgressColor = getColor(this, mProgressColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_progress_selector));
        mThumbColor = getColor(this, mThumbColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_thumb_selector));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int minHeight = mMinHeight + getPaddingTop() + getPaddingBottom();
        if (MeasureSpec.EXACTLY != heightMode || height < minHeight) {
            height = minHeight;
        }
        if (mMaxWidth > 0 && width > mMaxWidth) {
            width = mMaxWidth;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        setBackgroundRect();
        setProgressRect();
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawInactiveTrack(canvas);
        drawActiveTrack(canvas, getSeekBarWidth());
    }

    protected void drawHorizontal(Canvas canvas) {
        setBackgroundRect();
        setProgressRect();
        drawInactiveTrack(canvas);
        drawActiveTrack(canvas, getSeekBarWidth());
        if (mShowText) {
            ensureLabelsAdded();
        }
    }

    public void drawInactiveTrack(Canvas canvas) {
        int smoothStyleType = mBackgroundSmoothRoundCornerHelper.getSmoothStyleType();
        if (smoothStyleType == 0) {
            mPaint.setColor(mBackgroundColor);
            if (mBackgroundRoundCornerWeight == 0f) {
                canvas.drawRoundRect(mBackgroundRect, mCurBackgroundHeight / 2f,
                        mCurBackgroundHeight / 2f, mPaint);
                return;
            }
            new OplusCanvas(canvas).drawSmoothRoundRect(mBackgroundRect, mCurBackgroundHeight / 2f,
                    mCurBackgroundHeight / 2f, mPaint, mBackgroundRoundCornerWeight);
            return;
        }
        if (smoothStyleType != 1) {
            mPaint.setColor(mBackgroundColor);
            canvas.drawRoundRect(mBackgroundRect, mCurBackgroundHeight / 2f,
                    mCurBackgroundHeight / 2f, mPaint);
            return;
        }
        mBackgroundPath.reset();
        canvas.save();
        mBackgroundSmoothRoundCornerHelper.getPathAdapter().addSmoothRoundRect(
                mBackgroundRect, mCurBackgroundHeight / 2f, mCurBackgroundHeight / 2f,
                Path.Direction.CCW);
        canvas.clipPath(mBackgroundPath);
        canvas.drawColor(mBackgroundColor);
        canvas.restore();
    }

    public void drawActiveTrack(Canvas canvas, float width) {
        drawProgress(canvas);
        drawGlitterEffect(canvas);
        drawThumb(canvas);
    }

    public void drawGlitterEffect(Canvas canvas) {
        if (!mShowGlitterEffect) {
            return;
        }
        if (mStartFromMiddle) {
            if (isLayoutRtl()) {
                if (mScale >= 1f) {
                    setMaxToMinLinearGradient();
                } else if (mScale <= 0f) {
                    setMinToMaxLinearGradient();
                }
            } else if (mScale >= 1f) {
                setMinToMaxLinearGradient();
            } else if (mScale <= 0f) {
                setMaxToMinLinearGradient();
            }
        } else if (isLayoutRtl()) {
            if (mScale >= 1f) {
                setMaxToMinLinearGradient();
            }
        } else if (mScale >= 1f) {
            setMinToMaxLinearGradient();
        }
        mGlitterEffectPaint.setAlpha(mCurGlitterEffectAlpha);
        canvas.drawRoundRect(mProgressRect, mProgressHeight / 2f, mProgressHeight / 2f,
                mGlitterEffectPaint);
    }

    private void setMaxToMinLinearGradient() {
        if (mMaxToMinLinearGradient == null) {
            mMaxToMinLinearGradient = new LinearGradient(mProgressRect.left, 0f,
                    mProgressRect.right, 0f, mGlitterEffectMaxColor, mGlitterEffectMinColor,
                    Shader.TileMode.CLAMP);
        }
        mGlitterEffectPaint.setShader(mMaxToMinLinearGradient);
    }

    private void setMinToMaxLinearGradient() {
        if (mMinToMaxLinearGradient == null) {
            mMinToMaxLinearGradient = new LinearGradient(mProgressRect.left, 0f,
                    mProgressRect.right, 0f, mGlitterEffectMinColor, mGlitterEffectMaxColor,
                    Shader.TileMode.CLAMP);
        }
        mGlitterEffectPaint.setShader(mMinToMaxLinearGradient);
    }

    private int computeGlitterEffectAlpha(float value) {
        return (int) Math.round((1.0d
                - Math.exp((-(Math.log(85.0d) / 360.0d)) * value)) * 255.0d);
    }

    public float getCurGlitterEffectValue() {
        return mCurGlitterEffectValue;
    }

    public void setCurGlitterEffectValue(float value) {
        mCurGlitterEffectValue = value;
        mCurGlitterEffectAlpha = computeGlitterEffectAlpha(value);
        invalidate();
    }

    private void startGlitterEffectAnim(float velocity) {
        if (!mShowGlitterEffect || mGlitterEffectAnim == null) {
            return;
        }
        mIsBumpingEdges = true;
        mGlitterEffectAnim.setStartValue(mCurGlitterEffectValue);
        mGlitterEffectAnim.animateToFinalPosition(0.0f);
        mGlitterEffectAnim.setStartVelocity(Math.abs(velocity));
    }

    private void executeFlingGlitterEffectAnim(BaseBehavior behavior, float oldScale) {
        float velocity = Math.min(Math.abs(behavior.getPropertyBodyVelocity().mX), 8000.0f);
        if (!mStartFromMiddle) {
            if (mScale < 1.0f || mIsBumpingEdges || oldScale >= 1.0f) {
                return;
            }
            startGlitterEffectAnim(velocity);
            return;
        }
        if (mScale >= 1.0f && !mIsBumpingEdges && oldScale < 1.0f) {
            startGlitterEffectAnim(velocity);
        } else if (mScale <= 0.0f && !mIsBumpingEdges && oldScale > 0.0f) {
            startGlitterEffectAnim(velocity);
        }
    }

    private Spring getFastMoveSpring() {
        if (mFastMoveSpring == null) {
            initFastMoveAnimation();
        }
        return mFastMoveSpring;
    }

    private void initFastMoveAnimation() {
        if (mFastMoveSpring != null) {
            return;
        }
        Spring spring = SpringSystem.create().createSpring();
        mFastMoveSpring = spring;
        spring.setSpringConfig(mFastMoveSpringConfig);
        spring.addListener(new SpringListener() {
            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringAtRest(Spring spring) {
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }

            @Override
            public void onSpringUpdate(Spring spring) {
                if (mFastMoveScaleOffsetX != spring.getEndValue()) {
                    mFastMoveScaleOffsetX = isEnabled() ? (float) spring.getCurrentValue() : 0.0f;
                    invalidate();
                }
            }
        });
    }

    private void startFastMoveAnimation(float velocity) {
        Spring fastMoveSpring = getFastMoveSpring();
        if (fastMoveSpring.getCurrentValue() != fastMoveSpring.getEndValue()) {
            return;
        }
        int range = mMax - mMin;
        float progress = mProgress;
        float minEdge = range * MIN_FAST_MOVE_PERCENT;
        float maxEdge = range * MAX_FAST_MOVE_PERCENT;
        if (velocity >= FAST_MOVE_VELOCITY) {
            if (progress > maxEdge || progress < minEdge) {
                return;
            }
            fastMoveSpring.setEndValue(1.0d);
        } else if (velocity <= -FAST_MOVE_VELOCITY) {
            if (progress > maxEdge || progress < minEdge) {
                return;
            }
            fastMoveSpring.setEndValue(-1.0d);
        } else {
            fastMoveSpring.setEndValue(0.0d);
        }
    }

    private float calculateDamping() {
        return mDamping != 0.0f ? mDamping : 1.0f;
    }

    public void executeTouchGlitterEffectAnim() {
        if (!mShowGlitterEffect || mVelocityTracker == null) {
            return;
        }
        mVelocityTracker.computeCurrentVelocity(1000, 8000f);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (!mStartFromMiddle) {
            if (isLayoutRtl()) {
                if (mScale >= 1f && xVelocity < 0f) {
                    startGlitterEffectAnim(xVelocity);
                }
            } else if (mScale >= 1f && xVelocity > 0f) {
                startGlitterEffectAnim(xVelocity);
            }
        } else if ((mScale >= 1f && ((!isLayoutRtl() && xVelocity > 0f)
                || (isLayoutRtl() && xVelocity < 0f)))
                || (mScale <= 0f && ((!isLayoutRtl() && xVelocity < 0f)
                || (isLayoutRtl() && xVelocity > 0f)))) {
            startGlitterEffectAnim(xVelocity);
        }
    }

    protected void drawProgress(Canvas canvas) {
        if (!mShowProgress) {
            return;
        }
        int smoothStyleType = mProgressSmoothRoundCornerHelper.getSmoothStyleType();
        if (smoothStyleType == 0) {
            mPaint.setColor(mProgressColor);
            if (mProgressRoundCornerWeight == 0f) {
                canvas.drawRoundRect(mProgressRect, mProgressHeight / 2f,
                        mProgressHeight / 2f, mPaint);
                return;
            }
            new OplusCanvas(canvas).drawSmoothRoundRect(mProgressRect, mProgressHeight / 2f,
                    mProgressHeight / 2f, mPaint, mProgressRoundCornerWeight);
            return;
        }
        if (smoothStyleType != 1) {
            mPaint.setColor(mProgressColor);
            canvas.drawRoundRect(mProgressRect, mProgressHeight / 2f,
                    mProgressHeight / 2f, mPaint);
            return;
        }
        mProgressPath.reset();
        canvas.save();
        mProgressSmoothRoundCornerHelper.getPathAdapter().addSmoothRoundRect(
                mProgressRect, mProgressHeight / 2f, mProgressHeight / 2f,
                Path.Direction.CCW);
        canvas.clipPath(mProgressPath);
        canvas.drawColor(mProgressColor);
        canvas.restore();
    }

    protected void drawMarks(Canvas canvas, float start, float end, float centerY) {
    }

    protected void drawThumb(Canvas canvas) {
        if (!mShowThumb) {
            return;
        }
        float x = mThumbPosition;
        float y = getSeekBarCenterY();
        float radius = mCurThumbRadius;
        if (mThumbShadowRadius > 0f && isEnabled()) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShadowLayer(mThumbShadowRadius, 0f, mThumbShadowOffsetY, mThumbShadowColor);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mThumbColor);
        canvas.drawRoundRect(x - radius, y - radius, x + radius, y + radius, radius, radius, mPaint);
        if (mThumbShadowRadius > 0f && isEnabled()) {
            mPaint.clearShadowLayer();
        }
    }

    protected void drawText(Canvas canvas, float centerY) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mTextColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(getResources().getDimension(R.dimen.coui_seekbar_text_size));
        String text = TextUtils.isEmpty(mText) ? String.format(Locale.getDefault(), "%d", mProgress) : mText;
        canvas.drawText(text, getThumbX(),
                centerY - mThumbPressedRadius - getResources().getDimension(R.dimen.coui_seekbar_text_margin_top),
                mPaint);
    }

    private void setValueForLabel(TextDrawable textDrawable, String text) {
        if (textDrawable == null || ViewUtil.getContentView(this) == null
                || ViewUtil.getContentViewOverlay(this) == null) {
            return;
        }
        textDrawable.setText(text);
        if (isLayoutRtl()) {
            int start = getStart();
            textDrawable.setBounds(start, -textDrawable.getIntrinsicHeight(),
                    textDrawable.getIntrinsicWidth() + start, 0);
        } else {
            int width = getWidth() - getEnd();
            textDrawable.setBounds(width, -textDrawable.getIntrinsicHeight(),
                    width - textDrawable.getIntrinsicWidth(), 0);
        }
        Rect rect = new Rect(textDrawable.getBounds());
        DescendantOffsetUtil.offsetDescendantRect(ViewUtil.getContentView(this), this, rect);
        textDrawable.setBounds(rect);
        ViewUtil.getContentViewOverlay(this).add(textDrawable);
    }

    public void ensureLabelsAdded(String text) {
        setValueForLabel(mTextDrawable, text);
    }

    public void ensureLabelsAdded() {
        ensureLabelsAdded(TextUtils.isEmpty(mText)
                ? String.format(Locale.getDefault(), "%d", mRealProgress)
                : mText);
    }

    public void ensureLabelsRemoved() {
        if (ViewUtil.getContentViewOverlay(this) != null && mTextDrawable != null) {
            ViewUtil.getContentViewOverlay(this).remove(mTextDrawable);
        }
    }

    protected float getTrackStart() {
        return getStart() + mProgressPaddingHorizontal;
    }

    protected float getTrackEnd() {
        return getWidth() - getEnd() - mProgressPaddingHorizontal;
    }

    public int getEnd() {
        return getPaddingEnd();
    }

    public int getLabelHeight() {
        return mTextDrawable.getIntrinsicHeight();
    }

    public int getStart() {
        return getPaddingStart();
    }

    protected float getThumbX() {
        return mThumbPosition;
    }

    protected float getThumbXForProgress(float progress) {
        float start = getTrackStart();
        float end = getTrackEnd();
        int range = Math.max(1, mMax - mMin);
        float fraction = (progress - mMin) / range;
        fraction = Math.max(0f, Math.min(1f, fraction));
        if (isLayoutRtl()) {
            fraction = 1f - fraction;
        }
        return start + ((end - start) * fraction);
    }

    protected float getProgressFraction() {
        int range = Math.max(1, mMax - mMin);
        return (mRealProgress - mMin) / (float) range;
    }

    public int getSeekBarCenterY() {
        return getPaddingTop() + (((getHeight() - getPaddingBottom()) - getPaddingTop()) >> 1);
    }

    public int getSeekBarWidth() {
        return Math.max(0, (int) (((getWidth() - getStart()) - getEnd())
                - (mProgressPaddingHorizontal * 2.0f)));
    }

    @Deprecated
    public int getNormalSeekBarWidth() {
        return getSeekBarWidth();
    }

    public void setBackgroundRect() {
        int centerY = getSeekBarCenterY();
        float start = (getStart() + mProgressPaddingHorizontal) - (mCurBackgroundHeight / 2.0f);
        float end = ((getWidth() - getEnd()) - mProgressPaddingHorizontal) + (mCurBackgroundHeight / 2.0f);
        if (isLayoutRtl()) {
            mBackgroundRect.set(
                    (start - mHeightTopDeformedUpValue) + mHeightTopDeformedDownValue,
                    centerY - ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue),
                    (end - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue,
                    centerY + ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue));
        } else {
            mBackgroundRect.set(
                    (start - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue,
                    centerY - ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue),
                    (end + mHeightTopDeformedUpValue) - mHeightTopDeformedDownValue,
                    centerY + ((mCurBackgroundHeight / 2.0f) - mWidthDeformedValue));
        }
    }

    public void setProgressRect() {
        float seekBarWidth = getSeekBarWidth();
        int centerY = getSeekBarCenterY();
        float progressEnd;
        float progressStart;
        float thumbPosition;
        if (mStartFromMiddle) {
            if (isLayoutRtl()) {
                progressEnd = getWidth() / 2.0f;
                progressStart = progressEnd - ((getRealScale(mDrawProgressScale) - 0.5f) * seekBarWidth);
                thumbPosition = progressStart;
            } else {
                progressStart = getWidth() / 2.0f;
                progressEnd = progressStart + ((getRealScale(mDrawProgressScale) - 0.5f) * seekBarWidth);
                thumbPosition = progressEnd;
            }
        } else if (isLayoutRtl()) {
            progressEnd = getStart() + mProgressPaddingHorizontal + seekBarWidth;
            progressStart = progressEnd - (getRealScale(mDrawProgressScale) * seekBarWidth);
            thumbPosition = progressStart;
        } else {
            progressStart = getStart() + mProgressPaddingHorizontal;
            progressEnd = progressStart + (getRealScale(mDrawProgressScale) * seekBarWidth);
            thumbPosition = progressEnd;
        }
        if (!mStartFromMiddle || progressStart <= progressEnd) {
            if (isLayoutRtl()) {
                mProgressRect.set(
                        (progressStart - mHeightTopDeformedUpValue) + mHeightBottomDeformedDownValue,
                        centerY - ((mProgressHeight / 2.0f) - mWidthDeformedValue),
                        (progressEnd - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue,
                        centerY + ((mProgressHeight / 2.0f) - mWidthDeformedValue));
            } else {
                mProgressRect.set(
                        (progressStart - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue,
                        centerY - ((mProgressHeight / 2.0f) - mWidthDeformedValue),
                        (progressEnd + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue,
                        centerY + ((mProgressHeight / 2.0f) - mWidthDeformedValue));
            }
        } else if (isLayoutRtl()) {
            mProgressRect.set(
                    (progressEnd - mHeightTopDeformedUpValue) + mHeightBottomDeformedDownValue,
                    centerY - ((mProgressHeight / 2.0f) - mWidthDeformedValue),
                    (progressStart - mHeightBottomDeformedUpValue) + mHeightBottomDeformedDownValue,
                    centerY + ((mProgressHeight / 2.0f) - mWidthDeformedValue));
        } else {
            mProgressRect.set(
                    (progressEnd - mHeightBottomDeformedDownValue) + mHeightBottomDeformedUpValue,
                    centerY - ((mProgressHeight / 2.0f) - mWidthDeformedValue),
                    (progressStart + mHeightTopDeformedUpValue) - mHeightBottomDeformedDownValue,
                    centerY + ((mProgressHeight / 2.0f) - mWidthDeformedValue));
        }
        mProgressRect.left -= mProgressHeight / 2.0f;
        mProgressRect.right += mProgressHeight / 2.0f;
        float deformationOffset = mHeightTopDeformedUpValue - mHeightBottomDeformedDownValue;
        if (isLayoutRtl()) {
            deformationOffset = -deformationOffset;
        }
        mThumbPosition = thumbPosition + deformationOffset;
    }

    protected float getVisualProgressFraction() {
        return getRealScale(mDrawProgressScale);
    }

    public boolean isLayoutRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                handleMotionEventUp(event);
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                handleMotionEventCancel();
                return true;
            }
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!isDeformationFling()) {
                    stopPhysicsMove();
                }
                if (mPhysicsEnable && mPhysicalAnimator == null) {
                    initPhysicsAnimator(getContext());
                }
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(event);
                mIsDragging = false;
                mStartDragging = false;
                handleMotionEventDown(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                resetBumpingEdges();
                clearDeformationValue();
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(event);
                handleMotionEventMove(event);
                return true;
            case MotionEvent.ACTION_UP:
                mClickAnim.cancel();
                mFlexibleFollowHandAnim.cancel();
                if (mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(1000, 8000f);
                    mFlingVelocity = mVelocityTracker.getXVelocity();
                }
                recycleVelocityTracker();
                handleMotionEventUp(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                mClickAnim.cancel();
                mFlexibleFollowHandAnim.cancel();
                recycleVelocityTracker();
                handleMotionEventCancel();
                return true;
            default:
                return true;
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initPhysicsAnimator(Context context) {
        mPhysicalAnimator = PhysicalAnimator.create(context);
        mFlingValueHolder = new com.oplus.physicsengine.engine.FloatValueHolder(0.0f);
        int seekBarWidth = getSeekBarWidth();
        FlingBehavior behavior = new FlingBehavior(4, 0.0f, (float) seekBarWidth);
        behavior.withProperty(mFlingValueHolder);
        behavior.setSpringProperty(mFlingFrequency, mFlingDampingRatio);
        mFlingBehavior = behavior;
        behavior.setLinearDamping(mFlingLinearDamping);
        mPhysicalAnimator.addBehavior(mFlingBehavior);
        mPhysicalAnimator.addAnimationListener(mFlingBehavior, this);
        mPhysicalAnimator.addAnimationUpdateListener(mFlingBehavior, this);
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public void handleMotionEventDown(MotionEvent event) {
        mTouchDownX = event.getX();
        mLastX = mTouchDownX;
        mIsBumpingEdges = false;
        executeThumbScaleAnim(event);
        setPressed(true);
        if (mShowText) {
            ensureLabelsAdded();
        }
    }

    public void handleMotionEventMove(MotionEvent event) {
        float seekBarWidth = getSeekBarWidth();
        int range = mMax - mMin;
        float currentPixel = (range > 0 ? (mProgress * seekBarWidth) / range : 0.0f) + mMin;
        if (mStartFromMiddle
                && Float.compare(currentPixel, seekBarWidth / 2.0f) == 0
                && Math.abs(event.getX() - mLastX) < 20.0f) {
            return;
        }
        if (mIsDragging && mStartDragging) {
            if (mMoveType != 0) {
                if (mMoveType == 1) {
                    trackTouchEventByFinger(event);
                    return;
                } else if (mMoveType != 2) {
                    return;
                }
            }
            trackTouchEvent(event);
            return;
        }
        if (isToucheInSeekBar(event) && Math.abs(event.getX() - mTouchDownX) > mTouchSlop) {
            if (mClickAnim != null) {
                mClickAnim.cancel();
            }
            stopDeformationFling();
            startDrag();
            touchAnim();
            mLastX = event.getX();
            if (mFlexibleFollowHandAnim != null) {
                mFlexibleFollowHandAnim.setStartValue(mScale * 1000f);
            }
            if (isMoveFollowHand()) {
                invalidateProgress(event);
            }
        }
    }

    public void handleMotionEventUp(MotionEvent event) {
        releaseThumbScaleAnim();
        getFastMoveSpring().setEndValue(0.0d);
        if (!mIsDragging) {
            if (isEnabled() && touchInSeekBar(event, this) && isMoveFollowHand()) {
                stopDeformationFling();
                animForClick(event.getX());
                return;
            }
            return;
        }
        mIsDragging = false;
        mStartDragging = false;
        if (!mPhysicsEnable || Math.abs(mFlingVelocity) < 100f) {
            if (mScale >= 0.0f && mScale <= 1.0f && mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
            }
            flingBehaviorAfterDeformationDrag();
        } else {
            flingBehaviorAfterEndDrag(mFlingVelocity);
        }
        setPressed(false);
        releaseAnim();
    }

    public void handleMotionEventCancel() {
        getFastMoveSpring().setEndValue(0.0d);
        releaseThumbScaleAnim();
        if (mIsDragging) {
            mIsDragging = false;
            mStartDragging = false;
        }
        setPressed(false);
        releaseAnim();
    }

    public void startDrag() {
        setPressed(true);
        startTrackingTouch(true, true);
        requestParentDisallowIntercept(true);
    }

    public void startTrackingTouch(boolean notify, boolean markDragging) {
        if (markDragging) {
            mIsDragging = true;
            mStartDragging = true;
        }
        if (notify && mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    public void stopTrackingTouch(boolean notify, boolean clearDragging) {
        boolean wasDragging = mIsDragging || mStartDragging;
        if (clearDragging) {
            mIsDragging = false;
            mStartDragging = false;
        }
        if (notify && wasDragging && mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    public boolean isToucheInSeekBar(MotionEvent event) {
        return touchInSeekBar(event, this);
    }

    public boolean touchInSeekBar(MotionEvent event, View view) {
        float y = event.getY();
        return mTouchDownX >= view.getPaddingStart()
                && mTouchDownX <= view.getWidth() - view.getPaddingEnd()
                && y >= 0f
                && y <= view.getHeight();
    }

    private boolean isMoveFollowHand() {
        return mMoveType != 2;
    }

    private boolean isDeformationFling() {
        return mDeformation
                && (mScale > 1.0f || mScale < 0.0f)
                && mPhysicalAnimator != null
                && mPhysicalAnimator.isFrameScheduled();
    }

    private void stopDeformationFling() {
        if (isDeformationFling()) {
            stopPhysicsMove();
        }
    }

    protected void releaseAnim() {
        cancelAnim(mTouchEnlargeAnimator);
        if (mTouchReleaseAnimator == null) {
            mTouchReleaseAnimator = getReleaseAnimator(TOUCH_ANIMATION_DURATION, PROGRESS_SCALE_INTERPOLATOR);
        } else {
            cancelAnim(mTouchReleaseAnimator);
        }
        setReleaseAnimatorValues(mTouchReleaseAnimator);
        mTouchReleaseAnimator.start();
        requestParentDisallowIntercept(false);
        ensureLabelsRemoved();
    }

    protected void touchAnim() {
        cancelAnim(mTouchEnlargeAnimator);
        if (mTouchEnlargeAnimator == null) {
            mTouchEnlargeAnimator = getEnlargeAnimator(TOUCH_ANIMATION_DURATION, PROGRESS_SCALE_INTERPOLATOR);
            setEnlargeAnimatorValues(mTouchEnlargeAnimator);
        }
        mTouchEnlargeAnimator.start();
    }

    private void cancelAnim(ValueAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private ValueAnimator getEnlargeAnimator(long duration, Interpolator interpolator) {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(animation -> {
            getCurAnimatorValues(animation);
            invalidate();
        });
        return animator;
    }

    private ValueAnimator getReleaseAnimator(long duration, Interpolator interpolator) {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(animation -> {
            getCurAnimatorValues(animation);
            invalidate();
        });
        return animator;
    }

    public void getCurAnimatorValues(ValueAnimator animator) {
        mCurBackgroundHeight = (Float) animator.getAnimatedValue("backgroundHeight");
    }

    public void setEnlargeAnimatorValues(ValueAnimator animator) {
        animator.setValues(PropertyValuesHolder.ofFloat(
                "backgroundHeight", mBackgroundHeight, mMaxBackgroundHeight));
    }

    public void setReleaseAnimatorValues(ValueAnimator animator) {
        animator.setValues(PropertyValuesHolder.ofFloat(
                "backgroundHeight", mCurBackgroundHeight, mBackgroundHeight));
    }

    private boolean isWithinThumbBounds(float x, float y) {
        int centerY = getSeekBarCenterY();
        float radius = Math.max(mThumbPressedRadius, mCurThumbRadius);
        return x >= mThumbPosition - radius
                && x <= mThumbPosition + radius
                && y >= centerY - radius
                && y <= centerY + radius;
    }

    public void executeThumbScaleAnim(MotionEvent event) {
        if (mShowThumb && isWithinThumbBounds(event.getX(), event.getY())
                && mThumbScaleAnim != null) {
            mThumbScaleAnim.setStartValue(mCurThumbRadius);
            mThumbScaleAnim.animateToFinalPosition(mThumbPressedRadius);
        }
    }

    public void performFeedback() {
        if (mEnableVibrator) {
            if (mRealProgress == getMax() || mRealProgress == getMin()) {
                performHapticFeedback(COUIHapticFeedbackConstants.EDGE_SHORT_VIBRATE);
            } else {
                performHapticFeedback(COUIHapticFeedbackConstants.STEPPING_SHORT_VIBRATE);
            }
        }
    }

    protected void trackTouchEvent(MotionEvent event) {
        float x = event.getX();
        float delta = x - mLastX;
        int range = mMax - mMin;
        if (isLayoutRtl()) {
            delta = -delta;
        }
        float rangeFloat = range;
        setTouchScale((mProgress / rangeFloat)
                + ((delta * calculateDamping()) / getSeekBarWidth()), false);
        executeTouchGlitterEffectAnim();
        if (mFlexibleFollowHandAnim != null) {
            mFlexibleFollowHandAnim.animateToFinalPosition(mScale * 1000f);
        }
        int progress = getProgressLimit(Math.round((mScale * rangeFloat) + getMin()));
        int oldProgress = mProgress;
        int oldRealProgress = mRealProgress;
        setLocalProgress(progress);
        if (mProgress != oldProgress) {
            mLastX = x;
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
            }
            if (oldRealProgress != mRealProgress) {
                performFeedback();
            }
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(100);
            startFastMoveAnimation(mVelocityTracker.getXVelocity());
        }
    }

    private void trackTouchEventByFinger(MotionEvent event) {
        float start;
        int seekBarWidth;
        int roundedX = Math.round(((event.getX() - mLastX) * calculateDamping()) + mLastX);
        if (isLayoutRtl()) {
            start = ((getWidth() - roundedX) - getEnd()) - mProgressPaddingHorizontal;
            seekBarWidth = getSeekBarWidth();
        } else {
            start = (roundedX - getStart()) - mProgressPaddingHorizontal;
            seekBarWidth = getSeekBarWidth();
        }
        setTouchScale(start / seekBarWidth, false);
        executeTouchGlitterEffectAnim();
        if (mFlexibleFollowHandAnim != null) {
            mFlexibleFollowHandAnim.animateToFinalPosition(mScale * 1000f);
        }
        int progress = getProgressLimit(Math.round((mScale * (getMax() - getMin())) + getMin()));
        int oldProgress = mProgress;
        int oldRealProgress = mRealProgress;
        setLocalProgress(progress);
        if (oldProgress != mProgress) {
            mLastX = roundedX;
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
            }
            if (oldRealProgress != mRealProgress) {
                performFeedback();
            }
        }
    }

    private void invalidateProgress(MotionEvent event) {
        float x = event.getX();
        setTouchScale(isLayoutRtl()
                ? (((getWidth() - x) - getEnd()) - mProgressPaddingHorizontal) / getSeekBarWidth()
                : ((x - getStart()) - mProgressPaddingHorizontal) / getSeekBarWidth(), true);
        if (mFlexibleFollowHandAnim != null) {
            mFlexibleFollowHandAnim.animateToFinalPosition(mScale * 1000f);
        }
        int progress = getProgressLimit(Math.round((mScale * (getMax() - getMin())) + getMin()));
        int oldProgress = mProgress;
        int oldRealProgress = mRealProgress;
        setLocalProgress(progress);
        if (oldProgress != mProgress) {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
            }
            if (oldRealProgress != mRealProgress) {
                performFeedback();
            }
        }
    }

    protected void updateProgressFromTouch(float x, boolean fromUser) {
        float start = getTrackStart();
        float end = getTrackEnd();
        float fraction = (x - start) / Math.max(1f, end - start);
        if (isLayoutRtl()) {
            fraction = 1f - fraction;
        }
        setTouchScale(fraction, true);
        int progress = getProgressLimit(Math.round(mMin + (mScale * (mMax - mMin))));
        setProgressInternal(progress, fromUser, true);
    }

    public void animForClick(float x) {
        float seekBarWidth = getSeekBarWidth();
        float progressHeight = mProgressHeight;
        float totalWidth = seekBarWidth + ((progressHeight / 2f) * 2f);
        float offset = mProgressPaddingHorizontal - (progressHeight / 2f);
        float scale = isLayoutRtl()
                ? (((getWidth() - x) - getStart()) - offset) / totalWidth
                : ((x - getStart()) - offset) / totalWidth;
        resetDeformationValue();
        int target = getProgressLimit(Math.round((scale * (getMax() - getMin())) + getMin()));
        startTransitionAnim(target, true, true);
    }

    public void startTransitionAnim(int progress, final boolean fromUser, final boolean clearDragging) {
        if (mClickAnim == null || mPixPerProgress <= 0f) {
            setProgressInternal(progress, fromUser, true);
            stopTrackingTouch(fromUser, clearDragging);
            return;
        }
        COUIDynamicAnimation.OnAnimationEndListener endListener =
                (animation, canceled, value, velocity) -> {
                    if (mOnSeekBarChangeListener != null) {
                        mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, fromUser);
                    }
                    stopTrackingTouch(fromUser, clearDragging);
                };
        mClickAnim.cancel();
        if (mLastEndClickListener != null) {
            mClickAnim.removeEndListener(mLastEndClickListener);
        }
        mClickAnim.addEndListener(endListener);
        float startProgress = (mScale * (getMax() - getMin())) + getMin();
        mClickAnim.setStartValue(startProgress * mPixPerProgress);
        startTrackingTouch(fromUser, clearDragging);
        mClickAnim.animateToFinalPosition(progress * mPixPerProgress);
        mLastEndClickListener = endListener;
    }

    public void onClickAnimationUpdate(float value) {
        if (mPixPerProgress <= 0f) {
            return;
        }
        setLocalProgress((int) (value / mPixPerProgress));
        float scale = getSeekBarWidth() > 0
                ? (value - (mMin * mPixPerProgress)) / getSeekBarWidth()
                : 0f;
        mScale = scale;
        mDrawProgressScale = scale;
        invalidate();
    }

    public void stopClikAnim() {
        if (mClickAnim != null && mClickAnim.isRunning()) {
            mClickAnim.cancelComplete();
        }
    }

    public void releaseThumbScaleAnim() {
        if (mShowThumb && mThumbScaleAnim != null && mCurThumbRadius != mThumbRadius) {
            mThumbScaleAnim.setStartValue(mCurThumbRadius);
            mThumbScaleAnim.animateToFinalPosition(mThumbRadius);
        }
    }

    public void flingBehaviorAfterEndDrag(float velocity) {
        if (mFlingValueHolder == null || mFlingBehavior == null) {
            return;
        }
        int seekBarWidth = getSeekBarWidth();
        int range = mMax - mMin;
        float pixPerProgress = range > 0 ? seekBarWidth / (float) range : 0.0f;
        if (isLayoutRtl()) {
            if (mDeformation) {
                mFlingValueHolder.setValue((mMax - (getDeformationFlingScale() * range)) * pixPerProgress);
            } else {
                mFlingValueHolder.setValue(((mMax - mProgress) + mMin) * pixPerProgress);
            }
        } else if (mDeformation) {
            mFlingValueHolder.setValue(getDeformationFlingScale() * range * pixPerProgress);
        } else {
            mFlingValueHolder.setValue((mProgress - mMin) * pixPerProgress);
        }
        mFlingBehavior.start(velocity);
    }

    public void flingBehaviorAfterDeformationDrag() {
        if (mFlingValueHolder == null || mFlingBehavior == null || !mDeformation) {
            return;
        }
        if (mScale > 1f || mScale < 0f) {
            int seekBarWidth = getSeekBarWidth();
            int range = mMax - mMin;
            float pixPerProgress = range > 0 ? seekBarWidth / (float) range : 0.0f;
            if (isLayoutRtl()) {
                mFlingValueHolder.setValue((mMax - (getDeformationFlingScale() * range)) * pixPerProgress);
            } else {
                mFlingValueHolder.setValue(getDeformationFlingScale() * range * pixPerProgress);
            }
            mFlingBehavior.start();
        }
    }

    public void stopPhysicsMove() {
        if (mPhysicsEnable && mPhysicalAnimator != null && mFlingBehavior != null) {
            mFlingBehavior.stop();
        }
    }

    private float getDeformationFlingScale() {
        return mScale > 1f ? ((mScale - 1f) / 5f) + 1f : mScale < 0f ? mScale / 5f : mScale;
    }

    private void setFlingScale(float scale) {
        if (!mDeformation) {
            float clamped = Math.max(0f, Math.min(scale, 1f));
            mScale = clamped;
            mDrawProgressScale = clamped;
            return;
        }
        calculateFlingDeformationValue(scale);
        setDeformationScale(scale);
        if (mOnDeformedListener != null) {
            DeformedValueBean bean = new DeformedValueBean(mHeightBottomDeformedUpValue,
                    mHeightTopDeformedUpValue, mWidthDeformedValue,
                    mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            bean.setScale(mScale);
            bean.setDrawProgressScale(mDrawProgressScale);
            mOnDeformedListener.onScaleChanged(bean);
        }
    }

    @Override
    public void onAnimationEnd(BaseBehavior behavior) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    @Override
    public void onAnimationUpdate(BaseBehavior behavior) {
        float oldScale = mScale;
        Object value = behavior.getAnimatedValue();
        if (value == null) {
            return;
        }
        float floatValue = ((Float) value).floatValue();
        int seekBarWidth = getSeekBarWidth();
        if (seekBarWidth <= 0) {
            return;
        }
        float oldProgress = mProgress;
        float scale = isLayoutRtl()
                ? (seekBarWidth - floatValue) / seekBarWidth
                : floatValue / seekBarWidth;
        setFlingScale(scale);
        executeFlingGlitterEffectAnim(behavior, oldScale);
        setLocalProgress(getProgressLimit(Math.round(((mMax - mMin) * mScale) + mMin)));
        invalidate();
        if (oldProgress != mProgress) {
            mLastX = floatValue + getStart();
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
            }
        }
    }

    private float getPercent(int progress) {
        float range = getMax() - getMin();
        if (range <= 0f) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, (progress - getMin()) / range));
    }

    protected int getProgressLimit(int progress) {
        int range = mMax - mMin;
        return Math.max(mMin - range, Math.min(progress, mMax + range));
    }

    protected int getRealProgress(int progress) {
        return Math.max(mMin, Math.min(progress, mMax));
    }

    protected float getRealScale(float scale) {
        return Math.max(0f, Math.min(scale, 1f));
    }

    protected void updatePixPerProgress() {
        int range = mMax - mMin;
        mPixPerProgress = range > 0 ? (getTrackEnd() - getTrackStart()) / range : 0f;
    }

    protected void updateScale() {
        int range = mMax - mMin;
        float scale = range > 0 ? (mProgress - mMin) / (float) range : 0f;
        mScale = scale;
        mDrawProgressScale = scale;
        mVisualProgress = mProgress;
    }

    protected void setLocalProgress(int progress) {
        mProgress = progress;
        mRealProgress = getRealProgress(progress);
        super.setProgress(mRealProgress);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxToMinLinearGradient = null;
        mMinToMaxLinearGradient = null;
        mStartDragging = false;
        stopPhysicsMove();
        updateBehavior();
        updatePixPerProgress();
    }

    private void updateBehavior() {
        if (!mPhysicsEnable || mPhysicalAnimator == null || mFlingBehavior == null) {
            return;
        }
        int seekBarWidth = getSeekBarWidth();
        mFlingBehavior.setValueRange(0.0f, (float) seekBarWidth);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Leapy modified 2026-08-14: Exact decoded OPPO COUISeekBar.setEnabled().
        mProgressColor = getColor(this, mProgressColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_progress_selector));
        mBackgroundColor = getColor(this, mBackgroundColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_background_selector));
        mThumbColor = getColor(this, mThumbColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_thumb_selector));
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        VibrateUtils.registerHapticObserver(getContext());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPhysicsMove();
        VibrateUtils.unRegisterHapticObserver();
    }

    private void requestParentDisallowIntercept(boolean disallow) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    public void calculateTouchDeformationValue() {
        if (!mDeformation) {
            resetDeformationValue();
            return;
        }
        if (mScale > 1f) {
            mDeformationAnim.animateToFinalPosition(((mScale - 1f) / 5f) * 100000f);
        } else if (mScale < 0f) {
            mDeformationAnim.animateToFinalPosition((Math.abs(mScale) / 5f) * 100000f);
        } else {
            resetDeformationValue();
        }
    }

    private void calculateFlingDeformationValue(float scale) {
        if (scale > 1.0f) {
            mDeformationAnim.animateToFinalPosition((scale - 1.0f) * 100000.0f);
        } else if (scale >= 0.0f) {
            resetDeformationValue();
        } else {
            mDeformationAnim.animateToFinalPosition(Math.abs(scale) * 100000.0f);
        }
    }

    private void setDeformationScale(float scale) {
        if (scale > 1.0f) {
            scale = ((scale - 1.0f) * 5.0f) + 1.0f;
        } else if (scale < 0.0f) {
            scale *= 5.0f;
        }
        float clamped = Math.max(-1.0f, Math.min(scale, 2.0f));
        mScale = clamped;
        mDrawProgressScale = clamped;
    }

    private void updateSpringDeformationValues(float deformScale) {
        if (mScale > 1f) {
            double value = deformScale;
            mHeightBottomDeformedUpValue = computeValue(value, mMaxMovingDistance);
            mHeightTopDeformedUpValue = computeValue(value, mMaxMovingDistance + mMaxHeightDeformedValue);
            mHeightTopDeformedDownValue = 0f;
            mHeightBottomDeformedDownValue = 0f;
            mWidthDeformedValue = computeValue(value, mMaxWidthDeformedValue);
        } else if (mScale < 0f) {
            double value = deformScale;
            mHeightTopDeformedDownValue = computeValue(value, mMaxMovingDistance);
            mHeightBottomDeformedDownValue = computeValue(value, mMaxMovingDistance + mMaxHeightDeformedValue);
            mHeightTopDeformedUpValue = 0f;
            mHeightBottomDeformedUpValue = 0f;
            mWidthDeformedValue = computeValue(value, mMaxWidthDeformedValue);
        }
        if (mOnDeformedListener != null) {
            mOnDeformedListener.onHeightDeformedChanged(
                    mHeightTopDeformedUpValue - mHeightTopDeformedDownValue,
                    mHeightBottomDeformedUpValue - mHeightBottomDeformedDownValue);
        }
        invalidate();
    }

    public float computeValue(double value, float max) {
        return (float) (max * (1.0d - Math.exp(value * -11.5d)));
    }

    public void resetDeformationValue() {
        mHeightBottomDeformedDownValue = 0f;
        mHeightBottomDeformedUpValue = 0f;
        mHeightTopDeformedDownValue = 0f;
        mHeightTopDeformedUpValue = 0f;
        mWidthDeformedValue = 0f;
    }

    private void clearDeformationValue() {
        if (mScale <= 0.0f || mScale >= 1.0f) {
            return;
        }
        resetDeformationValue();
    }

    public void resetBumpingEdges() {
        if (!mStartFromMiddle) {
            if (mScale < 1.0f) {
                mIsBumpingEdges = false;
            }
            return;
        }
        if (mScale < 1.0f && mScale > 0.0f) {
            mIsBumpingEdges = false;
        }
    }

    public void setTouchScale(float scale, boolean updateDrawProgressScale) {
        if (!mDeformation) {
            float clamped = Math.max(0f, Math.min(scale, 1f));
            mScale = clamped;
            if (updateDrawProgressScale) {
                mDrawProgressScale = clamped;
            }
            return;
        }
        float clamped = Math.max(-1f, Math.min(scale, 2f));
        mScale = clamped;
        if (updateDrawProgressScale) {
            mDrawProgressScale = clamped;
        }
        calculateTouchDeformationValue();
        if (mOnDeformedListener != null) {
            DeformedValueBean bean = new DeformedValueBean(mHeightBottomDeformedUpValue,
                    mHeightTopDeformedUpValue, mWidthDeformedValue,
                    mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            bean.setScale(mScale);
            bean.setDrawProgressScale(mDrawProgressScale);
            mOnDeformedListener.onScaleChanged(bean);
        }
    }

    public void setDeformedListener(OnDeformedListener listener) {
        mOnDeformedListener = listener;
    }

    public int getColor(View view, ColorStateList colorStateList, int defaultColor) {
        return colorStateList == null
                ? defaultColor
                : colorStateList.getColorForState(view.getDrawableState(), defaultColor);
    }

    public void setSeekBarBackgroundColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mBackgroundColorStateList = colorStateList;
            mBackgroundColor = getColor(this, colorStateList,
                    COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_background_color_normal));
            invalidate();
        }
    }

    public void setProgressColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mProgressColorStateList = colorStateList;
            mProgressColor = getColor(this, colorStateList,
                    COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_progress_color_normal));
            invalidate();
        }
    }

    public void setThumbColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mThumbColorStateList = colorStateList;
            mThumbColor = getColor(this, colorStateList,
                    COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_thumb_color));
            invalidate();
        }
    }

    public void setBackgroundRoundCornerWeight(float weight) {
        mBackgroundRoundCornerWeight = weight;
        invalidate();
    }

    public void setProgressRoundCornerWeight(float weight) {
        mProgressRoundCornerWeight = weight;
        invalidate();
    }

    public void setMaxHeightDeformed(float value) {
        mMaxHeightDeformedValue = value;
    }

    public void setMaxWidthDeformed(float value) {
        mMaxWidthDeformedValue = value;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mOnSeekBarChangeListener = listener;
    }

    public synchronized void setProgress(int progress) {
        setProgress(progress, false);
    }

    public void setProgress(int progress, boolean animate) {
        setProgress(progress, animate, false);
    }

    public void setProgress(int progress, boolean animate, boolean fromUser) {
        if (mFlexibleFollowHandAnim != null) {
            mFlexibleFollowHandAnim.cancel();
        }
        mOldProgress = mProgress;
        int clamped = Math.max(mMin, Math.min(progress, mMax));
        if (mOldProgress != clamped) {
            if (animate) {
                startTransitionAnim(clamped, fromUser, false);
            } else {
                setLocalProgress(clamped);
                mOldProgress = clamped;
                updateScale();
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(this, getRealProgress(clamped), fromUser);
                }
                invalidate();
            }
            resetDeformationValue();
        }
    }

    protected void setProgressInternal(int progress, boolean fromUser, boolean notify) {
        int clamped = fromUser && mDeformation
                ? getProgressLimit(progress)
                : Math.max(mMin, Math.min(mMax, progress));
        if (mProgress == clamped && mRealProgress == getRealProgress(clamped)) {
            return;
        }
        int old = mRealProgress;
        mOldProgress = mProgress;
        setLocalProgress(clamped);
        if (fromUser && mDeformation) {
            mVisualProgress = mProgress;
        } else if (fromUser || !mPhysicsEnable) {
            updateScale();
        } else if (mProgressAnimator == null || !mProgressAnimator.isRunning()) {
            updateScale();
        }
        if (notify && mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, fromUser);
        }
        if (notify && fromUser && mEnableVibrator && old != mRealProgress) {
            performHapticFeedback(COUIHapticFeedbackConstants.STEPPING_SHORT_VIBRATE);
        }
        invalidate();
    }

    protected void animateProgressTo(int progress, boolean fromUser, boolean notify) {
        int clamped = Math.max(mMin, Math.min(mMax, progress));
        if (mProgress == clamped) {
            return;
        }
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
        }
        float start = mVisualProgress;
        setProgressInternal(clamped, fromUser, notify);
        float end = mProgress;
        mProgressAnimator = ValueAnimator.ofFloat(start, end);
        mProgressAnimator.setDuration(PROGRESS_ANIMATION_DURATION);
        mProgressAnimator.setInterpolator(PROGRESS_SCALE_INTERPOLATOR);
        mProgressAnimator.addUpdateListener(animation -> {
            mVisualProgress = (float) animation.getAnimatedValue();
            int range = Math.max(1, mMax - mMin);
            mDrawProgressScale = (mVisualProgress - mMin) / range;
            invalidate();
        });
        mProgressAnimator.start();
    }

    public synchronized int getProgress() {
        return mRealProgress;
    }

    public void setLocalMax(int max) {
        mMax = max;
        updatePixPerProgress();
        updateScale();
        super.setMax(max);
    }

    public void setLocalMin(int min) {
        mMin = min;
        updatePixPerProgress();
        updateScale();
        super.setMin(min);
    }

    public synchronized void setMax(int max) {
        if (max < getMin()) {
            max = getMin();
        }
        if (max != mMax) {
            setLocalMax(max);
            if (mProgress > max) {
                setProgress(max);
            }
        }
        invalidate();
    }

    public synchronized int getMax() {
        return super.getMax();
    }

    public synchronized void setMin(int min) {
        int clamped = min < 0 ? 0 : min;
        if (min > getMax()) {
            clamped = getMax();
        }
        if (clamped != mMin) {
            setLocalMin(clamped);
            if (mProgress < clamped) {
                setProgress(clamped);
            }
        }
        invalidate();
    }

    public synchronized int getMin() {
        return super.getMin();
    }

    public void setIncrement(int increment) {
        mIncrement = Math.abs(increment);
    }

    public float getMoveDamping() {
        return mDamping;
    }

    public int getMoveType() {
        return mMoveType;
    }

    public void setMoveDamping(float damping) {
        mDamping = damping;
    }

    public void setMoveType(int moveType) {
        mMoveType = moveType;
    }

    public void setShowText(boolean showText) {
        mShowText = showText;
        invalidate();
    }

    public void setSeekBarText(String text) {
        mText = text;
        invalidate();
    }

    public void setStartFromMiddle(boolean startFromMiddle) {
        mStartFromMiddle = startFromMiddle;
        setProgressRect();
        invalidate();
    }

    public void setSupportDeformation(boolean supportDeformation) {
        mDeformation = supportDeformation;
        if (!supportDeformation) {
            resetDeformationValue();
        }
        invalidate();
    }

    public void setPhysicalEnabled(boolean physicalEnabled) {
        if (physicalEnabled == mPhysicsEnable) {
            return;
        }
        if (!physicalEnabled) {
            stopPhysicsMove();
        }
        mPhysicsEnable = physicalEnabled;
        if (physicalEnabled) {
            updateBehavior();
        }
    }

    public void setFlingLinearDamping(float damping) {
        if (mPhysicsEnable) {
            mFlingLinearDamping = damping;
            if (mPhysicalAnimator != null && mFlingBehavior != null) {
                mFlingBehavior.setLinearDamping(damping);
            }
        }
    }

    public void setFlingProperty(float frequency, float dampingRatio) {
        if (mPhysicsEnable) {
            mFlingFrequency = frequency;
            mFlingDampingRatio = dampingRatio;
            if (mPhysicalAnimator != null && mFlingBehavior != null) {
                mFlingBehavior.setSpringProperty(frequency, dampingRatio);
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.progress = mProgress;
        state.min = mMin;
        state.max = mMax;
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mMin = savedState.min;
        mMax = savedState.max;
        mProgress = savedState.progress;
        mRealProgress = getRealProgress(mProgress);
        super.setMin(mMin);
        super.setMax(mMax);
        super.setProgress(mRealProgress);
        mVisualProgress = mProgress;
        updateScale();
        updatePixPerProgress();
    }

    public static class SavedState extends BaseSavedState {
        int progress;
        int min;
        int max;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
            min = source.readInt();
            max = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
            dest.writeInt(min);
            dest.writeInt(max);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
