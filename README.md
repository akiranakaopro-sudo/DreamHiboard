# Weather widget API

The Weather card is a local **2×4** tile (2 rows × 4 columns, ColorOS `TWO_PLUS_FOUR`). Hiboard never talks to the network for weather. You push a snapshot in, and the minus-one card renders it with Oppo-style backgrounds and icons.

## Default snapshot

Until you sync, the card shows this sample (same layout as ColorOS All-weather):

| Field | Default |
| --- | --- |
| Location | `Vientiane` |
| Condition | `rain` |
| Temperature | `26°C` |
| Today | Rain, 24° / 34° |
| Tomorrow | Rain, 24° / 34° |
| Day +2 | Cloudy, 25° / 35° |
| Day +3 | Cloudy, 25° / 35° |
| Day +4 | Rain, 25° / 34° |

Day titles are filled at bind time from the device clock (`Today`, `Tomorrow`, then `Wed` / `Thu` / …). Override them in JSON if you want fixed labels.

## Conditions

Use any of these strings (case-insensitive):

| JSON | Card label | Background / icon |
| --- | --- | --- |
| `sunny`, `clear` | Sunny | `bg_weather_sunny` |
| `cloudy`, `overcast` | Cloudy | `bg_weather_cloudy` |
| `rain`, `showers` | Rain | `bg_weather_rain` |
| `thunder`, `storm` | Thunder | `bg_weather_thunder` |
| `snow` | Snow | `bg_weather_snow` |
| `fog`, `mist` | Fog | `bg_weather_fog` |
| `night`, `clear_night` | Clear | `bg_weather_night` |

Unknown values fall back to **rain**.

## Content provider

- Authority: `gd.app.hiboard.weather`
- URI: `content://gd.app.hiboard.weather/current`
- Exported: yes
- No `INTERNET` permission on this app

### JSON document

```json
{
  "location": "Paris",
  "condition": "sunny",
  "temperature_c": 18,
  "days": [
    { "label": "Today", "condition": "sunny", "low_c": 12, "high_c": 21 },
    { "label": "Tomorrow", "condition": "cloudy", "low_c": 11, "high_c": 19 },
    { "condition": "rain", "low_c": 10, "high_c": 16 },
    { "condition": "rain", "low_c": 9, "high_c": 15 },
    { "condition": "cloudy", "low_c": 10, "high_c": 17 }
  ]
}
```

`days` is optional. Omit it (or send fewer than 5) and Hiboard keeps / pads the current 5-day row. `label` is optional per day.

### `call` methods

| Method | Arg / extras | Result extras |
| --- | --- | --- |
| `get` | — | `ok`, `location`, `condition`, `temperature_c`, `json` |
| `set` | JSON in `arg`, or extras / `json` | same as `get` for the stored snapshot |
| `reset` | — | restores the default Vientiane rain sample |

```bash
# Read
adb shell content call --uri content://gd.app.hiboard.weather --method get

# Full replace
adb shell content call --uri content://gd.app.hiboard.weather --method set --arg '{"location":"Paris","condition":"sunny","temperature_c":18,"days":[{"condition":"sunny","low_c":12,"high_c":21},{"condition":"cloudy","low_c":11,"high_c":19},{"condition":"rain","low_c":10,"high_c":16},{"condition":"rain","low_c":9,"high_c":15},{"condition":"cloudy","low_c":10,"high_c":17}]}'

# Patch current values
adb shell content call --uri content://gd.app.hiboard.weather --method set --extra location:s:Local --extra condition:s:cloudy --extra temperature_c:i:22

# Restore defaults
adb shell content call --uri content://gd.app.hiboard.weather --method reset
```

`query`, `insert` / `update` (with the same column names), and `delete` (reset) also work on `content://gd.app.hiboard.weather/current`.

### From an app

```kotlin
val client = context.contentResolver.acquireUnstableContentProviderClient("gd.app.hiboard.weather")
val extras = Bundle().apply {
    putString("json", json)
}
val result = client?.call("set", null, extras)
```

Or:

```kotlin
context.contentResolver.call(
    Uri.parse("content://gd.app.hiboard.weather/current"),
    "set",
    json,
    null,
)
```

The widget listens on `content://gd.app.hiboard.weather/current` and refreshes as soon as `set` / `reset` writes `files/weather.json`. No network lookup is performed.
