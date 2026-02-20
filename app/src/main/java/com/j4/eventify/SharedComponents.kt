package com.j4.eventify


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.j4.eventify.ui.theme.*

/**
 * Shared UI components used across multiple screens
 */

@Composable
fun EventifyFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .padding(bottom = 16.dp, end = 16.dp)
            .size(64.dp)
            .border(5.dp, Black, RoundedCornerShape(16.dp)),  // ← Bold border directly on FAB
        containerColor = White,
        contentColor = Black,
        shape = RoundedCornerShape(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Event",
            modifier = Modifier.size(36.dp),
            tint = Black
        )
    }
}