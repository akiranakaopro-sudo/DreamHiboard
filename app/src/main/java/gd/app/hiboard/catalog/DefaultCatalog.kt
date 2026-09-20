package gd.app.hiboard.catalog

import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardSize

object DefaultCatalog {
    const val GROUP_GLANCE = "glance"
    const val GROUP_TOOLS = "tools"
    const val GROUP_FEED = "feed"

    val entries: List<CardCatalogEntry> = listOf(
        CardCatalogEntry(
            id = "recent",
            groupId = GROUP_TOOLS,
            groupTitle = "Tools",
            name = "Recent apps",
            description = "Last opened app first, then Dialer, Contacts, Messages, Camera, and Settings.",
            size = CardSize.FullByOne,
            engine = CardEngineId.RecentApps,
            defaultSubscribed = true,
            locked = true,
        ),
        CardCatalogEntry(
            id = "advice",
            groupId = GROUP_GLANCE,
            groupTitle = "Glance",
            name = "Advice",
            description = "Full-width daily glance. ColorOS Dragonfly analog, without SMS/cloud.",
            size = CardSize.FullByTwo,
            engine = CardEngineId.Advice,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "shortcuts",
            groupId = GROUP_TOOLS,
            groupTitle = "Tools",
            name = "Shortcuts",
            description = "Dock of frequently used apps.",
            size = CardSize.FullByTwo,
            engine = CardEngineId.Shortcuts,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "weather",
            groupId = GROUP_TOOLS,
            groupTitle = "Tools",
            name = "Weather",
            description = "Local weather tile.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Weather,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "notes",
            groupId = GROUP_TOOLS,
            groupTitle = "Tools",
            name = "Notes",
            description = "Quick note. Opens gd.app.note when installed.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Notes,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "favorite",
            groupId = GROUP_TOOLS,
            groupTitle = "Tools",
            name = "Favorite",
            description = "Pinned apps.",
            size = CardSize.TwoByTwo,
            engine = CardEngineId.Favorite,
            defaultSubscribed = true,
        ),
        CardCatalogEntry(
            id = "infoflow",
            groupId = GROUP_FEED,
            groupTitle = "Discover",
            name = "Info flow",
            description = "Recommended stories. ColorOS InfoFlow analog, local sample only.",
            size = CardSize.FourByFour,
            engine = CardEngineId.InfoFlow,
            defaultSubscribed = false,
        ),
    )

    fun byId(id: String): CardCatalogEntry? = entries.firstOrNull { it.id == id }

    fun lockedIds(): List<String> = entries.filter { it.locked }.map { it.id }

    fun pinLocked(ids: List<String>): List<String> {
        val locked = lockedIds()
        return locked + ids.filter { it !in locked }
    }
}
