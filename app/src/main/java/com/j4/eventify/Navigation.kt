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

object Routes {
    const val HOME          = "home"
    const val ADD_EVENT     = "add_event"
    const val EDIT_EVENT    = "edit_event"
    const val EVENT_DETAILS = "event_details"

    fun eventDetails(eventId: Int) = "event_details/$eventId"
    fun editEvent(eventId: Int)    = "edit_event/$eventId"
}

@Composable
fun EventifyNavigation() {
    val navController = rememberNavController()

    // rememberSaveable survives back-stack pops AND config changes.
    // Stored as Int (ordinal) because enums aren't directly saveable.
    var themeOrdinal by rememberSaveable { mutableStateOf(AppTheme.DEFAULT.ordinal) }
    val currentTheme = AppTheme.entries[themeOrdinal]

    // Single source of truth for all event types
    val registry = remember { EventTypeRegistry() }

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
            val event   = DummyData.events.find { it.id == eventId }

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
            val event   = DummyData.events.find { it.id == eventId }

            if (event != null) {
                CountdownTimerScreen(
                    event          = event,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit         = { navController.navigate(Routes.editEvent(eventId)) },
                    onDelete       = { },
                    registry       = registry
                )
            }
        }
    }
}