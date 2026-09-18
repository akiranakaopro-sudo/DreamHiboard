package com.coui.component.responsiveui.status;

import com.coui.component.responsiveui.window.LayoutGridWindowSize;
import com.coui.component.responsiveui.window.WindowSizeClass;

public final class WindowStatus implements IWindowStatus {
    private int orientation;
    private WindowSizeClass windowSizeClass;
    private LayoutGridWindowSize layoutGridWindowSize;

    public WindowStatus(int orientation, WindowSizeClass windowSizeClass, LayoutGridWindowSize layoutGridWindowSize) {
        this.orientation = orientation;
        this.windowSizeClass = windowSizeClass;
        this.layoutGridWindowSize = layoutGridWindowSize;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setWindowSizeClass(WindowSizeClass windowSizeClass) {
        this.windowSizeClass = windowSizeClass;
    }

    public WindowSizeClass getWindowSizeClass() {
        return windowSizeClass;
    }

    public void setLayoutGridWindowSize(LayoutGridWindowSize layoutGridWindowSize) {
        this.layoutGridWindowSize = layoutGridWindowSize;
    }

    @Override
    public LayoutGridWindowSize layoutGridWindowSize() {
        return layoutGridWindowSize;
    }

    @Override
    public int windowOrientation() {
        return orientation;
    }

    @Override
    public WindowSizeClass windowSizeClass() {
        return windowSizeClass;
    }

    @Override
    public String toString() {
        return "WindowStatus(orientation = " + orientation + ", windowSizeClass = " + windowSizeClass
                + ", windowSize = " + layoutGridWindowSize + ')';
    }
}
