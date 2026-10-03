package gd.app.hiboard.catalog

import gd.app.hiboard.R
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardSize

object DefaultCatalog {
    const val GROUP_FEATURES = "features"
    const val GROUP_WEATHER = "weather"
    const val GROUP_TOOLS = GROUP_FEATURES

    val entries: List<CardCatalogEntry> = listOf(
        CardCatalogEntry(
            id = "recent",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_recent_name,
            descriptionRes = R.string.widget_recent_desc,
            size = CardSize.FullByOne,
            engine = CardEngineId.RecentApps,
            defaultSubscribed = true,
            locked = true,
        ),
        CardCatalogEntry(
            id = "weather",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_weather_name,
            descriptionRes = R.string.widget_weather_desc,
            size = CardSize.FullByTwo,
            engine = CardEngineId.Weather,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "weathersquare",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_weathersquare_name,
            descriptionRes = R.string.widget_weathersquare_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Weather,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_weather,
        ),
        CardCatalogEntry(
            id = "storage",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_storage_name,
            descriptionRes = R.string.widget_storage_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Storage,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "flashlight",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_flashlight_name,
            descriptionRes = R.string.widget_flashlight_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Flashlight,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "notes",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_notes_name,
            descriptionRes = R.string.widget_notes_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "noteswide",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_noteswide_name,
            descriptionRes = R.string.widget_noteswide_desc,
            size = CardSize.FullByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_notes,
        ),
        CardCatalogEntry(
            id = "noteslarge",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_noteslarge_name,
            descriptionRes = R.string.widget_noteslarge_desc,
            size = CardSize.FourByFour,
            engine = CardEngineId.Notes,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_notes,
        ),
        CardCatalogEntry(
            id = "recorder",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_recorder_name,
            descriptionRes = R.string.widget_recorder_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Recorder,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "contacts",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_contacts_name,
            descriptionRes = R.string.widget_contacts_desc,
            size = CardSize.FullByTwo,
            engine = CardEngineId.Contacts,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "music",
            groupId = GROUP_FEATURES,
            groupTitleRes = R.string.widget_group_features,
            nameRes = R.string.widget_music_name,
            descriptionRes = R.string.widget_music_desc,
            size = CardSize.FullByTwo,
            engine = CardEngineId.Music,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "calendar",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_calendar_name,
            descriptionRes = R.string.widget_calendar_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Calendar,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "clock",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_clock_name,
            descriptionRes = R.string.widget_clock_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Clock,
            defaultSubscribed = true,
            storeCategoryRes = R.string.widget_category_clock,
        ),
        CardCatalogEntry(
            id = "weatherclock",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_weatherclock_name,
            descriptionRes = R.string.widget_weatherclock_desc,
            size = CardSize.FullByTwo,
            engine = CardEngineId.WeatherClock,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_clock,
        ),
        CardCatalogEntry(
            id = "weatherclocksquare",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_weatherclocksquare_name,
            descriptionRes = R.string.widget_weatherclocksquare_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.WeatherClock,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_clock,
        ),
        CardCatalogEntry(
            id = "localtime",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_localtime_name,
            descriptionRes = R.string.widget_localtime_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.LocalTime,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_clock,
        ),
        CardCatalogEntry(
            id = "romanclock",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_romanclock_name,
            descriptionRes = R.string.widget_romanclock_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.RomanClock,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_clock,
        ),
        CardCatalogEntry(
            id = "weatherdial",
            groupId = GROUP_WEATHER,
            groupTitleRes = R.string.widget_group_weather,
            nameRes = R.string.widget_weatherdial_name,
            descriptionRes = R.string.widget_weatherdial_desc,
            size = CardSize.TwoByTwo,
            engine = CardEngineId.WeatherDial,
            defaultSubscribed = false,
            storeCategoryRes = R.string.widget_category_clock,
        ),
    )

    fun byId(id: String): CardCatalogEntry? = entries.firstOrNull { it.id == id }

    fun lockedIds(): List<String> = entries.filter { it.locked }.map { it.id }

    /** Seat order of the default board; the grid packs cards in this order. */
    private val defaultBoardOrder = listOf(
        "weather",
        "storage", "clock",
        "notes", "flashlight",
        "calendar", "recorder",
        "contacts",
        "music",
    )

    fun defaultBoardIds(): List<String> =
        entries.filter { it.defaultSubscribed && !it.locked }
            .sortedBy { entry -> defaultBoardOrder.indexOf(entry.id).let { if (it < 0) Int.MAX_VALUE else it } }
            .map { it.id }

    fun defaultRecommendedIds(): List<String> =
        entries.filter { !it.defaultSubscribed && !it.locked }.map { it.id }

    fun pinLocked(ids: List<String>): List<String> {
        val locked = lockedIds()
        return locked + ids.filter { it !in locked }
    }
}
