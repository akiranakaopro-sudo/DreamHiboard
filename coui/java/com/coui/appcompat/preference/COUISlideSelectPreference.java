package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;

public class COUISlideSelectPreference extends COUIPreference {
    public static final int FORCE_CLICK = 1;
    public static final int FORCE_UNCLICK = 2;
    public static final int NORMAL = 0;

    private int mClickStyle;
    Context mContext;
    CharSequence mSelectionText;
    private TextView mStatus1;

    public COUISlideSelectPreference(Context context) {
        this(context, null);
    }

    public COUISlideSelectPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiSlideSelectPreferenceStyle);
    }

    public COUISlideSelectPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUISelectPreference);
    }

    public COUISlideSelectPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mClickStyle = NORMAL;
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUISlideSelectPreference, defStyleAttr, defStyleRes);
        mSelectionText = a.getText(R.styleable.COUISlideSelectPreference_coui_select_status1);
        a.recycle();
    }

    public CharSequence getSelectionText() {
        return mSelectionText != null ? mSelectionText : "";
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View preferenceView = holder.findViewById(R.id.coui_preference);
        if (preferenceView != null) {
            preferenceView.setTag(new Object());
            if (mClickStyle == FORCE_CLICK) {
                preferenceView.setClickable(false);
            } else if (mClickStyle == FORCE_UNCLICK) {
                preferenceView.setClickable(true);
            }
        }
        View statusView = holder.findViewById(R.id.coui_statusText_select);
        if (statusView == null || !(statusView instanceof TextView)) {
            return;
        }
        mStatus1 = (TextView) statusView;
        CharSequence selectionText = mSelectionText;
        if (TextUtils.isEmpty(selectionText)) {
            mStatus1.setVisibility(View.GONE);
        } else {
            mStatus1.setText(selectionText);
            mStatus1.setVisibility(View.VISIBLE);
        }
    }

    public void setBlurView(View view) {
    }

    public void setSelectionText(CharSequence selectionText) {
        if ((selectionText != null || mSelectionText == null) && (selectionText == null || selectionText.equals(mSelectionText))) {
            return;
        }
        mSelectionText = selectionText;
        notifyChanged();
    }

    public void setStatusText(CharSequence statusText) {
        if ((statusText != null || mSelectionText == null) && (statusText == null || statusText.equals(mSelectionText))) {
            return;
        }
        mSelectionText = statusText;
        notifyChanged();
    }
}
