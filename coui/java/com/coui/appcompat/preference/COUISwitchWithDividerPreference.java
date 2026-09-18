package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.poplist.PreciseClickHelper;

public class COUISwitchWithDividerPreference extends COUISwitchPreference {
    private OnMainLayoutClickListener mClickListener;
    private LinearLayout mMainLayout;
    private PreciseClickHelper mPreciseHelper;
    private PreciseClickHelper.OnPreciseClickListener mPreciseListener;
    private LinearLayout mSwitchLayout;

    public interface OnMainLayoutClickListener {
        void onMainLayoutClick();
    }

    public COUISwitchWithDividerPreference(Context context) {
        this(context, null);
    }

    public COUISwitchWithDividerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiSwitchWithDividerPreferenceStyle);
    }

    public COUISwitchWithDividerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUISwitchWithDividerPreference);
    }

    public COUISwitchWithDividerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initPreciseHelper() {
        if (mMainLayout == null || mPreciseListener == null) {
            return;
        }
        removePreciseClickListener();
        PreciseClickHelper preciseClickHelper = new PreciseClickHelper(mMainLayout, (view, x, y) -> {
            mPreciseListener.onClick(view, x, y);
            if (mMainLayout == null || mClickListener == null) {
                return;
            }
            mClickListener.onMainLayoutClick();
        });
        mPreciseHelper = preciseClickHelper;
        preciseClickHelper.setup();
    }

    public OnMainLayoutClickListener getOnMainLayoutClickListener() {
        return mClickListener;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        LinearLayout mainLayout = (LinearLayout) holder.itemView.findViewById(R.id.main_layout);
        mMainLayout = mainLayout;
        View itemView = holder.itemView;
        if ((itemView instanceof COUICardListSelectedItemLayout) && mainLayout != null) {
            ((COUICardListSelectedItemLayout) itemView).setMainLayoutToSetExtraPadding(mainLayout);
        }
        super.onBindViewHolder(holder);
        initPreciseHelper();
        LinearLayout main = mMainLayout;
        if (main != null) {
            if (mPreciseListener == null) {
                main.setOnClickListener(view -> {
                    if (mClickListener != null) {
                        mClickListener.onMainLayoutClick();
                    }
                });
            }
            mMainLayout.setClickable(isSelectable());
        }
        LinearLayout switchLayout = (LinearLayout) holder.itemView.findViewById(R.id.switch_layout);
        mSwitchLayout = switchLayout;
        if (switchLayout != null) {
            switchLayout.setOnClickListener(view -> COUISwitchWithDividerPreference.super.onClick());
            mSwitchLayout.setClickable(isSelectable());
        }
    }

    public void removePreciseClickListener() {
        PreciseClickHelper preciseClickHelper = mPreciseHelper;
        if (preciseClickHelper != null) {
            preciseClickHelper.unSet();
            mPreciseHelper = null;
        }
        LinearLayout main = mMainLayout;
        if (main != null) {
            main.setOnClickListener(view -> {
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
