package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.content.Context;
import com.coui.appcompat.state.COUIMaskEffectDrawable;


public class ListItemMaskEffectDrawable extends COUIMaskEffectDrawable {
    public ListItemMaskEffectDrawable mChild;
    public ListItemMaskEffectDrawable mParent;

    public ListItemMaskEffectDrawable(Context context, int index, ListItemMaskEffectDrawable listItemMaskEffectDrawable) {
        super(context, index);
        this.mChild = null;
        this.mParent = null;
        if (listItemMaskEffectDrawable == null) {
            this.mChild = new ListItemMaskEffectDrawable(context, index, this);
        } else {
            this.mParent = listItemMaskEffectDrawable;
        }
        setIsRoundStyle(false);
        enableFocusedState(false);
    }

    private void notifyTouchEntered() {
        super.setTouchEntered();
    }

    private void notifyTouchExited() {
        super.setTouchExited();
    }

    public ListItemMaskEffectDrawable getChild() {
        return this.mChild;
    }

    @Override
    public void setTouchEntered() {
        ListItemMaskEffectDrawable listItemMaskEffectDrawable = this.mChild;
        if (listItemMaskEffectDrawable != null) {
            listItemMaskEffectDrawable.notifyTouchEntered();
        }
        ListItemMaskEffectDrawable listItemMaskEffectDrawable2 = this.mParent;
        if (listItemMaskEffectDrawable2 != null) {
            listItemMaskEffectDrawable2.notifyTouchEntered();
        }
        notifyTouchEntered();
    }

    @Override
    public void setTouchExited() {
        ListItemMaskEffectDrawable listItemMaskEffectDrawable = this.mChild;
        if (listItemMaskEffectDrawable != null) {
            listItemMaskEffectDrawable.notifyTouchExited();
        }
        ListItemMaskEffectDrawable listItemMaskEffectDrawable2 = this.mParent;
        if (listItemMaskEffectDrawable2 != null) {
            listItemMaskEffectDrawable2.notifyTouchExited();
        }
        notifyTouchExited();
    }
}
