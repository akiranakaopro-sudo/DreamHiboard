package gd.app.hiboard.engine

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.MediaStore
import gd.app.hiboard.R
import gd.app.hiboard.model.RecorderUiState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalRecorder(context: Context) {
    private val appContext = context.applicationContext
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var state: RecorderUiState = RecorderUiState.Idle
    private var startedAtRealtime = 0L
    private var accumulatedMs = 0L
    private val marks = mutableListOf<RecorderMark>()
    private var lastAmplitude = 0f

    fun status(): RecorderStatus = RecorderStatus(
        state = state,
        elapsedMs = elapsedMs(),
        marks = marks.map { it.timeMs },
        amplitude = lastAmplitude,
    )

    fun amplitude01(): Float {
        if (state != RecorderUiState.Recording) return lastAmplitude
        lastAmplitude = try {
            ((recorder?.maxAmplitude ?: 0) / 32768f).coerceIn(0f, 1f)
        } catch (_: Exception) {
            lastAmplitude
        }
        return lastAmplitude
    }

    fun start(): Boolean {
        if (state == RecorderUiState.Recording || state == RecorderUiState.Paused) return true
        val file = File(appContext.cacheDir, "hiboard-record-${System.currentTimeMillis()}.mp3")
        val mr = createRecorder()
        return try {
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            // Same as DreamRecorder: AAC in MPEG-4, published as .mp3.
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioSamplingRate(44_100)
            mr.setAudioEncodingBitRate(128_000)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            outputFile = file
            marks.clear()
            lastAmplitude = 0f
            accumulatedMs = 0L
            startedAtRealtime = SystemClock.elapsedRealtime()
            state = RecorderUiState.Recording
            true
        } catch (_: Exception) {
            runCatching { mr.reset() }
            runCatching { mr.release() }
            file.delete()
            recorder = null
            outputFile = null
            state = RecorderUiState.Idle
            false
        }
    }

    fun pause(): Boolean {
        val mr = recorder ?: return false
        if (state != RecorderUiState.Recording) return state == RecorderUiState.Paused
        return try {
            mr.pause()
            accumulatedMs = elapsedMs()
            state = RecorderUiState.Paused
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resume(): Boolean {
        val mr = recorder ?: return false
        if (state != RecorderUiState.Paused) return state == RecorderUiState.Recording
        return try {
            mr.resume()
            startedAtRealtime = SystemClock.elapsedRealtime()
            state = RecorderUiState.Recording
            true
        } catch (_: Exception) {
            false
        }
    }

    internal fun mark(): RecorderMark? {
        if (state == RecorderUiState.Idle) return null
        val mark = RecorderMark(
            text = appContext.getString(R.string.recorder_flag_new, marks.size + 1),
            timeMs = elapsedMs(),
        )
        marks += mark
        return mark
    }

    fun save(): Boolean {
        if (state == RecorderUiState.Idle) return true
        val file = outputFile
        val mr = recorder
        val duration = elapsedMs()
        val savedMarks = marks.toList()
        state = RecorderUiState.Idle
        startedAtRealtime = 0L
        accumulatedMs = 0L
        lastAmplitude = 0f
        recorder = null
        outputFile = null
        marks.clear()
        try {
            mr?.stop()
        } catch (_: Exception) {
        }
        runCatching { mr?.reset() }
        runCatching { mr?.release() }
        if (file == null || !file.exists() || duration < 400L) {
            file?.delete()
            return true
        }
        val published = publish(file)
        file.delete()
        if (published != null) persistMarks(published, savedMarks)
        return true
    }

    private fun elapsedMs(): Long {
        return when (state) {
            RecorderUiState.Recording ->
                accumulatedMs + (SystemClock.elapsedRealtime() - startedAtRealtime)
            RecorderUiState.Paused -> accumulatedMs
            RecorderUiState.Idle -> 0L
        }
    }

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= 31) {
            MediaRecorder(appContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private fun persistMarks(audio: File, savedMarks: List<RecorderMark>) {
        if (savedMarks.isEmpty()) return
        val arr = JSONArray()
        savedMarks.forEach { mark ->
            arr.put(
                JSONObject()
                    .put("id", UUID.randomUUID().toString())
                    .put("timeMs", mark.timeMs)
                    .put("text", mark.text)
                    .put("picturePath", ""),
            )
        }
        val payload = arr.toString()
        if (!importViaRecorder(audio, payload)) {
            writeRecorderOwnedSidecar(audio, payload)
        }
    }

    private fun importViaRecorder(audio: File, payload: String): Boolean {
        return try {
            val extras = android.os.Bundle().apply {
                putString("path", audio.absolutePath)
                putString("json", payload)
            }
            val result = appContext.contentResolver.call(
                android.net.Uri.parse("content://gd.app.soundrecorder.marks"),
                "save",
                null,
                extras,
            )
            result?.getBoolean("ok") == true
        } catch (_: Exception) {
            false
        }
    }

    private fun writeRecorderOwnedSidecar(audio: File, payload: String) {
        val keys = listOf(
            audio.absolutePath,
            audio.absolutePath.replace("/storage/emulated/0", "/sdcard"),
            audio.absolutePath.replace("/sdcard", "/storage/emulated/0"),
        ).distinct().map { Integer.toHexString(it.hashCode()) }.distinct()
        val dir = File(
            Environment.getExternalStorageDirectory(),
            "Android/data/gd.app.soundrecorder/files/marks",
        )
        try {
            dir.mkdirs()
            for (key in keys) {
                File(dir, "$key.marks.json").writeText(payload)
            }
        } catch (_: Exception) {
        }
    }

    private fun publish(file: File): File? {
        val name = "Recording ${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp3"
        publishViaRecorder(file, name)?.let { return it }
        val relativePath = "${Environment.DIRECTORY_MUSIC}/Recordings/Standard Recordings"
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Recordings/Standard Recordings",
        )
        if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, name)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "$relativePath/")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            val uri = appContext.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                values,
            ) ?: return null
            appContext.contentResolver.openOutputStream(uri)?.use { out ->
                FileInputStream(file).use { input -> input.copyTo(out) }
            }
            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            appContext.contentResolver.update(uri, values, null, null)
            return queryPublishedFile(uri) ?: File(publicDir, name)
        }
        publicDir.mkdirs()
        val dest = File(publicDir, name)
        file.copyTo(dest, overwrite = true)
        return dest
    }

    /**
     * Copy into Sound Recorder so that app owns the file. MediaStore inserts
     * from this UID cannot be trashed by Sound Recorder on Android 10+ FUSE.
     */
    private fun publishViaRecorder(file: File, name: String): File? {
        val pfd = try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (_: Exception) {
            return null
        }
        return try {
            val extras = Bundle().apply {
                putString("name", name)
                putParcelable("fd", pfd)
            }
            val result = appContext.contentResolver.call(
                Uri.parse("content://gd.app.soundrecorder.marks"),
                "publish",
                null,
                extras,
            )
            val dest = result?.getString("path")
            if (result?.getBoolean("ok") == true && !dest.isNullOrBlank()) File(dest) else null
        } catch (_: Exception) {
            null
        } finally {
            runCatching { pfd.close() }
        }
    }

    private fun queryPublishedFile(uri: Uri): File? {
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH,
        )
        return try {
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return null
                val dataIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (dataIdx >= 0) {
                    val data = cursor.getString(dataIdx)
                    if (!data.isNullOrBlank()) return File(data)
                }
                val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val relIdx = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                val name = if (nameIdx >= 0) cursor.getString(nameIdx) else return null
                val rel = if (relIdx >= 0) {
                    cursor.getString(relIdx) ?: "Music/Recordings/Standard Recordings/"
                } else {
                    "Music/Recordings/Standard Recordings/"
                }
                File(File(Environment.getExternalStorageDirectory(), rel.trim('/')), name)
            }
        } catch (_: Exception) {
            null
        }
    }
}
