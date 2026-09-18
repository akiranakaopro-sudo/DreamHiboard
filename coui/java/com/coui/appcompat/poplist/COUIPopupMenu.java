package com.coui.appcompat.poplist;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;

import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;

import java.util.ArrayList;

public class COUIPopupMenu {
    private final Context mContext;
    private final MenuBuilder mMenu;
    private final COUIPopupListWindow mPopup;
    private View mAnchorView;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    private OnDismissListener mOnDismissListener;

    public interface OnDismissListener {
        void onDismiss(COUIPopupMenu menu);
    }

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem item);
    }

    public COUIPopupMenu(Context context) {
        this(context, null);
    }

    public COUIPopupMenu(Context context, View anchor) {
        this(context, anchor, 0);
    }

    public COUIPopupMenu(Context context, View anchor, int gravity) {
        this(context, anchor, gravity, androidx.appcompat.R.attr.popupMenuStyle, com.coui.appcompat.R.style.Widget_COUI_PopupMenu);
    }

    public COUIPopupMenu(Context context, View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        mContext = context;
        mAnchorView = anchor;
        mMenu = new MenuBuilder(context);
        mPopup = new COUIPopupListWindow(context);
        if (anchor != null) {
            mPopup.setAnchorView(anchor);
        }
        mPopup.setOnItemClickListener((parent, view, position, id) -> {
            MenuItem item = position >= 0 && position < mMenu.size() ? mMenu.getItem(position) : null;
            if (item != null && mOnMenuItemClickListener != null && mOnMenuItemClickListener.onMenuItemClick(item)) {
                dismiss();
            }
        });
        mPopup.setOnDismissListener(() -> {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss(this);
            }
        });
    }

    public void dismiss() {
        mPopup.dismiss();
    }

    public void dismissImmediatly() {
        mPopup.forceDismiss();
    }

    public Menu getMenu() {
        return mMenu;
    }

    public MenuInflater getMenuInflater() {
        return new SupportMenuInflater(mContext);
    }

    public COUIPopupListWindow getPopup() {
        return mPopup;
    }

    public void inflate(int menuRes) {
        getMenuInflater().inflate(menuRes, mMenu);
        syncItemsFromMenu();
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    public void setAnchorView(View view) {
        mAnchorView = view;
        mPopup.setAnchorView(view);
    }

    public void setMenuRedDot(int itemId, int amount) {
        for (PopupListItem item : mPopup.getItemList()) {
            if (item.getId() == itemId) {
                item.setHintType(PopupListItem.MENU_HINT_TYPE_RED_DOT);
                item.setRedDotAmount(amount);
            }
        }
        mPopup.refresh();
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mOnMenuItemClickListener = listener;
    }

    public void show() {
        syncItemsFromMenu();
        mPopup.show();
    }

    public void show(View view) {
        setAnchorView(view);
        show();
    }

    private void syncItemsFromMenu() {
        ArrayList<PopupListItem> items = new ArrayList<>();
        for (int i = 0; i < mMenu.size(); i++) {
            MenuItem item = mMenu.getItem(i);
            items.add(new PopupListItem.Builder()
                    .setId(item.getItemId())
                    .setTitle(String.valueOf(item.getTitle()))
                    .setIcon(item.getIcon())
                    .setIsEnable(item.isEnabled())
                    .build());
        }
        mPopup.setItemList(items);
    }
}
