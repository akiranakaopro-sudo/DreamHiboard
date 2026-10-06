package gd.app.hiboard.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.ALL_NOTES_FOLDER
import gd.app.hiboard.engine.RECENT_APP_LIMIT
import gd.app.hiboard.engine.formatNotesWhen
import gd.app.hiboard.engine.noteFolderLabel
import gd.app.hiboard.engine.notesInFolder
import gd.app.hiboard.model.NoteItem
import gd.app.hiboard.engine.RecorderCommand
import gd.app.hiboard.engine.RecorderStatus
import gd.app.hiboard.engine.WeatherCondition
import gd.app.hiboard.engine.WeatherSnapshot
import gd.app.hiboard.engine.monthPageToday
import gd.app.hiboard.engine.resolved
import gd.app.hiboard.engine.formatRecorderTime
import gd.app.hiboard.engine.STORAGE_DISPLAY_OFFSET_BYTES
import gd.app.hiboard.engine.BATTERY_STICK_COUNT
import gd.app.hiboard.engine.BATTERY_STICK_COUNT_COMPACT
import gd.app.hiboard.engine.densifyBatterySamples
import gd.app.hiboard.engine.formatBatteryRemainingDuration
import gd.app.hiboard.engine.formatStoragePair
import gd.app.hiboard.engine.formatStoragePercent
import gd.app.hiboard.engine.recorderPrimaryCommand
import gd.app.hiboard.model.BatterySample
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.CardSize
import gd.app.hiboard.model.RecorderUiState
import gd.app.hiboard.model.ShortcutApp
class CardBinder(
    private val onOpenNotes: () -> Unit,
    private val onOpenNote: (Long) -> Unit,
    private val onCreateNote: () -> Unit,
    private val onToggleFlashlight: () -> Unit,
    private val onOpenStorage: () -> Unit,
    private val onOpenBattery: () -> Unit,
    private val onRecorderCommand: (RecorderCommand) -> Unit,
    private val recorderLive: () -> RecorderStatus,
    private val onOpenRecorder: () -> Unit,
    private val onOpenApp: (ShortcutApp) -> Unit,
    private val onOpenContact: (String) -> Unit,
    private val onOpenContacts: () -> Unit,
    private val onAllowContacts: () -> Unit,
    private val onOpenCalendar: () -> Unit,
    private val onOpenClock: () -> Unit,
    private val onRemove: (String) -> Unit,
    private val onAdd: (String) -> Unit,
) {
    fun create(parent: ViewGroup, card: CardInstance, state: HiboardUiState, recommend: Boolean): View {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.item_board_card, parent, false)
        val body = root.findViewById<LinearLayout>(R.id.cardBody)
        val badge = root.findViewById<TextView>(R.id.cardBadge)
        when (card.engine) {
            CardEngineId.Weather -> if (card.size == CardSize.TwoByTwo) {
                bindWeatherSquareCard(root, body, state)
            } else {
                bindWeather(inflater, root, body, state)
            }
            CardEngineId.Notes -> if (card.size.columns >= 4) {
                bindNotesWideCard(inflater, root, body, card, state)
            } else {
                bindNotes(inflater, root, body, card, state)
            }
            CardEngineId.RecentApps -> bindRecentApps(inflater, root, body, state)
            CardEngineId.Flashlight -> bindFlashlight(inflater, root, body, state)
            CardEngineId.Storage -> bindStorage(inflater, root, body, state)
            CardEngineId.Battery -> when {
                card.catalogId == "batterylevelwide" -> bindBatteryLevel(inflater, root, body, state, R.layout.card_battery_level_wide)
                card.catalogId == "batterylevel" -> bindBatteryLevel(inflater, root, body, state, R.layout.card_battery_level)
                card.size == CardSize.TwoByTwo -> bindBatterySmall(inflater, root, body, state)
                else -> bindBattery(inflater, root, body, state)
            }
            CardEngineId.Recorder -> bindRecorder(inflater, root, body, state)
            CardEngineId.Contacts -> bindContacts(inflater, root, body, state)
            CardEngineId.Calendar -> bindCalendar(root, body)
            CardEngineId.Clock -> bindClock(root, body)
            CardEngineId.WeatherClock -> bindWeatherClockCard(root, body, state)
            CardEngineId.LocalTime -> bindLocalTime(root, body)
            CardEngineId.RomanClock -> bindRomanClockCard(root, body)
            CardEngineId.WeatherDial -> bindWeatherDialCard(root, body, state)
            CardEngineId.Music -> bindMusic(root, body)
        }
        if (state.editMode && card.canEdit) {
            badge.visibility = View.VISIBLE
            badge.text = if (recommend) "+" else "×"
            badge.setOnClickListener {
                if (recommend) onAdd(card.catalogId) else onRemove(card.catalogId)
            }
        } else {
            badge.visibility = View.GONE
        }
        return root
    }

    private fun bindWeather(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val condition = WeatherCondition.from(state.content.weatherCondition.ifBlank { state.content.weatherSummary })
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_weather_fallback))
            setContentPadding(0, 0, 0, 0)
            clipToOutline = true
        }
        val view = inflater.inflate(R.layout.card_weather, body, true)
        val days = state.content.weatherDays.map { day ->
            WeatherWideDay(day.label, WeatherCondition.from(day.condition), day.lowC, day.highC)
        }.ifEmpty {
            WeatherSnapshot.DEFAULT.resolved().days.map { day ->
                WeatherWideDay(day.label, day.condition, day.lowC, day.highC)
            }
        }
        bindWeatherWide(
            view = view,
            location = state.content.weatherLocation.ifBlank { WeatherSnapshot.DEFAULT.location },
            summary = state.content.weatherSummary.ifBlank { condition.displayName },
            condition = condition,
            temperatureC = state.content.weatherTempC,
            days = days,
        )
    }

    private fun bindWeatherSquareCard(root: View, body: LinearLayout, state: HiboardUiState) {
        val card = root as? COUICardView ?: return
        val content = state.content
        val condition = WeatherCondition.from(content.weatherCondition.ifBlank { content.weatherSummary })
        val today = content.weatherDays.firstOrNull()
        val fallback = WeatherSnapshot.DEFAULT.days.first()
        bindWeatherSquare(
            card = card,
            body = body,
            location = content.weatherLocation.ifBlank { WeatherSnapshot.DEFAULT.location },
            condition = condition,
            summary = content.weatherSummary.ifBlank { condition.displayName },
            temperatureC = content.weatherTempC,
            lowC = today?.lowC ?: fallback.lowC,
            highC = today?.highC ?: fallback.highC,
        )
    }

    private fun bindNotes(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        card: CardInstance,
        state: HiboardUiState,
    ) {
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_notes_card))
            setContentPadding(0, 0, 0, 0)
        }
        val view = inflater.inflate(R.layout.card_notes, body, true)
        val folder = bindNotesLabel(view, card, state)
        val latest = notesInFolder(state.content.notes, folder).firstOrNull()
        view.findViewById<TextView>(R.id.notesTitle).text =
            latest?.title ?: body.context.getString(R.string.notes_default_title)
        view.findViewById<TextView>(R.id.notesSnippet).text =
            latest?.snippet ?: body.context.getString(R.string.notes_default_content)
        view.findViewById<TextView>(R.id.notesWhen).text = latest?.let { formatNotesWhen(it.updatedAt) }.orEmpty()
        view.findViewById<View>(R.id.notesAdd).setOnClickListener { onCreateNote() }
        val open = View.OnClickListener { openNotesIn(folder, latest) }
        view.findViewById<View>(R.id.notesRoot).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    /** Shows the card's folder name in the header and returns its folder key. */
    private fun bindNotesLabel(view: View, card: CardInstance, state: HiboardUiState): String {
        val folder = state.noteFolderSelections[card.catalogId] ?: ALL_NOTES_FOLDER
        view.findViewById<TextView>(R.id.notesLabel).text = noteFolderLabel(
            state.content.noteFolders,
            folder,
            view.context.getString(R.string.notes_label),
        )
        return folder
    }

    private fun openNotesIn(folder: String, latest: NoteItem?) {
        when {
            folder == ALL_NOTES_FOLDER -> onOpenNotes()
            latest != null -> onOpenNote(latest.id)
            else -> onCreateNote()
        }
    }

    private fun bindNotesWideCard(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        card: CardInstance,
        state: HiboardUiState,
    ) {
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_notes_card))
            setContentPadding(0, 0, 0, 0)
        }
        val view = inflater.inflate(R.layout.card_notes_wide, body, true)
        val folder = bindNotesLabel(view, card, state)
        val notes = notesInFolder(state.content.notes, folder).take(WIDE_NOTES_LIMIT)
        bindNotesWide(view, notes, onOpenNote)
        view.findViewById<View>(R.id.notesAdd).setOnClickListener { onCreateNote() }
        val open = View.OnClickListener { openNotesIn(folder, notes.firstOrNull()) }
        view.findViewById<View>(R.id.notesRoot).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    private fun bindRecentApps(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val pad = (8 * body.resources.displayMetrics.density).toInt()
        val fill = body.context.getColor(R.color.hiboard_chrome_fill)
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(fill)
            setContentPadding(pad, pad, pad, pad)
        }
        val view = inflater.inflate(R.layout.card_recent_apps, body, true)
        val row = view.findViewById<LinearLayout>(R.id.recentRow)
        row.removeAllViews()
        val pm = body.context.packageManager
        val labelColor = body.context.getColor(R.color.hiboard_chrome)
        state.content.recentApps.take(RECENT_APP_LIMIT).forEach { app ->
            val item = inflater.inflate(R.layout.item_recent_app, row, false)
            item.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val iconView = item.findViewById<ImageView>(R.id.recentIcon)
            iconView.setImageDrawable(recentIcon(pm, app))
            item.findViewById<TextView>(R.id.recentLabel).apply {
                text = app.label
                setTextColor(labelColor)
            }
            item.setOnClickListener { onOpenApp(app) }
            row.addView(item)
        }
    }

    private fun bindFlashlight(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val on = state.content.flashlightOn
        val available = state.content.flashlightAvailable
        val cardColor = body.context.getColor(R.color.hiboard_flashlight_off)
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(cardColor)
            setContentPadding(0, 0, 0, 0)
            clipToOutline = true
        }
        val view = inflater.inflate(R.layout.card_flashlight, body, true)
        val art = view.findViewById<ImageView>(R.id.flashlightArt)
        // Warm both frames so the next toggle does not stall on decode.
        art.context.getDrawable(R.drawable.flashlight_on)
        art.context.getDrawable(R.drawable.flashlight_off)
        applyFlashlightArt(art, on, available)
        val toggle = View.OnClickListener { onToggleFlashlight() }
        view.findViewById<View>(R.id.flashlightRoot).setOnClickListener(toggle)
        root.setOnClickListener(toggle)
    }

    companion object {
        fun applyFlashlightArt(art: ImageView, on: Boolean, available: Boolean) {
            art.setImageResource(if (on) R.drawable.flashlight_on else R.drawable.flashlight_off)
            art.scaleType = ImageView.ScaleType.CENTER_CROP
            art.alpha = if (available || on) 1f else 0.72f
            art.contentDescription = when {
                !available -> art.context.getString(R.string.flashlight_unavailable)
                on -> art.context.getString(R.string.flashlight_on)
                else -> art.context.getString(R.string.flashlight_off)
            }
        }

        fun applyBatteryStatus(
            percentView: TextView,
            icon: ImageView?,
            label: TextView,
            graph: BatteryUsageGraph,
            percent: Int,
            charging: Boolean,
            remainingMs: Long,
            samples: List<BatterySample>,
        ) {
            percentView.text = "$percent%"
            applyBatteryLabel(icon, label, charging, remainingMs, percent)
            graph.samples = densifyBatterySamples(
                samples,
                currentLevel = percent,
                stickCount = if (graph.compact) BATTERY_STICK_COUNT_COMPACT else BATTERY_STICK_COUNT,
            )
        }

        fun applyBatteryLevelStatus(
            percentView: TextView,
            label: TextView,
            remain: TextView,
            bar: BatteryLevelBar,
            percent: Int,
            plugged: Boolean,
            remainingMs: Long,
            untilFullMs: Long,
        ) {
            percentView.text = "$percent%"
            when {
                plugged && percent >= 100 -> {
                    label.setText(R.string.battery_fully_charged)
                    remain.visibility = View.INVISIBLE
                    remain.text = ""
                }
                plugged -> {
                    label.setText(R.string.battery_charging)
                    remain.visibility = View.VISIBLE
                    val duration = formatBatteryRemainingDuration(remain.resources, untilFullMs)
                    remain.text = remain.resources.getString(R.string.battery_until_full, duration)
                }
                else -> {
                    label.setText(R.string.battery_device_label)
                    remain.visibility = View.VISIBLE
                    val duration = formatBatteryRemainingDuration(remain.resources, remainingMs)
                    remain.text = remain.resources.getString(R.string.battery_use_remaining, duration)
                }
            }
            bar.progress = percent / 100f
        }

        private fun applyBatteryLabel(
            icon: ImageView?,
            label: TextView,
            charging: Boolean,
            remainingMs: Long,
            percent: Int,
        ) {
            if (charging) {
                icon?.visibility = View.VISIBLE
                icon?.setImageResource(R.drawable.ic_battery_charge)
                label.setText(R.string.battery_charging)
            } else {
                if (icon != null) {
                    icon.visibility = View.VISIBLE
                    val ring = icon.drawable as? BatteryRemainRingDrawable
                        ?: BatteryRemainRingDrawable(
                            fillColor = icon.context.getColor(R.color.hiboard_battery_charging),
                            trackColor = icon.context.getColor(R.color.hiboard_battery_remain_track),
                        ).also { icon.setImageDrawable(it) }
                    ring.levelPercent = percent
                }
                val duration = formatBatteryRemainingDuration(label.resources, remainingMs)
                label.text = label.resources.getString(R.string.battery_should_last, duration)
            }
        }
    }

    private fun bindStorage(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val density = body.resources.displayMetrics.density
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_storage_card))
            setContentPadding(
                (14 * density).toInt(),
                (14 * density).toInt(),
                (14 * density).toInt(),
                (14 * density).toInt(),
            )
        }
        val view = inflater.inflate(R.layout.card_storage, body, true)
        val total = state.content.storageTotalBytes
        val used = state.content.storageUsedBytes
        val displayUsed = (used - STORAGE_DISPLAY_OFFSET_BYTES).coerceAtLeast(0L)
        val displayTotal = (total - STORAGE_DISPLAY_OFFSET_BYTES).coerceAtLeast(0L)
        view.findViewById<StorageUsageRing>(R.id.storageRing).progress =
            if (displayTotal <= 0L) {
                0f
            } else {
                (displayUsed.toDouble() / displayTotal).toFloat().coerceIn(0f, 1f)
            }
        view.findViewById<TextView>(R.id.storagePercent).text = formatStoragePercent(used, total)
        view.findViewById<TextView>(R.id.storageUsage).text = formatStoragePair(used, total)
        val open = View.OnClickListener { onOpenStorage() }
        view.findViewById<View>(R.id.storageRoot).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    private fun bindBattery(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val density = body.resources.displayMetrics.density
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_battery_card))
            setContentPadding(
                (16 * density).toInt(),
                (14 * density).toInt(),
                (16 * density).toInt(),
                (12 * density).toInt(),
            )
        }
        val view = inflater.inflate(R.layout.card_battery, body, true)
        val graph = view.findViewById<BatteryUsageGraph>(R.id.batteryGraph)
        graph.compact = false
        applyBatteryStatus(
            percentView = view.findViewById(R.id.batteryPercent),
            icon = view.findViewById(R.id.batteryStatusIcon),
            label = view.findViewById(R.id.batteryStatus),
            graph = graph,
            percent = state.content.batteryPercent,
            charging = state.content.batteryCharging,
            remainingMs = state.content.batteryRemainingMs,
            samples = state.content.batterySamples,
        )
        val open = View.OnClickListener { onOpenBattery() }
        view.findViewById<View>(R.id.batteryRoot).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    private fun bindBatterySmall(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val density = body.resources.displayMetrics.density
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_battery_card))
            setContentPadding(
                (14 * density).toInt(),
                (12 * density).toInt(),
                (14 * density).toInt(),
                (10 * density).toInt(),
            )
        }
        val view = inflater.inflate(R.layout.card_battery_small, body, true)
        val graph = view.findViewById<BatteryUsageGraph>(R.id.batteryGraph)
        graph.compact = true
        applyBatteryStatus(
            percentView = view.findViewById(R.id.batteryPercent),
            icon = null,
            label = view.findViewById(R.id.batteryStatus),
            graph = graph,
            percent = state.content.batteryPercent,
            charging = state.content.batteryCharging,
            remainingMs = state.content.batteryRemainingMs,
            samples = state.content.batterySamples,
        )
        val open = View.OnClickListener { onOpenBattery() }
        view.findViewById<View>(R.id.batteryRoot).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    private fun bindBatteryLevel(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
        layout: Int,
    ) {
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_battery_level_card))
            // Padding lives on the level layout so the bottom-left glow can fill the card.
            setContentPadding(0, 0, 0, 0)
        }
        val view = inflater.inflate(layout, body, true)
        applyBatteryLevelStatus(
            percentView = view.findViewById(R.id.batteryPercent),
            label = view.findViewById(R.id.batteryStatus),
            remain = view.findViewById(R.id.batteryRemain),
            bar = view.findViewById(R.id.batteryLevelBar),
            percent = state.content.batteryPercent,
            plugged = state.content.batteryPlugged,
            remainingMs = state.content.batteryRemainingMs,
            untilFullMs = state.content.batteryUntilFullMs,
        )
        val open = View.OnClickListener { onOpenBattery() }
        view.findViewById<View>(R.id.batteryLevelRoot).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    private fun bindRecorder(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_recorder_card))
            setContentPadding(0, 0, 0, 0)
            clipToPadding = false
        }
        (root as? ViewGroup)?.clipChildren = false
        body.clipChildren = false
        body.clipToPadding = false
        val view = inflater.inflate(R.layout.card_recorder, body, true)
        applyRecorder(view, state.content.recorderState, state.content.recorderElapsedMs)
        val open = View.OnClickListener { onOpenRecorder() }
        view.findViewById<View>(R.id.recorderRoot).setOnClickListener(open)
        root.setOnClickListener(open)
        val primary = view.findViewById<View>(R.id.recorderPrimary)
        primary.setOnClickListener {
            val shown = primary.getTag(R.id.recorderPrimary) as? RecorderUiState ?: RecorderUiState.Idle
            onRecorderCommand(recorderPrimaryCommand(shown))
        }
        view.findViewById<View>(R.id.recorderMark).setOnClickListener {
            onRecorderCommand(RecorderCommand.Mark)
        }
        view.findViewById<View>(R.id.recorderSave).setOnClickListener {
            onRecorderCommand(RecorderCommand.Save)
        }
    }

    /** Repaints an inflated recorder card for [recorderState] without rebuilding it. */
    fun applyRecorder(view: View, recorderState: RecorderUiState, elapsedMs: Long) {
        val context = view.context
        val live = recorderState != RecorderUiState.Idle
        val time = view.findViewById<TextView>(R.id.recorderTime)
        time.setTextColor(
            context.getColor(
                if (live) R.color.hiboard_recorder_title else R.color.hiboard_recorder_title_idle,
            ),
        )
        time.text = formatRecorderTime(elapsedMs)
        view.findViewById<RecorderWaveView>(R.id.recorderWave).bind(
            recording = recorderState == RecorderUiState.Recording,
            sessionActive = live,
            timeView = time,
            source = recorderLive,
        )
        val mark = view.findViewById<View>(R.id.recorderMark)
        val save = view.findViewById<View>(R.id.recorderSave)
        val primary = view.findViewById<ImageView>(R.id.recorderPrimary)
        mark.visibility = if (live) View.VISIBLE else View.INVISIBLE
        save.visibility = if (live) View.VISIBLE else View.INVISIBLE
        mark.isClickable = live
        save.isClickable = live
        primary.setTag(R.id.recorderPrimary, recorderState)
        when (recorderState) {
            RecorderUiState.Recording -> {
                primary.setImageResource(R.drawable.ic_recorder_pause)
                primary.contentDescription = context.getString(R.string.recorder_pause)
            }
            RecorderUiState.Paused -> {
                primary.setImageResource(R.drawable.ic_recorder_resume)
                primary.contentDescription = context.getString(R.string.recorder_resume)
            }
            RecorderUiState.Idle -> {
                primary.setImageResource(R.drawable.ic_recorder_record)
                primary.contentDescription = context.getString(R.string.recorder_start)
            }
        }
    }

    private fun bindContacts(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val card = root as? COUICardView ?: return
        val permitted = state.content.contactsPermitted
        val people = state.content.contacts.map { person ->
            ContactFace(person.name, photoUri = person.photoUri, lookupUri = person.lookupUri)
        }
        when {
            permitted && people.isNotEmpty() -> bindContactsCard(
                inflater, card, body, people, message = null,
                onPerson = { face -> face.lookupUri?.let(onOpenContact) },
                onCard = null,
            )
            !permitted -> bindContactsCard(
                inflater, card, body, emptyList(),
                message = body.context.getString(R.string.contacts_allow),
                onPerson = null,
                onCard = onAllowContacts,
            )
            else -> bindContactsCard(
                inflater, card, body, emptyList(),
                message = body.context.getString(R.string.contacts_empty),
                onPerson = null,
                onCard = onOpenContacts,
            )
        }
    }

    private fun bindCalendar(root: View, body: LinearLayout) {
        val card = root as? COUICardView ?: return
        bindCalendarCard(card, body, monthPageToday(), onOpenCalendar)
    }

    private fun bindClock(root: View, body: LinearLayout) {
        val card = root as? COUICardView ?: return
        bindClockCard(card, body, onOpenClock)
    }

    private fun bindWeatherClockCard(root: View, body: LinearLayout, state: HiboardUiState) {
        val card = root as? COUICardView ?: return
        val (condition, temperature) = clockWeather(state)
        bindWeatherClock(card, body, condition, temperature, onOpenClock)
    }

    private fun bindWeatherDialCard(root: View, body: LinearLayout, state: HiboardUiState) {
        val card = root as? COUICardView ?: return
        val (condition, temperature) = clockWeather(state)
        bindWeatherDialClock(card, body, condition, temperature, onOpenClock)
    }

    private fun clockWeather(state: HiboardUiState): Pair<WeatherCondition, Int> {
        val fallback = WeatherSnapshot.DEFAULT.resolved()
        val condition = WeatherCondition.from(
            state.content.weatherCondition.ifBlank { state.content.weatherSummary }.ifBlank { fallback.condition.json },
        )
        val temperature = if (state.content.weatherSummary.isBlank() && state.content.weatherCondition.isBlank()) {
            fallback.temperatureC
        } else {
            state.content.weatherTempC
        }
        return condition to temperature
    }

    private fun bindLocalTime(root: View, body: LinearLayout) {
        val card = root as? COUICardView ?: return
        bindLocalTimeClock(card, body, onOpenClock)
    }

    private fun bindRomanClockCard(root: View, body: LinearLayout) {
        val card = root as? COUICardView ?: return
        bindRomanClock(card, body, onOpenClock)
    }

    private fun bindMusic(root: View, body: LinearLayout) {
        val card = root as? COUICardView ?: return
        bindMusicCard(card, body, live = true)
    }
}

private fun recentIcon(pm: PackageManager, app: ShortcutApp) = try {
    if (app.activityName != null) {
        pm.getActivityIcon(ComponentName(app.packageName, app.activityName))
    } else {
        pm.getApplicationIcon(app.packageName)
    }
} catch (_: PackageManager.NameNotFoundException) {
    null
}

private const val WIDE_NOTES_LIMIT = 5

fun launchIntent(view: View, intent: Intent?) {
    if (intent != null) {
        view.context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
