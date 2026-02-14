package com.j4.eventify.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.ui.theme.AcademicBlue
import com.j4.eventify.ui.theme.BadgeAcademic
import com.j4.eventify.ui.theme.Black
import com.j4.eventify.ui.theme.EventifyTheme
import com.j4.eventify.ui.theme.OccasionYellow
import com.j4.eventify.ui.theme.PersonalPink
import com.j4.eventify.ui.theme.White

/**
 * Event types for categorization
 */
enum class EventType {
    ACADEMIC,
    PERSONAL,
    OCCASION
}

/**
 * Data class representing an event
 */
data class Event(
    val id: Int,
    val title: String,
    val type: EventType,
    val dateTime: String,
    val countdownNumber: String,
    val countdownLabel: String,
    val notes: String = ""
)

/**
 * Neo-brutalism style event card
 *
 * Features:
 * - Bold colors based on event type
 * - Thick black border
 * - Drop shadow effect
 * - Type badge
 * - Large countdown display
 */
@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Determine colors based on event type
    val backgroundColor = when (event.type) {
        EventType.ACADEMIC -> AcademicBlue
        EventType.PERSONAL -> PersonalPink
        EventType.OCCASION -> OccasionYellow
    }

    val badgeColor = when (event.type) {
        EventType.ACADEMIC -> BadgeAcademic
        EventType.PERSONAL -> Color(0xFF00E676)  // Green
        EventType.OCCASION -> Color(0xFF9C27B0)  // Purple
    }

    val textColor = when (event.type) {
        EventType.OCCASION -> Black  // Black text on yellow
        else -> White                // White text on blue/pink
    }

    // Shadow layer (positioned behind main card)
    Box(modifier = modifier) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)  // Shadow offset
                .clip(RoundedCornerShape(12.dp))
                .background(Black)
        )

        // Main card
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(0.dp)  // No Material elevation
        ) {
            Box(
                modifier = Modifier
                    .border(
                        width = 4.dp,
                        color = Black,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Event info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Event title
                        Text(
                            text = event.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            lineHeight = 26.sp
                        )

                        // Underline decoration
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .padding(top = 4.dp)
                                .background(textColor)
                                .padding(vertical = 1.5.dp)
                        )

                        // Date/time
                        Text(
                            text = event.dateTime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.8f)
                        )

                        // Event type badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = event.type.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Black
                            )
                        }
                    }

                    // Right side: Countdown
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Big number
                        Text(
                            text = event.countdownNumber,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            lineHeight = 56.sp
                        )

                        // Label
                        Text(
                            text = event.countdownLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// Preview with Academic event
@Preview(showBackground = true)
@Composable
fun EventCardAcademicPreview() {
    EventifyTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            EventCard(
                event = Event(
                    id = 1,
                    title = "Project Deadline",
                    type = EventType.ACADEMIC,
                    dateTime = "Due: Feb 25, 2024",
                    countdownNumber = "5",
                    countdownLabel = "DAYS LEFT"
                )
            )
        }
    }
}

// Preview with Personal event
@Preview(showBackground = true)
@Composable
fun EventCardPersonalPreview() {
    EventifyTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            EventCard(
                event = Event(
                    id = 2,
                    title = "Gym Workout",
                    type = EventType.PERSONAL,
                    dateTime = "Tomorrow, 10:00 AM",
                    countdownNumber = "1",
                    countdownLabel = "DAY LEFT"
                )
            )
        }
    }
}

// Preview with Occasion event
@Preview(showBackground = true)
@Composable
fun EventCardOccasionPreview() {
    EventifyTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            EventCard(
                event = Event(
                    id = 3,
                    title = "Birthday Party",
                    type = EventType.OCCASION,
                    dateTime = "Today at 7:00 PM",
                    countdownNumber = "NOW",
                    countdownLabel = "HAPPENING"
                )
            )
        }
    }
}

// Preview with all three types
@Preview(showBackground = true, heightDp = 600)
@Composable
fun EventCardAllTypesPreview() {
    EventifyTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventCard(
                event = Event(
                    id = 1,
                    title = "Final Exam - Mobile Dev",
                    type = EventType.ACADEMIC,
                    dateTime = "Due: Mar 8, 2024",
                    countdownNumber = "12",
                    countdownLabel = "DAYS LEFT"
                )
            )

            EventCard(
                event = Event(
                    id = 2,
                    title = "Concert - Ben&Ben",
                    type = EventType.PERSONAL,
                    dateTime = "Mar 15, 2024",
                    countdownNumber = "19",
                    countdownLabel = "DAYS LEFT"
                )
            )

            EventCard(
                event = Event(
                    id = 3,
                    title = "Graduation Day",
                    type = EventType.OCCASION,
                    dateTime = "Today at 3:00 PM",
                    countdownNumber = "NOW",
                    countdownLabel = "HAPPENING"
                )
            )
        }
    }
}