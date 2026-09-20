package gd.app.hiboard.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import gd.app.hiboard.data.RecentAppsRepository
import gd.app.hiboard.model.AdviceItem
import gd.app.hiboard.model.CardAction
import gd.app.hiboard.model.CardContent
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.InfoFlowItem
import gd.app.hiboard.model.ShortcutApp
import java.util.Calendar

fun interface CardEngine {
    fun bind(action: CardAction): CardContent
}

class CardEngineRegistry(context: Context) {
    private val appContext = context.applicationContext
    private val recents = RecentAppsRepository(appContext)
    private val engines: Map<CardEngineId, CardEngine> = mapOf(
        CardEngineId.Advice to CardEngine { AdviceContent.current() },
        CardEngineId.Shortcuts to CardEngine { CardContent(shortcuts = launcherApps(appContext, 5)) },
        CardEngineId.Weather to CardEngine {
            CardContent(
                weatherTempC = 18 + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) % 8,
                weatherSummary = "Local sample",
            )
        },
        CardEngineId.Notes to CardEngine {
            CardContent(notesPreview = "Tap to write")
        },
        CardEngineId.Favorite to CardEngine { CardContent(favorites = launcherApps(appContext, 3)) },
        CardEngineId.InfoFlow to CardEngine {
            CardContent(
                infoFlow = listOf(
                    InfoFlowItem("Minus-one, without the ad stack", "Hiboard"),
                    InfoFlowItem("Subscribe vs recommend is the product", "Hiboard"),
                ),
            )
        },
        CardEngineId.RecentApps to CardEngine { CardContent(recentApps = recents.apps()) },
    )

    fun compose(action: CardAction): CardContent {
        return engines.values.fold(CardContent()) { acc, engine ->
            merge(acc, engine.bind(action))
        }
    }

    fun openNotes(): Intent? {
        val launch = appContext.packageManager.getLaunchIntentForPackage("gd.app.note")
        return launch ?: Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "")
        }
    }

    fun openQuickSearch(): Intent? {
        return appContext.packageManager.getLaunchIntentForPackage("gd.app.quicksearch")
    }

    fun openApp(app: ShortcutApp): Intent? {
        recents.remember(app.packageName)
        if (app.packageName == SETTINGS_PACKAGE) {
            return systemSettingsLaunch()
        }
        if (app.activityName != null) {
            return Intent(Intent.ACTION_MAIN).setClassName(app.packageName, app.activityName)
        }
        return appContext.packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
    }

    private fun systemSettingsLaunch(): Intent {
        return Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(SETTINGS_PACKAGE, SETTINGS_ACTIVITY)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }

    fun refreshRecents() {
        recents.syncFromUsage()
    }

    private fun merge(a: CardContent, b: CardContent): CardContent = CardContent(
        adviceGreeting = b.adviceGreeting.ifBlank { a.adviceGreeting },
        adviceItems = b.adviceItems.ifEmpty { a.adviceItems },
        shortcuts = b.shortcuts.ifEmpty { a.shortcuts },
        weatherTempC = if (b.weatherSummary.isNotBlank()) b.weatherTempC else a.weatherTempC,
        weatherSummary = b.weatherSummary.ifBlank { a.weatherSummary },
        notesPreview = b.notesPreview.ifBlank { a.notesPreview },
        favorites = b.favorites.ifEmpty { a.favorites },
        infoFlow = b.infoFlow.ifEmpty { a.infoFlow },
        recentApps = b.recentApps.ifEmpty { a.recentApps },
    )

    private fun launcherApps(context: Context, limit: Int): List<ShortcutApp> {
        val pm = context.packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(query, PackageManager.MATCH_DEFAULT_ONLY)
            .asSequence()
            .map {
                ShortcutApp(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                )
            }
            .filterNot { it.packageName == context.packageName }
            .distinctBy { it.packageName }
            .take(limit)
            .toList()
    }
}

private object AdviceContent {
    fun current(): CardContent {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
        return CardContent(
            adviceGreeting = greeting,
            adviceItems = listOf(
                AdviceItem("Minus-one screen", "Pinned cards stay; discover stays below."),
                AdviceItem("No ad SDK", "Pangle, Dingxiang, and Seedling stay out."),
            ),
        )
    }
}
