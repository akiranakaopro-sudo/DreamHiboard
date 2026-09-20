package gd.app.hiboard.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gd.app.hiboard.HiboardApp
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.data.BoardRepository
import gd.app.hiboard.engine.CardEngineRegistry
import gd.app.hiboard.host.HostEvent
import gd.app.hiboard.model.BoardSnapshot
import gd.app.hiboard.model.CardAction
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardContent
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.ShortcutApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HiboardUiState(
    val board: BoardSnapshot = BoardSnapshot(emptyList(), emptyList()),
    val catalog: List<CardCatalogEntry> = DefaultCatalog.entries,
    val content: CardContent = CardContent(),
    val editMode: Boolean = false,
    val showStore: Boolean = false,
    val screenVisible: Boolean = true,
    val pendingDeeplinkCard: String? = null,
)

class HiboardViewModel(
    private val repository: BoardRepository,
    private val engines: CardEngineRegistry,
) : ViewModel() {

    private val _state = MutableStateFlow(HiboardUiState())
    val state: StateFlow<HiboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.snapshot.collect { board ->
                _state.update { it.copy(board = board) }
            }
        }
        onHostEvent(HostEvent.Create)
        viewModelScope.launch {
            engines.flashlightOn.collect {
                _state.update { it.copy(content = engines.compose(CardAction.Bind)) }
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
                engines.refreshRecents()
                _state.update {
                    it.copy(
                        screenVisible = true,
                        content = engines.compose(CardAction.Visible),
                    )
                }
            }
            HostEvent.Exit, HostEvent.Pause -> {
                _state.update {
                    it.copy(
                        screenVisible = false,
                        content = engines.compose(CardAction.Hidden),
                    )
                }
            }
            HostEvent.Destroy -> {
                engines.compose(CardAction.Destroy)
            }
        }
    }

    fun toggleEdit() {
        _state.update { it.copy(editMode = !it.editMode, showStore = false) }
    }

    fun openStore() {
        _state.update { it.copy(showStore = true) }
    }

    fun closeStore() {
        _state.update { it.copy(showStore = false, editMode = false) }
    }

    fun subscribe(catalogId: String) {
        viewModelScope.launch { repository.subscribe(catalogId) }
    }

    fun unsubscribe(catalogId: String) {
        viewModelScope.launch { repository.unsubscribe(catalogId) }
    }

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
        viewModelScope.launch { repository.reorder(area, catalogIds) }
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
            )
        }
        if (cardId != null && DefaultCatalog.byId(cardId) != null) {
            subscribe(cardId)
        }
    }

    fun openNotes() = engines.openNotes()

    fun createNote() = engines.createNote()

    fun toggleFlashlight() = engines.toggleFlashlight()

    fun openQuickSearch() = engines.openQuickSearch()

    fun openApp(app: ShortcutApp): Intent? {
        val intent = engines.openApp(app)
        _state.update { it.copy(content = engines.compose(CardAction.Bind)) }
        return intent
    }

    private fun sortByCatalog(cards: List<CardInstance>, catalogIds: List<String>): List<CardInstance> {
        val byId = cards.associateBy { it.catalogId }
        val ordered = catalogIds.mapNotNull(byId::get)
        val rest = cards.filter { it.catalogId !in catalogIds.toSet() }
        return (ordered + rest).mapIndexed { index, card -> card.copy(order = index) }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = HiboardApp.instance
                return HiboardViewModel(app.boardRepository, app.engines) as T
            }
        }
    }
}
