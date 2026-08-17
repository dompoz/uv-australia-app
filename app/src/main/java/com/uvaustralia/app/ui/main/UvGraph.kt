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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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
import com.uvaustralia.app.ui.theme.GraphShadeTopDark
import com.uvaustralia.app.ui.theme.GraphShadeTopLight
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
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val isDark = isSystemInDarkTheme()

    val shadeTop    = if (isDark) GraphShadeTopDark    else GraphShadeTopLight
    val shadeBottom = if (isDark) GraphShadeBottomDark else GraphShadeBottomLight
    val colorForecast = if (isDark) GraphForecastDark  else GraphForecastLight
    val colorMeasured = MaterialTheme.colorScheme.primary
    val bgAlpha = if (isDark) 1f else 0.35f

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

        // Background fill
        drawRect(
            color = surfaceVariant.copy(alpha = bgAlpha),
            topLeft = Offset(chartLeft, chartTop),
            size = Size(chartWidth, chartHeight),
        )

        // Protection shading (UV >= 3 zone, upward gradient)
        val shadeTopY    = uvToY(GRAPH_MAX_UV)
        val shadeBottomY = uvToY(PROTECTION_THRESHOLD)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(shadeTop, shadeBottom),
                startY = shadeTopY,
                endY = shadeBottomY,
            ),
            topLeft = Offset(chartLeft, shadeTopY),
            size = Size(chartWidth, shadeBottomY - shadeTopY),
        )

        // Future region darkening — drawn before curves so they appear on top
        val nowX = if (nowMinutes in GRAPH_START_MINUTE..GRAPH_END_MINUTE)
            minuteToX(nowMinutes) else null
        if (nowX != null && nowX < chartRight) {
            drawRect(
                color = Color.Gray.copy(alpha = if (isDark) 0.30f else 0.20f),
                topLeft = Offset(nowX, chartTop),
                size = Size(chartRight - nowX, chartHeight),
            )
        }

        // "↑ Protection Recommended ↑" label centred just above the threshold line
        val thresholdColor = if (isDark) Color(0xEEFF9900) else Color(0xeee38826)
        val labelColor = thresholdColor
        val labelStyle = TextStyle(
            fontFamily = JostFamily,
            color = labelColor,
            fontSize = axisFontSp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )
        val labelText = "↑ Protection Recommended ↑"
        val labelResult = textMeasurer.measure(labelText, labelStyle)
        val labelY = shadeBottomY - labelResult.size.height - 3.dp.toPx()
        if (labelY > shadeTopY) {
            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(
                    chartLeft + (chartWidth - labelResult.size.width) / 2f,
                    labelY,
                ),
            )
        }

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

        // Threshold line at UV 3
        val thresholdY = uvToY(PROTECTION_THRESHOLD)
        drawLine(
            color = thresholdColor,
            start = Offset(chartLeft, thresholdY),
            end   = Offset(chartRight, thresholdY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
        )

        if (curve.isNotEmpty()) {
            clipRect(chartLeft, chartTop, chartRight, chartBottom) {
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


    } // end Canvas
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
