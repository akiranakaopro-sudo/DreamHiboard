package com.coui.appcompat.uiutil;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.coui.appcompat.R;
import com.coui.appcompat.log.COUILog;
import java.util.List;

public class UIUtil {
    public static final int ANIM_LEVEL_HIGN_END = 1;
    public static final int ANIM_LEVEL_INVALID = -1;
    public static final int ANIM_LEVEL_LOW_END = 3;
    public static final int ANIM_LEVEL_MID_END = 2;
    public static final int ANIM_LEVEL_ULTRA_LOW_END = 4;
    public static final int CONSTANT_COLOR_MASK = 16777215;
    public static final int CONSTANT_INT_EIGHT = 8;
    public static final int CONSTANT_INT_EIGHTEEN = 18;
    public static final int CONSTANT_INT_ELEVEN = 11;
    public static final int CONSTANT_INT_FIFTEEN = 15;
    public static final int CONSTANT_INT_FIVE = 5;
    public static final int CONSTANT_INT_FORE = 4;
    public static final int CONSTANT_INT_FOURTEEN = 14;
    public static final int CONSTANT_INT_NINE = 9;
    public static final int CONSTANT_INT_NINETY = 90;
    public static final int CONSTANT_INT_ONE_HUNDRED = 100;
    public static final int CONSTANT_INT_ONE_HUNDRED_TEENTY = 120;
    public static final int CONSTANT_INT_ONE_THOUSAND = 1000;
    public static final int CONSTANT_INT_SEVEN = 7;
    public static final int CONSTANT_INT_SIX = 6;
    public static final int CONSTANT_INT_SIXTEEN = 16;
    public static final int CONSTANT_INT_SIXTY = 60;
    public static final int CONSTANT_INT_TEN = 10;
    public static final int CONSTANT_INT_THIRTEEN = 13;
    public static final int CONSTANT_INT_THIRTY = 30;
    public static final int CONSTANT_INT_THREE = 3;
    public static final int CONSTANT_INT_THREE_HUNDRED = 300;
    public static final int CONSTANT_INT_THTEE_HUNDRED_THIRTY = 330;
    public static final int CONSTANT_INT_TWELVE = 12;
    public static final int CONSTANT_INT_TWO_HUNDRED_SEVENTY = 270;
    public static final boolean DEBUG = false;
    public static final int DIRECTION_BOTTOM = 3;
    public static final int DIRECTION_END = 4;
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_START = 5;
    public static final int DIRECTION_TOP = 1;
    public static final int INT_TWENTY_FOUR = 24;
    public static final int LARGE_WIDTH_DP = 840;
    public static final int MEDIUM_WIDTH_DP = 600;
    private static final String TAG = "UIUtil";
    public static final int TWO_FIVE_FIVE = 255;
    private static SparseArray<String> sStringMap;
    public static final AnimLevel ANIM_LEVEL_SUPPORT_BLUR_MIN = AnimLevel.MID_END;
    private static int sCurrentAnimLevel = ANIM_LEVEL_INVALID;

    public static int alphaColor(int index, float value) {
        return (index & CONSTANT_COLOR_MASK) | (((int) (value * TWO_FIVE_FIVE)) << INT_TWENTY_FOUR);
    }

    public static float[] colorToFloats(int index) {
        return new float[]{((index >> 16) & TWO_FIVE_FIVE) / (float) TWO_FIVE_FIVE, ((index >> 8) & TWO_FIVE_FIVE) / (float) TWO_FIVE_FIVE, (index & TWO_FIVE_FIVE) / (float) TWO_FIVE_FIVE, ((index >> INT_TWENTY_FOUR) & TWO_FIVE_FIVE) / (float) TWO_FIVE_FIVE};
    }

    public static boolean confirmLevelAnim(AnimLevel animLevel) {
        if (sCurrentAnimLevel == ANIM_LEVEL_INVALID) {
            sCurrentAnimLevel = getAnimLevelVersion();
        }
        return sCurrentAnimLevel <= animLevel.getIntValue() && sCurrentAnimLevel != ANIM_LEVEL_INVALID;
    }

    public static Activity contextToActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static int dip2px(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static int getAdjustmentPointerIndex(MotionEvent motionEvent, int index) {
        return Math.min(Math.max(0, index), motionEvent.getPointerCount() - 1);
    }

    public static int getAnimLevel() {
        if (sCurrentAnimLevel == ANIM_LEVEL_INVALID) {
            sCurrentAnimLevel = getAnimLevelVersion();
        }
        return sCurrentAnimLevel;
    }

    public static int getAnimLevelVersion() {
        try {
            String str = (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, "persist.sys.oplus.anim_level");
            if (TextUtils.isEmpty(str)) {
                return -1;
            }
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            COUILog.e(TAG, "getAnimLevelVersion e: " + e);
            return -1;
        }
    }

    public static ColorStateList getColorStateListCompatNoCache(Context context, int index) {
        return context.getResources().getColorStateList(index, context.getTheme());
    }

    public static final float getConvertedFraction(float value, float value_2, float value_3) {
        return value + ((value_2 - value) * value_3);
    }

    public static String getPrice(double doubleValue) {
        return removeZero(doubleValue + "");
    }

    public static int getPx(Context context, int index) {
        return context.getResources().getDimensionPixelSize(index);
    }

    public static int getScreenHeightMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int getScreenHeightRealSize(Context context) {
        return getScreenSize(context).y;
    }

    public static Point getScreenSize(Context context) {
        WindowManager windowManager;
        Display defaultDisplay;
        Point point = new Point();
        if (context != null && (windowManager = (WindowManager) context.getSystemService("window")) != null && (defaultDisplay = windowManager.getDefaultDisplay()) != null) {
            defaultDisplay.getRealSize(point);
        }
        return point;
    }

    public static int getScreenWidthMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenWidthRealSize(Context context) {
        return getScreenSize(context).x;
    }

    public static int getStatusBarHeight(Context context) {
        int identifier = context.getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            try {
                return context.getApplicationContext().getResources().getDimensionPixelSize(identifier);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static String getString(Context context, int index) {
        if (sStringMap == null) {
            sStringMap = new SparseArray<>();
        }
        String str = sStringMap.get(index);
        if (str != null) {
            return str;
        }
        String string = context.getString(index);
        sStringMap.put(index, string);
        return string;
    }

    public static void initEditViewCursor(TextView textView, int index) {
        if (textView == null) {
            return;
        }
        CharSequence text = textView.getText();
        if (text instanceof Spannable) {
            Spannable spannable = (Spannable) text;
            if (index <= text.length()) {
                Selection.setSelection(spannable, index);
                return;
            }
            try {
                throw new Exception("the cursor of EditText is indexOutOfBoundException!!!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isInMarketStack(Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getApplicationContext().getSystemService("activity")).getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            return false;
        }
        return context.getPackageName().equals(runningTasks.get(0).baseActivity.getPackageName());
    }

    public static boolean isInVisibleRect(View view) {
        return view.getLocalVisibleRect(new Rect()) && view.getVisibility() == 0 && view.isShown();
    }

    public static boolean isLargeScreenBaseOnRealSize(Context context) {
        return px2dip(context, getScreenWidthRealSize(context)) >= LARGE_WIDTH_DP;
    }

    public static boolean isSmallScreenBaseOnRealSize(Context context) {
        return px2dip(context, getScreenWidthRealSize(context)) < MEDIUM_WIDTH_DP;
    }

    public static GradientDrawable makeGradientDrawable(int index, int index_2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColors(new int[]{index, index_2});
        return gradientDrawable;
    }

    public static StateListDrawable makeSelector(Drawable drawable, Drawable doubleValue) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-16842908, -16842913, -16842919}, drawable);
        stateListDrawable.addState(new int[]{-16842908, 16842913, -16842919}, doubleValue);
        stateListDrawable.addState(new int[]{16842908, -16842913, -16842919}, doubleValue);
        stateListDrawable.addState(new int[]{16842908, 16842913, -16842919}, doubleValue);
        stateListDrawable.addState(new int[]{16842913, 16842919}, doubleValue);
        stateListDrawable.addState(new int[]{16842919}, doubleValue);
        return stateListDrawable;
    }

    public static GradientDrawable makeShapeDrawable(float value, int index, int index_2, int index_3) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(index_3);
        if (value > 0.0f) {
            gradientDrawable.setCornerRadius(value);
        }
        if (index > 0) {
            gradientDrawable.setStroke(index, index_2);
        }
        return gradientDrawable;
    }

    public static int px2dip(Context context, int index) {
        return Math.round(index / context.getResources().getDisplayMetrics().density);
    }

    public static String removeZero(String str) {
        return str.indexOf(".") > 0 ? str.replaceAll("0+?$", "").replaceAll("[.]$", "") : str;
    }

    public static void safeForceHasOverlappingRendering(View view, boolean flag) {
        if (view == null) {
            return;
        }
        view.forceHasOverlappingRendering(flag);
    }

    public static void safeSetOutlineAmbientShadowColor(View view, int index) {
        if (view == null) {
            return;
        }
        view.setOutlineAmbientShadowColor(index);
    }

    public static void safeSetOutlineSpotShadowColor(View view, int index) {
        if (view == null) {
            return;
        }
        view.setOutlineSpotShadowColor(index);
    }

    public static void setElevationToView(View view, int index, int index_2) {
        setElevationToView(view, index, index_2, view.getResources().getDimensionPixelOffset(R.dimen.support_shadow_size_level_for_lowerP));
    }

    public static void setMargin(View view, int index, int index_2) {
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                if (index == 0) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = index_2;
                } else if (index == 1) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = index_2;
                } else if (index == 2) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = index_2;
                } else if (index == 3) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = index_2;
                } else if (index == 4) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(index_2);
                } else if (index == 5) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(index_2);
                }
                view.setLayoutParams(layoutParams);
            }
        }
    }

    public static void setElevationToView(View view, int index, int index_2, int index_3) {
        if (view == null) {
            return;
        }
        view.setOutlineSpotShadowColor(index_2);
        view.setElevation(index);
    }

    public static StateListDrawable makeSelector(int index, int index_2) {
        return makeSelector(new ColorDrawable(index), new ColorDrawable(index_2));
    }
}
