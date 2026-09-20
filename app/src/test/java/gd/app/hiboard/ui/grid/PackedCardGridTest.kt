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
    fun dropTargetIgnoresSharedEdgeOfSideBySideTiles() {
        val notes = card("notes", CardSize.TwoByTwo)
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val packed = packCards(listOf(notes, shortcuts, weather))
        assertEquals(null, dropTargetId(packed, "weather", 2f, 1f))
        assertEquals("notes", dropTargetId(packed, "weather", 1f, 1f))
        assertEquals("shortcuts", dropTargetId(packed, "weather", 3f, 1f))
        assertEquals("notes", dropTargetId(packed, "weather", 0.9f, 1f))
        assertEquals("shortcuts", dropTargetId(packed, "weather", 3.1f, 1f))
        assertEquals(
            "notes",
            dropTargetId(packed, "weather", 2f, 1f, draggedColumns = 4),
        )
    }

    @Test
    fun fullWidthCardOverHalfWidthPairInsertsAboveTheRow() {
        val dock = card("dock", CardSize.FullByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val infoflow = card("infoflow", CardSize.FourByFour)
        val origin = listOf(dock, notes, shortcuts, advice, infoflow)
        val packed = packCards(origin)
        assertEquals(2, packed[1].row)
        assertEquals(2, packed[2].row)
        assertEquals(0, packed[1].column)
        assertEquals(2, packed[2].column)
        assertEquals("notes", dropTargetId(packed, "infoflow", 2f, 3f, draggedColumns = 4))
        val preview = previewCardsForDrop(origin, origin, "infoflow", 2f, 3f)
        assertEquals(
            listOf("dock", "infoflow", "notes", "shortcuts", "advice"),
            preview.map { it.catalogId },
        )
        val next = packCards(preview)
        assertEquals("infoflow", next[1].instanceId)
        assertEquals(2, next[1].row)
        assertEquals("notes", next[2].instanceId)
        assertEquals("shortcuts", next[3].instanceId)
        assertEquals(next[2].row, next[3].row)
        assertEquals(0, next[2].column)
        assertEquals(2, next[3].column)
    }

    @Test
    fun fullWidthCardBelowHalfWidthPairKeepsThePairAbove() {
        val shortcuts = card("shortcuts", CardSize.FullByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val origin = listOf(shortcuts, notes, weather)
        val packed = packCards(origin)
        assertEquals("weather", dropTargetId(packed, "shortcuts", 2f, 3f, draggedColumns = 4))
        val preview = previewCardsForDrop(origin, origin, "shortcuts", 2f, 3f)
        assertEquals(listOf("notes", "weather", "shortcuts"), preview.map { it.catalogId })
        val next = packCards(preview)
        assertEquals("notes", next[0].instanceId)
        assertEquals(0, next[0].column)
        assertEquals("weather", next[1].instanceId)
        assertEquals(2, next[1].column)
        assertEquals(next[0].row, next[1].row)
        assertEquals("shortcuts", next[2].instanceId)
        assertEquals(2, next[2].row)
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
    fun draggingBackToBornSlotPutsNeighborOnTheRight() {
        val weather = card("weather", CardSize.TwoByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val origin = listOf(weather, notes)
        val swapped = moveCardsLikeOppo(origin, 0, 1)
        assertEquals(listOf("notes", "weather"), swapped.map { it.catalogId })
        val packedSwap = packCards(swapped)
        assertEquals(0, packedSwap[0].column)
        assertEquals(2, packedSwap[1].column)

        val restored = previewCardsForDrop(origin, swapped, "weather", 1f, 1f)
        assertEquals(listOf("weather", "notes"), restored.map { it.catalogId })
        val packedBorn = packCards(restored)
        assertEquals("weather", packedBorn[0].instanceId)
        assertEquals(0, packedBorn[0].column)
        assertEquals("notes", packedBorn[1].instanceId)
        assertEquals(2, packedBorn[1].column)
    }

    @Test
    fun draggingOntoNeighborStillSwapsAwayFromBornSlot() {
        val weather = card("weather", CardSize.TwoByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val origin = listOf(weather, notes)
        val preview = previewCardsForDrop(origin, origin, "weather", 3f, 1f)
        assertEquals(listOf("notes", "weather"), preview.map { it.catalogId })
        val packed = packCards(preview)
        assertEquals("notes", packed[0].instanceId)
        assertEquals(0, packed[0].column)
        assertEquals("weather", packed[1].instanceId)
        assertEquals(2, packed[1].column)
        val overHole = previewCardsForDrop(origin, preview, "weather", 3f, 1f)
        assertEquals(listOf("notes", "weather"), overHole.map { it.catalogId })
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

    @Test
    fun halfWidthPartnerFollowsWhenMovingOntoFullWidth() {
        val dock = card("dock", CardSize.FullByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val origin = listOf(dock, advice, shortcuts, weather)
        assertEquals("weather", packedRowPartnerId(origin, "shortcuts"))
        val preview = previewCardsForDrop(origin, origin, "shortcuts", 2f, 3f)
        assertEquals(listOf("dock", "shortcuts", "weather", "advice"), preview.map { it.catalogId })
        val packed = packCards(preview)
        assertEquals("shortcuts", packed[1].instanceId)
        assertEquals(0, packed[1].column)
        assertEquals("weather", packed[2].instanceId)
        assertEquals(2, packed[2].column)
        assertEquals(packed[1].row, packed[2].row)
    }

    @Test
    fun halfWidthCardAboveFullWidthInsertsAboveIt() {
        val favorite = card("favorite", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val dock = card("dock", CardSize.FullByTwo)
        val origin = listOf(favorite, weather, advice, notes, dock)
        val packed = packCards(origin)
        assertEquals(0, packed[0].row)
        assertEquals(0, packed[1].row)
        assertEquals(2, packed[2].row)
        assertEquals(4, packed[3].row)
        assertEquals("advice", dropTargetId(packed, "notes", 0.5f, 3.9f))
        assertEquals("advice", dropTargetId(packed, "notes", 1f, 2.1f))
        val preview = previewCardsForDrop(origin, origin, "notes", 0.5f, 3.9f)
        assertEquals(
            listOf("favorite", "weather", "notes", "advice", "dock"),
            preview.map { it.catalogId },
        )
        val next = packCards(preview)
        assertEquals("notes", next[2].instanceId)
        assertEquals(2, next[2].row)
        assertEquals(0, next[2].column)
        assertEquals("advice", next[3].instanceId)
        assertEquals(4, next[3].row)
    }

    @Test
    fun halfWidthPartnerDoesNotFollowWhenSwappingOntoAnotherTile() {
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val origin = listOf(shortcuts, weather, notes)
        val preview = previewCardsForDrop(origin, origin, "shortcuts", 1f, 3f)
        assertEquals(listOf("weather", "notes", "shortcuts"), preview.map { it.catalogId })
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
