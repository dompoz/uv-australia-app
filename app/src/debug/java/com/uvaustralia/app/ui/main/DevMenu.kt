package com.uvaustralia.app.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

data class DevOverrides(
    val forceProtectionWindow: Boolean = false,
    val forceProtectionWarning: Boolean = false,
    val forceFigureInaccuracy: Boolean = false,
    val pauseLivePolling: Boolean = false,
    val devUvIndex: Double? = null,
)

@Composable
fun rememberDevOverrides(): DevOverridesState {
    var overrides by remember { mutableStateOf(DevOverrides()) }
    var showMenu by remember { mutableStateOf(false) }
    return DevOverridesState(
        overrides = overrides,
        showMenu = showMenu,
        onShowMenu = { showMenu = true },
        onOverridesChange = { overrides = it },
        onDismissMenu = { showMenu = false },
    )
}

class DevOverridesState(
    val overrides: DevOverrides,
    val showMenu: Boolean,
    val onShowMenu: () -> Unit,
    val onOverridesChange: (DevOverrides) -> Unit,
    val onDismissMenu: () -> Unit,
)

@Composable
fun GearIconContent(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Dev options",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = androidx.compose.ui.Modifier.size(24.dp),
        )
    }
}

@Composable
fun DevMenuHost(
    state: DevOverridesState,
    onPauseChange: (Boolean) -> Unit,
) {
    if (state.showMenu) {
        DevMenuDialog(
            overrides = state.overrides,
            onOverridesChange = { new ->
                if (new.pauseLivePolling != state.overrides.pauseLivePolling) {
                    onPauseChange(new.pauseLivePolling)
                }
                state.onOverridesChange(new)
            },
            onDismiss = state.onDismissMenu,
        )
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
                        onOverridesChange(overrides.copy(devUvIndex = text.toDoubleOrNull()))
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
