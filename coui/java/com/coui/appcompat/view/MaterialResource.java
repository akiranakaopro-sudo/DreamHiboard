package com.coui.appcompat.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TintTypedArray;


public class MaterialResource {
    private static final float FONT_SCALE_1_3 = 1.3f;
    private static final float FONT_SCALE_2_0 = 2.0f;

    private MaterialResource() {
    }

    public static ColorStateList getColorStateList(Context context, TypedArray typedArray, int index) {
        int resourceId;
        ColorStateList colorStateListA;
        return (!typedArray.hasValue(index) || (resourceId = typedArray.getResourceId(index, 0)) == 0 || (colorStateListA = AppCompatResources.getColorStateList(context, resourceId)) == null) ? typedArray.getColorStateList(index) : colorStateListA;
    }

    private static int getComplexUnit(TypedValue typedValue) {
        return typedValue.getComplexUnit();
    }

    public static int getDimensionPixelSize(Context context, TypedArray typedArray, int index, int defaultValue) {
        TypedValue typedValue = new TypedValue();
        if (!typedArray.getValue(index, typedValue) || typedValue.type != TypedValue.TYPE_ATTRIBUTE) {
            return typedArray.getDimensionPixelSize(index, defaultValue);
        }
        TypedArray typedArrayObtainStyledAttributes = context.getTheme().obtainStyledAttributes(new int[]{typedValue.data});
        int dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(0, defaultValue);
        typedArrayObtainStyledAttributes.recycle();
        return dimensionPixelSize;
    }

    public static Drawable getDrawable(Context context, TypedArray typedArray, int index) {
        int resourceId;
        Drawable drawableB;
        return (!typedArray.hasValue(index) || (resourceId = typedArray.getResourceId(index, 0)) == 0 || (drawableB = AppCompatResources.getDrawable(context, resourceId)) == null) ? typedArray.getDrawable(index) : drawableB;
    }

    public static float getFontScale(Context context) {
        return context.getResources().getConfiguration().fontScale;
    }

    public static int getIndexWithValue(TypedArray typedArray, int firstIndex, int secondIndex) {
        return typedArray.hasValue(firstIndex) ? firstIndex : secondIndex;
    }

    public static boolean isFontScaleAtLeast1(Context context) {
        return context.getResources().getConfiguration().fontScale >= FONT_SCALE_1_3;
    }

    public static boolean isFontScaleAtLeast2(Context context) {
        return context.getResources().getConfiguration().fontScale >= FONT_SCALE_2_0;
    }

    public static ColorStateList getColorStateList(Context context, TintTypedArray typedArray, int index) {
        int resourceId;
        ColorStateList colorStateListA;
        return (!typedArray.hasValue(index) || (resourceId = typedArray.getResourceId(index, 0)) == 0 || (colorStateListA = AppCompatResources.getColorStateList(context, resourceId)) == null) ? typedArray.getColorStateList(index) : colorStateListA;
    }
}
