package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;

public class COUIBottomPreference extends Preference {
    private int mPlaceholderHeight;

    public COUIBottomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.coui_preference_bottom);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.COUIBottomPreference, 0, 0);
        mPlaceholderHeight = typedArray.getDimensionPixelSize(
                R.styleable.COUIBottomPreference_placeholderHeight,
                getContext().getResources().getDimensionPixelSize(R.dimen.support_preference_foot_preference_padding_bottom));
        typedArray.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        int height = layoutParams.height;
        int placeholderHeight = mPlaceholderHeight;
        if (height != placeholderHeight) {
            layoutParams.height = placeholderHeight;
            holder.itemView.setLayoutParams(layoutParams);
        }
    }

    public void setPlaceholderHeight(int placeholderHeight) {
        if (mPlaceholderHeight != placeholderHeight) {
            mPlaceholderHeight = placeholderHeight;
            notifyChanged();
        }
    }
}
