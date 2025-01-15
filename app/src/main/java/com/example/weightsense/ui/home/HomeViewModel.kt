package com.example.weightsense.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weightsense.data.ble.BleManager
import com.example.weightsense.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightState(
    val currentWeight: Float = 0f,
    val maxWeight: Float = 100f
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bleManager: BleManager
) : ViewModel() {

    private val _weightState = MutableStateFlow(WeightState())
    val weightState: StateFlow<WeightState> = _weightState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    // Add connection state
    val isDeviceConnected: StateFlow<Boolean> = bleManager.isConnected

    init {
        viewModelScope.launch {
            bleManager.weightData.collect { weight ->
                weight?.let {
                    updateWeight(it)
                    _lastSyncTime.value = System.currentTimeMillis()
                }
            }
        }
    }

    fun readWeightData() {
        bleManager.readWeightData()
    }

    private fun updateWeight(weight: Float) {
        _weightState.value = _weightState.value.copy(
            currentWeight = weight
        )
    }
}
