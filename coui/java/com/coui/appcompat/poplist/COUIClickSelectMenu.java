package com.coui.appcompat.poplist;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import com.coui.appcompat.R;

import java.util.ArrayList;

public class COUIClickSelectMenu {
    private static final String TAG = "COUIClickSelectMenu";
    private boolean mEnable = true;
    private PreciseClickHelper mHelper;
    private final InputMethodManager mInputManager;
    private PreciseClickHelper.OnPreciseClickListener mListener;
    private COUIContextMenu.MenuShowStateListener mMenuShowStateListener;
    private final COUIPopupListWindow mPopup;

    private class ClickListener implements PreciseClickHelper.OnPreciseClickListener {
        @Override
        public void onClick(View view, int x, int y) {
            if (mListener != null) {
                mListener.onClick(view, x, y);
            }
            if (mInputManager == null || !mInputManager.hideSoftInputFromWindow(view.getWindowToken(), 0)) {
                setPopupShow(view, x, y);
            } else {
                view.postDelayed(() -> setPopupShow(view, x, y),
                        view.getContext().getResources().getInteger(R.integer.support_menu_click_select_time));
            }
        }
    }

    public COUIClickSelectMenu(Context context) {
        this(context, null);
    }

    public COUIClickSelectMenu(Context context, View view) {
        mPopup = new COUIPopupListWindow(context);
        if (view != null) {
            mPopup.setAnchorView(view);
        }
        mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void dismiss() {
        mPopup.dismiss();
    }

    public COUIPopupListWindow getPopup() {
        return mPopup;
    }

    public void registerForClickSelectItems(View view, ArrayList<PopupListItem> items) {
        if (items.size() <= 0) {
            return;
        }
        mPopup.setItemList(items);
        view.setClickable(true);
        view.setLongClickable(true);
        mPopup.setAnchorView(view);
        if (mHelper == null || mHelper.getTargetView() != view) {
            mHelper = new PreciseClickHelper(view, new ClickListener());
            if (mEnable) {
                mHelper.setup();
            }
        } else {
            Log.w(TAG, "ItemView is same, no need to create PreciseClickHelper");
        }
    }

    @Deprecated
    public void registerForClickSelectItems(View view, ArrayList<PopupListItem> items, int color) {
        registerForClickSelectItems(view, items);
        mPopup.setSelectItemColor(color);
    }

    @Deprecated
    public void setEnableAddExtraWidth(boolean enable) {
        if (mEnable) {
            mPopup.setEnableAddExtraWidth(enable);
        }
    }

    public void setHelperEnabled(boolean enable) {
        mEnable = enable;
        if (mHelper != null) {
            if (enable) {
                mHelper.setup();
            } else {
                mHelper.unSet();
            }
        }
    }

    @Deprecated
    public void setMaxShowItemCount(int count) {
        mPopup.setMaxShowItemCount(count);
    }

    public void setMenuShowStateListener(COUIContextMenu.MenuShowStateListener listener) {
        mMenuShowStateListener = listener;
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mMenuShowStateListener != null) {
                    mMenuShowStateListener.onDismiss();
                }
            }
        });
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mPopup.setOnItemClickListener(listener);
    }

    public void setOnPreciseClickListener(PreciseClickHelper.OnPreciseClickListener listener) {
        mListener = listener;
    }

    public void setPopupShow(View view) {
        setPopupShow(view, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void setPopupShow(View view, int x, int y) {
        if (mEnable) {
            mPopup.show(view, x, y);
            if (mMenuShowStateListener != null) {
                mMenuShowStateListener.onShow();
            }
        }
    }

    public void setPopupState(boolean show) {
        if (!show || !mEnable) {
            dismiss();
            return;
        }
        mPopup.show();
        if (mMenuShowStateListener != null) {
            mMenuShowStateListener.onShow();
        }
    }

    public void setSubMenuItemClickListener(AdapterView.OnItemClickListener listener) {
        mPopup.setSubMenuClickListener(listener);
    }
}
