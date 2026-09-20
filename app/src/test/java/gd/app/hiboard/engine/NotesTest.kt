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
}
