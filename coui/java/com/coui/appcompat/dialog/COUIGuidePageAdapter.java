package com.coui.appcompat.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;

import java.util.ArrayList;
import java.util.List;

class COUIGuidePageAdapter extends RecyclerView.Adapter<COUIGuidePageAdapter.GuideViewHolder> {
    private final Context mContext;
    private final List<COUIGuidePageItem> mItems;
    private int mImagePaddingLeftDp = 0;
    private int mImagePaddingTopDp = 0;
    private int mImagePaddingRightDp = 0;
    private int mImagePaddingBottomDp = 16;
    private int mImageHeightDp = 328;

    public static class GuideViewHolder extends RecyclerView.ViewHolder {
        final TextView mDescView;
        final ImageView mImageView;
        final TextView mTitleView;

        public GuideViewHolder(@NonNull View view) {
            super(view);
            mImageView = view.findViewById(R.id.pager_item_im);
            mTitleView = view.findViewById(R.id.guide_tips_title);
            mDescView = view.findViewById(R.id.guide_tips_description);
        }
    }

    COUIGuidePageAdapter(Context context, List<COUIGuidePageItem> list) {
        mContext = context;
        mItems = new ArrayList<>(list);
    }

    private int dpToPx(int dp) {
        return Math.round(mContext.getResources().getDisplayMetrics().density * dp);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setImageHeightDp(int imageHeightDp) {
        mImageHeightDp = imageHeightDp;
        notifyDataSetChanged();
    }

    public void setImagePadding(int left, int top, int right, int bottom) {
        mImagePaddingLeftDp = left;
        mImagePaddingTopDp = top;
        mImagePaddingRightDp = right;
        mImagePaddingBottomDp = bottom;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        COUIGuidePageItem item = mItems.get(position);
        if (item.getImageResId() != 0) {
            holder.mImageView.setImageResource(item.getImageResId());
            holder.mImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mImageView.setVisibility(View.GONE);
        }
        holder.mTitleView.setText(item.getTitle());
        holder.mDescView.setText(item.getDescription());
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) holder.mImageView.getLayoutParams();
        if (params != null) {
            params.setMargins(mImagePaddingLeftDp, mImagePaddingTopDp, mImagePaddingRightDp,
                    mImagePaddingBottomDp);
            params.height = dpToPx(mImageHeightDp);
            holder.mImageView.setLayoutParams(params);
        }
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GuideViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coui_guide_page_item, parent, false));
    }
}
