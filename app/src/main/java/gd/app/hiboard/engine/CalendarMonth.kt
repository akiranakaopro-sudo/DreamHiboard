package gd.app.hiboard.engine

import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarCell(
    val day: Int,
    val inMonth: Boolean,
    val today: Boolean,
)

data class MonthPage(
    val title: String,
    val weekdays: List<String>,
    val weeks: List<List<CalendarCell>>,
)

fun monthPage(
    year: Int,
    month: Int,
    day: Int,
    locale: Locale = Locale.getDefault(),
): MonthPage {
    val cells = monthCells(year, month, year, month, day)
    return MonthPage(
        title = calendarTitle(year, month, day, locale),
        weekdays = weekdayLabels(locale),
        weeks = cells.chunked(7),
    )
}

fun monthPageToday(locale: Locale = Locale.getDefault(), now: Calendar = Calendar.getInstance(locale)): MonthPage {
    return monthPage(
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        locale,
    )
}

/** Sunday-first month grid. Days outside the month stay in the row so the week lines up. */
fun monthCells(
    year: Int,
    month: Int,
    todayYear: Int,
    todayMonth: Int,
    todayDay: Int,
): List<CalendarCell> {
    val cursor = Calendar.getInstance(Locale.US)
    cursor.clear()
    cursor.set(year, month, 1)
    val lead = cursor.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val daysInMonth = cursor.getActualMaximum(Calendar.DAY_OF_MONTH)
    cursor.add(Calendar.DAY_OF_MONTH, -1)
    val previousLast = cursor.get(Calendar.DAY_OF_MONTH)
    val cells = ArrayList<CalendarCell>(42)
    for (offset in lead downTo 1) {
        cells += CalendarCell(previousLast - offset + 1, inMonth = false, today = false)
    }
    for (day in 1..daysInMonth) {
        cells += CalendarCell(
            day = day,
            inMonth = true,
            today = year == todayYear && month == todayMonth && day == todayDay,
        )
    }
    var next = 1
    while (cells.size % 7 != 0) {
        cells += CalendarCell(next++, inMonth = false, today = false)
    }
    return cells
}

fun calendarTitle(year: Int, month: Int, day: Int, locale: Locale): String {
    if (locale.language == "zh") return "${month + 1}月${day}日"
    val cursor = Calendar.getInstance(locale)
    cursor.clear()
    cursor.set(year, month, day)
    return SimpleDateFormat("MMM d", locale).format(cursor.time)
}

fun weatherClockDate(year: Int, month: Int, day: Int, locale: Locale = Locale.getDefault()): String {
    val cursor = Calendar.getInstance(locale)
    cursor.clear()
    cursor.set(year, month, day)
    if (locale.language == "zh") {
        val weeks = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val week = weeks[cursor.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY]
        return "${month + 1}月${day}日 $week"
    }
    return SimpleDateFormat("MMM d EEE", locale).format(cursor.time)
}

fun weekdayLabels(locale: Locale): List<String> {
    if (locale.language == "zh") return listOf("日", "一", "二", "三", "四", "五", "六")
    val symbols = DateFormatSymbols.getInstance(locale).shortWeekdays
    return (Calendar.SUNDAY..Calendar.SATURDAY).map { index ->
        symbols[index].trim().take(1).uppercase(locale)
    }
}
