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

// ============ DATA MODELS ============

enum class EventType {
    ACADEMIC,
    PERSONAL,
    OCCASION
}

enum class TimeFilter {
    RECENT,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

data class Event(
    val id: Int,
    val title: String,
    val type: EventType,
    val dateTime: String,
    val countdownNumber: String,
    val countdownLabel: String,
    val notes: String = ""
)

// ============ UI COMPONENT ============

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val backgroundColor = when (event.type) {
        EventType.ACADEMIC -> AcademicBlue
        EventType.PERSONAL -> PersonalPink
        EventType.OCCASION -> OccasionYellow
    }

    val badgeColor = when (event.type) {
        EventType.ACADEMIC -> BadgeAcademic
        EventType.PERSONAL -> Color(0xFF00E676)
        EventType.OCCASION -> Color(0xFF9C27B0)
    }

    val textColor = when (event.type) {
        EventType.OCCASION -> Black
        else -> White
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Black)
        )

        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .border(4.dp, Black, RoundedCornerShape(12.dp))
                    .padding(12.dp)  // ← Changed from 16dp to 12dp (25% reduction)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)  // ← Changed from 6dp to 4dp
                    ) {
                        Text(
                            text = event.title,
                            fontSize = 16.sp,  // ← Changed from 18sp to 16sp
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            lineHeight = 20.sp  // ← Changed from 22sp to 20sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.45f)  // ← Changed from 0.5f to 0.45f
                                .padding(top = 1.dp)  // ← Changed from 2dp to 1dp
                                .background(textColor)
                                .padding(vertical = 0.8.dp)  // ← Changed from 1dp to 0.8dp
                        )

                        Text(
                            text = event.dateTime,
                            fontSize = 11.sp,  // ← Changed from 12sp to 11sp
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.8f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)  // ← Changed from 10dp, 4dp
                        ) {
                            Text(
                                text = event.type.name,
                                fontSize = 10.sp,  // ← Changed from 11sp to 10sp
                                fontWeight = FontWeight.Bold,
                                color = Black
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = event.countdownNumber,
                            fontSize = 32.sp,  // ← Changed from 42sp to 32sp (24% reduction)
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            lineHeight = 32.sp  // ← Changed from 42sp to 32sp
                        )

                        Text(
                            text = event.countdownLabel,
                            fontSize = 11.sp,  // ← Changed from 12sp to 11sp
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
// ============ PREVIEWS ============

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