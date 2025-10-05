package com.pagzone.sonavi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pagzone.sonavi.data.repository.SoundPreferencesRepository
import com.pagzone.sonavi.data.repository.SoundPreferencesRepositoryImpl
import com.pagzone.sonavi.model.SoundPreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SoundPreferencesViewModel(
    private val repo: SoundPreferencesRepository = SoundPreferencesRepositoryImpl
) : ViewModel() {
    val preferencesFlow: StateFlow<List<SoundPreference>> =
        repo.getPreferencesFlow(repo.mergeMap.keys.toList())
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleSound(label: String, enabled: Boolean) {
        viewModelScope.launch {
            repo.savePreference(SoundPreference(label, enabled))
        }
    }

    fun snoozeSound(label: String, minutes: Int) {
        val until = System.currentTimeMillis() + minutes * 60 * 1000
        viewModelScope.launch {
            repo.savePreference(SoundPreference(label, enabled = false, snoozedUntil = until))
        }
    }
}
