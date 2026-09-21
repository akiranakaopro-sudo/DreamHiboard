package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotesTest {
    @Test
    fun relativeTimeMatchesOppoAllNotes() {
        val nowCal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 21, 15, 31, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = nowCal.timeInMillis
        assertEquals("Just now", formatNotesWhen(now - TimeUnit.SECONDS.toMillis(20), now))
        assertEquals("Today", formatNotesWhen(now - TimeUnit.HOURS.toMillis(3), now))
        assertEquals(
            "Yesterday",
            formatNotesWhen(now - TimeUnit.DAYS.toMillis(1), now),
        )
        val lastWeek = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_MONTH, -5)
        }.timeInMillis
        assertEquals("Sep 16", formatNotesWhen(lastWeek, now))
        assertEquals("", formatNotesWhen(0L, now))
    }

    @Test
    fun emptyTitleAndBodyStayBlankForPlaceholders() {
        assertEquals("" to "", noteHeadlineAndBody("", ""))
        assertEquals("" to "", noteHeadlineAndBody("   ", "\n\n"))
    }

    @Test
    fun contentUsesEveryLineAfterTheTitle() {
        assertEquals(
            "Meeting" to "Bring slides\nRoom 4",
            noteHeadlineAndBody("", "Meeting\nBring slides\nRoom 4"),
        )
        assertEquals(
            "Pinned" to "Line one\nLine two",
            noteHeadlineAndBody("Pinned", "Line one\nLine two"),
        )
        assertEquals(
            "Pinned" to "Line two",
            noteHeadlineAndBody("Pinned", "Pinned\nLine two"),
        )
    }

    @Test
    fun displayNoteSkipsEmptyEditsAndDeletedGaps() {
        val emptyLatest = NotesPreview(id = 3, title = "", snippet = "", updatedAt = 30)
        val older = NotesPreview(id = 2, title = "Keep me", snippet = "Body", updatedAt = 20)
        val blank = NotesPreview(id = 1, title = "", snippet = "", updatedAt = 10)
        assertEquals(older, pickDisplayNote(listOf(emptyLatest, older, blank)))
        assertEquals(emptyLatest, pickDisplayNote(listOf(emptyLatest, blank)))
        assertEquals(NotesPreview(), pickDisplayNote(emptyList()))
    }
}
