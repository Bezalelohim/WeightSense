package com.example.weightsense.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weightsense.data.ble.BleManager
import com.example.weightsense.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightState(
    val cylinderWeight: Float = 0f,     // Raw sensor reading
    val fullCylinderWeight: Float = 0f, // From settings
    val netWeight: Float = 0f,          // Pure gas capacity from settings
    val currentGasWeight: Float = 0f    // Calculated gas weight
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bleManager: BleManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _weightState = MutableStateFlow(WeightState())
    val weightState: StateFlow<WeightState> = _weightState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())

    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    val isDeviceConnected: StateFlow<Boolean> = bleManager.isConnected

    init {
        loadCylinderWeights()
        observeWeightData()
    }

    private fun loadCylinderWeights() {
        viewModelScope.launch {
            val emptyWeight = settingsRepository.getEmptyCylinderWeight()
            val fullWeight = settingsRepository.getFullCylinderWeight()
            _weightState.value = _weightState.value.copy(
                fullCylinderWeight = fullWeight,
                netWeight = fullWeight - emptyWeight
            )
        }
    }

    private fun observeWeightData() {
        viewModelScope.launch {
            bleManager.weightData.collect { sensorReading ->
                if (sensorReading != null) {
                    updateWeightCalculations(sensorReading)
                }
            }
        }
    }

    private fun updateWeightCalculations(sensorReading: Float) {
        val currentState = _weightState.value
        
        // Ensure sensor reading is valid
        val validSensorReading = sensorReading.coerceAtLeast(0f)
        
        // Calculate tare weight (empty cylinder weight)
        val tareWeight = currentState.fullCylinderWeight - currentState.netWeight
        
        // Calculate current gas weight
        val currentGasWeight = (validSensorReading - tareWeight).coerceAtLeast(0f)
        
        // Update state with new values
        _weightState.value = currentState.copy(
            cylinderWeight = validSensorReading,
            currentGasWeight = currentGasWeight
        )
        _lastSyncTime.value = System.currentTimeMillis()
    }

    fun readWeightData() {
        bleManager.readWeightData()
    }
}
