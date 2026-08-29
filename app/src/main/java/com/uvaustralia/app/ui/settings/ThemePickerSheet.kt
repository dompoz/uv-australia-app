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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.unit.sp
import com.uvaustralia.app.prefs.RiskScheme
import com.uvaustralia.app.prefs.ThemePreference
import com.uvaustralia.app.ui.theme.DarkBackground
import com.uvaustralia.app.ui.theme.DarkOnSurface
import com.uvaustralia.app.ui.theme.DarkSurface
import com.uvaustralia.app.ui.theme.LightBackground
import com.uvaustralia.app.ui.theme.LightOnSurface
import com.uvaustralia.app.ui.theme.LightSurface
import com.uvaustralia.app.ui.theme.UvAmber
import com.uvaustralia.app.ui.theme.UvAmberLight
import com.uvaustralia.app.ui.theme.WhoBandExtreme
import com.uvaustralia.app.ui.theme.WhoBandHigh
import com.uvaustralia.app.ui.theme.WhoBandLow
import com.uvaustralia.app.ui.theme.WhoBandModerate
import com.uvaustralia.app.ui.theme.WhoBandVeryHigh

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
    currentTheme: ThemePreference,
    currentScheme: RiskScheme,
    onSelectTheme: (ThemePreference) -> Unit,
    onSelectScheme: (RiskScheme) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            Text(
                text = "Display mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SystemThemeCard(
                    selected = currentTheme == ThemePreference.SYSTEM,
                    onClick = { onSelectTheme(ThemePreference.SYSTEM) },
                    modifier = Modifier.weight(1f),
                )
                SingleThemeCard(
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    bgColor = lightCardBg,
                    textColor = lightCardText,
                    iconColor = lightCardIcon,
                    selected = currentTheme == ThemePreference.LIGHT,
                    onClick = { onSelectTheme(ThemePreference.LIGHT) },
                    modifier = Modifier.weight(1f),
                )
                SingleThemeCard(
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    bgColor = darkCardBg,
                    textColor = darkCardText,
                    iconColor = darkCardIcon,
                    selected = currentTheme == ThemePreference.DARK,
                    onClick = { onSelectTheme(ThemePreference.DARK) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Risk indication",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SunSmartSchemeCard(
                    selected = currentScheme == RiskScheme.SUNSMART,
                    onClick = { onSelectScheme(RiskScheme.SUNSMART) },
                    modifier = Modifier.weight(1f),
                )
                GlobalSolarUviSchemeCard(
                    selected = currentScheme == RiskScheme.GLOBAL_SOLAR_UVI,
                    onClick = { onSelectScheme(RiskScheme.GLOBAL_SOLAR_UVI) },
                    modifier = Modifier.weight(1f),
                )
                // Empty third column to match the three-card theme row width
                Spacer(modifier = Modifier.weight(1f))
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

@Composable
private fun SunSmartSchemeCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val bgColor = Color(0xFF3A2000)
    val textColor = UvAmberLight

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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = UvAmber,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "3+",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = textColor,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "SunSmart",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GlobalSolarUviSchemeCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    val bandColors = listOf(
        WhoBandExtreme,
        WhoBandVeryHigh,
        WhoBandHigh,
        WhoBandModerate,
        WhoBandLow,
    )

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
            Column(modifier = Modifier.fillMaxSize()) {
                for ((index, color) in bandColors.withIndex()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(color),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Global Solar UVI",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
