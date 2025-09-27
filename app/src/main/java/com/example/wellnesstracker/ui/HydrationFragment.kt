package com.example.wellnesstracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.*
import com.example.wellnesstracker.R
import com.example.wellnesstracker.utils.PreferencesManager
import java.util.concurrent.TimeUnit

class HydrationFragment : Fragment() {

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)
        prefs = PreferencesManager(requireContext())

        val switchReminder = view.findViewById<Switch>(R.id.switchReminder)
        val etInterval = view.findViewById<EditText>(R.id.etInterval)
        val btnSave = view.findViewById<Button>(R.id.btnSaveReminder)

        // Load saved state
        val hydrationSettings = prefs.getHydrationSettings()
        switchReminder.isChecked = hydrationSettings.reminderEnabled
        etInterval.setText(hydrationSettings.intervalMinutes.toString())

        btnSave.setOnClickListener {
            val enabled = switchReminder.isChecked
            val interval = etInterval.text.toString().toIntOrNull() ?: 60

            if (enabled) {
                schedulePeriodicReminder(interval)
                triggerTestNotification()
            } else {
                cancelReminder()
            }

            prefs.saveHydrationSettings(
                hydrationSettings.copy(reminderEnabled = enabled, intervalMinutes = interval)
            )

            Toast.makeText(requireContext(), "Settings saved ✅", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun schedulePeriodicReminder(intervalMinutes: Int) {
        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "WaterReminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    private fun cancelReminder() {
        WorkManager.getInstance(requireContext())
            .cancelUniqueWork("WaterReminder")
    }

    private fun triggerTestNotification() {
        val request = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(request)
    }
}