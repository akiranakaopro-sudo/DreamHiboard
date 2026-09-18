package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.contextutil.COUIContextUtil;

import java.util.ArrayList;
import java.util.List;

public class COUIRecommendedPreference extends Preference {
    private COUIRecommendedDrawable mBackground;
    private int mColor;
    private String mHeaderText;
    private float mRadius;
    private List<RecommendedEntity> mRecommendedEntityList;

    public interface OnRecommendedClickListener {
        void onRecommendedClick(View view);
    }

    public static class RecommendedAdapter extends RecyclerView.Adapter<RecommendedVH> {
        private static final int ITEM_VIEW_TYPE_ENTITY = 1;
        private static final int ITEM_VIEW_TYPE_HEADER = 0;
        private int mBackgroundColor;
        private Context mContext;
        private List<RecommendedEntity> mEntities = new ArrayList<>();
        private float mRadius;

        public RecommendedAdapter(Context context, List<RecommendedEntity> list, String header, float radius, int color) {
            mContext = context;
            mRadius = radius;
            mBackgroundColor = color;
            setData(list, header);
        }

        @Override
        public int getItemCount() {
            return mEntities.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ENTITY;
        }

        public void setData(List<RecommendedEntity> list, String header) {
            mEntities.clear();
            if (list != null) {
                mEntities.addAll(list);
                mEntities.add(0, new RecommendedEntity(header));
            }
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull RecommendedVH holder, int position) {
            final RecommendedEntity entity = mEntities.get(position);
            holder.mTitleView.setText(entity.title);
            holder.mLayout.setRadius(mRadius);
            holder.mLayout.setBackgroundColor(mBackgroundColor);
            if (position <= 0) {
                if (position == 0) {
                    holder.mLayout.setClickable(false);
                    holder.mLayout.setPositionInGroup(1);
                    return;
                }
                return;
            }
            int bottomPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.recommended_recyclerView_padding_bottom);
            if (position == getItemCount() - 1) {
                holder.mLayout.setPositionInGroup(3);
                holder.mLayout.setPaddingRelative(holder.mLayout.getPaddingStart(), holder.mLayout.getPaddingTop(), holder.mLayout.getPaddingEnd(), bottomPadding);
            } else if (holder.mLayout.getPaddingBottom() == bottomPadding) {
                holder.mLayout.setPaddingRelative(holder.mLayout.getPaddingStart(), holder.mLayout.getPaddingTop(), holder.mLayout.getPaddingEnd(), 0);
                holder.mLayout.setPositionInGroup(2);
            } else {
                holder.mLayout.setPositionInGroup(2);
            }
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (entity.onRecommendedClickListener != null) {
                        entity.onRecommendedClickListener.onRecommendedClick(view);
                    }
                }
            });
        }

        @NonNull
        @Override
        public RecommendedVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ITEM_VIEW_TYPE_HEADER) {
                return new RecommendedVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_head_textview, parent, false));
            }
            return new RecommendedVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_common_textview, parent, false));
        }
    }

    public static class RecommendedEntity {
        private OnRecommendedClickListener onRecommendedClickListener;
        private String title;

        public RecommendedEntity(String title) {
            this.title = title;
        }

        public RecommendedEntity(String title, OnRecommendedClickListener listener) {
            this.title = title;
            onRecommendedClickListener = listener;
        }
    }

    public static class RecommendedVH extends RecyclerView.ViewHolder {
        private COUICardListSelectedItemLayout mLayout;
        private TextView mTitleView;

        public RecommendedVH(@NonNull View view) {
            super(view);
            mLayout = (COUICardListSelectedItemLayout) view;
            TextView titleView = view.findViewById(R.id.txt_content);
            mTitleView = titleView;
            titleView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setClassName(COUIAccessibilityUtil.BUTTON_CLASS_NAME);
                }
            });
            mLayout.setClickable(true);
        }
    }

    public COUIRecommendedPreference(Context context) {
        this(context, null);
    }

    public COUIRecommendedPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiRecommendedPreferenceStyle);
    }

    public COUIRecommendedPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUIRecommendedPreference);
    }

    public COUIRecommendedPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.coui_recommended_preference_layout);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIRecommendedPreference, defStyleAttr, 0);
        mRadius = a.getDimension(R.styleable.COUIRecommendedPreference_recommendedCardBgRadius,
                COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerM));
        mColor = a.getColor(R.styleable.COUIRecommendedPreference_recommendedCardBgColor,
                COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorContainer4));
        mBackground = new COUIRecommendedDrawable(mRadius, mColor);
        String headerText = a.getString(R.styleable.COUIRecommendedPreference_recommendedHeaderTitle);
        mHeaderText = headerText;
        if (headerText == null) {
            mHeaderText = getContext().getResources().getString(R.string.bottom_recommended_header_title);
        }
        a.recycle();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        RecyclerView recyclerView = (RecyclerView) holder.itemView;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new RecommendedAdapter(getContext(), mRecommendedEntityList, mHeaderText, mRadius, mColor));
        } else {
            ((RecommendedAdapter) adapter).setData(mRecommendedEntityList, mHeaderText);
        }
        recyclerView.setFocusable(false);
    }

    public void setData(List<RecommendedEntity> list) {
        if (list == null || list.isEmpty()) {
            setVisible(false);
            return;
        }
        setVisible(true);
        mRecommendedEntityList = list;
        notifyChanged();
    }

    public void setHeaderText(String headerText) {
        setVisible(true);
        if (TextUtils.equals(mHeaderText, headerText)) {
            return;
        }
        mHeaderText = headerText;
        notifyChanged();
    }
}
