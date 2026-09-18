package com.coui.appcompat.panel;

import android.view.View;
import android.view.ViewGroup;


public class COUIViewMarginUtil {
    public static final int DIRECTION_BOTTOM = 3;
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_SIZE = 4;
    public static final int DIRECTION_TOP = 1;

    public static int getMargin(View view, int direction) {
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                if (direction == DIRECTION_LEFT) {
                    return marginLayoutParams.leftMargin;
                }
                if (direction == DIRECTION_TOP) {
                    return marginLayoutParams.topMargin;
                }
                if (direction == DIRECTION_RIGHT) {
                    return marginLayoutParams.rightMargin;
                }
                if (direction != DIRECTION_BOTTOM) {
                    return 0;
                }
                return marginLayoutParams.bottomMargin;
            }
        }
        return 0;
    }

    public static int[] getMargins(View view) {
        int[] margins = new int[DIRECTION_SIZE];
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                margins[DIRECTION_LEFT] = marginLayoutParams.leftMargin;
                margins[DIRECTION_TOP] = marginLayoutParams.topMargin;
                margins[DIRECTION_RIGHT] = marginLayoutParams.rightMargin;
                margins[DIRECTION_BOTTOM] = marginLayoutParams.bottomMargin;
            }
        }
        return margins;
    }

    public static void setMargin(View view, int direction, int margin) {
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                if (direction == DIRECTION_LEFT) {
                    marginLayoutParams.leftMargin = margin;
                } else if (direction == DIRECTION_TOP) {
                    marginLayoutParams.topMargin = margin;
                } else if (direction == DIRECTION_RIGHT) {
                    marginLayoutParams.rightMargin = margin;
                } else if (direction == DIRECTION_BOTTOM) {
                    marginLayoutParams.bottomMargin = margin;
                }
                view.setLayoutParams(layoutParams);
            }
        }
    }

    public static void setMargins(View view, int[] margins) {
        if (view == null || margins == null || margins.length != DIRECTION_SIZE) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.leftMargin = margins[DIRECTION_LEFT];
            marginLayoutParams.topMargin = margins[DIRECTION_TOP];
            marginLayoutParams.rightMargin = margins[DIRECTION_RIGHT];
            marginLayoutParams.bottomMargin = margins[DIRECTION_BOTTOM];
            view.setLayoutParams(layoutParams);
        }
    }
}
