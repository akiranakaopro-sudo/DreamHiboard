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
    insetFraction: Float = 0.12f,
    draggedColumns: Int = 2,
): String? {
    if (draggedColumns >= 4) {
        return dropTargetIdForFullWidth(placements, draggedId, row)
    }
    val halfHits = mutableListOf<GridPlacement>()
    val wideHits = mutableListOf<GridPlacement>()
    placements.forEach { place ->
        if (place.instanceId == draggedId || place.locked) return@forEach
        if (place.columns >= 4) {
            if (inWidePlacement(place, column, row)) wideHits += place
        } else if (inPlacement(place, column, row, insetFraction)) {
            halfHits += place
        }
    }
    val candidates = if (halfHits.isNotEmpty()) halfHits else wideHits
    var bestId: String? = null
    var bestDist = Float.MAX_VALUE
    candidates.forEach { place ->
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
    val aboveBand = 0.5f
    return column >= place.column &&
        column < place.column + place.columns &&
        row >= place.row - aboveBand &&
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
        if (place.instanceId == draggedId || place.locked) return@forEachIndexed
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
    if (origin.firstOrNull { it.instanceId == draggedId }?.canDrag == false) {
        return pinLockedCards(current)
    }
    val originPlace = packCards(origin, columns).firstOrNull { it.instanceId == draggedId }
        ?: return pinLockedCards(current)
    if (inPlacement(originPlace, column, row)) return pinLockedCards(origin)
    previewSameRowPair(current, draggedId, column, row, columns)?.let { return pinLockedCards(it) }
    val draggedColumns = origin.firstOrNull { it.instanceId == draggedId }?.size?.columns ?: 2
    val packedCurrent = packCards(current, columns)
    val hitId = dropTargetId(
        packedCurrent,
        draggedId,
        column,
        row,
        draggedColumns = draggedColumns,
    ) ?: return pinLockedCards(current)
    val targetId = adjacentRowTargetId(packedCurrent, draggedId, hitId)
    val from = current.indexOfFirst { it.instanceId == draggedId }
    val to = current.indexOfFirst { it.instanceId == targetId }
    if (from < 0 || to < 0 || from == to) return pinLockedCards(current)
    if (shouldRestoreBornRow(origin, current, draggedId, originPlace, row, columns)) {
        return pinLockedCards(origin)
    }
    if (alreadyCrossedHit(origin, draggedId, targetId, from, to, packedCurrent, row)) {
        return pinLockedCards(current)
    }
    val partnerId = packedRowPartnerId(origin, draggedId, columns)
    if (partnerId != null && targetId != partnerId) {
        val hitSpan = current.firstOrNull { it.instanceId == targetId }?.size?.columns ?: 0
        if (hitSpan >= 4) {
            return pinLockedCards(
                moveWithRowPartner(
                    current,
                    draggedId,
                    targetId,
                    partnerId,
                    partnerIsOnRight(origin, draggedId, partnerId, columns),
                ),
            )
        }
    }
    return pinLockedCards(
        moveLikeOppo(
            current,
            from,
            to,
            current[from].size.columns,
            current[to].size.columns,
        ),
    )
}

/**
 * Side-by-side 2x2 tiles swap when the dragged center crosses their shared
 * edge, with a small hysteresis so they do not flicker. Staying in that row
 * also keeps For you / the dock from stealing the drop.
 */
fun previewSameRowPair(
    current: List<CardInstance>,
    draggedId: String,
    column: Float,
    row: Float,
    columns: Int = 4,
): List<CardInstance>? {
    val packed = packCards(current, columns)
    val dragged = packed.firstOrNull { it.instanceId == draggedId } ?: return null
    if (dragged.columns >= columns) return null
    val neighbor = packed.firstOrNull { other ->
        other.instanceId != draggedId &&
            other.row == dragged.row &&
            other.columns < columns
    } ?: return null
    if (row < dragged.row || row >= dragged.row + dragged.rows) return null
    val left = if (dragged.column <= neighbor.column) dragged else neighbor
    val right = if (dragged.column <= neighbor.column) neighbor else dragged
    val seam = right.column.toFloat()
    val hysteresis = 0.16f
    val from = current.indexOfFirst { it.instanceId == draggedId }
    val to = current.indexOfFirst { it.instanceId == neighbor.instanceId }
    if (from < 0 || to < 0) return current
    val draggingLeft = dragged.instanceId == left.instanceId
    val shouldSwap = if (draggingLeft) {
        column > seam + hysteresis
    } else {
        column < seam - hysteresis
    }
    if (!shouldSwap) return current
    return moveLikeOppo(
        current,
        from,
        to,
        current[from].size.columns,
        current[to].size.columns,
    )
}

/**
 * Hovering a 4-span we already passed must not walk back over it while
 * the pointer is still on the far half. For you stays put so it does
 * not bounce; a 2-row dock can reverse from its top half.
 */
fun alreadyCrossedHit(
    origin: List<CardInstance>,
    draggedId: String,
    hitId: String,
    from: Int,
    to: Int,
    packedCurrent: List<GridPlacement> = emptyList(),
    row: Float = 0f,
): Boolean {
    val originFrom = origin.indexOfFirst { it.instanceId == draggedId }
    val originHit = origin.indexOfFirst { it.instanceId == hitId }
    if (originFrom < 0 || originHit < 0) return false
    val hit = packedCurrent.firstOrNull { it.instanceId == hitId }
    val hitCenter = hit?.let { it.row + it.rows / 2f }
    if (originFrom < originHit && from > to) {
        if (hit == null || hitCenter == null) return true
        val dead = (hit.rows * 0.12f).coerceAtLeast(0.15f)
        if (row >= hitCenter - dead) return true
        if (hit.columns >= 4 && hit.rows >= 4) return true
        return false
    }
    if (originFrom > originHit && from < to) {
        if (hit == null || hitCenter == null) return true
        val dead = (hit.rows * 0.12f).coerceAtLeast(0.15f)
        if (row < hitCenter + dead) return true
        if (hit.columns >= 4 && hit.rows >= 4) return true
        return false
    }
    return false
}

/**
 * A drop two rows away must walk through the next row first. Jumping
 * over that row is what snapped a widget to the top of the second card
 * when the drag reversed.
 */
fun adjacentRowTargetId(
    packed: List<GridPlacement>,
    draggedId: String,
    hitId: String,
): String {
    val dragged = packed.firstOrNull { it.instanceId == draggedId } ?: return hitId
    val hit = packed.firstOrNull { it.instanceId == hitId } ?: return hitId
    val draggedTop = dragged.row
    val draggedBottom = dragged.row + dragged.rows
    val hitTop = hit.row
    val hitBottom = hit.row + hit.rows
    if (hitTop < draggedBottom && hitBottom > draggedTop) return hitId
    if (hitTop == draggedBottom || hitBottom == draggedTop) return hitId
    val others = packed.filter { it.instanceId != draggedId }
    if (hitTop >= draggedBottom) {
        val nextRow = others.filter { it.row >= draggedBottom }.minOfOrNull { it.row } ?: return hitId
        val inRow = others.filter { it.row == nextRow }
        return inRow.maxBy { it.column }.instanceId
    }
    if (hitBottom <= draggedTop) {
        val prevBottom = others.filter { it.row + it.rows <= draggedTop }
            .maxOfOrNull { it.row + it.rows } ?: return hitId
        val inRow = others.filter { it.row + it.rows == prevBottom }
        return inRow.minBy { it.column }.instanceId
    }
    return hitId
}

fun inOriginRow(place: GridPlacement, row: Float, insetFraction: Float = 0.22f): Boolean {
    val insetY = place.rows * insetFraction
    return row >= place.row + insetY &&
        row < place.row + place.rows - insetY
}

fun shouldRestoreBornRow(
    origin: List<CardInstance>,
    current: List<CardInstance>,
    draggedId: String,
    originPlace: GridPlacement,
    row: Float,
    columns: Int = 4,
): Boolean {
    if (current.map { it.instanceId } == origin.map { it.instanceId }) return false
    if (!inOriginRow(originPlace, row)) return false
    val fullWidthOnBornRow = packCards(current, columns).any { place ->
        place.columns >= 4 &&
            originPlace.row < place.row + place.rows &&
            originPlace.row + originPlace.rows > place.row
    }
    if (!fullWidthOnBornRow && packedRowPartnerId(origin, draggedId, columns) != null) {
        return false
    }
    return true
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
    return pinLockedCards(moveLikeOppo(cards, from, to, cards[from].size.columns, cards[to].size.columns))
}

/** Locked tiles (Recent apps) always occupy the first rows. */
fun pinLockedCards(cards: List<CardInstance>): List<CardInstance> {
    val locked = cards.filter { !it.canDrag }
    if (locked.isEmpty()) return cards
    return locked + cards.filter { it.canDrag }
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
        val placed = GridPlacement(card.instanceId, column, row, width, height, locked = !card.canDrag)
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

fun emptyAddSlots(placements: List<GridPlacement>, columns: Int = 4): List<GridPlacement> {
    if (placements.isEmpty()) return emptyList()
    val slots = mutableListOf<GridPlacement>()
    placements.groupBy { it.row }.forEach { (row, inRow) ->
        val occupied = BooleanArray(columns)
        var rowHeight = 0
        inRow.forEach { place ->
            rowHeight = max(rowHeight, place.rows)
            val end = (place.column + place.columns).coerceAtMost(columns)
            for (col in place.column until end) occupied[col] = true
        }
        if (rowHeight < 2) return@forEach
        var col = 0
        while (col <= columns - 2) {
            if (!occupied[col] && !occupied[col + 1]) {
                slots += GridPlacement("add-slot-$row-$col", col, row, 2, 2)
                col += 2
            } else {
                col += 1
            }
        }
    }
    return slots
}
