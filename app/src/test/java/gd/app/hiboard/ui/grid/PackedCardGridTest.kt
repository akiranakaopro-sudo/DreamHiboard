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

    private fun card(id: String, size: CardSize) = CardInstance(
        instanceId = id,
        catalogId = id,
        displayName = id,
        size = size,
        area = CardArea.Subscribe,
        engine = CardEngineId.Weather,
    )
}
