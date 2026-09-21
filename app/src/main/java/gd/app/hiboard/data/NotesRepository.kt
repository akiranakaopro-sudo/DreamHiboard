package gd.app.hiboard.data

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import gd.app.hiboard.engine.NOTE_PACKAGE
import gd.app.hiboard.engine.NotesPreview
import gd.app.hiboard.engine.noteHeadlineAndBody
import gd.app.hiboard.engine.pickDisplayNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotesRepository(context: Context) {
    private val appContext = context.applicationContext
    private val _revisions = MutableStateFlow(0)
    val revisions: StateFlow<Int> = _revisions

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            _revisions.value = _revisions.value + 1
        }
    }

    init {
        val resolver = appContext.contentResolver
        (listOf(SEARCH_URI) + URIS).forEach { uri ->
            try {
                resolver.registerContentObserver(uri, true, observer)
            } catch (_: Exception) {
            }
        }
    }

    fun latest(): NotesPreview = pickDisplayNote(allNotes())

    private fun allNotes(): List<NotesPreview> {
        val byId = linkedMapOf<Long, NotesPreview>()
        (querySearch() + queryLatest()).forEach { note ->
            val key = if (note.id > 0L) note.id else note.updatedAt
            val existing = byId[key]
            byId[key] = when {
                existing == null -> note
                note.hasText && !existing.hasText -> note
                existing.hasText && !note.hasText -> existing
                note.updatedAt >= existing.updatedAt -> note
                else -> existing
            }
        }
        return byId.values.toList()
    }

    private fun querySearch(): List<NotesPreview> {
        val resolver = appContext.contentResolver
        return try {
            resolver.query(SEARCH_URI, null, null, null, null)?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        readNote(cursor, titleKeys = emptyArray(), contentKeys = arrayOf("content"))
                            ?.let(::add)
                    }
                }
            }.orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun queryLatest(): List<NotesPreview> {
        val resolver = appContext.contentResolver
        URIS.forEach { uri ->
            try {
                resolver.query(uri, null, null, null, "updated DESC")?.use { cursor ->
                    val notes = buildList {
                        while (cursor.moveToNext()) {
                            readNote(
                                cursor,
                                titleKeys = arrayOf("title", "name", "subject"),
                                contentKeys = arrayOf("snippet", "summary", "content", "body", "text"),
                            )?.let(::add)
                        }
                    }
                    if (notes.isNotEmpty()) return notes
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
        return emptyList()
    }

    private fun readNote(
        cursor: Cursor,
        titleKeys: Array<String>,
        contentKeys: Array<String>,
    ): NotesPreview? {
        if (isGone(cursor)) return null
        val id = long(cursor, "_id", "id", "note_id", "guid")
        val title = if (titleKeys.isEmpty()) "" else string(cursor, *titleKeys)
        val content = string(cursor, *contentKeys)
        val updated = long(cursor, "updated", "updated_at", "modified", "time", "date")
        if (id <= 0L && title.isBlank() && content.isBlank()) return null
        val (headline, body) = noteHeadlineAndBody(title, content)
        return NotesPreview(id, headline, body, updated)
    }

    private fun isGone(cursor: Cursor): Boolean {
        val keys = arrayOf("deleted", "is_deleted", "trash", "in_trash", "is_trash")
        keys.forEach { key ->
            val index = cursor.getColumnIndex(key)
            if (index < 0) return@forEach
            when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_INTEGER -> if (cursor.getInt(index) != 0) return true
                Cursor.FIELD_TYPE_STRING -> {
                    val value = cursor.getString(index).orEmpty().trim()
                    if (value == "1" || value.equals("true", true) || value.equals("yes", true)) {
                        return true
                    }
                }
                else -> Unit
            }
        }
        return false
    }

    private fun string(cursor: Cursor, vararg keys: String): String {
        keys.forEach { key ->
            val index = cursor.getColumnIndex(key)
            if (index >= 0) return cursor.getString(index).orEmpty().trim()
        }
        return ""
    }

    private fun long(cursor: Cursor, vararg keys: String): Long {
        keys.forEach { key ->
            val index = cursor.getColumnIndex(key)
            if (index < 0) return@forEach
            return when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                Cursor.FIELD_TYPE_STRING -> cursor.getString(index)?.toLongOrNull() ?: 0L
                else -> cursor.getLong(index)
            }
        }
        return 0L
    }

    private companion object {
        val SEARCH_URI: Uri = Uri.parse("content://$NOTE_PACKAGE.search/Search")
        val URIS = listOf(
            Uri.parse("content://$NOTE_PACKAGE.provider/notes"),
            Uri.parse("content://$NOTE_PACKAGE.notes/notes"),
            Uri.parse("content://$NOTE_PACKAGE/notes"),
        )
    }
}
