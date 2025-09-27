package com.example.wellnesstracker.utils

import android.content.Context
import com.example.wellnesstracker.data.Habit
import com.example.wellnesstracker.data.MoodEntry
import com.example.wellnesstracker.data.HydrationSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS = "habits_data"
        private const val KEY_MOODS = "moods_data"
        private const val KEY_HYDRATION = "hydration_settings"
    }

    // Habits
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type)
    }

    // Moods
    fun saveMoods(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        prefs.edit().putString(KEY_MOODS, json).apply()
    }

    fun loadMoods(): MutableList<MoodEntry> {
        val json = prefs.getString(KEY_MOODS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    // Hydration
    fun saveHydrationSettings(settings: HydrationSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString(KEY_HYDRATION, json).apply()
    }

    fun getHydrationSettings(): HydrationSettings {
        val json = prefs.getString(KEY_HYDRATION, null)
        return if (json != null) {
            gson.fromJson(json, HydrationSettings::class.java)
        } else {
            HydrationSettings()
        }
    }
}