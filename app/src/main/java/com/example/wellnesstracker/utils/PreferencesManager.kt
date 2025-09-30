// File: app/src/main/java/com/example/wellnesstracker/utils/PreferencesManager.kt
package com.example.wellnesstracker.utils

import android.content.Context
import com.example.wellnesstracker.data.Habit
import com.example.wellnesstracker.data.MoodEntry
import com.example.wellnesstracker.data.HydrationSettings
import com.example.wellnesstracker.data.User // <--- NEW: Import User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS = "habits_data"
        private const val KEY_MOODS = "moods_data"
        private const val KEY_HYDRATION = "hydration_settings"
        private const val KEY_USERS = "users_list"               // <--- NEW KEY
        private const val KEY_CURRENT_USER_ID = "current_user_id" // <--- NEW KEY
    }

    // --- NEW: User Management Methods ---
    fun saveUsers(users: List<User>) {
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_USERS, json).apply()
    }

    fun loadUsers(): MutableList<User> {
        val json = prefs.getString(KEY_USERS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<User>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveCurrentUser(userId: String) {
        prefs.edit().putString(KEY_CURRENT_USER_ID, userId).apply()
    }

    fun getCurrentUserId(): String? {
        return prefs.getString(KEY_CURRENT_USER_ID, null)
    }

    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }

    // --- Existing: Habits Management ---
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- Existing: Moods Management ---
    fun saveMoods(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        prefs.edit().putString(KEY_MOODS, json).apply()
    }

    fun loadMoods(): MutableList<MoodEntry> {
        val json = prefs.getString(KEY_MOODS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- Existing: Hydration Management ---
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