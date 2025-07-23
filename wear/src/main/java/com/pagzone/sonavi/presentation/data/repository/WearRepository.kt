package com.pagzone.sonavi.presentation.data.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable.getCapabilityClient
import com.google.android.gms.wearable.Wearable.getDataClient
import com.google.android.gms.wearable.Wearable.getMessageClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface WearRepository {
    val isConnected: StateFlow<Boolean>
    val nodeId: StateFlow<String?>
    val isListening: StateFlow<Boolean>

    fun setIsConnected(connected: Boolean)
    fun setNodeId(nodeId: String)
    fun toggleListening(enable: Boolean)
    fun clearData()

    fun initializeListeners()
    fun destroyListeners()
    fun startWearableActivity()
    fun startListening()
    fun stopListening()
    suspend fun handleMessage(messageEvent: MessageEvent)
    fun handleDataChange(dataEvents: DataEventBuffer)
    fun handleCapability(capabilityInfo: CapabilityInfo)
}

object WearRepositoryImpl : WearRepository {
    private const val TAG = "WearRepository"

    private const val WEAR_CAPABILITY = "wear"
    private const val START_LISTENING_PATH = "/start_listening"
    private const val STOP_LISTENING_PATH = "/stop_listening"

    private lateinit var appContext: Context

    private val dataClient by lazy { getDataClient(appContext) }
    private val messageClient by lazy { getMessageClient(appContext) }
    private val capabilityClient by lazy { getCapabilityClient(appContext) }

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            handleCapability(capabilityInfo)
        }
    private val dataListener =
        DataClient.OnDataChangedListener { dataEventBuffer ->
            handleDataChange(dataEventBuffer)
        }

    private val _isConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _nodeId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _isListening: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val isConnected: StateFlow<Boolean> = _isConnected
    override val nodeId: StateFlow<String?> = _nodeId
    override val isListening: StateFlow<Boolean> = _isListening

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    override fun setIsConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    override fun setNodeId(nodeId: String) {
        _nodeId.value = nodeId
    }

    override fun toggleListening(enable: Boolean) {
        if (_isListening.value != enable) {
            _isListening.value = enable
        }
    }

    override fun clearData() {
        _isConnected.value = false
        _nodeId.value = null
    }

    override fun initializeListeners() {
        capabilityClient.addListener(
            capabilityListener,
            "wear://".toUri(),
            CapabilityClient.FILTER_REACHABLE
        )
        dataClient.addListener(dataListener)
    }

    override fun destroyListeners() {
        capabilityClient.removeListener(capabilityListener)
        dataClient.removeListener(dataListener)
    }

    override fun startWearableActivity() {
        Log.d(TAG, "startWearableActivity")

        capabilityClient.getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { capabilityInfo ->
                handleCapability(capabilityInfo)
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to get capability info", it)
            }
    }

    override fun startListening() {
        Log.d(TAG, "startListening")

        nodeId.value?.let { id ->
            messageClient.sendMessage(id, START_LISTENING_PATH, null)
                .addOnSuccessListener {
                    Log.d(TAG, "Message sent: $START_LISTENING_PATH")
                    toggleListening(true)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to send $START_LISTENING_PATH", it)
                }
        } ?: Log.e(TAG, "Node ID is null, cannot start listening")
    }

    override fun stopListening() {
        Log.d(TAG, "stopListening")

        nodeId.value?.let { id ->
            messageClient.sendMessage(id, STOP_LISTENING_PATH, null)
                .addOnSuccessListener {
                    Log.d(TAG, "Message sent: $STOP_LISTENING_PATH")
                    toggleListening(false)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to send $STOP_LISTENING_PATH", it)
                }
        } ?: Log.e(TAG, "Node ID is null, cannot stop listening")
    }

    override suspend fun handleMessage(messageEvent: MessageEvent) {
        Log.d(TAG, "handleMessage: ${messageEvent.path}")

        when (messageEvent.path) {
            "/start_listening" -> toggleListening(true)
            "/stop_listening" -> toggleListening(false)
        }
    }

    override fun handleDataChange(dataEvents: DataEventBuffer) {
        Log.d(TAG, "handleDataChange")
    }

    override fun handleCapability(capabilityInfo: CapabilityInfo) {
        val nodes = capabilityInfo.nodes
        if (nodes.isNotEmpty() && nodes.first().isNearby) {
            setIsConnected(true)
            setNodeId(nodes.first().id)

            Log.d(TAG, "Node connected: ${nodes.first().displayName}")
        } else {
            toggleListening(false)
            clearData()

            Log.d(TAG, "No nodes connected")
        }
    }
}

