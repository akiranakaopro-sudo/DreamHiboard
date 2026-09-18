package com.coui.appcompat.viewpager;

import android.animation.LayoutTransition;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.lang.reflect.Array;
import java.util.Arrays;

public class COUIAnimateLayoutChangeDetector {
    private static final ViewGroup.MarginLayoutParams ZERO_MARGIN_LAYOUT_PARAMS;

    static {
        ZERO_MARGIN_LAYOUT_PARAMS = new ViewGroup.MarginLayoutParams(-1, -1);
        ZERO_MARGIN_LAYOUT_PARAMS.setMargins(0, 0, 0, 0);
    }

    private final LinearLayoutManager mLayoutManager;

    public COUIAnimateLayoutChangeDetector(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    private boolean arePagesLaidOutContiguously() {
        int childCount = mLayoutManager.getChildCount();
        if (childCount == 0) {
            return true;
        }
        boolean horizontal = mLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL;
        int[][] bounds = (int[][]) Array.newInstance(Integer.TYPE, childCount, 2);
        for (int i = 0; i < childCount; i++) {
            View child = mLayoutManager.getChildAt(i);
            if (child == null) {
                throw new IllegalStateException("null view contained in the view hierarchy");
            }
            ViewGroup.LayoutParams params = child.getLayoutParams();
            ViewGroup.MarginLayoutParams margins = params instanceof ViewGroup.MarginLayoutParams
                    ? (ViewGroup.MarginLayoutParams) params : ZERO_MARGIN_LAYOUT_PARAMS;
            bounds[i][0] = horizontal ? child.getLeft() - margins.leftMargin : child.getTop() - margins.topMargin;
            bounds[i][1] = horizontal ? child.getRight() + margins.rightMargin : child.getBottom() + margins.bottomMargin;
        }
        Arrays.sort(bounds, (left, right) -> left[0] - right[0]);
        for (int i = 1; i < childCount; i++) {
            if (bounds[i - 1][1] != bounds[i][0]) {
                return false;
            }
        }
        return bounds[0][0] <= 0 && bounds[childCount - 1][1] >= bounds[0][1] - bounds[0][0];
    }

    private boolean hasRunningChangingLayoutTransition() {
        for (int i = 0; i < mLayoutManager.getChildCount(); i++) {
            if (hasRunningChangingLayoutTransition(mLayoutManager.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean mayHaveInterferingAnimations() {
        return (!arePagesLaidOutContiguously() || mLayoutManager.getChildCount() <= 1)
                && hasRunningChangingLayoutTransition();
    }

    private static boolean hasRunningChangingLayoutTransition(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LayoutTransition transition = viewGroup.getLayoutTransition();
            if (transition != null && transition.isChangingLayout()) {
                return true;
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (hasRunningChangingLayoutTransition(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
