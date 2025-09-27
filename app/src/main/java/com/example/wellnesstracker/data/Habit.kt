package com.example.wellnesstracker.data

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    var isCompletedToday: Boolean = false
)