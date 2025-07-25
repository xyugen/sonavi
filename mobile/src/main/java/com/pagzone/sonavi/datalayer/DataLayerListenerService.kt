package com.pagzone.sonavi.datalayer

import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Channel
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.pagzone.sonavi.data.repository.ClientDataRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onChannelOpened(channel: Channel) {
        Log.d(TAG, "onChannelOpened")
        super.onChannelOpened(channel)

        serviceScope.launch {
            ClientDataRepositoryImpl.handleChannelOpened(channel as ChannelClient.Channel)
        }
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.d(TAG, "onDataChanged")
        super.onDataChanged(dataEventBuffer)

        serviceScope.launch {
            ClientDataRepositoryImpl.handleDataChange(dataEventBuffer)
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived")
        super.onMessageReceived(messageEvent)

        serviceScope.launch {
            ClientDataRepositoryImpl.handleMessage(messageEvent)
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged")
        super.onCapabilityChanged(capabilityInfo)

        serviceScope.launch {
            ClientDataRepositoryImpl.handleCapability(capabilityInfo)
        }
    }

    companion object {
        private const val TAG = "DataLayerListenerService"
    }
}