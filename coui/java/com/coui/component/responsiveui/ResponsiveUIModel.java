package com.coui.component.responsiveui;

import android.content.Context;
import android.content.res.Configuration;
import com.coui.component.responsiveui.layoutgrid.LayoutGridSystem;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import com.coui.component.responsiveui.proxy.ResponsiveUIProxy;
import com.coui.component.responsiveui.status.WindowStatus;
import com.coui.component.responsiveui.unit.Dp;
import com.coui.component.responsiveui.unit.DpKt;
import com.coui.component.responsiveui.window.LayoutGridWindowSize;
import com.coui.component.responsiveui.window.WindowSizeClass;

public final class ResponsiveUIModel {
    private final Context context;
    private LayoutGridWindowSize windowSize;
    private final ResponsiveUIProxy responsiveUIProxy;

    public ResponsiveUIModel(Context context, LayoutGridWindowSize windowSize) {
        this.context = context;
        this.windowSize = windowSize;
        WindowStatus windowStatus = new WindowStatus(context.getResources().getConfiguration().orientation,
                WindowSizeClass.Companion.calculateFromSize(DpKt.pixel2Dp(windowSize.getWidth(), context), DpKt.pixel2Dp(windowSize.getHeight(), context)),
                new LayoutGridWindowSize(windowSize));
        LayoutGridSystem layoutGridSystem = new LayoutGridSystem(context, windowStatus.windowSizeClass(), windowSize.getWidth());
        this.responsiveUIProxy = new ResponsiveUIProxy(layoutGridSystem, windowStatus);
    }

    public ResponsiveUIModel(Context context, float widthDp, float heightDp) {
        this(context, new LayoutGridWindowSize(context, new Dp(widthDp), new Dp(heightDp)));
    }

    public ResponsiveUIModel(Context context, int widthPx, int heightPx) {
        this(context, new LayoutGridWindowSize(widthPx, heightPx));
    }

    public int[][] allColumnWidth() {
        return responsiveUIProxy.allColumnWidth();
    }

    public int[] allMargin() {
        return responsiveUIProxy.allMargin();
    }

    public int calculateGridWidth(int gridNumber) {
        if (gridNumber > responsiveUIProxy.columnCount()) {
            gridNumber = responsiveUIProxy.columnCount();
        }
        int startColumn = (responsiveUIProxy.columnCount() - gridNumber) / 2;
        return responsiveUIProxy.width(startColumn, (gridNumber + startColumn) - 1);
    }

    public ResponsiveUIModel chooseMargin(MarginType marginType) {
        responsiveUIProxy.chooseMargin(marginType);
        return this;
    }

    public int columnCount() {
        return responsiveUIProxy.columnCount();
    }

    public int[] columnWidth() {
        return responsiveUIProxy.columnWidth();
    }

    public IResponsiveUI getResponsiveUI() {
        return responsiveUIProxy;
    }

    public int gutter() {
        return responsiveUIProxy.gutter();
    }

    public LayoutGridWindowSize layoutGridWindowSize() {
        return responsiveUIProxy.layoutGridWindowSize();
    }

    public int layoutGridWindowWidth() {
        return responsiveUIProxy.layoutGridWindowWidth();
    }

    public int margin() {
        return responsiveUIProxy.margin();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        windowSize.setWidth((int) new Dp(newConfig.screenWidthDp).toPixel(context));
        windowSize.setHeight((int) new Dp(newConfig.screenWidthDp).toPixel(context));
        responsiveUIProxy.rebuild(context, windowSize);
    }

    public ResponsiveUIModel rebuild(LayoutGridWindowSize windowSize) {
        this.windowSize = windowSize;
        responsiveUIProxy.rebuild(context, windowSize);
        return this;
    }

    public ResponsiveUIModel rebuild(float widthDp, float heightDp) {
        windowSize.setWidth((int) new Dp(widthDp).toPixel(context));
        windowSize.setHeight((int) new Dp(heightDp).toPixel(context));
        responsiveUIProxy.rebuild(context, windowSize);
        return this;
    }

    public ResponsiveUIModel rebuild(int widthPx, int heightPx) {
        windowSize.setWidth(widthPx);
        windowSize.setHeight(heightPx);
        responsiveUIProxy.rebuild(context, windowSize);
        return this;
    }

    public String showLayoutGridInfo() {
        return responsiveUIProxy.showLayoutGridInfo();
    }

    public String showWindowStatusInfo() {
        return responsiveUIProxy.showWindowStatusInfo();
    }

    public int width(int startColumn, int endColumn) {
        return responsiveUIProxy.width(startColumn, endColumn);
    }

    public int windowOrientation() {
        return responsiveUIProxy.windowOrientation();
    }

    public WindowSizeClass windowSizeClass() {
        return responsiveUIProxy.windowSizeClass();
    }
}
