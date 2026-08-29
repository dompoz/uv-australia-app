package com.uvaustralia.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uvaustralia.app.data.LocationRepository
import com.uvaustralia.app.data.UvRepository
import com.uvaustralia.app.domain.ALL_STATIONS
import com.uvaustralia.app.domain.ProtectionWindow
import com.uvaustralia.app.domain.Station
import com.uvaustralia.app.domain.UvCurvePoint
import com.uvaustralia.app.domain.UvReading
import com.uvaustralia.app.domain.computeProtectionWindow
import com.uvaustralia.app.domain.distanceTo
import com.uvaustralia.app.domain.nearestStation
import com.uvaustralia.app.prefs.RiskScheme
import com.uvaustralia.app.prefs.ThemePreference
import com.uvaustralia.app.prefs.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val station: Station? = null,
    val currentUvIndex: Double? = null,
    val stationStatus: String = "OK",
    val curve: List<UvCurvePoint> = emptyList(),
    val protectionWindow: ProtectionWindow? = null,
    val distanceKm: Double? = null,
    val autoLocation: Boolean = true,
    val isLoadingCurve: Boolean = false,
    val liveError: Boolean = false,
    val curveError: Boolean = false,
    val locationPermissionNeeded: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val riskScheme: RiskScheme = RiskScheme.SUNSMART,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val uvRepo = UvRepository()
    private val locationRepo = LocationRepository(app)
    private val prefs = UserPreferences(app)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var livePollingJob: Job? = null
    private var curveRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            val autoLocation = prefs.autoLocation.first()
            val theme = prefs.themePreference.first()
            val scheme = prefs.riskScheme.first()
            _uiState.update { it.copy(autoLocation = autoLocation, themePreference = theme, riskScheme = scheme) }
            if (autoLocation) {
                resolveLocationAndLoad()
            } else {
                val savedCode = prefs.stationCode.first()
                val station = ALL_STATIONS.find { it.code == savedCode } ?: ALL_STATIONS.first()
                loadForStation(station, deviceLat = null, deviceLon = null)
            }
        }
    }

    fun onAppResumed(hasLocationPermission: Boolean) {
        viewModelScope.launch {
            val autoLocation = prefs.autoLocation.first()
            if (autoLocation) {
                if (hasLocationPermission) {
                    resolveLocationAndLoad()
                } else {
                    _uiState.update { it.copy(locationPermissionNeeded = true) }
                    val savedCode = prefs.stationCode.first()
                    val station = ALL_STATIONS.find { it.code == savedCode } ?: ALL_STATIONS.first()
                    loadForStation(station, null, null)
                }
            }
        }
    }

    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(locationPermissionNeeded = false) }
        viewModelScope.launch { resolveLocationAndLoad() }
    }

    fun onLocationPermissionDenied() {
        _uiState.update { it.copy(locationPermissionNeeded = false) }
    }

    private suspend fun resolveLocationAndLoad() {
        val location = locationRepo.getCurrentLocation()
        if (location != null) {
            val station = nearestStation(location.latitude, location.longitude)
            val distance = station.distanceTo(location.latitude, location.longitude)
            prefs.saveStation(station.code)
            loadForStation(station, location.latitude, location.longitude)
            _uiState.update { it.copy(distanceKm = distance) }
        } else {
            val savedCode = prefs.stationCode.first()
            val station = ALL_STATIONS.find { it.code == savedCode } ?: ALL_STATIONS.first()
            loadForStation(station, null, null)
        }
    }

    private fun loadForStation(station: Station, deviceLat: Double?, deviceLon: Double?) {
        _uiState.update { it.copy(station = station) }
        startLivePolling()
        startCurveRefresh(station)
        if (deviceLat != null && deviceLon != null) {
            _uiState.update { it.copy(distanceKm = station.distanceTo(deviceLat, deviceLon)) }
        }
    }

    private fun startLivePolling() {
        livePollingJob?.cancel()
        livePollingJob = viewModelScope.launch {
            uvRepo.liveReadingsFlow(60_000L).collect { result ->
                result.onSuccess { readings ->
                    val stationCode = _uiState.value.station?.code ?: return@onSuccess
                    val reading = readings.find { it.stationCode == stationCode }
                    _uiState.update {
                        it.copy(
                            currentUvIndex = reading?.index,
                            stationStatus = reading?.status ?: "NA",
                            liveError = reading == null,
                        )
                    }
                }.onFailure {
                    _uiState.update { it.copy(liveError = true) }
                }
            }
        }
    }

    private fun startCurveRefresh(station: Station) {
        curveRefreshJob?.cancel()
        curveRefreshJob = viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(isLoadingCurve = true) }
                uvRepo.fetchCurve(station.lat, station.lon, LocalDate.now()).onSuccess { curve ->
                    val window = computeProtectionWindow(curve)
                    _uiState.update {
                        it.copy(
                            curve = curve,
                            protectionWindow = window,
                            isLoadingCurve = false,
                            curveError = false,
                        )
                    }
                }.onFailure {
                    _uiState.update { it.copy(isLoadingCurve = false, curveError = true) }
                }
                delay(60 * 60 * 1000L) // refresh curve hourly
            }
        }
    }

    fun selectStation(station: Station) {
        viewModelScope.launch {
            prefs.saveStation(station.code)
            _uiState.update { it.copy(station = station, distanceKm = null) }
            loadForStation(station, null, null)
        }
    }

    fun setAutoLocation(enabled: Boolean) {
        viewModelScope.launch {
            prefs.saveAutoLocation(enabled)
            _uiState.update { it.copy(autoLocation = enabled) }
            if (enabled) resolveLocationAndLoad()
        }
    }

    fun setLivePollingPaused(paused: Boolean) {
        if (paused) {
            livePollingJob?.cancel()
            livePollingJob = null
        } else {
            startLivePolling()
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            prefs.saveTheme(theme)
            _uiState.update { it.copy(themePreference = theme) }
        }
    }

    fun setRiskScheme(scheme: RiskScheme) {
        viewModelScope.launch {
            prefs.saveRiskScheme(scheme)
            _uiState.update { it.copy(riskScheme = scheme) }
        }
    }
}
