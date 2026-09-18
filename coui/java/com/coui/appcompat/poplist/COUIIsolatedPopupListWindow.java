package com.coui.appcompat.poplist;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class COUIIsolatedPopupListWindow extends COUIPopupListWindow {
    private PopupWindow.OnDismissListener mIsolatedOnDismissListener;

    public class DummyAnchorView extends View implements PopupMenuConfigRule {
        private int mX;
        private int mY;
        private boolean mRuleEnabled = true;

        public DummyAnchorView(Context context) {
            super(context);
        }

        public void setPosition(int x, int y) {
            mX = x;
            mY = y;
        }

        @Override
        public boolean getGlobalVisibleRect(Rect rect, Point globalOffset) {
            rect.set(mX, mY, mX + 1, mY + 1);
            if (globalOffset != null) {
                globalOffset.set(0, 0);
            }
            return true;
        }

        @Override
        public int getBarrierDirection() {
            return BARRIER_GONE;
        }

        @Override
        public Rect getDisplayFrame() {
            return new Rect(mX, mY, mX + 1, mY + 1);
        }

        @Override
        public Rect getOutsets() {
            return new Rect();
        }

        @Override
        public boolean getPopupMenuRuleEnabled() {
            return mRuleEnabled;
        }

        @Override
        public int getType() {
            return TYPE_ANCHOR;
        }

        @Override
        public void setPopupMenuRuleEnabled(boolean enabled) {
            mRuleEnabled = enabled;
        }
    }

    public class DummyRootView extends ViewGroup {
        public DummyRootView(Context context) {
            super(context);
        }

        @Override
        public boolean getGlobalVisibleRect(Rect rect, Point globalOffset) {
            rect.set(0, 0, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
            if (globalOffset != null) {
                globalOffset.set(0, 0);
            }
            return true;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
    }

    public COUIIsolatedPopupListWindow(Context context) {
        super(context);
        setAnimationStyle(com.coui.appcompat.R.style.Animation_COUI_IsolatedPopupListWindow);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mIsolatedOnDismissListener != null) {
            mIsolatedOnDismissListener.onDismiss();
        }
    }

    public void setIsolatedOnDismissListener(PopupWindow.OnDismissListener listener) {
        mIsolatedOnDismissListener = listener;
    }

    public void show(Context context, int x, int y) {
        show(context, x, y, null);
    }

    public void show(Context context, int x, int y, WindowManager.LayoutParams layoutParams) {
        DummyAnchorView anchor = new DummyAnchorView(context);
        anchor.setPosition(x, y);
        setAnchorView(anchor);
        super.show(anchor, x, y);
    }
}
