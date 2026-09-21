package gd.app.hiboard.engine

import android.os.SystemClock
import kotlin.math.min

internal data class RecorderMark(
    val text: String,
    val timeMs: Long,
)

internal object RecorderWaveSession {
    data class Bar(val amp: Float, val bornAt: Long)

    val bars = ArrayList<Bar>(1024)
    var lastBucket = -1L
    var displayAmp = 0f
    var targetAmp = 0f

    fun reset() {
        bars.clear()
        lastBucket = -1L
        displayAmp = 0f
        targetAmp = 0f
    }

    fun commitBarsUpTo(timeMs: Long, amp: Float, sampleMs: Long) {
        val bucket = timeMs / sampleMs
        if (bucket <= lastBucket) return
        val missing = (bucket - lastBucket).toInt().coerceAtLeast(1)
        val now = SystemClock.uptimeMillis()
        if (missing > MAX_CATCHUP) {
            val need = bucket.toInt().coerceAtLeast(0)
            while (bars.size < need) bars += Bar(0f, now)
            bars += Bar(amp, now)
        } else {
            val from = bars.lastOrNull()?.amp ?: 0f
            repeat(missing) { i ->
                val t = (i + 1).toFloat() / missing
                bars += Bar(from + (amp - from) * t, now - (missing - 1 - i) * 8L)
            }
        }
        lastBucket = bucket
    }

    fun smoothedAmp(idx: Int): Float {
        if (bars.isEmpty()) return 0f
        val c = bars[idx.coerceIn(0, bars.size - 1)].amp
        val l = bars.getOrNull(idx - 1)?.amp ?: c
        val r = bars.getOrNull(idx + 1)?.amp ?: c
        return (l * 0.2f + c * 0.6f + r * 0.2f).coerceIn(0f, 1f)
    }

    fun needsGrowFrames(growMs: Float): Boolean {
        val cutoff = SystemClock.uptimeMillis() - growMs.toLong()
        for (i in bars.size - 1 downTo 0) {
            if (bars[i].bornAt < cutoff) return false
            val age = (SystemClock.uptimeMillis() - bars[i].bornAt).toFloat()
            val t = min(1f, age / growMs)
            if (t < 1f) return true
        }
        return false
    }

    private const val MAX_CATCHUP = 100
}
