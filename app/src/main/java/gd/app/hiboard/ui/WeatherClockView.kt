package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
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
    private val statusView: LinearLayout
    private val ticks: WeatherClockTicks
    private val handsView: WeatherClockHands
    private val heavy = Typeface.create("sans-serif-medium", Typeface.BOLD)
    private val glyphProbe = Rect()
    private var fitted = 0

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
        statusView = findViewById(R.id.weatherClockStatus)
        ticks = findViewById(R.id.weatherClockTicks)
        handsView = findViewById(R.id.weatherClockHands)
        hourView.typeface = heavy
        minuteView.typeface = heavy
        render()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val key = w * 100000 + h
        if (w == 0 || h == 0 || key == fitted) return
        fitted = key
        fitGlyph(hourView, h * 0.30f, "8")
        fitGlyph(minuteView, h * 0.30f, "8")
        fitGlyph(dateView, h * 0.046f, "8")
        fitGlyph(labelView, h * 0.052f, "8")
        val towardCenter = w * 0.023f
        hourView.translationX = towardCenter
        minuteView.translationX = -towardCenter
        dateView.translationY = h * 0.205f
        statusView.translationY = h * 0.18f
        val icon = (h * 0.075f).toInt().coerceAtLeast(1)
        iconView.layoutParams = iconView.layoutParams.apply {
            width = icon
            height = icon
        }
    }

    private fun fitGlyph(view: TextView, glyphHeight: Float, sample: String) {
        val paint = view.paint
        val previous = paint.textSize
        paint.textSize = 100f
        paint.getTextBounds(sample, 0, sample.length, glyphProbe)
        val glyph = glyphProbe.height().toFloat().coerceAtLeast(1f)
        paint.textSize = previous
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, 100f * glyphHeight / glyph)
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
        colorDate(
            weatherClockDate(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH),
                Locale.getDefault(),
            ),
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

    private fun colorDate(text: String) {
        val gap = text.lastIndexOf(' ')
        if (gap <= 0) {
            dateView.text = text
            return
        }
        val span = SpannableString(text)
        val accent = context.getColor(R.color.hiboard_clock_second)
        span.setSpan(ForegroundColorSpan(accent), gap + 1, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        dateView.text = span
    }
}

class WeatherClockTicks @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : android.view.View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val majorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val minorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFC8C8C8.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        if (width <= 0 || height <= 0) return
        val unit = min(width, height).toFloat()
        val inset = unit * 0.057f
        val majorLen = unit * 0.120f
        val minorLen = unit * 0.044f
        val stroke = (unit * 0.006f).coerceAtLeast(1f)
        majorPaint.strokeWidth = stroke
        minorPaint.strokeWidth = stroke
        val corner = (16f * density - inset).coerceAtLeast(0f)
        val cx = width / 2f
        val cy = height / 2f
        for (index in 0 until 60) {
            val angle = Math.toRadians(index * 6.0)
            val dx = sin(angle).toFloat()
            val dy = (-cos(angle)).toFloat()
            val major = index % 5 == 0
            val outer = clockFacePoint(width, height, cx, cy, inset, corner, dx, dy)
            val length = if (major) majorLen else minorLen
            canvas.drawLine(
                outer[0],
                outer[1],
                outer[0] - dx * length,
                outer[1] - dy * length,
                if (major) majorPaint else minorPaint,
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
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.4f * density
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_second)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 1.2f * density
    }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_second)
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val reach = min(width, height) / 2f
        drawHand(canvas, cx, cy, hands.hourDegrees, reach * 0.66f, reach * 0.05f, handPaint)
        drawHand(canvas, cx, cy, hands.minuteDegrees, reach * 0.66f, reach * 0.05f, handPaint)
        drawHand(canvas, cx, cy, hands.secondDegrees, reach * 0.73f, reach * 0.18f, secondPaint)
        canvas.drawCircle(cx, cy, 4f * density, hubPaint)
    }

    private fun drawHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        degrees: Float,
        length: Float,
        tail: Float,
        paint: Paint,
    ) {
        canvas.save()
        canvas.rotate(degrees, cx, cy)
        canvas.drawLine(cx, cy + tail, cx, cy - length, paint)
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
    if (onOpen != null) {
        val open = android.view.View.OnClickListener { onOpen.invoke() }
        clock.setOnClickListener(open)
        card.setOnClickListener(open)
    }
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
