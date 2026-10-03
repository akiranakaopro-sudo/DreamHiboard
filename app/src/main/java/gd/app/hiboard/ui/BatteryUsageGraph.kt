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
    private val labelColor = context.getColor(R.color.hiboard_battery_axis)
    private val gridColor = context.getColor(R.color.hiboard_battery_grid)
    private val nowLabel = context.getString(R.string.battery_now)

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = gridColor
        pathEffect = DashPathEffect(floatArrayOf(4f, 6f), 0f)
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
        gridPaint.strokeWidth = max(1f, density * 0.8f)

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
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            val textY = y - (yLabelPaint.descent() + yLabelPaint.ascent()) / 2f
            canvas.drawText("$pct%", w - 2f * density, textY, yLabelPaint)
        }

        val points = samples.ifEmpty { return }
        val count = points.size
        val slot = (chartRight - chartLeft) / count
        val barWidth = max(2f * density, slot * 0.55f)
        val radius = barWidth / 2f

        points.forEachIndexed { index, sample ->
            val level = sample.levelPercent.coerceIn(0, 100) / 100f
            val cx = chartLeft + slot * (index + 0.5f)
            val barTopY = chartBottom - (chartBottom - chartTop) * level
            barRect.set(cx - barWidth / 2f, barTopY, cx + barWidth / 2f, chartBottom)
            barPaint.shader = LinearGradient(
                0f,
                barTopY,
                0f,
                chartBottom,
                barTop,
                barBottom,
                Shader.TileMode.CLAMP,
            )
            barPath.reset()
            barPath.addRoundRect(barRect, floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f), Path.Direction.CW)
            canvas.drawPath(barPath, barPaint)
        }
        barPaint.shader = null

        val xLabels = xAxisLabels(points)
        xLabels.forEach { (fraction, text) ->
            val x = chartLeft + (chartRight - chartLeft) * fraction
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint)
            canvas.drawText(text, x, h - 2f * density, axisPaint)
        }
    }

    private fun xAxisLabels(points: List<BatterySample>): List<Pair<Float, String>> {
        if (points.isEmpty()) return emptyList()
        val start = points.first().epochMillis
        val end = points.last().epochMillis
        val span = max(1L, end - start)
        val cal = Calendar.getInstance().apply {
            timeInMillis = end
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val hour = get(Calendar.HOUR_OF_DAY)
            set(Calendar.HOUR_OF_DAY, (hour / 4) * 4)
            if (timeInMillis >= end - 30L * 60L * 1000L) {
                add(Calendar.HOUR_OF_DAY, -4)
            }
        }
        val labels = ArrayList<Pair<Float, String>>()
        repeat(6) {
            val t = cal.timeInMillis
            if (t in start until end) {
                val fraction = ((t - start).toFloat() / span).coerceIn(0f, 0.92f)
                labels += fraction to String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
            }
            cal.add(Calendar.HOUR_OF_DAY, -4)
        }
        labels += 1f to nowLabel
        return labels.sortedBy { it.first }
    }
}
