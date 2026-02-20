package com.j4.eventify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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

enum class SortOption {
    DATE,
    TYPE,
    NAME
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
    var sortOption by remember { mutableStateOf(SortOption.DATE) }
    var showSortMenu by remember { mutableStateOf(false) }


    val filteredAndSortedEvents = remember(selectedFilter, searchQuery, sortOption) {
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

        when (sortOption) {
            SortOption.DATE -> searched.sortedBy { it.dateTime }
            SortOption.TYPE -> searched.sortedBy { it.type.name }
            SortOption.NAME -> searched.sortedBy { it.title }
        }
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
                        sortOption = sortOption,
                        onSortOptionChange = { sortOption = it },
                        showSortMenu = showSortMenu,
                        onShowSortMenuChange = { showSortMenu = it },
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
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
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
                            // Sort button
                            Box {
                                IconButton(
                                    onClick = {
                                        if (viewMode == ViewMode.LIST) {  // ← Only works in LIST view
                                            onShowSortMenuChange(true)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp),
                                    enabled = viewMode == ViewMode.LIST  // ← Disabled in CALENDAR view
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "Sort",
                                        tint = if (viewMode == ViewMode.LIST) Black else Color.Gray,  // ← Gray when disabled
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Dropdown menu - only show in LIST view
                                if (viewMode == ViewMode.LIST) {
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { onShowSortMenuChange(false) },
                                        modifier = Modifier.background(White)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Sort by Date",
                                                    fontWeight = if (sortOption == SortOption.DATE) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                onSortOptionChange(SortOption.DATE)
                                                onShowSortMenuChange(false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Sort by Type",
                                                    fontWeight = if (sortOption == SortOption.TYPE) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                onSortOptionChange(SortOption.TYPE)
                                                onShowSortMenuChange(false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Sort by Name",
                                                    fontWeight = if (sortOption == SortOption.NAME) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                onSortOptionChange(SortOption.NAME)
                                                onShowSortMenuChange(false)
                                            }
                                        )
                                    }
                                }
                            }

                            // View mode toggle
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
                                    imageVector = if (viewMode == ViewMode.LIST)
                                        Icons.Default.CalendarMonth  // ← Changed to Calendar icon
                                    else
                                        Icons.Default.ViewAgenda,     // ← List view icon
                                    contentDescription = "Toggle view",
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