package com.example.alarmclockapp.data

import android.app.Application
import com.example.alarmclockapp.util.createNotificationChannel

class AlarmApplication : Application() {

    // Removed the 'init' block

    override fun onCreate() {
        super.onCreate()
        // The context is guaranteed to be non-null here
        createNotificationChannel(this)
    }

    val database by lazy { AlarmDatabase.getDatabase(this) }
    val repository by lazy { AlarmRepository(database.alarmDao()) }
}