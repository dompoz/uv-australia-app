package com.uvaustralia.app.ui.main

import androidx.compose.runtime.Composable

data class DevOverrides(
    val forceProtectionWindow: Boolean = false,
    val forceProtectionWarning: Boolean = false,
    val forceFigureInaccuracy: Boolean = false,
    val pauseLivePolling: Boolean = false,
    val devUvIndex: Double? = null,
)

@Composable
fun rememberDevOverrides() = DevOverridesState(
    overrides = DevOverrides(),
    showMenu = false,
    onShowMenu = {},
    onOverridesChange = {},
    onDismissMenu = {},
)

class DevOverridesState(
    val overrides: DevOverrides,
    val showMenu: Boolean,
    val onShowMenu: () -> Unit,
    val onOverridesChange: (DevOverrides) -> Unit,
    val onDismissMenu: () -> Unit,
)

@Composable
fun GearIconContent(onClick: () -> Unit) {
    // No gear icon in release builds.
}

@Composable
fun DevMenuHost(
    state: DevOverridesState,
    onPauseChange: (Boolean) -> Unit,
) {
    // No dev menu in release builds.
}
