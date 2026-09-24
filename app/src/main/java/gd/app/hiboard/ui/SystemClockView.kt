package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
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

    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.hiboard_clock_face)
        style = Paint.Style.FILL
    }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFD5D8DE.toInt()
        style = Paint.Style.STROKE
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
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
    private val handFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2F313D.toInt()
        style = Paint.Style.FILL
    }
    private val handPath = Path()
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
        val face = min(width, height) / 2f * 0.82f
        rimPaint.strokeWidth = (face * 0.012f).coerceAtLeast(1f)
        canvas.drawCircle(cx, cy, face, facePaint)
        canvas.drawCircle(cx, cy, face, rimPaint)
        drawTicks(canvas, cx, cy, face)
        drawNumbers(canvas, cx, cy, face)
        val now = Calendar.getInstance()
        val hands = clockHands(
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND),
        )
        val shadow = face * 0.04f
        handFill.setShadowLayer(shadow, face * 0.012f, face * 0.02f, 0x33000000)
        drawTaperedHand(canvas, cx, cy, hands.hourDegrees, face * 0.56f, face * 0.078f, face * 0.046f)
        drawTaperedHand(canvas, cx, cy, hands.minuteDegrees, face * 0.78f, face * 0.062f, face * 0.036f)
        handFill.clearShadowLayer()
        canvas.drawCircle(cx, cy, face * 0.082f, hubPaint)
        secondPaint.strokeWidth = (face * 0.014f).coerceAtLeast(1f)
        drawHand(canvas, cx, cy, hands.secondDegrees, face * 0.92f, face * 0.16f, secondPaint)
        canvas.drawCircle(cx, cy, face * 0.051f, pinPaint)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val outer = radius * 0.93f
        val hourLen = radius * 0.105f
        val minuteLen = radius * 0.042f
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
        numberPaint.textSize *= radius * 0.115f / glyph
        val metrics = numberPaint.fontMetrics
        val textDy = -(metrics.ascent + metrics.descent) / 2f
        val numberRadius = radius * 0.73f
        for (hour in 1..12) {
            val angle = Math.toRadians(hour * 30.0 - 90.0)
            val x = cx + (cos(angle) * numberRadius).toFloat()
            val y = cy + (sin(angle) * numberRadius).toFloat() + textDy
            canvas.drawText(hour.toString(), x, y, numberPaint)
        }
    }

    private fun drawTaperedHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        degrees: Float,
        length: Float,
        baseWidth: Float,
        tipWidth: Float,
    ) {
        canvas.save()
        canvas.rotate(degrees, cx, cy)
        val baseY = cy + length * 0.04f
        val tipY = cy - length
        val tipR = tipWidth / 2f
        handPath.rewind()
        handPath.moveTo(cx - baseWidth / 2f, baseY)
        handPath.lineTo(cx - tipR, tipY)
        handPath.quadTo(cx, tipY - tipR, cx + tipR, tipY)
        handPath.lineTo(cx + baseWidth / 2f, baseY)
        handPath.close()
        canvas.drawPath(handPath, handFill)
        canvas.restore()
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
    card.setCardBackgroundColor(body.context.getColor(R.color.hiboard_clock_card))
    card.setContentPadding(0, 0, 0, 0)
    val view = LayoutInflater.from(body.context).inflate(R.layout.card_clock, body, true)
    if (onOpen != null) {
        val open = View.OnClickListener { onOpen.invoke() }
        view.setOnClickListener(open)
        card.setOnClickListener(open)
    }
}
