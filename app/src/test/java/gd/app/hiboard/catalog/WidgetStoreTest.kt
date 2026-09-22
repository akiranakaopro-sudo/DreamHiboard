package gd.app.hiboard.catalog

import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardSize
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetStoreTest {
    private val weather = entry("weather", "Weather", groupId = DefaultCatalog.GROUP_WEATHER)
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
            listOf("Weather"),
            widgetStoreSections(catalog, groupId = DefaultCatalog.GROUP_WEATHER)
                .flatMap { it.entries }
                .map { it.name },
        )
        assertEquals(
            listOf("All notes", "Storage"),
            widgetStoreSections(catalog, groupId = DefaultCatalog.GROUP_FEATURES)
                .flatMap { it.entries }
                .map { it.name },
        )
        assertEquals(
            listOf("Weather"),
            widgetStoreSections(catalog, query = "wea", groupId = DefaultCatalog.GROUP_FEATURES)
                .flatMap { it.entries }
                .map { it.name },
        )
        assertEquals(
            emptyList<String>(),
            widgetStoreSections(catalog, groupId = "missing").flatMap { it.entries }.map { it.name },
        )
    }

    @Test
    fun featuresAndWeatherShowUnlockedWidgets() {
        val features = widgetStoreSections(DefaultCatalog.entries, groupId = DefaultCatalog.GROUP_FEATURES)
            .flatMap { it.entries }
        assertEquals(listOf("All notes", "Contacts", "Flashlight", "Recorder", "Storage"), features.map { it.name })
        assertEquals(CardSize.FullByTwo, features.first { it.name == "Contacts" }.size)
        assertEquals(
            true,
            features.filter { it.name != "Contacts" }.all { it.size.columns == 2 && it.size.rows == 2 },
        )
        val weather = widgetStoreSections(DefaultCatalog.entries, groupId = DefaultCatalog.GROUP_WEATHER)
            .flatMap { it.entries }
        assertEquals(listOf("Calendar", "Clock", "Weather", "Weather clock"), weather.map { it.name })
        assertEquals(CardSize.TwoByTwo, weather.first { it.name == "Calendar" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.name == "Clock" }.size)
        assertEquals(CardSize.FullByTwo, weather.first { it.name == "Weather" }.size)
        assertEquals(CardSize.FullByTwo, weather.first { it.name == "Weather clock" }.size)
    }

    @Test
    fun defaultBoardOrderIsWeatherStorageFlashlightNotesRecorder() {
        assertEquals(
            listOf("weather", "storage", "flashlight", "notes", "recorder"),
            DefaultCatalog.defaultBoardIds(),
        )
        assertEquals(true, DefaultCatalog.byId("flashlight")?.defaultSubscribed)
        assertEquals(CardSize.FullByTwo, DefaultCatalog.byId("weather")?.size)
        assertEquals(listOf("contacts", "calendar", "clock", "weatherclock"), DefaultCatalog.defaultRecommendedIds())
    }

    @Test
    fun tabsAreAllFeaturesWeather() {
        assertEquals(
            listOf(null, DefaultCatalog.GROUP_FEATURES, DefaultCatalog.GROUP_WEATHER),
            widgetStoreTabs().map { it.first },
        )
        assertEquals(listOf("All", "Features", "Weather"), widgetStoreTabs().map { it.second })
        assertEquals(0, widgetStoreTabIndex(null))
        assertEquals(1, widgetStoreTabIndex(DefaultCatalog.GROUP_FEATURES))
        assertEquals(2, widgetStoreTabIndex(DefaultCatalog.GROUP_WEATHER))
        assertEquals(0, widgetStoreTabIndex("missing"))
    }

    @Test
    fun countLabelMatchesOppo() {
        assertEquals("1 widget", widgetCountLabel(1))
        assertEquals("3 widgets", widgetCountLabel(3))
    }

    private fun entry(
        id: String,
        name: String,
        locked: Boolean = false,
        groupId: String = DefaultCatalog.GROUP_FEATURES,
    ): CardCatalogEntry {
        return CardCatalogEntry(
            id = id,
            groupId = groupId,
            groupTitle = if (groupId == DefaultCatalog.GROUP_WEATHER) "Weather" else "Features",
            name = name,
            description = name,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = false,
            locked = locked,
        )
    }
}
