package com.pagzone.sonavi.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pagzone.sonavi.data.repository.ClientDataRepository
import com.pagzone.sonavi.data.repository.ClientDataRepositoryImpl
import com.pagzone.sonavi.model.VibrationEffectDTO
import kotlinx.coroutines.launch

class ClientDataViewModel(
    private val repository: ClientDataRepository = ClientDataRepositoryImpl
) : ViewModel() {

    val isConnected = repository.isConnected
    val deviceName = repository.deviceName
    val nodeId = repository.nodeId
    val isListening = repository.isListening
    val events = repository.events

    fun clearEvents() = repository.clearEvents()
    fun clearData() = repository.clearData()

    fun initializeListeners() {
        viewModelScope.launch {
            repository.initializeListeners()
        }
    }

    fun destroyListeners() {
        viewModelScope.launch {
            repository.destroyListeners()
        }
    }

    fun startWearableActivity() {
        viewModelScope.launch {
            repository.startWearableActivity()
        }
    }

    fun startListening() {
        viewModelScope.launch {
            repository.startListening()
        }
    }

    fun stopListening() {
        viewModelScope.launch {
            repository.stopListening()
        }
    }

    fun toggleListening(enable: Boolean) = repository.toggleListening(enable)

    fun sendPrediction(label: String, confidence: Float, vibration: VibrationEffectDTO) {
        viewModelScope.launch {
            repository.sendPrediction(label, confidence, vibration)
        }
    }
}

/**
 * A data holder describing a client event.
 */
data class Event(
    @StringRes val title: Int,
    val text: String
)