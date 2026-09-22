package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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

    private val density = resources.displayMetrics.density
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_title)
        textAlign = Paint.Align.CENTER
        textSize = 13f * density
    }
    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_today)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.4f * density
    }
    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_today)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2.4f * density
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_today)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 1.2f * density
    }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_calendar_today)
        style = Paint.Style.FILL
    }

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
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f
        val numberRadius = radius * 0.78f
        val metrics = numberPaint.fontMetrics
        val textDy = -(metrics.ascent + metrics.descent) / 2f
        for (hour in 1..12) {
            val angle = Math.toRadians(hour * 30.0 - 90.0)
            val x = cx + (cos(angle) * numberRadius).toFloat()
            val y = cy + (sin(angle) * numberRadius).toFloat() + textDy
            canvas.drawText(hour.toString(), x, y, numberPaint)
        }
        val now = Calendar.getInstance()
        val hands = clockHands(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )
        drawHand(canvas, cx, cy, hands.hourDegrees, radius * 0.46f, hourPaint)
        drawHand(canvas, cx, cy, hands.minuteDegrees, radius * 0.68f, minutePaint)
        drawHand(canvas, cx, cy, hands.secondDegrees, radius * 0.74f, secondPaint)
        canvas.drawCircle(cx, cy, 3.2f * density, hubPaint)
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, degrees: Float, length: Float, paint: Paint) {
        canvas.save()
        canvas.rotate(degrees, cx, cy)
        canvas.drawLine(cx, cy, cx, cy - length, paint)
        canvas.restore()
    }
}

fun bindClockCard(
    card: COUICardView,
    body: LinearLayout,
    onOpen: (() -> Unit)?,
) {
    val density = body.resources.displayMetrics.density
    val pad = (8 * density).toInt()
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_calendar_card))
    card.setContentPadding(pad, pad, pad, pad)
    val view = LayoutInflater.from(body.context).inflate(R.layout.card_clock, body, true)
    if (onOpen != null) {
        val open = View.OnClickListener { onOpen.invoke() }
        view.setOnClickListener(open)
        card.setOnClickListener(open)
    }
}
