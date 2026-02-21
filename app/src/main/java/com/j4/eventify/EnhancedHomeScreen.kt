package com.j4.eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.Event
import com.j4.eventify.components.EventCard
import com.j4.eventify.components.EventType
import com.j4.eventify.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHomeScreen(
    onNavigateToAddEvent: () -> Unit = {},
    onNavigateToEventDetails: (Int) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf<EventType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var timeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var showTimeFilterMenu by remember { mutableStateOf(false) }


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

        // Time filtering
        val timeFiltered = when (timeFilter) {
            TimeFilter.ALL -> searched
            TimeFilter.TODAY -> {
                searched.filter { event ->
                    val daysFromNow = event.countdownNumber.toIntOrNull() ?: 999
                    daysFromNow == 0
                }
            }
            TimeFilter.TOMORROW -> {
                searched.filter { event ->
                    val daysFromNow = event.countdownNumber.toIntOrNull() ?: 999
                    daysFromNow == 1
                }
            }
            TimeFilter.THIS_WEEK -> {
                searched.filter { event ->
                    val daysFromNow = event.countdownNumber.toIntOrNull() ?: 999
                    daysFromNow in 0..7
                }
            }
            TimeFilter.THIS_MONTH -> {
                searched.filter { event ->
                    val daysFromNow = event.countdownNumber.toIntOrNull() ?: 999
                    daysFromNow in 0..30
                }
            }
        }

        // Always sort by date (soonest first)
        timeFiltered.sortedBy { it.countdownNumber.toIntOrNull() ?: 999 }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    scope.launch { drawerState.close() }
                }
            )
        },
        content = {
            Scaffold(
                topBar = {
                    KeepStyleTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        viewMode = viewMode,
                        onViewModeChange = { viewMode = it },
                        timeFilter = timeFilter,                      // ← Changed
                        onTimeFilterChange = { timeFilter = it },     // ← Changed
                        showTimeFilterMenu = showTimeFilterMenu,      // ← Changed
                        onShowTimeFilterMenuChange = { showTimeFilterMenu = it },
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                },
                floatingActionButton = {
                    EventifyFAB(onClick = onNavigateToAddEvent)
                }
            ) { paddingValues ->
                if (filteredAndSortedEvents.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.padding(paddingValues),
                        message = if (searchQuery.isNotBlank())
                            "No events found for \"$searchQuery\""
                        else
                            "No events found"
                    )
                } else {
                    when (viewMode) {
                        ViewMode.LIST -> {
                            EventListView(
                                events = filteredAndSortedEvents,
                                onEventClick = onNavigateToEventDetails,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                        ViewMode.CALENDAR -> {
                            CalendarView(  // ← Updated to use new CalendarView
                                events = filteredAndSortedEvents,
                                onEventClick = onNavigateToEventDetails,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepStyleTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    showTimeFilterMenu: Boolean,
    onShowTimeFilterMenuChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
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
                // Back button
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
                        tint = Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Expanded Search bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFF1F3F4))
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
                            // Placeholder
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search events",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }

                            // BasicTextField
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    color = Black
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Black)
                            )
                        }

                        // Clear button
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Black,
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
                // Hamburger menu
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Collapsed Search bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFF1F3F4))
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
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Search events",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Time Filter button
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
                                        tint = if (viewMode == ViewMode.LIST) Black else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Time filter dropdown menu - only show in LIST view
                                if (viewMode == ViewMode.LIST) {
                                    DropdownMenu(
                                        expanded = showTimeFilterMenu,
                                        onDismissRequest = { onShowTimeFilterMenuChange(false) },
                                        modifier = Modifier.background(White)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "All Events",
                                                    fontWeight = if (timeFilter == TimeFilter.ALL) FontWeight.Bold else FontWeight.Normal
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
                                                    fontWeight = if (timeFilter == TimeFilter.TODAY) FontWeight.Bold else FontWeight.Normal
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
                                                    fontWeight = if (timeFilter == TimeFilter.TOMORROW) FontWeight.Bold else FontWeight.Normal
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
                                                    fontWeight = if (timeFilter == TimeFilter.THIS_WEEK) FontWeight.Bold else FontWeight.Normal
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
                                                    fontWeight = if (timeFilter == TimeFilter.THIS_MONTH) FontWeight.Bold else FontWeight.Normal
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

                            // View mode toggle (List/Calendar)
                            IconButton(
                                onClick = {
                                    onViewModeChange(
                                        if (viewMode == ViewMode.LIST) ViewMode.CALENDAR else ViewMode.LIST
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.CALENDAR)  // ← REVERSED
                                        Icons.Default.CalendarMonth      // In CALENDAR view, show Calendar icon
                                    else
                                        Icons.Default.ViewAgenda,        // In LIST view, show List icon
                                    contentDescription = if (viewMode == ViewMode.LIST)
                                        "Switch to Calendar view"
                                    else
                                        "Switch to List view",
                                    tint = Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Notification Icon (instead of Profile)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { /* TODO: Show notifications */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
    }
}
@Composable
fun DrawerContent(
    selectedFilter: EventType?,
    onFilterSelected: (EventType?) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = White,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Text(
                text = "EVENTIFY",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(8.dp))

            // Filter options
            DrawerItem(
                icon = Icons.Default.CalendarToday,
                text = "All Events",
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) }
            )

            DrawerItem(
                icon = Icons.Default.School,
                text = "Academic",
                selected = selectedFilter == EventType.ACADEMIC,
                color = AcademicBlue,
                onClick = { onFilterSelected(EventType.ACADEMIC) }
            )

            DrawerItem(
                icon = Icons.Default.FitnessCenter,
                text = "Personal",
                selected = selectedFilter == EventType.PERSONAL,
                color = PersonalPink,
                onClick = { onFilterSelected(EventType.PERSONAL) }
            )

            DrawerItem(
                icon = Icons.Default.Cake,
                text = "Occasion",
                selected = selectedFilter == EventType.OCCASION,
                color = OccasionYellow,
                onClick = { onFilterSelected(EventType.OCCASION) }
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // About (Settings removed - will become theme toggle later)
            DrawerItem(
                icon = Icons.Default.Info,
                text = "About",
                selected = false,
                onClick = { /* TODO: Show about dialog/screen */ }
            )
        }
    }
}

@Composable
fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    color: Color = Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFFE8F0FE) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (selected) color else Color.Gray
        )
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Black else Color.DarkGray
        )
    }
}

@Composable
fun EventListView(
    events: List<Event>,
    onEventClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event.id) }
            )
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    message: String = "No events found"
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Try a different filter or search",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EnhancedHomeScreenPreview() {
    EventifyTheme {
        EnhancedHomeScreen()
    }
}