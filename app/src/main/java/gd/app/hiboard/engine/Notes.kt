package gd.app.hiboard.engine

import java.util.Calendar
import java.util.concurrent.TimeUnit

const val NOTE_PACKAGE = "gd.app.note"

data class NotesPreview(
    val id: Long = 0L,
    val title: String = "",
    val snippet: String = "",
    val updatedAt: Long = 0L,
)

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
