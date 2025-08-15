package com.example.alarmclockapp.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.alarmclockapp.data.Alarm
import com.example.alarmclockapp.data.AlarmApplication
import com.example.alarmclockapp.databinding.FragmentSetAlarmBinding
import com.example.alarmclockapp.viewmodel.HomeViewModel
import com.example.alarmclockapp.viewmodel.HomeViewModelFactory
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class SetAlarmFragment : Fragment() {

    private var _binding: FragmentSetAlarmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(
            requireActivity().application,
            (requireActivity().application as AlarmApplication).repository
        )
    }

    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var selectedRingtoneUri: Uri? = null


    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {


            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {

                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }

            selectedRingtoneUri = uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSetTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonChooseRingtone.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            ringtonePickerLauncher.launch(intent)
        }

        binding.buttonSave.setOnClickListener {
            val newAlarm = Alarm(
                hour = selectedHour,
                minute = selectedMinute,
                isEnabled = true,
                ringtoneUri = selectedRingtoneUri?.toString()
            )
            viewModel.insert(newAlarm)
            findNavController().popBackStack()
        }
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            selectedHour = picker.hour
            selectedMinute = picker.minute
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val hour12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            binding.selectedTimeText.text = String.format(Locale.US, "%02d:%02d %s", hour12, selectedMinute, amPm)
        }

        picker.show(childFragmentManager, "TIME_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}