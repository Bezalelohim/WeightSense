package com.example.weightsense.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val EMPTY_CYLINDER_WEIGHT = floatPreferencesKey("empty_cylinder_weight")
        private val FULL_CYLINDER_WEIGHT = floatPreferencesKey("full_cylinder_weight")
        private val NET_WEIGHT = floatPreferencesKey("net_weight")
        private val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    }

    suspend fun getEmptyCylinderWeight(): Float = dataStore.data.first()[EMPTY_CYLINDER_WEIGHT] ?: 0f
    suspend fun getFullCylinderWeight(): Float = dataStore.data.first()[FULL_CYLINDER_WEIGHT] ?: 0f
    suspend fun getNetWeight(): Float = dataStore.data.first()[NET_WEIGHT] ?: 0f
    suspend fun getWeightUnit(): String = dataStore.data.first()[WEIGHT_UNIT] ?: "kg"

    suspend fun saveEmptyCylinderWeight(weight: Float) {
        dataStore.edit { preferences ->
            preferences[EMPTY_CYLINDER_WEIGHT] = weight
        }
    }

    suspend fun saveFullCylinderWeight(weight: Float) {
        dataStore.edit { preferences ->
            preferences[FULL_CYLINDER_WEIGHT] = weight
        }
    }

    suspend fun saveNetWeight(weight: Float) {
        dataStore.edit { preferences ->
            preferences[NET_WEIGHT] = weight
        }
    }

    suspend fun saveWeightUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[WEIGHT_UNIT] = unit
        }
    }
} 