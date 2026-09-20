package gd.app.hiboard.engine

const val RECENT_APP_LIMIT = 5

fun recentPackageOrder(
    defaults: List<String>,
    recents: List<String>,
    lastOpened: String? = recents.firstOrNull(),
    limit: Int = RECENT_APP_LIMIT,
): List<String> {
    val ordered = LinkedHashSet<String>()
    if (!lastOpened.isNullOrBlank()) ordered += lastOpened
    recents.forEach { packageName ->
        if (packageName.isNotBlank()) ordered += packageName
    }
    defaults.forEach { packageName ->
        if (packageName.isNotBlank()) ordered += packageName
    }
    return ordered.take(limit)
}
