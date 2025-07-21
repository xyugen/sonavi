package com.pagzone.sonavi.presentation.datalayer

import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import com.pagzone.sonavi.presentation.data.repository.WearRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onPeerConnected(node: Node) {
        Log.d(TAG, "onPeerConnected()")
        super.onPeerConnected(node)
    }

    override fun onPeerDisconnected(node: Node) {
        Log.d(TAG, "onPeerDisconnected()")
        super.onPeerDisconnected(node)
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.d(TAG, "onDataChanged")
        super.onDataChanged(dataEventBuffer)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived")
        super.onMessageReceived(messageEvent)

        CoroutineScope(Dispatchers.IO).launch {
            WearRepositoryImpl.handleMessage(messageEvent)
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged")
        super.onCapabilityChanged(capabilityInfo)

        CoroutineScope(Dispatchers.IO).launch {
            WearRepositoryImpl.handleCapability(capabilityInfo)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "DataLayerListenerService"
    }
}