package com.coui.appcompat.progressbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.animation.COUIOutEaseInterpolator;

public class COUICircularProgressDrawable extends Drawable {
    private static final int ACCURACY = 100;
    private static final float DEFAULT_ALPHA_START_FRACTION = 0.7f;
    private static final long DEFAULT_DELAY = 200L;
    private static final long DEFAULT_ERROR_DURATION = 350L;
    private static final long DEFAULT_ICON_PAUSE_DURATION = 300L;
    private static final long DEFAULT_ICON_RESUME_DURATION = 200L;
    private static final float DEFAULT_MIN_PROGRESS_VALUE = 1.0E-4f;
    private static final long DEFAULT_PROGRESS_PAUSE_DURATION = 200L;
    private static final long DEFAULT_PROGRESS_RESUME_DURATION = 200L;
    private static final int FULL_ALPHA = 255;
    private static final int FULL_DEGREE = 360;
    private static final int ORIGINAL_ANGLE = -90;
    private static final String TAG = "COUICircularDrawable";

    private static final Interpolator DEFAULT_LINEAR_INTERPOLATOR = new COUILinearInterpolator();
    private static final Interpolator DEFAULT_IN_EASE_INTERPOLATOR = new COUIInEaseInterpolator();
    private static final Interpolator DEFAULT_OUT_EASE_INTERPOLATOR = new COUIOutEaseInterpolator();
    private static final Interpolator DEFAULT_MOVE_EASE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final ArgbEvaluator COLOR_EVALUATOR = new ArgbEvaluator();

    private static final FloatPropertyCompat<COUICircularProgressDrawable> VISUAL_PROGRESS =
            new FloatPropertyCompat<COUICircularProgressDrawable>("visualProgress") {
                @Override
                public float getValue(COUICircularProgressDrawable object) {
                    return object.getVisualProgress();
                }

                @Override
                public void setValue(COUICircularProgressDrawable object, float value) {
                    object.setVisualProgress(value);
                    object.notifyVisualProgressChanged();
                }
            };

    private int mActualProgress;
    private float mCenterX;
    private float mCenterY;
    private AnimatorSet mErrorAnimatorSet;
    private float mErrorIconCircleBias;
    private float mErrorIconCircleRadius;
    private int mErrorIconColor;
    private float mErrorIconRectBias;
    private float mErrorIconRectHeight;
    private float mErrorIconRectWidth;
    private int mGlobalAlpha = FULL_ALPHA;
    private View mHostView;
    private ValueAnimator mIconErrorAnimator;
    private float mIconErrorScale;
    private Paint mIconPaint;
    private ValueAnimator mIconPauseAnimator;
    private float mIconPauseScale;
    private ValueAnimator mIconRecoverAnimator;
    private ValueAnimator mIconResumeAnimator;
    private int mCurrentErrorIconAlpha;
    private int mCurrentPauseIconAlpha;
    private int mMax = 100;
    private OnProgressChangedListener mOnProgressChangedListener;
    private OnProgressStateAnimatorListener mOnProgressStateAnimatorListener;
    private AnimatorSet mPauseAnimatorSet;
    private int mPauseIconColor;
    private float mPauseIconRectGap;
    private float mPauseIconRectHeight;
    private float mPauseIconRectRadius;
    private float mPauseIconRectWidth;
    private int mProgressBarAlpha = FULL_ALPHA;
    private float mProgressBarOuterDiameter;
    private float mProgressBarStrokeWidth;
    private ValueAnimator mProgressEnlargeAnimator;
    private Paint mProgressPaint;
    private ValueAnimator mProgressPauseAnimator;
    private ProgressBarStyleProperty mProgressProperty;
    private ValueAnimator mProgressResumeAnimator;
    private ValueAnimator mProgressShrinkAnimator;
    private AnimatorSet mRecoverAnimatorSet;
    private AnimatorSet mResumeAnimatorSet;
    private int mShadowColor;
    private float mShadowRadius;
    private float mShadowXBias;
    private float mShadowYBias;
    private SpringAnimation mSpringAnimation;
    private Paint mTrackPaint;
    private ProgressBarStyleProperty mTrackProperty;
    private float mVisualProgress;
    private boolean mAnimating;

    public interface OnProgressChangedListener {
        default void onProgressChanged(int progress) {
        }

        default void onVisualProgressChanged(float visualProgress) {
        }
    }

    public interface OnProgressStateAnimatorListener {
        default void onPauseAnimationStart() {
        }

        default void onPauseAnimationEnd() {
        }

        default void onResumeAnimationStart() {
        }

        default void onResumeAnimationEnd() {
        }

        default void onErrorAnimationStart() {
        }

        default void onErrorAnimationEnd() {
        }

        default void onRecoverAnimationStart() {
        }

        default void onRecoverAnimationEnd() {
        }
    }

    public static class ProgressBarStyleProperty {
        private int mCurrentBarColor;
        private float mProgressBarCurrentOuterDiameter;
        private float mProgressBarCurrentStrokeWidth;
        private int mProgressBarErrorColor;
        private float mProgressBarErrorOuterDiameter = Float.MIN_VALUE;
        private float mProgressBarErrorStrokeWidth = Float.MIN_VALUE;
        private float mProgressBarCenterX;
        private float mProgressBarCenterY;
        private float mProgressBarStrokeWidth;
        private float mProgressBarOuterDiameter;
        private int mProgressBarColor;

        public int getCurrentBarColor() {
            return mCurrentBarColor;
        }

        public float getProgressBarCenterX() {
            return mProgressBarCenterX;
        }

        public float getProgressBarCenterY() {
            return mProgressBarCenterY;
        }

        public int getProgressBarColor() {
            return mProgressBarColor;
        }

        public float getProgressBarCurrentOuterDiameter() {
            return mProgressBarCurrentOuterDiameter;
        }

        public float getProgressBarCurrentStrokeWidth() {
            return mProgressBarCurrentStrokeWidth;
        }

        public int getProgressBarErrorColor() {
            return mProgressBarErrorColor;
        }

        public float getProgressBarErrorOuterDiameter() {
            return mProgressBarErrorOuterDiameter == Float.MIN_VALUE
                    ? mProgressBarOuterDiameter : mProgressBarErrorOuterDiameter;
        }

        public float getProgressBarErrorStrokeWidth() {
            return mProgressBarErrorStrokeWidth == Float.MIN_VALUE
                    ? mProgressBarStrokeWidth : mProgressBarErrorStrokeWidth;
        }

        public float getProgressBarOuterDiameter() {
            return mProgressBarOuterDiameter;
        }

        public float getProgressBarStrokeWidth() {
            return mProgressBarStrokeWidth;
        }

        public void setCurrentBarColor(int currentBarColor) {
            mCurrentBarColor = currentBarColor;
        }

        public void setProgressBarCenterX(float centerX) {
            mProgressBarCenterX = centerX;
        }

        public void setProgressBarCenterY(float centerY) {
            mProgressBarCenterY = centerY;
        }

        public void setProgressBarColor(int color) {
            mProgressBarColor = color;
            mCurrentBarColor = color;
        }

        public void setProgressBarCurrentOuterDiameter(float diameter) {
            if (diameter < 0f) {
                Log.w(TAG, "Progress bar outer diameter should be greater than 0 !");
            }
            mProgressBarCurrentOuterDiameter = Math.max(0f, diameter);
        }

        public void setProgressBarCurrentStrokeWidth(float strokeWidth) {
            if (strokeWidth < 0f) {
                Log.w(TAG, "Progress bar stroke width should be greater than 0 !");
            }
            mProgressBarCurrentStrokeWidth = Math.max(0f, strokeWidth);
        }

        public void setProgressBarErrorColor(int errorColor) {
            mProgressBarErrorColor = errorColor;
        }

        public void setProgressBarErrorOuterDiameter(float diameter) {
            if (diameter < 0f) {
                Log.w(TAG, "Progress bar outer diameter should be greater than 0 !");
            }
            mProgressBarErrorOuterDiameter = Math.max(0f, diameter);
        }

        public void setProgressBarErrorStrokeWidth(float strokeWidth) {
            if (strokeWidth < 0f) {
                Log.w(TAG, "Progress bar stroke width should be greater than 0 !");
            }
            mProgressBarErrorStrokeWidth = Math.max(0f, strokeWidth);
        }

        public void setProgressBarOuterDiameter(float diameter) {
            if (diameter < 0f) {
                Log.w(TAG, "Progress bar outer diameter should be greater than 0 !");
            }
            mProgressBarOuterDiameter = Math.max(0f, diameter);
        }

        public void setProgressBarStrokeWidth(float strokeWidth) {
            if (strokeWidth < 0f) {
                Log.w(TAG, "Progress bar stroke width should be greater than 0 !");
            }
            mProgressBarStrokeWidth = Math.max(0f, strokeWidth);
        }
    }

    public COUICircularProgressDrawable(Context context) {
        initAttr(context);
        initPaint();
        initAnimator();
    }

    private float actual2VisualProgress(float actualProgress) {
        if (mMax <= 0) {
            return 0f;
        }
        return (((int) ((actualProgress * ACCURACY) / mMax)) / (float) ACCURACY) * mMax;
    }

    private void drawErrorIcon(Canvas canvas) {
        if (mCurrentErrorIconAlpha != 0) {
            canvas.saveLayerAlpha(0f, 0f, mCenterX * 2f, mCenterY * 2f, mCurrentErrorIconAlpha);
            canvas.scale(mIconErrorScale, mIconErrorScale, mCenterX, mCenterY);
            mIconPaint.setColor(mErrorIconColor);
            canvas.drawRect(mCenterX - (mErrorIconRectWidth / 2f), mCenterY - mErrorIconRectBias,
                    mCenterX + (mErrorIconRectWidth / 2f),
                    (mCenterY - mErrorIconRectBias) + mErrorIconRectHeight, mIconPaint);
            canvas.drawCircle(mCenterX, mCenterY + mErrorIconCircleBias, mErrorIconCircleRadius,
                    mIconPaint);
            canvas.restore();
        }
    }

    private void drawPauseIcon(Canvas canvas) {
        if (mCurrentPauseIconAlpha != 0) {
            canvas.saveLayerAlpha(0f, 0f, mCenterX * 2f, mCenterY * 2f, mCurrentPauseIconAlpha);
            canvas.scale(mIconPauseScale, mIconPauseScale, mCenterX, mCenterY);
            mIconPaint.setColor(mPauseIconColor);
            canvas.drawRoundRect(mCenterX - mPauseIconRectWidth - (mPauseIconRectGap / 2f),
                    mCenterY - (mPauseIconRectHeight / 2f),
                    mCenterX - (mPauseIconRectGap / 2f),
                    mCenterY + (mPauseIconRectHeight / 2f),
                    mPauseIconRectRadius, mPauseIconRectRadius, mIconPaint);
            canvas.drawRoundRect(mCenterX + (mPauseIconRectGap / 2f),
                    mCenterY - (mPauseIconRectHeight / 2f),
                    mCenterX + mPauseIconRectWidth + (mPauseIconRectGap / 2f),
                    mCenterY + (mPauseIconRectHeight / 2f),
                    mPauseIconRectRadius, mPauseIconRectRadius, mIconPaint);
            canvas.restore();
        }
    }

    private void drawProgress(Canvas canvas) {
        float trackRadius = (mTrackProperty.getProgressBarCurrentOuterDiameter()
                - mTrackProperty.getProgressBarCurrentStrokeWidth()) / 2f;
        float progressRadius = (mProgressProperty.getProgressBarCurrentOuterDiameter()
                - mProgressProperty.getProgressBarCurrentStrokeWidth()) / 2f;
        if (mProgressBarAlpha != FULL_ALPHA) {
            canvas.saveLayerAlpha(0f, 0f, mCenterX * 2f, mCenterY * 2f, mProgressBarAlpha);
        } else {
            canvas.save();
        }
        canvas.drawCircle(mCenterX, mCenterY, trackRadius, mTrackPaint);
        canvas.rotate(ORIGINAL_ANGLE, mCenterX, mCenterY);
        canvas.drawArc(mProgressProperty.getProgressBarCenterX() - progressRadius,
                mProgressProperty.getProgressBarCenterY() - progressRadius,
                mProgressProperty.getProgressBarCenterX() + progressRadius,
                mProgressProperty.getProgressBarCenterY() + progressRadius,
                0f, Math.max(DEFAULT_MIN_PROGRESS_VALUE,
                        (mVisualProgress * FULL_DEGREE) / Math.max(1, mMax)),
                false, mProgressPaint);
        canvas.restore();
    }

    private void initAnimator() {
        initSpring();
        initPauseAnimator();
        initResumeAnimator();
        initErrorAnimator();
        initRecoverAnimator();
    }

    private void initAttr(Context context) {
        mPauseIconRectWidth = context.getResources().getDimension(R.dimen.coui_circular_progress_pause_icon_rect_width);
        mPauseIconRectHeight = context.getResources().getDimension(R.dimen.coui_circular_progress_pause_icon_rect_height);
        mPauseIconRectRadius = context.getResources().getDimension(R.dimen.coui_circular_progress_pause_icon_rect_radius);
        mPauseIconRectGap = context.getResources().getDimension(R.dimen.coui_circular_progress_pause_icon_rect_gap);
        mErrorIconRectWidth = context.getResources().getDimension(R.dimen.coui_circular_progress_error_icon_rect_width);
        mErrorIconRectHeight = context.getResources().getDimension(R.dimen.coui_circular_progress_error_icon_rect_height);
        mErrorIconRectBias = context.getResources().getDimension(R.dimen.coui_circular_progress_error_icon_rect_bias);
        mErrorIconCircleRadius = context.getResources().getDimension(R.dimen.coui_circular_progress_error_icon_circle_radius);
        mErrorIconCircleBias = context.getResources().getDimension(R.dimen.coui_circular_progress_error_icon_circle_bias);
        mShadowRadius = context.getResources().getDimension(R.dimen.coui_circular_progress_shadow_radius);
        mShadowXBias = context.getResources().getDimension(R.dimen.coui_circular_progress_shadow_x_bias);
        mShadowYBias = context.getResources().getDimension(R.dimen.coui_circular_progress_shadow_y_bias);
        mShadowColor = context.getColor(R.color.coui_circular_progress_shadow_color);
    }

    private void initErrorAnimator() {
        mProgressShrinkAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressShrinkAnimator.setDuration(DEFAULT_ERROR_DURATION);
        mProgressShrinkAnimator.setInterpolator(DEFAULT_MOVE_EASE_INTERPOLATOR);
        mProgressShrinkAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            float reverse = 1f - fraction;
            float progressOuter = mProgressProperty.getProgressBarErrorOuterDiameter()
                    + ((mProgressProperty.getProgressBarOuterDiameter()
                    - mProgressProperty.getProgressBarErrorOuterDiameter()) * reverse);
            float trackOuter = mTrackProperty.getProgressBarErrorOuterDiameter()
                    + ((mTrackProperty.getProgressBarOuterDiameter()
                    - mTrackProperty.getProgressBarErrorOuterDiameter()) * reverse);
            float progressStroke = mProgressProperty.getProgressBarErrorStrokeWidth()
                    + ((mProgressProperty.getProgressBarStrokeWidth()
                    - mProgressProperty.getProgressBarErrorStrokeWidth()) * reverse);
            float trackStroke = mTrackProperty.getProgressBarErrorStrokeWidth()
                    + ((mTrackProperty.getProgressBarStrokeWidth()
                    - mTrackProperty.getProgressBarErrorStrokeWidth()) * reverse);
            int trackColor = (Integer) COLOR_EVALUATOR.evaluate(fraction,
                    mTrackProperty.getProgressBarColor(), mTrackProperty.getProgressBarErrorColor());
            int progressColor = (Integer) COLOR_EVALUATOR.evaluate(fraction,
                    mProgressProperty.getProgressBarColor(), mProgressProperty.getProgressBarErrorColor());
            mProgressProperty.setProgressBarCurrentOuterDiameter(progressOuter);
            mProgressProperty.setProgressBarCurrentStrokeWidth(progressStroke);
            mProgressProperty.setCurrentBarColor(progressColor);
            mTrackProperty.setProgressBarCurrentOuterDiameter(trackOuter);
            mTrackProperty.setProgressBarCurrentStrokeWidth(trackStroke);
            mTrackProperty.setCurrentBarColor(trackColor);
            invalidateSelf();
        });
        mIconErrorAnimator = ValueAnimator.ofFloat(0f, 1f);
        mIconErrorAnimator.setDuration(DEFAULT_ERROR_DURATION);
        mIconErrorAnimator.setInterpolator(DEFAULT_MOVE_EASE_INTERPOLATOR);
        mIconErrorAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            mIconErrorScale = (0.3f * fraction) + DEFAULT_ALPHA_START_FRACTION;
            mCurrentErrorIconAlpha = (int) (fraction * FULL_ALPHA);
            invalidateSelf();
        });
        mErrorAnimatorSet = new AnimatorSet();
        mErrorAnimatorSet.playTogether(mIconErrorAnimator, mProgressShrinkAnimator);
        mErrorAnimatorSet.addListener(new StateListener() {
            @Override
            void onStart() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onErrorAnimationStart();
                }
            }

            @Override
            void onEnd() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onErrorAnimationEnd();
                }
            }
        });
    }

    private void initPaint() {
        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackPaint.setStyle(Paint.Style.STROKE);
        mTrackProperty = new ProgressBarStyleProperty();
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        mProgressProperty = new ProgressBarStyleProperty();
        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setStyle(Paint.Style.FILL);
    }

    private void initPauseAnimator() {
        mProgressPauseAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressPauseAnimator.setDuration(DEFAULT_PROGRESS_PAUSE_DURATION);
        mProgressPauseAnimator.setInterpolator(DEFAULT_IN_EASE_INTERPOLATOR);
        mProgressPauseAnimator.addUpdateListener(animation -> {
            mProgressBarAlpha = (int) ((1f - animation.getAnimatedFraction()) * FULL_ALPHA);
            invalidateSelf();
        });
        mIconPauseAnimator = ValueAnimator.ofFloat(0f, 1f);
        mIconPauseAnimator.setStartDelay(DEFAULT_DELAY);
        mIconPauseAnimator.setDuration(DEFAULT_ICON_PAUSE_DURATION);
        mIconPauseAnimator.setInterpolator(DEFAULT_OUT_EASE_INTERPOLATOR);
        mIconPauseAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            mCurrentPauseIconAlpha = (int) (FULL_ALPHA * fraction);
            mIconPauseScale = (fraction * 0.3f) + DEFAULT_ALPHA_START_FRACTION;
            invalidateSelf();
        });
        mPauseAnimatorSet = new AnimatorSet();
        mPauseAnimatorSet.playTogether(mProgressPauseAnimator, mIconPauseAnimator);
        mPauseAnimatorSet.addListener(new StateListener() {
            @Override
            void onStart() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onPauseAnimationStart();
                }
            }

            @Override
            void onEnd() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onPauseAnimationEnd();
                }
            }
        });
    }

    private void initRecoverAnimator() {
        mProgressEnlargeAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressEnlargeAnimator.setDuration(DEFAULT_ERROR_DURATION);
        mProgressEnlargeAnimator.setInterpolator(DEFAULT_MOVE_EASE_INTERPOLATOR);
        mProgressEnlargeAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            float progressOuter = mProgressProperty.getProgressBarErrorOuterDiameter()
                    + ((mProgressProperty.getProgressBarOuterDiameter()
                    - mProgressProperty.getProgressBarErrorOuterDiameter()) * fraction);
            float trackOuter = mTrackProperty.getProgressBarErrorOuterDiameter()
                    + ((mTrackProperty.getProgressBarOuterDiameter()
                    - mTrackProperty.getProgressBarErrorOuterDiameter()) * fraction);
            float progressStroke = mProgressProperty.getProgressBarErrorStrokeWidth()
                    + ((mProgressProperty.getProgressBarStrokeWidth()
                    - mProgressProperty.getProgressBarErrorStrokeWidth()) * fraction);
            float trackStroke = mTrackProperty.getProgressBarErrorStrokeWidth()
                    + ((mTrackProperty.getProgressBarStrokeWidth()
                    - mTrackProperty.getProgressBarErrorStrokeWidth()) * fraction);
            int trackColor = (Integer) COLOR_EVALUATOR.evaluate(fraction,
                    mTrackProperty.getProgressBarErrorColor(), mTrackProperty.getProgressBarColor());
            int progressColor = (Integer) COLOR_EVALUATOR.evaluate(fraction,
                    mProgressProperty.getProgressBarErrorColor(), mProgressProperty.getProgressBarColor());
            mProgressProperty.setProgressBarCurrentOuterDiameter(progressOuter);
            mProgressProperty.setProgressBarCurrentStrokeWidth(progressStroke);
            mProgressProperty.setCurrentBarColor(progressColor);
            mTrackProperty.setProgressBarCurrentOuterDiameter(trackOuter);
            mTrackProperty.setProgressBarCurrentStrokeWidth(trackStroke);
            mTrackProperty.setCurrentBarColor(trackColor);
            invalidateSelf();
        });
        mIconRecoverAnimator = ValueAnimator.ofFloat(0f, 1f);
        mIconRecoverAnimator.setDuration(DEFAULT_ERROR_DURATION);
        mIconRecoverAnimator.setInterpolator(DEFAULT_MOVE_EASE_INTERPOLATOR);
        mIconRecoverAnimator.addUpdateListener(animation -> {
            float reverse = 1f - animation.getAnimatedFraction();
            mIconErrorScale = (0.3f * reverse) + DEFAULT_ALPHA_START_FRACTION;
            mCurrentErrorIconAlpha = (int) (reverse * FULL_ALPHA);
            invalidateSelf();
        });
        mRecoverAnimatorSet = new AnimatorSet();
        mRecoverAnimatorSet.playTogether(mProgressEnlargeAnimator, mIconRecoverAnimator);
        mRecoverAnimatorSet.addListener(new StateListener() {
            @Override
            void onStart() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onRecoverAnimationStart();
                }
            }

            @Override
            void onEnd() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onRecoverAnimationEnd();
                }
            }
        });
    }

    private void initResumeAnimator() {
        mProgressResumeAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressResumeAnimator.setStartDelay(DEFAULT_DELAY);
        mProgressResumeAnimator.setDuration(DEFAULT_PROGRESS_RESUME_DURATION);
        mProgressResumeAnimator.setInterpolator(DEFAULT_OUT_EASE_INTERPOLATOR);
        mProgressResumeAnimator.addUpdateListener(animation -> {
            mProgressBarAlpha = (int) (animation.getAnimatedFraction() * FULL_ALPHA);
            invalidateSelf();
        });
        mIconResumeAnimator = ValueAnimator.ofFloat(0f, 1f);
        mIconResumeAnimator.setDuration(DEFAULT_ICON_RESUME_DURATION);
        mIconResumeAnimator.setInterpolator(DEFAULT_LINEAR_INTERPOLATOR);
        mIconResumeAnimator.addUpdateListener(animation -> {
            mCurrentPauseIconAlpha = (int) ((1f - animation.getAnimatedFraction()) * FULL_ALPHA);
            mIconPauseScale = 1f;
            invalidateSelf();
        });
        mResumeAnimatorSet = new AnimatorSet();
        mResumeAnimatorSet.playTogether(mProgressResumeAnimator, mIconResumeAnimator);
        mResumeAnimatorSet.addListener(new StateListener() {
            @Override
            void onStart() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onResumeAnimationStart();
                }
            }

            @Override
            void onEnd() {
                if (mOnProgressStateAnimatorListener != null) {
                    mOnProgressStateAnimatorListener.onResumeAnimationEnd();
                }
            }
        });
    }

    private void initSpring() {
        SpringForce force = new SpringForce();
        force.setDampingRatio(1f);
        force.setStiffness(50f);
        mSpringAnimation = new SpringAnimation(this, VISUAL_PROGRESS);
        mSpringAnimation.setSpring(force);
        mSpringAnimation.addUpdateListener((animation, value, velocity) -> invalidateSelf());
    }

    private void notifyActualProgressChanged() {
        if (mOnProgressChangedListener != null) {
            mOnProgressChangedListener.onProgressChanged(mActualProgress);
        }
    }

    private void notifyVisualProgressChanged() {
        if (mOnProgressChangedListener != null) {
            mOnProgressChangedListener.onVisualProgressChanged(mVisualProgress);
        }
    }

    private void setVisualProgress(float visualProgress) {
        mVisualProgress = visualProgress;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mTrackPaint.setColor(mTrackProperty.getCurrentBarColor());
        mTrackPaint.setStrokeWidth(mTrackProperty.getProgressBarCurrentStrokeWidth());
        mProgressPaint.setColor(mProgressProperty.getCurrentBarColor());
        mProgressPaint.setStrokeWidth(mProgressProperty.getProgressBarCurrentStrokeWidth());
        canvas.saveLayerAlpha(0f, 0f, mCenterX * 2f, mCenterY * 2f, mGlobalAlpha);
        drawProgress(canvas);
        drawPauseIcon(canvas);
        drawErrorIcon(canvas);
        canvas.restore();
    }

    public void error() {
        mErrorAnimatorSet.start();
    }

    @Override
    public int getAlpha() {
        return mGlobalAlpha;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public float getVisualProgress() {
        return mVisualProgress;
    }

    @Override
    public void invalidateSelf() {
        super.invalidateSelf();
        if (mHostView != null) {
            mHostView.invalidate();
        }
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    public void pause() {
        mPauseAnimatorSet.start();
    }

    public void recover() {
        mRecoverAnimatorSet.start();
    }

    public void recycle() {
        mHostView = null;
    }

    public void resume() {
        mResumeAnimatorSet.start();
    }

    @Override
    public void setAlpha(int alpha) {
        mGlobalAlpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        invalidateSelf();
    }

    public void setErrorIconColor(int color) {
        mErrorIconColor = color;
        mTrackProperty.setProgressBarErrorColor(color);
        mProgressProperty.setProgressBarErrorColor(color);
    }

    public void setHostView(View hostView) {
        mHostView = hostView;
    }

    public void setIsDrawShadow(boolean drawShadow) {
        if (drawShadow) {
            mTrackPaint.setShadowLayer(mShadowRadius, mShadowXBias, mShadowYBias, mShadowColor);
            mIconPaint.setShadowLayer(mShadowRadius, mShadowXBias, mShadowYBias, mShadowColor);
        } else {
            mTrackPaint.clearShadowLayer();
            mIconPaint.clearShadowLayer();
        }
    }

    public void setMax(int max) {
        if (max < 0) {
            Log.w(TAG, "Max value should not lesser than 0!");
            max = 0;
        }
        if (max != mMax) {
            if (max < mActualProgress) {
                mActualProgress = max;
                mVisualProgress = max;
            }
            mMax = max;
        }
        invalidateSelf();
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mOnProgressChangedListener = listener;
    }

    public void setOnProgressStateAnimatorListener(OnProgressStateAnimatorListener listener) {
        mOnProgressStateAnimatorListener = listener;
    }

    public void setPauseIconColor(int color) {
        mPauseIconColor = color;
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    public void setProgress(int progress, boolean animate) {
        Log.d(TAG, "setProgress: " + progress + "\nmActualProgress = " + mActualProgress
                + "\nmVisualProgress = " + mVisualProgress + "\nanimate = " + animate);
        mActualProgress = progress;
        float visual = actual2VisualProgress(progress);
        if (animate && mVisualProgress != visual) {
            mSpringAnimation.setStartValue(mVisualProgress);
            mSpringAnimation.animateToFinalPosition(visual);
        } else {
            mVisualProgress = visual;
            notifyVisualProgressChanged();
            invalidateSelf();
        }
        notifyActualProgressChanged();
    }

    public void setProgressBarErrorColor(int color) {
        mTrackProperty.setProgressBarErrorColor(color);
        mProgressProperty.setProgressBarErrorColor(color);
    }

    public void setProgressBarErrorSize(float diameter, float strokeWidth) {
        mProgressProperty.setProgressBarErrorOuterDiameter(diameter);
        mProgressProperty.setProgressBarErrorStrokeWidth(strokeWidth);
        mTrackProperty.setProgressBarErrorOuterDiameter(diameter);
        mTrackProperty.setProgressBarErrorStrokeWidth(strokeWidth);
    }

    public void setProgressColor(int color) {
        mProgressProperty.setProgressBarColor(color);
        invalidateSelf();
    }

    public void setProperties(float centerX, float centerY, float outerDiameter, float strokeWidth) {
        mCenterX = centerX;
        mCenterY = centerY;
        mProgressBarOuterDiameter = outerDiameter;
        mProgressBarStrokeWidth = strokeWidth;
        mTrackProperty.setProgressBarCenterX(centerX);
        mTrackProperty.setProgressBarCenterY(mCenterY);
        mTrackProperty.setProgressBarOuterDiameter(mProgressBarOuterDiameter);
        mTrackProperty.setProgressBarStrokeWidth(mProgressBarStrokeWidth);
        mTrackProperty.setProgressBarCurrentOuterDiameter(mProgressBarOuterDiameter);
        mTrackProperty.setProgressBarCurrentStrokeWidth(mProgressBarStrokeWidth);
        mProgressProperty.setProgressBarCenterX(mCenterX);
        mProgressProperty.setProgressBarCenterY(mCenterY);
        mProgressProperty.setProgressBarOuterDiameter(mProgressBarOuterDiameter);
        mProgressProperty.setProgressBarStrokeWidth(mProgressBarStrokeWidth);
        mProgressProperty.setProgressBarCurrentOuterDiameter(mProgressBarOuterDiameter);
        mProgressProperty.setProgressBarCurrentStrokeWidth(mProgressBarStrokeWidth);
        mTrackPaint.setStrokeWidth(mTrackProperty.getProgressBarStrokeWidth());
        mProgressPaint.setStrokeWidth(mProgressProperty.getProgressBarStrokeWidth());
    }

    public void setTrackColor(int color) {
        mTrackProperty.setProgressBarColor(color);
        invalidateSelf();
    }

    private abstract class StateListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
            mAnimating = true;
            onStart();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mAnimating = false;
            onEnd();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mAnimating = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        void onStart() {
        }

        void onEnd() {
        }
    }
}
