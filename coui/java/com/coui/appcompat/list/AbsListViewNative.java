package com.coui.appcompat.list;

import android.util.Log;
import android.widget.AbsListView;

import com.coui.appcompat.version.COUICompatUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AbsListViewNative {
    private static final String TAG = "AbsListViewNative";
    public static final int TOUCH_MODE_DONE_WAITING = 2;
    public static final int TOUCH_MODE_DOWN = 0;
    public static final int TOUCH_MODE_FLING = 4;
    public static final int TOUCH_MODE_OVERFLING = 6;
    public static final int TOUCH_MODE_OVERSCROLL = 5;
    public static final int TOUCH_MODE_REST = -1;
    public static final int TOUCH_MODE_SCROLL = 3;
    public static final int TOUCH_MODE_TAP = 1;
    private static final boolean USE_WRAPPER = true;
    private static final String VIEW_WRAPPER_PATH_NEW = "com.oplus.inner.widget.AbsListViewWrapper";
    private static String sAbsListViewWrapperName;

    private static boolean canReachFrameworkWrapper() {
        try {
            Class.forName(VIEW_WRAPPER_PATH_NEW);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getTouchMode(AbsListView absListView) {
        sAbsListViewWrapperName = canReachFrameworkWrapper()
                ? VIEW_WRAPPER_PATH_NEW
                : COUICompatUtil.getInstance().getAbsListViewName();
        try {
            if (USE_WRAPPER) {
                Method method = Class.forName(sAbsListViewWrapperName)
                        .getDeclaredMethod("getTouchMode", AbsListView.class);
                return ((Integer) method.invoke(null, absListView)).intValue();
            }
            Field field = AbsListView.class.getDeclaredField("mTouchMode");
            field.setAccessible(true);
            return field.getInt(absListView);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return TOUCH_MODE_REST;
        }
    }

    public static void setTouchMode(AbsListView absListView, int touchMode) {
        sAbsListViewWrapperName = canReachFrameworkWrapper()
                ? VIEW_WRAPPER_PATH_NEW
                : COUICompatUtil.getInstance().getAbsListViewName();
        try {
            if (USE_WRAPPER) {
                Method method = Class.forName(sAbsListViewWrapperName)
                        .getDeclaredMethod("setTouchMode", AbsListView.class, Integer.TYPE);
                method.invoke(null, absListView, Integer.valueOf(touchMode));
                return;
            }
            Field field = AbsListView.class.getDeclaredField("mTouchMode");
            field.setAccessible(true);
            field.setInt(absListView, touchMode);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}
