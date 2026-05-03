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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.j4.eventify.data.EventViewModel
import com.j4.eventify.data.EventViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────

enum class ViewMode { LIST, CALENDAR }

enum class TimeFilter { ALL, TODAY, TOMORROW, THIS_WEEK, THIS_MONTH }

enum class AppTheme {
    DEFAULT,      // Light / white — sun icon
    REDDISH_PINK,
    YELLOWISH,
    BLUEISH,
    DARK
}

// ─────────────────────────────────────────────
// Theme color helpers
// ─────────────────────────────────────────────

fun getBackgroundColor(theme: AppTheme): Color = when (theme) {
    AppTheme.DEFAULT      -> Color(0xFFFAFAFA)
    AppTheme.REDDISH_PINK -> Color(0xFFFFF5F7)
    AppTheme.YELLOWISH    -> Color(0xFFFFFBF0)
    AppTheme.BLUEISH      -> Color(0xFFF0F8FF)
    AppTheme.DARK         -> Color(0xFF1A1A1A)
}

fun getAccentColor(theme: AppTheme): Color = when (theme) {
    AppTheme.DEFAULT      -> Color(0xFF667eea)
    AppTheme.REDDISH_PINK -> Color(0xFFE91E63)
    AppTheme.YELLOWISH    -> Color(0xFFFF9800)
    AppTheme.BLUEISH      -> Color(0xFF2196F3)
    AppTheme.DARK         -> Color(0xFF667eea)
}

fun getTextColor(theme: AppTheme): Color = when (theme) {
    AppTheme.DARK -> Color(0xFFFFFFFF)
    else          -> Color(0xFF1A1A1A)
}

fun getSurfaceColor(theme: AppTheme): Color = when (theme) {
    AppTheme.DARK -> Color(0xFF2A2A2A)
    else          -> Color(0xFFFFFFFF)
}

// Top bar background: light pastel tint per theme
fun getTopBarColor(theme: AppTheme): Color = when (theme) {
    AppTheme.DEFAULT      -> Color(0xFFFFFFFF)
    AppTheme.REDDISH_PINK -> Color(0xFFFFE4EE)
    AppTheme.YELLOWISH    -> Color(0xFFFFF3DC)
    AppTheme.BLUEISH      -> Color(0xFFDCEEFF)
    AppTheme.DARK         -> Color(0xFF2A2A2A)
}

// Icon / text colour on the top bar — dark on light bars, white on dark
fun getTopBarContentColor(theme: AppTheme): Color = when (theme) {
    AppTheme.DARK -> Color(0xFFFFFFFF)
    else          -> Color(0xFF1A1A1A)
}

// ─────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EventViewModel = viewModel(factory = EventViewModelFactory),
    onNavigateToAddEvent: (String?, AppTheme) -> Unit = { _, _ -> },
    onNavigateToEventDetails: (Int) -> Unit = {},
    currentTheme: AppTheme = AppTheme.DEFAULT,
    onThemeChange: (AppTheme) -> Unit = {},
    registry: EventTypeRegistry
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedFilter       by remember { mutableStateOf<EventType?>(null) }
    var searchQuery          by remember { mutableStateOf("") }
    var viewMode             by remember { mutableStateOf(ViewMode.LIST) }
    var timeFilter           by remember { mutableStateOf(TimeFilter.ALL) }
    var showTimeFilterMenu   by remember { mutableStateOf(false) }
    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }
    var showAboutDialog      by remember { mutableStateOf(false) }
    var showCustomTypeDialog by remember { mutableStateOf(false) }

    var editingBuiltIn by remember { mutableStateOf<BuiltInTypeState?>(null) }
    var editingCustom  by remember { mutableStateOf<com.j4.eventify.components.EventTypeConfig?>(null) }

    val backgroundColor    = getBackgroundColor(currentTheme)
    val accentColor        = getAccentColor(currentTheme)
    val textColor          = getTextColor(currentTheme)
    val surfaceColor       = getSurfaceColor(currentTheme)
    val topBarColor        = getTopBarColor(currentTheme)
    val topBarContentColor = getTopBarContentColor(currentTheme)

    // 1. COLLECT THE REAL DATABASE FLOW
    val entityList by viewModel.allEvents.collectAsState(initial = emptyList())

    // 2. CONVERT ENTITIES TO UI EVENTS ON THE FLY
    val realEvents = remember(entityList) {
        entityList.map { entity ->
            mapEntityToUiEvent(entity)
        }
    }

    // 3. FILTERING LOGIC USING THE REAL EVENTS
    val filteredAndSortedEvents = remember(selectedFilter, searchQuery, timeFilter, realEvents) {
        val filtered = if (selectedFilter != null)
            realEvents.filter { it.type == selectedFilter }
        else
            realEvents

        val searched = if (searchQuery.isNotBlank())
            filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.notes.contains(searchQuery, ignoreCase = true)
            }
        else filtered

        val timeFiltered = when (timeFilter) {
            TimeFilter.ALL        -> searched
            TimeFilter.TODAY      -> searched.filter { (it.countdownNumber.toIntOrNull() ?: 999) == 0 }
            TimeFilter.TOMORROW   -> searched.filter { (it.countdownNumber.toIntOrNull() ?: 999) == 1 }
            TimeFilter.THIS_WEEK  -> searched.filter { (it.countdownNumber.toIntOrNull() ?: 999) in 0..7 }
            TimeFilter.THIS_MONTH -> searched.filter { (it.countdownNumber.toIntOrNull() ?: 999) in 0..30 }
        }

        timeFiltered.sortedBy { it.countdownNumber.toIntOrNull() ?: 999 }
    }

    val isFiltered = selectedFilter != null ||
            searchQuery.isNotBlank() ||
            timeFilter != TimeFilter.ALL

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModernDrawer(
                selectedFilter   = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    scope.launch { drawerState.close() }
                },
                currentTheme     = currentTheme,
                onThemeSelected  = { onThemeChange(it) },
                onAboutClick     = {
                    showAboutDialog = true
                    scope.launch { drawerState.close() }
                },
                accentColor    = accentColor,
                textColor      = textColor,
                surfaceColor   = surfaceColor,
                registry       = registry,
                onEditBuiltIn  = { editingBuiltIn = it },
                onEditCustom   = { editingCustom = it }
            )
        },
        content = {
            Scaffold(
                containerColor = backgroundColor,
                topBar = {
                    ModernTopBar(
                        searchQuery                = searchQuery,
                        onSearchQueryChange        = { searchQuery = it },
                        viewMode                   = viewMode,
                        onViewModeChange           = { viewMode = it },
                        timeFilter                 = timeFilter,
                        onTimeFilterChange         = { timeFilter = it },
                        showTimeFilterMenu         = showTimeFilterMenu,
                        onShowTimeFilterMenuChange = { showTimeFilterMenu = it },
                        onMenuClick                = { scope.launch { drawerState.open() } },
                        onResetClick               = {
                            selectedFilter = null
                            searchQuery    = ""
                            timeFilter     = TimeFilter.ALL
                            selectedCalendarDate = null
                        },
                        isFiltered         = isFiltered,
                        topBarColor        = topBarColor,
                        topBarContentColor = topBarContentColor,
                        surfaceColor       = surfaceColor,
                        currentTheme       = currentTheme
                    )
                },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (filteredAndSortedEvents.isEmpty()) {
                        ModernEmptyState(
                            message   = if (searchQuery.isNotBlank()) "No events found for \"$searchQuery\"" else "No events found",
                            textColor = textColor
                        )
                    } else {
                        when (viewMode) {
                            ViewMode.LIST -> {
                                LazyColumn(
                                    modifier            = Modifier.fillMaxSize(),
                                    contentPadding      = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredAndSortedEvents, key = { it.id }) { event ->

                                        val latestConfig = when (event.type) {
                                            EventType.ACADEMIC -> registry.academic.toConfig()
                                            EventType.PERSONAL -> registry.personal.toConfig()
                                            EventType.OCCASION -> registry.occasion.toConfig()
                                            EventType.OTHER    -> registry.other.toConfig()
                                            EventType.CUSTOM -> {
                                                val liveCategory = registry.customTypes.find {
                                                    it.label.equals(event.customConfig?.label, ignoreCase = true)
                                                }
                                                liveCategory ?: event.customConfig ?: registry.academic.toConfig()
                                            }
                                        }

                                        EventCard(
                                            event          = event,
                                            onClick        = { onNavigateToEventDetails(event.id) },
                                            overrideConfig = latestConfig
                                        )

                                    }
                                }
                            }
                            ViewMode.CALENDAR -> {
                                CalendarView(
                                    events          = filteredAndSortedEvents,
                                    onEventClick    = onNavigateToEventDetails,
                                    onDateSelected  = { selectedCalendarDate = it },
                                    modifier        = Modifier.fillMaxSize(),
                                    accentColor     = accentColor,
                                    textColor       = textColor,
                                    surfaceColor    = surfaceColor,
                                    configResolver  = { event ->
                                        when (event.type) {
                                            EventType.ACADEMIC -> registry.academic.toConfig()
                                            EventType.PERSONAL -> registry.personal.toConfig()
                                            EventType.OCCASION -> registry.occasion.toConfig()
                                            EventType.OTHER    -> registry.other.toConfig()
                                            EventType.CUSTOM -> {
                                                val liveCategory = registry.customTypes.find {
                                                    it.label.equals(event.customConfig?.label, ignoreCase = true)
                                                }
                                                liveCategory ?: event.customConfig ?: registry.academic.toConfig()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    EventifyFAB(
                        onClick = {
                            val dateToPass = if (viewMode == ViewMode.CALENDAR) selectedCalendarDate else null
                            onNavigateToAddEvent(dateToPass, currentTheme)
                        },
                        backgroundColor = accentColor,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    )

    editingBuiltIn?.let { state ->
        EditTypeDialog(
            initialLabel    = state.label,
            initialGradient = state.gradientIndex,
            initialIconKey  = state.iconKey,
            surfColor       = surfaceColor,
            textColor       = textColor,
            onDismiss       = { editingBuiltIn = null },
            onConfirm       = { result ->
                val updated = state.copy(
                    label         = result.label,
                    gradientIndex = result.gradientIndex,
                    iconKey       = result.iconKey
                )
                registry.updateBuiltIn(updated)
                editingBuiltIn = null
            }
        )
    }

    editingCustom?.let { cfg ->
        val currentIndex = com.j4.eventify.components.gradientPalette.indexOfFirst { it.first == cfg.gradientStart }.coerceAtLeast(0)

        // Count how many events are using this exact category!
        val linkedEvents = realEvents.filter {
            it.type == EventType.CUSTOM && it.customConfig?.label.equals(cfg.label, ignoreCase = true)
        }

        EditTypeDialog(
            initialLabel         = cfg.label,
            initialGradient      = currentIndex,
            initialIconKey       = cfg.iconKey ?: BuiltInIcon.STAR,
            showDelete           = true,
            associatedEventCount = linkedEvents.size, // Pass the count to the dialog!
            onDelete = { moveToOther -> // <--- Captures the new boolean!
                linkedEvents.forEach { eventToMove ->
                    val entity = entityList.find { it.id == eventToMove.id }
                    if (entity != null) {
                        if (moveToOther) {
                            // IF CHECKED: Move to 'Other' safely!
                            val safeEntity = entity.copy(
                                eventType = EventType.OTHER.name,
                                customLabel = null
                            )
                            viewModel.updateEvent(safeEntity)
                        } else {
                            // IF UNCHECKED: Nuke the event!
                            viewModel.deleteEvent(entity)
                        }
                    }
                }

                // Destroy the empty custom category
                registry.removeCustomType(cfg)
                editingCustom = null
            },
            surfColor       = surfaceColor,
            textColor       = textColor,
            onDismiss       = { editingCustom = null },
            onConfirm       = { result ->
                val pair = com.j4.eventify.components.gradientPalette[result.gradientIndex]
                val updated = cfg.copy(
                    label         = result.label,
                    gradientStart = pair.first,
                    gradientEnd   = pair.second,
                    textColor     = com.j4.eventify.components.textColorForGradient(pair.first),
                    badgeColor    = com.j4.eventify.components.badgeColorForGradient(pair.first, pair.second),
                    iconKey       = result.iconKey
                )
                registry.updateCustomType(cfg, updated)
                editingCustom = null
            }
        )
    }

    if (showAboutDialog) {
        ModernAboutDialog(
            onDismiss   = { showAboutDialog = false },
            surfColor   = surfaceColor,
            textColor   = textColor,
            accentColor = accentColor
        )
    }

    if (showCustomTypeDialog) {
        CustomTypeDialog(
            onDismiss = { showCustomTypeDialog = false },
            onConfirm = { showCustomTypeDialog = false }
        )
    }
}

// ─────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────

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
    onResetClick: () -> Unit,
    isFiltered: Boolean,
    topBarColor: Color,
    topBarContentColor: Color,
    surfaceColor: Color,
    currentTheme: AppTheme
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    val focusRequester   = remember { FocusRequester() }
    val focusManager     = LocalFocusManager.current

    val isColoredBar = currentTheme != AppTheme.DEFAULT && currentTheme != AppTheme.DARK
    val searchBarBg = when {
        isColoredBar                  -> Color.White.copy(alpha = 0.70f)
        currentTheme == AppTheme.DARK -> Color.White.copy(alpha = 0.14f)
        else                          -> Color(0xFFF0F0F0)
    }
    val hintColor = topBarContentColor.copy(alpha = 0.65f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(topBarColor)
            .statusBarsPadding()
    ) {
        if (isSearchExpanded) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick  = {
                        isSearchExpanded = false
                        onSearchQueryChange("")
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = topBarContentColor,
                        modifier           = Modifier.size(24.dp)
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
                        modifier          = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier         = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text("Search events", color = hintColor, fontSize = 16.sp)
                            }
                            BasicTextField(
                                value         = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle     = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    color    = topBarContentColor
                                ),
                                singleLine  = true,
                                cursorBrush = SolidColor(topBarContentColor)
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick  = { onSearchQueryChange("") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint               = hintColor,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(isSearchExpanded) {
                if (isSearchExpanded) focusRequester.requestFocus()
            }

        } else {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onMenuClick, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint               = topBarContentColor,
                        modifier           = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier         = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(searchBarBg)
                        .clickable { isSearchExpanded = true },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Search, "Search", tint = hintColor, modifier = Modifier.size(20.dp))
                            Text("Search events", color = hintColor, fontSize = 16.sp)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Box {
                                IconButton(
                                    onClick  = { if (viewMode == ViewMode.LIST) onShowTimeFilterMenuChange(true) },
                                    modifier = Modifier.size(36.dp),
                                    enabled  = viewMode == ViewMode.LIST
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = "Filter by time",
                                        tint               = if (viewMode == ViewMode.LIST) topBarContentColor else hintColor,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }

                                if (viewMode == ViewMode.LIST) {
                                    DropdownMenu(
                                        expanded         = showTimeFilterMenu,
                                        onDismissRequest = { onShowTimeFilterMenuChange(false) },
                                        modifier         = Modifier.background(surfaceColor)
                                    ) {
                                        listOf(
                                            TimeFilter.ALL        to "All Events",
                                            TimeFilter.TODAY      to "Today",
                                            TimeFilter.TOMORROW   to "Tomorrow",
                                            TimeFilter.THIS_WEEK  to "This Week",
                                            TimeFilter.THIS_MONTH to "This Month"
                                        ).forEach { (filter, label) ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        label,
                                                        fontWeight = if (timeFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                                        color      = getTextColor(currentTheme)
                                                    )
                                                },
                                                onClick = {
                                                    onTimeFilterChange(filter)
                                                    onShowTimeFilterMenuChange(false)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick  = { onViewModeChange(if (viewMode == ViewMode.LIST) ViewMode.CALENDAR else ViewMode.LIST) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector        = if (viewMode == ViewMode.CALENDAR) Icons.Default.CalendarMonth else Icons.Default.ViewAgenda,
                                    contentDescription = "Toggle view",
                                    tint               = topBarContentColor,
                                    modifier           = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(44.dp),
                    enabled = isFiltered
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset filters",
                        tint = if (isFiltered)
                            topBarContentColor
                        else
                            topBarContentColor.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = topBarContentColor.copy(alpha = 0.12f), thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────
// Drawer
// ─────────────────────────────────────────────

@Composable
fun ModernDrawer(
    selectedFilter: EventType?,
    onFilterSelected: (EventType?) -> Unit,
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onAboutClick: () -> Unit,
    accentColor: Color,
    textColor: Color,
    surfaceColor: Color,
    registry: EventTypeRegistry,
    onEditBuiltIn: (BuiltInTypeState) -> Unit = {},
    onEditCustom: (com.j4.eventify.components.EventTypeConfig) -> Unit = {}
) {
    ModalDrawerSheet(
        drawerContainerColor = surfaceColor,
        modifier             = Modifier.width(280.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "EVENTIFY",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Black,
                color      = accentColor,
                modifier   = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.10f))

            Spacer(modifier = Modifier.height(4.dp))

            ModernDrawerItem(
                icon      = Icons.Default.GridView,
                text      = "All Events",
                selected  = selectedFilter == null,
                color     = accentColor,
                onClick   = { onFilterSelected(null) },
                textColor = textColor
            )

            // ── THE FIX: The loop here is properly formatted now ──
            val builtIns = listOf(registry.academic, registry.personal, registry.occasion, registry.other)
            builtIns.forEach { state ->
                val cfg = state.toConfig()
                DrawerTypeItem(
                    state       = state,
                    config      = cfg,
                    selected    = selectedFilter == state.type,
                    textColor   = textColor,
                    onClick     = { onFilterSelected(state.type) },
                    onEditClick = { onEditBuiltIn(state) }
                )
            }

            if (registry.customTypes.isNotEmpty()) {
                HorizontalDivider(color = textColor.copy(alpha = 0.06f))
                registry.customTypes.forEach { cfg ->
                    DrawerCustomTypeItem(
                        config      = cfg,
                        selected    = selectedFilter == EventType.CUSTOM,
                        textColor   = textColor,
                        onClick     = { onFilterSelected(EventType.CUSTOM) },
                        onEditClick = { onEditCustom(cfg) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = textColor.copy(alpha = 0.10f))

            ThemeSelector(
                currentTheme    = currentTheme,
                onThemeSelected = onThemeSelected,
                textColor       = textColor
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.10f))

            ModernDrawerItem(
                icon      = Icons.Default.Info,
                text      = "About",
                selected  = false,
                color     = Color.Gray,
                onClick   = onAboutClick,
                textColor = textColor
            )
        }
    }
}

// ─────────────────────────────────────────────
// Theme Selector
// ─────────────────────────────────────────────

@Composable
fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    textColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier            = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            "THEME",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Black,
            color         = textColor.copy(alpha = 0.45f),
            letterSpacing = 1.sp
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            ThemeCircle(
                circleColor = Color(0xFFFFFFFF),
                selected    = currentTheme == AppTheme.DEFAULT,
                onClick     = { onThemeSelected(AppTheme.DEFAULT) },
                iconVector  = Icons.Default.WbSunny,
                iconTint    = Color(0xFFFFA000),
                borderColor = Color(0xFFDDDDDD)
            )
            ThemeCircle(
                circleColor = Color(0xFFE91E63),
                selected    = currentTheme == AppTheme.REDDISH_PINK,
                onClick     = { onThemeSelected(AppTheme.REDDISH_PINK) }
            )
            ThemeCircle(
                circleColor = Color(0xFFFF9800),
                selected    = currentTheme == AppTheme.YELLOWISH,
                onClick     = { onThemeSelected(AppTheme.YELLOWISH) }
            )
            ThemeCircle(
                circleColor = Color(0xFF2196F3),
                selected    = currentTheme == AppTheme.BLUEISH,
                onClick     = { onThemeSelected(AppTheme.BLUEISH) }
            )
            ThemeCircle(
                circleColor = Color(0xFF1A1A1A),
                selected    = currentTheme == AppTheme.DARK,
                onClick     = { onThemeSelected(AppTheme.DARK) },
                iconVector  = Icons.Default.DarkMode,
                iconTint    = Color.White,
                borderColor = Color.Gray.copy(alpha = 0.25f)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Theme Circle
// ─────────────────────────────────────────────

@Composable
fun ThemeCircle(
    circleColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: Color = Color.White,
    borderColor: Color? = null
) {
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "theme_scale"
    )

    val border = when {
        selected            -> androidx.compose.foundation.BorderStroke(2.5.dp, Color.Gray.copy(alpha = 0.4f))
        borderColor != null -> androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        else                -> null
    }

    Surface(
        onClick         = onClick,
        shape           = CircleShape,
        color           = circleColor,
        modifier        = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shadowElevation = if (selected) 4.dp else 1.dp,
        border          = border
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                iconVector != null -> {
                    Icon(
                        imageVector        = iconVector,
                        contentDescription = null,
                        tint               = if (selected && iconVector == Icons.Default.WbSunny)
                            Color(0xFFFF6F00)
                        else
                            iconTint,
                        modifier           = Modifier.size(18.dp)
                    )
                }
                selected -> {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Drawer Item
// ─────────────────────────────────────────────

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
        shape   = RoundedCornerShape(12.dp),
        color   = if (selected) color.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, text, tint = if (selected) color else Color.Gray, modifier = Modifier.size(22.dp))
            Text(
                text,
                fontSize   = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color      = if (selected) textColor else Color.Gray
            )
        }
    }
}

// ─────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────

@Composable
fun ModernEmptyState(message: String, textColor: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("📭", fontSize = 64.sp)
            Text(message, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text("Try adjusting your filters", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

// ─────────────────────────────────────────────
// About Dialog
// ─────────────────────────────────────────────

@Composable
fun ModernAboutDialog(
    onDismiss: () -> Unit,
    surfColor: Color = White,
    textColor: Color = Color(0xFF1A1A1A),
    accentColor: Color = Color(0xFF667eea)
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "dialog_scale"
    )
    LaunchedEffect(Unit) { visible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(24.dp),
        modifier         = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth(0.92f),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier            = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Eventify",
                    fontSize      = 28.sp,
                    fontWeight    = FontWeight.Black,
                    color         = textColor,
                    letterSpacing = 0.5.sp,
                    textAlign     = TextAlign.Center,
                    modifier      = Modifier.fillMaxWidth()
                )
                Text(
                    "Version 1.0",
                    fontSize  = 14.sp,
                    color     = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Text("by J", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "⁴",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor,
                        modifier   = Modifier.offset(y = (-3).dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    "Your ultimate personal countdown companion for tracking life's important moments",
                    fontSize   = 15.sp,
                    color      = textColor,
                    textAlign  = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                    modifier   = Modifier.fillMaxWidth()
                )
                HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "What Eventify Does:",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Never miss important dates! Track academic deadlines, personal milestones, and special occasions all in one beautiful app with customizable event types, live countdown timers, and smart filtering.",
                        fontSize   = 14.sp,
                        color      = textColor,
                        lineHeight = 21.sp,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                Text(
                    "© 2026 J⁴ Team",
                    fontSize  = 13.sp,
                    color     = textColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Surface(onClick = onDismiss, shape = RoundedCornerShape(12.dp), color = accentColor) {
                Text(
                    "Got it!",
                    fontWeight = FontWeight.Bold,
                    color      = White,
                    modifier   = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    fontSize   = 15.sp
                )
            }
        }
    )
}

// ─────────────────────────────────────────────
// Custom Type Dialog
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTypeDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var typeName by remember { mutableStateOf("") }
    var visible  by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "dialog_scale"
    )
    LaunchedEffect(Unit) { visible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = White,
        shape            = RoundedCornerShape(16.dp),
        modifier         = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        title = {
            Text("Add Custom Event Type", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Create a custom category for your events", fontSize = 14.sp, color = Color.Gray)
                OutlinedTextField(
                    value         = typeName,
                    onValueChange = { typeName = it },
                    label         = { Text("Type Name") },
                    placeholder   = { Text("e.g., Work, Travel, Health") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF667eea),
                        focusedLabelColor     = Color(0xFF667eea),
                        cursorColor           = Color(0xFF667eea)
                    )
                )
            }
        },
        confirmButton = {
            Surface(
                onClick = { if (typeName.isNotBlank()) onConfirm(typeName) },
                shape   = RoundedCornerShape(10.dp),
                color   = if (typeName.isNotBlank()) Color(0xFF667eea) else Color(0xFFE0E0E0)
            ) {
                Text(
                    "Add",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (typeName.isNotBlank()) White else Color.Gray,
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            Surface(onClick = onDismiss, shape = RoundedCornerShape(10.dp), color = Color(0xFFF5F5F5)) {
                Text(
                    "Cancel",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A),
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    )
}

// ─────────────────────────────────────────────
// Drawer Type Items
// ─────────────────────────────────────────────

@Composable
fun DrawerTypeItem(
    state: BuiltInTypeState,
    config: com.j4.eventify.components.EventTypeConfig,
    selected: Boolean,
    textColor: Color,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(12.dp),
        color   = if (selected) config.gradientStart.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(config.gradientStart, config.gradientEnd)
                        )
                    )
            )

            // ── THE FIX: Read the icon from the state! ──
            Icon(
                state.iconKey.imageVector,
                null,
                tint     = if (selected) config.gradientStart else Color.Gray,
                modifier = Modifier.size(20.dp)
            )

            Text(
                state.label,
                fontSize   = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color      = if (selected) textColor else Color.Gray,
                modifier   = Modifier.weight(1f)
            )

            IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Edit,
                    "Edit type",
                    tint     = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DrawerCustomTypeItem(
    config: com.j4.eventify.components.EventTypeConfig,
    selected: Boolean,
    textColor: Color,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(12.dp),
        color   = if (selected) config.gradientStart.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(config.gradientStart, config.gradientEnd)
                        )
                    )
            )

            // ── THE FIX: Read the iconKey from the config, fallback to Star! ──
            Icon(
                config.iconKey?.imageVector ?: Icons.Default.Star,
                null,
                tint     = if (selected) config.gradientStart else Color.Gray,
                modifier = Modifier.size(20.dp)
            )

            Text(
                config.label,
                fontSize   = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color      = if (selected) textColor else Color.Gray,
                modifier   = Modifier.weight(1f)
            )
            IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Edit,
                    "Edit type",
                    tint     = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}