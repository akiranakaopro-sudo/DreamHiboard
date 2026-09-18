package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;

public class COUIPagerFooterPreference extends Preference {
    private boolean mIsEnableClickSpan;
    private boolean mWithExtraMarginBottom;

    public COUIPagerFooterPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWithExtraMarginBottom = true;
        setLayoutResource(R.layout.coui_pager_footer_preference);
        TypedArray footerArray = context.obtainStyledAttributes(attrs,
                R.styleable.COUIPagerFooterPreference, 0, 0);
        mWithExtraMarginBottom = footerArray.getBoolean(
                R.styleable.COUIPagerFooterPreference_withExtraMarginBottom,
                mWithExtraMarginBottom);
        footerArray.recycle();
        TypedArray preferenceArray = context.obtainStyledAttributes(attrs,
                R.styleable.COUIPreference, 0, 0);
        mIsEnableClickSpan = preferenceArray.getBoolean(
                R.styleable.COUIPreference_couiEnalbeClickSpan, false);
        preferenceArray.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (holder.itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            if (mWithExtraMarginBottom) {
                params.bottomMargin = getContext().getResources().getDimensionPixelSize(
                        R.dimen.support_preference_footer_preference_margin_bottom);
            } else {
                params.bottomMargin = 0;
            }
            holder.itemView.setLayoutParams(params);
        }
        if (mIsEnableClickSpan) {
            COUIPreferenceUtils.setSummaryView(getContext(), holder);
        }
    }

    public void setIsEnableClickSpan(boolean enableClickSpan) {
        if (mIsEnableClickSpan != enableClickSpan) {
            mIsEnableClickSpan = enableClickSpan;
            notifyChanged();
        }
    }
}
