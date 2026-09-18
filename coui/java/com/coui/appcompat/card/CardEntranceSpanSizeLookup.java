package com.coui.appcompat.card;

import android.annotation.SuppressLint;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;
import androidx.recyclerview.widget.GridLayoutManager;

public final class CardEntranceSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    public static final Companion Companion = new Companion();
    public static final int SPAN_COUNT_CARD_TYPE_LARGE = 2;
    public static final int SPAN_COUNT_CARD_TYPE_SMALL = 1;

    private PreferenceGroupAdapter adapter;

    public CardEntranceSpanSizeLookup() {
        this(null);
    }

    public CardEntranceSpanSizeLookup(PreferenceGroupAdapter adapter) {
        this.adapter = adapter;
    }

    public PreferenceGroupAdapter getAdapter() {
        return adapter;
    }

    @Override
    @SuppressLint("RestrictedApi")
    public int getSpanSize(int position) {
        Preference preference = adapter != null ? adapter.getItem(position) : null;
        if (!(preference instanceof COUICardEntrancePreference)) {
            return SPAN_COUNT_CARD_TYPE_LARGE;
        }
        int cardType = ((COUICardEntrancePreference) preference).getCardType();
        return cardType == COUICardEntrancePreference.CARD_TYPE_LARGE
                ? SPAN_COUNT_CARD_TYPE_LARGE : SPAN_COUNT_CARD_TYPE_SMALL;
    }

    public void setAdapter(PreferenceGroupAdapter adapter) {
        this.adapter = adapter;
    }

    public static final class Companion {
        private Companion() {
        }
    }
}
