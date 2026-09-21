package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import gd.app.hiboard.R
import gd.app.hiboard.engine.RecorderStatus
import gd.app.hiboard.engine.RecorderWaveSession
import gd.app.hiboard.engine.formatRecorderTime
import kotlin.math.min
import kotlin.math.sqrt

class RecorderWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var liveSource: (() -> RecorderStatus)? = null
    private var timeView: TextView? = null
    private var recording = false
    private var sessionActive = false
    private var elapsedMs = 0L
    private var markTimes: List<Long> = emptyList()
    private var animRunning = false
    private var syncElapsedMs = 0L
    private var syncAtRealtime = 0L

    private val density = resources.displayMetrics.density
    private val pxPerMs = (17.5f * density) / 500f
    private val ampPitchPx = (17.5f * density) / 5f
    private val barWidthPx = 2.2f * density
    private val minBarH = 1.2f * density
    private val emptyPadHalfH = 0.5f * density
    private val sampleMs = 100L
    private val flagDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_recorder_flag_small)?.mutate()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = context.getColor(R.color.hiboard_recorder_wave)
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(2.5f * density, 5f * density), 0f)
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_tick)
    }
    private val tickHalf = 7.5f * density
    private val tickWidth = 1.5f * density
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_wave)
        alpha = 165
    }
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_wave)
        alpha = 70
    }
    private val markLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_flag)
        alpha = 0x4C
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!advanceFrame()) {
                stopAnim()
            } else {
                invalidate()
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    private val markPoll = object : Runnable {
        override fun run() {
            if (!sessionActive || recording) return
            val snap = liveSource?.invoke() ?: return
            if (snap.marks != markTimes || snap.elapsedMs != elapsedMs) {
                markTimes = snap.marks
                elapsedMs = snap.elapsedMs
                timeView?.text = formatRecorderTime(elapsedMs)
                invalidate()
            }
            postDelayed(this, 120L)
        }
    }

    fun bind(
        recording: Boolean,
        sessionActive: Boolean,
        timeView: TextView,
        source: () -> RecorderStatus,
    ) {
        this.recording = recording
        this.sessionActive = sessionActive
        this.timeView = timeView
        this.liveSource = source
        val snap = source()
        markTimes = snap.marks
        elapsedMs = snap.elapsedMs
        syncElapsedMs = snap.elapsedMs
        syncAtRealtime = SystemClock.elapsedRealtime()
        timeView.text = formatRecorderTime(elapsedMs)
        removeCallbacks(markPoll)
        if (recording) {
            startAnim()
        } else {
            stopAnim()
            if (sessionActive) post(markPoll)
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (recording) startAnim()
        else if (sessionActive) post(markPoll)
    }

    override fun onDetachedFromWindow() {
        stopAnim()
        removeCallbacks(markPoll)
        super.onDetachedFromWindow()
    }

    private fun startAnim() {
        if (animRunning) return
        animRunning = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun stopAnim() {
        if (!animRunning) return
        animRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun advanceFrame(): Boolean {
        val source = liveSource
        val tape = RecorderWaveSession
        var keep = tape.needsGrowFrames(BAR_GROW_MS)
        if (recording && source != null) {
            val snap = source()
            syncElapsedMs = snap.elapsedMs
            syncAtRealtime = SystemClock.elapsedRealtime()
            elapsedMs = snap.elapsedMs
            markTimes = snap.marks
            tape.targetAmp = softAmp(snap.amplitude)
            val alpha = if (tape.targetAmp > tape.displayAmp) AMP_RISE else AMP_FALL
            tape.displayAmp += (tape.targetAmp - tape.displayAmp) * alpha
            tape.commitBarsUpTo(elapsedMs, tape.displayAmp, sampleMs)
            timeView?.text = formatRecorderTime(elapsedMs)
            keep = true
        } else if (sessionActive && source != null) {
            val snap = source()
            markTimes = snap.marks
            elapsedMs = snap.elapsedMs
        }
        return keep
    }

    private fun softAmp(amp01: Float): Float = sqrt(amp01.coerceIn(0f, 1f)) * AMP_GAIN

    private fun growFactor(bornAt: Long): Float {
        val age = (SystemClock.uptimeMillis() - bornAt).toFloat()
        val t = min(1f, age / BAR_GROW_MS)
        return 1f - (1f - t) * (1f - t)
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0f || height <= 0f) return
        val cy = height / 2f
        val cx = width / 2f
        val tape = RecorderWaveSession
        val maxHalf = height * 0.38f
        if (!sessionActive || tape.bars.isEmpty()) {
            canvas.drawLine(0f, cy, width, cy, linePaint)
        } else {
            val scrollX = elapsedMs * pxPerMs
            val n = tape.bars.size
            val liveBucket = elapsedMs / sampleMs
            var tMs = kotlin.math.floor(((elapsedMs - (width * 0.5f + ampPitchPx) / pxPerMs) / sampleMs).toDouble()).toFloat() * sampleMs
            val lastMs = elapsedMs + (width * 0.5f + ampPitchPx) / pxPerMs
            while (tMs <= lastMs) {
                val x = cx + (tMs * pxPerMs - scrollX)
                if (x >= -ampPitchPx && x <= width + ampPitchPx) {
                    val left = x - barWidthPx * 0.5f
                    if (tMs < 0f || tMs > elapsedMs) {
                        canvas.drawRoundRect(
                            left,
                            cy - emptyPadHalfH,
                            left + barWidthPx,
                            cy + emptyPadHalfH,
                            barWidthPx,
                            barWidthPx,
                            dimPaint,
                        )
                    } else {
                        val bucket = (tMs / sampleMs).toLong()
                        val isLive = recording && bucket == liveBucket
                        val amp = if (isLive) tape.displayAmp else tape.smoothedAmp(bucket.toInt())
                        val bornAt = if (isLive || n == 0) {
                            SystemClock.uptimeMillis()
                        } else {
                            tape.bars[bucket.toInt().coerceIn(0, n - 1)].bornAt
                        }
                        val grow = if (isLive) 1f else growFactor(bornAt)
                        val half = (amp.coerceIn(0f, 1f) * maxHalf * grow).coerceAtLeast(minBarH * 0.5f)
                        canvas.drawRoundRect(
                            left,
                            cy - half,
                            left + barWidthPx,
                            cy + half,
                            barWidthPx,
                            barWidthPx,
                            barPaint,
                        )
                    }
                }
                tMs += sampleMs
            }
            drawFlags(canvas, cx, scrollX, height)
        }
        val left = cx - tickWidth * 0.5f
        canvas.drawRoundRect(
            left,
            cy - tickHalf,
            left + tickWidth,
            cy + tickHalf,
            tickWidth,
            tickWidth,
            tickPaint,
        )
    }

    private fun drawFlags(canvas: Canvas, centerX: Float, scrollX: Float, height: Float) {
        if (markTimes.isEmpty()) return
        val flag = flagDrawable
        val flagH = (10f * density).toInt()
        val flagW = (10f * density).toInt()
        val rx = barWidthPx
        for (t in markTimes) {
            val x = centerX + (t * pxPerMs - scrollX)
            if (x < -rx * 4f || x > width + rx * 4f) continue
            canvas.drawRoundRect(x, 0f, x + rx, height, rx, rx, markLinePaint)
            if (flag != null) {
                val top = (1.5f * density).toInt()
                val left = (x - flagW * 0.15f).toInt()
                flag.setBounds(left, top, left + flagW, top + flagH)
                flag.draw(canvas)
            }
        }
    }

    private companion object {
        const val BAR_GROW_MS = 280f
        const val AMP_RISE = 0.42f
        const val AMP_FALL = 0.24f
        const val AMP_GAIN = 0.9f
    }
}
