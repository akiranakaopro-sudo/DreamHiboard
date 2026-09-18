package com.coui.appcompat.state;

public interface DrawableStateProxy {
    int STATE_TOUCH_ENTERED = 1;
    int STATE_FOCUSED = 16842908;
    int STATE_ENABLED = 16842910;
    int STATE_SELECTED = 16842913;
    int STATE_PRESSED = 16842919;
    int STATE_HOVERED = 16843623;

    int STATE_TOUCH_ENTERED_FLAG = 1;
    int STATE_FOCUSED_FLAG = 2;
    int STATE_HOVERED_FLAG = 4;
    int STATE_SELECTED_FLAG = 8;
    int STATE_PRESSED_FLAG = 16;
    int STATE_ENABLED_FLAG = 32;

    int TOUCH_TYPE_PRESSED = 0;
    int TOUCH_TYPE_SELECTED = 1;

    int getTouchType();
    boolean isDrawableEnabled();
    boolean isEnabled();
    boolean isFocused();
    boolean isHovered();
    boolean isNativeStateEnabled(int stateFlag);
    boolean isPressed();
    boolean isSelected();
    boolean isStateLocked(int state);
    boolean isTouchEntered();
    void onViewStateChanged(int state);
    void setDrawableEnabled(boolean enabled);
    void setFocusEntered();
    void setFocusExited();
    void setHoverEntered();
    void setHoverExited();
    void setNativeStateEnabled(int stateFlag, boolean disabled);
    void setSelectedEntered();
    void setSelectedExited();
    void setStateLocked(int state, boolean locked, boolean entered, boolean animated);
    void setTouchEntered();
    void setTouchExited();
    void setTouchSelectEntered();
    void setTouchSelectExited();
}
