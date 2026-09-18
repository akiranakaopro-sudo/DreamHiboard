package com.coui.appcompat.scroll;

import android.os.Bundle;

import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.version.COUIVersionUtil;

import java.lang.reflect.Method;

public class COUlFrameRateScrollSceneHelper {
    public static final int ANIMATION_TYPE_LIST_SCROLL = 10102;
    public static final int FRAME_RATE_MIN_SUB_SDK = 10;
    public static final String TAG = "COUlFrameRateHelper";
    private static final String FRAME_RATE_MANAGER =
            "com.oplus.dynamicframerate.DynamicFrameRateManager";

    private boolean mFrameRateIsOpening = false;
    private boolean mSupportRateVSdk;

    public COUlFrameRateScrollSceneHelper(boolean enable) {
        enableFrameRate(enable);
    }

    public final void enableFrameRate(boolean enable) {
        if (!enable || !COUIVersionUtil.checkOPlusViewSubSDK(34, FRAME_RATE_MIN_SUB_SDK)) {
            mSupportRateVSdk = false;
            return;
        }
        try {
            Class<?> manager = Class.forName(FRAME_RATE_MANAGER);
            Method method = manager.getDeclaredMethod("getDynamicFrameRateType");
            int dynamicFrameRateType = (Integer) method.invoke(null);
            if (dynamicFrameRateType == 1 || dynamicFrameRateType == 2) {
                mSupportRateVSdk = true;
            }
        } catch (Throwable ignored) {
            mSupportRateVSdk = false;
        }
    }

    public void setFrameRate(boolean start) {
        if (!mSupportRateVSdk) {
            COUILog.d(TAG, "SetFrameRate not success, mSupportRateVSdk is false");
            return;
        }
        if (mFrameRateIsOpening != start) {
            try {
                Class<?> manager = Class.forName(FRAME_RATE_MANAGER);
                Method method = manager.getDeclaredMethod(
                        "setFrameRate",
                        Object.class,
                        Integer.TYPE,
                        Integer.TYPE,
                        Bundle.class);
                method.invoke(null, this, ANIMATION_TYPE_LIST_SCROLL, start ? -1 : -2, null);
            } catch (Throwable ignored) {
                return;
            }
            COUILog.d(TAG, "setFrameRate isStart:" + start);
            mFrameRateIsOpening = start;
        }
    }
}
