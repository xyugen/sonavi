package com.pagzone.sonavi.domain

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.pagzone.sonavi.R
import com.pagzone.sonavi.data.repository.EmergencyContactRepository
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.service.SmsService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmergencyHandler {
    private lateinit var appContext: Context
    private lateinit var emergencyContactRepository: EmergencyContactRepository
    private lateinit var soundRepository: SoundRepository

    fun init(
        context: Context,
        emergencyRepository: EmergencyContactRepository,
        soundRepository: SoundRepository
    ) {
        this.appContext = context.applicationContext
        this.emergencyContactRepository = emergencyRepository
        this.soundRepository = soundRepository
    }

    suspend fun handleEmergencyEvent(sound: SoundProfile, confidence: Float) {
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
            Log.d("EmergencyHandler", "Contacts: $contacts")
            val message = generateMessage(sound, confidence)

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

    private fun generateMessage(sound: SoundProfile, confidence: Float): String {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return "🚨 ALERT\n${sound.displayName} detected at $timeStr with ${(confidence * 100).toInt()}% confidence.\n\nSent with Sonavi"
    }
}