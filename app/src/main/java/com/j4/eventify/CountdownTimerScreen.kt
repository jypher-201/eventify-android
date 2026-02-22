package com.j4.eventify

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate


data class TimeRemaining(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int
)

fun calculateTimeRemaining(countdownNumber: String): TimeRemaining {
    val philippinesZone = java.util.TimeZone.getTimeZone("Asia/Manila")
    val calendar = java.util.Calendar.getInstance(philippinesZone)
    val currentTime = calendar.timeInMillis

    val daysFromNow = countdownNumber.toIntOrNull() ?: 0

    val targetCalendar = java.util.Calendar.getInstance(philippinesZone)
    targetCalendar.add(java.util.Calendar.DAY_OF_MONTH, daysFromNow)
    targetCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
    targetCalendar.set(java.util.Calendar.MINUTE, 59)
    targetCalendar.set(java.util.Calendar.SECOND, 59)

    val targetTime = targetCalendar.timeInMillis
    val diff = targetTime - currentTime

    // Handle negative time (event has passed)
    if (diff <= 0) {
        return TimeRemaining(0, 0, 0, 0)
    }

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

    // Update countdown every second
    LaunchedEffect(event.id) {  // ← Added event.id as key
        while (true) {
            kotlinx.coroutines.delay(1000L)
            timeRemaining = calculateTimeRemaining(event.countdownNumber)
        }
    }

// Rotating effect for decorative circles
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
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
                // Main Countdown Display - WITH ROTATING CIRCLES
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(Alignment.CenterVertically)  // ← Center vertically
                ) {
                    // Event title at top
                    Text(
                        text = event.title.uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Default
                    )

                    // Decorative line
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(4.dp)
                            .background(textColor)
                    )

                    // Rotating circles with countdown - IRREGULAR OVERLAPPING SHAPES
                    Box(
                        modifier = Modifier.size(380.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Oblong circle 1 - Wider horizontally
                        Box(
                            modifier = Modifier
                                .width(360.dp)
                                .height(320.dp)
                                .rotate(rotation)
                                .border(
                                    width = 10.dp,
                                    color = textColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(50)  // ← Creates oblong shape
                                )
                        )

                        // Oblong circle 2 - Taller vertically (opposite)
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .height(340.dp)
                                .rotate(-rotation * 1.2f)  // ← Different speed, opposite direction
                                .border(
                                    width = 12.dp,
                                    color = textColor.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(50)
                                )
                        )

                        // Oblong circle 3 - Diagonal lean
                        Box(
                            modifier = Modifier
                                .width(330.dp)
                                .height(280.dp)
                                .rotate(rotation * 0.7f)
                                .border(
                                    width = 8.dp,
                                    color = textColor.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(50)
                                )
                        )

                        // Oblong circle 4 - Opposite diagonal
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(310.dp)
                                .rotate(-rotation / 1.5f)
                                .border(
                                    width = 10.dp,
                                    color = textColor.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(50)
                                )
                        )

                        // Perfect circle in the middle for contrast
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .rotate(rotation * 2f)  // ← Faster rotation
                                .border(6.dp, textColor.copy(alpha = 0.2f), CircleShape)
                        )

                        // Countdown numbers in center
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Days and Hours Row
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Top
                            ) {
                                BigTimeUnit(
                                    value = String.format(Locale.US, "%02d", timeRemaining.days),
                                    label = "DAYS",
                                    color = textColor
                                )

                                Text(
                                    text = ":",
                                    fontSize = 72.sp,  // ← Slightly smaller to fit better
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    fontFamily = FontFamily.Default
                                )

                                BigTimeUnit(
                                    value = String.format(Locale.US, "%02d", timeRemaining.hours),
                                    label = "HOURS",
                                    color = textColor
                                )
                            }

                            // Minutes and Seconds Row
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Top
                            ) {
                                BigTimeUnit(
                                    value = String.format(Locale.US, "%02d", timeRemaining.minutes),
                                    label = "MINS",
                                    color = textColor
                                )

                                Text(
                                    text = ":",
                                    fontSize = 72.sp,  // ← Slightly smaller
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    fontFamily = FontFamily.Default
                                )

                                BigTimeUnit(
                                    value = String.format(Locale.US, "%02d", timeRemaining.seconds),
                                    label = "SECS",
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                // Show "Event Passed" message if countdown is at 0
                if (timeRemaining.days == 0 &&
                    timeRemaining.hours == 0 &&
                    timeRemaining.minutes == 0 &&
                    timeRemaining.seconds == 0) {

                    Text(
                        text = "🎉 EVENT TIME! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
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
fun BigTimeUnit(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            fontSize = 72.sp,  // ← Changed from 80sp to fit better
            fontWeight = FontWeight.Black,
            color = color,
            lineHeight = 72.sp,
            fontFamily = FontFamily.Default
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f),
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
fun NeoInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    textColor: Color
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
                .border(3.dp, textColor, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Cancel Button
                Box {
                    // Shadow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Black)
                    )

                    // Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White,
                            contentColor = Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Black),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // Delete Button
                Box {
                    // Shadow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Black)
                    )

                    // Button
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(3.dp, Black),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        },
        dismissButton = {}  // Empty since we're putting both buttons in confirmButton
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