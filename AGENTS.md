# UV Australia — Agent Memory

## Project Overview
Android app (Kotlin + Jetpack Compose) that displays live UV index data from ARPANSA for Australian monitoring stations. `minSdk 29`, `targetSdk 35`, `compileSdk 35`.

## Build & Run
Open the project root in Android Studio. Gradle syncs automatically.
- No build commands to run from CLI (requires Android SDK / Studio).
- Package: `com.uvaustralia.app`
- **When verifying changes via CLI, consult `build.sh` in the root directory for the correct build commands.**

## Project Structure
```
app/src/main/java/com/uvaustralia/app/
  data/          — ARPANSA API services, XML/JSON parsers, repositories
  domain/        — Station list, UvReading, UvCurvePoint, ProtectionWindow logic
  ui/main/       — MainScreen, MainViewModel, UvGraph, UvIndexDisplay
  ui/settings/   — StationPickerSheet (bottom sheet)
  ui/theme/      — Color.kt, Theme.kt (light+dark), Typography.kt
  widget/        — UvWidget (Glance), UvWidgetWorker (WorkManager)
  prefs/         — UserPreferences (DataStore)
  MainActivity.kt, UvApplication.kt
app/src/main/res/
  drawable/      — ic_launcher_foreground.xml (placeholder sun), ic_launcher_background.xml
  mipmap-anydpi-v26/ — ic_launcher.xml, ic_launcher_round.xml (adaptive icon)
  xml/           — uv_widget_info.xml
  values/        — strings.xml, themes.xml
```

## Data Sources
- **Live readings (XML):** `https://uvdata.arpansa.gov.au/xml/uvvalues.xml`
  - Polled every 60 s via coroutine flow in `UvRepository`
  - Parsed by `ArpansaXmlParser` using Android's built-in XML DOM parser
- **Full-day curve (JSON):** `https://uvdata.arpansa.gov.au/api/uvlevel?site=<code>&date=<YYYYMMDD>`
  - Fetched once on load, refreshed hourly
  - Returns per-minute `forecast` + `measured` UV values
  - Parsed by `ArpansaCurveParser`
  - **Note:** If this endpoint returns 404, inspect the ARPANSA UV chart page in DevTools to find the actual API call URL and update `ArpansaService.getCurveData()`.

## Stations
Hardcoded in `domain/Station.kt` as two lists: `AUSTRALIAN_STATIONS` and `ELSEWHERE_STATIONS`, combined into `ALL_STATIONS`.
- **Australia (13):** Adelaide (adl), Alice Springs (ali), Brisbane (bri), Canberra (can), Darwin (dar), Emerald (emd), Gold Coast (gco), Kingston/Tas (kin), Melbourne (mel), Newcastle (new), Perth (per), Sydney (syd), Townsville (tow).
- **Elsewhere (4):** Casey (cas), Davis (dav), Mawson (maw), Macquarie Island (mcq) — Antarctic/sub-Antarctic stations from the ARPANSA feed.
- `Station.isAustralian` flag distinguishes the two groups (used by the picker UI for grouped headings).

## Key Domain Rules
- **Protection threshold:** UV ≥ 3.0 (constant `PROTECTION_THRESHOLD` in `ProtectionWindow.kt`)
- **Protection window:** derived from forecast curve; start rounded DOWN to 15 min, end rounded UP to 15 min
- **Window display:** hidden after the window end time passes for the day
- **Distance warning:** shown when device is > 30 km from matched station
- **Graph axes:** fixed 6 AM–8 PM (x), 0–16 UV (y) — do not make these dynamic

## Colour Scheme
Brand colours from the app icon:
- `#FFEA99` — light yellow (UvAmberLight) — body text in dark theme
- `#FFC62A` — mid amber (UvAmber) — primary, measured graph line
- `#FFD4FF` — pale magenta (UvMagenta) — accent, protection warning text

Dark theme is the default (outdoor readability). Both light and dark are supported.

## Dependencies (libs.versions.toml)
- Retrofit + ScalarsConverter for HTTP
- OkHttp for the client
- Jetpack Glance (1.1.0) for the home-screen widget
- WorkManager for widget background polling (15-min minimum interval)
- DataStore Preferences for user settings (station, auto-location toggle)
- play-services-location (FusedLocationProviderClient)
- Accompanist Permissions for runtime location permission

## Widget Notes
- `UvWidget` (Glance) + `UvWidgetReceiver` in `widget/`
- Background refresh via `UvWidgetWorker` (WorkManager periodic, 15-min minimum)
- Widget state keys: `WIDGET_KEY_UV_INDEX`, `WIDGET_KEY_STATION`, `WIDGET_KEY_STATUS` (defined in `UvWidget.kt`)
- Displays: station name, UV index, "⚠ Sun protection" if UV ≥ 3
- Tapping opens `MainActivity`

## Auto-location Behaviour
- On every app open (`onResume`): if auto-location is ON, re-run GPS lookup + nearest-station match
- If auto-location is OFF: use saved station from DataStore, skip GPS
- Manual station selection persists until next app open (if auto-location on) or indefinitely (if off)

## Icon
- Placeholder adaptive icon: radial gradient background (`#FFEA99`→`#FFC62A`) + white sun vector foreground
- Replace `res/drawable/ic_launcher_foreground.xml` with the reworked `uv-app-icon.svg` once converted to a flat vector (no blend modes)
- Background drawable is `ic_launcher_background.xml`

## ARPANSA Data Licence
- Data must be attributed: *"UV observations courtesy of ARPANSA"*
- Non-commercial use only without written permission
- A disclaimer about data accuracy should be accessible in the app (currently in the distance warning modal; consider adding an About screen)
- Notify ARPANSA at uv.index@arpansa.gov.au when the app is published
