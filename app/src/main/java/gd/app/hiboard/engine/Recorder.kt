package gd.app.hiboard.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import gd.app.hiboard.R
import gd.app.hiboard.model.RecorderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class RecorderCommand {
    Start,
    Pause,
    Resume,
    Mark,
    Save,
    Open,
}

sealed class RecorderSendResult {
    data object Sent : RecorderSendResult()
    data object NeedsMic : RecorderSendResult()
    data object Failed : RecorderSendResult()
    data object Saved : RecorderSendResult()
    data class Marked(val text: String, val timeMs: Long) : RecorderSendResult()
}

data class RecorderStatus(
    val state: RecorderUiState = RecorderUiState.Idle,
    val elapsedMs: Long = 0L,
    val marks: List<Long> = emptyList(),
    val amplitude: Float = 0f,
)

fun recorderUiStateFrom(name: String?): RecorderUiState = when (name) {
    "RECORDING" -> RecorderUiState.Recording
    "PAUSED" -> RecorderUiState.Paused
    else -> RecorderUiState.Idle
}

fun recorderPrimaryCommand(state: RecorderUiState): RecorderCommand = when (state) {
    RecorderUiState.Idle -> RecorderCommand.Start
    RecorderUiState.Recording -> RecorderCommand.Pause
    RecorderUiState.Paused -> RecorderCommand.Resume
}

fun formatRecorderTime(elapsedMs: Long): String {
    val totalSec = (elapsedMs.coerceAtLeast(0L) / 1000L)
    val hours = totalSec / 3600L
    val minutes = (totalSec % 3600L) / 60L
    val seconds = totalSec % 60L
    return if (hours > 0L) {
        "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

internal const val RECORDER_PACKAGE = "gd.app.soundrecorder"
internal const val RECORDER_SERVICE = "gd.app.soundrecorder.recorderservice.RecorderService"
internal const val RECORDER_PERMISSION = "gd.app.soundrecorder.permission.CONTROL_RECORDING"
internal const val RECORDER_ACTION_START = "gd.app.soundrecorder.action.START"
internal const val RECORDER_ACTION_PAUSE = "gd.app.soundrecorder.action.PAUSE"
internal const val RECORDER_ACTION_RESUME = "gd.app.soundrecorder.action.RESUME"
internal const val RECORDER_ACTION_STOP = "gd.app.soundrecorder.action.STOP"
internal const val RECORDER_ACTION_MARK = "gd.app.soundrecorder.action.QUICK_MARK"
internal const val RECORDER_ACTION_SYNC = "gd.app.soundrecorder.action.SYNC"
internal const val RECORDER_ACTION_STATE = "gd.app.soundrecorder.broadcast.STATE"
internal const val RECORDER_EXTRA_STATE = "extra_card_state"
internal const val RECORDER_EXTRA_ELAPSED = "extra_card_elapsed"

fun recorderServiceIntent(action: String): Intent {
    return Intent(action).setClassName(RECORDER_PACKAGE, RECORDER_SERVICE)
}

class RecorderClient(context: Context) {
    private val appContext = context.applicationContext
    private val local = LocalRecorder(appContext)
    private val _status = MutableStateFlow(RecorderStatus())
    val status: StateFlow<RecorderStatus> = _status
    private var usingRemote = false
    private var remoteMarkCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private var tickStartElapsed = 0L
    private var tickStartRealtime = 0L
    private val tick = object : Runnable {
        override fun run() {
            if (_status.value.state != RecorderUiState.Recording) return
            _status.value = if (usingRemote) {
                val elapsed = tickStartElapsed + (SystemClock.elapsedRealtime() - tickStartRealtime)
                _status.value.copy(state = RecorderUiState.Recording, elapsedMs = elapsed)
            } else {
                local.status()
            }
            handler.postDelayed(this, 200L)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != RECORDER_ACTION_STATE) return
            usingRemote = true
            val next = recorderUiStateFrom(intent.getStringExtra(RECORDER_EXTRA_STATE))
            _status.value = RecorderStatus(
                state = next,
                elapsedMs = intent.getLongExtra(RECORDER_EXTRA_ELAPSED, 0L).coerceAtLeast(0L),
                marks = _status.value.marks,
            )
            if (next == RecorderUiState.Idle) {
                remoteMarkCount = 0
                RecorderWaveSession.reset()
            }
            syncTicker()
        }
    }

    init {
        val filter = IntentFilter(RECORDER_ACTION_STATE)
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            RECORDER_PERMISSION,
            null,
            ContextCompat.RECEIVER_EXPORTED,
        )
        sync()
    }

    fun live(): RecorderStatus {
        return if (usingRemote) {
            val current = _status.value
            if (current.state == RecorderUiState.Recording) {
                val elapsed = tickStartElapsed + (SystemClock.elapsedRealtime() - tickStartRealtime)
                current.copy(elapsedMs = elapsed)
            } else {
                current
            }
        } else {
            local.status().copy(amplitude = local.amplitude01())
        }
    }

    fun send(command: RecorderCommand): RecorderSendResult {
        if (command == RecorderCommand.Open) return RecorderSendResult.Failed
        if (command == RecorderCommand.Start && !hasMicPermission()) {
            return RecorderSendResult.NeedsMic
        }
        if (canUseRemote()) {
            usingRemote = true
            optimistic(command)
            dispatchRemote(remoteAction(command))
            return when (command) {
                RecorderCommand.Mark -> {
                    remoteMarkCount += 1
                    RecorderSendResult.Marked(
                        appContext.getString(R.string.recorder_flag_new, remoteMarkCount),
                        _status.value.elapsedMs,
                    )
                }
                RecorderCommand.Save -> RecorderSendResult.Saved
                else -> RecorderSendResult.Sent
            }
        }
        usingRemote = false
        return when (command) {
            RecorderCommand.Start -> {
                RecorderWaveSession.reset()
                finishLocal(local.start())
            }
            RecorderCommand.Pause -> finishLocal(local.pause())
            RecorderCommand.Resume -> finishLocal(local.resume())
            RecorderCommand.Mark -> {
                val mark = local.mark() ?: return RecorderSendResult.Failed
                _status.value = local.status()
                RecorderSendResult.Marked(mark.text, mark.timeMs)
            }
            RecorderCommand.Save -> {
                val ok = local.save()
                if (ok) {
                    RecorderWaveSession.reset()
                    _status.value = local.status()
                    syncTicker()
                    RecorderSendResult.Saved
                } else {
                    RecorderSendResult.Failed
                }
            }
            RecorderCommand.Open -> RecorderSendResult.Failed
        }
    }

    fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun finishLocal(ok: Boolean): RecorderSendResult {
        if (ok) {
            _status.value = local.status()
            syncTicker()
        }
        return if (ok) RecorderSendResult.Sent else RecorderSendResult.Failed
    }

    private fun canUseRemote(): Boolean {
        // Starting DreamRecorder's microphone FGS from this process is blocked on API 34+
        // ("app must be in the eligible state"). Record locally and import marks instead.
        return false
    }

    private fun remoteAction(command: RecorderCommand): String = when (command) {
        RecorderCommand.Start -> RECORDER_ACTION_START
        RecorderCommand.Pause -> RECORDER_ACTION_PAUSE
        RecorderCommand.Resume -> RECORDER_ACTION_RESUME
        RecorderCommand.Mark -> RECORDER_ACTION_MARK
        RecorderCommand.Save -> RECORDER_ACTION_STOP
        RecorderCommand.Open -> RECORDER_ACTION_SYNC
    }

    private fun syncTicker() {
        handler.removeCallbacks(tick)
        val current = _status.value
        if (current.state == RecorderUiState.Recording) {
            tickStartElapsed = current.elapsedMs
            tickStartRealtime = SystemClock.elapsedRealtime()
            handler.post(tick)
        }
    }

    fun sync() {
        if (canUseRemote()) {
            usingRemote = true
            dispatchRemote(RECORDER_ACTION_SYNC)
        } else if (!usingRemote) {
            _status.value = local.status()
            syncTicker()
        }
    }

    fun openRecorder(): Intent? {
        return appContext.packageManager.getLaunchIntentForPackage(RECORDER_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
    }

    private fun optimistic(command: RecorderCommand) {
        val current = _status.value
        _status.value = when (command) {
            RecorderCommand.Start -> {
                remoteMarkCount = 0
                RecorderWaveSession.reset()
                RecorderStatus(RecorderUiState.Recording, 0L)
            }
            RecorderCommand.Pause -> current.copy(state = RecorderUiState.Paused)
            RecorderCommand.Resume -> current.copy(state = RecorderUiState.Recording)
            RecorderCommand.Save -> {
                remoteMarkCount = 0
                RecorderWaveSession.reset()
                RecorderStatus(RecorderUiState.Idle, 0L)
            }
            RecorderCommand.Mark -> current.copy(marks = current.marks + current.elapsedMs)
            RecorderCommand.Open -> current
        }
        syncTicker()
    }

    private fun dispatchRemote(action: String): Boolean {
        val intent = recorderServiceIntent(action)
        return try {
            if (action == RECORDER_ACTION_START) {
                if (Build.VERSION.SDK_INT >= 26) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            } else {
                appContext.startService(intent)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
