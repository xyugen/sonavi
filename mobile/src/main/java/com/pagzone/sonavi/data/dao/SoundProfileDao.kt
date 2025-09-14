package com.pagzone.sonavi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pagzone.sonavi.model.SoundProfile
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SoundProfileDao {
    @Query("SELECT * FROM sound_profiles ORDER BY isBuiltIn DESC, name ASC")
    fun getAllProfiles(): Flow<List<SoundProfile>>

    @Query("SELECT * FROM sound_profiles WHERE isEnabled = 1")
    suspend fun getActiveProfiles(): List<SoundProfile>

    @Query("SELECT * FROM sound_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): SoundProfile?

    @Query("SELECT * FROM sound_profiles WHERE isBuiltIn = 1")
    suspend fun getBuiltInProfiles(): List<SoundProfile>

    @Query("SELECT * FROM sound_profiles WHERE isBuiltIn = 0")
    suspend fun getCustomProfiles(): List<SoundProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SoundProfile): Long

    @Update
    suspend fun updateProfile(profile: SoundProfile)

    @Delete
    suspend fun deleteProfile(profile: SoundProfile)

    @Query("UPDATE sound_profiles SET threshold = :threshold WHERE id = :id")
    suspend fun updateThreshold(id: Long, threshold: Float)

    @Query("UPDATE sound_profiles SET vibrationPattern = :pattern WHERE id = :id")
    suspend fun updateVibrationPattern(id: Long, pattern: String)

    @Query("UPDATE sound_profiles SET isCritical = :isCritical WHERE id = :id")
    suspend fun updateCriticalStatus(id: Long, isCritical: Boolean)

    @Query("UPDATE sound_profiles SET displayName = :displayName WHERE id = :id AND isBuiltIn = 0")
    suspend fun updateCustomSoundName(id: Long, displayName: String)

    @Query("UPDATE sound_profiles SET lastDetectedAt = :timestamp WHERE id = :id")
    suspend fun updateLastDetected(id: Long, timestamp: Date)
}