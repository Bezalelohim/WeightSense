package com.example.weightsense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weightsense.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val username: String = "",
    val notificationsEnabled: Boolean = false,
    val currentProfile: String = "Default Profile",
    val maxWeight: Float = 100f,
    val weightUnit: String = "kg",
    val theme: String = "system",
    val language: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                username = userProfileRepository.getUsername(),
                notificationsEnabled = userProfileRepository.getNotificationsEnabled(),
                currentProfile = userProfileRepository.getCurrentProfile(),
                maxWeight = userProfileRepository.getMaxWeight(),
                weightUnit = userProfileRepository.getWeightUnit(),
                theme = userProfileRepository.getTheme(),
                language = userProfileRepository.getLanguage()
            )
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updateCurrentProfile(profile: String) {
        _uiState.value = _uiState.value.copy(currentProfile = profile)
    }

    fun updateMaxWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(maxWeight = weight)
    }

    fun updateWeightUnit(unit: String) {
        _uiState.value = _uiState.value.copy(weightUnit = unit)
    }

    fun updateTheme(theme: String) {
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun updateLanguage(language: String) {
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun toggleNotifications() {
        _uiState.value = _uiState.value.copy(notificationsEnabled = !_uiState.value.notificationsEnabled)
    }

    fun saveSettings() {
        viewModelScope.launch {
            with(userProfileRepository) {
                saveUsername(_uiState.value.username)
                saveNotificationsEnabled(_uiState.value.notificationsEnabled)
                setCurrentProfile(_uiState.value.currentProfile)
                setMaxWeight(_uiState.value.maxWeight)
                setWeightUnit(_uiState.value.weightUnit)
                setTheme(_uiState.value.theme)
                setLanguage(_uiState.value.language)
            }
        }
    }
}

