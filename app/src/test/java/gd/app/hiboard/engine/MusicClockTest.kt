package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class MusicClockTest {
    @Test
    fun formatsIdleAndLongTracks() {
        assertEquals("00:00", musicClock(0L))
        assertEquals("00:00", musicClock(999L))
        assertEquals("01:30", musicClock(90_000L))
        assertEquals("1:02:03", musicClock(3_723_000L))
    }
}
