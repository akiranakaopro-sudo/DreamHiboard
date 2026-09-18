package com.coui.appcompat.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;

public final class CardEntranceDecoration extends BaseCardItemDecoration {
    private PreferenceGroupAdapter adapter;
    private final CardPositionPredicate cardPositionPredicate;
    private CardMargin largeCardMargin;
    private CardMargin smallCardMargin;

    public CardEntranceDecoration(Context appContext, CardPositionPredicate cardPositionPredicate) {
        this(appContext, cardPositionPredicate, null);
    }

    public CardEntranceDecoration(Context appContext, CardPositionPredicate cardPositionPredicate,
            PreferenceGroupAdapter adapter) {
        super(appContext);
        if (cardPositionPredicate == null) {
            throw new NullPointerException("cardPositionPredicate");
        }
        this.cardPositionPredicate = cardPositionPredicate;
        this.adapter = adapter;
        largeCardMargin = getDefaultLargeCardMargin();
        smallCardMargin = getDefaultSmallCardMargin();
    }

    private CardMargin getDefaultLargeCardMargin() {
        int top = getDimenPx(R.dimen.coui_component_card_entrance_large_top_margin);
        int horizontal = getDimenPx(R.dimen.coui_component_card_entrance_large_horizontal_margin);
        return new CardMargin(top, top, horizontal, horizontal);
    }

    private CardMargin getDefaultSmallCardMargin() {
        return new CardMargin(
                getDimenPx(R.dimen.coui_component_card_entrance_small_top_margin_first),
                getDimenPx(R.dimen.coui_component_card_entrance_small_top_margin_other),
                getDimenPx(R.dimen.coui_component_card_entrance_small_horizontal_margin_outer),
                getDimenPx(R.dimen.coui_component_card_entrance_small_horizontal_margin_inner));
    }

    public PreferenceGroupAdapter getAdapter() {
        return adapter;
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        Preference preference = adapter != null ? adapter.getItem(position) : null;
        if (preference instanceof COUICardEntrancePreference) {
            int cardType = ((COUICardEntrancePreference) preference).getCardType();
            if (cardType == COUICardEntrancePreference.CARD_TYPE_SMALL) {
                setCardColumnMargin(outRect, smallCardMargin, new CardPosition(cardPositionPredicate, position));
            } else if (cardType == COUICardEntrancePreference.CARD_TYPE_LARGE) {
                setCardColumnMargin(outRect, largeCardMargin, new CardPosition(cardPositionPredicate, position));
            }
        }
    }

    public CardMargin getLargeCardMargin() {
        return largeCardMargin;
    }

    public CardMargin getSmallCardMargin() {
        return smallCardMargin;
    }

    public void setAdapter(PreferenceGroupAdapter adapter) {
        this.adapter = adapter;
    }

    public void setLargeCardMargin(CardMargin largeCardMargin) {
        if (largeCardMargin == null) {
            throw new NullPointerException("<set-?>");
        }
        this.largeCardMargin = largeCardMargin;
    }

    public void setSmallCardMargin(CardMargin smallCardMargin) {
        if (smallCardMargin == null) {
            throw new NullPointerException("<set-?>");
        }
        this.smallCardMargin = smallCardMargin;
    }
}
