package com.pagzone.sonavi.data.repository

import com.pagzone.sonavi.data.db.AppDatabase
import com.pagzone.sonavi.model.DetectionLog
import com.pagzone.sonavi.model.EmergencyContact
import com.pagzone.sonavi.model.SoundProfile
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundRepository @Inject constructor(
    database: AppDatabase
) {

    private val soundProfileDao = database.soundProfileDao()
    private val vibrationPatternDao = database.vibrationPatternDao()
    private val emergencyContactDao = database.emergencyContactDao()
    private val detectionLogDao = database.detectionLogDao()

    // Sound Profile operations
    fun getAllSounds() = soundProfileDao.getAllProfiles()
    suspend fun getActiveSounds() = soundProfileDao.getActiveProfiles()
    suspend fun getBuiltInSounds() = soundProfileDao.getBuiltInProfiles()
    suspend fun getCustomSounds() = soundProfileDao.getCustomProfiles()

    suspend fun addCustomSound(
        name: String,
        mfccEmbedding: String,
        threshold: Float = 0.5f,
        isCritical: Boolean = false,
        vibrationPattern: List<Long> = listOf(500, 200, 100)
    ): Long {
        val profile = SoundProfile(
            name = name,
            displayName = name,
            isBuiltIn = false,
            mfccEmbedding = mfccEmbedding,
            threshold = threshold,
            isCritical = isCritical,
            vibrationPattern = vibrationPattern
        )
        return soundProfileDao.insertProfile(profile)
    }

    suspend fun addCustomSound(customSound: SoundProfile): Long {
        val profile = SoundProfile(
            name = customSound.name,
            displayName = customSound.name,
            isBuiltIn = false,
            mfccEmbedding = customSound.mfccEmbedding,
            yamnetIndices = emptyList(),
            threshold = customSound.threshold,
            isCritical = customSound.isCritical,
            vibrationPattern = customSound.vibrationPattern
        )
        return soundProfileDao.insertProfile(profile)
    }

    suspend fun updateSoundProfile(
        soundId: Long,
        threshold: Float? = null,
        vibrationPattern: List<Long>? = null,
        isCritical: Boolean? = null,
        displayName: String? = null,
        fullProfile: SoundProfile? = null
    ) {
        val profile = soundProfileDao.getProfileById(soundId)
            ?: throw IllegalArgumentException("SoundProfile not found: $soundId")

        val updatedProfile = fullProfile?.copy(updatedAt = Date())
            ?: profile.copy(
                threshold = threshold ?: profile.threshold,
                vibrationPattern = vibrationPattern ?: profile.vibrationPattern,
                isCritical = isCritical ?: profile.isCritical,
                displayName = if (!profile.isBuiltIn && displayName != null) displayName else profile.displayName,
                updatedAt = Date()
            )

        soundProfileDao.updateProfile(updatedProfile)
    }

    suspend fun toggleSoundProfile(id: Long) {
        val soundProfile = soundProfileDao.getProfileById(id) // fetch existing
        soundProfile?.let {
            val updated = it.copy(isEnabled = !it.isEnabled) // toggle
            soundProfileDao.updateProfile(updated)
        }
    }

    suspend fun setSoundProfileEnabled(id: Long, enabled: Boolean) {
        val soundProfile = soundProfileDao.getProfileById(id)
        soundProfile?.let {
            val updated = it.copy(isEnabled = enabled)
            soundProfileDao.updateProfile(updated)
        }
    }

    suspend fun updateLastEmergencyMessageSent(soundId: Long, date: Date) {
        val soundProfile = soundProfileDao.getProfileById(soundId)
        soundProfile?.let {
            val updated = it.copy(lastEmergencyMessageSent = date)
            soundProfileDao.updateProfile(updated)
        }
    }

    suspend fun updateSnoozeUntil(soundId: Long, snoozeUntil: Date) {
        val soundProfile = soundProfileDao.getProfileById(soundId)
        soundProfile?.let {
            val updated = it.copy(snoozedUntil = snoozeUntil)
            soundProfileDao.updateProfile(updated)
        }
    }

    suspend fun clearSnooze(id: Long) {
        val soundProfile = soundProfileDao.getProfileById(id)
        soundProfile?.let {
            val updated = it.copy(snoozedUntil = null)
            soundProfileDao.updateProfile(updated)
        }
    }

    suspend fun deleteCustomSound(soundId: Long) {
        val profile = soundProfileDao.getProfileById(soundId)
        if (profile != null && !profile.isBuiltIn) {
            soundProfileDao.deleteProfile(profile)
        }
    }

    // Detection logging
    suspend fun logDetection(
        soundProfileId: Long,
        confidence: Float,
        wasEmergency: Boolean = false
    ) {
        val log = DetectionLog(
            soundProfileId = soundProfileId,
            confidence = confidence,
            wasEmergencyTriggered = wasEmergency
        )
        detectionLogDao.insertLog(log)
        soundProfileDao.updateLastDetected(soundProfileId, Date())
    }

    // Emergency contacts
    suspend fun getEmergencyContacts() = emergencyContactDao.getActiveContacts()

    suspend fun addEmergencyContact(name: String, phoneNumber: String) {
        val contact = EmergencyContact(
            name = name,
            number = phoneNumber
        )
        emergencyContactDao.insert(contact)
    }
}
