package com.coui.appcompat.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.core.graphics.ColorUtils;

public class COUIThemeUtils {
    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE = new ThreadLocal<>();
    public static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};
    public static final int[] FOCUSED_STATE_SET = {android.R.attr.state_focused};
    public static final int[] ACTIVATED_STATE_SET = {android.R.attr.state_activated};
    public static final int[] PRESSED_STATE_SET = {android.R.attr.state_pressed};
    public static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    public static final int[] SELECTED_STATE_SET = {android.R.attr.state_selected};
    public static final int[] NOT_PRESSED_OR_FOCUSED_STATE_SET = {-android.R.attr.state_pressed, -android.R.attr.state_focused};
    static final int[] EMPTY_STATE_SET = new int[0];
    private static final int[] TEMP_ARRAY = new int[1];

    public static ColorStateList createDisabledStateList(int normalColor, int disabledColor) {
        return new ColorStateList(new int[][]{DISABLED_STATE_SET, EMPTY_STATE_SET}, new int[]{disabledColor, normalColor});
    }

    public static int getDisabledThemeAttrColor(Context context, int attr) {
        ColorStateList colorStateList = getThemeAttrColorStateList(context, attr);
        if (colorStateList != null && colorStateList.isStateful()) {
            return colorStateList.getColorForState(DISABLED_STATE_SET, colorStateList.getDefaultColor());
        }
        TypedValue typedValue = getTypedValue();
        context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, typedValue, true);
        return getThemeAttrColor(context, attr, typedValue.getFloat());
    }

    public static int getThemeAttrColor(Context context, int attr) {
        int[] attrs = TEMP_ARRAY;
        attrs[0] = attr;
        TypedArray typedArray = context.obtainStyledAttributes((AttributeSet) null, attrs);
        try {
            return typedArray.getColor(0, 0);
        } finally {
            typedArray.recycle();
        }
    }

    public static ColorStateList getThemeAttrColorStateList(Context context, int attr) {
        int[] attrs = TEMP_ARRAY;
        attrs[0] = attr;
        TypedArray typedArray = context.obtainStyledAttributes((AttributeSet) null, attrs);
        try {
            return typedArray.getColorStateList(0);
        } finally {
            typedArray.recycle();
        }
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = TL_TYPED_VALUE.get();
        if (typedValue != null) {
            return typedValue;
        }
        TypedValue newTypedValue = new TypedValue();
        TL_TYPED_VALUE.set(newTypedValue);
        return newTypedValue;
    }

    public static int getThemeAttrColor(Context context, int attr, float alpha) {
        int color = getThemeAttrColor(context, attr);
        return ColorUtils.setAlphaComponent(color, Math.round(Color.alpha(color) * alpha));
    }
}
