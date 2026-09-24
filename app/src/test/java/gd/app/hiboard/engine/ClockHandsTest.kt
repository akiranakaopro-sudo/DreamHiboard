package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockHandsTest {
    @Test
    fun noonPointsAtTwelve() {
        val hands = clockHands(12, 0, 0)
        assertEquals(0f, hands.hourDegrees)
        assertEquals(0f, hands.minuteDegrees)
        assertEquals(0f, hands.secondDegrees)
    }

    @Test
    fun threeOClockIsARightAngle() {
        assertEquals(90f, clockHands(15, 0, 0).hourDegrees)
    }

    @Test
    fun halfPastSixMovesTheHourHand() {
        val hands = clockHands(6, 30, 0)
        assertEquals(195f, hands.hourDegrees)
        assertEquals(180f, hands.minuteDegrees)
    }

    @Test
    fun secondsSweepTheSecondHand() {
        val hands = clockHands(12, 0, 30)
        assertEquals(180f, hands.secondDegrees)
        assertEquals(3f, hands.minuteDegrees)
    }
}
