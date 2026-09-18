package com.coui.component.responsiveui.window;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.coui.component.responsiveui.breakpoints.Breakpoints;
import com.coui.component.responsiveui.unit.Dp;

public final class WindowTotalSizeClass {
    public static final Companion Companion = new Companion();
    public static final WindowTotalSizeClass Compact = new WindowTotalSizeClass("Compact");
    public static final WindowTotalSizeClass MediumLandScape = new WindowTotalSizeClass("MediumLandScape");
    public static final WindowTotalSizeClass MediumSquare = new WindowTotalSizeClass("MediumSquare");
    public static final WindowTotalSizeClass MediumPortrait = new WindowTotalSizeClass("MediumPortrait");
    public static final WindowTotalSizeClass Expanded = new WindowTotalSizeClass("Expanded");
    public static final WindowTotalSizeClass ExpandedLandPortrait = new WindowTotalSizeClass("ExpandedLandPortrait");
    public static final WindowTotalSizeClass ExpandedPortrait = new WindowTotalSizeClass("ExpandedPortrait");
    private final String name;

    private WindowTotalSizeClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " window base-total";
    }

    public static final class Companion {
        private WindowTotalSizeClass classify(float widthDp, float heightDp) {
            WindowWidthSizeClass widthClass = WindowWidthSizeClass.Companion._hide_fromWidth(widthDp);
            if (widthClass == WindowWidthSizeClass.Compact) {
                return Compact;
            }
            if (widthClass == WindowWidthSizeClass.Medium) {
                WindowHeightSizeClass heightClass = WindowHeightSizeClass.Companion._hide_fromHeight(heightDp);
                return heightClass == WindowHeightSizeClass.Compact ? MediumLandScape : heightClass == WindowHeightSizeClass.Medium ? MediumSquare : MediumPortrait;
            }
            WindowHeightSizeClass heightClass = WindowHeightSizeClass.Companion._hide_fromHeight(heightDp);
            if (heightClass == WindowHeightSizeClass.Compact) {
                return ExpandedLandPortrait;
            }
            if (heightClass != WindowHeightSizeClass.Medium && heightDp > widthDp && widthDp < Breakpoints.BP_EXPANDED_WINDOW_MAXIMUM_WIDTH.getValue()) {
                return ExpandedPortrait;
            }
            return Expanded;
        }

        public WindowTotalSizeClass fromWidthAndHeight(Dp width, Dp height) {
            if (width.getValue() >= 0.0f && height.getValue() >= 0.0f) {
                return classify(width.getValue(), height.getValue());
            }
            Log.e("WindowHeightSizeClass", "width :" + width.getValue() + " height :" + height.getValue() + " and Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
            return Compact;
        }

        public WindowTotalSizeClass fromWidthAndHeight(Context context, int widthPx, int heightPx) {
            if (widthPx >= 0 && heightPx >= 0) {
                float density = context.getResources().getDisplayMetrics().density;
                return classify(widthPx / density, heightPx / density);
            }
            Log.e("WindowHeightSizeClass", "width :" + widthPx + " height :" + heightPx + " and Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
            return Compact;
        }
    }
}
