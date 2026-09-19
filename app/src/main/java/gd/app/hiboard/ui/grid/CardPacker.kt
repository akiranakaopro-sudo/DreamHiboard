package gd.app.hiboard.ui.grid

import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.GridPlacement
import kotlin.math.max

fun <T> moveItem(items: List<T>, from: Int, to: Int): List<T> {
    if (from == to || from !in items.indices) return items
    val dest = to.coerceIn(0, items.lastIndex)
    val mutable = items.toMutableList()
    val item = mutable.removeAt(from)
    mutable.add(dest, item)
    return mutable
}

fun occupiedIndexAt(placements: List<GridPlacement>, column: Float, row: Float): Int {
    return placements.indexOfFirst { place ->
        column >= place.column &&
            column < place.column + place.columns &&
            row >= place.row &&
            row < place.row + place.rows
    }
}

/**
 * ColorOS inner-drag `onMove`: adjacent bubble swaps.
 * If the dragged span is smaller than the target, the target walks instead.
 */
fun <T> moveLikeOppo(
    items: List<T>,
    from: Int,
    to: Int,
    fromSpan: Int,
    toSpan: Int,
): List<T> {
    if (from == to || from !in items.indices || to !in items.indices) return items
    val mutable = items.toMutableList()
    if (fromSpan >= toSpan) {
        if (from < to) {
            for (index in from until to) {
                val held = mutable[index]
                mutable[index] = mutable[index + 1]
                mutable[index + 1] = held
            }
        } else {
            for (index in from downTo to + 1) {
                val held = mutable[index]
                mutable[index] = mutable[index - 1]
                mutable[index - 1] = held
            }
        }
    } else if (from < to) {
        for (index in to downTo from + 1) {
            val held = mutable[index]
            mutable[index] = mutable[index - 1]
            mutable[index - 1] = held
        }
    } else {
        for (index in to until from) {
            val held = mutable[index]
            mutable[index] = mutable[index + 1]
            mutable[index + 1] = held
        }
    }
    return mutable
}

fun moveCardsLikeOppo(cards: List<CardInstance>, from: Int, to: Int): List<CardInstance> {
    if (from !in cards.indices || to !in cards.indices) return cards
    return moveLikeOppo(cards, from, to, cards[from].size.columns, cards[to].size.columns)
}

/** Sequential 4-column wrap, same as ColorOS `CardGridLayoutManager`. */
fun packCards(cards: List<CardInstance>, columns: Int = 4): List<GridPlacement> {
    var column = 0
    var row = 0
    var rowHeight = 0
    return cards.map { card ->
        val width = card.size.columns.coerceIn(1, columns)
        val height = card.size.rows.coerceAtLeast(1)
        if (column + width > columns) {
            column = 0
            row += rowHeight
            rowHeight = 0
        }
        val placed = GridPlacement(card.instanceId, column, row, width, height)
        column += width
        rowHeight = max(rowHeight, height)
        if (column >= columns) {
            column = 0
            row += rowHeight
            rowHeight = 0
        }
        placed
    }
}
