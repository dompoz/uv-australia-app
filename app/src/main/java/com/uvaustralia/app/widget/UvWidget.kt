package com.uvaustralia.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
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
import com.uvaustralia.app.MainActivity
import com.uvaustralia.app.prefs.RiskScheme
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

// Keys written by UvWidgetWorker
val WIDGET_KEY_UV_INDEX         = doublePreferencesKey("widget_uv_index")
val WIDGET_KEY_STATION_NAME     = stringPreferencesKey("widget_station_name")
val WIDGET_KEY_STATUS           = stringPreferencesKey("widget_status")
val WIDGET_KEY_LOADING          = booleanPreferencesKey("widget_loading")
val WIDGET_KEY_PROTECTION_START = intPreferencesKey("widget_protection_start")
val WIDGET_KEY_PROTECTION_END   = intPreferencesKey("widget_protection_end")
val WIDGET_KEY_CURRENT_MINUTES  = intPreferencesKey("widget_current_minutes")
val WIDGET_KEY_RISK_SCHEME      = stringPreferencesKey("widget_risk_scheme")

private fun dn(day: Color, night: Color): androidx.glance.unit.ColorProvider =
    ColorProvider(day = day, night = night)

private data class UvBand(
    val text:      androidx.glance.unit.ColorProvider,
    val label:     androidx.glance.unit.ColorProvider,
    val box:       androidx.glance.unit.ColorProvider,
    val bandName:  String = "UV Index",
)

private fun band(
    dayText: Color, nightText: Color,
    dayBox:  Color, nightBox:  Color,
    bandName: String = "UV Index",
): UvBand = UvBand(
    text     = dn(dayText,                    nightText),
    label    = dn(dayText.copy(alpha = 0.6f), nightText.copy(alpha = 0.6f)),
    box      = dn(dayBox,                     nightBox),
    bandName = bandName,
)

// SunSmart bands
private val BAND_LOW = band(
    dayText   = Color(0xFF3A2800), nightText = Color(0xFFFFEA99),
    dayBox    = Color(0xFFFFF4CC), nightBox  = Color(0xFF2A2200),
)
private val BAND_6 = band(
    dayText   = Color(0xFF4F0008), nightText = Color(0xFFFFCECA),
    dayBox    = Color(0xFFFFDFDD), nightBox  = Color(0xFF420C13),
)
private val BAND_8 = band(
    dayText   = Color(0xFF440044), nightText = Color(0xFFFFC8FD),
    dayBox    = Color(0xFFFFDFFE), nightBox  = Color(0xFF3A0738),
)
private val BAND_11 = band(
    dayText   = Color(0xFF3A0057), nightText = Color(0xFFE1BDFF),
    dayBox    = Color(0xFFF0E0FF), nightBox  = Color(0xFF310749),
)
private val BAND_EX = band(
    dayText   = Color(0xFF2D0068), nightText = Color(0xFFCAC1FF),
    dayBox    = Color(0xFFE7E4FF), nightBox  = Color(0xFF260858),
)

// Fallback used when no UV data is available
private val BAND_NONE = band(
    dayText   = Color(0xFF6B4A00), nightText = Color(0xFFCCBB77),
    dayBox    = Color(0xFFFFF4CC), nightBox  = Color(0xFF2A2200),
)

private fun sunSmartBand(uv: Double): UvBand = when {
    uv < 3  -> BAND_LOW
    uv < 6  -> BAND_6
    uv < 8  -> BAND_8
    uv < 11 -> BAND_11
    else    -> BAND_EX
}

// WHO Global Solar UVI bands
private val WHO_BAND_LOW = band(
    dayText   = WhoLightTextLow,      nightText = WhoDarkTextLow,
    dayBox    = WhoLightBoxLow,       nightBox  = WhoDarkBoxLow,
    bandName  = "Low",
)
private val WHO_BAND_MODERATE = band(
    dayText   = WhoLightTextModerate, nightText = WhoDarkTextModerate,
    dayBox    = WhoLightBoxModerate,  nightBox  = WhoDarkBoxModerate,
    bandName  = "Moderate",
)
private val WHO_BAND_HIGH = band(
    dayText   = WhoLightTextHigh,     nightText = WhoDarkTextHigh,
    dayBox    = WhoLightBoxHigh,      nightBox  = WhoDarkBoxHigh,
    bandName  = "High",
)
private val WHO_BAND_VERY_HIGH = band(
    dayText   = WhoLightTextVeryHigh, nightText = WhoDarkTextVeryHigh,
    dayBox    = WhoLightBoxVeryHigh,  nightBox  = WhoDarkBoxVeryHigh,
    bandName  = "Very High",
)
private val WHO_BAND_EXTREME = band(
    dayText   = WhoLightTextExtreme,  nightText = WhoDarkTextExtreme,
    dayBox    = WhoLightBoxExtreme,   nightBox  = WhoDarkBoxExtreme,
    bandName  = "Extreme",
)

private fun whoBand(uv: Double): UvBand = when {
    uv < 3  -> WHO_BAND_LOW
    uv < 6  -> WHO_BAND_MODERATE
    uv < 8  -> WHO_BAND_HIGH
    uv < 11 -> WHO_BAND_VERY_HIGH
    else    -> WHO_BAND_EXTREME
}

private fun formatUv(uv: Double): String =
    if (uv == uv.toLong().toDouble()) uv.toLong().toString() else "%.1f".format(uv)

class UvWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs       = currentState<Preferences>()
            val uvIndex     = prefs[WIDGET_KEY_UV_INDEX]
            val status      = prefs[WIDGET_KEY_STATUS] ?: "OK"
            val schemeStr   = prefs[WIDGET_KEY_RISK_SCHEME]
            val riskScheme  = if (schemeStr == RiskScheme.GLOBAL_SOLAR_UVI.name)
                RiskScheme.GLOBAL_SOLAR_UVI else RiskScheme.SUNSMART

            val isUnavailable = status == "NA"
            val band = when {
                isUnavailable || uvIndex == null -> BAND_NONE
                riskScheme == RiskScheme.GLOBAL_SOLAR_UVI -> whoBand(uvIndex)
                else -> sunSmartBand(uvIndex)
            }

            val numberText = if (isUnavailable || uvIndex == null) "—" else formatUv(uvIndex)
            val uvLabel = when {
                isUnavailable -> "Offline"
                riskScheme == RiskScheme.GLOBAL_SOLAR_UVI && uvIndex != null -> band.bandName
                else -> "UV Index"
            }
            val labelWeight = if (riskScheme == RiskScheme.GLOBAL_SOLAR_UVI && !isUnavailable && uvIndex != null)
                FontWeight.Medium else FontWeight.Normal

            val numFontSize   = 28.sp
            val labelFontSize = 8.sp

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(band.box)
                    .clickable(actionStartActivity(MainActivity::class.java))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = numberText,
                    style = TextStyle(
                        color = band.text,
                        fontSize = numFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                )
                Text(
                    text = uvLabel,
                    style = TextStyle(
                        color = band.label,
                        fontSize = labelFontSize,
                        fontWeight = labelWeight,
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
