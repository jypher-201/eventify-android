package com.j4.eventify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.j4.eventify.data.EventViewModel
import com.j4.eventify.data.EventViewModelFactory

object Routes {
    const val HOME          = "home"
    const val ADD_EVENT     = "add_event"
    const val EDIT_EVENT    = "edit_event"
    const val EVENT_DETAILS = "event_details"

    fun eventDetails(eventId: Int) = "event_details/$eventId"
    fun editEvent(eventId: Int)    = "edit_event/$eventId"
}

@Composable
fun EventifyNavigation(
    viewModel: EventViewModel = viewModel(factory = EventViewModelFactory)
) {
    val navController = rememberNavController()

    // 1. Grab the context
    val context = androidx.compose.ui.platform.LocalContext.current

    // 2. We collect the live database flow here
    val entityList by viewModel.allEvents.collectAsState(initial = emptyList())

    // rememberSaveable survives back-stack pops AND config changes.
    var themeOrdinal by rememberSaveable { mutableStateOf(AppTheme.DEFAULT.ordinal) }
    val currentTheme = AppTheme.entries[themeOrdinal]

    // 3. THIS MUST BE THE ONLY REGISTRY LINE! (Delete any other 'val registry...' lines)
    val registry = remember { EventTypeRegistry(context) }

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME
    ) {
        // ── Home ──────────────────────────────────────────────
        composable(route = Routes.HOME) {
            HomeScreen(
                currentTheme             = currentTheme,
                onThemeChange            = { themeOrdinal = it.ordinal },
                registry                 = registry,
                onNavigateToAddEvent     = { selectedDate, _ ->
                    val handle = navController.currentBackStackEntry?.savedStateHandle
                    if (selectedDate != null) handle?.set("selectedDate", selectedDate)
                    navController.navigate(Routes.ADD_EVENT)
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(Routes.eventDetails(eventId))
                }
            )
        }

        // ── Add Event ─────────────────────────────────────────
        composable(route = Routes.ADD_EVENT) {
            val handle       = navController.previousBackStackEntry?.savedStateHandle
            val selectedDate = handle?.get<String>("selectedDate")

            AddEventScreen(
                registry       = registry,
                onNavigateBack = {
                    handle?.remove<String>("selectedDate")
                    navController.popBackStack()
                },
                prefilledDate = selectedDate,
                currentTheme  = currentTheme
            )
        }

        // ── Edit Event ───────────────────────────────────────────
        composable(
            route     = "${Routes.EDIT_EVENT}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0

            // Find the REAL event in the database, not DummyData
            val entity = entityList.find { it.id == eventId }
            val event = entity?.let { mapEntityToUiEvent(it) }

            if (event != null) {
                AddEventScreen(
                    registry       = registry,
                    prefilledEvent = event,
                    currentTheme   = currentTheme,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ── Event Details ─────────────────────────────────────
        composable(
            route     = "${Routes.EVENT_DETAILS}/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0

            // Find the REAL event in the database, not DummyData
            val entity = entityList.find { it.id == eventId }
            val event = entity?.let { mapEntityToUiEvent(it) }

            if (event != null) {
                CountdownTimerScreen(
                    event          = event,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit         = { navController.navigate(Routes.editEvent(eventId)) },
                    onDelete       = {
                        // MAGIC: We pass the real entity to the delete function!
                        entity?.let { viewModel.deleteEvent(it) }
                    },
                    registry       = registry
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Database to UI Mapper Helper
// ─────────────────────────────────────────────
// ─────────────────────────────────────────────
// Database to UI Mapper Helper
// ─────────────────────────────────────────────
fun mapEntityToUiEvent(entity: com.j4.eventify.data.local.EventEntity): com.j4.eventify.components.Event {
    val date = java.util.Date(entity.timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
    var dateString = formatter.format(date)

    if (dateString.endsWith("at 12:00 AM")) {
        dateString = dateString.replace("at 12:00 AM", "(All Day)")
    }

    val diffInMillis = entity.timestamp - System.currentTimeMillis()
    val diffInDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis)

    val (countNum, countLabel) = when {
        diffInDays > 1 -> Pair(diffInDays.toString(), "DAYS LEFT")
        diffInDays == 1L -> Pair("1", "DAY LEFT")
        diffInDays == 0L && diffInMillis > 0 -> Pair("NOW", "HAPPENING")
        diffInMillis <= 0 -> Pair("DONE", "PASSED")
        else -> Pair("--", "")
    }

    val typeEnum = try {
        com.j4.eventify.components.EventType.valueOf(entity.eventType)
    } catch (e: Exception) {
        com.j4.eventify.components.EventType.PERSONAL
    }

    // 1. REBUILD THE SAVED COLOR FROM THE DATABASE
    val pair = com.j4.eventify.components.gradientPalette[entity.gradientIndex.coerceIn(0, com.j4.eventify.components.gradientPalette.lastIndex)]
    val rebuiltConfig = com.j4.eventify.components.EventTypeConfig(
        type = typeEnum,
        label = entity.customLabel ?: typeEnum.name, // Use custom name if it exists
        gradientStart = pair.first,
        gradientEnd = pair.second,
        textColor = com.j4.eventify.components.textColorForGradient(pair.first),
        badgeColor = com.j4.eventify.components.badgeColorForGradient(pair.first, pair.second)
    )

    // 2. ATTACH IT TO THE EVENT
    return com.j4.eventify.components.Event(
        id = entity.id,
        title = entity.title,
        type = typeEnum,
        dateTime = dateString,
        countdownNumber = countNum,
        countdownLabel = countLabel,
        notes = entity.description ?: "",
        customConfig = rebuiltConfig // <--- Restores the exact color to the UI!
    )
}