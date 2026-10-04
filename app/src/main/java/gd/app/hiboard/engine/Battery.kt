package gd.app.hiboard.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.BatteryManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import gd.app.hiboard.R
import gd.app.hiboard.model.BatterySample
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatterySnapshot(
    val levelPercent: Int = 0,
    val charging: Boolean = false,
    /** Estimated remaining use time while discharging; -1 when unknown/charging. */
    val remainingMs: Long = -1L,
    val samples: List<BatterySample> = emptyList(),
)

private const val PREFS = "hiboard_battery"
private const val KEY_SAMPLES = "samples"
private const val MAX_SAMPLES = 49
private const val SAMPLE_INTERVAL_MS = 30L * 60L * 1000L
private const val HISTORY_WINDOW_MS = 24L * 60L * 60L * 1000L
/** Fallback drain assumption when sensors/history are thin (~3.5%/h). */
private const val FALLBACK_PERCENT_PER_HOUR = 3.5f

class BatteryReader(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val batteryManager = appContext.getSystemService(BatteryManager::class.java)
    private val _snapshot = MutableStateFlow(readSnapshot())
    val snapshots: StateFlow<BatterySnapshot> = _snapshot.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED,
                Intent.ACTION_POWER_CONNECTED,
                Intent.ACTION_POWER_DISCONNECTED,
                -> publish()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        publish()
    }

    fun snapshot(): BatterySnapshot = readSnapshot().also { publish(it) }

    fun openBattery(): Intent? {
        val pm = appContext.packageManager
        pickSystemManagerIntent(systemManagerIntents()) { intent ->
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        }?.let { return it }
        val power = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (pm.resolveActivity(power, PackageManager.MATCH_DEFAULT_ONLY) != null) return power
        return Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .takeIf { pm.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null }
    }

    private fun publish(next: BatterySnapshot = readSnapshot()) {
        val prev = _snapshot.value
        if (prev.levelPercent == next.levelPercent &&
            prev.charging == next.charging &&
            prev.remainingMs / 3_600_000L == next.remainingMs / 3_600_000L &&
            prev.samples == next.samples
        ) {
            return
        }
        _snapshot.value = next
    }

    private fun readSnapshot(): BatterySnapshot {
        val sticky = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = sticky.batteryPercent()
        // ColorOS aC/f: only EXTRA_STATUS == CHARGING (2). FULL / NOT_CHARGING / unplugged ≠ charging.
        val charging = sticky.isBatteryCharging()
        val samples = persistAndLoad(level)
        val remaining = if (charging) -1L else estimateRemainingMs(level, samples)
        return BatterySnapshot(
            levelPercent = level,
            charging = charging,
            remainingMs = remaining,
            samples = samples,
        )
    }

    private fun estimateRemainingMs(level: Int, samples: List<BatterySample>): Long {
        if (level <= 0) return 0L
        sensorRemainingMs()?.let { return it }
        historyRemainingMs(level, samples)?.let { return it }
        val hours = level / FALLBACK_PERCENT_PER_HOUR
        return (hours * 60L * 60L * 1000L).toLong().coerceAtLeast(0L)
    }

    private fun sensorRemainingMs(): Long? {
        val manager = batteryManager ?: return null
        val chargeUah = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val currentUa = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (chargeUah <= 0L || currentUa == Long.MIN_VALUE || currentUa == 0L) return null
        // Discharging current is negative on most devices.
        val drainUa = if (currentUa < 0L) -currentUa else return null
        if (drainUa <= 0L) return null
        return (chargeUah * 3600_000L) / drainUa
    }

    private fun historyRemainingMs(level: Int, samples: List<BatterySample>): Long? {
        val discharge = samples.filter { !it.charging }
        if (discharge.size < 3) return null
        val first = discharge.first()
        val last = discharge.last()
        val elapsed = last.epochMillis - first.epochMillis
        val dropped = first.levelPercent - last.levelPercent
        if (elapsed < 30L * 60L * 1000L || dropped <= 0) return null
        val percentPerMs = dropped.toDouble() / elapsed.toDouble()
        if (percentPerMs <= 0.0) return null
        return (level / percentPerMs).toLong().coerceAtLeast(0L)
    }

    private fun persistAndLoad(level: Int): List<BatterySample> {
        val now = System.currentTimeMillis()
        val existing = loadSamples().filter { now - it.epochMillis <= HISTORY_WINDOW_MS }
        val last = existing.lastOrNull()
        // Sticks advance on the sample clock only — never on plug / level flicker.
        val shouldAppend = last == null || now - last.epochMillis >= SAMPLE_INTERVAL_MS
        val next = when {
            existing.isEmpty() -> seedFlatHistory(level, now)
            shouldAppend -> {
                val rising = last != null && level > last.levelPercent
                (existing + BatterySample(now, level, rising)).takeLast(MAX_SAMPLES)
            }
            else -> existing
        }
        // Green = percent rose vs the previous stick (re-derived so older prefs stay consistent).
        val marked = markRising(next)
        if (marked != existing) saveSamples(marked)
        return marked
    }

    private fun seedFlatHistory(level: Int, now: Long): List<BatterySample> {
        val step = HISTORY_WINDOW_MS / (MAX_SAMPLES - 1)
        return List(MAX_SAMPLES) { index ->
            BatterySample(
                epochMillis = now - (MAX_SAMPLES - 1 - index) * step,
                levelPercent = level,
                charging = false,
            )
        }
    }

    private fun markRising(samples: List<BatterySample>): List<BatterySample> {
        if (samples.isEmpty()) return samples
        return samples.mapIndexed { index, sample ->
            val rising = index > 0 && sample.levelPercent > samples[index - 1].levelPercent
            if (sample.charging == rising) sample else sample.copy(charging = rising)
        }
    }

    private fun loadSamples(): List<BatterySample> {
        val raw = prefs.getString(KEY_SAMPLES, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(';').mapNotNull { token ->
            val parts = token.split(':')
            if (parts.size < 2) return@mapNotNull null
            val time = parts[0].toLongOrNull() ?: return@mapNotNull null
            val level = parts[1].toIntOrNull() ?: return@mapNotNull null
            val charging = parts.getOrNull(2) == "1"
            BatterySample(time, level.coerceIn(0, 100), charging)
        }
    }

    private fun saveSamples(samples: List<BatterySample>) {
        val packed = samples.joinToString(";") {
            "${it.epochMillis}:${it.levelPercent}:${if (it.charging) 1 else 0}"
        }
        prefs.edit().putString(KEY_SAMPLES, packed).apply()
    }
}

/** Short duration fragment used inside [R.string.battery_should_last], e.g. "19 h". */
fun formatBatteryRemainingDuration(resources: Resources, remainingMs: Long): String {
    val totalMinutes = if (remainingMs < 0L) 0L else (remainingMs / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return when {
        hours <= 0L -> resources.getString(R.string.battery_duration_minutes, minutes.toInt().coerceAtLeast(0))
        else -> resources.getString(R.string.battery_duration_hours, hours.toInt())
    }
}

private fun Intent?.batteryPercent(): Int {
    if (this == null) return 0
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
    if (level < 0) return 0
    return ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
}

/**
 * ColorOS `aC/f` maps EXTRA_STATUS: 2→"charging", 3→"discharging", 4→"not_charging", 5→"full".
 * Status-row "Charging" follows status==CHARGING only (not FULL / plugged).
 */
private fun Intent?.isBatteryCharging(): Boolean {
    if (this == null) return false
    return getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ==
        BatteryManager.BATTERY_STATUS_CHARGING
}

/**
 * Builds a full 24h stick grid. Gaps between real samples (and out to [end]) are filled by
 * linear interpolation; before the first sample the first known level is held.
 */
fun densifyBatterySamples(
    samples: List<BatterySample>,
    end: Long = System.currentTimeMillis(),
    currentLevel: Int = samples.lastOrNull()?.levelPercent ?: 0,
): List<BatterySample> {
    val start = end - HISTORY_WINDOW_MS
    val step = HISTORY_WINDOW_MS / (MAX_SAMPLES - 1)
    val sorted = samples.sortedBy { it.epochMillis }
    val level = currentLevel.coerceIn(0, 100)
    if (sorted.isEmpty()) {
        return List(MAX_SAMPLES) { index ->
            BatterySample(
                epochMillis = start + index * step,
                levelPercent = level,
                charging = false,
            )
        }
    }
    val levels = IntArray(MAX_SAMPLES) { index ->
        levelAtTime(sorted, start + index * step, end, level)
    }
    return List(MAX_SAMPLES) { index ->
        val rising = index > 0 && levels[index] > levels[index - 1]
        BatterySample(
            epochMillis = start + index * step,
            levelPercent = levels[index],
            charging = rising,
        )
    }
}

private fun levelAtTime(
    sorted: List<BatterySample>,
    time: Long,
    end: Long,
    currentLevel: Int,
): Int {
    val first = sorted.first()
    val last = sorted.last()
    when {
        time <= first.epochMillis -> return first.levelPercent
        time >= end -> return currentLevel
        time >= last.epochMillis -> {
            val span = (end - last.epochMillis).coerceAtLeast(1L)
            val t = ((time - last.epochMillis).toFloat() / span).coerceIn(0f, 1f)
            return (last.levelPercent + (currentLevel - last.levelPercent) * t).roundToInt()
                .coerceIn(0, 100)
        }
    }
    for (index in 0 until sorted.lastIndex) {
        val a = sorted[index]
        val b = sorted[index + 1]
        if (time in a.epochMillis..b.epochMillis) {
            val span = (b.epochMillis - a.epochMillis).coerceAtLeast(1L)
            val t = (time - a.epochMillis).toFloat() / span
            return (a.levelPercent + (b.levelPercent - a.levelPercent) * t).roundToInt()
                .coerceIn(0, 100)
        }
    }
    return currentLevel
}

/** Demo series for store previews (decline with a short rising / charge bump). */
fun previewBatterySamples(
    level: Int = 63,
    chargingNow: Boolean = false,
    now: Long = System.currentTimeMillis(),
): List<BatterySample> {
    val step = HISTORY_WINDOW_MS / (MAX_SAMPLES - 1)
    val levels = List(MAX_SAMPLES) { index ->
        val t = index.toFloat() / (MAX_SAMPLES - 1)
        val inChargeBurst = t in 0.70f..0.80f
        when {
            t < 0.70f -> (level + 8 - (t / 0.70f) * 12f).roundToInt()
            inChargeBurst -> (level - 4 + ((t - 0.70f) / 0.10f) * 10f).roundToInt()
            else -> (level + 6 - ((t - 0.80f) / 0.20f) * 6f).roundToInt()
        }.coerceIn(0, 100).let { value ->
            if (chargingNow && index == MAX_SAMPLES - 1) (value + 1).coerceAtMost(100) else value
        }
    }
    return levels.mapIndexed { index, value ->
        val rising = index > 0 && value > levels[index - 1]
        BatterySample(
            epochMillis = now - (MAX_SAMPLES - 1 - index) * step,
            levelPercent = value,
            charging = rising,
        )
    }
}
