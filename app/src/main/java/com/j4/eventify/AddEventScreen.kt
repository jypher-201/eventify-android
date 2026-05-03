package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventType
import com.j4.eventify.components.EventTypeConfig
import com.j4.eventify.components.gradientPalette
import com.j4.eventify.components.textColorForGradient
import com.j4.eventify.components.badgeColorForGradient
import com.j4.eventify.ui.theme.*
import androidx.compose.foundation.layout.statusBarsPadding
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.j4.eventify.data.EventViewModel
import com.j4.eventify.data.EventViewModelFactory
import com.j4.eventify.data.local.EventEntity

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
    NotificationOption("2 hours before",    "2h"),  // <--- Added!
    NotificationOption("1 day before",      "1d"),
    NotificationOption("2 days before",     "2d"),  // <--- Added!
    NotificationOption("1 week before",     "1w"),  // <--- Added!
    NotificationOption("Custom",            "custom")
)

// ── THE FIX: Dynamically search the list instead of hardcoding a 'when' block! ──
fun getNotificationLabel(value: String): String {
    return standardNotificationOptions.find { it.value == value }?.label ?: value
}

fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0  -> 12
        hour > 12  -> hour - 12
        else       -> hour
    }
    return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
}

fun parseNotificationToMinutes(label: String): Int? {
    val parts = label.lowercase().split(" ")
    val amount = parts.firstOrNull()?.toIntOrNull() ?: return null
    return when {
        parts.any { it.startsWith("minute") } -> amount
        parts.any { it.startsWith("hour") }   -> amount * 60
        parts.any { it.startsWith("day") }    -> amount * 60 * 24
        parts.any { it.startsWith("week") }   -> amount * 60 * 24 * 7
        else -> null
    }
}

fun formatMinutesToNotification(minutes: Int): String {
    return when {
        minutes % (60 * 24 * 7) == 0 -> "${minutes / (60 * 24 * 7)} ${if (minutes == 60 * 24 * 7) "week" else "weeks"} before"
        minutes % (60 * 24) == 0     -> "${minutes / (60 * 24)} ${if (minutes == 60 * 24) "day" else "days"} before"
        minutes % 60 == 0            -> "${minutes / 60} ${if (minutes == 60) "hour" else "hours"} before"
        else                         -> "$minutes minutes before"
    }
}

// ─────────────────────────────────────────────
// AddEventScreen
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit = {},
    prefilledDate: String? = null,
    currentTheme: AppTheme = AppTheme.DEFAULT,
    registry: EventTypeRegistry,
    prefilledEvent: Event? = null,
    viewModel: EventViewModel = viewModel(factory = EventViewModelFactory)
) {
    val isEditMode = prefilledEvent != null

    val accent    = getAccentColor(currentTheme)
    val bgColor   = getBackgroundColor(currentTheme)
    val surfColor = getSurfaceColor(currentTheme)
    val textColor = getTextColor(currentTheme)
    val topBarBg  = getTopBarColor(currentTheme)
    val topBarFg  = getTopBarContentColor(currentTheme)
    val isDark    = currentTheme == AppTheme.DARK

    var title        by remember { mutableStateOf(prefilledEvent?.title ?: "") }
    var selectedType by remember { mutableStateOf(prefilledEvent?.type ?: EventType.ACADEMIC) }

    var customTypes by remember {
        mutableStateOf(
            if (prefilledEvent?.customConfig != null) listOf(prefilledEvent.customConfig)
            else listOf<EventTypeConfig>()
        )
    }
    var selectedCustomCfg    by remember { mutableStateOf(prefilledEvent?.customConfig) }
    var showCustomTypeDialog by remember { mutableStateOf(false) }
    var showCustomTypePicker by remember { mutableStateOf(false) }

    val isPrefilledAllDay = prefilledEvent?.isAllDay == true

    var startDate by remember {
        mutableStateOf(
            prefilledEvent?.rawStartMs?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) }
                ?: prefilledDate ?: SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        )
    }

    var endDate by remember {
        mutableStateOf(
            prefilledEvent?.rawEndMs?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) }
                ?: startDate
        )
    }

    var isAllDay by remember { mutableStateOf(isPrefilledAllDay) }

    val calendar = remember { Calendar.getInstance() }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    var startTime by remember {
        mutableStateOf(
            prefilledEvent?.rawStartMs?.let { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it)) }
                ?.takeIf { !isPrefilledAllDay } ?: formatTime(currentHour, currentMinute)
        )
    }

    var endTime by remember {
        mutableStateOf(
            prefilledEvent?.rawEndMs?.let { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it)) }
                ?.takeIf { !isPrefilledAllDay } ?: formatTime((currentHour + 1) % 24, currentMinute)
        )
    }

    var notes     by remember { mutableStateOf(prefilledEvent?.notes ?: "") }

    // ── THE FIX: Explicitly tell Kotlin this is a List of Strings! ──
    var notifications by remember {
        mutableStateOf<List<String>>(
            prefilledEvent?.remindBeforeMinutes?.map { formatMinutesToNotification(it) } ?: emptyList()
        )
    }
    var showNotifPicker  by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    var locationName by remember { mutableStateOf(prefilledEvent?.locationName ?: "") }
    var locationLat  by remember { mutableStateOf(prefilledEvent?.latitude) }
    var locationLon  by remember { mutableStateOf(prefilledEvent?.longitude) }

    var repeatOption by remember { mutableStateOf(prefilledEvent?.repeatMode ?: "Does not repeat") }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showCustomRepeat by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker   by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker   by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showNotifPicker = true
        else showPermissionDialog = true
    }

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
                onSave = {
                    if (title.isNotBlank()) {
                        val finalStartTime = if (isAllDay) "12:00 AM" else startTime
                        val startFullDateString = "$startDate at $finalStartTime"
                        val format = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
                        val startTimestamp = try {
                            format.parse(startFullDateString)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) { System.currentTimeMillis() }

                        val finalEndTime = if (isAllDay) "12:00 AM" else endTime
                        val endFullDateString = "$endDate at $finalEndTime"
                        val endTimestamp = try {
                            format.parse(endFullDateString)?.time
                        } catch (e: Exception) { null }

                        val chosenGradient = if (selectedType == EventType.CUSTOM) {
                            com.j4.eventify.components.gradientPalette.indexOfFirst {
                                it.first == selectedCustomCfg?.gradientStart
                            }.coerceAtLeast(0)
                        } else {
                            when (selectedType) {
                                EventType.ACADEMIC -> registry.academic.gradientIndex
                                EventType.PERSONAL -> registry.personal.gradientIndex
                                EventType.OCCASION -> registry.occasion.gradientIndex
                                else -> 0
                            }
                        }

                        val remindMinutesList = notifications.mapNotNull { parseNotificationToMinutes(it) }

                        val newEvent = EventEntity(
                            id = prefilledEvent?.id ?: 0,
                            title = title.trim(),
                            description = notes.trim().ifEmpty { null },
                            eventType = selectedType.name,
                            timestamp = startTimestamp,
                            endTimestamp = endTimestamp,
                            locationName = locationName.trim().ifEmpty { null },
                            latitude = locationLat,
                            longitude = locationLon,
                            remindBeforeMinutes = remindMinutesList,
                            gradientIndex = chosenGradient,
                            customLabel = selectedCustomCfg?.label,
                            repeatMode = if (repeatOption == "Does not repeat") null else repeatOption,
                            isAllDay = isAllDay
                        )

                        viewModel.addEvent(newEvent, context)
                        onNavigateBack()
                    }
                },
                canSave        = title.isNotBlank(),
                topBarBg       = topBarBg,
                topBarFg       = topBarFg,
                accent         = accent,
                isEditMode     = isEditMode
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // ── THE FIX: Dynamically shrinks the screen so the keyboard doesn't cover it ──
                .imePadding()
                .verticalScroll(rememberScrollState())
                .graphicsLayer { this.alpha = alpha }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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

            AddEventTypeSelector(
                selectedType      = selectedType,
                onTypeSelected    = { type ->
                    selectedType      = type
                    selectedCustomCfg = null
                },
                customTypes       = customTypes,
                selectedCustomCfg = selectedCustomCfg,
                onCustomSelected  = { cfg ->
                    selectedType      = EventType.CUSTOM
                    selectedCustomCfg = cfg
                },
                onAddCustomClick  = { showCustomTypePicker = true },
                accent            = accent,
                surfColor         = surfColor,
                isDark            = isDark,
                registry          = registry
            )

            DateTimeCard(
                startDate        = startDate,
                endDate          = endDate,
                startTime        = startTime,
                endTime          = endTime,
                isAllDay         = isAllDay,
                onStartDateClick = { showStartDatePicker = true },
                onEndDateClick   = { showEndDatePicker   = true },
                onStartTimeClick = { showStartTimePicker = true },
                onEndTimeClick   = { showEndTimePicker   = true },
                onAllDayChange   = { isAllDay = it },
                accent           = accent,
                surfColor        = surfColor,
                textColor        = textColor,
                isDark           = isDark
            )

            NotificationsCard(
                notifications = notifications,
                onAddClick    = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val status = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        if (status == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            showNotifPicker = true
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        showNotifPicker = true
                    }
                },
                // ── THE FIX: Use .filter to safely remove the item! ──
                onRemove      = { labelToRemove -> notifications = notifications.filter { it != labelToRemove } },
                accent        = accent,
                surfColor     = surfColor,
                textColor     = textColor,
                isDark        = isDark
            )

            com.j4.eventify.components.LocationPicker(
                currentLocationName = locationName,
                onLocationSelected  = { name, lat, lon ->
                    locationName = name
                    locationLat  = lat
                    locationLon  = lon
                },
                accentColor = accent,
                textColor   = textColor
            )

            RepeatCard(
                repeatOption = repeatOption,
                onClick      = { showRepeatPicker = true },
                accent       = accent,
                surfColor    = surfColor,
                textColor    = textColor
            )

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

    // ─────────────────────────────────────────────
    // Dialogs & Pickers
    // ─────────────────────────────────────────────

    if (showNotifPicker) {
        NotificationPickerDialog(
            existing  = notifications,
            accent    = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showNotifPicker = false },
            onSelect  = { value ->
                if (value == "custom") {
                    showNotifPicker  = false
                    showCustomDialog = true
                } else {
                    val label = getNotificationLabel(value)
                    // ── THE FIX: Create a fresh list with the new item! ──
                    if (!notifications.contains(label)) {
                        notifications = notifications.toMutableList().apply { add(label) }
                    }
                    showNotifPicker = false
                }
            }
        )
    }

    if (showCustomDialog) {
        CustomNotificationDialog(
            accent    = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showCustomDialog = false },
            onConfirm = { customLabel ->
                // ── THE FIX: Create a fresh list with the new item! ──
                if (!notifications.contains(customLabel)) {
                    notifications = notifications.toMutableList().apply { add(customLabel) }
                }
                showCustomDialog = false
            }
        )
    }

    if (showCustomTypePicker) {
        CustomTypePickerSheet(
            registry         = registry,
            accent           = accent,
            surfColor        = surfColor,
            textColor        = textColor,
            onDismiss        = { showCustomTypePicker = false },
            onSelectExisting = { cfg ->
                customTypes          = if (!customTypes.contains(cfg)) customTypes + cfg else customTypes
                selectedType         = EventType.CUSTOM
                selectedCustomCfg    = cfg
                showCustomTypePicker = false
            },
            onCreateNew = {
                showCustomTypePicker = false
                showCustomTypeDialog = true
            }
        )
    }

    if (showCustomTypeDialog) {
        EditTypeDialog(
            initialLabel    = "",
            initialGradient = 3,
            initialIconKey  = BuiltInIcon.STAR,
            surfColor       = surfColor,
            textColor       = textColor,
            onDismiss       = { showCustomTypeDialog = false },
            onConfirm       = { result ->
                val pair = gradientPalette[result.gradientIndex]
                val cfg  = EventTypeConfig(
                    type          = EventType.CUSTOM,
                    label         = result.label.take(10),
                    gradientStart = pair.first,
                    gradientEnd   = pair.second,
                    textColor     = textColorForGradient(pair.first),
                    badgeColor    = badgeColorForGradient(pair.first, pair.second),
                    iconKey       = result.iconKey // <--- ADD THIS LINE!
                )
                registry.addCustomType(cfg)
                customTypes          = customTypes + cfg
                selectedType         = EventType.CUSTOM
                selectedCustomCfg    = cfg
                showCustomTypeDialog = false
            }
        )
    }

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

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notifications Disabled", fontWeight = FontWeight.Bold) },
            text = { Text("You need to enable notifications in your device settings to use event reminders.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to Settings", color = accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // 1. Start Date Picker
    if (showStartDatePicker) {
        val parsedStartDateMs: Long? = remember(startDate) {
            try {
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format.parse(startDate)?.time
            } catch (e: Exception) { null }
        }
        val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = parsedStartDateMs)

        ThemedDatePickerDialog(
            state = startDatePickerState,
            accent = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showStartDatePicker = false },
            onConfirm = {
                startDatePickerState.selectedDateMillis?.let {
                    val format = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val d = format.format(Date(it))
                    startDate = d
                    endDate = d
                }
                showStartDatePicker = false
            }
        )
    }

    // 2. End Date Picker
    if (showEndDatePicker) {
        val parsedEndDateMs: Long? = remember(endDate) {
            try {
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format.parse(endDate)?.time
            } catch (e: Exception) { null }
        }
        val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = parsedEndDateMs)

        ThemedDatePickerDialog(
            state = endDatePickerState,
            accent = accent,
            surfColor = surfColor,
            textColor = textColor,
            onDismiss = { showEndDatePicker = false },
            onConfirm = {
                endDatePickerState.selectedDateMillis?.let {
                    val format = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    endDate = format.format(Date(it))
                }
                showEndDatePicker = false
            }
        )
    }

    // 3. Start Time Picker
    if (showStartTimePicker) {
        val parsedStart: Pair<Int, Int> = remember(startTime) {
            try {
                val cal = Calendar.getInstance().apply { time = SimpleDateFormat("h:mm a", Locale.US).parse(startTime)!! }
                cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
            } catch (e: Exception) { 9 to 0 }
        }
        val startTimePickerState = rememberTimePickerState(initialHour = parsedStart.first, initialMinute = parsedStart.second)

        ThemedTimePickerDialog(
            state = startTimePickerState,
            accent = accent,
            surfColor = surfColor,
            textColor = textColor,
            isDark = isDark,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                val h = startTimePickerState.hour
                val m = startTimePickerState.minute
                startTime = formatTime(h, m)
                endTime = formatTime((h + 1) % 24, m)
                showStartTimePicker = false
            }
        )
    }

    // 4. End Time Picker
    if (showEndTimePicker) {
        val parsedEnd: Pair<Int, Int> = remember(endTime) {
            try {
                val cal = Calendar.getInstance().apply { time = SimpleDateFormat("h:mm a", Locale.US).parse(endTime)!! }
                cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
            } catch (e: Exception) { 10 to 0 }
        }
        val endTimePickerState = rememberTimePickerState(initialHour = parsedEnd.first, initialMinute = parsedEnd.second)

        ThemedTimePickerDialog(
            state = endTimePickerState,
            accent = accent,
            surfColor = surfColor,
            textColor = textColor,
            isDark = isDark,
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
    accent: Color,
    isEditMode: Boolean = false
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
                if (isEditMode) "Edit Event" else "New Event",
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
                    if (isEditMode) "Update" else "Save",
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
    customTypes: List<EventTypeConfig>,
    selectedCustomCfg: EventTypeConfig?,
    onCustomSelected: (EventTypeConfig) -> Unit,
    onAddCustomClick: () -> Unit,
    accent: Color,
    surfColor: Color,
    isDark: Boolean,
    registry: EventTypeRegistry
) {
    LazyRow(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val builtIns = listOf(registry.academic, registry.personal, registry.occasion)
        items(builtIns) { state ->
            val cfg = state.toConfig()
            AddEventTypeChip(
                text          = state.label,
                icon          = state.icon,
                selected      = selectedType == state.type,
                onClick       = { onTypeSelected(state.type) },
                color         = cfg.gradientStart,
                surfColor     = surfColor,
                accent        = accent,
                chipTextColor = if (isDark) Color.White else Color(0xFF1A1A1A),
                modifier      = Modifier.width(86.dp)
            )
        }

        items(customTypes) { cfg ->
            AddEventTypeChip(
                text          = cfg.label,
                icon          = cfg.iconKey?.imageVector ?: Icons.Default.Star,
                selected      = selectedType == EventType.CUSTOM && selectedCustomCfg == cfg,
                onClick       = { onCustomSelected(cfg) },
                color         = cfg.gradientStart,
                surfColor     = surfColor,
                accent        = accent,
                chipTextColor = if (isDark) Color.White else Color(0xFF1A1A1A),
                modifier      = Modifier.width(86.dp)
            )
        }

        item {
            Surface(
                onClick         = onAddCustomClick,
                modifier        = Modifier.size(54.dp),
                shape           = RoundedCornerShape(12.dp),
                color           = surfColor,
                shadowElevation = 2.dp,
                border          = BorderStroke(1.5.dp, accent.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = accent, modifier = Modifier.size(22.dp))
                    Text("Add", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
                }
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
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFF667eea),
    chipTextColor: Color = Color(0xFF1A1A1A)
) {
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "chip_scale"
    )

    Surface(
        onClick         = onClick,
        modifier        = modifier
            .height(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape           = RoundedCornerShape(13.dp),
        color           = if (selected) accent else surfColor,
        shadowElevation = if (selected) 4.dp else 2.dp,
        border          = if (!selected) BorderStroke(1.dp, accent.copy(alpha = 0.1f)) else null
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, null,
                tint     = if (selected) Color.White else accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = text,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = if (selected) Color.White else chipTextColor.copy(alpha = 0.8f),
                maxLines   = 1
            )
        }
    }
}

// ─────────────────────────────────────────────
// Date / Time Card
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
    date: String, time: String?,
    onDateClick: () -> Unit, onTimeClick: () -> Unit,
    accent: Color, textColor: Color, isFirst: Boolean
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (isFirst) "Start" else "End",
            fontSize = 12.sp,
            color    = textColor.copy(alpha = 0.45f),
            modifier = Modifier.width(36.dp)
        )
        Surface(onClick = onDateClick, shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.08f)) {
            Text(
                date,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = accent,
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (time != null) {
            Surface(
                onClick = onTimeClick,
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent
            ) {
                Text(
                    time,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.65f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }

    }
}

// ─────────────────────────────────────────────
// Notifications Card
// ─────────────────────────────────────────────

@Composable
fun NotificationsCard(
    notifications: List<String>,
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit,
    accent: Color, surfColor: Color, textColor: Color, isDark: Boolean
) {
    Surface(shape = RoundedCornerShape(13.dp), color = surfColor, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(4.dp)) {
            notifications.forEachIndexed { index, label ->
                if (index > 0) GCalNotifDivider(textColor)
                NotificationRow(label = label, onRemove = { onRemove(label) }, accent = accent, textColor = textColor)
            }
            if (notifications.isNotEmpty()) GCalNotifDivider(textColor)
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable { onAddClick() }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.NotificationsNone, null, tint = accent, modifier = Modifier.size(22.dp))
                Text("Add notification", fontSize = 16.sp, color = accent)
            }
        }
    }
}

@Composable
private fun GCalNotifDivider(textColor: Color) {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = textColor.copy(alpha = 0.07f), thickness = 1.dp)
}

@Composable
private fun NotificationRow(label: String, onRemove: () -> Unit, accent: Color, textColor: Color) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Notifications, null, tint = accent, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 15.sp, color = textColor, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, "Remove", tint = textColor.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Notification Picker Dialog
// ─────────────────────────────────────────────

@Composable
fun NotificationPickerDialog(
    existing: List<String>, accent: Color, surfColor: Color, textColor: Color,
    onDismiss: () -> Unit, onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(16.dp),
        title  = { Text("Add notification", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor) },
        text   = {
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
                        Text(option.label, fontSize = 15.sp, color = if (alreadyAdded) accent else textColor)
                    }
                    if (option != standardNotificationOptions.last()) {
                        HorizontalDivider(color = textColor.copy(alpha = 0.06f))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = accent, fontWeight = FontWeight.Bold) } }
    )
}

// ─────────────────────────────────────────────
// Custom Notification Dialog
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomNotificationDialog(
    accent: Color, surfColor: Color, textColor: Color,
    onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    var minutes by remember { mutableStateOf("") }
    var unit    by remember { mutableStateOf("minutes") }
    val units   = listOf("minutes", "hours", "days")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(20.dp),
        title  = { Text("Custom notification", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("How long before the event?", fontSize = 15.sp, color = textColor.copy(alpha = 0.6f))

                // Number input full width
                OutlinedTextField(
                    value         = minutes,
                    onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) minutes = it },
                    label         = { Text("Amount", fontSize = 14.sp) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = textColor),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent, focusedLabelColor = accent,
                        cursorColor = accent, unfocusedTextColor = textColor, focusedTextColor = textColor
                    )
                )

                // Unit options as row of chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Unit", fontSize = 13.sp, color = textColor.copy(alpha = 0.5f))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        units.forEach { u ->
                            val sel = unit == u
                            Surface(
                                onClick  = { unit = u },
                                shape    = RoundedCornerShape(20.dp),
                                color    = if (sel) accent else accent.copy(alpha = 0.08f),
                                border   = if (!sel) BorderStroke(1.dp, accent.copy(alpha = 0.3f)) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    u.replaceFirstChar { it.uppercase() },
                                    fontSize   = 13.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (sel) White else accent,
                                    modifier   = Modifier.padding(vertical = 10.dp),
                                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = { if (minutes.isNotBlank()) onConfirm("$minutes $unit before") },
                shape   = RoundedCornerShape(10.dp),
                color   = if (minutes.isNotBlank()) accent else accent.copy(alpha = 0.3f)
            ) {
                Text("Add", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 15.sp, color = textColor.copy(alpha = 0.6f)) } }
    )
}

// ─────────────────────────────────────────────
// Notes Field
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventNotesField(
    value: String, onValueChange: (String) -> Unit,
    accent: Color, surfColor: Color, textColor: Color, isDark: Boolean
) {
    Surface(shape = RoundedCornerShape(13.dp), color = surfColor, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                modifier         = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Notes, null, tint = accent, modifier = Modifier.size(21.dp))
            }
            TextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = { Text("Add notes or location…", color = textColor.copy(alpha = 0.35f), fontSize = 16.sp) },
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = accent, unfocusedTextColor = textColor, focusedTextColor = textColor
                ),
                modifier  = Modifier.fillMaxWidth().heightIn(min = 70.dp, max = 130.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = textColor)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Date / Time Pickers (FIXED)
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedDatePickerDialog(
    state: DatePickerState,
    accent: Color,
    surfColor: Color, // Added parameter
    textColor: Color, // Added parameter
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK", color = accent, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = accent) } },
        colors = DatePickerDefaults.colors(containerColor = surfColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = surfColor,
                titleContentColor = textColor,
                headlineContentColor = textColor,
                weekdayContentColor = textColor.copy(alpha = 0.6f),
                subheadContentColor = textColor.copy(alpha = 0.6f),
                yearContentColor = textColor,
                currentYearContentColor = accent,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = accent,
                dayContentColor = textColor,
                selectedDayContainerColor = accent,
                selectedDayContentColor = Color.White,
                todayContentColor = accent,
                todayDateBorderColor = accent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedTimePickerDialog(
    state: TimePickerState,
    accent: Color,
    surfColor: Color, // Added parameter
    textColor: Color, // Added parameter
    isDark: Boolean,  // Added parameter
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfColor,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Select time", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = if (isDark) textColor.copy(alpha = 0.05f) else Color(0xFFF5F5F5),
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = textColor,
                    selectorColor = accent,
                    periodSelectorBorderColor = accent,
                    periodSelectorSelectedContainerColor = accent.copy(alpha = 0.2f),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = accent,
                    periodSelectorUnselectedContentColor = textColor,
                    timeSelectorSelectedContainerColor = accent,
                    timeSelectorUnselectedContainerColor = if (isDark) textColor.copy(alpha = 0.05f) else Color(0xFFF5F5F5),
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContentColor = textColor
                )
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK", color = accent, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = accent) } }
    )
}


// ─────────────────────────────────────────────
// Repeat Card
// ─────────────────────────────────────────────

@Composable
fun RepeatCard(
    repeatOption: String, onClick: () -> Unit,
    accent: Color, surfColor: Color, textColor: Color
) {
    Surface(shape = RoundedCornerShape(13.dp), color = surfColor, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Repeat, null, tint = accent, modifier = Modifier.size(22.dp))
            Text(
                repeatOption,
                fontSize = 16.sp,
                color    = if (repeatOption == "Does not repeat") textColor.copy(alpha = 0.55f) else textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ExpandMore, null, tint = textColor.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Repeat Picker Dialog
// ─────────────────────────────────────────────

private val repeatOptions = listOf(
    "Does not repeat", "Every day", "Every week", "Every month", "Every year", "Custom…"
)

@Composable
fun RepeatPickerDialog(
    current: String, accent: Color, surfColor: Color, textColor: Color,
    onDismiss: () -> Unit, onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(16.dp),
        title  = { Text("Repeat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                repeatOptions.forEach { option ->
                    val selected = option == current
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }.padding(horizontal = 4.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                null,
                                tint     = if (selected) accent else textColor.copy(alpha = 0.35f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(option, fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            color      = if (selected) accent else textColor)
                    }
                    if (option != repeatOptions.last()) HorizontalDivider(color = textColor.copy(alpha = 0.06f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = accent, fontWeight = FontWeight.Bold) } }
    )
}

// ─────────────────────────────────────────────
// Custom Repeat Dialog
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRepeatDialog(
    accent: Color, surfColor: Color, textColor: Color,
    onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    var every by remember { mutableStateOf("1") }
    var unit  by remember { mutableStateOf("week") }
    val units = listOf("day", "week", "month", "year")
    val preview = if (every.isNotBlank() && every != "0") {
        val num = every.toIntOrNull() ?: 1
        "Every $every ${if (num == 1) unit else "${unit}s"}"
    } else ""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(20.dp),
        title  = { Text("Custom repeat", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("Repeat every", fontSize = 15.sp, color = textColor.copy(alpha = 0.6f))

                // Number input full width
                OutlinedTextField(
                    value         = every,
                    onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) every = it },
                    label         = { Text("Every", fontSize = 14.sp) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = textColor),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent, focusedLabelColor = accent,
                        cursorColor = accent, unfocusedTextColor = textColor, focusedTextColor = textColor
                    )
                )

                // Unit chips — 4 equal chips in a single row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Unit", fontSize = 13.sp, color = textColor.copy(alpha = 0.5f))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        units.forEach { u ->
                            val sel = unit == u
                            Surface(
                                onClick  = { unit = u },
                                shape    = RoundedCornerShape(20.dp),
                                color    = if (sel) accent else accent.copy(alpha = 0.08f),
                                border   = if (!sel) BorderStroke(1.dp, accent.copy(alpha = 0.3f)) else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    u.replaceFirstChar { it.uppercase() },
                                    fontSize   = 13.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (sel) White else accent,
                                    modifier   = Modifier.padding(vertical = 10.dp),
                                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Live preview
                if (preview.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accent.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            preview,
                            fontSize   = 15.sp,
                            color      = accent,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = { if (preview.isNotBlank()) onConfirm(preview) },
                shape   = RoundedCornerShape(10.dp),
                color   = if (preview.isNotBlank()) accent else accent.copy(alpha = 0.3f)
            ) {
                Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 15.sp, color = textColor.copy(alpha = 0.6f)) } }
    )
}

// ─────────────────────────────────────────────
// Custom Type Picker Sheet
// ─────────────────────────────────────────────

@Composable
fun CustomTypePickerSheet(
    registry: EventTypeRegistry,
    accent: Color, surfColor: Color, textColor: Color,
    onDismiss: () -> Unit,
    onSelectExisting: (EventTypeConfig) -> Unit,
    onCreateNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(20.dp),
        title  = { Text("Add event type", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (registry.customTypes.isEmpty()) {
                    Text("No custom types yet. Create one!", fontSize = 15.sp,
                        color = textColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    Text("Your custom types", fontSize = 13.sp, color = textColor.copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))
                    registry.customTypes.forEach { cfg ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectExisting(cfg) }.padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp).clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(cfg.gradientStart, cfg.gradientEnd)))
                            )
                            Text(cfg.label, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null, tint = textColor.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                        }
                        HorizontalDivider(color = textColor.copy(alpha = 0.06f))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .clickable { onCreateNew() }.padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = accent, modifier = Modifier.size(16.dp))
                    }
                    Text("Create new type", fontSize = 16.sp, color = accent,
                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 15.sp, color = textColor.copy(alpha = 0.5f)) } }
    )
}