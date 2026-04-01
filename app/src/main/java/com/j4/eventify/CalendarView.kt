package com.j4.eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventCard
import com.j4.eventify.components.EventType
import java.util.Calendar

// ─────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────

data class CalendarEvent(
    val event: Event,
    val day: Int,
    val month: Int,
    val year: Int
)

fun mapEventsToDays(events: List<Event>): List<CalendarEvent> {
    val philippinesZone = java.util.TimeZone.getTimeZone("Asia/Manila")
    return events.mapNotNull { event ->
        val daysFromNow = event.countdownNumber.toIntOrNull() ?: return@mapNotNull null
        val eventCal = Calendar.getInstance(philippinesZone)
        eventCal.add(Calendar.DAY_OF_MONTH, daysFromNow)
        CalendarEvent(
            event = event,
            day   = eventCal.get(Calendar.DAY_OF_MONTH),
            month = eventCal.get(Calendar.MONTH),
            year  = eventCal.get(Calendar.YEAR)
        )
    }
}

// ─────────────────────────────────────────────
// CalendarView
// ─────────────────────────────────────────────

@Composable
fun CalendarView(
    events: List<Event>,
    onEventClick: (Int) -> Unit,
    onDateSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF667eea),
    textColor: Color   = Color(0xFF1A1A1A),
    surfaceColor: Color = Color.White
) {
    val philippinesZone = java.util.TimeZone.getTimeZone("Asia/Manila")
    val today           = Calendar.getInstance(philippinesZone)

    val todayDay   = today.get(Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(Calendar.MONTH)
    val todayYear  = today.get(Calendar.YEAR)

    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var currentYear  by remember { mutableIntStateOf(todayYear) }
    var selectedDay  by remember { mutableIntStateOf(todayDay) }

    val calendarEvents   = remember(events) { mapEventsToDays(events) }

    val selectedDayEvents = calendarEvents.filter {
        it.day   == selectedDay  &&
                it.month == currentMonth &&
                it.year  == currentYear
    }.map { it.event }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    LaunchedEffect(selectedDay, currentMonth, currentYear) {
        onDateSelected("${monthNames[currentMonth]} $selectedDay, $currentYear")
    }

    // Background uses the theme's background color (dark on DARK, light otherwise)
    val listBg = if (textColor == Color.White) Color(0xFF1A1A1A) else Color(0xFFFAFAFA)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(listBg)
    ) {
        // ── Calendar header card ──────────────────────────────
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            color           = surfaceColor,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Month / year navigation
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 0) { currentMonth = 11; currentYear-- }
                        else currentMonth--
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            "Previous Month",
                            tint     = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        "${monthNames[currentMonth]} $currentYear",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = textColor
                    )

                    IconButton(onClick = {
                        if (currentMonth == 11) { currentMonth = 0; currentYear++ }
                        else currentMonth++
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Next Month",
                            tint     = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Day-of-week labels
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { label ->
                        Text(
                            label,
                            modifier   = Modifier.weight(1f),
                            textAlign  = TextAlign.Center,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = textColor.copy(alpha = 0.45f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calendar grid
                val cal = Calendar.getInstance(philippinesZone)
                cal.set(currentYear, currentMonth, 1)
                val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                val daysInMonth    = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    var dayCounter = 1
                    for (week in 0..5) {
                        if (dayCounter > daysInMonth) break
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (dayOfWeek in 0..6) {
                                val show = if (week == 0) dayOfWeek >= firstDayOfWeek
                                else dayCounter <= daysInMonth

                                if (show && dayCounter <= daysInMonth) {
                                    val currentDay  = dayCounter
                                    val eventsOnDay = calendarEvents.filter {
                                        it.day   == currentDay  &&
                                                it.month == currentMonth &&
                                                it.year  == currentYear
                                    }
                                    ModernDayCell(
                                        day         = currentDay,
                                        isToday     = currentDay   == todayDay   &&
                                                currentMonth == todayMonth &&
                                                currentYear  == todayYear,
                                        isSelected  = currentDay == selectedDay,
                                        events      = eventsOnDay,
                                        onClick     = { selectedDay = currentDay },
                                        modifier    = Modifier.weight(1f),
                                        accentColor = accentColor,
                                        textColor   = textColor
                                    )
                                    dayCounter++
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Events list for selected day ──────────────────────
        if (selectedDayEvents.isNotEmpty()) {
            Text(
                "Events on ${monthNames[currentMonth]} $selectedDay",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor,
                modifier   = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier        = Modifier.fillMaxSize(),
                contentPadding  = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDayEvents) { event ->
                    EventCard(event = event, onClick = { onEventClick(event.id) })
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📅", fontSize = 48.sp)
                    Text(
                        "No events on this day",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Day Cell  (single overload — fully dynamic)
// ─────────────────────────────────────────────

@Composable
fun ModernDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF667eea),
    textColor: Color   = Color(0xFF1A1A1A)
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> accentColor.copy(alpha = 0.15f)
                    isToday    -> accentColor.copy(alpha = 0.08f)
                    else       -> Color.Transparent
                }
            )
            .border(
                width  = if (isSelected) 2.dp else 0.dp,
                color  = accentColor,
                shape  = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier            = Modifier.fillMaxHeight()
        ) {
            Spacer(Modifier.height(2.dp))

            Text(
                day.toString(),
                fontSize   = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = when {
                    isSelected -> accentColor
                    isToday    -> accentColor.copy(alpha = 0.85f)
                    else       -> textColor
                }
            )

            // Event indicator dots/bars
            if (events.isNotEmpty()) {
                Row(
                    modifier              = Modifier.fillMaxWidth(0.75f),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    events.take(3).forEach { calEvent ->
                        val dotColor = when (calEvent.event.type) {
                            EventType.ACADEMIC -> Color(0xFF667eea)
                            EventType.PERSONAL -> Color(0xFFf093fb)
                            EventType.OCCASION -> Color(0xFFfcb69f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(dotColor)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
            } else {
                Spacer(Modifier.height(7.dp))
            }
        }
    }
}