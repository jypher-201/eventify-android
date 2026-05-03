package com.j4.eventify.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.BuiltInIcon

// ─────────────────────────────────────────────
// Event Type
// ─────────────────────────────────────────────

enum class EventType {
    ACADEMIC,
    PERSONAL,
    OCCASION,

    OTHER,
    CUSTOM,

    HOLIDAY
}

// ─────────────────────────────────────────────
// EventTypeConfig
//
// Carries everything needed to render a card for
// any type — built-in or custom.
// gradientStart / gradientEnd drive the card bg.
// badgeColor is the pill text colour.
// label is what appears on the badge pill.
// ─────────────────────────────────────────────

data class EventTypeConfig(
    val type: EventType,
    val label: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val textColor: Color,
    val badgeColor: Color,
    val iconKey: BuiltInIcon? = null
)

// Default configs for the three built-in types
val defaultAcademicConfig = EventTypeConfig(
    type          = EventType.ACADEMIC,
    label         = "ACADEMIC",
    gradientStart = Color(0xFF667eea),
    gradientEnd   = Color(0xFF764ba2),
    textColor     = Color.White,
    badgeColor    = Color(0xFF5E35B1)
)

val defaultPersonalConfig = EventTypeConfig(
    type          = EventType.PERSONAL,
    label         = "PERSONAL",
    gradientStart = Color(0xFFf093fb),
    gradientEnd   = Color(0xFFF5576C),
    textColor     = Color.White,
    badgeColor    = Color(0xFFD81B60)
)

val defaultOccasionConfig = EventTypeConfig(
    type          = EventType.OCCASION,
    label         = "OCCASION",
    gradientStart = Color(0xFFffecd2),
    gradientEnd   = Color(0xFFfcb69f),
    textColor     = Color(0xFF8B4513),
    badgeColor    = Color(0xFFEF6C00)
)

// Gradient palette exposed for pickers (12 options)
val gradientPalette: List<Pair<Color, Color>> = listOf(
    Pair(Color(0xFF667eea), Color(0xFF764ba2)),  // 0  Purple-indigo (Academic default)
    Pair(Color(0xFFf093fb), Color(0xFFF5576C)),  // 1  Pink-red (Personal default)
    Pair(Color(0xFFffecd2), Color(0xFFfcb69f)),  // 2  Peach (Occasion default)
    Pair(Color(0xFF2196F3), Color(0xFF1565C0)),  // 3  Blue
    Pair(Color(0xFF4CAF50), Color(0xFF1B5E20)),  // 4  Green
    Pair(Color(0xFF009688), Color(0xFF004D40)),  // 5  Teal
    Pair(Color(0xFFFF5722), Color(0xFFBF360C)),  // 6  Deep orange
    Pair(Color(0xFFF44336), Color(0xFFB71C1C)),  // 7  Red
    Pair(Color(0xFF9C27B0), Color(0xFF4A148C)),  // 8  Purple
    Pair(Color(0xFF00BCD4), Color(0xFF006064)),  // 9  Cyan
    Pair(Color(0xFFE91E63), Color(0xFF7B1FA2)),  // 10 Pink-purple
    Pair(Color(0xFF795548), Color(0xFF3E2723)),  // 11 Brown
)

// Derive readable text + badge color from a gradient start
fun textColorForGradient(start: Color): Color {
    // Light gradients (peach/yellow) need dark text
    val luminance = 0.299f * start.red + 0.587f * start.green + 0.114f * start.blue
    return if (luminance > 0.72f) Color(0xFF4A2800) else Color.White
}

fun badgeColorForGradient(start: Color, end: Color): Color {
    val luminance = 0.299f * start.red + 0.587f * start.green + 0.114f * start.blue
    return if (luminance > 0.72f) end.copy(alpha = 1f) else end
}

// ─────────────────────────────────────────────
// Event Data Class
// ─────────────────────────────────────────────

data class Event(
    val id: Int,
    val title: String,
    val type: EventType,
    val dateTime: String,
    val countdownNumber: String,
    val countdownLabel: String,
    val notes: String = "",
    // Optional: custom type config (non-null for CUSTOM events)
    val customConfig: EventTypeConfig? = null,
    val rawStartMs: Long = 0L,
    val rawEndMs: Long? = null,
    val isAllDay: Boolean = false,
    val remindBeforeMinutes: List<Int> = emptyList(),
    val repeatMode: String? = null,

    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

// Resolve the config to use for rendering
fun Event.resolvedConfig(): EventTypeConfig = when {
    type == EventType.CUSTOM && customConfig != null -> customConfig
    type == EventType.ACADEMIC -> defaultAcademicConfig
    type == EventType.PERSONAL -> defaultPersonalConfig
    else                       -> defaultOccasionConfig
}

// ─────────────────────────────────────────────
// EventCard
// ─────────────────────────────────────────────

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    // Pass registry.resolveForType(...) here to get live color updates
    overrideConfig: EventTypeConfig? = null
) {
    val config = overrideConfig ?: remember(event) { event.resolvedConfig() }

    val cardBrush = remember(config) {
        Brush.linearGradient(listOf(config.gradientStart, config.gradientEnd))
    }

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "zoom_scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue   = if (isPressed) 3.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "shadow_elevation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = shadowElevation, shape = RoundedCornerShape(16.dp), clip = false)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBrush)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            val released = tryAwaitRelease()
                            isPressed = false
                            if (released) onClick()
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { drawRect(Color.White.copy(alpha = 0.1f)) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Left — title + date
                    Column(
                        modifier            = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text      = event.title,
                                fontSize  = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color     = config.textColor,
                                maxLines  = 1,
                                overflow  = TextOverflow.Ellipsis,
                                modifier  = Modifier.weight(1f, fill = false)
                            )

                            Spacer(Modifier.width(8.dp))

                            // Type badge pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .border(
                                        width = 1.dp,
                                        color = config.textColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text          = config.label.uppercase(), // <--- Force it to UPPERCASE here!
                                    fontSize      = 10.sp,
                                    fontWeight    = FontWeight.Black,
                                    color         = config.badgeColor,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Date row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                null,
                                tint     = config.textColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text       = event.dateTime.replace(" (All Day)", ""),
                                fontSize   = 12.sp, // <--- Shrink slightly from 13.sp to 12.sp
                                fontWeight = FontWeight.Medium,
                                color      = config.textColor.copy(alpha = 0.8f),
                                maxLines   = 2, // <--- CHANGE THIS to 2
                                lineHeight = 16.sp, // <--- ADD THIS so it looks clean when stacked
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Right — countdown circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .drawBehind { drawCircle(Color.White.copy(alpha = 0.25f)) }
                            .border(2.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier            = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text       = event.countdownNumber,
                                fontSize   = 26.sp,
                                fontWeight = FontWeight.Black,
                                color      = config.textColor,
                                lineHeight = 26.sp,
                                textAlign  = TextAlign.Center
                            )
                            Spacer(Modifier.height(1.dp))
                            Text(
                                text          = event.countdownLabel,
                                fontSize      = 9.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = config.textColor.copy(alpha = 0.8f),
                                letterSpacing = 0.4.sp,
                                textAlign     = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}