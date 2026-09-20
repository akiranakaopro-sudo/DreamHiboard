package gd.app.hiboard.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.coui.appcompat.dialog.COUIAlertDialogBuilder
import com.coui.appcompat.poplist.COUIPopupListWindow
import com.coui.appcompat.poplist.PopupListItem
import gd.app.hiboard.R
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.databinding.ViewHiboardBinding
import gd.app.hiboard.engine.FlashlightToggle
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
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
    private var cardMenu: COUIPopupListWindow? = null

    init {
        binding.searchBar.setUseResponsivePadding(false)
        val chrome = context.getColor(R.color.hiboard_chrome)
        binding.searchBar.searchEditText.hint = context.getString(R.string.header_search_hint)
        binding.searchBar.searchEditText.setHintTextColor(context.getColor(R.color.hiboard_chrome_hint))
        binding.searchBar.searchEditText.setTextColor(chrome)
        binding.searchBar.setSearchViewIcon(context.getDrawable(R.drawable.ic_search_chrome))
        binding.searchBar.setSearchBackgroundColor(
            ColorStateList.valueOf(context.getColor(R.color.hiboard_chrome_fill)),
        )
        binding.addButton.setTextColor(chrome)
        binding.addButton.setDrawableColor(context.getColor(R.color.hiboard_chrome_fill))
    }

    fun bind(viewModel: HiboardViewModel, lifecycleOwner: LifecycleOwner) {
        val binder = CardBinder(
            onOpenNotes = { launchIntent(this, viewModel.openNotes()) },
            onCreateNote = { launchIntent(this, viewModel.createNote()) },
            onToggleFlashlight = { toggleFlashlight(viewModel) },
            onOpenStorage = { launchIntent(this, viewModel.openSystemManager()) },
            onOpenApp = { launchIntent(this, viewModel.openApp(it)) },
            onRemove = viewModel::unsubscribe,
            onAdd = viewModel::subscribe,
        )
        binding.editButton.setOnClickListener { viewModel.toggleEdit() }
        binding.addButton.setOnClickListener { viewModel.openStore() }
        binding.emptyAddButton.setOnClickListener { viewModel.openStore() }
        binding.storeClose.setOnClickListener { viewModel.closeStore() }
        binding.searchBar.setInputMethodAnimationEnabled(false)
        binding.searchBar.searchEditText.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isCursorVisible = false
            keyListener = null
        }
        binding.searchBar.setOnClickListener {
            launchIntent(this, viewModel.openQuickSearch())
        }
        binding.subscribedGrid.onReorder = { viewModel.reorder(CardArea.Subscribe, it) }
        binding.subscribedGrid.onAddSlotClick = { viewModel.openStore() }
        binding.subscribedGrid.onCardLongPress = { card, anchor ->
            showCardMenu(card, anchor, viewModel)
        }
        binding.subscribedGrid.onDragStarted = { dismissCardMenu() }
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
        dismissCardMenu()
        collectJob?.cancel()
        super.onDetachedFromWindow()
    }

    private fun showCardMenu(card: CardInstance, anchor: View, viewModel: HiboardViewModel) {
        dismissCardMenu()
        val popup = COUIPopupListWindow(context)
        popup.setItemList(
            listOf(
                PopupListItem.Builder()
                    .setTitle(context.getString(R.string.remove_widget))
                    .setIcon(ContextCompat.getDrawable(context, R.drawable.ic_widget_remove))
                    .setForceTint(PopupListItem.MENU_ITEM_FORCE_TINT_NONE)
                    .build(),
                PopupListItem.Builder()
                    .setTitle(context.getString(R.string.widget_details))
                    .setIcon(ContextCompat.getDrawable(context, R.drawable.ic_widget_info))
                    .setForceTint(PopupListItem.MENU_ITEM_FORCE_TINT_NONE)
                    .build(),
            ),
        )
        popup.setOnItemClickListener { _, _, position, _ ->
            popup.dismiss()
            when (position) {
                0 -> viewModel.unsubscribe(card.catalogId)
                1 -> showWidgetDetails(card)
            }
        }
        popup.setOnDismissListener { if (cardMenu === popup) cardMenu = null }
        cardMenu = popup
        fun present() {
            if (cardMenu !== popup || !anchor.isAttachedToWindow || anchor.windowToken == null) return
            popup.show(anchor)
        }
        if (anchor.windowToken != null) {
            present()
        } else {
            anchor.post { present() }
        }
    }

    private fun showWidgetDetails(card: CardInstance) {
        val entry = DefaultCatalog.byId(card.catalogId) ?: return
        val sizeLine = context.getString(
            R.string.widget_details_size,
            entry.size.columns,
            entry.size.rows,
        )
        val body = listOf(entry.description, sizeLine).filter { it.isNotBlank() }.joinToString("\n")
        COUIAlertDialogBuilder(context)
            .setTitle(entry.name)
            .setMessage(body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun dismissCardMenu() {
        cardMenu?.dismiss()
        cardMenu = null
    }

    private fun toggleFlashlight(viewModel: HiboardViewModel) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        when (viewModel.toggleFlashlight()) {
            FlashlightToggle.NeedsCamera -> {
                val activity = context.findActivity() ?: return
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    activity.requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
                }
            }
            FlashlightToggle.Changed, FlashlightToggle.Unavailable -> Unit
        }
    }

    private companion object {
        const val CAMERA_PERMISSION = 42
    }

    private fun render(
        state: HiboardUiState,
        viewModel: HiboardViewModel,
        binder: CardBinder,
    ) {
        binding.boardRoot.isVisible = !state.showStore
        binding.storeRoot.isVisible = state.showStore
        binding.editButton.text = context.getString(R.string.edit_done)
        binding.editButton.isVisible = state.editMode
        binding.addButton.text = context.getString(R.string.add_widget_symbol)
        binding.addButton.isVisible = true
        binding.emptyPinned.isVisible = state.board.subscribed.none { it.canEdit }
        binding.subscribedGrid.isVisible = state.board.subscribed.isNotEmpty()
        binding.recentAppsHeader.isVisible =
            !state.showStore && state.board.subscribed.any { it.engine == CardEngineId.RecentApps }
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
        state.catalog.filter { !it.locked }.forEach { entry ->
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
