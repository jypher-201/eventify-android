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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Event Type Enum
 */
enum class EventType {
    ACADEMIC,
    PERSONAL,
    OCCASION
}

/**
 * Event Data Class
 */
data class Event(
    val id: Int,
    val title: String,
    val type: EventType,
    val dateTime: String,
    val countdownNumber: String,
    val countdownLabel: String,
    val notes: String = ""
)

/**
 * Ultimate Glassmorphism Event Card
 * - RELIABLE smooth zoom animation
 * - Perfect rounded shadows
 * - Consistent press feedback
 */
@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Pre-compute colors
    val (backgroundColor, textColor, badgeColor) = remember(event.type) {
        when (event.type) {
            EventType.ACADEMIC -> Triple(
                Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2))),
                Color.White,
                Color(0xFF5E35B1)
            )
            EventType.PERSONAL -> Triple(
                Brush.linearGradient(listOf(Color(0xFFf093fb), Color(0xFFF5576C))),
                Color.White,
                Color(0xFFD81B60)
            )
            EventType.OCCASION -> Triple(
                Brush.linearGradient(listOf(Color(0xFFffecd2), Color(0xFFfcb69f))),
                Color(0xFF8B4513),
                Color(0xFFEF6C00)
            )
        }
    }

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "zoom_scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "shadow_elevation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            val released = tryAwaitRelease()
                            isPressed = false
                            if (released) {
                                onClick()
                            }
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(Color.White.copy(alpha = 0.1f))
                    }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.title,
                                fontSize = 17.sp,  // ← INCREASED from 15sp
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .border(
                                        width = 1.dp,
                                        color = textColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)  // ← Increased padding
                            ) {
                                Text(
                                    text = event.type.name,
                                    fontSize = 9.sp,  // ← INCREASED from 8sp
                                    fontWeight = FontWeight.Black,
                                    color = badgeColor,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)  // ← INCREASED from 13dp
                            )
                            Text(
                                text = event.dateTime,
                                fontSize = 12.sp,  // ← INCREASED from 10sp
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(54.dp)  // ← Slightly increased from 52dp
                            .clip(CircleShape)
                            .drawBehind {
                                drawCircle(Color.White.copy(alpha = 0.25f))
                            }
                            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = event.countdownNumber,
                                fontSize = 21.sp,  // ← INCREASED from 19sp
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = event.countdownLabel,
                                fontSize = 8.sp,  // ← INCREASED from 7sp
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.8f),
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }
    }
}