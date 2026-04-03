package com.j4.eventify

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.j4.eventify.components.EventType
import com.j4.eventify.components.EventTypeConfig
import com.j4.eventify.components.gradientPalette
import com.j4.eventify.components.textColorForGradient
import com.j4.eventify.components.badgeColorForGradient

// ─────────────────────────────────────────────
// Icon choices available for all event types
// ─────────────────────────────────────────────

enum class BuiltInIcon {
    SCHOOL, FITNESS, CAKE, STAR,
    WORK, HOME, SPORTS, MUSIC,
    HEART, BOOK, FLIGHT, CAMERA,
    SHOPPING, COFFEE, GAME, MEDAL;

    val imageVector: ImageVector
        get() = when (this) {
            SCHOOL   -> Icons.Default.School
            FITNESS  -> Icons.Default.FitnessCenter
            CAKE     -> Icons.Default.Cake
            STAR     -> Icons.Default.Star
            WORK     -> Icons.Default.Work
            HOME     -> Icons.Default.Home
            SPORTS   -> Icons.Default.Sports
            MUSIC    -> Icons.Default.MusicNote
            HEART    -> Icons.Default.Favorite
            BOOK     -> Icons.Default.MenuBook
            FLIGHT   -> Icons.Default.Flight
            CAMERA   -> Icons.Default.CameraAlt
            SHOPPING -> Icons.Default.ShoppingCart
            COFFEE   -> Icons.Default.Coffee
            GAME     -> Icons.Default.SportsEsports
            MEDAL    -> Icons.Default.EmojiEvents
        }

    val label: String
        get() = when (this) {
            SCHOOL   -> "School"
            FITNESS  -> "Fitness"
            CAKE     -> "Cake"
            STAR     -> "Star"
            WORK     -> "Work"
            HOME     -> "Home"
            SPORTS   -> "Sports"
            MUSIC    -> "Music"
            HEART    -> "Heart"
            BOOK     -> "Book"
            FLIGHT   -> "Travel"
            CAMERA   -> "Camera"
            SHOPPING -> "Shop"
            COFFEE   -> "Coffee"
            GAME     -> "Games"
            MEDAL    -> "Award"
        }
}

// ─────────────────────────────────────────────
// Built-in type descriptor
// ─────────────────────────────────────────────

data class BuiltInTypeState(
    val type: EventType,
    val label: String,
    val iconKey: BuiltInIcon,
    val gradientIndex: Int
) {
    val icon: ImageVector get() = iconKey.imageVector

    fun toConfig(): EventTypeConfig {
        val pair = gradientPalette[gradientIndex.coerceIn(0, gradientPalette.lastIndex)]
        return EventTypeConfig(
            type          = type,
            label         = label.uppercase(),
            gradientStart = pair.first,
            gradientEnd   = pair.second,
            textColor     = textColorForGradient(pair.first),
            badgeColor    = badgeColorForGradient(pair.first, pair.second)
        )
    }
}

// ─────────────────────────────────────────────
// Registry
// ─────────────────────────────────────────────

class EventTypeRegistry {

    var academic by mutableStateOf(
        BuiltInTypeState(EventType.ACADEMIC, "Academic", BuiltInIcon.SCHOOL,  gradientIndex = 0)
    )
    var personal by mutableStateOf(
        BuiltInTypeState(EventType.PERSONAL, "Personal", BuiltInIcon.FITNESS, gradientIndex = 1)
    )
    var occasion by mutableStateOf(
        BuiltInTypeState(EventType.OCCASION, "Occasion", BuiltInIcon.CAKE,    gradientIndex = 2)
    )

    var customTypes by mutableStateOf(listOf<EventTypeConfig>())
        private set

    fun addCustomType(config: EventTypeConfig) {
        customTypes = customTypes + config
    }

    fun removeCustomType(config: EventTypeConfig) {
        customTypes = customTypes - config
    }

    fun updateCustomType(old: EventTypeConfig, new: EventTypeConfig) {
        customTypes = customTypes.map { if (it == old) new else it }
    }

    fun academicConfig() = academic.toConfig()
    fun personalConfig() = personal.toConfig()
    fun occasionConfig() = occasion.toConfig()

    fun allConfigs(): List<EventTypeConfig> =
        listOf(academicConfig(), personalConfig(), occasionConfig()) + customTypes

    fun resolveForType(type: EventType, customConfig: EventTypeConfig?): EventTypeConfig = when (type) {
        EventType.ACADEMIC -> academicConfig()
        EventType.PERSONAL -> personalConfig()
        EventType.OCCASION -> occasionConfig()
        EventType.CUSTOM   -> customConfig ?: academicConfig()
    }
}