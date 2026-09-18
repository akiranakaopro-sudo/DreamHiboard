package com.coui.appcompat.animation;

public class COUIPhysicalAnimationUtil {
    private static final float FLOAT_NUM_ONE = 1.0f;
    private static final float INT_NUM_FIVE = 5.0f;
    private static final float INT_NUM_TWO = 2.0f;
    private static final float OVERFLING_MAX_DISTANCE_SCREEN_FACTOR = 0.3731f;

    public static int calcOverFlingDecelerateDist(int current, int velocity, int screenSize) {
        float maxDistance = screenSize * OVERFLING_MAX_DISTANCE_SCREEN_FACTOR;
        double numerator = velocity * maxDistance;
        float velocitySquared = velocity * velocity;
        float distanceSquared = maxDistance * maxDistance;
        return current + (int) (numerator / Math.sqrt(velocitySquared + distanceSquared));
    }

    public static int calcRealOverScrollDist(int delta, int currentOverScroll, int maxOverScroll) {
        float ratio = (Math.abs(currentOverScroll) * FLOAT_NUM_ONE) / maxOverScroll;
        return (int) (((delta * (FLOAT_NUM_ONE - Math.min(ratio, FLOAT_NUM_ONE)))
                / INT_NUM_FIVE) * INT_NUM_TWO);
    }
}
