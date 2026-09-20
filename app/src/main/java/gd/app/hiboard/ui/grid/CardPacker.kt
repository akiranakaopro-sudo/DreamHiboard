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

fun inPlacement(
    place: GridPlacement,
    column: Float,
    row: Float,
    insetFraction: Float = 0.22f,
): Boolean {
    val insetX = place.columns * insetFraction
    val insetY = place.rows * insetFraction
    return column >= place.column + insetX &&
        column < place.column + place.columns - insetX &&
        row >= place.row + insetY &&
        row < place.row + place.rows - insetY
}

/**
 * Stable drop target for inner drag. Half-width tiles keep a seam dead zone.
 * A full-width card's center sits on that seam, so span-4 picks the leftmost
 * tile in the row instead — otherwise For you can never sit above Notes|Shortcuts.
 *
 * Half-width onto full-width uses the whole tile: the 22% inset was meant for
 * side-by-side 2-span seams, and it hid Advice from Notes dragged up the left.
 */
fun dropTargetId(
    placements: List<GridPlacement>,
    draggedId: String,
    column: Float,
    row: Float,
    insetFraction: Float = 0.22f,
    draggedColumns: Int = 2,
): String? {
    if (draggedColumns >= 4) {
        return dropTargetIdForFullWidth(placements, draggedId, row)
    }
    var bestId: String? = null
    var bestDist = Float.MAX_VALUE
    placements.forEach { place ->
        if (place.instanceId == draggedId) return@forEach
        val hit = if (place.columns >= 4) {
            inWidePlacement(place, column, row)
        } else {
            inPlacement(place, column, row, insetFraction)
        }
        if (!hit) return@forEach
        val dx = column - (place.column + place.columns / 2f)
        val dy = row - (place.row + place.rows / 2f)
        val dist = dx * dx + dy * dy
        if (dist < bestDist) {
            bestDist = dist
            bestId = place.instanceId
        }
    }
    return bestId
}

fun inWidePlacement(place: GridPlacement, column: Float, row: Float): Boolean {
    return column >= place.column &&
        column < place.column + place.columns &&
        row >= place.row &&
        row < place.row + place.rows
}

private fun dropTargetIdForFullWidth(
    placements: List<GridPlacement>,
    draggedId: String,
    row: Float,
): String? {
    val draggedIndex = placements.indexOfFirst { it.instanceId == draggedId }
    val hits = mutableListOf<Pair<Int, GridPlacement>>()
    placements.forEachIndexed { index, place ->
        if (place.instanceId == draggedId) return@forEachIndexed
        val insetY = place.rows * 0.12f
        if (row < place.row + insetY) return@forEachIndexed
        if (row >= place.row + place.rows - insetY) return@forEachIndexed
        hits += index to place
    }
    if (hits.isEmpty()) return null
    val targetRow = hits.minBy { (_, place) ->
        val center = place.row + place.rows / 2f
        val delta = row - center
        delta * delta
    }.second.row
    val inRow = hits.filter { it.second.row == targetRow }
    val firstInRow = inRow.minOf { it.first }
    val movingDown = draggedIndex in 0 until firstInRow
    val chosen = if (movingDown) {
        inRow.maxBy { it.second.column }
    } else {
        inRow.minBy { it.second.column }
    }
    return chosen.second.instanceId
}

/**
 * Preview order while dragging. Hovering the dragged card's original cell
 * restores the born layout so a neighbor (e.g. Notes) returns to its seat.
 */
fun previewCardsForDrop(
    origin: List<CardInstance>,
    current: List<CardInstance>,
    draggedId: String,
    column: Float,
    row: Float,
    columns: Int = 4,
): List<CardInstance> {
    val originPlace = packCards(origin, columns).firstOrNull { it.instanceId == draggedId }
        ?: return current
    if (inPlacement(originPlace, column, row)) return origin
    val draggedColumns = origin.firstOrNull { it.instanceId == draggedId }?.size?.columns ?: 2
    val hitId = dropTargetId(
        packCards(current, columns),
        draggedId,
        column,
        row,
        draggedColumns = draggedColumns,
    ) ?: return current
    val from = current.indexOfFirst { it.instanceId == draggedId }
    val to = current.indexOfFirst { it.instanceId == hitId }
    if (from < 0 || to < 0 || from == to) return current
    val partnerId = packedRowPartnerId(origin, draggedId, columns)
    if (partnerId != null && hitId != partnerId) {
        val hitSpan = current.firstOrNull { it.instanceId == hitId }?.size?.columns ?: 0
        if (hitSpan >= 4) {
            return moveWithRowPartner(
                current,
                draggedId,
                hitId,
                partnerId,
                partnerIsOnRight(origin, draggedId, partnerId, columns),
            )
        }
    }
    return moveLikeOppo(
        current,
        from,
        to,
        current[from].size.columns,
        current[to].size.columns,
    )
}

fun packedRowPartnerId(
    cards: List<CardInstance>,
    draggedId: String,
    columns: Int = 4,
): String? {
    val packed = packCards(cards, columns)
    val dragged = packed.firstOrNull { it.instanceId == draggedId } ?: return null
    if (dragged.columns >= columns) return null
    return packed.firstOrNull { other ->
        other.instanceId != draggedId &&
            other.row == dragged.row &&
            other.columns < columns
    }?.instanceId
}

fun partnerIsOnRight(
    cards: List<CardInstance>,
    draggedId: String,
    partnerId: String,
    columns: Int = 4,
): Boolean {
    val packed = packCards(cards, columns)
    val dragged = packed.firstOrNull { it.instanceId == draggedId } ?: return true
    val partner = packed.firstOrNull { it.instanceId == partnerId } ?: return true
    return partner.column > dragged.column
}

fun moveWithRowPartner(
    cards: List<CardInstance>,
    draggedId: String,
    targetId: String,
    partnerId: String,
    partnerOnRight: Boolean,
): List<CardInstance> {
    val withoutPartner = cards.filter { it.instanceId != partnerId }
    val from = withoutPartner.indexOfFirst { it.instanceId == draggedId }
    val to = withoutPartner.indexOfFirst { it.instanceId == targetId }
    if (from < 0 || to < 0) return cards
    val moved = moveCardsLikeOppo(withoutPartner, from, to)
    val dragIndex = moved.indexOfFirst { it.instanceId == draggedId }
    if (dragIndex < 0) return cards
    val partner = cards.firstOrNull { it.instanceId == partnerId } ?: return moved
    val next = moved.toMutableList()
    val insertAt = if (partnerOnRight) dragIndex + 1 else dragIndex
    next.add(insertAt.coerceIn(0, next.size), partner)
    return next
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
