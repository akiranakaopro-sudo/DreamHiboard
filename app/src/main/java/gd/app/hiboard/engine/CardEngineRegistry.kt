package gd.app.hiboard.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import gd.app.hiboard.data.NotesRepository
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
    private val notes = NotesRepository(appContext)
    private val engines: Map<CardEngineId, CardEngine> = mapOf(
        CardEngineId.Advice to CardEngine { AdviceContent.current() },
        CardEngineId.Weather to CardEngine {
            CardContent(
                weatherTempC = 18 + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) % 8,
                weatherSummary = "Local sample",
            )
        },
        CardEngineId.Notes to CardEngine {
            val latest = notes.latest()
            CardContent(
                notesPreview = latest.title,
                notesSnippet = latest.snippet,
                notesWhen = formatNotesWhen(latest.updatedAt),
            )
        },
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
        val latest = notes.latest()
        if (latest.id > 0L) return editNoteIntent(latest.id)
        return createNote()
    }

    fun createNote(): Intent {
        return Intent(Intent.ACTION_CREATE_NOTE)
            .setClassName(NOTE_PACKAGE, "$NOTE_PACKAGE.MainActivity")
            .addCategory(Intent.CATEGORY_DEFAULT)
    }

    private fun editNoteIntent(noteId: Long): Intent {
        return Intent("gd.app.note.VIEW_NOTE")
            .setClassName(NOTE_PACKAGE, "$NOTE_PACKAGE.widget.WidgetNoteActivity")
            .setData(Uri.parse("notes://note/$noteId"))
            .putExtra("id", noteId)
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
        weatherTempC = if (b.weatherSummary.isNotBlank()) b.weatherTempC else a.weatherTempC,
        weatherSummary = b.weatherSummary.ifBlank { a.weatherSummary },
        notesPreview = b.notesPreview.ifBlank { a.notesPreview },
        notesSnippet = b.notesSnippet.ifBlank { a.notesSnippet },
        notesWhen = b.notesWhen.ifBlank { a.notesWhen },
        infoFlow = b.infoFlow.ifEmpty { a.infoFlow },
        recentApps = b.recentApps.ifEmpty { a.recentApps },
    )
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
