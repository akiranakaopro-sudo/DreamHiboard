package gd.app.hiboard.data

import android.content.Context
import gd.app.hiboard.engine.ALL_NOTES_FOLDER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** DreamNote folder picked per notes card with "Edit"; cards without an entry show all notes. */
class NoteFolderStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _selections = MutableStateFlow(read())
    val selections: StateFlow<Map<String, String>> = _selections

    fun set(catalogId: String, folder: String) {
        val editor = prefs.edit()
        if (folder == ALL_NOTES_FOLDER) editor.remove(catalogId) else editor.putString(catalogId, folder)
        editor.apply()
        _selections.value = read()
    }

    private fun read(): Map<String, String> =
        prefs.all.mapNotNull { (key, value) -> (value as? String)?.takeIf { it.isNotBlank() }?.let { key to it } }.toMap()

    private companion object {
        const val PREFS = "note_folders"
    }
}
