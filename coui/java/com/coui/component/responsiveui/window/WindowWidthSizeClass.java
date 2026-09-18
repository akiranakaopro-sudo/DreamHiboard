package com.coui.component.responsiveui.window;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.coui.component.responsiveui.breakpoints.Breakpoints;
import com.coui.component.responsiveui.unit.Dp;

public final class WindowWidthSizeClass {
    public static final Companion Companion = new Companion();
    public static final WindowWidthSizeClass Compact = new WindowWidthSizeClass("Compact");
    public static final WindowWidthSizeClass Medium = new WindowWidthSizeClass("Medium");
    public static final WindowWidthSizeClass Expanded = new WindowWidthSizeClass("Expanded");
    private final String name;

    private WindowWidthSizeClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " window base-width";
    }

    public static final class Companion {
        public WindowWidthSizeClass _hide_fromWidth(float widthDp) {
            return widthDp < Breakpoints.BP_MEDIUM_WINDOW_BASE_WIDTH.getValue()
                    ? Compact
                    : widthDp < Breakpoints.BP_EXPANDED_WINDOW_BASE_WIDTH.getValue() ? Medium : Expanded;
        }

        public WindowWidthSizeClass fromWidth(Dp width) {
            if (width.getValue() >= 0.0f) {
                return _hide_fromWidth(width.getValue());
            }
            Log.e("WindowWidthSizeClass", "width :" + width.getValue() + " and Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
            return Compact;
        }

        public WindowWidthSizeClass fromWidth(Context context, int widthPx) {
            if (widthPx < 0) {
                Log.e("WindowWidthSizeClass", "width :" + widthPx + " and Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
                return Compact;
            }
            return _hide_fromWidth(widthPx / context.getResources().getDisplayMetrics().density);
        }
    }
}
