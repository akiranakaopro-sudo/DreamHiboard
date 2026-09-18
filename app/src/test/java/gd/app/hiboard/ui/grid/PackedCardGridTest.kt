package gd.app.hiboard.ui.grid

import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.CardSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackedCardGridTest {
    @Test
    fun packsTwoByTwoBesideEachOther() {
        val a = card("a", CardSize.TwoByTwo)
        val b = card("b", CardSize.TwoByTwo)
        val packed = packCards(listOf(a, b))
        assertEquals(0, packed[0].column)
        assertEquals(2, packed[1].column)
        assertEquals(0, packed[1].row)
    }

    @Test
    fun fullWidthAdviceTakesWholeRow() {
        val advice = card("advice", CardSize.FullByTwo)
        val tile = card("weather", CardSize.TwoByTwo)
        val packed = packCards(listOf(advice, tile))
        assertEquals(4, packed[0].columns)
        assertEquals(0, packed[1].column)
        assertEquals(2, packed[1].row)
        assertTrue(packed[1].row >= packed[0].rows)
    }

    @Test
    fun moveItemReordersWithoutLosingCards() {
        val moved = moveItem(listOf("a", "b", "c"), 2, 0)
        assertEquals(listOf("c", "a", "b"), moved)
    }

    @Test
    fun targetIndexHitsOccupiedCell() {
        val placements = packCards(
            listOf(
                card("left", CardSize.TwoByTwo),
                card("right", CardSize.TwoByTwo),
            ),
        )
        assertEquals(0, targetIndexAt(placements, 0.5f, 0.5f))
        assertEquals(1, targetIndexAt(placements, 2.5f, 0.5f))
    }

    @Test
    fun reorderingTwoByTwoSwapsPackedColumns() {
        val a = card("a", CardSize.TwoByTwo)
        val b = card("b", CardSize.TwoByTwo)
        val packed = packCards(moveItem(listOf(a, b), 0, 1))
        assertEquals("b", packed[0].instanceId)
        assertEquals(0, packed[0].column)
        assertEquals("a", packed[1].instanceId)
        assertEquals(2, packed[1].column)
    }

    private fun card(id: String, size: CardSize) = CardInstance(
        instanceId = id,
        catalogId = id,
        displayName = id,
        size = size,
        area = CardArea.Subscribe,
        engine = CardEngineId.Weather,
    )
}
