package com.coui.appcompat.card;

public final class CardPositionPredicate {
    public interface Predicate {
        boolean invoke(int position);
    }

    private final Predicate isFirstRow;
    private final Predicate isLastRow;
    private final Predicate isFirstColumn;
    private final Predicate isLastColumn;

    public CardPositionPredicate(Predicate isFirstRow, Predicate isLastRow,
            Predicate isFirstColumn, Predicate isLastColumn) {
        if (isFirstRow == null || isLastRow == null || isFirstColumn == null || isLastColumn == null) {
            throw new NullPointerException("CardPositionPredicate predicates cannot be null");
        }
        this.isFirstRow = isFirstRow;
        this.isLastRow = isLastRow;
        this.isFirstColumn = isFirstColumn;
        this.isLastColumn = isLastColumn;
    }

    public final Predicate isFirstColumn() {
        return isFirstColumn;
    }

    public final Predicate isFirstRow() {
        return isFirstRow;
    }

    public final Predicate isLastColumn() {
        return isLastColumn;
    }

    public final Predicate isLastRow() {
        return isLastRow;
    }
}
