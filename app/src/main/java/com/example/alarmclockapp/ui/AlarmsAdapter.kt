package com.example.alarmclockapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclockapp.R
import com.example.alarmclockapp.data.Alarm
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class AlarmsAdapter(
    private val onToggle: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmsAdapter.AlarmViewHolder>(AlarmDiffCallback) {

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewAlarmTime)
        private val enabledSwitch: SwitchMaterial = itemView.findViewById(R.id.switchAlarmEnabled)

        fun bind(alarm: Alarm) {
            val amPm = if (alarm.hour >= 12) "PM" else "AM"
            val hour12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
            timeTextView.text = String.format(Locale.US, "%02d:%02d %s", hour12, alarm.minute, amPm)

            enabledSwitch.setOnCheckedChangeListener(null)
            enabledSwitch.isChecked = alarm.isEnabled

            enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm.copy(isEnabled = isChecked))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_item, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = getItem(position)
        holder.bind(alarm)
    }
}

object AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
    override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem == newItem
    }
}