package com.coui.appcompat.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.preference.PreferenceGroupAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;

public final class CardButtonDecoration extends BaseCardItemDecoration {
    private PreferenceGroupAdapter adapter;
    private CardMargin cardMargin;
    private final CardPositionPredicate cardPositionPredicate;

    public CardButtonDecoration(Context appContext, CardPositionPredicate cardPositionPredicate) {
        this(appContext, cardPositionPredicate, null);
    }

    public CardButtonDecoration(Context appContext, CardPositionPredicate cardPositionPredicate,
            PreferenceGroupAdapter adapter) {
        super(appContext);
        if (cardPositionPredicate == null) {
            throw new NullPointerException("cardPositionPredicate");
        }
        this.cardPositionPredicate = cardPositionPredicate;
        this.adapter = adapter;
        this.cardMargin = getDefaultCardMargin();
    }

    private CardMargin getDefaultCardMargin() {
        int top = getDimenPx(R.dimen.coui_component_card_button_first_top_margin);
        int horizontal = getDimenPx(R.dimen.coui_component_card_button_horizontal_margin_inner);
        return new CardMargin(0, top, horizontal, horizontal);
    }

    public PreferenceGroupAdapter getAdapter() {
        return adapter;
    }

    public CardMargin getCardMargin() {
        return cardMargin;
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (adapter != null && adapter.getItem(position) instanceof COUICardButtonPreference) {
            setCardColumnMargin(outRect, cardMargin, new CardPosition(cardPositionPredicate, position));
        }
    }

    public void setAdapter(PreferenceGroupAdapter adapter) {
        this.adapter = adapter;
    }

    public void setCardMargin(CardMargin cardMargin) {
        if (cardMargin == null) {
            throw new NullPointerException("<set-?>");
        }
        this.cardMargin = cardMargin;
    }
}
