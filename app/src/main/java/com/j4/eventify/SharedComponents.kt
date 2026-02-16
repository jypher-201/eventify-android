package com.j4.eventify

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.ui.theme.FABRed
import com.j4.eventify.ui.theme.White

/**
 * Shared UI components used across multiple screens
 */

@Composable
fun EventifyFAB(onClick: () -> Unit = {}) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = FABRed,
        contentColor = White,
        elevation = FloatingActionButtonDefaults.elevation(0.dp),
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Text(
            text = "+",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}