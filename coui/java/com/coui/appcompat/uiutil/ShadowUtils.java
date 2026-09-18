package com.coui.appcompat.uiutil;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.view.OplusView;

public class ShadowUtils {
    public static final int SDK_SUB_VERSION = 2;
    public static final int SDK_VERSION = 34;
    public static final int SHADOW_LV1 = 0;
    public static final int SHADOW_LV2 = 1;
    public static final int SHADOW_LV3 = 2;
    public static final int SHADOW_LV4 = 3;
    public static final int SHADOW_LV5 = 4;
    private static final String TAG = "ShadowUtils";

    public static boolean checkOPlusViewElevationSDK() {
        return COUIVersionUtil.checkOPlusViewSubSDK(SDK_VERSION, SDK_SUB_VERSION);
    }

    public static void clearShadow(View view) {
        if (checkOPlusViewElevationSDK()) {
            setElevationToViewFromOPlusView(view, 0, 0, 0, 0, 0, -1);
        } else {
            setElevationToViewFromLower(view, 0, 0, 0);
        }
    }

    public static void setElevationToFloatingActionButton(View view, int index, int index_2) {
        setElevationToFloatingActionButton(view, index, index_2, COUIContextUtil.getAttrColor(view.getContext(), R.attr.couiColorPrimary));
    }

    public static void setElevationToView(View view, int elevationToView) {
        setElevationToView(view, elevationToView, 0, 0, 0);
    }

    public static void setElevationToViewFromLower(View view, int index, int index_2, int index_3) {
        if (view == null) {
            return;
        }
        view.setOutlineSpotShadowColor(index_2);
        view.setElevation(index);
    }

    public static void setElevationToViewFromOPlusView(View view, int index, int index_2, int index_3, int index_4, int index_5, int index_6) {
        if (view != null && checkOPlusViewElevationSDK()) {
            view.setOutlineAmbientShadowColor(index_2);
            view.setOutlineSpotShadowColor(index_2);
            view.setElevation(index);
            try {
                new OplusView(view).setOverrideLightSourceGeometry(-1.0f, index_3, index_4, index_5, index_6);
            } catch (Exception e) {
                COUILog.d(TAG, "setOverrideLightSourceGeometry error:" + e.getMessage());
            }
        }
    }

    public static void setElevationToView(View view, int index, int index_2, int index_3) {
        setElevationToView(view, index, index_2, view.getResources().getDimensionPixelOffset(R.dimen.support_shadow_size_level_for_lowerP), index_3);
    }

    public static void setElevationToFloatingActionButton(View view, int index, int index_2, int index_3) {
        if (view == null) {
            COUILog.e(TAG, "setElevationToFloatingActionButton target view is null");
            return;
        }
        if (index_3 == -1) {
            index_3 = COUIContextUtil.getAttrColor(view.getContext(), R.attr.couiColorPrimary);
        }
        if (checkOPlusViewElevationSDK()) {
            Resources resources = view.getContext().getResources();
            setElevationToViewFromOPlusView(view, resources.getDimensionPixelSize(R.dimen.coui_float_btn_shadow_elevation), Color.argb(resources.getInteger(R.integer.coui_shadow_color_float_btn), Color.red(index_3), Color.green(index_3), Color.blue(index_3)), resources.getDimensionPixelSize(R.dimen.coui_float_btn_shadow_light_y), resources.getDimensionPixelSize(R.dimen.coui_float_btn_shadow_light_z), resources.getDimensionPixelSize(R.dimen.coui_float_btn_shadow_light_r), resources.getDimensionPixelSize(R.dimen.coui_float_btn_shadow_blur_r));
        } else {
            setElevationToViewFromLower(view, index, index_2, view.getResources().getDimensionPixelOffset(R.dimen.support_shadow_size_level_for_lowerP));
        }
    }

    public static void setElevationToView(View view, int index, int index_2, int index_3, int index_4) {
        if (view == null) {
            COUILog.e(TAG, "setElevationToView view is null");
            return;
        }
        if (checkOPlusViewElevationSDK()) {
            Resources resources = view.getContext().getResources();
            int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.coui_shadow_elevation_default);
            if (index == 0) {
                setElevationToViewFromOPlusView(view, dimensionPixelSize, Color.argb(resources.getInteger(R.integer.coui_shadow_color_lv1), 0, 0, 0), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_y_level1), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_z_level1), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_r_level1), resources.getDimensionPixelSize(R.dimen.coui_shadow_blur_r_level1));
                return;
            }
            if (index == 1) {
                setElevationToViewFromOPlusView(view, dimensionPixelSize, Color.argb(resources.getInteger(R.integer.coui_shadow_color_lv2), 0, 0, 0), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_y_level2), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_z_level2), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_r_level2), resources.getDimensionPixelSize(R.dimen.coui_shadow_blur_r_level2));
                return;
            }
            if (index == 2) {
                setElevationToViewFromOPlusView(view, dimensionPixelSize, Color.argb(resources.getInteger(R.integer.coui_shadow_color_lv3), 0, 0, 0), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_y_level3), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_z_level3), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_r_level3), resources.getDimensionPixelSize(R.dimen.coui_shadow_blur_r_level3));
                return;
            } else if (index == 3) {
                setElevationToViewFromOPlusView(view, resources.getDimensionPixelSize(R.dimen.coui_shadow_elevation_four), Color.argb(resources.getInteger(R.integer.coui_shadow_color_lv4), 0, 0, 0), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_y_level4), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_z_level4), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_r_level4), resources.getDimensionPixelSize(R.dimen.coui_shadow_blur_r_level4));
                return;
            } else {
                if (index == 4) {
                    setElevationToViewFromOPlusView(view, resources.getDimensionPixelSize(R.dimen.coui_shadow_elevation_five), Color.argb(resources.getInteger(R.integer.coui_shadow_color_lv5), 0, 0, 0), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_y_level5), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_z_level5), resources.getDimensionPixelSize(R.dimen.coui_shadow_light_r_level5), resources.getDimensionPixelSize(R.dimen.coui_shadow_blur_r_level5));
                    return;
                }
                return;
            }
        }
        setElevationToViewFromLower(view, index_2, index_4, index_3);
    }
}
