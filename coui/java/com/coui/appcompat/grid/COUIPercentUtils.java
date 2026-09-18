package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.Resources;


@Deprecated
public class COUIPercentUtils {
    private static final int CARD_LIST_FLAG = 2;
    private static final int LARGE_SCRREN_GRID_SIZE = 12;
    private static final int LARGE_SCRREN_WIDTH_LIMIT = 840;
    private static final int MEDIUM_SCRREN_GRID_SIZE = 8;
    private static final int MEDIUM_SCRREN_WIDTH_LIMIT = 600;
    private static final int PADDING_COUNT = 2;
    private static final int PREFERENCE_FLAG = 1;
    private static final int SMALL_SCRREN_GRID_SIZE = 4;
    private static final int SMALL_SCRREN_WIDTH_LIMIT = 480;

    @Deprecated
    public static float calculateWidth(float width, int gridNumber, int totalGridNumber, int typeFlag, Context context) {
        if (gridNumber <= 0 || gridNumber > totalGridNumber) {
            return width;
        }
        boolean addPadding = typeFlag == PREFERENCE_FLAG || typeFlag == CARD_LIST_FLAG;
        int horizontalMargin = (typeFlag == CARD_LIST_FLAG ? context.getResources().getDimensionPixelOffset(R.dimen.grid_guide_column_card_margin_start) : context.getResources().getDimensionPixelOffset(R.dimen.grid_guide_column_default_margin_start)) * PADDING_COUNT;
        Resources resources = context.getResources();
        int columnGap = R.dimen.grid_guide_column_gap;
        return ((((width - horizontalMargin) - ((totalGridNumber - 1) * resources.getDimensionPixelOffset(columnGap))) / totalGridNumber) * gridNumber) + (context.getResources().getDimensionPixelOffset(columnGap) * Math.max(gridNumber - 1, 0)) + (addPadding ? horizontalMargin : 0);
    }

    @Deprecated
    public static int getTotalGridSize(Context context) {
        int screenWidthDp = context.getResources().getConfiguration().screenWidthDp;
        if (screenWidthDp < MEDIUM_SCRREN_WIDTH_LIMIT) {
            return SMALL_SCRREN_GRID_SIZE;
        }
        if (screenWidthDp < LARGE_SCRREN_WIDTH_LIMIT) {
            return MEDIUM_SCRREN_GRID_SIZE;
        }
        return screenWidthDp > LARGE_SCRREN_WIDTH_LIMIT ? LARGE_SCRREN_GRID_SIZE : SMALL_SCRREN_GRID_SIZE;
    }
}
