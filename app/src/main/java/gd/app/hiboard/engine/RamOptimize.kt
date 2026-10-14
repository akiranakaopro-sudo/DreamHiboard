package gd.app.hiboard.engine

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.widget.Toast
import gd.app.hiboard.R
import java.util.concurrent.Executors

private const val TAG = "HiboardRam"
private const val RECENT_LIMIT = 21
private const val BYTES_PER_MB = 1_000_000L

private val optimizer = Executors.newSingleThreadExecutor { runnable ->
    Thread(runnable, "hiboard-ram")
}

/** Frees RAM the way System Manager does: drop recent tasks, then their background processes. */
fun optimizeRam(context: Context) {
    val app = context.applicationContext
    optimizer.execute {
        val freed = runCatching { clearRecentRam(app) }
            .onFailure { error -> Log.w(TAG, "optimize failed", error) }
            .getOrDefault(0L)
            .coerceAtLeast(0L)
        val message = if (freed < BYTES_PER_MB) {
            app.getString(R.string.storage_already_clean)
        } else {
            app.getString(R.string.storage_optimized, Formatter.formatFileSize(app, freed))
        }
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
        }
    }
}

private fun clearRecentRam(context: Context): Long {
    val activityManager = context.getSystemService(ActivityManager::class.java) ?: return 0L
    val before = availableMemory(activityManager)
    val home = homeComponents(context.packageManager)
    val homePackages = home.map { it.packageName }.toSet()
    val targets = LinkedHashSet<String>()
    for (task in recentTasks(activityManager)) {
        val component = componentOf(task) ?: continue
        val packageName = component.packageName
        if (packageName == context.packageName) continue
        if (packageName == "com.yft.systemmanager") continue
        if (component in home || packageName in homePackages) continue
        if (isWhitelisted(context, packageName)) continue
        removeTask(taskIdOf(task))
        targets.add(packageName)
    }
    for (packageName in targets) {
        runCatching { activityManager.killBackgroundProcesses(packageName) }
    }
    val after = availableMemory(activityManager)
    val freed = after - before
    Log.i(TAG, "optimized ${targets.size} apps, freed $freed bytes")
    return freed
}

@Suppress("DEPRECATION")
private fun recentTasks(activityManager: ActivityManager): List<ActivityManager.RecentTaskInfo> {
    return runCatching {
        activityManager.getRecentTasks(RECENT_LIMIT, ActivityManager.RECENT_IGNORE_UNAVAILABLE)
    }.getOrNull().orEmpty()
}

@Suppress("DEPRECATION")
private fun componentOf(task: ActivityManager.RecentTaskInfo): ComponentName? {
    return task.origActivity ?: task.baseIntent?.component
}

@Suppress("DEPRECATION")
private fun taskIdOf(task: ActivityManager.RecentTaskInfo): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return task.taskId
    return runCatching {
        ActivityManager.RecentTaskInfo::class.java.getField("persistentId").getInt(task)
    }.getOrDefault(task.id)
}

private fun homeComponents(packageManager: PackageManager): Set<ComponentName> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolved = runCatching {
        packageManager.queryIntentActivities(intent, 0)
    }.getOrNull().orEmpty()
    return resolved.mapNotNull { info ->
        val activity = info.activityInfo ?: return@mapNotNull null
        ComponentName(activity.packageName, activity.name)
    }.toSet()
}

/** System Manager stores a whitelist flag in Settings.System under the package name. */
private fun isWhitelisted(context: Context, packageName: String): Boolean {
    return runCatching {
        Settings.System.getInt(context.contentResolver, packageName, 0) == 1
    }.getOrDefault(false)
}

private fun removeTask(taskId: Int): Boolean {
    if (taskId < 0) return false
    if (removeTaskFrom("android.app.ActivityTaskManager", taskId)) return true
    return removeTaskFrom("android.app.ActivityManager", taskId)
}

private fun removeTaskFrom(className: String, taskId: Int): Boolean {
    return try {
        val service = Class.forName(className).getMethod("getService").invoke(null) ?: return false
        val method = service.javaClass.methods.firstOrNull { candidate ->
            candidate.name == "removeTask" &&
                candidate.parameterTypes.size == 1 &&
                candidate.parameterTypes[0] == Int::class.javaPrimitiveType
        } ?: return false
        method.invoke(service, taskId)
        true
    } catch (_: Throwable) {
        false
    }
}

private fun availableMemory(activityManager: ActivityManager): Long {
    val info = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(info)
    return info.availMem
}
