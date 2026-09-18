package com.coui.appcompat.panel;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.coui.appcompat.R;
import com.coui.appcompat.checkbox.COUICheckBox;
import java.util.HashSet;


public class COUIBottomSheetChoiceListAdapter extends RecyclerView.Adapter<COUIBottomSheetChoiceListAdapter.ViewHolder> {
    private HashSet<Integer> mCheckBoxStates;
    private int mCheckedItem;
    private Context mContext;
    private boolean mIsMultiChoice;
    private CharSequence[] mItems;
    private int mLayoutResId;
    private OnItemClickListener mOnItemClickListener;
    private CharSequence[] mSummaries;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, int state);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        COUICheckBox checkBox;
        TextView itemText;
        View mLayout;
        RadioButton radioButton;
        TextView summaryText;

        public ViewHolder(View view) {
            super(view);
            this.itemText = (TextView) view.findViewById(android.R.id.text1);
            this.summaryText = (TextView) view.findViewById(R.id.summary_text2);
            if (COUIBottomSheetChoiceListAdapter.this.mIsMultiChoice) {
                this.checkBox = (COUICheckBox) view.findViewById(R.id.checkbox);
            } else {
                this.radioButton = (RadioButton) view.findViewById(R.id.radio_button);
            }
            view.setBackground(COUIBottomSheetChoiceListAdapter.this.mContext.getDrawable(com.coui.appcompat.R.drawable.coui_list_selector_background));
            this.mLayout = view;
        }
    }

    public COUIBottomSheetChoiceListAdapter(Context context, int layoutResId, CharSequence[] items, CharSequence[] summaries, int checkedItem, boolean[] checkedItems, boolean isMultiChoice) {
        this.mCheckedItem = -1;
        this.mContext = context;
        this.mLayoutResId = layoutResId;
        this.mItems = items;
        this.mSummaries = summaries;
        this.mIsMultiChoice = isMultiChoice;
        this.mCheckBoxStates = new HashSet<>();
        this.mCheckedItem = checkedItem;
        if (checkedItems != null) {
            initCheckboxStates(checkedItems);
        }
    }

    private void initCheckboxStates(boolean[] checkedItems) {
        for (int position = 0; position < checkedItems.length; position++) {
            if (checkedItems[position]) {
                this.mCheckBoxStates.add(Integer.valueOf(position));
            }
        }
    }

    public CharSequence getItem(int position) {
        CharSequence[] items = this.mItems;
        if (items == null || position >= items.length) {
            return null;
        }
        return items[position];
    }

    @Override
    public int getItemCount() {
        CharSequence[] items = this.mItems;
        if (items == null) {
            return 0;
        }
        return items.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public CharSequence getSummary(int position) {
        CharSequence[] summaries = this.mSummaries;
        if (summaries == null || position >= summaries.length) {
            return null;
        }
        return summaries[position];
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        if (this.mIsMultiChoice) {
            viewHolder.checkBox.setState(this.mCheckBoxStates.contains(position) ? 2 : 0);
        } else {
            viewHolder.radioButton.setChecked(this.mCheckedItem == position);
        }
        CharSequence item = getItem(position);
        CharSequence summary = getSummary(position);
        viewHolder.itemText.setText(item);
        if (TextUtils.isEmpty(summary)) {
            viewHolder.summaryText.setVisibility(View.GONE);
        } else {
            viewHolder.summaryText.setVisibility(View.VISIBLE);
            viewHolder.summaryText.setText(summary);
        }
        if (this.mOnItemClickListener != null) {
            viewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int state;
                    if (COUIBottomSheetChoiceListAdapter.this.mIsMultiChoice) {
                        if (viewHolder.checkBox.getState() != 2) {
                            COUIBottomSheetChoiceListAdapter.this.mCheckBoxStates.add(Integer.valueOf(position));
                        } else {
                            COUIBottomSheetChoiceListAdapter.this.mCheckBoxStates.remove(Integer.valueOf(position));
                        }
                        state = COUIBottomSheetChoiceListAdapter.this.mCheckBoxStates.contains(Integer.valueOf(position)) ? 2 : 0;
                        viewHolder.checkBox.setState(state);
                    } else {
                        if (position == COUIBottomSheetChoiceListAdapter.this.mCheckedItem) {
                            COUIBottomSheetChoiceListAdapter.this.mOnItemClickListener.onItemClick(view, position, 0);
                            return;
                        }
                        boolean wasChecked = viewHolder.radioButton.isChecked();
                        state = !wasChecked ? 1 : 0;
                        viewHolder.radioButton.setChecked(!wasChecked);
                        COUIBottomSheetChoiceListAdapter adapter = COUIBottomSheetChoiceListAdapter.this;
                        adapter.notifyItemChanged(adapter.mCheckedItem);
                        COUIBottomSheetChoiceListAdapter.this.mCheckedItem = position;
                    }
                    COUIBottomSheetChoiceListAdapter.this.mOnItemClickListener.onItemClick(view, position, state);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(this.mLayoutResId, parent, false));
    }

    public COUIBottomSheetChoiceListAdapter(Context context, int layoutResId, CharSequence[] items, CharSequence[] summaries, int checkedItem) {
        this(context, layoutResId, items, summaries, checkedItem, null, false);
    }
}
