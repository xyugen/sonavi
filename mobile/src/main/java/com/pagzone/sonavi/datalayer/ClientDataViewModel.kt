package com.pagzone.sonavi.datalayer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ClientDataViewModel @Inject constructor() : ViewModel() {
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus

    private val _deviceName = MutableStateFlow<String?>(null)
    val deviceName: StateFlow<String?> = _deviceName

    fun updateConnectionStatus(connected: Boolean) {
        _connectionStatus.value = connected
    }

    fun updateDeviceName(name: String) {
        _deviceName.value = name
    }
}