package gd.app.hiboard.data

import android.content.Context
import android.net.Uri
import gd.app.hiboard.engine.WeatherSnapshot
import gd.app.hiboard.engine.parseWeatherJson
import gd.app.hiboard.engine.toJson
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeatherStore private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)
    private val lock = Any()
    private val _snapshot = MutableStateFlow(readLocked() ?: WeatherSnapshot.DEFAULT)
    val snapshot: StateFlow<WeatherSnapshot> = _snapshot

    fun current(): WeatherSnapshot = synchronized(lock) { _snapshot.value }

    fun set(next: WeatherSnapshot): WeatherSnapshot {
        val stored = synchronized(lock) {
            file.writeText(next.toJson())
            _snapshot.value = next
            next
        }
        appContext.contentResolver.notifyChange(CONTENT_URI, null)
        return stored
    }

    fun reset(): WeatherSnapshot = set(WeatherSnapshot.DEFAULT)

    private fun readLocked(): WeatherSnapshot? {
        if (!file.exists()) return null
        return try {
            parseWeatherJson(file.readText())
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        const val AUTHORITY = "gd.app.hiboard.weather"
        const val FILE_NAME = "weather.json"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/current")

        @Volatile
        private var instance: WeatherStore? = null

        fun get(context: Context): WeatherStore {
            return instance ?: synchronized(this) {
                instance ?: WeatherStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
