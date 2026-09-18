package com.coui.component.responsiveui.proxy;

import android.content.Context;
import com.coui.component.responsiveui.IResponsiveUI;
import com.coui.component.responsiveui.layoutgrid.ILayoutGrid;
import com.coui.component.responsiveui.layoutgrid.LayoutGridSystem;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import com.coui.component.responsiveui.status.WindowStatus;
import com.coui.component.responsiveui.unit.DpKt;
import com.coui.component.responsiveui.window.LayoutGridWindowSize;
import com.coui.component.responsiveui.window.WindowSizeClass;

public final class ResponsiveUIProxy implements IResponsiveUI {
    private final WindowStatus windowStatus;
    private final LayoutGridSystem layoutGridSystem;

    public ResponsiveUIProxy(LayoutGridSystem layoutGridSystem, WindowStatus windowStatus) {
        this.layoutGridSystem = layoutGridSystem;
        this.windowStatus = windowStatus;
    }

    @Override
    public void onConfigurationChanged(Context context, LayoutGridWindowSize windowSize) {
        rebuild(context, windowSize);
    }

    @Override
    public void rebuild(Context context, LayoutGridWindowSize windowSize) {
        windowStatus.setOrientation(context.getResources().getConfiguration().orientation);
        windowStatus.setLayoutGridWindowSize(windowSize);
        windowStatus.setWindowSizeClass(WindowSizeClass.Companion.calculateFromSize(
                DpKt.pixel2Dp(windowSize.getWidth(), context),
                DpKt.pixel2Dp(windowSize.getHeight(), context)));
        layoutGridSystem.rebuild(context, windowStatus.getWindowSizeClass(), windowSize.getWidth());
    }

    @Override
    public String showLayoutGridInfo() {
        return String.valueOf(layoutGridSystem);
    }

    @Override
    public String showWindowStatusInfo() {
        return String.valueOf(windowStatus);
    }

    @Override
    public int[][] allColumnWidth() {
        return layoutGridSystem.allColumnWidth();
    }

    @Override
    public int[] allMargin() {
        return layoutGridSystem.allMargin();
    }

    @Override
    public ILayoutGrid chooseMargin(MarginType marginType) {
        return layoutGridSystem.chooseMargin(marginType);
    }

    @Override
    public int columnCount() {
        return layoutGridSystem.columnCount();
    }

    @Override
    public int[] columnWidth() {
        return layoutGridSystem.columnWidth();
    }

    @Override
    public int gutter() {
        return layoutGridSystem.gutter();
    }

    @Override
    public int layoutGridWindowWidth() {
        return layoutGridSystem.layoutGridWindowWidth();
    }

    @Override
    public int margin() {
        return layoutGridSystem.margin();
    }

    @Override
    public int width(int startColumn, int endColumn) {
        return layoutGridSystem.width(startColumn, endColumn);
    }

    @Override
    public LayoutGridWindowSize layoutGridWindowSize() {
        return windowStatus.layoutGridWindowSize();
    }

    @Override
    public int windowOrientation() {
        return windowStatus.windowOrientation();
    }

    @Override
    public WindowSizeClass windowSizeClass() {
        return windowStatus.windowSizeClass();
    }
}
