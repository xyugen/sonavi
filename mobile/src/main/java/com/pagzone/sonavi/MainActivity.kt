package com.pagzone.sonavi

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.pagzone.sonavi.data.repository.ClientDataRepositoryImpl
import com.pagzone.sonavi.data.repository.EmergencyContactRepository
import com.pagzone.sonavi.data.repository.SoundPreferencesRepositoryImpl
import com.pagzone.sonavi.domain.EmergencyHandler
import com.pagzone.sonavi.service.AudioClassifierService
import com.pagzone.sonavi.service.SmsService
import com.pagzone.sonavi.ui.navigation.NavigationManager
import com.pagzone.sonavi.ui.screen.MainScreen
import com.pagzone.sonavi.ui.screen.OnboardingScreen
import com.pagzone.sonavi.ui.theme.SonaviTheme
import com.pagzone.sonavi.util.Constants.DataStoreKeys.PROFILE_SETTINGS
import com.pagzone.sonavi.util.ModelUtils
import com.pagzone.sonavi.viewmodel.ClientDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PROFILE_SETTINGS)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var emergencyRepository: EmergencyContactRepository

    private val clientDataViewModel: ClientDataViewModel by viewModels()

    private var pendingOnboardingComplete: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SoundPreferencesRepositoryImpl.init(this)
        ClientDataRepositoryImpl.init(this)
        AudioClassifierService.init(this)
        SmsService.init(this)
        EmergencyHandler.init(this, emergencyRepository)

        setContent {
            SonaviTheme {
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    NavigationManager.setNavController(navController)
                }
                var showOnboarding by remember { mutableStateOf(isFirstLaunch()) }

                if (showOnboarding) {
                    OnboardingScreen {
                        showOnboarding = false
                        markOnboardingComplete()
                    }
                } else {
                    MainScreen(
                        navController = navController,
                        onStartListening = ::startListening,
                        onStopListening = ::stopListening
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        startWearableActivity()
        clientDataViewModel.initializeListeners()
    }

    override fun onStop() {
        super.onStop()

        clientDataViewModel.destroyListeners()
    }

    /**
     * Cleans up navigation resources to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()

        NavigationManager.clear()
    }

    private fun startWearableActivity() {
        clientDataViewModel.startWearableActivity()
        val labels = ModelUtils.loadYamnetLabels(this)
        Log.d("MainActivity", "labels: $labels")
    }

    private fun startListening() {
        clientDataViewModel.startListening()
    }

    private fun stopListening() {
        clientDataViewModel.stopListening()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        when (requestCode) {
            1001 -> { // SMS_PERMISSION_REQUEST_CODE
                val granted = grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    showPermissionGrantedMessage()
                } else {
                    showPermissionDeniedDialog()
                }

                pendingOnboardingComplete?.invoke()
                pendingOnboardingComplete = null
            }
        }
    }

    private fun showPermissionGrantedMessage() {
        Toast.makeText(this, "Emergency SMS feature enabled", Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage(
                "Emergency SMS alerts are disabled. You can enable them later in Settings → Emergency Contacts."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun isFirstLaunch(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return !prefs.getBoolean("onboarding_complete", false)
    }

    private fun markOnboardingComplete() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit {
                putBoolean("onboarding_complete", true)
            }
    }
}