package gd.app.hiboard.catalog

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.AlarmClock
import gd.app.hiboard.engine.NOTE_PACKAGE
import gd.app.hiboard.engine.RECORDER_PACKAGE
import gd.app.hiboard.engine.SYSTEM_MANAGER_PACKAGES
import gd.app.hiboard.model.CardEngineId

/** Launcher icon for the app that owns this store category, if installed. */
fun storeAppIcon(context: Context, engine: CardEngineId): Drawable? {
    val pm = context.packageManager
    storeAppPackages(engine).forEach { pkg ->
        iconForPackage(pm, pkg)?.let { return it }
    }
    storeAppIntent(engine)?.let { intent ->
        val resolved = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) ?: return@let
        iconForPackage(pm, resolved.activityInfo.packageName)?.let { return it }
    }
    return null
}

internal fun storeAppPackages(engine: CardEngineId): List<String> = when (engine) {
    CardEngineId.Notes -> listOf(NOTE_PACKAGE)
    CardEngineId.Calendar -> listOf("gd.app.calendar")
    CardEngineId.Clock,
    CardEngineId.WeatherClock,
    CardEngineId.LocalTime,
    -> listOf(
        "com.coloros.alarmclock",
        "com.oplus.alarmclock",
        "com.android.deskclock",
    )
    CardEngineId.Contacts -> listOf(
        "gd.app.contacts",
        "com.android.contacts",
        "com.google.android.contacts",
        "com.coloros.contacts",
        "com.oplus.contacts",
    )
    CardEngineId.Flashlight -> listOf(
        "gd.app.flashlight",
        "com.oplus.flashlight",
        "com.coloros.flashlight",
    )
    CardEngineId.Music -> listOf(
        "gd.app.musicplayer",
        "com.heytap.music",
        "com.oplus.music",
        "com.tencent.qqmusic",
        "com.netease.cloudmusic",
        "com.kugou.android",
        "cn.kuwo.player",
        "com.google.android.apps.youtube.music",
        "com.android.music",
    )
    CardEngineId.Recorder -> listOf(RECORDER_PACKAGE)
    CardEngineId.Storage -> SYSTEM_MANAGER_PACKAGES + listOf(
        "com.yft.systemmanager",
        "com.android.storagemanager",
    )
    CardEngineId.Weather -> listOf(
        "com.coloros.weather2",
        "com.oplus.weather",
        "com.coloros.weather",
        "gd.app.weather",
    )
    CardEngineId.RecentApps -> listOf("gd.app.quicksearch")
}

private fun storeAppIntent(engine: CardEngineId): Intent? = when (engine) {
    CardEngineId.Calendar -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
    CardEngineId.Contacts -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)
    CardEngineId.Music -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
    CardEngineId.Clock,
    CardEngineId.WeatherClock,
    CardEngineId.LocalTime,
    -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
    else -> null
}

private fun iconForPackage(pm: PackageManager, packageName: String): Drawable? {
    if (packageName.isBlank()) return null
    return try {
        pm.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}
