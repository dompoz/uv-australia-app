package com.uvaustralia.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.uvaustralia.app.MainActivity

// Keys written by UvWidgetWorker
val WIDGET_KEY_UV_INDEX         = doublePreferencesKey("widget_uv_index")
val WIDGET_KEY_STATION_NAME     = stringPreferencesKey("widget_station_name")
val WIDGET_KEY_STATUS           = stringPreferencesKey("widget_status")
val WIDGET_KEY_LOADING          = booleanPreferencesKey("widget_loading")
val WIDGET_KEY_PROTECTION_START = intPreferencesKey("widget_protection_start")
val WIDGET_KEY_PROTECTION_END   = intPreferencesKey("widget_protection_end")
val WIDGET_KEY_CURRENT_MINUTES  = intPreferencesKey("widget_current_minutes")

private fun hex(color: String): ColorProvider =
    ColorProvider(Color(android.graphics.Color.parseColor(color)))

// Dark-theme brand palette
private val LabelColor = hex("#FFCCBB77")
private val TextColor  = hex("#FFFFEA99")

// UV band colours (dark theme, matching Color.kt)
private data class UvBand(val text: ColorProvider, val box: ColorProvider)

private val BAND_LOW = UvBand(TextColor,        hex("#FF2A2200"))
private val BAND_6   = UvBand(hex("#FFFFCECA"), hex("#FF420C13"))
private val BAND_8   = UvBand(hex("#FFFFC8FD"), hex("#FF3A0738"))
private val BAND_11  = UvBand(hex("#FFE1BDFF"), hex("#FF310749"))
private val BAND_EX  = UvBand(hex("#FFCAC1FF"), hex("#FF260858"))

private fun uvBand(uv: Double): UvBand = when {
    uv < 3  -> BAND_LOW
    uv < 6  -> BAND_6
    uv < 8  -> BAND_8
    uv < 11 -> BAND_11
    else    -> BAND_EX
}

private fun formatUv(uv: Double): String =
    if (uv == uv.toLong().toDouble()) uv.toLong().toString() else "%.1f".format(uv)

// Responsive size breakpoints.
// COMPACT is served when the launcher allocates less than ~100×100dp — typical
// of a 1×1 cell on high-density or small-grid launchers. FULL is used otherwise.
private val SIZE_COMPACT = DpSize(80.dp,  80.dp)
private val SIZE_FULL    = DpSize(100.dp, 100.dp)

class UvWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(setOf(SIZE_COMPACT, SIZE_FULL))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs       = currentState<Preferences>()
            val uvIndex     = prefs[WIDGET_KEY_UV_INDEX]
            val status      = prefs[WIDGET_KEY_STATUS] ?: "OK"

            val isUnavailable = status == "NA"
            val band          = uvIndex?.let { uvBand(it) }
            val numberText    = if (isUnavailable || uvIndex == null) "—" else formatUv(uvIndex)
            val numberColor   = band?.text ?: LabelColor
            val boxColor      = band?.box ?: hex("#FF2A2200")
            val uvLabel       = if (isUnavailable) "Offline" else "UV Index"
            // Pick font sizes based on which responsive size Glance matched
            val isCompact     = LocalSize.current.width < SIZE_FULL.width
            val numFontSize   = if (isCompact) 28.sp else 36.sp
            val labelFontSize = if (isCompact) 8.sp  else 10.sp

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(boxColor)
                    .clickable(actionStartActivity(MainActivity::class.java))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = numberText,
                    style = TextStyle(
                        color = numberColor,
                        fontSize = numFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                )
                Text(
                    text = uvLabel,
                    style = TextStyle(
                        color = LabelColor,
                        fontSize = labelFontSize,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

class UvWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UvWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        UvWidgetWorker.schedule(context)
        UvWidgetWorker.runNow(context)
    }
}
