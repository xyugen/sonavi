package com.pagzone.sonavi.domain

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit

object PermissionManager {
    private const val SMS_PERMISSION_REQUEST_CODE = 1001
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
    private const val READ_MEDIA_PERMISSION_REQUEST_CODE = 1003
    private const val READ_CONTACTS_PERMISSION_REQUEST_CODE = 1004
    private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1005
    private const val PREF_SMS_PERMISSION_EXPLAINED = "sms_permission_explained"

    private var notificationLauncher: ActivityResultLauncher<String>? = null
    private var audioLauncher: ActivityResultLauncher<String>? = null
    private var contactsLauncher: ActivityResultLauncher<String>? = null
    private var recordAudioLauncher: ActivityResultLauncher<String>? = null
    private var smsLauncher: ActivityResultLauncher<String>? = null

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
    }

    private var notificationCallback: ((Boolean) -> Unit)? = null
    private var audioCallback: ((Boolean) -> Unit)? = null
    private var contactsCallback: ((Boolean) -> Unit)? = null
    private var recordAudioCallback: ((Boolean) -> Unit)? = null
    private var smsCallback: ((Boolean) -> Unit)? = null

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

    private fun shouldShowEducationalDialog(activity: ComponentActivity): Boolean {
        val prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return !prefs.getBoolean(PREF_SMS_PERMISSION_EXPLAINED, false) &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.SEND_SMS
                )
    }

    private fun showSmsPermissionEducationalDialog(activity: ComponentActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Emergency SMS Feature")
            .setMessage(
                "SoundGuard can send emergency SMS messages when critical sounds are detected.\n\n" +
                        "This helps alert your emergency contacts when you might not be able to respond, " +
                        "such as during medical emergencies or safety incidents.\n\n" +
                        "Your SMS permission will only be used for emergency alerts you configure."
            )
            .setPositiveButton("Grant Permission") { _, _ ->
                markPermissionExplained(activity)
                requestSmsPermission(activity)
            }
            .setNegativeButton("Not Now") { _, _ ->
                markPermissionExplained(activity)
            }
            .setCancelable(false)
            .show()
    }

    private fun requestSmsPermission(activity: ComponentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.SEND_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    private fun markPermissionExplained(activity: ComponentActivity) {
        activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit {
                putBoolean(PREF_SMS_PERMISSION_EXPLAINED, true)
            }
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
}