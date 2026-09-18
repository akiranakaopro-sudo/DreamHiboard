package com.coui.appcompat.dialog;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class AppFeatureUtil {
    public static final float DENSITY_160F = 160.0f;
    public static final float DENSITY_360F = 360.0f;
    private static final String FEATURE_FOLD = "oplus.hardware.type.fold";
    private static final String FOLD_MODE_NAME = "oplus_system_folding_mode";
    private static final String TAG = "AppFeatureUtil";

    public static boolean isFlipDisplayFeature(Context context) {
        return false;
    }

    public static boolean isFoldDisplayFeature(Context context) {
        if (context == null) {
            return false;
        }
        try {
            return context.getPackageManager().hasSystemFeature(FEATURE_FOLD);
        } catch (Error | Exception e) {
            Log.d(TAG, "Load feature_fold failed : " + e.getMessage());
            return false;
        }
    }

    public static boolean isFoldLargeScreen(Context context) {
        return context != null && Settings.Global.getInt(context.getContentResolver(), FOLD_MODE_NAME, 0) == 1;
    }

    public static boolean isFoldSmallScreen(Context context) {
        return context != null && Settings.Global.getInt(context.getContentResolver(), FOLD_MODE_NAME, 0) == 0;
    }

    public static boolean isSecondaryScreen(Context context) {
        return isFoldDisplayFeature(context) && isFoldSmallScreen(context);
    }

    public static boolean isSeparateWallpaperForMultiDisplay(Context context) {
        return false;
    }
}
