package com.pagzone.sonavi.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.pagzone.sonavi.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientDataViewModel :
    ViewModel(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private val _events = mutableStateListOf<Event>()

    /**
     * The list of events from the clients.
     */
    val events: List<Event> = _events

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _deviceName = MutableStateFlow<String?>(null)
    val deviceName: StateFlow<String?> = _deviceName

    private val _nodeId = MutableStateFlow<String?>(null)
    val nodeId: StateFlow<String?> = _nodeId

    private val _isListening = MutableStateFlow<Boolean>(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun setIsConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun setDeviceName(name: String) {
        _deviceName.value = name
    }

    fun setNodeId(nodeId: String) {
        _nodeId.value = nodeId
    }

    fun toggleListening(enable: Boolean) {
        if (_isListening.value == enable) return

        _isListening.value = enable

        if (enable) {
            Log.i(TAG, "Toggle listening to true")
        } else {
            Log.i(TAG, "Toggle listening to false")
        }
    }

    fun clearData() {
        _isConnected.value = false
        _deviceName.value = null
    }

    fun clearEvents() {
        _events.clear()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        _events.addAll(
            dataEvents.map { dataEvent ->
                val title = when (dataEvent.type) {
                    DataEvent.TYPE_CHANGED -> R.string.data_item_changed
                    DataEvent.TYPE_DELETED -> R.string.data_item_deleted
                    else -> R.string.data_item_unknown
                }

                Event(
                    title = title,
                    text = dataEvent.dataItem.toString()
                )
            }
        )
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        _events.add(
            Event(
                title = R.string.message_from_watch,
                text = messageEvent.toString()
            )
        )

        when (messageEvent.path) {
            START_LISTENING_PATH -> {
                Log.i(TAG, "Started listening")

                viewModelScope.launch {
                    toggleListening(true)
                }
            }

            STOP_LISTENING_PATH -> {
                Log.i(TAG, "Stopped listening")

                viewModelScope.launch {
                    toggleListening(false)
                }
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        _events.add(
            Event(
                title = R.string.capability_changed,
                text = capabilityInfo.toString()
            )
        )
    }

    companion object {
        private const val TAG = "Mobile/ClientDataViewModel"

        private const val START_LISTENING_PATH = "/start_listening"
        private const val STOP_LISTENING_PATH = "/stop_listening"
    }
}

/**
 * A data holder describing a client event.
 */
data class Event(
    @StringRes val title: Int,
    val text: String
)