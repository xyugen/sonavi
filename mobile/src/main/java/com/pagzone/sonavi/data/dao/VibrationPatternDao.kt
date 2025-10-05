package com.pagzone.sonavi.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.pagzone.sonavi.model.VibrationPattern

@Dao
interface VibrationPatternDao {
    @Query(value = "SELECT * FROM vibration_patterns")
    suspend fun getAllPatterns(): List<VibrationPattern>

    @Insert
    suspend fun insertPattern(pattern: VibrationPattern): Long

    @Delete
    suspend fun deletePattern(pattern: VibrationPattern)
}