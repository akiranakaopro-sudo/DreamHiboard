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
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.coui.appcompat.animation.COUIEaseInterpolator
import com.coui.appcompat.dialog.COUIAlertDialogBuilder
import com.coui.appcompat.poplist.COUIPopupListWindow
import com.coui.appcompat.poplist.PopupListItem
import com.coui.appcompat.searchview.COUISearchBar
import gd.app.hiboard.R
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.catalog.widgetStoreSections
import gd.app.hiboard.catalog.widgetStoreTabIndex
import gd.app.hiboard.catalog.widgetStoreTabs
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
    private var storeSheetOpen = false
    private var storePagerAdapter: StorePagerAdapter? = null
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
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            binding.boardRoot.updatePadding(bottom = bars.bottom)
            binding.storeListPane.updatePadding(bottom = bars.bottom)
            binding.storeDetailPane.updatePadding(bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
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
        binding.storeDetailBack.setOnClickListener { viewModel.closeStoreDetail() }
        bindStoreSearchBar(viewModel)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
    }

    override fun onDetachedFromWindow() {
        setStoreNavBarContrast(false)
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
        animateStoreSheet(state.showStore)
        if (state.showStore) {
            binding.storeListPane.isVisible = state.storeDetailId == null
            binding.storeDetailPane.isVisible = state.storeDetailId != null
            binding.storeClose.isVisible = !state.storeSearchOpen
            binding.storeTitle.isVisible = !state.storeSearchOpen
            binding.storeSearch.isVisible = !state.storeSearchOpen
            binding.storeSearchBar.isVisible = state.storeSearchOpen
            binding.storeChips.isVisible = !state.storeSearchOpen
            binding.storePager.isVisible = !state.storeSearchOpen
            binding.storePager.isUserInputEnabled = !state.storeSearchOpen
            binding.storeSearchPane.isVisible = state.storeSearchOpen
            if (state.storeSearchOpen) {
                val field = binding.storeSearchBar.searchEditText
                if (field.text.toString() != state.storeQuery) {
                    field.setText(state.storeQuery)
                    field.setSelection(state.storeQuery.length)
                }
            }
        }
        if (state.storeSearchOpen != lastStoreSearchOpen) {
            lastStoreSearchOpen = state.storeSearchOpen
            syncStoreSearchBar(state.storeSearchOpen)
        }
        if (!state.showStore && lastStoreSearchOpen) {
            lastStoreSearchOpen = false
            syncStoreSearchBar(false)
        }
        binding.editButton.text = context.getString(R.string.edit_done)
        binding.editButton.isVisible = state.editMode
        binding.addButton.text = context.getString(R.string.add_widget_symbol)
        binding.addButton.isVisible = true
        binding.emptyPinned.isVisible =
            state.boardReady && state.board.subscribed.none { it.canEdit }
        binding.subscribedGrid.isVisible = state.board.subscribed.isNotEmpty()
        binding.recentAppsHeader.isVisible =
            state.board.subscribed.any { it.engine == CardEngineId.RecentApps }
        val dragging = binding.subscribedGrid.isDragging
        val gridKey = listOf(state.board, state.editMode, state.content)
        if (!dragging && gridKey != lastGridKey) {
            lastGridKey = gridKey
            binding.subscribedGrid.setCards(state.board.subscribed) { card ->
                binder.create(binding.subscribedGrid, card, state, recommend = false)
            }
        }
        if (!state.showStore) {
            state.revealCatalogId?.let { catalogId ->
                binding.subscribedGrid.post { scrollBoardTo(catalogId) }
                viewModel.consumeReveal()
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

    private fun animateStoreSheet(show: Boolean) {
        val sheet = binding.storeRoot
        if (show == storeSheetOpen) return
        storeSheetOpen = show
        sheet.animate().cancel()
        val distance = sheet.height.takeIf { it > 0 }?.toFloat()
            ?: height.takeIf { it > 0 }?.toFloat()
            ?: resources.displayMetrics.heightPixels.toFloat()
        val ease = COUIEaseInterpolator()
        setStoreNavBarContrast(show)
        if (show) {
            if (sheet.translationY == 0f) sheet.translationY = distance
            sheet.isVisible = true
            fun slideUp() {
                if (!storeSheetOpen) return
                val from = sheet.height.takeIf { it > 0 }?.toFloat() ?: distance
                if (sheet.translationY == 0f) sheet.translationY = from
                sheet.animate()
                    .translationY(0f)
                    .setDuration(STORE_SLIDE_IN_MS)
                    .setInterpolator(ease)
                    .start()
            }
            if (sheet.height == 0) sheet.post { slideUp() } else slideUp()
        } else {
            sheet.animate()
                .translationY(distance)
                .setDuration(STORE_SLIDE_OUT_MS)
                .setInterpolator(ease)
                .withEndAction {
                    if (storeSheetOpen) return@withEndAction
                    sheet.isVisible = false
                    sheet.translationY = 0f
                }
                .start()
        }
    }

    private fun bindStore(state: HiboardUiState, viewModel: HiboardViewModel, binder: CardBinder) {
        bindStorePager(state)
        bindStoreChips(state)
        if (state.storeSearchOpen) bindStoreSearchList(state, viewModel)
        bindStoreDetail(state, viewModel, binder)
    }

    private fun bindStorePager(state: HiboardUiState) {
        val adapter = storePagerAdapter ?: StorePagerAdapter().also {
            storePagerAdapter = it
            binding.storePager.offscreenPageLimit = widgetStoreTabs().size.coerceAtLeast(1)
            binding.storePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val vm = this@HiboardView.viewModel ?: return
                    val id = widgetStoreTabs().getOrNull(position)?.first
                    if (vm.state.value.storeGroupId != id) vm.setStoreGroup(id)
                }
            })
            binding.storePager.adapter = it
        }
        adapter.submit(state.catalog)
        val target = widgetStoreTabIndex(state.storeGroupId)
        if (binding.storePager.currentItem != target) {
            binding.storePager.setCurrentItem(target, false)
        }
    }

    private fun bindStoreChips(state: HiboardUiState) {
        val chips = binding.storeChips
        val tabs = widgetStoreTabs()
        if (chips.childCount != tabs.size) {
            chips.removeAllViews()
            tabs.forEachIndexed { index, (id, _) ->
                val title = context.getString(
                    when (id) {
                        DefaultCatalog.GROUP_FEATURES -> R.string.store_filter_features
                        DefaultCatalog.GROUP_WEATHER -> R.string.store_filter_weather
                        else -> R.string.store_filter_all
                    },
                )
                chips.addView(storeChip(title, last = index == tabs.lastIndex) {
                    binding.storePager.setCurrentItem(index, true)
                })
            }
        }
        val selected = state.storeGroupId
        for (index in 0 until chips.childCount) {
            val chip = chips.getChildAt(index) as TextView
            val on = tabs.getOrNull(index)?.first == selected
            chip.setBackgroundResource(if (on) R.drawable.bg_store_chip_on else R.drawable.bg_store_chip_off)
            chip.setTextColor(context.getColor(R.color.hiboard_store_title))
        }
    }

    private fun storeChip(label: String, last: Boolean, onClick: () -> Unit): TextView {
        val density = resources.displayMetrics.density
        val padV = (11 * density).toInt()
        val gap = (8 * density).toInt()
        return TextView(context).apply {
            text = label
            textSize = 14f
            gravity = Gravity.CENTER
            maxLines = 1
            includeFontPadding = false
            minHeight = (40 * density).toInt()
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, padV, 0, padV)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (!last) marginEnd = gap
            }
            setOnClickListener { onClick() }
        }
    }

    private fun bindStoreIndex(
        indexBar: LinearLayout,
        list: LinearLayout,
        scroll: ScrollView,
        used: Set<String>,
    ) {
        indexBar.removeAllViews()
        val density = resources.displayMetrics.density
        INDEX_LETTERS.forEach { letter ->
            val label = TextView(context).apply {
                text = letter
                textSize = 13f
                gravity = Gravity.CENTER
                includeFontPadding = false
                minWidth = (18 * density).toInt()
                setTextColor(
                    if (letter in used) context.getColor(R.color.hiboard_store_title)
                    else 0xFF8E8E93.toInt(),
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setPadding(0, 0, 0, 0)
                setOnClickListener { scrollStoreTo(list, scroll, letter) }
            }
            indexBar.addView(label)
        }
    }

    private fun bindStoreSearchList(state: HiboardUiState, viewModel: HiboardViewModel) {
        fillStoreSections(
            list = binding.storeList,
            indexBar = binding.storeIndex,
            scroll = binding.storeScroll,
            catalog = state.catalog,
            query = state.storeQuery,
            groupId = null,
            viewModel = viewModel,
        )
    }

    private fun fillStoreSections(
        list: LinearLayout,
        indexBar: LinearLayout,
        scroll: ScrollView,
        catalog: List<CardCatalogEntry>,
        query: String,
        groupId: String?,
        viewModel: HiboardViewModel,
    ) {
        list.removeAllViews()
        val inflater = LayoutInflater.from(context)
        val sections = widgetStoreSections(catalog, query, groupId)
        bindStoreIndex(indexBar, list, scroll, sections.map { it.letter }.toSet())
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

    private fun scrollStoreTo(list: LinearLayout, scroll: ScrollView, letter: String) {
        val target = (0 until list.childCount)
            .map { list.getChildAt(it) }
            .firstOrNull { it.tag == "section-$letter" } ?: return
        scroll.smoothScrollTo(0, target.top)
    }

    private fun scrollBoardTo(catalogId: String) {
        val card = binding.subscribedGrid.findCard(catalogId) ?: return
        val y = (binding.subscribedGrid.top + card.top).coerceAtLeast(0)
        binding.boardScroll.smoothScrollTo(0, y)
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

    private fun setStoreNavBarContrast(storeOpen: Boolean) {
        val window = context.findActivity()?.window ?: return
        WindowInsetsControllerCompat(window, this).isAppearanceLightNavigationBars = storeOpen
    }

    private fun bindStoreSearchBar(viewModel: HiboardViewModel) {
        val bar = binding.storeSearchBar
        bar.setUseResponsivePadding(false)
        bar.setSearchAnimateType(COUISearchBar.TYPE_NON_INSTANT_SEARCH)
        bar.searchEditText.doAfterTextChanged { text ->
            viewModel.setStoreQuery(text?.toString().orEmpty())
        }
        bar.functionalButton?.setOnClickListener { viewModel.setStoreSearchOpen(false) }
        bar.addOnStateChangeListener { from, to ->
            if (from == COUISearchBar.STATE_EDIT &&
                to == COUISearchBar.STATE_NORMAL &&
                viewModel.state.value.storeSearchOpen
            ) {
                viewModel.setStoreSearchOpen(false)
            }
        }
    }

    private fun syncStoreSearchBar(open: Boolean) {
        val bar = binding.storeSearchBar
        if (open) {
            bar.post {
                if (this.viewModel?.state?.value?.storeSearchOpen != true) return@post
                if (bar.searchState != COUISearchBar.STATE_EDIT) {
                    bar.changeState(COUISearchBar.STATE_EDIT, true)
                }
            }
        } else if (bar.searchState != COUISearchBar.STATE_NORMAL) {
            bar.changeState(COUISearchBar.STATE_NORMAL, false)
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

    private inner class StorePagerAdapter : RecyclerView.Adapter<StorePagerAdapter.Holder>() {
        private val tabs = widgetStoreTabs()
        private var catalog: List<CardCatalogEntry> = emptyList()

        fun submit(entries: List<CardCatalogEntry>) {
            if (catalog == entries) return
            catalog = entries
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store_page, parent, false)
            view.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val vm = viewModel ?: return
            fillStoreSections(
                list = holder.list,
                indexBar = holder.index,
                scroll = holder.scroll,
                catalog = catalog,
                query = "",
                groupId = tabs.getOrNull(position)?.first,
                viewModel = vm,
            )
        }

        override fun getItemCount(): Int = tabs.size

        inner class Holder(root: View) : RecyclerView.ViewHolder(root) {
            val scroll: ScrollView = root.findViewById(R.id.pageScroll)
            val list: LinearLayout = root.findViewById(R.id.pageList)
            val index: LinearLayout = root.findViewById(R.id.pageIndex)
        }
    }

    private companion object {
        const val CAMERA_PERMISSION = 42
        const val MIC_PERMISSION = 43
        const val STORE_SLIDE_IN_MS = 360L
        const val STORE_SLIDE_OUT_MS = 280L
        val INDEX_LETTERS = (('A'..'Z') + '#').map { it.toString() }
    }
}
