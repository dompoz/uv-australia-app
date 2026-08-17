package com.uvaustralia.app.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_STATION_CODE = stringPreferencesKey("station_code")
        private val KEY_AUTO_LOCATION = booleanPreferencesKey("auto_location")
    }

    val stationCode: Flow<String?> = context.dataStore.data.map { it[KEY_STATION_CODE] }
    val autoLocation: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_LOCATION] ?: true }

    suspend fun saveStation(code: String) {
        context.dataStore.edit { it[KEY_STATION_CODE] = code }
    }

    suspend fun saveAutoLocation(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_LOCATION] = enabled }
    }
}
