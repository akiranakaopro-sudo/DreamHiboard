package com.coui.appcompat.card;

public final class CardPosition {
    private final boolean isFirstRow;
    private final boolean isLastRow;
    private final boolean isFirstColumn;
    private final boolean isLastColumn;

    public CardPosition(CardPositionPredicate cardPositionPredicate, int position) {
        if (cardPositionPredicate == null) {
            throw new NullPointerException("cardPositionPredicate");
        }
        isFirstRow = cardPositionPredicate.isFirstRow().invoke(position);
        isLastRow = cardPositionPredicate.isLastRow().invoke(position);
        isFirstColumn = cardPositionPredicate.isFirstColumn().invoke(position);
        isLastColumn = cardPositionPredicate.isLastColumn().invoke(position);
    }

    public final boolean isFirstColumn() {
        return isFirstColumn;
    }

    public final boolean isFirstRow() {
        return isFirstRow;
    }

    public final boolean isLastColumn() {
        return isLastColumn;
    }

    public final boolean isLastRow() {
        return isLastRow;
    }
}
