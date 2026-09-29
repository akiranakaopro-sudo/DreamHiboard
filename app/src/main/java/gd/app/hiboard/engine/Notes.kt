package gd.app.hiboard.engine

import gd.app.hiboard.model.NoteFolder
import gd.app.hiboard.model.NoteItem
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val NOTE_PACKAGE = "gd.app.note"

data class NotesPreview(
    val id: Long = 0L,
    val title: String = "",
    val snippet: String = "",
    val updatedAt: Long = 0L,
    val folder: String = "",
) {
    val hasText: Boolean
        get() = title.isNotBlank() || snippet.isNotBlank()
}

/** Folder key meaning every note; DreamNote folder keys are never blank. */
const val ALL_NOTES_FOLDER = ""

fun notesInFolder(notes: List<NoteItem>, folder: String): List<NoteItem> =
    if (folder == ALL_NOTES_FOLDER) notes else notes.filter { it.folder.equals(folder, ignoreCase = true) }

fun noteFolderLabel(folders: List<NoteFolder>, folder: String, allNotes: String): String {
    if (folder == ALL_NOTES_FOLDER) return allNotes
    return folders.firstOrNull { it.key.equals(folder, ignoreCase = true) }?.label ?: folder
}

fun pickDisplayNote(notes: List<NotesPreview>): NotesPreview {
    val ordered = notes.sortedByDescending { it.updatedAt }
    return ordered.firstOrNull { it.hasText } ?: ordered.firstOrNull() ?: NotesPreview()
}

fun recentDisplayNotes(notes: List<NotesPreview>, limit: Int): List<NotesPreview> =
    notes.filter { it.hasText }.sortedByDescending { it.updatedAt }.take(limit)

fun noteHeadlineAndBody(title: String, content: String): Pair<String, String> {
    val lines = content.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
    val headline = title.trim().ifBlank { lines.firstOrNull().orEmpty() }
    val body = when {
        title.trim().isNotBlank() -> {
            val rest = if (lines.firstOrNull() == headline) lines.drop(1) else lines
            rest.joinToString("\n")
        }
        lines.size > 1 -> lines.drop(1).joinToString("\n")
        else -> ""
    }
    return headline to body
}

fun formatNotesWhen(updatedAt: Long, now: Long = System.currentTimeMillis()): String {
    if (updatedAt <= 0L) return ""
    val delta = (now - updatedAt).coerceAtLeast(0L)
    if (delta < TimeUnit.MINUTES.toMillis(1)) return "Just now"
    val nowCal = Calendar.getInstance().apply { timeInMillis = now }
    val thenCal = Calendar.getInstance().apply { timeInMillis = updatedAt }
    val nowDay = nowCal.get(Calendar.YEAR) * 1000 + nowCal.get(Calendar.DAY_OF_YEAR)
    val thenDay = thenCal.get(Calendar.YEAR) * 1000 + thenCal.get(Calendar.DAY_OF_YEAR)
    val dayDiff = nowDay - thenDay
    return when (dayDiff) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> {
            val months = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
            )
            "${months[thenCal.get(Calendar.MONTH)]} ${thenCal.get(Calendar.DAY_OF_MONTH)}"
        }
    }
}
