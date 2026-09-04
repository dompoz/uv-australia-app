package com.uvaustralia.app.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.uvaustralia.app.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.uvaustralia.app.domain.PROTECTION_THRESHOLD
import com.uvaustralia.app.domain.UviBand
import com.uvaustralia.app.domain.uviBandFor
import com.uvaustralia.app.prefs.RiskScheme
import com.uvaustralia.app.ui.theme.UvDarkBox11
import com.uvaustralia.app.ui.theme.UvDarkBox6
import com.uvaustralia.app.ui.theme.UvDarkBox8
import com.uvaustralia.app.ui.theme.UvDarkBoxEx
import com.uvaustralia.app.ui.theme.UvDarkText11
import com.uvaustralia.app.ui.theme.UvDarkText6
import com.uvaustralia.app.ui.theme.UvDarkText8
import com.uvaustralia.app.ui.theme.UvDarkTextEx
import com.uvaustralia.app.ui.theme.UvLightBox11
import com.uvaustralia.app.ui.theme.UvLightBox6
import com.uvaustralia.app.ui.theme.UvLightBox8
import com.uvaustralia.app.ui.theme.UvLightBoxEx
import com.uvaustralia.app.ui.theme.UvLightText11
import com.uvaustralia.app.ui.theme.UvLightText6
import com.uvaustralia.app.ui.theme.UvLightText8
import com.uvaustralia.app.ui.theme.UvLightTextEx
import com.uvaustralia.app.ui.theme.WhoDarkBoxExtreme
import com.uvaustralia.app.ui.theme.WhoDarkBoxHigh
import com.uvaustralia.app.ui.theme.WhoDarkBoxLow
import com.uvaustralia.app.ui.theme.WhoDarkBoxModerate
import com.uvaustralia.app.ui.theme.WhoDarkBoxVeryHigh
import com.uvaustralia.app.ui.theme.WhoDarkTextExtreme
import com.uvaustralia.app.ui.theme.WhoDarkTextHigh
import com.uvaustralia.app.ui.theme.WhoDarkTextLow
import com.uvaustralia.app.ui.theme.WhoDarkTextModerate
import com.uvaustralia.app.ui.theme.WhoDarkTextVeryHigh
import com.uvaustralia.app.ui.theme.WhoLightBoxExtreme
import com.uvaustralia.app.ui.theme.WhoLightBoxHigh
import com.uvaustralia.app.ui.theme.WhoLightBoxLow
import com.uvaustralia.app.ui.theme.WhoLightBoxModerate
import com.uvaustralia.app.ui.theme.WhoLightBoxVeryHigh
import com.uvaustralia.app.ui.theme.WhoLightTextExtreme
import com.uvaustralia.app.ui.theme.WhoLightTextHigh
import com.uvaustralia.app.ui.theme.WhoLightTextLow
import com.uvaustralia.app.ui.theme.WhoLightTextModerate
import com.uvaustralia.app.ui.theme.WhoLightTextVeryHigh
import com.uvaustralia.app.ui.theme.WarningColor

// Height reserved for the "Sun protection recommended" line so sibling
// composables can sit beneath it at a stable position regardless of visibility.
val ProtectionWarningSlotHeight = 28.dp

@Composable
fun UvIndexDisplay(
    uvIndex: Double?,
    isError: Boolean,
    stationStatus: String,
    riskScheme: RiskScheme = RiskScheme.SUNSMART,
    forceProtectionWarning: Boolean = false,
    showHint: Boolean = false,
    onTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val showProtectionWarning = forceProtectionWarning ||
        (uvIndex != null && uvIndex >= PROTECTION_THRESHOLD && stationStatus != "NA" && !isError)
    val currentUvColors = uvIndex?.let { uvColors(it, riskScheme) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedContent(
            targetState = uvIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "UV index",
        ) { index ->
            when {
                isError || stationStatus == "NA" -> {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                index == null -> {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                else -> {
                    val uvColors = uvColors(index, riskScheme)
                    val numberStyle = MaterialTheme.typography.displayMedium.copy(fontSize = 80.sp)
                    val labelStyle  = MaterialTheme.typography.bodyMedium
                    val labelColor  = uvColors.text.copy(alpha = 0.5f)
                    val boxWidthDp  = 160.dp

                    val labelText: String
                    val labelFontWeight: FontWeight
                    if (riskScheme == RiskScheme.GLOBAL_SOLAR_UVI) {
                        labelText = uviBandFor(index).displayName
                        labelFontWeight = FontWeight.SemiBold
                    } else {
                        labelText = "UV Index"
                        labelFontWeight = FontWeight.Normal
                    }

                    val tapModifier = if (onTap != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTap,
                        )
                    } else Modifier

                    OnboardingHint(
                        visible = showHint,
                        onTap = { onTap?.invoke() },
                        cornerRadius = 16.dp,
                        padding = 0.dp,
                    ) {
                        SubcomposeLayout(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(uvColors.box)
                                .then(tapModifier),
                        ) { _ ->
                            val boxWidthPx = boxWidthDp.roundToPx()
                            val widthConstraints = Constraints(minWidth = boxWidthPx, maxWidth = boxWidthPx)

                            val numberPlaceable = subcompose("number") {
                                Text(
                                    text = formatUv(index),
                                    style = numberStyle,
                                    color = uvColors.text,
                                    textAlign = TextAlign.Center,
                                )
                            }[0].measure(widthConstraints)

                            val boxHeight  = numberPlaceable.height
                            val shiftUpPx  = (boxHeight * 0.045f).roundToInt()

                            val labelPlaceable = subcompose("label") {
                                Text(
                                    text = labelText,
                                    style = labelStyle.copy(fontWeight = labelFontWeight),
                                    color = labelColor,
                                    textAlign = TextAlign.Center,
                                )
                            }[0].measure(widthConstraints)

                            val labelCentreY = (boxHeight * 0.875f).roundToInt()
                            val labelY = labelCentreY - labelPlaceable.height / 2

                            layout(boxWidthPx, boxHeight) {
                                numberPlaceable.placeRelative(0, -shiftUpPx)
                                labelPlaceable.placeRelative(0, labelY)
                            }
                        }
                    }
                }
            }
        }

        if (stationStatus == "NA") {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Station offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Fixed-height slot: always reserves space for the warning line so
        // anything placed below UvIndexDisplay stays at a stable position.
        Box(Modifier.height(ProtectionWarningSlotHeight), contentAlignment = Alignment.Center) {
            if (showProtectionWarning) {
                Text(
                    text = "⚠ Protection needed now ⚠",
                    style = MaterialTheme.typography.bodyLarge,
                    color = currentUvColors?.text ?: WarningColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun formatUv(uv: Double): String {
    return if (uv == uv.toLong().toDouble()) uv.toLong().toString()
    else "%.1f".format(uv)
}

private data class UvColors(val text: Color, val box: Color)

@Composable
private fun uvColors(uv: Double, riskScheme: RiskScheme): UvColors {
    val dark = LocalIsDarkTheme.current
    return if (riskScheme == RiskScheme.GLOBAL_SOLAR_UVI) {
        whoUvColors(uv, dark)
    } else {
        sunSmartUvColors(uv, dark)
    }
}

@Composable
private fun sunSmartUvColors(uv: Double, dark: Boolean): UvColors {
    return when {
        uv < 3  -> UvColors(
            text = MaterialTheme.colorScheme.onSurface,
            box  = MaterialTheme.colorScheme.surface,
        )
        uv < 6  -> if (dark) UvColors(UvDarkText6,  UvDarkBox6)  else UvColors(UvLightText6,  UvLightBox6)
        uv < 8  -> if (dark) UvColors(UvDarkText8,  UvDarkBox8)  else UvColors(UvLightText8,  UvLightBox8)
        uv < 11 -> if (dark) UvColors(UvDarkText11, UvDarkBox11) else UvColors(UvLightText11, UvLightBox11)
        else    -> if (dark) UvColors(UvDarkTextEx, UvDarkBoxEx) else UvColors(UvLightTextEx, UvLightBoxEx)
    }
}

private fun whoUvColors(uv: Double, dark: Boolean): UvColors {
    return when (uviBandFor(uv)) {
        UviBand.LOW       -> if (dark) UvColors(WhoDarkTextLow,      WhoDarkBoxLow)      else UvColors(WhoLightTextLow,      WhoLightBoxLow)
        UviBand.MODERATE  -> if (dark) UvColors(WhoDarkTextModerate, WhoDarkBoxModerate) else UvColors(WhoLightTextModerate, WhoLightBoxModerate)
        UviBand.HIGH      -> if (dark) UvColors(WhoDarkTextHigh,     WhoDarkBoxHigh)     else UvColors(WhoLightTextHigh,     WhoLightBoxHigh)
        UviBand.VERY_HIGH -> if (dark) UvColors(WhoDarkTextVeryHigh, WhoDarkBoxVeryHigh) else UvColors(WhoLightTextVeryHigh, WhoLightBoxVeryHigh)
        UviBand.EXTREME   -> if (dark) UvColors(WhoDarkTextExtreme,  WhoDarkBoxExtreme)  else UvColors(WhoLightTextExtreme,  WhoLightBoxExtreme)
    }
}
