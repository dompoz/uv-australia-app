package com.uvaustralia.app.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.uvaustralia.app.ui.theme.JostFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uvaustralia.app.BuildConfig
import com.uvaustralia.app.prefs.ThemePreference
import com.uvaustralia.app.ui.settings.StationPickerSheet
import com.uvaustralia.app.ui.settings.ThemePickerSheet
import java.time.LocalTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreenContent(
        state = state,
        onSelectStation = { viewModel.selectStation(it) },
        onAutoLocationToggle = { viewModel.setAutoLocation(it) },
        onLivePollingPaused = { viewModel.setLivePollingPaused(it) },
        onThemeChange = { viewModel.setThemePreference(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreenContent(
    state: MainUiState,
    initialDevOverrides: DevOverrides = DevOverrides(),
    onSelectStation: (com.uvaustralia.app.domain.Station) -> Unit = {},
    onAutoLocationToggle: (Boolean) -> Unit = {},
    onLivePollingPaused: (Boolean) -> Unit = {},
    onThemeChange: (ThemePreference) -> Unit = {},
) {
    var showPicker by remember { mutableStateOf(false) }
    var showDistanceModal by remember { mutableStateOf(false) }
    var showDevMenu by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var devOverrides by remember { mutableStateOf(initialDevOverrides) }
    var graphExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = graphExpanded) {
        graphExpanded = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val isLandscape = maxWidth > maxHeight
            val topWidth: Dp
            val paneWidth: Dp
            val topHeight: Dp
            val paneHeight: Dp
            if (isLandscape) {
                topWidth  = (maxWidth * 0.40f).coerceIn(220.dp, 340.dp)
                paneWidth = maxWidth - topWidth
                topHeight  = maxHeight
                paneHeight = maxHeight
            } else {
                topHeight  = (maxHeight * 0.45f).coerceIn(280.dp, 340.dp)
                paneHeight = maxHeight - topHeight
                topWidth  = maxWidth
                paneWidth = maxWidth
            }

            if (graphExpanded) {
                ExpandedGraph(
                    state = state,
                    devOverrides = devOverrides,
                    onShowDistanceModal = { showDistanceModal = true },
                    onCollapse = { graphExpanded = false },
                )
            } else {
                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        TopPane(
                            state = state,
                            devOverrides = devOverrides,
                            onShowPicker = { showPicker = true },
                            onShowDevMenu = { showDevMenu = true },
                            onShowThemePicker = { showThemePicker = true },
                            modifier = Modifier
                                .width(topWidth)
                                .fillMaxHeight(),
                        )
                        BottomPane(
                            state = state,
                            paneWidth = paneWidth,
                            paneHeight = paneHeight,
                            devOverrides = devOverrides,
                            onShowDistanceModal = { showDistanceModal = true },
                            onExpand = { graphExpanded = true },
                            modifier = Modifier
                                .width(paneWidth)
                                .fillMaxHeight(),
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopPane(
                            state = state,
                            devOverrides = devOverrides,
                            onShowPicker = { showPicker = true },
                            onShowDevMenu = { showDevMenu = true },
                            onShowThemePicker = { showThemePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(topHeight),
                        )
                        BottomPane(
                            state = state,
                            paneWidth = paneWidth,
                            paneHeight = paneHeight,
                            devOverrides = devOverrides,
                            onShowDistanceModal = { showDistanceModal = true },
                            onExpand = { graphExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(paneHeight),
                        )
                    }
                }
            }
        }
    }

    if (showPicker) {
        StationPickerSheet(
            currentStation = state.station,
            autoLocation = state.autoLocation,
            onDismiss = { showPicker = false },
            onSelectStation = { station ->
                onSelectStation(station)
                showPicker = false
            },
            onAutoLocationToggle = onAutoLocationToggle,
        )
    }

    if (showDistanceModal) {
        val distanceKm = state.distanceKm?.roundToInt() ?: 0
        val stationName = state.station?.displayName ?: ""
        AlertDialog(
            onDismissRequest = { showDistanceModal = false },
            title = { Text("Distant Station") },
            text = {
                Text(
                    "This UV data is from the $stationName monitoring station, " +
                    "which is $distanceKm km from your current location. " +
                    "UV levels can vary significantly with distance, especially " +
                    "due to local cloud cover and geography. The reading may not " +
                    "accurately reflect conditions where you are."
                )
            },
            confirmButton = {
                TextButton(onClick = { showDistanceModal = false }) {
                    Text("Got it")
                }
            },
        )
    }

    if (BuildConfig.DEBUG && showDevMenu) {
        DevMenuDialog(
            overrides = devOverrides,
            onOverridesChange = { new ->
                if (new.pauseLivePolling != devOverrides.pauseLivePolling) {
                    onLivePollingPaused(new.pauseLivePolling)
                }
                devOverrides = new
            },
            onDismiss = { showDevMenu = false },
        )
    }

    if (showThemePicker) {
        ThemePickerSheet(
            current = state.themePreference,
            onSelect = { theme ->
                onThemeChange(theme)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false },
        )
    }
}

@Composable
private fun ExpandedGraph(
    state: MainUiState,
    devOverrides: DevOverrides,
    onShowDistanceModal: () -> Unit,
    onCollapse: () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val navBarHeight = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    val hMargin = 8.dp
    val warningStripHeight = 32.dp

    val distanceKm = state.distanceKm
    val showFigureInaccuracy = devOverrides.forceFigureInaccuracy ||
        (distanceKm != null && distanceKm > 30.0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCollapse,
            ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalHeight = maxHeight
            val reservedBelow = navBarHeight + if (showFigureInaccuracy) warningStripHeight else 0.dp
            val graphHeight = totalHeight - statusBarHeight - reservedBelow

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarHeight)
                    .height(graphHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoadingCurve && state.curve.isEmpty()) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    UvGraph(
                        curve = state.curve,
                        graphHeight = graphHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = hMargin, end = hMargin + 21.dp),
                    )
                }
            }

            if (showFigureInaccuracy) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = navBarHeight)
                        .height(warningStripHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    DistanceWarning(
                        stationName = state.station?.displayName ?: "",
                        distanceKm = distanceKm?.roundToInt() ?: 0,
                        onInfoClick = onShowDistanceModal,
                    )
                }
            }
        }
    }
}


@Composable
private fun TopPane(
    state: MainUiState,
    devOverrides: DevOverrides,
    onShowPicker: () -> Unit,
    onShowDevMenu: () -> Unit,
    onShowThemePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Button row pinned at the top
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
        ) {
            Button(
                onClick = onShowPicker,
                modifier = Modifier.padding(start = 8.dp),
                contentPadding = PaddingValues(start = 14.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                    )
                    Text(
                        text = state.station?.displayName ?: "UV Australia",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (BuildConfig.DEBUG) {
                IconButton(onClick = onShowDevMenu) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Dev options",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            IconButton(onClick = onShowThemePicker) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Appearance",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        // UV index + protection window centred in the remaining space
        val window = state.protectionWindow
        val showProtectionWindow = devOverrides.forceProtectionWindow ||
            (window != null && LocalTime.now().let { it.hour * 60 + it.minute } <= window.endMinutes)
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                UvIndexDisplay(
                    uvIndex = devOverrides.devUvIndex ?: state.currentUvIndex,
                    isError = state.liveError,
                    stationStatus = state.stationStatus,
                    forceProtectionWarning = devOverrides.forceProtectionWarning,
                )

                Box(
                    modifier = Modifier.height(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showProtectionWindow) {
                        val displayWindow = window ?: state.protectionWindow
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Expect harmful UV levels between",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = if (displayWindow != null)
                                    "${displayWindow.startDisplay()} – ${displayWindow.endDisplay()}"
                                else
                                    "— : — – — : —",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomPane(
    state: MainUiState,
    paneWidth: Dp,
    paneHeight: Dp,
    devOverrides: DevOverrides,
    onShowDistanceModal: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warningStripHeight = 32.dp
    val graphHeight = paneHeight - warningStripHeight
    // The y-axis label inset (42 dp) sits inside the canvas, so we offset end by half
    // that (21 dp) to keep the plot area itself centred within the horizontal margins.
    val hMargin = paneWidth * 0.05f

    val distanceKm = state.distanceKm
    val showFigureInaccuracy = devOverrides.forceFigureInaccuracy ||
        (distanceKm != null && distanceKm > 30.0)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExpand,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoadingCurve && state.curve.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hMargin)
                        .height(graphHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                UvGraph(
                    curve = state.curve,
                    graphHeight = graphHeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = hMargin, end = hMargin + 21.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(warningStripHeight),
            contentAlignment = Alignment.Center,
        ) {
            if (showFigureInaccuracy) {
                DistanceWarning(
                    stationName = state.station?.displayName ?: "",
                    distanceKm = distanceKm?.roundToInt() ?: 0,
                    onInfoClick = onShowDistanceModal,
                )
            } else {
                Text(
                    text = "UV observations courtesy of ARPANSA",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JostFamily,
                        fontWeight = FontWeight.ExtraLight,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DistanceWarning(
    stationName: String,
    distanceKm: Int,
    onInfoClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "May not be accurate for you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onInfoClick, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "More info",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DevMenuDialog(
    overrides: DevOverrides,
    onOverridesChange: (DevOverrides) -> Unit,
    onDismiss: () -> Unit,
) {
    var uvIndexText by remember { mutableStateOf(overrides.devUvIndex?.toString() ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dev Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DevToggleRow(
                    label = "Force protection window",
                    checked = overrides.forceProtectionWindow,
                    onCheckedChange = { onOverridesChange(overrides.copy(forceProtectionWindow = it)) },
                )
                DevToggleRow(
                    label = "Force protection warning",
                    checked = overrides.forceProtectionWarning,
                    onCheckedChange = { onOverridesChange(overrides.copy(forceProtectionWarning = it)) },
                )
                DevToggleRow(
                    label = "Force figure inaccuracy warning",
                    checked = overrides.forceFigureInaccuracy,
                    onCheckedChange = { onOverridesChange(overrides.copy(forceFigureInaccuracy = it)) },
                )
                DevToggleRow(
                    label = "Pause live polling",
                    checked = overrides.pauseLivePolling,
                    onCheckedChange = { onOverridesChange(overrides.copy(pauseLivePolling = it)) },
                )
                OutlinedTextField(
                    value = uvIndexText,
                    onValueChange = { text ->
                        uvIndexText = text
                        val parsed = text.toDoubleOrNull()
                        onOverridesChange(overrides.copy(devUvIndex = parsed))
                    },
                    label = { Text("Override UV index") },
                    placeholder = { Text("e.g. 8.5") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun DevToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
