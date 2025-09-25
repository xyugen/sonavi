package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.EmergencyContactRepository
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.service.SmsService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmergencyHandler {
    private lateinit var appContext: Context
    private lateinit var emergencyContactRepository: EmergencyContactRepository // You'll need this

    fun init(context: Context, repository: EmergencyContactRepository) {
        this.appContext = context.applicationContext
        this.emergencyContactRepository = repository
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

        // Get contacts and send SMS
        try {
            val contacts = emergencyContactRepository.getActiveEmergencyContacts()
            Log.d("EmergencyMessageHandler", contacts.toString())
            val message = generateMessage(sound, confidence)

            contacts.forEach { contact ->
                SmsService.sendEmergencySms(contact.number, message)
            }

            // TODO: Update last sent time
            // emergencyContactRepository.updateLastEmergencyMessageSent(sound.id, Date())

        } catch (e: Exception) {
            Log.e("EmergencyHandler", "Failed to send emergency SMS", e)
        }
    }

    private fun isInCooldown(sound: SoundProfile): Boolean {
        val lastSent = sound.lastEmergencyMessageSent ?: return false
        val cooldownMs = sound.emergencyCooldownMinutes * 60 * 1000L
        return (System.currentTimeMillis() - lastSent.time) < cooldownMs
    }

    private fun generateMessage(sound: SoundProfile, confidence: Float): String {
        return run {
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            "🚨 ALERT\n${sound.displayName} detected at $timeStr with ${(confidence * 100).toInt()}% confidence.\n\nSent with Sonavi"
        }
    }
}