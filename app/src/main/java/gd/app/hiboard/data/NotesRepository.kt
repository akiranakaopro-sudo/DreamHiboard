package gd.app.hiboard.data

import android.content.Context
import android.net.Uri
import gd.app.hiboard.engine.NOTE_PACKAGE
import gd.app.hiboard.engine.NotesPreview

class NotesRepository(context: Context) {
    private val appContext = context.applicationContext

    fun latest(): NotesPreview {
        querySearch()?.let { return it }
        queryLatest()?.let { return it }
        return NotesPreview()
    }

    private fun querySearch(): NotesPreview? {
        val resolver = appContext.contentResolver
        return try {
            resolver.query(SEARCH_URI, null, null, null, null)?.use { cursor ->
                var best: NotesPreview? = null
                while (cursor.moveToNext()) {
                    val id = long(cursor, "_id", "guid")
                    val content = string(cursor, "content")
                    val updated = long(cursor, "updated", "updated_at")
                    if (id <= 0L && content.isBlank()) continue
                    val lines = content.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
                    val candidate = NotesPreview(
                        id = id,
                        title = lines.firstOrNull().orEmpty(),
                        snippet = lines.drop(1).firstOrNull().orEmpty(),
                        updatedAt = updated,
                    )
                    if (best == null || candidate.updatedAt >= best.updatedAt) {
                        best = candidate
                    }
                }
                best
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun queryLatest(): NotesPreview? {
        val resolver = appContext.contentResolver
        URIS.forEach { uri ->
            try {
                resolver.query(uri, null, null, null, "updated DESC")?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use
                    val id = long(cursor, "_id", "id", "note_id", "guid")
                    val title = string(cursor, "title", "name", "subject")
                    val snippet = string(cursor, "snippet", "summary", "content", "body", "text")
                    val updated = long(cursor, "updated", "updated_at", "modified", "time", "date")
                    if (id > 0L || title.isNotBlank() || snippet.isNotBlank()) {
                        val lines = snippet.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
                        val headline = title.ifBlank { lines.firstOrNull().orEmpty() }
                        val rest = if (title.isBlank()) lines.drop(1).firstOrNull().orEmpty() else lines.firstOrNull().orEmpty()
                        return NotesPreview(id, headline, rest, updated)
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun string(cursor: android.database.Cursor, vararg keys: String): String {
        keys.forEach { key ->
            val index = cursor.getColumnIndex(key)
            if (index >= 0) return cursor.getString(index).orEmpty().trim()
        }
        return ""
    }

    private fun long(cursor: android.database.Cursor, vararg keys: String): Long {
        keys.forEach { key ->
            val index = cursor.getColumnIndex(key)
            if (index < 0) return@forEach
            return when (cursor.getType(index)) {
                android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                android.database.Cursor.FIELD_TYPE_STRING -> cursor.getString(index)?.toLongOrNull() ?: 0L
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
