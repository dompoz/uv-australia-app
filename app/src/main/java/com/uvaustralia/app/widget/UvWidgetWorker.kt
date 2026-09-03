package com.uvaustralia.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.uvaustralia.app.data.ArpansaCurveParser
import com.uvaustralia.app.data.ArpansaRetrofit
import com.uvaustralia.app.data.ArpansaXmlParser
import com.uvaustralia.app.domain.ALL_STATIONS
import com.uvaustralia.app.domain.computeProtectionWindow
import com.uvaustralia.app.prefs.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class UvWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs       = UserPreferences(context)
        val stationCode = prefs.stationCode.first() ?: "syd"
        val riskScheme  = prefs.riskScheme.first()

        val manager  = GlanceAppWidgetManager(context)
        val glanceIds: List<GlanceId> = manager.getGlanceIds(UvWidget::class.java)
        if (glanceIds.isEmpty()) return Result.success()

        // Mark loading so the widget can show a loading state on first run
        glanceIds.forEach { id ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { p ->
                p.toMutablePreferences().apply { set(WIDGET_KEY_LOADING, true) }
            }
            UvWidget().update(context, id)
        }

        return runCatching {
            // Resolve the station from the known list so we get its human-readable
            // display name rather than the raw code or the name string from the XML.
            val station = ALL_STATIONS.find { it.code == stationCode }
                ?: ALL_STATIONS.first()

            // Fetch live UV reading for this station.
            // The XML <name> field contains the station code (e.g. "syd"),
            // so we match directly against the station code.
            val xml      = ArpansaRetrofit.service.getLiveReadings()
            val readings = ArpansaXmlParser.parseXml(xml)
            val reading  = readings.find { it.stationCode == station.code }

            // Fetch the UV forecast curve and compute the protection window
            val dateStr        = ArpansaCurveParser.curveApiDate(LocalDate.now())
            val curveJson      = ArpansaRetrofit.service.getCurveData(station.lon, station.lat, dateStr)
            val curve          = ArpansaCurveParser.parseCurve(curveJson)
            val window         = computeProtectionWindow(curve)
            val currentMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { p ->
                    p.toMutablePreferences().apply {
                        set(WIDGET_KEY_STATION_NAME, station.displayName)
                        set(WIDGET_KEY_LOADING, false)

                        if (reading != null) {
                            set(WIDGET_KEY_UV_INDEX, reading.index)
                            // Mirror the main app: store "NA" only when no reading
                            // is found; the raw API status value is not used directly.
                            set(WIDGET_KEY_STATUS, "OK")
                        } else {
                            // No reading found: clear stale UV data so we don't
                            // show old values as if they're current.
                            remove(WIDGET_KEY_UV_INDEX)
                            set(WIDGET_KEY_STATUS, "NA")
                        }

                        if (window != null) {
                            set(WIDGET_KEY_PROTECTION_START, window.startMinutes)
                            set(WIDGET_KEY_PROTECTION_END, window.endMinutes)
                        } else {
                            set(WIDGET_KEY_PROTECTION_START, -1)
                            set(WIDGET_KEY_PROTECTION_END, -1)
                        }

                        set(WIDGET_KEY_CURRENT_MINUTES, currentMinutes)
                        set(WIDGET_KEY_RISK_SCHEME, riskScheme.name)
                    }
                }
                UvWidget().update(context, id)
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                // On failure, clear the loading flag so the widget doesn't
                // spin forever — it will show whatever stale data it has.
                glanceIds.forEach { id ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { p ->
                        p.toMutablePreferences().apply { set(WIDGET_KEY_LOADING, false) }
                    }
                    UvWidget().update(context, id)
                }
                Result.retry()
            },
        )
    }

    companion object {
        private const val WORK_PERIODIC = "uv_widget_refresh_periodic"
        private const val WORK_ONE_TIME = "uv_widget_refresh_immediate"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UvWidgetWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<UvWidgetWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
