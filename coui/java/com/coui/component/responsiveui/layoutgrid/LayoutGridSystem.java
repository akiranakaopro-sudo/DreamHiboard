package com.coui.component.responsiveui.layoutgrid;

import android.content.Context;
import com.coui.appcompat.R;
import com.coui.component.responsiveui.window.WindowSizeClass;
import com.coui.component.responsiveui.window.WindowTotalSizeClass;

public final class LayoutGridSystem implements ILayoutGrid {
    private final int[] margins = new int[MarginType.values().length];
    private int gutter;
    private int layoutGridWindowWidth;
    private LayoutGrid layoutGrid;
    private MarginType marginType = MarginType.MARGIN_LARGE;
    private final IColumnsWidthCalculator calculator = new AccumulationCalculator();

    public LayoutGridSystem(Context context, WindowSizeClass windowSizeClass, int width) {
        rebuild(context, windowSizeClass, width);
    }

    private LayoutGrid calculateLayoutGrid(WindowTotalSizeClass totalSizeClass) {
        int columnCount = totalSizeClass == WindowTotalSizeClass.Compact ? 4
                : (totalSizeClass == WindowTotalSizeClass.MediumLandScape
                || totalSizeClass == WindowTotalSizeClass.MediumPortrait
                || totalSizeClass == WindowTotalSizeClass.MediumSquare
                || totalSizeClass == WindowTotalSizeClass.ExpandedLandPortrait
                || totalSizeClass == WindowTotalSizeClass.ExpandedPortrait) ? 8 : 12;
        int[][] columns = new int[MarginType.values().length][columnCount];
        for (MarginType type : MarginType.values()) {
            columns[type.ordinal()] = calculator.calculate(layoutGridWindowWidth, margins[type.ordinal()], gutter, columnCount);
        }
        return new LayoutGrid(columnCount, columns, gutter, margins);
    }

    public void rebuild(Context context, WindowSizeClass windowSizeClass, int width) {
        for (MarginType type : MarginType.values()) {
            WindowTotalSizeClass totalSizeClass = windowSizeClass.getWindowTotalSizeClass();
            int resIndex = totalSizeClass == WindowTotalSizeClass.Compact ? 0 : totalSizeClass == WindowTotalSizeClass.Expanded ? 2 : 1;
            margins[type.ordinal()] = context.getResources().getDimensionPixelSize(type.resId()[resIndex]);
        }
        gutter = windowSizeClass.getWindowTotalSizeClass() == WindowTotalSizeClass.Expanded
                ? context.getResources().getDimensionPixelSize(R.dimen.layout_grid_gutter_expanded)
                : context.getResources().getDimensionPixelSize(R.dimen.layout_grid_gutter);
        layoutGridWindowWidth = width;
        layoutGrid = calculateLayoutGrid(windowSizeClass.getWindowTotalSizeClass());
    }

    @Override
    public int[][] allColumnWidth() {
        return layoutGrid.getColumnsWidth();
    }

    @Override
    public int[] allMargin() {
        return layoutGrid.getMargin();
    }

    @Override
    public ILayoutGrid chooseMargin(MarginType marginType) {
        this.marginType = marginType;
        return this;
    }

    @Override
    public int columnCount() {
        return layoutGrid.getColumnCount();
    }

    @Override
    public int[] columnWidth() {
        return layoutGrid.getColumnsWidth()[marginType.ordinal()];
    }

    @Override
    public int gutter() {
        return layoutGrid.getGutter();
    }

    @Override
    public int layoutGridWindowWidth() {
        return layoutGridWindowWidth;
    }

    @Override
    public int margin() {
        return layoutGrid.getMargin()[marginType.ordinal()];
    }

    @Override
    public int width(int startColumn, int endColumn) {
        int start = Math.min(startColumn, endColumn);
        int end = Math.max(startColumn, endColumn);
        if (start < 0) {
            throw new IllegalArgumentException("column index must not be negative");
        }
        if (end >= layoutGrid.getColumnCount()) {
            throw new IllegalArgumentException("column index must be less than " + layoutGrid.getColumnCount());
        }
        int width = (end - start) * layoutGrid.getGutter();
        int[] columns = layoutGrid.getColumnsWidth()[marginType.ordinal()];
        for (int i = start; i <= end; i++) {
            width += columns[i];
        }
        return width;
    }

    @Override
    public String toString() {
        return "layout-grid width = " + layoutGridWindowWidth + ", current margin = " + margin() + ", " + layoutGrid;
    }
}
