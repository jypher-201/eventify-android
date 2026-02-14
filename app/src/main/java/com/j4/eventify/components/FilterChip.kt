package com.j4.eventify.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.ui.theme.AcademicBlue
import com.j4.eventify.ui.theme.Black
import com.j4.eventify.ui.theme.EventifyTheme
import com.j4.eventify.ui.theme.OccasionYellow
import com.j4.eventify.ui.theme.PersonalPink
import com.j4.eventify.ui.theme.White

/**
 * Neo-brutalism style filter chip
 *
 * Features:
 * - Bold color based on event type
 * - Thick black border
 * - No rounded corners (sharp edges)
 * - Clear selected/unselected states
 */
@Composable
fun EventFilterChip(
    text: String,
    selected: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) backgroundColor else White
    val contentColor = if (selected) {
        if (backgroundColor == OccasionYellow) Black else White
    } else {
        Black
    }
    val borderWidth = if (selected) 4.dp else 3.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))  // Pill shape
            .background(containerColor)
            .border(
                width = borderWidth,
                color = Black,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

// Previews
@Preview
@Composable
fun FilterChipAcademicSelectedPreview() {
    EventifyTheme {
        EventFilterChip(
            text = "Academic",
            selected = true,
            backgroundColor = AcademicBlue,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FilterChipPersonalUnselectedPreview() {
    EventifyTheme {
        EventFilterChip(
            text = "Personal",
            selected = false,
            backgroundColor = PersonalPink,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FilterChipOccasionSelectedPreview() {
    EventifyTheme {
        EventFilterChip(
            text = "Occasion",
            selected = true,
            backgroundColor = OccasionYellow,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterChipsRowPreview() {
    EventifyTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventFilterChip(
                text = "Academic",
                selected = true,
                backgroundColor = AcademicBlue,
                onClick = {}
            )
            EventFilterChip(
                text = "Personal",
                selected = false,
                backgroundColor = PersonalPink,
                onClick = {}
            )
            EventFilterChip(
                text = "Occasion",
                selected = false,
                backgroundColor = OccasionYellow,
                onClick = {}
            )
        }
    }
}