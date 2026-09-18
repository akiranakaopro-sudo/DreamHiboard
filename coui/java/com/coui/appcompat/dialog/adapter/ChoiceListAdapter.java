package com.coui.appcompat.dialog.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.checkbox.COUICheckBox;

public class ChoiceListAdapter extends BaseAdapter {
    private boolean[] mCheckBoxStates;
    private Context mContext;
    private boolean[] mDisableStatus;
    private Drawable[] mDrawables;
    private int[] mIcons;
    private boolean mIsBottom;
    private boolean mIsMultiChoice;
    private boolean mIsTop;
    private CharSequence[] mItems;
    private int mLayoutResId;
    private MaxCheckedListener mMaxCheckedListener;
    private int mMaxCheckedNum;
    private MultiChoiceItemClickListener mMultiChoiceItemClickListener;
    private CharSequence[] mSummaries;

    public interface MaxCheckedListener {
        void maxCheckedNotice(int maxCheckedNum);
    }

    public interface MultiChoiceItemClickListener {
        void onClick(int position, boolean checked);
    }

    public static class ViewHolder {
        COUICheckBox checkBox;
        ImageView divider;
        ImageView icon;
        TextView itemText;
        RadioButton radioButton;
        FrameLayout radioLayout;
        TextView summaryText;
        LinearLayout textLayout;
    }

    public ChoiceListAdapter(Context context, int layoutResId, CharSequence[] items,
            CharSequence[] summaries, boolean[] checkedItems, boolean isMultiChoice) {
        this(context, layoutResId, items, summaries, checkedItems, null, isMultiChoice);
    }

    public ChoiceListAdapter(Context context, int layoutResId, CharSequence[] items,
            CharSequence[] summaries, boolean[] checkedItems, boolean[] disableStatus,
            boolean isMultiChoice) {
        this(context, layoutResId, items, summaries, checkedItems, disableStatus, isMultiChoice, 0);
    }

    public ChoiceListAdapter(Context context, int layoutResId, CharSequence[] items,
            CharSequence[] summaries, boolean[] checkedItems, boolean[] disableStatus,
            boolean isMultiChoice, int maxCheckedNum) {
        mIsTop = false;
        mIsBottom = false;
        mContext = context;
        mLayoutResId = layoutResId;
        mItems = items;
        mSummaries = summaries;
        mIsMultiChoice = isMultiChoice;
        mCheckBoxStates = new boolean[items.length];
        if (checkedItems != null) {
            initCheckboxStates(checkedItems);
        }
        mDisableStatus = new boolean[mItems.length];
        if (disableStatus != null) {
            initCheckboxStatesDisable(disableStatus);
        }
        mMaxCheckedNum = maxCheckedNum;
    }

    public ChoiceListAdapter(Context context, int layoutResId, CharSequence[] items,
            CharSequence[] summaries) {
        this(context, layoutResId, items, summaries, null, false);
    }

    private int getCheckedNum() {
        int checked = 0;
        for (boolean state : mCheckBoxStates) {
            if (state) {
                checked++;
            }
        }
        return checked;
    }

    private void initCheckboxStates(boolean[] states) {
        for (int i = 0; i < states.length; i++) {
            if (i >= mCheckBoxStates.length) {
                return;
            }
            mCheckBoxStates[i] = states[i];
        }
    }

    private void initCheckboxStatesDisable(boolean[] states) {
        for (int i = 0; i < states.length; i++) {
            if (i >= mDisableStatus.length) {
                return;
            }
            mDisableStatus[i] = states[i];
        }
    }

    private void setPaddingBottom(View view, int bottom) {
        if (view != null) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), bottom);
        }
    }

    public boolean[] getCheckBoxStates() {
        return mCheckBoxStates;
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
    public int getItemViewType(int position) {
        return position;
    }

    public MultiChoiceItemClickListener getMultiChoiceItemClickListener() {
        return mMultiChoiceItemClickListener;
    }

    public CharSequence getSummary(int position) {
        return mSummaries != null && position < mSummaries.length ? mSummaries[position] : null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemView;
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            itemView = LayoutInflater.from(mContext).inflate(mLayoutResId, parent, false);
            holder.icon = itemView.findViewById(R.id.alertdialog_choice_icon);
            holder.textLayout = itemView.findViewById(R.id.text_layout);
            holder.itemText = itemView.findViewById(android.R.id.text1);
            holder.summaryText = itemView.findViewById(R.id.summary_text2);
            holder.divider = itemView.findViewById(R.id.item_divider);
            if (mIsMultiChoice) {
                holder.checkBox = itemView.findViewById(R.id.checkbox);
            } else {
                holder.radioLayout = itemView.findViewById(R.id.radio_layout);
                holder.radioButton = itemView.findViewById(R.id.radio_button);
            }
            itemView.setTag(holder);
        } else {
            itemView = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        boolean disabled = mDisableStatus[position];
        holder.itemText.setEnabled(!disabled);
        holder.summaryText.setEnabled(!disabled);
        if (mIsMultiChoice) {
            holder.checkBox.setEnabled(!disabled);
        } else {
            holder.radioButton.setEnabled(!disabled);
        }
        itemView.setOnTouchListener(disabled ? (view, event) -> true : null);

        if (mIsMultiChoice) {
            holder.checkBox.setState(mCheckBoxStates[position] ? 2 : 0);
            itemView.setOnClickListener(view -> {
                View checkView = view.findViewById(R.id.checkbox);
                if (checkView instanceof COUICheckBox) {
                    COUICheckBox checkBox = (COUICheckBox) checkView;
                    if (checkBox.getState() == 2) {
                        checkBox.setState(0);
                        mCheckBoxStates[position] = false;
                    } else if (mMaxCheckedNum <= 0 || mMaxCheckedNum > getCheckedNum()) {
                        checkBox.setState(2);
                        mCheckBoxStates[position] = true;
                    } else if (mMaxCheckedListener != null) {
                        mMaxCheckedListener.maxCheckedNotice(mMaxCheckedNum);
                    }
                    if (mMultiChoiceItemClickListener != null) {
                        mMultiChoiceItemClickListener.onClick(position, checkBox.getState() == 2);
                    }
                } else if (checkView instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) checkView;
                    checkBox.setChecked(!checkBox.isChecked());
                    if (mMultiChoiceItemClickListener != null) {
                        mMultiChoiceItemClickListener.onClick(position, checkBox.isChecked());
                    }
                }
            });
        } else {
            holder.radioButton.setChecked(mCheckBoxStates[position]);
        }

        holder.itemText.setText(getItem(position));
        CharSequence summary = getSummary(position);
        if (TextUtils.isEmpty(summary)) {
            holder.summaryText.setVisibility(View.GONE);
        } else {
            holder.summaryText.setVisibility(View.VISIBLE);
            holder.summaryText.setText(summary);
        }
        if (holder.divider != null) {
            holder.divider.setVisibility(getCount() == 1 || position == getCount() - 1
                    ? View.GONE : View.VISIBLE);
        }

        int[] icons = mIcons;
        if (icons != null && position < icons.length) {
            Drawable drawable = ContextCompat.getDrawable(mContext, icons[position]);
            if (drawable != null) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageDrawable(drawable);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
        } else {
            Drawable drawable = mDrawables != null && position < mDrawables.length ? mDrawables[position] : null;
            if (drawable != null) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageDrawable(drawable);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
        }
        return itemView;
    }

    public void setCheckboxState(int state, int position, ListView listView) {
        int childPosition = position - listView.getFirstVisiblePosition();
        if (childPosition < 0) {
            return;
        }
        View child = listView.getChildAt(childPosition);
        if (child == null) {
            return;
        }
        ViewHolder holder = (ViewHolder) child.getTag();
        if (mIsMultiChoice && holder.checkBox != null) {
            holder.checkBox.setState(state);
            mCheckBoxStates[position] = state == 2;
        }
    }

    public void setDrawables(Drawable[] drawables) {
        mDrawables = drawables;
    }

    public void setIcons(int[] icons) {
        mIcons = icons;
    }

    public void setIsBottom(boolean isBottom) {
        mIsBottom = isBottom;
    }

    public void setIsTop(boolean isTop) {
        mIsTop = isTop;
    }

    public void setMaxCheckedListener(MaxCheckedListener listener) {
        mMaxCheckedListener = listener;
    }

    public void setMultiChoiceItemClickListener(MultiChoiceItemClickListener listener) {
        mMultiChoiceItemClickListener = listener;
    }
}
