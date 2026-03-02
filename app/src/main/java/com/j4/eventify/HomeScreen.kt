package com.j4.eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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
                }
            )
        },
        content = {
            Scaffold(
                containerColor = Color(0xFFFAFAFA),
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
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                },
                floatingActionButton = {
                    EventifyFAB(
                        onClick = {
                            val dateToPass = if (viewMode == ViewMode.CALENDAR) {
                                selectedCalendarDate
                            } else null
                            onNavigateToAddEvent(dateToPass)
                        }
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
                                "No events found"
                        )
                    } else {
                        when (viewMode) {
                            ViewMode.LIST -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)  // ← Changed from 12dp to 8dp
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
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    )
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
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF5F5F5))
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
                                    color = Color.Gray,
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
                                    color = Color(0xFF1A1A1A)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF667eea))
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
                                    tint = Color.Gray,
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
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF5F5F5))
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
                                            Color(0xFF667eea) else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

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
                                                    fontWeight = if (timeFilter == TimeFilter.ALL)
                                                        FontWeight.Bold else FontWeight.Normal
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
                                                        FontWeight.Bold else FontWeight.Normal
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
                                                        FontWeight.Bold else FontWeight.Normal
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
                                                        FontWeight.Bold else FontWeight.Normal
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
                                                        FontWeight.Bold else FontWeight.Normal
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
                                    tint = Color(0xFF667eea),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE8E8E8), thickness = 1.dp)
    }
}

@Composable
fun ModernDrawer(
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "EVENTIFY",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(8.dp))

            ModernDrawerItem(
                icon = Icons.Default.GridView,
                text = "All Events",
                selected = selectedFilter == null,
                color = Color(0xFF667eea),
                onClick = { onFilterSelected(null) }
            )

            ModernDrawerItem(
                icon = Icons.Default.School,
                text = "Academic",
                selected = selectedFilter == EventType.ACADEMIC,
                color = Color(0xFF667eea),
                onClick = { onFilterSelected(EventType.ACADEMIC) }
            )

            ModernDrawerItem(
                icon = Icons.Default.FitnessCenter,
                text = "Personal",
                selected = selectedFilter == EventType.PERSONAL,
                color = Color(0xFFf093fb),
                onClick = { onFilterSelected(EventType.PERSONAL) }
            )

            ModernDrawerItem(
                icon = Icons.Default.Cake,
                text = "Occasion",
                selected = selectedFilter == EventType.OCCASION,
                color = Color(0xFFfcb69f),
                onClick = { onFilterSelected(EventType.OCCASION) }
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = Color(0xFFE0E0E0))

            ModernDrawerItem(
                icon = Icons.Default.Info,
                text = "About",
                selected = false,
                color = Color.Gray,
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun ModernDrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
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
                color = if (selected) Color(0xFF1A1A1A) else Color.Gray
            )
        }
    }
}

@Composable
fun ModernEmptyState(message: String) {
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
                color = Color(0xFF1A1A1A)
            )
            Text(
                "Try adjusting your filters",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}