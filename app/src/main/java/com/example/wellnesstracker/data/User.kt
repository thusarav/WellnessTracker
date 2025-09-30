// File: app/src/main/java/com/example/wellnesstracker/data/User.kt
package com.example.wellnesstracker.data

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val passwordHash: String // For a real app, always store a hashed password! For this project, we'll store it as-is for simplicity.
)