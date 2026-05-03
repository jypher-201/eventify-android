package com.j4.eventify

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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

    // 1. Grab the context and open the internal storage
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("eventify_settings", Context.MODE_PRIVATE) }

    // 2. We collect the live database flow here
    val entityList by viewModel.allEvents.collectAsState(initial = emptyList())

    // 3. Read the saved theme from disk (Defaults to 0 / DEFAULT if none exists)
    val savedThemeOrdinal = prefs.getInt("app_theme_ordinal", AppTheme.DEFAULT.ordinal)
    var themeOrdinal by remember { mutableStateOf(savedThemeOrdinal) }

    // Safely map the number back to the AppTheme enum
    val currentTheme = try {
        AppTheme.entries[themeOrdinal]
    } catch (e: Exception) {
        AppTheme.DEFAULT
    }

    // 4. Registry initialization
    val registry = remember { EventTypeRegistry(context) }

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME
    ) {
        // ── Home ──────────────────────────────────────────────
        composable(route = Routes.HOME) {
            HomeScreen(
                currentTheme             = currentTheme,
                onThemeChange            = { newTheme ->
                    // Update the UI
                    themeOrdinal = newTheme.ordinal
                    // ── THE MAGIC: Save to the phone's hard drive! ──
                    prefs.edit().putInt("app_theme_ordinal", newTheme.ordinal).apply()
                },
                registry                 = registry,
                onNavigateToAddEvent     = { selectedDate, _ ->
                    val handle = navController.currentBackStackEntry?.savedStateHandle
                    if (selectedDate != null) handle?.set("selectedDate", selectedDate)
                    navController.navigate(Routes.ADD_EVENT)
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(Routes.eventDetails(eventId))
                },
                // ── THE FIX: Wire the swipe gesture to your Edit Route! ──
                onEditEvent = { eventId ->
                    navController.navigate(Routes.editEvent(eventId))
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

            // Find the REAL event in the database
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

            // Find the REAL event in the database
            val entity = entityList.find { it.id == eventId }
            val event = entity?.let { mapEntityToUiEvent(it) }

            if (event != null) {
                CountdownTimerScreen(
                    event          = event,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit         = { navController.navigate(Routes.editEvent(eventId)) },
                    onDelete       = {
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
fun mapEntityToUiEvent(entity: com.j4.eventify.data.local.EventEntity): com.j4.eventify.components.Event {

    // 1. Resolve Type and Custom Config
    val type = try {
        com.j4.eventify.components.EventType.valueOf(entity.eventType)
    } catch (e: Exception) {
        com.j4.eventify.components.EventType.ACADEMIC
    }

    val customCfg = if (type == com.j4.eventify.components.EventType.CUSTOM) {
        val pair = com.j4.eventify.components.gradientPalette[entity.gradientIndex.coerceIn(0, com.j4.eventify.components.gradientPalette.lastIndex)]
        com.j4.eventify.components.EventTypeConfig(
            type = com.j4.eventify.components.EventType.CUSTOM,
            label = entity.customLabel ?: "CUSTOM",
            gradientStart = pair.first,
            gradientEnd = pair.second,
            textColor = com.j4.eventify.components.textColorForGradient(pair.first),
            badgeColor = com.j4.eventify.components.badgeColorForGradient(pair.first, pair.second)
        )
    } else null

    // 2. Format Start and End Dates
    val formatFull = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
    val formatDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    val formatTime = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())

    val startDate = java.util.Date(entity.timestamp)
    val startStrFull = formatFull.format(startDate)
    val isAllDay = startStrFull.endsWith("12:00 AM")

    val d1 = formatDate.format(startDate)
    val t1 = formatTime.format(startDate)

    var displayString = ""

    if (entity.endTimestamp != null && entity.endTimestamp != entity.timestamp) {
        val endDate = java.util.Date(entity.endTimestamp)
        val d2 = formatDate.format(endDate)
        val t2 = formatTime.format(endDate)

        if (isAllDay) {
            displayString = if (d1 == d2) d1 else "$d1 - $d2"
        } else {
            displayString = if (d1 == d2) "$d1 at $t1 - $t2" else "$d1 at $t1 - $d2 at $t2"
        }
    } else {
        displayString = if (isAllDay) d1 else "$d1 at $t1"
    }

    // 3. Calculate Countdown
    val diffInMillis = entity.timestamp - System.currentTimeMillis()
    val diffInDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis)

    val (countdownNumber, countdownLabel) = when {
        diffInDays > 0 -> diffInDays.toString() to "DAYS"
        diffInDays == 0L && diffInMillis > 0 -> {
            val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffInMillis)
            if (hours > 0) hours.toString() to "HOURS" else "!" to "SOON"
        }
        else -> "0" to "DAYS"
    }

    // 4. Return the fully mapped Event
    return com.j4.eventify.components.Event(
        id = entity.id,
        title = entity.title,
        type = type,
        dateTime = displayString,
        countdownNumber = countdownNumber,
        countdownLabel = countdownLabel,
        notes = entity.description ?: "",
        customConfig = customCfg,
        rawStartMs = entity.timestamp,
        rawEndMs = entity.endTimestamp,
        isAllDay = entity.isAllDay,
        remindBeforeMinutes = entity.remindBeforeMinutes,
        repeatMode = entity.repeatMode,

        locationName = entity.locationName,
        latitude = entity.latitude,
        longitude = entity.longitude
    )
}