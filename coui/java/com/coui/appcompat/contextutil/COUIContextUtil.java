package com.coui.appcompat.contextutil;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import androidx.core.graphics.ColorUtils;

import com.coui.appcompat.R;

public class COUIContextUtil {
    private static final String TAG = "COUIContextUtil";
    private Context mContext;

    public COUIContextUtil(Context context) {
        mContext = context;
    }

    public static int getAttrColor(Context context, int attr) {
        if (attr == R.attr.couiColorFocus || attr == R.attr.couiColorDisable) {
            return makeColorAlpha(getAttrColor(context, attr, 0), getAttrColor(context, R.attr.couiColorContainerTheme, 0));
        }
        if (attr == R.attr.couiColorFocusOutline) {
            return makeColorAlpha(getAttrColor(context, attr, 0), getAttrColor(context, R.attr.couiColorLabelTheme, 0));
        }
        return getAttrColor(context, attr, 0);
    }

    public static int getAttrColor(Context context, int attr, int defValue) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int color = array.getColor(0, defValue);
        array.recycle();
        return color;
    }

    public static int getAttrDimens(Context context, int attr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int value = array.getDimensionPixelSize(0, 0);
        array.recycle();
        return value;
    }

    public static float getAttrDimensFloat(Context context, int attr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        float value = array.getDimension(0, 0.0f);
        array.recycle();
        return value;
    }

    public static float getAttrFloat(Context context, int attr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        float value = array.getFloat(0, 0.0f);
        array.recycle();
        return value;
    }

    public static int getAttrId(Context context, int attr, int defValue) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int value = array.getResourceId(0, defValue);
        array.recycle();
        return value;
    }

    public static String getAttrString(Context context, int attr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        String value = array.getString(0);
        array.recycle();
        return value;
    }

    public static Context getCOUIThemeContext(Context context) {
        return isCOUITheme(context) ? context : new ContextThemeWrapper(context, R.style.Theme_COUI);
    }

    public static int getColor(Context context, int resId) {
        return context.getColor(resId);
    }

    public static float getFloat(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        try {
            context.getResources().getValue(resId, typedValue, true);
            return typedValue.getFloat();
        } catch (Resources.NotFoundException | NumberFormatException e) {
            Log.e(TAG, "getFloat: failed error=" + e);
            return 0.0f;
        }
    }

    public static int getResId(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(resId, typedValue, true);
        return typedValue.resourceId;
    }

    public static boolean isCOUIDarkTheme(Context context) {
        if (context == null) {
            return false;
        }
        TypedArray array = context.getTheme().obtainStyledAttributes(R.styleable.COUITheme);
        boolean value = array.getBoolean(R.styleable.COUITheme_isCOUIDarkTheme, false);
        array.recycle();
        return value;
    }

    public static boolean isCOUIStyle(Context context) {
        return isCOUITheme(context);
    }

    public static boolean isCOUITheme(Context context) {
        if (context == null) {
            return false;
        }
        TypedArray array = context.getTheme().obtainStyledAttributes(R.styleable.COUITheme);
        boolean value = array.getBoolean(R.styleable.COUITheme_isCOUITheme, false);
        array.recycle();
        return value;
    }

    private static int makeColorAlpha(int color, int alphaSource) {
        return ColorUtils.setAlphaComponent(alphaSource, android.graphics.Color.alpha(color));
    }

    public boolean isCOUITheme() {
        return isCOUITheme(mContext);
    }

    public void setContext(Context context) {
        if (context != null) {
            mContext = context;
        }
    }
}
