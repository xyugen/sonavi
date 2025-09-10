package com.pagzone.sonavi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pagzone.sonavi.data.repository.ProfileSettingsRepository
import com.pagzone.sonavi.model.ProfileSettings
import com.pagzone.sonavi.model.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val profileSettingsRepository: ProfileSettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val profileSettings: StateFlow<ProfileSettings> = profileSettingsRepository
        .getProfileSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileSettings()
        )

    fun updateProfile(name: String, address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            profileSettingsRepository.updateProfile(name, address)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    // Clear success state after delay
                    delay(2000)
                    _uiState.value = _uiState.value.copy(isSuccess = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update profile"
                    )
                }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            profileSettingsRepository.updateName(name)
        }
    }

    fun updateAddress(address: String) {
        viewModelScope.launch {
            profileSettingsRepository.updateAddress(address)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearProfile() {
        viewModelScope.launch {
            profileSettingsRepository.clearProfile()
        }
    }
}