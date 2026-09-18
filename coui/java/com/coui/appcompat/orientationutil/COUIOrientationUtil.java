package com.coui.appcompat.orientationutil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;


public class COUIOrientationUtil {
    public static final int ORIENTATION_LANDSCAPE = 2;
    public static final int ORIENTATION_PORTRAIT = 1;

    public static int getRealOrientation(Context context) {
        Point screenSize = getScreenSize(context);
        return (screenSize.x <= screenSize.y || isInMultiWindowMode(context)) ? ORIENTATION_PORTRAIT : ORIENTATION_LANDSCAPE;
    }

    public static Point getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        return point;
    }

    public static boolean isInMultiWindowMode(Context context) {
        if (context instanceof Activity) {
            return ((Activity) context).isInMultiWindowMode();
        }
        return false;
    }

    public static boolean isPortrait(int orientation) {
        return orientation == ORIENTATION_PORTRAIT;
    }

    public static boolean isPortrait(Context context) {
        return getRealOrientation(context) == ORIENTATION_PORTRAIT;
    }
}
