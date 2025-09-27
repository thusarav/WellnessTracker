// In data/MoodEntry.kt
package com.example.wellnesstracker.data

import java.text.SimpleDateFormat
import java.util.*

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val emoji: String,
    val moodLevel: Int, // Added for charting: 1=sad, 3=neutral, 5=happy
    val note: String = ""
) {
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    companion object {
        // Helper to convert emoji to a numeric mood level for charting
        fun getMoodLevelFromEmoji(emoji: String): Int {
            return when (emoji) {
                "😔" -> 1 // Sad
                "😐" -> 3 // Neutral
                "😄", "😊" -> 5 // Happy / Very Happy
                else -> 3 // Default to neutral
            }
        }

        // --- NEW HELPER FUNCTION REQUIRED FOR CHARTING ---
        // Used by MoodFragment to format X-axis labels.
        fun getFormattedDateFromTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }
}