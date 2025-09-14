package com.pagzone.sonavi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundViewModel @Inject constructor(
    private val repository: SoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val sounds = repository
        .getAllSounds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getAllSounds(): Flow<List<SoundProfile>> {
        return repository.getAllSounds()
    }

    suspend fun getActiveSounds(): List<SoundProfile> {
        return repository.getActiveSounds()
    }

    suspend fun getBuiltInSounds(): List<SoundProfile> {
        return repository.getBuiltInSounds()
    }

    suspend fun getCustomSounds(): List<SoundProfile> {
        return repository.getCustomSounds()
    }

    fun updateSoundProfile(
        soundId: Long,
        threshold: Float? = null,
        vibrationPattern: List<Int>? = null,
        isCritical: Boolean? = null,
        displayName: String? = null
    ) = viewModelScope.launch {
        try {
            repository.updateSoundProfile(
                soundId,
                threshold,
                vibrationPattern,
                isCritical,
                displayName
            )
            _uiState.value = _uiState.value.copy(message = "Sound profile updated successfully")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    fun updateSoundProfile(soundProfile: SoundProfile) = viewModelScope.launch {
        try {
            repository.updateSoundProfile(soundProfile.id, fullProfile = soundProfile)
            _uiState.value = _uiState.value.copy(message = "Sound profile updated successfully")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    fun toggleSoundProfile(id: Long) {
        viewModelScope.launch {
            repository.toggleSoundProfile(id)
        }
    }

    fun setSoundProfileEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundProfileEnabled(id, enabled)
        }
    }
}