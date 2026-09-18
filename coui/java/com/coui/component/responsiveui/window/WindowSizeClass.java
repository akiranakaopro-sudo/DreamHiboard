package com.coui.component.responsiveui.window;

import com.coui.component.responsiveui.unit.Dp;

public final class WindowSizeClass {
    public static final Companion Companion = new Companion();
    private final WindowWidthSizeClass widthSizeClass;
    private final WindowHeightSizeClass heightSizeClass;
    private final WindowTotalSizeClass totalSizeClass;

    public WindowSizeClass(WindowWidthSizeClass widthSizeClass, WindowHeightSizeClass heightSizeClass, WindowTotalSizeClass totalSizeClass) {
        this.widthSizeClass = widthSizeClass;
        this.heightSizeClass = heightSizeClass;
        this.totalSizeClass = totalSizeClass;
    }

    public WindowWidthSizeClass getWindowWidthSizeClass() {
        return widthSizeClass;
    }

    public WindowHeightSizeClass getWindowHeightSizeClass() {
        return heightSizeClass;
    }

    public WindowTotalSizeClass getWindowTotalSizeClass() {
        return totalSizeClass;
    }

    @Override
    public String toString() {
        return "WindowSizeClass(" + widthSizeClass + ", " + heightSizeClass + ", " + totalSizeClass + ')';
    }

    public static final class Companion {
        public WindowSizeClass calculateFromSize(Dp width, Dp height) {
            return new WindowSizeClass(WindowWidthSizeClass.Companion.fromWidth(width),
                    WindowHeightSizeClass.Companion.fromHeight(height),
                    WindowTotalSizeClass.Companion.fromWidthAndHeight(width, height));
        }
    }
}
