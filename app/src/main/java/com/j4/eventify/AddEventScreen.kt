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
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Input - Beautiful
                UltimateTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Event Title",
                    icon = Icons.Default.Title,
                    required = true
                )

                // Event Type - Modern Cards
                UltimateTypeSelector(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it }
                )

                // Date & Time - Smart Layout
                UltimateDateTimeSection(
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    isAllDay = isAllDay,
                    onStartDateClick = { /* TODO */ },
                    onEndDateClick = { /* TODO */ },
                    onStartTimeClick = { /* TODO */ },
                    onEndTimeClick = { /* TODO */ },
                    onAllDayChange = { isAllDay = it }
                )

                // Notes - Expandable
                UltimateNotesField(
                    value = notes,
                    onValueChange = { notes = it }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
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
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(26.dp)
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
                shape = RoundedCornerShape(12.dp),
                color = if (canSave) Color(0xFF667eea) else Color(0xFFE0E0E0),
                shadowElevation = if (canSave) 4.dp else 0.dp
            ) {
                Text(
                    "Save",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) White else Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    required: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = White,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF667eea).copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            placeholder,
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        if (required) {
                            Text("*", color = Color(0xFFFF5252), fontSize = 16.sp)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Event Type",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UltimateTypeCard(
                text = "Academic",
                icon = Icons.Default.School,
                selected = selectedType == EventType.ACADEMIC,
                onClick = { onTypeSelected(EventType.ACADEMIC) },
                color = Color(0xFF667eea),
                modifier = Modifier.weight(1f)
            )

            UltimateTypeCard(
                text = "Personal",
                icon = Icons.Default.FitnessCenter,
                selected = selectedType == EventType.PERSONAL,
                onClick = { onTypeSelected(EventType.PERSONAL) },
                color = Color(0xFFf093fb),
                modifier = Modifier.weight(1f)
            )

            UltimateTypeCard(
                text = "Occasion",
                icon = Icons.Default.Cake,
                selected = selectedType == EventType.OCCASION,
                onClick = { onTypeSelected(EventType.OCCASION) },
                color = Color(0xFFfcb69f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun UltimateTypeCard(
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
        label = "card_scale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) color else White,
        shadowElevation = if (selected) 6.dp else 3.dp,
        border = if (!selected) BorderStroke(1.5.dp, Color(0xFFE8E8E8)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (selected) White else color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Date & Time",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UltimateDateTimeField(
                        label = "Start Date",
                        value = startDate,
                        icon = Icons.Default.CalendarToday,
                        onClick = onStartDateClick,
                        modifier = Modifier.weight(1f)
                    )

                    UltimateDateTimeField(
                        label = "End Date",
                        value = endDate,
                        icon = Icons.Default.Event,
                        onClick = onEndDateClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Times Row (only when not all-day)
                if (!isAllDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UltimateDateTimeField(
                            label = "Start Time",
                            value = startTime,
                            icon = Icons.Default.Schedule,
                            onClick = onStartTimeClick,
                            modifier = Modifier.weight(1f)
                        )

                        UltimateDateTimeField(
                            label = "End Time",
                            value = endTime,
                            icon = Icons.Default.AccessTime,
                            onClick = onEndTimeClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // All-day Toggle
                HorizontalDivider(color = Color(0xFFF0F0F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WbSunny,
                            null,
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            "All-day event",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                    }
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
            }
        }
    }
}

@Composable
fun UltimateDateTimeField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = Color(0xFF667eea),
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun UltimateNotesField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Notes",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF667eea).copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Notes,
                            null,
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            "Add location, details, or reminders...",
                            color = Color.Gray,
                            fontSize = 15.sp
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
                        .heightIn(min = 80.dp, max = 150.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A1A)
                    )
                )
            }
        }
    }
}