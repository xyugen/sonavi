package com.pagzone.sonavi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pagzone.sonavi.model.DetectionLog
import com.pagzone.sonavi.model.DetectionLogWithSound
import java.util.Date

@Dao
interface DetectionLogDao {
    @Insert
    suspend fun insertLog(log: DetectionLog)

    @Query("SELECT * FROM detection_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 100): List<DetectionLog>

    @Query("""
        SELECT dl.*, sp.displayName as soundName
        FROM detection_logs dl
        INNER JOIN sound_profiles sp ON dl.soundProfileId = sp.id
        WHERE dl.timestamp >= :startDate
        ORDER BY dl.timestamp DESC
    """)
    suspend fun getLogsAfterDate(startDate: Date): List<DetectionLogWithSound>

    @Query("DELETE FROM detection_logs WHERE timestamp < :beforeDate")
    suspend fun deleteOldLogs(beforeDate: Date)
}