package gd.app.hiboard.ui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Remaining-battery ring for the status row.
 * 0% starts at 12 o'clock; fill advances clockwise like a clock hand.
 */
class BatteryRemainRingDrawable(
    private val fillColor: Int,
    private val trackColor: Int,
) : Drawable() {

    var levelPercent: Int = 100
        set(value) {
            val next = value.coerceIn(0, 100)
            if (field == next) return
            field = next
            invalidateSelf()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        color = trackColor
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        color = fillColor
    }
    private val oval = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val stroke = min(bounds.width(), bounds.height()) * 0.18f
        trackPaint.strokeWidth = stroke
        fillPaint.strokeWidth = stroke
        val inset = stroke / 2f + 0.5f
        oval.set(
            bounds.left + inset,
            bounds.top + inset,
            bounds.right - inset,
            bounds.bottom - inset,
        )
    }

    override fun draw(canvas: Canvas) {
        if (oval.isEmpty) return
        canvas.drawOval(oval, trackPaint)
        val sweep = 360f * (levelPercent / 100f)
        if (sweep <= 0f) return
        // Canvas arcs: -90° is 12 o'clock; positive sweep is clockwise.
        canvas.drawArc(oval, -90f, sweep, false, fillPaint)
    }

    override fun setAlpha(alpha: Int) {
        trackPaint.alpha = alpha
        fillPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        trackPaint.colorFilter = colorFilter
        fillPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
