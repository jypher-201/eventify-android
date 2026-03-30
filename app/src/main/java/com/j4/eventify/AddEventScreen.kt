package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import androidx.compose.foundation.layout.statusBarsPadding
import java.text.SimpleDateFormat
import java.util.*

// Reminder data class
data class ReminderOption(
    val label: String,
    val value: String  // e.g., "15m", "1h", "1d", "1w", "at_time"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {},
    onSaveEvent: (String, EventType, String, String, String) -> Unit = { _, _, _, _, _ -> },
    prefilledDate: String? = null
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.ACADEMIC) }
    var startDate by remember { mutableStateOf(prefilledDate ?: "Feb 25, 2024") }
    var endDate by remember { mutableStateOf(prefilledDate ?: "Feb 25, 2024") }
    var startTime by remember { mutableStateOf("9:00 AM") }
    var endTime by remember { mutableStateOf("10:00 AM") }
    var notes by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }

    // REMINDER STATES
    var selectedReminders by remember { mutableStateOf(listOf<String>()) }  // List of selected reminder values
    var showReminderMenu by remember { mutableStateOf(false) }

    // Dialog states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Date picker states
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    // Time picker states
    val startTimePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    val endTimePickerState = rememberTimePickerState(initialHour = 10, initialMinute = 0)

    // Smart all-day logic: When toggled ON, set end date = start date
    LaunchedEffect(isAllDay) {
        if (isAllDay) {
            endDate = startDate
        }
    }

    // Smooth entrance animation
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "fade_in"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8F9FA),
            Color(0xFFFFFFFF)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            UltimateTopBar(
                onNavigateBack = onNavigateBack,
                onSave = {
                    if (title.isNotBlank()) {
                        onSaveEvent(title, selectedType, startDate, startTime, notes)
                    }
                },
                canSave = title.isNotBlank()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .graphicsLayer { this.alpha = alpha }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Input
                UltimateTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Event Title *",
                    icon = Icons.Default.Title
                )

                // Event Type Selector
                UltimateTypeSelector(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it }
                )

                // Date & Time Section
                UltimateDateTimeSection(
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    isAllDay = isAllDay,
                    onStartDateClick = { showStartDatePicker = true },
                    onEndDateClick = { showEndDatePicker = true },
                    onStartTimeClick = { showStartTimePicker = true },
                    onEndTimeClick = { showEndTimePicker = true },
                    onAllDayChange = { isAllDay = it }
                )

                // REMINDER SECTION (NEW!)
                ReminderSection(
                    selectedReminders = selectedReminders,
                    onAddReminderClick = { showReminderMenu = true },
                    onRemoveReminder = { reminderValue ->
                        selectedReminders = selectedReminders.filter { it != reminderValue }
                    }
                )

                // Notes Field
                UltimateNotesField(
                    value = notes,
                    onValueChange = { notes = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // REMINDER MENU DIALOG (NEW!)
    if (showReminderMenu) {
        ReminderMenuDialog(
            selectedReminders = selectedReminders,
            onDismiss = { showReminderMenu = false },
            onReminderSelected = { reminderValue ->
                if (!selectedReminders.contains(reminderValue)) {
                    selectedReminders = selectedReminders + reminderValue
                }
                showReminderMenu = false
            }
        )
    }

    // Date Picker Dialogs
    if (showStartDatePicker) {
        ModernDatePickerDialog(
            state = startDatePickerState,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                startDatePickerState.selectedDateMillis?.let { millis ->
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    val newDate = formatter.format(Date(millis))
                    startDate = newDate
                    // Smart: if all-day, update end date too
                    if (isAllDay) {
                        endDate = newDate
                    }
                }
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        ModernDatePickerDialog(
            state = endDatePickerState,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                endDatePickerState.selectedDateMillis?.let { millis ->
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    endDate = formatter.format(Date(millis))
                }
                showEndDatePicker = false
            }
        )
    }

    // Time Picker Dialogs
    if (showStartTimePicker) {
        ModernTimePickerDialog(
            state = startTimePickerState,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val hour = startTimePickerState.hour
                val minute = startTimePickerState.minute
                startTime = formatTime(hour, minute)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        ModernTimePickerDialog(
            state = endTimePickerState,
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                val hour = endTimePickerState.hour
                val minute = endTimePickerState.minute
                endTime = formatTime(hour, minute)
                showEndTimePicker = false
            }
        )
    }
}

// NEW COMPONENT: Reminder Section
@Composable
fun ReminderSection(
    selectedReminders: List<String>,
    onAddReminderClick: () -> Unit,
    onRemoveReminder: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with icon and "Add Reminder" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFF667eea).copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Notifications,
                            null,
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    "Reminders",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    onClick = onAddReminderClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF667eea).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Add",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667eea)
                        )
                    }
                }
            }

            // Display selected reminders
            if (selectedReminders.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedReminders.forEach { reminderValue ->
                        ReminderChip(
                            label = getReminderLabel(reminderValue),
                            onRemove = { onRemoveReminder(reminderValue) }
                        )
                    }
                }
            } else {
                // Empty state
                Text(
                    "No reminders set",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 50.dp, top = 4.dp)
                )
            }
        }
    }
}

// NEW COMPONENT: Individual Reminder Chip
@Composable
fun ReminderChip(
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    null,
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    "Remove",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// NEW COMPONENT: Reminder Menu Dialog
@Composable
fun ReminderMenuDialog(
    selectedReminders: List<String>,
    onDismiss: () -> Unit,
    onReminderSelected: (String) -> Unit
) {
    val reminderOptions = listOf(
        ReminderOption("At time of event", "at_time"),
        ReminderOption("5 minutes before", "5m"),
        ReminderOption("15 minutes before", "15m"),
        ReminderOption("30 minutes before", "30m"),
        ReminderOption("1 hour before", "1h"),
        ReminderOption("2 hours before", "2h"),
        ReminderOption("1 day before", "1d"),
        ReminderOption("2 days before", "2d"),
        ReminderOption("1 week before", "1w")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Add Reminder",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                reminderOptions.forEach { option ->
                    val isSelected = selectedReminders.contains(option.value)

                    Surface(
                        onClick = { onReminderSelected(option.value) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF667eea).copy(alpha = 0.1f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                option.label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF667eea) else Color(0xFF1A1A1A)
                            )

                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color(0xFF667eea),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF667eea)
            ) {
                Text(
                    "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    )
}

// Helper function to get reminder label from value
fun getReminderLabel(value: String): String {
    return when (value) {
        "at_time" -> "At time of event"
        "5m" -> "5 minutes before"
        "15m" -> "15 minutes before"
        "30m" -> "30 minutes before"
        "1h" -> "1 hour before"
        "2h" -> "2 hours before"
        "1d" -> "1 day before"
        "2d" -> "2 days before"
        "1w" -> "1 week before"
        else -> value
    }
}

// Helper function to format time
fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", displayHour, minute, amPm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDatePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF667eea)
            ) {
                Text(
                    "OK",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Color(0xFF667eea),
                todayContentColor = Color(0xFF667eea),
                todayDateBorderColor = Color(0xFF667eea)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTimePickerDialog(
    state: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Select Time",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = Color(0xFFF5F5F5),
                    selectorColor = Color(0xFF667eea),
                    timeSelectorSelectedContainerColor = Color(0xFF667eea),
                    timeSelectorUnselectedContainerColor = Color(0xFFF5F5F5),
                    timeSelectorSelectedContentColor = White,
                    timeSelectorUnselectedContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        confirmButton = {
            Surface(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF667eea)
            ) {
                Text(
                    "OK",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    )
}

@Composable
fun UltimateTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(25.dp)
                )
            }

            Text(
                "New Event",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Surface(
                onClick = onSave,
                shape = RoundedCornerShape(11.dp),
                color = if (canSave) Color(0xFF667eea) else Color(0xFFE0E0E0),
                shadowElevation = if (canSave) 3.dp else 0.dp
            ) {
                Text(
                    "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) White else Color.Gray,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
fun UltimateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = Color(0xFF667eea).copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        placeholder,
                        color = Color.Gray,
                        fontSize = 17.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun UltimateTypeSelector(
    selectedType: EventType,
    onTypeSelected: (EventType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UltimateTypeChip(
            text = "Academic",
            icon = Icons.Default.School,
            selected = selectedType == EventType.ACADEMIC,
            onClick = { onTypeSelected(EventType.ACADEMIC) },
            color = Color(0xFF667eea),
            modifier = Modifier.weight(1f)
        )

        UltimateTypeChip(
            text = "Personal",
            icon = Icons.Default.FitnessCenter,
            selected = selectedType == EventType.PERSONAL,
            onClick = { onTypeSelected(EventType.PERSONAL) },
            color = Color(0xFFf093fb),
            modifier = Modifier.weight(1f)
        )

        UltimateTypeChip(
            text = "Occasion",
            icon = Icons.Default.Cake,
            selected = selectedType == EventType.OCCASION,
            onClick = { onTypeSelected(EventType.OCCASION) },
            color = Color(0xFFfcb69f),
            modifier = Modifier.weight(1f)
        )

        AddTypeButton(
            onClick = { /* TODO */ }
        )
    }
}

@Composable
fun UltimateTypeChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color else White,
        shadowElevation = if (selected) 5.dp else 2.dp,
        border = if (!selected) BorderStroke(1.5.dp, Color(0xFFE8E8E8)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) White else color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) White else Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
fun AddTypeButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(54.dp)
            .height(54.dp),
        shape = RoundedCornerShape(12.dp),
        color = White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.5.dp, Color(0xFF667eea).copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    "Add Type",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Add",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667eea)
                )
            }
        }
    }
}

@Composable
fun UltimateDateTimeSection(
    startDate: String,
    endDate: String,
    startTime: String,
    endTime: String,
    isAllDay: Boolean,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onAllDayChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // All-day Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFF667eea).copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    "All-day",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = isAllDay,
                    onCheckedChange = onAllDayChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Color(0xFF667eea),
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // Start Date & Time Row
            UltimateDateTimeRow(
                label = "Start",
                date = startDate,
                time = startTime,
                showTime = !isAllDay,
                onDateClick = onStartDateClick,
                onTimeClick = onStartTimeClick
            )

            // End Date & Time Row
            UltimateDateTimeRow(
                label = "End",
                date = endDate,
                time = endTime,
                showTime = !isAllDay,
                onDateClick = onEndDateClick,
                onTimeClick = onEndTimeClick
            )
        }
    }
}

@Composable
fun UltimateDateTimeRow(
    label: String,
    date: String,
    time: String,
    showTime: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),  // ← REDUCED from 8dp to 4dp
        verticalArrangement = Arrangement.spacedBy(4.dp)  // ← REDUCED from 5dp to 4dp
    ) {
        // Label at top left
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        // Date and Time row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date
            Surface(
                onClick = onDateClick,
                color = Color.Transparent
            ) {
                Text(
                    date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            }

            // Time (if not all-day)
            if (showTime) {
                Surface(
                    onClick = onTimeClick,
                    color = Color.Transparent
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            time,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun UltimateNotesField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = Color(0xFF667eea).copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        "Add notes or location...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 70.dp, max = 130.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
            )
        }
    }
}