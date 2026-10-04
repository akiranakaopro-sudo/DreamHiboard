package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import gd.app.hiboard.R
import gd.app.hiboard.model.BatterySample
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

private const val WINDOW_MS = 24L * 60L * 60L * 1000L
private const val MARK_STEP_MS = 4L * 60L * 60L * 1000L
/** Expected stick count across 24h; keeps bar width stable when sample count changes. */
private const val SLOT_COUNT = 49

class BatteryUsageGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var samples: List<BatterySample> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val barTop = context.getColor(R.color.hiboard_battery_bar_top)
    private val barBottom = context.getColor(R.color.hiboard_battery_bar_bottom)
    private val barChargeTop = context.getColor(R.color.hiboard_battery_bar_charge_top)
    private val barChargeBottom = context.getColor(R.color.hiboard_battery_bar_charge_bottom)
    private val labelColor = context.getColor(R.color.hiboard_battery_axis)
    private val gridColor = context.getColor(R.color.hiboard_battery_grid)
    private val nowLabel = context.getString(R.string.battery_now)

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val dashGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = gridColor
        pathEffect = DashPathEffect(floatArrayOf(4f, 6f), 0f)
    }
    private val solidGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = gridColor
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = labelColor
        textAlign = Paint.Align.CENTER
    }
    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = labelColor
        textAlign = Paint.Align.RIGHT
    }
    private val barRect = RectF()
    private val barPath = Path()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val density = resources.displayMetrics.density
        axisPaint.textSize = 11f * density
        yLabelPaint.textSize = 11f * density
        val gridStroke = max(1f, density * 0.8f)
        dashGridPaint.strokeWidth = gridStroke
        solidGridPaint.strokeWidth = gridStroke

        val yLabelWidth = yLabelPaint.measureText("100%") + 8f * density
        val xLabelHeight = axisPaint.textSize + 8f * density
        val chartLeft = 0f
        val chartRight = w - yLabelWidth
        val chartTop = 4f * density
        val chartBottom = h - xLabelHeight
        if (chartRight <= chartLeft + 8f || chartBottom <= chartTop + 8f) return

        val yTicks = intArrayOf(100, 50, 0)
        yTicks.forEach { pct ->
            val y = chartTop + (chartBottom - chartTop) * (1f - pct / 100f)
            // 100% uses a solid line; 50% / 0% stay dashed.
            val paint = if (pct == 100) solidGridPaint else dashGridPaint
            canvas.drawLine(chartLeft, y, chartRight, y, paint)
            val textY = y - (yLabelPaint.descent() + yLabelPaint.ascent()) / 2f
            canvas.drawText("$pct%", w - 2f * density, textY, yLabelPaint)
        }

        val end = System.currentTimeMillis()
        val start = end - WINDOW_MS
        val chartWidth = chartRight - chartLeft
        val slot = chartWidth / SLOT_COUNT
        val barWidth = max(2f * density, slot * 0.55f)
        val radius = barWidth / 2f

        samples.forEach { sample ->
            val t = sample.epochMillis
            if (t < start || t > end) return@forEach
            val fraction = ((t - start).toFloat() / WINDOW_MS).coerceIn(0f, 1f)
            val level = sample.levelPercent.coerceIn(0, 100) / 100f
            val cx = chartLeft + chartWidth * fraction
            val barTopY = chartBottom - (chartBottom - chartTop) * level
            // Green when this stick's level rose vs the previous sample (time-based charge).
            val topColor = if (sample.charging) barChargeTop else barTop
            val bottomColor = if (sample.charging) barChargeBottom else barBottom
            barRect.set(cx - barWidth / 2f, barTopY, cx + barWidth / 2f, chartBottom)
            barPaint.shader = LinearGradient(
                0f,
                barTopY,
                0f,
                chartBottom,
                topColor,
                bottomColor,
                Shader.TileMode.CLAMP,
            )
            barPath.reset()
            barPath.addRoundRect(barRect, floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f), Path.Direction.CW)
            canvas.drawPath(barPath, barPaint)
        }
        barPaint.shader = null

        val labelBaseline = h - 2f * density
        val labelTop = labelBaseline + axisPaint.ascent()
        val xLabels = xAxisLabels(start, end)
        xLabels.forEachIndexed { index, (fraction, text) ->
            val x = chartLeft + chartWidth * fraction
            // Dashed time partitions run through 0% down to the time label.
            canvas.drawLine(x, chartTop, x, labelTop, dashGridPaint)
            axisPaint.textAlign = when {
                index == 0 -> Paint.Align.LEFT
                index == xLabels.lastIndex -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            canvas.drawText(text, x, labelBaseline, axisPaint)
        }
        axisPaint.textAlign = Paint.Align.CENTER
    }

    /**
     * Always a fixed 24h axis ending at now. Hour marks keep the current hour's odd/even
     * parity (step 4h). If the last hour mark would sit on "Now", move it to the first seat.
     */
    private fun xAxisLabels(start: Long, end: Long): List<Pair<Float, String>> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = end
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis >= end - 30L * 60L * 1000L) {
                add(Calendar.HOUR_OF_DAY, -4)
            }
        }
        val labels = ArrayList<Pair<Float, String>>()
        var mark = cal.timeInMillis
        while (mark > start) mark -= MARK_STEP_MS
        while (mark < start) mark += MARK_STEP_MS
        while (mark < end) {
            val fraction = ((mark - start).toFloat() / WINDOW_MS).coerceIn(0f, 1f)
            cal.timeInMillis = mark
            labels += fraction to String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
            mark += MARK_STEP_MS
        }
        // e.g. "14" next to "Now" → put "14" in the first seat instead.
        if (labels.isNotEmpty() && labels.last().first >= 0.90f) {
            val hour = labels.removeAt(labels.lastIndex).second
            labels.add(0, 0f to hour)
        }
        labels += 1f to nowLabel
        return labels
    }
}
