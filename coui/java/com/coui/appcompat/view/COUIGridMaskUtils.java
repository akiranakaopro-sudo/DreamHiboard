package com.coui.appcompat.view;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.coui.component.responsiveui.layoutgrid.MarginType;

public class COUIGridMaskUtils {
    private static final String TAG = "COUIGridMaskUtils";

    public static void injectMask(Activity activity, MarginType marginType) {
        if (isMaskAlreadyExist(activity)) {
            return;
        }
        COUIResponsiveGridMaskView gridMaskView = new COUIResponsiveGridMaskView(activity);
        gridMaskView.setMarginType(marginType);
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        if (viewGroup != null) {
            viewGroup.addView(gridMaskView);
        }
    }

    public static boolean isMaskAlreadyExist(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        if (viewGroup != null) {
            for (int index = 0; index < viewGroup.getChildCount(); index++) {
                if (viewGroup.getChildAt(index) instanceof COUIResponsiveGridMaskView) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeMask(Activity activity) {
        if (isMaskAlreadyExist(activity)) {
            ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
            if (viewGroup == null) {
                Log.w(TAG, "removeMask: content is null!");
                return;
            }
            View childAt = null;
            for (int index = 0; index < viewGroup.getChildCount(); index++) {
                if (viewGroup.getChildAt(index) instanceof COUIResponsiveGridMaskView) {
                    childAt = viewGroup.getChildAt(index);
                }
            }
            viewGroup.removeView(childAt);
        }
    }

    public static void injectMask(Activity activity) {
        injectMask(activity, MarginType.MARGIN_LARGE);
    }
}
