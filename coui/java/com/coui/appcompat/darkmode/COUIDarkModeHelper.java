package com.coui.appcompat.darkmode;

import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class COUIDarkModeHelper {
    private static final String KEY_BACKGROUNDMAXL = "DarkMode_BackgroundMaxL";
    private static final String KEY_DIALOGBGMAXL = "DarkMode_DialogBgMaxL";
    private static final String KEY_FOREGROUNDMINL = "DarkMode_ForegroundMinL";

    private float mSystemDarkModeDialogBgMaxL = -1.0f;
    private float mSystemDarkModeBackgroundMaxL = -1.0f;
    private float mSystemDarkModeForegroundMinL = -1.0f;
    private List<ICOUIDarkColorObserver> mObservers = new ArrayList<>();

    public interface ICOUIDarkColorObserver {
        void onBackgroundChange();

        void onDialogBackgroundChange();

        void onForegroundChange();
    }

    public static abstract class COUIDarkColorObserver implements ICOUIDarkColorObserver {
        @Override
        public void onBackgroundChange() {
        }

        @Override
        public void onDialogBackgroundChange() {
        }

        @Override
        public void onForegroundChange() {
        }
    }

    public static class Holder {
        static COUIDarkModeHelper INSTANCE = new COUIDarkModeHelper();

        private Holder() {
        }
    }

    private void COUIDarkModeHelper() {
    }

    public static COUIDarkModeHelper getInstance() {
        return Holder.INSTANCE;
    }

    private void initObserver(final Context context) {
        mSystemDarkModeDialogBgMaxL = Settings.Global.getFloat(
                context.getContentResolver(), KEY_DIALOGBGMAXL, -1.0f);
        mSystemDarkModeBackgroundMaxL = Settings.Global.getFloat(
                context.getContentResolver(), KEY_BACKGROUNDMAXL, -1.0f);
        mSystemDarkModeForegroundMinL = Settings.Global.getFloat(
                context.getContentResolver(), KEY_FOREGROUNDMINL, -1.0f);

        Handler handler = null;
        context.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(KEY_DIALOGBGMAXL), true, new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        mSystemDarkModeDialogBgMaxL = Settings.Global.getFloat(
                                context.getContentResolver(), KEY_DIALOGBGMAXL, -1.0f);
                        notifyDialogBackgroundObserver();
                    }
                });
        context.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(KEY_BACKGROUNDMAXL), true, new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        mSystemDarkModeBackgroundMaxL = Settings.Global.getFloat(
                                context.getContentResolver(), KEY_BACKGROUNDMAXL, -1.0f);
                        notifyBackgroundObserver();
                    }
                });
        context.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(KEY_FOREGROUNDMINL), true, new ContentObserver(handler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        mSystemDarkModeForegroundMinL = Settings.Global.getFloat(
                                context.getContentResolver(), KEY_FOREGROUNDMINL, -1.0f);
                        notifyForegroundObserver();
                    }
                });
    }

    public void notifyBackgroundObserver() {
        if (mObservers == null || mObservers.size() == 0) {
            return;
        }
        Iterator<ICOUIDarkColorObserver> iterator = mObservers.iterator();
        while (iterator.hasNext()) {
            iterator.next().onBackgroundChange();
        }
    }

    public void notifyDialogBackgroundObserver() {
        if (mObservers == null || mObservers.size() == 0) {
            return;
        }
        Iterator<ICOUIDarkColorObserver> iterator = mObservers.iterator();
        while (iterator.hasNext()) {
            iterator.next().onDialogBackgroundChange();
        }
    }

    public void notifyForegroundObserver() {
        if (mObservers == null || mObservers.size() == 0) {
            return;
        }
        Iterator<ICOUIDarkColorObserver> iterator = mObservers.iterator();
        while (iterator.hasNext()) {
            iterator.next().onForegroundChange();
        }
    }

    public void addObserver(COUIDarkColorObserver observer) {
        if (observer == null || mObservers.contains(observer)) {
            return;
        }
        mObservers.add(observer);
    }

    public void attach(Application application) {
        initObserver(application.getApplicationContext());
    }

    public int getBackgroundColor() {
        return makeDark(Color.WHITE);
    }

    public int getDialogBackgroundColor() {
        return makeDialogDark(Color.WHITE);
    }

    public int getForegroundColor() {
        return makeLight(Color.BLACK);
    }

    public int makeDark(int color) {
        float maxL = mSystemDarkModeBackgroundMaxL;
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double l = lab[0];
        double targetL = 100.0d - l;
        if (targetL >= l) {
            return color;
        }
        if (maxL != -1.0f) {
            targetL = ((targetL / 50.0d) * (50.0f - maxL)) + maxL;
        }
        lab[0] = targetL;
        int rgb = ColorUtils.LABToColor(targetL, lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(rgb), Color.green(rgb), Color.blue(rgb));
    }

    public int makeDialogDark(int color) {
        float maxL = mSystemDarkModeDialogBgMaxL;
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double l = lab[0];
        double targetL = 100.0d - l;
        if (targetL >= l) {
            return color;
        }
        if (maxL != -1.0f) {
            targetL = ((targetL / 50.0d) * (50.0f - maxL)) + maxL;
        }
        lab[0] = targetL;
        int rgb = ColorUtils.LABToColor(targetL, lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(rgb), Color.green(rgb), Color.blue(rgb));
    }

    public int makeLight(int color) {
        float minL = mSystemDarkModeForegroundMinL;
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double l = lab[0];
        double targetL = 100.0d - l;
        if (targetL <= l) {
            return color;
        }
        if (minL != -1.0f) {
            targetL = (((targetL - 50.0d) / 50.0d) * (minL - 50.0f)) + 50.0d;
        }
        lab[0] = targetL;
        int rgb = ColorUtils.LABToColor(targetL, lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(rgb), Color.green(rgb), Color.blue(rgb));
    }

    public void removeObserver(COUIDarkColorObserver observer) {
        if (observer == null) {
            return;
        }
        mObservers.remove(observer);
    }
}
