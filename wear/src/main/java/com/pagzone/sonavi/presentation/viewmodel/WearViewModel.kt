package com.pagzone.sonavi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WearViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _nodeId = MutableStateFlow<String?>(null)
    val nodeId: StateFlow<String?> = _nodeId

    fun setIsConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun setNodeId(nodeId: String) {
        _nodeId.value = nodeId
    }

    fun clearNodeId() {
        _nodeId.value = null
    }
}