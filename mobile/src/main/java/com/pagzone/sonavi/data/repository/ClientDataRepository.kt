package com.pagzone.sonavi.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toUri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.pagzone.sonavi.R
import com.pagzone.sonavi.util.Constants.Capabilities.WEAR_CAPABILITY
import com.pagzone.sonavi.util.Constants.MessagePaths.MIC_AUDIO_PATH
import com.pagzone.sonavi.util.Constants.MessagePaths.START_LISTENING_PATH
import com.pagzone.sonavi.util.Constants.MessagePaths.STOP_LISTENING_PATH
import com.pagzone.sonavi.util.Helper.Companion.convertPcmToWav
import com.pagzone.sonavi.viewmodel.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

interface ClientDataRepository {
    val isConnected: StateFlow<Boolean>
    val deviceName: StateFlow<String?>
    val nodeId: StateFlow<String?>
    val isListening: StateFlow<Boolean>
    val events: List<Event>

    fun setIsConnected(connected: Boolean)
    fun setDeviceName(name: String)
    fun setNodeId(nodeId: String)
    fun toggleListening(enable: Boolean)
    fun clearData()
    fun clearEvents()

    fun initializeListeners()
    fun destroyListeners()
    fun startWearableActivity()
    fun startListening()
    fun stopListening()
    suspend fun handleChannelOpened(channel: ChannelClient.Channel)
    suspend fun handleMessage(messageEvent: MessageEvent)
    fun handleDataChange(dataEvents: DataEventBuffer)
    fun handleCapability(capabilityInfo: CapabilityInfo)
}

object ClientDataRepositoryImpl : ClientDataRepository {
    private const val TAG = "ClientDataRepository"

    private lateinit var appContext: Context

    private val capabilityClient by lazy { Wearable.getCapabilityClient(appContext) }
    private val messageClient by lazy { Wearable.getMessageClient(appContext) }
    private val dataClient by lazy { Wearable.getDataClient(appContext) }
    private val channelClient by lazy { Wearable.getChannelClient(appContext) }

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            handleCapability(capabilityInfo)
        }
    private val dataListener =
        DataClient.OnDataChangedListener { dataEventBuffer ->
            handleDataChange(dataEventBuffer)
        }

    private val _events = mutableStateListOf<Event>()
    override val events: List<Event> = _events

    private val _isConnected = MutableStateFlow(false)
    override val isConnected = _isConnected

    private val _deviceName = MutableStateFlow<String?>(null)
    override val deviceName = _deviceName

    private val _nodeId = MutableStateFlow<String?>(null)
    override val nodeId = _nodeId

    private val _isListening = MutableStateFlow(false)
    override val isListening = _isListening

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    override fun setIsConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    override fun setDeviceName(name: String) {
        _deviceName.value = name
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
        _deviceName.value = null
        _nodeId.value = null
    }

    override fun clearEvents() {
        _events.clear()
    }

    override fun initializeListeners() {
        Log.d(TAG, "initializeListeners")

        capabilityClient.addListener(
            capabilityListener,
            "wear://".toUri(),
            CapabilityClient.FILTER_REACHABLE
        )
        dataClient.addListener(dataListener)

        // TODO: Could improve implementation
        channelClient.registerChannelCallback(
            object : ChannelClient.ChannelCallback() {
                override fun onChannelOpened(channel: ChannelClient.Channel) {
                    Log.d(TAG, "onChannelOpened")
                    super.onChannelOpened(channel)

                    CoroutineScope(Dispatchers.IO).launch {
                        handleChannelOpened(channel)
                    }
                }
            }
        )
    }

    override fun destroyListeners() {
        Log.d(TAG, "destroyListeners")

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
//                    toggleListening(false)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to send $STOP_LISTENING_PATH", it)
                }
        } ?: Log.e(TAG, "Node ID is null, cannot stop listening")
    }

    override suspend fun handleChannelOpened(channel: ChannelClient.Channel) {
        when (channel.path) {
            MIC_AUDIO_PATH -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val inputStream = channelClient.getInputStream(channel).await()
                    handleAudioStream(inputStream)

                    Log.d(TAG, "Audio stream closed")
                }
            }
        }
    }

    override suspend fun handleMessage(messageEvent: MessageEvent) {
        _events.add(
            Event(
                title = R.string.message_from_watch,
                text = messageEvent.toString()
            )
        )

        when (messageEvent.path) {
            "/start_listening" -> toggleListening(true)
            "/stop_listening" -> toggleListening(false)
        }
    }

    override fun handleDataChange(dataEvents: DataEventBuffer) {
        _events.addAll(dataEvents.map {
            val title = when (it.type) {
                DataEvent.TYPE_CHANGED -> R.string.data_item_changed
                DataEvent.TYPE_DELETED -> R.string.data_item_deleted
                else -> R.string.data_item_unknown
            }
            Event(title, it.dataItem.toString())
        })
    }

    override fun handleCapability(capabilityInfo: CapabilityInfo) {
        val nodes = capabilityInfo.nodes
        if (nodes.isNotEmpty() && nodes.first().isNearby) {
            setDeviceName(nodes.first().displayName)
            setIsConnected(true)
            setNodeId(nodes.first().id)

            Log.d(TAG, "Node connected: ${nodes.first().displayName}")
        } else {
            toggleListening(false)
            clearData()

            Log.d(TAG, "No wearable connected")
        }
    }

    private fun handleAudioStream(inputStream: InputStream) {
        val fileName = "streamed_audio.pcm"
        val externalDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Sonavi"
        )

        if (!externalDir.exists()) {
            externalDir.mkdirs()
        }

        val buffer = ByteArray(1024)
        val pcmFile = File(externalDir, fileName)
        val wavFile = File(externalDir, fileName.replace(".pcm", ".wav"))

        FileOutputStream(pcmFile).use { outputStream ->
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                outputStream.write(buffer, 0, read)
            }
        }

        Log.d(TAG, "Saved PCM: ${pcmFile.absolutePath}")

        // ✅ Convert PCM to WAV
        convertPcmToWav(
            pcmFile = pcmFile,
            wavFile = wavFile,
            sampleRate = 16000,   // Make sure this matches your recorder settings
            channels = 1,
            bitsPerSample = 16
        )

        Log.d(TAG, "Converted WAV: ${wavFile.absolutePath}")
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS
    }
}
