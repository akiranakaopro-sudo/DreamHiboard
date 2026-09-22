package gd.app.hiboard.catalog

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
            groupTitle = "Features",
            name = "Recent apps",
            description = "Last opened app first, then Dialer, Contacts, Messages, Camera, and Settings.",
            size = CardSize.FullByOne,
            engine = CardEngineId.RecentApps,
            defaultSubscribed = true,
            locked = true,
        ),
        CardCatalogEntry(
            id = "weather",
            groupId = GROUP_WEATHER,
            groupTitle = "Weather",
            name = "Weather",
            description = "Local 4×2 weather scene. Push your own snapshot through the weather API.",
            size = CardSize.FullByTwo,
            engine = CardEngineId.Weather,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "storage",
            groupId = GROUP_FEATURES,
            groupTitle = "Features",
            name = "Storage",
            description = "RAM used and free. Tap to open System Manager.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Storage,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "flashlight",
            groupId = GROUP_FEATURES,
            groupTitle = "Features",
            name = "Flashlight",
            description = "Tap to turn the torch on or off. Uses the rear camera flash.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Flashlight,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "notes",
            groupId = GROUP_FEATURES,
            groupTitle = "Features",
            name = "All notes",
            description = "Latest note from DreamNote. Tap to open the list, plus to write a new note.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "recorder",
            groupId = GROUP_FEATURES,
            groupTitle = "Features",
            name = "Recorder",
            description = "Start, pause, mark, and save with DreamRecorder. Tap the card to open Recorder.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Recorder,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "contacts",
            groupId = GROUP_FEATURES,
            groupTitle = "Features",
            name = "Contacts",
            description = "Favorite people in one row. Tap a person to open them.",
            size = CardSize.FullByTwo,
            engine = CardEngineId.Contacts,
            defaultSubscribed = false,
        ),
        CardCatalogEntry(
            id = "music",
            groupId = GROUP_FEATURES,
            groupTitle = "Features",
            name = "Music",
            description = "Cover, progress, and playback for the current track.",
            size = CardSize.FullByTwo,
            engine = CardEngineId.Music,
            defaultSubscribed = false,
        ),
        CardCatalogEntry(
            id = "calendar",
            groupId = GROUP_WEATHER,
            groupTitle = "Weather",
            name = "Calendar",
            description = "This month, with today marked. Tap to open Calendar.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Calendar,
            defaultSubscribed = false,
        ),
        CardCatalogEntry(
            id = "clock",
            groupId = GROUP_WEATHER,
            groupTitle = "Weather",
            name = "Clock",
            description = "Analog clock set to the system time.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Clock,
            defaultSubscribed = false,
        ),
        CardCatalogEntry(
            id = "weatherclock",
            groupId = GROUP_WEATHER,
            groupTitle = "Weather",
            name = "Weather clock",
            description = "System time with today's weather.",
            size = CardSize.FullByTwo,
            engine = CardEngineId.WeatherClock,
            defaultSubscribed = false,
        ),
        CardCatalogEntry(
            id = "localtime",
            groupId = GROUP_WEATHER,
            groupTitle = "Weather",
            name = "Local time clock",
            description = "Hour over minute, set to the system time.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.LocalTime,
            defaultSubscribed = false,
        ),
    )

    fun byId(id: String): CardCatalogEntry? = entries.firstOrNull { it.id == id }

    fun lockedIds(): List<String> = entries.filter { it.locked }.map { it.id }

    fun defaultBoardIds(): List<String> =
        entries.filter { it.defaultSubscribed && !it.locked }.map { it.id }

    fun defaultRecommendedIds(): List<String> =
        entries.filter { !it.defaultSubscribed && !it.locked }.map { it.id }

    fun pinLocked(ids: List<String>): List<String> {
        val locked = lockedIds()
        return locked + ids.filter { it !in locked }
    }
}
