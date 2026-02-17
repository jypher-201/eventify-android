package com.j4.eventify

import androidx.compose.ui.graphics.Color
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*

fun getEventTypeColor(type: EventType): Color {
    return when (type) {
        EventType.ACADEMIC -> AcademicBlue
        EventType.PERSONAL -> PersonalPink
        EventType.OCCASION -> OccasionYellow
    }
}