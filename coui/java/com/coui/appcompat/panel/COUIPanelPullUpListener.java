package com.coui.appcompat.panel;


public interface COUIPanelPullUpListener {
    void onCancel();

    int onDragging(int currentTop, int expandedOffset);

    void onDraggingPanel();

    void onOffsetChanged(float slideOffset);

    void onReleased(int expandedOffset);

    void onReleasedDrag();
}
