package com.pagzone.sonavi.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pagzone.sonavi.data.dao.EmergencyContactDao
import com.pagzone.sonavi.model.EmergencyContact

@Database(entities = [EmergencyContact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
}