package com.j4.eventify.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom Light Theme
private val EventifyLightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = GrayLight,
    onPrimaryContainer = Black,

    secondary = AcademicBlue,
    onSecondary = White,
    secondaryContainer = AcademicBlueLight,
    onSecondaryContainer = Black,

    tertiary = PersonalPink,
    onTertiary = White,
    tertiaryContainer = PersonalPinkLight,
    onTertiaryContainer = Black,

    background = GrayLight,
    onBackground = Black,

    surface = White,
    onSurface = Black,
    surfaceVariant = GrayLight,
    onSurfaceVariant = GrayDark,

    error = Error,
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Black,
    outlineVariant = GrayMedium,
)

// Custom Dark Theme (preserved for Phase 2)
private val EventifyDarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = GrayDark,
    onPrimaryContainer = White,

    secondary = AcademicBlueLight,
    onSecondary = Black,
    secondaryContainer = AcademicBlueDark,
    onSecondaryContainer = White,

    tertiary = PersonalPinkLight,
    onTertiary = Black,
    tertiaryContainer = PersonalPinkDark,
    onTertiaryContainer = White,

    background = Black,
    onBackground = White,

    surface = GrayDark,
    onSurface = White,
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = GrayLight,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = White,
    outlineVariant = GrayMedium,
)

@Composable
fun EventifyTheme(
    darkTheme: Boolean = false,  // ← CHANGED: Always false (ignore system)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // ALWAYS use light theme
    val colorScheme = EventifyLightColorScheme  // ← CHANGED: Force light

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true  // ← CHANGED: Always light
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}