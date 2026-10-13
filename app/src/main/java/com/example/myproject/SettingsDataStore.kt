package com.example.myproject

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    val showDetails: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHOW_DETAILS_KEY] ?: DEFAULT_SHOW_DETAILS
    }

    suspend fun setShowDetails(value: Boolean) {
        dataStore.edit { prefs -> prefs[SHOW_DETAILS_KEY] = value }
    }

    companion object {
        const val DEFAULT_SHOW_DETAILS = true
        private val SHOW_DETAILS_KEY = booleanPreferencesKey("show_details")
    }
}
