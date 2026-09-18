package com.coui.component.responsiveui.layoutgrid;

import com.coui.component.responsiveui.unit.Dp;

public interface IColumnsWidthCalculator {
    int[] calculate(int layoutGridWidth, int margin, int gutter, int columnCount);

    Dp[] calculate(Dp layoutGridWidth, Dp margin, Dp gutter, int columnCount);
}
