package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import gd.app.hiboard.engine.clockHands
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Analog face driven by the device clock. */
class SystemClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1A1A1A.toInt()
        textAlign = Paint.Align.CENTER
    }
    private val hourTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF696969.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val minuteTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE4E4E4.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2F313D.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_second)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2F313D.toInt()
        style = Paint.Style.FILL
    }
    private val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_second)
        style = Paint.Style.FILL
    }
    private val glyphProbe = Rect()

    private val tick = object : Runnable {
        override fun run() {
            invalidate()
            val delay = 1000L - (System.currentTimeMillis() % 1000L)
            postDelayed(this, delay.coerceAtLeast(16L))
        }
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
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f
        drawTicks(canvas, cx, cy, radius)
        drawNumbers(canvas, cx, cy, radius)
        val now = Calendar.getInstance()
        val hands = clockHands(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )
        val stroke = (radius * 0.063f).coerceAtLeast(1f)
        handPaint.strokeWidth = stroke
        secondPaint.strokeWidth = (radius * 0.016f).coerceAtLeast(1f)
        drawHand(canvas, cx, cy, hands.hourDegrees, radius * 0.86f, radius * 0.04f, handPaint)
        drawHand(canvas, cx, cy, hands.minuteDegrees, radius * 0.86f, radius * 0.04f, handPaint)
        canvas.drawCircle(cx, cy, radius * 0.095f, hubPaint)
        drawHand(canvas, cx, cy, hands.secondDegrees, radius * 0.96f, radius * 0.27f, secondPaint)
        canvas.drawCircle(cx, cy, radius * 0.055f, pinPaint)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val outer = radius * 0.968f
        val hourLen = radius * 0.145f
        val minuteLen = radius * 0.055f
        val stroke = (radius * 0.012f).coerceAtLeast(1f)
        hourTickPaint.strokeWidth = stroke
        minuteTickPaint.strokeWidth = stroke * 0.7f
        for (index in 0 until 60) {
            val angle = Math.toRadians(index * 6.0 - 90.0)
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            val major = index % 5 == 0
            val length = if (major) hourLen else minuteLen
            canvas.drawLine(
                cx + outer * cosA,
                cy + outer * sinA,
                cx + (outer - length) * cosA,
                cy + (outer - length) * sinA,
                if (major) hourTickPaint else minuteTickPaint,
            )
        }
    }

    private fun drawNumbers(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        numberPaint.textSize = 100f
        numberPaint.getTextBounds("8", 0, 1, glyphProbe)
        val glyph = glyphProbe.height().toFloat().coerceAtLeast(1f)
        numberPaint.textSize *= radius * 0.135f / glyph
        val metrics = numberPaint.fontMetrics
        val textDy = -(metrics.ascent + metrics.descent) / 2f
        val numberRadius = radius * 0.76f
        for (hour in 1..12) {
            val angle = Math.toRadians(hour * 30.0 - 90.0)
            val x = cx + (cos(angle) * numberRadius).toFloat()
            val y = cy + (sin(angle) * numberRadius).toFloat() + textDy
            canvas.drawText(hour.toString(), x, y, numberPaint)
        }
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

fun bindClockCard(
    card: COUICardView,
    body: LinearLayout,
    onOpen: (() -> Unit)?,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_calendar_card))
    card.setContentPadding(0, 0, 0, 0)
    val view = LayoutInflater.from(body.context).inflate(R.layout.card_clock, body, true)
    if (onOpen != null) {
        val open = View.OnClickListener { onOpen.invoke() }
        view.setOnClickListener(open)
        card.setOnClickListener(open)
    }
}
