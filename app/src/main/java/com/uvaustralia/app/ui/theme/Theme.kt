package com.uvaustralia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = UvAmber,
    onPrimary        = DarkBackground,
    primaryContainer = DarkSurface2,
    onPrimaryContainer = UvAmberLight,
    secondary        = UvMagenta,
    onSecondary      = DarkBackground,
    background       = DarkBackground,
    onBackground     = DarkOnSurface,
    surface          = DarkSurface,
    onSurface        = DarkOnSurface,
    surfaceVariant   = DarkSurface2,
    onSurfaceVariant = DarkOnSurface2,
    outline          = DarkSurface2,
)

private val LightAmberDark  = Color(0xFFFF9E3B)
private val LightMagentaDark = Color(0xFF9040A0)

private val LightColorScheme = lightColorScheme(
    primary          = LightAmberDark,
    onPrimary        = LightBackground,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightOnSurface,
    secondary        = LightMagentaDark,
    onSecondary      = LightBackground,
    background       = LightBackground,
    onBackground     = LightOnSurface,
    surface          = LightSurface,
    onSurface        = LightOnSurface,
    surfaceVariant   = LightSurface2,
    onSurfaceVariant = LightOnSurface2,
    outline          = LightSurface2,
)

@Composable
fun UvAustraliaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = UvTypography,
        content     = content,
    )
}
