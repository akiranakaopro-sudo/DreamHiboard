package gd.app.hiboard.engine

const val CONTACT_LIMIT = 4

data class RankedContact(
    val name: String,
    val starred: Boolean,
    val timesContacted: Int = 0,
    val lookupUri: String = "",
    val photoUri: String? = null,
)

/** Starred people first, then the ones opened most, then name. */
fun rankContacts(people: List<RankedContact>, limit: Int = CONTACT_LIMIT): List<RankedContact> {
    return people
        .filter { it.name.isNotBlank() }
        .sortedWith(
            compareByDescending<RankedContact> { it.starred }
                .thenByDescending { it.timesContacted }
                .thenBy { it.name.lowercase() },
        )
        .take(limit)
}

/** Oppo prints the given name under the photo. */
fun contactGivenName(displayName: String): String {
    val trimmed = displayName.trim()
    val split = trimmed.indexOf(' ')
    return if (split > 0) trimmed.substring(0, split) else trimmed
}
