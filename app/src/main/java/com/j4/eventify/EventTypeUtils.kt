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
        EventType.CUSTOM   -> Color(0xFF667eea)
    }
}

// ── THE FIX: ALWAYS check the database's customConfig first! ──
fun Event.getDisplayColor(): Color {
    // If we mapped a saved color from the database, use it immediately
    if (this.customConfig != null) {
        return this.customConfig.gradientStart
    }
    // Otherwise, fall back to the original hardcoded logic
    return resolvedConfig().gradientStart
}