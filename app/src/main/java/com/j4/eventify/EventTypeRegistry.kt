package com.j4.eventify

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import android.content.Context

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
            BOOK     -> Icons.AutoMirrored.Filled.MenuBook
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
        val (textColor, badgeColor) = when {
            type == EventType.ACADEMIC && gradientIndex == 0 ->
                Pair(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color(0xFF5E35B1))
            type == EventType.PERSONAL && gradientIndex == 1 ->
                Pair(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color(0xFFD81B60))
            type == EventType.OCCASION && gradientIndex == 2 ->
                Pair(androidx.compose.ui.graphics.Color(0xFF8B4513), androidx.compose.ui.graphics.Color(0xFFEF6C00))
            else ->
                Pair(textColorForGradient(pair.first), badgeColorForGradient(pair.first, pair.second))
        }
        return EventTypeConfig(
            type          = type,
            label         = label,
            gradientStart = pair.first,
            gradientEnd   = pair.second,
            textColor     = textColor,
            badgeColor    = badgeColor,
            iconKey       = iconKey // Pass it through here!
        )
    }
}

// ─────────────────────────────────────────────
// Registry
// ─────────────────────────────────────────────

class EventTypeRegistry(context: Context) {
    private val prefs = context.getSharedPreferences("EventifyRegistryPrefs", Context.MODE_PRIVATE)

    var academic by mutableStateOf(
        BuiltInTypeState(
            EventType.ACADEMIC,
            prefs.getString("ACADEMIC_label", "Academic") ?: "Academic",
            try { BuiltInIcon.valueOf(prefs.getString("ACADEMIC_icon", "SCHOOL") ?: "SCHOOL") } catch(e: Exception) { BuiltInIcon.SCHOOL },
            prefs.getInt("ACADEMIC_gradient", 0)
        )
    )
    var personal by mutableStateOf(
        BuiltInTypeState(
            EventType.PERSONAL,
            prefs.getString("PERSONAL_label", "Personal") ?: "Personal",
            try { BuiltInIcon.valueOf(prefs.getString("PERSONAL_icon", "FITNESS") ?: "FITNESS") } catch(e: Exception) { BuiltInIcon.FITNESS },
            prefs.getInt("PERSONAL_gradient", 1)
        )
    )
    var occasion by mutableStateOf(
        BuiltInTypeState(
            EventType.OCCASION,
            prefs.getString("OCCASION_label", "Occasion") ?: "Occasion",
            try { BuiltInIcon.valueOf(prefs.getString("OCCASION_icon", "CAKE") ?: "CAKE") } catch(e: Exception) { BuiltInIcon.CAKE },
            prefs.getInt("OCCASION_gradient", 2)
        )
    )

    var other by mutableStateOf(
        BuiltInTypeState(
            EventType.OTHER,
            prefs.getString("OTHER_label", "Other") ?: "Other",
            try { BuiltInIcon.valueOf(prefs.getString("OTHER_icon", "BOOK") ?: "BOOK") } catch(e: Exception) { BuiltInIcon.BOOK },
            prefs.getInt("OTHER_gradient", 3) // Uses a different default color!
        )
    )

    var customTypes by mutableStateOf(loadCustomTypes())
        private set

    fun updateBuiltIn(state: BuiltInTypeState) {
        when (state.type) {
            EventType.ACADEMIC -> academic = state
            EventType.PERSONAL -> personal = state
            EventType.OCCASION -> occasion = state
            EventType.OTHER    -> other = state
            else -> return
        }
        prefs.edit()
            .putString("${state.type.name}_label", state.label)
            .putString("${state.type.name}_icon", state.iconKey.name)
            .putInt("${state.type.name}_gradient", state.gradientIndex)
            .apply()
    }

    fun addCustomType(config: EventTypeConfig) {
        if (!customTypes.any { it.label == config.label }) {
            customTypes = customTypes + config
            saveCustomTypes()
        }
    }

    fun removeCustomType(config: EventTypeConfig) {
        customTypes = customTypes - config
        saveCustomTypes()
    }

    fun updateCustomType(old: EventTypeConfig, new: EventTypeConfig) {
        customTypes = customTypes.map { if (it == old) new else it }
        saveCustomTypes()
    }

    private fun saveCustomTypes() {
        val stringSet = customTypes.map { cfg ->
            val index = gradientPalette.indexOfFirst { it.first == cfg.gradientStart }.coerceAtLeast(0)
            // ── THE FIX: Save the icon name alongside the label and color! ──
            val iconName = cfg.iconKey?.name ?: "STAR"
            "${cfg.label}|$index|$iconName"
        }.toSet()
        prefs.edit().putStringSet("CUSTOM_TYPES_LIST", stringSet).apply()
    }

    private fun loadCustomTypes(): List<EventTypeConfig> {
        val savedSet = prefs.getStringSet("CUSTOM_TYPES_LIST", emptySet()) ?: emptySet()
        return savedSet.mapNotNull { savedString ->
            val parts = savedString.split("|")
            // ── THE FIX: Load all 3 parts ──
            if (parts.size >= 2) {
                val label = parts[0]
                val index = parts[1].toIntOrNull() ?: 0
                val iconName = parts.getOrNull(2) ?: "STAR" // Default to star if they made it before this update

                val pair = gradientPalette[index.coerceIn(0, gradientPalette.lastIndex)]
                val icon = try { BuiltInIcon.valueOf(iconName) } catch (e: Exception) { BuiltInIcon.STAR }

                EventTypeConfig(
                    type = EventType.CUSTOM,
                    label = label,
                    gradientStart = pair.first,
                    gradientEnd = pair.second,
                    textColor = textColorForGradient(pair.first),
                    badgeColor = badgeColorForGradient(pair.first, pair.second),
                    iconKey = icon // Put it back into the config!
                )
            } else null
        }.sortedBy { it.label }
    }

    fun academicConfig() = academic.toConfig()
    fun personalConfig() = personal.toConfig()
    fun occasionConfig() = occasion.toConfig()
    // ── THE FIX: Add a helper for the new OTHER category ──
    fun otherConfig()    = other.toConfig()

    // Update this list so OTHER appears in any generated lists
    fun allConfigs(): List<EventTypeConfig> = listOf(academicConfig(), personalConfig(), occasionConfig(), otherConfig()) + customTypes

    fun resolveForType(type: EventType, customConfig: EventTypeConfig?): EventTypeConfig {
        if (customConfig != null) return customConfig
        return when (type) {
            EventType.ACADEMIC -> academicConfig()
            EventType.PERSONAL -> personalConfig()
            EventType.OCCASION -> occasionConfig()
            // ── THE FIX: Tell the 'when' block what to do with OTHER ──
            EventType.OTHER    -> otherConfig()
            EventType.CUSTOM   -> academicConfig() // Fallback
        }
    }
}