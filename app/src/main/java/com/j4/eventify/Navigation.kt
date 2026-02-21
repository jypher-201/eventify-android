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
        composable(route = Routes.HOME) {
            EnhancedHomeScreen(
                onNavigateToAddEvent = { selectedDate ->
                    // Save selected date to navigation state
                    if (selectedDate != null) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selectedDate", selectedDate)
                    }
                    navController.navigate(Routes.ADD_EVENT)
                },
                onNavigateToEventDetails = { eventId ->
                    navController.navigate("${Routes.EVENT_DETAILS}/$eventId")
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
                    // Clear the saved date
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
                prefilledDate = selectedDate  // ← Pass to AddEventScreen
            )
        }

        // Event Details Screen - NOW SHOWS COUNTDOWN TIMER
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
                CountdownTimerScreen(  // ← Changed from EventDetailsScreen
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