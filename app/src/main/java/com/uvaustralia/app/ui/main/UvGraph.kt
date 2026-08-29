package com.uvaustralia.app.ui.main

import androidx.compose.foundation.Canvas
import com.uvaustralia.app.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uvaustralia.app.domain.GRAPH_END_HOUR
import com.uvaustralia.app.domain.GRAPH_MAX_UV
import com.uvaustralia.app.domain.GRAPH_START_HOUR
import com.uvaustralia.app.domain.PROTECTION_THRESHOLD
import com.uvaustralia.app.domain.UvCurvePoint
import com.uvaustralia.app.domain.UviBand
import com.uvaustralia.app.prefs.RiskScheme
import com.uvaustralia.app.ui.theme.GraphForecastDark
import com.uvaustralia.app.ui.theme.GraphForecastLight
import com.uvaustralia.app.ui.theme.GraphShadeBottomDark
import com.uvaustralia.app.ui.theme.GraphShadeBottomLight
import com.uvaustralia.app.ui.theme.JostFamily
import com.uvaustralia.app.ui.theme.WhoBandExtreme
import com.uvaustralia.app.ui.theme.WhoBandHigh
import com.uvaustralia.app.ui.theme.WhoBandLow
import com.uvaustralia.app.ui.theme.WhoBandModerate
import com.uvaustralia.app.ui.theme.WhoBandVeryHigh
import kotlinx.coroutines.delay
import java.time.LocalTime

private val GRAPH_START_MINUTE = GRAPH_START_HOUR * 60  // 360
private val GRAPH_END_MINUTE   = GRAPH_END_HOUR * 60    // 1200
private val GRAPH_MINUTE_SPAN  = GRAPH_END_MINUTE - GRAPH_START_MINUTE

private data class WhoBandSpec(
    val band: UviBand,
    val color: Color,
    val upperBound: Double,
)

private val WHO_BANDS = listOf(
    WhoBandSpec(UviBand.LOW,       WhoBandLow,      3.0),
    WhoBandSpec(UviBand.MODERATE,  WhoBandModerate, 6.0),
    WhoBandSpec(UviBand.HIGH,      WhoBandHigh,     8.0),
    WhoBandSpec(UviBand.VERY_HIGH, WhoBandVeryHigh, 11.0),
    WhoBandSpec(UviBand.EXTREME,   WhoBandExtreme,  GRAPH_MAX_UV),
)

@Composable
fun UvGraph(
    curve: List<UvCurvePoint>,
    graphHeight: Dp,
    riskScheme: RiskScheme = RiskScheme.SUNSMART,
    showMinorGridLines: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val isDark = LocalIsDarkTheme.current

    val colorForecast = if (isDark) GraphForecastDark  else GraphForecastLight
    val colorMeasured = MaterialTheme.colorScheme.primary
    val curveFillColor = if (isDark) Color(0xFFFFA000) else Color(0xFFFFC259)
    val protectionBandColor = if (isDark) GraphShadeBottomDark else GraphShadeBottomLight

    // In WHO mode the measured line/fill are neutral greys matching the luminosity
    // of the SunSmart curve colours but fully desaturated
    val whoMeasuredColor  = if (isDark) Color(0xFFD9D9D9) else Color(0xFF666666)
    val whoForecastColor  = if (isDark) Color(0xFFD9D9D9) else Color(0xFFAAAAAA)
    // Soft-light fill: pure white in dark mode, pure black in light mode, for maximum
    // blend contrast against the coloured band fills
    val whoFillColor = if (isDark) Color.White else Color.Black

    // Neutral grey for the protection threshold line + label in WHO mode
    val thresholdNeutral = if (isDark) Color(0xFF888888) else Color(0xFF999999)

    var nowMinutes by remember { mutableIntStateOf(LocalTime.now().let { it.hour * 60 + it.minute }) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
        }
    }

    val graphMaxUv: Double = remember(curve) {
        val dayMax = curve.mapNotNull { it.forecast ?: it.measured }.maxOrNull()
        if (dayMax != null) {
            maxOf(13.0, kotlin.math.ceil(dayMax + 1.0))
        } else {
            13.0
        }
    }

    val axisFontSp = (graphHeight.value * 0.038f).coerceIn(9f, 13f).sp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(graphHeight)
    ) {
        val paddingLeft   = 42.dp.toPx()
        val paddingRight  = 12.dp.toPx()
        val paddingTop    = 8.dp.toPx()

        val xAxisStyle = TextStyle(
            fontFamily = JostFamily,
            color = onSurfaceVariant,
            fontSize = axisFontSp,
            fontWeight = FontWeight.Light,
        )
        val widestLabel = textMeasurer.measure("12 PM", xAxisStyle)
        val tickGap = 4.dp.toPx()
        val rotatedExtent = (widestLabel.size.width + widestLabel.size.height) * 0.7072f
        val paddingBottom = tickGap + rotatedExtent

        val chartLeft   = paddingLeft
        val chartRight  = size.width - paddingRight
        val chartTop    = paddingTop
        val chartBottom = size.height - paddingBottom
        val chartWidth  = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        fun minuteToX(minutes: Int): Float =
            chartLeft + (minutes - GRAPH_START_MINUTE).toFloat() / GRAPH_MINUTE_SPAN * chartWidth

        fun uvToY(uv: Double): Float =
            chartBottom - (uv / graphMaxUv).toFloat() * chartHeight

        val shadeTopY    = uvToY(graphMaxUv)
        val shadeBottomY = uvToY(PROTECTION_THRESHOLD)

        val thresholdColor = if (isDark) Color(0xEEFF9900) else Color(0xeee38826)

        if (riskScheme == RiskScheme.SUNSMART) {
            // --- SunSmart mode: existing appearance ---

            // Protection band: UV 3–6, alpha 0.5 at UV=3 fading to 0 at UV=6
            val shadeUv6Y = uvToY(6.0)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        protectionBandColor.copy(alpha = 0f),
                        protectionBandColor.copy(alpha = 0.25f),
                    ),
                    startY = shadeUv6Y,
                    endY   = shadeBottomY,
                ),
                topLeft = Offset(chartLeft, shadeUv6Y),
                size    = Size(chartWidth, shadeBottomY - shadeUv6Y),
            )

            // Grid lines + Y axis labels
            val gridUvValues = generateSequence(3) { it + 3 }.takeWhile { it <= graphMaxUv }.toList()
            val gridStyle = TextStyle(
                fontFamily = JostFamily,
                color = onSurfaceVariant,
                fontSize = axisFontSp,
                fontWeight = FontWeight.Light,
            )
            if (showMinorGridLines) {
                val minorDash = PathEffect.dashPathEffect(floatArrayOf(3f, 8f))
                val majorSet = gridUvValues.toSet()
                for (uvVal in 1..graphMaxUv.toInt()) {
                    if (uvVal in majorSet) continue
                    val y = uvToY(uvVal.toDouble())
                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.65f),
                        start = Offset(chartLeft, y),
                        end = Offset(chartRight, y),
                        strokeWidth = 0.5.dp.toPx(),
                        pathEffect = minorDash,
                    )
                }
            }
            for (uvVal in gridUvValues) {
                val y = uvToY(uvVal.toDouble())
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.25f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 0.5.dp.toPx(),
                )
                val label = textMeasurer.measure(uvVal.toString(), gridStyle)
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        chartLeft - label.size.width - 4.dp.toPx(),
                        y - label.size.height / 2f,
                    ),
                )
            }

            // X axis labels
            for (hour in GRAPH_START_HOUR..GRAPH_END_HOUR step 2) {
                val minutes = hour * 60
                val x = minuteToX(minutes)
                val label = when {
                    hour == 12 -> "12 PM"
                    hour < 12  -> "${hour} AM"
                    else       -> "${hour - 12} PM"
                }
                val measured = textMeasurer.measure(label, xAxisStyle)
                val labelTopY = chartBottom + 4.dp.toPx()
                withTransform({ rotate(degrees = 45f, pivot = Offset(x, labelTopY)) }) {
                    drawText(textLayoutResult = measured, topLeft = Offset(x, labelTopY))
                }
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    start = Offset(x, chartBottom),
                    end   = Offset(x, chartBottom + 3.dp.toPx()),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }

            if (curve.isNotEmpty()) {
                clipRect(chartLeft, chartTop, chartRight, chartBottom) {
                    val measuredPoints = curve.filter { (it.measured ?: -1.0) >= 0 }
                    if (measuredPoints.isNotEmpty()) {
                        val peakY = measuredPoints.minOf { uvToY(it.measured!!) }
                        drawCurveFill(
                            points = measuredPoints,
                            getValue = { it.measured!! },
                            fillBrush = Brush.verticalGradient(
                                colors = listOf(
                                    curveFillColor.copy(alpha = 1.0f),
                                    curveFillColor.copy(alpha = 0f),
                                ),
                                startY = peakY,
                                endY   = chartBottom,
                            ),
                            chartBottom = chartBottom,
                            minuteToX = ::minuteToX,
                            uvToY = ::uvToY,
                        )
                    }

                    drawCurve(
                        points = curve,
                        getValue = { it.forecast },
                        color = colorForecast.copy(alpha = 0.2f),
                        strokeWidth = 1.dp.toPx(),
                        dashed = true,
                        minuteToX = ::minuteToX,
                        uvToY = ::uvToY,
                    )
                    drawCurve(
                        points = curve,
                        getValue = { it.measured },
                        color = colorMeasured,
                        strokeWidth = 2.dp.toPx(),
                        dashed = false,
                        minuteToX = ::minuteToX,
                        uvToY = ::uvToY,
                    )

                    val lastMeasured = curve.lastOrNull { (it.measured ?: -1.0) >= 0 }
                    if (lastMeasured != null) {
                        val dotX = minuteToX(lastMeasured.minutesFromMidnight)
                        val dotY = uvToY(lastMeasured.measured!!)
                        drawCircle(
                            color = colorMeasured,
                            radius = 2.5.dp.toPx(),
                            center = Offset(dotX, dotY),
                        )
                    }
                }
            }

            // Threshold line + label on top
            val thresholdY = uvToY(PROTECTION_THRESHOLD)
            drawLine(
                color = thresholdColor,
                start = Offset(chartLeft, thresholdY),
                end   = Offset(chartRight, thresholdY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
            )
            val labelStyle = TextStyle(
                fontFamily = JostFamily,
                color = thresholdColor,
                fontSize = axisFontSp,
                fontWeight = FontWeight.Light,
            )
            val labelResult = textMeasurer.measure("Protection Recommended", labelStyle)
            val textW = labelResult.size.width.toFloat()
            val textH = labelResult.size.height.toFloat()
            val pivotX = chartRight - textH
            val pivotY = thresholdY - 3.dp.toPx()
            if (pivotY - textW > shadeTopY) {
                withTransform({
                    rotate(degrees = -90f, pivot = Offset(pivotX, pivotY))
                }) {
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(pivotX, pivotY),
                    )
                }
            }

        } else {
            // --- Global Solar UVI mode ---

            // 1. Band filled rectangles (25% alpha) and boundary lines
            for (spec in WHO_BANDS) {
                val bandLowerY = uvToY(spec.band.lowerBound).coerceIn(chartTop, chartBottom)
                val effectiveUpperBound = if (spec.band == UviBand.EXTREME) graphMaxUv else spec.upperBound
                val bandUpperY = uvToY(effectiveUpperBound).coerceIn(chartTop, chartBottom)

                // Filled rectangle for the band (25% opacity)
                drawRect(
                    color = spec.color.copy(alpha = 0.25f),
                    topLeft = Offset(chartLeft, bandUpperY),
                    size    = Size(chartWidth, bandLowerY - bandUpperY),
                )

                // Lower-bound line (solid, 1px) for all bands except Low (lowerBound == 0)
                if (spec.band.lowerBound > 0.0) {
                    drawLine(
                        color = spec.color,
                        start = Offset(chartLeft, bandLowerY),
                        end   = Offset(chartRight, bandLowerY),
                        strokeWidth = 0.75.dp.toPx(),
                    )
                }
            }

            // 1b. Minor grid lines drawn after band rects so they're visible on top
            if (showMinorGridLines) {
                val minorDash = PathEffect.dashPathEffect(floatArrayOf(3f, 8f))
                val majorBounds = WHO_BANDS.map { it.band.lowerBound.toInt() }.toSet()
                for (uvVal in 1..graphMaxUv.toInt()) {
                    if (uvVal in majorBounds) continue
                    val band = WHO_BANDS.lastOrNull { uvVal >= it.band.lowerBound }
                    val bandColor = when {
                        !isDark && band?.band == UviBand.LOW      -> Color(0xFF4A8A1A)
                        !isDark && band?.band == UviBand.MODERATE -> Color(0xFFB89600)
                        else -> band?.color ?: WhoBandLow
                    }
                    val bandAlpha = when {
                        isDark && band?.band == UviBand.EXTREME -> 1.0f
                        band?.band == UviBand.LOW || band?.band == UviBand.MODERATE || band?.band == UviBand.HIGH -> 1.0f
                        else -> 0.8f
                    }
                    val y = uvToY(uvVal.toDouble())
                    drawLine(
                        color = bandColor.copy(alpha = bandAlpha),
                        start = Offset(chartLeft, y),
                        end = Offset(chartRight, y),
                        strokeWidth = 0.5.dp.toPx(),
                        pathEffect = minorDash,
                    )
                }
            }

            // 2. Y axis tick labels coloured per band, at each band's lower bound
            for (spec in WHO_BANDS) {
                if (spec.band.lowerBound == 0.0) continue
                val uvVal = spec.band.lowerBound.toInt()
                val y = uvToY(spec.band.lowerBound)
                val tickStyle = TextStyle(
                    fontFamily = JostFamily,
                    color = spec.color,
                    fontSize = axisFontSp,
                    fontWeight = FontWeight.Light,
                )
                val label = textMeasurer.measure(uvVal.toString(), tickStyle)
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        chartLeft - label.size.width - 4.dp.toPx(),
                        y - label.size.height / 2f,
                    ),
                )
            }
            // Extra ticks at multiples of 3 above 11 (14, 17, ...) coloured as Extreme
            val extremeTickStyle = TextStyle(
                fontFamily = JostFamily,
                color = WhoBandExtreme,
                fontSize = axisFontSp,
                fontWeight = FontWeight.Light,
            )
            var extremeTick = 14
            while (extremeTick <= graphMaxUv) {
                val y = uvToY(extremeTick.toDouble())
                val label = textMeasurer.measure(extremeTick.toString(), extremeTickStyle)
                drawText(
                    textLayoutResult = label,
                    topLeft = Offset(
                        chartLeft - label.size.width - 4.dp.toPx(),
                        y - label.size.height / 2f,
                    ),
                )
                extremeTick += 3
            }

            // 3. X axis labels
            for (hour in GRAPH_START_HOUR..GRAPH_END_HOUR step 2) {
                val minutes = hour * 60
                val x = minuteToX(minutes)
                val label = when {
                    hour == 12 -> "12 PM"
                    hour < 12  -> "${hour} AM"
                    else       -> "${hour - 12} PM"
                }
                val measured = textMeasurer.measure(label, xAxisStyle)
                val labelTopY = chartBottom + 4.dp.toPx()
                withTransform({ rotate(degrees = 45f, pivot = Offset(x, labelTopY)) }) {
                    drawText(textLayoutResult = measured, topLeft = Offset(x, labelTopY))
                }
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    start = Offset(x, chartBottom),
                    end   = Offset(x, chartBottom + 3.dp.toPx()),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }

            // 5. Curves and fill using onSurfaceVariant colour
            if (curve.isNotEmpty()) {
                clipRect(chartLeft, chartTop, chartRight, chartBottom) {
                    val measuredPoints = curve.filter { (it.measured ?: -1.0) >= 0 }
                    if (measuredPoints.isNotEmpty()) {
                        val peakY = measuredPoints.minOf { uvToY(it.measured!!) }
                        val whoFillBrush = Brush.verticalGradient(
                            colors = listOf(
                                whoFillColor.copy(alpha = 1.0f),
                                whoFillColor.copy(alpha = 0f),
                            ),
                            startY = peakY,
                            endY   = chartBottom,
                        )
                        repeat(2) {
                            drawCurveFill(
                                points = measuredPoints,
                                getValue = { it.measured!! },
                                fillBrush = whoFillBrush,
                                blendMode = BlendMode.Softlight,
                                chartBottom = chartBottom,
                                minuteToX = ::minuteToX,
                                uvToY = ::uvToY,
                            )
                        }
                    }

                    drawCurve(
                        points = curve,
                        getValue = { it.forecast },
                        color = whoForecastColor.copy(alpha = 0.2f),
                        strokeWidth = 1.dp.toPx(),
                        dashed = true,
                        minuteToX = ::minuteToX,
                        uvToY = ::uvToY,
                    )
                    drawCurve(
                        points = curve,
                        getValue = { it.measured },
                        color = whoMeasuredColor,
                        strokeWidth = 2.dp.toPx(),
                        dashed = false,
                        minuteToX = ::minuteToX,
                        uvToY = ::uvToY,
                    )

                    val lastMeasured = curve.lastOrNull { (it.measured ?: -1.0) >= 0 }
                    if (lastMeasured != null) {
                        val dotX = minuteToX(lastMeasured.minutesFromMidnight)
                        val dotY = uvToY(lastMeasured.measured!!)
                        drawCircle(
                            color = whoMeasuredColor,
                            radius = 2.5.dp.toPx(),
                            center = Offset(dotX, dotY),
                        )
                    }
                }
            }

            // 6. Protection threshold: extended dashed line (drawn before labels so labels sit on top)
            val thresholdY = uvToY(PROTECTION_THRESHOLD)
            val thresholdLabelStyle = TextStyle(
                fontFamily = JostFamily,
                color = thresholdNeutral,
                fontSize = axisFontSp,
                fontWeight = FontWeight.Light,
            )
            val thresholdLabelResult = textMeasurer.measure("Protection Recommended", thresholdLabelStyle)
            val thresholdLabelW = thresholdLabelResult.size.width.toFloat()
            val thresholdLabelH = thresholdLabelResult.size.height.toFloat()

            // Pivot placed so the text sits entirely outside the plot area.
            // After -90° rotation around (pivotX, pivotY) with topLeft=(pivotX,pivotY):
            //   the left edge of the rendered text maps to x = pivotX
            //   the right edge maps to x = pivotX + textH
            // We want left edge = chartRight, so pivotX = chartRight.
            // The text is allowed to extend rightward off-canvas into the margin.
            val pivotX = chartRight
            val pivotY = thresholdY - 3.dp.toPx()

            // Line extends from chartLeft to just past chartRight (under the text)
            drawLine(
                color = thresholdNeutral,
                start = Offset(chartLeft, thresholdY),
                end   = Offset(pivotX + thresholdLabelH, thresholdY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 10f)),
            )

            // Band labels drawn after the threshold line so they render on top
            val bandLabelStyle2 = TextStyle(
                fontFamily = JostFamily,
                fontSize = axisFontSp,
                fontWeight = FontWeight.Light,
            )
            for (spec in WHO_BANDS) {
                val bandLowerY2 = uvToY(spec.band.lowerBound).coerceIn(chartTop, chartBottom)
                val bandUpperY2 = uvToY(spec.upperBound).coerceIn(chartTop, chartBottom)

                val labelResult2 = textMeasurer.measure(
                    spec.band.displayName + " ",
                    bandLabelStyle2.copy(color = spec.color),
                )
                val labelH2 = labelResult2.size.height.toFloat()
                val labelW2 = labelResult2.size.width.toFloat()

                val labelY2 = bandLowerY2 - labelH2 - 2.dp.toPx()

                drawText(
                    textLayoutResult = labelResult2,
                    topLeft = Offset(chartRight - labelW2, labelY2),
                )
            }

            // Threshold label text on top of everything
            if (pivotY - thresholdLabelW > shadeTopY) {
                withTransform({
                    rotate(degrees = -90f, pivot = Offset(pivotX, pivotY))
                }) {
                    drawText(
                        textLayoutResult = thresholdLabelResult,
                        topLeft = Offset(pivotX, pivotY),
                    )
                }
            }
        }

    } // end Canvas
}

private fun DrawScope.drawCurveFill(
    points: List<UvCurvePoint>,
    getValue: (UvCurvePoint) -> Double,
    fillBrush: Brush,
    chartBottom: Float,
    minuteToX: (Int) -> Float,
    uvToY: (Double) -> Float,
    blendMode: BlendMode = BlendMode.SrcOver,
) {
    if (points.isEmpty()) return
    val path = Path()
    path.moveTo(minuteToX(points.first().minutesFromMidnight), chartBottom)
    for (point in points) {
        path.lineTo(minuteToX(point.minutesFromMidnight), uvToY(getValue(point)))
    }
    path.lineTo(minuteToX(points.last().minutesFromMidnight), chartBottom)
    path.close()
    drawPath(path = path, brush = fillBrush, style = Fill, blendMode = blendMode)
}

private fun DrawScope.drawCurve(
    points: List<UvCurvePoint>,
    getValue: (UvCurvePoint) -> Double?,
    color: Color,
    strokeWidth: Float,
    dashed: Boolean,
    minuteToX: (Int) -> Float,
    uvToY: (Double) -> Float,
) {
    val pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 5f)) else null
    var prevX: Float? = null
    var prevY: Float? = null

    for (point in points) {
        val value = getValue(point) ?: continue
        if (value < 0) continue
        val x = minuteToX(point.minutesFromMidnight)
        val y = uvToY(value)
        if (prevX != null && prevY != null) {
            drawLine(
                color = color,
                start = Offset(prevX, prevY),
                end   = Offset(x, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = pathEffect,
            )
        }
        prevX = x
        prevY = y
    }
}
