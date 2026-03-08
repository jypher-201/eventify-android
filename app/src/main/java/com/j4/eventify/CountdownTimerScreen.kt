package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import androidx.compose.foundation.layout.statusBarsPadding
import java.util.Locale

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

@Composable
fun CountdownTimerScreen(
    event: Event,
    onNavigateBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val backgroundColor = when (event.type) {
        EventType.ACADEMIC -> Brush.linearGradient(
            colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
        )
        EventType.PERSONAL -> Brush.linearGradient(
            colors = listOf(Color(0xFFf093fb), Color(0xFFF5576C))
        )
        EventType.OCCASION -> Brush.linearGradient(
            colors = listOf(Color(0xFFffecd2), Color(0xFFfcb69f))
        )
    }

    val textColor = if (event.type == EventType.OCCASION) Color(0xFF8B4513) else Color.White

    var showDeleteDialog by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(calculateTimeRemaining(event.countdownNumber)) }

    // Update countdown every second
    LaunchedEffect(event.id) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            timeRemaining = calculateTimeRemaining(event.countdownNumber)
        }
    }

    // Rotating circles animation
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

    // Fade in animation
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "fade_in"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer { this.alpha = alpha }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Modern Top Bar - at the very top
            ModernCountdownTopBar(
                onNavigateBack = onNavigateBack,
                onEdit = onEdit,
                onDelete = { showDeleteDialog = true },
                textColor = textColor
            )

            // Main Countdown with rotating circles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(Alignment.CenterVertically)
            ) {
                // Event title
                Text(
                    text = event.title.uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Default
                )

                // Decorative line
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textColor)
                )

                // Rotating circles with countdown
                Box(
                    modifier = Modifier.size(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating ring
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .rotate(rotation)
                            .border(
                                8.dp,
                                textColor.copy(alpha = if (event.type == EventType.PERSONAL) 0.5f else 0.3f),  // ← Darker for pink
                                CircleShape
                            )
                    )

                    // Middle rotating ring (opposite)
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .rotate(-rotation / 2)
                            .border(
                                6.dp,
                                textColor.copy(alpha = if (event.type == EventType.PERSONAL) 0.4f else 0.2f),  // ← Darker for pink
                                CircleShape
                            )
                    )

                    // Inner rotating ring
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .rotate(rotation * 1.5f)
                            .border(
                                4.dp,
                                textColor.copy(alpha = if (event.type == EventType.PERSONAL) 0.3f else 0.15f),  // ← Darker for pink
                                CircleShape
                            )
                    )

                    // Countdown numbers
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Days and Hours
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Top
                        ) {
                            AnimatedTimeUnit(
                                value = String.format(Locale.US, "%02d", timeRemaining.days),
                                label = "DAYS",
                                color = textColor
                            )

                            Text(
                                text = ":",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                fontFamily = FontFamily.Default
                            )

                            AnimatedTimeUnit(
                                value = String.format(Locale.US, "%02d", timeRemaining.hours),
                                label = "HOURS",
                                color = textColor
                            )
                        }

                        // Minutes and Seconds
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Top
                        ) {
                            AnimatedTimeUnit(
                                value = String.format(Locale.US, "%02d", timeRemaining.minutes),
                                label = "MINS",
                                color = textColor
                            )

                            Text(
                                text = ":",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                fontFamily = FontFamily.Default
                            )

                            AnimatedTimeUnit(
                                value = String.format(Locale.US, "%02d", timeRemaining.seconds),
                                label = "SECS",
                                color = textColor
                            )
                        }
                    }
                }
            }

            // Event passed message
            if (timeRemaining.days == 0 && timeRemaining.hours == 0 &&
                timeRemaining.minutes == 0 && timeRemaining.seconds == 0) {
                Text(
                    text = "🎉 EVENT TIME! 🎉",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Modern Event Details Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernInfoCard(
                    icon = Icons.Default.CalendarToday,
                    title = "Date & Time",
                    content = event.dateTime,
                    textColor = textColor
                )

                ModernInfoCard(
                    icon = Icons.AutoMirrored.Filled.Label,
                    title = "Event Type",
                    content = event.type.name,
                    textColor = textColor
                )

                if (event.notes.isNotEmpty()) {
                    ModernInfoCard(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        title = "Notes",
                        content = event.notes,
                        textColor = textColor
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ModernDeleteDialog(
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
fun ModernCountdownTopBar(
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button - left edge
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = textColor,
                modifier = Modifier.size(26.dp)
            )
        }

        // Edit & Delete buttons - right edge
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
                onClick = onEdit,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = textColor,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }

            Surface(
                onClick = onDelete,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = textColor,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedTimeUnit(
    value: String,
    label: String,
    color: Color
) {
    var previousValue by remember { mutableStateOf(value) }
    val scale by animateFloatAsState(
        targetValue = if (value != previousValue) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "number_change"
    )

    LaunchedEffect(value) {
        if (value != previousValue) {
            kotlinx.coroutines.delay(150)
            previousValue = value
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 56.sp,
            fontWeight = FontWeight.Black,
            color = color,
            lineHeight = 56.sp,
            fontFamily = FontFamily.Default,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
fun ModernInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ModernDeleteDialog(
    eventTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dialog_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFF5252).copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Delete Event?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$eventTitle\"? This action cannot be undone.",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF5252)
            ) {
                Text(
                    text = "Delete",
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    fontSize = 15.sp
                )
            }
        }
    )
}