package com.example.alarmclockapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarmclockapp.ui.AlarmRingingActivity
import com.example.alarmclockapp.service.RingtoneService

class AlarmReceiver : BroadcastReceiver() {

    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm broadcast received! Starting AlarmRingingActivity.")


        val ringtoneUri = intent.getStringExtra(RingtoneService.ALARM_TONE_URI)


        val serviceIntent = Intent(context, RingtoneService::class.java).apply {
            putExtra(RingtoneService.ALARM_TONE_URI, ringtoneUri)
        }
        context.startService(serviceIntent)

        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra(RingtoneService.ALARM_TONE_URI, ringtoneUri)
        }
        context.startActivity(activityIntent)
    }
}
