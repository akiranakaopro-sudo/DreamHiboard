package gd.app.hiboard.catalog

import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardSize
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetStoreTest {
    private val labels = mapOf(
        1 to "Weather",
        2 to "All notes",
        3 to "Storage",
        4 to "Recent apps",
        10 to "Clock",
        11 to "Local time clock",
        12 to "Weather clock",
    )

    private val weather = entry("weather", nameRes = 1, groupId = DefaultCatalog.GROUP_WEATHER)
    private val notes = entry("notes", nameRes = 2)
    private val storage = entry("storage", nameRes = 3)
    private val locked = entry("recent", nameRes = 4, locked = true)

    @Test
    fun sectionsSkipLockedAndGroupByLetter() {
        val sections = sectionsOf(listOf(weather, notes, storage, locked))
        assertEquals(listOf("A", "S", "W"), sections.map { it.letter })
        assertEquals(listOf("notes"), sections[0].entries.map { it.id })
        assertEquals(listOf("storage"), sections[1].entries.map { it.id })
        assertEquals(listOf("weather"), sections[2].entries.map { it.id })
    }

    @Test
    fun queryAndGroupFilterTheList() {
        val catalog = listOf(weather, notes, storage)
        assertEquals(
            listOf("weather"),
            sectionsOf(catalog, query = "wea").flatMap { it.entries }.map { it.id },
        )
        assertEquals(
            listOf("weather"),
            sectionsOf(catalog, groupId = DefaultCatalog.GROUP_WEATHER)
                .flatMap { it.entries }
                .map { it.id },
        )
        assertEquals(
            listOf("notes", "storage"),
            sectionsOf(catalog, groupId = DefaultCatalog.GROUP_FEATURES)
                .flatMap { it.entries }
                .map { it.id },
        )
        assertEquals(
            listOf("weather"),
            sectionsOf(catalog, query = "wea", groupId = DefaultCatalog.GROUP_FEATURES)
                .flatMap { it.entries }
                .map { it.id },
        )
        assertEquals(
            emptyList<String>(),
            sectionsOf(catalog, groupId = "missing").flatMap { it.entries }.map { it.id },
        )
    }

    @Test
    fun featuresAndWeatherShowUnlockedWidgets() {
        val features = DefaultCatalog.entries
            .filter { !it.locked && it.groupId == DefaultCatalog.GROUP_FEATURES }
            .sortedBy { it.id }
        assertEquals(
            listOf(
                "battery",
                "batterysmall",
                "contacts",
                "flashlight",
                "music",
                "notes",
                "noteslarge",
                "noteswide",
                "recorder",
                "storage",
            ),
            features.map { it.id },
        )
        assertEquals(CardSize.FullByTwo, features.first { it.id == "battery" }.size)
        assertEquals(CardSize.TwoByTwo, features.first { it.id == "batterysmall" }.size)
        assertEquals(CardSize.FullByTwo, features.first { it.id == "contacts" }.size)
        assertEquals(CardSize.FullByTwo, features.first { it.id == "music" }.size)
        assertEquals(CardSize.FullByTwo, features.first { it.id == "noteswide" }.size)
        assertEquals(CardSize.FourByFour, features.first { it.id == "noteslarge" }.size)
        assertEquals(
            true,
            features.filter {
                it.id !in setOf("battery", "contacts", "music", "noteswide", "noteslarge")
            }.all { it.size.columns == 2 && it.size.rows == 2 },
        )
        val weather = DefaultCatalog.entries
            .filter { !it.locked && it.groupId == DefaultCatalog.GROUP_WEATHER }
            .sortedBy { it.id }
        assertEquals(
            listOf(
                "calendar",
                "clock",
                "localtime",
                "romanclock",
                "weather",
                "weatherclock",
                "weatherclocksquare",
                "weatherdial",
                "weathersquare",
            ),
            weather.map { it.id },
        )
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "weatherclocksquare" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "weathersquare" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "calendar" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "clock" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "localtime" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "romanclock" }.size)
        assertEquals(CardSize.TwoByTwo, weather.first { it.id == "weatherdial" }.size)
        assertEquals(CardSize.FullByTwo, weather.first { it.id == "weather" }.size)
        assertEquals(CardSize.FullByTwo, weather.first { it.id == "weatherclock" }.size)
    }

    @Test
    fun defaultBoardOrderMatchesCuratedBoard() {
        assertEquals(
            listOf("weather", "storage", "clock", "notes", "flashlight", "calendar", "recorder", "contacts", "music"),
            DefaultCatalog.defaultBoardIds(),
        )
        assertEquals(true, DefaultCatalog.byId("flashlight")?.defaultSubscribed)
        assertEquals(CardSize.FullByTwo, DefaultCatalog.byId("weather")?.size)
        assertEquals(
            listOf(
                "weathersquare",
                "battery",
                "batterysmall",
                "noteswide",
                "noteslarge",
                "weatherclock",
                "weatherclocksquare",
                "localtime",
                "romanclock",
                "weatherdial",
            ),
            DefaultCatalog.defaultRecommendedIds(),
        )
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
    fun clockWidgetsShareOneCategory() {
        val clockIds = listOf(
            "clock",
            "weatherclock",
            "weatherclocksquare",
            "localtime",
            "romanclock",
            "weatherdial",
        )
        val catalog = clockIds.map { id ->
            entry(
                id = id,
                nameRes = when (id) {
                    "localtime" -> 11
                    "weatherclock" -> 12
                    else -> 10
                },
                groupId = DefaultCatalog.GROUP_WEATHER,
                storeCategoryRes = 10,
            )
        } + entry("notes", nameRes = 2, storeCategoryRes = 2)
        val categories = widgetStoreCategories(
            catalog = catalog,
            nameOf = ::label,
            categoryOf = { entry ->
                if (entry.storeCategoryRes != 0) label(entry.copy(nameRes = entry.storeCategoryRes))
                else label(entry)
            },
        ).flatMap { it.categories }
        assertEquals(
            false,
            categories.any { it.name == "Local time clock" || it.name == "Weather clock" },
        )
        val clock = categories.first { it.name == "Clock" }
        assertEquals(clockIds, clock.entries.map { it.id })
        assertEquals(
            listOf("localtime"),
            widgetStoreCategories(
                catalog = catalog,
                query = "local time",
                nameOf = ::label,
                categoryOf = { entry ->
                    if (entry.storeCategoryRes != 0) label(entry.copy(nameRes = entry.storeCategoryRes))
                    else label(entry)
                },
            )
                .flatMap { it.categories }
                .flatMap { it.entries }
                .map { it.id },
        )
    }

    @Test
    fun countLabelMatchesOppo() {
        assertEquals("1 widget", widgetCountLabel(1))
        assertEquals("3 widgets", widgetCountLabel(3))
    }

    private fun sectionsOf(
        catalog: List<CardCatalogEntry>,
        query: String = "",
        groupId: String? = null,
    ) = widgetStoreSections(
        catalog = catalog,
        query = query,
        groupId = groupId,
        nameOf = ::label,
    )

    private fun label(entry: CardCatalogEntry): String = labels.getValue(entry.nameRes)

    private fun entry(
        id: String,
        nameRes: Int,
        locked: Boolean = false,
        groupId: String = DefaultCatalog.GROUP_FEATURES,
        storeCategoryRes: Int = 0,
    ): CardCatalogEntry {
        return CardCatalogEntry(
            id = id,
            groupId = groupId,
            groupTitleRes = if (groupId == DefaultCatalog.GROUP_WEATHER) 100 else 101,
            nameRes = nameRes,
            descriptionRes = nameRes,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = false,
            locked = locked,
            storeCategoryRes = storeCategoryRes,
        )
    }
}
