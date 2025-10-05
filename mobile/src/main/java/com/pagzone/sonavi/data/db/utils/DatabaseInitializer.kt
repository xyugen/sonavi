package com.pagzone.sonavi.data.db.utils

import com.pagzone.sonavi.data.dao.SoundProfileDao
import com.pagzone.sonavi.model.SoundProfile

object DatabaseInitializer {
    suspend fun populateBuiltInSounds(dao: SoundProfileDao) {
        val builtInSounds = listOf(
            // Human vocalizations
            SoundProfile(
                name = "Speech", displayName = "Speech", isBuiltIn = true,
                yamnetIndices = listOf(0, 6, 9, 10, 11) // Speech, Shout, Groan, Growling
            ),
            SoundProfile(
                name = "Baby cry", displayName = "Baby cry", isBuiltIn = true,
                yamnetIndices = listOf(19)
            ),

            // Animals
            SoundProfile(
                name = "Dog", displayName = "Dog", isBuiltIn = true,
                yamnetIndices = listOf(69, 71, 74) // Dog, Dog bark, Dog growl
            ),

            // Vehicles & traffic
            SoundProfile(
                name = "Bicycle bell", displayName = "Bicycle bell", isBuiltIn = true,
                yamnetIndices = listOf(198)
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
                yamnetIndices = listOf(301, 306, 307) // Car, Skidding, Idk
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
                yamnetIndices = listOf(281, 420, 429, 430) // Thunder, Explosion, Eruption, Boom
            ),
            SoundProfile(
                name = "Gunshot", displayName = "Gunshot", isBuiltIn = true,
                yamnetIndices = listOf(421, 422, 423, 424, 425)
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
                yamnetIndices = listOf(313, 475) // Reversing beep, Beep
            )
        )

        builtInSounds.forEach { sound ->
            dao.insertProfile(sound)
        }
    }
}