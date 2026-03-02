package com.j4.eventify

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Navigation routes for the app
 */
object Routes {
    const val HOME = "home"
    const val ADD_EVENT = "add_event"
    const val EVENT_DETAILS = "event_details"

    fun eventDetails(eventId: Int) = "event_details/$eventId"
}

/**
 * Main navigation setup for the app
 */
@Composable
fun EventifyNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // Home Screen
        composable(route = Routes.HOME) {
            HomeScreen(
                onNavigateToAddEvent = { selectedDate ->
                    if (selectedDate != null) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedDate", selectedDate)
                    }
                    navController.navigate(Routes.ADD_EVENT)
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(Routes.eventDetails(eventId))
                }
            )
        }

        // Add Event Screen
        composable(route = Routes.ADD_EVENT) {
            val selectedDate = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("selectedDate")

            AddEventScreen(
                onNavigateBack = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("selectedDate")
                    navController.popBackStack()
                },
                onSaveEvent = { title, type, startDate, startTime, notes ->
                    // TODO: Save to database
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("selectedDate")
                    navController.popBackStack()
                },
                prefilledDate = selectedDate
            )
        }

        // Event Details Screen
        composable(
            route = "${Routes.EVENT_DETAILS}/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            val event = DummyData.events.find { it.id == eventId }

            if (event != null) {
                CountdownTimerScreen(
                    event = event,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEdit = {
                        navController.navigate(Routes.ADD_EVENT)
                    },
                    onDelete = {
                        // TODO: In Phase 2, delete from database
                    }
                )
            }
        }
    }
}