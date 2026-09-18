package com.coui.appcompat.progressbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.coui.appcompat.R;

public class COUIHorizontalProgressBar extends ProgressBar {
    private static final int DEFAULT_BACKGROUND_COLOR = Color.argb(12, 0, 0, 0);
    private static final int DEFAULT_PROGRESS_COLOR = Color.parseColor("#FF2AD181");

    private ColorStateList mBackgroundColor;
    private final RectF mBackgroundRect = new RectF();
    private final Context mContext;
    private boolean mNeedRadius;
    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();
    private ColorStateList mProgressColor;
    private final Path mProgressPath = new Path();
    private final RectF mProgressRect = new RectF();
    private int mRadius = Integer.MAX_VALUE;
    private int mStyle;

    public COUIHorizontalProgressBar(Context context) {
        this(context, null);
    }

    public COUIHorizontalProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiHorizontalProgressBarStyle);
    }

    public COUIHorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.COUIProgressHorizontal);
    }

    public COUIHorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mStyle = attrs == null || attrs.getStyleAttribute() == 0 ? defStyleAttr : attrs.getStyleAttribute();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIHorizontalProgressBar,
                defStyleAttr, defStyleRes);
        mBackgroundColor = a.getColorStateList(
                R.styleable.COUIHorizontalProgressBar_couiHorizontalProgressBarBackgroundColor);
        mProgressColor = a.getColorStateList(
                R.styleable.COUIHorizontalProgressBar_couiHorizontalProgressBarProgressColor);
        mNeedRadius = a.getBoolean(
                R.styleable.COUIHorizontalProgressBar_couiHorizontalProgressNeedRadius, true);
        a.recycle();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
    }

    private int getStateColor(ColorStateList colors, int defaultColor) {
        return colors == null ? defaultColor : colors.getColorForState(getDrawableState(), defaultColor);
    }

    public boolean isLayoutRtl() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        mProgressPath.reset();
        mPath.reset();
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        mPaint.setColor(getStateColor(mBackgroundColor, DEFAULT_BACKGROUND_COLOR));
        mBackgroundRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        Path.Direction direction = Path.Direction.CCW;
        mPath.addRoundRect(mBackgroundRect, mRadius, mRadius, direction);
        canvas.save();
        canvas.clipPath(mPath);
        canvas.drawRoundRect(mBackgroundRect, mRadius, mRadius, mPaint);
        float progress = getMax() == 0 ? 0f : getProgress() / (float) getMax();
        if (isLayoutRtl()) {
            float left = Math.round((getWidth() - getPaddingRight()) - (progress * width));
            mProgressRect.set(left, getPaddingTop(), left + width, getHeight() - getPaddingBottom());
        } else {
            float left = Math.round(getPaddingLeft() - ((1f - progress) * width));
            mProgressRect.set(left, getPaddingTop(), left + width, getHeight() - getPaddingBottom());
        }
        mPaint.setColor(getStateColor(mProgressColor, DEFAULT_PROGRESS_COLOR));
        mProgressPath.addRoundRect(mProgressRect, mRadius, 0f, direction);
        mProgressPath.op(mPath, Path.Op.INTERSECT);
        canvas.drawPath(mProgressPath, mPaint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = w - getPaddingRight() - getPaddingLeft();
        int height = h - getPaddingTop() - getPaddingBottom();
        mRadius = mNeedRadius ? Math.min(width, height) / 2 : 0;
    }

    public void refresh() {
        TypedArray a = null;
        String type = getResources().getResourceTypeName(mStyle);
        if ("attr".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUIHorizontalProgressBar, mStyle, 0);
        } else if ("style".equals(type)) {
            a = mContext.obtainStyledAttributes(null, R.styleable.COUIHorizontalProgressBar, 0, mStyle);
        }
        if (a != null) {
            mBackgroundColor = a.getColorStateList(
                    R.styleable.COUIHorizontalProgressBar_couiHorizontalProgressBarBackgroundColor);
            mProgressColor = a.getColorStateList(
                    R.styleable.COUIHorizontalProgressBar_couiHorizontalProgressBarProgressColor);
            a.recycle();
        }
        invalidate();
    }

    public void setBackgroundColor(ColorStateList colorStateList) {
        mBackgroundColor = colorStateList;
        invalidate();
    }

    public void setProgressColor(ColorStateList colorStateList) {
        mProgressColor = colorStateList;
        invalidate();
    }
}
