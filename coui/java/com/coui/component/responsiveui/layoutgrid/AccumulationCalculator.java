package com.coui.component.responsiveui.layoutgrid;

import com.coui.component.responsiveui.unit.Dp;

public final class AccumulationCalculator implements IColumnsWidthCalculator {
    @Override
    public Dp[] calculate(Dp layoutGridWidth, Dp margin, Dp gutter, int columnCount) {
        Dp[] widths = new Dp[columnCount];
        for (int i = 0; i < columnCount; i++) {
            widths[i] = new Dp(0.0f);
        }
        float gaps = columnCount - 1;
        if ((gutter.getValue() * gaps) + (margin.getValue() * 2.0f) > layoutGridWidth.getValue()) {
            return widths;
        }
        double rawColumnWidth = (layoutGridWidth.getValue() - (margin.getValue() * 2.0f) - (gaps * gutter.getValue())) / columnCount;
        double accumulated = 0.0d;
        for (int i = 0; i < columnCount; i++) {
            int value = (int) Math.round(((i + 1) * rawColumnWidth) - accumulated);
            widths[i] = new Dp(value);
            accumulated += value;
        }
        return widths;
    }

    @Override
    public int[] calculate(int layoutGridWidth, int margin, int gutter, int columnCount) {
        int[] widths = new int[columnCount];
        double gaps = (columnCount - 1) * gutter;
        double margins = margin * 2.0d;
        if (gaps + margins > layoutGridWidth) {
            return widths;
        }
        double rawColumnWidth = (layoutGridWidth - margins - gaps) / columnCount;
        double accumulated = 0.0d;
        for (int i = 0; i < columnCount; i++) {
            int value = (int) Math.round(((i + 1) * rawColumnWidth) - accumulated);
            widths[i] = value;
            accumulated += value;
        }
        return widths;
    }
}
