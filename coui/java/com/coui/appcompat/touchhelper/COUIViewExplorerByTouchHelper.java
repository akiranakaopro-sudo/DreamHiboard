package com.coui.appcompat.touchhelper;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeProviderCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import java.util.List;


public class COUIViewExplorerByTouchHelper extends ExploreByTouchHelper {
    private static final String VIEW_LOG_TAG = "COUIViewExplorerByTouchHelper";
    private COUIViewTalkBalkInteraction mCOUIViewTalkBalkInteraction;
    private View mHostView;
    private final Rect mTempRect;

    public interface COUIViewTalkBalkInteraction {
        CharSequence getClassName();

        int getCurrentPosition();

        int getDisablePosition();

        void getItemBounds(int index, Rect rect);

        int getItemCounts();

        CharSequence getItemDescription(int index);

        int getVirtualViewAt(float value, float value_2);

        void performAction(int index, int index_2, boolean flag);
    }

    public COUIViewExplorerByTouchHelper(View view) {
        super(view);
        this.mTempRect = new Rect();
        this.mCOUIViewTalkBalkInteraction = null;
        this.mHostView = view;
    }

    private void getItemBounds(int index, Rect rect) {
        if (index < 0 || index >= this.mCOUIViewTalkBalkInteraction.getItemCounts()) {
            return;
        }
        this.mCOUIViewTalkBalkInteraction.getItemBounds(index, rect);
    }

    public void clearFocusedVirtualView() {
        int focusedVirtualView = getFocusedVirtualView();
        if (focusedVirtualView != Integer.MIN_VALUE) {
            AccessibilityNodeProviderCompat provider = getAccessibilityNodeProvider(this.mHostView);
            provider.performAction(focusedVirtualView, 128, null);
        }
    }

    @Override
    public int getVirtualViewAt(float value, float value_2) {
        int virtualViewAt = this.mCOUIViewTalkBalkInteraction.getVirtualViewAt(value, value_2);
        if (virtualViewAt >= 0) {
            return virtualViewAt;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public void getVisibleVirtualViews(List<Integer> list) {
        for (int index = 0; index < this.mCOUIViewTalkBalkInteraction.getItemCounts(); index++) {
            list.add(Integer.valueOf(index));
        }
    }

    @Override
    public boolean onPerformActionForVirtualView(int index, int index_2, Bundle bundle) {
        if (index_2 != 16) {
            return false;
        }
        this.mCOUIViewTalkBalkInteraction.performAction(index, 16, false);
        return true;
    }

    @Override
    public void onPopulateEventForVirtualView(int index, AccessibilityEvent accessibilityEvent) {
        accessibilityEvent.setContentDescription(this.mCOUIViewTalkBalkInteraction.getItemDescription(index));
    }

    @Override
    public void onPopulateNodeForVirtualView(int index, AccessibilityNodeInfoCompat nodeInfo) {
        getItemBounds(index, this.mTempRect);
        nodeInfo.setContentDescription(this.mCOUIViewTalkBalkInteraction.getItemDescription(index));
        nodeInfo.setBoundsInParent(this.mTempRect);
        if (this.mCOUIViewTalkBalkInteraction.getClassName() != null) {
            nodeInfo.setClassName(this.mCOUIViewTalkBalkInteraction.getClassName());
        }
        nodeInfo.addAction(16);
        if (index == this.mCOUIViewTalkBalkInteraction.getCurrentPosition()) {
            nodeInfo.setSelected(true);
        }
        if (index == this.mCOUIViewTalkBalkInteraction.getDisablePosition()) {
            nodeInfo.setEnabled(false);
        }
    }

    public void setCOUIViewTalkBalkInteraction(COUIViewTalkBalkInteraction cOUIViewTalkBalkInteraction) {
        this.mCOUIViewTalkBalkInteraction = cOUIViewTalkBalkInteraction;
    }

    public void setFocusedVirtualView(int focusedVirtualView) {
        AccessibilityNodeProviderCompat provider = getAccessibilityNodeProvider(this.mHostView);
        provider.performAction(focusedVirtualView, 64, null);
    }
}
