package gd.app.hiboard.model

/**
 * 4-column grid sizes, mapped from ColorOS `CardSizeOf`.
 *
 * TWO_PLUS_TWO -> TwoByTwo, TWO_PLUS_FOUR -> TwoByFour,
 * FOUR_PLUS_FOUR -> FourByFour, N_PLUS_FOUR -> FullByTwo,
 * ONE_PLUS_TWO -> OneByTwo.
 */
enum class CardSize(val columns: Int, val rows: Int) {
    OneByTwo(1, 2),
    TwoByTwo(2, 2),
    TwoByFour(2, 4),
    FourByFour(4, 4),
    FullByOne(4, 1),
    FullByTwo(4, 2),
}

enum class CardArea {
    Subscribe,
    Recommend,
}

enum class CardEngineId {
    Weather,
    Notes,
    RecentApps,
    Flashlight,
    Storage,
    Recorder,
}

enum class RecorderUiState {
    Idle,
    Recording,
    Paused,
}

enum class CardAction {
    Create,
    Bind,
    Visible,
    Hidden,
    Destroy,
}

/** Catalog row — ColorOS `CardConfigInfo`. */
data class CardCatalogEntry(
    val id: String,
    val groupId: String,
    val groupTitle: String,
    val name: String,
    val description: String,
    val size: CardSize,
    val engine: CardEngineId,
    val defaultSubscribed: Boolean,
    val resizable: Boolean = false,
    val locked: Boolean = false,
)

/** Placed instance — ColorOS `CardInfo`. */
data class CardInstance(
    val instanceId: String,
    val catalogId: String,
    val displayName: String,
    val size: CardSize,
    val area: CardArea,
    val engine: CardEngineId,
    val canDrag: Boolean = true,
    val canEdit: Boolean = true,
    val order: Int = 0,
)

data class BoardSnapshot(
    val subscribed: List<CardInstance>,
    val recommended: List<CardInstance>,
)

data class ShortcutApp(
    val label: String,
    val packageName: String,
    val activityName: String?,
)

data class CardContent(
    val weatherTempC: Int = 22,
    val weatherSummary: String = "",
    val notesPreview: String = "",
    val notesSnippet: String = "",
    val notesWhen: String = "",
    val flashlightOn: Boolean = false,
    val flashlightAvailable: Boolean = false,
    val storageUsedBytes: Long = 0L,
    val storageTotalBytes: Long = 0L,
    val recorderState: RecorderUiState = RecorderUiState.Idle,
    val recorderElapsedMs: Long = 0L,
    val recorderBound: Boolean = false,
    val recentApps: List<ShortcutApp> = emptyList(),
)

data class GridPlacement(
    val instanceId: String,
    val column: Int,
    val row: Int,
    val columns: Int,
    val rows: Int,
    val locked: Boolean = false,
)
