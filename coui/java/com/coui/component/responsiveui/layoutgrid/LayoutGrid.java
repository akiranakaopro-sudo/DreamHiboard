package com.coui.component.responsiveui.layoutgrid;

import java.util.Arrays;

final class LayoutGrid {
    private int columnCount;
    private int[][] columnsWidth;
    private int gutter;
    private int[] margin;

    LayoutGrid(int columnCount, int[][] columnsWidth, int gutter, int[] margin) {
        this.columnCount = columnCount;
        this.columnsWidth = columnsWidth;
        this.gutter = gutter;
        this.margin = margin;
    }

    int getColumnCount() {
        return columnCount;
    }

    int[][] getColumnsWidth() {
        return columnsWidth;
    }

    int getGutter() {
        return gutter;
    }

    int[] getMargin() {
        return margin;
    }

    @Override
    public String toString() {
        return "[LayoutGrid] columnCount = " + columnCount + ", gutter = " + gutter
                + ", margins = " + Arrays.toString(margin) + ", columnWidth = " + Arrays.deepToString(columnsWidth);
    }
}
