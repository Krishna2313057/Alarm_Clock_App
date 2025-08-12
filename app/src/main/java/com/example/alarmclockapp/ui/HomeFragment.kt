package com.example.alarmclockapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarmclockapp.R
import com.example.alarmclockapp.data.AlarmApplication
import com.example.alarmclockapp.databinding.FragmentHomeBinding
import com.example.alarmclockapp.viewmodel.HomeViewModel
import com.example.alarmclockapp.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Find this line in HomeFragment.kt
    private val homeViewModel: HomeViewModel by activityViewModels {
        // CHANGE IT TO THIS
        HomeViewModelFactory(
            requireActivity().application,
            (requireActivity().application as AlarmApplication).repository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AlarmsAdapter { alarm ->
            homeViewModel.update(alarm)
        }
        binding.recyclerViewAlarms.adapter = adapter
        binding.recyclerViewAlarms.layoutManager = LinearLayoutManager(requireContext())

        homeViewModel.allAlarms.observe(viewLifecycleOwner) { alarms ->
            alarms?.let { adapter.submitList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.currentTime.collect { timeString ->
                binding.textViewTime.text = timeString
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.currentDate.collect { dateString ->
                binding.textViewDate.text = dateString
            }
        }

        binding.fabAddAlarm.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_setAlarmFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}