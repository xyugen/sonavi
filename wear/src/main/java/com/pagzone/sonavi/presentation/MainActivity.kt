/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.pagzone.sonavi.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable.getCapabilityClient
import com.google.android.gms.wearable.Wearable.getDataClient
import com.google.android.gms.wearable.Wearable.getMessageClient
import com.pagzone.sonavi.presentation.theme.SonaviTheme

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { getDataClient(this) }
    private val messageClient by lazy { getMessageClient(this) }
    private val capabilityClient by lazy { getCapabilityClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
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

    private val capabilityListener =
        CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            val nodes = capabilityInfo.nodes
            if (nodes.isNotEmpty()) {
                Log.d(TAG, "Node connected: ${nodes.first().displayName}")
            } else {
                Log.d(TAG, "No wearable connected")
            }
        }

    companion object {
        private const val TAG = "Wear/MainActivity"

        const val WEAR_CAPABILITY = "wear"
    }
}

@Composable
fun WearApp() {
    SonaviTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
        }
    }
}