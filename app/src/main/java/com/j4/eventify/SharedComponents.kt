package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Modern Glassmorphism FAB
 */
@Composable
fun EventifyFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF667eea)
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.90f else 1f,
        // Snap down instantly, spring back on release
        animationSpec = if (isPressed)
            tween(durationMillis = 60, easing = LinearEasing)
        else
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .padding(bottom = 16.dp, end = 16.dp)
            .size(64.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = if (isPressed) 4.dp else 12.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.80f))
                    )
                )
                // pointerInput fires onPress immediately — no 100ms delay like clickable
                .pointerInput(onClick) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                    )
                }
        ) {
            // Glassmorphism highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.14f))
            )

            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Add Event",
                    modifier           = Modifier.size(32.dp),
                    tint               = Color.White
                )
            }
        }
    }
}