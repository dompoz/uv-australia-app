package com.uvaustralia.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand colours from the icon
val UvAmberLight   = Color(0xFFFFEA99)  // Light yellow
val UvAmber        = Color(0xFFFFC62A)  // Mid amber
val UvAmberDeep    = Color(0xFFFF9500)  // Deep amber for graph gradient top
val UvMagenta      = Color(0xFFFFD4FF)  // Pale magenta accent

// Dark theme surfaces
val DarkBackground = Color(0xFF1A1400)
val DarkSurface    = Color(0xFF2A2200)
val DarkSurface2   = Color(0xFF3A3000)
val DarkOnSurface  = Color(0xFFFFEA99)
val DarkOnSurface2 = Color(0xFFCCBB77)

// Light theme surfaces
val LightBackground = Color(0xFFFFFBF0)
val LightSurface    = Color(0xFFFFF4CC)
val LightSurface2   = Color(0xFFFFE97A)
val LightOnSurface  = Color(0xFF3A2800)
val LightOnSurface2 = Color(0xFF6B4A00)

// Graph colours — dark theme
val GraphShadeTopDark    = Color(0x70FFA000)  // Subtle warm amber at UV=16 (was deep orange)
val GraphShadeBottomDark = Color(0x60FFC62A)  // Amber at UV=3, brighter than before
val GraphForecastDark    = Color(0x99FFD055)  // Dashed forecast line (60% alpha)

// Graph colours — light theme
val GraphShadeTopLight    = Color(0x60FFB200)  // Subtle warm amber at UV=16 (was deep orange)
val GraphShadeBottomLight = Color(0x80FFA500)  // Amber at UV=3, more visible on light bg
val GraphForecastLight    = Color(0x99B07000)  // Dashed forecast, dark enough to read

// Kept for any remaining usages
val GraphForecast    = GraphForecastDark
val GraphShadeBottom = GraphShadeBottomDark
val GraphShadeTop    = GraphShadeTopDark
val GraphCurrentTime = Color(0x99FFFFFF)

val WarningColor     = UvMagenta

// UV index band colours — dark theme (text, box)
val UvDarkText6  = Color(0xFFFFCECA); val UvDarkBox6  = Color(0xFF420C13)
val UvDarkText8  = Color(0xFFFFC8FD); val UvDarkBox8  = Color(0xFF3A0738)
val UvDarkText11 = Color(0xFFE1BDFF); val UvDarkBox11 = Color(0xFF310749)
val UvDarkTextEx = Color(0xFFCAC1FF); val UvDarkBoxEx = Color(0xFF260858)

// UV index band colours — light theme (text, box)
val UvLightText6  = Color(0xFF4F0008); val UvLightBox6  = Color(0xFFFFDFDD)
val UvLightText8  = Color(0xFF440044); val UvLightBox8  = Color(0xFFFFDFFE)
val UvLightText11 = Color(0xFF3A0057); val UvLightBox11 = Color(0xFFF0E0FF)
val UvLightTextEx = Color(0xFF2D0068); val UvLightBoxEx = Color(0xFFE7E4FF)

// WHO Global Solar UVI band base colours (midpoint references)
val WhoBandLow      = Color(0xFF78C639)  // Green
val WhoBandModerate = Color(0xFFFFD500)  // Yellow
val WhoBandHigh     = Color(0xFFFF8800)  // Orange
val WhoBandVeryHigh = Color(0xFFFF002A)  // Red
val WhoBandExtreme  = Color(0xFF543FC0)  // Purple

// WHO band colours — dark theme (box background, text)
val WhoDarkBoxLow      = Color(0xFF1F4D00); val WhoDarkTextLow      = Color(0xFFB8F082)
val WhoDarkBoxModerate = Color(0xFF3D3000); val WhoDarkTextModerate = Color(0xFFFFEA66)
val WhoDarkBoxHigh     = Color(0xFF3D1E00); val WhoDarkTextHigh     = Color(0xFFFFBB66)
val WhoDarkBoxVeryHigh = Color(0xFF3D000C); val WhoDarkTextVeryHigh = Color(0xFFFF8899)
val WhoDarkBoxExtreme  = Color(0xFF251848); val WhoDarkTextExtreme  = Color(0xFFB8AAFF)

// WHO band colours — light theme (box background, text)
val WhoLightBoxLow      = Color(0xFFDEF7CC); val WhoLightTextLow      = Color(0xFF1A4A00)
val WhoLightBoxModerate = Color(0xFFFFF5B8); val WhoLightTextModerate = Color(0xFF4A3800)
val WhoLightBoxHigh     = Color(0xFFFFEBCC); val WhoLightTextHigh     = Color(0xFF4A2000)
val WhoLightBoxVeryHigh = Color(0xFFFFDDE1); val WhoLightTextVeryHigh = Color(0xFF4A0010)
val WhoLightBoxExtreme  = Color(0xFFE8E4FF); val WhoLightTextExtreme  = Color(0xFF1E1260)
