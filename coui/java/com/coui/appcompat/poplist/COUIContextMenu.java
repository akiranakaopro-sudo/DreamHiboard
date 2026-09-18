package com.coui.appcompat.poplist;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import java.util.ArrayList;

public class COUIContextMenu {
    private final Context mContext;
    private final COUIPopupListWindow mPopupListWindow;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private MenuShowStateListener mMenuShowStateListener;
    private View.OnTouchListener mOnTouchListener;

    public interface MenuShowStateListener {
        void onShow();
        void onDismiss();
    }

    public COUIContextMenu(Context context) {
        this(context, null);
    }

    public COUIContextMenu(Context context, View view) {
        mContext = context;
        mPopupListWindow = new COUIPopupListWindow(context);
        if (view != null) {
            mPopupListWindow.setAnchorView(view);
        }
    }

    public void dismiss() {
        mPopupListWindow.dismiss();
    }

    public COUIPopupListWindow getPopupListWindow() {
        return mPopupListWindow;
    }

    public void registerForContextMenu(View view, MenuBuilder menuBuilder) {
        ArrayList<PopupListItem> items = new ArrayList<>();
        for (int i = 0; i < menuBuilder.size(); i++) {
            MenuItem item = menuBuilder.getItem(i);
            items.add(new PopupListItem.Builder()
                    .setId(item.getItemId())
                    .setTitle(String.valueOf(item.getTitle()))
                    .setIcon(item.getIcon())
                    .setIsEnable(item.isEnabled())
                    .build());
        }
        mPopupListWindow.setItemList(items);
        registerForContextMenu(view, mPopupListWindow);
    }

    public void registerForContextMenu(View view, final COUIPopupListWindow popupListWindow) {
        PreciseLongPressHelper helper = new PreciseLongPressHelper(view, (target, x, y) -> {
            popupListWindow.show(target, x, y);
            if (mMenuShowStateListener != null) {
                mMenuShowStateListener.onShow();
            }
        });
        helper.setTouchListenerTransfer(mOnTouchListener);
        helper.setup();
        popupListWindow.setOnItemClickListener(mOnItemClickListener);
        popupListWindow.setOnDismissListener(() -> {
            if (mMenuShowStateListener != null) {
                mMenuShowStateListener.onDismiss();
            }
        });
    }

    public void setMenuShowStateListener(MenuShowStateListener listener) {
        mMenuShowStateListener = listener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
        mPopupListWindow.setOnItemClickListener(listener);
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mOnTouchListener = listener;
    }
}
