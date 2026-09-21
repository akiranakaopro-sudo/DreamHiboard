package gd.app.hiboard.catalog

import gd.app.hiboard.model.CardCatalogEntry

data class WidgetStoreSection(
    val letter: String,
    val entries: List<CardCatalogEntry>,
)

fun widgetStoreLetter(name: String): String {
    val ch = name.trim().firstOrNull() ?: return "#"
    return if (ch.isLetter()) ch.uppercaseChar().toString() else "#"
}

fun widgetStoreSections(
    catalog: List<CardCatalogEntry>,
    query: String = "",
    groupId: String? = null,
): List<WidgetStoreSection> {
    val needle = query.trim()
    val visible = catalog.filter { !it.locked }
        .filter { groupId == null || it.groupId == groupId }
        .filter { entry ->
            needle.isBlank() ||
                entry.name.contains(needle, ignoreCase = true) ||
                entry.description.contains(needle, ignoreCase = true)
        }
        .sortedBy { it.name.lowercase() }
    return visible
        .groupBy { widgetStoreLetter(it.name) }
        .toSortedMap()
        .map { WidgetStoreSection(it.key, it.value) }
}

fun widgetStoreTabs(): List<Pair<String?, String>> {
    return listOf(
        null to "All",
        DefaultCatalog.GROUP_FEATURES to "Features",
        DefaultCatalog.GROUP_WEATHER to "Weather",
    )
}

fun widgetStoreGroups(catalog: List<CardCatalogEntry>): List<Pair<String, String>> {
    return catalog.filter { !it.locked }
        .map { it.groupId to it.groupTitle }
        .distinct()
}

fun widgetCountLabel(count: Int = 1): String {
    return if (count == 1) "1 widget" else "$count widgets"
}
