package com.example.alarmclockapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.example.alarmclockapp.R
import com.example.alarmclockapp.receiver.AlarmReceiver
import java.util.Calendar

class RingtoneService : Service() {

    companion object {
        const val ACTION_STOP = "com.example.alarmclockapp.service.ACTION_STOP"
        const val ACTION_SNOOZE = "com.example.alarmclockapp.service.ACTION_SNOOZE"
        private const val SNOOZE_DURATION_MINUTES = 5
        private const val REQUEST_CODE_SNOOZE = 101
        const val ALARM_TONE_URI = "ALARM_TONE_URI"


        private var ringtone: Ringtone? = null
    }

    private val tag = "RingtoneService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand() called with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_STOP -> {
                Log.d(tag, "Received STOP command. Calling stopSelf().")
                stopAlarm()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE -> {
                Log.d(tag, "Received SNOOZE command. Stopping alarm and rescheduling.")
                stopAlarm()
                val ringtoneUriString = intent.getStringExtra(ALARM_TONE_URI)
                scheduleSnoozeAlarm(this, ringtoneUriString)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startAlarm(intent)
            }
        }
        return START_STICKY
    }

    private fun startAlarm(intent: Intent?) {

        stopAlarm()

        val ringtoneUriString = intent?.getStringExtra(ALARM_TONE_URI)
        Log.d(tag, "Attempting to play ringtone with URI: $ringtoneUriString")

        val ringtoneUri: Uri = if (!ringtoneUriString.isNullOrEmpty()) {
            ringtoneUriString.toUri()
        } else {
            Uri.parse("android.resource://$packageName/${R.raw.default_alarm_sound}")
        }

        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)

        if (ringtone != null) {
            Log.d(tag, "Ringtone loaded successfully.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        } else {
            Log.e(tag, "Failed to load ringtone. Falling back to default.")
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, defaultRingtoneUri)
            ringtone?.play()
        }
    }


    private fun stopAlarm() {
        Log.d(tag, "stopAlarm() called. Stopping ringtone.")
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone?.stop()
        }
        ringtone = null
    }

    private fun scheduleSnoozeAlarm(context: Context, ringtoneUriString: String?) {
        val alarmManager = context.getSystemService<AlarmManager>()!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e(tag, "Cannot schedule exact alarms. App needs MANAGE_SCHEDULE_EXACT_ALARM permission.")
            return
        }

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.alarmclockapp.ALARM_ACTION"
            putExtra(ALARM_TONE_URI, ringtoneUriString)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SNOOZE,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, SNOOZE_DURATION_MINUTES)
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            snoozeTime.timeInMillis,
            pendingIntent
        )
        Log.d(tag, "Snooze alarm scheduled for ${SNOOZE_DURATION_MINUTES} minutes.")
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
