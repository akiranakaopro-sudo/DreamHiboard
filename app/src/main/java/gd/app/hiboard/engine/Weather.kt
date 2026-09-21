package gd.app.hiboard.engine

import java.util.Calendar

enum class WeatherCondition {
    Sunny,
    Cloudy,
    Rain,
    Thunder,
    Snow,
    Fog,
    Night,
    ;

    val json: String
        get() = name.lowercase()

    val displayName: String
        get() = when (this) {
            Sunny -> "Sunny"
            Cloudy -> "Cloudy"
            Rain -> "Rain"
            Thunder -> "Thunder"
            Snow -> "Snow"
            Fog -> "Fog"
            Night -> "Clear"
        }

    companion object {
        fun from(raw: String?): WeatherCondition {
            val key = raw.orEmpty().trim().lowercase().replace(' ', '_')
            return when (key) {
                "sun", "sunny", "clear", "fine" -> Sunny
                "cloud", "clouds", "cloudy", "overcast" -> Cloudy
                "rain", "rainy", "shower", "showers", "drizzle" -> Rain
                "thunder", "storm", "tstorm", "thunderstorm" -> Thunder
                "snow", "snowy", "sleet" -> Snow
                "fog", "foggy", "mist", "haze" -> Fog
                "night", "clear_night", "clearnight" -> Night
                else -> Rain
            }
        }
    }
}

data class WeatherDay(
    val label: String = "",
    val condition: WeatherCondition = WeatherCondition.Sunny,
    val lowC: Int = 0,
    val highC: Int = 0,
)

data class WeatherSnapshot(
    val location: String,
    val condition: WeatherCondition,
    val temperatureC: Int,
    val days: List<WeatherDay>,
) {
    companion object {
        val DEFAULT = WeatherSnapshot(
            location = "Vientiane",
            condition = WeatherCondition.Sunny,
            temperatureC = 26,
            days = listOf(
                WeatherDay(condition = WeatherCondition.Sunny, lowC = 24, highC = 34),
                WeatherDay(condition = WeatherCondition.Cloudy, lowC = 24, highC = 34),
                WeatherDay(condition = WeatherCondition.Cloudy, lowC = 25, highC = 35),
                WeatherDay(condition = WeatherCondition.Cloudy, lowC = 25, highC = 35),
                WeatherDay(condition = WeatherCondition.Cloudy, lowC = 25, highC = 34),
            ),
        )
    }
}

fun forecastDayLabels(now: Calendar = Calendar.getInstance(), count: Int = 5): List<String> {
    val names = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return (0 until count).map { offset ->
        when (offset) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> {
                val day = now.clone() as Calendar
                day.add(Calendar.DAY_OF_YEAR, offset)
                names[day.get(Calendar.DAY_OF_WEEK) - 1]
            }
        }
    }
}

fun padWeatherDays(days: List<WeatherDay>, fallback: List<WeatherDay> = WeatherSnapshot.DEFAULT.days): List<WeatherDay> {
    val source = days.ifEmpty { fallback }
    if (source.size >= 5) return source.take(5)
    val last = source.last()
    return source + List(5 - source.size) { last.copy(label = "") }
}

fun WeatherSnapshot.resolved(now: Calendar = Calendar.getInstance()): WeatherSnapshot {
    val padded = padWeatherDays(days)
    val auto = forecastDayLabels(now, padded.size)
    return copy(
        location = location.ifBlank { WeatherSnapshot.DEFAULT.location },
        days = padded.mapIndexed { index, day ->
            day.copy(label = day.label.ifBlank { auto.getOrElse(index) { "" } })
        },
    )
}

fun WeatherSnapshot.toJson(): String {
    val daysJson = days.joinToString(",") { day ->
        buildString {
            append("{")
            if (day.label.isNotBlank()) append("\"label\":\"${escapeJson(day.label)}\",")
            append("\"condition\":\"${day.condition.json}\",")
            append("\"low_c\":${day.lowC},")
            append("\"high_c\":${day.highC}")
            append("}")
        }
    }
    return "{" +
        "\"location\":\"${escapeJson(location)}\"," +
        "\"condition\":\"${condition.json}\"," +
        "\"temperature_c\":$temperatureC," +
        "\"days\":[$daysJson]" +
        "}"
}

fun parseWeatherJson(raw: String, base: WeatherSnapshot = WeatherSnapshot.DEFAULT): WeatherSnapshot? {
    val json = raw.trim()
    if (json.isEmpty()) return null
    if (!(json.startsWith("{") && json.endsWith("}"))) return null
    val location = jsonString(json, "location") ?: base.location
    val condition = WeatherCondition.from(jsonString(json, "condition") ?: base.condition.json)
    val temperatureC = jsonInt(json, "temperature_c") ?: jsonInt(json, "temp_c") ?: base.temperatureC
    val days = parseWeatherDays(json).ifEmpty { base.days }
    return WeatherSnapshot(
        location = location.ifBlank { base.location },
        condition = condition,
        temperatureC = temperatureC,
        days = padWeatherDays(days, base.days),
    )
}

fun mergeWeatherExtras(
    base: WeatherSnapshot,
    location: String?,
    condition: String?,
    temperatureC: Int?,
    daysJson: String?,
): WeatherSnapshot {
    val days = daysJson?.let { payload ->
        val wrapped = if (payload.trim().startsWith("[")) {
            "{\"days\":$payload}"
        } else {
            payload
        }
        parseWeatherDays(wrapped)
    }.orEmpty()
    return WeatherSnapshot(
        location = location?.trim()?.ifBlank { base.location } ?: base.location,
        condition = if (condition.isNullOrBlank()) base.condition else WeatherCondition.from(condition),
        temperatureC = temperatureC ?: base.temperatureC,
        days = padWeatherDays(days.ifEmpty { base.days }, base.days),
    )
}

internal fun escapeJson(value: String): String = buildString(value.length) {
    value.forEach { ch ->
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
}

internal fun unescapeJson(value: String): String = buildString(value.length) {
    var index = 0
    while (index < value.length) {
        val ch = value[index]
        if (ch == '\\' && index + 1 < value.length) {
            when (val next = value[index + 1]) {
                '\\', '"' -> append(next)
                'n' -> append('\n')
                'r' -> append('\r')
                't' -> append('\t')
                else -> append(next)
            }
            index += 2
        } else {
            append(ch)
            index += 1
        }
    }
}

private fun jsonString(source: String, key: String): String? {
    val match = Regex(""""$key"\s*:\s*"((?:\\.|[^"\\])*)"""").find(source) ?: return null
    return unescapeJson(match.groupValues[1])
}

private fun jsonInt(source: String, key: String): Int? {
    return Regex(""""$key"\s*:\s*(-?\d+)""").find(source)?.groupValues?.get(1)?.toIntOrNull()
}

private fun parseWeatherDays(source: String): List<WeatherDay> {
    val header = Regex(""""days"\s*:\s*\[""").find(source) ?: return emptyList()
    val body = extractJsonArray(source, header.range.last)
    return objectSlices(body).map { slice ->
        WeatherDay(
            label = jsonString(slice, "label").orEmpty(),
            condition = WeatherCondition.from(jsonString(slice, "condition")),
            lowC = jsonInt(slice, "low_c") ?: jsonInt(slice, "min_c") ?: 0,
            highC = jsonInt(slice, "high_c") ?: jsonInt(slice, "max_c") ?: 0,
        )
    }
}

private fun extractJsonArray(source: String, openIndex: Int): String {
    var depth = 1
    var index = openIndex + 1
    while (index < source.length && depth > 0) {
        when (source[index]) {
            '[' -> depth += 1
            ']' -> depth -= 1
        }
        index += 1
    }
    return source.substring(openIndex + 1, (index - 1).coerceAtLeast(openIndex + 1))
}

private fun objectSlices(source: String): List<String> {
    val slices = mutableListOf<String>()
    var depth = 0
    var start = -1
    source.forEachIndexed { index, ch ->
        if (ch == '{') {
            if (depth == 0) start = index
            depth += 1
        } else if (ch == '}') {
            depth -= 1
            if (depth == 0 && start >= 0) {
                slices += source.substring(start, index + 1)
                start = -1
            }
        }
    }
    return slices
}
