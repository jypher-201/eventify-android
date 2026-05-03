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
import com.j4.eventify.components.EventTypeConfig
import java.util.Calendar

// ─────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────

// Cached once at class-load time
private val philippinesZone: java.util.TimeZone =
    java.util.TimeZone.getTimeZone("Asia/Manila")

data class CalendarEvent(
    val event: Event,
    val day: Int,
    val month: Int,
    val year: Int
)

// ── THE FIX: The Time Machine! Only generates events for the year the user is looking at. ──
fun mapEventsToDays(events: List<Event>, targetYear: Int): List<CalendarEvent> {
    val results = mutableListOf<CalendarEvent>()
    val cal = Calendar.getInstance(philippinesZone)

    events.forEach { event ->
        // Start at the exact millisecond the event originally began
        cal.timeInMillis = event.rawStartMs
        val originalYear = cal.get(Calendar.YEAR)

        if (event.repeatMode.isNullOrBlank() || event.repeatMode == "Does not repeat") {
            // Single event: Just add it as-is
            results.add(
                CalendarEvent(
                    event = event,
                    day   = cal.get(Calendar.DAY_OF_MONTH),
                    month = cal.get(Calendar.MONTH),
                    year  = originalYear
                )
            )
        } else {
            // Repeating Event: We need to time travel!
            val lower = event.repeatMode.lowercase()
            val parts = lower.split(" ")
            val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1

            // Smart translation for jumping forward in time safely
            val unit = when {
                lower.contains("day") -> Calendar.DAY_OF_YEAR
                lower.contains("week") -> Calendar.DAY_OF_YEAR // We use days for weeks to avoid end-of-year bugs
                lower.contains("month") -> Calendar.MONTH
                lower.contains("year") -> Calendar.YEAR
                else -> Calendar.DAY_OF_YEAR
            }

            // If they said "Every 2 weeks", we jump 14 days at a time
            val addAmount = if (lower.contains("week")) amount * 7 else amount

            // Fast forward through time until we jump past the year the user is viewing
            while (cal.get(Calendar.YEAR) <= targetYear) {
                val currentLoopYear = cal.get(Calendar.YEAR)

                // Only save the ghost if it happens in the Target Year OR its Original Creation Year
                if (currentLoopYear == targetYear || currentLoopYear == originalYear) {
                    results.add(
                        CalendarEvent(
                            event = event,
                            day   = cal.get(Calendar.DAY_OF_MONTH),
                            month = cal.get(Calendar.MONTH),
                            year  = currentLoopYear
                        )
                    )
                }
                // Time travel forward!
                cal.add(unit, addAmount)
            }
        }
    }
    // Remove any accidental duplicates and return
    return results.distinct()
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
    surfaceColor: Color = Color.White,
    configResolver: ((Event) -> com.j4.eventify.components.EventTypeConfig)? = null
) {
    val today = Calendar.getInstance(philippinesZone)

    val todayDay   = today.get(Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(Calendar.MONTH)
    val todayYear  = today.get(Calendar.YEAR)

    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var currentYear  by remember { mutableIntStateOf(todayYear) }
    var selectedDay  by remember { mutableIntStateOf(todayDay) }

    // ── THE FIX: The calendar recalculates the ghosts dynamically whenever you change the year! ──
    val calendarEvents = remember(events, currentYear) {
        mapEventsToDays(events, currentYear)
    }

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

    // Background uses the theme's background color
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
                val cal = today.clone() as Calendar
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
                                        day            = currentDay,
                                        isToday        = currentDay   == todayDay   &&
                                                currentMonth == todayMonth &&
                                                currentYear  == todayYear,
                                        isSelected     = currentDay == selectedDay,
                                        events         = eventsOnDay,
                                        onClick        = { selectedDay = currentDay },
                                        modifier       = Modifier.weight(1f),
                                        accentColor    = accentColor,
                                        textColor      = textColor,
                                        configResolver = configResolver
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
                    EventCard(
                        event          = event,
                        onClick        = { onEventClick(event.id) },
                        overrideConfig = configResolver?.invoke(event)
                    )
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
// Day Cell
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
    textColor: Color   = Color(0xFF1A1A1A),
    configResolver: ((Event) -> com.j4.eventify.components.EventTypeConfig)? = null
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
                        val dotColor = configResolver?.invoke(calEvent.event)?.gradientStart
                            ?: when (calEvent.event.type) {
                                EventType.ACADEMIC -> Color(0xFF667eea)
                                EventType.PERSONAL -> Color(0xFFf093fb)
                                EventType.OCCASION -> Color(0xFFfcb69f)
                                EventType.OTHER    -> Color(0xFF9E9E9E)
                                EventType.CUSTOM   -> calEvent.event.customConfig?.gradientStart ?: accentColor
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