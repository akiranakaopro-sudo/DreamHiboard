package com.coui.appcompat.itemview;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;

public class COUIBaseListItemViewHolder extends RecyclerView.ViewHolder implements COUIRecyclerView.ICOUIDividerDecorationInterface {
    public static final int BOTH_RIGHT_ANGLE = 2;
    public static final int BOTH_ROUNDED_CORNER = 4;
    public static final int BOTTOM_ROUNDED_CORNER = 3;
    public static final int TOP_ROUNDED_CORNER = 1;

    private int mCardType;
    private int mDividerPadding;
    private TextView mListTitleView;
    private View mRootListItemView;

    public COUIBaseListItemViewHolder(View view) {
        super(view);
        mCardType = BOTH_RIGHT_ANGLE;
        mDividerPadding = view.getContext().getResources()
                .getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
        mRootListItemView = view.findViewById(R.id.coui_preference);
        mListTitleView = (TextView) view.findViewById(android.R.id.title);
    }

    @Override
    public boolean drawDivider() {
        if (mRootListItemView == null || !(mRootListItemView instanceof COUICardListSelectedItemLayout)) {
            return false;
        }
        return mCardType == TOP_ROUNDED_CORNER || mCardType == BOTH_RIGHT_ANGLE;
    }

    @Override
    public View getDividerEndAlignView() {
        return null;
    }

    @Override
    public int getDividerEndInset() {
        return mDividerPadding;
    }

    @Override
    public View getDividerStartAlignView() {
        return mListTitleView;
    }

    @Override
    public int getDividerStartInset() {
        return mDividerPadding;
    }

    public void setCornerType(int cornerType) {
        mCardType = cornerType;
        COUICardListHelper.setItemCardBackground(mRootListItemView, cornerType);
    }
}
