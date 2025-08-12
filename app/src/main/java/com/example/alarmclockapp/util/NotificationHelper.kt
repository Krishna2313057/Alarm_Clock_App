package com.example.alarmclockapp.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(application: Application) {
    // No 'if' check is needed since the project's minSdk is 26 or higher,
    // and Notification Channels were introduced in API 26.
    val name = "Alarm Channel"
    val descriptionText = "Channel for alarm notifications"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel("alarm_channel", name, importance).apply {
        description = descriptionText
    }
    // Register the channel with the system
    val notificationManager: NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}