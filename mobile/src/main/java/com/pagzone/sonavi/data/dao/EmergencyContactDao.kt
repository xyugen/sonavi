package com.pagzone.sonavi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pagzone.sonavi.model.EmergencyContact
import com.pagzone.sonavi.util.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: EmergencyContact)

    @Update
    suspend fun updateEmergencyContact(contact: EmergencyContact)

    @Delete
    suspend fun delete(contact: EmergencyContact)

    @Query("SELECT * FROM ${Constants.RoomKeys.EMERGENCY_CONTACTS}")
    fun getAll(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts WHERE isActive = 1")
    suspend fun getActiveContacts(): List<EmergencyContact>
}