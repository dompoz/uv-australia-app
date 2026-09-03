package com.uvaustralia.app.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uvaustralia.app.domain.PROTECTION_THRESHOLD
import com.uvaustralia.app.domain.SUNSMART_STEPS
import com.uvaustralia.app.domain.UviBand
import com.uvaustralia.app.domain.burnEasilyMinutes
import com.uvaustralia.app.domain.burnRarelyMinutes
import com.uvaustralia.app.domain.formatBurnTime
import com.uvaustralia.app.domain.uvBandInfoFor
import com.uvaustralia.app.domain.uviBandFor
import com.uvaustralia.app.prefs.RiskScheme
import com.uvaustralia.app.ui.theme.LocalIsDarkTheme

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
import com.uvaustralia.app.ui.theme.UvDarkBox6
import com.uvaustralia.app.ui.theme.UvDarkBox8
import com.uvaustralia.app.ui.theme.UvDarkBox11
import com.uvaustralia.app.ui.theme.UvDarkBoxEx
import com.uvaustralia.app.ui.theme.UvDarkText6
import com.uvaustralia.app.ui.theme.UvDarkText8
import com.uvaustralia.app.ui.theme.UvDarkText11
import com.uvaustralia.app.ui.theme.UvDarkTextEx
import com.uvaustralia.app.ui.theme.UvLightBox6
import com.uvaustralia.app.ui.theme.UvLightBox8
import com.uvaustralia.app.ui.theme.UvLightBox11
import com.uvaustralia.app.ui.theme.UvLightBoxEx
import com.uvaustralia.app.ui.theme.UvLightText6
import com.uvaustralia.app.ui.theme.UvLightText8
import com.uvaustralia.app.ui.theme.UvLightText11
import com.uvaustralia.app.ui.theme.UvLightTextEx

private val BurnEasilyLightText = Color(0xFF662522)
private val BurnEasilyLightBox  = Color(0xFFFFCBA9)
private val BurnRarelyLightText = Color(0xFF3F1000)
private val BurnRarelyLightBox  = Color(0xFFFBC187)

private val BurnEasilyDarkText  = Color(0xFFFFB1A2)
private val BurnEasilyDarkBox   = Color(0xFF250F0B)
private val BurnRarelyDarkText  = Color(0xFFFFA156)
private val BurnRarelyDarkBox   = Color(0xFF0D0400)

@Composable
fun UvDetailModal(
    uvIndex: Double,
    riskScheme: RiskScheme,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)

    val band = uviBandFor(uvIndex)
    val bandInfo = uvBandInfoFor(band, uvIndex)
    val dark = LocalIsDarkTheme.current
    val bandColors = modalBandColors(uvIndex, riskScheme, dark)

    val showBurnTimes = uvIndex >= PROTECTION_THRESHOLD
    val burnEasily = if (showBurnTimes) burnEasilyMinutes(uvIndex) else null
    val burnRarely = if (showBurnTimes) burnRarelyMinutes(uvIndex) else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.85f) + fadeIn(),
            exit = scaleOut(targetScale = 0.85f) + fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HeaderSection(uvIndex, band, bandColors, riskScheme)

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = bandInfo.riskSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    if (burnEasily != null && burnRarely != null) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                        BurnTimeSection(burnEasily, burnRarely)
                    }

                    if (bandInfo.showProtectionSteps) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                        SunSmartStepsSection()
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Burn estimates based on Fitzpatrick MED data. Recommendations: WHO Global Solar UVI & Cancer Council Australia SunSmart.",
                        style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    uvIndex: Double,
    band: UviBand,
    colors: ModalBandColors,
    riskScheme: RiskScheme,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.box),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatUvModal(uvIndex),
                style = MaterialTheme.typography.displayMedium,
                color = colors.text,
                textAlign = TextAlign.Center,
            )
            if (riskScheme == RiskScheme.GLOBAL_SOLAR_UVI) {
                Text(
                    text = band.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = colors.text.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = "UV Index",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BurnTimeSection(
    burnEasilyMin: Double,
    burnRarelyMin: Double,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val dark = LocalIsDarkTheme.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BurnTimeCell(
                question = "Burn easily?",
                timeFormatted = formatBurnTime(burnEasilyMin),
                textColor = if (dark) BurnEasilyDarkText else BurnEasilyLightText,
                boxColor = if (dark) BurnEasilyDarkBox else BurnEasilyLightBox,
                modifier = Modifier.weight(1f),
            )
            BurnTimeCell(
                question = "Burn rarely?",
                timeFormatted = formatBurnTime(burnRarelyMin),
                textColor = if (dark) BurnRarelyDarkText else BurnRarelyLightText,
                boxColor = if (dark) BurnRarelyDarkBox else BurnRarelyLightBox,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "UV affects all skin types.\nDamage accumulates over your lifetime, even if you don't burn.",
            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BurnTimeCell(
    question: String,
    timeFormatted: String,
    textColor: Color,
    boxColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(boxColor)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
            ),
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
            ),
            color = textColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "to burn",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = textColor.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "without protection",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = textColor.copy(alpha = 0.45f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SunSmartStepsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "SunSmart protection steps",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(2.dp))
        SUNSMART_STEPS.forEach { (keyword, detail) ->
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(keyword)
                    }
                    append("  $detail")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }
    }
}

private data class ModalBandColors(val text: Color, val box: Color)

private fun modalBandColors(uvi: Double, riskScheme: RiskScheme, dark: Boolean): ModalBandColors {
    return if (riskScheme == RiskScheme.GLOBAL_SOLAR_UVI) {
        modalWhoBandColors(uviBandFor(uvi), dark)
    } else {
        modalSunSmartColors(uvi, dark)
    }
}

private fun modalWhoBandColors(band: UviBand, dark: Boolean): ModalBandColors = when (band) {
    UviBand.LOW       -> if (dark) ModalBandColors(WhoDarkTextLow,      WhoDarkBoxLow)      else ModalBandColors(WhoLightTextLow,      WhoLightBoxLow)
    UviBand.MODERATE  -> if (dark) ModalBandColors(WhoDarkTextModerate, WhoDarkBoxModerate) else ModalBandColors(WhoLightTextModerate, WhoLightBoxModerate)
    UviBand.HIGH      -> if (dark) ModalBandColors(WhoDarkTextHigh,     WhoDarkBoxHigh)     else ModalBandColors(WhoLightTextHigh,     WhoLightBoxHigh)
    UviBand.VERY_HIGH -> if (dark) ModalBandColors(WhoDarkTextVeryHigh, WhoDarkBoxVeryHigh) else ModalBandColors(WhoLightTextVeryHigh, WhoLightBoxVeryHigh)
    UviBand.EXTREME   -> if (dark) ModalBandColors(WhoDarkTextExtreme,  WhoDarkBoxExtreme)  else ModalBandColors(WhoLightTextExtreme,  WhoLightBoxExtreme)
}

private fun modalSunSmartColors(uvi: Double, dark: Boolean): ModalBandColors = when {
    uvi < 3  -> ModalBandColors(Color.Unspecified, Color.Unspecified)
    uvi < 6  -> if (dark) ModalBandColors(UvDarkText6,  UvDarkBox6)  else ModalBandColors(UvLightText6,  UvLightBox6)
    uvi < 8  -> if (dark) ModalBandColors(UvDarkText8,  UvDarkBox8)  else ModalBandColors(UvLightText8,  UvLightBox8)
    uvi < 11 -> if (dark) ModalBandColors(UvDarkText11, UvDarkBox11) else ModalBandColors(UvLightText11, UvLightBox11)
    else     -> if (dark) ModalBandColors(UvDarkTextEx, UvDarkBoxEx) else ModalBandColors(UvLightTextEx, UvLightBoxEx)
}

private fun formatUvModal(uv: Double): String =
    if (uv == uv.toLong().toDouble()) uv.toLong().toString()
    else "%.1f".format(uv)
