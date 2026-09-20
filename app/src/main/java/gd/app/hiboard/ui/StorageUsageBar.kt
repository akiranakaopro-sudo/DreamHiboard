package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import gd.app.hiboard.R

class StorageUsageBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_storage_bar_track)
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_storage_bar_used)
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0f || height <= 0f) return
        val radius = height / 2f
        canvas.drawRoundRect(0f, 0f, width, height, radius, radius, trackPaint)
        val fillWidth = width * progress
        if (fillWidth <= 0f) return
        val drawn = fillWidth.coerceAtLeast(height).coerceAtMost(width)
        canvas.drawRoundRect(0f, 0f, drawn, height, radius, radius, fillPaint)
    }
}
