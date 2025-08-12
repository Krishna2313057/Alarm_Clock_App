package com.example.alarmclockapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarmclockapp.service.RingtoneService
import com.example.alarmclockapp.ui.AlarmRingingActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Get the ringtone URI from the intent that scheduled the alarm
        val ringtoneUri = intent.getStringExtra("ALARM_TONE_URI")

        // Start the RingtoneService, passing the URI
        val serviceIntent = Intent(context, RingtoneService::class.java).apply {
            putExtra("ALARM_TONE_URI", ringtoneUri)
        }
        context.startService(serviceIntent)

        // Start the ringing activity
        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(activityIntent)
    }
}