package gd.app.hiboard.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin
import kotlin.random.Random

/** One-click optimize: a rocket lifts out of the storage ring, then a shockwave settles. */
class StorageRocketOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var running = false
    private var phase = 0f
    private var animator: ValueAnimator? = null
    private val sparks = ArrayList<Spark>(64)
    private val trail = ArrayList<Float>(TRAIL * 2)
    private val random = Random(7)

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF7FBFF.toInt() }
    private val nosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1780FE.toInt() }
    private val finPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0057FE.toInt() }
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF7ED0FF.toInt() }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFF5EC8FF.toInt()
    }
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = 0xFF1780FE.toInt()
    }
    private val nose = Path()
    private val fin = Path()

    fun launch() {
        if (running) return
        running = true
        phase = 0f
        sparks.clear()
        trail.clear()
        visibility = VISIBLE
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
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
        sparks.clear()
        trail.clear()
        if (visibility != GONE) visibility = GONE
    }

    private fun advance(next: Float) {
        val flight = flightT(next)
        if (flight in 0.02f..0.96f && next > phase) {
            val pos = rocketPosition(flight)
            trail.add(pos[0])
            trail.add(pos[1])
            if (trail.size > TRAIL * 2) {
                trail.subList(0, trail.size - TRAIL * 2).clear()
            }
            repeat(3) {
                sparks.add(
                    Spark(
                        x = pos[0] + random.nextFloat() * dp(6f) - dp(3f),
                        y = pos[1] + dp(10f),
                        vx = (random.nextFloat() - 0.5f) * dp(4.2f),
                        vy = dp(1.2f) + random.nextFloat() * dp(3.4f),
                        life = 0.55f + random.nextFloat() * 0.45f,
                        hot = random.nextBoolean(),
                    ),
                )
            }
        }
        val iterator = sparks.iterator()
        while (iterator.hasNext()) {
            val spark = iterator.next()
            spark.x += spark.vx
            spark.y += spark.vy
            spark.vy += dp(0.18f)
            spark.life -= 0.045f
            if (spark.life <= 0f) iterator.remove()
        }
        phase = next
    }

    override fun onDraw(canvas: Canvas) {
        if (!running || width <= 0 || height <= 0) return
        val flight = flightT(phase)
        val pos = rocketPosition(flight)
        drawSweep(canvas)
        drawTrail(canvas)
        drawSparks(canvas)
        drawShockwave(canvas, pos)
        if (flight < 0.98f) drawRocket(canvas, pos[0], pos[1], flight)
    }

    private fun flightT(value: Float): Float {
        val t = ((value - 0.05f) / 0.78f).coerceIn(0f, 1f)
        return t * t * (0.25f + 0.75f * t)
    }

    private fun rocketPosition(flight: Float): FloatArray {
        val sway = sin(flight * Math.PI).toFloat() * width * 0.045f
        return floatArrayOf(
            width * 0.50f + sway,
            height * (0.70f - 0.98f * flight),
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

    private fun drawTrail(canvas: Canvas) {
        val count = trail.size / 2
        if (count < 2) return
        for (index in 1 until count) {
            val fade = index / count.toFloat()
            trailPaint.strokeWidth = dp(2f + 7f * fade)
            trailPaint.color = if (fade > 0.55f) 0xFFFFB020.toInt() else 0xFF5EC8FF.toInt()
            trailPaint.alpha = (40 + 180 * fade).toInt().coerceAtMost(220)
            canvas.drawLine(
                trail[(index - 1) * 2],
                trail[(index - 1) * 2 + 1],
                trail[index * 2],
                trail[index * 2 + 1],
                trailPaint,
            )
        }
    }

    private fun drawSparks(canvas: Canvas) {
        for (spark in sparks) {
            sparkPaint.color = if (spark.hot) 0xFFFFC14A.toInt() else 0xFF8AD8FF.toInt()
            sparkPaint.alpha = (spark.life * 255f).toInt().coerceIn(0, 255)
            canvas.drawCircle(spark.x, spark.y, dp(1.3f + spark.life * 1.6f), sparkPaint)
        }
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
        val appear = (phase / 0.08f).coerceIn(0f, 1f)
        val fade = if (flight < 0.86f) 1f else (1f - (flight - 0.86f) / 0.14f).coerceIn(0f, 1f)
        val alpha = (appear * fade * 255f).toInt()
        if (alpha <= 0) return
        val flicker = 0.75f + 0.25f * sin(phase * 48f).toFloat()
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(sin(flight * Math.PI).toFloat() * -10f)
        canvas.scale(dp(1f), dp(1f))

        glowPaint.shader = RadialGradient(
            0f,
            14f,
            28f,
            0x66FFB020,
            0x00FFB020,
            Shader.TileMode.CLAMP,
        )
        glowPaint.alpha = (alpha * flicker).toInt()
        canvas.drawCircle(0f, 16f, 26f, glowPaint)

        val flame = 10f + 12f * flicker * (0.35f + flight)
        flamePaint.shader = LinearGradient(
            0f,
            8f,
            0f,
            8f + flame,
            intArrayOf(0xFFFFF4C8.toInt(), 0xFFFF9A1F.toInt(), 0x00FF5A1F),
            floatArrayOf(0f, 0.35f, 1f),
            Shader.TileMode.CLAMP,
        )
        flamePaint.alpha = alpha
        canvas.drawOval(-4.5f, 7f, 4.5f, 8f + flame, flamePaint)

        fin.reset()
        fin.moveTo(-6f, 4f)
        fin.lineTo(-13f, 12f)
        fin.lineTo(-5f, 8f)
        fin.close()
        finPaint.alpha = alpha
        canvas.drawPath(fin, finPaint)
        canvas.save()
        canvas.scale(-1f, 1f)
        canvas.drawPath(fin, finPaint)
        canvas.restore()

        bodyPaint.alpha = alpha
        canvas.drawRoundRect(-6.5f, -8f, 6.5f, 9f, 4f, 4f, bodyPaint)
        nose.reset()
        nose.moveTo(0f, -20f)
        nose.lineTo(7.2f, -6f)
        nose.lineTo(-7.2f, -6f)
        nose.close()
        nosePaint.alpha = alpha
        canvas.drawPath(nose, nosePaint)
        windowPaint.alpha = alpha
        canvas.drawCircle(0f, -1f, 2.4f, windowPaint)
        drawStar(canvas, -16f, -18f, phase)
        drawStar(canvas, 18f, -26f, phase + 0.2f)
        canvas.restore()
    }

    private fun drawStar(canvas: Canvas, x: Float, y: Float, seed: Float) {
        val twinkle = (0.35f + 0.65f * ((sin(seed * 30.0) + 1.0) / 2.0)).toFloat()
        sparkPaint.color = 0xFFFFFFFF.toInt()
        sparkPaint.alpha = (twinkle * 220f).toInt()
        canvas.drawCircle(x, y, 1.1f, sparkPaint)
        sparkPaint.style = Paint.Style.STROKE
        sparkPaint.strokeWidth = 1.2f
        sparkPaint.strokeCap = Paint.Cap.ROUND
        sparkPaint.alpha = (twinkle * 140f).toInt()
        canvas.drawLine(x - 3.2f, y, x + 3.2f, y, sparkPaint)
        canvas.drawLine(x, y - 3.2f, x, y + 3.2f, sparkPaint)
        sparkPaint.style = Paint.Style.FILL
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private class Spark(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val hot: Boolean,
    )

    private companion object {
        const val TRAIL = 18
    }
}
