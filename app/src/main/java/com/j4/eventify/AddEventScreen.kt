package com.j4.eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {},
    onSaveEvent: (String, EventType, String, String, String) -> Unit = { _, _, _, _, _ -> },
    prefilledDate: String? = null  // ← ADD THIS
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.ACADEMIC) }
    var showTypeDialog by remember { mutableStateOf(false) }

    var isAllDay by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(prefilledDate ?: "Feb 25, 2024") }  // ← USE prefilledDate
    var startTime by remember { mutableStateOf("10:00 AM") }
    var endDate by remember { mutableStateOf(prefilledDate ?: "Feb 25, 2024") }    // ← USE prefilledDate
    var endTime by remember { mutableStateOf("11:00 AM") }

    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var repeatOption by remember { mutableStateOf("Does not repeat") }
    var showRepeatDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AddEventTopBar(onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Event Title
            NeoTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Event title",
                icon = Icons.Default.Edit,
                singleLine = true
            )

            // Event Type Selector
            NeoClickableField(
                label = selectedType.name,
                icon = Icons.AutoMirrored.Filled.Label,
                iconTint = getEventTypeColor(selectedType),
                onClick = { showTypeDialog = true }
            )

            // Start and End Date/Time Boxes
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Combined Date & Time Box (Start and End in one)
                NeoDateTimeBox(
                    startDate = startDate,
                    startTime = startTime,
                    endDate = endDate,
                    endTime = endTime,
                    isAllDay = isAllDay,
                    onAllDayToggle = { isAllDay = it },
                    onStartDateClick = { /* TODO: Show date picker */ },
                    onStartTimeClick = { /* TODO: Show time picker */ },
                    onEndDateClick = { /* TODO: Show date picker */ },
                    onEndTimeClick = { /* TODO: Show time picker */ }
                )
            }

            // Repeat
            NeoClickableField(
                label = repeatOption,
                icon = Icons.Default.Repeat,
                onClick = { showRepeatDialog = true }
            )

            // Location
            NeoTextField(
                value = location,
                onValueChange = { location = it },
                placeholder = "Add location",
                icon = Icons.Default.LocationOn,
                singleLine = true
            )

            // Notes
            NeoTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Add notes (optional)",
                icon = Icons.AutoMirrored.Filled.Notes,
                singleLine = false,
                minLines = 3
            )

            // Save Button
            NeoButton(
                text = "Save Event",
                onClick = {
                    onSaveEvent(title, selectedType, startDate, startTime, notes)
                    onNavigateBack()
                },
                backgroundColor = getEventTypeColor(selectedType),
                enabled = title.isNotEmpty()
            )
        }
    }

    // Type Selection Dialog
    if (showTypeDialog) {
        TypeSelectionDialog(
            selectedType = selectedType,
            onTypeSelected = {
                selectedType = it
                showTypeDialog = false
            },
            onAddCustomType = {
                showTypeDialog = false
                // TODO: In Phase 2, implement custom type addition
            },
            onDismiss = { showTypeDialog = false }
        )
    }

    // Repeat Options Dialog
    if (showRepeatDialog) {
        RepeatOptionsDialog(
            selectedOption = repeatOption,
            onOptionSelected = {
                repeatOption = it
                showRepeatDialog = false
            },
            onDismiss = { showRepeatDialog = false }
        )
    }
}  // ← END of AddEventScreen

// ← NeoDateTimeBox should be HERE (separate function)
@Composable
fun NeoDateTimeBox(
    startDate: String,
    startTime: String,
    endDate: String,
    endTime: String,
    isAllDay: Boolean,
    onAllDayToggle: (Boolean) -> Unit,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(3.dp, Black, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: All Day Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "All day",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
                Switch(
                    checked = isAllDay,
                    onCheckedChange = onAllDayToggle,
                    modifier = Modifier.height(24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Black,
                        uncheckedThumbColor = Black,
                        uncheckedTrackColor = Color(0xFFE0E0E0),
                        checkedBorderColor = Black,
                        uncheckedBorderColor = Black
                    )
                )
            }
        }

        // Start Date & Time Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start Date
            Row(
                modifier = Modifier
                    .weight(if (isAllDay) 1f else 0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(2.dp, Black, RoundedCornerShape(8.dp))
                    .clickable(onClick = onStartDateClick)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = startDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Start Time (hidden if all day)
            if (!isAllDay) {
                Row(
                    modifier = Modifier
                        .weight(0.4f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(2.dp, Black, RoundedCornerShape(8.dp))
                        .clickable(onClick = onStartTimeClick)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = startTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        // End Date & Time Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // End Date
            Row(
                modifier = Modifier
                    .weight(if (isAllDay) 1f else 0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(2.dp, Black, RoundedCornerShape(8.dp))
                    .clickable(onClick = onEndDateClick)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = endDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            // End Time (hidden if all day)
            if (!isAllDay) {
                Row(
                    modifier = Modifier
                        .weight(0.4f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(2.dp, Black, RoundedCornerShape(8.dp))
                        .clickable(onClick = onEndTimeClick)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = endTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun AddEventTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .statusBarsPadding()  // ← Prevents overlap with status bar
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Black
            )
        }
        Text(
            text = "Add Event",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )
    }
}

@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(3.dp, Black, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Black,
            modifier = Modifier.size(24.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = Black,
                fontWeight = FontWeight.Medium
            ),
            singleLine = singleLine,
            minLines = minLines,
            cursorBrush = SolidColor(Black),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun NeoClickableField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = Black,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(3.dp, Black, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Black,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = Black
        )
    }
}

@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val textColor = if (backgroundColor == OccasionYellow) Black else White

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Black)
        )

        // Button
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) backgroundColor else Color.Gray,
                contentColor = textColor,
                disabledContainerColor = Color.Gray,
                disabledContentColor = White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            border = androidx.compose.foundation.BorderStroke(4.dp, Black)
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun TypeSelectionDialog(
    selectedType: EventType,
    onTypeSelected: (EventType) -> Unit,
    onAddCustomType: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = {
            Text(
                text = "Select Event Type",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EventType.entries.forEach { type ->
                    TypeOption(
                        type = type,
                        isSelected = type == selectedType,
                        onClick = { onTypeSelected(type) }
                    )
                }

                HorizontalDivider(thickness = 2.dp, color = Black, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddCustomType)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Black
                    )
                    Text(
                        text = "Add custom type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun TypeOption(
    type: EventType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) getEventTypeColor(type).copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) getEventTypeColor(type) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(getEventTypeColor(type))
                .border(2.dp, Black, RoundedCornerShape(4.dp))
        )

        Text(
            text = type.name,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = Black
        )
    }
}

@Composable
fun RepeatOptionsDialog(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "Does not repeat",
        "Daily",
        "Weekly",
        "Monthly",
        "Yearly",
        "Every weekday (Mon-Fri)",
        "Custom"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = {
            Text(
                text = "Repeat",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (option == selectedOption) Color(0xFFE8F0FE) else Color.Transparent)
                            .clickable { onOptionSelected(option) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            color = Black
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddEventScreenPreview() {
    EventifyTheme {
        AddEventScreen()
    }
}