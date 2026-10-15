package gd.app.hiboard.catalog

import android.content.res.Resources
import gd.app.hiboard.model.CardCatalogEntry

data class WidgetStoreSection(
    val letter: String,
    val entries: List<CardCatalogEntry>,
)

data class WidgetStoreCategory(
    val name: String,
    val entries: List<CardCatalogEntry>,
)

data class WidgetStoreCategorySection(
    val letter: String,
    val categories: List<WidgetStoreCategory>,
)

fun widgetStoreLetter(name: String): String {
    val ch = name.trim().firstOrNull() ?: return "#"
    return if (ch.isLetter()) ch.uppercaseChar().toString() else "#"
}

fun widgetStoreSections(
    resources: Resources,
    catalog: List<CardCatalogEntry>,
    query: String = "",
    groupId: String? = null,
): List<WidgetStoreSection> = widgetStoreSections(
    catalog = catalog,
    query = query,
    groupId = groupId,
    nameOf = { it.name(resources) },
    descriptionOf = { it.description(resources) },
)

fun widgetStoreSections(
    catalog: List<CardCatalogEntry>,
    query: String = "",
    groupId: String? = null,
    nameOf: (CardCatalogEntry) -> String,
    descriptionOf: (CardCatalogEntry) -> String = nameOf,
): List<WidgetStoreSection> {
    val needle = query.trim()
    val visible = catalog.filter { !it.locked }
        .filter { groupId == null || needle.isNotBlank() || it.groupId == groupId }
        .filter { entry ->
            needle.isBlank() ||
                nameOf(entry).contains(needle, ignoreCase = true) ||
                descriptionOf(entry).contains(needle, ignoreCase = true)
        }
        .sortedBy { nameOf(it).lowercase() }
    return visible
        .groupBy { widgetStoreLetter(nameOf(it)) }
        .toSortedMap()
        .map { WidgetStoreSection(it.key, it.value) }
}

fun widgetStoreCategories(
    resources: Resources,
    catalog: List<CardCatalogEntry>,
    query: String = "",
    groupId: String? = null,
): List<WidgetStoreCategorySection> = widgetStoreCategories(
    catalog = catalog,
    query = query,
    groupId = groupId,
    nameOf = { it.name(resources) },
    descriptionOf = { it.description(resources) },
    categoryOf = { it.listCategory(resources) },
)

fun widgetStoreCategories(
    catalog: List<CardCatalogEntry>,
    query: String = "",
    groupId: String? = null,
    nameOf: (CardCatalogEntry) -> String,
    descriptionOf: (CardCatalogEntry) -> String = nameOf,
    categoryOf: (CardCatalogEntry) -> String = nameOf,
): List<WidgetStoreCategorySection> {
    val needle = query.trim()
    val pool = catalog.filter { !it.locked }
        .filter { groupId == null || needle.isNotBlank() || it.groupId == groupId }
    val rows = pool.filter { entry ->
        needle.isBlank() ||
            categoryOf(entry).contains(needle, ignoreCase = true) ||
            nameOf(entry).contains(needle, ignoreCase = true) ||
            descriptionOf(entry).contains(needle, ignoreCase = true)
    }
        .groupBy { categoryOf(it) }
        .map { (name, entries) -> WidgetStoreCategory(name, entries) }
        .sortedBy { it.name.lowercase() }
    return rows
        .groupBy { widgetStoreLetter(it.name) }
        .toSortedMap()
        .map { WidgetStoreCategorySection(it.key, it.value) }
}

fun widgetStoreTabs(): List<Pair<String?, String>> {
    return listOf(
        null to "All",
        DefaultCatalog.GROUP_FEATURES to "Features",
        DefaultCatalog.GROUP_WEATHER to "Weather",
    )
}

fun widgetStoreTabIndex(groupId: String?): Int {
    val index = widgetStoreTabs().indexOfFirst { it.first == groupId }
    return if (index >= 0) index else 0
}

fun widgetStoreGroups(resources: Resources, catalog: List<CardCatalogEntry>): List<Pair<String, String>> {
    return catalog.filter { !it.locked }
        .map { it.groupId to it.groupTitle(resources) }
        .distinct()
}

fun widgetCountLabel(count: Int = 1): String {
    return if (count == 1) "1 widget" else "$count widgets"
}
