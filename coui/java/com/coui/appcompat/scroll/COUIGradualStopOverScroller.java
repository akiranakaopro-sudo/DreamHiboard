package com.coui.appcompat.scroll;

import android.content.Context;
import android.util.Log;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.scroll.SpringOverScroller;


public class COUIGradualStopOverScroller extends SpringOverScroller {
    public static final boolean DEBUG;
    public static final String TAG = "GradualStopOverScroller";
    private COUIGradualStopHelper mGradualStopHelper;
    private final int mMaxFlingDistance;

    public static class GradualStopReboundOverScroller extends SpringOverScroller.ReboundOverScroller {
        private static final double DELTA_POSITION_MID = 1.0d;
        private COUIGradualStopHelper mGradualStopHelper;
        private double mRestSpeedThreshold = 5.0d;
        private double mDisplacementFromRestThreshold = 0.05d;

        public GradualStopReboundOverScroller(COUIGradualStopHelper gradualStopHelper) {
            this.mGradualStopHelper = gradualStopHelper;
        }

        private void adjustSplineDistance(int velocity) {
            int simulateSplineDistance = getSimulateSplineDistance();
            if (simulateSplineDistance == 0) {
                return;
            }
            float centerToEdgeOffsetInVelocityDirection = this.mGradualStopHelper.getCenterToEdgeOffsetInVelocityDirection(velocity);
            boolean debug = COUIGradualStopOverScroller.DEBUG;
            if (debug) {
                Log.d(COUIGradualStopOverScroller.TAG, this + "[ simulateSplineDistance = " + simulateSplineDistance + " edgeDistance = " + centerToEdgeOffsetInVelocityDirection + " ]");
            }
            if (centerToEdgeOffsetInVelocityDirection != 0.0f && Math.abs(simulateSplineDistance) <= Math.abs(centerToEdgeOffsetInVelocityDirection)) {
                adjustSimulateSplineDistance(0.0f);
                springBack();
                return;
            }
            float displacementToAlignCenter = this.mGradualStopHelper.getDisplacementToAlignCenter(velocity, simulateSplineDistance);
            if (debug) {
                Log.d(COUIGradualStopOverScroller.TAG, this + "[ adaptDistance = " + displacementToAlignCenter + " ]");
            }
            if (displacementToAlignCenter != 0.0f) {
                adjustSimulateSplineDistance(displacementToAlignCenter);
            }
        }

        @Override
        public void fling(int start, int min, int max, int velocity, int over) {
            super.fling(start, min, max, velocity, over);
            adjustSplineDistance(velocity);
        }

        @Override
        public double getDisplacementFromRestThreshold() {
            return this.mDisplacementFromRestThreshold;
        }

        @Override
        public double getRestSpeedThreshold() {
            return this.mRestSpeedThreshold;
        }

        @Override
        public double getSplineMinDelta(float unused) {
            return DELTA_POSITION_MID;
        }

        public boolean springBack() {
            int centerToItemCenterOffsetUnderCenter = (int) this.mGradualStopHelper.getCenterToItemCenterOffsetUnderCenter();
            if (COUIGradualStopOverScroller.DEBUG) {
                Log.d(COUIGradualStopOverScroller.TAG, this + " childCenterDiff " + centerToItemCenterOffsetUnderCenter + " ]");
            }
            return springBack(0, centerToItemCenterOffsetUnderCenter, centerToItemCenterOffsetUnderCenter, true);
        }
    }

    static {
        DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public COUIGradualStopOverScroller(Context context, COUIGradualStopHelper gradualStopHelper) {
        super(context, null);
        this.mMaxFlingDistance = 10000;
        this.mGradualStopHelper = gradualStopHelper;
        this.mScrollerX = new GradualStopReboundOverScroller(gradualStopHelper);
        this.mScrollerY = new GradualStopReboundOverScroller(gradualStopHelper);
    }

    public double getMaxFlingDistance() {
        return 10000.0d;
    }

    public boolean springBackToCenter(int orientation) {
        int startY;
        int minY;
        int startX;
        int minX;
        int centerToItemCenterOffsetUnderCenter = (int) this.mGradualStopHelper.getCenterToItemCenterOffsetUnderCenter();
        if (centerToItemCenterOffsetUnderCenter == 0) {
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, this + " childCenterDiff " + centerToItemCenterOffsetUnderCenter + " ]");
        }
        if (orientation == 0) {
            startX = centerToItemCenterOffsetUnderCenter;
            minX = startX;
            startY = 0;
            minY = 0;
        } else {
            startY = centerToItemCenterOffsetUnderCenter;
            minY = startY;
            startX = 0;
            minX = 0;
        }
        springBack(0, 0, startX, minX, startY, minY);
        return true;
    }
}
