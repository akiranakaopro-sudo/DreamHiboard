package gd.app.hiboard.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import gd.app.hiboard.R

/** One-click optimize: a rocket lifts out of the storage ring, then a shockwave settles. */
class StorageRocketOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var running = false
    private var phase = 0f
    private var animator: ValueAnimator? = null

    private val rocketArt: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_optimize_rocket)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFF5EC8FF.toInt()
    }
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = 0xFF1780FE.toInt()
    }
    fun launch() {
        if (running) return
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        running = true
        phase = 0f
        visibility = VISIBLE
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1680L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val next = it.animatedValue as Float
                advance(next)
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    finish()
                }

                override fun onAnimationCancel(animation: android.animation.Animator) {
                    finish()
                }
            })
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    private fun finish() {
        running = false
        phase = 0f
        if (visibility != GONE) visibility = GONE
    }

    private fun advance(next: Float) {
        phase = next
    }

    override fun onDraw(canvas: Canvas) {
        if (!running || width <= 0 || height <= 0) return
        val flight = flightT(phase)
        val pos = rocketPosition(flight)
        drawSweep(canvas)
        drawShockwave(canvas, pos)
        if (flight < 0.98f) drawRocket(canvas, pos[0], pos[1], flight)
    }

    private fun flightT(value: Float): Float {
        val t = ((value - 0.05f) / 0.78f).coerceIn(0f, 1f)
        return t * t * (0.25f + 0.75f * t)
    }

    private fun rocketPosition(flight: Float): FloatArray {
        return floatArrayOf(
            width * 0.5f,
            height * (0.72f - 1.05f * flight),
        )
    }

    private fun drawSweep(canvas: Canvas) {
        if (phase < 0.12f || phase > 0.92f) return
        val cx = width * 0.5f
        val cy = height * 0.58f
        val radius = minOf(width, height) * 0.34f
        val turn = (phase - 0.12f) / 0.80f
        sweepPaint.strokeWidth = dp(3.2f)
        sweepPaint.alpha = ((1f - turn) * 170f).toInt().coerceIn(0, 170)
        canvas.drawArc(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius,
            -90f + turn * 520f,
            70f,
            false,
            sweepPaint,
        )
    }

    private fun drawShockwave(canvas: Canvas, pos: FloatArray) {
        if (phase < 0.62f) return
        val t = ((phase - 0.62f) / 0.38f).coerceIn(0f, 1f)
        val radius = minOf(width, height) * (0.08f + 0.62f * t)
        wavePaint.strokeWidth = dp(2.4f * (1f - t) + 0.6f)
        wavePaint.alpha = ((1f - t) * 200f).toInt()
        canvas.drawCircle(pos[0], height * 0.42f, radius, wavePaint)
        if (t > 0.15f) {
            val inner = radius * 0.62f
            wavePaint.alpha = ((1f - t) * 120f).toInt()
            canvas.drawCircle(width * 0.5f, height * 0.58f, inner, wavePaint)
        }
    }

    private fun drawRocket(canvas: Canvas, x: Float, y: Float, flight: Float) {
        val art = rocketArt ?: return
        val appear = (phase / 0.08f).coerceIn(0f, 1f)
        val fade = if (flight < 0.86f) 1f else (1f - (flight - 0.86f) / 0.14f).coerceIn(0f, 1f)
        val alpha = (appear * fade * 255f).toInt()
        if (alpha <= 0) return
        val size = minOf(width, height) * 0.62f
        val left = x - size / 2f
        val top = y - size / 2f
        art.setBounds(left.toInt(), top.toInt(), (left + size).toInt(), (top + size).toInt())
        art.alpha = alpha
        art.draw(canvas)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

}
