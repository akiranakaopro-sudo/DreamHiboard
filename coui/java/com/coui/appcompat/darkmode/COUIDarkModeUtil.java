package com.coui.appcompat.darkmode;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;

public class COUIDarkModeUtil {
    private static final int COLOR_DDDDDD = 0xffdddddd;

    private static ColorFilter getDarkFilter() {
        return new LightingColorFilter(COLOR_DDDDDD, 0);
    }

    public static boolean isNightMode(Context context) {
        return 32 == (context.getResources().getConfiguration().uiMode & 48);
    }

    public static int makeDark(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float lightness = 1.0f - hsl[2];
        if (lightness >= hsl[2]) {
            return color;
        }
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int makeDarkLimit(int color, float limit) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float lightness = Math.max(limit, 1.0f - hsl[2]);
        if (lightness >= hsl[2]) {
            return color;
        }
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    public static int makeLight(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float lightness = 1.0f - hsl[2];
        if (lightness <= hsl[2]) {
            return color;
        }
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    public static void makeDrawableDark(Drawable drawable) {
        if (drawable != null) {
            drawable.setColorFilter(getDarkFilter());
        }
    }

    public static void makeImageViewDark(ImageView imageView) {
        if (imageView != null) {
            imageView.setColorFilter(getDarkFilter());
        }
    }

    public static void setForceDarkAllow(View view, boolean allow) {
        view.setForceDarkAllowed(allow);
    }
}
