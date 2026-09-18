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
    fun halfWidthCardsFillEarlierHoleInsteadOfSecondEmpty() {
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val full = card("dock", CardSize.FullByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val packed = packCards(listOf(shortcuts, full, weather))
        assertEquals(0, packed[0].column)
        assertEquals(0, packed[0].row)
        assertEquals(0, packed[1].column)
        assertEquals(2, packed[1].row)
        assertEquals("weather", packed[2].instanceId)
        assertEquals(2, packed[2].column)
        assertEquals(0, packed[2].row)
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
    fun sameSizeTilesSwapColumnsInsteadOfInserting() {
        val weather = card("weather", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val originList = listOf(weather, advice, shortcuts)
        val origin = packCards(originList)
        assertEquals(0, origin[0].column)
        assertEquals(2, origin[2].column)
        assertEquals(0, origin[2].row)
        val drop = dropIndexFromOrigin(origin, 0, 3f, 1f)
        assertEquals(2, drop)
        val swapped = reorderCards(originList, 0, drop)
        assertEquals(listOf("shortcuts", "advice", "weather"), swapped.map { it.catalogId })
        val packed = packCards(swapped)
        assertEquals("shortcuts", packed[0].instanceId)
        assertEquals(0, packed[0].column)
        assertEquals("weather", packed[2].instanceId)
        assertEquals(2, packed[2].column)
        assertEquals(0, packed[2].row)
    }

    @Test
    fun shortCardOverTallCardKeepsOriginTarget() {
        val infoflow = card("infoflow", CardSize.FourByFour)
        val advice = card("advice", CardSize.FullByTwo)
        val origin = packCards(listOf(infoflow, advice))
        assertEquals(0, dropIndexFromOrigin(origin, 1, 2f, 2f))
        val swapped = packCards(moveItem(listOf(infoflow, advice), 1, 0))
        assertTrue(occupiedIndexAt(origin, 2f, 2f) != occupiedIndexAt(swapped, 2f, 2f))
        assertEquals(0, dropIndexFromOrigin(origin, 1, 2f, 3f))
        assertEquals(0, dropIndexFromOrigin(origin, 1, 2f, 1f))
    }

    @Test
    fun previewInsertsAdviceAboveInfoFlowAndOriginHitStays() {
        val shortcuts = card("shortcuts", CardSize.FullByTwo)
        val infoflow = card("infoflow", CardSize.FourByFour)
        val advice = card("advice", CardSize.FullByTwo)
        val originList = listOf(shortcuts, infoflow, advice)
        val origin = packCards(originList)
        val drop = dropIndexFromOrigin(origin, 2, 2f, 3f)
        assertEquals(1, drop)
        val preview = moveItem(originList, 2, drop)
        assertEquals(listOf("shortcuts", "advice", "infoflow"), preview.map { it.catalogId })
        val packed = packCards(preview)
        assertEquals("advice", packed[1].instanceId)
        assertEquals(2, packed[1].row)
        assertEquals("infoflow", packed[2].instanceId)
        assertEquals(4, packed[2].row)
        assertEquals(1, dropIndexFromOrigin(origin, 2, 2f, 2.5f))
        assertEquals(1, dropIndexFromOrigin(origin, 2, 2f, 5f))
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
