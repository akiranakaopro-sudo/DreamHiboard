package com.coui.appcompat.statelistutil;

import android.content.res.ColorStateList;

public class COUIStateListUtil {
    private static final int[] EMPTY_STATE_SET = new int[0];

    private COUIStateListUtil() {
    }

    public static ColorStateList createColorStateList(int normalColor, int disabledColor) {
        return new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_enabled}, EMPTY_STATE_SET},
                new int[]{disabledColor, normalColor});
    }

    public static ColorStateList createColorStateList(int normalColor, int disabledColor,
            int checkedColor, int pressedColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
                        EMPTY_STATE_SET
                },
                new int[]{disabledColor, checkedColor, pressedColor, normalColor});
    }
}
