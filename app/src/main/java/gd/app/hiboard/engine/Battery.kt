package gd.app.hiboard.engine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.provider.Settings
import gd.app.hiboard.model.BatterySample
import kotlin.math.roundToInt

data class BatterySnapshot(
    val levelPercent: Int = 0,
    val charging: Boolean = false,
    val samples: List<BatterySample> = emptyList(),
)

private const val PREFS = "hiboard_battery"
private const val KEY_SAMPLES = "samples"
private const val MAX_SAMPLES = 49
private const val SAMPLE_INTERVAL_MS = 30L * 60L * 1000L
private const val HISTORY_WINDOW_MS = 24L * 60L * 60L * 1000L

class BatteryReader(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun snapshot(): BatterySnapshot {
        val sticky = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = sticky.batteryPercent()
        val charging = sticky.isCharging()
        val samples = persistAndLoad(level)
        return BatterySnapshot(levelPercent = level, charging = charging, samples = samples)
    }

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

    private fun persistAndLoad(level: Int): List<BatterySample> {
        val now = System.currentTimeMillis()
        val existing = loadSamples().filter { now - it.epochMillis <= HISTORY_WINDOW_MS }
        val last = existing.lastOrNull()
        val shouldAppend = last == null ||
            last.levelPercent != level ||
            now - last.epochMillis >= SAMPLE_INTERVAL_MS
        val next = when {
            existing.isEmpty() -> seedFlatHistory(level, now)
            shouldAppend -> (existing + BatterySample(now, level)).takeLast(MAX_SAMPLES)
            else -> existing
        }
        if (next != existing) saveSamples(next)
        return next
    }

    private fun seedFlatHistory(level: Int, now: Long): List<BatterySample> {
        val step = HISTORY_WINDOW_MS / (MAX_SAMPLES - 1)
        return List(MAX_SAMPLES) { index ->
            BatterySample(epochMillis = now - (MAX_SAMPLES - 1 - index) * step, levelPercent = level)
        }
    }

    private fun loadSamples(): List<BatterySample> {
        val raw = prefs.getString(KEY_SAMPLES, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(';').mapNotNull { token ->
            val parts = token.split(':')
            if (parts.size != 2) return@mapNotNull null
            val time = parts[0].toLongOrNull() ?: return@mapNotNull null
            val level = parts[1].toIntOrNull() ?: return@mapNotNull null
            BatterySample(time, level.coerceIn(0, 100))
        }
    }

    private fun saveSamples(samples: List<BatterySample>) {
        val packed = samples.joinToString(";") { "${it.epochMillis}:${it.levelPercent}" }
        prefs.edit().putString(KEY_SAMPLES, packed).apply()
    }
}

private fun Intent?.batteryPercent(): Int {
    if (this == null) return 0
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
    if (level < 0) return 0
    return ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
}

private fun Intent?.isCharging(): Boolean {
    if (this == null) return false
    return when (getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
        BatteryManager.BATTERY_STATUS_CHARGING,
        BatteryManager.BATTERY_STATUS_FULL,
        -> true
        else -> {
            val plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            plugged != 0
        }
    }
}

/** Demo series for store previews (gentle decline toward now). */
fun previewBatterySamples(level: Int = 63, now: Long = System.currentTimeMillis()): List<BatterySample> {
    val step = HISTORY_WINDOW_MS / (MAX_SAMPLES - 1)
    return List(MAX_SAMPLES) { index ->
        val t = index.toFloat() / (MAX_SAMPLES - 1)
        val value = (level + 4 - (t * 6f)).roundToInt().coerceIn(0, 100)
        BatterySample(epochMillis = now - (MAX_SAMPLES - 1 - index) * step, levelPercent = value)
    }
}
