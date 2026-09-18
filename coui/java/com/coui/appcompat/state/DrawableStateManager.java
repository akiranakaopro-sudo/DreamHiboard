package com.coui.appcompat.state;

import android.util.SparseIntArray;

import com.coui.appcompat.log.COUILog;

public final class DrawableStateManager implements DrawableStateProxy {
    private final SparseIntArray mStateMap = new SparseIntArray();
    private final String mTag;
    private final DrawableStateProxy mCallback;
    private boolean mDrawableEnabled = true;
    private int mNativeStateDisabledFlag;
    private int mStateFlag;
    private int mStateLockedFlag;
    private int mTouchType;

    public DrawableStateManager(String tag, DrawableStateProxy callback) {
        mTag = tag;
        mCallback = callback;
        mStateMap.put(STATE_FOCUSED, STATE_FOCUSED_FLAG);
        mStateMap.put(STATE_HOVERED, STATE_HOVERED_FLAG);
        mStateMap.put(STATE_TOUCH_ENTERED, STATE_TOUCH_ENTERED_FLAG);
        mStateMap.put(STATE_SELECTED, STATE_SELECTED_FLAG);
        mStateMap.put(STATE_PRESSED, STATE_PRESSED_FLAG);
        mStateMap.put(STATE_ENABLED, STATE_ENABLED_FLAG);
    }

    private void checkOnViewStateChanged(int[] states, int state) {
        boolean contains = false;
        for (int current : states) {
            if (current == state) {
                contains = true;
                break;
            }
        }
        boolean oldValue = (mStateFlag & mStateMap.get(state)) != 0;
        if (oldValue != contains) {
            notifyStateChanged(state, contains);
        }
    }

    private void notifyStateChanged(int state, boolean entered) {
        boolean oldValue = (mStateFlag & mStateMap.get(state)) != 0;
        if (oldValue == entered && state != STATE_TOUCH_ENTERED) {
            return;
        }
        int flag = mStateMap.get(state);
        mStateFlag = entered ? mStateFlag | flag : mStateFlag & ~flag;
        onViewStateChanged(state);
        COUILog.d(mTag, "state " + getStateName(state) + " changed from " + oldValue + " to " + entered);
    }

    private String getStateName(int state) {
        switch (state) {
            case STATE_TOUCH_ENTERED:
                return "touch entered";
            case STATE_FOCUSED:
                return "focused";
            case STATE_ENABLED:
                return "enabled";
            case STATE_SELECTED:
                return "selected";
            case STATE_PRESSED:
                return "pressed";
            case STATE_HOVERED:
                return "hovered";
            default:
                return "unknown";
        }
    }

    public void onStateChange(int[] states) {
        if (isNativeStateEnabled(STATE_ENABLED_FLAG)) {
            checkOnViewStateChanged(states, STATE_ENABLED);
        }
        if (isNativeStateEnabled(STATE_FOCUSED_FLAG)) {
            checkOnViewStateChanged(states, STATE_FOCUSED);
        }
        if (isNativeStateEnabled(STATE_HOVERED_FLAG)) {
            checkOnViewStateChanged(states, STATE_HOVERED);
        }
        if (isNativeStateEnabled(STATE_SELECTED_FLAG)) {
            checkOnViewStateChanged(states, STATE_SELECTED);
        }
        if (isNativeStateEnabled(STATE_PRESSED_FLAG)) {
            checkOnViewStateChanged(states, STATE_PRESSED);
        }
    }

    @Override public int getTouchType() { return mTouchType; }
    @Override public boolean isDrawableEnabled() { return mDrawableEnabled; }
    @Override public boolean isEnabled() { return (mStateFlag & STATE_ENABLED_FLAG) != 0; }
    @Override public boolean isFocused() { return (mStateFlag & STATE_FOCUSED_FLAG) != 0; }
    @Override public boolean isHovered() { return (mStateFlag & STATE_HOVERED_FLAG) != 0; }
    @Override public boolean isNativeStateEnabled(int stateFlag) { return (mNativeStateDisabledFlag & stateFlag) == 0; }
    @Override public boolean isPressed() { return (mStateFlag & STATE_PRESSED_FLAG) != 0; }
    @Override public boolean isSelected() { return (mStateFlag & STATE_SELECTED_FLAG) != 0; }
    @Override public boolean isStateLocked(int state) { return (mStateLockedFlag & mStateMap.get(state)) != 0; }
    @Override public boolean isTouchEntered() { return (mStateFlag & STATE_TOUCH_ENTERED_FLAG) != 0; }
    public boolean isStateful() { return true; }
    @Override public void onViewStateChanged(int state) { mCallback.onViewStateChanged(state); }
    @Override public void setDrawableEnabled(boolean enabled) { mDrawableEnabled = enabled; }
    @Override public void setFocusEntered() { notifyStateChanged(STATE_FOCUSED, true); }
    @Override public void setFocusExited() { notifyStateChanged(STATE_FOCUSED, false); }
    @Override public void setHoverEntered() { notifyStateChanged(STATE_HOVERED, true); }
    @Override public void setHoverExited() { notifyStateChanged(STATE_HOVERED, false); }

    @Override
    public void setNativeStateEnabled(int stateFlag, boolean disabled) {
        if (disabled) {
            mNativeStateDisabledFlag |= stateFlag;
        } else {
            mNativeStateDisabledFlag &= ~stateFlag;
        }
    }

    @Override public void setSelectedEntered() { notifyStateChanged(STATE_SELECTED, true); }
    @Override public void setSelectedExited() { notifyStateChanged(STATE_SELECTED, false); }

    @Override
    public void setStateLocked(int state, boolean locked, boolean entered, boolean animated) {
        int flag = mStateMap.get(state);
        mStateLockedFlag = locked ? mStateLockedFlag | flag : mStateLockedFlag & ~flag;
    }

    @Override public void setTouchEntered() { mTouchType = TOUCH_TYPE_PRESSED; notifyStateChanged(STATE_TOUCH_ENTERED, true); }
    @Override public void setTouchExited() { mTouchType = TOUCH_TYPE_PRESSED; notifyStateChanged(STATE_TOUCH_ENTERED, false); }
    @Override public void setTouchSelectEntered() { mTouchType = TOUCH_TYPE_SELECTED; notifyStateChanged(STATE_TOUCH_ENTERED, true); }
    @Override public void setTouchSelectExited() { mTouchType = TOUCH_TYPE_SELECTED; notifyStateChanged(STATE_TOUCH_ENTERED, false); }
}
