package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class WeatherTest {
    @Test
    fun defaultSnapshotMatchesOppoSample() {
        val snapshot = WeatherSnapshot.DEFAULT
        assertEquals("Vientiane", snapshot.location)
        assertEquals(WeatherCondition.Rain, snapshot.condition)
        assertEquals(26, snapshot.temperatureC)
        assertEquals(5, snapshot.days.size)
        assertEquals(24, snapshot.days[0].lowC)
        assertEquals(34, snapshot.days[0].highC)
    }

    @Test
    fun forecastLabelsUseTodayTomorrowThenWeekdays() {
        val monday = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 21, 15, 31, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals(
            listOf("Today", "Tomorrow", "Wed", "Thu", "Fri"),
            forecastDayLabels(monday, 5),
        )
    }

    @Test
    fun resolvedFillsBlankDayLabels() {
        val monday = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 21, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val resolved = WeatherSnapshot.DEFAULT.resolved(monday)
        assertEquals("Today", resolved.days[0].label)
        assertEquals("Tomorrow", resolved.days[1].label)
        assertEquals("Wed", resolved.days[2].label)
    }

    @Test
    fun jsonRoundTripKeepsCustomSnapshot() {
        val original = WeatherSnapshot(
            location = "Paris",
            condition = WeatherCondition.Sunny,
            temperatureC = 18,
            days = listOf(
                WeatherDay("Today", WeatherCondition.Sunny, 12, 20),
                WeatherDay("Tomorrow", WeatherCondition.Cloudy, 11, 19),
            ),
        )
        val parsed = parseWeatherJson(original.toJson())
        assertEquals("Paris", parsed?.location)
        assertEquals(WeatherCondition.Sunny, parsed?.condition)
        assertEquals(18, parsed?.temperatureC)
        assertEquals(5, parsed?.days?.size)
        assertEquals("Today", parsed?.days?.first()?.label)
        assertEquals(WeatherCondition.Sunny, parsed?.days?.first()?.condition)
    }

    @Test
    fun parseAcceptsConditionAliases() {
        assertEquals(WeatherCondition.Thunder, WeatherCondition.from("thunderstorm"))
        assertEquals(WeatherCondition.Cloudy, WeatherCondition.from("Overcast"))
        assertEquals(WeatherCondition.Rain, WeatherCondition.from("unknown"))
    }

    @Test
    fun extrasMergeKeepsPreviousForecast() {
        val next = mergeWeatherExtras(
            base = WeatherSnapshot.DEFAULT,
            location = "Local",
            condition = "sunny",
            temperatureC = 22,
            daysJson = null,
        )
        assertEquals("Local", next.location)
        assertEquals(WeatherCondition.Sunny, next.condition)
        assertEquals(22, next.temperatureC)
        assertEquals(WeatherSnapshot.DEFAULT.days, next.days)
    }

    @Test
    fun invalidJsonIsIgnored() {
        assertNull(parseWeatherJson(""))
        assertNull(parseWeatherJson("not-json"))
    }

    @Test
    fun padWeatherDaysCapsAtFive() {
        val days = List(8) { WeatherDay("D$it", WeatherCondition.Sunny, it, it + 1) }
        assertEquals(5, padWeatherDays(days).size)
        assertEquals("D0", padWeatherDays(days).first().label)
    }
}
