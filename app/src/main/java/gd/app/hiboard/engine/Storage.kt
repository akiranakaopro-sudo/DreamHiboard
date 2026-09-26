package gd.app.hiboard.engine

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlin.math.roundToInt

data class StorageStatus(
    val usedBytes: Long = 0L,
    val totalBytes: Long = 0L,
) {
    val ratio: Float
        get() = if (totalBytes <= 0L) {
            0f
        } else {
            (usedBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
        }
}

fun storageStatusFrom(totalBytes: Long, freeBytes: Long): StorageStatus {
    val total = totalBytes.coerceAtLeast(0L)
    val free = freeBytes.coerceIn(0L, total)
    return StorageStatus(usedBytes = total - free, totalBytes = total)
}

fun formatStorageGb(bytes: Long): String {
    val tenths = ((bytes.coerceAtLeast(0L) / 1_000_000_000.0) * 10.0).roundToInt()
    return if (tenths % 10 == 0) {
        "${tenths / 10}GB"
    } else {
        "${tenths / 10}.${tenths % 10}GB"
    }
}

fun formatStorageGbValue(bytes: Long): String {
    val tenths = ((bytes.coerceAtLeast(0L) / 1_000_000_000.0) * 10.0).roundToInt()
    return if (tenths % 10 == 0) {
        "${tenths / 10}"
    } else {
        "${tenths / 10}.${tenths % 10}"
    }
}

fun formatStorageUsage(usedBytes: Long, totalBytes: Long): String {
    if (totalBytes <= 0L) return "—"
    return "${formatStorageGb(usedBytes)} / ${formatStorageGb(totalBytes)}"
}

fun formatStoragePair(usedBytes: Long, totalBytes: Long): String {
    if (totalBytes <= 0L) return "—"
    val used = (usedBytes - STORAGE_DISPLAY_OFFSET_BYTES).coerceAtLeast(0L)
    val total = (totalBytes - STORAGE_DISPLAY_OFFSET_BYTES).coerceAtLeast(0L)
    return "${formatStorageGb(used)} | ${formatStorageGb(total)}"
}

fun formatStoragePercent(usedBytes: Long, totalBytes: Long): String {
    if (totalBytes <= 0L) return "—"
    val used = (usedBytes - STORAGE_DISPLAY_OFFSET_BYTES).coerceAtLeast(0L)
    val total = (totalBytes - STORAGE_DISPLAY_OFFSET_BYTES).coerceAtLeast(0L)
    if (total <= 0L) return "—"
    val percent = ((used.toDouble() / total.toDouble()) * 100.0)
        .roundToInt()
        .coerceIn(0, 100)
    return "$percent%"
}

/** Always subtract this from displayed used/total GB labels. */
internal const val STORAGE_DISPLAY_OFFSET_BYTES = 400_000_000L

internal val SYSTEM_MANAGER_PACKAGES = listOf(
    "gd.app.systemmanager",
    "gd.app.phonemanager",
    "com.coloros.phonemanager",
    "com.oplus.phonemanager",
    "com.oplus.safecenter",
    "com.coloros.safecenter",
)

fun systemManagerIntents(): List<Intent> {
    val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
    val explicit = listOf(
        ComponentName("com.coloros.phonemanager", "com.oplus.phonemanager.clear.ClearMainActivity"),
        ComponentName("com.oplus.phonemanager", "com.oplus.phonemanager.clear.ClearMainActivity"),
        ComponentName("gd.app.systemmanager", "gd.app.systemmanager.MainActivity"),
        ComponentName("gd.app.phonemanager", "gd.app.phonemanager.MainActivity"),
    ).map { name ->
        Intent(Intent.ACTION_MAIN).setComponent(name).addFlags(flags)
    }
    val actions = listOf(
        "oplus.intent.action.CLEAR_MAIN_ACTIVITY",
        "com.oppo.cleandroid.ui.ClearMainActivity",
        "oplus.intent.action.PHONE_MANAGER_MAIN_ACTIVITY",
        "oppo.intent.action.SAFE_CENTER_MAIN",
    ).map { action ->
        Intent(action).addFlags(flags)
    }
    val launchers = SYSTEM_MANAGER_PACKAGES.map { pkg ->
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(pkg)
            .addFlags(flags)
    }
    return explicit + actions + launchers
}

fun <T> pickFirstResolvable(
    candidates: List<T>,
    canResolve: (T) -> Boolean,
): T? = candidates.firstOrNull(canResolve)

fun pickSystemManagerIntent(
    candidates: List<Intent>,
    canResolve: (Intent) -> Boolean,
): Intent? = pickFirstResolvable(candidates, canResolve)

class StorageReader(context: Context) {
    private val appContext = context.applicationContext

    fun status(): StorageStatus {
        val manager = appContext.getSystemService(ActivityManager::class.java)
            ?: return StorageStatus()
        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        return storageStatusFrom(info.totalMem, info.availMem)
    }

    fun openSystemManager(): Intent? {
        val pm = appContext.packageManager
        pickSystemManagerIntent(systemManagerIntents()) { intent ->
            pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        }?.let { return it }
        return launcherNamedSystemManager(pm)
    }

    private fun launcherNamedSystemManager(pm: PackageManager): Intent? {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val match = pm.queryIntentActivities(query, 0).firstOrNull { resolve ->
            val label = resolve.loadLabel(pm).toString()
            label.equals("System Manager", ignoreCase = true) ||
                label.equals("Phone Manager", ignoreCase = true)
        } ?: return null
        return Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(match.activityInfo.packageName, match.activityInfo.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }
}
