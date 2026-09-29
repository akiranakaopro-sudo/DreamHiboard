package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import gd.app.hiboard.R
import kotlin.math.cos
import kotlin.math.sin

class StorageUsageRing @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var progress: Float = 0f
        set(value) {
            val next = value.coerceIn(0f, 1f)
            if (field == next) return
            field = next
            rebuildGradient()
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = context.getColor(R.color.hiboard_storage_ring_track)
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        // BUTT + solid end dots: ROUND + SweepGradient samples wrong colors in the cap.
        strokeCap = Paint.Cap.BUTT
    }
    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bounds = RectF()
    private val hardColor = context.getColor(R.color.hiboard_storage_ring_start)
    private val midColor = context.getColor(R.color.hiboard_storage_ring_mid)
    private val softColor = context.getColor(R.color.hiboard_storage_ring_end)
    private var strokeWidth = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        strokeWidth = minOf(w, h) * 0.10f
        trackPaint.strokeWidth = strokeWidth
        fillPaint.strokeWidth = strokeWidth
        val inset = strokeWidth / 2f + 1f
        bounds.set(inset, inset, w - inset, h - inset)
        rebuildGradient()
    }

    override fun onDraw(canvas: Canvas) {
        if (bounds.isEmpty) return
        canvas.drawOval(bounds, trackPaint)
        val sweepAngle = 360f * progress
        if (sweepAngle <= 0f || strokeWidth <= 0f) return

        val cx = bounds.centerX()
        val cy = bounds.centerY()
        val radius = bounds.width() / 2f
        val capRadius = strokeWidth / 2f

        canvas.save()
        // Start at 6.5 o'clock and draw clockwise.
        canvas.rotate(-90f + START_ANGLE_FROM_TOP, cx, cy)
        canvas.drawArc(bounds, 0f, sweepAngle, false, fillPaint)
        canvas.restore()

        // Solid caps so SweepGradient cannot tint the round heads oddly.
        val startRad = Math.toRadians(START_ANGLE_FROM_TOP.toDouble())
        val endRad = Math.toRadians((START_ANGLE_FROM_TOP + sweepAngle).toDouble())
        capPaint.color = hardColor
        canvas.drawCircle(
            cx + radius * sin(startRad).toFloat(),
            cy - radius * cos(startRad).toFloat(),
            capRadius,
            capPaint,
        )
        capPaint.color = softColor
        canvas.drawCircle(
            cx + radius * sin(endRad).toFloat(),
            cy - radius * cos(endRad).toFloat(),
            capRadius,
            capPaint,
        )
    }

    private fun rebuildGradient() {
        if (bounds.isEmpty) return
        val cx = bounds.centerX()
        val cy = bounds.centerY()
        // Pack hard→soft into the visible arc length.
        val end = progress.coerceIn(0.02f, 1f)
        fillPaint.shader = SweepGradient(
            cx,
            cy,
            intArrayOf(hardColor, midColor, softColor, softColor),
            floatArrayOf(0f, end * 0.45f, end, 1f),
        )
    }

    private companion object {
        /** Degrees clockwise from 12 o'clock — 6.5 o'clock. */
        const val START_ANGLE_FROM_TOP = 195f
    }
}
