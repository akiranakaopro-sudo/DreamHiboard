package com.coui.component.responsiveui.window;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.coui.component.responsiveui.breakpoints.Breakpoints;
import com.coui.component.responsiveui.unit.Dp;

public final class WindowHeightSizeClass {
    public static final Companion Companion = new Companion();
    public static final WindowHeightSizeClass Compact = new WindowHeightSizeClass("Compact");
    public static final WindowHeightSizeClass Medium = new WindowHeightSizeClass("Medium");
    public static final WindowHeightSizeClass Expanded = new WindowHeightSizeClass("Expanded");
    private final String name;

    private WindowHeightSizeClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " window base-height";
    }

    public static final class Companion {
        public WindowHeightSizeClass _hide_fromHeight(float heightDp) {
            return heightDp < Breakpoints.BP_MEDIUM_WINDOW_BASE_HEIGHT.getValue()
                    ? Compact
                    : heightDp < Breakpoints.BP_EXPANDED_WINDOW_BASE_HEIGHT.getValue() ? Medium : Expanded;
        }

        public WindowHeightSizeClass fromHeight(Dp height) {
            if (height.getValue() >= 0.0f) {
                return _hide_fromHeight(height.getValue());
            }
            Log.e("WindowHeightSizeClass", "height :" + height.getValue() + " and Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
            return Compact;
        }

        public WindowHeightSizeClass fromHeight(Context context, int heightPx) {
            if (heightPx < 0) {
                Log.e("WindowHeightSizeClass", "height :" + heightPx + " and Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
                return Compact;
            }
            return _hide_fromHeight(heightPx / context.getResources().getDisplayMetrics().density);
        }
    }
}
