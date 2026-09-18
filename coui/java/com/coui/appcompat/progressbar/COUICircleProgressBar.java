package com.coui.appcompat.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

import java.util.ArrayList;

@Deprecated
public class COUICircleProgressBar extends View {
    public static final int ACCURACY = 2;
    public static final int DEFAULT_TYPE = 0;
    public static final int MEDIUM_TYPE = 1;
    public static final int LARGE_TYPE = 2;
    public static final int ORIGINAL_ANGLE = -90;

    private static final String TAG = "COUICircleProgressBar";
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 10;

    private AccessibilityEventSender mAccessibilityEventSender;
    private float mArcRadius;
    private RectF mArcRect;
    private Paint mBackGroundPaint;
    private AccessibilityManager mManager;
    private final Context mContext;
    private int mCurrentStepProgress;
    private int mHalfStrokeWidth;
    private int mHalfWidth;
    private int mHeight;
    private int mMax = 100;
    private final ArrayList<ProgressPoint> mPointList = new ArrayList<>();
    private int mPointRadius;
    private int mPreStepProgress = -1;
    private int mProgress;
    private int mProgressBarBgCircleColor;
    private int mProgressBarColor;
    private int mProgressBarType = DEFAULT_TYPE;
    private Paint mProgressPaint;
    private int mStrokeDefaultWidth;
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

    public class ProgressPoint {
        float mCurrentAlpha;

        public float getCurrentAlpha() {
            return mCurrentAlpha;
        }

        public void setCurrentAlpha(float currentAlpha) {
            mCurrentAlpha = currentAlpha;
        }
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
        int mProgress;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = (Integer) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mProgress);
        }

        @Override
        public String toString() {
            return "COUICircleProgressBar.SavedState { "
                    + Integer.toHexString(System.identityHashCode(this))
                    + " mProgress = " + mProgress + " }";
        }
    }

    public COUICircleProgressBar(Context context) {
        this(context, null);
    }

    public COUICircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiCircleProgressBarStyle);
    }

    public COUICircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_COUICircleProgressBar);
    }

    public COUICircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mStyle = attrs == null || attrs.getStyleAttribute() == 0 ? defStyleAttr : attrs.getStyleAttribute();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICircleProgressBar,
                defStyleAttr, defStyleRes);
        int defaultLength = getResources().getDimensionPixelSize(R.dimen.coui_loading_view_default_length);
        mWidth = a.getDimensionPixelSize(R.styleable.COUICircleProgressBar_couiCircleProgressBarWidth,
                defaultLength);
        mHeight = a.getDimensionPixelSize(R.styleable.COUICircleProgressBar_couiCircleProgressBarHeight,
                defaultLength);
        mProgressBarType = a.getInteger(R.styleable.COUICircleProgressBar_couiCircleProgressBarType,
                DEFAULT_TYPE);
        mProgressBarColor = a.getColor(R.styleable.COUICircleProgressBar_couiCircleProgressBarColor,
                0);
        mProgressBarBgCircleColor = a.getColor(
                R.styleable.COUICircleProgressBar_couiCircleProgressBarBgCircleColor, 0);
        mProgress = a.getInteger(R.styleable.COUICircleProgressBar_couiCircleProgress, mProgress);
        mMax = a.getInteger(R.styleable.COUICircleProgressBar_couiCircleMax, mMax);
        a.recycle();
        mStrokeDefaultWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_circle_loading_strokewidth);
        mStrokeMediumWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_circle_loading_medium_strokewidth);
        mStrokeLargeWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_circle_loading_large_strokewidth);
        mStrokeWidth = mProgressBarType == MEDIUM_TYPE ? mStrokeMediumWidth
                : mProgressBarType == LARGE_TYPE ? mStrokeLargeWidth : mStrokeDefaultWidth;
        mPointRadius = mStrokeWidth >> 1;
        init();
    }

    private void drawBackgroudCicle(Canvas canvas) {
        mBackGroundPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(mHalfWidth, mHalfWidth, mArcRadius, mBackGroundPaint);
    }

    private void init() {
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        for (int i = 0; i < 360; i++) {
            mPointList.add(new ProgressPoint());
        }
        initBackgroundPaint();
        initProgressPaint();
        setProgress(mProgress);
        setMax(mMax);
        mManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    private void initBackgroundPaint() {
        mBackGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackGroundPaint.setColor(mProgressBarBgCircleColor);
        mBackGroundPaint.setStyle(Paint.Style.STROKE);
    }

    private void initProgressPaint() {
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressBarColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void scheduleAccessibilityEventSender() {
        if (mAccessibilityEventSender == null) {
            mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(mAccessibilityEventSender);
        }
        postDelayed(mAccessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    private void verifyProgress() {
        if (mMax > 0) {
            int step = (int) (mProgress / (mMax / 360f));
            mCurrentStepProgress = 360 - step < ACCURACY ? 360 : step;
            mPreStepProgress = mCurrentStepProgress;
        } else {
            mPreStepProgress = 0;
            mCurrentStepProgress = 0;
        }
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public int getProgress() {
        return mProgress;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAccessibilityEventSender != null) {
            removeCallbacks(mAccessibilityEventSender);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackgroudCicle(canvas);
        canvas.save();
        canvas.rotate(ORIGINAL_ANGLE, mHalfWidth, mHalfWidth);
        canvas.drawArc(mArcRect, 0f, mCurrentStepProgress, false, mProgressPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
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
        setProgress(savedState.mProgress);
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mProgress = mProgress;
        return savedState;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHalfStrokeWidth = mStrokeWidth / 2;
        mHalfWidth = getWidth() / 2;
        mArcRadius = mHalfWidth - mHalfStrokeWidth;
        mArcRect = new RectF(mHalfWidth - mArcRadius, mHalfWidth - mArcRadius,
                mHalfWidth + mArcRadius, mHalfWidth + mArcRadius);
    }

    public void refresh() {
        TypedArray a = null;
        String type = getResources().getResourceTypeName(mStyle);
        if ("attr".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUICircleProgressBar, mStyle, 0);
        } else if ("style".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUICircleProgressBar, 0, mStyle);
        }
        if (a != null) {
            mProgressBarColor = a.getColor(R.styleable.COUICircleProgressBar_couiCircleProgressBarColor, 0);
            mProgressBarBgCircleColor = a.getColor(
                    R.styleable.COUICircleProgressBar_couiCircleProgressBarBgCircleColor, 0);
            a.recycle();
        }
        initBackgroundPaint();
        initProgressPaint();
        invalidate();
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != mMax) {
            mMax = max;
            if (mProgress > max) {
                mProgress = max;
            }
        }
        verifyProgress();
    }

    public void setProgress(int progress) {
        Log.i(TAG, "setProgress: " + progress);
        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMax) {
            progress = mMax;
        }
        if (progress != mProgress) {
            mProgress = progress;
        }
        verifyProgress();
        onProgressRefresh();
    }

    public void setProgressBarBgCircleColor(int color) {
        mProgressBarBgCircleColor = color;
        initBackgroundPaint();
        invalidate();
    }

    public void setProgressBarColor(int color) {
        mProgressBarColor = color;
        initProgressPaint();
        invalidate();
    }

    public void setProgressBarType(int type) {
        mProgressBarType = type;
    }

    public void setWidth(int width) {
        mWidth = width;
    }
}
