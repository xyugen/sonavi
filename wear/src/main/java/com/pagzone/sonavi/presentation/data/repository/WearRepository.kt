package com.pagzone.sonavi.presentation.data.repository

import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
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
import com.pagzone.sonavi.presentation.model.SoundPrediction
import com.pagzone.sonavi.presentation.model.SoundPredictionDTO
import com.pagzone.sonavi.presentation.util.AudioStreamingService
import com.pagzone.sonavi.presentation.util.Constants.Capabilities.WEAR_CAPABILITY
import com.pagzone.sonavi.presentation.util.Constants.MessagePaths.START_LISTENING_PATH
import com.pagzone.sonavi.presentation.util.Constants.MessagePaths.STOP_LISTENING_PATH
import com.pagzone.sonavi.presentation.util.VibrationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

interface WearRepository {
    val isConnected: StateFlow<Boolean>
    val nodeId: StateFlow<String?>
    val isListening: StateFlow<Boolean>
    val soundPrediction: StateFlow<SoundPrediction?>

    fun setIsConnected(connected: Boolean)
    fun setNodeId(nodeId: String)
    fun setSoundPrediction(soundPrediction: SoundPrediction)
    fun toggleListening(enable: Boolean)
    fun clearData()
    fun clearPrediction()

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

    private lateinit var appContext: Context
    private lateinit var vibrationHelper: VibrationHelper

    //    private val channelClient by lazy { getChannelClient(appContext) }
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
    private val _soundPrediction: MutableStateFlow<SoundPrediction?> = MutableStateFlow(null)

    override val isConnected: StateFlow<Boolean> = _isConnected
    override val nodeId: StateFlow<String?> = _nodeId
    override val isListening: StateFlow<Boolean> = _isListening
    override val soundPrediction: StateFlow<SoundPrediction?> = _soundPrediction

    fun init(context: Context) {
        appContext = context.applicationContext
        vibrationHelper = VibrationHelper(appContext)
    }

    override fun setIsConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    override fun setNodeId(nodeId: String) {
        _nodeId.value = nodeId
    }

    override fun setSoundPrediction(soundPrediction: SoundPrediction) {
        _soundPrediction.value = soundPrediction
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

    override fun clearPrediction() {
        _soundPrediction.value = null
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
                    startAudioStream()
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
                    stopAudioStream()
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to send $STOP_LISTENING_PATH", it)
                }
        } ?: Log.e(TAG, "Node ID is null, cannot stop listening")
    }

    override suspend fun handleMessage(messageEvent: MessageEvent) {
        Log.d(TAG, "handleMessage: ${messageEvent.path}")

        when (messageEvent.path) {
            "/start_listening" -> startListening()
            "/stop_listening" -> stopListening()
            "/sound_detected" -> soundDetected(String(messageEvent.data))
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

    private fun startAudioStream() {
        Log.d(TAG, "Starting audio streaming service")
        val intent = Intent(appContext, AudioStreamingService::class.java).apply {
            action = AudioStreamingService.ACTION_START
            putExtra(AudioStreamingService.EXTRA_NODE_ID, nodeId.value.toString())
        }
        appContext.startService(intent)
    }

    private fun stopAudioStream() {
        Log.d(TAG, "Stopping audio streaming service")
        val intent = Intent(appContext, AudioStreamingService::class.java).apply {
            action = AudioStreamingService.ACTION_STOP
        }
        appContext.startService(intent)
    }

    private fun soundDetected(payload: String) {
        Log.d(TAG, "soundDetected")

        val decodedPayload = Json.decodeFromString<SoundPredictionDTO>(payload)
        val vibrationEffect = VibrationEffect.createWaveform(
            decodedPayload.vibration.timings.toLongArray(),
            decodedPayload.vibration.repeat
        )

        Log.d("DECODED PAYLOAD", decodedPayload.vibration.timings.toString())

        val shouldTrigger = vibrationHelper.shouldTrigger(decodedPayload.label)

        if (shouldTrigger) {
            vibrationHelper.vibrate(vibrationEffect)
            Log.d(TAG, "Vibration triggered")
        }

        setSoundPrediction(
            SoundPrediction(
                decodedPayload.label,
                decodedPayload.confidence,
                decodedPayload.isCritical
            )
        )

        Log.d(TAG, "Vibration effect: ${decodedPayload.label} | ${decodedPayload.confidence}")
    }
}

