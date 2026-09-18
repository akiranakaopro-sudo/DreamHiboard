package com.coui.appcompat.dialog.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coui.appcompat.R;

public class COUIListDialogAdapter extends BaseAdapter {
    private final Context mContext;
    private final CharSequence[] mItems;
    private final int[] mTextAppearances;
    private boolean mIsBottom;
    private boolean mIsTop;

    private static class ViewHolder {
        ImageView divider;
        TextView textView;
        LinearLayout mainLayout;
    }

    public COUIListDialogAdapter(Context context, CharSequence[] items, int[] textAppearances) {
        mContext = context;
        mItems = items;
        mTextAppearances = textAppearances;
    }

    private View getViewInternal(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.coui_list_dialog_item,
                    parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(android.R.id.text1);
            holder.divider = convertView.findViewById(R.id.item_divider);
            holder.mainLayout = convertView.findViewById(R.id.main_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(getItem(position));
        if (mTextAppearances != null) {
            int textAppearance = mTextAppearances[position];
            holder.textView.setTextAppearance(mContext, textAppearance > 0
                    ? textAppearance : R.style.DefaultDialogItemTextStyle);
        }
        if (holder.divider != null) {
            holder.divider.setVisibility(getCount() <= 1 || position == getCount() - 1
                    ? View.GONE : View.VISIBLE);
        }
        return convertView;
    }

    private void resetPadding(int position, View view) {
        Resources resources = mContext.getResources();
        int extra = resources.getDimensionPixelSize(
                R.dimen.coui_bottom_alert_dialog_vertical_button_padding_bottom_extra_new);
        int vertical = resources.getDimensionPixelSize(
                R.dimen.coui_bottom_alert_dialog_vertical_button_padding_vertical_new);
        int left = resources.getDimensionPixelSize(R.dimen.alert_dialog_list_item_padding_left);
        int right = resources.getDimensionPixelSize(R.dimen.alert_dialog_list_item_padding_right);
        if (position == getCount() - 1 && mIsBottom) {
            view.setPadding(left, vertical, right, vertical + extra);
        } else if (position == 0 && mIsTop) {
            view.setPadding(left, vertical + extra, right, vertical);
        } else {
            view.setPadding(left, vertical, right, vertical);
        }
    }

    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.length;
    }

    @Override
    public CharSequence getItem(int position) {
        return mItems == null ? null : mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = getViewInternal(position, convertView, parent);
        resetPadding(position, view.findViewById(R.id.main_layout));
        return view;
    }

    public void setIsBottom(boolean isBottom) {
        mIsBottom = isBottom;
    }

    public void setIsTop(boolean isTop) {
        mIsTop = isTop;
    }
}
