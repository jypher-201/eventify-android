package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale

data class TimeRemaining(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int
)

fun calculateTimeRemaining(countdownNumber: String): TimeRemaining {
    // Get current time in Philippines (Asia/Manila)
    val philippinesZone = java.util.TimeZone.getTimeZone("Asia/Manila")
    val calendar = java.util.Calendar.getInstance(philippinesZone)
    val currentTime = calendar.timeInMillis

    // For demo: Calculate target time based on days from now
    val daysFromNow = countdownNumber.toIntOrNull() ?: 0

    // Set target time (e.g., 5 days from now at 11:59 PM)
    val targetCalendar = java.util.Calendar.getInstance(philippinesZone)
    targetCalendar.add(java.util.Calendar.DAY_OF_MONTH, daysFromNow)
    targetCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
    targetCalendar.set(java.util.Calendar.MINUTE, 59)
    targetCalendar.set(java.util.Calendar.SECOND, 59)

    val targetTime = targetCalendar.timeInMillis
    val diff = targetTime - currentTime

    // Calculate time components
    val totalSeconds = diff / 1000
    val days = (totalSeconds / (24 * 60 * 60)).toInt()
    val hours = ((totalSeconds % (24 * 60 * 60)) / (60 * 60)).toInt()
    val minutes = ((totalSeconds % (60 * 60)) / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()

    return TimeRemaining(
        days = days.coerceAtLeast(0),
        hours = hours.coerceAtLeast(0),
        minutes = minutes.coerceAtLeast(0),
        seconds = seconds.coerceAtLeast(0)
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownTimerScreen(
    event: Event,
    onNavigateBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val backgroundColor = when (event.type) {
        EventType.ACADEMIC -> AcademicBlue
        EventType.PERSONAL -> PersonalPink
        EventType.OCCASION -> OccasionYellow
    }

    val textColor = if (event.type == EventType.OCCASION) Black else White

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Real-time countdown state
    var timeRemaining by remember { mutableStateOf(calculateTimeRemaining(event.countdownNumber)) }

    // Update countdown every second with Philippines time
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)

            // Recalculate from Philippines time to stay accurate
            timeRemaining = calculateTimeRemaining(event.countdownNumber)
        }
    }
    // Animated pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotating effect for decorative elements
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            CountdownTopBar(
                onNavigateBack = onNavigateBack,
                onEdit = onEdit,
                onDelete = { showDeleteDialog = true },
                backgroundColor = backgroundColor,
                textColor = textColor
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Main Countdown Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    // Decorative rotating circles
                    Box(
                        modifier = Modifier.size(360.dp),  // ← Made bigger
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer rotating ring
                        Box(
                            modifier = Modifier
                                .size(340.dp)
                                .rotate(rotation)
                                .border(8.dp, textColor.copy(alpha = 0.3f), CircleShape)
                        )

                        // Middle rotating ring (opposite direction)
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .rotate(-rotation / 2)
                                .border(6.dp, textColor.copy(alpha = 0.2f), CircleShape)
                        )

                        // Center countdown box with pulse - NOW CONTAINS FULL COUNTDOWN
                        Box(
                            modifier = Modifier.scale(pulse)
                        ) {
                            // Shadow
                            Box(
                                modifier = Modifier
                                    .size(260.dp)  // ← Made bigger
                                    .offset(x = 8.dp, y = 8.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Black)
                            )

                            // Main countdown box
                            Column(
                                modifier = Modifier
                                    .size(260.dp)  // ← Made bigger
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(White)
                                    .border(6.dp, Black, RoundedCornerShape(24.dp))
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Event title at top
                                Text(
                                    text = event.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = backgroundColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Full countdown display
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Days and Hours Row
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TimeUnitCompact(
                                            value = String.format(Locale.US, "%02d", timeRemaining.days),
                                            label = "D",
                                            color = backgroundColor
                                        )

                                        Text(
                                            text = ":",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = backgroundColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                                        )

                                        TimeUnitCompact(
                                            value = String.format(Locale.US, "%02d", timeRemaining.hours),
                                            label = "H",
                                            color = backgroundColor
                                        )
                                    }

                                    // Minutes and Seconds Row
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TimeUnitCompact(
                                            value = String.format(Locale.US, "%02d", timeRemaining.minutes),
                                            label = "M",
                                            color = backgroundColor
                                        )

                                        Text(
                                            text = ":",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = backgroundColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                                        )

                                        TimeUnitCompact(
                                            value = String.format(Locale.US, "%02d", timeRemaining.seconds),
                                            label = "S",
                                            color = backgroundColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Event Details Cards
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date & Time Card
                    NeoInfoCard(
                        icon = Icons.Default.CalendarToday,
                        title = "Date & Time",
                        content = event.dateTime,
                        textColor = textColor
                    )

                    // Event Type Card
                    NeoInfoCard(
                        icon = Icons.AutoMirrored.Filled.Label,
                        title = "Event Type",
                        content = event.type.name,
                        textColor = textColor
                    )

                    // Notes Card (if exists)
                    if (event.notes.isNotEmpty()) {
                        NeoInfoCard(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            title = "Notes",
                            content = event.notes,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            eventTitle = event.title,
            onConfirm = {
                showDeleteDialog = false
                onDelete()
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
@Composable
fun CountdownTopBar(
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    backgroundColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TimeUnitCompact(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = color,
            lineHeight = 32.sp
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.7f),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun NeoInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    textColor: Color  // ← This parameter exists but wasn't being used
) {
    Box {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Black)
        )

        // Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(White)
                .border(3.dp, textColor, RoundedCornerShape(12.dp))  // ← Use textColor for border
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.1f)),  // ← Use textColor for icon background
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,  // ← Use textColor for icon
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            }
        }
    }
}
@Composable
fun DeleteConfirmationDialog(
    eventTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Delete Event?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$eventTitle\"? This action cannot be undone.",
                fontSize = 16.sp,
                color = Black
            )
        },
        confirmButton = {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Black)
                )

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Black)
                ) {
                    Text(
                        text = "Delete",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Black)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Black)
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CountdownTimerScreenPreview() {
    EventifyTheme {
        CountdownTimerScreen(
            event = Event(
                id = 1,
                title = "Project Deadline",
                type = EventType.ACADEMIC,
                dateTime = "Due: Feb 25, 2024 at 11:59 PM",
                countdownNumber = "5",
                countdownLabel = "DAYS LEFT",
                notes = "Submit final documentation and presentation slides."
            )
        )
    }
}