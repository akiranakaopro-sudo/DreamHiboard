package com.coui.appcompat.card;

import android.content.Context;
import android.graphics.Rect;

import androidx.recyclerview.widget.RecyclerView;

public class BaseCardItemDecoration extends RecyclerView.ItemDecoration {
    private final Context appContext;

    public BaseCardItemDecoration(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("appContext");
        }
        this.appContext = appContext;
    }

    public final int getDimenPx(int dimenRes) {
        return appContext.getResources().getDimensionPixelSize(dimenRes);
    }

    public final void setCardColumnMargin(Rect outRect, CardMargin cardMargin, CardPosition cardPosition) {
        if (outRect == null || cardMargin == null || cardPosition == null) {
            throw new NullPointerException("setCardColumnMargin arguments cannot be null");
        }
        outRect.top = cardMargin.getOtherRowTop();
        outRect.left = cardMargin.getFirstColumnInner();
        outRect.right = cardMargin.getFirstColumnInner();
        if (cardPosition.isFirstColumn()) {
            outRect.left = cardMargin.getFirstColumnOuter();
        }
        if (cardPosition.isLastColumn()) {
            outRect.right = cardMargin.getFirstColumnOuter();
        }
        if (cardPosition.isFirstRow()) {
            outRect.top = cardMargin.getFirstRowTop();
        }
    }
}
