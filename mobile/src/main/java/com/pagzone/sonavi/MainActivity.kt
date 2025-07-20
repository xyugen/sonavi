package com.pagzone.sonavi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.pagzone.sonavi.ui.navigation.NavigationManager
import com.pagzone.sonavi.ui.screen.MainScreen
import com.pagzone.sonavi.ui.theme.SonaviTheme
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Main entry point for the Sonavi mobile application.
 *
 * This activity manages the main UI and handles communication with the Wear OS companion app.
 * It coordinates the data layer, message passing, and navigation between different screens.
 *
 * @property dataClient Wearable DataClient for syncing data between devices
 * @property messageClient Wearable MessageClient for sending messages to the wearable
 * @property capabilityClient Wearable CapabilityClient for checking device capabilities
 * @property clientDataViewModel ViewModel that manages the UI state and business logic
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel: ClientDataViewModel by viewModels()

    /**
     * Called when the activity is first created.
     * Sets up the UI components and initializes the navigation controller.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this Bundle contains the data it most recently supplied in
     * [onSaveInstanceState]. Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonaviTheme {
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    NavigationManager.setNavController(navController)
                }

                MainScreen(
                    navController = navController,
                    onStartWearableActivityClick = ::startWearableActivity,
                    onStartListening = ::startListening,
                    onStopListening = ::stopListening
                )
            }
        }
    }

    /**
     * Called when the activity is becoming visible to the user.
     * Initiates the connection to the Wear OS device.
     */
    override fun onStart() {
        Log.d(TAG, "onStart()")
        super.onStart()

        startWearableActivity()
    }

    /**
     * Performs final cleanup before the activity is destroyed.
     * Cleans up navigation resources to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()

        NavigationManager.clear()
    }

    /**
     * Called when the activity will start interacting with the user.
     * Registers listeners for data, messages, and capability changes.
     */
    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            capabilityListener,
            "wear://".toUri(),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     * Unregisters all the listeners to prevent memory leaks.
     */
    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(capabilityListener)
    }

    /**
     * Checks if Google Play Services is available on the device.
     *
     * @return `true` if Google Play Services is available, `false` otherwise
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
    }

    /**
     * Initiates the connection to the Wear OS device and starts the companion activity.
     * Sends a message to the wearable to start its main activity.
     * Handles the connection asynchronously and updates the UI state accordingly.
     */
    private fun startWearableActivity() {
        lifecycleScope.launch {
            try {
                if (!isGooglePlayServicesAvailable()) {
                    Log.d(TAG, "Google Play Services not available")
                    return@launch
                }

                val nodes = capabilityClient
                    .getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .await()
                    .nodes

                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, START_ACTIVITY_PATH, null)
                            .await()
                    }
                }.awaitAll()

                val firstNode = nodes.first()
                if (firstNode.isNearby) {
                    Log.d(TAG, "Starting activity requests sent to ${firstNode.displayName}")
                    clientDataViewModel.setDeviceName(nodes.first().displayName)
                    clientDataViewModel.setNodeId(nodes.first().id)
                    clientDataViewModel.setIsConnected(true)
                }

                Log.d(
                    TAG,
                    "Starting activity requests sent successfully!"
                )
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Starting activity failed: $exception")
            }
        }
    }

    /**
     * Sends a message to the connected Wear OS device to start listening for sounds.
     * Updates the listening state in the ViewModel upon successful message delivery.
     * Logs any errors that occur during the process.
     */
    private fun startListening() {
        val nodeId = clientDataViewModel.nodeId.value
        if (nodeId == null) {
            Log.w(TAG, "NodeId is null. Cannot send start message.")
            return
        }

        lifecycleScope.launch {
            try {
                messageClient.sendMessage(
                    nodeId,
                    START_LISTENING_PATH,
                    null
                ).await()
                clientDataViewModel.toggleListening(true)
                Log.d(TAG, "Start listening message sent successfully")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send start listening message: ${e.message}")
            }
        }
    }

    /**
     * Sends a message to the connected Wear OS device to stop listening for sounds.
     * Updates the listening state in the ViewModel upon successful message delivery.
     * Logs any errors that occur during the process.
     */
    private fun stopListening() {
        val nodeId = clientDataViewModel.nodeId.value
        if (nodeId == null) {
            Log.w(TAG, "NodeId is null. Cannot send start message.")
            return
        }

        lifecycleScope.launch {
            try {
                messageClient.sendMessage(
                    nodeId,
                    STOP_LISTENING_PATH,
                    null
                ).await()
                clientDataViewModel.toggleListening(false)
                Log.d(TAG, "Stop listening message sent successfully")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send stop listening message: ${e.message}")
            }
        }
    }

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            val nodes = capabilityInfo.nodes
            if (nodes.isNotEmpty()) {
                clientDataViewModel.setDeviceName(nodes.first().displayName)
                clientDataViewModel.setIsConnected(true)
                clientDataViewModel.setNodeId(nodes.first().id)

                Log.d(TAG, "Node connected: ${nodes.first().displayName}")
            } else {
                clientDataViewModel.clearData()

                Log.d(TAG, "No wearable connected")
            }
        }

    /**
     * Companion object containing constants and static members.
     *
     * @property TAG Logging tag for this class
     * @property START_ACTIVITY_PATH Path for starting the wearable activity
     * @property START_LISTENING_PATH Path for starting the listening mode
     * @property STOP_LISTENING_PATH Path for stopping the listening mode
     */
    companion object {
        private const val TAG = "Mobile/MainActivity"

        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val START_LISTENING_PATH = "/start_listening"
        private const val STOP_LISTENING_PATH = "/stop_listening"

        private const val WEAR_CAPABILITY = "wear"
    }
}