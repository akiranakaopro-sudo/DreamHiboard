package gd.app.hiboard.catalog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.AlarmClock
import gd.app.hiboard.R
import gd.app.hiboard.engine.NOTE_PACKAGE
import gd.app.hiboard.engine.RECORDER_PACKAGE
import gd.app.hiboard.engine.SYSTEM_MANAGER_PACKAGES
import gd.app.hiboard.model.CardEngineId

/** Bundled add-widget icon when we should not use the installed app icon. */
fun storeRowIconRes(engine: CardEngineId): Int? = when (engine) {
    CardEngineId.Battery -> R.drawable.ic_store_battery
    CardEngineId.Calendar -> R.drawable.ic_store_calendar
    CardEngineId.Flashlight -> R.drawable.ic_store_flashlight
    CardEngineId.Weather -> R.drawable.ic_store_weather
    else -> null
}

/** Launcher icon for the app that owns this store category, if installed. */
fun storeAppIcon(context: Context, engine: CardEngineId): Drawable? {
    val pm = context.packageManager
    storeAppComponents(engine).forEach { component ->
        iconForComponent(pm, component)?.let { return it }
    }
    storeAppPackages(engine).forEach { pkg ->
        if (engine == CardEngineId.Contacts && pkg == "com.android.dialer") {
            // Dialer's application icon is Phone; Contacts uses a dedicated activity icon.
            return@forEach
        }
        iconForPackage(pm, pkg)?.let { return it }
    }
    storeAppIntents(engine).forEach { intent ->
        val resolved = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) ?: return@forEach
        resolved.loadIcon(pm)?.let { return it }
    }
    return null
}

internal fun storeAppPackages(engine: CardEngineId): List<String> = when (engine) {
    CardEngineId.Notes -> listOf(NOTE_PACKAGE)
    CardEngineId.Calendar -> listOf(
        "gd.app.calendar",
        "com.oplus.calendar",
        "com.coloros.calendar",
        "com.google.android.calendar",
        "com.android.calendar",
    )
    CardEngineId.Clock,
    CardEngineId.WeatherClock,
    CardEngineId.LocalTime,
    CardEngineId.RomanClock,
    CardEngineId.WeatherDial,
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
        "com.android.dialer",
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
    CardEngineId.Storage,
    CardEngineId.Battery,
    -> SYSTEM_MANAGER_PACKAGES + listOf(
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

private fun storeAppComponents(engine: CardEngineId): List<ComponentName> = when (engine) {
    CardEngineId.Calendar -> listOf(
        ComponentName("gd.app.calendar", "com.gdcalendar.MainActivity"),
        ComponentName("com.oplus.calendar", "com.android.calendar.AllInOneActivity"),
        ComponentName("com.coloros.calendar", "com.android.calendar.AllInOneActivity"),
        ComponentName("com.google.android.calendar", "com.android.calendar.AllInOneActivity"),
    )
    CardEngineId.Contacts -> listOf(
        ComponentName("com.android.dialer", "com.android.contacts.PeopleActivity"),
        ComponentName("com.android.contacts", "com.android.contacts.activities.PeopleActivity"),
        ComponentName("com.google.android.contacts", "com.android.contacts.activities.PeopleActivity"),
    )
    else -> emptyList()
}

private fun storeAppIntents(engine: CardEngineId): List<Intent> = when (engine) {
    CardEngineId.Calendar -> listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("gd.app.calendar"),
    )
    CardEngineId.Contacts -> listOf(
        Intent("com.android.contacts.action.CONTACTS_MAIN").addCategory(Intent.CATEGORY_DEFAULT),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS),
    )
    CardEngineId.Music -> listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC),
    )
    CardEngineId.Clock,
    CardEngineId.WeatherClock,
    CardEngineId.LocalTime,
    CardEngineId.RomanClock,
    CardEngineId.WeatherDial,
    -> listOf(Intent(AlarmClock.ACTION_SHOW_ALARMS))
    else -> emptyList()
}

private fun iconForPackage(pm: PackageManager, packageName: String): Drawable? {
    if (packageName.isBlank()) return null
    return try {
        pm.getLaunchIntentForPackage(packageName)?.component?.let { iconForComponent(pm, it) }
            ?: launcherMipmapIcon(pm, packageName)
            ?: pm.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}

private fun launcherMipmapIcon(pm: PackageManager, packageName: String): Drawable? {
    return try {
        val res = pm.getResourcesForApplication(packageName)
        val id = res.getIdentifier("ic_launcher", "mipmap", packageName)
        if (id != 0) res.getDrawable(id, null) else null
    } catch (_: Exception) {
        null
    }
}

private fun iconForComponent(pm: PackageManager, component: ComponentName): Drawable? {
    return try {
        pm.getActivityIcon(component)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}
