package com.coui.appcompat.poplist;

import android.graphics.Rect;
import android.util.Log;

import com.coui.appcompat.log.COUILog;

class PopupMenuDomain {
    private static final String TAG = "PopupMenuDomain";
    private static final boolean COUI_DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);

    Rect mWindow = new Rect();
    Rect mAnchor = new Rect();
    Rect mMainMenu = new Rect();
    Rect mMainMenuRelocated = new Rect();
    Rect mSubMenu = new Rect();
    Rect mPopup = new Rect();
    Rect mSubMenuAnchor = new Rect();
    Rect mAnchorOutsets = new Rect();
    Rect mWindowBarriers = new Rect();
    int mGlobalOffsetX = 0;
    int mGlobalOffsetY = 0;
    boolean mSubMenuAnchorIsFirstItem = false;
    boolean mMainMenuCenterAlign = false;

    public void dump() {
        Log.d(TAG, "mWindow = " + mWindow + " mAnchor = " + mAnchor
                + " mAnchorOutsets = " + mAnchorOutsets + " mWindowBarriers = " + mWindowBarriers
                + " mMainMenu = " + mMainMenu + " mMainMenuRelocated = " + mMainMenuRelocated
                + " mSubMenu = " + mSubMenu + " mSubMenuAnchor = " + mSubMenuAnchor
                + " mGlobalOffsetX = " + mGlobalOffsetX + " mGlobalOffsetY = " + mGlobalOffsetY);
    }

    public void getAnchorRealRect(Rect rect) {
        rect.set(mAnchor.left - mAnchorOutsets.left,
                mAnchor.top - mAnchorOutsets.top,
                mAnchor.right + mAnchorOutsets.right,
                mAnchor.bottom + mAnchorOutsets.bottom);
    }

    public void getAvailableRect(Rect rect) {
        rect.set(mWindow.left + mWindowBarriers.left,
                mWindow.top + mWindowBarriers.top,
                mWindow.right - mWindowBarriers.right,
                mWindow.bottom - mWindowBarriers.bottom);
        if (COUI_DEBUG) {
            Log.d(TAG, "PopupMenuDomain getAvailableRect mWindow.left " + mWindow.left
                    + " mWindowBarriers.left " + mWindowBarriers.left
                    + " mWindow.top " + mWindow.top
                    + " mWindowBarriers.top " + mWindowBarriers.top
                    + " mWindow.right " + mWindow.right
                    + " mWindowBarriers.right " + mWindowBarriers.right
                    + " mWindow.bottom " + mWindow.bottom
                    + " mWindowBarriers.bottom " + mWindowBarriers.bottom);
        }
    }

    public int getAvailableRectHeight() {
        return (mWindow.bottom - mWindowBarriers.bottom) - (mWindow.top + mWindowBarriers.top);
    }

    public int getAvailableRectWidth() {
        return (mWindow.right - mWindowBarriers.right) - (mWindow.left + mWindowBarriers.left);
    }

    public int getMainMenuEnterPivotX() {
        return mMainMenuCenterAlign ? mMainMenu.centerX()
                : Math.min(Math.max(mAnchor.centerX(), mMainMenu.left), mMainMenu.right);
    }

    public int getMainMenuEnterPivotY() {
        return mMainMenuCenterAlign ? mMainMenu.centerY()
                : mMainMenu.centerY() > mAnchor.centerY() ? mMainMenu.top : mMainMenu.bottom;
    }

    public int getSubMenuEnterPivotX() {
        return mSubMenu.left > mMainMenu.left ? 0 : mSubMenu.width();
    }

    public int getSubMenuEnterPivotY() {
        return mSubMenuAnchor.centerY() - mSubMenu.top;
    }

    public void reset() {
        mWindow.setEmpty();
        mAnchor.setEmpty();
        mMainMenu.setEmpty();
        mSubMenu.setEmpty();
        mPopup.setEmpty();
        mAnchorOutsets.setEmpty();
        mWindowBarriers.setEmpty();
        mMainMenuRelocated.setEmpty();
        mSubMenuAnchor.setEmpty();
    }
}
