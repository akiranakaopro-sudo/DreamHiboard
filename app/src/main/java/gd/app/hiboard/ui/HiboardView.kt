package gd.app.hiboard.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import gd.app.hiboard.R
import gd.app.hiboard.databinding.ViewHiboardBinding
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardCatalogEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HiboardView @JvmOverloads constructor(
    rawContext: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(couiContext(rawContext), attrs) {

    private val binding = ViewHiboardBinding.inflate(LayoutInflater.from(context), this, true)
    private var collectJob: Job? = null
    private var lastGridKey: Any? = null
    private var lastStoreKey: Any? = null
    private var hintsBound = false

    init {
        binding.searchBar.setUseResponsivePadding(false)
    }

    fun bind(viewModel: HiboardViewModel, lifecycleOwner: LifecycleOwner) {
        val binder = CardBinder(
            onOpenNotes = { launchIntent(this, viewModel.openNotes()) },
            onOpenApp = { launchIntent(this, viewModel.openApp(it)) },
            onRemove = viewModel::unsubscribe,
            onAdd = viewModel::subscribe,
        )
        binding.editButton.setOnClickListener { viewModel.toggleEdit() }
        binding.addButton.setOnClickListener { viewModel.openStore() }
        binding.emptyAddButton.setOnClickListener { viewModel.openStore() }
        binding.storeClose.setOnClickListener { viewModel.closeStore() }
        binding.subscribedGrid.onDragStarted = { viewModel.enterEdit() }
        binding.subscribedGrid.onReorder = { viewModel.reorder(CardArea.Subscribe, it) }
        binding.subscribedGrid.onDragEnded = {
            lastGridKey = null
            viewModel.exitEdit()
        }

        collectJob?.cancel()
        collectJob = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state, viewModel, binder)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        collectJob?.cancel()
        super.onDetachedFromWindow()
    }

    private fun render(
        state: HiboardUiState,
        viewModel: HiboardViewModel,
        binder: CardBinder,
    ) {
        binding.boardRoot.isVisible = !state.showStore
        binding.storeRoot.isVisible = state.showStore
        if (!hintsBound && state.headerHints.isNotEmpty()) {
            hintsBound = true
            binding.searchBar.hintAnimationLayout.setHintsAnimation(state.headerHints)
        }
        binding.editButton.text = context.getString(R.string.edit_done)
        binding.editButton.isVisible = state.editMode
        binding.addButton.text = context.getString(R.string.add_widget_symbol)
        binding.addButton.isVisible = true
        binding.emptyPinned.isVisible = state.board.subscribed.isEmpty()
        binding.subscribedGrid.isVisible = state.board.subscribed.isNotEmpty()
        val dragging = binding.subscribedGrid.isDragging
        val gridKey = listOf(state.board, state.editMode, state.content)
        if (!dragging && gridKey != lastGridKey) {
            lastGridKey = gridKey
            binding.subscribedGrid.setCards(state.board.subscribed) { card ->
                binder.create(binding.subscribedGrid, card, state, recommend = false)
            }
        }
        val storeKey = state.board.subscribed.map { it.catalogId } to state.showStore
        if (state.showStore && storeKey != lastStoreKey) {
            lastStoreKey = storeKey
            bindStore(state, viewModel)
        }
    }

    private fun bindStore(state: HiboardUiState, viewModel: HiboardViewModel) {
        val list = binding.storeList
        list.removeAllViews()
        val pinned = state.board.subscribed.map { it.catalogId }.toSet()
        val inflater = LayoutInflater.from(context)
        state.catalog.forEach { entry ->
            list.addView(storeRow(inflater, list, entry, entry.id in pinned, viewModel))
        }
    }

    private fun storeRow(
        inflater: android.view.LayoutInflater,
        parent: LinearLayout,
        entry: CardCatalogEntry,
        added: Boolean,
        viewModel: HiboardViewModel,
    ): View {
        val row = inflater.inflate(R.layout.item_store_card, parent, false)
        row.findViewById<TextView>(R.id.storeName).text = entry.name
        row.findViewById<TextView>(R.id.storeDesc).text = entry.description
        val action = row.findViewById<TextView>(R.id.storeAction)
        action.text = if (added) context.getString(R.string.unsubscribe) else context.getString(R.string.subscribe)
        action.setOnClickListener {
            if (added) viewModel.unsubscribe(entry.id) else viewModel.subscribe(entry.id)
        }
        return row
    }
}
