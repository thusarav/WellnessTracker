package com.example.wellnesstracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions // IMPORTANT: Ensure this import is present
import androidx.navigation.fragment.findNavController
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.Habit
import com.example.wellnesstracker.data.HydrationSettings
import com.example.wellnesstracker.data.MoodEntry
import com.example.wellnesstracker.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var tvWelcomeUser: TextView
    private lateinit var tvHabitProgress: TextView
    private lateinit var tvLatestMoodEmoji: TextView
    private lateinit var tvLatestMoodNote: TextView
    private lateinit var tvHydrationProgress: TextView
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        prefs = PreferencesManager(requireContext())

        // Initialize UI elements
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser)
        tvHabitProgress = view.findViewById(R.id.tvHabitProgress)
        tvLatestMoodEmoji = view.findViewById(R.id.tvLatestMoodEmoji)
        tvLatestMoodNote = view.findViewById(R.id.tvLatestMoodNote)
        tvHydrationProgress = view.findViewById(R.id.tvHydrationProgress)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Set up navigation buttons
        view.findViewById<Button>(R.id.btnGoToHabits).setOnClickListener {
            findNavController().navigate(R.id.habitsFragment)
        }
        view.findViewById<Button>(R.id.btnGoToMood).setOnClickListener {
            findNavController().navigate(R.id.moodFragment)
        }
        view.findViewById<Button>(R.id.btnGoToHydration).setOnClickListener {
            findNavController().navigate(R.id.hydrationFragment)
        }

        // Logout logic
        btnLogout.setOnClickListener {
            prefs.logout() // Clears the current user ID

            // --- CORRECTED NAVIGATION FOR LOGOUT (AGAIN) ---
            val navOptions = NavOptions.Builder()
                // Pop up to the ID of the LoginFragment, which is the start destination for the auth flow
                // and make it inclusive (remove LoginFragment from back stack too)
                .setPopUpTo(R.id.loginFragment, true)
                .build()

            // Navigate back to login screen with the specified NavOptions
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }

        updateDashboardUI()
        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI whenever fragment becomes visible (e.g., coming back from another tab)
        updateDashboardUI()
    }

    private fun updateDashboardUI() {
        // Update Welcome Message
        val currentUserId = prefs.getCurrentUserId()
        val users = prefs.loadUsers()
        val currentUser = users.find { it.id == currentUserId }
        tvWelcomeUser.text = getString(R.string.home_welcome_message, currentUser?.username ?: getString(R.string.hint_username))

        // Update Habit Progress
        val habits = prefs.loadHabits()
        val completedHabits = habits.count { it.isCompletedToday }
        tvHabitProgress.text = getString(R.string.habits_completion_progress, (completedHabits * 100) / (if (habits.isEmpty()) 1 else habits.size)) // Avoid division by zero

        // Update Mood Snapshot
        val moods = prefs.loadMoods()
        val latestMood = moods.firstOrNull() // Get the latest mood (most recent, due to how we add to list)
        if (latestMood != null) {
            tvLatestMoodEmoji.text = latestMood.emoji
            tvLatestMoodNote.text = if (latestMood.note.isNotEmpty()) latestMood.note else getString(R.string.mood_no_note)
        } else {
            tvLatestMoodEmoji.text = "" // Clear emoji if no mood
            tvLatestMoodNote.text = getString(R.string.home_mood_placeholder)
        }

        // Update Hydration Progress
        val hydrationSettings = prefs.getHydrationSettings()
        tvHydrationProgress.text = getString(R.string.home_hydration_progress_placeholder,
            hydrationSettings.currentIntake,
            hydrationSettings.dailyGoalGlasses,
            hydrationSettings.currentIntake * hydrationSettings.glassSize)
    }
}