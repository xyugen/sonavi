package com.pagzone.sonavi.datalayer

import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.pagzone.sonavi.data.repository.ClientDataRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.d(TAG, "onDataChanged")
        super.onDataChanged(dataEventBuffer)

        ClientDataRepositoryImpl.handleDataChange(dataEventBuffer)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived")
        super.onMessageReceived(messageEvent)

        CoroutineScope(Dispatchers.IO).launch {
            ClientDataRepositoryImpl.handleMessage(messageEvent)
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged")
        super.onCapabilityChanged(capabilityInfo)

        CoroutineScope(Dispatchers.Main).launch {
            ClientDataRepositoryImpl.handleCapability(capabilityInfo)
        }
    }

    companion object {
        private const val TAG = "DataLayerListenerService"
    }
}