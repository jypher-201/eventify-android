package com.j4.eventify

import com.j4.eventify.components.Event
import com.j4.eventify.components.EventType

/**
 * Dummy data for prototype (Phase 1)
 * Will be replaced with Room database in Phase 2
 */
object DummyData {
    val events = listOf(
        Event(
            id = 1,
            title = "Project Deadline",
            type = EventType.ACADEMIC,
            dateTime = "Due: April 25, 2026",
            countdownNumber = "5",
            countdownLabel = "DAYS LEFT",
            notes = "Submit final documentation and presentation slides."
        ),
        Event(
            id = 2,
            title = "Gym Workout",
            type = EventType.PERSONAL,
            dateTime = "Tomorrow, 10:00 AM",
            countdownNumber = "1",
            countdownLabel = "DAY LEFT",
            notes = "Leg day - don't skip!"
        ),
        Event(
            id = 3,
            title = "Birthday Party",
            type = EventType.OCCASION,
            dateTime = "Today at 7:00 PM",
            countdownNumber = "NOW",
            countdownLabel = "HAPPENING",
            notes = "Bring a gift! Venue: The Garden Restaurant"
        ),
        Event(
            id = 4,
            title = "Final Exam - Mobile Dev",
            type = EventType.ACADEMIC,
            dateTime = "Due: April 8, 2026",
            countdownNumber = "12",
            countdownLabel = "DAYS LEFT",
            notes = "Study chapters 5-10. Focus on Jetpack Compose."
        ),
        Event(
            id = 5,
            title = "Concert - Ben&Ben",
            type = EventType.PERSONAL,
            dateTime = "April 15, 2026",
            countdownNumber = "19",
            countdownLabel = "DAYS LEFT",
            notes = "Gates open at 6 PM. Bring valid ID."
        )
    )
}