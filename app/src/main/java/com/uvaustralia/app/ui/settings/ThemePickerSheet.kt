package com.uvaustralia.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uvaustralia.app.prefs.ThemePreference
import com.uvaustralia.app.ui.theme.DarkBackground
import com.uvaustralia.app.ui.theme.DarkOnSurface
import com.uvaustralia.app.ui.theme.DarkSurface
import com.uvaustralia.app.ui.theme.LightBackground
import com.uvaustralia.app.ui.theme.LightOnSurface
import com.uvaustralia.app.ui.theme.LightSurface
import com.uvaustralia.app.ui.theme.UvAmber
import com.uvaustralia.app.ui.theme.UvAmberLight

private val cardShape = RoundedCornerShape(12.dp)

private val lightCardBg   = LightSurface
private val lightCardText = LightOnSurface
private val lightCardIcon = Color(0xFFFF9E3B)

private val darkCardBg   = DarkSurface
private val darkCardText = DarkOnSurface
private val darkCardIcon = UvAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(
    current: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SystemThemeCard(
                    selected = current == ThemePreference.SYSTEM,
                    onClick = { onSelect(ThemePreference.SYSTEM) },
                    modifier = Modifier.weight(1f),
                )
                SingleThemeCard(
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    bgColor = lightCardBg,
                    textColor = lightCardText,
                    iconColor = lightCardIcon,
                    selected = current == ThemePreference.LIGHT,
                    onClick = { onSelect(ThemePreference.LIGHT) },
                    modifier = Modifier.weight(1f),
                )
                SingleThemeCard(
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    bgColor = darkCardBg,
                    textColor = darkCardText,
                    iconColor = darkCardIcon,
                    selected = current == ThemePreference.DARK,
                    onClick = { onSelect(ThemePreference.DARK) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SingleThemeCard(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    iconColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clip(cardShape)
                .border(2.dp, borderColor, cardShape)
                .background(bgColor)
                .clickable(onClick = onClick),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SystemThemeCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clip(cardShape)
                .border(2.dp, borderColor, cardShape)
                .clickable(onClick = onClick),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(lightCardBg),
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = null,
                        tint = lightCardIcon,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(darkCardBg),
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = darkCardIcon,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "System",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
