package gd.app.hiboard.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gd.app.hiboard.HiboardApp
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.catalog.name
import gd.app.hiboard.data.BoardRepository
import gd.app.hiboard.engine.ALL_NOTES_FOLDER
import gd.app.hiboard.engine.CardEngineRegistry
import gd.app.hiboard.engine.FlashlightToggle
import gd.app.hiboard.engine.RecorderCommand
import gd.app.hiboard.engine.RecorderSendResult
import gd.app.hiboard.host.HostEvent
import gd.app.hiboard.model.BoardSnapshot
import gd.app.hiboard.model.CardAction
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardContent
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.ShortcutApp
import gd.app.hiboard.ui.grid.insertFillingEmptyTwoByTwo
import gd.app.hiboard.ui.grid.pinLockedCards
import android.os.SystemClock
import java.util.UUID
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HiboardUiState(
    val board: BoardSnapshot = BoardSnapshot(emptyList(), emptyList()),
    val catalog: List<CardCatalogEntry> = DefaultCatalog.entries,
    val content: CardContent = CardContent(),
    val editMode: Boolean = false,
    val showStore: Boolean = false,
    val screenVisible: Boolean = true,
    val pendingDeeplinkCard: String? = null,
    val boardReady: Boolean = false,
    val storeQuery: String = "",
    val storeGroupId: String? = null,
    val storeDetailId: String? = null,
    val storeSearchOpen: Boolean = false,
    val revealCatalogId: String? = null,
    val noteFolderSelections: Map<String, String> = emptyMap(),
)

class HiboardViewModel(
    private val repository: BoardRepository,
    private val engines: CardEngineRegistry,
) : ViewModel() {

    private val _state = MutableStateFlow(HiboardUiState())
    val state: StateFlow<HiboardUiState> = _state.asStateFlow()
    private val loader = Executors.newSingleThreadExecutor { r -> Thread(r, "hiboard-cards") }
        .asCoroutineDispatcher()
    private var lastVisibleRefresh = 0L

    init {
        viewModelScope.launch {
            repository.closeStoredHalfRowGaps()
            repository.snapshot.collect { board ->
                _state.update { it.copy(board = board, boardReady = true) }
            }
        }
        onHostEvent(HostEvent.Create)
        viewModelScope.launch {
            engines.flashlightOn.collect { on ->
                _state.update {
                    it.copy(
                        content = it.content.copy(
                            flashlightOn = on,
                            flashlightAvailable = engines.flashlightAvailable,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            engines.batterySnapshots.collect { snap ->
                _state.update {
                    it.copy(
                        content = it.content.copy(
                            batteryPercent = snap.levelPercent,
                            batteryCharging = snap.charging,
                            batteryRemainingMs = snap.remainingMs,
                            batterySamples = snap.samples,
                            batteryReady = true,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            engines.notesRevisions.collect { refreshContent(CardAction.Bind) }
        }
        viewModelScope.launch {
            engines.noteFolderSelections.collect { selections ->
                _state.update { it.copy(noteFolderSelections = selections) }
            }
        }
        viewModelScope.launch {
            engines.weatherSnapshot.collect { refreshContent(CardAction.Bind) }
        }
        viewModelScope.launch {
            var lastState = engines.recorderStatus.value.state
            engines.recorderStatus.collect { status ->
                if (status.state != lastState) {
                    lastState = status.state
                    _state.update {
                        it.copy(
                            content = it.content.copy(
                                recorderState = status.state,
                                recorderElapsedMs = status.elapsedMs,
                                recorderBound = true,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun onHostEvent(event: HostEvent) {
        when (event) {
            HostEvent.Create -> {
                engines.refreshRecents()
                _state.update { it.copy(content = engines.compose(CardAction.Create)) }
            }
            HostEvent.Enter, HostEvent.Resume -> {
                engines.syncRecorder()
                _state.update { it.copy(screenVisible = true) }
                // Returning from an app fires Enter (onStart) and Resume back to back.
                val now = SystemClock.uptimeMillis()
                if (now - lastVisibleRefresh >= VISIBLE_REFRESH_GAP_MS) {
                    lastVisibleRefresh = now
                    refreshContent(CardAction.Visible, syncRecents = true)
                }
            }
            HostEvent.Exit, HostEvent.Pause -> {
                _state.update { it.copy(screenVisible = false) }
            }
            HostEvent.Destroy -> Unit
        }
    }

    /**
     * Re-queries card data on [loader]; contacts, notes and usage stats can take hundreds of
     * milliseconds. Flashlight, battery and recorder fields are driven by their own flows, so keep
     * the current values instead of a snapshot that may be stale by the time the query returns.
     */
    private fun refreshContent(action: CardAction, syncRecents: Boolean = false) {
        viewModelScope.launch {
            val fresh = withContext(loader) {
                if (syncRecents) engines.refreshRecents()
                engines.compose(action)
            }
            _state.update {
                it.copy(
                    content = fresh.copy(
                        flashlightOn = it.content.flashlightOn,
                        flashlightAvailable = it.content.flashlightAvailable,
                        batteryPercent = it.content.batteryPercent,
                        batteryCharging = it.content.batteryCharging,
                        batteryRemainingMs = it.content.batteryRemainingMs,
                        batterySamples = it.content.batterySamples,
                        batteryReady = it.content.batteryReady,
                        recorderState = it.content.recorderState,
                        recorderElapsedMs = it.content.recorderElapsedMs,
                        recorderBound = it.content.recorderBound,
                    ),
                )
            }
        }
    }

    override fun onCleared() {
        loader.close()
        super.onCleared()
    }

    fun toggleEdit() {
        _state.update { it.copy(editMode = !it.editMode, showStore = false) }
    }

    fun openStore() {
        _state.update {
            it.copy(
                showStore = true,
                storeQuery = "",
                storeGroupId = null,
                storeDetailId = null,
                storeSearchOpen = false,
            )
        }
    }

    fun closeStore() {
        _state.update {
            it.copy(
                showStore = false,
                editMode = false,
                storeQuery = "",
                storeGroupId = null,
                storeDetailId = null,
                storeSearchOpen = false,
            )
        }
    }

    fun setStoreQuery(query: String) {
        _state.update { if (it.storeQuery == query) it else it.copy(storeQuery = query) }
    }

    fun setStoreGroup(groupId: String?) {
        _state.update { it.copy(storeGroupId = groupId, storeDetailId = null) }
    }

    fun openStoreDetail(catalogId: String) {
        _state.update { it.copy(storeDetailId = catalogId, storeSearchOpen = false) }
    }

    fun closeStoreDetail() {
        _state.update { it.copy(storeDetailId = null) }
    }

    fun setStoreSearchOpen(open: Boolean) {
        _state.update {
            it.copy(
                storeSearchOpen = open,
                storeQuery = if (open) it.storeQuery else "",
                storeDetailId = null,
            )
        }
    }

    fun handleStoreBack(): Boolean {
        val state = _state.value
        if (!state.showStore) return false
        if (state.storeDetailId != null) {
            closeStoreDetail()
            return true
        }
        if (state.storeSearchOpen) {
            setStoreSearchOpen(false)
            return true
        }
        closeStore()
        return true
    }

    fun pinFromStore(catalogId: String) {
        val entry = DefaultCatalog.byId(catalogId) ?: return
        val keepIds = _state.updateAndGet { state ->
            val subscribed = if (state.board.subscribed.any { it.catalogId == catalogId }) {
                state.board.subscribed
            } else {
                val incoming = CardInstance(
                    instanceId = UUID.nameUUIDFromBytes("${CardArea.Subscribe}:$catalogId".toByteArray()).toString(),
                    catalogId = entry.id,
                    displayName = entry.name(HiboardApp.instance),
                    size = entry.size,
                    area = CardArea.Subscribe,
                    engine = entry.engine,
                    canDrag = !entry.locked,
                    canEdit = !entry.locked,
                    order = state.board.subscribed.size,
                )
                pinLockedCards(insertFillingEmptyTwoByTwo(state.board.subscribed, incoming))
            }
            state.copy(
                board = state.board.copy(subscribed = subscribed),
                showStore = false,
                editMode = false,
                storeQuery = "",
                storeGroupId = null,
                storeDetailId = null,
                storeSearchOpen = false,
                revealCatalogId = catalogId,
            )
        }.board.subscribed.map { it.catalogId }
        subscribe(catalogId, keepIds)
    }

    fun consumeReveal() {
        _state.update { if (it.revealCatalogId == null) it else it.copy(revealCatalogId = null) }
    }

    fun subscribe(catalogId: String, keepIds: List<String>? = null) {
        val shown = keepIds ?: _state.value.board.subscribed.map { it.catalogId }
        viewModelScope.launch { repository.subscribe(catalogId, shown) }
    }

    fun unsubscribe(catalogId: String) {
        val keepIds = _state.value.board.subscribed
            .map { it.catalogId }
            .filterNot { it == catalogId }
        _state.update { state ->
            state.copy(
                board = state.board.copy(
                    subscribed = state.board.subscribed.filterNot { it.catalogId == catalogId },
                ),
            )
        }
        viewModelScope.launch { repository.unsubscribe(catalogId, keepIds) }
        engines.setNoteFolder(catalogId, ALL_NOTES_FOLDER)
    }

    fun setNoteFolder(catalogId: String, folder: String) = engines.setNoteFolder(catalogId, folder)

    fun enterEdit() {
        _state.update { if (it.editMode) it else it.copy(editMode = true, showStore = false) }
    }

    fun exitEdit() {
        _state.update { if (!it.editMode && !it.showStore) it else it.copy(editMode = false, showStore = false) }
    }

    fun reorder(area: CardArea, catalogIds: List<String>) {
        val pinned = DefaultCatalog.pinLocked(catalogIds)
        _state.update { state ->
            val board = when (area) {
                CardArea.Subscribe -> state.board.copy(
                    subscribed = sortByCatalog(state.board.subscribed, pinned),
                )
                CardArea.Recommend -> state.board.copy(
                    recommended = sortByCatalog(state.board.recommended, catalogIds),
                )
            }
            state.copy(board = board)
        }
        val keepIds = if (area == CardArea.Subscribe) {
            _state.value.board.subscribed.map { it.catalogId }
        } else {
            emptyList()
        }
        viewModelScope.launch { repository.reorder(area, catalogIds, keepIds) }
    }

    fun consumeDeeplink() {
        _state.update { it.copy(pendingDeeplinkCard = null) }
    }

    fun applyDeeplink(cardId: String?, edit: Boolean, store: Boolean) {
        _state.update {
            it.copy(
                pendingDeeplinkCard = cardId,
                editMode = edit,
                showStore = store,
                storeQuery = "",
                storeGroupId = null,
                storeDetailId = null,
                storeSearchOpen = false,
            )
        }
        if (cardId != null && DefaultCatalog.byId(cardId) != null) {
            subscribe(cardId)
        }
    }

    fun openNotes() = engines.openNotes()

    fun openNote(noteId: Long) = engines.openNote(noteId)

    fun createNote() = engines.createNote()

    fun toggleFlashlight(): FlashlightToggle {
        val result = engines.toggleFlashlight()
        if (result == FlashlightToggle.Changed) {
            _state.update {
                it.copy(
                    content = it.content.copy(
                        flashlightOn = engines.flashlightOn.value,
                        flashlightAvailable = engines.flashlightAvailable,
                    ),
                )
            }
        }
        return result
    }

    fun openSystemManager() = engines.openSystemManager()

    fun openBattery() = engines.openBattery()

    fun sendRecorder(command: RecorderCommand): RecorderSendResult = engines.sendRecorder(command)

    fun recorderLive() = engines.recorderLive()

    fun openRecorder() = engines.openRecorder()

    fun openQuickSearch() = engines.openQuickSearch()

    fun openApp(app: ShortcutApp): Intent? {
        val intent = engines.openApp(app)
        refreshContent(CardAction.Bind)
        return intent
    }

    fun openContact(lookupUri: String): Intent? = engines.openContact(lookupUri)

    fun openContactsApp(): Intent = engines.openContactsApp()

    fun openCalendar(): Intent = engines.openCalendar()

    fun openClock(): Intent? = engines.openClock()

    private fun sortByCatalog(cards: List<CardInstance>, catalogIds: List<String>): List<CardInstance> {
        val byId = cards.associateBy { it.catalogId }
        val ordered = catalogIds.mapNotNull(byId::get)
        val rest = cards.filter { it.catalogId !in catalogIds.toSet() }
        return (ordered + rest).mapIndexed { index, card -> card.copy(order = index) }
    }

    companion object {
        private const val VISIBLE_REFRESH_GAP_MS = 1_000L

        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = HiboardApp.instance
                return HiboardViewModel(app.boardRepository, app.engines) as T
            }
        }
    }
}
