package com.example.alarmclockapp.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.*
import com.example.alarmclockapp.data.Alarm
import com.example.alarmclockapp.data.AlarmRepository
import com.example.alarmclockapp.receiver.AlarmReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val application: Application, private val repository: AlarmRepository) : ViewModel() {

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms.asLiveData()

    private val _currentTime = MutableStateFlow("")
    val currentTime = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate = _currentDate.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                updateTimeAndDate()
                delay(1000)
            }
        }
    }

    private fun updateTimeAndDate() {
        val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val now = Date()
        _currentTime.value = timeFormat.format(now)
        _currentDate.value = dateFormat.format(now)
    }

    fun insert(alarm: Alarm) = viewModelScope.launch {
        repository.insert(alarm)
        scheduleAlarm(alarm)
    }

    fun update(alarm: Alarm) = viewModelScope.launch {
        repository.update(alarm)
        if (alarm.isEnabled) {
            scheduleAlarm(alarm)
        } else {
            cancelAlarm(alarm)
        }
    }

    private fun scheduleAlarm(alarm: Alarm) {
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {

            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = Uri.parse("package:" + application.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
            Log.e("HomeViewModel", "Exact alarm permission is required. Launching settings screen.")
            return
        }

        val intent = Intent(application, AlarmReceiver::class.java).apply {
            putExtra("ALARM_TONE_URI", alarm.ringtoneUri)
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            application,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d("HomeViewModel", "Alarm scheduled with ID: ${alarm.id}")
    }

    private fun cancelAlarm(alarm: Alarm) {
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(application, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("HomeViewModel", "Alarm canceled with ID: ${alarm.id}")
    }
}
