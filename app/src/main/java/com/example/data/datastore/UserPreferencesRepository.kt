package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val PREFERRED_RESOLUTION = stringPreferencesKey("preferred_resolution")
        val IS_AUTO_WALLPAPER_ENABLED = booleanPreferencesKey("is_auto_wallpaper_enabled")
        val AUTO_WALLPAPER_INTERVAL_HOURS = intPreferencesKey("auto_wallpaper_interval_hours")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val IS_PREMIUM_USER = booleanPreferencesKey("is_premium_user")
        val SEARCH_HISTORY = stringPreferencesKey("search_history_list")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: true
    }

    val preferredResolution: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PREFERRED_RESOLUTION] ?: "8K"
    }

    val isAutoWallpaperEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_AUTO_WALLPAPER_ENABLED] ?: false
    }

    val autoWallpaperIntervalHours: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_WALLPAPER_INTERVAL_HOURS] ?: 6
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "ar"
    }

    val isPremiumUser: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PREMIUM_USER] ?: false
    }

    val searchHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.SEARCH_HISTORY] ?: ""
        if (raw.isBlank()) emptyList() else raw.split("|||")
    }

    suspend fun addSearchQuery(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { preferences ->
            val raw = preferences[PreferencesKeys.SEARCH_HISTORY] ?: ""
            val list = if (raw.isBlank()) mutableListOf() else raw.split("|||").toMutableList()
            list.remove(clean)
            list.add(0, clean)
            val trimmedList = list.take(15)
            preferences[PreferencesKeys.SEARCH_HISTORY] = trimmedList.joinToString("|||")
        }
    }

    suspend fun removeSearchQuery(query: String) {
        context.dataStore.edit { preferences ->
            val raw = preferences[PreferencesKeys.SEARCH_HISTORY] ?: ""
            if (raw.isNotBlank()) {
                val list = raw.split("|||").toMutableList()
                list.remove(query)
                preferences[PreferencesKeys.SEARCH_HISTORY] = list.joinToString("|||")
            }
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_HISTORY] = ""
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDarkMode(darkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = darkMode
        }
    }

    suspend fun setPreferredResolution(resolution: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_RESOLUTION] = resolution
        }
    }

    suspend fun setAutoWallpaperEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUTO_WALLPAPER_ENABLED] = enabled
        }
    }

    suspend fun setAutoWallpaperIntervalHours(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_WALLPAPER_INTERVAL_HOURS] = hours
        }
    }

    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = language
        }
    }

    suspend fun setPremiumUser(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM_USER] = isPremium
        }
    }
}
