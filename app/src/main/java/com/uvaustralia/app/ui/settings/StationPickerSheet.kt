package com.uvaustralia.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvaustralia.app.domain.AUSTRALIAN_STATIONS
import com.uvaustralia.app.domain.ELSEWHERE_STATIONS
import com.uvaustralia.app.domain.Station

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationPickerSheet(
    currentStation: Station?,
    autoLocation: Boolean,
    onDismiss: () -> Unit,
    onSelectStation: (Station) -> Unit,
    onAutoLocationToggle: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Monitoring Station",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            // Auto-location toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "Auto-select nearest station",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    Text(
                        text = "Automatically picks the closest station when the app opens.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    modifier = Modifier.padding(start = 12.dp),
                    checked = autoLocation,
                    onCheckedChange = onAutoLocationToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                item {
                    Text(
                        text = "Australia",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
                items(AUSTRALIAN_STATIONS) { station ->
                    StationRow(station, station.code == currentStation?.code, onSelectStation)
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Elsewhere",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
                items(ELSEWHERE_STATIONS) { station ->
                    StationRow(station, station.code == currentStation?.code, onSelectStation)
                }
                item {
                    Text(
                        text = "UV observations courtesy of ARPANSA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StationRow(
    station: Station,
    isSelected: Boolean,
    onSelectStation: (Station) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectStation(station) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = station.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
