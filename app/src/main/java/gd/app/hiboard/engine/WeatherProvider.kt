package gd.app.hiboard.engine

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import gd.app.hiboard.data.WeatherStore

class WeatherProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        context?.let(WeatherStore::get)
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val store = WeatherStore.get(context ?: return null)
        return when (method) {
            METHOD_GET -> bundleOf(store.current())
            METHOD_RESET -> bundleOf(store.reset())
            METHOD_SET -> bundleOf(store.set(resolveIncoming(store.current(), arg, extras)))
            else -> null
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val snapshot = WeatherStore.get(context ?: return MatrixCursor(COLUMNS)).current()
        return MatrixCursor(COLUMNS).apply {
            addRow(
                arrayOf<Any>(
                    snapshot.location,
                    snapshot.condition.json,
                    snapshot.temperatureC,
                    snapshot.toJson(),
                ),
            )
        }
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.item/vnd.$AUTHORITY.weather"

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        val store = WeatherStore.get(context ?: return null)
        store.set(fromValues(store.current(), values))
        return WeatherStore.CONTENT_URI
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        if (values == null) return 0
        val store = WeatherStore.get(context ?: return 0)
        store.set(fromValues(store.current(), values))
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        WeatherStore.get(context ?: return 0).reset()
        return 1
    }

    private fun resolveIncoming(base: WeatherSnapshot, arg: String?, extras: Bundle?): WeatherSnapshot {
        parseWeatherJson(arg.orEmpty(), base)?.let { return it }
        extras?.getString(EXTRA_JSON)?.let { json ->
            parseWeatherJson(json, base)?.let { return it }
        }
        return mergeWeatherExtras(
            base = base,
            location = extras?.getString(EXTRA_LOCATION),
            condition = extras?.getString(EXTRA_CONDITION),
            temperatureC = extras?.intOrNull(EXTRA_TEMPERATURE_C),
            daysJson = extras?.getString(EXTRA_DAYS),
        )
    }

    private fun fromValues(base: WeatherSnapshot, values: ContentValues): WeatherSnapshot {
        values.getAsString(EXTRA_JSON)?.let { json ->
            parseWeatherJson(json, base)?.let { return it }
        }
        return mergeWeatherExtras(
            base = base,
            location = values.getAsString(EXTRA_LOCATION),
            condition = values.getAsString(EXTRA_CONDITION),
            temperatureC = values.getAsInteger(EXTRA_TEMPERATURE_C),
            daysJson = values.getAsString(EXTRA_DAYS),
        )
    }

    private fun bundleOf(snapshot: WeatherSnapshot): Bundle {
        return Bundle().apply {
            putBoolean(EXTRA_OK, true)
            putString(EXTRA_LOCATION, snapshot.location)
            putString(EXTRA_CONDITION, snapshot.condition.json)
            putInt(EXTRA_TEMPERATURE_C, snapshot.temperatureC)
            putString(EXTRA_JSON, snapshot.toJson())
        }
    }

    private fun Bundle.intOrNull(key: String): Int? {
        if (!containsKey(key)) return null
        return getInt(key)
    }

    companion object {
        const val AUTHORITY = WeatherStore.AUTHORITY
        const val METHOD_GET = "get"
        const val METHOD_SET = "set"
        const val METHOD_RESET = "reset"
        const val EXTRA_OK = "ok"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_CONDITION = "condition"
        const val EXTRA_TEMPERATURE_C = "temperature_c"
        const val EXTRA_DAYS = "days"
        const val EXTRA_JSON = "json"
        private val COLUMNS = arrayOf(EXTRA_LOCATION, EXTRA_CONDITION, EXTRA_TEMPERATURE_C, EXTRA_JSON)
    }
}
