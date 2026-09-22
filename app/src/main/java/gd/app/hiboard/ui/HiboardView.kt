package gd.app.hiboard.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
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
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.coui.appcompat.animation.COUIEaseInterpolator
import com.coui.appcompat.animation.COUIMoveEaseInterpolator
import com.coui.appcompat.dialog.COUIAlertDialogBuilder
import com.coui.appcompat.poplist.COUIPopupListWindow
import com.coui.appcompat.poplist.PopupListItem
import com.coui.appcompat.searchview.COUISearchBar
import gd.app.hiboard.R
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.catalog.WidgetStoreCategory
import gd.app.hiboard.catalog.listCategory
import gd.app.hiboard.catalog.widgetStoreCategories
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
    private var storeDetailOpen = false
    private var openedDetailId: String? = null
    private var storeDetailPick: String? = null
    private var detailMemberKey: String? = null
    private var storePeekAnimator: ValueAnimator? = null
    private var storeSheetDragging = false
    private var storeSheetDragDownY = 0f
    private var storeSheetDragStartTy = 0f
    private var storeSheetVelocity: VelocityTracker? = null
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
            binding.boardScroll.updatePadding(bottom = bars.bottom)
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
            onOpenContact = { launchIntent(this, viewModel.openContact(it)) },
            onOpenContacts = { launchIntent(this, viewModel.openContactsApp()) },
            onAllowContacts = { requestContacts() },
            onOpenCalendar = { launchIntent(this, viewModel.openCalendar()) },
            onOpenClock = { launchIntent(this, viewModel.openClock()) },
            onRemove = viewModel::unsubscribe,
            onAdd = viewModel::subscribe,
        )
        binding.editButton.setOnClickListener { viewModel.toggleEdit() }
        binding.addButton.setOnClickListener { viewModel.openStore() }
        binding.emptyAddButton.setOnClickListener { viewModel.openStore() }
        binding.storeClose.setOnClickListener { viewModel.closeStore() }
        binding.storeSearch.setOnClickListener { viewModel.setStoreSearchOpen(true) }
        binding.storeDetailBack.setOnClickListener { viewModel.closeStoreDetail() }
        binding.storeRoot.setOnClickListener {
            if (viewModel.state.value.storeDetailId != null) viewModel.closeStore()
        }
        binding.storeListPane.isClickable = true
        binding.storeDetailPane.isClickable = true
        bindStoreSheetDrag(viewModel)
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

    private fun requestContacts() {
        val activity = context.findActivity() ?: return
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        activity.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION)
    }

    private fun render(
        state: HiboardUiState,
        viewModel: HiboardViewModel,
        binder: CardBinder,
    ) {
        animateStoreSheet(state.showStore)
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
            bindStore(state, viewModel)
        }
        if (!state.showStore) lastStoreKey = null
        if (state.showStore) {
            animateStoreDetail(state.storeDetailId != null)
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
    }

    private fun bindStoreSheetDrag(viewModel: HiboardViewModel) {
        val title = binding.storeTitle
        val sheet = binding.storeRoot
        val slop = ViewConfiguration.get(context).scaledTouchSlop
        title.setOnTouchListener { _, event ->
            if (!title.isVisible || !storeSheetOpen) return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    storeSheetDragDownY = event.rawY
                    storeSheetDragStartTy = sheet.translationY
                    storeSheetDragging = false
                    storeSheetVelocity?.recycle()
                    storeSheetVelocity = VelocityTracker.obtain().also { it.addMovement(event) }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    storeSheetVelocity?.addMovement(event)
                    val dy = event.rawY - storeSheetDragDownY
                    if (!storeSheetDragging && dy > slop) {
                        storeSheetDragging = true
                        sheet.animate().cancel()
                        title.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    if (storeSheetDragging) {
                        sheet.translationY = (storeSheetDragStartTy + dy).coerceAtLeast(0f)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    storeSheetVelocity?.addMovement(event)
                    storeSheetVelocity?.computeCurrentVelocity(1000)
                    val velocityY = storeSheetVelocity?.yVelocity ?: 0f
                    storeSheetVelocity?.recycle()
                    storeSheetVelocity = null
                    title.parent.requestDisallowInterceptTouchEvent(false)
                    val dragged = storeSheetDragging
                    storeSheetDragging = false
                    if (!dragged) return@setOnTouchListener true
                    val distance = sheet.height.takeIf { it > 0 }?.toFloat()
                        ?: height.takeIf { it > 0 }?.toFloat()
                        ?: resources.displayMetrics.heightPixels.toFloat()
                    val dismiss = event.actionMasked == MotionEvent.ACTION_UP &&
                        (velocityY > STORE_DISMISS_VELOCITY ||
                            sheet.translationY > distance * STORE_DISMISS_FRACTION)
                    if (dismiss) {
                        viewModel.closeStore()
                        if (storeSheetOpen) animateStoreSheet(false)
                    } else {
                        snapStoreSheet()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapStoreSheet() {
        val sheet = binding.storeRoot
        if (!storeSheetOpen) return
        sheet.animate().cancel()
        sheet.animate()
            .translationY(0f)
            .setDuration(STORE_SLIDE_IN_MS)
            .setInterpolator(COUIEaseInterpolator())
            .start()
    }

    private fun animateStoreSheet(show: Boolean) {
        val sheet = binding.storeRoot
        if (show == storeSheetOpen) return
        storeSheetOpen = show
        storeSheetDragging = false
        sheet.animate().cancel()
        storePeekAnimator?.cancel()
        val distance = sheet.height.takeIf { it > 0 }?.toFloat()
            ?: height.takeIf { it > 0 }?.toFloat()
            ?: resources.displayMetrics.heightPixels.toFloat()
        val ease = COUIEaseInterpolator()
        setStoreNavBarContrast(show)
        if (show) {
            storeDetailOpen = false
            resetStorePanes(showDetail = false)
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
                    storeDetailOpen = false
                    resetStorePanes(showDetail = false)
                    sheet.isVisible = false
                    sheet.translationY = 0f
                }
                .start()
        }
    }

    private fun bindStore(state: HiboardUiState, viewModel: HiboardViewModel) {
        bindStorePager(state)
        bindStoreChips(state)
        if (state.storeSearchOpen) bindStoreSearchList(state, viewModel)
        bindStoreDetail(state, viewModel)
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
            binding.storePager.clipChildren = false
            binding.storePager.clipToPadding = false
            binding.storePager.adapter = it
            binding.storePager.post {
                (binding.storePager.getChildAt(0) as? RecyclerView)?.apply {
                    clipChildren = false
                    clipToPadding = false
                    val slop = ViewConfiguration.get(context).scaledTouchSlop * 6
                    val field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
                    field.isAccessible = true
                    if (field.getInt(this) < slop) field.setInt(this, slop)
                }
            }
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
        val density = resources.displayMetrics.density
        list.setPadding(0, 0, (36 * density).toInt(), (24 * density).toInt())
        indexBar.isVisible = true
        val inflater = LayoutInflater.from(context)
        val sections = widgetStoreCategories(catalog, query, groupId)
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
            section.categories.forEachIndexed { entryIndex, category ->
                list.addView(widgetRow(inflater, list, category, viewModel))
                if (entryIndex < section.categories.lastIndex) {
                    list.addView(rowDivider())
                }
            }
        }
    }

    private fun widgetRow(
        inflater: LayoutInflater,
        parent: LinearLayout,
        category: WidgetStoreCategory,
        viewModel: HiboardViewModel,
    ): View {
        val entry = category.entries.first()
        val row = inflater.inflate(R.layout.item_widget_row, parent, false)
        val icon = row.findViewById<ImageView>(R.id.widgetIcon)
        val look = widgetIcon(entry.engine)
        icon.setImageResource(look.first)
        icon.imageTintList = look.second?.let { ColorStateList.valueOf(it) }
        icon.backgroundTintList = ColorStateList.valueOf(look.third)
        row.findViewById<TextView>(R.id.widgetName).text = category.name
        val count = category.entries.size
        row.findViewById<TextView>(R.id.widgetCount).text = if (count == 1) {
            context.getString(R.string.store_widget_one)
        } else {
            context.getString(R.string.store_widget_many, count)
        }
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

    private fun bindStoreDetail(state: HiboardUiState, viewModel: HiboardViewModel) {
        val focus = DefaultCatalog.byId(state.storeDetailId.orEmpty())
        if (focus == null) {
            openedDetailId = null
            storeDetailPick = null
            detailMemberKey = null
            binding.storeDetailIndicator.isVisible = false
            return
        }
        val members = state.catalog.filter { !it.locked && it.listCategory() == focus.listCategory() }
        if (members.isEmpty()) return
        if (state.storeDetailId != openedDetailId) {
            openedDetailId = state.storeDetailId
            storeDetailPick = state.storeDetailId
        }
        val picked = members.firstOrNull { it.id == storeDetailPick } ?: members.first()
        storeDetailPick = picked.id
        applyDetailSelection(picked, viewModel)
        binding.storeDetailIndicator.isVisible = members.size > 1
        val memberKey = members.joinToString(",") { it.id }
        val host = binding.storeDetailPreview
        if (memberKey != detailMemberKey || host.childCount == 0) {
            detailMemberKey = memberKey
            if (members.size == 1) {
                fillStoreDetailPreview(members.first())
            } else {
                fillStoreDetailPager(members, members.indexOf(picked).coerceAtLeast(0))
            }
        }
    }

    private fun applyDetailSelection(entry: CardCatalogEntry, viewModel: HiboardViewModel) {
        val added = viewModel.state.value.board.subscribed.any { it.catalogId == entry.id }
        binding.storeDetailTitle.text = entry.groupTitle
        binding.storeDetailHeadline.text = entry.name
        binding.storeDetailDesc.text = entry.description
        binding.storeDetailAdd.text = context.getString(
            if (added) R.string.store_added else R.string.store_add_to_board,
        )
        binding.storeDetailAdd.isEnabled = !added
        binding.storeDetailAdd.setOnClickListener {
            if (!added) viewModel.pinFromStore(entry.id)
        }
    }

    private fun animateStoreDetail(showDetail: Boolean) {
        if (showDetail == storeDetailOpen) return
        if (!storeSheetOpen) {
            storeDetailOpen = false
            resetStorePanes(showDetail = false)
            return
        }
        storeDetailOpen = showDetail
        val list = binding.storeListPane
        val detail = binding.storeDetailPane
        list.animate().cancel()
        detail.animate().cancel()
        val ease = COUIMoveEaseInterpolator()
        if (showDetail) {
            detail.alpha = 0f
            list.alpha = 1f
        } else {
            list.alpha = 0f
            detail.alpha = 1f
        }
        list.isVisible = true
        detail.isVisible = true
        if (showDetail) {
            list.animate()
                .alpha(0f)
                .setStartDelay(0)
                .setDuration(STORE_FADE_MS)
                .setInterpolator(ease)
                .withEndAction {
                    if (!storeDetailOpen) return@withEndAction
                    list.isVisible = false
                    list.alpha = 1f
                }
                .start()
            detail.animate()
                .alpha(1f)
                .setStartDelay(STORE_FADE_DELAY_MS)
                .setDuration(STORE_FADE_MS)
                .setInterpolator(ease)
                .start()
        } else {
            list.animate()
                .alpha(1f)
                .setStartDelay(STORE_FADE_DELAY_MS)
                .setDuration(STORE_FADE_MS)
                .setInterpolator(ease)
                .start()
            detail.animate()
                .alpha(0f)
                .setStartDelay(0)
                .setDuration(STORE_FADE_MS)
                .setInterpolator(ease)
                .withEndAction {
                    if (storeDetailOpen) return@withEndAction
                    detail.isVisible = false
                    detail.alpha = 1f
                }
                .start()
        }
        animateStorePeek(showDetail)
    }

    private fun animateStorePeek(detail: Boolean) {
        val sheet = binding.storeRoot
        val target = if (detail) storeDetailPeekMargin() else 0
        val from = (sheet.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
        storePeekAnimator?.cancel()
        if (from == target) {
            applyStorePeekMargin(target)
            return
        }
        storePeekAnimator = ValueAnimator.ofInt(from, target).apply {
            duration = STORE_PEEK_MS
            interpolator = COUIMoveEaseInterpolator()
            addUpdateListener { animator ->
                applyStorePeekMargin(animator.animatedValue as Int)
            }
            start()
        }
    }

    private fun applyStorePeekMargin(margin: Int) {
        binding.storeRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            if (topMargin != margin) topMargin = margin
        }
    }

    private fun resetStorePanes(showDetail: Boolean) {
        storePeekAnimator?.cancel()
        storePeekAnimator = null
        binding.storeListPane.animate().cancel()
        binding.storeDetailPane.animate().cancel()
        binding.storeListPane.translationY = 0f
        binding.storeDetailPane.translationY = 0f
        binding.storeListPane.alpha = 1f
        binding.storeDetailPane.alpha = 1f
        binding.storeListPane.isVisible = !showDetail
        binding.storeDetailPane.isVisible = showDetail
        applyStorePeekMargin(if (showDetail) storeDetailPeekMargin() else 0)
    }

    private fun storeDetailPeekMargin(): Int {
        val density = resources.displayMetrics.density
        val peek = binding.boardHeader.bottom.takeIf { it > 0 } ?: (96 * density).toInt()
        return peek + (20 * density).toInt()
    }

    private fun fillStoreDetailPreview(entry: CardCatalogEntry) {
        val host = binding.storeDetailPreview
        host.removeAllViews()
        fun addPreview() {
            if (!host.isAttachedToWindow) return
            val boardWidth = (host.width - host.paddingLeft - host.paddingRight).takeIf { it > 0 }
                ?: (resources.displayMetrics.widthPixels - (64 * resources.displayMetrics.density).toInt())
                    .coerceAtLeast(1)
            val (cardW, cardH) = storePreviewDims(entry, boardWidth, resources.displayMetrics.density)
            host.removeAllViews()
            host.addView(createStoreWidgetPreview(host, entry, cardW, cardH))
        }
        if (host.width > 0) addPreview() else host.post { addPreview() }
    }

    private fun fillStoreDetailPager(members: List<CardCatalogEntry>, index: Int) {
        val host = binding.storeDetailPreview
        host.removeAllViews()
        fun addPager() {
            if (!host.isAttachedToWindow) return
            val boardWidth = (host.width - host.paddingLeft - host.paddingRight).takeIf { it > 0 }
                ?: (resources.displayMetrics.widthPixels - (64 * resources.displayMetrics.density).toInt())
                    .coerceAtLeast(1)
            host.clipChildren = true
            host.removeAllViews()
            val pager = ViewPager2(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
                offscreenPageLimit = 1
                adapter = DetailPreviewAdapter(members, boardWidth, resources.displayMetrics.density)
            }
            host.addView(pager)
            val indicator = binding.storeDetailIndicator
            indicator.setDotsCount(members.size)
            centerDetailIndicator(members.size)
            indicator.setCurrentPosition(index)
            indicator.setIsClickable(true)
            indicator.setOnDotClickListener { dot -> pager.setCurrentItem(dot, true) }
            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    indicator.setCurrentPosition(position, positionOffset)
                }

                override fun onPageSelected(position: Int) {
                    val entry = members.getOrNull(position) ?: return
                    storeDetailPick = entry.id
                    val vm = viewModel ?: return
                    applyDetailSelection(entry, vm)
                }
            })
            pager.setCurrentItem(index, false)
        }
        if (host.width > 0) addPager() else host.post { addPager() }
    }

    /** COUI measures a full slot after the last dot, so the ink sits left of the view. */
    private fun centerDetailIndicator(count: Int) {
        val indicator = binding.storeDetailIndicator
        indicator.post {
            if (count <= 1) {
                indicator.translationX = 0f
                return@post
            }
            val density = resources.displayMetrics.density
            val dot = 6f * density
            val interval = 12f * density
            val content = dot + interval * (count - 1)
            val shift = (indicator.width - content) / 2f
            indicator.translationX = if (indicator.layoutDirection == View.LAYOUT_DIRECTION_RTL) -shift else shift
        }
    }

    private fun fillStoreGallery(
        list: LinearLayout,
        indexBar: LinearLayout,
        catalog: List<CardCatalogEntry>,
        groupId: String,
        viewModel: HiboardViewModel,
    ) {
        val density = resources.displayMetrics.density
        indexBar.isVisible = false
        indexBar.removeAllViews()
        list.clipChildren = false
        list.clipToPadding = false
        list.removeAllViews()
        val padH = (16 * density).toInt()
        list.setPadding(padH, (12 * density).toInt(), padH, (24 * density).toInt())
        val entries = widgetStoreSections(catalog, query = "", groupId).flatMap { it.entries }
        if (entries.isEmpty()) {
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
        val boardWidth = storeGalleryBoardWidth(list)
        val gap = (12 * density).roundToInt()
        val pending = mutableListOf<CardCatalogEntry>()
        fun flushRow() {
            if (pending.isEmpty()) return
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                clipChildren = false
                clipToPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            }
            pending.forEachIndexed { index, entry ->
                val block = storeWidgetBlock(row, entry, viewModel, (boardWidth - gap).coerceAtLeast(1))
                block.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (pending.size > 1) {
                        if (index == 0) marginEnd = gap / 2 else marginStart = gap / 2
                    }
                }
                row.addView(block)
            }
            if (pending.size == 1) {
                row.addView(
                    View(context),
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
                )
            }
            list.addView(row)
            pending.clear()
        }
        entries.forEach { entry ->
            if (entry.size.columns >= 4) {
                flushRow()
                list.addView(storeWidgetBlock(list, entry, viewModel, boardWidth))
            } else {
                pending.add(entry)
                if (pending.size == 2) flushRow()
            }
        }
        flushRow()
    }

    private fun storeGalleryBoardWidth(list: LinearLayout): Int {
        val pad = list.paddingLeft + list.paddingRight
        list.width.takeIf { it > pad }?.let { return it - pad }
        binding.storePager.width.takeIf { it > pad }?.let { return it - pad }
        return (resources.displayMetrics.widthPixels - pad).coerceAtLeast(1)
    }

    private fun storeWidgetBlock(
        parent: ViewGroup,
        entry: CardCatalogEntry,
        viewModel: HiboardViewModel,
        boardWidth: Int,
    ): View {
        val density = resources.displayMetrics.density
        val (cardW, cardH) = storePreviewDims(entry, boardWidth, density)
        val block = LayoutInflater.from(context).inflate(R.layout.item_store_widget, parent, false)
        val openDetail = View.OnClickListener { viewModel.openStoreDetail(entry.id) }
        block.findViewById<TextView>(R.id.widgetPreviewName).text = entry.name
        val host = block.findViewById<FrameLayout>(R.id.widgetPreview)
        host.addView(createStoreWidgetPreview(host, entry, cardW, cardH))
        host.setOnClickListener(openDetail)
        block.setOnClickListener(openDetail)
        return block
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
            CardEngineId.Contacts -> Triple(R.drawable.ic_contact, context.getColor(R.color.hiboard_store_title), Color.WHITE)
            CardEngineId.Calendar -> Triple(R.drawable.ic_calendar, context.getColor(R.color.hiboard_store_title), Color.WHITE)
            CardEngineId.Clock -> Triple(R.drawable.ic_clock, context.getColor(R.color.hiboard_store_title), Color.WHITE)
            CardEngineId.WeatherClock -> Triple(R.drawable.ic_clock, context.getColor(R.color.hiboard_store_title), Color.WHITE)
            CardEngineId.LocalTime -> Triple(R.drawable.ic_clock, context.getColor(R.color.hiboard_store_title), Color.WHITE)
            CardEngineId.Music -> Triple(R.drawable.ic_music_note, Color.WHITE, 0xFFA48462.toInt())
        }
    }

    private class DetailPreviewAdapter(
        private val members: List<CardCatalogEntry>,
        private val boardWidth: Int,
        private val density: Float,
    ) : RecyclerView.Adapter<DetailPreviewAdapter.Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val page = FrameLayout(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            return Holder(page)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val page = holder.page
            page.removeAllViews()
            val entry = members[position]
            val (cardW, cardH) = storePreviewDims(entry, boardWidth, density)
            page.addView(createStoreWidgetPreview(page, entry, cardW, cardH))
        }

        override fun getItemCount(): Int = members.size

        class Holder(val page: FrameLayout) : RecyclerView.ViewHolder(page)
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
            val groupId = tabs.getOrNull(position)?.first
            if (groupId == null) {
                fillStoreSections(
                    list = holder.list,
                    indexBar = holder.index,
                    scroll = holder.scroll,
                    catalog = catalog,
                    query = "",
                    groupId = null,
                    viewModel = vm,
                )
                return
            }
            fillStoreGallery(
                list = holder.list,
                indexBar = holder.index,
                catalog = catalog,
                groupId = groupId,
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
        const val CONTACTS_PERMISSION = 44
        const val STORE_SLIDE_IN_MS = 360L
        const val STORE_SLIDE_OUT_MS = 280L
        const val STORE_DISMISS_FRACTION = 0.18f
        const val STORE_DISMISS_VELOCITY = 900f
        const val STORE_PEEK_MS = 420L
        const val STORE_FADE_MS = 320L
        const val STORE_FADE_DELAY_MS = 40L
        val INDEX_LETTERS = (('A'..'Z') + '#').map { it.toString() }
    }
}
