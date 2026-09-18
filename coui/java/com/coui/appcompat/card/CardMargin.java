package com.coui.appcompat.card;

public final class CardMargin {
    private final int firstRowTop;
    private final int otherRowTop;
    private final int firstColumnOuter;
    private final int firstColumnInner;

    public CardMargin(int firstRowTop, int otherRowTop, int firstColumnOuter, int firstColumnInner) {
        this.firstRowTop = firstRowTop;
        this.otherRowTop = otherRowTop;
        this.firstColumnOuter = firstColumnOuter;
        this.firstColumnInner = firstColumnInner;
    }

    public static CardMargin copy$default(CardMargin cardMargin, int firstRowTop, int otherRowTop,
            int firstColumnOuter, int firstColumnInner, int mask, Object marker) {
        if ((mask & 1) != 0) {
            firstRowTop = cardMargin.firstRowTop;
        }
        if ((mask & 2) != 0) {
            otherRowTop = cardMargin.otherRowTop;
        }
        if ((mask & 4) != 0) {
            firstColumnOuter = cardMargin.firstColumnOuter;
        }
        if ((mask & 8) != 0) {
            firstColumnInner = cardMargin.firstColumnInner;
        }
        return cardMargin.copy(firstRowTop, otherRowTop, firstColumnOuter, firstColumnInner);
    }

    public final int component1() {
        return firstRowTop;
    }

    public final int component2() {
        return otherRowTop;
    }

    public final int component3() {
        return firstColumnOuter;
    }

    public final int component4() {
        return firstColumnInner;
    }

    public final CardMargin copy(int firstRowTop, int otherRowTop, int firstColumnOuter, int firstColumnInner) {
        return new CardMargin(firstRowTop, otherRowTop, firstColumnOuter, firstColumnInner);
    }

    public final int getFirstColumnInner() {
        return firstColumnInner;
    }

    public final int getFirstColumnOuter() {
        return firstColumnOuter;
    }

    public final int getFirstRowTop() {
        return firstRowTop;
    }

    public final int getOtherRowTop() {
        return otherRowTop;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CardMargin)) {
            return false;
        }
        CardMargin that = (CardMargin) other;
        return firstRowTop == that.firstRowTop
                && otherRowTop == that.otherRowTop
                && firstColumnOuter == that.firstColumnOuter
                && firstColumnInner == that.firstColumnInner;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(firstRowTop);
        result = 31 * result + Integer.hashCode(otherRowTop);
        result = 31 * result + Integer.hashCode(firstColumnOuter);
        result = 31 * result + Integer.hashCode(firstColumnInner);
        return result;
    }

    @Override
    public String toString() {
        return "CardMargin(firstRowTop=" + firstRowTop
                + ", otherRowTop=" + otherRowTop
                + ", firstColumnOuter=" + firstColumnOuter
                + ", firstColumnInner=" + firstColumnInner + ')';
    }
}
