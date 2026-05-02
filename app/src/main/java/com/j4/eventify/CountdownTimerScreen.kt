package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import com.j4.eventify.EventTypeRegistry
import com.j4.eventify.ui.theme.*
import androidx.compose.foundation.layout.statusBarsPadding
import java.util.Locale

@Composable
fun CountdownTimerScreen(
    event: Event,
    onNavigateBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    registry: EventTypeRegistry
) {
    val config = when (event.type) {
        EventType.ACADEMIC -> registry.academic.toConfig()
        EventType.PERSONAL -> registry.personal.toConfig()
        EventType.OCCASION -> registry.occasion.toConfig()
        EventType.CUSTOM -> {
            val liveCategory = registry.customTypes.find {
                it.label.equals(event.customConfig?.label, ignoreCase = true)
            }
            liveCategory ?: event.customConfig ?: registry.academic.toConfig()
        }
    }

    val backgroundColor = Brush.linearGradient(
        listOf(config.gradientStart, config.gradientEnd)
    )
    val textColor = config.textColor

    var showDeleteDialog by remember { mutableStateOf(false) }

    // ─────────────────────────────────────────────
    // NEW SMART TIMER ENGINE
    // ─────────────────────────────────────────────
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Safely figure out the End Time (Default to 1 hour, or 24 hours if All Day)
    val effectiveEndMs = event.rawEndMs ?: if (event.isAllDay) event.rawStartMs + 86400000L else event.rawStartMs + 3600000L

    // Determine the exact State of the event
    val isFinished = currentTime > effectiveEndMs
    val isOngoing = currentTime >= event.rawStartMs && currentTime <= effectiveEndMs

    // Target the Start Time if upcoming, Target the End Time if ongoing!
    val targetTime = if (isOngoing) effectiveEndMs else event.rawStartMs

    // Never allow negative numbers!
    val diffInMillis = (targetTime - currentTime).coerceAtLeast(0L)

    val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis)
    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60
    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(diffInMillis) % 60
    // ─────────────────────────────────────────────

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label         = "fade_in"
    )
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer { this.alpha = alpha }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ModernCountdownTopBar(
                onNavigateBack = onNavigateBack,
                onEdit         = onEdit,
                onDelete       = { showDeleteDialog = true },
                textColor      = textColor
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(Alignment.CenterVertically)
            ) {
                Text(
                    text          = event.title.uppercase(),
                    fontSize      = 28.sp,
                    fontWeight    = FontWeight.Black,
                    color         = textColor,
                    textAlign     = TextAlign.Center,
                    maxLines      = 2,
                    letterSpacing = 1.5.sp,
                    fontFamily    = FontFamily.Default
                )

                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textColor)
                )

                Box(
                    modifier         = Modifier.size(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .rotate(rotation)
                            .border(8.dp, textColor.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .rotate(-rotation / 2)
                            .border(6.dp, textColor.copy(alpha = 0.2f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .rotate(rotation * 1.5f)
                            .border(4.dp, textColor.copy(alpha = 0.15f), CircleShape)
                    )

                    // ─────────────────────────────────────────────
                    // UI STATE MACHINE (Finished vs Ongoing vs Upcoming)
                    // ─────────────────────────────────────────────
                    if (isFinished) {
                        Text(
                            text = "EVENT\nCOMPLETED",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp
                        )
                    } else if (isOngoing) {
                        // Pulsing Animation Engine
                        val infinitePulse = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by infinitePulse.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = textColor.copy(alpha = 0.15f * pulseAlpha),
                                border = BorderStroke(1.dp, textColor.copy(alpha = pulseAlpha))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(textColor.copy(alpha = pulseAlpha)))
                                    Text(
                                        "HAPPENING NOW",
                                        color = textColor.copy(alpha = pulseAlpha),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = String.format(Locale.US, "Ends in %02d:%02d:%02d", hours, minutes, seconds),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        // Standard Upcoming Timer
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            val showDays    = days > 0
                            val showHours   = hours > 0 || showDays
                            val showMinutes = minutes > 0 || showHours
                            val showSeconds = true

                            val activeUnits = listOf(showDays, showHours, showMinutes, showSeconds).count { it }

                            val numberSize = when (activeUnits) {
                                1    -> 72.sp
                                2    -> 64.sp
                                3    -> 58.sp
                                else -> 56.sp
                            }
                            val labelSize = when (activeUnits) {
                                1    -> 14.sp
                                2    -> 12.sp
                                else -> 11.sp
                            }

                            if (showDays || showHours) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment     = Alignment.Top
                                ) {
                                    if (showDays) {
                                        AnimatedTimeUnit(
                                            value      = String.format(Locale.US, "%02d", days),
                                            label      = "DAYS",
                                            color      = textColor,
                                            numberSize = numberSize,
                                            labelSize  = labelSize
                                        )
                                        if (showHours) {
                                            Text(
                                                text       = ":",
                                                fontSize   = numberSize,
                                                fontWeight = FontWeight.Black,
                                                color      = textColor,
                                                modifier   = Modifier.padding(horizontal = 12.dp),
                                                fontFamily = FontFamily.Default
                                            )
                                        }
                                    }
                                    if (showHours) {
                                        AnimatedTimeUnit(
                                            value      = String.format(Locale.US, "%02d", hours),
                                            label      = "HOURS",
                                            color      = textColor,
                                            numberSize = numberSize,
                                            labelSize  = labelSize
                                        )
                                    }
                                }
                            }

                            if (showMinutes || showSeconds) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment     = Alignment.Top
                                ) {
                                    if (showMinutes) {
                                        AnimatedTimeUnit(
                                            value      = String.format(Locale.US, "%02d", minutes),
                                            label      = "MINS",
                                            color      = textColor,
                                            numberSize = numberSize,
                                            labelSize  = labelSize
                                        )
                                        if (showSeconds) {
                                            Text(
                                                text       = ":",
                                                fontSize   = numberSize,
                                                fontWeight = FontWeight.Black,
                                                color      = textColor,
                                                modifier   = Modifier.padding(horizontal = 12.dp),
                                                fontFamily = FontFamily.Default
                                            )
                                        }
                                    }
                                    if (showSeconds) {
                                        AnimatedTimeUnit(
                                            value      = String.format(Locale.US, "%02d", seconds),
                                            label      = "SECS",
                                            color      = textColor,
                                            numberSize = numberSize,
                                            labelSize  = labelSize
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernInfoCard(
                    icon      = Icons.Default.CalendarToday,
                    title     = if (event.isAllDay) "Date" else "Date & Time",
                    content   = event.dateTime.replace(" at ", "\n"),
                    textColor = textColor
                )
                ModernInfoCard(
                    icon      = Icons.AutoMirrored.Filled.Label,
                    title     = "Event Type",
                    content   = config.label,
                    textColor = textColor
                )
                if (event.notes.isNotEmpty()) {
                    ModernInfoCard(
                        icon      = Icons.AutoMirrored.Filled.Notes,
                        title     = "Notes",
                        content   = event.notes,
                        textColor = textColor
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ModernDeleteDialog(
            eventTitle = event.title,
            onConfirm  = {
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
        verticalAlignment     = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back",
                tint     = textColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(onClick = onEdit, shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                Icon(
                    Icons.Default.Edit, "Edit",
                    tint     = textColor,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }
            Surface(onClick = onDelete, shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                Icon(
                    Icons.Default.Delete, "Delete",
                    tint     = textColor,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedTimeUnit(
    value: String,
    label: String,
    color: Color,
    numberSize: androidx.compose.ui.unit.TextUnit = 56.sp,
    labelSize: androidx.compose.ui.unit.TextUnit  = 11.sp
) {
    var previousValue by remember { mutableStateOf(value) }
    val scale by animateFloatAsState(
        targetValue   = if (value != previousValue) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label         = "number_change"
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
            text       = value,
            fontSize   = numberSize,
            fontWeight = FontWeight.Black,
            color      = color,
            lineHeight = numberSize,
            fontFamily = FontFamily.Default,
            modifier   = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        )
        Text(
            text          = label,
            fontSize      = labelSize,
            fontWeight    = FontWeight.Bold,
            color         = color.copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            fontFamily    = FontFamily.Default
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
        shape    = RoundedCornerShape(16.dp),
        color    = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title,   fontSize = 12.sp, fontWeight = FontWeight.Bold,  color = textColor.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Text(content, fontSize = 16.sp, fontWeight = FontWeight.Bold,  color = textColor)
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
        targetValue   = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "dialog_scale"
    )
    LaunchedEffect(Unit) { visible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = White,
        shape            = RoundedCornerShape(24.dp),
        modifier         = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        icon = {
            Surface(shape = CircleShape, color = Color(0xFFFF5252).copy(alpha = 0.1f), modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFF5252), modifier = Modifier.size(32.dp))
                }
            }
        },
        title = {
            Text("Delete Event?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center)
        },
        text = {
            Text(
                "Are you sure you want to delete \"$eventTitle\"? This action cannot be undone.",
                fontSize  = 15.sp,
                color     = Color.Gray,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Surface(onClick = onConfirm, shape = RoundedCornerShape(12.dp), color = Color(0xFFFF5252)) {
                Text("Delete", fontWeight = FontWeight.Bold, color = White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), fontSize = 15.sp)
            }
        },
        dismissButton = {
            Surface(onClick = onDismiss, shape = RoundedCornerShape(12.dp), color = Color(0xFFF5F5F5)) {
                Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), fontSize = 15.sp)
            }
        }
    )
}