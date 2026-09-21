package gd.app.hiboard.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import gd.app.hiboard.data.NotesRepository
import gd.app.hiboard.data.RecentAppsRepository
import gd.app.hiboard.data.WeatherStore
import gd.app.hiboard.model.CardAction
import gd.app.hiboard.model.CardContent
import gd.app.hiboard.model.CardEngineId
import gd.app.hiboard.model.ShortcutApp
import gd.app.hiboard.model.WeatherDayContent

fun interface CardEngine {
    fun bind(action: CardAction): CardContent
}

class CardEngineRegistry(context: Context) {
    private val appContext = context.applicationContext
    private val recents = RecentAppsRepository(appContext)
    private val notes = NotesRepository(appContext)
    private val weather = WeatherStore.get(appContext)
    private val flashlight = FlashlightController(appContext)
    private val storage = StorageReader(appContext)
    private val recorder = RecorderClient(appContext)
    private val engines: Map<CardEngineId, CardEngine> = mapOf(
        CardEngineId.Weather to CardEngine {
            val latest = weather.current().resolved()
            CardContent(
                weatherLocation = latest.location,
                weatherTempC = latest.temperatureC,
                weatherSummary = latest.condition.displayName,
                weatherCondition = latest.condition.json,
                weatherDays = latest.days.map { day ->
                    WeatherDayContent(day.label, day.condition.json, day.lowC, day.highC)
                },
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
        CardEngineId.RecentApps to CardEngine { CardContent(recentApps = recents.apps()) },
        CardEngineId.Flashlight to CardEngine {
            CardContent(
                flashlightOn = flashlight.on.value,
                flashlightAvailable = flashlight.available,
            )
        },
        CardEngineId.Storage to CardEngine {
            val status = storage.status()
            CardContent(
                storageUsedBytes = status.usedBytes,
                storageTotalBytes = status.totalBytes,
            )
        },
        CardEngineId.Recorder to CardEngine {
            val status = recorder.status.value
            CardContent(
                recorderState = status.state,
                recorderElapsedMs = status.elapsedMs,
                recorderBound = true,
            )
        },
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

    val flashlightOn = flashlight.on
    val recorderStatus = recorder.status
    val notesRevisions = notes.revisions
    val weatherSnapshot = weather.snapshot

    fun toggleFlashlight(): FlashlightToggle = flashlight.toggle()

    fun openSystemManager(): Intent? = storage.openSystemManager()

    fun sendRecorder(command: RecorderCommand): RecorderSendResult = recorder.send(command)

    fun recorderLive() = recorder.live()

    fun openRecorder(): Intent? = recorder.openRecorder()

    fun syncRecorder() = recorder.sync()

    private fun merge(a: CardContent, b: CardContent): CardContent = CardContent(
        weatherLocation = b.weatherLocation.ifBlank { a.weatherLocation },
        weatherTempC = if (b.weatherSummary.isNotBlank()) b.weatherTempC else a.weatherTempC,
        weatherSummary = b.weatherSummary.ifBlank { a.weatherSummary },
        weatherCondition = b.weatherCondition.ifBlank { a.weatherCondition },
        weatherDays = b.weatherDays.ifEmpty { a.weatherDays },
        notesPreview = b.notesPreview.ifBlank { a.notesPreview },
        notesSnippet = b.notesSnippet.ifBlank { a.notesSnippet },
        notesWhen = b.notesWhen.ifBlank { a.notesWhen },
        flashlightOn = b.flashlightOn || a.flashlightOn,
        flashlightAvailable = b.flashlightAvailable || a.flashlightAvailable,
        storageUsedBytes = if (b.storageTotalBytes > 0L) b.storageUsedBytes else a.storageUsedBytes,
        storageTotalBytes = if (b.storageTotalBytes > 0L) b.storageTotalBytes else a.storageTotalBytes,
        recorderState = if (b.recorderBound) b.recorderState else a.recorderState,
        recorderElapsedMs = if (b.recorderBound) b.recorderElapsedMs else a.recorderElapsedMs,
        recorderBound = a.recorderBound || b.recorderBound,
        recentApps = b.recentApps.ifEmpty { a.recentApps },
    )
}
