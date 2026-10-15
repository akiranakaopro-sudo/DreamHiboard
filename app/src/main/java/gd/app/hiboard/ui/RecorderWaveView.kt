package gd.app.hiboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.widget.TextView
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
    private val sampleMs = 100L

    private val idleTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_idle_tick)
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_tick)
    }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.hiboard_recorder_wave)
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
        val snap = if (sessionActive) source() else RecorderStatus()
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
            drawIdleTicks(canvas, cx, cy, width)
        } else {
            val scrollX = elapsedMs * pxPerMs
            val n = tape.bars.size
            val liveBucket = elapsedMs / sampleMs
            var tMs = kotlin.math.floor(((elapsedMs - (width * 0.5f + ampPitchPx) / pxPerMs) / sampleMs).toDouble()).toFloat() * sampleMs
            val lastMs = elapsedMs + (width * 0.5f + ampPitchPx) / pxPerMs
            while (tMs <= lastMs) {
                val x = cx + (tMs * pxPerMs - scrollX)
                if (x >= -ampPitchPx && x <= width + ampPitchPx) {
                    if (tMs < 0f || tMs > elapsedMs) {
                        drawIdleDot(canvas, x, cy, width)
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
                        val halfW = idleDotHalfW(width)
                        val minHalf = idleDotHalfH(width)
                        val half = (amp.coerceIn(0f, 1f) * maxHalf * grow).coerceAtLeast(minHalf)
                        canvas.drawRoundRect(
                            x - halfW,
                            cy - half,
                            x + halfW,
                            cy + half,
                            halfW,
                            halfW,
                            barPaint,
                        )
                    }
                }
                tMs += sampleMs
            }
            drawFlags(canvas, cx, cy, width, scrollX)
        }
        drawTracker(canvas, cx, cy, width)
    }

    private fun idleDotHalfW(width: Float): Float =
        (width * IDLE_TICK_WIDTH).coerceAtLeast(density * 0.8f) * 0.5f

    private fun idleDotHalfH(width: Float): Float = width * IDLE_TICK_HALF

    private fun trackerWidth(width: Float): Float =
        (width * CURSOR_WIDTH).coerceAtLeast(density)

    private fun trackerHalfH(width: Float): Float = width * CURSOR_HALF

    private fun drawIdleDot(canvas: Canvas, x: Float, cy: Float, width: Float) {
        val halfW = idleDotHalfW(width)
        val halfH = idleDotHalfH(width)
        canvas.drawRect(x - halfW, cy - halfH, x + halfW, cy + halfH, idleTickPaint)
    }

    private fun drawTracker(canvas: Canvas, x: Float, cy: Float, width: Float) {
        val tickWidth = trackerWidth(width)
        val tickHalf = trackerHalfH(width)
        canvas.drawRect(x - tickWidth * 0.5f, cy - tickHalf, x + tickWidth * 0.5f, cy + tickHalf, tickPaint)
    }

    private fun drawIdleTicks(canvas: Canvas, cx: Float, cy: Float, width: Float) {
        val pitch = width * IDLE_PITCH
        val reach = width * (0.5f - IDLE_INSET)
        var offset = pitch
        while (offset <= reach) {
            drawIdleDot(canvas, cx - offset, cy, width)
            drawIdleDot(canvas, cx + offset, cy, width)
            offset += pitch
        }
    }

    private fun drawFlags(canvas: Canvas, centerX: Float, cy: Float, width: Float, scrollX: Float) {
        if (markTimes.isEmpty()) return
        val reach = trackerWidth(width) * 4f
        for (t in markTimes) {
            val x = centerX + (t * pxPerMs - scrollX)
            if (x < -reach || x > this.width + reach) continue
            drawTracker(canvas, x, cy, width)
        }
    }

    private companion object {
        const val BAR_GROW_MS = 280f
        const val AMP_RISE = 0.42f
        const val AMP_FALL = 0.24f
        const val AMP_GAIN = 0.9f
        const val CURSOR_HALF = 0.09f
        const val CURSOR_WIDTH = 0.0045f
        const val IDLE_PITCH = 0.0192f
        const val IDLE_TICK_WIDTH = 0.0064f
        const val IDLE_TICK_HALF = 0.0085f
        const val IDLE_INSET = 0.077f
    }
}
