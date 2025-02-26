package com.example.weightsense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weightsense.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val fullCylinderWeight: Float = 0f,
    val netWeight: Float = 0f,
    val weightUnit: String = "kg"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                fullCylinderWeight = settingsRepository.getFullCylinderWeight(),
                netWeight = settingsRepository.getNetWeight(),
                weightUnit = settingsRepository.getWeightUnit()
            )
        }
    }

    fun updateFullCylinderWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(fullCylinderWeight = weight)
    }

    fun updateNetWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(netWeight = weight)
    }

    fun updateWeightUnit(unit: String) {
        _uiState.value = _uiState.value.copy(weightUnit = unit)
    }

    fun saveSettings() {
        viewModelScope.launch {
            with(settingsRepository) {
                saveFullCylinderWeight(_uiState.value.fullCylinderWeight)
                saveNetWeight(_uiState.value.netWeight)
                saveWeightUnit(_uiState.value.weightUnit)
            }
        }
    }
}

