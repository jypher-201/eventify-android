package com.j4.eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward  // ← Fixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import java.util.Calendar

// Data class to map events to specific calendar days
data class CalendarEvent(
    val event: Event,
    val day: Int,
    val month: Int,
    val year: Int
)

// Map dummy events to real calendar dates
fun mapEventsToDays(): List<CalendarEvent> {
    val philippinesZone = java.util.TimeZone.getTimeZone("Asia/Manila")

    return DummyData.events.mapNotNull { event ->
        val daysFromNow = event.countdownNumber.toIntOrNull() ?: return@mapNotNull null
        val eventCal = Calendar.getInstance(philippinesZone)
        eventCal.add(Calendar.DAY_OF_MONTH, daysFromNow)

        CalendarEvent(
            event = event,
            day = eventCal.get(Calendar.DAY_OF_MONTH),
            month = eventCal.get(Calendar.MONTH),
            year = eventCal.get(Calendar.YEAR)
        )
    }
}

@Composable
fun CalendarView(
    events: List<Event>,
    onEventClick: (Int) -> Unit,
    onDateSelected: (String) -> Unit = {},  // ← NEW parameter
    modifier: Modifier = Modifier
) {
    val philippinesZone = java.util.TimeZone.getTimeZone("Asia/Manila")
    val today = Calendar.getInstance(philippinesZone)

    val todayDay = today.get(Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(Calendar.MONTH)
    val todayYear = today.get(Calendar.YEAR)

    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var currentYear by remember { mutableIntStateOf(todayYear) }
    var selectedDay by remember { mutableIntStateOf(todayDay) }

    val calendarEvents = remember(events) { mapEventsToDays() }

    // Get events for selected day
    val selectedDayEvents = calendarEvents.filter {
        it.day == selectedDay &&
                it.month == currentMonth &&
                it.year == currentYear
    }.map { it.event }

    // Format and send selected date when day changes
    LaunchedEffect(selectedDay, currentMonth, currentYear) {
        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val formattedDate = "${monthNames[currentMonth]} $selectedDay, $currentYear"
        onDateSelected(formattedDate)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Month Navigation Header
        MonthHeader(
            currentMonth = currentMonth,
            currentYear = currentYear,
            onPrevMonth = {
                if (currentMonth == 0) {
                    currentMonth = 11
                    currentYear--
                } else {
                    currentMonth--
                }
                selectedDay = 1
            },
            onNextMonth = {
                if (currentMonth == 11) {
                    currentMonth = 0
                    currentYear++
                } else {
                    currentMonth++
                }
                selectedDay = 1
            }
        )

        // Calendar Grid
        CalendarGrid(
            currentMonth = currentMonth,
            currentYear = currentYear,
            todayDay = todayDay,        // ← Pass individual values
            todayMonth = todayMonth,
            todayYear = todayYear,
            selectedDay = selectedDay,
            calendarEvents = calendarEvents,
            onDayClick = { day -> selectedDay = day }
        )

        HorizontalDivider(thickness = 3.dp, color = Black)

        // Selected Day Events
        SelectedDayEvents(
            selectedDay = selectedDay,
            currentMonth = currentMonth,
            currentYear = currentYear,
            events = selectedDayEvents,
            onEventClick = onEventClick
        )
    }
}

@Composable
fun MonthHeader(
    currentMonth: Int,
    currentYear: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL",
        "MAY", "JUNE", "JULY", "AUGUST",
        "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .border(width = 3.dp, color = Black)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev Month Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Black)
                .clickable(onClick = onPrevMonth),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Month",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Month and Year
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = monthNames[currentMonth],
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Black,
                letterSpacing = 2.sp
            )
            Text(
                text = currentYear.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        // Next Month Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Black)
                .clickable(onClick = onNextMonth),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,  // ← Fixed
                contentDescription = "Next Month",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Int,
    currentYear: Int,
    todayDay: Int,          // ← Individual params instead of Calendar
    todayMonth: Int,
    todayYear: Int,
    selectedDay: Int,
    calendarEvents: List<CalendarEvent>,
    onDayClick: (Int) -> Unit
) {
    val dayNames = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    // Calculate first day of month and total days
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
    }
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
    val totalDays = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
    ) {
        // Day names header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Black)
                .padding(vertical = 8.dp)
        ) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = White,
                    letterSpacing = 1.sp
                )
            }
        }

        // Calendar days grid
        val totalCells = startDayOfWeek + totalDays
        val totalRows = kotlin.math.ceil(totalCells / 7.0).toInt()  // ← Kotlin function

        repeat(totalRows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = cellIndex - startDayOfWeek + 1

                    if (day !in 1..totalDays) {  // ← Range check fix
                        // Empty cell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .background(Color(0xFFF5F5F5))
                                .border(0.5.dp, Color(0xFFE0E0E0))
                        )
                    } else {
                        // Day cell
                        val isToday = day == todayDay &&
                                currentMonth == todayMonth &&
                                currentYear == todayYear

                        val isSelected = day == selectedDay

                        // Get events for this day
                        val dayEvents = calendarEvents.filter {
                            it.day == day &&
                                    it.month == currentMonth &&
                                    it.year == currentYear
                        }

                        DayCell(
                            day = day,
                            isToday = isToday,
                            isSelected = isSelected,
                            events = dayEvents,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            onClick = { onDayClick(day) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                when {
                    isSelected -> Black
                    isToday -> Color(0xFFF0F0F0)
                    else -> White
                }
            )
            .border(0.5.dp, Color(0xFFE0E0E0))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day Number
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> White          // ← Selected: White circle
                            isToday -> Black             // ← Today: Black circle
                            else -> Color.Transparent    // ← Normal: No circle
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 13.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Black else FontWeight.Normal,
                    color = when {
                        isSelected -> Black          // ← Selected: Black text on white circle
                        isToday -> White             // ← Today: White text on black circle ✅ FIXED
                        else -> Black                // ← Normal: Black text
                    }
                )
            }

            // Event color bookmarks
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    events.take(3).forEach { calendarEvent ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(getEventTypeColor(calendarEvent.event.type))
                                .border(
                                    1.dp,
                                    Black.copy(alpha = 0.3f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // Show "+X more" if more than 3 events
                    if (events.size > 3) {
                        Text(
                            text = "+${events.size - 3}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SelectedDayEvents(
    selectedDay: Int,
    currentMonth: Int,
    currentYear: Int,
    events: List<Event>,
    onEventClick: (Int) -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Selected date header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$selectedDay ${monthNames[currentMonth]} $currentYear",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Black
            )

            // Event count badge
            if (events.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Black)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${events.size} EVENT${if (events.size > 1) "S" else ""}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        HorizontalDivider(thickness = 2.dp, color = Color(0xFFE0E0E0))

        if (events.isEmpty()) {
            // No events placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📅", fontSize = 32.sp)
                    Text(
                        text = "NO EVENTS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Tap + to add an event",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Events list for selected day
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    CalendarEventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarEventCard(
    event: Event,
    onClick: () -> Unit
) {
    val eventColor = getEventTypeColor(event.type)
    val textColor = if (event.type == EventType.OCCASION) Black else White

    Box {
        // Neo-brutalism shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Black)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(eventColor)
                .border(3.dp, Black, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color bookmark strip on left
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(White.copy(alpha = 0.5f))
            )

            // Event details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.dateTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.8f)
                )

                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Black.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = event.type.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Countdown badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(White)
                        .border(2.dp, Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.countdownNumber,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = eventColor
                    )
                }
                Text(
                    text = event.countdownLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}