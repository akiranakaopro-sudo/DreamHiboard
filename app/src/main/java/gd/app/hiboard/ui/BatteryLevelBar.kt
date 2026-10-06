package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
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
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = context.getColor(R.color.hiboard_battery_level_track_rim)
        strokeWidth = resources.displayMetrics.density * 1.25f
    }
    private val fillStart = context.getColor(R.color.hiboard_battery_level_fill)
    private val fillEnd = context.getColor(R.color.hiboard_battery_level_fill_end)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val trackRect = RectF()
    private val fillRect = RectF()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val density = resources.displayMetrics.density
        val stroke = strokePaint.strokeWidth
        val radius = h / 2f
        trackRect.set(stroke / 2f, stroke / 2f, w - stroke / 2f, h - stroke / 2f)
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint)
        canvas.drawRoundRect(trackRect, radius, radius, strokePaint)
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
        fillPaint.shader = LinearGradient(
            fillRect.left,
            0f,
            fillRect.right,
            0f,
            fillStart,
            fillEnd,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(fillRect, innerH / 2f, innerH / 2f, fillPaint)
    }
}
