package com.example.alarmclockapp.service

import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import com.example.alarmclockapp.R

class RingtoneService : Service() {

    companion object {
        const val ACTION_STOP = "com.example.alarmclockapp.service.ACTION_STOP"
    }

    private var ringtone: Ringtone? = null
    private val TAG = "RingtoneService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Received STOP command. Calling stopSelf().")
            stopSelf() // This will trigger onDestroy()
            return START_NOT_STICKY
        }

        val ringtoneUriString = intent?.getStringExtra("ALARM_TONE_URI")
        Log.d(TAG, "Attempting to play ringtone with URI: $ringtoneUriString")

        val ringtoneUri: Uri = if (!ringtoneUriString.isNullOrEmpty()) {
            ringtoneUriString.toUri()
        } else {
            Uri.parse("android.resource://$packageName/${R.raw.default_alarm_sound}")
        }

        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)

        if (ringtone != null) {
            Log.d(TAG, "Ringtone loaded successfully.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        } else {
            Log.e(TAG, "Failed to load ringtone. Falling back to default.")
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, defaultRingtoneUri)
            ringtone?.play()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called. Stopping ringtone.")
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone?.stop()
        }
    }
}
