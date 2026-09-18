package com.coui.appcompat.scroll;

import android.content.Context;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;


public class COUILocateOverScroller extends OverScroller implements COUIIOverScroller {
    private static final int FLING_MODE = 1;
    private static final int INVALID_POSITION = -1;
    private static final float ONE = 1.0f;
    private static final Interpolator SCROLL = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            float fraction = input - 1.0f;
            return (fraction * fraction * fraction * fraction * fraction) + 1.0f;
        }
    };
    private static final int SCROLL_DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final String TAG = "COUILocateOverScroller";
    private static final float THOUSAND = 1000.0f;
    private COUlFrameRateScrollSceneHelper mFrameRateHelper;
    private Interpolator mInterpolator;
    private int mMode;
    private COUISplineOverScroller mScrollerX;
    private COUISplineOverScroller mScrollerY;

    public static class COUISplineOverScroller {
        private static final int BALLISTIC = 2;
        private static final int CUBIC = 1;
        private static final float END_TENSION = 1.0f;
        private static final float GRAVITY = 2000.0f;
        private static final float INFLEXION = 0.35f;
        private static final float LENGTH = 39.37f;
        private static final double MIN_ALPHA = 1.0E-5d;
        private static final int NB_SAMPLES = 100;
        private static final float ONE = 1.0f;
        private static final float P1 = 0.175f;
        private static final float P2 = 0.35000002f;
        private static final float SIX = 6.0f;
        private static final int SPLINE = 0;
        private static final float START_TENSION = 0.5f;
        private static final float THREE = 3.0f;
        private static final float TUNING = 0.84f;
        private static final float TWO = 2.0f;
        private static final float ZERO = 0.0f;
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private int mOver;
        private float mPhysicalCoeff;
        private int mSplineDistance;
        private int mSplineDuration;
        private int mStart;
        private long mStartTime;
        private int mVelocity;
        private static final float DECELERATION_RATE = (float) (Math.log(0.78d) / Math.log(0.9d));
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private float mDurationRatio = 1.0f;
        private float mVelocityRatio = 1.0f;
        private float mFlingFriction = ViewConfiguration.getScrollFriction() * 2.5f;
        private int mState = 0;
        private boolean mFinished = true;

        static {
            float fraction;
            float ratio;
            float scale;
            float x;
            float y;
            float progress;
            float alpha;
            float distance;
            float velocityF;
            float factor;
            float input = 0.0f;
            float tensionF = 0.0f;
            for (int i = 0; i < 100; i++) {
                float frictionF = i / 100.0f;
                float deltaF = 1.0f;
                while (true) {
                    fraction = 2.0f;
                    ratio = ((deltaF - input) / 2.0f) + input;
                    scale = THREE;
                    x = 1.0f - ratio;
                    y = ratio * THREE * x;
                    progress = ratio * ratio * ratio;
                    float offsetF = (((x * P1) + (ratio * P2)) * y) + progress;
                    if (Math.abs(offsetF - frictionF) < MIN_ALPHA) {
                        break;
                    } else if (offsetF > frictionF) {
                        deltaF = ratio;
                    } else {
                        input = ratio;
                    }
                }
                SPLINE_POSITION[i] = (y * ((x * 0.5f) + ratio)) + progress;
                float startF = 1.0f;
                while (true) {
                    alpha = ((startF - tensionF) / fraction) + tensionF;
                    distance = 1.0f - alpha;
                    velocityF = alpha * scale * distance;
                    factor = alpha * alpha * alpha;
                    float endF = (((distance * 0.5f) + alpha) * velocityF) + factor;
                    if (Math.abs(endF - frictionF) < MIN_ALPHA) {
                        break;
                    }
                    if (endF > frictionF) {
                        startF = alpha;
                    } else {
                        tensionF = alpha;
                    }
                    fraction = 2.0f;
                    scale = THREE;
                }
                SPLINE_TIME[i] = (velocityF * ((distance * P1) + (alpha * P2))) + factor;
            }
            SPLINE_POSITION[100] = 1.0f;
            SPLINE_TIME[100] = 1.0f;
        }

        public COUISplineOverScroller(Context context) {
            this.mPhysicalCoeff = context.getResources().getDisplayMetrics().density * 160.0f * 386.0878f * TUNING;
        }

        private void adjustDuration(int index, int count, int value) {
            float fAbs = Math.abs((value - index) / (count - index));
            int end = (int) (fAbs * 100.0f);
            if (end >= 100 || end < 0) {
                return;
            }
            float fraction = end / 100.0f;
            int min = end + 1;
            float[] fArr = SPLINE_TIME;
            float ratio = fArr[end];
            this.mDuration = (int) (this.mDuration * (ratio + (((fAbs - fraction) / ((min / 100.0f) - fraction)) * (fArr[min] - ratio))));
        }

        private void fitOnBounceCurve(int index, int currentPosition, int count) {
            float fraction = this.mDeceleration;
            float ratio = (-count) / fraction;
            float scale = count;
            float fSqrt = (float) Math.sqrt((((double) ((((scale * scale) / 2.0f) / Math.abs(fraction)) + Math.abs(currentPosition - index))) * 2.0d) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) ((fSqrt - ratio) * COUILocateOverScroller.THOUSAND));
            this.mCurrentPosition = currentPosition;
            this.mStart = currentPosition;
            this.mVelocity = (int) ((-this.mDeceleration) * fSqrt);
        }

        private static float getDeceleration(int index) {
            if (index > 0) {
                return -GRAVITY;
            }
            return GRAVITY;
        }

        private double getSplineDeceleration(int index) {
            return Math.log((Math.abs(index) * INFLEXION) / (this.mFlingFriction * this.mPhysicalCoeff));
        }

        private double getSplineFlingDistance(int index) {
            double splineDeceleration = getSplineDeceleration(index);
            float fraction = DECELERATION_RATE;
            return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) fraction) / (((double) fraction) - 1.0d)) * splineDeceleration);
        }

        private int getSplineFlingDuration(int index) {
            return (int) (Math.exp(getSplineDeceleration(index) / ((double) (DECELERATION_RATE - 1.0f))) * THOUSAND);
        }

        private void onEdgeReached() {
            int index = this.mVelocity;
            float fraction = index * index;
            float fAbs = fraction / (Math.abs(this.mDeceleration) * 2.0f);
            float fSignum = Math.signum(this.mVelocity);
            int count = this.mOver;
            if (fAbs > count) {
                this.mDeceleration = ((-fSignum) * fraction) / (count * 2.0f);
                fAbs = count;
            }
            this.mOver = (int) fAbs;
            this.mState = 2;
            int finalPos = this.mStart;
            int value = this.mVelocity;
            if (value <= 0) {
                fAbs = -fAbs;
            }
            this.mFinal = finalPos + ((int) fAbs);
            this.mDuration = -((int) ((value * COUILocateOverScroller.THOUSAND) / this.mDeceleration));
        }

        private void startAfterEdge(int index, int count, int value, int offset) {
            if (index > count && index < value) {
                Log.e(COUILocateOverScroller.TAG, "startAfterEdge called from a valid position");
                this.mFinished = true;
                return;
            }
            boolean enabled = index > value;
            int min = enabled ? value : count;
            if ((index - min) * offset >= 0) {
                startBounceAfterEdge(index, min, offset);
            } else if (getSplineFlingDistance(offset) > Math.abs(index - min)) {
                fling(index, offset, enabled ? count : index, enabled ? index : value, this.mOver);
            } else {
                startSpringback(index, min, offset);
            }
        }

        private void startBounceAfterEdge(int index, int count, int value) {
            this.mDeceleration = getDeceleration(value == 0 ? index - count : value);
            fitOnBounceCurve(index, count, value);
            onEdgeReached();
        }

        private void startSpringback(int currentPosition, int finalPos, int index) {
            this.mFinished = false;
            this.mState = 1;
            this.mCurrentPosition = currentPosition;
            this.mStart = currentPosition;
            this.mFinal = finalPos;
            int count = currentPosition - finalPos;
            this.mDeceleration = getDeceleration(count);
            this.mVelocity = -count;
            this.mOver = Math.abs(count);
            this.mDuration = (int) (Math.sqrt((count * (-2.0f)) / this.mDeceleration) * THOUSAND);
        }

        public boolean continueWhenFinished() {
            int index = this.mState;
            if (index != 0) {
                if (index == 1) {
                    return false;
                }
                if (index == 2) {
                    this.mStartTime += (long) this.mDuration;
                    startSpringback(this.mFinal, this.mStart, 0);
                }
            } else {
                if (this.mDuration >= this.mSplineDuration) {
                    return false;
                }
                int currentPosition = this.mFinal;
                this.mCurrentPosition = currentPosition;
                this.mStart = currentPosition;
                int velocity = (int) this.mCurrVelocity;
                this.mVelocity = velocity;
                this.mDeceleration = getDeceleration(velocity);
                this.mStartTime += (long) this.mDuration;
                onEdgeReached();
            }
            update();
            return true;
        }

        public void finish() {
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
        }

        public void fling(int currentPosition, int velocity, int min, int max, int over) {
            double splineFlingDistance;
            this.mOver = over;
            this.mFinished = false;
            float currVelocity = velocity;
            this.mCurrVelocity = currVelocity;
            this.mVelocity = velocity;
            this.mDuration = 0;
            this.mSplineDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mCurrentPosition = currentPosition;
            this.mStart = currentPosition;
            if (currentPosition > max || currentPosition < min) {
                startAfterEdge(currentPosition, min, max, velocity);
                return;
            }
            float fraction = this.mVelocityRatio;
            if (fraction != 1.0f) {
                velocity = (int) (currVelocity * fraction);
                float currVelocity2 = velocity;
                this.mCurrVelocity = currVelocity2;
                this.mVelocity = Math.round(currVelocity2 * fraction);
            }
            this.mState = 0;
            if (velocity != 0) {
                int iRound = Math.round(getSplineFlingDuration(velocity) * this.mDurationRatio);
                this.mDuration = iRound;
                this.mSplineDuration = iRound;
                splineFlingDistance = getSplineFlingDistance(velocity);
            } else {
                splineFlingDistance = 0.0d;
            }
            int iSignum = (int) (splineFlingDistance * ((double) Math.signum(velocity)));
            this.mSplineDistance = iSignum;
            int finalPos5 = currentPosition + iSignum;
            this.mFinal = finalPos5;
            if (finalPos5 < min) {
                adjustDuration(this.mStart, finalPos5, min);
                this.mFinal = min;
            }
            int index = this.mFinal;
            if (index > max) {
                adjustDuration(this.mStart, index, max);
                this.mFinal = max;
            }
        }

        public void notifyEdgeReached(int index, int count, int over) {
            if (this.mState == 0) {
                this.mOver = over;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                startAfterEdge(index, count, count, (int) this.mCurrVelocity);
            }
        }

        public void setFinalPosition(int finalPos) {
            this.mFinal = finalPos;
            this.mSplineDistance = finalPos - this.mStart;
            this.mFinished = false;
        }

        public void setFriction(float friction) {
            this.mFlingFriction = friction;
        }

        public boolean springback(int currentPosition, int index, int count) {
            this.mFinished = true;
            this.mCurrentPosition = currentPosition;
            this.mStart = currentPosition;
            this.mFinal = currentPosition;
            this.mVelocity = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 0;
            if (currentPosition < index) {
                startSpringback(currentPosition, index, 0);
            } else if (currentPosition > count) {
                startSpringback(currentPosition, count, 0);
            }
            return !this.mFinished;
        }

        public void startScroll(int currentPosition, int index, int duration) {
            this.mFinished = false;
            this.mCurrentPosition = currentPosition;
            this.mStart = currentPosition;
            this.mFinal = currentPosition + index;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        public boolean update() {
            float fraction;
            float ratio;
            double positionD;
            long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            if (jCurrentAnimationTimeMillis == 0) {
                return this.mDuration > 0;
            }
            int index = this.mDuration;
            if (jCurrentAnimationTimeMillis > index) {
                return false;
            }
            int count = this.mState;
            if (count == 0) {
                int value = this.mSplineDuration;
                float scale = jCurrentAnimationTimeMillis / value;
                int offset = (int) (scale * 100.0f);
                if (offset >= 100 || offset < 0) {
                    fraction = 1.0f;
                    ratio = 0.0f;
                } else {
                    float x = offset / 100.0f;
                    int delta = offset + 1;
                    float[] fArr = SPLINE_POSITION;
                    float y = fArr[offset];
                    ratio = (fArr[delta] - y) / ((delta / 100.0f) - x);
                    fraction = y + ((scale - x) * ratio);
                }
                int start = this.mSplineDistance;
                this.mCurrVelocity = ((ratio * start) / value) * COUILocateOverScroller.THOUSAND;
                positionD = fraction * start;
            } else if (count == 1) {
                float progress = jCurrentAnimationTimeMillis / index;
                float alpha = progress * progress;
                float fSignum = Math.signum(this.mVelocity);
                int end = this.mOver;
                double tension = end * fSignum * ((THREE * alpha) - ((2.0f * progress) * alpha));
                this.mCurrVelocity = fSignum * end * SIX * ((-progress) + alpha);
                positionD = tension;
            } else if (count != 2) {
                positionD = 0.0d;
            } else {
                float distance = jCurrentAnimationTimeMillis / COUILocateOverScroller.THOUSAND;
                int currVelocity = this.mVelocity;
                float velocityF = this.mDeceleration;
                this.mCurrVelocity = currVelocity + (velocityF * distance);
                positionD = (currVelocity * distance) + (((velocityF * distance) * distance) / 2.0f);
            }
            this.mCurrentPosition = this.mStart + ((int) Math.round(positionD));
            return true;
        }

        public void updateScroll(float fraction) {
            this.mCurrentPosition = this.mStart + Math.round(fraction * (this.mFinal - this.mStart));
        }
    }

    public COUILocateOverScroller(Context context) {
        this(context, null);
    }

    @Override
    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
        this.mFrameRateHelper.setFrameRate(false);
    }

    @Override
    public boolean computeScrollOffset() {
        if (isCOUIFinished()) {
            return false;
        }
        int index = this.mMode;
        if (index == 0) {
            long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mStartTime;
            int count = this.mScrollerX.mDuration;
            if (jCurrentAnimationTimeMillis < count) {
                float interpolation = this.mInterpolator.getInterpolation(jCurrentAnimationTimeMillis / count);
                this.mScrollerX.updateScroll(interpolation);
                this.mScrollerY.updateScroll(interpolation);
            } else {
                abortAnimation();
            }
        } else if (index == 1) {
            if (!this.mScrollerX.mFinished && !this.mScrollerX.update() && !this.mScrollerX.continueWhenFinished()) {
                this.mScrollerX.finish();
            }
            if (!this.mScrollerY.mFinished && !this.mScrollerY.update() && !this.mScrollerY.continueWhenFinished()) {
                this.mScrollerY.finish();
            }
        }
        return true;
    }

    public void enableFrameRate(boolean enabled) {
        this.mFrameRateHelper.enableFrameRate(enabled);
    }

    @Override
    public void fling(int index, int count, int value, int offset, int delta, int start, int end, int min) {
        fling(index, count, value, offset, delta, start, end, min, 0, 0);
    }

    @Override
    public int getCOUICurrX() {
        return this.mScrollerX.mCurrentPosition;
    }

    @Override
    public int getCOUICurrY() {
        return this.mScrollerY.mCurrentPosition;
    }

    @Override
    public int getCOUIFinalX() {
        return this.mScrollerX.mFinal;
    }

    @Override
    public int getCOUIFinalY() {
        return this.mScrollerY.mFinal;
    }

    @Override
    public float getCurrVelocity() {
        return (float) Math.hypot(this.mScrollerX.mCurrVelocity, this.mScrollerY.mCurrVelocity);
    }

    @Override
    public float getCurrVelocityX() {
        return this.mScrollerX.mCurrVelocity;
    }

    @Override
    public float getCurrVelocityY() {
        return this.mScrollerY.mCurrVelocity;
    }

    @Override
    public boolean isCOUIFinished() {
        return this.mScrollerX.mFinished && this.mScrollerY.mFinished;
    }

    @Override
    public boolean isScrollingInDirection(float fraction, float ratio) {
        return !isFinished() && Math.signum(fraction) == Math.signum((float) (this.mScrollerX.mFinal - this.mScrollerX.mStart)) && Math.signum(ratio) == Math.signum((float) (this.mScrollerY.mFinal - this.mScrollerY.mStart));
    }

    @Override
    public void notifyHorizontalEdgeReached(int index, int count, int value) {
        this.mScrollerX.notifyEdgeReached(index, count, value);
        springBack(index, 0, 0, 0, 0, 0);
    }

    @Override
    public void notifyVerticalEdgeReached(int index, int count, int value) {
        this.mScrollerY.notifyEdgeReached(index, count, value);
        springBack(0, index, 0, 0, 0, 0);
    }

    @Override
    public void setCOUIFriction(float fraction) {
    }

    @Override
    public void setCurrVelocityX(float currVelocity) {
        this.mScrollerX.mCurrVelocity = currVelocity;
    }

    @Override
    public void setCurrVelocityY(float currVelocity) {
        this.mScrollerY.mCurrVelocity = currVelocity;
    }

    @Override
    public void setDurationRatio(float durationRatio) {
        this.mScrollerX.mDurationRatio = durationRatio;
        this.mScrollerY.mDurationRatio = durationRatio;
    }

    @Override
    public void setFinalX(int index) {
        if (index == -1) {
            return;
        }
        this.mScrollerX.setFinalPosition(index);
    }

    @Override
    public void setFinalY(int index) {
        if (index == -1) {
            return;
        }
        this.mScrollerY.setFinalPosition(index);
    }

    @Override
    public void setFlingFriction(float fraction) {
        this.mScrollerX.setFriction(fraction);
        this.mScrollerY.setFriction(fraction);
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = SCROLL;
        } else {
            this.mInterpolator = interpolator;
        }
    }

    @Override
    public void setIsScrollView(boolean enabled) {
    }

    @Override
    public void setVelocityXRatio(float velocityRatio) {
        this.mScrollerX.mVelocityRatio = velocityRatio;
    }

    @Override
    public void setVelocityYRatio(float velocityRatio) {
        this.mScrollerY.mVelocityRatio = velocityRatio;
    }

    @Override
    public boolean springBack(int index, int count, int value, int offset, int delta, int start) {
        boolean zSpringback = this.mScrollerX.springback(index, value, offset);
        boolean zSpringback2 = this.mScrollerY.springback(count, delta, start);
        if (zSpringback || zSpringback2) {
            this.mMode = 1;
        }
        return zSpringback || zSpringback2;
    }

    @Override
    public void startScroll(int index, int count, int value, int offset) {
        startScroll(index, count, value, offset, SCROLL_DEFAULT_DURATION);
    }

    public COUILocateOverScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
        this.mScrollerX = new COUISplineOverScroller(context);
        this.mScrollerY = new COUISplineOverScroller(context);
        if (interpolator == null) {
            this.mInterpolator = SCROLL;
        } else {
            this.mInterpolator = interpolator;
        }
        this.mFrameRateHelper = new COUlFrameRateScrollSceneHelper(false);
    }

    @Override
    public void fling(int index, int count, int value, int offset, int delta, int start, int end, int min, int max, int size) {
        if (count > min || count < end) {
            springBack(index, count, delta, start, end, min);
        } else {
            fling(index, count, value, offset);
        }
    }

    @Override
    public void startScroll(int index, int count, int value, int offset, int delta) {
        this.mMode = 0;
        this.mScrollerX.startScroll(index, value, delta);
        this.mScrollerY.startScroll(count, offset, delta);
        this.mFrameRateHelper.setFrameRate(true);
    }

    @Override
    public void fling(int index, int count, int value, int offset) {
        this.mMode = 1;
        this.mScrollerX.fling(index, value, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        this.mScrollerY.fling(count, offset, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        this.mFrameRateHelper.setFrameRate(true);
    }
}
