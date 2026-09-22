package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class CalendarMonthTest {
    @Test
    fun july2024StartsOnMondayAndMarksTheFirst() {
        val cells = monthCells(2024, Calendar.JULY, 2024, Calendar.JULY, 1)
        assertEquals(35, cells.size)
        assertEquals(30, cells.first().day)
        assertEquals(false, cells.first().inMonth)
        assertEquals(1, cells[1].day)
        assertEquals(true, cells[1].inMonth)
        assertEquals(true, cells[1].today)
        assertEquals(31, cells[31].day)
        assertEquals(true, cells[31].inMonth)
        assertEquals(1, cells[32].day)
        assertEquals(false, cells[32].inMonth)
        assertEquals(1, cells.count { it.today })
    }

    @Test
    fun chineseTitleAndSundayFirstWeek() {
        val page = monthPage(2024, Calendar.JULY, 1, Locale.CHINA)
        assertEquals("7月1日", page.title)
        assertEquals(listOf("日", "一", "二", "三", "四", "五", "六"), page.weekdays)
        assertEquals(5, page.weeks.size)
        assertTrue(page.weeks[0][1].today)
    }

    @Test
    fun weatherClockDateUsesChineseWeekday() {
        assertEquals("12月25日 周三", weatherClockDate(2024, Calendar.DECEMBER, 25, Locale.CHINA))
    }
}
