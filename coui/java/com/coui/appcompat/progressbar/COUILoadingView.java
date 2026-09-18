package com.coui.appcompat.progressbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

import java.lang.ref.WeakReference;

public class COUILoadingView extends View {
    public static final int SMALL_TYPE = 0;
    public static final int DEFAULT_TYPE = 1;
    public static final int MEDIUM_TYPE = 1;
    public static final int LARGE_TYPE = 2;
    public static final int ORIGINAL_ANGLE = -90;
    public static final int SWIPT_ANGEL = 60;

    private static final String TAG = "COUILoadingView";
    private static final float LARGE_POINT_START_ALPHA = 0.215f;
    private static final float LARGE_POINT_END_ALPHA = 1.0f;
    private static final float MEDIUM_POINT_START_ALPHA = 0.1f;
    private static final float MEDIUM_POINT_END_ALPHA = 0.4f;
    private static final int ONE_CYCLE_DURATION = 480;
    private static final float ONE_THOUSAND_MILLISECOND = 1000.0f;

    private String mAccessDescription;
    private float mArcRadius;
    private RectF mArcRect;
    private Paint mBackGroundPaint;
    private float mHalfHeight;
    private float mHalfStrokeWidth;
    private float mHalfWidth;
    private int mHeight;
    private boolean mIsAnimationCreated;
    private boolean mIsAnimationStarted;
    private int mLoadingType = DEFAULT_TYPE;
    private int mLoadingViewBgCircleColor;
    private int mLoadingViewColor;
    private ValueAnimator mProgressAnimator;
    private Paint mProgressPaint;
    private float mStartAlpha = MEDIUM_POINT_START_ALPHA;
    private float mEndAlpha = MEDIUM_POINT_END_ALPHA;
    private int mStrokeDefaultWidth;
    private int mStrokeLargeWidth;
    private int mStrokeMediumWidth;
    private float mStrokeWidth;
    private int mStyle;
    private final Context mContext;
    private int mWidth;

    public static class LoadingAnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private final WeakReference<COUILoadingView> mWeakRef;

        public LoadingAnimUpdateListener(COUILoadingView view) {
            mWeakRef = new WeakReference<>(view);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            COUILoadingView view = mWeakRef.get();
            if (view != null) {
                if (view.isAttachedToWindow() && view.getVisibility() == VISIBLE) {
                    view.invalidate();
                } else {
                    Log.e(TAG, "LoadingView state error,cancelAnimations");
                    view.cancelAnimations();
                }
            }
        }
    }

    public COUILoadingView(Context context) {
        this(context, null);
    }

    public COUILoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiLoadingViewStyle);
    }

    public COUILoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_COUILoadingView);
    }

    public COUILoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mStyle = attrs == null || attrs.getStyleAttribute() == 0 ? defStyleAttr : attrs.getStyleAttribute();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUILoadingView,
                defStyleAttr, defStyleRes);
        int defaultLength = getResources().getDimensionPixelSize(R.dimen.coui_loading_view_default_length);
        mWidth = a.getDimensionPixelSize(R.styleable.COUILoadingView_couiLoadingViewWidth, defaultLength);
        mHeight = a.getDimensionPixelSize(R.styleable.COUILoadingView_couiLoadingViewHeight, defaultLength);
        mLoadingType = a.getInteger(R.styleable.COUILoadingView_couiLoadingViewType, DEFAULT_TYPE);
        mLoadingViewColor = a.getColor(R.styleable.COUILoadingView_couiLoadingViewColor, 0);
        mLoadingViewBgCircleColor = a.getColor(R.styleable.COUILoadingView_couiLoadingViewBgCircleColor, 0);
        a.recycle();
        mStrokeDefaultWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_circle_loading_strokewidth);
        mStrokeMediumWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_circle_loading_medium_strokewidth);
        mStrokeLargeWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_circle_loading_large_strokewidth);
        mStrokeWidth = mStrokeDefaultWidth;
        if (mLoadingType == MEDIUM_TYPE) {
            mStrokeWidth = mStrokeMediumWidth;
            mStartAlpha = MEDIUM_POINT_START_ALPHA;
            mEndAlpha = MEDIUM_POINT_END_ALPHA;
        } else if (mLoadingType == LARGE_TYPE) {
            mStrokeWidth = mStrokeLargeWidth;
            mStartAlpha = LARGE_POINT_START_ALPHA;
            mEndAlpha = LARGE_POINT_END_ALPHA;
        }
        mAccessDescription = context.getString(R.string.coui_loading_view_access_string);
        setContentDescription(mAccessDescription);
        initProgressPaint();
        initBackgroundPaint();
    }

    private void cancelAnimations() {
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
        }
    }

    private void createAnimator() {
        mProgressAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressAnimator.setDuration(ONE_CYCLE_DURATION);
        mProgressAnimator.setInterpolator(new COUILinearInterpolator());
        mProgressAnimator.addUpdateListener(new LoadingAnimUpdateListener(this));
        mProgressAnimator.setRepeatMode(ValueAnimator.RESTART);
        mProgressAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    private void destroyAnimator() {
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
            mProgressAnimator.removeAllListeners();
            mProgressAnimator.removeAllUpdateListeners();
            mProgressAnimator = null;
        }
    }

    private void drawBackgroundCircle(Canvas canvas) {
        canvas.drawCircle(mHalfWidth, mHalfWidth, mArcRadius, mBackGroundPaint);
    }

    private void initArcRect() {
        mHalfStrokeWidth = mStrokeWidth / 2f;
        mHalfWidth = getWidth() / 2f;
        mHalfHeight = getHeight() / 2f;
        mArcRadius = mHalfWidth - mHalfStrokeWidth;
        mArcRect = new RectF(mHalfWidth - mArcRadius, mHalfWidth - mArcRadius,
                mHalfWidth + mArcRadius, mHalfWidth + mArcRadius);
    }

    private void initBackgroundPaint() {
        mBackGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackGroundPaint.setColor(mLoadingViewBgCircleColor);
        mBackGroundPaint.setStyle(Paint.Style.STROKE);
        mBackGroundPaint.setStrokeWidth(mStrokeWidth);
    }

    private void initProgressPaint() {
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(mLoadingViewColor);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void startAnimations() {
        if (mProgressAnimator != null) {
            if (mProgressAnimator.isRunning()) {
                mProgressAnimator.cancel();
            }
            mProgressAnimator.start();
        }
    }

    public void startLoading() {
        if (mProgressAnimator == null) {
            createAnimator();
            mIsAnimationCreated = true;
        }
        startAnimations();
        mIsAnimationStarted = true;
    }

    public void stopLoading() {
        cancelAnimations();
        mIsAnimationStarted = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mIsAnimationCreated) {
            createAnimator();
            mIsAnimationCreated = true;
        }
        if (!mIsAnimationStarted && getVisibility() == VISIBLE) {
            startAnimations();
            mIsAnimationStarted = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroyAnimator();
        mIsAnimationCreated = false;
        mIsAnimationStarted = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float currentStepProgress = ((SystemClock.uptimeMillis() % 1000) * 360f)
                / ONE_THOUSAND_MILLISECOND;
        drawBackgroundCircle(canvas);
        canvas.save();
        canvas.rotate(ORIGINAL_ANGLE, mHalfWidth, mHalfHeight);
        if (mArcRect == null) {
            initArcRect();
        }
        canvas.drawArc(mArcRect, currentStepProgress - 30f,
                (2f - Math.abs((180f - currentStepProgress) / 180f)) * SWIPT_ANGEL,
                false, mProgressPaint);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mArcRect == null) {
            initArcRect();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initArcRect();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (getVisibility() != VISIBLE || !isAttachedToWindow()) {
            cancelAnimations();
            mIsAnimationStarted = false;
            return;
        }
        if (!mIsAnimationCreated) {
            createAnimator();
            mIsAnimationCreated = true;
        }
        if (!mIsAnimationStarted) {
            startAnimations();
            mIsAnimationStarted = true;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE && isAttachedToWindow() && getVisibility() == VISIBLE
                && getWindowVisibility() == VISIBLE) {
            startAnimations();
        } else {
            cancelAnimations();
        }
    }

    public void refresh() {
        TypedArray a = null;
        String type = getResources().getResourceTypeName(mStyle);
        if ("attr".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUILoadingView, mStyle, 0);
        } else if ("style".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUILoadingView, 0, mStyle);
        }
        if (a != null) {
            mLoadingViewColor = a.getColor(R.styleable.COUILoadingView_couiLoadingViewColor, 0);
            mLoadingViewBgCircleColor = a.getColor(
                    R.styleable.COUILoadingView_couiLoadingViewBgCircleColor, 0);
            a.recycle();
        }
        initProgressPaint();
        initBackgroundPaint();
        invalidate();
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setLoadingType(int loadingType) {
        mLoadingType = loadingType;
    }

    public void setLoadingViewBgCircleColor(int color) {
        mLoadingViewBgCircleColor = color;
        initBackgroundPaint();
        invalidate();
    }

    public void setLoadingViewColor(int color) {
        mLoadingViewColor = color;
        initProgressPaint();
        invalidate();
    }

    public void setWidth(int width) {
        mWidth = width;
    }
}
