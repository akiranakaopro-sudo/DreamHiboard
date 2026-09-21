package gd.app.hiboard.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.coui.appcompat.dialog.COUIAlertDialogBuilder
import com.coui.appcompat.poplist.COUIPopupListWindow
import com.coui.appcompat.poplist.PopupListItem
import gd.app.hiboard.R
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.catalog.widgetStoreGroups
import gd.app.hiboard.catalog.widgetStoreSections
import gd.app.hiboard.databinding.ViewHiboardBinding
import gd.app.hiboard.engine.FlashlightToggle
import gd.app.hiboard.engine.RecorderCommand
import gd.app.hiboard.engine.RecorderSendResult
import gd.app.hiboard.engine.formatRecorderTime
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
import kotlin.math.roundToInt
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
    private var lastStoreSearchOpen = false
    private var cardMenu: COUIPopupListWindow? = null
    private var viewModel: HiboardViewModel? = null

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

    fun onBackPressed(): Boolean = viewModel?.handleStoreBack() == true

    fun bind(viewModel: HiboardViewModel, lifecycleOwner: LifecycleOwner) {
        this.viewModel = viewModel
        val binder = CardBinder(
            onOpenNotes = { launchIntent(this, viewModel.openNotes()) },
            onCreateNote = { launchIntent(this, viewModel.createNote()) },
            onToggleFlashlight = { toggleFlashlight(viewModel) },
            onOpenStorage = { launchIntent(this, viewModel.openSystemManager()) },
            onRecorderCommand = { command -> sendRecorder(viewModel, command) },
            recorderLive = viewModel::recorderLive,
            onOpenRecorder = { launchIntent(this, viewModel.openRecorder()) },
            onOpenApp = { launchIntent(this, viewModel.openApp(it)) },
            onRemove = viewModel::unsubscribe,
            onAdd = viewModel::subscribe,
        )
        binding.editButton.setOnClickListener { viewModel.toggleEdit() }
        binding.addButton.setOnClickListener { viewModel.openStore() }
        binding.emptyAddButton.setOnClickListener { viewModel.openStore() }
        binding.storeClose.setOnClickListener { viewModel.closeStore() }
        binding.storeSearch.setOnClickListener { viewModel.setStoreSearchOpen(true) }
        binding.storeSearchBack.setOnClickListener { viewModel.setStoreSearchOpen(false) }
        binding.storeDetailBack.setOnClickListener { viewModel.closeStoreDetail() }
        binding.storeSearchField.doAfterTextChanged { text ->
            viewModel.setStoreQuery(text?.toString().orEmpty())
        }
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

    private fun sendRecorder(viewModel: HiboardViewModel, command: RecorderCommand) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        when (val result = viewModel.sendRecorder(command)) {
            RecorderSendResult.NeedsMic -> {
                val activity = context.findActivity() ?: return
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    activity.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION)
                }
            }
            is RecorderSendResult.Marked -> {
                Toast.makeText(
                    context,
                    "${result.text}  ${formatRecorderTime(result.timeMs)}",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            RecorderSendResult.Saved -> {
                Toast.makeText(context, R.string.recorder_saved, Toast.LENGTH_SHORT).show()
            }
            RecorderSendResult.Sent, RecorderSendResult.Failed -> Unit
        }
    }

    private fun render(
        state: HiboardUiState,
        viewModel: HiboardViewModel,
        binder: CardBinder,
    ) {
        binding.boardRoot.isVisible = !state.showStore
        binding.storeRoot.isVisible = state.showStore
        binding.storeListPane.isVisible = state.showStore && state.storeDetailId == null
        binding.storeDetailPane.isVisible = state.showStore && state.storeDetailId != null
        binding.storeClose.isVisible = !state.storeSearchOpen
        binding.storeTitle.isVisible = !state.storeSearchOpen
        binding.storeSearch.isVisible = !state.storeSearchOpen
        binding.storeSearchBack.isVisible = state.storeSearchOpen
        binding.storeSearchField.isVisible = state.storeSearchOpen
        binding.storeChipScroll.isVisible = !state.storeSearchOpen
        if (state.storeSearchOpen && binding.storeSearchField.text.toString() != state.storeQuery) {
            binding.storeSearchField.setText(state.storeQuery)
            binding.storeSearchField.setSelection(state.storeQuery.length)
        }
        if (state.storeSearchOpen != lastStoreSearchOpen) {
            lastStoreSearchOpen = state.storeSearchOpen
            setStoreIme(state.storeSearchOpen)
        }
        if (!state.showStore && lastStoreSearchOpen) {
            lastStoreSearchOpen = false
            setStoreIme(false)
        }
        binding.editButton.text = context.getString(R.string.edit_done)
        binding.editButton.isVisible = state.editMode
        binding.addButton.text = context.getString(R.string.add_widget_symbol)
        binding.addButton.isVisible = true
        binding.emptyPinned.isVisible =
            state.boardReady && state.board.subscribed.none { it.canEdit }
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
        val storeKey = listOf(
            state.board.subscribed.map { it.catalogId },
            state.showStore,
            state.storeQuery,
            state.storeGroupId,
            state.storeDetailId,
            state.storeSearchOpen,
        )
        if (state.showStore && storeKey != lastStoreKey) {
            lastStoreKey = storeKey
            bindStore(state, viewModel, binder)
        }
        if (!state.showStore) lastStoreKey = null
    }

    private fun bindStore(state: HiboardUiState, viewModel: HiboardViewModel, binder: CardBinder) {
        bindStoreChips(state, viewModel)
        bindStoreList(state, viewModel)
        bindStoreDetail(state, viewModel, binder)
    }

    private fun bindStoreChips(state: HiboardUiState, viewModel: HiboardViewModel) {
        val chips = binding.storeChips
        if (chips.childCount == 0) {
            chips.addView(storeChip(context.getString(R.string.store_filter_all)) {
                viewModel.setStoreGroup(null)
            })
            widgetStoreGroups(state.catalog).forEach { (id, title) ->
                chips.addView(storeChip(title) { viewModel.setStoreGroup(id) })
            }
        }
        val selected = state.storeGroupId
        for (index in 0 until chips.childCount) {
            val chip = chips.getChildAt(index) as TextView
            val groupId = if (index == 0) null else widgetStoreGroups(state.catalog).getOrNull(index - 1)?.first
            val on = groupId == selected
            chip.setBackgroundResource(if (on) R.drawable.bg_store_chip_on else R.drawable.bg_store_chip_off)
            chip.setTextColor(context.getColor(R.color.hiboard_store_title))
        }
    }

    private fun storeChip(label: String, onClick: () -> Unit): TextView {
        val padH = (14 * resources.displayMetrics.density).toInt()
        val padV = (6 * resources.displayMetrics.density).toInt()
        val gap = (8 * resources.displayMetrics.density).toInt()
        return TextView(context).apply {
            text = label
            textSize = 14f
            setPadding(padH, padV, padH, padV)
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = gap }
            setOnClickListener { onClick() }
        }
    }

    private fun bindStoreIndex(used: Set<String>) {
        val index = binding.storeIndex
        index.removeAllViews()
        INDEX_LETTERS.filter { it in used }.forEach { letter ->
            val label = TextView(context).apply {
                text = letter
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(context.getColor(R.color.hiboard_store_title))
                setPadding(0, (1 * resources.displayMetrics.density).toInt(), 0, 0)
                setOnClickListener { scrollStoreTo(letter) }
            }
            index.addView(label)
        }
    }

    private fun bindStoreList(state: HiboardUiState, viewModel: HiboardViewModel) {
        val list = binding.storeList
        list.removeAllViews()
        val inflater = LayoutInflater.from(context)
        val sections = widgetStoreSections(state.catalog, state.storeQuery, state.storeGroupId)
        bindStoreIndex(sections.map { it.letter }.toSet())
        if (sections.isEmpty()) {
            val empty = TextView(context).apply {
                text = context.getString(R.string.store_empty)
                setTextColor(0xFF8E8E93.toInt())
                textSize = 15f
                gravity = Gravity.CENTER
                setPadding(24, 48, 24, 24)
            }
            list.addView(empty)
            return
        }
        sections.forEach { section ->
            val header = inflater.inflate(R.layout.item_widget_section, list, false) as TextView
            header.text = section.letter
            header.tag = "section-${section.letter}"
            list.addView(header)
            section.entries.forEachIndexed { entryIndex, entry ->
                list.addView(widgetRow(inflater, list, entry, viewModel))
                if (entryIndex < section.entries.lastIndex) {
                    list.addView(rowDivider())
                }
            }
        }
    }

    private fun widgetRow(
        inflater: LayoutInflater,
        parent: LinearLayout,
        entry: CardCatalogEntry,
        viewModel: HiboardViewModel,
    ): View {
        val row = inflater.inflate(R.layout.item_widget_row, parent, false)
        val icon = row.findViewById<ImageView>(R.id.widgetIcon)
        val look = widgetIcon(entry.engine)
        icon.setImageResource(look.first)
        icon.imageTintList = look.second?.let { ColorStateList.valueOf(it) }
        icon.backgroundTintList = ColorStateList.valueOf(look.third)
        row.findViewById<TextView>(R.id.widgetName).text = entry.name
        row.findViewById<TextView>(R.id.widgetCount).text = context.getString(R.string.store_widget_one)
        row.setOnClickListener { viewModel.openStoreDetail(entry.id) }
        return row
    }

    private fun rowDivider(): View {
        val start = (76 * resources.displayMetrics.density).toInt()
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (resources.displayMetrics.density).toInt().coerceAtLeast(1),
            ).apply { marginStart = start }
            setBackgroundColor(0x1A000000)
        }
    }

    private fun scrollStoreTo(letter: String) {
        val target = (0 until binding.storeList.childCount)
            .map { binding.storeList.getChildAt(it) }
            .firstOrNull { it.tag == "section-$letter" } ?: return
        binding.storeScroll.smoothScrollTo(0, target.top)
    }

    private fun bindStoreDetail(state: HiboardUiState, viewModel: HiboardViewModel, binder: CardBinder) {
        val entry = DefaultCatalog.byId(state.storeDetailId.orEmpty()) ?: return
        val added = state.board.subscribed.any { it.catalogId == entry.id }
        binding.storeDetailTitle.text = entry.name
        binding.storeDetailSize.text = context.getString(
            R.string.widget_details_size,
            entry.size.columns,
            entry.size.rows,
        )
        binding.storeDetailDesc.text = entry.description
        binding.storeDetailAdd.text = context.getString(
            if (added) R.string.store_added else R.string.subscribe,
        )
        binding.storeDetailAdd.isEnabled = !added
        binding.storeDetailAdd.setOnClickListener {
            if (!added) viewModel.pinFromStore(entry.id)
        }
        val preview = binding.storeDetailPreview
        preview.removeAllViews()
        preview.post { fillStorePreview(preview, entry, state, binder) }
    }

    private fun fillStorePreview(
        host: FrameLayout,
        entry: CardCatalogEntry,
        state: HiboardUiState,
        binder: CardBinder,
    ) {
        if (!host.isAttachedToWindow) return
        host.removeAllViews()
        val width = host.width.takeIf { it > 0 }
        if (width == null) {
            host.post { fillStorePreview(host, entry, state, binder) }
            return
        }
        val gutter = (10 * resources.displayMetrics.density).roundToInt()
        val cell = ((width - gutter * 3) / 4f).roundToInt().coerceAtLeast(1)
        val cardW = cell * entry.size.columns + gutter * (entry.size.columns - 1).coerceAtLeast(0)
        val cardH = cell * entry.size.rows + gutter * (entry.size.rows - 1).coerceAtLeast(0)
        val instance = CardInstance(
            instanceId = "preview:${entry.id}",
            catalogId = entry.id,
            displayName = entry.name,
            size = entry.size,
            area = CardArea.Subscribe,
            engine = entry.engine,
            canDrag = false,
            canEdit = false,
        )
        val card = binder.create(host, instance, state, recommend = false)
        card.layoutParams = FrameLayout.LayoutParams(cardW, cardH).apply { gravity = Gravity.CENTER_HORIZONTAL }
        host.addView(card)
        val shield = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(cardW, cardH).apply { gravity = Gravity.CENTER_HORIZONTAL }
            isClickable = true
        }
        host.addView(shield)
        host.layoutParams = host.layoutParams.apply { height = cardH }
        host.requestLayout()
    }

    private fun setStoreIme(show: Boolean) {
        val field = binding.storeSearchField
        val imm = context.getSystemService(InputMethodManager::class.java)
        if (show) {
            field.requestFocus()
            field.post { imm?.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT) }
        } else {
            imm?.hideSoftInputFromWindow(field.windowToken, 0)
            field.clearFocus()
        }
    }

    private fun widgetIcon(engine: CardEngineId): Triple<Int, Int?, Int> {
        return when (engine) {
            CardEngineId.Weather -> Triple(R.drawable.ic_weather_sunny, null, 0xFFD6ECFF.toInt())
            CardEngineId.Notes -> Triple(R.drawable.ic_notes_mark, null, context.getColor(R.color.hiboard_notes_card))
            CardEngineId.Storage -> Triple(R.drawable.ic_storage_clean, context.getColor(R.color.hiboard_storage_title), Color.WHITE)
            CardEngineId.Recorder -> Triple(R.drawable.ic_recorder_record, 0xFFE32E27.toInt(), 0xFFFFE8E6.toInt())
            CardEngineId.Flashlight -> Triple(
                R.drawable.ic_flashlight,
                context.getColor(R.color.hiboard_flashlight_icon_off),
                context.getColor(R.color.hiboard_flashlight_off),
            )
            CardEngineId.RecentApps -> Triple(R.drawable.ic_search_dark, context.getColor(R.color.hiboard_store_title), Color.WHITE)
        }
    }

    private companion object {
        const val CAMERA_PERMISSION = 42
        const val MIC_PERMISSION = 43
        val INDEX_LETTERS = (('A'..'Z') + '#').map { it.toString() }
    }
}
