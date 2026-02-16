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
    const val EVENT_DETAILS = "event_details/{eventId}"

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
        composable(Routes.HOME) {
            EnhancedHomeScreen(
                onNavigateToAddEvent = {
                    navController.navigate(Routes.ADD_EVENT)
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate(Routes.eventDetails(eventId))
                }
            )
        }

        // Add Event Screen
        composable(Routes.ADD_EVENT) {
            AddEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveEvent = { title, type, date, time, notes ->
                    // TODO: In Phase 2, save to database
                    // For now, just navigate back
                }
            )
        }

        // Event Details Screen
        composable(
            route = Routes.EVENT_DETAILS,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0

            // Find the event from dummy data
            val event = DummyData.events.find { it.id == eventId }

            if (event != null) {
                EventDetailsScreen(
                    event = event,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEdit = {
                        // TODO: Navigate to edit screen (same as Add Event but with data)
                        navController.navigate(Routes.ADD_EVENT)
                    },
                    onDelete = {
                        // TODO: In Phase 2, delete from database
                        // For now, just navigate back
                    }
                )
            }
        }
    }
}