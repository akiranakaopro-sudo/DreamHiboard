package gd.app.hiboard.engine

data class ClockHands(
    val hourDegrees: Float,
    val minuteDegrees: Float,
    val secondDegrees: Float,
)

/** Degrees clockwise from 12 o'clock. */
fun clockHands(hour: Int, minute: Int, second: Int): ClockHands {
    val secondDegrees = second.coerceIn(0, 59) * 6f
    val minuteDegrees = minute.coerceIn(0, 59) * 6f + secondDegrees / 60f
    val hourDegrees = (hour % 12) * 30f + minuteDegrees / 12f
    return ClockHands(hourDegrees, minuteDegrees, secondDegrees)
}
