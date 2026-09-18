package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.poplist.PreciseClickHelper;

public class COUIMarkWithDividerPreference extends COUIMarkPreference {
    private OnMainLayoutClickListener mClickListener;
    private LinearLayout mMainLayout;
    private PreciseClickHelper mPreciseHelper;
    private PreciseClickHelper.OnPreciseClickListener mPreciseListener;
    private LinearLayout mRadioLayout;

    public interface OnMainLayoutClickListener {
        void onMainLayoutClick();
    }

    public COUIMarkWithDividerPreference(Context context) {
        this(context, null);
    }

    public COUIMarkWithDividerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiRadioWithDividerPreferenceStyle);
    }

    public COUIMarkWithDividerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUIRadioWithDividerPreference);
    }

    public COUIMarkWithDividerPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initPreciseHelper() {
        if (mMainLayout == null || mPreciseListener == null) {
            return;
        }
        removePreciseClickListener();
        mPreciseHelper = new PreciseClickHelper(mMainLayout, (view, x, y) -> {
            mPreciseListener.onClick(view, x, y);
            if (mMainLayout != null && mClickListener != null) {
                mClickListener.onMainLayoutClick();
            }
        });
        mPreciseHelper.setup();
    }

    public OnMainLayoutClickListener getOnMainLayoutClickListener() {
        return mClickListener;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        mMainLayout = (LinearLayout) holder.itemView.findViewById(R.id.main_layout);
        View itemView = holder.itemView;
        if (itemView instanceof COUICardListSelectedItemLayout && mMainLayout != null) {
            ((COUICardListSelectedItemLayout) itemView).setMainLayoutToSetExtraPadding(mMainLayout);
        }
        super.onBindViewHolder(holder);
        initPreciseHelper();
        if (mMainLayout != null) {
            if (mPreciseListener == null) {
                mMainLayout.setOnClickListener(view -> {
                    if (mClickListener != null) {
                        mClickListener.onMainLayoutClick();
                    }
                });
            }
            mMainLayout.setClickable(isSelectable());
        }
        mRadioLayout = (LinearLayout) holder.itemView.findViewById(R.id.radio_layout);
        if (mRadioLayout != null) {
            mRadioLayout.setOnClickListener(view -> super.onClick());
            mRadioLayout.setClickable(isSelectable());
        }
    }

    public void removePreciseClickListener() {
        if (mPreciseHelper != null) {
            mPreciseHelper.unSet();
            mPreciseHelper = null;
        }
        if (mMainLayout != null) {
            mMainLayout.setOnClickListener(view -> {
                if (mClickListener != null) {
                    mClickListener.onMainLayoutClick();
                }
            });
        }
    }

    public void setOnMainLayoutListener(OnMainLayoutClickListener listener) {
        mClickListener = listener;
    }

    public void setOnPreciseClickListener(PreciseClickHelper.OnPreciseClickListener listener) {
        mPreciseListener = listener;
        initPreciseHelper();
    }
}
