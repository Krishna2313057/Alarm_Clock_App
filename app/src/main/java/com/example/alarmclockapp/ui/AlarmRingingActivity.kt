package com.example.alarmclockapp.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmclockapp.data.AlarmApplication
import com.example.alarmclockapp.databinding.ActivityAlarmRingingBinding
import com.example.alarmclockapp.service.RingtoneService

class AlarmRingingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingingBinding
    private var ringtoneUri: String? = null

    private val repository by lazy { (application as AlarmApplication).repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ringtoneUri = intent.getStringExtra(RingtoneService.ALARM_TONE_URI)

        val serviceIntent = Intent(this, RingtoneService::class.java).apply {
            putExtra(RingtoneService.ALARM_TONE_URI, ringtoneUri)
        }
        startService(serviceIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        binding.buttonDismiss.setOnClickListener {
            val stopIntent = Intent(this, RingtoneService::class.java).apply {
                action = RingtoneService.ACTION_STOP
            }
            startService(stopIntent)
            finish()
        }

        binding.buttonSnooze.setOnClickListener {

            val snoozeIntent = Intent(this, RingtoneService::class.java).apply {
                action = RingtoneService.ACTION_SNOOZE
                putExtra(RingtoneService.ALARM_TONE_URI, ringtoneUri)
            }
            startService(snoozeIntent)
            finish()


            Toast.makeText(this, "Alarm snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
        }
    }
}
