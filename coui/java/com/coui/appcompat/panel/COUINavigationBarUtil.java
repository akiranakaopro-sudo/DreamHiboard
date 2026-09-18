package com.coui.appcompat.panel;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


public class COUINavigationBarUtil {
    private static final String ANDROID_RESOURCE_PACKAGE = "android";
    private static final String BOOL_RESOURCE_TYPE = "bool";
    private static final String INTEGER_RESOURCE_TYPE = "integer";
    private static final String CONFIG_NAV_BAR_INTERACTION_MODE = "config_navBarInteractionMode";
    private static final String CONFIG_SHOW_NAVIGATION_BAR = "config_showNavigationBar";
    private static final String HIDE_NAVIGATION_BAR_ENABLE = "hide_navigationbar_enable";
    private static final String MANUAL_HIDE_NAVIGATION_BAR = "manual_hide_navigationbar";
    private static final String NAVIGATION_BAR_HEIGHT_RESOURCE_TYPE = "dimen";
    private static final String NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String NAVIGATION_BAR_IMMERSIVE = "nav_bar_immersive";
    private static final Uri NAVIGATION_BAR_IMMERSIVE_URI = Settings.Secure.getUriFor(NAVIGATION_BAR_IMMERSIVE);
    private static final int NAV_STATE_SWIPE_SIDE_GESTURE = 3;
    private static final int NAV_STATE_SWIPE_UP_GESTURE = 2;
    private static final int NAV_STATE_VIRTUAL_KEY = 0;
    private static final int NAV_STATE_VIRTUAL_KEY_AND_HIDE = 1;
    private static final int AOSP_NAV_STATE_GESTURAL = 2;
    private static final String QEMU_HW_MAINKEYS = "qemu.hw.mainkeys";
    private static final String SYSTEM_PROPERTIES_CLASS = "android.os.SystemProperties";
    private static final String SYSTEM_PROPERTIES_GET_METHOD = "get";
    private static final String TAG = "NavigationBarUtils";
    private static final String WINDOW_SERVICE = "window";

    public interface NavigationBarChangeListener {
        void onNavigationBarChange(boolean isNavigationBarVisible);
    }

    public static class NavigationBarContentObserver extends ContentObserver {
        private Context mContext;
        private NavigationBarChangeListener mListener;

        public NavigationBarContentObserver(Context context, NavigationBarChangeListener navigationBarChangeListener) {
            super(null);
            this.mContext = context;
            this.mListener = navigationBarChangeListener;
        }

        public void clearListener() {
            if (this.mListener != null) {
                this.mListener = null;
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Context context;
            super.onChange(selfChange, uri);
            if (COUINavigationBarUtil.NAVIGATION_BAR_IMMERSIVE_URI == null || !COUINavigationBarUtil.NAVIGATION_BAR_IMMERSIVE_URI.equals(uri) || (context = this.mContext) == null || this.mListener == null) {
                return;
            }
            this.mListener.onNavigationBarChange(COUINavigationBarUtil.isNavigationBarVisibleInImmersiveMode(context));
        }
    }

    public static int getNavigationBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        Resources resources = context.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(resources.getIdentifier(NAVIGATION_BAR_HEIGHT, NAVIGATION_BAR_HEIGHT_RESOURCE_TYPE, ANDROID_RESOURCE_PACKAGE));
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(realDisplayMetrics);
        float realDensity = realDisplayMetrics.density;
        float displayDensity = displayMetrics.density;
        if (realDensity == displayDensity) {
            return dimensionPixelSize;
        }
        return (int) ((dimensionPixelSize * (realDensity / displayDensity)) + 0.5f);
    }

    public static boolean isGestureNavigation(Context context) {
        if (context == null) {
            return false;
        }
        /*
         * Leapy modified 2026-07-30: BEGIN preserve the decoded COUI gesture
         * branch while sourcing its state from D960's Android framework.
         *
         * ColorOS publishes hide_navigationbar_enable=3. D960 does not publish
         * that vendor key; its authoritative navigation overlay is
         * config_navBarInteractionMode (0=three button, 2=gestural), as used by
         * MtkSettings' SystemNavigationPreferenceController integration.
         * Reading the absent ColorOS key as zero made COUI apply non-gesture
         * system-bar insets and left the bottom-sheet spring travelling through
         * an additional invisible status/navigation-bar distance before hidden.
         */
        Resources resources = context.getResources();
        int identifier = resources.getIdentifier(
                CONFIG_NAV_BAR_INTERACTION_MODE,
                INTEGER_RESOURCE_TYPE,
                ANDROID_RESOURCE_PACKAGE);
        if (identifier > 0) {
            return resources.getInteger(identifier) == AOSP_NAV_STATE_GESTURAL;
        }
        return getNavigationState(context) == NAV_STATE_SWIPE_SIDE_GESTURE;
        // Leapy end 2026-07-30: keep COUI panel geometry aligned with D960 navigation mode.
    }

    public static boolean isNavigationBarShow(Context context) {
        if (!isSupportNavigationBar(context)) {
            return false;
        }
        int navigationState = getNavigationState(context);
        return navigationState == NAV_STATE_VIRTUAL_KEY || (navigationState == NAV_STATE_VIRTUAL_KEY_AND_HIDE && getManualHideNavigationBar(context) == 0) || Build.VERSION.SDK_INT > 30;
    }

    public static boolean isSupportNavigationBar(Context context) {
        boolean isNavigationBarSupported = false;
        if (context == null) {
            return false;
        }
        Resources resources = context.getResources();
        int identifier = resources.getIdentifier(CONFIG_SHOW_NAVIGATION_BAR, BOOL_RESOURCE_TYPE, ANDROID_RESOURCE_PACKAGE);
        boolean configShowNavigationBar = identifier > 0 ? resources.getBoolean(identifier) : false;
        try {
            Class<?> systemPropertiesClass = Class.forName(SYSTEM_PROPERTIES_CLASS);
            String mainKeys = (String) systemPropertiesClass.getMethod(SYSTEM_PROPERTIES_GET_METHOD, String.class).invoke(systemPropertiesClass, QEMU_HW_MAINKEYS);
            if (!"1".equals(mainKeys)) {
                isNavigationBarSupported = "0".equals(mainKeys) ? true : configShowNavigationBar;
            }
            return isNavigationBarSupported;
        } catch (Exception exception) {
            Log.d(TAG, "fail to get navigation bar status message is " + exception.getMessage());
            return configShowNavigationBar;
        }
    }

    public static NavigationBarContentObserver registerObserver(Context context, NavigationBarChangeListener navigationBarChangeListener) {
        if (context == null) {
            return null;
        }
        NavigationBarContentObserver navigationBarContentObserver = new NavigationBarContentObserver(context, navigationBarChangeListener);
        context.getContentResolver().registerContentObserver(NAVIGATION_BAR_IMMERSIVE_URI, false, navigationBarContentObserver);
        return navigationBarContentObserver;
    }

    public static void unregisterObserver(Context context, NavigationBarContentObserver navigationBarContentObserver) {
        if (context == null || navigationBarContentObserver == null) {
            return;
        }
        navigationBarContentObserver.clearListener();
        context.getContentResolver().unregisterContentObserver(navigationBarContentObserver);
    }

    private static int getManualHideNavigationBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), MANUAL_HIDE_NAVIGATION_BAR, 0);
    }

    private static int getNavigationState(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), HIDE_NAVIGATION_BAR_ENABLE, NAV_STATE_VIRTUAL_KEY);
    }

    private static boolean isNavigationBarVisibleInImmersiveMode(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), NAVIGATION_BAR_IMMERSIVE, NAV_STATE_VIRTUAL_KEY) == NAV_STATE_VIRTUAL_KEY;
    }
}
