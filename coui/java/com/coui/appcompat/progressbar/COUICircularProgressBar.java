package com.coui.appcompat.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class COUICircularProgressBar extends View {
    private static final int FOLLOW_THEME_PROGRESS_BAR_TRACK_ALPHA = 89;
    public static final int MEDIUM_SIZE = 0;
    public static final int LARGE_SIZE = 1;
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_ON_IMAGE = 1;
    public static final int TYPE_FOLLOW_THEME = 2;
    private static final String TAG = "COUICircularProgressBar";
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 10;

    private AccessibilityEventSender mAccessibilityEventSender;
    private float mCenterX;
    private float mCenterY;
    private final Context mContext;
    private final COUICircularProgressDrawable mDrawable;
    private boolean mError;
    private int mErrorDrawableTint;
    private int mHeight;
    private int mInnerPadding;
    private AccessibilityManager mManager;
    private int mMax = 100;
    private int mPauseDrawableTint;
    private boolean mPaused;
    private int mProgress;
    private int mProgressBarColor;
    private int mProgressBarSize = MEDIUM_SIZE;
    private int mProgressBarTrackColor;
    private int mProgressBarType = TYPE_DEFAULT;
    private int mStrokeLargeWidth;
    private int mStrokeMediumWidth;
    private int mStrokeWidth;
    private int mStyle;
    private int mWidth;

    public class AccessibilityEventSender implements Runnable {
        @Override
        public void run() {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgressBarSize {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgressBarType {
    }

    public static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int mMax;
        int mProgress;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = (Integer) in.readValue(getClass().getClassLoader());
            mMax = (Integer) in.readValue(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mProgress);
            out.writeValue(mMax);
        }

        @Override
        public String toString() {
            return "COUICircularProgressBar.SavedState { "
                    + Integer.toHexString(System.identityHashCode(this))
                    + " mProgress = " + mProgress + " mMax = " + mMax + " }";
        }
    }

    public COUICircularProgressBar(Context context) {
        this(context, null);
    }

    public COUICircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiCircularProgressBarStyle);
    }

    public COUICircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_COUICircularProgressBar);
    }

    public COUICircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mStyle = attrs == null || attrs.getStyleAttribute() == 0 ? defStyleAttr : attrs.getStyleAttribute();
        int defaultLength = getResources().getDimensionPixelSize(R.dimen.coui_circular_progress_large_length);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICircularProgressBar,
                defStyleAttr, defStyleRes);
        mWidth = a.getDimensionPixelSize(R.styleable.COUICircularProgressBar_couiCircularProgressBarWidth,
                defaultLength);
        mHeight = a.getDimensionPixelSize(R.styleable.COUICircularProgressBar_couiCircularProgressBarHeight,
                defaultLength);
        mProgressBarType = a.getInteger(R.styleable.COUICircularProgressBar_couiCircularProgressBarType,
                TYPE_DEFAULT);
        mProgressBarSize = a.getInteger(R.styleable.COUICircularProgressBar_couiCircularProgressBarSize,
                LARGE_SIZE);
        mProgressBarColor = a.getColor(R.styleable.COUICircularProgressBar_couiCircularProgressBarColor,
                0);
        mProgressBarTrackColor = a.getColor(
                R.styleable.COUICircularProgressBar_couiCircularProgressBarTrackColor, 0);
        mPauseDrawableTint = a.getColor(
                R.styleable.COUICircularProgressBar_couiCircularPauseDrawableTint, 0);
        mErrorDrawableTint = a.getColor(
                R.styleable.COUICircularProgressBar_couiCircularErrorDrawableTint, 0);
        mProgress = a.getInteger(R.styleable.COUICircularProgressBar_couiCircularProgress, mProgress);
        mMax = a.getInteger(R.styleable.COUICircularProgressBar_couiCircularMax, mMax);
        a.recycle();
        mInnerPadding = context.getResources().getDimensionPixelSize(
                R.dimen.coui_circular_progress_default_padding);
        mStrokeMediumWidth = context.getResources().getDimensionPixelSize(
                R.dimen.coui_circular_progress_medium_stroke_width);
        mStrokeLargeWidth = context.getResources().getDimensionPixelSize(
                R.dimen.coui_circular_progress_large_stroke_width);
        mStrokeWidth = mProgressBarSize == MEDIUM_SIZE ? mStrokeMediumWidth : mStrokeLargeWidth;
        mCenterX = mWidth >> 1;
        mCenterY = mHeight >> 1;
        mDrawable = new COUICircularProgressDrawable(context);
        init();
    }

    private void configDrawable() {
        if (mProgressBarType == TYPE_FOLLOW_THEME) {
            mDrawable.setTrackColor(setAlphaComponent(mProgressBarTrackColor,
                    FOLLOW_THEME_PROGRESS_BAR_TRACK_ALPHA));
        } else {
            mDrawable.setTrackColor(mProgressBarTrackColor);
        }
        mDrawable.setIsDrawShadow(mProgressBarType == TYPE_ON_IMAGE);
        mDrawable.setProgressColor(mProgressBarColor);
        mDrawable.setPauseIconColor(mPauseDrawableTint);
        mDrawable.setErrorIconColor(mErrorDrawableTint);
        mDrawable.setProperties(mCenterX + mInnerPadding, mCenterY + mInnerPadding,
                mWidth - (mInnerPadding * 2f), mStrokeWidth);
        mDrawable.setProgressBarErrorSize(
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.coui_circular_progress_error_diameter),
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.coui_circular_progress_error_stroke_width));
        mDrawable.invalidateSelf();
        invalidate();
    }

    private void init() {
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        mManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        setProgress(mProgress);
        setMax(mMax);
        configDrawable();
    }

    private void scheduleAccessibilityEventSender() {
        if (mAccessibilityEventSender == null) {
            mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(mAccessibilityEventSender);
        }
        postDelayed(mAccessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    private static int setAlphaComponent(int color, int alpha) {
        return (color & 0x00ffffff) | ((alpha & 0xff) << 24);
    }

    public void error() {
        if (!mDrawable.isAnimating() && !mPaused && !mError) {
            mDrawable.error();
            mError = true;
        }
    }

    public int getMax() {
        return mMax;
    }

    public int getProgress() {
        return mProgress;
    }

    public float getVisualProgress() {
        return mDrawable.getVisualProgress();
    }

    public boolean isError() {
        return mError;
    }

    public boolean isPaused() {
        return mPaused;
    }

    @Override
    protected void onAttachedToWindow() {
        mDrawable.setHostView(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mDrawable.recycle();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth + (mInnerPadding * 2), mHeight + (mInnerPadding * 2));
    }

    public void onProgressRefresh() {
        if (mManager != null && mManager.isEnabled() && mManager.isTouchExplorationEnabled()) {
            scheduleAccessibilityEventSender();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setMax(savedState.mMax);
        setProgress(savedState.mProgress, false);
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mProgress = mProgress;
        savedState.mMax = mMax;
        return savedState;
    }

    public void pause() {
        if (mProgressBarType == TYPE_DEFAULT || mProgressBarType == TYPE_FOLLOW_THEME) {
            Log.w(TAG, "Default type circular progress bar can not pause!");
        } else if (!mDrawable.isAnimating() && !mError && !mPaused) {
            mDrawable.pause();
            mPaused = true;
        }
    }

    public void recover() {
        if (!mDrawable.isAnimating() && !mPaused && mError) {
            mDrawable.recover();
            mError = false;
        }
    }

    public void refresh() {
        TypedArray a = null;
        String type = getResources().getResourceTypeName(mStyle);
        if ("attr".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUICircularProgressBar, mStyle, 0);
        } else if ("style".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUICircularProgressBar, 0, mStyle);
        }
        if (a != null) {
            mProgressBarColor = a.getColor(
                    R.styleable.COUICircularProgressBar_couiCircularProgressBarColor, 0);
            mProgressBarTrackColor = a.getColor(
                    R.styleable.COUICircularProgressBar_couiCircularProgressBarTrackColor, 0);
            mPauseDrawableTint = a.getColor(
                    R.styleable.COUICircularProgressBar_couiCircularPauseDrawableTint, 0);
            mErrorDrawableTint = a.getColor(
                    R.styleable.COUICircularProgressBar_couiCircularErrorDrawableTint, 0);
            a.recycle();
        }
        configDrawable();
    }

    public void resume() {
        if (!mDrawable.isAnimating() && !mError && mPaused) {
            mDrawable.resume();
            mPaused = false;
        }
    }

    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != mMax) {
            mMax = max;
            mDrawable.setMax(max);
            if (mProgress > max) {
                mProgress = max;
            }
        }
    }

    public void setOnProgressChangedListener(
            COUICircularProgressDrawable.OnProgressChangedListener listener) {
        mDrawable.setOnProgressChangedListener(listener);
    }

    public void setOnProgressStateAnimationListener(
            COUICircularProgressDrawable.OnProgressStateAnimatorListener listener) {
        mDrawable.setOnProgressStateAnimatorListener(listener);
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    public void setProgress(int progress, boolean animate) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMax) {
            progress = mMax;
        }
        if (progress != mProgress) {
            mProgress = progress;
            mDrawable.setProgress(progress, animate);
        }
        onProgressRefresh();
    }

    public void setProgressBarType(@ProgressBarType int type) {
        mProgressBarType = type;
        configDrawable();
    }

    public void setProgressSize(@ProgressBarSize int size) {
        mProgressBarSize = size;
        if (size == MEDIUM_SIZE) {
            int length = mContext.getResources().getDimensionPixelOffset(
                    R.dimen.coui_circular_progress_medium_length);
            mWidth = length;
            mHeight = length;
            mStrokeWidth = mStrokeMediumWidth;
        } else if (size == LARGE_SIZE) {
            int length = mContext.getResources().getDimensionPixelOffset(
                    R.dimen.coui_circular_progress_large_length);
            mWidth = length;
            mHeight = length;
            mStrokeWidth = mStrokeLargeWidth;
        }
        mCenterX = mWidth >> 1;
        mCenterY = mHeight >> 1;
        configDrawable();
        requestLayout();
    }
}
