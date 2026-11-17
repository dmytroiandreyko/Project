package com.example.myproject

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

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

class FavoritesRepository(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    val favoriteIds: Flow<Set<Int>> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[FAVORITE_IDS_KEY].orEmpty().mapNotNull { it.toIntOrNull() }.toSet() }
        .distinctUntilChanged()

    suspend fun toggle(id: Int) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS_KEY].orEmpty()
            val key = id.toString()
            prefs[FAVORITE_IDS_KEY] = if (key in current) current - key else current + key
        }
    }

    suspend fun setFavorite(id: Int, favorite: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS_KEY].orEmpty()
            val key = id.toString()
            prefs[FAVORITE_IDS_KEY] = if (favorite) current + key else current - key
        }
    }

    private companion object {
        val FAVORITE_IDS_KEY = stringSetPreferencesKey("favorite_ids")
    }
}
