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
        assertTrue(emptyAddSlots(packed).isEmpty())
    }

    @Test
    fun trailingTwoByTwoGetsASquareAddSlotOnTheRight() {
        val notes = card("notes", CardSize.TwoByTwo)
        val slots = emptyAddSlots(packCards(listOf(notes)))
        assertEquals(1, slots.size)
        assertEquals(2, slots[0].column)
        assertEquals(0, slots[0].row)
        assertEquals(2, slots[0].columns)
        assertEquals(2, slots[0].rows)
    }

    @Test
    fun twoByTwoThenFullWidthGetsAnAddSlotBesideTheHalfCard() {
        val notes = card("notes", CardSize.TwoByTwo)
        val dock = card("dock", CardSize.FullByTwo)
        val slots = emptyAddSlots(packCards(listOf(notes, dock)))
        assertEquals(1, slots.size)
        assertEquals(2, slots[0].column)
        assertEquals(0, slots[0].row)
    }

    @Test
    fun emptyBoardHasNoAddSlot() {
        assertTrue(emptyAddSlots(emptyList()).isEmpty())
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
        val first = previewCardsForDrop(origin, origin, "infoflow", 2f, 3f)
        assertEquals(
            listOf("dock", "notes", "shortcuts", "infoflow", "advice"),
            first.map { it.catalogId },
        )
        val preview = previewCardsForDrop(origin, first, "infoflow", 2f, 3f)
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
    fun sideBySideTilesSwapWhenCenterCrossesTheSeam() {
        val shortcuts = card("favorite", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val origin = listOf(shortcuts, weather)
        val hold = previewCardsForDrop(origin, origin, "favorite", 2.05f, 1f)
        assertEquals(listOf("favorite", "weather"), hold.map { it.catalogId })
        val swapped = previewCardsForDrop(origin, origin, "favorite", 2.25f, 1f)
        assertEquals(listOf("weather", "favorite"), swapped.map { it.catalogId })
        val packed = packCards(swapped)
        assertEquals("weather", packed[0].instanceId)
        assertEquals(0, packed[0].column)
        assertEquals("favorite", packed[1].instanceId)
        assertEquals(2, packed[1].column)
        val back = previewCardsForDrop(origin, swapped, "favorite", 1.7f, 1f)
        assertEquals(listOf("favorite", "weather"), back.map { it.catalogId })
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
    fun hoveringFullWidthAfterInsertBelowDoesNotBounceIt() {
        val advice = card("advice", CardSize.FullByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val infoflow = card("infoflow", CardSize.FourByFour)
        val origin = listOf(advice, notes, shortcuts, infoflow)
        val below = previewCardsForDrop(origin, origin, "shortcuts", 2f, 6f)
        assertEquals(
            listOf("advice", "infoflow", "notes", "shortcuts"),
            below.map { it.catalogId },
        )
        val packed = packCards(below)
        val forYou = packed.first { it.instanceId == "infoflow" }
        val lower = forYou.row + forYou.rows * 0.75f
        val still = previewCardsForDrop(origin, below, "shortcuts", 2f, lower)
        assertEquals(below.map { it.catalogId }, still.map { it.catalogId })
        val upper = forYou.row + forYou.rows * 0.45f
        val heldUpper = previewCardsForDrop(origin, below, "shortcuts", 2f, upper)
        assertEquals(below.map { it.catalogId }, heldUpper.map { it.catalogId })
        val originPlace = packCards(origin).first { it.instanceId == "shortcuts" }
        val bornRow = originPlace.row + originPlace.rows / 2f
        val restored = previewCardsForDrop(origin, below, "shortcuts", 2f, bornRow)
        assertEquals(origin.map { it.catalogId }, restored.map { it.catalogId })
    }

    @Test
    fun draggingPastTwoRowsThenBackUpReversesOneRowAtATime() {
        val a = card("a", CardSize.FullByTwo)
        val b = card("b", CardSize.FullByTwo)
        val c = card("c", CardSize.FullByTwo)
        val origin = listOf(a, b, c)
        val first = previewCardsForDrop(origin, origin, "a", 2f, 5f)
        assertEquals(listOf("b", "a", "c"), first.map { it.catalogId })
        val second = previewCardsForDrop(origin, first, "a", 2f, 5f)
        assertEquals(listOf("b", "c", "a"), second.map { it.catalogId })
        val packed = packCards(second)
        val cPlace = packed.first { it.instanceId == "c" }
        val back = previewCardsForDrop(
            origin,
            second,
            "a",
            2f,
            cPlace.row + cPlace.rows * 0.2f,
        )
        assertEquals(listOf("b", "a", "c"), back.map { it.catalogId })
        val bPlace = packCards(back).first { it.instanceId == "b" }
        val home = previewCardsForDrop(
            origin,
            back,
            "a",
            2f,
            bPlace.row + bPlace.rows * 0.2f,
        )
        assertEquals(listOf("a", "b", "c"), home.map { it.catalogId })
    }

    @Test
    fun halfWidthBelowFullWidthCanMoveToItsTop() {
        val small = card("favorite", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val big = card("shortcuts", CardSize.FullByTwo)
        val origin = listOf(small, weather, big)
        val below = previewCardsForDrop(origin, origin, "favorite", 2f, 3f)
        assertEquals(listOf("shortcuts", "favorite", "weather"), below.map { it.catalogId })
        val packed = packCards(below)
        val bigPlace = packed.first { it.instanceId == "shortcuts" }
        val lower = bigPlace.row + bigPlace.rows * 0.9f
        val stillBelow = previewCardsForDrop(origin, below, "favorite", 2f, lower)
        assertEquals(below.map { it.catalogId }, stillBelow.map { it.catalogId })
        val top = bigPlace.row + bigPlace.rows * 0.2f
        val above = previewCardsForDrop(origin, below, "favorite", 2f, top)
        assertEquals(origin.map { it.catalogId }, above.map { it.catalogId })
        val overTop = previewCardsForDrop(origin, below, "favorite", 2f, bigPlace.row - 0.4f)
        assertEquals(origin.map { it.catalogId }, overTop.map { it.catalogId })
    }

    @Test
    fun hoveringFullWidthAfterInsertAboveDoesNotBounceIt() {
        val advice = card("advice", CardSize.FullByTwo)
        val infoflow = card("infoflow", CardSize.FourByFour)
        val shortcuts = card("shortcuts", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val origin = listOf(advice, infoflow, shortcuts, weather)
        val above = previewCardsForDrop(origin, origin, "weather", 3f, 3f)
        assertEquals(
            listOf("advice", "shortcuts", "weather", "infoflow"),
            above.map { it.catalogId },
        )
        val packed = packCards(above)
        val forYou = packed.first { it.instanceId == "infoflow" }
        val mid = forYou.row + forYou.rows / 2f
        val still = previewCardsForDrop(origin, above, "weather", 3f, mid)
        assertEquals(above.map { it.catalogId }, still.map { it.catalogId })
        val originPlace = packCards(origin).first { it.instanceId == "weather" }
        val bornRow = originPlace.row + originPlace.rows / 2f
        val restored = previewCardsForDrop(origin, above, "weather", 3f, bornRow)
        assertEquals(origin.map { it.catalogId }, restored.map { it.catalogId })
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
    fun sideBySidePairSwapsInsteadOfJumpingOntoTheDock() {
        val infoflow = card("infoflow", CardSize.FourByFour)
        val small = card("favorite", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val dock = card("shortcuts", CardSize.FullByTwo)
        val origin = listOf(infoflow, small, weather, dock)
        val packed = packCards(origin)
        assertEquals(4, packed[1].row)
        assertEquals(4, packed[2].row)
        assertEquals(6, packed[3].row)
        val preview = previewCardsForDrop(origin, origin, "favorite", 3.1f, 5.3f)
        assertEquals(
            listOf("infoflow", "weather", "favorite", "shortcuts"),
            preview.map { it.catalogId },
        )
        val next = packCards(preview)
        assertEquals("weather", next[1].instanceId)
        assertEquals(0, next[1].column)
        assertEquals("favorite", next[2].instanceId)
        assertEquals(2, next[2].column)
        assertEquals(next[1].row, next[2].row)
        assertEquals("shortcuts", next[3].instanceId)
        assertEquals(6, next[3].row)
    }

    @Test
    fun lockedRecentAppsStayAtTheTopAndCannotBeADropTarget() {
        val recent = card("recent", CardSize.FullByOne).copy(canDrag = false, canEdit = false)
        val weather = card("weather", CardSize.TwoByTwo)
        val notes = card("notes", CardSize.TwoByTwo)
        val pinned = pinLockedCards(listOf(weather, notes, recent))
        assertEquals(listOf("recent", "weather", "notes"), pinned.map { it.catalogId })
        val packed = packCards(pinned)
        assertEquals(0, packed[0].row)
        assertEquals(4, packed[0].columns)
        assertEquals(1, packed[0].rows)
        assertTrue(packed[0].locked)
        assertEquals(1, packed[1].row)
        assertEquals(null, dropTargetId(packed, "notes", 1f, 0.4f, draggedColumns = 2))
        val preview = previewCardsForDrop(pinned, pinned, "weather", 1f, 0.2f)
        assertEquals("recent", preview.first().catalogId)
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

    @Test
    fun closeHalfRowGapsPullsLaterTwoByTwoIntoTheHole() {
        val clock = card("clock", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.FullByTwo)
        val recorder = card("recorder", CardSize.TwoByTwo)
        val music = card("music", CardSize.FullByTwo)
        val closed = closeHalfRowGaps(listOf(clock, weather, recorder, music))
        assertEquals(listOf("clock", "recorder", "weather", "music"), closed.map { it.catalogId })
        assertTrue(emptyAddSlots(packCards(closed)).isEmpty())
    }

    @Test
    fun closeHalfRowGapsFillsEveryHoleBeforeLeavingOneSeat() {
        val a = card("a", CardSize.TwoByTwo)
        val full = card("full", CardSize.FullByTwo)
        val b = card("b", CardSize.TwoByTwo)
        val full2 = card("full2", CardSize.FullByTwo)
        val c = card("c", CardSize.TwoByTwo)
        val closed = closeHalfRowGaps(listOf(a, full, b, full2, c))
        assertEquals(listOf("a", "b", "full", "full2", "c"), closed.map { it.catalogId })
        assertEquals(1, emptyAddSlots(packCards(closed)).size)
    }

    @Test
    fun closeHalfRowGapsLeavesAPackedRowAlone() {
        val clock = card("clock", CardSize.TwoByTwo)
        val recorder = card("recorder", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.FullByTwo)
        val closed = closeHalfRowGaps(listOf(clock, recorder, weather))
        assertEquals(listOf("clock", "recorder", "weather"), closed.map { it.catalogId })
    }

    @Test
    fun addedTwoByTwoFillsTheEmptySeatBesideAHalfCard() {
        val notes = card("notes", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val next = insertFillingEmptyTwoByTwo(listOf(notes, advice), weather)
        assertEquals(listOf("notes", "weather", "advice"), next.map { it.catalogId })
        val packed = packCards(next)
        assertEquals(0, packed[0].column)
        assertEquals(2, packed[1].column)
        assertEquals(0, packed[1].row)
        assertEquals(2, packed[2].row)
        assertTrue(emptyAddSlots(packed).isEmpty())
    }

    @Test
    fun addedTwoByTwoFillsTheFirstEmptySeatWhenSeveralExist() {
        val notes = card("notes", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val extra = card("extra", CardSize.TwoByTwo)
        val next = insertFillingEmptyTwoByTwo(listOf(notes, advice, extra), weather)
        assertEquals(listOf("notes", "weather", "advice", "extra"), next.map { it.catalogId })
    }

    @Test
    fun addedFullWidthDoesNotJumpIntoATwoByTwoHole() {
        val notes = card("notes", CardSize.TwoByTwo)
        val advice = card("advice", CardSize.FullByTwo)
        val next = insertFillingEmptyTwoByTwo(listOf(notes), advice)
        assertEquals(listOf("notes", "advice"), next.map { it.catalogId })
    }

    @Test
    fun addedTwoByTwoAppendsWhenThereIsNoEmptySeat() {
        val notes = card("notes", CardSize.TwoByTwo)
        val weather = card("weather", CardSize.TwoByTwo)
        val extra = card("extra", CardSize.TwoByTwo)
        val next = insertFillingEmptyTwoByTwo(listOf(notes, weather), extra)
        assertEquals(listOf("notes", "weather", "extra"), next.map { it.catalogId })
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
