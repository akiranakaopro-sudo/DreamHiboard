package com.coui.appcompat.seekbar;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
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

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.oplus.physicsengine.engine.AnimationListener;
import com.oplus.physicsengine.engine.AnimationUpdateListener;
import com.oplus.physicsengine.engine.BaseBehavior;
import com.oplus.physicsengine.engine.FlingBehavior;
import com.oplus.physicsengine.engine.FloatValueHolder;
import com.oplus.physicsengine.engine.PhysicalAnimator;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.oplus.graphics.OplusCanvas;
import com.oplus.graphics.OplusPathAdapter;

import java.math.BigDecimal;

public class COUIVerticalSeekBar extends AbsSeekBar
        implements AnimationListener, AnimationUpdateListener {
    private static final float BACKGROUND_RADIUS_SCALE = 6.0f;
    private static final long BUTTON_DEFORMATION_ANIM_DURATION = 350L;
    private static final int DURATION_150 = 150;
    private static final int DURATION_483 = 483;
    private static final float PROGRESS_RADIUS_SCALE = 4.0f;
    protected static final int RELEASE_ANIM_DURATION = 183;
    public static final int MOVE_BY_DEFAULT = 0;
    public static final int MOVE_BY_FINGER = 1;
    public static final int MOVE_BY_DISTANCE = 2;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_ENLARGE = 1;

    private static final Interpolator THUMB_ANIMATE_INTERPOLATOR =
            new COUIMoveEaseInterpolator();
    private static final Interpolator PROGRESS_SCALE_INTERPOLATOR =
            new COUIEaseInterpolator();

    public interface OnSeekBarChangeListener {
        void onProgressChanged(COUIVerticalSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(COUIVerticalSeekBar seekBar);

        void onStopTrackingTouch(COUIVerticalSeekBar seekBar);
    }

    public interface OnDeformedListener {
        default void onHeightDeformedChanged(float top, float bottom) {
        }

        default void onScaleChanged(DeformedValueBean deformedValueBean) {
        }
    }

    protected int mBackgroundColor;
    protected ColorStateList mBackgroundColorStateList;
    protected float mBackgroundEnlargeScale;
    protected Paint mBackgroundPaint;
    protected float mBackgroundRadius;
    protected float mBackgroundRadiusEnlargeScale;
    protected Rect mBackgroundRect;
    protected float mBackgroundRoundCornerWeight;
    protected float mBackgroundWidth;
    protected Rect mClipProgressRect;
    protected Path mClipProgressPath;
    protected float mCurBackgroundRadius;
    protected float mCurBackgroundWidth;
    protected float mCurPaddingVertical;
    protected float mCurProgressRadius;
    protected float mCurProgressWidth;
    protected boolean mEnableAdaptiveVibrator;
    protected boolean mEnableVibrator = true;
    protected boolean mHasMotorVibrator = true;
    protected float mHeightBottomDeformedDownValue;
    protected float mHeightBottomDeformedUpValue;
    protected float mHeightTopDeformedDownValue;
    protected float mHeightTopDeformedUpValue;
    protected boolean mIsDragging;
    protected boolean mIsSupportSmoothRoundCorner;
    protected int mLastProgress;
    protected float mLastY;
    protected Object mLinearMotorVibrator;
    protected int mMax = 100;
    protected int mMin;
    protected int mOldProgress;
    protected float mPaddingVertical;
    protected int mProgress;
    protected int mProgressColor;
    protected ColorStateList mProgressColorStateList;
    protected Paint mProgressPaint;
    protected float mProgressEnlargeScale;
    protected float mProgressRadius;
    protected float mProgressRadiusEnlargeScale;
    protected Rect mProgressRect;
    protected float mProgressRoundCornerWeight;
    protected float mProgressWidth;
    protected boolean mShowProgress;
    protected boolean mShowThumb;
    protected Rect mTempRect;
    protected int mThumbColor;
    protected ColorStateList mThumbColorStateList;
    protected float mThumbOutRadius;
    protected float mThumbOutRoundCornerWeight;
    protected float mThumbOutWidth;
    protected Paint mThumbPaint;
    protected float mTouchDownY;
    protected ValueAnimator mTouchEnlargeAnimator;
    protected ValueAnimator mTouchReleaseAnimator;
    protected int mTouchSlop;
    protected float mVerticalPaddingScale;

    private final Path mBackgroundPath = new Path();
    private Bitmap mThumbBitmap;
    private float mCustomProgressAnimDuration = -1.0f;
    private Interpolator mCustomProgressAnimInterpolator;
    private float mDamping;
    private float mDefaultPaddingVertical;
    private boolean mEnableCustomEnlarge;
    private ValueAnimator mEnlargeAnimator;
    private ValueAnimator mReleaseAnimator;
    private Spring mFastMoveSpring;
    private SpringConfig mFastMoveSpringConfig =
            SpringConfig.fromOrigamiTensionAndFriction(500.0d, 30.0d);
    private float mFastMoveScaleOffsetY;
    private FlingBehavior mFlingBehavior;
    private FloatValueHolder mFlingValueHolder;
    private float mFlingDampingRatio = 1.0f;
    private float mFlingFrequency = 2.8f;
    private float mFlingLinearDamping = 15.0f;
    private float mFlingVelocity;
    private int mIncrement = 1;
    private boolean mIsProgressFull;
    private boolean mIsPhysicsEnable;
    private boolean mIsSupportDeformation;
    private int mMaxHeight;
    private int mMaxBottomMovingDistance = 30;
    private int mMaxTopMovingDistance = 30;
    private float mMaxBottomHeightDeformedValue = 28.5f;
    private float mMaxTopHeightDeformedValue = 28.5f;
    private float mMaxWidthDeformedValue = 4.7f;
    private int mMoveType = MOVE_BY_FINGER;
    private OnDeformedListener mOnDeformedListener;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private PhysicalAnimator mPhysicalAnimator;
    private float mPixPerProgress;
    private int mRealProgress;
    private float mScale;
    private int mSeekbarMinWidth;
    private boolean mShowText;
    private boolean mShowTextShadow = true;
    private boolean mStartDragging;
    private String mTextContent;
    private int mTextColor;
    private TextPaint mTextPaint;
    private Paint.FontMetricsInt mFmi;
    private float mTextMarginTop;
    private TextDrawable mTextDrawable;
    private float mThumbPosition;
    private VelocityTracker mVelocityTracker;
    private float mWidthDeformedValue;
    private float mCurBottomDeformationValue;
    private float mCurTopDeformationValue;
    private ValueAnimator mClickAnimator;
    private ValueAnimator mButtonDeformationAnimator;
    private Drawable mInactiveTrackDrawable;

    public COUIVerticalSeekBar(Context context) {
        this(context, null);
    }

    public COUIVerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiVerticalSeekBarStyle);
    }

    public COUIVerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,
                COUIContextUtil.isCOUIDarkTheme(context) ? R.style.COUIVerticalSeekBar_Dark
                        : R.style.COUIVerticalSeekBar);
    }

    public COUIVerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mClipProgressPath = new Path();
        mClipProgressRect = new Rect();
        mProgressRect = new Rect();
        mTempRect = new Rect();
        mBackgroundRect = new Rect();
        mBackgroundPaint = createPaint();
        mProgressPaint = createPaint();
        mThumbPaint = createPaint();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextDrawable = new TextDrawable(context);
        readAttrs(context, attrs, defStyleAttr, defStyleRes);
        mHasMotorVibrator = VibrateUtils.isLinearMotorVersion(context);
        mIsSupportSmoothRoundCorner = RoundCornerUtil.getSmoothStyleType() == 0;
        initView();
        setThumbBitmap();
        ensureSize();
        setFocusable(true);
        setClickable(true);
    }

    private void readAttrs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mBackgroundColor = COUIContextUtil.getColor(context,
                R.color.coui_seekbar_background_color_normal);
        mProgressColor = COUIContextUtil.getColor(context,
                R.color.coui_seekbar_progress_color_normal);
        mThumbColor = mProgressColor;
        mTextColor = COUIContextUtil.getColor(context, R.color.coui_seekbar_text_color);
        mBackgroundRadius = getResources().getDimension(R.dimen.coui_seekbar_background_radius);
        mProgressRadius = getResources().getDimension(R.dimen.coui_seekbar_progress_radius);
        mDefaultPaddingVertical = getResources().getDimension(
                R.dimen.coui_vertical_seekbar_progress_padding_vertical);
        mPaddingVertical = mDefaultPaddingVertical;
        mSeekbarMinWidth = getResources().getDimensionPixelSize(
                R.dimen.coui_vertical_seekbar_view_min_width);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIVerticalSeekBar,
                defStyleAttr, defStyleRes);
        mMin = a.getInt(R.styleable.COUIVerticalSeekBar_android_min, mMin);
        mMax = a.getInt(R.styleable.COUIVerticalSeekBar_android_max, mMax);
        mProgress = a.getInt(R.styleable.COUIVerticalSeekBar_android_progress, mProgress);
        mEnableVibrator = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarEnableVibrator, true);
        mEnableAdaptiveVibrator = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarAdaptiveVibrator, false);
        mIsPhysicsEnable = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarPhysicsEnable, true);
        mShowProgress = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarShowProgress, true);
        mShowThumb = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarShowThumb, true);
        mIsProgressFull = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressFull, false);
        mShowText = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarShowText, false);
        mIsSupportDeformation = a.getBoolean(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarDeformation, false);
        mBackgroundColorStateList = a.getColorStateList(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarBackgroundColor);
        mProgressColorStateList = a.getColorStateList(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressColor);
        mThumbColorStateList = a.getColorStateList(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarThumbColor);
        mTextColor = a.getColor(R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarTextColor,
                mTextColor);
        mBackgroundRadius = a.getDimension(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarBackgroundRadius,
                mBackgroundRadius);
        mProgressRadius = a.getDimension(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressRadius,
                mProgressRadius);
        mBackgroundRoundCornerWeight = a.getFloat(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarBackgroundRoundCornerWeight,
                0.0f);
        mProgressRoundCornerWeight = a.getFloat(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressRoundCornerWeight,
                0.0f);
        mPaddingVertical = a.getDimension(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressPaddingVertical,
                mDefaultPaddingVertical);
        if (mPaddingVertical == 0.0f) {
            mPaddingVertical = mDefaultPaddingVertical;
        }
        mBackgroundWidth = a.getDimensionPixelSize(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarBackgroundWidth,
                (int) (mBackgroundRadius * 2.0f));
        mProgressWidth = a.getDimensionPixelSize(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressWidth,
                (int) (mProgressRadius * 2.0f));
        mSeekbarMinWidth = a.getDimensionPixelOffset(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarMinWidth,
                mSeekbarMinWidth);
        mMaxHeight = a.getDimensionPixelSize(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarMaxHeight, 0);
        mBackgroundEnlargeScale = a.getFloat(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarBackGroundEnlargeScale,
                BACKGROUND_RADIUS_SCALE);
        mProgressEnlargeScale = a.getFloat(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressEnlargeScale,
                PROGRESS_RADIUS_SCALE);
        mBackgroundRadiusEnlargeScale = a.getFloat(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarBackGroundRadiusEnlargeScale,
                mBackgroundEnlargeScale);
        mProgressRadiusEnlargeScale = a.getFloat(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarProgressRadiusEnlargeScale,
                mProgressEnlargeScale);
        mTextContent = a.getString(R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarText);
        mTextMarginTop = a.getDimension(
                R.styleable.COUIVerticalSeekBar_couiVerticalSeekBarTextMarginTop,
                getResources().getDimension(R.dimen.coui_vertical_seekbar_text_padding_top));
        a.recycle();
        updateStateColors();
        mProgress = Math.max(mMin, Math.min(mProgress, mMax));
        mRealProgress = getRealProgress(mProgress);
        super.setMin(mMin);
        super.setMax(mMax);
        super.setProgress(mRealProgress);
    }

    private Paint createPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        return paint;
    }

    private void initView() {
        refreshTextColor();
        mFmi = mTextPaint.getFontMetricsInt();
        mTouchEnlargeAnimator = getEnlargeAnimator(RELEASE_ANIM_DURATION,
                PROGRESS_SCALE_INTERPOLATOR);
        mTouchReleaseAnimator = getReleaseAnimator(RELEASE_ANIM_DURATION,
                PROGRESS_SCALE_INTERPOLATOR);
    }

    private void ensureSize() {
        resetProgressSize();
        mVerticalPaddingScale = mBackgroundEnlargeScale != 1.0f
                ? (getResources().getDimensionPixelSize(
                R.dimen.coui_vertical_seekbar_progress_pressed_padding_vertical)
                + (mBackgroundRadius * mBackgroundRadiusEnlargeScale)) / mPaddingVertical
                : 1.0f;
        mCurProgressRadius = mProgressRadius;
        mCurBackgroundRadius = mBackgroundRadius;
        mThumbOutRadius = mProgressRadius * mProgressRadiusEnlargeScale;
        mThumbOutRoundCornerWeight = mProgressRoundCornerWeight;
        mCurProgressWidth = mProgressWidth;
        mCurBackgroundWidth = mBackgroundWidth;
        mThumbOutWidth = mProgressWidth * mProgressEnlargeScale;
        mCurPaddingVertical = mPaddingVertical;
        updateBehavior();
        updatePixPerProgress();
        updateScale();
    }

    private void resetProgressSize() {
        if (mIsProgressFull) {
            mProgressRadius = mBackgroundRadius;
            mProgressRoundCornerWeight = mBackgroundRoundCornerWeight;
            mProgressWidth = mBackgroundWidth;
            mProgressEnlargeScale = mBackgroundEnlargeScale;
            mProgressRadiusEnlargeScale = mBackgroundRadiusEnlargeScale;
        }
    }

    private void refreshTextColor() {
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(getResources().getDimension(R.dimen.coui_seekbar_text_size));
        if (mShowTextShadow) {
            mTextPaint.setShadowLayer(25.0f, 0.0f, 8.0f, mTextColor);
        } else {
            mTextPaint.clearShadowLayer();
        }
        mFmi = mTextPaint.getFontMetricsInt();
    }

    private void updateStateColors() {
        mProgressColor = getColor(this, mProgressColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_progress_color_normal));
        mBackgroundColor = getColor(this, mBackgroundColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_background_color_normal));
        mThumbColor = getColor(this, mThumbColorStateList,
                COUIContextUtil.getColor(getContext(), R.color.coui_seekbar_progress_color_normal));
    }

    @Override
    public void draw(Canvas canvas) {
        int seekBarCenterX = getSeekBarCenterX();
        float seekBarHeight = getSeekBarHeightFloat();
        float progressDrawHeight;
        float clipHeight;
        float progressOffset;
        float backgroundOffset;
        if (mShowThumb) {
            float padding = mCurPaddingVertical;
            float thumbWidth = mThumbOutWidth;
            float thumbRadius = mThumbOutRadius;
            progressOffset = ((thumbWidth / 2.0f) - thumbRadius) + padding;
            clipHeight = seekBarHeight - (thumbWidth - (thumbRadius * 2.0f));
            float progressRadius = mCurProgressRadius;
            backgroundOffset = padding - progressRadius;
            progressDrawHeight = seekBarHeight + (progressRadius * 2.0f);
        } else {
            float padding = mCurPaddingVertical;
            float progressRadius = mCurProgressRadius;
            clipHeight = seekBarHeight + (progressRadius * 2.0f);
            backgroundOffset = padding - progressRadius;
            progressOffset = backgroundOffset;
            progressDrawHeight = clipHeight;
        }
        mClipProgressRect.left = (int) ((seekBarCenterX - (mCurProgressWidth / 2.0f))
                + mWidthDeformedValue);
        mClipProgressRect.right = (int) ((seekBarCenterX + (mCurProgressWidth / 2.0f))
                - mWidthDeformedValue);
        float progressBottom = getPaddingTop() + progressOffset + clipHeight;
        float thumbPosition = progressBottom - (getRealScale(mScale) * clipHeight);
        mThumbPosition = thumbPosition;
        float clipTop = (getPaddingTop() + backgroundOffset + mHeightTopDeformedDownValue)
                - mHeightTopDeformedUpValue;
        mClipProgressRect.top = (int) clipTop;
        mClipProgressRect.bottom = (int) (((clipTop + progressDrawHeight)
                + mHeightTopDeformedUpValue - mHeightBottomDeformedUpValue)
                + mHeightBottomDeformedDownValue - mHeightTopDeformedDownValue);
        setBackgroundRect(seekBarCenterX);
        setProgressRect(seekBarCenterX, thumbPosition, progressBottom);
        setDrawableBounds(mInactiveTrackDrawable);
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawInactiveTrack(canvas);
        drawActiveTrack(canvas);
    }

    public void drawInactiveTrack(Canvas canvas) {
        boolean smooth = mIsSupportSmoothRoundCorner && mBackgroundRoundCornerWeight != 0.0f;
        if (mInactiveTrackDrawable != null) {
            drawBackgroundByClip(canvas, smooth);
        } else {
            drawBackgroundByPaint(canvas, smooth);
        }
    }

    public void drawActiveTrack(Canvas canvas) {
        if (mShowProgress) {
            drawProgress(canvas);
        }
        if (mShowThumb) {
            drawThumb(canvas);
        }
        if (mShowText) {
            drawText(canvas);
        }
    }

    private void drawBackgroundByClip(Canvas canvas, boolean smooth) {
        mBackgroundPath.reset();
        if (smooth) {
            new OplusPathAdapter(mBackgroundPath, RoundCornerUtil.getSmoothStyleType())
                    .addSmoothRoundRect(mBackgroundRect.left,
                    mBackgroundRect.top, mBackgroundRect.right, mBackgroundRect.bottom,
                    mCurBackgroundRadius, mCurBackgroundRadius, mProgressRoundCornerWeight,
                    Path.Direction.CCW);
        } else {
            mBackgroundPath.addRoundRect(mBackgroundRect.left, mBackgroundRect.top,
                    mBackgroundRect.right, mBackgroundRect.bottom, mCurBackgroundRadius,
                    mCurBackgroundRadius, Path.Direction.CCW);
        }
        int save = canvas.save();
        canvas.clipPath(mBackgroundPath);
        mInactiveTrackDrawable.draw(canvas);
        canvas.restoreToCount(save);
    }

    private void drawBackgroundByPaint(Canvas canvas, boolean smooth) {
        mBackgroundPaint.setColor(mBackgroundColor);
        if (smooth) {
            new OplusCanvas(canvas).drawSmoothRoundRect(mBackgroundRect.left,
                    mBackgroundRect.top, mBackgroundRect.right, mBackgroundRect.bottom,
                    mCurBackgroundRadius, mCurBackgroundRadius, mBackgroundPaint,
                    mBackgroundRoundCornerWeight);
        } else {
            canvas.drawRoundRect(mBackgroundRect.left, mBackgroundRect.top,
                    mBackgroundRect.right, mBackgroundRect.bottom, mCurBackgroundRadius,
                    mCurBackgroundRadius, mBackgroundPaint);
        }
    }

    private void drawProgress(Canvas canvas) {
        boolean smooth = mIsSupportSmoothRoundCorner && mProgressRoundCornerWeight != 0.0f;
        mProgressPaint.setColor(mProgressColor);
        mClipProgressPath.reset();
        if (smooth) {
            new OplusPathAdapter(mClipProgressPath, RoundCornerUtil.getSmoothStyleType())
                    .addSmoothRoundRect(mClipProgressRect.left,
                    mClipProgressRect.top, mClipProgressRect.right, mClipProgressRect.bottom,
                    mCurProgressRadius, mCurProgressRadius, mProgressRoundCornerWeight,
                    Path.Direction.CCW);
        } else {
            mClipProgressPath.addRoundRect(mClipProgressRect.left, mClipProgressRect.top,
                    mClipProgressRect.right, mClipProgressRect.bottom, mCurProgressRadius,
                    mCurProgressRadius, Path.Direction.CCW);
        }
        int save = canvas.save();
        canvas.clipPath(mClipProgressPath);
        if (mShowThumb) {
            int oldTop = mProgressRect.top;
            int oldBottom = mProgressRect.bottom;
            mProgressRect.top = (int) (oldTop - (mThumbOutWidth / 2.0f));
            mProgressRect.bottom = (int) (oldBottom + (mThumbOutWidth / 2.0f));
            if (smooth) {
                new OplusCanvas(canvas).drawSmoothRoundRect(mProgressRect.left,
                        mProgressRect.top, mProgressRect.right, mProgressRect.bottom,
                        mCurProgressRadius, mCurProgressRadius, mProgressPaint,
                        mProgressRoundCornerWeight);
            } else {
                canvas.drawRoundRect(mProgressRect.left, mProgressRect.top,
                        mProgressRect.right, mProgressRect.bottom, mCurProgressRadius,
                        mCurProgressRadius, mProgressPaint);
            }
            mProgressRect.top = oldTop;
            mProgressRect.bottom = oldBottom;
        } else {
            canvas.drawRect(mProgressRect, mProgressPaint);
        }
        canvas.restoreToCount(save);
    }

    private void drawThumb(Canvas canvas) {
        float top = mThumbPosition - (mThumbOutWidth / 2.0f);
        float bottom = mThumbPosition + (mThumbOutWidth / 2.0f);
        int centerX = getSeekBarCenterX();
        if (getThumb() != null && mThumbBitmap != null) {
            canvas.drawBitmap(mThumbBitmap, centerX - (mThumbOutWidth / 2.0f), top, mThumbPaint);
            return;
        }
        mThumbPaint.setColor(mThumbColor);
        if (mIsSupportSmoothRoundCorner && mThumbOutRoundCornerWeight != 0.0f) {
            new OplusCanvas(canvas).drawSmoothRoundRect(centerX - (mThumbOutWidth / 2.0f),
                    top, centerX + (mThumbOutWidth / 2.0f), bottom, mThumbOutRadius,
                    mThumbOutRadius, mThumbPaint, mThumbOutRoundCornerWeight);
        } else {
            canvas.drawRoundRect(centerX - (mThumbOutWidth / 2.0f), top,
                    centerX + (mThumbOutWidth / 2.0f), bottom, mThumbOutRadius,
                    mThumbOutRadius, mThumbPaint);
        }
    }

    private void drawText(Canvas canvas) {
        if (TextUtils.isEmpty(mTextContent)) {
            return;
        }
        canvas.drawText(mTextContent,
                (getWidth() - mTextPaint.measureText(mTextContent)) / 2.0f,
                ((mTextMarginTop - mFmi.ascent) + mHeightTopDeformedDownValue)
                        - mHeightTopDeformedUpValue,
                mTextPaint);
    }

    private void setBackgroundRect(int centerX) {
        float top = (getPaddingTop() + mCurPaddingVertical) - mCurBackgroundRadius;
        float bottom = ((getHeight() - getPaddingBottom()) - mCurPaddingVertical)
                + mCurBackgroundRadius;
        mBackgroundRect.set((int) (centerX - ((mCurBackgroundWidth / 2.0f)
                        - mWidthDeformedValue)),
                (int) ((top - mHeightTopDeformedUpValue) + mHeightTopDeformedDownValue),
                (int) (centerX + ((mCurBackgroundWidth / 2.0f) - mWidthDeformedValue)),
                (int) ((bottom - mHeightBottomDeformedUpValue)
                        + mHeightBottomDeformedDownValue));
    }

    private void setProgressRect(int centerX, float thumbPosition, float bottom) {
        mProgressRect.set((int) (centerX - ((mCurProgressWidth / 2.0f)
                        - mWidthDeformedValue)),
                (int) ((thumbPosition - mHeightTopDeformedUpValue)
                        + mHeightBottomDeformedDownValue),
                (int) (centerX + ((mCurProgressWidth / 2.0f) - mWidthDeformedValue)),
                (int) ((bottom - mHeightBottomDeformedUpValue)
                        + mHeightBottomDeformedDownValue));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int minWidth = mSeekbarMinWidth + getPaddingStart() + getPaddingEnd();
        if (MeasureSpec.EXACTLY != widthMode || width < minWidth) {
            width = minWidth;
        }
        if (mMaxHeight > 0 && height > mMaxHeight) {
            height = mMaxHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                handleMotionEventUp(event);
                return true;
            }
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isMoveFollowHand()) {
                    cancelAnim(mClickAnimator);
                }
                if (!isDeformationFling()) {
                    stopPhysicsMove();
                }
                if (mIsPhysicsEnable && mPhysicalAnimator == null) {
                    initPhysicsAnimator(getContext());
                }
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(event);
                mIsDragging = false;
                mStartDragging = false;
                handleMotionEventDown(event);
                setPressed(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                clearDeformationValue();
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(event);
                handleMotionEventMove(event);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(1000, 8000.0f);
                    mFlingVelocity = mVelocityTracker.getYVelocity();
                }
                recycleVelocityTracker();
                handleMotionEventUp(event);
                return true;
            default:
                return true;
        }
    }

    public void handleMotionEventDown(MotionEvent event) {
        mTouchDownY = event.getY();
        mLastY = event.getY();
    }

    public void handleMotionEventMove(MotionEvent event) {
        if (mIsDragging && mStartDragging) {
            cancelAnim(mButtonDeformationAnimator);
            if (mMoveType == MOVE_BY_FINGER) {
                trackTouchEventByFinger(event);
            } else {
                trackTouchEvent(event);
            }
            return;
        }
        float y = event.getY();
        if (Math.abs(y - mTouchDownY) > mTouchSlop) {
            if (!isMoveFollowHand()) {
                cancelAnim(mClickAnimator);
            }
            startDrag();
            touchAnim();
            mLastY = y;
            if (isMoveFollowHand()) {
                invalidateProgress(event);
            }
        }
    }

    public void handleMotionEventUp(MotionEvent event) {
        getFastMoveSpring().setEndValue(0.0d);
        if (!mIsDragging) {
            if (isEnabled() && touchInSeekBar(event, this) && isMoveFollowHand()) {
                animForClick(event.getY());
            }
            return;
        }
        mIsDragging = false;
        mStartDragging = false;
        if (!mIsPhysicsEnable || Math.abs(mFlingVelocity) < 100.0f) {
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
        if (mIsDragging) {
            mIsDragging = false;
            mStartDragging = false;
        }
        setPressed(false);
        releaseAnim();
    }

    private void trackTouchEvent(MotionEvent event) {
        float delta = (mLastY - event.getY()) * calculateDamping();
        setTouchScale(mScale + (delta / Math.max(1.0f, getSeekBarHeightFloat())));
        int oldProgress = mProgress;
        int oldRealProgress = mRealProgress;
        setLocalProgress(getProgressLimit(Math.round(((mMax - mMin) * mScale) + mMin)));
        invalidate();
        if (oldProgress != mProgress) {
            mLastY = event.getY();
            if (oldRealProgress != mRealProgress) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
                }
                performFeedback();
            }
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(100);
            startFastMoveAnimation(mVelocityTracker.getYVelocity());
        }
    }

    private void trackTouchEventByFinger(MotionEvent event) {
        float y = mLastY - ((mLastY - event.getY()) * calculateDamping());
        int roundedY = Math.round(y);
        int height = (getHeight() - getPaddingTop()) - getPaddingBottom();
        float scale;
        if (roundedY > getHeight() - getPaddingBottom()) {
            scale = 0.0f;
        } else if (roundedY < getPaddingTop()) {
            scale = 1.0f;
        } else if (height > 0) {
            scale = ((getHeight() - getPaddingBottom()) - roundedY) / (float) height;
        } else {
            scale = 0.0f;
        }
        mScale = Math.max(0.0f, Math.min(scale, 1.0f));
        int progress = getProgressLimit(Math.round(((getMax() - getMin()) * mScale) + getMin()));
        int oldProgress = mProgress;
        int oldRealProgress = mRealProgress;
        setLocalProgress(progress);
        invalidate();
        if (oldProgress != mProgress) {
            mLastY = roundedY;
            if (oldRealProgress != mRealProgress) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
                }
                performFeedback();
            }
        }
    }

    private void invalidateProgress(MotionEvent event) {
        float height = getSeekBarHeightFloat();
        float progressRadius = mCurProgressRadius;
        float totalHeight = height + (2.0f * progressRadius);
        float scale = totalHeight > 0.0f
                ? (((getHeight() - getPaddingBottom()) - (mCurPaddingVertical - progressRadius))
                - event.getY()) / totalHeight
                : 0.0f;
        mScale = Math.max(0.0f, Math.min(scale, 1.0f));
        int progress = getProgressLimit(Math.round(((mMax - mMin) * mScale) + mMin));
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

    public void animForClick(float y) {
        float seekBarHeight = getSeekBarHeightFloat();
        float progressRadius = mCurProgressRadius;
        float totalHeight = seekBarHeight + (2.0f * progressRadius);
        float scale = totalHeight > 0.0f
                ? (((getHeight() - getPaddingBottom()) - (mCurPaddingVertical - progressRadius))
                - y) / totalHeight
                : 0.0f;
        startTransitionAnim(getProgressLimit(Math.round(
                (scale * (getMax() - getMin())) + getMin())), true);
    }

    public void startTransitionAnim(int progress, boolean fromUser) {
        int startProgress = mProgress;
        if (mClickAnimator == null) {
            mClickAnimator = createClickAnimator(fromUser);
        } else {
            cancelAnim(mClickAnimator);
        }
        if (!fromUser && mCustomProgressAnimDuration != -1.0f) {
            mClickAnimator.setDuration((long) mCustomProgressAnimDuration);
        } else {
            int range = mMax - mMin;
            float ratio = range > 0 ? Math.abs(progress - startProgress) / (float) range : 0.0f;
            long duration = (long) (ratio * DURATION_483);
            if (duration < DURATION_150) {
                duration = DURATION_150;
            }
            mClickAnimator.setDuration(duration);
        }
        mClickAnimator.setValues(PropertyValuesHolder.ofFloat("progressLength",
                startProgress * mPixPerProgress, progress * mPixPerProgress));
        mClickAnimator.start();
    }

    private ValueAnimator createClickAnimator(final boolean fromUser) {
        ValueAnimator animator = new ValueAnimator();
        if (fromUser || mCustomProgressAnimInterpolator == null) {
            animator.setInterpolator(THUMB_ANIMATE_INTERPOLATOR);
        } else {
            animator.setInterpolator(mCustomProgressAnimInterpolator);
        }
        animator.addUpdateListener(animation -> {
            float progressLength = (Float) animation.getAnimatedValue("progressLength");
            int progress = getProgressLimit(Math.round((progressLength / mPixPerProgress)));
            setLocalProgress(progress);
            mScale = getSeekBarHeightFloat() > 0.0f
                    ? (progressLength - (mMin * mPixPerProgress)) / getSeekBarHeightFloat()
                    : 0.0f;
            invalidate();
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onStartTrackingTouch(fromUser);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(
                            COUIVerticalSeekBar.this, mRealProgress, fromUser);
                }
                onStopTrackingTouch(fromUser);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(
                            COUIVerticalSeekBar.this, mRealProgress, fromUser);
                }
                onStopTrackingTouch(fromUser);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return animator;
    }

    private void setTouchScale(float scale) {
        if (!mIsSupportDeformation) {
            mScale = Math.max(0.0f, Math.min(scale, 1.0f));
            return;
        }
        mScale = Math.max(-1.0f, Math.min(scale, 2.0f));
        calculateTouchDeformationValue();
        if (mOnDeformedListener != null) {
            DeformedValueBean bean = new DeformedValueBean(mHeightBottomDeformedUpValue,
                    mHeightTopDeformedUpValue, mWidthDeformedValue,
                    mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            bean.setScale(mScale);
            mOnDeformedListener.onScaleChanged(bean);
        }
    }

    public void calculateTouchDeformationValue() {
        if (!mIsSupportDeformation) {
            resetDeformationValue();
            return;
        }
        if (mScale >= 1.0f) {
            double value = (mScale - 1.0f) / 5.0f;
            mHeightBottomDeformedUpValue = computeValue(value, mMaxBottomMovingDistance);
            mHeightTopDeformedUpValue = computeValue(value,
                    mMaxTopMovingDistance + mMaxTopHeightDeformedValue);
            mWidthDeformedValue = computeValue(value, mMaxWidthDeformedValue);
            heightDeformedChanged();
            return;
        }
        if (mScale <= 0.0f) {
            double value = Math.abs(mScale) / 5.0f;
            mHeightTopDeformedDownValue = computeValue(value, mMaxTopMovingDistance);
            mHeightBottomDeformedDownValue = computeValue(value,
                    mMaxBottomMovingDistance + mMaxBottomHeightDeformedValue);
            mWidthDeformedValue = computeValue(value, mMaxWidthDeformedValue);
            heightDeformedChanged();
        }
    }

    public float computeValue(double value, float max) {
        return (int) (max * (1.0d - Math.exp(value * -11.5d)));
    }

    public void resetDeformationValue() {
        if (mIsSupportDeformation) {
            mHeightBottomDeformedDownValue = 0.0f;
            mHeightBottomDeformedUpValue = 0.0f;
            mHeightTopDeformedDownValue = 0.0f;
            mHeightTopDeformedUpValue = 0.0f;
            mWidthDeformedValue = 0.0f;
            heightDeformedChanged();
        }
    }

    public void startDrag() {
        setPressed(true);
        onStartTrackingTouch(true);
        attemptClaimDrag();
    }

    public void onStartTrackingTouch(boolean notify) {
        if (notify) {
            mIsDragging = true;
            mStartDragging = true;
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    public void onStopTrackingTouch(boolean notify) {
        if (notify && mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private boolean isMoveFollowHand() {
        return mMoveType != MOVE_BY_DISTANCE;
    }

    private float calculateDamping() {
        return mDamping == 0.0f ? 0.4f : mDamping;
    }

    public boolean touchInSeekBar(MotionEvent event, View view) {
        float x = event.getX();
        float y = event.getY();
        return x >= 0.0f && x <= view.getWidth()
                && y >= view.getPaddingTop()
                && y <= view.getHeight() - view.getPaddingBottom();
    }

    private void attemptClaimDrag() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void releaseParentDrag() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(false);
        }
    }

    public void touchAnim() {
        if (mEnableCustomEnlarge) {
            return;
        }
        cancelAnim(mTouchEnlargeAnimator);
        setEnlargeAnimatorValues(mTouchEnlargeAnimator);
        mTouchEnlargeAnimator.start();
    }

    public void releaseAnim() {
        if (mEnableCustomEnlarge) {
            return;
        }
        cancelAnim(mTouchEnlargeAnimator);
        cancelAnim(mTouchReleaseAnimator);
        setReleaseAnimatorValues(mTouchReleaseAnimator);
        mTouchReleaseAnimator.start();
        releaseParentDrag();
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
        mCurProgressRadius = (Float) animator.getAnimatedValue("progressRadius");
        mCurBackgroundRadius = (Float) animator.getAnimatedValue("backgroundRadius");
        mCurProgressWidth = (Float) animator.getAnimatedValue("progressWidth");
        mCurBackgroundWidth = (Float) animator.getAnimatedValue("backgroundWidth");
        mCurPaddingVertical = (Float) animator.getAnimatedValue("animatePadding");
    }

    private void setEnlargeAnimatorValues(ValueAnimator animator) {
        animator.setValues(
                PropertyValuesHolder.ofFloat("progressRadius", mCurProgressRadius,
                        mProgressRadius * mProgressRadiusEnlargeScale),
                PropertyValuesHolder.ofFloat("backgroundRadius", mCurBackgroundRadius,
                        mBackgroundRadius * mBackgroundRadiusEnlargeScale),
                PropertyValuesHolder.ofFloat("progressWidth", mCurProgressWidth,
                        mProgressWidth * mProgressEnlargeScale),
                PropertyValuesHolder.ofFloat("backgroundWidth", mCurBackgroundWidth,
                        mBackgroundWidth * mBackgroundEnlargeScale),
                PropertyValuesHolder.ofFloat("animatePadding", mCurPaddingVertical,
                        mPaddingVertical * mVerticalPaddingScale));
    }

    private void setReleaseAnimatorValues(ValueAnimator animator) {
        animator.setValues(
                PropertyValuesHolder.ofFloat("progressRadius", mCurProgressRadius,
                        mProgressRadius),
                PropertyValuesHolder.ofFloat("backgroundRadius", mCurBackgroundRadius,
                        mBackgroundRadius),
                PropertyValuesHolder.ofFloat("progressWidth", mCurProgressWidth,
                        mProgressWidth),
                PropertyValuesHolder.ofFloat("backgroundWidth", mCurBackgroundWidth,
                        mBackgroundWidth),
                PropertyValuesHolder.ofFloat("animatePadding", mCurPaddingVertical,
                        mPaddingVertical));
    }

    private void cancelAnim(ValueAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void initPhysicsAnimator(Context context) {
        mPhysicalAnimator = PhysicalAnimator.create(context);
        mFlingValueHolder = new FloatValueHolder(0.0f);
        float height = getNormalSeekBarHeightFloat();
        FlingBehavior behavior = new FlingBehavior(4, 0.0f, height);
        behavior.withProperty(mFlingValueHolder);
        behavior.setSpringProperty(mFlingFrequency, mFlingDampingRatio);
        mFlingBehavior = behavior;
        behavior.setLinearDamping(mFlingLinearDamping);
        mPhysicalAnimator.addBehavior(mFlingBehavior);
        mPhysicalAnimator.addAnimationListener(mFlingBehavior, this);
        mPhysicalAnimator.addAnimationUpdateListener(mFlingBehavior, this);
    }

    private void updateBehavior() {
        if (!mIsPhysicsEnable || mPhysicalAnimator == null || mFlingBehavior == null) {
            return;
        }
        mFlingBehavior.setValueRange(0.0f, getNormalSeekBarHeightFloat());
    }

    private boolean isDeformationFling() {
        return mIsSupportDeformation
                && (mScale > 1.0f || mScale < 0.0f)
                && mPhysicalAnimator != null
                && mPhysicalAnimator.isFrameScheduled();
    }

    private void clearDeformationValue() {
        if (mProgress > mMin && mProgress < mMax) {
            resetDeformationValue();
        }
    }

    private void flingBehaviorAfterDeformationDrag() {
        if (mFlingValueHolder == null || mFlingBehavior == null || !mIsSupportDeformation) {
            return;
        }
        if (mScale > 1.0f || mScale < 0.0f) {
            mLastProgress = 0;
            float height = getNormalSeekBarHeightFloat();
            int range = mMax - mMin;
            float pixPerProgress = range > 0 ? height / range : 0.0f;
            float startValue = range * getDeformationFlingScale() * pixPerProgress;
            mFlingValueHolder.setValue(startValue);
            mFlingBehavior.start();
        }
    }

    private void flingBehaviorAfterEndDrag(float velocity) {
        if (mFlingValueHolder == null || mFlingBehavior == null) {
            return;
        }
        mLastProgress = 0;
        float height = getNormalSeekBarHeightFloat();
        int range = mMax - mMin;
        float pixPerProgress = range > 0 ? height / range : 0.0f;
        if (mIsSupportDeformation) {
            mFlingValueHolder.setValue(range * getDeformationFlingScale() * pixPerProgress);
        } else {
            mFlingValueHolder.setValue((mProgress - mMin) * pixPerProgress);
        }
        mFlingBehavior.start(-velocity);
    }

    private float getDeformationFlingScale() {
        return mScale > 1.0f
                ? ((mScale - 1.0f) / 5.0f) + 1.0f
                : mScale < 0.0f ? mScale / 5.0f : mScale;
    }

    private void calculateFlingDeformationValue(float scale) {
        if (scale > 1.0f) {
            double value = scale - 1.0f;
            mHeightBottomDeformedUpValue = computeValue(value, mMaxBottomMovingDistance);
            mHeightTopDeformedUpValue = computeValue(value,
                    mMaxTopMovingDistance + mMaxTopHeightDeformedValue);
            mWidthDeformedValue = computeValue(value, mMaxWidthDeformedValue);
            heightDeformedChanged();
            return;
        }
        if (scale >= 0.0f) {
            resetDeformationValue();
            return;
        }
        double value = Math.abs(scale);
        mHeightTopDeformedDownValue = computeValue(value, mMaxTopMovingDistance);
        mHeightBottomDeformedDownValue = computeValue(value,
                mMaxBottomMovingDistance + mMaxBottomHeightDeformedValue);
        mWidthDeformedValue = computeValue(value, mMaxWidthDeformedValue);
        heightDeformedChanged();
    }

    private void setDeformationScale(float scale) {
        if (scale > 1.0f) {
            scale = ((scale - 1.0f) * 5.0f) + 1.0f;
        } else if (scale < 0.0f) {
            scale *= 5.0f;
        }
        mScale = Math.max(-1.0f, Math.min(scale, 2.0f));
    }

    private void setFlingScale(float scale) {
        if (!mIsSupportDeformation) {
            mScale = Math.max(0.0f, Math.min(scale, 1.0f));
            return;
        }
        calculateFlingDeformationValue(scale);
        setDeformationScale(scale);
        if (mOnDeformedListener != null) {
            DeformedValueBean bean = new DeformedValueBean(mHeightBottomDeformedUpValue,
                    mHeightTopDeformedUpValue, mWidthDeformedValue,
                    mHeightBottomDeformedDownValue, mHeightTopDeformedDownValue, mProgress);
            bean.setScale(mScale);
            mOnDeformedListener.onScaleChanged(bean);
        }
    }

    private void heightDeformedChanged() {
        if (mOnDeformedListener == null) {
            return;
        }
        float top = mHeightTopDeformedDownValue - mHeightTopDeformedUpValue;
        float bottom = mHeightBottomDeformedDownValue - mHeightBottomDeformedUpValue;
        if (mCurTopDeformationValue != top || mCurBottomDeformationValue != bottom) {
            mCurTopDeformationValue = top;
            mCurBottomDeformationValue = bottom;
            mOnDeformedListener.onHeightDeformedChanged(top, bottom);
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
                if (mFastMoveScaleOffsetY != spring.getEndValue()) {
                    mFastMoveScaleOffsetY = isEnabled()
                            ? (float) spring.getCurrentValue()
                            : 0.0f;
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
        if (velocity <= -95.0f) {
            float rangeFloat = range;
            if (mProgress > rangeFloat * 0.95f || mProgress < rangeFloat * 0.05f) {
                return;
            }
            fastMoveSpring.setEndValue(1.0d);
            return;
        }
        if (velocity < 95.0f) {
            fastMoveSpring.setEndValue(0.0d);
            return;
        }
        float rangeFloat = range;
        if (mProgress > rangeFloat * 0.95f || mProgress < rangeFloat * 0.05f) {
            return;
        }
        fastMoveSpring.setEndValue(-1.0d);
    }

    private void updatePixPerProgress() {
        int range = mMax - mMin;
        mPixPerProgress = range > 0 ? getSeekBarHeightFloat() / range : 0.0f;
    }

    private void updateScale() {
        int range = mMax - mMin;
        mScale = range > 0 ? (mProgress - mMin) / (float) range : 0.0f;
    }

    public int getSeekBarCenterX() {
        return getPaddingStart() + (((getWidth() - getPaddingStart()) - getPaddingEnd()) >> 1);
    }

    public int getSeekBarHeight() {
        return (int) getSeekBarHeightFloat();
    }

    public float getSeekBarHeightFloat() {
        return ((getHeight() - getPaddingTop()) - getPaddingBottom())
                - (mCurPaddingVertical * 2.0f);
    }

    public float getNormalSeekBarHeightFloat() {
        return ((getHeight() - getPaddingTop()) - getPaddingBottom())
                - (mPaddingVertical * 2.0f);
    }

    @Override
    public void onAnimationEnd(BaseBehavior behavior) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    @Override
    public void onAnimationUpdate(BaseBehavior behavior) {
        Object value = behavior.getAnimatedValue();
        if (value == null) {
            return;
        }
        float floatValue = ((Float) value).floatValue();
        float height = getNormalSeekBarHeightFloat();
        if (height <= 0.0f) {
            return;
        }
        setFlingScale(floatValue / height);
        int oldProgress = mProgress;
        int oldRealProgress = mRealProgress;
        setLocalProgress(getProgressLimit(Math.round(((mMax - mMin) * mScale) + mMin)));
        invalidate();
        if (oldProgress != mProgress) {
            mLastY = (getHeight() - getPaddingBottom()) - floatValue;
            if (oldRealProgress != mRealProgress && mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, mRealProgress, true);
            }
        }
    }

    public float getRealScale(float scale) {
        return Math.max(0.0f, Math.min(scale, 1.0f));
    }

    protected int getProgressLimit(int progress) {
        int range = mMax - mMin;
        return Math.max(mMin - range, Math.min(progress, mMax + range));
    }

    protected int getRealProgress(int progress) {
        return Math.max(mMin, Math.min(progress, mMax));
    }

    public int getColor(View view, ColorStateList colorStateList, int defaultColor) {
        return colorStateList == null
                ? defaultColor
                : colorStateList.getColorForState(view.getDrawableState(), defaultColor);
    }

    private void setThumbBitmap() {
        if (getThumb() != null) {
            mThumbBitmap = drawableToBitmap(getThumb());
        }
    }

    private void setDrawableBounds(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (!mIsSupportDeformation) {
            drawable.setBounds(0, 0, getWidth(), getHeight());
            return;
        }
        drawable.setBounds(0,
                -(mMaxTopMovingDistance + ((int) Math.ceil(mMaxTopHeightDeformedValue))),
                getWidth(),
                getHeight() + mMaxBottomMovingDistance
                        + ((int) Math.ceil(mMaxBottomHeightDeformedValue)));
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int height = Math.max(1, drawable.getIntrinsicHeight());
        int width = Math.max(1, drawable.getIntrinsicWidth());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        setThumbBitmap();
    }

    @Override
    public synchronized void setProgress(int progress) {
        setProgress(progress, false);
    }

    public void setProgress(int progress, boolean animate) {
        int clamped = Math.max(mMin, Math.min(progress, mMax));
        if (mProgress == clamped) {
            return;
        }
        if (animate) {
            startTransitionAnim(clamped, false);
            return;
        }
        setLocalProgress(clamped);
        updateScale();
        invalidate();
    }

    public void setLocalProgress(int progress) {
        mProgress = progress;
        mRealProgress = getRealProgress(progress);
        super.setProgress(mRealProgress);
    }

    @Override
    public synchronized int getProgress() {
        return mRealProgress;
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
        }
        invalidate();
    }

    public void setLocalMax(int max) {
        mMax = max;
        updatePixPerProgress();
        updateScale();
        super.setMax(max);
    }

    @Override
    public synchronized int getMax() {
        return mMax;
    }

    @Override
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

    public void setLocalMin(int min) {
        mMin = min;
        updatePixPerProgress();
        updateScale();
        super.setMin(min);
    }

    @Override
    public synchronized int getMin() {
        return mMin;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateStateColors();
        invalidate();
    }

    public void performFeedback() {
        if (!mEnableVibrator) {
            return;
        }
        if (mRealProgress == getMax() || mRealProgress == getMin()) {
            performHapticFeedback(COUIHapticFeedbackConstants.EDGE_SHORT_VIBRATE);
        } else {
            performHapticFeedback(COUIHapticFeedbackConstants.STEPPING_SHORT_VIBRATE);
        }
    }

    public boolean performAdaptiveFeedback() {
        performFeedback();
        return true;
    }

    public void refresh() {
        updateScale();
        invalidate();
    }

    public void preCalcClipPath() {
        invalidate();
    }

    public void ensureLabelsRemoved() {
        if (mTextDrawable != null) {
            getOverlay().remove(mTextDrawable);
        }
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

    public Paint getProgressPaint() {
        return mProgressPaint;
    }

    public Paint getThumbPaint() {
        return mThumbPaint;
    }

    public int getLabelHeight() {
        return mTextDrawable != null ? mTextDrawable.getIntrinsicHeight() : 0;
    }

    public boolean getHasEnlarge() {
        return mEnableCustomEnlarge;
    }

    public void endEnlargeAnim() {
        cancelAnim(mEnlargeAnimator);
        cancelAnim(mReleaseAnimator);
        releaseAnim();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mOnSeekBarChangeListener = listener;
    }

    public void setMoveType(int moveType) {
        mMoveType = moveType;
    }

    public int getMoveType() {
        return mMoveType;
    }

    public void setMoveDamping(float damping) {
        mDamping = damping;
    }

    public float getMoveDamping() {
        return mDamping;
    }

    public void setIncrement(int increment) {
        mIncrement = Math.abs(increment);
    }

    public void setSupportDeformation(boolean supportDeformation) {
        mIsSupportDeformation = supportDeformation;
    }

    public void setDeformedListener(OnDeformedListener listener) {
        mOnDeformedListener = listener;
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
        setLocalProgress(bean.getProgress());
        invalidate();
    }

    public void setEnableAdaptiveVibrator(boolean enable) {
        mEnableAdaptiveVibrator = enable;
    }

    public void setEnableCustomEnlarge(boolean enable) {
        mEnableCustomEnlarge = enable;
    }

    public void setEnableVibrator(boolean enable) {
        mEnableVibrator = enable;
    }

    public void setBackgroundWidth(float width) {
        mBackgroundWidth = width;
        ensureSize();
        invalidate();
    }

    public void setBackgroundEnlargeScale(float scale) {
        mBackgroundEnlargeScale = scale;
    }

    public void setBackgroundRoundCornerWeight(float weight) {
        mBackgroundRoundCornerWeight = weight;
        invalidate();
    }

    public void setProgressWidth(float width) {
        mProgressWidth = width;
        ensureSize();
        invalidate();
    }

    public void setProgressEnlargeScale(float scale) {
        mProgressEnlargeScale = scale;
    }

    public void setBackgroundRadius(float radius) {
        mBackgroundRadius = radius;
        ensureSize();
        invalidate();
    }

    public void setProgressRadius(float radius) {
        mProgressRadius = radius;
        ensureSize();
        invalidate();
    }

    public void setProgressRoundCornerWeight(float weight) {
        mProgressRoundCornerWeight = weight;
        invalidate();
    }

    public void setProgressContentDescription(String description) {
        setContentDescription(description);
    }

    public void setProgressFull() {
        mIsProgressFull = true;
        setProgress(getMax(), false);
    }

    public void setCustomProgressAnimDuration(float duration) {
        mCustomProgressAnimDuration = duration;
    }

    public void setCustomProgressAnimInterpolator(Interpolator interpolator) {
        mCustomProgressAnimInterpolator = interpolator;
    }

    public void setPaddingVertical(float paddingVertical) {
        mPaddingVertical = paddingVertical == 0.0f ? mDefaultPaddingVertical : paddingVertical;
        ensureSize();
        invalidate();
    }

    public void showText(boolean showText) {
        mShowText = showText;
        invalidate();
    }

    public void setText(String text) {
        mTextContent = text;
        invalidate();
    }

    public void setTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            refreshTextColor();
            invalidate();
        }
    }

    public void setTextMarginTop(float marginTop) {
        if (mTextMarginTop != marginTop) {
            mTextMarginTop = marginTop;
            invalidate();
        }
    }

    public void setTextShadowEnabled(boolean enabled) {
        if (mShowTextShadow != enabled) {
            mShowTextShadow = enabled;
            refreshTextColor();
            invalidate();
        }
    }

    public void setTextTypeface(Typeface typeface) {
        if (typeface != null && !typeface.equals(mTextPaint.getTypeface())) {
            mTextPaint.setTypeface(typeface);
            invalidate();
        }
    }

    public void setPhysicalEnabled(boolean enabled) {
        if (!enabled) {
            stopPhysicsMove();
        }
        mIsPhysicsEnable = enabled;
        if (enabled) {
            updateBehavior();
        }
    }

    public void setFlingLinearDamping(float damping) {
        mFlingLinearDamping = damping;
        if (mFlingBehavior != null) {
            mFlingBehavior.setLinearDamping(damping);
        }
    }

    public void setFlingProperty(float frequency, float dampingRatio) {
        mFlingFrequency = frequency;
        mFlingDampingRatio = dampingRatio;
        if (mFlingBehavior != null) {
            mFlingBehavior.setSpringProperty(frequency, dampingRatio);
        }
    }

    public void setMaxHeightDeformed(float value) {
        mMaxBottomHeightDeformedValue = value;
        mMaxTopHeightDeformedValue = value;
    }

    public void setMaxMovingDistance(int distance) {
        mMaxBottomMovingDistance = distance;
        mMaxTopMovingDistance = distance;
    }

    public void setMaxWidthDeformed(float value) {
        mMaxWidthDeformedValue = value;
    }

    public void setSeekBarBackgroundColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mBackgroundColorStateList = colorStateList;
            updateStateColors();
            invalidate();
        }
    }

    public void setProgressColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mProgressColorStateList = colorStateList;
            updateStateColors();
            invalidate();
        }
    }

    public void setThumbColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mThumbColorStateList = colorStateList;
            updateStateColors();
            invalidate();
        }
    }

    public void setInactiveTrackDrawable(Drawable drawable) {
        if (mInactiveTrackDrawable != null) {
            mInactiveTrackDrawable.setCallback(null);
        }
        mInactiveTrackDrawable = drawable;
        if (drawable != null) {
            drawable.setCallback(this);
            setDrawableBounds(drawable);
        }
        invalidate();
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (mInactiveTrackDrawable != null) {
            Rect dirty = drawable.getDirtyBounds();
            invalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    public float subtract(float value, float base) {
        return new BigDecimal(Float.toString(value))
                .subtract(new BigDecimal(Float.toString(base)))
                .floatValue();
    }

    public void startEnlargeAnim(int type, long duration, Interpolator interpolator) {
        cancelAnim(mReleaseAnimator);
        cancelAnim(mEnlargeAnimator);
        if (type == TYPE_ENLARGE) {
            if (mEnlargeAnimator == null) {
                mEnlargeAnimator = getEnlargeAnimator(duration, interpolator);
            }
            setEnlargeAnimatorValues(mEnlargeAnimator);
            mEnlargeAnimator.start();
        } else if (type == TYPE_NORMAL) {
            if (mReleaseAnimator == null) {
                mReleaseAnimator = getReleaseAnimator(duration, interpolator);
            }
            setReleaseAnimatorValues(mReleaseAnimator);
            mReleaseAnimator.start();
        }
    }

    public void startCustomDeformation(float scale) {
        if (mIsSupportDeformation) {
            if (mButtonDeformationAnimator == null) {
                mButtonDeformationAnimator = ValueAnimator.ofFloat(mScale, scale);
                mButtonDeformationAnimator.setInterpolator(new COUIMoveEaseInterpolator());
                mButtonDeformationAnimator.setDuration(BUTTON_DEFORMATION_ANIM_DURATION);
                mButtonDeformationAnimator.addUpdateListener(animation -> {
                    setTouchScale((Float) animation.getAnimatedValue());
                    invalidate();
                });
            }
            mButtonDeformationAnimator.start();
        }
    }

    public void endCustomDeformation() {
        cancelAnim(mButtonDeformationAnimator);
        setTouchScale(Math.max(0.0f, Math.min(mScale, 1.0f)));
        setLocalProgress(getProgressLimit(Math.round(((mMax - mMin) * mScale) + mMin)));
        invalidate();
    }

    public void stopPhysicsMove() {
        if (mIsPhysicsEnable && mPhysicalAnimator != null && mFlingBehavior != null) {
            mFlingBehavior.stop();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        VibrateUtils.registerHapticObserver(getContext());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        VibrateUtils.unRegisterHapticObserver();
        recycleVelocityTracker();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.progress = mProgress;
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
        setProgress(savedState.progress);
    }

    public static class SavedState extends BaseSavedState {
        int progress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
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
