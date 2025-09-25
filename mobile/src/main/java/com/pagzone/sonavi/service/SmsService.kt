package com.pagzone.sonavi.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsService {
    private lateinit var appContext: Context

    fun init(context: Context) {
        this.appContext = context.applicationContext
    }

    fun canSendSms(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED && hasDefaultSmsCapability()
    }

    suspend fun sendEmergencySms(
        phoneNumber: String,
        message: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!canSendSms()) {
                return@withContext Result.failure(
                    SecurityException("SMS permission not granted")
                )
            }

            val smsManager = appContext.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)

            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(
                    phoneNumber, null, parts, null, null
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SmsService", "Sms not sent: ${e.message}")
            Result.failure(e)
        }
    }

    private fun hasDefaultSmsCapability(): Boolean {
        return appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
}