package com.example.weightsense.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weightsense.data.ble.BleService
import com.example.weightsense.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightState(
    val currentWeight: Float = 0f,
    val maxWeight: Float = 100f
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bleService: BleService,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _weightState = MutableStateFlow(WeightState())
    val weightState: StateFlow<WeightState> = _weightState

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime

    var currentProfile: String = "Default Profile"
        private set

    init {
        viewModelScope.launch {
            bleService.weightData.collect { weight ->
                _weightState.value = _weightState.value.copy(currentWeight = weight)
                _lastSyncTime.value = System.currentTimeMillis()
            }
        }
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            currentProfile = userProfileRepository.getCurrentProfile()
            // Load max weight from user profile or use a default value
            val maxWeight = userProfileRepository.getMaxWeight() ?: 100f
            _weightState.value = _weightState.value.copy(maxWeight = maxWeight)
        }
    }

    fun readWeightData() {
        // Implementation to read weight data from the BLE device
        bleService.readWeightData()
    }
}
