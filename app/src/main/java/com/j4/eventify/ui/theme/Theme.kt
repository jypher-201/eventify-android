package com.j4.eventify.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    errorContainer = Color(0xFFFFDAD6),        // Uses Color from import
    onErrorContainer = Color(0xFF410002),      // Uses Color from import

    outline = Black,
    outlineVariant = GrayMedium,
)

// Custom Dark Theme
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
    surfaceVariant = Color(0xFF424242),        // Uses Color from import
    onSurfaceVariant = GrayLight,

    error = Color(0xFFFFB4AB),                 // Uses Color from import
    onError = Color(0xFF690005),               // Uses Color from import
    errorContainer = Color(0xFF93000A),        // Uses Color from import
    onErrorContainer = Color(0xFFFFDAD6),      // Uses Color from import

    outline = White,
    outlineVariant = GrayMedium,
)

@Composable
fun EventifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> EventifyDarkColorScheme
        else -> EventifyLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // ✅ FIXED: Use WindowInsetsControllerCompat instead of deprecated API
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}