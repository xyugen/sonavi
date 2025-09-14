package com.pagzone.sonavi.data.db.utils

import com.pagzone.sonavi.data.dao.SoundProfileDao
import com.pagzone.sonavi.model.SoundProfile

object DatabaseInitializer {
    suspend fun populateBuiltInSounds(dao: SoundProfileDao) {
        val builtInSounds = listOf(
            // Human vocalizations
            SoundProfile(
                name = "Speech", displayName = "Speech", isBuiltIn = true,
                yamnetIndices = listOf(0)
            ),
            SoundProfile(
                name = "Shout", displayName = "Shout", isBuiltIn = true,
                yamnetIndices = listOf(6, 9, 10, 11)
            ),
            SoundProfile(
                name = "Baby cry", displayName = "Baby cry", isBuiltIn = true,
                yamnetIndices = listOf(19)
            ),
            SoundProfile(
                name = "Groan", displayName = "Groan", isBuiltIn = true,
                yamnetIndices = listOf(33)
            ),
            SoundProfile(
                name = "Growling", displayName = "Growling", isBuiltIn = true,
                yamnetIndices = listOf(74)
            ),

            // Animals
            SoundProfile(
                name = "Snake", displayName = "Snake", isBuiltIn = true,
                yamnetIndices = listOf(129)
            ),
            SoundProfile(
                name = "Rattle", displayName = "Rattle", isBuiltIn = true,
                yamnetIndices = listOf(130)
            ),

            // Vehicles & traffic
            SoundProfile(
                name = "Bicycle bell", displayName = "Bicycle bell", isBuiltIn = true,
                yamnetIndices = listOf(198)
            ),
            SoundProfile(
                name = "Thunder", displayName = "Thunder", isBuiltIn = true,
                yamnetIndices = listOf(281)
            ),
            SoundProfile(
                name = "Fire", displayName = "Fire", isBuiltIn = true,
                yamnetIndices = listOf(292, 293)
            ),
            SoundProfile(
                name = "Vehicle", displayName = "Vehicle", isBuiltIn = true,
                yamnetIndices = listOf(294)
            ),
            SoundProfile(
                name = "Motorcycle (road)", displayName = "Motorcycle (road)", isBuiltIn = true,
                yamnetIndices = listOf(300, 320)
            ),
            SoundProfile(
                name = "Car", displayName = "Car", isBuiltIn = true,
                yamnetIndices = listOf(301)
            ),
            SoundProfile(
                name = "Vehicle horn", displayName = "Vehicle horn", isBuiltIn = true,
                yamnetIndices = listOf(302, 312)
            ),
            SoundProfile(
                name = "Car alarm", displayName = "Car alarm", isBuiltIn = true,
                yamnetIndices = listOf(304)
            ),
            SoundProfile(
                name = "Skidding", displayName = "Skidding", isBuiltIn = true,
                yamnetIndices = listOf(306, 307)
            ),
            SoundProfile(
                name = "Reversing beep", displayName = "Reversing beep", isBuiltIn = true,
                yamnetIndices = listOf(313)
            ),
            SoundProfile(
                name = "Emergency vehicle", displayName = "Emergency vehicle", isBuiltIn = true,
                yamnetIndices = listOf(316)
            ),
            SoundProfile(
                name = "Police car siren", displayName = "Police car siren", isBuiltIn = true,
                yamnetIndices = listOf(317)
            ),
            SoundProfile(
                name = "Ambulance siren", displayName = "Ambulance siren", isBuiltIn = true,
                yamnetIndices = listOf(318)
            ),
            SoundProfile(
                name = "Fire truck siren", displayName = "Fire truck siren", isBuiltIn = true,
                yamnetIndices = listOf(319)
            ),
            SoundProfile(
                name = "Train horn", displayName = "Train horn", isBuiltIn = true,
                yamnetIndices = listOf(324, 325)
            ),

            // Household
            SoundProfile(
                name = "Doorbell", displayName = "Doorbell", isBuiltIn = true,
                yamnetIndices = listOf(349)
            ),
            SoundProfile(
                name = "Siren", displayName = "Siren", isBuiltIn = true,
                yamnetIndices = listOf(390, 391)
            ),
            SoundProfile(
                name = "Buzzer", displayName = "Buzzer", isBuiltIn = true,
                yamnetIndices = listOf(392)
            ),
            SoundProfile(
                name = "Smoke alarm", displayName = "Smoke alarm", isBuiltIn = true,
                yamnetIndices = listOf(393)
            ),
            SoundProfile(
                name = "Fire alarm", displayName = "Fire alarm", isBuiltIn = true,
                yamnetIndices = listOf(395)
            ),

            // Explosives / Impact
            SoundProfile(
                name = "Explosion", displayName = "Explosion", isBuiltIn = true,
                yamnetIndices = listOf(420)
            ),
            SoundProfile(
                name = "Gunshot", displayName = "Gunshot", isBuiltIn = true,
                yamnetIndices = listOf(421, 422, 423, 424, 425)
            ),
            SoundProfile(
                name = "Eruption", displayName = "Eruption", isBuiltIn = true,
                yamnetIndices = listOf(429)
            ),
            SoundProfile(
                name = "Boom", displayName = "Boom", isBuiltIn = true,
                yamnetIndices = listOf(430)
            ),
            SoundProfile(
                name = "Crack", displayName = "Crack", isBuiltIn = true,
                yamnetIndices = listOf(434)
            ),
            SoundProfile(
                name = "Glass breaking", displayName = "Glass breaking", isBuiltIn = true,
                yamnetIndices = listOf(435, 437, 464)
            ),
            SoundProfile(
                name = "Bang", displayName = "Bang", isBuiltIn = true,
                yamnetIndices = listOf(460)
            ),
            SoundProfile(
                name = "Crushing", displayName = "Crushing", isBuiltIn = true,
                yamnetIndices = listOf(473)
            ),
            SoundProfile(
                name = "Beep", displayName = "Beep", isBuiltIn = true,
                yamnetIndices = listOf(475)
            )
        )

        builtInSounds.forEach { sound ->
            dao.insertProfile(sound)
        }
    }
}