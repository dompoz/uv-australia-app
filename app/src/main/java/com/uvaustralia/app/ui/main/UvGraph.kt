package com.uvaustralia.app.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.uvaustralia.app.ui.theme.GraphForecastDark
import com.uvaustralia.app.ui.theme.GraphForecastLight
import com.uvaustralia.app.ui.theme.GraphShadeBottomDark
import com.uvaustralia.app.ui.theme.GraphShadeBottomLight
import com.uvaustralia.app.ui.theme.JostFamily
import kotlinx.coroutines.delay
import java.time.LocalTime

private val GRAPH_START_MINUTE = GRAPH_START_HOUR * 60  // 360
private val GRAPH_END_MINUTE   = GRAPH_END_HOUR * 60    // 1200
private val GRAPH_MINUTE_SPAN  = GRAPH_END_MINUTE - GRAPH_START_MINUTE

@Composable
fun UvGraph(
    curve: List<UvCurvePoint>,
    graphHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val isDark = isSystemInDarkTheme()

    val colorForecast = if (isDark) GraphForecastDark  else GraphForecastLight
    val colorMeasured = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val curveFillColor = if (isDark) Color(0xFFFFA000) else Color(0xFFFFC259)
    val protectionBandColor = if (isDark) GraphShadeBottomDark else GraphShadeBottomLight

    var nowMinutes by remember { mutableIntStateOf(LocalTime.now().let { it.hour * 60 + it.minute }) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
        }
    }

    // Axis label font size scales with graph height, clamped to a sensible range
    val axisFontSp = (graphHeight.value * 0.038f).coerceIn(9f, 13f).sp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(graphHeight)
    ) {
        val paddingLeft   = 42.dp.toPx()
        val paddingRight  = 12.dp.toPx()
        val paddingTop    = 8.dp.toPx()

        // Derive paddingBottom from the actual rotated label extent so the plot
        // area is as tall as possible without risking any x-tick being clipped.
        // At 45° rotation around the label's top-left, the vertical footprint
        // below that pivot is (textWidth + textHeight) * sin(45°).
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
            chartBottom - (uv / GRAPH_MAX_UV).toFloat() * chartHeight

        // Protection shading extents (still needed for label + threshold line positioning)
        val shadeTopY    = uvToY(GRAPH_MAX_UV)
        val shadeBottomY = uvToY(PROTECTION_THRESHOLD)

        // Protection band: UV 3–6, alpha 0.5 at UV=3 (bottom) fading to 0 at UV=6 (top)
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

        // Future region — track nowX for curve clipping reference
        val nowX = if (nowMinutes in GRAPH_START_MINUTE..GRAPH_END_MINUTE)
            minuteToX(nowMinutes) else null

        val thresholdColor = if (isDark) Color(0xEEFF9900) else Color(0xeee38826)

        // Grid lines + Y axis labels (skip 0 to avoid clash with "6 AM")
        val gridUvValues = listOf(3, 6, 9, 12, 15)
        val gridStyle = TextStyle(
            fontFamily = JostFamily,
            color = onSurfaceVariant,
            fontSize = axisFontSp,
            fontWeight = FontWeight.Light,
        )
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

        // X axis labels every 2 hours
        for (hour in GRAPH_START_HOUR..GRAPH_END_HOUR step 2) {
            val minutes = hour * 60
            val x = minuteToX(minutes)
            val label = when {
                hour == 12  -> "12 PM"
                hour < 12   -> "${hour} AM"
                else        -> "${hour - 12} PM"
            }
            val measured = textMeasurer.measure(label, xAxisStyle)
            val labelTopY = chartBottom + 4.dp.toPx()
            withTransform({
                rotate(
                    degrees = 45f,
                    pivot = Offset(x, labelTopY),
                )
            }) {
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(x, labelTopY),
                )
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
                // Fill under the measured curve with a surfaceVariant alpha gradient
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

                // Dot at the end of the measured curve
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

        // Threshold line and label drawn last so they appear on top of all other elements
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
        // Rotate -90° so text reads bottom-to-top. After rotation the text occupies
        // a vertical strip of width = labelResult.height at the right edge.
        // Pivot at top-left of the unrotated text; after -90° rotation that pivot
        // maps the text so its bottom sits at thresholdY and its right edge at chartRight.
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


    } // end Canvas
}

private fun DrawScope.drawCurveFill(
    points: List<UvCurvePoint>,
    getValue: (UvCurvePoint) -> Double,
    fillBrush: Brush,
    chartBottom: Float,
    minuteToX: (Int) -> Float,
    uvToY: (Double) -> Float,
) {
    if (points.isEmpty()) return
    val path = Path()
    path.moveTo(minuteToX(points.first().minutesFromMidnight), chartBottom)
    for (point in points) {
        path.lineTo(minuteToX(point.minutesFromMidnight), uvToY(getValue(point)))
    }
    path.lineTo(minuteToX(points.last().minutesFromMidnight), chartBottom)
    path.close()
    drawPath(path = path, brush = fillBrush, style = Fill)
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
