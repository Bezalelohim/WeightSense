package com.example.weightsense.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val CURRENT_PROFILE_KEY = stringPreferencesKey("current_profile")
        private val MAX_WEIGHT_KEY = floatPreferencesKey("max_weight")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val WEIGHT_UNIT_KEY = stringPreferencesKey("weight_unit")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    // Flow properties for reactive updates
    val currentProfile: Flow<String> = dataStore.data.map { preferences ->
        preferences[CURRENT_PROFILE_KEY] ?: "Default Profile"
    }

    val maxWeight: Flow<Float> = dataStore.data.map { preferences ->
        preferences[MAX_WEIGHT_KEY] ?: 100f
    }

    val username: Flow<String> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY] ?: "User"
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    val weightUnit: Flow<String> = dataStore.data.map { preferences ->
        preferences[WEIGHT_UNIT_KEY] ?: "kg"
    }

    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    // Suspend functions for one-time reads
    suspend fun getCurrentProfile(): String {
        return dataStore.data.first()[CURRENT_PROFILE_KEY] ?: "Default Profile"
    }

    suspend fun getMaxWeight(): Float {
        return dataStore.data.first()[MAX_WEIGHT_KEY] ?: 100f
    }

    suspend fun getUsername(): String {
        return dataStore.data.first()[USERNAME_KEY] ?: "User"
    }

    suspend fun getNotificationsEnabled(): Boolean {
        return dataStore.data.first()[NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    suspend fun getWeightUnit(): String {
        return dataStore.data.first()[WEIGHT_UNIT_KEY] ?: "kg"
    }

    suspend fun getTheme(): String {
        return dataStore.data.first()[THEME_KEY] ?: "system"
    }

    suspend fun getLanguage(): String {
        return dataStore.data.first()[LANGUAGE_KEY] ?: "en"
    }

    // Suspend functions for updates
    suspend fun setCurrentProfile(profile: String) {
        dataStore.edit { preferences ->
            preferences[CURRENT_PROFILE_KEY] = profile
        }
    }

    suspend fun setMaxWeight(weight: Float) {
        dataStore.edit { preferences ->
            preferences[MAX_WEIGHT_KEY] = weight
        }
    }

    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setWeightUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[WEIGHT_UNIT_KEY] = unit
        }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
