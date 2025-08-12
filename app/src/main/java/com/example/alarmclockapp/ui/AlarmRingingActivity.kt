package com.example.alarmclockapp.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmclockapp.data.Alarm
import com.example.alarmclockapp.data.AlarmApplication
import com.example.alarmclockapp.databinding.ActivityAlarmRingingBinding
import com.example.alarmclockapp.receiver.AlarmReceiver
import com.example.alarmclockapp.service.RingtoneService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AlarmRingingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingingBinding
    private var ringtoneUri: String? = null

    // Get a reference to the repository from the application class
    private val repository by lazy { (application as AlarmApplication).repository }

    // Create the stop intent once
    private val stopServiceIntent by lazy {
        Intent(this, RingtoneService::class.java).apply {
            action = RingtoneService.ACTION_STOP
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ringtoneUri = intent.getStringExtra("ALARM_TONE_URI")

        val serviceIntent = Intent(this, RingtoneService::class.java).apply {
            putExtra("ALARM_TONE_URI", ringtoneUri)
        }
        startService(serviceIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        binding.buttonDismiss.setOnClickListener {
            startService(stopServiceIntent)
            finish()
        }

        binding.buttonSnooze.setOnClickListener {
            startService(stopServiceIntent)
            snoozeAlarm()
            finish()
        }
    }

    private fun snoozeAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes snooze

        val calendar = Calendar.getInstance().apply {
            timeInMillis = snoozeTime
        }

        // Create a new Alarm object for the snoozed alarm
        val newAlarm = Alarm(
            id = UUID.randomUUID().hashCode(), // Create a new unique ID
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            ringtoneUri = ringtoneUri,
            isEnabled = true
        )

        // Use a GlobalScope coroutine to insert the new alarm into the database.
        // This will make it appear in the app's alarm list.
        GlobalScope.launch {
            repository.insert(newAlarm)
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_TONE_URI", ringtoneUri)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            newAlarm.id, // Use the new unique ID for the PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    snoozePendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                snoozePendingIntent
            )
        }
    }
}