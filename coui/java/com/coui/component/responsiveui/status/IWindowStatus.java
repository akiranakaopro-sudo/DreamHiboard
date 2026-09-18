package com.coui.component.responsiveui.status;

import com.coui.component.responsiveui.window.LayoutGridWindowSize;
import com.coui.component.responsiveui.window.WindowSizeClass;

public interface IWindowStatus {
    LayoutGridWindowSize layoutGridWindowSize();

    int windowOrientation();

    WindowSizeClass windowSizeClass();
}
