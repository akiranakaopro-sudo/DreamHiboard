package gd.app.hiboard.ui

import android.content.res.ColorStateList
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
import gd.app.hiboard.engine.formatStorageUsage
import gd.app.hiboard.engine.monthPageToday
import gd.app.hiboard.engine.resolved
import gd.app.hiboard.model.CardCatalogEntry
import gd.app.hiboard.model.CardEngineId
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
        CardEngineId.Weather -> bindWeatherPreview(inflater, card, body)
        CardEngineId.Notes -> bindNotesPreview(inflater, card, body)
        CardEngineId.Storage -> bindStoragePreview(inflater, card, body)
        CardEngineId.Recorder -> bindRecorderPreview(inflater, card, body, density)
        CardEngineId.Flashlight -> bindFlashlightPreview(inflater, card, body, density)
        CardEngineId.RecentApps -> Unit
        CardEngineId.Contacts -> bindContactsPreview(inflater, card, body)
        CardEngineId.Calendar -> bindCalendarCard(card, body, monthPageToday(), onOpen = null)
        CardEngineId.Clock -> bindClockCard(card, body, onOpen = null)
        CardEngineId.WeatherClock -> {
            val weather = WeatherSnapshot.DEFAULT.resolved()
            bindWeatherClock(card, body, weather.condition, weather.temperatureC, onOpen = null)
        }
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
    view.findViewById<ImageView>(R.id.weatherBackground).setImageResource(weatherBackgroundRes(weather.condition))
    view.findViewById<TextView>(R.id.weatherLocation).text = weather.location
    view.findViewById<TextView>(R.id.weatherSummary).text = weather.condition.displayName
    view.findViewById<ImageView>(R.id.weatherConditionIcon).setImageResource(weatherIconRes(weather.condition))
    view.findViewById<TextView>(R.id.weatherTemp).text = "${weather.temperatureC}°"
    val forecast = view.findViewById<LinearLayout>(R.id.weatherForecast)
    forecast.removeAllViews()
    weather.days.forEach { day ->
        val item = inflater.inflate(R.layout.item_weather_day, forecast, false)
        item.findViewById<TextView>(R.id.weatherDayLabel).text = day.label
        item.findViewById<ImageView>(R.id.weatherDayIcon).setImageResource(weatherIconRes(day.condition))
        item.findViewById<TextView>(R.id.weatherDayRange).text = "${day.lowC}° / ${day.highC}°"
        forecast.addView(item)
    }
}

private fun bindNotesPreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_notes_card))
    val view = inflater.inflate(R.layout.card_notes, body, true)
    view.findViewById<TextView>(R.id.notesTitle).text = body.context.getString(R.string.notes_default_title)
    view.findViewById<TextView>(R.id.notesSnippet).text = body.context.getString(R.string.notes_default_content)
    view.findViewById<TextView>(R.id.notesWhen).text = ""
}

private fun bindStoragePreview(inflater: LayoutInflater, card: COUICardView, body: LinearLayout) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_storage_card))
    val view = inflater.inflate(R.layout.card_storage, body, true)
    view.findViewById<StorageUsageBar>(R.id.storageBar).progress = 0.42f
    view.findViewById<TextView>(R.id.storageUsage).text = formatStorageUsage(5_400_000_000L, 12_800_000_000L)
}

private fun bindRecorderPreview(
    inflater: LayoutInflater,
    card: COUICardView,
    body: LinearLayout,
    density: Float,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_recorder_card))
    card.setContentPadding(
        (10 * density).toInt(),
        (10 * density).toInt(),
        (10 * density).toInt(),
        (8 * density).toInt(),
    )
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
    density: Float,
) {
    val pad = (8 * density).toInt()
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_flashlight_off))
    card.setContentPadding(pad, pad, pad, pad)
    val view = inflater.inflate(R.layout.card_flashlight, body, true)
    val labelColor = body.context.getColor(R.color.hiboard_flashlight_label_off)
    view.findViewById<TextView>(R.id.flashlightLabel).setTextColor(labelColor)
    view.findViewById<ImageView>(R.id.flashlightGlow).visibility = View.INVISIBLE
    view.findViewById<ImageView>(R.id.flashlightIcon).imageTintList =
        ColorStateList.valueOf(body.context.getColor(R.color.hiboard_flashlight_icon_off))
    view.findViewById<TextView>(R.id.flashlightState).apply {
        setTextColor(labelColor)
        text = context.getString(R.string.flashlight_off)
    }
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
