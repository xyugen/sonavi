package com.pagzone.sonavi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.pagzone.sonavi.datalayer.ClientDataViewModel
import com.pagzone.sonavi.ui.screen.MainScreen
import com.pagzone.sonavi.ui.theme.SonaviTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel: ClientDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startWearableActivity()
        setContent {
            SonaviTheme {
                MainScreen(
                    onStartWearableActivityClick = ::startWearableActivity
                )
            }
        }
    }

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

                clientDataViewModel.updateDeviceName(nodes.first().displayName)
                clientDataViewModel.updateConnectionStatus(true)

                Log.d(TAG, "Starting activity requests sent successfully! ${nodes.first().displayName}")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Starting activity failed: $exception")
            }
        }
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

    private fun isGooglePlayServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS
    }

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            val nodes = capabilityInfo.nodes
            if (nodes.isNotEmpty()) {
                clientDataViewModel.updateDeviceName(nodes.first().displayName)
                clientDataViewModel.updateConnectionStatus(true)

                Log.d(TAG, "Node connected: ${nodes.first().displayName}")
            } else {
                Log.d(TAG, "No wearable connected")
            }
        }

    companion object {
        private const val TAG = "Mobile/MainActivity"

        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val WEAR_CAPABILITY = "wear"
    }
}