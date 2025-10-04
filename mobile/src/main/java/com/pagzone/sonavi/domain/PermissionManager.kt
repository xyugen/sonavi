package com.pagzone.sonavi.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

object PermissionManager {
    private var notificationLauncher: ActivityResultLauncher<String>? = null
    private var audioLauncher: ActivityResultLauncher<String>? = null
    private var contactsLauncher: ActivityResultLauncher<String>? = null
    private var recordAudioLauncher: ActivityResultLauncher<String>? = null
    private var smsLauncher: ActivityResultLauncher<String>? = null
    private var locationLauncher: ActivityResultLauncher<Array<String>>? = null

    fun initializeLaunchers(activity: ComponentActivity) {
        notificationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            notificationCallback?.invoke(isGranted)
        }

        audioLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            audioCallback?.invoke(isGranted)
        }

        contactsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            contactsCallback?.invoke(isGranted)
        }

        recordAudioLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            recordAudioCallback?.invoke(isGranted)
        }

        smsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            smsCallback?.invoke(isGranted)
        }

        locationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            locationCallback?.invoke(isGranted)
        }
    }

    private var notificationCallback: ((Boolean) -> Unit)? = null
    private var audioCallback: ((Boolean) -> Unit)? = null
    private var contactsCallback: ((Boolean) -> Unit)? = null
    private var recordAudioCallback: ((Boolean) -> Unit)? = null
    private var smsCallback: ((Boolean) -> Unit)? = null
    private var locationCallback: ((Boolean) -> Unit)? = null

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun hasAudioPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestNotificationPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationCallback = onResult
            notificationLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onResult(true) // Granted by default on older versions
        }
    }

    fun requestAudioPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        audioCallback = onResult
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            audioLauncher?.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            audioLauncher?.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun requestContactsPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        contactsCallback = onResult
        contactsLauncher?.launch(Manifest.permission.READ_CONTACTS)
    }

    fun requestRecordAudioPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        recordAudioCallback = onResult
        recordAudioLauncher?.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun requestSmsPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        smsCallback = onResult
        smsLauncher?.launch(Manifest.permission.SEND_SMS)
    }

    fun requestLocationPermission(activity: ComponentActivity, onResult: (Boolean) -> Unit) {
        locationCallback = onResult
        locationLauncher?.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}