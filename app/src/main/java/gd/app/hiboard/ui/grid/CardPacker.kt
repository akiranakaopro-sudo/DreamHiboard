package gd.app.hiboard.ui.grid

import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.GridPlacement

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

fun occupiedIndexAt(placements: List<GridPlacement>, column: Float, row: Float): Int {
    return placements.indexOfFirst { place ->
        column >= place.column &&
            column < place.column + place.columns &&
            row >= place.row &&
            row < place.row + place.rows
    }
}

fun dropIndexFromOrigin(
    originPlacements: List<GridPlacement>,
    originIndex: Int,
    column: Float,
    row: Float,
    extraColumn: Float = column,
    extraRow: Float = row,
): Int {
    val hits = listOf(
        occupiedIndexAt(originPlacements, extraColumn, extraRow),
        occupiedIndexAt(originPlacements, column, row),
    )
    return hits.firstOrNull { it >= 0 && it != originIndex }
        ?: hits.firstOrNull { it >= 0 }
        ?: originIndex
}

fun reorderCards(cards: List<CardInstance>, from: Int, to: Int): List<CardInstance> {
    if (from == to || from !in cards.indices || to !in cards.indices) return cards
    if (cards[from].size == cards[to].size) {
        val swapped = cards.toMutableList()
        swapped[from] = cards[to]
        swapped[to] = cards[from]
        return swapped
    }
    return moveItem(cards, from, to)
}

fun <T> reorderItems(
    items: List<T>,
    cards: List<CardInstance>,
    from: Int,
    to: Int,
): List<T> {
    if (from == to || from !in items.indices || to !in items.indices) return items
    if (cards.getOrNull(from)?.size == cards.getOrNull(to)?.size) {
        val swapped = items.toMutableList()
        val held = swapped[from]
        swapped[from] = swapped[to]
        swapped[to] = held
        return swapped
    }
    return moveItem(items, from, to)
}

fun packCards(cards: List<CardInstance>, columns: Int = 4): List<GridPlacement> {
    val occupied = ArrayList<BooleanArray>()

    fun ensureRow(y: Int): BooleanArray {
        while (occupied.size <= y) occupied.add(BooleanArray(columns))
        return occupied[y]
    }

    fun fits(x: Int, y: Int, width: Int, height: Int): Boolean {
        if (x < 0 || width < 1 || x + width > columns) return false
        for (row in y until y + height) {
            if (row >= occupied.size) continue
            val cells = occupied[row]
            for (col in x until x + width) {
                if (cells[col]) return false
            }
        }
        return true
    }

    fun occupy(x: Int, y: Int, width: Int, height: Int) {
        for (row in y until y + height) {
            val cells = ensureRow(row)
            for (col in x until x + width) cells[col] = true
        }
    }

    return cards.map { card ->
        val width = card.size.columns.coerceIn(1, columns)
        val height = card.size.rows.coerceAtLeast(1)
        var placeX = 0
        var placeY = 0
        var found = false
        for (y in 0..occupied.size) {
            for (x in 0..(columns - width)) {
                if (fits(x, y, width, height)) {
                    placeX = x
                    placeY = y
                    found = true
                    break
                }
            }
            if (found) break
        }
        occupy(placeX, placeY, width, height)
        GridPlacement(card.instanceId, placeX, placeY, width, height)
    }
}
