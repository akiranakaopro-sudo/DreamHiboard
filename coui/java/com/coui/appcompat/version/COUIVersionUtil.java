package com.coui.appcompat.version;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class COUIVersionUtil {
    public static final int COUI_1_0 = 1;
    public static final int COUI_1_2 = 2;
    public static final int COUI_1_4 = 3;
    public static final int COUI_2_0 = 4;
    public static final int COUI_2_1 = 5;
    public static final int COUI_3_0 = 6;
    public static final int COUI_3_1 = 7;
    public static final int COUI_3_2 = 8;
    public static final int COUI_5_0 = 9;
    public static final int COUI_5_1 = 10;
    public static final int COUI_5_2 = 11;
    public static final int COUI_6_0 = 12;
    public static final int COUI_6_1 = 13;
    public static final int COUI_6_2 = 14;
    public static final int COUI_6_7 = 15;
    public static final int COUI_7_0 = 16;
    public static final int COUI_7_1 = 17;
    public static final int COUI_7_2 = 18;
    public static final int COUI_8_0 = 19;
    public static final int COUI_8_1 = 20;
    public static final int COUI_8_2 = 21;
    public static final int OPLUS_OUTLINE_MAJOR_VERSION_33 = 33;
    public static final int OPLUS_OUTLINE_MAJOR_VERSION_37 = 37;
    public static final int OPLUS_OUTLINE_SUB_VERSION_1 = 1;
    public static final int OPLUS_OUTLINE_SUB_VERSION_34 = 34;
    public static final int UNKNOWN = 0;

    private static final String GET_OS_VERSION_METHOD = "getOplusOSVERSION";
    private static final String TAG = "COUIVersionUtil";
    private static final String VERSION_WRAPPER = "com.oplus.os.OplusBuild";

    private static Boolean sCanReachFrameworkWrapper;
    private static Integer sOSVersionCode;
    private static Integer sSDKSubVersion;

    private static boolean canReachFrameworkWrapper() {
        if (sCanReachFrameworkWrapper != null) {
            return sCanReachFrameworkWrapper;
        }
        // Same gate as CouiPlatform (OplusBuild); keep local cache for call sites.
        sCanReachFrameworkWrapper = com.coui.appcompat.compat.CouiPlatform.isColorOsRuntime();
        return sCanReachFrameworkWrapper;
    }

    public static boolean checkOPlusViewBackgroundRenderEffectSupport() {
        return checkOPlusViewSubSDK(OPLUS_OUTLINE_MAJOR_VERSION_33, OPLUS_OUTLINE_SUB_VERSION_34);
    }

    public static boolean checkOPlusViewSubSDK(int sdk, int subSdk) {
        if (Build.VERSION.SDK_INT <= 31) {
            return false;
        }
        if (getOSVersionCode() > sdk) {
            return true;
        }
        return getOSVersionCode() == sdk && getSDKSubVersion() >= subSdk;
    }

    public static String getDeviceName(Context context) {
        try {
            Class<?> cls = Class.forName(VERSION_WRAPPER);
            return (String) cls.getDeclaredMethod("getDeviceName", Context.class).invoke(cls, context);
        } catch (Exception e) {
            Log.e(TAG, "getDeviceName failed. error = " + e.getMessage());
            return "";
        }
    }

    public static int getOSVersionCode() {
        if (sOSVersionCode != null) {
            return sOSVersionCode;
        }
        if (!canReachFrameworkWrapper()) {
            sOSVersionCode = UNKNOWN;
            return sOSVersionCode;
        }
        try {
            Class<?> cls = Class.forName(VERSION_WRAPPER);
            if (Build.VERSION.SDK_INT > 31) {
                Class<?> versionClass = Class.forName("com.oplus.os.OplusBuild$VERSION");
                sOSVersionCode = versionClass.getField("SDK_VERSION").getInt(null);
                return sOSVersionCode;
            }
            sOSVersionCode = ((Integer) cls.getDeclaredMethod(GET_OS_VERSION_METHOD).invoke(null)).intValue();
            return sOSVersionCode;
        } catch (Throwable e) {
            Log.e(TAG, "getOSVersionCode failed. error = " + e.getMessage());
            sOSVersionCode = UNKNOWN;
            return UNKNOWN;
        }
    }

    public static int getSDKSubVersion() {
        if (sSDKSubVersion != null) {
            return sSDKSubVersion;
        }
        if (Build.VERSION.SDK_INT < 31) {
            sSDKSubVersion = UNKNOWN;
            return UNKNOWN;
        }
        if (!canReachFrameworkWrapper()) {
            sSDKSubVersion = UNKNOWN;
            return sSDKSubVersion;
        }
        try {
            Class<?> versionClass = Class.forName("com.oplus.os.OplusBuild$VERSION");
            sSDKSubVersion = versionClass.getField("SDK_SUB_VERSION").getInt(null);
            return sSDKSubVersion;
        } catch (Throwable ignored) {
            sSDKSubVersion = UNKNOWN;
            return UNKNOWN;
        }
    }

    public static boolean isColorOS() {
        return getOSVersionCode() != UNKNOWN;
    }
}
