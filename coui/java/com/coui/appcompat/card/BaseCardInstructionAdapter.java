package com.coui.appcompat.card;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCardInstructionAdapter<HOLDER extends BaseCardInstructionAdapter.BaseHolder>
        extends RecyclerView.Adapter<HOLDER> {
    public static final Companion Companion = new Companion();
    public static final String EMPTY_STRING = "";

    private final List<BaseDisplayInfo> displayInfos;
    private int pagerLastHeight;

    public BaseCardInstructionAdapter() {
        displayInfos = new ArrayList<>();
    }

    public BaseCardInstructionAdapter(List<BaseDisplayInfo> displayInfos) {
        this();
        if (displayInfos == null) {
            throw new NullPointerException("displayInfos");
        }
        this.displayInfos.addAll(displayInfos);
    }

    @Override
    public int getItemCount() {
        return displayInfos.size();
    }

    @Override
    public void onBindViewHolder(HOLDER holder, int position) {
        holder.bind(displayInfos.get(position));
        holder.setMatchChildrenMaxHeight();
    }

    @SuppressLint("NotifyDataSetChanged")
    public final void updateDisplayInfos(List<? extends BaseDisplayInfo> displayInfos) {
        if (displayInfos == null) {
            throw new NullPointerException("displayInfos");
        }
        this.displayInfos.clear();
        this.displayInfos.addAll(displayInfos);
        pagerLastHeight = 0;
        notifyDataSetChanged();
    }

    public static abstract class BaseHolder extends RecyclerView.ViewHolder {
        private final BaseCardInstructionAdapter<?> adapter;

        public BaseHolder(View itemView, BaseCardInstructionAdapter<?> adapter) {
            super(itemView);
            if (adapter == null) {
                throw new NullPointerException("adapter");
            }
            this.adapter = adapter;
        }

        public abstract void bind(BaseDisplayInfo baseDisplayInfo);

        public final BaseCardInstructionAdapter<?> getAdapter() {
            return adapter;
        }

        public final void setMatchChildrenMaxHeight() {
            if (adapter.displayInfos.size() <= 1) {
                return;
            }
            itemView.post(() -> {
                itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(itemView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST));
                ViewParent parent = itemView.getParent();
                ViewParent grandParent = parent != null ? parent.getParent() : null;
                if (grandParent instanceof ViewPager2) {
                    ViewPager2 viewPager = (ViewPager2) grandParent;
                    int height = Math.max(adapter.pagerLastHeight, itemView.getMeasuredHeight());
                    if (viewPager.getLayoutParams().height != height) {
                        ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                        layoutParams.height = height;
                        adapter.pagerLastHeight = height;
                        viewPager.setLayoutParams(layoutParams);
                    }
                }
            });
        }
    }

    public static final class Companion {
        private Companion() {
        }

        public void updateContentAndVisibility(TextView textView, CharSequence content) {
            updateContentAndVisibility(textView, content, textView);
        }

        public void updateContentAndVisibility(TextView textView, CharSequence content, View view) {
            if (textView == null || content == null || view == null) {
                throw new NullPointerException("updateContentAndVisibility arguments cannot be null");
            }
            if (content.length() <= 0) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
                textView.setText(content);
            }
        }
    }
}
