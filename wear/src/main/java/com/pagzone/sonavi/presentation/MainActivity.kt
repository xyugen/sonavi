/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.pagzone.sonavi.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable.getCapabilityClient
import com.google.android.gms.wearable.Wearable.getDataClient
import com.google.android.gms.wearable.Wearable.getMessageClient
import com.pagzone.sonavi.presentation.navigation.NavigationManager
import com.pagzone.sonavi.presentation.navigation.WearNavGraph
import com.pagzone.sonavi.presentation.theme.SonaviTheme
import com.pagzone.sonavi.presentation.viewmodel.WearViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { getDataClient(this) }
    private val messageClient by lazy { getMessageClient(this) }
    private val capabilityClient by lazy { getCapabilityClient(this) }

    private val viewModel: WearViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            SonaviTheme {
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    NavigationManager.setNavController(navController)
                }

                WearNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    startListening = ::startListening,
                    stopListening = ::stopListening
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startWearableActivity()
    }

    override fun onDestroy() {
        super.onDestroy()

        NavigationManager.clear()
    }

    override fun onResume() {
        super.onResume()
        capabilityClient.addListener(
            capabilityListener,
            "wear://".toUri(),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        capabilityClient.removeListener(capabilityListener)
    }

    private fun startWearableActivity() {
        lifecycleScope.launch {
            try {
                val nodes = capabilityClient
                    .getCapability(MOBILE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .await()
                    .nodes

                val firstNode = nodes.first()
                if (firstNode.isNearby) {
                    viewModel.setIsConnected(true)
                    viewModel.setNodeId(firstNode.id)
                    Log.d(TAG, "Starting activity requests sent to ${firstNode.displayName}")
                } else {
                    viewModel.setIsConnected(false)
                    viewModel.clearNodeId()
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

    private fun startListening() {
        Log.i(TAG, "Start listening")
        val nodeId = viewModel.nodeId.value
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
                Log.d(TAG, "Start listening message sent successfully")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send start listening message: ${e.message}")
            }
        }
    }

    private fun stopListening() {
        Log.i(TAG, "Stop listening")
        val nodeId = viewModel.nodeId.value
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
            if (nodes.isNotEmpty() && nodes.first().isNearby) {
                viewModel.setIsConnected(true)
                viewModel.setNodeId(nodes.first().id)
                Log.d(TAG, "Node connected: ${nodes.first().displayName}")
            } else {
                viewModel.setIsConnected(false)
                viewModel.clearNodeId()
                Log.d(TAG, "No node connected")
            }
        }

    companion object {
        private const val TAG = "Wear/MainActivity"

        private const val START_LISTENING_PATH = "/start_listening"
        private const val STOP_LISTENING_PATH = "/stop_listening"

        const val MOBILE_CAPABILITY = "mobile"
    }
}