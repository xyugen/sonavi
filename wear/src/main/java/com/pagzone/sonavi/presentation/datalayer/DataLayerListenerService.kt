package com.pagzone.sonavi.presentation.datalayer

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import com.pagzone.sonavi.presentation.navigation.NavigationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataLayerListenerService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")
    }

    override fun onPeerConnected(node: Node) {
        super.onPeerConnected(node)

        Log.i(TAG, "onPeerConnected()")
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)
    }

    override fun onPeerDisconnected(node: Node) {
        super.onPeerDisconnected(node)
        Log.i(TAG, "onPeerDisconnected()")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when (messageEvent.path) {
            START_LISTENING_PATH -> {
                Log.i(TAG, "Started listening")

                CoroutineScope(Dispatchers.Main).launch {
                    NavigationManager.navigate("listening")
                }
            }

            STOP_LISTENING_PATH -> {
                Log.i(TAG, "Stopped listening")

                CoroutineScope(Dispatchers.Main).launch {
                    NavigationManager.navigate("welcome")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "Wear/DataLayerListenerService"

        private const val START_LISTENING_PATH = "/start_listening"
        private const val STOP_LISTENING_PATH = "/stop_listening"
    }
}