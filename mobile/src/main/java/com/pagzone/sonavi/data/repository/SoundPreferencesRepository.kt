package com.pagzone.sonavi.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pagzone.sonavi.model.SoundPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.soundPrefsDataStore by preferencesDataStore(
    name = "sound_prefs"
)

interface SoundPreferencesRepository {
    val mergeMap: Map<String, List<Int>>

    suspend fun savePreference(pref: SoundPreference)
    fun getPreferencesFlow(labels: List<String>): Flow<List<SoundPreference>>
}

object SoundPreferencesRepositoryImpl : SoundPreferencesRepository {

    private lateinit var appContext: Context
    private lateinit var dataStore: DataStore<Preferences>

    override val mergeMap: Map<String, List<Int>> = mapOf(
        // Human vocalizations
        "Speech" to listOf(0),
        "Shout" to listOf(6, 9, 11), // Shout, Yell, Screaming
        "Children shouting" to listOf(10),
        "Baby cry" to listOf(19),
        "Groan" to listOf(33),
        "Growling" to listOf(74),
        "Caterwaul" to listOf(80),

        // Animals
        "Snake" to listOf(129),
        "Rattle" to listOf(130),

        // Vehicles & traffic
        "Bicycle bell" to listOf(198),
        "Thunder" to listOf(281),
        "Fire" to listOf(292, 293), // Fire + Crackle
        "Vehicle" to listOf(294),
        "Motorcycle (road)" to listOf(300, 320), // Road + General
        "Car" to listOf(301),
        "Vehicle horn" to listOf(302, 312), // Car horn + Truck/Air horn
        "Car alarm" to listOf(304),
        "Skidding" to listOf(306, 307), // Skidding + Tire squeal
        "Reversing beep" to listOf(313),
        "Emergency vehicle" to listOf(316),
        "Police car siren" to listOf(317),
        "Ambulance siren" to listOf(318),
        "Fire truck siren" to listOf(319),
        "Train horn" to listOf(324, 325), // Whistle + Horn

        // Household
        "Doorbell" to listOf(349),
        "Siren" to listOf(390, 391), // General + Civil defense
        "Buzzer" to listOf(392),
        "Smoke alarm" to listOf(393),
        "Fire alarm" to listOf(395),

        // Explosives / Impact
        "Explosion" to listOf(420),
        "Gunshot" to listOf(421, 422, 423, 424, 425),
        "Eruption" to listOf(429),
        "Boom" to listOf(430),
        "Crack" to listOf(434),
        "Glass breaking" to listOf(435, 437, 464), // Glass, Shatter, Breaking
        "Bang" to listOf(460),
        "Crushing" to listOf(473),
        "Beep" to listOf(475),
        "Clitter" to listOf(483),
    )

    fun init(context: Context) {
        appContext = context.applicationContext
        dataStore = appContext.soundPrefsDataStore
    }

    override suspend fun savePreference(pref: SoundPreference) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(pref.label)] =
                if (pref.enabled) "enabled"
                else if (pref.snoozedUntil != null) "snoozed:${pref.snoozedUntil}"
                else "disabled"
        }
    }

    override fun getPreferencesFlow(labels: List<String>): Flow<List<SoundPreference>> {
        return dataStore.data.map { prefs ->
            labels.map { label ->
                val raw = prefs[stringPreferencesKey(label)]
                when {
                    raw == "enabled" -> SoundPreference(label, true)
                    raw?.startsWith("snoozed:") == true -> {
                        val until = raw.split(":").last().toLongOrNull()
                        SoundPreference(label, enabled = false, snoozedUntil = until)
                    }

                    else -> SoundPreference(label, true)
                }
            }
        }
    }
}