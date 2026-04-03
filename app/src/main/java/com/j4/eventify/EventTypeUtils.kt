package com.j4.eventify

import androidx.compose.ui.graphics.Color
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventType
import com.j4.eventify.components.resolvedConfig
import com.j4.eventify.ui.theme.*

fun getEventTypeColor(type: EventType): Color {
    return when (type) {
        EventType.ACADEMIC -> AcademicBlue
        EventType.PERSONAL -> PersonalPink
        EventType.OCCASION -> OccasionYellow
        EventType.CUSTOM   -> Color(0xFF667eea) // fallback; use resolvedConfig for accurate color
    }
}

// Preferred: get the exact gradient start color for any event, including custom types
fun Event.getDisplayColor(): Color = resolvedConfig().gradientStart