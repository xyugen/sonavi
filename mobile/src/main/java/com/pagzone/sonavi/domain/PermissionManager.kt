package com.pagzone.sonavi.domain

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit

object PermissionManager {
    private const val SMS_PERMISSION_REQUEST_CODE = 1001
    private const val PREF_SMS_PERMISSION_EXPLAINED = "sms_permission_explained"

    fun checkAndRequestSmsPermission(activity: ComponentActivity): Boolean {
        return when {
            hasSmsPermission(activity) -> true
            shouldShowEducationalDialog(activity) -> {
                showSmsPermissionEducationalDialog(activity)
                false
            }
            else -> {
                requestSmsPermission(activity)
                false
            }
        }
    }

    private fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowEducationalDialog(activity: ComponentActivity): Boolean {
        val prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return !prefs.getBoolean(PREF_SMS_PERMISSION_EXPLAINED, false) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
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
}