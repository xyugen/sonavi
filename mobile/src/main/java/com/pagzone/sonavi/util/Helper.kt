package com.pagzone.sonavi.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Helper {
    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}