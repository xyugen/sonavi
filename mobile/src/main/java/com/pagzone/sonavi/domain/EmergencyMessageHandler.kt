package com.pagzone.sonavi.domain

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pagzone.sonavi.R
import com.pagzone.sonavi.data.repository.EmergencyContactRepository
import com.pagzone.sonavi.data.repository.ProfileSettingsRepository
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.model.ProfileSettings
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.service.SmsService
import com.pagzone.sonavi.util.LocationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

object EmergencyHandler {
    private lateinit var appContext: Context
    private lateinit var emergencyContactRepository: EmergencyContactRepository
    private lateinit var soundRepository: SoundRepository
    private lateinit var profileSettingsRepository: ProfileSettingsRepository

    fun init(
        context: Context,
        emergencyRepository: EmergencyContactRepository,
        soundRepository: SoundRepository,
        profileSettingsRepository: ProfileSettingsRepository
    ) {
        this.appContext = context.applicationContext
        this.emergencyContactRepository = emergencyRepository
        this.soundRepository = soundRepository
        this.profileSettingsRepository = profileSettingsRepository
    }

    suspend fun handleEmergencyEvent(sound: SoundProfile, confidence: Float) {
        val profile = profileSettingsRepository.getProfileSettings().first()

        if (!sound.isEnabled) {
            Log.d("EmergencyHandler", "Sound not enabled")
            return
        }

        // Check cooldown
        if (isInCooldown(sound)) {
            Log.d("EmergencyHandler", "Still in cooldown for ${sound.displayName}")
            return
        }

        // Show local notification
        showNotification(sound, confidence)

        // Send emergency SMS
        try {
            val contacts = emergencyContactRepository.getActiveEmergencyContacts()
            val locationHelper = LocationHelper(appContext)

            val message =
                if (profile.hasCurrentLocation && locationHelper.isLocationEnabled(appContext)) {
                    var locationText = ""

                    // Get location synchronously (wrapped in coroutine)
                    suspendCancellableCoroutine { continuation ->
                        locationHelper.getCurrentLocation { lat, lng ->
                            locationText = "\nLocation: https://maps.google.com/?q=$lat,$lng"
                            continuation.resume(Unit)
                        }
                    }

                    Log.d("EmergencyHandler", "Location: $locationText")

                    generateMessage(sound, confidence, profile) + locationText
                } else {
                    generateMessage(sound, confidence, profile)
                }

            contacts.forEach { contact ->
                SmsService.sendEmergencySms(contact.number, message)
            }

            soundRepository.updateLastEmergencyMessageSent(sound.id, Date())
        } catch (e: Exception) {
            Log.e("EmergencyHandler", "Failed to send emergency SMS", e)
        }
    }

    private fun showNotification(sound: SoundProfile, confidence: Float) {
        val notificationManager: NotificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(appContext, "critical_sounds")
            .setSmallIcon(R.drawable.ic_emergency_home_filled)
            .setContentTitle("Critical Sound Detected")
            .setContentText("${sound.displayName} (Conf: ${(confidence * 100).toInt()}%)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(sound.vibrationPattern.toLongArray())
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun isInCooldown(sound: SoundProfile): Boolean {
        val lastSent = sound.lastEmergencyMessageSent ?: return false
        val cooldownMs = sound.emergencyCooldownMinutes * 60 * 1000L
        return (System.currentTimeMillis() - lastSent.time) < cooldownMs
    }

    private fun generateMessage(
        sound: SoundProfile,
        confidence: Float,
        profile: ProfileSettings
    ): String {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val confidencePercent = (confidence * 100).toInt()
        val location = if (profile.address.isNotEmpty()) " at ${profile.address}" else ""

        return "🚨 EMERGENCY ALERT\n" +
                "${sound.displayName} detected at $timeStr ($confidencePercent% confidence)\n\n" +
                "From: ${profile.name}$location\n" +
                "Sent via Sonavi"
    }
}