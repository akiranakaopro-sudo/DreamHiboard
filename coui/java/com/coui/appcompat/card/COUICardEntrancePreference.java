package com.coui.appcompat.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.theme.COUIThemeOverlay;

public class COUICardEntrancePreference extends COUIPressFeedbackJumpPreference {
    public static final Companion Companion = new Companion();
    public static final int CARD_TYPE_SMALL = 1;
    public static final int CARD_TYPE_LARGE = 2;
    public static final int TINT_ICON_NONE = 0;
    public static final int TINT_ICON_BY_GLOBAL_THEME = 1;
    public static final int TINT_ICON_ANYWAY = 2;

    private static final int LAYOUT_RES_TYPE_SMALL =
            R.layout.coui_component_card_entrance_preference_type_small;
    private static final int LAYOUT_RES_TYPE_LARGE =
            R.layout.coui_component_card_entrance_preference_type_large;

    private int cardType = CARD_TYPE_SMALL;
    private boolean showSummary = true;
    private boolean statusOn;
    private TextView summaryView;
    private int tintIcon;

    public COUICardEntrancePreference(Context context) {
        this(context, null);
    }

    public COUICardEntrancePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiJumpPreferenceStyle);
    }

    public COUICardEntrancePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUICardEntrancePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.COUICardEntrancePreference, defStyleAttr, defStyleRes);
        setCardType(a.getInteger(
                R.styleable.COUICardEntrancePreference_entranceCardType, CARD_TYPE_SMALL));
        setShowSummary(a.getBoolean(
                R.styleable.COUICardEntrancePreference_showSummary, true));
        setTintIcon(a.getInteger(
                R.styleable.COUICardEntrancePreference_tintIcon, TINT_ICON_NONE));
        a.recycle();
    }

    public int getCardType() {
        return cardType;
    }

    public boolean getShowSummary() {
        return showSummary;
    }

    public boolean getStatusOn() {
        return statusOn;
    }

    public int getTintIcon() {
        return tintIcon;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        COUIDarkModeUtil.setForceDarkAllow(holder.itemView, false);
        initSummaryView(holder);
        applyIconToTintType(holder);
    }

    public void initSummaryView(PreferenceViewHolder holder) {
        summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            COUIDarkModeUtil.setForceDarkAllow(summaryView, false);
        }
        setSummaryStatus(statusOn);
    }

    private void applyIconToTintType(PreferenceViewHolder holder) {
        if (tintIcon == TINT_ICON_ANYWAY || tintIcon == TINT_ICON_BY_GLOBAL_THEME) {
            Object icon = holder.findViewById(android.R.id.icon);
            COUIThemeOverlay.getInstance().applyCOUITintIcon(
                    getContext(), icon instanceof ImageView ? (ImageView) icon : null,
                    tintIcon == TINT_ICON_ANYWAY);
        }
    }

    private int getLayoutResByCardType(int cardType) {
        return cardType == CARD_TYPE_LARGE ? LAYOUT_RES_TYPE_LARGE : LAYOUT_RES_TYPE_SMALL;
    }

    public void setCardType(int cardType) {
        setLayoutResource(getLayoutResByCardType(cardType));
        this.cardType = cardType;
        notifyChanged();
    }

    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
        notifyChanged();
    }

    public void setStatusOn(boolean statusOn) {
        this.statusOn = statusOn;
        notifyChanged();
    }

    @Override
    public void setSummary(CharSequence summary) {
        if (showSummary) {
            super.setSummary(summary);
        } else {
            setStatusText1(summary);
        }
    }

    @Override
    public void setSummary(int summaryResId) {
        setSummary(getContext().getString(summaryResId));
    }

    public void setSummary(CharSequence summary, boolean showAsSummary) {
        if (summary == null) {
            throw new NullPointerException("summary");
        }
        if (showAsSummary) {
            super.setSummary(summary);
        } else {
            setSummary(summary);
        }
    }

    @SuppressLint("PrivateResource")
    public void setSummaryStatus(boolean statusOn) {
        int offColor = COUIContextUtil.getAttrColor(
                getContext(), R.attr.couiColorSecondNeutral, 0);
        int onColor = COUIContextUtil.getAttrColor(
                getContext(), R.attr.couiColorPrimaryText, 0);
        if (summaryView != null) {
            summaryView.setTextColor(statusOn ? onColor : offColor);
        }
    }

    public void setTintIcon(int tintIcon) {
        this.tintIcon = tintIcon;
        notifyChanged();
    }

    public static final class Companion {
        private Companion() {
        }
    }
}
