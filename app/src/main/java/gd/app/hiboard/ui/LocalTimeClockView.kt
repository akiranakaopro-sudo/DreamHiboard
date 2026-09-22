package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.coui.appcompat.cardview.COUICardView
import gd.app.hiboard.R
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Square system clock: hour over minute, with tick marks around the edge. */
class LocalTimeClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val face: LocalTimeFace

    private val tick = object : Runnable {
        override fun run() {
            render()
            val delay = 1000L - (System.currentTimeMillis() % 1000L)
            postDelayed(this, delay.coerceAtLeast(16L))
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.card_local_time_clock, this, true)
        face = findViewById(R.id.localTimeFace)
        render()
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
        face.setTime(
            String.format(Locale.US, "%02d", now.get(Calendar.HOUR_OF_DAY)),
            String.format(Locale.US, "%02d", now.get(Calendar.MINUTE)),
        )
    }
}

/**
 * Oppo local-time face: 60 ticks on a rounded-rect perimeter (hour marks longer
 * and black, minute marks shorter and gray) and heavy hour-over-minute digits.
 */
class LocalTimeFace @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var hour = "--"
    private var minute = "--"

    private val density = resources.displayMetrics.density
    private val majorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val minorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFC6C6C6.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
    }
    private val probe = Rect()

    fun setTime(hour: String, minute: String) {
        if (this.hour == hour && this.minute == minute) return
        this.hour = hour
        this.minute = minute
        contentDescription = "$hour:$minute"
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (width <= 0 || height <= 0) return
        drawTicks(canvas)
        drawDigits(canvas)
    }

    private fun drawTicks(canvas: Canvas) {
        val unit = min(width, height).toFloat()
        val inset = unit * 0.028f
        val majorLen = unit * 0.091f
        val minorLen = unit * 0.039f
        val stroke = (unit * 0.0045f).coerceAtLeast(1f)
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
            val outer = perimeterPoint(cx, cy, inset, corner, dx, dy)
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

    private fun perimeterPoint(
        cx: Float,
        cy: Float,
        inset: Float,
        radius: Float,
        dx: Float,
        dy: Float,
    ): FloatArray {
        val left = inset
        val top = inset
        val right = width - inset
        val bottom = height - inset
        val rad = radius.coerceAtMost(min(right - left, bottom - top) / 2f)
        if (dy < -1e-4f) {
            val t = (top - cy) / dy
            val x = cx + dx * t
            if (x in left + rad..right - rad) return floatArrayOf(x, top)
        }
        if (dy > 1e-4f) {
            val t = (bottom - cy) / dy
            val x = cx + dx * t
            if (x in left + rad..right - rad) return floatArrayOf(x, bottom)
        }
        if (dx > 1e-4f) {
            val t = (right - cx) / dx
            val y = cy + dy * t
            if (y in top + rad..bottom - rad) return floatArrayOf(right, y)
        }
        if (dx < -1e-4f) {
            val t = (left - cx) / dx
            val y = cy + dy * t
            if (y in top + rad..bottom - rad) return floatArrayOf(left, y)
        }
        val ccx = if (dx >= 0f) right - rad else left + rad
        val ccy = if (dy >= 0f) bottom - rad else top + rad
        val ox = cx - ccx
        val oy = cy - ccy
        val b = 2f * (ox * dx + oy * dy)
        val c = ox * ox + oy * oy - rad * rad
        val disc = b * b - 4f * c
        if (disc > 0f && rad > 0f) {
            val t = (-b + sqrt(disc)) / 2f
            return floatArrayOf(cx + dx * t, cy + dy * t)
        }
        return floatArrayOf(cx + dx * (right - left) / 2f, cy + dy * (bottom - top) / 2f)
    }

    private fun drawDigits(canvas: Canvas) {
        textPaint.textSize = 100f
        textPaint.getTextBounds("8", 0, 1, probe)
        val glyph = probe.height().toFloat().coerceAtLeast(1f)
        textPaint.textSize *= height * 0.292f / glyph
        textPaint.getTextBounds("8", 0, 1, probe)
        val glyphH = probe.height().toFloat()
        val gap = height * 0.096f
        val hourTop = (height - glyphH * 2f - gap) / 2f
        val hourBaseline = hourTop - probe.top
        val minuteBaseline = hourTop + glyphH + gap - probe.top
        val cx = width / 2f
        canvas.drawText(hour, cx, hourBaseline, textPaint)
        canvas.drawText(minute, cx, minuteBaseline, textPaint)
    }
}

fun bindLocalTimeClock(
    card: COUICardView,
    body: LinearLayout,
    onOpen: (() -> Unit)?,
) {
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_calendar_card))
    card.setContentPadding(0, 0, 0, 0)
    card.clipToOutline = true
    val clock = LocalTimeClockView(body.context)
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
