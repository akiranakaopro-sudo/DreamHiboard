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
import androidx.core.view.doOnLayout
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.RECENT_APP_LIMIT
import gd.app.hiboard.engine.RecorderCommand
import gd.app.hiboard.engine.RecorderStatus
import gd.app.hiboard.engine.WeatherCondition
import gd.app.hiboard.engine.WeatherSnapshot
import gd.app.hiboard.engine.monthPageToday
import gd.app.hiboard.engine.resolved
import gd.app.hiboard.engine.formatRecorderTime
import gd.app.hiboard.engine.formatStorageUsage
import gd.app.hiboard.engine.recorderPrimaryCommand
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.model.RecorderUiState
import gd.app.hiboard.model.ShortcutApp
import gd.app.hiboard.model.WeatherDayContent

class CardBinder(
    private val onOpenNotes: () -> Unit,
    private val onCreateNote: () -> Unit,
    private val onToggleFlashlight: () -> Unit,
    private val onOpenStorage: () -> Unit,
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
            CardEngineId.Weather -> bindWeather(inflater, root, body, state)
            CardEngineId.Notes -> bindNotes(inflater, root, body, state)
            CardEngineId.RecentApps -> bindRecentApps(inflater, root, body, state)
            CardEngineId.Flashlight -> bindFlashlight(inflater, root, body, state)
            CardEngineId.Storage -> bindStorage(inflater, root, body, state)
            CardEngineId.Recorder -> bindRecorder(inflater, root, body, state)
            CardEngineId.Contacts -> bindContacts(inflater, root, body, state)
            CardEngineId.Calendar -> bindCalendar(root, body)
            CardEngineId.Clock -> bindClock(root, body)
            CardEngineId.WeatherClock -> bindWeatherClockCard(root, body, state)
            CardEngineId.LocalTime -> bindLocalTime(root, body)
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
        view.findViewById<ImageView>(R.id.weatherBackground).setImageResource(weatherBackgroundRes(condition))
        view.findViewById<TextView>(R.id.weatherLocation).text =
            state.content.weatherLocation.ifBlank { WeatherSnapshot.DEFAULT.location }
        view.findViewById<TextView>(R.id.weatherSummary).text =
            state.content.weatherSummary.ifBlank { condition.displayName }
        view.findViewById<ImageView>(R.id.weatherConditionIcon).setImageResource(weatherIconRes(condition))
        view.findViewById<TextView>(R.id.weatherTemp).text = "${state.content.weatherTempC}°"
        val forecast = view.findViewById<LinearLayout>(R.id.weatherForecast)
        forecast.removeAllViews()
        val days = state.content.weatherDays.ifEmpty {
            WeatherSnapshot.DEFAULT.resolved().days.map { day ->
                WeatherDayContent(day.label, day.condition.json, day.lowC, day.highC)
            }
        }
        days.forEach { day ->
            val item = inflater.inflate(R.layout.item_weather_day, forecast, false)
            val dayCondition = WeatherCondition.from(day.condition)
            item.findViewById<TextView>(R.id.weatherDayLabel).text = day.label
            item.findViewById<ImageView>(R.id.weatherDayIcon).setImageResource(weatherIconRes(dayCondition))
            item.findViewById<TextView>(R.id.weatherDayRange).text = "${day.lowC}° / ${day.highC}°"
            forecast.addView(item)
        }
    }

    private fun weatherBackgroundRes(condition: WeatherCondition): Int = when (condition) {
        WeatherCondition.Sunny -> R.drawable.bg_weather_sunny
        WeatherCondition.Cloudy -> R.drawable.bg_weather_cloudy
        WeatherCondition.Rain -> R.drawable.bg_weather_rain
        WeatherCondition.Thunder -> R.drawable.bg_weather_thunder
        WeatherCondition.Snow -> R.drawable.bg_weather_snow
        WeatherCondition.Fog -> R.drawable.bg_weather_fog
        WeatherCondition.Night -> R.drawable.bg_weather_night
    }

    private fun weatherIconRes(condition: WeatherCondition): Int = when (condition) {
        WeatherCondition.Sunny -> R.drawable.ic_weather_sunny
        WeatherCondition.Cloudy -> R.drawable.ic_weather_cloudy
        WeatherCondition.Rain -> R.drawable.ic_weather_rain
        WeatherCondition.Thunder -> R.drawable.ic_weather_thunder
        WeatherCondition.Snow -> R.drawable.ic_weather_snow
        WeatherCondition.Fog -> R.drawable.ic_weather_fog
        WeatherCondition.Night -> R.drawable.ic_weather_night
    }

    private fun bindNotes(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        (root as? COUICardView)?.setCardBackgroundColor(body.context.getColor(R.color.hiboard_notes_card))
        val view = inflater.inflate(R.layout.card_notes, body, true)
        val titleView = view.findViewById<TextView>(R.id.notesTitle)
        val snippetView = view.findViewById<TextView>(R.id.notesSnippet)
        titleView.text = state.content.notesPreview.ifBlank {
            body.context.getString(R.string.notes_default_title)
        }
        snippetView.text = state.content.notesSnippet.ifBlank {
            body.context.getString(R.string.notes_default_content)
        }
        snippetView.doOnLayout { measured ->
            val line = snippetView.lineHeight.coerceAtLeast(1)
            snippetView.maxLines = (measured.height / line).coerceAtLeast(1)
        }
        view.findViewById<TextView>(R.id.notesWhen).text = state.content.notesWhen
        view.findViewById<View>(R.id.notesAdd).setOnClickListener { onCreateNote() }
        view.findViewById<View>(R.id.notesRoot).setOnClickListener { onOpenNotes() }
        root.setOnClickListener { onOpenNotes() }
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
        art.setImageResource(if (on) R.drawable.flashlight_on else R.drawable.flashlight_off)
        art.scaleType = ImageView.ScaleType.CENTER_CROP
        art.alpha = if (available || on) 1f else 0.72f
        art.contentDescription = when {
            !available -> body.context.getString(R.string.flashlight_unavailable)
            on -> body.context.getString(R.string.flashlight_on)
            else -> body.context.getString(R.string.flashlight_off)
        }
        val toggle = View.OnClickListener { onToggleFlashlight() }
        view.findViewById<View>(R.id.flashlightRoot).setOnClickListener(toggle)
        root.setOnClickListener(toggle)
    }

    private fun bindStorage(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        (root as? COUICardView)?.setCardBackgroundColor(
            body.context.getColor(R.color.hiboard_storage_card),
        )
        val view = inflater.inflate(R.layout.card_storage, body, true)
        val total = state.content.storageTotalBytes
        val used = state.content.storageUsedBytes
        view.findViewById<StorageUsageBar>(R.id.storageBar).progress =
            if (total <= 0L) 0f else (used.toDouble() / total).toFloat().coerceIn(0f, 1f)
        view.findViewById<TextView>(R.id.storageUsage).text = formatStorageUsage(used, total)
        val open = View.OnClickListener { onOpenStorage() }
        view.findViewById<View>(R.id.storageRoot).setOnClickListener(open)
        view.findViewById<View>(R.id.storageCleanup).setOnClickListener(open)
        root.setOnClickListener(open)
    }

    private fun bindRecorder(
        inflater: LayoutInflater,
        root: View,
        body: LinearLayout,
        state: HiboardUiState,
    ) {
        val density = body.resources.displayMetrics.density
        (root as? COUICardView)?.apply {
            setCardBackgroundColor(body.context.getColor(R.color.hiboard_recorder_card))
            setContentPadding(
                (10 * density).toInt(),
                (10 * density).toInt(),
                (10 * density).toInt(),
                (8 * density).toInt(),
            )
            clipToPadding = false
        }
        (root as? ViewGroup)?.clipChildren = false
        body.clipChildren = false
        body.clipToPadding = false
        val view = inflater.inflate(R.layout.card_recorder, body, true)
        val recorderState = state.content.recorderState
        val live = recorderState != RecorderUiState.Idle
        val time = view.findViewById<TextView>(R.id.recorderTime)
        time.setTextColor(
            body.context.getColor(
                if (recorderState == RecorderUiState.Idle) {
                    R.color.hiboard_recorder_title_idle
                } else {
                    R.color.hiboard_recorder_title
                },
            ),
        )
        time.text = formatRecorderTime(state.content.recorderElapsedMs)
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
        when (recorderState) {
            RecorderUiState.Recording -> {
                primary.setImageResource(R.drawable.ic_recorder_pause)
                primary.contentDescription = body.context.getString(R.string.recorder_pause)
            }
            RecorderUiState.Paused -> {
                primary.setImageResource(R.drawable.ic_recorder_resume)
                primary.contentDescription = body.context.getString(R.string.recorder_resume)
            }
            RecorderUiState.Idle -> {
                primary.setImageResource(R.drawable.ic_recorder_record)
                primary.contentDescription = body.context.getString(R.string.recorder_start)
            }
        }
        val open = View.OnClickListener { onOpenRecorder() }
        view.findViewById<View>(R.id.recorderRoot).setOnClickListener(open)
        root.setOnClickListener(open)
        primary.setOnClickListener {
            onRecorderCommand(recorderPrimaryCommand(recorderState))
        }
        mark.setOnClickListener { onRecorderCommand(RecorderCommand.Mark) }
        save.setOnClickListener { button ->
            button.post { onRecorderCommand(RecorderCommand.Save) }
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
        val fallback = WeatherSnapshot.DEFAULT.resolved()
        val condition = WeatherCondition.from(
            state.content.weatherCondition.ifBlank { state.content.weatherSummary }.ifBlank { fallback.condition.json },
        )
        val temperature = if (state.content.weatherSummary.isBlank() && state.content.weatherCondition.isBlank()) {
            fallback.temperatureC
        } else {
            state.content.weatherTempC
        }
        bindWeatherClock(card, body, condition, temperature, onOpenClock)
    }

    private fun bindLocalTime(root: View, body: LinearLayout) {
        val card = root as? COUICardView ?: return
        bindLocalTimeClock(card, body, onOpenClock)
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

fun launchIntent(view: View, intent: Intent?) {
    if (intent != null) {
        view.context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
