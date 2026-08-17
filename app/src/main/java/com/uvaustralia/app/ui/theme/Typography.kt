package com.uvaustralia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.uvaustralia.app.R

val JostFamily = FontFamily(
    Font(R.font.jost_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.jost_light,      weight = FontWeight.Light),
    Font(R.font.jost_regular,    weight = FontWeight.Normal),
)

val UvTypography = Typography(
    displayLarge = TextStyle(
        fontFamily    = JostFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 96.sp,
        lineHeight    = 96.sp,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = JostFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 60.sp,
        lineHeight = 64.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = JostFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = JostFamily,
        fontWeight = FontWeight.Light,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = JostFamily,
        fontWeight = FontWeight.Light,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = JostFamily,
        fontWeight = FontWeight.ExtraLight,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = JostFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
