package gd.app.hiboard.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager
import gd.app.hiboard.engine.RECENT_APP_LIMIT
import gd.app.hiboard.engine.recentPackageOrder
import gd.app.hiboard.model.ShortcutApp

class RecentAppsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private var recents: List<String> = load()

    fun apps(): List<ShortcutApp> {
        val defaults = defaultApps()
        val byPackage = defaults.associateBy { it.packageName }.toMutableMap()
        recents.forEach { packageName ->
            if (packageName !in byPackage) {
                lookup(packageName)?.let { byPackage[packageName] = it }
            }
        }
        return recentPackageOrder(
            defaults = defaults.map { it.packageName },
            recents = recents,
        ).mapNotNull(byPackage::get).let { ordered ->
            if (ordered.size >= RECENT_APP_LIMIT) {
                ordered
            } else {
                val extra = launcherFallback().filter { candidate ->
                    ordered.none { it.packageName == candidate.packageName }
                }
                (ordered + extra).take(RECENT_APP_LIMIT)
            }
        }
    }

    fun remember(packageName: String) {
        if (packageName.isBlank() || packageName == appContext.packageName) return
        if (lookup(packageName) == null) return
        recents = recentPackageOrder(
            defaults = defaultApps().map { it.packageName },
            recents = recents,
            lastOpened = packageName,
        )
        prefs.edit().putString(KEY, recents.joinToString(",")).apply()
    }

    fun syncFromUsage() {
        lastUsedLauncherPackage()?.let(::remember)
    }

    private fun load(): List<String> =
        prefs.getString(KEY, "")
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }

    private fun defaultApps(): List<ShortcutApp> = listOfNotNull(
        resolve(dialerIntent(), "Dialer"),
        resolve(contactsIntent(), "Contacts"),
        resolve(messagesIntent(), "Messages"),
        resolve(cameraIntent(), "Camera"),
        resolve(settingsIntent(), "Settings"),
    ).distinctBy { it.packageName }.take(RECENT_APP_LIMIT)

    private fun lastUsedLauncherPackage(): String? {
        val usm = appContext.getSystemService(UsageStatsManager::class.java) ?: return null
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - DAY_MS, now)
            ?: return null
        return stats.sortedByDescending { it.lastTimeUsed }
            .map { it.packageName }
            .firstOrNull { packageName ->
                packageName != appContext.packageName && lookup(packageName) != null
            }
    }

    private fun launcherFallback(): List<ShortcutApp> {
        val pm = appContext.packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(query, PackageManager.MATCH_DEFAULT_ONLY)
            .map {
                ShortcutApp(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                )
            }
            .filterNot { it.packageName == appContext.packageName }
            .distinctBy { it.packageName }
    }

    private fun lookup(packageName: String): ShortcutApp? {
        val pm = appContext.packageManager
        val launch = pm.getLaunchIntentForPackage(packageName) ?: return null
        val info = launch.resolveActivityInfo(pm, 0) ?: return null
        return ShortcutApp(
            label = info.loadLabel(pm).toString(),
            packageName = info.packageName,
            activityName = info.name,
        )
    }

    private fun resolve(intent: Intent, fallbackLabel: String): ShortcutApp? {
        val pm = appContext.packageManager
        val info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo
            ?: return null
        if (info.packageName == appContext.packageName) return null
        val launch = pm.getLaunchIntentForPackage(info.packageName)
        val activity = launch?.resolveActivityInfo(pm, 0) ?: info
        return ShortcutApp(
            label = activity.loadLabel(pm)?.toString()?.ifBlank { fallbackLabel } ?: fallbackLabel,
            packageName = activity.packageName,
            activityName = activity.name,
        )
    }

    private fun dialerIntent(): Intent {
        val pm = appContext.packageManager
        val dialer = appContext.getSystemService(TelecomManager::class.java)?.defaultDialerPackage
        if (!dialer.isNullOrBlank() && pm.getLaunchIntentForPackage(dialer) != null) {
            return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(dialer)
        }
        listOf("com.android.dialer", "com.google.android.dialer", "com.coloros.dialer").forEach { pkg ->
            if (pm.getLaunchIntentForPackage(pkg) != null) {
                return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg)
            }
        }
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
    }

    private fun contactsIntent(): Intent =
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)
            .takeIf { appContext.packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null }
            ?: Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)

    private fun messagesIntent(): Intent {
        val sms = Telephony.Sms.getDefaultSmsPackage(appContext)
        if (!sms.isNullOrBlank()) {
            return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(sms)
        }
        return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
    }

    private fun cameraIntent(): Intent =
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            .takeIf { appContext.packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null }
            ?: Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    private fun settingsIntent(): Intent {
        val launch = appContext.packageManager.getLaunchIntentForPackage("com.android.settings")
        if (launch != null) return launch
        return Intent(Settings.ACTION_SETTINGS)
    }

    private companion object {
        const val PREFS = "hiboard_recents"
        const val KEY = "packages"
        const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
