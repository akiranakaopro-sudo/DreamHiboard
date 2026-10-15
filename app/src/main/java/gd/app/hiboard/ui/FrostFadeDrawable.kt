package gd.app.hiboard.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.math.max

/**
 * Same baked wallpaper frost as the board, faded out toward the top so widgets
 * stay visible under the search row.
 */
class FrostFadeDrawable(
    private val host: View,
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val onBlur = { invalidateSelf() }

    init {
        BoardBackdrop.addBlurListener(onBlur)
    }

    fun detach() {
        BoardBackdrop.removeBlurListener(onBlur)
    }

    override fun draw(canvas: Canvas) {
        if (bounds.isEmpty) {
            return
        }
        val save = canvas.saveLayer(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            null,
        )
        val bitmap = BoardBackdrop.peekBlurBitmap()
        val frame = host.parent as? View
        if (bitmap != null && frame != null && frame.width > 0 && frame.height > 0) {
            val scale = max(frame.width / bitmap.width.toFloat(), frame.height / bitmap.height.toFloat())
            val drawnW = bitmap.width * scale
            val drawnH = bitmap.height * scale
            val dest = RectF(
                (frame.width - drawnW) / 2f - host.left,
                (frame.height - drawnH) / 2f - host.top,
                (frame.width - drawnW) / 2f - host.left + drawnW,
                (frame.height - drawnH) / 2f - host.top + drawnH,
            )
            canvas.drawBitmap(bitmap, null, dest, paint)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        maskPaint.shader = LinearGradient(
            0f,
            bounds.top.toFloat(),
            0f,
            bounds.bottom.toFloat(),
            intArrayOf(Color.TRANSPARENT, 0xEEFFFFFF.toInt(), Color.WHITE, Color.WHITE),
            floatArrayOf(0f, 0.28f, 0.36f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(bounds, maskPaint)
        canvas.restoreToCount(save)
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
