package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import gd.app.hiboard.R
import kotlin.math.max

/**
 * Oppo / ColorOS capsule level bar: dark track with a thin rim, green fill inset
 * so the track ring stays visible even near 100%.
 */
class BatteryLevelBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var progress: Float = 0f
        set(value) {
            val next = value.coerceIn(0f, 1f)
            if (field == next) return
            field = next
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_battery_level_track)
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_battery_level_fill)
    }
    private val trackRect = RectF()
    private val fillRect = RectF()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val density = resources.displayMetrics.density
        val radius = h / 2f
        trackRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint)
        if (progress <= 0f) return
        // ColorOS-style gap between fill pill and track rim (~3dp).
        val inset = max(3f * density, h * 0.18f)
        val innerLeft = inset
        val innerTop = inset
        val innerRight = w - inset
        val innerBottom = h - inset
        val innerH = innerBottom - innerTop
        val innerW = innerRight - innerLeft
        if (innerH <= 0f || innerW <= 0f) return
        val fillW = max(innerH, innerW * progress).coerceAtMost(innerW)
        fillRect.set(innerLeft, innerTop, innerLeft + fillW, innerBottom)
        canvas.drawRoundRect(fillRect, innerH / 2f, innerH / 2f, fillPaint)
    }
}
