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
    val plugged: Boolean = false,
    /** Estimated remaining use time while discharging; -1 when unknown/charging. */
    val remainingMs: Long = -1L,
    /** Estimated time to 100% while charging; -1 when unknown/not charging. */
    val untilFullMs: Long = -1L,
    val samples: List<BatterySample> = emptyList(),
)

private const val PREFS = "hiboard_battery"
private const val KEY_SAMPLES = "samples"
private const val KEY_RATE = "rate"
/** 30‑minute sticks for the FullByTwo card. */
const val BATTERY_STICK_COUNT = 49
/** Hourly sticks for the 2×2 card — wider gaps than the FullByTwo grid. */
const val BATTERY_STICK_COUNT_COMPACT = 25
private const val MAX_SAMPLES = BATTERY_STICK_COUNT
private const val SAMPLE_INTERVAL_MS = 30L * 60L * 1000L
private const val HISTORY_WINDOW_MS = 24L * 60L * 60L * 1000L
/** Fallback drain when sensors and recent history are both missing (~4%/h, mixed use). */
private const val FALLBACK_PERCENT_PER_HOUR = 4f
/** Fallback charge rate when sensors and recent history are both missing (~25%/h). */
private const val FALLBACK_CHARGE_PERCENT_PER_HOUR = 25f
private const val RATE_INTERVAL_MS = 60_000L
private const val RATE_WINDOW_MS = 6L * 60L * 60L * 1000L
private const val HOUR_MS = 3_600_000L

class BatteryReader(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val batteryManager = appContext.getSystemService(BatteryManager::class.java)
    private var smoothedCurrentUa = 0L
    private var smoothedForCharge = false
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
            prev.plugged == next.plugged &&
            prev.remainingMs / 60_000L == next.remainingMs / 60_000L &&
            prev.untilFullMs / 60_000L == next.untilFullMs / 60_000L &&
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
        val plugged = sticky.isBatteryPlugged()
        val samples = persistAndLoad(level)
        val rate = recordRate(level, plugged)
        val remaining = if (plugged && charging) {
            -1L
        } else {
            estimateRemainingMs(level, rate, useSensor = !plugged)
        }
        val untilFull = if (plugged && level < 100) estimateUntilFullMs(level, rate) else -1L
        return BatterySnapshot(
            levelPercent = level,
            charging = charging,
            plugged = plugged,
            remainingMs = remaining,
            untilFullMs = untilFull,
            samples = samples,
        )
    }

    private fun estimateUntilFullMs(level: Int, rate: List<BatteryRatePoint>): Long {
        if (level >= 100) return 0L
        systemUntilFullMs(level)?.let { return it }
        readPowerSupplyTimeMs(toFull = true, level)?.let { return it }
        val sensor = sensorUntilFullMs(level)
        val history = estimateMsFromRate(rate, level, plugged = true)
        blendBatteryEstimates(sensor, history)?.let { return it }
        val hours = (100 - level) / FALLBACK_CHARGE_PERCENT_PER_HOUR
        return (hours * HOUR_MS).toLong().coerceAtLeast(0L)
    }

    private fun estimateRemainingMs(level: Int, rate: List<BatteryRatePoint>, useSensor: Boolean): Long {
        if (level <= 0) return 0L
        val history = estimateMsFromRate(rate, level, plugged = false)
        if (!useSensor) return history ?: fallbackUseMs(level)
        readPowerSupplyTimeMs(toFull = false, level)?.let { return it }
        val sensor = sensorRemainingMs(level)
        blendBatteryEstimates(sensor, history)?.let { return it }
        return fallbackUseMs(level)
    }

    private fun fallbackUseMs(level: Int): Long {
        val hours = level / FALLBACK_PERCENT_PER_HOUR
        return (hours * HOUR_MS).toLong().coerceAtLeast(0L)
    }

    private fun systemUntilFullMs(level: Int): Long? {
        val manager = batteryManager ?: return null
        val ms = try {
            manager.computeChargeTimeRemaining()
        } catch (_: RuntimeException) {
            -1L
        }
        if (ms <= 0L) return null
        return ms.takeIf { plausibleUntilFullMs(it, level) }
    }

    private fun sensorUntilFullMs(level: Int): Long? {
        val currentUa = smoothedChargeOrDrainUa(charging = true) ?: return null
        val chargeUah = chargeCounterMicroAh(level) ?: return null
        return batteryMillisFromCounters(chargeUah, currentUa, level, toFull = true)
    }

    private fun sensorRemainingMs(level: Int): Long? {
        val drainUa = smoothedChargeOrDrainUa(charging = false) ?: return null
        val chargeUah = chargeCounterMicroAh(level) ?: return null
        return batteryMillisFromCounters(chargeUah, drainUa, level, toFull = false)
    }

    private fun chargeCounterMicroAh(level: Int): Long? {
        val manager = batteryManager ?: return null
        val raw = manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        return asMicroAmpHours(raw, level)
    }

    /** Prefer the averaged current. Magnitude is used because OEM sign conventions differ. */
    private fun smoothedChargeOrDrainUa(charging: Boolean): Long? {
        val manager = batteryManager ?: return null
        if (smoothedForCharge != charging) {
            smoothedCurrentUa = 0L
            smoothedForCharge = charging
        }
        val average = asMicroAmps(manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE))
        val now = asMicroAmps(manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW))
        val sample = when {
            average != null && now != null -> (average * 3L + now) / 4L
            average != null -> average
            else -> now
        } ?: return null
        smoothedCurrentUa = if (smoothedCurrentUa <= 0L) {
            sample
        } else {
            (smoothedCurrentUa * 7L + sample * 3L) / 10L
        }
        return smoothedCurrentUa.takeIf { it > 0L }
    }

    private fun readPowerSupplyTimeMs(toFull: Boolean, level: Int): Long? {
        val name = if (toFull) "time_to_full_now" else "time_to_empty_now"
        val bases = arrayOf(
            "/sys/class/power_supply/battery/",
            "/sys/class/power_supply/bms/",
            "/sys/class/power_supply/Battery/",
        )
        for (base in bases) {
            val text = try {
                java.io.File(base + name).takeIf { it.canRead() }?.readText()
            } catch (_: Exception) {
                null
            } ?: continue
            parsePowerSupplyTimeMs(text, level, toFull)?.let { return it }
        }
        return null
    }

    private fun recordRate(level: Int, plugged: Boolean): List<BatteryRatePoint> {
        val now = System.currentTimeMillis()
        val existing = loadRate().filter { now - it.epochMillis <= RATE_WINDOW_MS }
        val last = existing.lastOrNull()
        val next = when {
            last == null || last.plugged != plugged || now - last.epochMillis >= RATE_INTERVAL_MS ->
                (existing + BatteryRatePoint(now, level, plugged)).takeLast(400)
            else -> existing
        }
        if (next != existing) saveRate(next)
        return next
    }

    private fun loadRate(): List<BatteryRatePoint> {
        val raw = prefs.getString(KEY_RATE, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(';').mapNotNull { token ->
            val parts = token.split(':')
            if (parts.size < 3) return@mapNotNull null
            val time = parts[0].toLongOrNull() ?: return@mapNotNull null
            val level = parts[1].toIntOrNull() ?: return@mapNotNull null
            BatteryRatePoint(time, level.coerceIn(0, 100), parts[2] == "1")
        }
    }

    private fun saveRate(points: List<BatteryRatePoint>) {
        val packed = points.joinToString(";") {
            "${it.epochMillis}:${it.levelPercent}:${if (it.plugged) 1 else 0}"
        }
        prefs.edit().putString(KEY_RATE, packed).apply()
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

/** Duration fragment, e.g. "2 h 15 m" or "40 m". */
fun formatBatteryRemainingDuration(resources: Resources, remainingMs: Long): String {
    val totalMinutes = if (remainingMs < 0L) 0L else (remainingMs / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = (totalMinutes % 60L).toInt()
    return when {
        hours <= 0L -> resources.getString(R.string.battery_duration_minutes, minutes.coerceAtLeast(0))
        minutes <= 0 -> resources.getString(R.string.battery_duration_hours, hours.toInt())
        else -> resources.getString(R.string.battery_duration_hours_minutes, hours.toInt(), minutes)
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

private fun Intent?.isBatteryPlugged(): Boolean {
    if (this == null) return false
    return getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
}

internal data class BatteryRatePoint(
    val epochMillis: Long,
    val levelPercent: Int,
    val plugged: Boolean,
)

/**
 * Charge-counter and current are documented as µAh / µA, but some HALs report mAh / mA.
 * Pick the scale that lands in a real phone range for this [levelPercent].
 */
internal fun asMicroAmpHours(raw: Long, levelPercent: Int): Long? {
    if (raw == Long.MIN_VALUE || raw == 0L || levelPercent <= 0) return null
    val abs = kotlin.math.abs(raw)
    val pct = levelPercent.coerceIn(1, 100)
    val minUah = pct * 8_000L
    val maxUah = pct * 150_000L
    val asUah = abs
    val asMah = abs * 1_000L
    val uahOk = asUah in minUah..maxUah
    val mahOk = asMah in minUah..maxUah
    return when {
        uahOk && !mahOk -> asUah
        mahOk && !uahOk -> asMah
        uahOk -> asUah
        else -> null
    }
}

/** Current magnitude in µA. Values that only make sense as mA are scaled. */
internal fun asMicroAmps(raw: Long): Long? {
    if (raw == Long.MIN_VALUE || raw == 0L) return null
    val abs = kotlin.math.abs(raw)
    val asUa = abs
    val asMa = abs * 1_000L
    val uaOk = asUa in 20_000L..8_000_000L
    val maOk = asMa in 20_000L..8_000_000L
    return when {
        uaOk && !maOk -> asUa
        maOk && !uaOk -> asMa
        uaOk -> asUa
        else -> null
    }
}

internal fun batteryMillisFromCounters(
    chargeUah: Long,
    currentUa: Long,
    levelPercent: Int,
    toFull: Boolean,
): Long? {
    if (chargeUah <= 0L || currentUa <= 0L) return null
    val ms = if (toFull) {
        if (levelPercent <= 0 || levelPercent >= 100) return if (levelPercent >= 100) 0L else null
        val remainUah = chargeUah * (100 - levelPercent) / levelPercent
        if (remainUah <= 0L) return 0L
        remainUah * HOUR_MS / currentUa
    } else {
        chargeUah * HOUR_MS / currentUa
    }
    return ms.takeIf { if (toFull) plausibleUntilFullMs(it, levelPercent) else plausibleUseMs(it, levelPercent) }
}

internal fun plausibleUntilFullMs(ms: Long, levelPercent: Int): Boolean {
    if (ms <= 0L || levelPercent >= 100) return ms == 0L && levelPercent >= 100
    val deficit = (100 - levelPercent).coerceAtLeast(1)
    val min = deficit * 5_000L
    val max = deficit * 30L * 60_000L
    return ms in min..max
}

internal fun plausibleUseMs(ms: Long, levelPercent: Int): Boolean {
    if (ms <= 0L || levelPercent <= 0) return false
    val min = levelPercent * 20_000L
    val max = levelPercent * 120L * 60_000L
    return ms in min..max
}

/**
 * Kernel ABI is seconds. Some vendor nodes store minutes. Keep whichever reading
 * is a believable time for the percent still to go.
 */
internal fun parsePowerSupplyTimeMs(rawText: String, levelPercent: Int, toFull: Boolean): Long? {
    val raw = rawText.trim().toLongOrNull() ?: return null
    if (raw <= 0L) return null
    val asSeconds = raw * 1_000L
    val asMinutes = raw * 60_000L
    val secondsOk = if (toFull) plausibleUntilFullMs(asSeconds, levelPercent) else plausibleUseMs(asSeconds, levelPercent)
    val minutesOk = if (toFull) plausibleUntilFullMs(asMinutes, levelPercent) else plausibleUseMs(asMinutes, levelPercent)
    return when {
        secondsOk -> asSeconds
        minutesOk -> asMinutes
        else -> null
    }
}

/** Recent same-plug stretch. Needs a real percent change, not a flat seeded chart. */
internal fun estimateMsFromRate(
    points: List<BatteryRatePoint>,
    levelPercent: Int,
    plugged: Boolean,
    now: Long = Long.MAX_VALUE,
): Long? {
    val same = points.filter { it.plugged == plugged && it.epochMillis <= now }
    if (same.size < 2) return null
    var elapsed = 0L
    var delta = 0
    var prev = same.last()
    for (index in same.lastIndex - 1 downTo 0) {
        val older = same[index]
        val gap = prev.epochMillis - older.epochMillis
        if (gap <= 0L || gap > 2L * HOUR_MS) break
        val step = prev.levelPercent - older.levelPercent
        if (plugged && step < 0) break
        if (!plugged && step > 0) break
        elapsed += gap
        delta += step
        prev = older
        if (elapsed >= 3L * HOUR_MS) break
    }
    if (elapsed < 12L * 60_000L || delta == 0) return null
    return if (plugged) {
        if (delta < 1) return null
        val ms = ((100 - levelPercent).toDouble() / delta.toDouble() * elapsed).toLong()
        ms.takeIf { plausibleUntilFullMs(it, levelPercent) }
    } else {
        val dropped = -delta
        if (dropped < 1) return null
        val ms = (levelPercent.toDouble() / dropped.toDouble() * elapsed).toLong()
        ms.takeIf { plausibleUseMs(it, levelPercent) }
    }
}

/** Geometric mean when the two estimates agree; otherwise keep the averaged history. */
internal fun blendBatteryEstimates(sensorMs: Long?, historyMs: Long?): Long? {
    if (sensorMs == null) return historyMs
    if (historyMs == null) return sensorMs
    val ratio = sensorMs.toDouble() / historyMs.toDouble()
    if (ratio < 0.5 || ratio > 2.0) return historyMs
    return kotlin.math.sqrt(sensorMs.toDouble() * historyMs.toDouble()).toLong()
}

/**
 * Builds a full 24h stick grid. Gaps between real samples (and out to [end]) are filled by
 * linear interpolation; before the first sample the first known level is held.
 *
 * @param stickCount number of sticks across 24h (default 49 ≈ every 30 min; compact uses ~25).
 */
fun densifyBatterySamples(
    samples: List<BatterySample>,
    end: Long = System.currentTimeMillis(),
    currentLevel: Int = samples.lastOrNull()?.levelPercent ?: 0,
    stickCount: Int = MAX_SAMPLES,
): List<BatterySample> {
    val count = stickCount.coerceAtLeast(2)
    val start = end - HISTORY_WINDOW_MS
    val step = HISTORY_WINDOW_MS / (count - 1)
    val sorted = samples.sortedBy { it.epochMillis }
    val level = currentLevel.coerceIn(0, 100)
    if (sorted.isEmpty()) {
        return List(count) { index ->
            BatterySample(
                epochMillis = start + index * step,
                levelPercent = level,
                charging = false,
            )
        }
    }
    val levels = IntArray(count) { index ->
        levelAtTime(sorted, start + index * step, end, level)
    }
    return List(count) { index ->
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
