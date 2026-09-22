package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.ClockHands
import gd.app.hiboard.engine.WeatherCondition
import gd.app.hiboard.engine.clockHands
import gd.app.hiboard.engine.weatherClockDate
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class WeatherClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val dateView: TextView
    private val hourView: TextView
    private val minuteView: TextView
    private val iconView: ImageView
    private val labelView: TextView
    private val ticks: WeatherClockTicks
    private val handsView: WeatherClockHands

    private val tick = object : Runnable {
        override fun run() {
            render()
            val delay = 1000L - (System.currentTimeMillis() % 1000L)
            postDelayed(this, delay.coerceAtLeast(16L))
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.card_weather_clock, this, true)
        dateView = findViewById(R.id.weatherClockDate)
        hourView = findViewById(R.id.weatherClockHour)
        minuteView = findViewById(R.id.weatherClockMinute)
        iconView = findViewById(R.id.weatherClockIcon)
        labelView = findViewById(R.id.weatherClockLabel)
        ticks = findViewById(R.id.weatherClockTicks)
        handsView = findViewById(R.id.weatherClockHands)
        render()
    }

    fun setWeather(condition: WeatherCondition, temperatureC: Int) {
        iconView.setImageResource(weatherClockIcon(condition))
        labelView.text = "${condition.label()} $temperatureC°"
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        removeCallbacks(tick)
        post(tick)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tick)
        super.onDetachedFromWindow()
    }

    private fun render() {
        val now = Calendar.getInstance()
        dateView.text = weatherClockDate(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH),
            Locale.getDefault(),
        )
        hourView.text = String.format(Locale.US, "%02d", now.get(Calendar.HOUR_OF_DAY))
        minuteView.text = String.format(Locale.US, "%02d", now.get(Calendar.MINUTE))
        handsView.hands = clockHands(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )
        ticks.invalidate()
        handsView.invalidate()
    }
}

class WeatherClockTicks @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : android.view.View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_weekday)
        strokeWidth = 1.2f * density
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val inset = 8f * density
        val rx = width / 2f - inset
        val ry = height / 2f - inset
        for (index in 0 until 60) {
            val angle = Math.toRadians(index * 6.0 - 90.0)
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            val scale = if (index % 5 == 0) 0.88f else 0.94f
            canvas.drawLine(
                cx + rx * scale * cosA,
                cy + ry * scale * sinA,
                cx + rx * cosA,
                cy + ry * sinA,
                paint,
            )
        }
    }
}

class WeatherClockHands @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : android.view.View(context, attrs) {

    var hands: ClockHands = clockHands(0, 0, 0)

    private val density = resources.displayMetrics.density
    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_title)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.2f * density
    }
    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_title)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2.4f * density
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_today)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 1.3f * density
    }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_today)
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val reach = min(width, height) / 2f
        drawHand(canvas, cx, cy, hands.hourDegrees, reach * 0.34f, hourPaint)
        drawHand(canvas, cx, cy, hands.minuteDegrees, reach * 0.48f, minutePaint)
        drawHand(canvas, cx, cy, hands.secondDegrees, reach * 0.62f, secondPaint)
        canvas.drawCircle(cx, cy, 3.4f * density, hubPaint)
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, degrees: Float, length: Float, paint: Paint) {
        canvas.save()
        canvas.rotate(degrees, cx, cy)
        canvas.drawLine(cx, cy, cx, cy - length, paint)
        canvas.restore()
    }
}

fun bindWeatherClock(
    card: COUICardView,
    body: LinearLayout,
    condition: WeatherCondition,
    temperatureC: Int,
    onOpen: (() -> Unit)?,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_calendar_card))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val clock = WeatherClockView(body.context)
    clock.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
    )
    clock.setWeather(condition, temperatureC)
    body.addView(clock)
    val open = android.view.View.OnClickListener { onOpen?.invoke() }
    clock.setOnClickListener(open)
    card.setOnClickListener(open)
}

private fun weatherClockIcon(condition: WeatherCondition): Int = when (condition) {
    WeatherCondition.Sunny -> R.drawable.ic_weather_sunny
    WeatherCondition.Cloudy -> R.drawable.ic_weather_cloudy
    WeatherCondition.Rain -> R.drawable.ic_weather_rain
    WeatherCondition.Thunder -> R.drawable.ic_weather_thunder
    WeatherCondition.Snow -> R.drawable.ic_weather_snow
    WeatherCondition.Fog -> R.drawable.ic_weather_fog
    WeatherCondition.Night -> R.drawable.ic_weather_night
}
