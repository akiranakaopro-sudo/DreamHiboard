package com.coui.appcompat.state;

import android.graphics.drawable.Drawable;

public abstract class StatefulDrawable extends Drawable implements DrawableStateProxy, IStateEffect {
    private final DrawableStateManager mDrawableStateManager;

    public StatefulDrawable(String tag) {
        mDrawableStateManager = new DrawableStateManager(tag, this);
    }

    @Override public final int getTouchType() { return mDrawableStateManager.getTouchType(); }
    @Override public boolean isDrawableEnabled() { return mDrawableStateManager.isDrawableEnabled(); }
    @Override public final boolean isEnabled() { return mDrawableStateManager.isEnabled(); }
    @Override public final boolean isFocused() { return mDrawableStateManager.isFocused(); }
    @Override public final boolean isHovered() { return mDrawableStateManager.isHovered(); }
    @Override public boolean isNativeStateEnabled(int stateFlag) { return mDrawableStateManager.isNativeStateEnabled(stateFlag); }
    @Override public final boolean isPressed() { return mDrawableStateManager.isPressed(); }
    @Override public final boolean isSelected() { return mDrawableStateManager.isSelected(); }
    @Override public boolean isStateLocked(int state) { return mDrawableStateManager.isStateLocked(state); }
    @Override public final boolean isStateful() { return mDrawableStateManager.isStateful(); }
    @Override public final boolean isTouchEntered() { return mDrawableStateManager.isTouchEntered(); }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        mDrawableStateManager.onStateChange(stateSet);
        return true;
    }

    @Override public void setDrawableEnabled(boolean enabled) { mDrawableStateManager.setDrawableEnabled(enabled); }
    @Override public final void setFocusEntered() { mDrawableStateManager.setFocusEntered(); }
    @Override public final void setFocusExited() { mDrawableStateManager.setFocusExited(); }
    @Override public final void setHoverEntered() { mDrawableStateManager.setHoverEntered(); }
    @Override public final void setHoverExited() { mDrawableStateManager.setHoverExited(); }
    @Override public void setNativeStateEnabled(int stateFlag, boolean disabled) { mDrawableStateManager.setNativeStateEnabled(stateFlag, disabled); }
    @Override public final void setSelectedEntered() { mDrawableStateManager.setSelectedEntered(); }
    @Override public final void setSelectedExited() { mDrawableStateManager.setSelectedExited(); }
    @Override public void setStateLocked(int state, boolean locked, boolean entered, boolean animated) { mDrawableStateManager.setStateLocked(state, locked, entered, animated); }
    @Override public void setTouchEntered() { mDrawableStateManager.setTouchEntered(); }
    @Override public void setTouchExited() { mDrawableStateManager.setTouchExited(); }
    @Override public void setTouchSelectEntered() { mDrawableStateManager.setTouchSelectEntered(); }
    @Override public void setTouchSelectExited() { mDrawableStateManager.setTouchSelectExited(); }
}
