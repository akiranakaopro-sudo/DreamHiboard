package com.coui.appcompat.vibrateutil;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

public class VibrateUtils {
    public static final int MIN_VIBRATOR_TIME = 25;
    public static final int STRENGTH_MAX_EDGE = 1200;
    public static final int STRENGTH_MAX_GRANULAR = 1600;
    public static final int STRENGTH_MAX_STEP = 2000;
    public static final int STRENGTH_MIN_EDGE = 800;
    public static final int STRENGTH_MIN_GRANULAR = 1200;
    public static final int STRENGTH_MIN_STEP = 200;
    public static final int STRENGTH_OFFSET = 400;
    public static final int TYPE_GRANULAR_SHORT_MODERATE = 1;
    public static final int TYPE_GRANULAR_SHORT_WEAK = 1;
    public static final int TYPE_GRANULAR_SHORT_WEAKEST = 0;
    public static final int TYPE_STEPABLE_EDGE = 154;
    public static final int TYPE_STEPABLE_REGULATE = 152;
    public static final int VIBRATE_CRISP_LEVEL_CRISP = 0;
    public static final int VIBRATE_CRISP_MAX_FREQUENCY = 90;
    public static final int VIBRATE_CRISP_MAX_INTENSITY = 100;
    public static final int VIBRATE_CRISP_MIN_FREQUENCY = 75;
    public static final int VIBRATE_CRISP_MIN_INTENSITY = 50;
    public static final int VIBRATE_SOFT_LEVEL_CRISP = 1;
    public static final int VIBRATE_SOFT_MAX_FREQUENCY = 55;
    public static final int VIBRATE_SOFT_MAX_INTENSITY = 68;
    public static final int VIBRATE_SOFT_MIN_FREQUENCY = 48;
    public static final int VIBRATE_SOFT_MIN_INTENSITY = 52;

    private static final String FEATURE_LINEAR_MOTOR = "oplus.software.vibrator_lmvibrator";
    private static final String FEATURE_LUXUN = "oplus.software.vibrator_luxunvibrator";
    private static final String LINEAR_MOTOR_SERVICE = "linearmotor";
    private static final String TAG = "VibrateUtils";

    private static long sLastVibratorTime = -1;
    private static Context sContext;
    private static boolean sHapticEnable;
    private static Boolean sOplusFeatureManagerPresent;
    private static final ContentObserver HAPTIC_OBSERVER = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (sContext != null) {
                sHapticEnable = Settings.System.getInt(sContext.getContentResolver(),
                        "haptic_feedback_enabled", 0) == 1;
            }
        }
    };

    private VibrateUtils() {
    }

    public static Object getLinearMotorVibrator(Context context) {
        if (context == null || !hasOplusFeature(FEATURE_LUXUN)) {
            return null;
        }
        try {
            return context.getSystemService(LINEAR_MOTOR_SERVICE);
        } catch (Throwable throwable) {
            Log.e(TAG, "get linear motor vibrator failed. error = " + throwable.getMessage());
            return null;
        }
    }

    public static boolean isLinearMotorVersion(Context context) {
        return hasOplusFeature(FEATURE_LINEAR_MOTOR);
    }

    public static void registerHapticObserver(Context context) {
        if (sContext != null || context == null) {
            return;
        }
        Context applicationContext = context.getApplicationContext();
        sContext = applicationContext;
        ContentResolver resolver = applicationContext.getContentResolver();
        sHapticEnable = Settings.System.getInt(resolver, "haptic_feedback_enabled", 0) == 1;
        resolver.registerContentObserver(Settings.System.getUriFor("haptic_feedback_enabled"),
                false, HAPTIC_OBSERVER);
    }

    public static void unRegisterHapticObserver() {
        if (sContext != null) {
            sContext.getContentResolver().unregisterContentObserver(HAPTIC_OBSERVER);
            sContext = null;
        }
    }

    public static void setLinearMotorVibratorStrength(Object vibrator, int effectType, int progress,
            int total, int minStrength, int maxStrength) {
        if (vibrator == null || !sHapticEnable) {
            return;
        }
        int strength = getVibratorStrengthWithLimit(progress, total, minStrength, maxStrength);
        if (effectType == 0) {
            strength += STRENGTH_OFFSET;
        }
        vibrateWithWaveformEffect(vibrator, effectType, strength);
    }

    public static void setLinearMotorVibratorStrength(Object vibrator, int effectType, int progress,
            int total, int minStrength, int maxStrength, int level, float scale) {
        if (vibrator == null || !sHapticEnable || filterVibrator()) {
            return;
        }
        int minFrequency = level == VIBRATE_CRISP_LEVEL_CRISP ? VIBRATE_CRISP_MIN_FREQUENCY : VIBRATE_SOFT_MIN_FREQUENCY;
        int maxFrequency = level == VIBRATE_CRISP_LEVEL_CRISP ? VIBRATE_CRISP_MAX_FREQUENCY : VIBRATE_SOFT_MAX_FREQUENCY;
        int minIntensity = level == VIBRATE_CRISP_LEVEL_CRISP ? VIBRATE_CRISP_MIN_INTENSITY : VIBRATE_SOFT_MIN_INTENSITY;
        int maxIntensity = level == VIBRATE_CRISP_LEVEL_CRISP ? VIBRATE_CRISP_MAX_INTENSITY : VIBRATE_SOFT_MAX_INTENSITY;
        getDynamicEffect(
                getVibratorValueWithLimit(progress, total, minFrequency, maxFrequency),
                Math.round(getVibratorValueWithLimit(progress, total, minIntensity, maxIntensity) * scale));
        setLinearMotorVibratorStrength(vibrator, effectType, progress, total, minStrength, maxStrength);
    }

    private static boolean filterVibrator() {
        if (sLastVibratorTime == -1) {
            sLastVibratorTime = SystemClock.elapsedRealtime();
            return false;
        }
        if (SystemClock.elapsedRealtime() - sLastVibratorTime < MIN_VIBRATOR_TIME) {
            return true;
        }
        sLastVibratorTime = SystemClock.elapsedRealtime();
        return false;
    }

    private static int getVibratorStrengthWithLimit(int value, int total, int min, int max) {
        if (total == 0) {
            return min;
        }
        int result = (int) (((value * 1.0d / total) * (max - min)) + min);
        return min < max ? Math.max(min, Math.min(result, max)) : Math.max(max, Math.min(result, min));
    }

    private static int getVibratorValueWithLimit(int value, int total, int min, int max) {
        if (total == 0) {
            return min;
        }
        int result = (int) (((value * 1.0d / total) * (max - min)) + min);
        return min < max ? Math.max(min, Math.min(result, max)) : Math.max(max, Math.min(result, min));
    }

    private static Object getDynamicEffect(int frequency, int intensity) {
        return "{\n"
                + "    \"Metadata\": {\n"
                + "        \"Version\": 2,\n"
                + "        \"Created\": \"2023-05-12\",\n"
                + "        \"Description\": \"Exported from RichTap Creator Pro\"\n"
                + "    },\n"
                + "    \"PatternList\": [\n"
                + "        {\n"
                + "            \"AbsoluteTime\": 0,\n"
                + "            \"Pattern\": [\n"
                + "                {\n"
                + "                    \"Event\": {\n"
                + "                        \"Type\": \"transient\",\n"
                + "                        \"RelativeTime\": 0,\n"
                + "                        \"Parameters\": {\n"
                + "                            \"Intensity\": " + intensity + ",\n"
                + "                            \"Frequency\": " + frequency + "\n"
                + "                        },\n"
                + "                        \"Index\": 0\n"
                + "                    }\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ]\n"
                + "}";
    }

    private static boolean hasOplusFeature(String feature) {
        if (!com.coui.appcompat.compat.CouiPlatform.isColorOsRuntime()) {
            return false;
        }
        if (sOplusFeatureManagerPresent != null && !sOplusFeatureManagerPresent) {
            return false;
        }
        try {
            Class<?> managerClass = Class.forName("com.oplus.content.OplusFeatureConfigManager");
            sOplusFeatureManagerPresent = true;
            Method getInstance = managerClass.getMethod("getInstance");
            Object manager = getInstance.invoke(null);
            Method hasFeature = managerClass.getMethod("hasFeature", String.class);
            Object result = hasFeature.invoke(manager, feature);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable throwable) {
            sOplusFeatureManagerPresent = false;
            return false;
        }
    }

    private static void vibrateWithWaveformEffect(Object vibrator, int effectType, int strength) {
        try {
            Class<?> builderClass = Class.forName("com.oplus.os.WaveformEffect$Builder");
            Object builder = builderClass.getConstructor().newInstance();
            builderClass.getMethod("setStrengthSettingEnabled", boolean.class).invoke(builder, false);
            builderClass.getMethod("setEffectStrength", int.class).invoke(builder, strength);
            builderClass.getMethod("setEffectType", int.class).invoke(builder, effectType);
            builderClass.getMethod("setAsynchronous", boolean.class).invoke(builder, true);
            Object effect = builderClass.getMethod("build").invoke(builder);
            vibrator.getClass().getMethod("vibrate", effect.getClass()).invoke(vibrator, effect);
        } catch (Throwable throwable) {
            Log.e(TAG, "linear motor vibrate failed. error = " + throwable.getMessage());
        }
    }
}
