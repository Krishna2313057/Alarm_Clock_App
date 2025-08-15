package com.example.alarmclockapp.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

fun createNotificationChannel(application: Application) {

    val name = "Alarm Channel"
    val descriptionText = "Channel for alarm notifications"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel("alarm_channel", name, importance).apply {
        description = descriptionText
    }

    val notificationManager: NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}