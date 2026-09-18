package com.coui.appcompat.indicator;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.PathInterpolator;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.log.COUILog;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;


public class COUIPageIndicator extends View implements COUIIPagerIndicator {
    private static final ArgbEvaluator COLOR_EVALUATOR;
    private static final float COMPACT_MOVE_FACTOR_THRESHOLD = 0.095f;
    private static final boolean DEBUG;
    private static final float DEFAULT_COLOR_FACTOR_THRESHOLD = 0.05f;
    private static final float DEFAULT_GONE_DOT_SIZE = 0.0f;
    private static final float DEFAULT_HALF_OF_PROGRESS = 0.5f;
    private static final PathInterpolator DEFAULT_INTERPOLATOR;
    private static final int DEFAULT_MAXIMUM_LARGE_DOT_NUMBER = 4;
    private static final int DEFAULT_MAXIMUM_MEDIUM_DOT_NUMBER = 2;
    private static final float DEFAULT_MINIMUM_VISIBLE_CHANGE = 0.005f;
    private static final float DEFAULT_MOVE_FACTOR_THRESHOLD = 0.005f;
    private static final float HALF_OF_PI_IN_DEGREE = 90.0f;
    private static final float INDICATOR_DOT_GONE_LEVEL_SIZE = 9.0f;
    private static final float INDICATOR_DOT_LARGE_LEVEL_SIZE = 3.0f;
    private static final float INDICATOR_DOT_LARGE_LEVEL_SIZE_WHILE_ALL_DOTS_VISIBLE = 5.0f;
    private static final int INDICATOR_DOT_LEVEL = 4;
    private static final float INDICATOR_DOT_MEDIUM_LEVEL_SIZE = 5.0f;
    private static final float INDICATOR_DOT_SMALL_LEVEL_SIZE = 7.0f;
    private static final int INVALID_INDEX = -1;
    private static final int MAX_VISIBLE_DOT_NUMBER = 6;
    private static final float ONE_AND_A_HALF_OF_PI_IN_DEGREE = 270.0f;
    private static final float PI_IN_DEGREE = 180.0f;
    private static final String TAG = "COUIPageIndicator";
    private Paint mCompatTracePaint;
    private int mDotColor;
    private Paint mDotPaint;
    private float mDotsInterval;
    private long mDownTime;
    private int mIndicatorDescriptionID;
    private boolean mIsClickable;
    private float mMediumDotSize;
    private PageIndicatorModel mModel;
    private float mMoveFactorThreshold;
    private float mNormalDotSize;
    private OnIndicatorDotClickListener mOnDotClickListener;
    private float mSmallDotSize;
    private String mStartContentDescription;
    private final int mStyle;
    private final float[] mTouchPoint;
    private int mTraceColor;
    private Paint mTracePaint;

    public interface OnIndicatorDotClickListener {
        void onClick(int index);
    }

    public static class PageIndicatorDotModel {
        protected int mFillColor;
        protected int mId;
        protected float mRadius = 0.0f;
        protected float mCenterX = 0.0f;
        protected float mCenterY = 0.0f;
        protected float mOffsetX = 0.0f;
        protected RectF mBounds = new RectF(0.0f, 0.0f, 0.0f, 0.0f);

        public PageIndicatorDotModel(int id) {
            this.mId = id;
        }

        private void updateBounds() {
            RectF rectF = this.mBounds;
            float offsetX = this.mOffsetX;
            float centerX = this.mCenterX;
            float radius = this.mRadius;
            float centerY = this.mCenterY;
            rectF.set((offsetX + centerX) - radius, centerY - radius, offsetX + centerX + radius, centerY + radius);
        }

        public void dump() {
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "id = " + this.mId + " dot = (" + this.mCenterX + ", " + this.mCenterY + ", " + this.mRadius + ") bounds = " + this.mBounds + " offsetX = " + this.mOffsetX);
        }

        public RectF getBounds() {
            return this.mBounds;
        }

        public float getCenterX() {
            return this.mCenterX;
        }

        public float getCenterY() {
            return this.mCenterY;
        }

        public int getFillColor() {
            return this.mFillColor;
        }

        public int getId() {
            return this.mId;
        }

        public float getRadius() {
            return this.mRadius;
        }

        public void onDraw(Canvas canvas, Paint paint) {
            paint.setColor(this.mFillColor);
            float centerX = this.mCenterX;
            float radius = this.mRadius;
            float centerY = this.mCenterY;
            canvas.drawOval(centerX - radius, centerY - radius, centerX + radius, centerY + radius, paint);
        }

        public void setCenterX(float centerX) {
            this.mCenterX = centerX;
            updateBounds();
        }

        public void setCenterY(float centerY) {
            this.mCenterY = centerY;
            updateBounds();
        }

        public void setFillColor(int fillColor) {
            this.mFillColor = fillColor;
        }

        public void setId(int id) {
            this.mId = id;
        }

        public void setOffsetX(float offsetX) {
            this.mOffsetX = offsetX;
            updateBounds();
        }

        public void setRadius(float radius) {
            this.mRadius = radius;
            updateBounds();
        }
    }

    public class PageIndicatorModel {
        private int mAccessCurrentIndex;
        private int mCurrentIndex;
        private float mCurrentOffset;
        private float mInterval;
        private float mMaskOffset;
        private final float[] mScaleMaskIndex;
        private final float[] mScaleMaskSize;
        private final float[] mScaleSize;
        private SpringAnimation mSpringAnimation;
        private View mViewHost;
        private final LinkedList<PageIndicatorDotModel> mDots = new LinkedList<>();
        private final int mMaxVisibleCount = MAX_VISIBLE_DOT_NUMBER;
        private final Path mTraceRectPath = new Path();
        private final RectF mVisibleRect = new RectF();
        private final float[] mVisibleBounds = new float[2];
        private final Path mPath = new Path();
        private final Path mPathLeft = new Path();
        private final Path mPathRight = new Path();
        private final Path mPathUp = new Path();
        private final Path mPathDown = new Path();
        private final Matrix mMatrix = new Matrix();
        private final Matrix mInvertMatrix = new Matrix();
        private final FloatPropertyCompat<PageIndicatorModel> mCurrentPosition = new FloatPropertyCompat<PageIndicatorModel>("currentPosition") {
            @Override
            public float getValue(PageIndicatorModel pageIndicatorModel) {
                return pageIndicatorModel.getCurrentPosition();
            }

            @Override
            public void setValue(PageIndicatorModel pageIndicatorModel, float value) {
                int floorPosition = (int) Math.floor(value);
                pageIndicatorModel.setCurrentPositionInternal(floorPosition, value - floorPosition);
            }
        };
        private int mIndicatorCount = 0;
        private float mDrawHorizontalOffset = 0.0f;
        private float mVisibleOffset = 0.0f;
        private boolean mMovingToEnd = false;

        public PageIndicatorModel(View view) {
            this.mViewHost = view;
            this.mScaleMaskSize = new float[]{COUIPageIndicator.INDICATOR_DOT_LARGE_LEVEL_SIZE, COUIPageIndicator.INDICATOR_DOT_MEDIUM_LEVEL_SIZE, COUIPageIndicator.INDICATOR_DOT_SMALL_LEVEL_SIZE, COUIPageIndicator.INDICATOR_DOT_GONE_LEVEL_SIZE};
            float mediumMaskStart = 0.0f - ((COUIPageIndicator.INDICATOR_DOT_MEDIUM_LEVEL_SIZE - this.mScaleMaskSize[0]) / 2.0f);
            float smallMaskStart = mediumMaskStart - ((COUIPageIndicator.INDICATOR_DOT_SMALL_LEVEL_SIZE - this.mScaleMaskSize[1]) / 2.0f);
            this.mScaleMaskIndex = new float[]{0.0f, mediumMaskStart, smallMaskStart, smallMaskStart - ((COUIPageIndicator.INDICATOR_DOT_GONE_LEVEL_SIZE - this.mScaleMaskSize[2]) / 2.0f)};
            this.mScaleSize = new float[]{COUIPageIndicator.this.mNormalDotSize / 2.0f, COUIPageIndicator.this.mMediumDotSize / 2.0f, COUIPageIndicator.this.mSmallDotSize / 2.0f, 0.0f};
            this.mMaskOffset = 0.0f;
            this.mInterval = COUIPageIndicator.this.mDotsInterval * 2.0f;
            initSpring();
        }

        private void configCanvas(Canvas canvas) {
            this.mMatrix.reset();
            if (COUIPageIndicator.this.isLayoutRtl()) {
                this.mMatrix.setTranslate(this.mVisibleBounds[0] - this.mDrawHorizontalOffset, 0.0f);
                Matrix matrix = this.mMatrix;
                float[] visibleBounds = this.mVisibleBounds;
                float visibleStart = visibleBounds[0];
                matrix.postRotate(COUIPageIndicator.PI_IN_DEGREE, visibleStart + ((visibleBounds[1] - visibleStart) / 2.0f), COUIPageIndicator.this.mNormalDotSize / 2.0f);
            } else {
                this.mMatrix.setTranslate((-this.mVisibleBounds[0]) + this.mDrawHorizontalOffset, 0.0f);
            }
            canvas.setMatrix(this.mMatrix);
            this.mMatrix.invert(this.mInvertMatrix);
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "draw rect bounds = " + Arrays.toString(this.mVisibleBounds) + " horizontalOffset = " + this.mDrawHorizontalOffset);
        }

        private void drawDots(Canvas canvas) {
            int currentIndex;
            for (PageIndicatorDotModel pageIndicatorDotModel : this.mDots) {
                int dotIndex = this.mDots.indexOf(pageIndicatorDotModel);
                if (this.mCurrentOffset == 0.0f || (dotIndex != (currentIndex = this.mCurrentIndex) && dotIndex - 1 != currentIndex)) {
                    float centerX = pageIndicatorDotModel.mCenterX;
                    float radius = pageIndicatorDotModel.mRadius;
                    float dotRight = centerX + radius;
                    float[] visibleBounds = this.mVisibleBounds;
                    if (dotRight >= visibleBounds[0] && centerX - radius <= visibleBounds[1]) {
                        COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "drawDots: dot index = " + dotIndex + " dot radius = " + pageIndicatorDotModel.mRadius + " dot location = (" + pageIndicatorDotModel.mCenterX + ", " + pageIndicatorDotModel.mCenterY + ") left = " + this.mVisibleBounds[0] + " right = " + this.mVisibleBounds[1]);
                        if (dotIndex == this.mCurrentIndex) {
                            pageIndicatorDotModel.setFillColor(COUIPageIndicator.this.mTraceColor);
                        } else {
                            pageIndicatorDotModel.setFillColor(COUIPageIndicator.this.mDotColor);
                        }
                        pageIndicatorDotModel.onDraw(canvas, COUIPageIndicator.this.mDotPaint);
                    }
                }
            }
        }

        private void drawTrace(Canvas canvas) {
            float colorFactor = getColorFactor();
            if (colorFactor == 1.0f) {
                COUIPageIndicator.this.mTracePaint.setColor(COUIPageIndicator.this.mTraceColor);
                canvas.drawPath(this.mPath, COUIPageIndicator.this.mTracePaint);
                return;
            }
            if (this.mCurrentOffset <= 0.5f) {
                COUIPageIndicator.this.mTracePaint.setColor(COUIPageIndicator.this.mTraceColor);
                canvas.drawPath(this.mPathLeft, COUIPageIndicator.this.mTracePaint);
                COUIPageIndicator.this.mTracePaint.setColor(((Integer) COUIPageIndicator.COLOR_EVALUATOR.evaluate(colorFactor, Integer.valueOf(COUIPageIndicator.this.mDotColor), Integer.valueOf(COUIPageIndicator.this.mTraceColor))).intValue());
            } else {
                COUIPageIndicator.this.mTracePaint.setColor(((Integer) COUIPageIndicator.COLOR_EVALUATOR.evaluate(colorFactor, Integer.valueOf(COUIPageIndicator.this.mDotColor), Integer.valueOf(COUIPageIndicator.this.mTraceColor))).intValue());
                canvas.drawPath(this.mPathLeft, COUIPageIndicator.this.mTracePaint);
                COUIPageIndicator.this.mTracePaint.setColor(COUIPageIndicator.this.mTraceColor);
            }
            canvas.drawPath(this.mPathRight, COUIPageIndicator.this.mTracePaint);
        }

        private float getColorFactor() {
            float currentOffset = this.mCurrentOffset;
            if (currentOffset <= COUIPageIndicator.DEFAULT_COLOR_FACTOR_THRESHOLD) {
                return currentOffset / COUIPageIndicator.DEFAULT_COLOR_FACTOR_THRESHOLD;
            }
            if (currentOffset >= 0.95f) {
                return (1.0f - currentOffset) / COUIPageIndicator.DEFAULT_COLOR_FACTOR_THRESHOLD;
            }
            return 1.0f;
        }

        private float getMaskedSize(int level, float dotPosition) {
            if (level == 0) {
                return this.mScaleSize[level];
            }
            float largeMaskStart = this.mScaleMaskIndex[0];
            if (dotPosition < largeMaskStart) {
                if (this.mMovingToEnd) {
                    float[] scaleSizes = this.mScaleSize;
                    float currentSize = scaleSizes[level];
                    int previousLevel = level - 1;
                    float previousSize = scaleSizes[previousLevel];
                    float interpolation = COUIPageIndicator.DEFAULT_INTERPOLATOR.getInterpolation(dotPosition - this.mScaleMaskIndex[level]);
                    float[] maskIndex = this.mScaleMaskIndex;
                    return Math.max(currentSize, previousSize - (((previousSize - currentSize) * 2.0f) * (1.0f - (interpolation / (maskIndex[previousLevel] - maskIndex[level])))));
                }
                float[] scaleSizes = this.mScaleSize;
                int previousLevel = level - 1;
                float previousSize = scaleSizes[previousLevel];
                float currentSize = scaleSizes[level];
                float interpolation = (previousSize - currentSize) * 2.0f * COUIPageIndicator.DEFAULT_INTERPOLATOR.getInterpolation(dotPosition - this.mScaleMaskIndex[level]);
                float[] maskIndex = this.mScaleMaskIndex;
                return Math.min(previousSize, currentSize + (interpolation / (maskIndex[previousLevel] - maskIndex[level])));
            }
            if (dotPosition <= largeMaskStart + this.mScaleMaskSize[0]) {
                return 0.0f;
            }
            if (this.mMovingToEnd) {
                float[] scaleSizes = this.mScaleSize;
                int previousLevel = level - 1;
                float previousSize = scaleSizes[previousLevel];
                float currentSize = scaleSizes[level];
                float interpolation = (previousSize - currentSize) * 2.0f * COUIPageIndicator.DEFAULT_INTERPOLATOR.getInterpolation((this.mScaleMaskIndex[level] + this.mScaleMaskSize[level]) - dotPosition);
                float[] maskIndex = this.mScaleMaskIndex;
                float maskStart = maskIndex[level];
                float[] maskSizes = this.mScaleMaskSize;
                return Math.min(previousSize, currentSize + (interpolation / (((maskStart + maskSizes[level]) - maskIndex[previousLevel]) - maskSizes[previousLevel])));
            }
            float[] scaleSizes = this.mScaleSize;
            float currentSize = scaleSizes[level];
            int previousLevel = level - 1;
            float previousSize = scaleSizes[previousLevel];
            float interpolation = COUIPageIndicator.DEFAULT_INTERPOLATOR.getInterpolation((this.mScaleMaskIndex[level] + this.mScaleMaskSize[level]) - dotPosition);
            float[] maskIndex = this.mScaleMaskIndex;
            float maskStart = maskIndex[level];
            float[] maskSizes = this.mScaleMaskSize;
            return Math.max(currentSize, previousSize - (((previousSize - currentSize) * 2.0f) * (1.0f - (interpolation / (((maskStart + maskSizes[level]) - maskIndex[previousLevel]) - maskSizes[previousLevel])))));
        }

        private float getMoveFactor() {
            float factor;
            if (this.mCurrentOffset > DEFAULT_COLOR_FACTOR_THRESHOLD && this.mCurrentOffset <= DEFAULT_HALF_OF_PROGRESS) {
                factor = DEFAULT_INTERPOLATOR.getInterpolation((this.mCurrentOffset - DEFAULT_COLOR_FACTOR_THRESHOLD) / 0.45f) / 2.0f;
            } else if (this.mCurrentOffset > DEFAULT_HALF_OF_PROGRESS && this.mCurrentOffset < 0.95f) {
                factor = DEFAULT_INTERPOLATOR.getInterpolation(((1.0f - this.mCurrentOffset) - DEFAULT_COLOR_FACTOR_THRESHOLD) / 0.45f) / 2.0f;
            } else {
                factor = 0.0f;
            }
            if (factor < COUIPageIndicator.this.mMoveFactorThreshold) {
                return COUIPageIndicator.this.mMoveFactorThreshold;
            }
            if (factor > DEFAULT_HALF_OF_PROGRESS - COUIPageIndicator.this.mMoveFactorThreshold) {
                return DEFAULT_HALF_OF_PROGRESS - COUIPageIndicator.this.mMoveFactorThreshold;
            }
            return factor;
        }

        private float getRadius(int index) {
            float dotPosition = index - this.mMaskOffset;
            int level = 0;
            while (true) {
                float[] maskIndex = this.mScaleMaskIndex;
                if (level >= maskIndex.length) {
                    return 0.0f;
                }
                float maskStart = maskIndex[level];
                if (dotPosition >= maskStart && dotPosition <= maskStart + this.mScaleMaskSize[level]) {
                    float maskedSize = getMaskedSize(level, dotPosition);
                    COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "index, mMaskOffset = " + index + " " + this.mMaskOffset + " level = " + level + " dot position = " + dotPosition + " size = " + maskedSize + " moving to end = " + this.mMovingToEnd);
                    return maskedSize;
                }
                level++;
            }
        }

        private void initSpring() {
            SpringForce springForce = new SpringForce();
            springForce.setDampingRatio(1.0f);
            springForce.setStiffness(1500.0f);
            SpringAnimation springAnimation = new SpringAnimation(this, this.mCurrentPosition);
            this.mSpringAnimation = springAnimation;
            springAnimation.setSpring(springForce);
            this.mSpringAnimation.setMinimumVisibleChange(DEFAULT_MINIMUM_VISIBLE_CHANGE);
        }

        private void mapPoints(float[] points) {
            this.mInvertMatrix.mapPoints(points);
        }

        public void setCurrentPositionInternal(int position, float offset) {
            if (position < 0 || position + offset > this.mIndicatorCount - 1) {
                COUILog.e(COUIPageIndicator.TAG, "setCurrentPositionInternal position invalid");
                return;
            }
            float previousPosition = this.mCurrentIndex + this.mCurrentOffset;
            this.mCurrentIndex = position;
            this.mCurrentOffset = offset;
            float currentPosition = position + offset;
            float maskStart = this.mScaleMaskIndex[0] + this.mMaskOffset;
            float maskEnd = maskStart + this.mScaleMaskSize[0];
            if (currentPosition > maskEnd) {
                this.mMovingToEnd = true;
                this.mMaskOffset += currentPosition - maskEnd;
            } else if (currentPosition < maskStart) {
                this.mMovingToEnd = false;
                this.mMaskOffset += currentPosition - maskStart;
            }
            if (previousPosition > currentPosition && previousPosition >= Math.floor(maskEnd) && currentPosition <= Math.floor(maskEnd)) {
                this.mMaskOffset = (float) Math.floor(this.mMaskOffset);
            } else if (previousPosition < currentPosition && previousPosition <= Math.ceil(maskStart) && currentPosition >= Math.ceil(maskStart)) {
                this.mMaskOffset = (float) Math.ceil(this.mMaskOffset);
            }
            this.mVisibleOffset = Math.min(this.mIndicatorCount - MAX_VISIBLE_DOT_NUMBER, Math.max(0.0f, this.mMaskOffset - 1.0f));
            if (this.mIndicatorCount < MAX_VISIBLE_DOT_NUMBER) {
                this.mVisibleOffset = 0.0f;
            }
            this.mVisibleBounds[0] = this.mVisibleOffset * this.mInterval;
            this.mVisibleBounds[1] = this.mVisibleBounds[0] + (Math.min(MAX_VISIBLE_DOT_NUMBER, this.mIndicatorCount) * (this.mInterval + COUIPageIndicator.this.mNormalDotSize));
            this.mDrawHorizontalOffset = 0.0f;
            for (int i = 0; i < this.mDots.size(); i++) {
                PageIndicatorDotModel dot = this.mDots.get(i);
                dot.setRadius(getRadius(i));
                dot.setCenterX((COUIPageIndicator.this.mNormalDotSize / 2.0f) + (this.mInterval * i));
                dot.setCenterY(COUIPageIndicator.this.mNormalDotSize / 2.0f);
                dot.setOffsetX(this.mDrawHorizontalOffset);
            }
            updatePath();
            this.mViewHost.invalidate();
        }

        private void updatePath() {
            if (this.mCurrentIndex >= this.mDots.size() - 1) {
                return;
            }
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "updatePath: mCurrentOffset = " + this.mCurrentOffset + " dots size = " + this.mDots.size());
            PageIndicatorDotModel pageIndicatorDotModel = this.mDots.get(this.mCurrentIndex);
            PageIndicatorDotModel pageIndicatorDotModel2 = this.mDots.get(this.mCurrentIndex + 1);
            float centerX = pageIndicatorDotModel.getCenterX();
            float centerY = pageIndicatorDotModel.getCenterY();
            float radius = pageIndicatorDotModel.getRadius();
            float centerX2 = pageIndicatorDotModel2.getCenterX();
            float centerY2 = pageIndicatorDotModel2.getCenterY();
            float radius2 = pageIndicatorDotModel2.getRadius();
            float colorFactor = getColorFactor();
            float moveFactor = getMoveFactor();
            float currentOffset = this.mCurrentOffset;
            float departOffset = currentOffset <= 0.5f ? moveFactor * 2.0f * (this.mInterval + radius2 + radius) : 0.0f;
            float portOffset = currentOffset > 0.5f ? moveFactor * 2.0f * (this.mInterval + radius2 + radius) : 0.0f;
            float departCenterX = centerX + departOffset;
            float snapFactor = 0.5f - moveFactor;
            float control1X = departCenterX + (radius * snapFactor * 2.0f);
            float control1Y = (float) (((double) centerY) - Math.sqrt((radius * radius) - (((((radius * 2.0f) * snapFactor) * 2.0f) * radius) * snapFactor)));
            float portCenterX = centerX2 - portOffset;
            float control3X = portCenterX - ((radius2 * snapFactor) * 2.0f);
            float control3Y = (float) (((double) centerY2) - Math.sqrt((radius2 * radius2) - (((((radius2 * 2.0f) * snapFactor) * 2.0f) * radius2) * snapFactor)));
            float control2X = (control1X + control3X) / 2.0f;
            float control2Y = (((radius * radius) - (((control2X - centerX) - departOffset) * ((control1X - centerX) - departOffset))) / (control1Y - centerY)) + centerY;
            float snapAngle = (float) ((Math.asin(snapFactor * 2.0f) * 180.0d) / 3.141592653589793d);
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "updatePath: mCurrentOffset = " + this.mCurrentOffset + " dots size = " + this.mDots.size() + " startDot = (" + centerX + ", " + centerY + ", " + radius + ") endDot = (" + centerX2 + ", " + centerY2 + ", " + radius2 + ") colorFactor = " + colorFactor + " moveFactor = " + moveFactor + " mDepartOffset = " + departOffset + " mPortOffset = " + portOffset + ") control1 = (" + control1X + ", " + control1Y + ") control2 = (" + control2X + ", " + control2Y + ") control3 = (" + control3X + ", " + control3Y + ") snapAngle = " + snapAngle);
            this.mPath.reset();
            Path path = this.mPath;
            float traceLeft = centerX - radius;
            float traceRight = centerX2 + radius2;
            float traceHeight = COUIPageIndicator.this.mNormalDotSize;
            Path.Direction direction = Path.Direction.CW;
            path.addRect(traceLeft, 0.0f, traceRight, traceHeight, direction);
            this.mTraceRectPath.reset();
            this.mTraceRectPath.moveTo(traceLeft, 0.0f);
            this.mTraceRectPath.lineTo(traceRight, 0.0f);
            this.mTraceRectPath.lineTo(traceRight, COUIPageIndicator.this.mNormalDotSize);
            this.mTraceRectPath.lineTo(traceLeft, COUIPageIndicator.this.mNormalDotSize);
            this.mTraceRectPath.close();
            this.mPathUp.reset();
            this.mPathUp.moveTo(traceLeft, centerY);
            float startTop = centerY - radius;
            float startRight = centerX + radius;
            float startBottom = radius + centerY;
            this.mPathUp.arcTo(traceLeft, startTop, startRight, startBottom, COUIPageIndicator.PI_IN_DEGREE, COUIPageIndicator.HALF_OF_PI_IN_DEGREE, false);
            this.mPathUp.lineTo(departCenterX, startTop);
            float departLeft = traceLeft + departOffset;
            float departRight = startRight + departOffset;
            this.mPathUp.arcTo(departLeft, startTop, departRight, startBottom, COUIPageIndicator.ONE_AND_A_HALF_OF_PI_IN_DEGREE, snapAngle, false);
            this.mPathUp.quadTo(control2X, control2Y, control3X, control3Y);
            float endLeft = centerX2 - radius2;
            float portLeft = endLeft - portOffset;
            float endTop = centerY2 - radius2;
            float portRight = traceRight - portOffset;
            float endBottom = centerY2 + radius2;
            this.mPathUp.arcTo(portLeft, endTop, portRight, endBottom, COUIPageIndicator.ONE_AND_A_HALF_OF_PI_IN_DEGREE - snapAngle, snapAngle, false);
            this.mPathUp.lineTo(centerX2, endTop);
            this.mPathUp.arcTo(endLeft, endTop, traceRight, endBottom, COUIPageIndicator.ONE_AND_A_HALF_OF_PI_IN_DEGREE, COUIPageIndicator.HALF_OF_PI_IN_DEGREE, false);
            this.mPathUp.lineTo(traceRight, 0.0f);
            this.mPathUp.lineTo(traceLeft, 0.0f);
            this.mPathUp.close();
            this.mPathDown.reset();
            this.mPathDown.moveTo(traceRight, centerY2);
            this.mPathDown.arcTo(endLeft, endTop, traceRight, endBottom, 0.0f, COUIPageIndicator.HALF_OF_PI_IN_DEGREE, false);
            this.mPathDown.lineTo(portCenterX, endBottom);
            this.mPathDown.arcTo(portLeft, endTop, portRight, endBottom, COUIPageIndicator.HALF_OF_PI_IN_DEGREE, snapAngle, false);
            this.mPathDown.quadTo(control2X, (centerY2 * 2.0f) - control2Y, control1X, (centerY * 2.0f) - control1Y);
            this.mPathDown.arcTo(departLeft, startTop, departRight, startBottom, COUIPageIndicator.HALF_OF_PI_IN_DEGREE - snapAngle, snapAngle, false);
            this.mPathDown.lineTo(centerX, startBottom);
            this.mPathDown.arcTo(traceLeft, startTop, startRight, startBottom, COUIPageIndicator.HALF_OF_PI_IN_DEGREE, COUIPageIndicator.HALF_OF_PI_IN_DEGREE, false);
            this.mPathDown.lineTo(traceLeft, COUIPageIndicator.this.mNormalDotSize);
            this.mPathDown.lineTo(traceRight, COUIPageIndicator.this.mNormalDotSize);
            this.mPathDown.close();
            Path path2 = this.mPath;
            Path path3 = this.mPathUp;
            Path.Op op = Path.Op.DIFFERENCE;
            path2.op(path3, op);
            this.mPath.op(this.mPathDown, op);
            this.mPathLeft.reset();
            this.mPathRight.reset();
            this.mPathLeft.addRect(traceLeft, 0.0f, control2X + 0.5f, COUIPageIndicator.this.mNormalDotSize, direction);
            this.mPathRight.addRect(control2X, 0.0f, traceRight, COUIPageIndicator.this.mNormalDotSize, direction);
            Path path4 = this.mPathLeft;
            Path path5 = this.mPath;
            Path.Op op2 = Path.Op.INTERSECT;
            path4.op(path5, op2);
            this.mPathRight.op(this.mPath, op2);
        }

        private void verifyMask(boolean removeDot) {
            if (removeDot) {
                if (this.mIndicatorCount >= 6) {
                    this.mMaskOffset = Math.max(0.0f, this.mMaskOffset - 1.0f);
                } else {
                    this.mMaskOffset = 0.0f;
                }
            }
            if (this.mIndicatorCount < MAX_VISIBLE_DOT_NUMBER) {
                this.mScaleMaskSize[0] = 5.0f;
            } else {
                this.mScaleMaskSize[0] = INDICATOR_DOT_LARGE_LEVEL_SIZE;
            }
        }

        public void addDot(int id) {
            PageIndicatorDotModel pageIndicatorDotModel = new PageIndicatorDotModel(id);
            pageIndicatorDotModel.setFillColor(COUIPageIndicator.this.mDotColor);
            pageIndicatorDotModel.setCenterX(COUIPageIndicator.this.mNormalDotSize / 2.0f);
            pageIndicatorDotModel.setCenterY(COUIPageIndicator.this.mNormalDotSize / 2.0f);
            this.mDots.add(pageIndicatorDotModel);
            this.mIndicatorCount = this.mDots.size();
            verifyMask(false);
            setCurrentPositionInternal(this.mCurrentIndex, this.mCurrentOffset);
            this.mViewHost.requestLayout();
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "addDot: current index = " + this.mCurrentIndex + " mCurrentOffset = " + this.mCurrentOffset);
            pageIndicatorDotModel.dump();
        }

        public void dump() {
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "current index = " + this.mCurrentIndex + " offset = " + this.mCurrentOffset + " dots count = " + this.mIndicatorCount + " maskOffset = " + this.mMaskOffset + " visible rect = " + this.mVisibleRect);
            Iterator<PageIndicatorDotModel> it = this.mDots.iterator();
            while (it.hasNext()) {
                it.next().dump();
            }
        }

        public int getClickedDotIndex(float x, float y) {
            float[] point = {x, y};
            mapPoints(point);
            float closestDistance = -1.0f;
            int closestIndex = -1;
            for (PageIndicatorDotModel pageIndicatorDotModel : this.mDots) {
                if (pageIndicatorDotModel.getBounds().contains(point[0], point[1])) {
                    return this.mDots.indexOf(pageIndicatorDotModel);
                }
                float distance = COUIPageIndicator.this.isLayoutRtl() ? Math.abs(point[0] - (pageIndicatorDotModel.getBounds().centerX() - (pageIndicatorDotModel.getBounds().width() / 2.0f))) : Math.abs(point[0] - pageIndicatorDotModel.getBounds().centerX());
                if (closestIndex == -1 || distance < closestDistance) {
                    closestIndex = this.mDots.indexOf(pageIndicatorDotModel);
                    closestDistance = distance;
                }
            }
            return closestIndex;
        }

        public float getCurrentPosition() {
            return this.mCurrentIndex + this.mCurrentOffset;
        }

        public PageIndicatorDotModel getDot(int index) {
            if (index < 0 || index >= this.mDots.size()) {
                return null;
            }
            return this.mDots.get(index);
        }

        public int getDotsCount() {
            return this.mIndicatorCount;
        }

        public RectF getVisibleRect() {
            this.mVisibleRect.set(0.0f, 0.0f, Math.min(MAX_VISIBLE_DOT_NUMBER, this.mIndicatorCount) * (this.mInterval + COUIPageIndicator.this.mNormalDotSize), COUIPageIndicator.this.mNormalDotSize);
            return this.mVisibleRect;
        }

        public void onDraw(Canvas canvas) {
            canvas.save();
            configCanvas(canvas);
            drawDots(canvas);
            if (this.mCurrentOffset != 0.0f) {
                drawTrace(canvas);
            }
            canvas.restore();
        }

        public void removeDot() {
            if (this.mDots.size() == 0) {
                COUILog.e(COUIPageIndicator.TAG, "The mDots has no data");
                return;
            }
            this.mDots.removeLast();
            int size = this.mDots.size();
            this.mIndicatorCount = size;
            if (this.mCurrentIndex + this.mCurrentOffset > size - 1) {
                this.mCurrentIndex = size - 1;
                this.mCurrentOffset = 0.0f;
            }
            verifyMask(true);
            setCurrentPositionInternal(this.mCurrentIndex, this.mCurrentOffset);
            this.mViewHost.requestLayout();
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "removeDot: current index = " + this.mCurrentIndex + " currentOffset = " + this.mCurrentOffset + " count = " + this.mIndicatorCount);
        }

        public void setCurrentPosition(int position, float offset, boolean animate) {
            COUILog.d(COUIPageIndicator.DEBUG, COUIPageIndicator.TAG, "setCurrentPosition: position: " + position + " offset: " + offset + " animate: " + animate);
            if (!animate) {
                setCurrentPositionInternal(position, offset);
            } else {
                this.mSpringAnimation.setStartValue(getCurrentPosition());
                this.mSpringAnimation.animateToFinalPosition(position + offset);
            }
        }
    }

    static {
        DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
        DEFAULT_INTERPOLATOR = new COUIEaseInterpolator();
        COLOR_EVALUATOR = new ArgbEvaluator();
    }

    public COUIPageIndicator(Context context) {
        this(context, null);
    }

    private void buildModel() {
        this.mModel = new PageIndicatorModel(this);
    }

    private void initPaints() {
        Paint paint = new Paint(1);
        this.mDotPaint = paint;
        paint.setStyle(Paint.Style.FILL);
        Paint paint2 = new Paint(1);
        this.mTracePaint = paint2;
        paint2.setColor(this.mTraceColor);
    }

    public void addDot() {
        PageIndicatorModel pageIndicatorModel = this.mModel;
        pageIndicatorModel.addDot(pageIndicatorModel.getDotsCount());
    }

    @Override
    public boolean callOnClick() {
        OnIndicatorDotClickListener onIndicatorDotClickListener;
        if (this.mIsClickable && (onIndicatorDotClickListener = this.mOnDotClickListener) != null) {
            PageIndicatorModel pageIndicatorModel = this.mModel;
            float[] touchPoint = this.mTouchPoint;
            onIndicatorDotClickListener.onClick(pageIndicatorModel.getClickedDotIndex(touchPoint[0], touchPoint[1]));
        }
        invalidate();
        return super.callOnClick();
    }

    @Override
    public CharSequence getContentDescription() {
        StringBuilder sb = new StringBuilder();
        String string = getResources().getString(R.string.indicator_content_description);
        if (!TextUtils.isEmpty(this.mStartContentDescription)) {
            string = this.mStartContentDescription;
        }
        sb.append(string);
        sb.append(", ");
        int currentPage = this.mModel.mAccessCurrentIndex + 1;
        sb.append(getResources().getQuantityString(this.mIndicatorDescriptionID, currentPage, Integer.valueOf(currentPage), Integer.valueOf(this.mModel.mDots.size()), Integer.valueOf(this.mModel.mDots.size())));
        sb.append(", ");
        sb.append(getResources().getString(R.string.indicator_content_end));
        return sb.toString();
    }

    public int getDotsCount() {
        return this.mModel.getDotsCount();
    }

    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mModel.onDraw(canvas);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        RectF visibleRect = this.mModel.getVisibleRect();
        setMeasuredDimension((int) Math.ceil(visibleRect.width()), (int) Math.ceil(visibleRect.height()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        setCurrentPosition(position, positionOffset);
    }

    @Override
    public void onPageSelected(int position) {
        this.mModel.mAccessCurrentIndex = position;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        IndicatorSavedState indicatorSavedState = (IndicatorSavedState) parcelable;
        super.onRestoreInstanceState(indicatorSavedState.getSuperState());
        setDotsCount(indicatorSavedState.mDotsCount);
        float currentPosition = indicatorSavedState.mCurrentPosition;
        int position = (int) currentPosition;
        setCurrentPosition(position, currentPosition - position);
        if (DEBUG) {
            Log.d(TAG, "onRestoreInstanceState dotsCount = " + indicatorSavedState.mDotsCount + " currentPosition = " + indicatorSavedState.mCurrentPosition);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        IndicatorSavedState indicatorSavedState = new IndicatorSavedState(super.onSaveInstanceState());
        indicatorSavedState.mDotsCount = this.mModel.getDotsCount();
        indicatorSavedState.mCurrentPosition = this.mModel.getCurrentPosition();
        if (DEBUG) {
            Log.d(TAG, "onSaveInstanceState dotsCount = " + indicatorSavedState.mDotsCount + " currentPosition = " + indicatorSavedState.mCurrentPosition);
        }
        return indicatorSavedState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDownTime = System.currentTimeMillis();
        } else if (actionMasked == 1 && System.currentTimeMillis() - this.mDownTime <= ViewConfiguration.getTapTimeout()) {
            this.mTouchPoint[0] = motionEvent.getX();
            this.mTouchPoint[1] = motionEvent.getY();
            callOnClick();
        }
        return true;
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(null, R.styleable.COUIPageIndicator, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(null, R.styleable.COUIPageIndicator, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            this.mTraceColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIPageIndicator_traceDotColor, 0);
            this.mDotColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIPageIndicator_dotColor, 0);
            typedArrayObtainStyledAttributes.recycle();
        }
        setTraceDotColor(this.mTraceColor);
        setPageIndicatorDotsColor(this.mDotColor);
    }

    public void removeDot() {
        this.mModel.removeDot();
    }

    public void setCurrentPosition(int position) {
        setCurrentPosition(position, 0.0f);
    }

    @Deprecated
    public void setDotCornerRadius(int radius) {
    }

    @Deprecated
    public void setDotSpacing(int spacing) {
    }

    @Deprecated
    public void setDotStrokeWidth(int strokeWidth) {
    }

    public void setDotsCount(int count) {
        int dotsCount = count - getDotsCount();
        for (int index = 0; index < Math.abs(dotsCount); index++) {
            if (dotsCount > 0) {
                addDot();
            } else {
                removeDot();
            }
        }
    }

    public void setIndicatorDescriptionID(int indicatorDescriptionID) {
        try {
            getResources().getQuantityString(this.mIndicatorDescriptionID, 1, 1, 1, 1);
            this.mIndicatorDescriptionID = indicatorDescriptionID;
        } catch (Exception e) {
            COUILog.e(TAG, "setIndicatorDescriptionID indicatorDescriptionID error :" + e.getMessage());
        }
    }

    public void setIsClickable(boolean clickable) {
        this.mIsClickable = clickable;
    }

    public void setOnDotClickListener(OnIndicatorDotClickListener onIndicatorDotClickListener) {
        this.mOnDotClickListener = onIndicatorDotClickListener;
    }

    public void setPageIndicatorDotsColor(int color) {
        this.mDotColor = color;
        this.mDotPaint.setColor(color);
        invalidate();
    }

    public void setStartContentDescription(String startContentDescription) {
        this.mStartContentDescription = startContentDescription;
    }

    public void setTraceDotColor(int color) {
        this.mTraceColor = color;
        this.mTracePaint.setColor(color);
        invalidate();
    }

    @Deprecated
    public void snapToPosition() {
    }

    @Deprecated
    public void stopTraceAnimator() {
    }

    public static class IndicatorSavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<IndicatorSavedState> CREATOR = new Parcelable.Creator<IndicatorSavedState>() {
            @Override
            public IndicatorSavedState createFromParcel(Parcel parcel) {
                return new IndicatorSavedState(parcel, IndicatorSavedState.class.getClassLoader());
            }

            @Override
            public IndicatorSavedState[] newArray(int size) {
                return new IndicatorSavedState[size];
            }
        };
        float mCurrentPosition;
        int mDotsCount;

        public IndicatorSavedState(Parcel parcel) {
            super(parcel);
            this.mDotsCount = 0;
            this.mCurrentPosition = 0.0f;
            readFromParcel(parcel);
        }

        private void readFromParcel(Parcel parcel) {
            this.mDotsCount = parcel.readInt();
            this.mCurrentPosition = parcel.readFloat();
        }

        public String toString() {
            return "IndicatorSavedState{" + Integer.toHexString(System.identityHashCode(this)) + "mDotsCount = " + this.mDotsCount + " mCurrentPosition = " + this.mCurrentPosition + "}";
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(this.mDotsCount);
            parcel.writeFloat(this.mCurrentPosition);
        }

        public IndicatorSavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.mDotsCount = 0;
            this.mCurrentPosition = 0.0f;
            readFromParcel(parcel);
        }

        public IndicatorSavedState(Parcelable parcelable) {
            super(parcelable);
            this.mDotsCount = 0;
            this.mCurrentPosition = 0.0f;
        }
    }

    public COUIPageIndicator(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiPageIndicatorStyle);
    }

    public void setCurrentPosition(int position, float offset) {
        setCurrentPosition(position, offset, false);
    }

    public COUIPageIndicator(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, COUIContextUtil.isCOUIDarkTheme(context) ? R.style.Widget_COUI_COUIPageIndicator_Dark : R.style.Widget_COUI_COUIPageIndicator);
    }

    public void setCurrentPosition(int position, float offset, boolean animate) {
        this.mModel.setCurrentPosition(position, offset, animate);
        invalidate();
    }

    public COUIPageIndicator(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mTouchPoint = new float[2];
        this.mMoveFactorThreshold = DEFAULT_MOVE_FACTOR_THRESHOLD;
        this.mDownTime = 0L;
        this.mIndicatorDescriptionID = R.plurals.coui_page_indicator_description;
        if (attributeSet == null || attributeSet.getStyleAttribute() == 0) {
            this.mStyle = defStyleAttr;
        } else {
            this.mStyle = attributeSet.getStyleAttribute();
        }
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUIPageIndicator, defStyleAttr, defStyleRes);
        this.mTraceColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIPageIndicator_traceDotColor, 0);
        this.mDotColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIPageIndicator_dotColor, 0);
        this.mNormalDotSize = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIPageIndicator_dotSize, 0.0f);
        this.mMediumDotSize = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIPageIndicator_dotSizeMedium, 0.0f);
        this.mSmallDotSize = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIPageIndicator_dotSizeSmall, 0.0f);
        this.mDotsInterval = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIPageIndicator_dotSpacing, 0.0f);
        this.mIsClickable = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIPageIndicator_dotClickable, true);
        typedArrayObtainStyledAttributes.recycle();
        buildModel();
        initPaints();
        setFocusable(false);
    }
}
