package com.weatherapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private val LAST_CITY_KEY = stringPreferencesKey("last_city")
        private val LAST_STATE_KEY = stringPreferencesKey("last_state")
    }

    val lastSearchedCity: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_CITY_KEY]
    }

    val lastSearchedState: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_STATE_KEY]
    }

    suspend fun saveLastSearchedCity(cityName: String, state: String?) {
        context.dataStore.edit { prefs ->
            prefs[LAST_CITY_KEY] = cityName
            if (state != null) {
                prefs[LAST_STATE_KEY] = state
            } else {
                prefs.remove(LAST_STATE_KEY)
            }
        }
    }

    suspend fun clearLastSearchedCity() {
        context.dataStore.edit { prefs ->
            prefs.remove(LAST_CITY_KEY)
            prefs.remove(LAST_STATE_KEY)
        }
    }
}
