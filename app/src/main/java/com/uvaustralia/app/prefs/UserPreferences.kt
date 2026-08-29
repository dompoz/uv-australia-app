package com.uvaustralia.app.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

enum class ThemePreference { SYSTEM, LIGHT, DARK }
enum class RiskScheme { SUNSMART, GLOBAL_SOLAR_UVI }

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_STATION_CODE = stringPreferencesKey("station_code")
        private val KEY_AUTO_LOCATION = booleanPreferencesKey("auto_location")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_RISK_SCHEME = stringPreferencesKey("risk_scheme")
    }

    val stationCode: Flow<String?> = context.dataStore.data.map { it[KEY_STATION_CODE] }
    val autoLocation: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_LOCATION] ?: true }
    val themePreference: Flow<ThemePreference> = context.dataStore.data.map {
        when (it[KEY_THEME]) {
            "LIGHT" -> ThemePreference.LIGHT
            "DARK"  -> ThemePreference.DARK
            else    -> ThemePreference.SYSTEM
        }
    }
    val riskScheme: Flow<RiskScheme> = context.dataStore.data.map {
        when (it[KEY_RISK_SCHEME]) {
            "GLOBAL_SOLAR_UVI" -> RiskScheme.GLOBAL_SOLAR_UVI
            else               -> RiskScheme.SUNSMART
        }
    }

    suspend fun saveStation(code: String) {
        context.dataStore.edit { it[KEY_STATION_CODE] = code }
    }

    suspend fun saveAutoLocation(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_LOCATION] = enabled }
    }

    suspend fun saveTheme(theme: ThemePreference) {
        context.dataStore.edit { it[KEY_THEME] = theme.name }
    }

    suspend fun saveRiskScheme(scheme: RiskScheme) {
        context.dataStore.edit { it[KEY_RISK_SCHEME] = scheme.name }
    }
}
