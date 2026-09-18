package com.coui.component.responsiveui.layoutgrid;

public interface ILayoutGrid {
    int[][] allColumnWidth();

    int[] allMargin();

    ILayoutGrid chooseMargin(MarginType marginType);

    int columnCount();

    int[] columnWidth();

    int gutter();

    int layoutGridWindowWidth();

    int margin();

    int width(int startColumn, int endColumn);
}
