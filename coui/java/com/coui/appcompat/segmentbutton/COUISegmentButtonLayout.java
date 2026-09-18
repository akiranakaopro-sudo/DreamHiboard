package com.coui.appcompat.segmentbutton;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;

import com.coui.appcompat.R;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;



import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.state.COUIStrokeDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.component.responsiveui.ResponsiveUIModel;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import com.oplus.graphics.OplusPathAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;





/**
 * Leapy added 2026-08-01: Segment control recovered from the decoded ColorOS
 * 16 COUI source. Obfuscated resource and AndroidX symbols are mapped to their
 * source equivalents; OPPO's spring, text, press, drag and rubber-band paths
 * are retained.
 */
public class COUISegmentButtonLayout extends LinearLayout implements ISegmentButtonLayout {
    private static final int COLOR_ANIMATOR_TIME = 300;
    private static final boolean COUI_DEBUG;
    private static final float DEFAULT_OVER_STIFFNESS = 0.12f;
    private static final int DEFAULT_SELECT_POSITION = 0;
    private static final float DEFAULT_SPRING_BOUNCE = 0.0f;
    private static final float DEFAULT_SPRING_RESPONSE = 0.3f;
    private static final int GRID_NUMBER_LARGE = 8;
    private static final int GRID_NUMBER_MEDIUM = 6;
    private static final int GRID_NUMBER_SMALL = 4;
    private static final float POS_DIFF_CURVE_EXPONENT = 1.0f;
    private static final float POS_SPRING_BOUNCE = 0.1f;
    private static final float POS_SPRING_BOUNCE_MIN = 0.03f;
    private static final float POS_SPRING_RESPONSE = 0.4f;
    private static final float POS_SPRING_VELOCITY = 0.0f;
    private static final float SHADOW_DX = 0.0f;
    private static final String TAG = "COUISegmentButtonLayout";
    private static final float WIDTH_DIFF_CURVE_EXPONENT = 2.0f;
    private static final float WIDTH_SPRING_BOUNCE = 0.5f;
    private static final float WIDTH_SPRING_BOUNCE_MIN = 0.1f;
    private static final float WIDTH_SPRING_RESPONSE = 0.6f;
    private static final float WIDTH_SPRING_RESPONSE_MIN = 0.4f;
    private static final float WIDTH_SPRING_VELOCITY = 1000.0f;
    private AttributeSet mAttrs;
    private int mBackgroundColor;
    private final Path mBackgroundPath;
    private final ArrayList<TextView> mButtons;
    private final ArrayList<Integer> mChildWidths;
    private final Paint mContainerPaint;
    private float mCustomTextSizePx;
    private SegmentButtonDrawDelegate mDrawDelegate;
    private boolean mEnableRubberEffect;
    private boolean mEnableTextScale;
    private float mIndicatorCenterLastX;
    private float mIndicatorCenterX;
    private float mIndicatorCenterY;
    private int mIndicatorColor;
    private float mIndicatorHeight;
    private final Paint mIndicatorPaint;
    private final RectF mIndicatorRectF;
    private float mIndicatorScale;
    private COUIPressFeedbackHelper mIndicatorScaleHelper;
    private final int mIndicatorShadowOffset;
    private float mIndicatorWidth;
    private boolean mIsDragging;
    private boolean mIsNeedVibration;
    private boolean mIsNightMode;
    private boolean mIsResponsiveWidthEnabled;
    private boolean mIsSizeChanged;
    private int mLastSelectedPosition;
    private int mLastTouchPosition;
    private float mLastX;
    private OnSelectedSegmentChangeListener mOnSelectedSegmentChangeListener;
    private int mOverDistance;
    private float mOverStiffness;
    private final ArrayList<COUIPressFeedbackHelper> mPressScaleHelper;
    private float mResponse;
    private final ResponsiveUIModel mResponsiveUIModel;
    private final int mSegmentExtraWidth;
    private final int mSegmentExtraWidthSmall;
    private final int mSegmentMinWidth;
    private final int mSegmentPaddingHorizontal;
    private final int mSegmentPaddingRegular;
    private final int mSegmentPaddingSmall;
    private String[] mSegmentStrings;
    private int mSelectedPosition;
    private int mSelectedTextColor;
    private int mShadowColor;
    private final int[] mShadowGradientColors;
    private int mShadowGradientEndColor;
    private final Paint mShadowGradientPaint;
    private final float[] mShadowGradientStops;
    private final int mShadowRadius;
    private final RectF mShadowRectF;
    private SmoothRoundCornerHelper mSmoothRoundCornerHelper;
    private COUISpringAnimation mSpringAnimationPos;
    private COUISpringAnimation mSpringAnimationRubber;
    private COUISpringAnimation mSpringAnimationWidth;
    private int mStyleAttr;
    private int mStyleRes;
    private int mSwitchDistance;
    private final ArrayList<SegmentButtonTextColorChangeHelper> mTextColorChangeHelper;
    private TextPaint mTextPaint;
    private final TextView mTextView;
    private final Rect mTmpRect;
    private final RectF mTmpRectF;
    private int mTouchPosition;
    private int mTouchSlop;
    private int mUnselectedTextColor;

    private static class IndexLengthPair {
        int mIndex;
        int mLength;

        IndexLengthPair(int index, int length) {
            this.mIndex = index;
            this.mLength = length;
        }
    }

    public interface OnSelectedSegmentChangeListener {
        void onSelectedSegmentChange(int fromIndex, int toIndex, float progress);
    }

    public interface SegmentButtonDrawDelegate {
        default Paint[] getCustomBackgroundPaint() {
            Paint customBackrgoundPaint = setCustomBackrgoundPaint();
            if (customBackrgoundPaint != null) {
                return new Paint[]{customBackrgoundPaint};
            }
            return null;
        }

        default Paint[] getCustomIndicatorPaint() {
            Paint customIndicatorPaint = setCustomIndicatorPaint();
            if (customIndicatorPaint != null) {
                return new Paint[]{customIndicatorPaint};
            }
            return null;
        }

        @Deprecated
        default Boolean needProxy() {
            return Boolean.TRUE;
        }

        @Deprecated
        default Paint setCustomBackrgoundPaint() {
            return null;
        }

        @Deprecated
        default Paint setCustomIndicatorPaint() {
            return null;
        }
    }

    private class SegmentButtonTextColorChangeHelper {
        private final ArgbEvaluator mColorEvaluator = new ArgbEvaluator();
        private float mCurrentFraction;
        private TextView mSegment;
        private final ValueAnimator mTextColorSelectAnimator;
        private final ValueAnimator mTextColorUnSelectAnimator;

        public SegmentButtonTextColorChangeHelper(TextView textView) {
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mTextColorSelectAnimator = valueAnimatorOfFloat;
            ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mTextColorUnSelectAnimator = valueAnimatorOfFloat2;
            this.mCurrentFraction = 0.0f;
            this.mSegment = textView;
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SegmentButtonTextColorChangeHelper.this.mCurrentFraction = valueAnimator.getAnimatedFraction();
                    SegmentButtonTextColorChangeHelper.this.mSegment.setTextColor(((Integer) SegmentButtonTextColorChangeHelper.this.mColorEvaluator.evaluate(SegmentButtonTextColorChangeHelper.this.mCurrentFraction, Integer.valueOf(COUISegmentButtonLayout.this.mUnselectedTextColor), Integer.valueOf(COUISegmentButtonLayout.this.mSelectedTextColor))).intValue());
                }
            });
            valueAnimatorOfFloat.setDuration(COLOR_ANIMATOR_TIME);
            valueAnimatorOfFloat.setInterpolator(new COUIMoveEaseInterpolator());
            valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SegmentButtonTextColorChangeHelper.this.mCurrentFraction = 1.0f - valueAnimator.getAnimatedFraction();
                    SegmentButtonTextColorChangeHelper.this.mSegment.setTextColor(((Integer) SegmentButtonTextColorChangeHelper.this.mColorEvaluator.evaluate(SegmentButtonTextColorChangeHelper.this.mCurrentFraction, Integer.valueOf(COUISegmentButtonLayout.this.mUnselectedTextColor), Integer.valueOf(COUISegmentButtonLayout.this.mSelectedTextColor))).intValue());
                }
            });
            valueAnimatorOfFloat2.setDuration(COLOR_ANIMATOR_TIME);
            valueAnimatorOfFloat2.setInterpolator(new COUIMoveEaseInterpolator());
        }

        public void startAnimation(boolean forward) {
            if (forward) {
                this.mTextColorUnSelectAnimator.cancel();
                this.mTextColorSelectAnimator.setCurrentFraction(this.mCurrentFraction);
                this.mTextColorSelectAnimator.start();
            } else {
                this.mTextColorSelectAnimator.cancel();
                this.mTextColorUnSelectAnimator.setCurrentFraction(1.0f - this.mCurrentFraction);
                this.mTextColorUnSelectAnimator.start();
            }
        }
    }

    private final class SmoothRoundCornerHelper {
        private OplusPathAdapter mPathAdapter;
        private final int mSmoothType;

        SmoothRoundCornerHelper() {
            this.mPathAdapter = null;
            int smoothStyleType = RoundCornerUtil.getSmoothStyleType();
            this.mSmoothType = smoothStyleType;
            if (smoothStyleType == 1) {
                this.mPathAdapter = new OplusPathAdapter(COUISegmentButtonLayout.this.mBackgroundPath, smoothStyleType);
            }
        }

        OplusPathAdapter getPathAdapter() {
            return this.mPathAdapter;
        }

        int getSmoothType() {
            return this.mSmoothType;
        }

        void updatePath(RectF rectF, float bounds) {
            COUIShapePath.getRoundRectPath(COUISegmentButtonLayout.this.mBackgroundPath, rectF, bounds);
        }
    }

    static {
        COUI_DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public COUISegmentButtonLayout(Context context) {
        this(context, null);
    }

    private void animateIndicatorToPosition(int index) {
        COUISpringAnimation cOUISpringAnimation = this.mSpringAnimationRubber;
        if (cOUISpringAnimation != null && cOUISpringAnimation.isRunning()) {
            this.mSpringAnimationRubber.cancel();
        }
        TextView segmentAt = getSegmentAt(index);
        if (segmentAt == null) {
            COUILog.w(TAG, index + "out of range of segment button layout");
            return;
        }
        float left = segmentAt.getLeft();
        float right = segmentAt.getRight();
        float indicatorCenterLastX = (left + right) / 2.0f;
        float value = right - left;
        ensureSpringAnimation();
        this.mSpringAnimationPos.setStartValue(this.mIndicatorCenterX);
        this.mSpringAnimationPos.animateToFinalPosition(indicatorCenterLastX);
        this.mIndicatorCenterLastX = indicatorCenterLastX;
        this.mSpringAnimationWidth.setStartValue(this.mIndicatorWidth);
        this.mSpringAnimationWidth.animateToFinalPosition(value);
    }

    private void attemptClaimDrag() {
        if (getParent() instanceof ViewGroup) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void calculateChildrenWidths(float value) {
        int size = this.mButtons.size();
        if (size == 0) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        int index_2 = 0;
        for (int index = 0; index < size; index++) {
            arrayList.add(new IndexLengthPair(index, getSegmentTextWidth(this.mButtons.get(index))));
        }
        Collections.sort(arrayList, new Comparator<IndexLengthPair>() {
            @Override
            public int compare(IndexLengthPair indexLengthPair, IndexLengthPair indexLengthPair2) {
                return Integer.compare(indexLengthPair.mLength, indexLengthPair2.mLength);
            }
        });
        float value_2 = value / size;
        float[] fArr = new float[size];
        int index_3 = 0;
        float value_3 = value_2;
        int index_4 = -1;
        while (true) {
            if (index_3 >= size) {
                break;
            }
            float value_4 = ((IndexLengthPair) arrayList.get(index_3)).mLength;
            if (value_4 < value_3) {
                fArr[((IndexLengthPair) arrayList.get(index_3)).mIndex] = value_4;
                value -= value_4;
                index_3++;
            } else if (index_4 == index_3) {
                while (index_3 < size) {
                    fArr[((IndexLengthPair) arrayList.get(index_3)).mIndex] = value_3;
                    index_3++;
                }
                value = 0.0f;
            } else {
                value_3 = value / (size - index_3);
                index_4 = index_3;
            }
        }
        if (index_4 == -1) {
            this.mChildWidths.clear();
            while (index_2 < size) {
                this.mChildWidths.add(Integer.valueOf(Math.round(value_2)));
                index_2++;
            }
            return;
        }
        if (value > 0.0f) {
            int index_5 = 0;
            while (true) {
                int index_6 = size - 1;
                if (index_5 >= index_6) {
                    break;
                }
                int index_7 = ((IndexLengthPair) arrayList.get(index_5)).mIndex;
                fArr[index_7] = fArr[index_7] + (value / index_6);
                index_5++;
            }
        }
        this.mChildWidths.clear();
        while (index_2 < size) {
            this.mChildWidths.add(Integer.valueOf(Math.round(fArr[index_2])));
            index_2++;
        }
    }

    private float calculateDynamicWidthParam(float value, float value_2, float value_3, float value_4, float value_5) {
        if (value <= 0.0f || value_2 <= 0.0f) {
            return value_3;
        }
        return Math.max(0.0f, Math.min(1.0f, value_3 - ((value_3 - value_4) * ((float) Math.pow(Math.abs(value - value_2) / Math.max(value, value_2), value_5)))));
    }

    private static double[] calculateRubberBand(double doubleValue, double doubleValue_2, double[] dArr) {
        double[] dArr2 = new double[dArr.length];
        for (int index = 0; index < dArr.length; index++) {
            if (doubleValue_2 == 0.0d || doubleValue == 0.0d) {
                dArr2[index] = 0.0d;
            } else {
                dArr2[index] = (1.0d - (1.0d / (((Math.abs(dArr[index]) * doubleValue) / doubleValue_2) + 1.0d))) * doubleValue_2 * Math.signum(dArr[index]);
            }
        }
        return dArr2;
    }

    private void drawButtonBackground(Canvas canvas, Paint paint) {
        drawSmoothRoundRect(canvas, this.mTmpRectF, paint);
    }

    private void drawIndicator(Canvas canvas, Boolean bool, Paint paint) {
        if (bool.booleanValue()) {
            drawShadowWithGradient(canvas);
            return;
        }
        canvas.save();
        drawSmoothRoundRect(canvas, this.mIndicatorRectF, paint);
        canvas.restore();
    }

    private void drawShadowWithGradient(Canvas canvas) {
        int iSave = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        float drawableRadius = getDrawableRadius(this.mIndicatorRectF);
        this.mBackgroundPath.reset();
        this.mBackgroundPath.addRoundRect(this.mIndicatorRectF, drawableRadius, drawableRadius, Path.Direction.CCW);
        canvas.clipPath(this.mBackgroundPath, Region.Op.DIFFERENCE);
        RectF rectF = this.mShadowRectF;
        RectF rectF_2 = this.mIndicatorRectF;
        rectF.left = rectF_2.left;
        rectF.right = rectF_2.right;
        rectF.top = rectF_2.top + this.mIndicatorShadowOffset;
        rectF.bottom = this.mTmpRectF.bottom;
        if (this.mIsNightMode) {
            this.mShadowGradientPaint.setStyle(Paint.Style.FILL);
            this.mShadowGradientPaint.setShader(null);
            this.mShadowGradientPaint.setColor(this.mShadowColor);
        } else {
            RadialGradient radialGradient = new RadialGradient(rectF_2.centerX(), this.mIndicatorRectF.centerY(), this.mShadowRectF.width() / 2.0f, this.mShadowGradientColors, this.mShadowGradientStops, Shader.TileMode.CLAMP);
            this.mShadowGradientPaint.setStyle(Paint.Style.FILL);
            this.mShadowGradientPaint.setShader(radialGradient);
        }
        canvas.drawRoundRect(this.mShadowRectF, drawableRadius, drawableRadius, this.mShadowGradientPaint);
        canvas.restoreToCount(iSave);
    }

    private void drawSmoothRoundRect(Canvas canvas, RectF rectF, Paint paint) {
        float drawableRadius = getDrawableRadius(rectF);
        int smoothType = this.mSmoothRoundCornerHelper.getSmoothType();
        if (smoothType == 0) {
            this.mBackgroundPath.reset();
            this.mSmoothRoundCornerHelper.updatePath(rectF, drawableRadius);
            canvas.clipPath(this.mBackgroundPath);
            canvas.drawRect(rectF, paint);
            return;
        }
        if (smoothType != 1) {
            canvas.drawRoundRect(rectF, drawableRadius, drawableRadius, paint);
            return;
        }
        OplusPathAdapter pathAdapter = this.mSmoothRoundCornerHelper.getPathAdapter();
        this.mBackgroundPath.reset();
        pathAdapter.addSmoothRoundRect(rectF.left, rectF.top, rectF.right, rectF.bottom, drawableRadius, drawableRadius, Path.Direction.CCW);
        canvas.clipPath(this.mBackgroundPath);
        canvas.drawRect(rectF, paint);
    }

    private void ensureIndicatorScaleAnimation() {
        if (this.mIndicatorScaleHelper == null) {
            COUIPressFeedbackHelper cOUIPressFeedbackHelper = new COUIPressFeedbackHelper(getContext());
            this.mIndicatorScaleHelper = cOUIPressFeedbackHelper;
            cOUIPressFeedbackHelper.setCallback(new COUIPressFeedbackHelper.COUIPressFeedbackHelperCallback() {
                @Override
                public int getTargetHeight() {
                    return (int) COUISegmentButtonLayout.this.mIndicatorHeight;
                }

                @Override
                public int getTargetWidth() {
                    return (int) COUISegmentButtonLayout.this.mIndicatorWidth;
                }

                @Override
                public void onScaleUpdate(float indicatorScale) {
                    COUISegmentButtonLayout.this.mIndicatorScale = indicatorScale;
                    COUISegmentButtonLayout.this.updateIndicatorRect();
                }
            });
        }
    }

    private void ensureSpringAnimation() {
        if (this.mSpringAnimationPos == null) {
            COUISpringForce cOUISpringForce = new COUISpringForce();
            cOUISpringForce.setBounce(DEFAULT_SPRING_BOUNCE);
            cOUISpringForce.setResponse(this.mResponse);
            COUISpringAnimation cOUISpringAnimation = new COUISpringAnimation(new FloatValueHolder(this.mIndicatorCenterX));
            this.mSpringAnimationPos = cOUISpringAnimation;
            cOUISpringAnimation.setSpring(cOUISpringForce);
            this.mSpringAnimationPos.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public final void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float value_2) {
                    COUISegmentButtonLayout.this.lambda$ensureSpringAnimation$0(
                            cOUIDynamicAnimation, value, value_2);
                }
            });
            this.mSpringAnimationPos.addEndListener(new COUIDynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean flag, float value, float value_2) {
                    COUISegmentButtonLayout.this.mEnableRubberEffect = true;
                }
            });
        }
        if (this.mSpringAnimationWidth == null) {
            COUISpringForce cOUISpringForce2 = new COUISpringForce();
            cOUISpringForce2.setBounce(DEFAULT_SPRING_BOUNCE);
            cOUISpringForce2.setResponse(this.mResponse);
            COUISpringAnimation cOUISpringAnimation2 = new COUISpringAnimation(new FloatValueHolder(this.mIndicatorWidth));
            this.mSpringAnimationWidth = cOUISpringAnimation2;
            cOUISpringAnimation2.setSpring(cOUISpringForce2);
            this.mSpringAnimationWidth.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public final void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float value_2) {
                    COUISegmentButtonLayout.this.lambda$ensureSpringAnimation$1(
                            cOUIDynamicAnimation, value, value_2);
                }
            });
        }
        if (this.mSpringAnimationRubber == null) {
            COUISpringForce cOUISpringForce3 = new COUISpringForce();
            cOUISpringForce3.setBounce(DEFAULT_SPRING_BOUNCE);
            cOUISpringForce3.setResponse(this.mResponse);
            COUISpringAnimation cOUISpringAnimation3 = new COUISpringAnimation(new FloatValueHolder(this.mIndicatorCenterX));
            this.mSpringAnimationRubber = cOUISpringAnimation3;
            cOUISpringAnimation3.setSpring(cOUISpringForce3);
            this.mSpringAnimationRubber.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public final void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float value_2) {
                    COUISegmentButtonLayout.this.lambda$ensureSpringAnimation$2(
                            cOUIDynamicAnimation, value, value_2);
                }
            });
        }
        ensureIndicatorScaleAnimation();
    }

    private void executeScaleAnimatorAt(boolean flag, int index) {
        if (index < 0 || index >= this.mPressScaleHelper.size()) {
            return;
        }
        this.mPressScaleHelper.get(index).executeFeedbackAnimator(flag);
    }

    private void executeTextColorChangeAnimatorAt(boolean flag, int index) {
        if (index < 0 || index >= this.mTextColorChangeHelper.size()) {
            return;
        }
        this.mTextColorChangeHelper.get(index).startAnimation(flag);
    }

    private float getDrawableRadius(RectF rectF) {
        return (rectF.bottom - rectF.top) / 2.0f;
    }

    private float getEffectiveMaxExtraWidthForShortToLong() {
        return isSmallPadding() ? this.mSegmentExtraWidthSmall : this.mSegmentExtraWidth;
    }

    private float getEffectiveTranslationBounceForShortToLong(float value, float value_2) {
        if (!isSmallPadding() || value <= 0.0f || value_2 <= 0.0f) {
            return 0.1f;
        }
        return calculateDynamicWidthParam(value, value_2, POS_SPRING_BOUNCE, POS_SPRING_BOUNCE_MIN, 1.0f);
    }

    private float getPositionChangeProgress() {
        int size = this.mButtons.size();
        if (size <= 1) {
            return 0.0f;
        }
        float value = this.mIndicatorCenterX;
        float left = (this.mButtons.get(0).getLeft() + this.mButtons.get(0).getRight()) / 2.0f;
        int index = 1;
        float value_2 = left;
        while (index < size) {
            float left2 = (this.mButtons.get(index).getLeft() + this.mButtons.get(index).getRight()) / 2.0f;
            float fMin = Math.min(value_2, left2);
            float fMax = Math.max(value_2, left2);
            if (value >= fMin && value <= fMax) {
                return (index - 1) + Math.max(0.0f, Math.min(1.0f, left2 != value_2 ? (value - value_2) / (left2 - value_2) : 0.0f));
            }
            index++;
            value_2 = left2;
        }
        if (Math.abs(value - left) <= Math.abs(value - value_2)) {
            return 0.0f;
        }
        return size - 1;
    }

    private int getResponsiveWidth() {
        int screenWidthMetrics = UIUtil.getScreenWidthMetrics(getContext());
        int screenHeightMetrics = UIUtil.getScreenHeightMetrics(getContext());
        this.mResponsiveUIModel.rebuild(screenWidthMetrics, screenHeightMetrics).chooseMargin(MarginType.MARGIN_SMALL);
        return COUIResponsiveUtils.isLargeScreen(getContext(), screenWidthMetrics, screenHeightMetrics) ? this.mResponsiveUIModel.calculateGridWidth(GRID_NUMBER_LARGE) : COUIResponsiveUtils.isMediumScreen(getContext(), screenWidthMetrics, screenHeightMetrics) ? this.mResponsiveUIModel.calculateGridWidth(GRID_NUMBER_MEDIUM) : this.mResponsiveUIModel.calculateGridWidth(GRID_NUMBER_SMALL);
    }

    private int getSegmentTextWidth(TextView textView) {
        TextPaint paint = this.mTextPaint;
        if (paint == null) {
            paint = textView.getPaint();
        }
        return Math.max(((int) paint.measureText(textView.getText().toString())) + (this.mSegmentPaddingHorizontal * 2), this.mSegmentMinWidth);
    }

    private int getSelectedIndexFromX(MotionEvent motionEvent) {
        for (int index = 0; index < this.mButtons.size(); index++) {
            if (isInsideView(this.mButtons.get(index), motionEvent.getX())) {
                return index;
            }
        }
        return -1;
    }

    private void handleActionCancel() {
        resetTouchEffect();
        this.mIsDragging = false;
    }

    private void handleRubberBandEffect(float value) {
        if (this.mEnableRubberEffect) {
            this.mLastX = value;
            this.mEnableRubberEffect = false;
        }
        float value_2 = value - this.mLastX;
        if (value_2 != 0.0f) {
            double[] dArrCalculateRubberBand = calculateRubberBand(this.mOverStiffness, this.mOverDistance, new double[]{value_2});
            ensureSpringAnimation();
            this.mSpringAnimationRubber.setStartValue(this.mIndicatorCenterX);
            this.mSpringAnimationRubber.animateToFinalPosition(this.mIndicatorCenterLastX + ((float) dArrCalculateRubberBand[0]));
        }
    }

    private void initPaint() {
        Paint paint = this.mContainerPaint;
        Paint.Style style = Paint.Style.FILL;
        paint.setStyle(style);
        this.mContainerPaint.setAntiAlias(true);
        this.mIndicatorPaint.setStyle(style);
        this.mIndicatorPaint.setAntiAlias(true);
        initShadowGradient();
        TextViewCompat.setTextAppearance(this.mTextView, R.style.couiTextButtonM);
        refreshTextPaint();
    }

    private void initShadowGradient() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setGradientType(1);
        updateShadowGradientColors();
    }

    private void initStateEffectBackground(View view) {
        Drawable background = view.getBackground();
        if (view instanceof TextView) {
            COUIStrokeDrawable cOUIStrokeDrawable = new COUIStrokeDrawable(getContext());
            cOUIStrokeDrawable.setStrokeType(1);
            view.setDefaultFocusHighlightEnabled(false);
            cOUIStrokeDrawable.setCallback(view);
            COUIMaskEffectDrawable cOUIMaskEffectDrawable = new COUIMaskEffectDrawable(getContext(), 0);
            if (background == null) {
                background = new ColorDrawable(0);
            }
            view.setBackground(new COUIStateEffectDrawable(new Drawable[]{background, cOUIMaskEffectDrawable, cOUIStrokeDrawable}));
        }
    }

    private void initializeSegmentButton(final TextView textView, final int index, String str) {
        textView.setGravity(17);
        textView.setTextAlignment(4);
        textView.setIncludeFontPadding(false);
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        int index_2 = this.mSegmentPaddingHorizontal;
        ViewCompat.setPaddingRelative(textView, index_2, 0, index_2, 0);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setMinWidth(this.mSegmentMinWidth);
        if (index == this.mSelectedPosition) {
            textView.setSelected(true);
        }
        ViewCompat.setAccessibilityDelegate(textView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat dVar) {
                super.onInitializeAccessibilityNodeInfo(view, dVar);
                dVar.setClassName(COUIAccessibilityUtil.BUTTON_CLASS_NAME);
                dVar.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(0, 1, index, 1, false, textView.isSelected()));
                if (!textView.isSelected()) {
                    dVar.setStateDescription(COUISegmentButtonLayout.this.getContext().getResources().getString(R.string.coui_accessibility_unselected));
                } else {
                    dVar.setClickable(false);
                    dVar.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
                }
            }

            @Override
            public boolean performAccessibilityAction(View view, int index_3, Bundle bundle) {
                if (index_3 != AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.getId()) {
                    return super.performAccessibilityAction(view, index_3, bundle);
                }
                COUISegmentButtonLayout.this.selectSegmentAt(textView);
                return true;
            }
        });
        initStateEffectBackground(textView);
        this.mPressScaleHelper.add(new COUIPressFeedbackHelper(textView));
        this.mTextColorChangeHelper.add(new SegmentButtonTextColorChangeHelper(textView));
        textView.setText(str);
        refreshSegmentTextConfig(textView, index);
    }

    private boolean isInsideView(View view, float value) {
        if (view == null || view.getVisibility() == 8) {
            return false;
        }
        float value_2 = this.mIsDragging ? this.mSwitchDistance : 0.0f;
        return value >= ((float) view.getLeft()) + value_2 && value <= ((float) view.getRight()) - value_2;
    }

    private boolean isSmallPadding() {
        int paddingStart = getPaddingStart();
        return Math.abs(paddingStart - this.mSegmentPaddingSmall) < Math.abs(paddingStart - this.mSegmentPaddingRegular);
    }


    public void lambda$ensureSpringAnimation$0(COUIDynamicAnimation cOUIDynamicAnimation, float value, float value_2) {
        this.mIndicatorCenterX = value;
        updateIndicatorRect();
        if (this.mOnSelectedSegmentChangeListener != null) {
            this.mOnSelectedSegmentChangeListener.onSelectedSegmentChange(this.mLastSelectedPosition, this.mSelectedPosition, getPositionChangeProgress());
        }
    }


    public void lambda$ensureSpringAnimation$1(COUIDynamicAnimation cOUIDynamicAnimation, float value, float value_2) {
        this.mIndicatorWidth = value;
        if (COUI_DEBUG) {
            COUILog.d(TAG, "update width: " + this.mIndicatorWidth);
        }
        updateIndicatorRect();
    }


    public void lambda$ensureSpringAnimation$2(COUIDynamicAnimation cOUIDynamicAnimation, float value, float value_2) {
        this.mIndicatorCenterX = value;
        updateIndicatorRect();
    }

    private void loadAttr(Context context, AttributeSet attributeSet, int index, int index_2) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUISegmentButtonLayout, index, index_2);
        this.mUnselectedTextColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUISegmentButtonLayout_UnSelectedTextColor, COUIContextUtil.getAttrColor(context, R.attr.couiColorLabelPrimary));
        this.mSelectedTextColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUISegmentButtonLayout_selectedTextColor, COUIContextUtil.getAttrColor(context, R.attr.couiColorLabelPrimary));
        this.mCustomTextSizePx = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUISegmentButtonLayout_segmentButtonTextSize, 0);
        this.mEnableTextScale = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUISegmentButtonLayout_enableTextScale, true);
        this.mIsResponsiveWidthEnabled = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUISegmentButtonLayout_enableResponsiveWidth, true);
        typedArrayObtainStyledAttributes.recycle();
        this.mIndicatorColor = COUIContextUtil.getColor(context, R.color.coui_color_segment_button_indicator);
        this.mBackgroundColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorSegmentButtonBackground, COUIContextUtil.getColor(context, R.color.coui_color_segment_button_background));
        this.mShadowColor = COUIContextUtil.getColor(context, R.color.coui_segment_button_shadow_color);
        this.mShadowGradientEndColor = COUIContextUtil.getColor(context, R.color.coui_segment_button_shadow_end_color);
        this.mIsNightMode = COUIDarkModeUtil.isNightMode(context);
    }

    private void performHapticFeedback() {
        if (this.mIsNeedVibration) {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
        }
    }

    private void refreshSegmentTextConfig(TextView textView, int index) {
        if (textView == null || index < 0 || index > this.mButtons.size()) {
            return;
        }
        TextViewCompat.setTextAppearance(textView, R.style.couiTextButtonM);
        if (this.mCustomTextSizePx > 0.0f) {
            textView.getPaint().setTextSize(this.mCustomTextSizePx);
        }
        if (this.mEnableTextScale) {
            COUIChangeTextUtil.adaptFontSize(textView, 4);
        }
        if (index == this.mSelectedPosition) {
            textView.setTextColor(this.mSelectedTextColor);
        } else {
            textView.setTextColor(this.mUnselectedTextColor);
        }
    }

    private void refreshTextPaint() {
        if (this.mCustomTextSizePx > 0.0f) {
            this.mTextView.getPaint().setTextSize(this.mCustomTextSizePx);
        }
        if (this.mEnableTextScale) {
            COUIChangeTextUtil.adaptFontSize(this.mTextView, 4);
        }
        this.mTextPaint = this.mTextView.getPaint();
    }

    private void resetAnimation(COUISpringAnimation cOUISpringAnimation) {
        if (cOUISpringAnimation == null || !cOUISpringAnimation.isRunning()) {
            return;
        }
        cOUISpringAnimation.skipToEnd();
    }

    private void resetIndicatorRect() {
        if (this.mButtons.isEmpty()) {
            return;
        }
        COUISpringAnimation cOUISpringAnimation = this.mSpringAnimationPos;
        if (cOUISpringAnimation != null && cOUISpringAnimation.isRunning()) {
            this.mSpringAnimationPos.cancel();
        }
        COUISpringAnimation cOUISpringAnimation2 = this.mSpringAnimationRubber;
        if (cOUISpringAnimation2 != null && cOUISpringAnimation2.isRunning()) {
            this.mSpringAnimationRubber.cancel();
        }
        COUISpringAnimation cOUISpringAnimation3 = this.mSpringAnimationWidth;
        if (cOUISpringAnimation3 != null && cOUISpringAnimation3.isRunning()) {
            this.mSpringAnimationWidth.cancel();
        }
        this.mIndicatorRectF.right = this.mButtons.get(this.mSelectedPosition).getRight();
        this.mIndicatorRectF.left = this.mButtons.get(this.mSelectedPosition).getLeft();
        this.mIndicatorRectF.top = getPaddingTop();
        this.mIndicatorRectF.bottom = getHeight() - getPaddingBottom();
        this.mIndicatorHeight = this.mIndicatorRectF.height();
        this.mIndicatorCenterY = this.mIndicatorRectF.centerY();
        this.mIndicatorCenterX = this.mIndicatorRectF.centerX();
        this.mIndicatorWidth = this.mIndicatorRectF.width();
    }

    private void resetRubberBandEffect(int index) {
        if (getSegmentAt(index) != null) {
            this.mSpringAnimationRubber.setStartValue(this.mIndicatorCenterX);
            this.mSpringAnimationRubber.animateToFinalPosition((getSegmentAt(index).getLeft() + getSegmentAt(index).getRight()) / 2.0f);
        } else {
            COUILog.w(TAG, index + " out of range of segment button layout");
        }
    }

    private void resetTouchEffect() {
        ensureSpringAnimation();
        int index = this.mLastTouchPosition;
        if (index < 0) {
            index = this.mSelectedPosition;
        }
        executeScaleAnimatorAt(false, index);
        if (this.mIsDragging) {
            this.mIndicatorScaleHelper.executeFeedbackAnimator(false);
            if (this.mSpringAnimationPos.isRunning()) {
                return;
            }
            resetRubberBandEffect(this.mSelectedPosition);
        }
    }

    private void setSelectedAt(int index, boolean flag) {
        if (index < 0 || index >= this.mButtons.size()) {
            return;
        }
        TextView textView = this.mButtons.get(index);
        textView.setSelected(flag);
        textView.setTextColor(flag ? this.mSelectedTextColor : this.mUnselectedTextColor);
    }


    public void updateIndicatorRect() {
        RectF rectF = this.mIndicatorRectF;
        float value = this.mIndicatorCenterX;
        float value_2 = this.mIndicatorScale;
        float value_3 = this.mIndicatorWidth;
        rectF.left = value - ((value_2 * value_3) / 2.0f);
        rectF.right = value + ((value_2 * value_3) / 2.0f);
        float value_4 = this.mIndicatorCenterY;
        float value_5 = this.mIndicatorHeight;
        rectF.top = value_4 - ((value_2 * value_5) / 2.0f);
        rectF.bottom = value_4 + ((value_5 * value_2) / 2.0f);
        this.mOverDistance = (int) (((1.0f - value_2) * value_3) / 2.0f);
        int index = this.mSelectedPosition;
        if (index == 0 || index == this.mButtons.size() - 1) {
            this.mOverDistance += getPaddingStart();
        } else {
            this.mOverDistance += this.mSwitchDistance;
        }
        invalidate();
    }

    private void updateShadowGradientColors() {
        int[] iArr = this.mShadowGradientColors;
        iArr[0] = this.mShadowColor;
        iArr[1] = this.mShadowGradientEndColor;
    }

    @Override
    public void addView(View view, int index, ViewGroup.LayoutParams layoutParams) {
        if (!(view instanceof TextView)) {
            COUILog.e(TAG, "Child views must be of type TextView.");
            return;
        }
        TextView textView = (TextView) view;
        initializeSegmentButton(textView, this.mButtons.size(), textView.getText().toString());
        this.mButtons.add(textView);
        super.addView(textView, -1, new ViewGroup.LayoutParams(-2, -1));
        this.mIsSizeChanged = true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int segmentIndex;
        if (!isEnabled()) {
            return false;
        }
        int keyCode = keyEvent.getKeyCode();
        if ((keyCode == 23 || keyCode == 66) && keyEvent.getAction() == 1 && (getFocusedChild() instanceof TextView) && (segmentIndex = getSegmentIndex((TextView) getFocusedChild())) != this.mSelectedPosition) {
            selectSegmentAt(segmentIndex);
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        this.mLastTouchPosition = this.mTouchPosition;
        this.mTouchPosition = getSelectedIndexFromX(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            ensureSpringAnimation();
            executeScaleAnimatorAt(true, this.mTouchPosition);
            if (!this.mIsDragging) {
                boolean isDragging = this.mSelectedPosition == this.mTouchPosition;
                this.mIsDragging = isDragging;
                if (isDragging) {
                    this.mLastX = motionEvent.getX();
                    this.mIndicatorCenterLastX = this.mIndicatorCenterX;
                    this.mIndicatorScaleHelper.executeFeedbackAnimator(true);
                }
            }
        } else if (actionMasked == 1) {
            resetTouchEffect();
            this.mIsDragging = false;
            int index = this.mTouchPosition;
            if (index != this.mSelectedPosition && index >= 0) {
                selectSegmentAt(index);
            }
        } else if (actionMasked != 2) {
            if (actionMasked == 3) {
                handleActionCancel();
            }
        } else if (this.mIsDragging) {
            if (Math.abs(motionEvent.getX() - this.mLastX) > this.mTouchSlop) {
                attemptClaimDrag();
            }
            int index_2 = this.mTouchPosition;
            if (index_2 != this.mSelectedPosition && index_2 >= 0) {
                selectSegmentAt(index_2);
                performHapticFeedback();
                executeScaleAnimatorAt(false, this.mLastSelectedPosition);
                executeScaleAnimatorAt(true, this.mSelectedPosition);
            } else if (!this.mSpringAnimationPos.isRunning()) {
                handleRubberBandEffect(motionEvent.getX());
            }
        } else {
            int index_3 = this.mTouchPosition;
            int index_4 = this.mLastTouchPosition;
            if (index_3 != index_4) {
                if (index_4 < 0) {
                    index_4 = this.mLastSelectedPosition;
                }
                executeScaleAnimatorAt(false, index_4);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public TextView getSegmentAt(int index) {
        if (index < 0 || index >= getSegmentCount()) {
            return null;
        }
        return this.mButtons.get(index);
    }

    public int getSegmentCount() {
        return this.mButtons.size();
    }

    public int getSegmentIndex(View view) {
        return this.mButtons.indexOf(view);
    }

    public int getSelectedSegmentPosition() {
        return this.mSelectedPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint[] customBackgroundPaint;
        Paint[] customIndicatorPaint;
        int iSave = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        SegmentButtonDrawDelegate segmentButtonDrawDelegate = this.mDrawDelegate;
        if (segmentButtonDrawDelegate != null) {
            customBackgroundPaint = segmentButtonDrawDelegate.getCustomBackgroundPaint();
            customIndicatorPaint = this.mDrawDelegate.getCustomIndicatorPaint();
        } else {
            customBackgroundPaint = null;
            customIndicatorPaint = null;
        }
        if (customBackgroundPaint == null || customBackgroundPaint.length == 0) {
            this.mContainerPaint.setColor(this.mBackgroundColor);
            drawButtonBackground(canvas, this.mContainerPaint);
        } else {
            for (Paint paint : customBackgroundPaint) {
                if (paint != null) {
                    drawButtonBackground(canvas, paint);
                }
            }
        }
        if (customIndicatorPaint == null || customIndicatorPaint.length == 0) {
            drawIndicator(canvas, Boolean.TRUE, this.mIndicatorPaint);
            this.mIndicatorPaint.setColor(this.mIndicatorColor);
            drawIndicator(canvas, Boolean.FALSE, this.mIndicatorPaint);
        } else {
            for (Paint paint_2 : customIndicatorPaint) {
                if (paint_2 != null) {
                    drawIndicator(canvas, Boolean.FALSE, paint_2);
                }
            }
        }
        canvas.restoreToCount(iSave);
        super.onDraw(canvas);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        AccessibilityNodeInfoCompat.wrap(accessibilityNodeInfo).setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(1, getSegmentCount(), false, 0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right_2, int bottom_2) {
        super.onLayout(changed, left, top, right_2, bottom_2);
        this.mTmpRect.right = getWidth();
        this.mTmpRect.bottom = getHeight();
        this.mTmpRectF.set(this.mTmpRect);
        if (this.mIsSizeChanged) {
            resetIndicatorRect();
            this.mIsSizeChanged = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        this.mChildWidths.clear();
        int index_3 = 0;
        if (size > 0) {
            int index_4 = 0;
            for (int index = 0; index < getChildCount(); index++) {
                View childAt = getChildAt(index);
                if (childAt instanceof TextView) {
                    ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                    int segmentTextWidth = getSegmentTextWidth((TextView) childAt);
                    layoutParams.width = segmentTextWidth;
                    this.mChildWidths.add(Integer.valueOf(segmentTextWidth));
                    index_4 += layoutParams.width;
                }
            }
            int paddingStart = index_4 + getPaddingStart() + getPaddingEnd();
            if (mode == 1073741824) {
                if (this.mIsResponsiveWidthEnabled) {
                    size = Math.min(getResponsiveWidth(), size);
                }
                calculateChildrenWidths((size - getPaddingStart()) - getPaddingEnd());
                while (index_3 < getChildCount()) {
                    getChildAt(index_3).getLayoutParams().width = this.mChildWidths.get(index_3).intValue();
                    index_3++;
                }
            } else if (paddingStart > size) {
                calculateChildrenWidths((size - getPaddingStart()) - getPaddingEnd());
                while (index_3 < getChildCount()) {
                    getChildAt(index_3).getLayoutParams().width = this.mChildWidths.get(index_3).intValue();
                    index_3++;
                }
            } else {
                size = paddingStart;
            }
        } else {
            for (int index_2 = 0; index_2 < getChildCount(); index_2++) {
                getChildAt(index_2).getLayoutParams().width = 0;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(size, View.MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position < 0 || position >= this.mButtons.size() || position == this.mSelectedPosition) {
            return;
        }
        selectSegmentAt(position);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mIsSizeChanged = true;
    }

    @Override
    public void onViewRemoved(View view) {
        if (!(view instanceof TextView)) {
            COUILog.e(TAG, "Child views must be of type TextView.");
            return;
        }
        this.mSelectedPosition = 0;
        this.mLastSelectedPosition = -1;
        int segmentIndex = getSegmentIndex((TextView) view);
        if (segmentIndex == -1) {
            COUILog.e(TAG, "Child view is not a direct child of this ViewGroup: " + view + "\nParent: " + this + "\nMake sure the view was properly added with addView()");
            return;
        }
        this.mButtons.remove(segmentIndex);
        this.mPressScaleHelper.remove(segmentIndex);
        this.mTextColorChangeHelper.remove(segmentIndex);
        resetAnimation(this.mSpringAnimationPos);
        resetAnimation(this.mSpringAnimationWidth);
        resetAnimation(this.mSpringAnimationRubber);
        this.mSpringAnimationPos = null;
        this.mSpringAnimationWidth = null;
        this.mSpringAnimationRubber = null;
        this.mIndicatorScaleHelper = null;
    }

    public void refresh() {
        int index_2;
        int index_3;
        String resourceTypeName = getContext().getResources().getResourceTypeName(this.mStyleAttr);
        if (TextUtils.equals(resourceTypeName, "attr")) {
            index_2 = this.mStyleAttr;
            index_3 = 0;
        } else if (TextUtils.equals(resourceTypeName, "style")) {
            index_3 = this.mStyleAttr;
            index_2 = 0;
        } else {
            index_2 = 0;
            index_3 = 0;
        }
        loadAttr(getContext(), null, index_2, index_3);
        updateShadowGradientColors();
        this.mIsNightMode = COUIDarkModeUtil.isNightMode(getContext());
        for (int index = 0; index < this.mButtons.size(); index++) {
            refreshSegmentTextConfig(this.mButtons.get(index), index);
        }
    }

    public void selectSegmentAt(int index) {
        int lastSelectedPosition;
        if (index < 0 || index >= this.mButtons.size() || index == (lastSelectedPosition = this.mSelectedPosition)) {
            return;
        }
        this.mLastSelectedPosition = lastSelectedPosition;
        this.mSelectedPosition = index;
        setSelectedAt(index, true);
        setSelectedAt(this.mLastSelectedPosition, false);
        if (this.mSelectedTextColor != this.mUnselectedTextColor) {
            executeTextColorChangeAnimatorAt(true, this.mSelectedPosition);
            executeTextColorChangeAnimatorAt(false, this.mLastSelectedPosition);
        }
        animateIndicatorToPosition(this.mSelectedPosition);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled && !enabled) {
            handleActionCancel();
        }
        super.setEnabled(enabled);
    }

    public void setIndicatorResponse(float indicatorResponse) {
        this.mResponse = indicatorResponse;
        this.mSpringAnimationRubber = null;
        this.mSpringAnimationPos = null;
        this.mIndicatorScaleHelper = null;
        this.mSpringAnimationWidth = null;
    }

    public void setOnSelectedSegmentChangeListener(OnSelectedSegmentChangeListener onSelectedSegmentChangeListener) {
        this.mOnSelectedSegmentChangeListener = onSelectedSegmentChangeListener;
    }

    public void setResponsiveWidthEnabled(boolean responsiveWidthEnabled) {
        this.mIsResponsiveWidthEnabled = responsiveWidthEnabled;
        requestLayout();
    }

    public void setSegmentButtonDrawDelegate(SegmentButtonDrawDelegate segmentButtonDrawDelegate) {
        this.mDrawDelegate = segmentButtonDrawDelegate;
    }

    public void setSegmentButtons(String[] strArr) {
        if (Arrays.equals(this.mSegmentStrings, strArr)) {
            return;
        }
        this.mSegmentStrings = strArr;
        removeAllViews();
        for (int index = 0; index < this.mSegmentStrings.length; index++) {
            TextView textView = new TextView(getContext());
            textView.setText(strArr[index]);
            addView(textView, new ViewGroup.LayoutParams(-2, -1));
        }
    }

    public void setSegmentSelectedTextColor(int segmentSelectedTextColor) {
        if (this.mButtons.isEmpty()) {
            COUILog.e(TAG, "Cannot set text color: COUISegmentButtonLayout has no children.");
        } else {
            this.mSelectedTextColor = segmentSelectedTextColor;
            this.mButtons.get(this.mSelectedPosition).setTextColor(this.mSelectedTextColor);
        }
    }

    public void setSegmentTextAt(String str, int segmentTextAt) {
        TextView segmentAt = getSegmentAt(segmentTextAt);
        if (segmentAt != null) {
            segmentAt.setText(str);
            this.mIsSizeChanged = true;
            requestLayout();
        } else {
            COUILog.w(TAG, "Segment at index " + segmentTextAt + "does not exist");
        }
    }

    public void setSegmentTextSize(float segmentTextSize) {
        setSegmentTextSize(2, segmentTextSize);
    }

    public void setSegmentUnselectedTextColor(int segmentUnselectedTextColor) {
        this.mUnselectedTextColor = segmentUnselectedTextColor;
        for (int index = 0; index < this.mButtons.size(); index++) {
            if (index != this.mSelectedPosition) {
                this.mButtons.get(index).setTextColor(this.mUnselectedTextColor);
            }
        }
    }

    public void setTextScaleEnabled(boolean textScaleEnabled) {
        this.mEnableTextScale = textScaleEnabled;
        requestLayout();
    }

    public COUISegmentButtonLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiSegmentButtonStyle);
    }

    public void setSegmentTextSize(int width, float height) {
        if (height == this.mTextPaint.getTextSize()) {
            return;
        }
        this.mCustomTextSizePx = TypedValue.applyDimension(width, height, getResources().getDisplayMetrics());
        for (int index = 0; index < this.mButtons.size(); index++) {
            this.mButtons.get(index).setTextSize(width, height);
            if (this.mEnableTextScale) {
                COUIChangeTextUtil.adaptFontSize(this.mButtons.get(index), 4);
            }
        }
        refreshTextPaint();
        this.mIsSizeChanged = true;
    }

    public COUISegmentButtonLayout(Context context, AttributeSet attributeSet, int index) {
        this(context, attributeSet, index, R.style.SegmentButton);
    }

    public COUISegmentButtonLayout(Context context, AttributeSet attributeSet, int index, int index_2) {
        super(context, attributeSet, index, index_2);
        this.mContainerPaint = new Paint(1);
        this.mIndicatorPaint = new Paint(1);
        this.mShadowGradientPaint = new Paint(1);
        this.mShadowGradientColors = new int[2];
        this.mShadowGradientStops = new float[]{0.0f, 1.0f};
        this.mTmpRect = new Rect();
        this.mTmpRectF = new RectF();
        this.mIndicatorRectF = new RectF();
        this.mShadowRectF = new RectF();
        this.mBackgroundPath = new Path();
        this.mResponsiveUIModel = new ResponsiveUIModel(getContext(), 0, 0);
        this.mPressScaleHelper = new ArrayList<>();
        this.mTextColorChangeHelper = new ArrayList<>();
        this.mButtons = new ArrayList<>();
        this.mChildWidths = new ArrayList<>();
        this.mTextView = new TextView(getContext());
        this.mTextPaint = null;
        this.mOnSelectedSegmentChangeListener = null;
        this.mDrawDelegate = null;
        this.mSmoothRoundCornerHelper = null;
        this.mIndicatorScale = 1.0f;
        this.mResponse = DEFAULT_SPRING_RESPONSE;
        this.mIsDragging = false;
        this.mEnableRubberEffect = false;
        this.mIsNeedVibration = true;
        this.mIsSizeChanged = false;
        this.mIsResponsiveWidthEnabled = true;
        this.mTouchPosition = -1;
        this.mLastTouchPosition = -1;
        this.mSelectedPosition = 0;
        this.mLastSelectedPosition = -1;
        this.mAttrs = null;
        this.mStyleAttr = 0;
        this.mStyleRes = 0;
        this.mTouchSlop = 0;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        this.mAttrs = attributeSet;
        if (attributeSet != null) {
            this.mStyleAttr = attributeSet.getStyleAttribute();
        }
        if (this.mStyleAttr == 0) {
            this.mStyleAttr = index;
        }
        this.mStyleRes = index_2;
        loadAttr(context, attributeSet, index, index_2);
        this.mSegmentMinWidth = getResources().getDimensionPixelOffset(R.dimen.coui_segment_min_width);
        this.mOverStiffness = DEFAULT_OVER_STIFFNESS;
        this.mOverDistance = getResources().getDimensionPixelOffset(R.dimen.coui_segment_over_distance);
        this.mSwitchDistance = getResources().getDimensionPixelOffset(R.dimen.coui_segment_switch_distance);
        this.mSegmentPaddingHorizontal = getResources().getDimensionPixelOffset(R.dimen.coui_segment_btn_padding_horizontal);
        this.mSegmentPaddingRegular = getResources().getDimensionPixelOffset(R.dimen.coui_segment_padding);
        this.mSegmentPaddingSmall = getResources().getDimensionPixelOffset(R.dimen.coui_segment_padding_tiny);
        this.mSegmentExtraWidth = getResources().getDimensionPixelOffset(R.dimen.coui_segment_regular_extra_width);
        this.mSegmentExtraWidthSmall = getResources().getDimensionPixelOffset(R.dimen.coui_segment_small_extra_width);
        this.mIndicatorShadowOffset = getResources().getDimensionPixelOffset(R.dimen.coui_segment_shadow_offset);
        this.mShadowRadius = getResources().getDimensionPixelOffset(R.dimen.coui_segment_shadow_radius);
        initPaint();
        setWillNotDraw(false);
        setGravity(16);
        setOrientation(0);
        setBackgroundColor(0);
        setClipChildren(false);
        setClipToPadding(false);
        setImportantForAccessibility(1);
        this.mSmoothRoundCornerHelper = new SmoothRoundCornerHelper();
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void selectSegmentAt(View view) {
        selectSegmentAt(getSegmentIndex(view));
    }
}
// Leapy end
