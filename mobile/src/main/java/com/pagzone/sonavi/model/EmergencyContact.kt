package com.pagzone.sonavi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pagzone.sonavi.util.Constants.RoomKeys.EMERGENCY_CONTACTS

@Entity(tableName = EMERGENCY_CONTACTS)
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val number: String,
    val isActive: Boolean = true
)
