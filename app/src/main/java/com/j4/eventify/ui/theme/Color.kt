package com.j4.eventify.ui.theme

import androidx.compose.ui.graphics.Color

// Base Colors
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val GrayLight = Color(0xFFF5F5F5)
val GrayMedium = Color(0xFF9E9E9E)
val GrayDark = Color(0xFF424242)

// Event Type Colors (Card Backgrounds)
val AcademicBlue      = Color(0xFF2196F3)
val AcademicBlueDark  = Color(0xFF1976D2)
val AcademicBlueLight = Color(0xFF64B5F6)

val PersonalPink      = Color(0xFFFF4081)
val PersonalPinkDark  = Color(0xFFF50057)
val PersonalPinkLight = Color(0xFFFF80AB)

val OccasionYellow      = Color(0xFFFFD600)
val OccasionYellowDark  = Color(0xFFFFC400)
val OccasionYellowLight = Color(0xFFFFE57F)

// Badge Colors
val BadgeAcademic = Color(0xFFFFD600)
val BadgePersonal = Color(0xFF00E676)
val BadgeOccasion = Color(0xFF9C27B0)

// UI Element Colors
val FABRed     = Color(0xFFFF5252)
val FABRedDark = Color(0xFFE53935)

// Success / Error / Warning / Info
val Success = Color(0xFF4CAF50)
val Error   = Color(0xFFF44336)
val Warning = Color(0xFFFF9800)
val Info    = Color(0xFF2196F3)

// Shadow
val ShadowColor = Color(0xFF000000)
val ShadowLight = Color(0x40000000)

// ─────────────────────────────────────────────
// Gradient Presets for Event Cards
// Each entry is a pair of (startColor, endColor).
// Index 0-2 are the defaults for ACADEMIC / PERSONAL / OCCASION.
// The rest are extras available in the custom-type picker.
// ─────────────────────────────────────────────

val eventGradientPresets: List<Pair<Color, Color>> = listOf(
    // 0 — Academic (blue)
    Pair(Color(0xFF2196F3), Color(0xFF1565C0)),
    // 1 — Personal (pink-red)
    Pair(Color(0xFFFF4081), Color(0xFFAD1457)),
    // 2 — Occasion (amber-orange)
    Pair(Color(0xFFFFD600), Color(0xFFFF6F00)),
    // 3 — Purple
    Pair(Color(0xFF9C27B0), Color(0xFF4A148C)),
    // 4 — Teal
    Pair(Color(0xFF009688), Color(0xFF004D40)),
    // 5 — Green
    Pair(Color(0xFF4CAF50), Color(0xFF1B5E20)),
    // 6 — Deep orange
    Pair(Color(0xFFFF5722), Color(0xFFBF360C)),
    // 7 — Indigo
    Pair(Color(0xFF3F51B5), Color(0xFF1A237E)),
    // 8 — Red
    Pair(Color(0xFFF44336), Color(0xFFB71C1C)),
    // 9 — Cyan
    Pair(Color(0xFF00BCD4), Color(0xFF006064)),
    // 10 — Brown
    Pair(Color(0xFF795548), Color(0xFF3E2723)),
    // 11 — Pink-purple
    Pair(Color(0xFFE91E63), Color(0xFF7B1FA2)),
)

// Convenience: get the gradient start/end for a preset index
fun gradientStart(index: Int): Color = eventGradientPresets[index.coerceIn(0, eventGradientPresets.lastIndex)].first
fun gradientEnd(index: Int): Color   = eventGradientPresets[index.coerceIn(0, eventGradientPresets.lastIndex)].second