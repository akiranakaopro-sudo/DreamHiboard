package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryEstimateTest {
    @Test
    fun chargeCounterAcceptsMicroAmpHoursAndMilliAmpHours() {
        assertEquals(2_500_000L, asMicroAmpHours(2_500_000L, 50))
        assertEquals(2_500_000L, asMicroAmpHours(2_500L, 50))
        assertNull(asMicroAmpHours(12L, 50))
    }

    @Test
    fun currentAcceptsMicroAmpsAndMilliAmps() {
        assertEquals(400_000L, asMicroAmps(-400_000L))
        assertEquals(400_000L, asMicroAmps(400L))
        assertNull(asMicroAmps(3L))
    }

    @Test
    fun countersEstimateUseAndTimeToFull() {
        val use = batteryMillisFromCounters(2_500_000L, 400_000L, 50, toFull = false)
        assertEquals(6 * 3_600_000L + 15 * 60_000L, use)
        val full = batteryMillisFromCounters(2_500_000L, 2_000_000L, 50, toFull = true)
        assertEquals(75 * 60_000L, full)
    }

    @Test
    fun rejectsImpossibleCounterTimes() {
        assertNull(batteryMillisFromCounters(2_500_000L, 8_000_000L, 80, toFull = false))
    }

    @Test
    fun powerSupplyNodePrefersSecondsWhenThatIsPlausible() {
        val ms = parsePowerSupplyTimeMs("5400", levelPercent = 40, toFull = true)
        assertEquals(5_400_000L, ms)
    }

    @Test
    fun powerSupplyNodeAcceptsMinutesWhenSecondsAreTooShort() {
        val ms = parsePowerSupplyTimeMs("90", levelPercent = 40, toFull = true)
        assertEquals(90 * 60_000L, ms)
    }

    @Test
    fun rateUsesOnlyTheLatestSamePlugStretch() {
        val start = 1_700_000_000_000L
        val points = listOf(
            BatteryRatePoint(start, 90, plugged = false),
            BatteryRatePoint(start + 30 * 60_000L, 80, plugged = false),
            BatteryRatePoint(start + 40 * 60_000L, 70, plugged = true),
            BatteryRatePoint(start + 70 * 60_000L, 85, plugged = true),
            BatteryRatePoint(start + 80 * 60_000L, 84, plugged = false),
            BatteryRatePoint(start + 140 * 60_000L, 78, plugged = false),
        )
        val now = start + 140 * 60_000L
        val use = estimateMsFromRate(points, levelPercent = 78, plugged = false, now = now)
        assertEquals(78.0 / 6.0 * 60 * 60_000L, use!!.toDouble(), 1.0)
        val full = estimateMsFromRate(points, levelPercent = 85, plugged = true, now = start + 70 * 60_000L)
        assertEquals(15.0 / 15.0 * 30 * 60_000L, full!!.toDouble(), 1.0)
    }

    @Test
    fun blendTrustsHistoryWhenSensorDisagrees() {
        val sensor = 2 * 3_600_000L
        val history = 8 * 3_600_000L
        assertEquals(history, blendBatteryEstimates(sensor, history))
        val close = blendBatteryEstimates(5 * 3_600_000L, 6 * 3_600_000L)!!
        assertTrue(close in 5 * 3_600_000L..6 * 3_600_000L)
    }
}
