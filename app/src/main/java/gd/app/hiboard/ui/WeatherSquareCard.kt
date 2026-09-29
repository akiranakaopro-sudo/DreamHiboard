package gd.app.hiboard.ui

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.WeatherCondition

fun bindWeatherSquare(
    card: COUICardView,
    body: LinearLayout,
    location: String,
    condition: WeatherCondition,
    summary: String,
    temperatureC: Int,
    lowC: Int,
    highC: Int,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_weather_fallback))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val view = LayoutInflater.from(body.context).inflate(R.layout.card_weather_square, body, true)
    view.findViewById<ImageView>(R.id.weatherSquareBackground).setImageResource(weatherBackgroundRes(condition))
    view.findViewById<ImageView>(R.id.weatherSquareIcon).setImageResource(weatherIconRes(condition))
    view.findViewById<TextView>(R.id.weatherSquareLocation).text = location
    view.findViewById<TextView>(R.id.weatherSquareTemp).text = "$temperatureC°"
    view.findViewById<TextView>(R.id.weatherSquareSummary).text = summary
    view.findViewById<TextView>(R.id.weatherSquareRange).text = "$lowC° / $highC°"
}
