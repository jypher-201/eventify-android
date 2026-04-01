package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
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

// ─────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────

data class NotificationOption(
    val label: String,
    val value: String
)

private val standardNotificationOptions = listOf(
    NotificationOption("5 minutes before",  "5m"),
    NotificationOption("10 minutes before", "10m"),
    NotificationOption("15 minutes before", "15m"),
    NotificationOption("30 minutes before", "30m"),
    NotificationOption("1 hour before",     "1h"),
    NotificationOption("1 day before",      "1d"),
    NotificationOption("Custom",            "custom")
)

fun getNotificationLabel(value: String): String = when (value) {
    "5m"     -> "5 minutes before"
    "10m"    -> "10 minutes before"
    "15m"    -> "15 minutes before"
    "30m"    -> "30 minutes before"
    "1h"     -> "1 hour before"
    "1d"     -> "1 day before"
    else     -> value   // custom values stored as display string directly
}

fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0  -> 12
        hour > 12  -> hour - 12
        else       -> hour
    }
    return String.format("%d:%02d %s", displayHour, minute, amPm)
}

// ─────────────────────────────────────────────
// AddEventScreen
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {},
    onSaveEvent: (String, EventType, String, String, String) -> Unit = { _, _, _, _, _ -> },
    prefilledDate: String? = null,
    currentTheme: AppTheme = AppTheme.DEFAULT
) {
    // Derive colors from theme
    val accent      = getAccentColor(currentTheme)
    val bgColor     = getBackgroundColor(currentTheme)
    val surfColor   = getSurfaceColor(currentTheme)
    val textColor   = getTextColor(currentTheme)
    val topBarBg    = getTopBarColor(currentTheme)
    val topBarFg    = getTopBarContentColor(currentTheme)
    val isDark      = currentTheme == AppTheme.DARK

    var title     by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.ACADEMIC) }
    var startDate by remember { mutableStateOf(prefilledDate ?: "Feb 25, 2024") }
    var endDate   by remember { mutableStateOf(prefilledDate ?: "Feb 25, 2024") }
    var startTime by remember { mutableStateOf("9:00 AM") }
    var endTime   by remember { mutableStateOf("10:00 AM") }
    var notes     by remember { mutableStateOf("") }
    var isAllDay  by remember { mutableStateOf(false) }

    // Notifications — each entry is a display label string
    var notifications by remember { mutableStateOf(listOf<String>()) }
    var showNotifPicker  by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    // Location
    var location by remember { mutableStateOf("") }

    // Repeat
    var repeatOption     by remember { mutableStateOf("Does not repeat") }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showCustomRepeat by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker   by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker   by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState   = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(initialHour = 9,  initialMinute = 0)
    val endTimePickerState   = rememberTimePickerState(initialHour = 10, initialMinute = 0)

    LaunchedEffect(isAllDay) { if (isAllDay) endDate = startDate }

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label         = "fade_in"
    )
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            AddEventTopBar(
                onNavigateBack = onNavigateBack,
                onSave         = { if (title.isNotBlank()) onSaveEvent(title, selectedType, startDate, startTime, notes) },
                canSave        = title.isNotBlank(),
                topBarBg       = topBarBg,
                topBarFg       = topBarFg,
                accent         = accent
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .graphicsLayer { this.alpha = alpha }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title
            AddEventField(
                value         = title,
                onValueChange = { title = it },
                placeholder   = "Title",
                icon          = Icons.Default.Title,
                accent        = accent,
                surfColor     = surfColor,
                textColor     = textColor,
                isDark        = isDark
            )

            // Type selector
            AddEventTypeSelector(
                selectedType    = selectedType,
                onTypeSelected  = { selectedType = it },
                accent          = accent,
                surfColor       = surfColor,
                isDark          = isDark
            )

            // Date / Time card — Google Calendar style
            DateTimeCard(
                startDate      = startDate,
                endDate        = endDate,
                startTime      = startTime,
                endTime        = endTime,
                isAllDay       = isAllDay,
                onStartDateClick = { showStartDatePicker = true },
                onEndDateClick   = { showEndDatePicker   = true },
                onStartTimeClick = { showStartTimePicker = true },
                onEndTimeClick   = { showEndTimePicker   = true },
                onAllDayChange   = { isAllDay = it },
                accent         = accent,
                surfColor      = surfColor,
                textColor      = textColor,
                isDark         = isDark
            )

            // Notifications card — Google Calendar style
            NotificationsCard(
                notifications      = notifications,
                onAddClick         = { showNotifPicker = true },
                onRemove           = { label -> notifications = notifications - label },
                accent             = accent,
                surfColor          = surfColor,
                textColor          = textColor,
                isDark             = isDark
            )

            // Location card
            LocationCard(
                location      = location,
                onValueChange = { location = it },
                accent        = accent,
                surfColor     = surfColor,
                textColor     = textColor
            )

            // Repeat card
            RepeatCard(
                repeatOption  = repeatOption,
                onClick       = { showRepeatPicker = true },
                accent        = accent,
                surfColor     = surfColor,
                textColor     = textColor
            )

            // Notes
            AddEventNotesField(
                value         = notes,
                onValueChange = { notes = it },
                accent        = accent,
                surfColor     = surfColor,
                textColor     = textColor,
                isDark        = isDark
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // Notification option picker
    if (showNotifPicker) {
        NotificationPickerDialog(
            existing    = notifications,
            accent      = accent,
            surfColor   = surfColor,
            textColor   = textColor,
            onDismiss   = { showNotifPicker = false },
            onSelect    = { value ->
                if (value == "custom") {
                    showNotifPicker  = false
                    showCustomDialog = true
                } else {
                    val label = getNotificationLabel(value)
                    if (!notifications.contains(label)) notifications = notifications + label
                    showNotifPicker = false
                }
            }
        )
    }

    // Custom notification time input
    if (showCustomDialog) {
        CustomNotificationDialog(
            accent    = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showCustomDialog = false },
            onConfirm = { customLabel ->
                if (!notifications.contains(customLabel)) notifications = notifications + customLabel
                showCustomDialog = false
            }
        )
    }

    // Repeat picker
    if (showRepeatPicker) {
        RepeatPickerDialog(
            current   = repeatOption,
            accent    = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showRepeatPicker = false },
            onSelect  = { option ->
                if (option == "Custom…") {
                    showRepeatPicker = false
                    showCustomRepeat = true
                } else {
                    repeatOption     = option
                    showRepeatPicker = false
                }
            }
        )
    }

    if (showCustomRepeat) {
        CustomRepeatDialog(
            accent    = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showCustomRepeat = false },
            onConfirm = { label ->
                repeatOption     = label
                showCustomRepeat = false
            }
        )
    }

    // Date pickers
    if (showStartDatePicker) {
        ThemedDatePickerDialog(
            state    = startDatePickerState,
            accent   = accent,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                startDatePickerState.selectedDateMillis?.let {
                    val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    val d   = fmt.format(Date(it))
                    startDate = d
                    if (isAllDay) endDate = d
                }
                showStartDatePicker = false
            }
        )
    }
    if (showEndDatePicker) {
        ThemedDatePickerDialog(
            state    = endDatePickerState,
            accent   = accent,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                endDatePickerState.selectedDateMillis?.let {
                    endDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(it))
                }
                showEndDatePicker = false
            }
        )
    }
    if (showStartTimePicker) {
        ThemedTimePickerDialog(
            state    = startTimePickerState,
            accent   = accent,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                startTime = formatTime(startTimePickerState.hour, startTimePickerState.minute)
                showStartTimePicker = false
            }
        )
    }
    if (showEndTimePicker) {
        ThemedTimePickerDialog(
            state    = endTimePickerState,
            accent   = accent,
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                endTime = formatTime(endTimePickerState.hour, endTimePickerState.minute)
                showEndTimePicker = false
            }
        )
    }
}

// ─────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────

@Composable
fun AddEventTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    topBarBg: Color,
    topBarFg: Color,
    accent: Color
) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color           = topBarBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(42.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint     = topBarFg,
                    modifier = Modifier.size(25.dp)
                )
            }

            Text(
                "New Event",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = topBarFg
            )

            Surface(
                onClick         = onSave,
                shape           = RoundedCornerShape(11.dp),
                color           = if (canSave) accent else accent.copy(alpha = 0.3f),
                shadowElevation = if (canSave) 3.dp else 0.dp
            ) {
                Text(
                    "Save",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (canSave) White else White.copy(alpha = 0.6f),
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Title Field
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    surfColor: Color,
    textColor: Color,
    isDark: Boolean
) {
    Surface(
        shape           = RoundedCornerShape(13.dp),
        color           = surfColor,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
            }

            TextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = { Text(placeholder, color = textColor.copy(alpha = 0.35f), fontSize = 17.sp) },
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = accent
                ),
                modifier  = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color      = textColor
                ),
                singleLine = true
            )
        }
    }
}

// ─────────────────────────────────────────────
// Type Selector
// ─────────────────────────────────────────────

@Composable
fun AddEventTypeSelector(
    selectedType: EventType,
    onTypeSelected: (EventType) -> Unit,
    accent: Color,
    surfColor: Color,
    isDark: Boolean
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AddEventTypeChip(
            text     = "Academic",
            icon     = Icons.Default.School,
            selected = selectedType == EventType.ACADEMIC,
            onClick  = { onTypeSelected(EventType.ACADEMIC) },
            color    = Color(0xFF667eea),
            surfColor = surfColor,
            modifier = Modifier.weight(1f)
        )
        AddEventTypeChip(
            text     = "Personal",
            icon     = Icons.Default.FitnessCenter,
            selected = selectedType == EventType.PERSONAL,
            onClick  = { onTypeSelected(EventType.PERSONAL) },
            color    = Color(0xFFf093fb),
            surfColor = surfColor,
            modifier = Modifier.weight(1f)
        )
        AddEventTypeChip(
            text     = "Occasion",
            icon     = Icons.Default.Cake,
            selected = selectedType == EventType.OCCASION,
            onClick  = { onTypeSelected(EventType.OCCASION) },
            color    = Color(0xFFfcb69f),
            surfColor = surfColor,
            modifier = Modifier.weight(1f)
        )
        // + Add button
        Surface(
            onClick         = { /* TODO */ },
            modifier        = Modifier.size(54.dp),
            shape           = RoundedCornerShape(12.dp),
            color           = surfColor,
            shadowElevation = 2.dp,
            border          = BorderStroke(1.5.dp, accent.copy(alpha = 0.4f))
        ) {
            Column(
                modifier              = Modifier.fillMaxSize(),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, null, tint = accent, modifier = Modifier.size(22.dp))
                Text("Add", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
            }
        }
    }
}

@Composable
fun AddEventTypeChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color,
    surfColor: Color,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "chip_scale"
    )

    Surface(
        onClick         = onClick,
        modifier        = modifier
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape           = RoundedCornerShape(12.dp),
        color           = if (selected) color else surfColor,
        shadowElevation = if (selected) 5.dp else 2.dp,
        border          = if (!selected) BorderStroke(1.5.dp, color.copy(alpha = 0.25f)) else null
    ) {
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (selected) White else color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(3.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (selected) White else color)
        }
    }
}

// ─────────────────────────────────────────────
// Date / Time Card  (Google Calendar style)
// ─────────────────────────────────────────────

@Composable
fun DateTimeCard(
    startDate: String, endDate: String,
    startTime: String, endTime: String,
    isAllDay: Boolean,
    onStartDateClick: () -> Unit, onEndDateClick: () -> Unit,
    onStartTimeClick: () -> Unit, onEndTimeClick: () -> Unit,
    onAllDayChange: (Boolean) -> Unit,
    accent: Color, surfColor: Color, textColor: Color, isDark: Boolean
) {
    Surface(
        shape           = RoundedCornerShape(13.dp),
        color           = surfColor,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(4.dp)) {

            // All-day row
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.WbSunny, null, tint = accent, modifier = Modifier.size(22.dp))
                Text("All day", fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
                Switch(
                    checked         = isAllDay,
                    onCheckedChange = onAllDayChange,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = White,
                        checkedTrackColor   = accent,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = textColor.copy(alpha = 0.2f)
                    )
                )
            }

            GCalDivider(textColor)

            // Start row
            GCalDateTimeRow(
                date        = startDate,
                time        = if (!isAllDay) startTime else null,
                onDateClick = onStartDateClick,
                onTimeClick = onStartTimeClick,
                accent      = accent,
                textColor   = textColor,
                isFirst     = true
            )

            GCalDivider(textColor)

            // End row
            GCalDateTimeRow(
                date        = endDate,
                time        = if (!isAllDay) endTime else null,
                onDateClick = onEndDateClick,
                onTimeClick = onEndTimeClick,
                accent      = accent,
                textColor   = textColor,
                isFirst     = false
            )
        }
    }
}

@Composable
private fun GCalDivider(textColor: Color) {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 14.dp),
        color     = textColor.copy(alpha = 0.07f),
        thickness = 1.dp
    )
}

@Composable
private fun GCalDateTimeRow(
    date: String,
    time: String?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    accent: Color,
    textColor: Color,
    isFirst: Boolean
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Left label
        Text(
            if (isFirst) "Start" else "End",
            fontSize  = 12.sp,
            color     = textColor.copy(alpha = 0.45f),
            modifier  = Modifier.width(36.dp)
        )

        // Date chip
        Surface(
            onClick = onDateClick,
            shape   = RoundedCornerShape(8.dp),
            color   = accent.copy(alpha = 0.08f)
        ) {
            Text(
                date,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = accent,
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }

        if (time != null) {
            Spacer(Modifier.width(8.dp))
            // Time chip
            Surface(
                onClick = onTimeClick,
                shape   = RoundedCornerShape(8.dp),
                color   = Color.Transparent
            ) {
                Text(
                    time,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = textColor.copy(alpha = 0.65f),
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Notifications Card  (Google Calendar style)
// ─────────────────────────────────────────────

@Composable
fun NotificationsCard(
    notifications: List<String>,
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit,
    accent: Color,
    surfColor: Color,
    textColor: Color,
    isDark: Boolean
) {
    Surface(
        shape           = RoundedCornerShape(13.dp),
        color           = surfColor,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(4.dp)) {

            // Each existing notification row
            notifications.forEachIndexed { index, label ->
                if (index > 0) GCalNotifDivider(textColor)
                NotificationRow(
                    label     = label,
                    onRemove  = { onRemove(label) },
                    accent    = accent,
                    textColor = textColor
                )
            }

            // Divider only if there were existing rows
            if (notifications.isNotEmpty()) GCalNotifDivider(textColor)

            // "Add notification" row — always shown
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable { onAddClick() }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.NotificationsNone,
                    null,
                    tint     = accent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Add notification",
                    fontSize = 16.sp,
                    color    = accent
                )
            }
        }
    }
}

@Composable
private fun GCalNotifDivider(textColor: Color) {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 14.dp),
        color     = textColor.copy(alpha = 0.07f),
        thickness = 1.dp
    )
}

@Composable
private fun NotificationRow(
    label: String,
    onRemove: () -> Unit,
    accent: Color,
    textColor: Color
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Notifications,
            null,
            tint     = accent,
            modifier = Modifier.size(22.dp)
        )
        Text(
            label,
            fontSize = 15.sp,
            color    = textColor,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                "Remove",
                tint     = textColor.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Notification Picker Dialog
// ─────────────────────────────────────────────

@Composable
fun NotificationPickerDialog(
    existing: List<String>,
    accent: Color,
    surfColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Add notification",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                standardNotificationOptions.forEach { option ->
                    val alreadyAdded = existing.contains(getNotificationLabel(option.value))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !alreadyAdded) { onSelect(option.value) }
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (alreadyAdded) Icons.Default.Check else Icons.Default.NotificationsNone,
                            null,
                            tint     = if (alreadyAdded) accent else textColor.copy(alpha = 0.45f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            option.label,
                            fontSize = 15.sp,
                            color    = if (alreadyAdded) accent else textColor
                        )
                    }
                    if (option != standardNotificationOptions.last()) {
                        HorizontalDivider(color = textColor.copy(alpha = 0.06f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ─────────────────────────────────────────────
// Custom Notification Dialog
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomNotificationDialog(
    accent: Color,
    surfColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var minutes by remember { mutableStateOf("") }
    var unit    by remember { mutableStateOf("minutes") } // "minutes" | "hours" | "days"
    val units   = listOf("minutes", "hours", "days")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(16.dp),
        title = {
            Text("Custom notification", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("How long before the event?", fontSize = 14.sp, color = textColor.copy(alpha = 0.6f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = minutes,
                        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) minutes = it },
                        label         = { Text("Amount") },
                        singleLine    = true,
                        modifier      = Modifier.width(90.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            focusedLabelColor  = accent,
                            cursorColor        = accent,
                            unfocusedTextColor = textColor,
                            focusedTextColor   = textColor
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        units.forEach { u ->
                            Row(
                                modifier          = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { unit = u }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RadioButton(
                                    selected = unit == u,
                                    onClick  = { unit = u },
                                    colors   = RadioButtonDefaults.colors(selectedColor = accent),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(u.replaceFirstChar { it.uppercase() }, fontSize = 14.sp, color = textColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = {
                    if (minutes.isNotBlank()) {
                        val label = "$minutes $unit before"
                        onConfirm(label)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                color = if (minutes.isNotBlank()) accent else accent.copy(alpha = 0.3f)
            ) {
                Text(
                    "Add",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = White,
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textColor.copy(alpha = 0.6f))
            }
        }
    )
}

// ─────────────────────────────────────────────
// Notes Field
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventNotesField(
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    surfColor: Color,
    textColor: Color,
    isDark: Boolean
) {
    Surface(
        shape           = RoundedCornerShape(13.dp),
        color           = surfColor,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Notes,
                    null,
                    tint     = accent,
                    modifier = Modifier.size(21.dp)
                )
            }

            TextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = {
                    Text(
                        "Add notes or location…",
                        color    = textColor.copy(alpha = 0.35f),
                        fontSize = 16.sp
                    )
                },
                colors    = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = accent,
                    unfocusedTextColor      = textColor,
                    focusedTextColor        = textColor
                ),
                modifier  = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 70.dp, max = 130.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = textColor)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Date / Time Pickers
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedDatePickerDialog(
    state: DatePickerState,
    accent: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton    = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accent)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = White),
        shape  = RoundedCornerShape(16.dp)
    ) {
        DatePicker(
            state  = state,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = accent,
                todayContentColor         = accent,
                todayDateBorderColor      = accent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedTimePickerDialog(
    state: TimePickerState,
    accent: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = White,
        shape            = RoundedCornerShape(16.dp),
        title            = {
            Text("Select time", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        },
        text = {
            TimePicker(
                state  = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor                    = Color(0xFFF5F5F5),
                    selectorColor                     = accent,
                    timeSelectorSelectedContainerColor = accent,
                    timeSelectorUnselectedContainerColor = Color(0xFFF5F5F5),
                    timeSelectorSelectedContentColor  = White,
                    timeSelectorUnselectedContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accent)
            }
        }
    )
}

// ─────────────────────────────────────────────
// Location Card
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCard(
    location: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    surfColor: Color,
    textColor: Color
) {
    Surface(
        shape           = RoundedCornerShape(13.dp),
        color           = surfColor,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                null,
                tint     = accent,
                modifier = Modifier.size(22.dp)
            )
            TextField(
                value         = location,
                onValueChange = onValueChange,
                placeholder   = {
                    Text(
                        "Add location",
                        color    = textColor.copy(alpha = 0.38f),
                        fontSize = 16.sp
                    )
                },
                colors    = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = accent,
                    unfocusedTextColor      = textColor,
                    focusedTextColor        = textColor
                ),
                modifier  = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color    = textColor
                ),
                singleLine = true
            )
        }
    }
}

// ─────────────────────────────────────────────
// Repeat Card
// ─────────────────────────────────────────────

@Composable
fun RepeatCard(
    repeatOption: String,
    onClick: () -> Unit,
    accent: Color,
    surfColor: Color,
    textColor: Color
) {
    Surface(
        shape           = RoundedCornerShape(13.dp),
        color           = surfColor,
        shadowElevation = 2.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Repeat,
                null,
                tint     = accent,
                modifier = Modifier.size(22.dp)
            )
            Text(
                repeatOption,
                fontSize = 16.sp,
                color    = if (repeatOption == "Does not repeat")
                    textColor.copy(alpha = 0.55f)
                else
                    textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ExpandMore,
                null,
                tint     = textColor.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Repeat Picker Dialog
// ─────────────────────────────────────────────

private val repeatOptions = listOf(
    "Does not repeat",
    "Every day",
    "Every week",
    "Every month",
    "Every year",
    "Custom…"
)

@Composable
fun RepeatPickerDialog(
    current: String,
    accent: Color,
    surfColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Repeat",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                repeatOptions.forEach { option ->
                    val selected = option == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }
                            .padding(horizontal = 4.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Leading radio-style dot
                        Box(
                            modifier = Modifier
                                .size(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Default.RadioButtonChecked,
                                    null,
                                    tint     = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.RadioButtonUnchecked,
                                    null,
                                    tint     = textColor.copy(alpha = 0.35f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            option,
                            fontSize   = 15.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            color      = if (selected) accent else textColor
                        )
                    }
                    if (option != repeatOptions.last()) {
                        HorizontalDivider(color = textColor.copy(alpha = 0.06f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ─────────────────────────────────────────────
// Custom Repeat Dialog
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRepeatDialog(
    accent: Color,
    surfColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var every    by remember { mutableStateOf("1") }
    var unit     by remember { mutableStateOf("week") }
    val units    = listOf("day", "week", "month", "year")

    // Derived label preview
    val preview = if (every.isNotBlank() && every != "0") {
        val num = every.toIntOrNull() ?: 1
        val unitLabel = if (num == 1) unit else "${unit}s"
        "Every $every $unitLabel"
    } else ""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Custom repeat",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Repeat every",
                    fontSize = 14.sp,
                    color    = textColor.copy(alpha = 0.6f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Number input
                    OutlinedTextField(
                        value         = every,
                        onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) every = it },
                        singleLine    = true,
                        modifier      = Modifier.width(72.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor  = accent,
                            focusedLabelColor   = accent,
                            cursorColor         = accent,
                            unfocusedTextColor  = textColor,
                            focusedTextColor    = textColor
                        )
                    )

                    // Unit chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        units.forEach { u ->
                            val sel = unit == u
                            Surface(
                                onClick = { unit = u },
                                shape   = RoundedCornerShape(20.dp),
                                color   = if (sel) accent else accent.copy(alpha = 0.08f),
                                border  = if (!sel) BorderStroke(1.dp, accent.copy(alpha = 0.25f)) else null
                            ) {
                                Text(
                                    u.replaceFirstChar { it.uppercase() },
                                    fontSize  = 12.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color     = if (sel) White else accent,
                                    modifier  = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Preview label
                if (preview.isNotBlank()) {
                    Text(
                        preview,
                        fontSize   = 13.sp,
                        color      = accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = { if (preview.isNotBlank()) onConfirm(preview) },
                shape   = RoundedCornerShape(10.dp),
                color   = if (preview.isNotBlank()) accent else accent.copy(alpha = 0.3f)
            ) {
                Text(
                    "Done",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = White,
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textColor.copy(alpha = 0.6f))
            }
        }
    )
}