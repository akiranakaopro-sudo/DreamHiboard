package com.coui.appcompat.dialog.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.coui.appcompat.R;
import com.coui.appcompat.checkbox.COUICheckBox;

import java.util.HashSet;

public class ChoiceListCursorAdapter extends CursorAdapter {
    private HashSet<Integer> mCheckBoxStates;
    private String mLabelColumn;
    private String mSummaryColumn;
    private String mIsCheckedColumn;
    private int mLabelIndex;
    private int mSummaryIndex;
    private int mIsCheckedIndex;
    private int mLayoutResId;
    private boolean mIsMultiChoice;

    public ChoiceListCursorAdapter(Context context, Cursor cursor, int layoutResId,
            String labelColumn, String summaryColumn) {
        this(context, cursor, layoutResId, labelColumn, null, summaryColumn, false);
    }

    public ChoiceListCursorAdapter(Context context, Cursor cursor, int layoutResId,
            String labelColumn, String isCheckedColumn, String summaryColumn, boolean isMultiChoice) {
        this(context, cursor);
        mIsMultiChoice = isMultiChoice;
        mLabelColumn = labelColumn;
        mSummaryColumn = summaryColumn;
        mIsCheckedColumn = isCheckedColumn;
        mLayoutResId = layoutResId;
        mCheckBoxStates = new HashSet<>();
        mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
        if (mSummaryColumn != null) {
            mSummaryIndex = cursor.getColumnIndexOrThrow(mSummaryColumn);
        }
        if (isMultiChoice) {
            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(mIsCheckedIndex) == 1) {
                        mCheckBoxStates.add(cursor.getPosition());
                    }
                } while (cursor.moveToNext());
            }
            cursor.moveToFirst();
        }
    }

    public ChoiceListCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
        mIsMultiChoice = false;
        mIsCheckedIndex = 0;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = view.findViewById(android.R.id.text1);
        TextView summaryView = view.findViewById(R.id.summary_text2);
        int state = mCheckBoxStates.contains(cursor.getPosition()) ? COUICheckBox.SELECT_ALL
                : COUICheckBox.SELECT_NONE;
        if (mIsMultiChoice) {
            ((COUICheckBox) view.findViewById(R.id.checkbox)).setState(state);
        }
        textView.setText(cursor.getString(mLabelIndex));
        if (mSummaryColumn == null) {
            summaryView.setVisibility(View.GONE);
        } else {
            summaryView.setVisibility(View.VISIBLE);
            summaryView.setText(cursor.getString(mSummaryIndex));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(mLayoutResId, parent, false);
    }

    public void setCheckboxState(int state, int position, ListView listView) {
        int visibleIndex = position - listView.getFirstVisiblePosition();
        if (visibleIndex >= 0) {
            ((COUICheckBox) listView.getChildAt(visibleIndex).findViewById(R.id.checkbox)).setState(state);
            if (state == COUICheckBox.SELECT_ALL) {
                mCheckBoxStates.add(position);
            } else {
                mCheckBoxStates.remove(position);
            }
        }
    }
}
