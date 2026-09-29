package gd.app.hiboard.ui

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import gd.app.hiboard.R
import gd.app.hiboard.engine.WeatherCondition

internal data class WeatherWideDay(
    val label: String,
    val condition: WeatherCondition,
    val lowC: Int,
    val highC: Int,
)

/** Fills an inflated `card_weather` layout; shared by the board card and the store preview. */
internal fun bindWeatherWide(
    view: View,
    location: String,
    summary: String,
    condition: WeatherCondition,
    temperatureC: Int,
    days: List<WeatherWideDay>,
) {
    view.findViewById<ImageView>(R.id.weatherBackground).setImageResource(weatherBackgroundRes(condition))
    view.findViewById<TextView>(R.id.weatherLocation).text = location
    view.findViewById<TextView>(R.id.weatherSummary).text = summary
    view.findViewById<ImageView>(R.id.weatherConditionIcon).setImageResource(weatherIconRes(condition))
    view.findViewById<TextView>(R.id.weatherTemp).text = "$temperatureC°"
    val forecast = view.findViewById<LinearLayout>(R.id.weatherForecast)
    forecast.removeAllViews()
    val inflater = LayoutInflater.from(view.context)
    val slashColor = view.context.getColor(R.color.hiboard_weather_range_slash)
    days.forEach { day ->
        val item = inflater.inflate(R.layout.item_weather_day, forecast, false)
        item.findViewById<TextView>(R.id.weatherDayLabel).text = day.label
        item.findViewById<ImageView>(R.id.weatherDayIcon).setImageResource(weatherIconRes(day.condition))
        item.findViewById<TextView>(R.id.weatherDayRange).text = weatherRange(day.lowC, day.highC, slashColor)
        forecast.addView(item)
    }
}

private fun weatherRange(lowC: Int, highC: Int, slashColor: Int): CharSequence {
    val text = SpannableStringBuilder("$lowC°")
    val slashStart = text.length
    text.append(" / ")
    text.setSpan(ForegroundColorSpan(slashColor), slashStart, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    text.append("$highC°")
    return text
}
