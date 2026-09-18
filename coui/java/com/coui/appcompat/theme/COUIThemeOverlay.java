package com.coui.appcompat.theme;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.OplusBaseConfiguration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.ImageView;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.tintimageview.COUITintUtil;
import com.coui.appcompat.version.COUICompatUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import oplus.content.res.OplusExtraConfiguration;


public class COUIThemeOverlay {
    private static final String BASE_CONFIG_NEW = "android.content.res.OplusBaseConfiguration";
    private static final String COLOR_MATERIAL_ENABLE = "color_material_enable";
    private static final int COMPAT_VERSION = 12000;
    private static final int COUI_CUSTOM_FALG = 131072;
    private static final int COUI_CUSTOM_THEME_FLAG = 256;
    private static final int COUI_ONLINE_FALG = 1048576;
    private static final int COUI_SELECT_FALG = 65535;
    private static final int COUI_SINGLE_FALG = 65536;
    private static final int COUI_THIRD_THEME_FLAG = 1;
    private static final int COUI_TYPE_FALG = 16711680;
    private static final int COUI_WALLPAPER_FALG = 262144;
    private static final String CUSTOM_THEME_PATH = "my_company/media/theme/";
    private static final String CUSTOM_THEME_PATH_SETTING = "custom_theme_path_setting";
    private static final String DATA_THEME_PATH = "data/theme/";
    private static final String TAG = "COUIThemeOverlay";
    private static final String THEME_VERSION_KEY = "ro.oplus.theme.version";
    private static final String WRAPPER_CLASS_NEW = "com.oplus.inner.content.res.ConfigurationWrapper";
    private static int mCompatVersion;
    private static boolean mThemeO;
    private static String mThemeOverlayName;
    private static boolean mThemeP;
    private static boolean mThemeR;
    private SparseIntArray themeOverlays = new SparseIntArray();
    private HashMap<String, WeakReference<Boolean>> mMetaCaches = new HashMap<>();

    public static class SingleTone {
        private static final COUIThemeOverlay INSTANCE = new COUIThemeOverlay();

        private SingleTone() {
        }
    }

    // Leapy added 2026-08-07 / strengthened 2026-08-08: AppCompat loads
    // viewInflaterClass by name. Keep a live static field so R8 cannot DCE the
    // Class literal (local unused vars are stripped). Consumer proguard props
    // are unsupported on this Soong; MtkSettings also keeps via proguard-coui.flags.
    private static final Class<?> sCouiViewInflaterClass = COUIComponentsViewInflater.class;

    static {
        mThemeOverlayName = canReachFrameworkWrapper() ? WRAPPER_CLASS_NEW : COUICompatUtil.getInstance().getConfigurationName();
        mThemeO = isThemeO();
        mThemeR = isThemeR();
        mThemeP = isThemeP() && COUIVersionUtil.getOSVersionCode() > 0;
        mCompatVersion = getCompatVersion();
        if (sCouiViewInflaterClass.getName().isEmpty()) {
            throw new AssertionError("COUIComponentsViewInflater missing");
        }
    }

    private boolean canReachBaseConfiguration() {
        // In-tree stub OplusBaseConfiguration would make Class.forName succeed on AOSP;
        // require a real ColorOS runtime (OplusBuild) instead.
        return com.coui.appcompat.compat.CouiPlatform.isColorOsRuntime();
    }

    private static boolean canReachFrameworkWrapper() {
        if (!com.coui.appcompat.compat.CouiPlatform.isColorOsRuntime()) {
            return false;
        }
        try {
            Class.forName(WRAPPER_CLASS_NEW);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private boolean getBoolValue(Resources resources, int resId) {
        if (resources == null || resId == 0) {
            return false;
        }
        return resources.getBoolean(resId);
    }

    private static int getCompatVersion() {
        int compatVersion = 0;
        try {
            Method method = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            String themeVersion = (String) method.invoke(null, THEME_VERSION_KEY);
            int parsedThemeVersion = !TextUtils.isEmpty(themeVersion) ? Integer.parseInt(themeVersion.trim()) : 0;
            if (parsedThemeVersion != 0) {
                return parsedThemeVersion;
            }
            try {
                String compatThemeVersion = (String) method.invoke(null, COUICompatUtil.getInstance().getThemeVerisonName());
                return !TextUtils.isEmpty(compatThemeVersion) ? Integer.parseInt(compatThemeVersion.trim()) : parsedThemeVersion;
            } catch (Exception e) {
                compatVersion = parsedThemeVersion;
                COUILog.e(TAG, "getCompatVersion e: " + e);
                return compatVersion;
            }
        } catch (Exception e) {
            COUILog.e(TAG, "getCompatVersion e: " + e);
            return compatVersion;
        }
    }

    private OplusExtraConfiguration getExtraConfig(Configuration configuration) {
        OplusBaseConfiguration oplusBaseConfiguration = (OplusBaseConfiguration) typeCasting(OplusBaseConfiguration.class, configuration);
        if (oplusBaseConfiguration == null) {
            return null;
        }
        return oplusBaseConfiguration.mOplusExtraConfiguration;
    }

    public static COUIThemeOverlay getInstance() {
        return SingleTone.INSTANCE;
    }

    private int getResId(Context context, String name, String defType) {
        if (context.getResources() == null || TextUtils.isEmpty(name) || TextUtils.isEmpty(defType) || TextUtils.isEmpty(context.getPackageName())) {
            return 0;
        }
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private int getThemeArrayId(Context context, int themeIndex, int themeTypeFlag) {
        int resourceId = 0;
        if (themeIndex > 0 && context.getResources() != null) {
            Resources resources = context.getResources();
            int compatVersion = mCompatVersion;
            if (compatVersion > COMPAT_VERSION) {
                TypedArray typedArrayObtainTypedArray = resources.obtainTypedArray(R.array.coui_theme_arrays_ids);
                resourceId = typedArrayObtainTypedArray.length() >= themeIndex ? typedArrayObtainTypedArray.getResourceId(themeIndex - 1, 0) : 0;
                typedArrayObtainTypedArray.recycle();
            } else if (compatVersion == COMPAT_VERSION) {
                int resId = getResId(context, mThemeR ? "coui_theme_arrays_ids_patch_r" : "coui_theme_arrays_ids_patch_o", "array");
                if (mThemeO && themeTypeFlag == COUI_ONLINE_FALG) {
                    resId = R.array.coui_theme_arrays_ids;
                }
                if (resId != 0) {
                    TypedArray typedArrayObtainTypedArray2 = resources.obtainTypedArray(resId);
                    resourceId = typedArrayObtainTypedArray2.length() >= themeIndex ? typedArrayObtainTypedArray2.getResourceId(themeIndex - 1, 0) : 0;
                    typedArrayObtainTypedArray2.recycle();
                }
            } else {
                int resId2 = getResId(context, mThemeR ? "coui_theme_arrays_ids_repatch_r" : "coui_theme_arrays_ids_repatch_o", "array");
                if (resId2 != 0) {
                    TypedArray typedArrayObtainTypedArray3 = resources.obtainTypedArray(resId2);
                    resourceId = typedArrayObtainTypedArray3.length() >= themeIndex ? typedArrayObtainTypedArray3.getResourceId(themeIndex - 1, 0) : 0;
                    typedArrayObtainTypedArray3.recycle();
                }
            }
        }
        return resourceId;
    }

    private boolean hasCustomThemePkg(Context context) {
        String packageName = context.getPackageName();
        File file = new File(CUSTOM_THEME_PATH);
        if (!file.exists() || TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (new File(file, packageName).exists()) {
            return true;
        }
        File[] fileArrListFiles = file.listFiles();
        if (fileArrListFiles == null || fileArrListFiles.length == 0) {
            return false;
        }
        String string = Settings.System.getString(context.getContentResolver(), CUSTOM_THEME_PATH_SETTING);
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        return new File(string, packageName).exists();
    }

    private boolean hasDataThemePkg(Context context) {
        String packageName = context.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        OplusExtraConfiguration extraConfig = getExtraConfig(context.getResources().getConfiguration());
        int userId = extraConfig != null ? extraConfig.mUserId : 0;
        String dataThemePath = DATA_THEME_PATH;
        if (userId > 0) {
            dataThemePath = DATA_THEME_PATH + userId;
        }
        return new File(dataThemePath, packageName).exists();
    }

    private boolean isCOUIEnable(Context context) {
        WeakReference<Boolean> weakReference = this.mMetaCaches.get(context.getPackageName());
        Boolean bool = weakReference != null ? weakReference.get() : null;
        if (bool != null) {
            return bool.booleanValue();
        }
        boolean couiEnabled = false;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (applicationInfo != null && applicationInfo.metaData.getBoolean(COLOR_MATERIAL_ENABLE)) {
                couiEnabled = true;
            }
            this.mMetaCaches.put(context.getPackageName(), new WeakReference<>(Boolean.valueOf(couiEnabled)));
        } catch (PackageManager.NameNotFoundException e) {
            COUILog.e(TAG, "isCOUIEnable e: " + e);
        }
        return couiEnabled;
    }

    private static boolean isThemeO() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer.equals(String.valueOf(new char[]{'O', 'P', 'P', 'O'})) || manufacturer.equals(String.valueOf(new char[]{'O', 'p', 'p', 'o'}));
    }

    private static boolean isThemeP() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer.equals(String.valueOf(new char[]{'O', 'n', 'e', 'P', 'l', 'u', 's'})) || manufacturer.equals(String.valueOf(new char[]{'O', 'N', 'E', 'P', 'L', 'U', 'S'})) || manufacturer.equals(String.valueOf(new char[]{'G', 'A', 'L', 'I', 'L', 'E', 'I'})) || manufacturer.equals(String.valueOf(new char[]{'g', 'a', 'l', 'i', 'l', 'e', 'i'})) || manufacturer.equals(String.valueOf(new char[]{'F', 'A', 'R', 'A', 'D', 'A', 'Y'})) || manufacturer.equals(String.valueOf(new char[]{'f', 'a', 'r', 'a', 'd', 'a', 'y'}));
    }

    private static boolean isThemeR() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer.equals(String.valueOf(new char[]{'R', 'E', 'A', 'L', 'M', 'E'})) || manufacturer.equals(String.valueOf(new char[]{'R', 'e', 'a', 'l', 'm', 'e'})) || manufacturer.equals(String.valueOf(new char[]{'r', 'e', 'a', 'l', 'm', 'e'}));
    }

    private void resolveThemeStyle(Context context) {
        int themeArrayId = 0;
        int resId;
        if (context == null || isRejectTheme(context)) {
            return;
        }
        TypedArray typedArrayObtainStyledAttributes = context.getTheme().obtainStyledAttributes(new int[]{R.attr.couiThemeIdentifier});
        int integer = typedArrayObtainStyledAttributes.getInteger(0, 0);
        typedArrayObtainStyledAttributes.recycle();
        long couiTheme = getCOUITheme(context.getResources().getConfiguration());
        int themeIndex = (int) (COUI_SELECT_FALG & couiTheme);
        int themeTypeFlag = (int) (COUI_TYPE_FALG & couiTheme);
        boolean needsRepatch = mCompatVersion < COMPAT_VERSION;
        if (couiTheme != 0) {
            if (themeIndex == 0 && themeTypeFlag == 0) {
                return;
            }
            if (themeTypeFlag == COUI_CUSTOM_FALG) {
                setThemeOverlay(R.id.coui_global_theme, R.style.COUIOverlay_Theme_Single_First);
                return;
            }
            if (themeTypeFlag != COUI_SINGLE_FALG) {
                if (themeTypeFlag == COUI_WALLPAPER_FALG) {
                    themeArrayId = R.array.coui_theme_arrays_default_patch;
                } else if (themeTypeFlag == 0 || themeTypeFlag == COUI_ONLINE_FALG) {
                    themeArrayId = getThemeArrayId(context, themeIndex, themeTypeFlag);
                } else {
                    resId = 0;
                    themeIndex = -1;
                }
                int selectedThemeIndex = integer - 1;
                resId = themeArrayId;
                themeIndex = selectedThemeIndex;
            } else if (mThemeP) {
                resId = getResId(context, needsRepatch ? "coui_theme_arrays_single_repatch_p" : "coui_theme_arrays_single_patch_p", "array");
            } else {
                resId = R.array.coui_theme_arrays_single;
            }
            if (resId == 0 || themeIndex == -1) {
                return;
            }
            TypedArray typedArrayObtainTypedArray = context.getResources().obtainTypedArray(resId);
            if (typedArrayObtainTypedArray.length() > themeIndex) {
                setThemeOverlay(R.id.coui_global_theme, typedArrayObtainTypedArray.getResourceId(themeIndex, 0));
            }
            typedArrayObtainTypedArray.recycle();
        }
    }

    private <T> T typeCasting(Class<T> type, Object object) {
        if (object == null || !type.isInstance(object)) {
            return null;
        }
        return type.cast(object);
    }

    public void applyCOUITintIcon(Context context, ImageView imageView, boolean forceTint) {
        Drawable drawable;
        if (imageView == null || isRejectTheme(context)) {
            return;
        }
        if ((getInstance().isCOUITheme(context) || forceTint) && (drawable = imageView.getDrawable()) != null) {
            if (drawable instanceof LayerDrawable) {
                COUITintUtil.tintDrawable(((LayerDrawable) drawable).getDrawable(0), COUIContextUtil.getAttrColor(context, R.attr.couiColorPrimaryText));
            } else {
                COUITintUtil.tintDrawable(drawable, COUIContextUtil.getAttrColor(context, R.attr.couiColorPrimaryText));
            }
            COUIDarkModeUtil.setForceDarkAllow(imageView, false);
            imageView.setImageDrawable(drawable);
        }
    }

    public void applyThemeOverlays(Context context) {
        synchronized (this.themeOverlays) {
            try {
                clearThemeOverlays();
                resolveThemeStyle(context);
                for (int index = 0; index < this.themeOverlays.size(); index++) {
                    context.setTheme(this.themeOverlays.valueAt(index));
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void clearThemeOverlays() {
        synchronized (this.themeOverlays) {
            this.themeOverlays.clear();
        }
    }

    public long getCOUITheme(Configuration configuration) {
        if (!canReachBaseConfiguration()) {
            return 0L;
        }
        OplusExtraConfiguration extraConfig = getExtraConfig(configuration);
        if (extraConfig != null) {
            return extraConfig.mMaterialColor;
        }
        try {
            Class<?> cls = Class.forName(mThemeOverlayName);
            if (cls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]) != null) {
                return ((Long) cls.getMethod("getMaterialColor", Configuration.class).invoke(null, configuration)).longValue();
            }
            return 0L;
        } catch (Exception e) {
            COUILog.e(TAG, "getCOUITheme e: " + e);
            return 0L;
        }
    }

    public int getThemeOverlay(int id) {
        int themeOverlay;
        synchronized (this.themeOverlays) {
            themeOverlay = this.themeOverlays.get(id);
        }
        return themeOverlay;
    }

    public boolean isCOUITheme(Context context) {
        long couiTheme = getCOUITheme(context.getResources().getConfiguration());
        return couiTheme > 0 && (couiTheme & Integer.MAX_VALUE) != 0;
    }

    public boolean isGreenMaterial(Context context) {
        long couiTheme = getCOUITheme(context.getResources().getConfiguration());
        return couiTheme > 0 && (couiTheme & COUI_SELECT_FALG) == 5;
    }

    public boolean isRejectTheme(Context context) {
        OplusExtraConfiguration extraConfig;
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration == null || !canReachBaseConfiguration()) {
            return false;
        }
        try {
            extraConfig = getExtraConfig(context.getResources().getConfiguration());
            try {
            } catch (Exception e) {
                Log.d(TAG, "get extra config failed : " + e.getMessage());
            }
        } catch (Exception e) {
            extraConfig = null;
            Log.d(TAG, "get extra config failed : " + e.getMessage());
        }
        long jLongValue = extraConfig instanceof OplusExtraConfiguration ? extraConfig.mThemeChangedFlags : 0L;
        if (extraConfig == null) {
            try {
                Class<?> cls = Class.forName(mThemeOverlayName);
                if (cls.newInstance() != null) {
                    jLongValue = ((Long) cls.getMethod("getThemeChangedFlags", Configuration.class).invoke(null, configuration)).longValue();
                }
            } catch (Exception e) {
                COUILog.e(TAG, "isRejectTheme e: " + e);
            }
        }
        if ((1 & jLongValue) != 0) {
            return (((jLongValue & COUI_CUSTOM_THEME_FLAG) > 0L ? 1 : ((jLongValue & COUI_CUSTOM_THEME_FLAG) == 0L ? 0 : -1)) != 0 ? hasCustomThemePkg(context) : hasDataThemePkg(context)) && (configuration.uiMode & 48) != 32;
        }
        return false;
    }

    public void setThemeOverlay(int id, int themeOverlay) {
        synchronized (this.themeOverlays) {
            this.themeOverlays.put(id, themeOverlay);
        }
    }

    public boolean shouldResetTheme(Configuration configuration) {
        long couiTheme = getCOUITheme(configuration);
        return (Integer.MAX_VALUE & couiTheme) == 0 || (couiTheme & COUI_WALLPAPER_FALG) != 0;
    }
}
