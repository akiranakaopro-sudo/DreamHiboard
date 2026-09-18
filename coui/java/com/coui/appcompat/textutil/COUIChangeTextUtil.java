package com.coui.appcompat.textutil;

import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.widget.TextView;

import com.coui.appcompat.log.COUILog;

public class COUIChangeTextUtil {
    private static final int DRAGONFLY_TINY_SCREEN_DENSITY = 296;
    private static final int FLAMINGO_TINY_SCREEN_DENSITY = 300;
    private static final String TAG = "COUIChangeTextUtil";
    private static final int TINY_SCREEN_SMALLEST_SCREEN_WIDTH_DP = 210;
    public static final float[] SCALE_LEVEL = {0.9f, 1.0f, 1.15f, 1.35f, 1.6f};
    public static final String MEDIUM_FONT = "sans-serif-medium";

    public static void adaptBoldAndMediumFont(TextView textView, boolean medium) {
        if (textView != null) {
            textView.setTypeface(medium ? Typeface.create(MEDIUM_FONT, 0) : Typeface.DEFAULT);
        }
    }

    public static void adaptBoldAndMediumFont(Paint paint, boolean medium) {
        if (paint != null) {
            paint.setTypeface(medium ? Typeface.create(MEDIUM_FONT, 0) : Typeface.DEFAULT);
        }
    }

    public static void adaptFontSize(TextView textView, int scaleLevel) {
        float textSize = textView.getTextSize();
        Configuration configuration = textView.getResources().getConfiguration();
        float fontScale = configuration.fontScale;
        int densityDpi = configuration.densityDpi;
        if (densityDpi == FLAMINGO_TINY_SCREEN_DENSITY
                || densityDpi == DRAGONFLY_TINY_SCREEN_DENSITY
                || configuration.smallestScreenWidthDp <= TINY_SCREEN_SMALLEST_SCREEN_WIDTH_DP) {
            fontScale = 1.0f;
        }
        textView.setTextSize(0, getSuitableFontSize(textSize, fontScale, scaleLevel));
    }

    public static int binarySearchForOptimalTextViewWidth(TextView textView, int lineCount, int minWidth, int maxWidth, int padding) {
        if (lineCount <= 0) {
            COUILog.e(TAG, "Line count should be greater than 0!");
            return 0;
        }
        if (minWidth < 0 || maxWidth < 0 || minWidth > maxWidth || padding < 0 || minWidth < padding) {
            COUILog.e(TAG, "Illegal width or padding!");
            return 0;
        }
        int low = minWidth - padding;
        int high = maxWidth - padding;
        int result = (low + high) / 2;
        while (low <= high) {
            result = (low + high) / 2;
            int lines = measureTextLineCount(textView, result, 0);
            int previous = measureTextLineCount(textView, result - 1, 0);
            if (lines <= lineCount && previous > lineCount) {
                break;
            }
            if (previous <= lineCount) {
                high = result - 1;
            } else {
                low = result + 1;
            }
        }
        return result + padding;
    }

    public static float getDpG2Size(float size, float fontScale) {
        return fontScale < 1.15f ? size : size * 1.15f;
    }

    public static float getG3FontSize(float size, float fontScale) {
        float base = Math.round(size / fontScale);
        return fontScale <= 1.0f ? size : base * 1.15f;
    }

    public static float getSuitableFontSize(float size, float fontScale, int scaleLevel) {
        if (scaleLevel < 2) {
            return size;
        }
        if (scaleLevel > SCALE_LEVEL.length) {
            scaleLevel = SCALE_LEVEL.length;
        }
        float base = Math.round(size / fontScale);
        if (scaleLevel == 2) {
            return fontScale < 1.15f ? base : base * 1.15f;
        }
        if (scaleLevel == 3) {
            return fontScale < 1.15f ? base : fontScale < 1.6f ? base * 1.15f : base * 1.35f;
        }
        float maxScale = SCALE_LEVEL[scaleLevel - 1];
        return fontScale > maxScale ? base * maxScale : base * fontScale;
    }

    public static int measureTextLineCount(TextView textView, int width, int padding) {
        if (padding < 0 || width <= padding) {
            COUILog.e(TAG, "Illegal width or padding!");
            return 0;
        }
        if (textView == null) {
            return 0;
        }
        return StaticLayout.Builder.obtain(textView.getText(), 0, textView.getText().length(),
                        textView.getPaint(), width - padding)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(textView.getLineSpacingExtra(), textView.getLineSpacingMultiplier())
                .setIncludePad(textView.getIncludeFontPadding())
                .setBreakStrategy(textView.getBreakStrategy())
                .build()
                .getLineCount();
    }
}
