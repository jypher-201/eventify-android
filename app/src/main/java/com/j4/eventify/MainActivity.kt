package com.j4.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.j4.eventify.ui.theme.EventifyTheme

/**
 * Main entry point of the Eventify app
 * Launches the navigation system
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventifyTheme {
                EventifyNavigation()
            }
        }
    }
}
