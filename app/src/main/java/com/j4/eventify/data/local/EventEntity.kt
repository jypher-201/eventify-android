package com.j4.eventify.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events_table")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String?,
    val eventType: String, // Maps to your EventTypeRegistry
    val timestamp: Long,
    val endTimestamp: Long? = null,

    // New: Location Data
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,

    // New: Notification Preference
    val remindBeforeMinutes: Int?, // e.g., 15 mins, 60 mins, or 1440 (1 day)

    // New: Holiday Flag
    val isPhilippineHoliday: Boolean = false, // Helps filter auto-added holidays

    val gradientIndex: Int = 0,       // Remembers the specific color!
    val customLabel: String? = null
)