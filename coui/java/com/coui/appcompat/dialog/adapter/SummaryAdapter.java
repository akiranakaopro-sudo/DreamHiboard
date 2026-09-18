package com.coui.appcompat.dialog.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coui.appcompat.R;

public class SummaryAdapter extends BaseAdapter {
    private final Context mContext;
    private boolean mIsBottom;
    private boolean mIsTop;
    private final CharSequence[] mItems;
    private final CharSequence[] mSummaries;
    private final int[] mTextColor;

    private static class ViewHolder {
        ImageView divider;
        TextView itemView;
        LinearLayout mainLayout;
        TextView summaryView;
    }

    public SummaryAdapter(Context context, boolean isTop, boolean isBottom,
            CharSequence[] items, CharSequence[] summaries, int[] textColor) {
        mIsTop = isTop;
        mIsBottom = isBottom;
        mContext = context;
        mItems = items;
        mSummaries = summaries;
        mTextColor = textColor;
    }

    private void resetPadding(int position, View view) {
        int extra = mContext.getResources().getDimensionPixelSize(
                R.dimen.coui_bottom_alert_dialog_vertical_button_padding_bottom_extra_new);
        int vertical = mContext.getResources().getDimensionPixelSize(
                R.dimen.coui_bottom_alert_dialog_vertical_button_padding_vertical_new);
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        if (position == getCount() - 1 && mIsBottom) {
            view.setPadding(paddingLeft, vertical, paddingRight, extra + vertical);
        } else if (position == 0 && mIsTop) {
            view.setPadding(paddingLeft, extra + vertical, paddingRight, vertical);
        } else {
            view.setPadding(paddingLeft, vertical, paddingRight, vertical);
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

    public CharSequence getSummary(int position) {
        return mSummaries != null && position < mSummaries.length ? mSummaries[position] : null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.coui_alert_dialog_summary_item, parent, false);
            holder = new ViewHolder();
            holder.itemView = convertView.findViewById(android.R.id.text1);
            holder.summaryView = convertView.findViewById(R.id.summary_text2);
            holder.divider = convertView.findViewById(R.id.item_divider);
            holder.mainLayout = convertView.findViewById(R.id.main_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.itemView.setText(getItem(position));
        CharSequence summary = getSummary(position);
        if (TextUtils.isEmpty(summary)) {
            holder.summaryView.setVisibility(View.GONE);
        } else {
            holder.summaryView.setVisibility(View.VISIBLE);
            holder.summaryView.setText(summary);
        }
        resetPadding(position, holder.mainLayout);
        if (mTextColor != null && position >= 0 && position < mTextColor.length) {
            holder.itemView.setTextColor(mTextColor[position]);
        }
        if (holder.divider != null) {
            holder.divider.setVisibility(getCount() <= 1 || position == getCount() - 1
                    ? View.GONE : View.VISIBLE);
        }
        convertView.requestLayout();
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
