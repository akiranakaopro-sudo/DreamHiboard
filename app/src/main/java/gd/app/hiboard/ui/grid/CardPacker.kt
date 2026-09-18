package gd.app.hiboard.ui.grid

import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.GridPlacement
import kotlin.math.max

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
