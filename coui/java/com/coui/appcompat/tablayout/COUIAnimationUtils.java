package com.coui.appcompat.tablayout;

import android.content.Context;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import com.coui.appcompat.animation.COUILinearInterpolator;


public class COUIAnimationUtils {
    private static final int SCALE_STANDARD = 100;
    public static final Interpolator LINEAR_INTERPOLATOR = new COUILinearInterpolator();
    public static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    public static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();
    public static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public static int getScaleStandard(Context context) {
        return (int) (context.getResources().getDisplayMetrics().density * 100.0f);
    }

    public static float lerp(float start, float end, float fraction) {
        return start + (fraction * (end - start));
    }

    public static int lerp(int start, int end, float fraction) {
        return start + Math.round(fraction * (end - start));
    }
}






