package com.example.alarmclockapp.data

import android.app.Application
import com.example.alarmclockapp.util.createNotificationChannel

class AlarmApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel(this)
    }

    val database by lazy { AlarmDatabase.getDatabase(this) }
    val repository by lazy { AlarmRepository(database.alarmDao()) }
}