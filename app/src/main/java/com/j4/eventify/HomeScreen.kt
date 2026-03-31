package com.j4.eventify

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import com.j4.eventify.components.EventCard
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import kotlinx.coroutines.launch

enum class ViewMode {
    LIST,
    CALENDAR
}

enum class TimeFilter {
    ALL,
    TODAY,
    TOMORROW,
    THIS_WEEK,
    THIS_MONTH
}

// NEW: App Theme enum
enum class AppTheme {
    DEFAULT,      // Original purple
    REDDISH_PINK, // Warm red-pink
    YELLOWISH,    // Warm yellow-orange
    BLUEISH,      // Cool blue
    DARK          // Dark mode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddEvent: (String?) -> Unit = { _ -> },
    onNavigateToEventDetails: (Int) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf<EventType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var timeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var showTimeFilterMenu by remember { mutableStateOf(false) }
    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // NEW: Theme state
    var currentTheme by remember { mutableStateOf(AppTheme.DEFAULT) }

    // NEW: Custom type dialog
    var showCustomTypeDialog by remember { mutableStateOf(false) }

    // Get theme colors based on selected theme
    val backgroundColor = getBackgroundColor(currentTheme)
    val accentColor = getAccentColor(currentTheme)
    val textColor = getTextColor(currentTheme)
    val surfaceColor = getSurfaceColor(currentTheme)

    // Optimized filtering with remember
    val filteredAndSortedEvents = remember(selectedFilter, searchQuery, timeFilter) {
        val filtered = if (selectedFilter != null) {
            DummyData.events.filter { it.type == selectedFilter }
        } else {
            DummyData.events
        }

        val searched = if (searchQuery.isNotBlank()) {
            filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.notes.contains(searchQuery, ignoreCase = true)
            }
        } else {
            filtered
        }

        val timeFiltered = when (timeFilter) {
            TimeFilter.ALL -> searched
            TimeFilter.TODAY -> searched.filter {
                (it.countdownNumber.toIntOrNull() ?: 999) == 0
            }
            TimeFilter.TOMORROW -> searched.filter {
                (it.countdownNumber.toIntOrNull() ?: 999) == 1
            }
            TimeFilter.THIS_WEEK -> searched.filter {
                (it.countdownNumber.toIntOrNull() ?: 999) in 0..7
            }
            TimeFilter.THIS_MONTH -> searched.filter {
                (it.countdownNumber.toIntOrNull() ?: 999) in 0..30
            }
        }

        timeFiltered.sortedBy { it.countdownNumber.toIntOrNull() ?: 999 }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModernDrawer(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    scope.launch { drawerState.close() }
                },
                currentTheme = currentTheme,
                onThemeSelected = { theme ->
                    currentTheme = theme
                },
                onAboutClick = {
                    showAboutDialog = true
                    scope.launch { drawerState.close() }
                },
                accentColor = accentColor,
                textColor = textColor,
                surfaceColor = surfaceColor
            )
        },
        content = {
            Scaffold(
                containerColor = backgroundColor,
                topBar = {
                    ModernTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        viewMode = viewMode,
                        onViewModeChange = { viewMode = it },
                        timeFilter = timeFilter,
                        onTimeFilterChange = { timeFilter = it },
                        showTimeFilterMenu = showTimeFilterMenu,
                        onShowTimeFilterMenuChange = { showTimeFilterMenu = it },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onResetClick = {
                            // RESET ALL FILTERS
                            selectedFilter = null
                            searchQuery = ""
                            timeFilter = TimeFilter.ALL
                            viewMode = ViewMode.LIST
                        },
                        accentColor = accentColor,
                        textColor = textColor,
                        surfaceColor = surfaceColor
                    )
                },
                floatingActionButton = {
                    EventifyFAB(
                        onClick = {
                            val dateToPass = if (viewMode == ViewMode.CALENDAR) {
                                selectedCalendarDate
                            } else null
                            onNavigateToAddEvent(dateToPass)
                        },
                        backgroundColor = accentColor
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (filteredAndSortedEvents.isEmpty()) {
                        ModernEmptyState(
                            message = if (searchQuery.isNotBlank())
                                "No events found for \"$searchQuery\""
                            else
                                "No events found",
                            textColor = textColor
                        )
                    } else {
                        when (viewMode) {
                            ViewMode.LIST -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = filteredAndSortedEvents,
                                        key = { it.id }
                                    ) { event ->
                                        EventCard(
                                            event = event,
                                            onClick = { onNavigateToEventDetails(event.id) }
                                        )
                                    }
                                }
                            }
                            ViewMode.CALENDAR -> {
                                CalendarView(
                                    events = filteredAndSortedEvents,
                                    onEventClick = onNavigateToEventDetails,
                                    onDateSelected = { date ->
                                        selectedCalendarDate = date
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    accentColor = accentColor,
                                    textColor = textColor,
                                    surfaceColor = surfaceColor
                                )
                            }
                        }
                    }
                }
            }
        }
    )

    // About Dialog
    if (showAboutDialog) {
        ModernAboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // NEW: Custom Type Dialog
    if (showCustomTypeDialog) {
        CustomTypeDialog(
            onDismiss = { showCustomTypeDialog = false },
            onConfirm = { typeName ->
                // TODO: Add custom type to list
                showCustomTypeDialog = false
            }
        )
    }
}

// NEW: Theme color functions
fun getBackgroundColor(theme: AppTheme): Color {
    return when (theme) {
        AppTheme.DEFAULT -> Color(0xFFFAFAFA)
        AppTheme.REDDISH_PINK -> Color(0xFFFFF5F7)  // Light pink-red tint
        AppTheme.YELLOWISH -> Color(0xFFFFFBF0)     // Light warm yellow
        AppTheme.BLUEISH -> Color(0xFFF0F8FF)       // Light cool blue
        AppTheme.DARK -> Color(0xFF1A1A1A)          // Dark background
    }
}

fun getAccentColor(theme: AppTheme): Color {
    return when (theme) {
        AppTheme.DEFAULT -> Color(0xFF667eea)
        AppTheme.REDDISH_PINK -> Color(0xFFE91E63)  // Pink-red accent
        AppTheme.YELLOWISH -> Color(0xFFFF9800)     // Orange-yellow accent
        AppTheme.BLUEISH -> Color(0xFF2196F3)       // Blue accent
        AppTheme.DARK -> Color(0xFF667eea)          // Keep purple for dark
    }
}

fun getTextColor(theme: AppTheme): Color {
    return when (theme) {
        AppTheme.DARK -> Color(0xFFFFFFFF)
        else -> Color(0xFF1A1A1A)
    }
}

fun getSurfaceColor(theme: AppTheme): Color {
    return when (theme) {
        AppTheme.DEFAULT -> White
        AppTheme.REDDISH_PINK -> Color(0xFFFFFFFF)
        AppTheme.YELLOWISH -> Color(0xFFFFFFFF)
        AppTheme.BLUEISH -> Color(0xFFFFFFFF)
        AppTheme.DARK -> Color(0xFF2A2A2A)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    showTimeFilterMenu: Boolean,
    onShowTimeFilterMenuChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    onResetClick: () -> Unit,  // NEW: Reset button
    accentColor: Color,
    textColor: Color,
    surfaceColor: Color
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Logic for a dynamic search bar background that works in both light and dark modes
    val searchBarBg = if (textColor == Color.White) textColor.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
    val hintTextColor = textColor.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .statusBarsPadding()
    ) {
        if (isSearchExpanded) {
            // EXPANDED SEARCH MODE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        isSearchExpanded = false
                        onSearchQueryChange("")
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(searchBarBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search events",
                                    color = hintTextColor,
                                    fontSize = 16.sp
                                )
                            }

                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    color = textColor
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(accentColor)
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = hintTextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(isSearchExpanded) {
                if (isSearchExpanded) {
                    focusRequester.requestFocus()
                }
            }

        } else {
            // NORMAL MODE (Collapsed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(searchBarBg)
                        .clickable { isSearchExpanded = true },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = hintTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Search events",
                                color = hintTextColor,
                                fontSize = 16.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                IconButton(
                                    onClick = {
                                        if (viewMode == ViewMode.LIST) {
                                            onShowTimeFilterMenuChange(true)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp),
                                    enabled = viewMode == ViewMode.LIST
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Filter by time",
                                        tint = if (viewMode == ViewMode.LIST)
                                            accentColor else hintTextColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (viewMode == ViewMode.LIST) {
                                    DropdownMenu(
                                        expanded = showTimeFilterMenu,
                                        onDismissRequest = { onShowTimeFilterMenuChange(false) },
                                        modifier = Modifier.background(surfaceColor)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "All Events",
                                                    fontWeight = if (timeFilter == TimeFilter.ALL)
                                                        FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            },
                                            onClick = {
                                                onTimeFilterChange(TimeFilter.ALL)
                                                onShowTimeFilterMenuChange(false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Today",
                                                    fontWeight = if (timeFilter == TimeFilter.TODAY)
                                                        FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            },
                                            onClick = {
                                                onTimeFilterChange(TimeFilter.TODAY)
                                                onShowTimeFilterMenuChange(false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Tomorrow",
                                                    fontWeight = if (timeFilter == TimeFilter.TOMORROW)
                                                        FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            },
                                            onClick = {
                                                onTimeFilterChange(TimeFilter.TOMORROW)
                                                onShowTimeFilterMenuChange(false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "This Week",
                                                    fontWeight = if (timeFilter == TimeFilter.THIS_WEEK)
                                                        FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            },
                                            onClick = {
                                                onTimeFilterChange(TimeFilter.THIS_WEEK)
                                                onShowTimeFilterMenuChange(false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "This Month",
                                                    fontWeight = if (timeFilter == TimeFilter.THIS_MONTH)
                                                        FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            },
                                            onClick = {
                                                onTimeFilterChange(TimeFilter.THIS_MONTH)
                                                onShowTimeFilterMenuChange(false)
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    onViewModeChange(
                                        if (viewMode == ViewMode.LIST)
                                            ViewMode.CALENDAR else ViewMode.LIST
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.CALENDAR)
                                        Icons.Default.CalendarMonth
                                    else
                                        Icons.Default.ViewAgenda,
                                    contentDescription = "Toggle view",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // RESET BUTTON
                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset filters",
                        tint = textColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = textColor.copy(alpha = 0.1f), thickness = 1.dp)
    }
}

@Composable
fun ModernDrawer(
    selectedFilter: EventType?,
    onFilterSelected: (EventType?) -> Unit,
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onAboutClick: () -> Unit,
    accentColor: Color,
    textColor: Color,
    surfaceColor: Color
) {
    ModalDrawerSheet(
        drawerContainerColor = surfaceColor,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "EVENTIFY",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(8.dp))

            ModernDrawerItem(
                icon = Icons.Default.GridView,
                text = "All Events",
                selected = selectedFilter == null,
                color = accentColor,
                onClick = { onFilterSelected(null) },
                textColor = textColor
            )

            ModernDrawerItem(
                icon = Icons.Default.School,
                text = "Academic",
                selected = selectedFilter == EventType.ACADEMIC,
                color = Color(0xFF667eea),
                onClick = { onFilterSelected(EventType.ACADEMIC) },
                textColor = textColor
            )

            ModernDrawerItem(
                icon = Icons.Default.FitnessCenter,
                text = "Personal",
                selected = selectedFilter == EventType.PERSONAL,
                color = Color(0xFFf093fb),
                onClick = { onFilterSelected(EventType.PERSONAL) },
                textColor = textColor
            )

            ModernDrawerItem(
                icon = Icons.Default.Cake,
                text = "Occasion",
                selected = selectedFilter == EventType.OCCASION,
                color = Color(0xFFfcb69f),
                onClick = { onFilterSelected(EventType.OCCASION) },
                textColor = textColor
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // NEW: Theme Selector
            ThemeSelector(
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            ModernDrawerItem(
                icon = Icons.Default.Info,
                text = "About",
                selected = false,
                color = Color.Gray,
                onClick = onAboutClick,
                textColor = textColor
            )
        }
    }
}

// NEW: Theme Selector Component
@Composable
fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 4.dp) // Slight padding for alignment
    ) {
        Text(
            "THEME", // Made uppercase to match the "EVENTIFY" vibe
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp), // Tighter spacing
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeCircle(
                color = Color(0xFF667eea),
                selected = currentTheme == AppTheme.DEFAULT,
                onClick = { onThemeSelected(AppTheme.DEFAULT) }
            )
            ThemeCircle(
                color = Color(0xFFE91E63),
                selected = currentTheme == AppTheme.REDDISH_PINK,
                onClick = { onThemeSelected(AppTheme.REDDISH_PINK) }
            )
            ThemeCircle(
                color = Color(0xFFFF9800),
                selected = currentTheme == AppTheme.YELLOWISH,
                onClick = { onThemeSelected(AppTheme.YELLOWISH) }
            )
            ThemeCircle(
                color = Color(0xFF2196F3),
                selected = currentTheme == AppTheme.BLUEISH,
                onClick = { onThemeSelected(AppTheme.BLUEISH) }
            )
            ThemeCircle(
                color = Color(0xFF1A1A1A),
                selected = currentTheme == AppTheme.DARK,
                onClick = { onThemeSelected(AppTheme.DARK) },
                isDarkMode = true
            )
        }
    }
}

@Composable
fun ThemeCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "theme_scale"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        modifier = Modifier
            .size(36.dp) // Reduced from 42.dp for better balance
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shadowElevation = if (selected) 3.dp else 1.dp,
        // Added a subtle border for the Dark circle so it matches the visual weight of the others
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color.White)
        } else if (isDarkMode) {
            androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isDarkMode) {
                Icon(
                    Icons.Default.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp) // Slightly smaller icon
                )
            }

            if (selected && !isDarkMode) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ModernDrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    textColor: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                text,
                tint = if (selected) color else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) textColor else Color.Gray
            )
        }
    }
}

@Composable
fun ModernEmptyState(
    message: String,
    textColor: Color
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "📭",
                fontSize = 64.sp
            )
            Text(
                message,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                "Try adjusting your filters",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ModernAboutDialog(
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dialog_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth(0.92f),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Eventify",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1A1A),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Version 1.0",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "by J",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⁴",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea),
                        modifier = Modifier.offset(y = (-3).dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Your ultimate personal countdown companion for tracking life's important moments",
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Color(0xFFE8E8E8))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "What Eventify Does:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Never miss important dates! Track academic deadlines, personal milestones, and special occasions all in one beautiful app with customizable event types, live countdown timers, and smart filtering.",
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = Color(0xFFE8E8E8))

                Text(
                    text = "© 2024 J⁴ Team",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF667eea)
            ) {
                Text(
                    text = "Got it!",
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    fontSize = 15.sp
                )
            }
        }
    )
}

// NEW: Custom Type Dialog
@Composable
fun CustomTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var typeName by remember { mutableStateOf("") }

    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dialog_scale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        title = {
            Text(
                "Add Custom Event Type",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Create a custom category for your events",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = typeName,
                    onValueChange = { typeName = it },
                    label = { Text("Type Name") },
                    placeholder = { Text("e.g., Work, Travel, Health") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF667eea),
                        focusedLabelColor = Color(0xFF667eea),
                        cursorColor = Color(0xFF667eea)
                    )
                )
            }
        },
        confirmButton = {
            Surface(
                onClick = {
                    if (typeName.isNotBlank()) {
                        onConfirm(typeName)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                color = if (typeName.isNotBlank()) Color(0xFF667eea) else Color(0xFFE0E0E0)
            ) {
                Text(
                    "Add",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (typeName.isNotBlank()) White else Color.Gray,
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