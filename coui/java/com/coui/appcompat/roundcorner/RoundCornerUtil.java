package com.coui.appcompat.roundcorner;

import android.content.Context;
import android.os.Build;
import android.util.SparseIntArray;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.version.COUIVersionUtil;

import java.lang.reflect.InvocationTargetException;

public class RoundCornerUtil {
    private static final String ANIM_LEVEL_KEY = "persist.sys.oplus.anim_level";
    private static final String DEFAULT_WEIGHT_KEY = "persist.sys.oplus.default_smooth_weight";
    private static final int DEFAULT_WEIGHT = 170;
    private static final float DEFAULT_WEIGHT_DIVISOR = 100.0f;
    private static final int LIGHT_ANIM = 3;
    private static final float NON_WEIGHT = 2.0f;
    private static final int SDK_SUB_VERSION_PATH_SUPPORT_SINGLE_CORNER = 12;
    public static final int SDK_SUB_VERSION_SUPPORT_BLUR = 10;
    public static final int SDK_VERSION = 34;
    public static final int SMOOTH_ROUND_CORNER_TYPE_OS15 = 0;
    public static final int SMOOTH_ROUND_CORNER_TYPE_OS16 = 1;
    public static final int SMOOTH_ROUND_CORNER_TYPE_UNSUPPORTED = -1;
    private static final String TAG = "RoundCornerUtil";
    private static final String UPGRADE_ANIM_LEVEL_KEY = "persist.sys.oplus.upgrade_anim_level";
    public static final int WEIGHT_17 = 3;

    private static Integer sAnimLevel;
    private static Float sDefaultWeight;
    private static Boolean sIsSmoothOn;
    private static SparseIntArray sRadiusMapping;
    private static Integer sSdkVersion;
    private static Integer sUpgradeAnimLevel;

    private RoundCornerUtil() {
    }

    public static int getRoundCornerForOS17(Context context, int radius) {
        if (sRadiusMapping == null) {
            SparseIntArray mapping = new SparseIntArray();
            sRadiusMapping = mapping;
            mapping.put(COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerXS),
                    context.getResources().getDimensionPixelSize(R.dimen.coui_round_corner_xs_radius_os17));
            mapping.put(COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerS),
                    context.getResources().getDimensionPixelSize(R.dimen.coui_round_corner_s_radius_os17));
            mapping.put(COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerM),
                    context.getResources().getDimensionPixelSize(R.dimen.coui_round_corner_m_radius_os17));
            mapping.put(COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerL),
                    context.getResources().getDimensionPixelSize(R.dimen.coui_round_corner_l_radius_os17));
            mapping.put(COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerXL),
                    context.getResources().getDimensionPixelSize(R.dimen.coui_round_corner_xl_radius_os17));
            mapping.put(COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerXXL),
                    context.getResources().getDimensionPixelSize(R.dimen.coui_round_corner_xxl_radius_os17));
        }
        return sRadiusMapping.get(radius, radius);
    }

    public static int getSmoothStyleType() {
        if (sSdkVersion == null) {
            sSdkVersion = COUIVersionUtil.getOSVersionCode();
        }
        if (COUIVersionUtil.checkOPlusViewSubSDK(37, 1) && isSmoothRoundRectOn()) {
            return SMOOTH_ROUND_CORNER_TYPE_OS16;
        }
        return COUIVersionUtil.checkOPlusViewBackgroundRenderEffectSupport() && isSmoothRoundRectOn()
                ? SMOOTH_ROUND_CORNER_TYPE_OS15
                : SMOOTH_ROUND_CORNER_TYPE_UNSUPPORTED;
    }

    private static int getSystemProp(String key, int defaultValue) {
        try {
            String value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class)
                    .invoke(null, key);
            if (value == null) {
                return defaultValue;
            }
            String trimmed = value.trim();
            return !trimmed.isEmpty() ? Integer.parseInt(trimmed) : defaultValue;
        } catch (ClassNotFoundException e) {
            COUILog.e(TAG, "Class not found:" + e);
        } catch (IllegalAccessException e) {
            COUILog.e(TAG, "Illegal access:" + e);
        } catch (NoSuchMethodException e) {
            COUILog.e(TAG, "Method not found:" + e);
        } catch (NumberFormatException e) {
            COUILog.e(TAG, "Illegal access:" + e);
        } catch (InvocationTargetException e) {
            COUILog.e(TAG, "Invocation target exception:" + e);
        }
        return defaultValue;
    }

    public static boolean isPathSupportSingleCorner() {
        if (Build.VERSION.SDK_INT <= 31) {
            return false;
        }
        if (sSdkVersion == null) {
            sSdkVersion = COUIVersionUtil.getOSVersionCode();
        }
        if (sSdkVersion > SDK_VERSION) {
            return true;
        }
        return sSdkVersion == SDK_VERSION
                && COUIVersionUtil.getSDKSubVersion() >= SDK_SUB_VERSION_PATH_SUPPORT_SINGLE_CORNER;
    }

    public static boolean isSmoothRoundRectOn() {
        if (sIsSmoothOn != null) {
            return sIsSmoothOn;
        }
        if (isVersionSupport()) {
            if (sAnimLevel == null) {
                sAnimLevel = getSystemProp(ANIM_LEVEL_KEY, LIGHT_ANIM);
            }
            if (sUpgradeAnimLevel == null) {
                sUpgradeAnimLevel = getSystemProp(UPGRADE_ANIM_LEVEL_KEY, LIGHT_ANIM);
            }
            if (sDefaultWeight == null) {
                sDefaultWeight = getSystemProp(DEFAULT_WEIGHT_KEY, DEFAULT_WEIGHT) / DEFAULT_WEIGHT_DIVISOR;
            }
            sIsSmoothOn = (sAnimLevel < LIGHT_ANIM || sUpgradeAnimLevel < LIGHT_ANIM)
                    && sDefaultWeight != NON_WEIGHT;
        } else {
            sIsSmoothOn = false;
        }
        return sIsSmoothOn;
    }

    public static boolean isSupportRoundCornerWhenBlur() {
        if (Build.VERSION.SDK_INT <= 31) {
            return false;
        }
        if (sSdkVersion == null) {
            sSdkVersion = COUIVersionUtil.getOSVersionCode();
        }
        if (sSdkVersion > SDK_VERSION) {
            return true;
        }
        return sSdkVersion == SDK_VERSION
                && COUIVersionUtil.getSDKSubVersion() >= SDK_SUB_VERSION_SUPPORT_BLUR;
    }

    public static boolean isVersionSupport() {
        if (Build.VERSION.SDK_INT <= 31) {
            return false;
        }
        if (sSdkVersion == null) {
            sSdkVersion = COUIVersionUtil.getOSVersionCode();
        }
        return sSdkVersion >= SDK_VERSION;
    }

    public static boolean supportSRCCompatibleBlur(boolean blur) {
        return isVersionSupport() && (!blur || isSupportRoundCornerWhenBlur());
    }
}
