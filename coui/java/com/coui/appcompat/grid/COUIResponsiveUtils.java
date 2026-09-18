package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.coui.appcompat.dialog.AppFeatureUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.component.responsiveui.ResponsiveUIModel;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import com.coui.component.responsiveui.unit.Dp;
import com.coui.component.responsiveui.window.WindowSizeClass;
import com.coui.component.responsiveui.window.WindowTotalSizeClass;
import com.coui.component.responsiveui.window.WindowWidthSizeClass;
import java.util.Arrays;


public class COUIResponsiveUtils {
    private static final int AUTO_GRID_NUMBER = -1;
    private static final int CARD_LIST_FLAG = 2;
    private static boolean DEBUG = false;
    private static final int DEFAULT_COLUMNS_FOR_CHILD = 8;
    private static final int DEFAULT_COLUMNS_FOR_COMPAT = 4;
    private static final int DEFAULT_COLUMNS_FOR_EXPANDED = 8;
    private static final int DEFAULT_COLUMNS_FOR_MEDIUM = 6;
    private static final int DEFAULT_FLAG = 0;
    private static final int LARGE_PADDING = 0;
    private static final int LIST_FLAG = 1;
    private static final int MARGIN_LARGE_DP_IN_LARGE_SCREEN = 40;
    private static final int MARGIN_LARGE_DP_IN_NON_LARGE_SCREEN = 24;
    private static final int PADDING_COUNT = 2;
    private static final int PADDING_MODE = 0;
    private static final int REMEASURE_MODE = 1;
    private static final int SMALL_PADDING = 1;
    private static final String TAG = "COUIResponsiveUtils";
    private static int sCouiFoldType;
    private static final Rect sRect = new Rect();
    private static final Point sPoint = new Point();

    static {
        DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
        sCouiFoldType = -1;
    }

    public static void calculatePadding(ResponsiveUIModel responsiveUIModel, int gridNumber, int specialPadding, boolean addPadding, float[] padding) {
        int margin = responsiveUIModel.margin();
        int gutter = responsiveUIModel.gutter();
        int columnCount = responsiveUIModel.columnCount();
        int[] columnWidth = responsiveUIModel.columnWidth();
        int startColumn = (columnCount - gridNumber) / 2;
        if (addPadding) {
            margin -= specialPadding;
        }
        float marginValue = margin;
        padding[1] = marginValue;
        padding[0] = marginValue;
        for (int index = 0; index < startColumn; index++) {
            padding[0] = padding[0] + columnWidth[index];
            padding[1] = padding[1] + columnWidth[(columnCount - index) - 1];
        }
        float gutterWidth = startColumn * gutter;
        padding[0] = padding[0] + gutterWidth;
        padding[1] = padding[1] + gutterWidth;
    }

    public static float calculateWidth(ResponsiveUIModel responsiveUIModel, int gridNumber, int specialPadding, boolean addPadding) {
        int startColumn = (responsiveUIModel.columnCount() - gridNumber) / 2;
        float width = responsiveUIModel.width(startColumn, (gridNumber + startColumn) - 1);
        if (DEBUG) {
            Log.d(TAG, "calculateWidth: width = " + width);
        }
        if (!addPadding) {
            specialPadding = 0;
        }
        return width + (specialPadding * PADDING_COUNT);
    }

    public static int getChildLayerDefaultTypeMargin(Context context, int width) {
        return (int) ((isLargeScreen(context, width) ? MARGIN_LARGE_DP_IN_LARGE_SCREEN : MARGIN_LARGE_DP_IN_NON_LARGE_SCREEN) * context.getResources().getDisplayMetrics().density);
    }

    public static int getDefaultGridNumbers(ResponsiveUIModel responsiveUIModel) {
        WindowTotalSizeClass windowTotalSizeClass = responsiveUIModel.windowSizeClass().getWindowTotalSizeClass();
        if (windowTotalSizeClass.equals(WindowTotalSizeClass.Compact)) {
            return DEFAULT_COLUMNS_FOR_COMPAT;
        }
        if (windowTotalSizeClass.equals(WindowTotalSizeClass.Expanded)) {
            return DEFAULT_COLUMNS_FOR_EXPANDED;
        }
        return (windowTotalSizeClass.equals(WindowTotalSizeClass.MediumLandScape) || windowTotalSizeClass.equals(WindowTotalSizeClass.MediumPortrait) || windowTotalSizeClass.equals(WindowTotalSizeClass.MediumSquare) || windowTotalSizeClass.equals(WindowTotalSizeClass.ExpandedLandPortrait) || windowTotalSizeClass.equals(WindowTotalSizeClass.ExpandedPortrait)) ? DEFAULT_COLUMNS_FOR_MEDIUM : DEFAULT_COLUMNS_FOR_COMPAT;
    }

    public static int getScreenPhysicalHeight(Activity activity) {
        return activity.getWindowManager().getMaximumWindowMetrics().getBounds().height();
    }

    public static int getScreenPhysicalWidth(Activity activity) {
        return activity.getWindowManager().getMaximumWindowMetrics().getBounds().width();
    }

    @Deprecated
    public static boolean isActivityEmbedded(Context context) {
        return false;
    }

    public static boolean isLargePadWindow(Context context, int widthDp, int heightDp) {
        if (sCouiFoldType == -1) {
            sCouiFoldType = AppFeatureUtil.isFoldDisplayFeature(context) ? 1 : 0;
        }
        return (isLargeScreenDp(widthDp, heightDp) || isLargeScreenDp(heightDp, widthDp)) && sCouiFoldType != 1;
    }

    @Deprecated
    public static boolean isLargeScreen(Context context, int width) {
        return WindowWidthSizeClass.Companion.fromWidth(context, width) == WindowWidthSizeClass.Expanded;
    }

    @Deprecated
    public static boolean isLargeScreenDp(int widthDp) {
        return WindowWidthSizeClass.Companion.fromWidth(new Dp((float) widthDp)) == WindowWidthSizeClass.Expanded;
    }

    @Deprecated
    public static boolean isMediumScreen(Context context, int width) {
        return WindowWidthSizeClass.Companion.fromWidth(context, width) == WindowWidthSizeClass.Medium;
    }

    @Deprecated
    public static boolean isMediumScreenDp(int widthDp) {
        return WindowWidthSizeClass.Companion.fromWidth(new Dp((float) widthDp)) == WindowWidthSizeClass.Medium;
    }

    public static boolean isSmallScreen(Context context, int width) {
        return WindowWidthSizeClass.Companion.fromWidth(context, width) == WindowWidthSizeClass.Compact;
    }

    public static boolean isSmallScreenDp(int widthDp) {
        return WindowWidthSizeClass.Companion.fromWidth(new Dp((float) widthDp)) == WindowWidthSizeClass.Compact;
    }

    public static void measureChildWithPercent(Context context, View view, int widthMeasureSpec, int typeFlag, int paddingFlag, int gridNumber, int percentMode) {
        if (gridNumber != 0) {
            if (percentMode != 0) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = (int) calculateWidth(View.MeasureSpec.getSize(widthMeasureSpec), gridNumber, typeFlag, paddingFlag, context);
                view.setLayoutParams(layoutParams);
            } else {
                int horizontalPadding = (View.MeasureSpec.getSize(widthMeasureSpec) - ((int) calculateWidth(View.MeasureSpec.getSize(widthMeasureSpec), gridNumber, typeFlag, paddingFlag, context))) / PADDING_COUNT;
                if (view.getPaddingLeft() == horizontalPadding && view.getPaddingRight() == horizontalPadding) {
                    return;
                }
                view.setPaddingRelative(horizontalPadding, view.getPaddingTop(), horizontalPadding, view.getPaddingBottom());
            }
        }
    }

    public static int measureLayout(View view, int widthMeasureSpec, int requestGridNumber, int typeFlag, int paddingFlag, int mode, int initPaddingStart, int initPaddingEnd, int screenPhysicalWidth, boolean isParentChildHierarchy, boolean isActivityEmbedded) {
        MarginType marginType = paddingFlag == 1 ? MarginType.MARGIN_SMALL : MarginType.MARGIN_LARGE;
        view.getWindowVisibleDisplayFrame(sRect);
        boolean isAddPadding = typeFlag == LIST_FLAG || typeFlag == CARD_LIST_FLAG;
        int windowHeight = UIUtil.getScreenHeightMetrics(view.getContext());
        int screenWidth = Math.max(screenPhysicalWidth, 0);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        if (widthSize <= 0 || (widthMode != View.MeasureSpec.EXACTLY && widthMode != View.MeasureSpec.AT_MOST)) {
            if (DEBUG) {
                Log.d(TAG, "Skip measure because of parent measure unspecific: widthSize = " + widthSize + "widthMode = " + widthMode);
            }
            return widthMeasureSpec;
        }
        boolean isInLargeParentMode = (isParentChildHierarchy || isActivityEmbedded) && isLargeScreen(view.getContext(), screenWidth, windowHeight);
        ResponsiveUIModel responsiveUIModel = new ResponsiveUIModel(view.getContext(), widthSize, windowHeight).chooseMargin(marginType);
        int gridNumber = isInLargeParentMode ? responsiveUIModel.columnCount() : requestGridNumber;
        int columnCount = responsiveUIModel.columnCount();
        int margin = (isParentChildHierarchy || isActivityEmbedded) && typeFlag == DEFAULT_FLAG
                ? getChildLayerDefaultTypeMargin(view.getContext(), widthSize, windowHeight)
                : responsiveUIModel.margin();
        if (gridNumber == AUTO_GRID_NUMBER) {
            gridNumber = getDefaultGridNumbers(responsiveUIModel);
        } else {
            gridNumber = Math.min(gridNumber, columnCount);
        }
        int specialPadding = 0;
        if (isAddPadding) {
            specialPadding = view.getContext().getResources().getDimensionPixelOffset(
                    typeFlag == LIST_FLAG ? R.dimen.grid_list_special_padding : R.dimen.grid_card_special_padding);
        }
        float contentWidth = calculateWidth(responsiveUIModel, gridNumber, specialPadding, isAddPadding);
        float[] padding = new float[2];
        calculatePadding(responsiveUIModel, gridNumber, specialPadding, isAddPadding, padding);
        int horizontalMargins = margin * 2;
        float maxContentWidth = widthSize - horizontalMargins;
        if (contentWidth > maxContentWidth || ((isParentChildHierarchy || isActivityEmbedded) && typeFlag == DEFAULT_FLAG)) {
            contentWidth = maxContentWidth;
        }
        int startColumn = (columnCount - gridNumber) / 2;
        if ((isParentChildHierarchy || isActivityEmbedded) && typeFlag == CARD_LIST_FLAG) {
            float value = margin - specialPadding;
            padding[0] = value;
            padding[1] = value;
        } else if ((isParentChildHierarchy || isActivityEmbedded) && typeFlag == DEFAULT_FLAG) {
            float value = margin;
            padding[0] = value;
            padding[1] = value;
        } else {
            float total = padding[0] + padding[1] + responsiveUIModel.width(startColumn, (startColumn + gridNumber) - 1);
            if (total > widthSize) {
                int value = isAddPadding ? margin - specialPadding : margin;
                padding[0] = value;
                padding[1] = value;
            }
        }
        if (gridNumber > 0) {
            if (mode == PADDING_MODE) {
                if (view.getPaddingLeft() != (int) padding[0] || view.getPaddingRight() != (int) padding[1]) {
                    view.setPadding((int) padding[0], view.getPaddingTop(), (int) padding[1], view.getPaddingBottom());
                }
            }
        } else if (mode == PADDING_MODE && (view.getPaddingLeft() != padding[0] || view.getPaddingRight() != padding[1])) {
            view.setPadding(initPaddingStart, view.getPaddingTop(), initPaddingEnd, view.getPaddingBottom());
        }
        if (mode == REMEASURE_MODE) {
            return View.MeasureSpec.makeMeasureSpec((int) contentWidth, View.MeasureSpec.EXACTLY);
        }
        return widthMeasureSpec;
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static boolean isLargeScreen(Context context, int width, int height) {
        return WindowTotalSizeClass.Companion.fromWidthAndHeight(context, width, height) == WindowTotalSizeClass.Expanded;
    }

    public static boolean isLargeScreenDp(int widthDp, int heightDp) {
        return WindowTotalSizeClass.Companion.fromWidthAndHeight(new Dp((float) widthDp), new Dp((float) heightDp)) == WindowTotalSizeClass.Expanded;
    }

    public static boolean isMediumScreen(Context context, int width, int height) {
        WindowTotalSizeClass windowTotalSizeClassFromWidthAndHeight = WindowTotalSizeClass.Companion.fromWidthAndHeight(context, width, height);
        return windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.MediumPortrait || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.MediumLandScape || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.MediumSquare || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.ExpandedPortrait || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.ExpandedLandPortrait;
    }

    @Deprecated
    public static boolean isMediumScreenDp(Context context, int widthDp) {
        float width = widthDp;
        return WindowSizeClass.Companion.calculateFromSize(new Dp(width), new Dp(width)).getWindowWidthSizeClass() == WindowWidthSizeClass.Medium;
    }

    @Deprecated
    public static boolean isSmallScreenDp(Context context, int widthDp) {
        float width = widthDp;
        return WindowSizeClass.Companion.calculateFromSize(new Dp(width), new Dp(width)).getWindowWidthSizeClass() == WindowWidthSizeClass.Compact;
    }

    public static int getChildLayerDefaultTypeMargin(Context context, int width, int height) {
        float density;
        float marginDp;
        if (isLargeScreen(context, width, height)) {
            density = context.getResources().getDisplayMetrics().density;
            marginDp = MARGIN_LARGE_DP_IN_LARGE_SCREEN;
        } else {
            density = context.getResources().getDisplayMetrics().density;
            marginDp = MARGIN_LARGE_DP_IN_NON_LARGE_SCREEN;
        }
        return (int) (density * marginDp);
    }

    @Deprecated
    public static boolean isLargeScreenDp(Context context, int widthDp) {
        float width = widthDp;
        return WindowSizeClass.Companion.calculateFromSize(new Dp(width), new Dp(width)).getWindowWidthSizeClass() == WindowWidthSizeClass.Expanded;
    }

    public static boolean isMediumScreenDp(int widthDp, int heightDp) {
        WindowTotalSizeClass windowTotalSizeClassFromWidthAndHeight = WindowTotalSizeClass.Companion.fromWidthAndHeight(new Dp(widthDp), new Dp(heightDp));
        return windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.MediumPortrait || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.MediumLandScape || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.MediumSquare || windowTotalSizeClassFromWidthAndHeight == WindowTotalSizeClass.ExpandedPortrait;
    }

    @Deprecated
    public static float calculateWidth(float screenWidth, int gridNumber, int typeFlag, int paddingFlag, Context context) {
        return calculateWidth(screenWidth, context instanceof Activity ? getScreenPhysicalHeight((Activity) context) : 0, gridNumber, typeFlag, paddingFlag, context);
    }

    public static float calculateWidth(float screenWidth, float screenHeight, int gridNumber, int typeFlag, int paddingFlag, Context context) {
        int dimensionPixelOffset;
        MarginType marginType = paddingFlag == SMALL_PADDING ? MarginType.MARGIN_SMALL : MarginType.MARGIN_LARGE;
        boolean addPadding = typeFlag == LIST_FLAG || typeFlag == CARD_LIST_FLAG;
        ResponsiveUIModel responsiveUIModelChooseMargin = new ResponsiveUIModel(context, (int) screenWidth, (int) screenHeight).chooseMargin(marginType);
        int margin = responsiveUIModelChooseMargin.margin();
        int columnCount = responsiveUIModelChooseMargin.columnCount();
        if (DEBUG) {
            Log.d(TAG, "calculateWidth: responsiveUIProxy.columnCount() = " + responsiveUIModelChooseMargin.columnCount() + " gridNumber = " + gridNumber + " screenSize = " + screenWidth);
        }
        int defaultGridNumbers = gridNumber == AUTO_GRID_NUMBER ? getDefaultGridNumbers(responsiveUIModelChooseMargin) : Math.min(gridNumber, columnCount);
        float calculateGridWidth = responsiveUIModelChooseMargin.calculateGridWidth(defaultGridNumbers);
        if (DEBUG) {
            Log.d(TAG, "calculateWidth = " + calculateGridWidth + " gridNumber = " + defaultGridNumbers + " getColumnsCount = " + responsiveUIModelChooseMargin.columnCount() + " width = " + calculateGridWidth + " margin = " + margin + " screenWidth = " + screenWidth + " columnWidth = " + Arrays.toString(responsiveUIModelChooseMargin.columnWidth()) + " typeFlag = " + typeFlag + "isAddPadding = " + addPadding);
        }
        if (!addPadding) {
            dimensionPixelOffset = 0;
        } else if (typeFlag == LIST_FLAG) {
            dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.grid_list_special_padding);
        } else {
            dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.grid_card_special_padding);
        }
        return calculateGridWidth + ((addPadding ? dimensionPixelOffset : 0) * PADDING_COUNT);
    }

    @Deprecated
    public static int measureLayout(View view, int widthMeasureSpec, int gridMode, int requestGridNumber, int typeFlag, int paddingFlag, int mode, int initPaddingStart, int initPaddingEnd, int screenPhysicalWidth, boolean isParentChildHierarchy, boolean isActivityEmbedded) {
        return measureLayout(view, widthMeasureSpec, requestGridNumber, typeFlag, paddingFlag, mode, initPaddingStart, initPaddingEnd, screenPhysicalWidth, isParentChildHierarchy, isActivityEmbedded);
    }
}
