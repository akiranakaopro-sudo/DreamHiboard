package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.WeatherCondition
import gd.app.hiboard.engine.clockHands
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class DialFace {
    /** Rounded-square tick ring with Roman numerals at the quarters. */
    Roman,

    /** Twelve radial ticks with today's weather under the hub. */
    Weather,
}

/** Square analog face driven by the device clock. */
class DialClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val face: DialFace = DialFace.Roman,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val majorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1A1A1A.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val minorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFD6D6D6.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val numeralPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF45474F.toInt()
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2B2D38.toInt()
        style = Paint.Style.FILL
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_second)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val secondFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_second)
        style = Paint.Style.FILL
    }
    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0046CC.toInt()
        style = Paint.Style.FILL
    }
    private val handPath = Path()
    private val rect = RectF()
    private val glyphProbe = Rect()
    private var weatherIcon: Drawable? = null
    private var temperatureText = ""

    private val tick = object : Runnable {
        override fun run() {
            invalidate()
            val delay = 1000L - (System.currentTimeMillis() % 1000L)
            postDelayed(this, delay.coerceAtLeast(16L))
        }
    }

    fun setWeather(condition: WeatherCondition, temperatureC: Int) {
        weatherIcon = context.getDrawable(weatherClockIcon(condition))
        temperatureText = "$temperatureC°"
        invalidate()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val unit = min(width, height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        when (face) {
            DialFace.Roman -> {
                drawSquareTicks(canvas, unit, cx, cy)
                drawNumerals(canvas, unit, cx, cy)
            }
            DialFace.Weather -> {
                drawRadialTicks(canvas, unit, cx, cy)
                drawWeather(canvas, unit, cx, cy)
            }
        }
        val now = Calendar.getInstance()
        val hands = clockHands(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )
        val reach = unit / 2f
        handPaint.setShadowLayer(reach * 0.03f, reach * 0.008f, reach * 0.016f, 0x33000000)
        drawNeckedHand(canvas, cx, cy, hands.hourDegrees, reach * 0.46f, reach * 0.07f, reach)
        drawNeckedHand(canvas, cx, cy, hands.minuteDegrees, reach * 0.62f, reach * 0.07f, reach)
        handPaint.clearShadowLayer()
        drawSecond(canvas, cx, cy, hands.secondDegrees, reach)
        canvas.drawCircle(cx, cy, reach * 0.065f, handPaint)
        canvas.drawCircle(cx, cy, reach * 0.046f, secondFill)
        canvas.drawCircle(cx, cy, reach * 0.022f, pinPaint)
    }

    private fun drawSquareTicks(canvas: Canvas, unit: Float, cx: Float, cy: Float) {
        val inset = unit * 0.032f
        val majorLen = unit * 0.092f
        val minorLen = unit * 0.036f
        majorPaint.strokeWidth = (unit * 0.0065f).coerceAtLeast(1f)
        minorPaint.strokeWidth = (unit * 0.0055f).coerceAtLeast(1f)
        val corner = (16f * density - inset).coerceAtLeast(0f)
        for (index in 0 until 120) {
            val angle = Math.toRadians(index * 3.0)
            val dx = sin(angle).toFloat()
            val dy = (-cos(angle)).toFloat()
            val major = index % 10 == 0
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

    private fun drawRadialTicks(canvas: Canvas, unit: Float, cx: Float, cy: Float) {
        val inner = unit * 0.35f
        val outer = unit * 0.47f
        majorPaint.strokeWidth = (unit * 0.0065f).coerceAtLeast(1f)
        for (hour in 0 until 12) {
            val angle = Math.toRadians(hour * 30.0)
            val dx = sin(angle).toFloat()
            val dy = (-cos(angle)).toFloat()
            canvas.drawLine(cx + dx * inner, cy + dy * inner, cx + dx * outer, cy + dy * outer, majorPaint)
        }
    }

    private fun drawNumerals(canvas: Canvas, unit: Float, cx: Float, cy: Float) {
        fitGlyph(numeralPaint, "X", unit * 0.072f)
        val baselineDy = glyphProbe.height() / 2f
        val side = unit * 0.298f
        canvas.drawText("XII", cx, cy - unit * 0.305f + baselineDy, numeralPaint)
        canvas.drawText("III", cx + side, cy + baselineDy, numeralPaint)
        canvas.drawText("VI", cx, cy + unit * 0.32f + baselineDy, numeralPaint)
        canvas.drawText("IX", cx - side, cy + baselineDy, numeralPaint)
    }

    private fun drawWeather(canvas: Canvas, unit: Float, cx: Float, cy: Float) {
        if (temperatureText.isEmpty()) return
        fitGlyph(tempPaint, "8", unit * 0.062f)
        val rowY = cy + unit * 0.195f
        val icon = unit * 0.095f
        val gap = unit * 0.03f
        val textW = tempPaint.measureText(temperatureText)
        val left = cx - (icon + gap + textW) / 2f
        weatherIcon?.let {
            it.setBounds(left.toInt(), (rowY - icon / 2f).toInt(), (left + icon).toInt(), (rowY + icon / 2f).toInt())
            it.draw(canvas)
        }
        canvas.drawText(temperatureText, left + icon + gap, rowY + glyphProbe.height() / 2f, tempPaint)
    }

    /** Sizes [paint] so [sample]'s glyph is [glyphHeight] tall; leaves its bounds in [glyphProbe]. */
    private fun fitGlyph(paint: Paint, sample: String, glyphHeight: Float) {
        paint.textSize = 100f
        paint.getTextBounds(sample, 0, sample.length, glyphProbe)
        val glyph = glyphProbe.height().toFloat().coerceAtLeast(1f)
        paint.textSize = 100f * glyphHeight / glyph
        paint.getTextBounds(sample, 0, sample.length, glyphProbe)
    }

    /** Thin neck out of the hub, short taper, then a thick body with a rounded tip. */
    private fun drawNeckedHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        degrees: Float,
        length: Float,
        bodyWidth: Float,
        reach: Float,
    ) {
        val neckHalf = reach * 0.012f
        val bodyHalf = bodyWidth / 2f
        val neckEnd = cy - reach * 0.10f
        val bodyStart = neckEnd - bodyWidth * 0.6f
        val tipY = cy - length
        handPath.reset()
        handPath.moveTo(cx - neckHalf, cy)
        handPath.lineTo(cx - neckHalf, neckEnd)
        handPath.lineTo(cx - bodyHalf, bodyStart)
        handPath.lineTo(cx - bodyHalf, tipY + bodyHalf)
        handPath.arcTo(cx - bodyHalf, tipY, cx + bodyHalf, tipY + bodyWidth, 180f, 180f, false)
        handPath.lineTo(cx + bodyHalf, bodyStart)
        handPath.lineTo(cx + neckHalf, neckEnd)
        handPath.lineTo(cx + neckHalf, cy)
        handPath.close()
        canvas.save()
        canvas.rotate(degrees, cx, cy)
        canvas.drawPath(handPath, handPaint)
        canvas.restore()
    }

    /** Hairline reaching the tick ring, with a thicker rounded tail below the hub. */
    private fun drawSecond(canvas: Canvas, cx: Float, cy: Float, degrees: Float, reach: Float) {
        secondPaint.strokeWidth = (reach * 0.012f).coerceAtLeast(1.2f * density)
        canvas.save()
        canvas.rotate(degrees, cx, cy)
        canvas.drawLine(cx, cy + reach * 0.08f, cx, cy - reach * 0.95f, secondPaint)
        val half = reach * 0.014f
        rect.set(cx - half, cy + reach * 0.06f, cx + half, cy + reach * 0.22f)
        canvas.drawRoundRect(rect, half, half, secondFill)
        canvas.restore()
    }
}

fun bindRomanClock(
    card: COUICardView,
    body: LinearLayout,
    onOpen: (() -> Unit)?,
) {
    bindDialClock(card, body, DialClockView(body.context, face = DialFace.Roman), onOpen)
}

fun bindWeatherDialClock(
    card: COUICardView,
    body: LinearLayout,
    condition: WeatherCondition,
    temperatureC: Int,
    onOpen: (() -> Unit)?,
) {
    val clock = DialClockView(body.context, face = DialFace.Weather)
    clock.setWeather(condition, temperatureC)
    bindDialClock(card, body, clock, onOpen)
}

private fun bindDialClock(
    card: COUICardView,
    body: LinearLayout,
    clock: DialClockView,
    onOpen: (() -> Unit)?,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_clock_card))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    clock.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
    )
    body.addView(clock)
    if (onOpen != null) {
        val open = View.OnClickListener { onOpen.invoke() }
        clock.setOnClickListener(open)
        card.setOnClickListener(open)
    }
}
