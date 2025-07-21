package com.pagzone.sonavi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pagzone.sonavi.presentation.data.repository.WearRepository
import com.pagzone.sonavi.presentation.data.repository.WearRepositoryImpl
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WearViewModel(
    private val repository: WearRepository = WearRepositoryImpl
) : ViewModel() {
    val isConnected: StateFlow<Boolean> = repository.isConnected
    val nodeId: StateFlow<String?> = repository.nodeId
    val isListening: StateFlow<Boolean> = repository.isListening

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
}