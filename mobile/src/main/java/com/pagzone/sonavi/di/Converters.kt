package com.pagzone.sonavi.di

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromIntList(value: String): List<Int> {
        return try {
            value.split(",").map { it.toInt() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromListInt(list: List<Int>): String {
        return list.joinToString(",")
    }
}