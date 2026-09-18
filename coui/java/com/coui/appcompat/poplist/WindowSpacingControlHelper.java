package com.coui.appcompat.poplist;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class WindowSpacingControlHelper {
    private static final int ORIENTATION_VERTICAL_TOP = 1;
    private static final int ORIENTATION_VERTICAL_BOTTOM = 2;
    private final String TAG = "WindowSpacingControlHelper";
    private Map<AnchorViewTypeEnum, Integer> marginMap = new HashMap<>();

    public enum AnchorViewTypeEnum {
        NORMAL,
        TOOLBAR,
        NAVIGATION,
        START,
        END,
        TOP,
        BOTTOM
    }

    private boolean checkViewFromEnumType(View view, AnchorViewTypeEnum type) {
        if (type == AnchorViewTypeEnum.TOOLBAR) {
            return view instanceof Toolbar;
        }
        return false;
    }

    public static int getORIENTATION_VERTICAL_BOTTOM() {
        return ORIENTATION_VERTICAL_BOTTOM;
    }

    public static int getORIENTATION_VERTICAL_TOP() {
        return ORIENTATION_VERTICAL_TOP;
    }

    public void addAnchorViewSpacingMap(int spacing, AnchorViewTypeEnum enumType) {
        marginMap.put(enumType, spacing);
    }

    public boolean checkInMarginMap(AnchorViewTypeEnum enumType) {
        if (marginMap.isEmpty()) {
            return false;
        }
        Iterator<AnchorViewTypeEnum> iterator = marginMap.keySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == enumType) {
                return true;
            }
        }
        return false;
    }

    public View getAnchorViewParentView(View anchorView, AnchorViewTypeEnum viewTypeEnum) {
        View current = anchorView;
        while (!checkViewFromEnumType(current, viewTypeEnum)) {
            if (!(current.getParent() instanceof View)) {
                Log.e(WindowSpacingControlHelper.class.getName(), "getAnchorViewParentView tempView " + current.getClass().getName());
                return current;
            }
            current = (View) current.getParent();
        }
        return current;
    }

    public int getAnchorViewSpacing(AnchorViewTypeEnum enumType) {
        Integer value = marginMap.get(enumType);
        return value == null ? 0 : value;
    }

    public int getAnchorViewSpacing(View anchorView, AnchorViewTypeEnum enumType) {
        Integer spacing = marginMap.get(enumType);
        if (spacing == null) {
            return 0;
        }
        View current = anchorView;
        while (current != null) {
            if (checkViewFromEnumType(current, enumType)) {
                int[] anchorLocation = new int[2];
                int[] currentLocation = new int[2];
                anchorView.getLocationInWindow(anchorLocation);
                current.getLocationInWindow(currentLocation);
                return spacing + getOrientationSpacing(current, anchorView, currentLocation, anchorLocation, enumType);
            }
            if (!(current.getParent() instanceof View)) {
                return spacing;
            }
            current = (View) current.getParent();
        }
        return spacing;
    }

    public AnchorViewTypeEnum getAnchorViewTypeEnum(View anchorView) {
        View current = anchorView;
        while (!(current instanceof Toolbar)) {
            if (!(current.getParent() instanceof View)) {
                return AnchorViewTypeEnum.NORMAL;
            }
            current = (View) current.getParent();
        }
        return AnchorViewTypeEnum.TOOLBAR;
    }

    public Map<AnchorViewTypeEnum, Integer> getMarginMap() {
        return marginMap;
    }

    public int getOrientationValue(AnchorViewTypeEnum enumType) {
        return enumType == AnchorViewTypeEnum.TOOLBAR ? ORIENTATION_VERTICAL_BOTTOM : ORIENTATION_VERTICAL_TOP;
    }

    public String getTAG() {
        return TAG;
    }

    public boolean isUtilMapInit() {
        return !marginMap.isEmpty();
    }

    public void setMarginMap(Map<AnchorViewTypeEnum, Integer> map) {
        marginMap = map;
    }

    @SuppressLint("LongLogTag")
    public void setOriginCenterPoint(int[] anchorViewLocationInScreen, int[] resultOriginCenterPoint, View anchorView) {
        if (anchorView.getWidth() <= 0 || anchorView.getHeight() <= 0) {
            Log.e(TAG, "setOriginCenterPoint anchorView.width <= 0 or anchorView.height <= 0");
            resultOriginCenterPoint[0] = (int) anchorView.getPivotX();
            resultOriginCenterPoint[1] = (int) anchorView.getPivotY();
            return;
        }
        float pivotX = anchorView.getPivotX() / anchorView.getWidth();
        float pivotY = anchorView.getPivotY() / anchorView.getHeight();
        float centerX = anchorViewLocationInScreen[0] + ((anchorView.getScaleX() * anchorView.getWidth()) / 2f);
        float centerY = anchorViewLocationInScreen[1] + ((anchorView.getScaleY() * anchorView.getHeight()) / 2f);
        resultOriginCenterPoint[0] = Math.round(centerX + ((pivotX - 0.5f) * (anchorView.getScaleX() - 1f) * anchorView.getWidth()));
        resultOriginCenterPoint[1] = Math.round(centerY + ((pivotY - 0.5f) * (anchorView.getScaleY() - 1f) * anchorView.getHeight()));
    }

    private int getOrientationSpacing(View container, View anchor, int[] containerLocation, int[] anchorLocation, AnchorViewTypeEnum enumType) {
        int orientation = getOrientationValue(enumType);
        if (orientation == ORIENTATION_VERTICAL_TOP) {
            return containerLocation[1] - anchorLocation[1];
        }
        if (orientation == ORIENTATION_VERTICAL_BOTTOM) {
            return (containerLocation[1] + container.getHeight()) - (anchorLocation[1] + anchor.getHeight());
        }
        return 0;
    }
}
