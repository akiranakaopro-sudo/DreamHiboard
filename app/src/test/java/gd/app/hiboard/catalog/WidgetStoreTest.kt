package gd.app.hiboard.catalog

import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardSize
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetStoreTest {
    private val weather = entry("weather", "Weather")
    private val notes = entry("notes", "All notes")
    private val storage = entry("storage", "Storage")
    private val locked = entry("recent", "Recent apps", locked = true)

    @Test
    fun sectionsSkipLockedAndGroupByLetter() {
        val sections = widgetStoreSections(listOf(weather, notes, storage, locked))
        assertEquals(listOf("A", "S", "W"), sections.map { it.letter })
        assertEquals(listOf("All notes"), sections[0].entries.map { it.name })
        assertEquals(listOf("Storage"), sections[1].entries.map { it.name })
        assertEquals(listOf("Weather"), sections[2].entries.map { it.name })
    }

    @Test
    fun queryAndGroupFilterTheList() {
        val catalog = listOf(weather, notes, storage)
        assertEquals(
            listOf("Weather"),
            widgetStoreSections(catalog, query = "wea").flatMap { it.entries }.map { it.name },
        )
        assertEquals(
            emptyList<String>(),
            widgetStoreSections(catalog, groupId = "missing").flatMap { it.entries }.map { it.name },
        )
    }

    @Test
    fun countLabelMatchesOppo() {
        assertEquals("1 widget", widgetCountLabel(1))
        assertEquals("3 widgets", widgetCountLabel(3))
    }

    private fun entry(id: String, name: String, locked: Boolean = false): CardCatalogEntry {
        return CardCatalogEntry(
            id = id,
            groupId = DefaultCatalog.GROUP_TOOLS,
            groupTitle = "Tools",
            name = name,
            description = name,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = false,
            locked = locked,
        )
    }
}
