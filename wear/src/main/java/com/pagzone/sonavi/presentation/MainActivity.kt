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
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable.getCapabilityClient
import com.google.android.gms.wearable.Wearable.getDataClient
import com.google.android.gms.wearable.Wearable.getMessageClient
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
                WearNavGraph(navController = navController, viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startWearableActivity()
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
                    Log.d(TAG, "Starting activity requests sent to ${firstNode.displayName}")
                } else {
                    viewModel.setIsConnected(false)
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

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            val nodes = capabilityInfo.nodes
            if (nodes.isNotEmpty() && nodes.first().isNearby) {
                viewModel.setIsConnected(true)
                Log.d(TAG, "Node connected: ${nodes.first().displayName}")
            } else {
                viewModel.setIsConnected(false)
                Log.d(TAG, "No node connected")
            }
        }

    companion object {
        private const val TAG = "Wear/MainActivity"

        const val MOBILE_CAPABILITY = "mobile"
    }
}