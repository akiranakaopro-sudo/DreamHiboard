package com.coui.appcompat.poplist;

import android.graphics.Rect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface PopupMenuConfigRule extends PopupMenuRule {
    int TYPE_WINDOW = 0;
    int TYPE_ANCHOR = 1;
    int TYPE_BARRIER = 2;
    int TYPE_SUBMENU_ANCHOR = 3;
    int BARRIER_GONE = -1;
    int BARRIER_FROM_LEFT = 0;
    int BARRIER_FROM_TOP = 1;
    int BARRIER_FROM_RIGHT = 2;
    int BARRIER_FROM_BOTTOM = 3;
    int BARRIER_WINDOW = 4;

    @Retention(RetentionPolicy.SOURCE)
    @interface PopupMenuConfigType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface BarrierDirection {
    }

    int getType();

    Rect getDisplayFrame();

    Rect getOutsets();

    boolean getPopupMenuRuleEnabled();

    int getBarrierDirection();

    void setPopupMenuRuleEnabled(boolean enabled);
}
