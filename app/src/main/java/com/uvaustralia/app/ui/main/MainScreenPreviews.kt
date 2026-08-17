package com.uvaustralia.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.uvaustralia.app.domain.ProtectionWindow
import com.uvaustralia.app.domain.Station
import com.uvaustralia.app.ui.theme.UvAustraliaTheme

@Preview(name = "1. Squat Portrait (Foldable)", widthDp = 600, heightDp = 800, showBackground = true)
@Preview(name = "1. Squat Landscape (Foldable)", widthDp = 800, heightDp = 600, showBackground = true)
@Preview(name = "2. Perfect Square", widthDp = 600, heightDp = 600, showBackground = true)
@Preview(name = "3. Ultra-Narrow Cover", widthDp = 320, heightDp = 840, showBackground = true)
@Preview(name = "3. Ultra-Wide Cover (Landscape)", widthDp = 840, heightDp = 320, showBackground = true)
@Preview(name = "4. Compact Multi-Window (Portrait)", widthDp = 360, heightDp = 480, showBackground = true)
@Preview(name = "4. Compact Multi-Window (Landscape)", widthDp = 480, heightDp = 360, showBackground = true)
@Preview(name = "5. Expanded Tablet (Landscape)", widthDp = 1280, heightDp = 800, showBackground = true)
@Preview(name = "5. Expanded Tablet (Portrait)", widthDp = 800, heightDp = 1280, showBackground = true)
annotation class AdaptiveEdgeCasePreviews

private val previewState = MainUiState(
    station = Station("syd", "Sydney", -33.8688, 151.2093),
    currentUvIndex = 7.5,
    stationStatus = "OK",
    curve = emptyList(),
    protectionWindow = ProtectionWindow(startMinutes = 600, endMinutes = 960),
    distanceKm = 42.0,
    autoLocation = false,
    isLoadingCurve = false,
    liveError = false,
    curveError = false,
    locationPermissionNeeded = false,
)

@AdaptiveEdgeCasePreviews
@Composable
fun MainScreenPreview() {
    UvAustraliaTheme {
        MainScreenContent(
            state = previewState,
            initialDevOverrides = DevOverrides(
                forceProtectionWindow = true,
                forceFigureInaccuracy = true,
            ),
        )
    }
}
