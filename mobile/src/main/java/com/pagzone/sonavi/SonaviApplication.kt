package com.pagzone.sonavi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SonaviApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "critical_sounds",
            "Critical Sound Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for critical sounds (e.g. alarms, sirens, baby cry)"
            enableVibration(true)
            enableLights(true)
            lightColor = Color.RED
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}