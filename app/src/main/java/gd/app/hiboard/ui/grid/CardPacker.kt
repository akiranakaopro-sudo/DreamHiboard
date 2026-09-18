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

fun targetIndexAt(placements: List<GridPlacement>, column: Float, row: Float): Int {
    if (placements.isEmpty()) return 0
    val hit = placements.indexOfFirst { place ->
        column >= place.column &&
            column < place.column + place.columns &&
            row >= place.row &&
            row < place.row + place.rows
    }
    if (hit >= 0) return hit
    return placements.indices.minBy { index ->
        val place = placements[index]
        val dx = column - (place.column + place.columns / 2f)
        val dy = row - (place.row + place.rows / 2f)
        dx * dx + dy * dy
    }
}

fun packCards(cards: List<CardInstance>, columns: Int = 4): List<GridPlacement> {
    val heights = IntArray(columns)
    return cards.map { card ->
        val width = card.size.columns.coerceIn(1, columns)
        val height = card.size.rows.coerceAtLeast(1)
        var bestX = 0
        var bestY = Int.MAX_VALUE
        for (x in 0..(columns - width)) {
            var y = 0
            for (col in x until x + width) {
                y = max(y, heights[col])
            }
            if (y < bestY) {
                bestY = y
                bestX = x
            }
        }
        for (col in bestX until bestX + width) {
            heights[col] = bestY + height
        }
        GridPlacement(card.instanceId, bestX, bestY, width, height)
    }
}
