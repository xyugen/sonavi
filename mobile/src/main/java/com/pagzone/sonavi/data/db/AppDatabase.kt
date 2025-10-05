package com.pagzone.sonavi.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pagzone.sonavi.data.dao.DetectionLogDao
import com.pagzone.sonavi.data.dao.EmergencyContactDao
import com.pagzone.sonavi.data.dao.SoundProfileDao
import com.pagzone.sonavi.data.dao.VibrationPatternDao
import com.pagzone.sonavi.di.Converters
import com.pagzone.sonavi.model.DetectionLog
import com.pagzone.sonavi.model.EmergencyContact
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.model.VibrationPattern

@Database(
    entities = [SoundProfile::class, VibrationPattern::class, EmergencyContact::class, DetectionLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soundProfileDao(): SoundProfileDao
    abstract fun vibrationPatternDao(): VibrationPatternDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun detectionLogDao(): DetectionLogDao
}