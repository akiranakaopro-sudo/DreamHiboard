package com.coui.appcompat.panel;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;

import com.coui.appcompat.R;
import com.coui.appcompat.orientationutil.COUIOrientationUtil;
import com.coui.appcompat.uiutil.FollowHandManager;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIPanelMultiWindowUtils {
    private static final int DEFAULT_MARGIN_TOP = 40;
    private static final float LARGEST_SCREEN_HEIGHT_DP_THRESHOLD = 809.0f;
    private static final int NAV_STATE_SWIPE_SIDE_GESTURE = 3;
    private static final int SETTING_CLOSE_FLAG = 0;
    private static final int SETTING_OPEN_FLAG = 1;
    private static final float SMALLEST_SCREEN_WIDTH_DP_THRESHOLD = 600.0f;
    private static final float TWO_THIRDS = 0.66f;

    public static Activity contextToActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static int dp2px(Context context, int dp) {
        return Double.valueOf(((double) (context.getResources().getDisplayMetrics().density * dp)) + 0.5d).intValue();
    }

    public static int getCurrentPanelWindowVisibleHeight(Activity activity, Configuration configuration) {
        Rect currentWindowVisibleRect;
        int visibleHeight = (!isInMultiWindowMode(activity) || (currentWindowVisibleRect = getCurrentWindowVisibleRect(activity)) == null) ? 0 : currentWindowVisibleRect.bottom - currentWindowVisibleRect.top;
        return visibleHeight == 0 ? getPanelNormalVisibleHeight(activity, configuration) : visibleHeight;
    }

    public static int getCurrentWindowVisibleHeight(Activity activity, Configuration configuration) {
        Rect currentWindowVisibleRect;
        int visibleHeight = (!isInMultiWindowMode(activity) || (currentWindowVisibleRect = getCurrentWindowVisibleRect(activity)) == null) ? 0 : currentWindowVisibleRect.bottom - currentWindowVisibleRect.top;
        return visibleHeight == 0 ? getNormalVisibleHeight(activity, configuration) : visibleHeight;
    }

    public static Rect getCurrentWindowVisibleRect(Activity activity) {
        if (activity == null) {
            return null;
        }
        View decorView = activity.getWindow().getDecorView();
        Rect rect = new Rect();
        decorView.getGlobalVisibleRect(rect);
        return rect;
    }

    public static int getNormalVisibleHeight(Context context, Configuration configuration) {
        int navigationBarHeight = 0;
        if (context == null) {
            return 0;
        }
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        int screenHeightRealSize = UIUtil.getScreenHeightRealSize(context);
        int statusBarHeight = getStatusBarHeight(context);
        if (COUINavigationBarUtil.isNavigationBarShow(context) && (configuration.screenWidthDp >= SMALLEST_SCREEN_WIDTH_DP_THRESHOLD || isPortrait(configuration))) {
            navigationBarHeight = COUINavigationBarUtil.getNavigationBarHeight(context);
        }
        return (screenHeightRealSize - statusBarHeight) - navigationBarHeight;
    }

    public static int getPanelMarginBottom(Context context, Configuration configuration) {
        if (context == null || configuration == null) {
            return 0;
        }
        int screenWidthDp = configuration.screenWidthDp;
        boolean isSmallLayout = (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 1;
        boolean isLandscape = configuration.orientation == 2;
        if (screenWidthDp >= SMALLEST_SCREEN_WIDTH_DP_THRESHOLD || (!isSmallLayout && isLandscape)) {
            return getStatusBarHeight(context) == 0 ? context.createConfigurationContext(configuration).getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_vertical_without_status_bar) : context.createConfigurationContext(configuration).getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_bottom_default);
        }
        return 0;
    }

    public static int getPanelMaxHeight(Context context, Configuration configuration) {
        int panelNormalVisibleHeight;
        int panelMarginBottom;
        if (context == null) {
            return 0;
        }
        Activity activity = contextToActivity(context);
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        if (activity != null) {
            panelNormalVisibleHeight = getCurrentPanelWindowVisibleHeight(activity, configuration);
            panelMarginBottom = getPanelMarginBottom(context, configuration);
        } else {
            panelNormalVisibleHeight = getPanelNormalVisibleHeight(context, configuration);
            panelMarginBottom = getPanelMarginBottom(context, configuration);
        }
        return Math.min(panelNormalVisibleHeight - panelMarginBottom, getPanelPercentFrameLayoutMaxHeight(context, context.getResources().getDimensionPixelOffset(R.dimen.coui_panel_max_height)));
    }

    public static int getPanelNormalVisibleHeight(Context context, Configuration configuration) {
        int navigationBarHeight = 0;
        if (context == null) {
            return 0;
        }
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        int screenHeightRealSize = UIUtil.getScreenHeightRealSize(context);
        int dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_panel_min_padding_top);
        if (getStatusBarHeight(context) == 0) {
            dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_vertical_without_status_bar);
        }
        boolean isNavigationBarShow = COUINavigationBarUtil.isNavigationBarShow(context);
        boolean isNormalLandscape = ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 2) && (configuration.orientation == 2);
        if (isNavigationBarShow && ((configuration.screenWidthDp >= SMALLEST_SCREEN_WIDTH_DP_THRESHOLD || isPortrait(configuration)) && isVirtualNavigation(context) && !isNormalLandscape)) {
            navigationBarHeight = COUINavigationBarUtil.getNavigationBarHeight(context);
        }
        return (screenHeightRealSize - dimensionPixelOffset) - navigationBarHeight;
    }

    public static int getPanelPercentFrameLayoutMaxHeight(Context context, int maxHeight) {
        return maxHeight;
    }

    public static int getStatusBarHeight(Context context) {
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    public static int getStatusBarHeightAfterR(WindowInsets windowInsets) {
        return Math.abs(windowInsets.getInsets(WindowInsets.Type.statusBars()).bottom - windowInsets.getInsets(WindowInsets.Type.statusBars()).top);
    }

    public static boolean isDisplayInHorizontal(Activity activity) {
        if (activity == null || activity.getResources().getConfiguration().orientation != 2 || !isInMultiWindowMode(activity)) {
            return false;
        }
        View decorView = activity.getWindow().getDecorView();
        Rect rect = new Rect();
        decorView.getWindowVisibleDisplayFrame(rect);
        return ((float) (rect.right - rect.left)) < ((float) UIUtil.getScreenWidthRealSize(activity)) * TWO_THIRDS;
    }

    public static boolean isDisplayInPrimaryScreen(Activity activity) {
        if (activity == null) {
            return true;
        }
        int statusBarHeight = getStatusBarHeight(activity);
        int[] locationOnScreen = new int[2];
        activity.getWindow().getDecorView().getLocationOnScreen(locationOnScreen);
        return locationOnScreen[0] <= statusBarHeight && locationOnScreen[1] <= statusBarHeight;
    }

    public static boolean isDisplayInUpperWindow(Activity activity) {
        if (activity == null) {
            return true;
        }
        int[] locationOnScreen = new int[2];
        activity.getWindow().getDecorView().getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1] <= getStatusBarHeight(activity);
    }

    public static boolean isInMultiWindowMode(Activity activity) {
        return activity != null && activity.isInMultiWindowMode();
    }

    public static boolean isLand(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static boolean isLargeHeightScreen(Context context, Configuration configuration) {
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        return ((float) configuration.screenHeightDp) > LARGEST_SCREEN_HEIGHT_DP_THRESHOLD;
    }

    public static boolean isNormalLandScreen(Context context, Configuration configuration) {
        if (context == null || configuration == null) {
            return false;
        }
        return ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 2) && (configuration.orientation == 2);
    }

    public static boolean isNormalScreen(Context context, Configuration configuration) {
        return context != null && configuration != null && (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public static boolean isPortrait(Context context) {
        return isPortrait(context.getResources().getConfiguration());
    }

    public static boolean isSmallScreen(Context context, Configuration configuration) {
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        return ((float) configuration.screenWidthDp) < SMALLEST_SCREEN_WIDTH_DP_THRESHOLD;
    }

    public static boolean isTaskBarShowInApp(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "enable_launcher_taskbar", 0) == 1;
    }

    public static boolean isVirtualNavigation(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "hide_navigationbar_enable", 0) != NAV_STATE_SWIPE_SIDE_GESTURE;
    }

    public static boolean isPortrait(Configuration configuration) {
        return configuration.orientation == 1;
    }

    public static int getStatusBarHeight(WindowInsets windowInsets, Context context) {
        return getStatusBarHeightAfterR(windowInsets);
    }

    public static int getCurrentPanelWindowVisibleHeight(Activity activity, Configuration configuration, WindowInsets windowInsets) {
        int visibleHeight;
        if (isInMultiWindowMode(activity)) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int dimensionPixelOffset = activity.getResources().getDimensionPixelOffset(R.dimen.coui_panel_min_padding_top);
            if (getStatusBarHeight(windowInsets, activity) == 0) {
                dimensionPixelOffset = activity.getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_vertical_without_status_bar);
            }
            visibleHeight = (displayMetrics.heightPixels - windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom) - dimensionPixelOffset;
        } else {
            visibleHeight = 0;
        }
        return visibleHeight == 0 ? getPanelNormalVisibleHeight(activity, configuration, windowInsets) : visibleHeight;
    }

    @Deprecated
    public static int getPanelMarginBottom(Context context, Configuration configuration, WindowInsets windowInsets, boolean ignorePanelMarginBottom) {
        return getPanelMarginBottom(context, configuration, windowInsets, ignorePanelMarginBottom, false);
    }

    @Deprecated
    public static int getPanelMaxHeight(Context context, Configuration configuration, WindowInsets windowInsets, boolean ignorePanelMarginBottom) {
        return getPanelMaxHeight(context, configuration, windowInsets, ignorePanelMarginBottom, false);
    }

    public static int getPanelMarginBottom(Context context, Configuration configuration, WindowInsets windowInsets, boolean ignorePanelMarginBottom, boolean ignoreNavigationInset) {
        if (context == null || configuration == null || ignorePanelMarginBottom) {
            return 0;
        }
        int screenWidthDp = configuration.screenWidthDp;
        boolean isSmallLayout = (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 1;
        boolean isLandscape = configuration.orientation == 2;
        if (screenWidthDp < SMALLEST_SCREEN_WIDTH_DP_THRESHOLD && (isSmallLayout || !isLandscape)) {
            return 0;
        }
        int navigationBarInsetBottom = windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        if ((context.getResources().getConfiguration().screenLayout & 48) == 32) {
            return Math.max(0, context.createConfigurationContext(configuration).getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_bottom_smallland_default) - navigationBarInsetBottom);
        }
        int dimensionPixelOffset = context.createConfigurationContext(configuration).getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_bottom_default);
        if (ignoreNavigationInset) {
            return dimensionPixelOffset;
        }
        return Math.max(0, (FollowHandManager.ifWidthDpIsFullScreen(context) && COUIOrientationUtil.isInMultiWindowMode(contextToActivity(context)) && !isVirtualNavigation(context) && isTaskBarShowInApp(context)) ? Math.max(dimensionPixelOffset, navigationBarInsetBottom) : dimensionPixelOffset - navigationBarInsetBottom);
    }

    public static int getPanelMaxHeight(Context context, Configuration configuration, WindowInsets windowInsets, boolean ignorePanelMarginBottom, boolean ignoreNavigationInset) {
        int panelNormalVisibleHeight;
        int panelMarginBottom;
        if (context == null) {
            return 0;
        }
        Activity activity = contextToActivity(context);
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        if (activity != null) {
            panelNormalVisibleHeight = getCurrentPanelWindowVisibleHeight(activity, configuration, windowInsets);
            panelMarginBottom = getPanelMarginBottom(context, configuration, windowInsets, ignorePanelMarginBottom, ignoreNavigationInset);
        } else {
            panelNormalVisibleHeight = getPanelNormalVisibleHeight(context, configuration, windowInsets);
            panelMarginBottom = getPanelMarginBottom(context, configuration, windowInsets, ignorePanelMarginBottom, ignoreNavigationInset);
        }
        return Math.min(panelNormalVisibleHeight - panelMarginBottom, getPanelPercentFrameLayoutMaxHeight(context, context.getResources().getDimensionPixelOffset(R.dimen.coui_panel_max_height)));
    }

    public static int getPanelNormalVisibleHeight(Context context, Configuration configuration, WindowInsets windowInsets) {
        int navigationBarHeight = 0;
        if (context == null) {
            return 0;
        }
        if (configuration == null) {
            configuration = context.getResources().getConfiguration();
        }
        int screenHeightRealSize = UIUtil.getScreenHeightRealSize(context);
        int dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_panel_min_padding_top);
        if (getStatusBarHeight(windowInsets, context) == 0) {
            dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_margin_vertical_without_status_bar);
        }
        boolean isNavigationBarShow = COUINavigationBarUtil.isNavigationBarShow(context);
        boolean isNormalLandscape = ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 2) && (configuration.orientation == 2);
        if (isNavigationBarShow && ((configuration.screenWidthDp >= SMALLEST_SCREEN_WIDTH_DP_THRESHOLD || isPortrait(configuration)) && isVirtualNavigation(context) && !isNormalLandscape)) {
            navigationBarHeight = COUINavigationBarUtil.getNavigationBarHeight(context);
        }
        return (screenHeightRealSize - dimensionPixelOffset) - navigationBarHeight;
    }
}
