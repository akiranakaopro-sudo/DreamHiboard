package com.coui.appcompat.tintimageview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import androidx.core.graphics.drawable.DrawableCompat;
import com.coui.appcompat.R;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public final class COUITintManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "TintManager";
    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};
    private static final int[] PRESSED_STATE_SET = {android.R.attr.state_pressed};
    private static final int[] NOT_PRESSED_OR_FOCUSED_STATE_SET = {-android.R.attr.state_pressed, -android.R.attr.state_focused};
    private final WeakReference<Context> mContextRef;
    private ColorStateList mDefaultColorStateList;
    private SparseArray<ColorStateList> mTintLists;
    public static final boolean SHOULD_BE_USED = false;
    private static final PorterDuff.Mode DEFAULT_MODE = PorterDuff.Mode.SRC_IN;
    private static final WeakHashMap<Context, COUITintManager> INSTANCE_CACHE = new WeakHashMap<>();
    private static final ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);

    public static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {
        public ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }

        private static int generateCacheKey(int color, PorterDuff.Mode mode) {
            return ((color + 31) * 31) + mode.hashCode();
        }

        public PorterDuffColorFilter get(int color, PorterDuff.Mode mode) {
            return get(Integer.valueOf(generateCacheKey(color, mode)));
        }

        public PorterDuffColorFilter put(int color, PorterDuff.Mode mode, PorterDuffColorFilter colorFilter) {
            return put(Integer.valueOf(generateCacheKey(color, mode)), colorFilter);
        }
    }

    private COUITintManager(Context context) {
        this.mContextRef = new WeakReference<>(context);
    }

    private ColorStateList createCOUIDefaultColorStateList(Context context) {
        return new ColorStateList(
                new int[][]{DISABLED_STATE_SET, PRESSED_STATE_SET, NOT_PRESSED_OR_FOCUSED_STATE_SET},
                new int[]{
                        getDisabledThemeAttrColor(context, R.attr.couiColorDisabledNeutral),
                        getThemeAttrColor(context, R.attr.couiColorPressBackground),
                        getThemeAttrColor(context, R.attr.couiColorPrimary)
                });
    }

    public static COUITintManager get(Context context) {
        WeakHashMap<Context, COUITintManager> weakHashMap = INSTANCE_CACHE;
        COUITintManager tintManager = weakHashMap.get(context);
        if (tintManager != null) {
            return tintManager;
        }
        COUITintManager newTintManager = new COUITintManager(context);
        weakHashMap.put(context, newTintManager);
        return newTintManager;
    }

    public static Drawable getDrawable(Context context, int resId) {
        return context.getDrawable(resId);
    }

    private static int getDisabledThemeAttrColor(Context context, int attr) {
        ColorStateList colorStateList = getThemeAttrColorStateList(context, attr);
        if (colorStateList != null && colorStateList.isStateful()) {
            return colorStateList.getColorForState(DISABLED_STATE_SET, colorStateList.getDefaultColor());
        }
        return getThemeAttrColor(context, attr);
    }

    private static int getThemeAttrColor(Context context, int attr) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{attr});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    private static ColorStateList getThemeAttrColorStateList(Context context, int attr) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{attr});
        ColorStateList colorStateList = typedArray.getColorStateList(0);
        typedArray.recycle();
        return colorStateList;
    }

    private static void setPorterDuffColorFilter(Drawable drawable, int color, PorterDuff.Mode mode) {
        if (mode == null) {
            mode = DEFAULT_MODE;
        }
        ColorFilterLruCache colorFilterLruCache = COLOR_FILTER_CACHE;
        PorterDuffColorFilter porterDuffColorFilter = colorFilterLruCache.get(color, mode);
        if (porterDuffColorFilter == null) {
            porterDuffColorFilter = new PorterDuffColorFilter(color, mode);
            colorFilterLruCache.put(color, mode, porterDuffColorFilter);
        }
        drawable.setColorFilter(porterDuffColorFilter);
    }

    public static void tintViewBackground(View view, COUITintInfo tintInfo) {
        Drawable background = view.getBackground();
        if (tintInfo.mHasTintList) {
            setPorterDuffColorFilter(background, tintInfo.mTintList.getColorForState(view.getDrawableState(), tintInfo.mTintList.getDefaultColor()), tintInfo.mHasTintMode ? tintInfo.mTintMode : null);
        } else {
            background.clearColorFilter();
        }
    }

    public final ColorStateList getTintList(int resId) {
        Context context = this.mContextRef.get();
        if (context == null) {
            return null;
        }
        SparseArray<ColorStateList> sparseArray = this.mTintLists;
        ColorStateList colorStateList = sparseArray != null ? sparseArray.get(resId) : null;
        if (colorStateList != null) {
            if (this.mTintLists == null) {
                this.mTintLists = new SparseArray<>();
            }
            this.mTintLists.append(resId, colorStateList);
        }
        return colorStateList;
    }

    public final PorterDuff.Mode getTintMode(int resId) {
        return null;
    }

    public final boolean tintDrawableUsingColorFilter(int resId, Drawable drawable) {
        this.mContextRef.get();
        return false;
    }

    public Drawable getDrawable(int resId) {
        return getDrawable(resId, false);
    }

    public Drawable getDrawable(int resId, boolean failIfNotKnown) {
        Context context = this.mContextRef.get();
        if (context == null) {
            return null;
        }
        Drawable drawable = context.getDrawable(resId);
        if (drawable != null) {
            drawable = drawable.mutate();
            ColorStateList tintList = getTintList(resId);
            if (tintList != null) {
                Drawable drawableR = DrawableCompat.wrap(drawable);
                DrawableCompat.setTintList(drawableR, tintList);
                PorterDuff.Mode tintMode = getTintMode(resId);
                if (tintMode == null) {
                    return drawableR;
                }
                DrawableCompat.setTintMode(drawableR, tintMode);
                return drawableR;
            }
            if (!tintDrawableUsingColorFilter(resId, drawable) && failIfNotKnown) {
                return null;
            }
        }
        return drawable;
    }
}
