package gd.app.hiboard.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.WeatherCondition
import gd.app.hiboard.engine.WeatherSnapshot
import gd.app.hiboard.engine.formatRecorderTime
import gd.app.hiboard.engine.formatStoragePair
import gd.app.hiboard.engine.formatStoragePercent
import gd.app.hiboard.engine.monthPageToday
import gd.app.hiboard.engine.BATTERY_STICK_COUNT_COMPACT
import gd.app.hiboard.engine.densifyBatterySamples
import gd.app.hiboard.engine.previewBatterySamples
import gd.app.hiboard.engine.resolved
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.CardSize
import gd.app.hiboard.model.NoteItem
import kotlin.math.roundToInt

fun storePreviewDims(entry: CardCatalogEntry, boardWidth: Int, density: Float): Pair<Int, Int> {
    val gutter = (10 * density).roundToInt()
    val cell = ((boardWidth - gutter * 3) / 4f).roundToInt().coerceAtLeast(1)
    val width = cell * entry.size.columns + gutter * (entry.size.columns - 1).coerceAtLeast(0)
    val height = cell * entry.size.rows + gutter * (entry.size.rows - 1).coerceAtLeast(0)
    return width to height
}

fun createStoreWidgetPreview(parent: ViewGroup, entry: CardCatalogEntry, width: Int, height: Int): View {
    val context = parent.context
    val inflater = LayoutInflater.from(context)
    val card = inflater.inflate(R.layout.item_board_card, parent, false) as COUICardView
    card.layoutParams = FrameLayout.LayoutParams(width, height).apply { gravity = Gravity.CENTER }
    card.isClickable = false
    card.isFocusable = false
    card.findViewById<View>(R.id.cardBadge).visibility = View.GONE
    val body = card.findViewById<LinearLayout>(R.id.cardBody)
    val density = context.resources.displayMetrics.density
    val elevation = 10f * density
    card.cardElevation = elevation
    card.maxCardElevation = 14f * density
    card.translationZ = elevation
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        card.outlineSpotShadowColor = 0x4D000000.toInt()
        card.outlineAmbientShadowColor = 0x29000000.toInt()
    }
    when (entry.engine) {
        CardEngineId.Weather -> if (entry.size == CardSize.TwoByTwo) {
            val weather = WeatherSnapshot.DEFAULT.resolved()
            val today = weather.days.first()
            bindWeatherSquare(
                card = card,
                body = body,
                location = weather.location,
                condition = weather.condition,
                summary = weather.condition.displayName,
                temperatureC = weather.temperatureC,
                lowC = today.lowC,
                highC = today.highC,
            )
        } else {
            bindWeatherPreview(inflater, card, body)
        }
        CardEngineId.Notes -> if (entry.size.columns >= 4) {
            card.setCardBackgroundColor(context.getColor(R.color.hiboard_notes_card))
            card.setContentPadding(0, 0, 0, 0)
            val sample = NoteItem(
                title = context.getString(R.string.notes_default_title),
                snippet = context.getString(R.string.notes_default_content),
            )
            bindNotesWide(inflater.inflate(R.layout.card_notes_wide, body, true), List(if (entry.size.rows >= 4) 3 else 2) { sample }, onOpenNote = null)
        } else {
            bindNotesPreview(inflater, card, body)
        }
        CardEngineId.Storage -> bindStoragePreview(inflater, card, body)
        CardEngineId.Battery -> when {
            entry.id == "batterylevelwide" -> bindBatteryLevelPreview(inflater, card, body, R.layout.card_battery_level_wide)
            entry.id == "batterylevel" -> bindBatteryLevelPreview(inflater, card, body, R.layout.card_battery_level)
            entry.size == CardSize.TwoByTwo -> bindBatterySmallPreview(inflater, card, body)
            else -> bindBatteryPreview(inflater, card, body)
        }
        CardEngineId.Recorder -> bindRecorderPreview(inflater, card, body)
        CardEngineId.Flashlight -> bindFlashlightPreview(inflater, card, body)
        CardEngineId.RecentApps -> Unit
        CardEngineId.Contacts -> bindContactsPreview(inflater, card, body)
        CardEngineId.Calendar -> bindCalendarCard(card, body, monthPageToday(), onOpen = null)
        CardEngineId.Clock -> bindClockCard(card, body, onOpen = null)
        CardEngineId.WeatherClock -> {
            val weather = WeatherSnapshot.DEFAULT.resolved()
            bindWeatherClock(card, body, weather.condition, weather.temperatureC, onOpen = null)
        }
        CardEngineId.LocalTime -> bindLocalTimeClock(card, body, onOpen = null)
        CardEngineId.RomanClock -> bindRomanClock(card, body, onOpen = null)
        CardEngineId.WeatherDial -> {
            val weather = WeatherSnapshot.DEFAULT.resolved()
            bindWeatherDialClock(card, body, weather.condition, weather.temperatureC, onOpen = null)
        }
        CardEngineId.Music -> bindMusicCard(card, body, live = false)
    }
    freezePreview(card)
    return card
}

private fun bindWeatherPreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    val weather = WeatherSnapshot.DEFAULT.resolved()
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_weather_fallback))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val view = inflater.inflate(R.layout.card_weather, body, true)
    bindWeatherWide(
        view = view,
        location = weather.location,
        summary = weather.condition.displayName,
        condition = weather.condition,
        temperatureC = weather.temperatureC,
        days = weather.days.map { WeatherWideDay(it.label, it.condition, it.lowC, it.highC) },
    )
}

private fun bindNotesPreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_notes_card))
    card.setContentPadding(0, 0, 0, 0)
    val view = inflater.inflate(R.layout.card_notes, body, true)
    view.findViewById<TextView>(R.id.notesTitle).text = body.context.getString(R.string.notes_default_title)
    view.findViewById<TextView>(R.id.notesSnippet).text = body.context.getString(R.string.notes_default_content)
    view.findViewById<TextView>(R.id.notesWhen).text = ""
}

private fun bindStoragePreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    val density = body.resources.displayMetrics.density
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_storage_card))
    card.setContentPadding(
        (14 * density).toInt(),
        (14 * density).toInt(),
        (14 * density).toInt(),
        (14 * density).toInt(),
    )
    val view = inflater.inflate(R.layout.card_storage, body, true)
    view.findViewById<StorageUsageRing>(R.id.storageRing).progress = 0.21f
    view.findViewById<TextView>(R.id.storagePercent).text =
        formatStoragePercent(2_900_000_000L, 12_400_000_000L)
    view.findViewById<TextView>(R.id.storageUsage).text =
        formatStoragePair(2_900_000_000L, 12_400_000_000L)
}

private fun bindBatteryPreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    val density = body.resources.displayMetrics.density
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_battery_card))
    card.setContentPadding(
        (16 * density).toInt(),
        (14 * density).toInt(),
        (16 * density).toInt(),
        (12 * density).toInt(),
    )
    val view = inflater.inflate(R.layout.card_battery, body, true)
    view.findViewById<TextView>(R.id.batteryPercent).text = "63%"
    view.findViewById<ImageView>(R.id.batteryStatusIcon).setImageResource(R.drawable.ic_battery_charge)
    view.findViewById<TextView>(R.id.batteryStatus).setText(R.string.battery_charging)
    view.findViewById<BatteryUsageGraph>(R.id.batteryGraph).apply {
        compact = false
        samples = densifyBatterySamples(previewBatterySamples(level = 63, chargingNow = true), currentLevel = 63)
    }
}

private fun bindBatterySmallPreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    val density = body.resources.displayMetrics.density
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_battery_card))
    card.setContentPadding(
        (14 * density).toInt(),
        (12 * density).toInt(),
        (14 * density).toInt(),
        (10 * density).toInt(),
    )
    val view = inflater.inflate(R.layout.card_battery_small, body, true)
    view.findViewById<TextView>(R.id.batteryPercent).text = "53%"
    view.findViewById<TextView>(R.id.batteryStatus).text =
        body.resources.getString(R.string.battery_should_last, body.resources.getString(R.string.battery_duration_hours, 12))
    view.findViewById<BatteryUsageGraph>(R.id.batteryGraph).apply {
        compact = true
        samples = densifyBatterySamples(
            previewBatterySamples(level = 53),
            currentLevel = 53,
            stickCount = BATTERY_STICK_COUNT_COMPACT,
        )
    }
}

private fun bindBatteryLevelPreview(
    inflater: LayoutInflater,
    card: COUICardView,
    body: LinearLayout,
    layout: Int,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_battery_level_card))
    card.setContentPadding(0, 0, 0, 0)
    val view = inflater.inflate(layout, body, true)
    view.findViewById<TextView>(R.id.batteryStatus).setText(R.string.battery_device_label)
    view.findViewById<TextView>(R.id.batteryPercent).text = "80%"
    view.findViewById<TextView>(R.id.batteryRemain).text =
        body.resources.getString(R.string.battery_use_remaining, body.resources.getString(R.string.battery_duration_hours, 24))
    view.findViewById<BatteryLevelBar>(R.id.batteryLevelBar).progress = 0.80f
}

private fun bindRecorderPreview(
    inflater: LayoutInflater,
    card: COUICardView,
    body: LinearLayout,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_recorder_card))
    card.setContentPadding(0, 0, 0, 0)
    val view = inflater.inflate(R.layout.card_recorder, body, true)
    view.findViewById<TextView>(R.id.recorderTime).text = formatRecorderTime(0L)
    view.findViewById<View>(R.id.recorderMark).visibility = View.INVISIBLE
    view.findViewById<View>(R.id.recorderSave).visibility = View.INVISIBLE
    view.findViewById<ImageView>(R.id.recorderPrimary).setImageResource(R.drawable.ic_recorder_record)
}

private fun bindFlashlightPreview(
    inflater: LayoutInflater,
    card: COUICardView,
    body: LinearLayout,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_flashlight_off))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val view = inflater.inflate(R.layout.card_flashlight, body, true)
    val art = view.findViewById<ImageView>(R.id.flashlightArt)
    art.setImageResource(R.drawable.flashlight_off)
    art.scaleType = ImageView.ScaleType.CENTER_CROP
    art.contentDescription = body.context.getString(R.string.flashlight_off)
}

private fun bindContactsPreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    bindContactsCard(
        inflater,
        card,
        body,
        people = listOf(
            ContactFace("Sophia", avatarRes = R.drawable.avatar_sophia),
            ContactFace("Jim", avatarRes = R.drawable.avatar_jim),
            ContactFace("Carlos", avatarRes = R.drawable.avatar_carlos),
            ContactFace("Layla", avatarRes = R.drawable.avatar_layla),
        ),
        message = null,
        onPerson = null,
        onCard = null,
    )
}

private fun freezePreview(root: View) {
    root.isClickable = false
    root.isFocusable = false
    if (root is ViewGroup) {
        root.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        for (index in 0 until root.childCount) freezePreview(root.getChildAt(index))
    }
}

internal fun weatherBackgroundRes(condition: WeatherCondition): Int = when (condition) {
    WeatherCondition.Sunny -> R.drawable.bg_weather_sunny
    WeatherCondition.Cloudy -> R.drawable.bg_weather_cloudy
    WeatherCondition.Rain -> R.drawable.bg_weather_rain
    WeatherCondition.Thunder -> R.drawable.bg_weather_thunder
    WeatherCondition.Snow -> R.drawable.bg_weather_snow
    WeatherCondition.Fog -> R.drawable.bg_weather_fog
    WeatherCondition.Night -> R.drawable.bg_weather_night
}

internal fun weatherIconRes(condition: WeatherCondition): Int = when (condition) {
    WeatherCondition.Sunny -> R.drawable.ic_weather_sunny
    WeatherCondition.Cloudy -> R.drawable.ic_weather_cloudy
    WeatherCondition.Rain -> R.drawable.ic_weather_rain
    WeatherCondition.Thunder -> R.drawable.ic_weather_thunder
    WeatherCondition.Snow -> R.drawable.ic_weather_snow
    WeatherCondition.Fog -> R.drawable.ic_weather_fog
    WeatherCondition.Night -> R.drawable.ic_weather_night
}
