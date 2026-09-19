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
    fun fullWidthBetweenHalfWidthLeavesHoleLikeOppo() {
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val full = card("dock", CardSize.FullByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val packed = packCards(listOf(shortcuts, full, weather))
        assertEquals(0, packed[0].column)
        assertEquals(0, packed[0].row)
        assertEquals(0, packed[1].column)
        assertEquals(2, packed[1].row)
        assertEquals(0, packed[2].column)
        assertEquals(4, packed[2].row)
    }

    @Test
    fun consecutiveHalfWidthCardsShareARow() {
        val weather = card("weather", CardSize.TwoByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val packed = packCards(listOf(weather, notes, advice))
        assertEquals(0, packed[0].column)
        assertEquals(2, packed[1].column)
        assertEquals(0, packed[1].row)
        assertEquals(2, packed[2].row)
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
    fun occupiedIndexIgnoresEmptySpace() {
        val placements = packCards(
            listOf(
                card("left", CardSize.TwoByTwo),
                card("right", CardSize.TwoByTwo),
            ),
        )
        assertEquals(0, occupiedIndexAt(placements, 0.5f, 0.5f))
        assertEquals(-1, occupiedIndexAt(placements, 1f, 3f))
        assertEquals(-1, occupiedIndexAt(placements, -0.2f, 0.5f))
    }

    @Test
    fun sameSpanTilesBubbleSwapLikeItemTouchHelper() {
        val weather = card("weather", CardSize.TwoByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val moved = moveCardsLikeOppo(listOf(weather, notes), 0, 1)
        assertEquals(listOf("notes", "weather"), moved.map { it.catalogId })
        val packed = packCards(moved)
        assertEquals("notes", packed[0].instanceId)
        assertEquals(0, packed[0].column)
        assertEquals("weather", packed[1].instanceId)
        assertEquals(2, packed[1].column)
    }

    @Test
    fun smallerSpanOntoLargerMovesTheLargerCard() {
        val weather = card("weather", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val moved = moveCardsLikeOppo(listOf(weather, advice), 0, 1)
        assertEquals(listOf("advice", "weather"), moved.map { it.catalogId })
        val packed = packCards(moved)
        assertEquals(0, packed[0].column)
        assertEquals(0, packed[0].row)
        assertEquals(0, packed[1].column)
        assertEquals(2, packed[1].row)
    }

    @Test
    fun equalFullWidthCardsBubbleTowardTarget() {
        val shortcuts = card("shortcuts", CardSize.FullByTwo)
        val infoflow = card("infoflow", CardSize.FourByFour)
        val advice = card("advice", CardSize.FullByTwo)
        val moved = moveCardsLikeOppo(listOf(shortcuts, infoflow, advice), 2, 1)
        assertEquals(listOf("shortcuts", "advice", "infoflow"), moved.map { it.catalogId })
        val packed = packCards(moved)
        assertEquals(2, packed[1].row)
        assertEquals(4, packed[2].row)
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
