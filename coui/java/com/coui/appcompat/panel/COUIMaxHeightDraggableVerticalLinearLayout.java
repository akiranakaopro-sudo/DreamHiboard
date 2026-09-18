package com.coui.appcompat.panel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class COUIMaxHeightDraggableVerticalLinearLayout extends COUIDraggableVerticalLinearLayout {
    private int mLayoutWindowVisibility;

    public COUIMaxHeightDraggableVerticalLinearLayout(Context context) {
        super(context);
    }

    public int getMaxHeight() {
        return COUIPanelMultiWindowUtils.getPanelMaxHeight(getContext(), null);
    }

    public void layoutAtMaxHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getMaxHeight();
        setLayoutParams(layoutParams);
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        if (this.mLayoutWindowVisibility == 8 && visibility == 0) {
            measure(View.MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
        }
        this.mLayoutWindowVisibility = visibility;
        super.onWindowVisibilityChanged(visibility);
    }

    public COUIMaxHeightDraggableVerticalLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public COUIMaxHeightDraggableVerticalLinearLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }
}
