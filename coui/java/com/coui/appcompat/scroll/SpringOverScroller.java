package com.coui.appcompat.scroll;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import com.coui.appcompat.animation.COUISpringInterpolator;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.uiutil.UIUtil;
import java.lang.reflect.Method;


public class SpringOverScroller extends OverScroller implements COUIIOverScroller {
    public static final float COUI_FLING_FRICTION_FAST = 0.76f;
    public static final int COUI_FLING_MODE_FAST = 0;
    public static final int COUI_FLING_MODE_NORMAL = 1;
    private static boolean DEBUG = false;
    private static final float ERROR_THRESHOLD = 0.025f;
    private static final int FLING_MODE = 1;
    private static final int FLING_SPEED_INCREASE_COUNT_THRESHOLD = 4;
    private static final int FLING_SPEED_INCREASE_EDGE_REACHED_VELOCITY = 1000;
    private static final int FLING_SPEED_INCREASE_EDGE_REACHED_VELOCITY_THRESHOLD = 20000;
    private static final int FLING_SPEED_INCREASE_MAX_VELOCITY = 70000;
    private static final float FLING_SPEED_INCREASE_RATE = 1.4f;
    private static final int FLING_SPEED_INCREASE_TIME_INTERVAL_THRESHOLD = 500;
    private static final int FLING_SPEED_INCREASE_VELOCITY_THRESHOLD = 8000;
    private static final float MIN_FRAME_INTERVAL = 0.008f;
    private static final double MIN_UPDATE_ONE_STEP = 0.5d;
    private static final float NANO_ONE_SECOND = 1.0E9f;
    private static final float NANO_TO_MILLIS = 1000000.0f;
    private static final float ONE_SECOND = 1000.0f;
    private static final int REST_MODE = 2;
    private static final int SCROLL_DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final float SOLVER_TIMESTEP_SEC = 0.016f;
    public static final String TAG = "SpringOverScroller";
    private static final int VSYNC_DURATION = 5000;
    private static float mRefreshTime = 0.0f;
    private static float mSpringBackFriction = 12.19f;
    private final Choreographer.FrameCallback mCallback;
    private boolean mCancelCallback;
    private Context mContext;
    private int mContinuousFlingCount;
    private long mCurrentComputeTimeFromCallback;
    private boolean mEnableFlingSpeedIncrease;
    private COUlFrameRateScrollSceneHelper mFrameRateHelper;
    private Interpolator mInterpolator;
    private boolean mIsUpdateTimeFromCallback;
    private long mLastComputeTimeFromCallback;
    private float mLastFlingSpeedIncreaseRate;
    private long mLastFlingTime;
    private int mMode;
    protected ReboundOverScroller mScrollerX;
    protected ReboundOverScroller mScrollerY;

    public static class COUIViscousFluidInterpolator implements Interpolator {
        private static final float VISCOUS_FLUID_NORMALIZE;
        private static final float VISCOUS_FLUID_OFFSET;
        private static final float VISCOUS_FLUID_SCALE = 8.0f;

        static {
            float fViscousFluid = 1.0f / viscousFluid(1.0f);
            VISCOUS_FLUID_NORMALIZE = fViscousFluid;
            VISCOUS_FLUID_OFFSET = 1.0f - (fViscousFluid * viscousFluid(1.0f));
        }

        private static float viscousFluid(float x) {
            float fraction = x * VISCOUS_FLUID_SCALE;
            return fraction < 1.0f ? fraction - (1.0f - ((float) Math.exp(-fraction))) : 0.36787945f + ((1.0f - ((float) Math.exp(1.0f - fraction))) * 0.63212055f);
        }

        @Override
        public float getInterpolation(float input) {
            float fViscousFluid = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
            return fViscousFluid > 0.0f ? fViscousFluid + VISCOUS_FLUID_OFFSET : fViscousFluid;
        }
    }

    public static class ReboundOverScroller {
        private static final float DEFAULT_CUTRATIO = 1.0f;
        private static final double DEFAULT_DAMPINGRATIO = 1.15d;
        private static final double DEFAULT_STIFFNESS = 40.0d;
        private static final float DEFAULT_VELOCITY_UNIT = 15000.0f;
        private static final double DELTA_POSITION_HIGH = 0.35d;
        private static final double DELTA_POSITION_LOW = 0.2d;
        private static final double DELTA_POSITION_MID = 0.3d;
        private static final double DELTA_POSITION_VELOCITY_HIGH = 8000.0d;
        private static final double DELTA_POSITION_VELOCITY_LOW = 5000.0d;
        private static final float DISTANCE_SCALE_MAX = 1.0f;
        private static final float DISTANCE_SCALE_MID = 0.8f;
        private static final float DISTANCE_SCALE_MIN = 0.4f;
        private static final float DURATION_SCALE_MAX = 1.3f;
        private static final float DURATION_SCALE_MID = 0.8f;
        private static final float DURATION_SCALE_MIN = 0.5f;
        private static final float FLING_CHANGE_INCREASE_STEP = 1.2f;
        private static final float FLING_CHANGE_REDUCE_STEP = 0.6f;
        private static final float FLING_DXDT_RATIO = 0.167f;
        private static final double FLING_FRICTION_DIVISOR = 10000.0d;
        private static final float FLING_VELOCITY_HIGH = 10000.0f;
        private static final float FLING_VELOCITY_HIGHEST = 20000.0f;
        private static final float FLING_VELOCITY_LOW = 6000.0f;
        private static final float FLING_VELOCITY_LOWEST = 2000.0f;
        private static final float FLOAT_1 = 1.0f;
        private static final float FLOAT_2 = 2.0f;
        private static final double INCREASE_FRICTION_COEF = 0.00125d;
        private static final double MAX_VELOCITY_ADJUST_FRICTION = 10000.0d;
        private static final double MID_VELOCITY_ADJUST_FRICTION = 4000.0d;
        private static final double MIN_FLING_FRICTION_REDUCE = 2.0d;
        private static final double MIN_VELOCITY_ADJUST_FRICTION = 1000.0d;
        public static final long NANOS_PER_MS = 1000000;
        private static final int NUM_60 = 60;
        private static final float ONE_MILLION = 1.0E-7f;
        private static final double REDUCE_FRICTION_COEF = 0.00125d;
        private static final float REST_SPEED_THRESHOLD = 5.0f;
        private static final int SPRING_BACK_ADJUST_TENSION_VALUE = 100;
        private static final int SPRING_BACK_ADJUST_THRESHOLD = 180;
        private static final float SPRING_BACK_FRICTION = 12.19f;
        private static final float SPRING_BACK_STOP_THRESHOLD = 0.25f;
        private static final float SPRING_BACK_TENSION = 16.0f;
        private static final long TIME_ADJUST_FRICTION = 480;
        private static final double VELOCITY_REDUCE_FRICTION = 2000.0d;
        private static float sCouiFlingFrictionNormal = 0.2f;
        private static double sMidFlingBaseFriction = 2.5d;
        private static double sSlowFlingBaseFriction = 2.5d;
        private static float sTimeIncrease = 1.0f;
        private int mCOUICount;
        private boolean mCancelCallback;
        private Choreographer mChoreographer;
        private boolean mComputeTimeFromCallbackUpdated;
        private ReboundConfig mConfig;
        private long mCurrentComputeTime;
        private long mCurrentComputeTimeFromCallback;
        private double mDisplacementFromRestThreshold;
        private int mDuration;
        private double mEndValue;
        private boolean mFinished;
        private ReboundConfig mFlingConfig;
        private float mFlingFriction;
        private Method mGetFrameIntervalNanos;
        private boolean mIsScrollView;
        private boolean mIsSpringBack;
        private long mLastComputeTime;
        private long mLastComputeTimeFromCallback;
        private long mLastFlingUpdateTime;
        private double mRestSpeedThreshold;
        private int mScrollFinal;
        private int mScrollStart;
        private long mScrollStartTime;
        private int mSimulateDuration;
        private int mSimulateSplineDistance;
        private int mSimulateSplineDuration;
        private int mSplineDistance;
        private int mSplineDuration;
        private double mSplineMinDelta;
        private ReboundConfig mSpringBackConfig;
        private float mSpringBackTensionMultiple;
        private COUISpringInterpolator mSpringInterpolator;
        private long mStartTime;
        private double mStartValue;
        private boolean mTensionAdjusted;
        private boolean mWithSpring;
        private PhysicsState mCurrentState = new PhysicsState();
        private PhysicsState mPreviousState = new PhysicsState();
        private PhysicsState mTempState = new PhysicsState();
        private Rk4Data mRk4Result = new Rk4Data(0.0d, 0.0d, 0.0d, 0.0d);

        public static class PhysicsState {
            double mPosition;
            double mVelocity;
        }

        public static class ReboundConfig {
            double mFriction;
            double mTension;

            public ReboundConfig(double positionD, double tension) {
                this.mFriction = frictionFromOrigamiValue((float) positionD);
                this.mTension = tensionFromOrigamiValue((float) tension);
            }

            private float frictionFromOrigamiValue(float value) {
                if (value == 0.0f) {
                    return 0.0f;
                }
                return 25.0f + ((value - 8.0f) * 3.0f);
            }

            private double tensionFromOrigamiValue(float value) {
                if (value == 0.0f) {
                    return 0.0d;
                }
                return ((value - 30.0f) * 3.62f) + 194.0f;
            }

            public void setFriction(double friction) {
                this.mFriction = frictionFromOrigamiValue((float) friction);
            }

            public void setTension(double tension) {
                this.mTension = tensionFromOrigamiValue((float) tension);
            }
        }

        public static class Rk4Data {
            double mCurrentPosition;
            double mCurrentVelocity;
            double mTempPosition;
            double mTempVelocity;

            public Rk4Data(double currentPosition, double currentVelocity, double tempPosition, double tempVelocity) {
                this.mCurrentPosition = currentPosition;
                this.mCurrentVelocity = currentVelocity;
                this.mTempPosition = tempPosition;
                this.mTempVelocity = tempVelocity;
            }
        }

        public ReboundOverScroller() {
            float flingFriction = sCouiFlingFrictionNormal;
            this.mFlingFriction = flingFriction;
            this.mRestSpeedThreshold = 20.0d;
            this.mDisplacementFromRestThreshold = 0.05d;
            this.mCOUICount = 1;
            this.mIsScrollView = false;
            this.mSpringBackTensionMultiple = 0.83f;
            this.mFlingConfig = new ReboundConfig(flingFriction, 0.0d);
            this.mSpringBackConfig = new ReboundConfig(SPRING_BACK_FRICTION, SPRING_BACK_TENSION);
            setConfig(this.mFlingConfig);
            this.mFinished = true;
            int animLevel = UIUtil.getAnimLevel();
            if (animLevel == 2) {
                sMidFlingBaseFriction = 3.799999952316284d;
                sSlowFlingBaseFriction = 3.4000000953674316d;
            } else if (animLevel >= 3) {
                sMidFlingBaseFriction = 4.5d;
                sSlowFlingBaseFriction = 4.0d;
                sCouiFlingFrictionNormal = 0.24f;
            }
        }

        private void adjustFrictionByStartVelocity() {
            if (this.mIsSpringBack || this.mCOUICount != 1) {
                return;
            }
            if (Math.abs(this.mCurrentState.mVelocity) > MID_VELOCITY_ADJUST_FRICTION && Math.abs(this.mCurrentState.mVelocity) < MAX_VELOCITY_ADJUST_FRICTION) {
                this.mConfig.mFriction = sMidFlingBaseFriction;
            } else if (Math.abs(this.mCurrentState.mVelocity) <= MID_VELOCITY_ADJUST_FRICTION) {
                this.mConfig.mFriction = sSlowFlingBaseFriction;
            }
        }

        private void calculateCurStateWithInterpolator(float fraction) {
            float interpolation = this.mSpringInterpolator.getInterpolation(fraction);
            PhysicsState physicsState = this.mCurrentState;
            physicsState.mPosition = ((double) (this.mSimulateSplineDistance * interpolation)) + this.mStartValue;
            physicsState.mVelocity = ((this.mSpringInterpolator.getSpeed(fraction) * this.mSimulateSplineDistance) / this.mSimulateSplineDuration) * SpringOverScroller.ONE_SECOND;
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, " calculateCurStateWithInterpolator fraction:" + fraction + ", ratio: " + interpolation + ", position : " + this.mCurrentState.mPosition + ", mVelocity: " + this.mCurrentState.mVelocity);
            }
            this.mCOUICount++;
        }

        private int[] calculateFinalPosition(double position, boolean enabled) {
            PhysicsState physicsState = this.mCurrentState;
            int position2 = (int) physicsState.mPosition;
            int velocity = (int) physicsState.mVelocity;
            int cOUICount = this.mCOUICount;
            boolean finished = this.mFinished;
            PhysicsState physicsState2 = this.mTempState;
            physicsState2.mPosition = 0.0d;
            physicsState2.mVelocity = 0.0d;
            float frameIntervalNanos = getFrameIntervalNanos() / SpringOverScroller.NANO_TO_MILLIS;
            float fraction = SpringOverScroller.ONE_SECOND;
            float unused = SpringOverScroller.mRefreshTime = frameIntervalNanos / SpringOverScroller.ONE_SECOND;
            if (SpringOverScroller.mRefreshTime == 0.0f) {
                calculateRefreshTime();
            }
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, this + " calculateFinalPosition finalValue " + position + " savedPosition " + position2 + " savedVelocity " + velocity + ", mRefreshTime: " + SpringOverScroller.mRefreshTime);
            }
            boolean finished2 = true;
            this.mCOUICount = 1;
            boolean flag = false;
            while (!this.mFinished) {
                double positionD = this.mCurrentState.mPosition;
                if (enabled) {
                    calculateCurStateWithInterpolator(((this.mCOUICount * SpringOverScroller.mRefreshTime) * fraction) / this.mSimulateSplineDuration);
                } else {
                    calculateOnceWithRebound();
                }
                double tension = this.mCurrentState.mPosition;
                double dAbs = Math.abs(tension - positionD);
                if (lostVelocity()) {
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " calculateFinalPosition lostVelocity");
                    }
                    this.mFinished = finished2;
                }
                if (dAbs < this.mSplineMinDelta) {
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " calculateFinalPosition deltaPosition < " + this.mSplineMinDelta);
                    }
                    finished2 = true;
                    this.mFinished = true;
                } else {
                    finished2 = true;
                }
                if (position != -1.0d && !flag && (positionD - position) * (tension - position) <= 0.0d) {
                    this.mCurrentState.mPosition = position;
                    if (enabled) {
                        this.mDuration = (int) (this.mCOUICount * SpringOverScroller.mRefreshTime * SpringOverScroller.ONE_SECOND);
                    } else {
                        this.mSimulateDuration = (int) (this.mCOUICount * SpringOverScroller.mRefreshTime * SpringOverScroller.ONE_SECOND);
                    }
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " calculateFinalPosition reaching edge" + position);
                    }
                    flag = finished2;
                }
                fraction = SpringOverScroller.ONE_SECOND;
            }
            int index = (int) this.mCurrentState.mPosition;
            int count = (int) (this.mCOUICount * SpringOverScroller.mRefreshTime * SpringOverScroller.ONE_SECOND);
            PhysicsState physicsState3 = this.mCurrentState;
            physicsState3.mPosition = position2;
            physicsState3.mVelocity = velocity;
            this.mCOUICount = cOUICount;
            PhysicsState physicsState4 = this.mTempState;
            physicsState4.mPosition = 0.0d;
            physicsState4.mVelocity = 0.0d;
            this.mFinished = finished;
            return new int[]{index, count};
        }

        private void calculateOnceWithRebound() {
            adjustFrictionByStartVelocity();
            Rk4Data rk4Data = this.mRk4Result;
            PhysicsState physicsState = this.mCurrentState;
            rk4Data.mCurrentPosition = physicsState.mPosition;
            rk4Data.mCurrentVelocity = physicsState.mVelocity;
            rk4Data.mTempPosition = this.mTempState.mPosition;
            Rk4Data rk4DataCalculateWithRk4 = calculateWithRk4(rk4Data, this.mConfig, this.mEndValue, SpringOverScroller.mRefreshTime);
            this.mCOUICount++;
            PhysicsState physicsState2 = this.mCurrentState;
            physicsState2.mPosition = rk4DataCalculateWithRk4.mCurrentPosition;
            physicsState2.mVelocity = rk4DataCalculateWithRk4.mCurrentVelocity;
            PhysicsState physicsState3 = this.mTempState;
            physicsState3.mPosition = rk4DataCalculateWithRk4.mTempPosition;
            physicsState3.mVelocity = rk4DataCalculateWithRk4.mTempVelocity;
        }

        private Rk4Data calculateWithRk4(Rk4Data rk4Data, ReboundConfig reboundConfig, double positionD, float fraction) {
            double tension = rk4Data.mCurrentPosition;
            double friction = rk4Data.mCurrentVelocity;
            double velocityD = rk4Data.mTempPosition;
            double deltaD = reboundConfig.mTension;
            double valueD = reboundConfig.mFriction;
            double tempD = (positionD - velocityD) * deltaD;
            double endD = fraction;
            double minD = ((friction * endD) / MIN_FLING_FRICTION_REDUCE) + tension;
            double maxD = friction + ((tempD * endD) / MIN_FLING_FRICTION_REDUCE);
            double dx = ((positionD - minD) * deltaD) - (valueD * maxD);
            double dy = tension + ((maxD * endD) / MIN_FLING_FRICTION_REDUCE);
            double force = friction + ((dx * endD) / MIN_FLING_FRICTION_REDUCE);
            double accel = ((positionD - dy) * deltaD) - (valueD * force);
            double tempPosition = tension + (force * endD);
            double tempVelocity = friction + (accel * endD);
            double dt = (friction + ((maxD + force) * MIN_FLING_FRICTION_REDUCE) + tempVelocity) * 0.16699999570846558d;
            double sumD = (tempD + ((dx + accel) * MIN_FLING_FRICTION_REDUCE) + (((positionD - tempPosition) * deltaD) - (valueD * tempVelocity))) * 0.16699999570846558d;
            double currentPosition = tension + (dt * endD);
            double currentVelocity = friction + (sumD * endD);
            rk4Data.mCurrentPosition = currentPosition;
            rk4Data.mCurrentVelocity = currentVelocity;
            rk4Data.mTempPosition = tempPosition;
            rk4Data.mTempVelocity = tempVelocity;
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, " calculateOnceWithRebound, position : " + currentPosition + ", mVelocity: " + currentVelocity + ",tempPosition:" + tempPosition + ",tempVelocity:" + tempVelocity + ",tension:" + deltaD + ",friction:" + valueD + ",refreshTime:" + fraction);
            }
            return rk4Data;
        }

        private float getDistanceScale(float fraction) {
            float ratio;
            float scale = 1.0f;
            if (fraction <= FLING_VELOCITY_LOWEST) {
                return 1.0f;
            }
            if (fraction <= FLING_VELOCITY_LOW) {
                ratio = ((fraction - FLING_VELOCITY_LOWEST) / (float) MID_VELOCITY_ADJUST_FRICTION) * 0.19999999f;
            } else {
                scale = 0.8f;
                if (fraction <= 10000.0f) {
                    return 0.8f;
                }
                if (fraction > FLING_VELOCITY_HIGHEST) {
                    return DISTANCE_SCALE_MIN;
                }
                ratio = ((fraction - 10000.0f) / 10000.0f) * DISTANCE_SCALE_MIN;
            }
            return scale - ratio;
        }

        private float getDurationScale(float fraction) {
            float ratio;
            float scale;
            float x = DURATION_SCALE_MAX;
            if (fraction <= FLING_VELOCITY_LOWEST) {
                return DURATION_SCALE_MAX;
            }
            if (fraction <= FLING_VELOCITY_LOW) {
                ratio = (fraction - FLING_VELOCITY_LOWEST) / (float) MID_VELOCITY_ADJUST_FRICTION;
                scale = 0.49999994f;
            } else {
                x = 0.8f;
                if (fraction <= 10000.0f) {
                    return 0.8f;
                }
                if (fraction > FLING_VELOCITY_HIGHEST) {
                    return 0.5f;
                }
                ratio = (fraction - 10000.0f) / 10000.0f;
                scale = 0.3f;
            }
            return x - (ratio * scale);
        }

        private long getFrameIntervalNanos() {
            try {
                if (this.mChoreographer == null) {
                    this.mChoreographer = Choreographer.getInstance();
                }
                if (this.mGetFrameIntervalNanos == null) {
                    Method declaredMethod = Class.forName("android.view.Choreographer").getDeclaredMethod("getFrameIntervalNanos", new Class[0]);
                    this.mGetFrameIntervalNanos = declaredMethod;
                    declaredMethod.setAccessible(true);
                }
                return ((Long) this.mGetFrameIntervalNanos.invoke(this.mChoreographer, new Object[0])).longValue();
            } catch (Exception e) {
                if (!SpringOverScroller.DEBUG) {
                    return 0L;
                }
                Log.e(SpringOverScroller.TAG, "getFrameIntervalNanos error" + e);
                return 0L;
            }
        }

        private float getSplineMinVelocity() {
            return REST_SPEED_THRESHOLD;
        }

        private boolean lostVelocity() {
            if (Math.abs(this.mCurrentState.mVelocity) >= getSplineMinVelocity()) {
                return false;
            }
            if (!SpringOverScroller.DEBUG) {
                return true;
            }
            Log.d(SpringOverScroller.TAG, this + " lostVelocity");
            return true;
        }


        public void updateComputeTimeFromCallback(long timestamp) {
            this.mLastComputeTimeFromCallback = this.mCurrentComputeTimeFromCallback;
            this.mCurrentComputeTimeFromCallback = timestamp;
            this.mComputeTimeFromCallbackUpdated = true;
        }

        public void adjustSimulateSplineDistance(float fraction) {
            int simulateSplineDistance = (int) fraction;
            this.mSimulateSplineDistance = simulateSplineDistance;
            this.mEndValue = this.mStartValue + ((double) simulateSplineDistance);
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, "adjustSimulateSplineDistance: StartValue = " + this.mStartValue + " EndValue = " + this.mEndValue + " SimulateSplineDistance = " + this.mSimulateSplineDistance);
            }
        }

        public void calculateRefreshTime() {
            this.mCurrentComputeTime = SystemClock.elapsedRealtime();
            if (this.mComputeTimeFromCallbackUpdated) {
                this.mComputeTimeFromCallbackUpdated = false;
                if (SpringOverScroller.DEBUG) {
                    Log.d(SpringOverScroller.TAG, "update if: " + ((this.mCurrentComputeTimeFromCallback - this.mLastComputeTimeFromCallback) / SpringOverScroller.NANO_ONE_SECOND));
                }
                float unused = SpringOverScroller.mRefreshTime = Math.max(SpringOverScroller.MIN_FRAME_INTERVAL, (this.mCurrentComputeTimeFromCallback - this.mLastComputeTimeFromCallback) / SpringOverScroller.NANO_ONE_SECOND);
            } else {
                if (SpringOverScroller.DEBUG) {
                    Log.d(SpringOverScroller.TAG, "update else: " + ((this.mCurrentComputeTime - this.mLastComputeTime) / SpringOverScroller.ONE_SECOND));
                }
                float unused2 = SpringOverScroller.mRefreshTime = Math.max(SpringOverScroller.MIN_FRAME_INTERVAL, (this.mCurrentComputeTime - this.mLastComputeTime) / SpringOverScroller.ONE_SECOND);
            }
            if (SpringOverScroller.mRefreshTime > SpringOverScroller.ERROR_THRESHOLD) {
                if (SpringOverScroller.DEBUG) {
                    Log.d(SpringOverScroller.TAG, "update: error mRefreshTime = " + SpringOverScroller.mRefreshTime);
                }
                float unused3 = SpringOverScroller.mRefreshTime = SpringOverScroller.MIN_FRAME_INTERVAL;
            }
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, "update: mRefreshTime = " + SpringOverScroller.mRefreshTime + " mLastComputeTime = " + this.mLastComputeTime);
            }
            this.mLastComputeTime = this.mCurrentComputeTime;
        }

        public void fling(int start, int min, int max, int velocity, int over) {
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, this + " fling start " + start + " min " + min + " max " + max + " velocity " + velocity + " over " + over);
            }
            this.mSplineMinDelta = getSplineMinDelta(Math.abs(velocity));
            this.mFinished = false;
            initFling(start, velocity);
            this.mSplineDuration = 0;
            this.mDuration = 0;
            this.mSimulateSplineDuration = 0;
            this.mSimulateDuration = 0;
            this.mSpringInterpolator = new COUISpringInterpolator(DEFAULT_STIFFNESS, DEFAULT_DAMPINGRATIO, velocity, 1.0f, DEFAULT_VELOCITY_UNIT, true);
            initFling(start, velocity);
            float durationScale = getDurationScale(Math.abs(velocity));
            float distanceScale = getDistanceScale(Math.abs(velocity));
            double positionD = velocity >= 0 ? max : min;
            int[] iArrCalculateFinalPosition = calculateFinalPosition(positionD, false);
            int splineDistance = iArrCalculateFinalPosition[0] - start;
            int splineDuration = iArrCalculateFinalPosition[1];
            int simulateDuration = this.mSimulateDuration;
            if (simulateDuration == 0) {
                simulateDuration = splineDuration;
            }
            this.mSimulateDuration = simulateDuration;
            this.mSimulateSplineDistance = (int) (splineDistance * distanceScale);
            this.mSimulateSplineDuration = (int) (splineDuration * durationScale);
            int[] iArrCalculateFinalPosition2 = calculateFinalPosition(positionD, true);
            this.mSplineDistance = iArrCalculateFinalPosition2[0] - start;
            int splineDuration2 = iArrCalculateFinalPosition2[1];
            this.mSplineDuration = splineDuration2;
            int duration = this.mDuration;
            if (duration == 0) {
                duration = splineDuration2;
            }
            this.mDuration = duration;
            this.mWithSpring = splineDuration2 == duration;
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, this + " fling mStartTime " + this.mStartTime + " mStart " + this.mStartValue + " edge " + positionD + " distanceScale " + distanceScale + " durationScale " + durationScale + " mWithSpring " + this.mWithSpring + " [ Distance_old " + splineDistance + " Distance_new " + this.mSplineDistance + " ] [ SplineDuration_old " + splineDuration + " Duration_old " + this.mSimulateDuration + " SplineDuration_new " + this.mSplineDuration + " mSimulateSplineDistance " + this.mSimulateSplineDistance + " Duration_new " + this.mDuration + " ]");
            }
            if (this.mWithSpring) {
                this.mEndValue = Math.max(Math.min(iArrCalculateFinalPosition2[0], max), min);
                return;
            }
            this.mSplineDistance = splineDistance;
            this.mSplineDuration = splineDuration;
            this.mDuration = this.mSimulateDuration;
            this.mEndValue = Math.max(Math.min(iArrCalculateFinalPosition[0], max), min);
        }

        public double getCurrentValue() {
            return this.mCurrentState.mPosition;
        }

        public double getDisplacementDistanceForState(PhysicsState physicsState) {
            return Math.abs(this.mEndValue - physicsState.mPosition);
        }

        public double getDisplacementFromRestThreshold() {
            return this.mDisplacementFromRestThreshold;
        }

        public double getEndValue() {
            return this.mEndValue;
        }

        public double getRestSpeedThreshold() {
            return this.mRestSpeedThreshold;
        }

        public int getSimulateSplineDistance() {
            return this.mSimulateSplineDistance;
        }

        public double getSplineMinDelta(float velocity) {
            double positionD = velocity;
            return positionD <= DELTA_POSITION_VELOCITY_LOW ? DELTA_POSITION_LOW : positionD <= DELTA_POSITION_VELOCITY_HIGH ? DELTA_POSITION_MID : DELTA_POSITION_HIGH;
        }

        public double getVelocity() {
            return this.mCurrentState.mVelocity;
        }

        public void initFling(int index, int count) {
            long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = jCurrentAnimationTimeMillis;
            this.mLastFlingUpdateTime = jCurrentAnimationTimeMillis;
            this.mCOUICount = 1;
            sTimeIncrease = 1.0f;
            this.mFlingConfig.setFriction(this.mFlingFriction);
            this.mFlingConfig.setTension(0.0d);
            setConfig(this.mFlingConfig);
            setCurrentValue(index, true);
            setVelocity(count);
            long jElapsedRealtime = SystemClock.elapsedRealtime();
            this.mLastComputeTime = jElapsedRealtime;
            this.mCurrentComputeTime = jElapsedRealtime;
        }

        public boolean isAtRest() {
            return Math.abs(this.mCurrentState.mVelocity) <= getRestSpeedThreshold() && (getDisplacementDistanceForState(this.mCurrentState) <= getDisplacementFromRestThreshold() || this.mConfig.mTension == 0.0d);
        }

        public void notifyEdgeReached(int position, int position2, int index) {
            PhysicsState physicsState = this.mCurrentState;
            physicsState.mPosition = position;
            PhysicsState physicsState2 = this.mPreviousState;
            physicsState2.mPosition = 0.0d;
            physicsState2.mVelocity = 0.0d;
            PhysicsState physicsState3 = this.mTempState;
            physicsState3.mPosition = position2;
            physicsState3.mVelocity = physicsState.mVelocity;
        }

        public void setAtRest() {
            PhysicsState physicsState = this.mCurrentState;
            double endValue = physicsState.mPosition;
            this.mEndValue = endValue;
            this.mTempState.mPosition = endValue;
            physicsState.mVelocity = 0.0d;
            this.mIsSpringBack = false;
            this.mCancelCallback = true;
        }

        public void setConfig(ReboundConfig reboundConfig) {
            if (reboundConfig == null) {
                throw new IllegalArgumentException("springConfig is required");
            }
            this.mConfig = reboundConfig;
        }

        public void setCurrentValue(double startValue, boolean enabled) {
            this.mStartValue = startValue;
            if (!this.mIsScrollView) {
                this.mPreviousState.mPosition = 0.0d;
                this.mTempState.mPosition = 0.0d;
            }
            this.mCurrentState.mPosition = startValue;
            if (enabled) {
                setAtRest();
            }
        }

        public void setEndValue(double endValue) {
            if (this.mEndValue == endValue) {
                return;
            }
            this.mStartValue = getCurrentValue();
            this.mEndValue = endValue;
            this.mFinished = false;
        }

        public void setVelocity(double velocity) {
            if (Math.abs(velocity - this.mCurrentState.mVelocity) < 1.0000000116860974E-7d) {
                return;
            }
            this.mCurrentState.mVelocity = velocity;
        }

        public boolean springBack(int index, int count, int value, boolean enabled) {
            double positionD = index;
            setCurrentValue(positionD, false);
            long jElapsedRealtime = SystemClock.elapsedRealtime();
            this.mLastComputeTime = jElapsedRealtime;
            this.mCurrentComputeTime = jElapsedRealtime;
            if (index <= value && index >= count && !enabled) {
                setConfig(new ReboundConfig(this.mFlingFriction, 0.0d));
                return false;
            }
            if (index > value) {
                setEndValue(value);
            } else if (index < count) {
                setEndValue(count);
            } else if (enabled) {
                setEndValue(positionD);
            }
            this.mIsSpringBack = true;
            this.mSpringBackConfig.setFriction(SpringOverScroller.mSpringBackFriction);
            this.mSpringBackConfig.setTension(this.mSpringBackTensionMultiple * SPRING_BACK_TENSION);
            setConfig(this.mSpringBackConfig);
            return true;
        }

        public void startScroll(int scrollStart, int index, int duration, long timestamp) {
            this.mScrollStart = scrollStart;
            int scrollFinal = scrollStart + index;
            this.mScrollFinal = scrollFinal;
            this.mEndValue = scrollFinal;
            this.mDuration = duration;
            this.mScrollStartTime = timestamp;
            setConfig(this.mFlingConfig);
            long jElapsedRealtime = SystemClock.elapsedRealtime();
            this.mLastComputeTime = jElapsedRealtime;
            this.mCurrentComputeTime = jElapsedRealtime;
        }

        public boolean update() {
            calculateRefreshTime();
            if (this.mIsSpringBack) {
                if (isAtRest()) {
                    return false;
                }
                PhysicsState physicsState = this.mCurrentState;
                double positionD = physicsState.mPosition;
                double displacementDistanceForState = getDisplacementDistanceForState(physicsState);
                if (!this.mTensionAdjusted && displacementDistanceForState < 180.0d) {
                    this.mTensionAdjusted = true;
                } else if (displacementDistanceForState < SPRING_BACK_STOP_THRESHOLD) {
                    this.mCurrentState.mPosition = this.mEndValue;
                    this.mTensionAdjusted = false;
                    this.mIsSpringBack = false;
                    this.mCancelCallback = true;
                    return false;
                }
                do {
                    Rk4Data rk4Data = this.mRk4Result;
                    PhysicsState physicsState2 = this.mCurrentState;
                    rk4Data.mCurrentPosition = physicsState2.mPosition;
                    rk4Data.mCurrentVelocity = physicsState2.mVelocity;
                    rk4Data.mTempPosition = this.mTempState.mPosition;
                    Rk4Data rk4DataCalculateWithRk4 = calculateWithRk4(rk4Data, this.mConfig, this.mEndValue, SpringOverScroller.mRefreshTime);
                    PhysicsState physicsState3 = this.mTempState;
                    physicsState3.mVelocity = rk4DataCalculateWithRk4.mTempVelocity;
                    physicsState3.mPosition = rk4DataCalculateWithRk4.mTempPosition;
                    PhysicsState physicsState4 = this.mCurrentState;
                    physicsState4.mVelocity = rk4DataCalculateWithRk4.mCurrentVelocity;
                    double position = rk4DataCalculateWithRk4.mCurrentPosition;
                    physicsState4.mPosition = position;
                    if (Math.abs(positionD - position) > SpringOverScroller.MIN_UPDATE_ONE_STEP || !this.mIsSpringBack) {
                        break;
                    }
                } while (!isAtRest());
                this.mCOUICount++;
            } else {
                if (isAtRest()) {
                    this.mCancelCallback = true;
                    return false;
                }
                long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
                float frameIntervalNanos = getFrameIntervalNanos();
                if (frameIntervalNanos != 0.0f) {
                    float unused = SpringOverScroller.mRefreshTime = Math.min((frameIntervalNanos / SpringOverScroller.ONE_SECOND) / SpringOverScroller.NANO_TO_MILLIS, (jCurrentAnimationTimeMillis - this.mLastFlingUpdateTime) / SpringOverScroller.ONE_SECOND);
                }
                this.mLastFlingUpdateTime = jCurrentAnimationTimeMillis;
                double tension = this.mCurrentState.mPosition;
                if (!this.mWithSpring || this.mIsSpringBack) {
                    calculateOnceWithRebound();
                } else {
                    if (this.mSimulateSplineDuration <= 0) {
                        if (SpringOverScroller.DEBUG) {
                            Log.d(SpringOverScroller.TAG, this + " update end : SPLINE OSpring error duration");
                        }
                        return false;
                    }
                    calculateCurStateWithInterpolator(Math.max(jCurrentAnimationTimeMillis - this.mStartTime, 0.0f) / this.mSimulateSplineDuration);
                }
                double friction = this.mCurrentState.mPosition;
                double dAbs = Math.abs(friction - tension);
                if (!this.mWithSpring && dAbs < this.mSplineMinDelta && SpringOverScroller.mRefreshTime != 0.0f) {
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " update end : deltaPosition < " + DELTA_POSITION_LOW);
                    }
                    return false;
                }
                if (this.mWithSpring && lostVelocity()) {
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " update end : lostVelocity when BALLISTIC (or only SPLINE)");
                    }
                    return false;
                }
                double position2 = this.mEndValue;
                if ((tension - position2) * (friction - position2) <= 0.0d) {
                    this.mCurrentState.mPosition = position2;
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " update end : reaching final " + this.mEndValue);
                    }
                    return false;
                }
                if (Double.isNaN(this.mCurrentState.mVelocity) || Double.isNaN(this.mCurrentState.mPosition)) {
                    if (SpringOverScroller.DEBUG) {
                        Log.d(SpringOverScroller.TAG, this + " update end : mVelocity or mPosition NaN ");
                    }
                    return false;
                }
            }
            if (SpringOverScroller.DEBUG) {
                Log.d(SpringOverScroller.TAG, this + " <<< FLING_MODE: update mSplineDuration:" + this.mSplineDuration + " ,elapsedInternalTime:" + (this.mLastFlingUpdateTime - this.mStartTime) + " ,mFinal:" + this.mEndValue + " ,position:" + this.mCurrentState.mPosition + " ,velocity:" + this.mCurrentState.mVelocity + " ,tension: " + this.mConfig.mTension + " ,friction: " + this.mConfig.mFriction + " ,mOplusCount:" + this.mCOUICount + " >>> ");
            }
            return true;
        }

        public void updateScroll(float fraction) {
            PhysicsState physicsState = this.mCurrentState;
            int position = this.mScrollStart;
            physicsState.mPosition = position + Math.round(fraction * (this.mScrollFinal - position));
        }
    }

    static {
        DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public SpringOverScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
        this.mMode = 2;
        this.mEnableFlingSpeedIncrease = true;
        this.mLastFlingSpeedIncreaseRate = 1.0f;
        this.mIsUpdateTimeFromCallback = false;
        this.mCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long currentComputeTimeFromCallback) {
                ReboundOverScroller reboundOverScroller = SpringOverScroller.this.mScrollerX;
                if (reboundOverScroller != null) {
                    reboundOverScroller.updateComputeTimeFromCallback(currentComputeTimeFromCallback);
                }
                ReboundOverScroller reboundOverScroller2 = SpringOverScroller.this.mScrollerY;
                if (reboundOverScroller2 != null) {
                    reboundOverScroller2.updateComputeTimeFromCallback(currentComputeTimeFromCallback);
                }
                SpringOverScroller springOverScroller = SpringOverScroller.this;
                springOverScroller.mLastComputeTimeFromCallback = springOverScroller.mCurrentComputeTimeFromCallback;
                SpringOverScroller.this.mCurrentComputeTimeFromCallback = currentComputeTimeFromCallback;
                SpringOverScroller.this.mIsUpdateTimeFromCallback = true;
                if (SpringOverScroller.this.mCancelCallback) {
                    return;
                }
                Choreographer.getInstance().postFrameCallback(this);
            }
        };
        this.mScrollerX = new ReboundOverScroller();
        this.mScrollerY = new ReboundOverScroller();
        if (interpolator == null) {
            this.mInterpolator = new COUIViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
        setRefreshRateUnConvert(SOLVER_TIMESTEP_SEC);
        this.mContext = context;
        this.mFrameRateHelper = new COUlFrameRateScrollSceneHelper(false);
    }

    private int increaseVelocityIfNeed(int index) {
        if (!this.mEnableFlingSpeedIncrease) {
            return index;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        int continuousFlingCount = this.mContinuousFlingCount;
        if (continuousFlingCount <= 0) {
            if (continuousFlingCount != 0) {
                return index;
            }
            this.mContinuousFlingCount = continuousFlingCount + 1;
            this.mLastFlingTime = jCurrentTimeMillis;
            return index;
        }
        if (jCurrentTimeMillis - this.mLastFlingTime > FLING_SPEED_INCREASE_TIME_INTERVAL_THRESHOLD || index < FLING_SPEED_INCREASE_VELOCITY_THRESHOLD) {
            resetFlingSpeedValue();
            return index;
        }
        this.mLastFlingTime = jCurrentTimeMillis;
        int continuousFlingCount2 = continuousFlingCount + 1;
        this.mContinuousFlingCount = continuousFlingCount2;
        if (continuousFlingCount2 <= FLING_SPEED_INCREASE_COUNT_THRESHOLD) {
            return index;
        }
        float lastFlingSpeedIncreaseRate = this.mLastFlingSpeedIncreaseRate * FLING_SPEED_INCREASE_RATE;
        this.mLastFlingSpeedIncreaseRate = lastFlingSpeedIncreaseRate;
        return Math.max(-70000, Math.min((int) (index * lastFlingSpeedIncreaseRate), FLING_SPEED_INCREASE_MAX_VELOCITY));
    }

    private void limitEdgeReachedVelocityIfNeed(ReboundOverScroller reboundOverScroller) {
        if (!this.mEnableFlingSpeedIncrease || this.mContinuousFlingCount <= FLING_SPEED_INCREASE_COUNT_THRESHOLD) {
            return;
        }
        ReboundOverScroller.PhysicsState physicsState = reboundOverScroller.mCurrentState;
        double positionD = physicsState.mVelocity;
        if (positionD > FLING_SPEED_INCREASE_EDGE_REACHED_VELOCITY_THRESHOLD) {
            physicsState.mVelocity = 1000.0d;
        } else if (positionD < -FLING_SPEED_INCREASE_EDGE_REACHED_VELOCITY_THRESHOLD) {
            physicsState.mVelocity = -1000.0d;
        }
    }

    private void resetFlingSpeedValue() {
        this.mLastFlingTime = 0L;
        this.mContinuousFlingCount = 0;
        this.mLastFlingSpeedIncreaseRate = 1.0f;
    }

    private void setRefreshRateUnConvert(float refreshTime) {
        mRefreshTime = refreshTime;
    }

    private static synchronized void setStaticSpringBackFriction(float springBackFriction) {
        mSpringBackFriction = springBackFriction;
    }

    @Override
    public void abortAnimation() {
        if (DEBUG) {
            Log.d(TAG, "abortAnimation", new Throwable());
        }
        this.mMode = 2;
        this.mScrollerX.setAtRest();
        this.mScrollerY.setAtRest();
        this.mCancelCallback = true;
        this.mFrameRateHelper.setFrameRate(false);
    }

    public void cancelCallback() {
        this.mCancelCallback = true;
    }

    @Override
    public boolean computeScrollOffset() {
        if (isCOUIFinished()) {
            this.mCancelCallback = this.mScrollerX.mCancelCallback && this.mScrollerY.mCancelCallback;
            return false;
        }
        int index = this.mMode;
        if (index == 0) {
            long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mScrollStartTime;
            int count = this.mScrollerX.mDuration;
            if (jCurrentAnimationTimeMillis < count) {
                float interpolation = this.mInterpolator.getInterpolation(jCurrentAnimationTimeMillis / count);
                this.mScrollerX.updateScroll(interpolation);
                this.mScrollerY.updateScroll(interpolation);
            } else {
                this.mScrollerX.updateScroll(1.0f);
                this.mScrollerY.updateScroll(1.0f);
                abortAnimation();
            }
        } else if (index == 1 && !this.mScrollerX.update() && !this.mScrollerY.update()) {
            abortAnimation();
        }
        return true;
    }

    public void enableFrameRate(boolean enabled) {
        this.mFrameRateHelper.enableFrameRate(enabled);
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    @Override
    public final int getCOUICurrX() {
        return (int) Math.round(this.mScrollerX.getCurrentValue());
    }

    @Override
    public final int getCOUICurrY() {
        return (int) Math.round(this.mScrollerY.getCurrentValue());
    }

    @Override
    public final int getCOUIFinalX() {
        return (int) this.mScrollerX.getEndValue();
    }

    @Override
    public final int getCOUIFinalY() {
        return (int) this.mScrollerY.getEndValue();
    }

    @Override
    public float getCurrVelocity() {
        double velocity = this.mScrollerX.getVelocity();
        double velocity2 = this.mScrollerY.getVelocity();
        return (int) Math.sqrt((velocity * velocity) + (velocity2 * velocity2));
    }

    @Override
    public float getCurrVelocityX() {
        return (float) this.mScrollerX.getVelocity();
    }

    @Override
    public float getCurrVelocityY() {
        return (float) this.mScrollerY.getVelocity();
    }

    @Override
    public final boolean isCOUIFinished() {
        boolean zIsAtRest = this.mScrollerX.isAtRest();
        boolean zIsAtRest2 = this.mScrollerY.isAtRest();
        if (DEBUG) {
            Log.d(TAG, "scrollX is rest: " + this.mScrollerX.isAtRest() + "  scrollY is rest: " + this.mScrollerY.isAtRest() + "  mMode = " + this.mMode);
        }
        return zIsAtRest && zIsAtRest2 && this.mMode != 0;
    }

    public boolean isEnableFlingSpeedIncrease() {
        return this.mEnableFlingSpeedIncrease;
    }

    @Override
    public boolean isScrollingInDirection(float fraction, float ratio) {
        return !isFinished() && Math.signum(fraction) == Math.signum((float) ((int) (this.mScrollerX.mEndValue - this.mScrollerX.mStartValue))) && Math.signum(ratio) == Math.signum((float) ((int) (this.mScrollerY.mEndValue - this.mScrollerY.mStartValue)));
    }

    @Override
    public void notifyHorizontalEdgeReached(int index, int count, int value) {
        this.mScrollerX.notifyEdgeReached(index, count, value);
        springBack(index, 0, 0, count, 0, 0);
    }

    @Override
    public void notifyVerticalEdgeReached(int index, int count, int value) {
        this.mScrollerY.notifyEdgeReached(index, count, value);
        springBack(0, index, 0, 0, 0, count);
    }

    public void postChoreographerCallback() {
        if (DEBUG) {
            Log.d(TAG, "postChoreographerCallback: post Callback");
        }
        Choreographer.getInstance().postFrameCallback(this.mCallback);
    }

    public void removeChoreographerCallback() {
        if (DEBUG) {
            Log.d(TAG, "removeChoreographerCallback: remove Callback");
        }
        Choreographer.getInstance().removeFrameCallback(this.mCallback);
    }

    @Override
    public void setCOUIFriction(float fraction) {
    }

    @Override
    public void setCurrVelocityX(float velocity) {
        this.mScrollerX.mCurrentState.mVelocity = velocity;
    }

    @Override
    public void setCurrVelocityY(float velocity) {
        this.mScrollerY.mCurrentState.mVelocity = velocity;
    }

    public void setDebug(boolean enabled) {
        DEBUG = enabled;
    }

    @Override
    public void setDurationRatio(float fraction) {
    }

    public void setEnableFlingSpeedIncrease(boolean enableFlingSpeedIncrease) {
        if (this.mEnableFlingSpeedIncrease == enableFlingSpeedIncrease) {
            return;
        }
        this.mEnableFlingSpeedIncrease = enableFlingSpeedIncrease;
        resetFlingSpeedValue();
    }

    @Override
    public void setFinalX(int index) {
    }

    @Override
    public void setFinalY(int index) {
    }

    @Override
    public void setFlingFriction(float flingFriction) {
        this.mScrollerX.mFlingFriction = flingFriction;
        this.mScrollerY.mFlingFriction = flingFriction;
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            this.mInterpolator = new COUIViscousFluidInterpolator();
        } else {
            this.mInterpolator = interpolator;
        }
    }

    @Override
    public void setIsScrollView(boolean isScrollView) {
        this.mScrollerX.mIsScrollView = isScrollView;
        this.mScrollerY.mIsScrollView = isScrollView;
    }

    public void setRefreshRate(float fraction) {
        mRefreshTime = Math.round(10000.0f / fraction) / 10000.0f;
    }

    public void setSpringBackFriction(float fraction) {
        setStaticSpringBackFriction(fraction);
    }

    public void setSpringBackTensionMultiple(float springBackTensionMultiple) {
        this.mScrollerX.mSpringBackTensionMultiple = springBackTensionMultiple;
        this.mScrollerY.mSpringBackTensionMultiple = springBackTensionMultiple;
    }

    @Override
    public void setVelocityXRatio(float fraction) {
    }

    @Override
    public void setVelocityYRatio(float fraction) {
    }

    @Override
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        if (DEBUG) {
            Log.d(TAG, "springBack startX = " + startX + " startY = " + startY + " minX = " + minX + " minY = " + minY + " maxY = " + maxY, new Throwable());
        }
        boolean zSpringBack = this.mScrollerX.springBack(startX, minX, maxX, false);
        boolean zSpringBack2 = this.mScrollerY.springBack(startY, minY, maxY, false);
        if (zSpringBack || zSpringBack2) {
            this.mMode = 1;
        }
        return zSpringBack || zSpringBack2;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, SCROLL_DEFAULT_DURATION);
    }

    public void triggerCallback() {
        removeChoreographerCallback();
        postChoreographerCallback();
        this.mCancelCallback = false;
        this.mScrollerX.mCancelCallback = false;
        this.mScrollerY.mCancelCallback = false;
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if (DEBUG) {
            Log.d(TAG, "startScroll startX = " + startX + " startY = " + startY + " dx = " + dx + " dy = " + dy + " duration = " + duration, new Throwable());
        }
        this.mMode = 0;
        long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mScrollerX.startScroll(startX, dx, duration, jCurrentAnimationTimeMillis);
        this.mScrollerY.startScroll(startY, dy, duration, jCurrentAnimationTimeMillis);
        this.mFrameRateHelper.setFrameRate(true);
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY) {
        if (DEBUG) {
            Log.d(TAG, "fling startX = " + startX + " startY = " + startY + " velocityX = " + velocityX + " velocityY = " + velocityY, new Throwable());
        }
        this.mMode = 1;
        this.mScrollerX.fling(startX, Integer.MIN_VALUE, Integer.MAX_VALUE, increaseVelocityIfNeed(velocityX), 0);
        this.mScrollerY.fling(startY, Integer.MIN_VALUE, Integer.MAX_VALUE, increaseVelocityIfNeed(velocityY), 0);
        this.mFrameRateHelper.setFrameRate(true);
    }

    public SpringOverScroller(Context context) {
        this(context, null);
    }
}
