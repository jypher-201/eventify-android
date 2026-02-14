package com.j4.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventCard
import com.j4.eventify.components.EventFilterChip
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.AcademicBlue
import com.j4.eventify.ui.theme.Black
import com.j4.eventify.ui.theme.EventifyTheme
import com.j4.eventify.ui.theme.FABRed
import com.j4.eventify.ui.theme.OccasionYellow
import com.j4.eventify.ui.theme.PersonalPink
import com.j4.eventify.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventifyTheme {
                EventifyHomeScreen()
            }
        }
    }
}

/**
 * Dummy data for prototype
 */
object DummyData {
    val events = listOf(
        Event(
            id = 1,
            title = "Project Deadline",
            type = EventType.ACADEMIC,
            dateTime = "Due: Feb 25, 2024",
            countdownNumber = "5",
            countdownLabel = "DAYS LEFT"
        ),
        Event(
            id = 2,
            title = "Gym Workout",
            type = EventType.PERSONAL,
            dateTime = "Tomorrow, 10:00 AM",
            countdownNumber = "1",
            countdownLabel = "DAY LEFT"
        ),
        Event(
            id = 3,
            title = "Birthday Party",
            type = EventType.OCCASION,
            dateTime = "Today at 7:00 PM",
            countdownNumber = "NOW",
            countdownLabel = "HAPPENING"
        ),
        Event(
            id = 4,
            title = "Final Exam - Mobile Dev",
            type = EventType.ACADEMIC,
            dateTime = "Due: Mar 8, 2024",
            countdownNumber = "12",
            countdownLabel = "DAYS LEFT"
        ),
        Event(
            id = 5,
            title = "Concert - Ben&Ben",
            type = EventType.PERSONAL,
            dateTime = "Mar 15, 2024",
            countdownNumber = "19",
            countdownLabel = "DAYS LEFT"
        )
    )
}

@Composable
fun EventifyHomeScreen() {
    var selectedFilter by remember { mutableStateOf<EventType?>(null) }

    // Filter events based on selection
    val filteredEvents = if (selectedFilter != null) {
        DummyData.events.filter { it.type == selectedFilter }
    } else {
        DummyData.events
    }

    Scaffold(
        topBar = {
            EventifyTopBar(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        },
        floatingActionButton = {
            EventifyFAB()
        }
    ) { innerPadding ->
        if (filteredEvents.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            EventList(
                events = filteredEvents,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun EventifyTopBar(
    selectedFilter: EventType?,
    onFilterSelected: (EventType?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "EVENTIFY",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            letterSpacing = 2.sp
        )

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventFilterChip(
                text = "Academic",
                selected = selectedFilter == EventType.ACADEMIC,
                backgroundColor = AcademicBlue,
                onClick = {
                    onFilterSelected(
                        if (selectedFilter == EventType.ACADEMIC) null
                        else EventType.ACADEMIC
                    )
                }
            )
            EventFilterChip(
                text = "Personal",
                selected = selectedFilter == EventType.PERSONAL,
                backgroundColor = PersonalPink,
                onClick = {
                    onFilterSelected(
                        if (selectedFilter == EventType.PERSONAL) null
                        else EventType.PERSONAL
                    )
                }
            )
            EventFilterChip(
                text = "Occasion",
                selected = selectedFilter == EventType.OCCASION,
                backgroundColor = OccasionYellow,
                onClick = {
                    onFilterSelected(
                        if (selectedFilter == EventType.OCCASION) null
                        else EventType.OCCASION
                    )
                }
            )
        }
    }
}

@Composable
fun EventList(
    events: List<Event>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events) { event ->
            EventCard(
                event = event,
                onClick = {
                    // TODO: Navigate to details
                }
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No events found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Try a different filter",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EventifyFAB() {
    FloatingActionButton(
        onClick = {
            // TODO: Navigate to add event screen
        },
        containerColor = FABRed,
        contentColor = White,
        elevation = FloatingActionButtonDefaults.elevation(0.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
    ) {
        Text(
            text = "+",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EventifyHomeScreenPreview() {
    EventifyTheme {
        EventifyHomeScreen()
    }
}