package com.coui.component.responsiveui.layoutgrid;

import com.coui.appcompat.R;

public enum MarginType {
    MARGIN_SMALL(R.dimen.layout_grid_margin_compat_small, R.dimen.layout_grid_margin_medium_small, R.dimen.layout_grid_margin_expanded_small),
    MARGIN_LARGE(R.dimen.layout_grid_margin_compat_large, R.dimen.layout_grid_margin_medium_large, R.dimen.layout_grid_margin_expanded_large);

    private final int[] resIds;

    MarginType(int compactResId, int mediumResId, int expandedResId) {
        this.resIds = new int[]{compactResId, mediumResId, expandedResId};
    }

    public int[] resId() {
        return resIds;
    }
}
