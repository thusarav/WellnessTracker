package com.example.wellnesstracker.data

import java.text.SimpleDateFormat
import java.util.*

data class HydrationSettings(
    val reminderEnabled: Boolean = false,
    val intervalMinutes: Int = 60,
    val dailyGoalGlasses: Int = 8,
    val glassSize: Int = 250, // ml
    var currentIntake: Int = 0,
    val lastResetDate: String = getCurrentDate()
) {
    companion object {
        private fun getCurrentDate(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }
    }

    fun getProgressPercentage(): Float {
        return (currentIntake.toFloat() / dailyGoalGlasses.toFloat()) * 100f
    }

    fun getRemainingGlasses(): Int {
        return kotlin.math.max(0, dailyGoalGlasses - currentIntake)
    }
}