package com.j4.eventify

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.j4.eventify.ui.theme.White

// Shared data class for edit results
data class EditTypeResult(
    val label: String,
    val gradientIndex: Int,
    val iconKey: BuiltInIcon
)

@Composable
fun EditBuiltInTypeDialog(
    state: BuiltInTypeState,
    onDismiss: () -> Unit,
    onConfirm: (EditTypeResult) -> Unit
) {
    EditTypeDialog(
        initialLabel       = state.label,
        initialGradient    = state.gradientIndex,
        initialIconKey     = state.iconKey,
        onDismiss          = onDismiss,
        onConfirm          = onConfirm,
        showDelete         = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTypeDialog(
    initialLabel: String,
    initialGradient: Int,
    initialIconKey: BuiltInIcon,
    onDismiss: () -> Unit,
    onConfirm: (EditTypeResult) -> Unit,
    showDelete: Boolean = false,
    associatedEventCount: Int = 0,
    onDelete: (moveToOther: Boolean) -> Unit = {}, // ── THE FIX: Flipped logic
    surfColor: Color = White,
    textColor: Color = Color(0xFF1A1A1A)
) {
    var label         by remember { mutableStateOf(initialLabel) }
    var gradientIndex by remember { mutableIntStateOf(initialGradient) }
    var iconKey       by remember { mutableStateOf(initialIconKey) }

    // ── Dialog State ──
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var moveToOther       by remember { mutableStateOf(true) } // ── THE FIX: Box is checked (Safe) by default!

    val allIcons  = BuiltInIcon.entries.toList()
    val palette   = com.j4.eventify.components.gradientPalette
    val curAccent = palette[gradientIndex].first
    val iconUnselBg = if (textColor == Color.White) Color.White.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
    val iconUnselTint = textColor.copy(alpha = 0.5f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (initialLabel.isBlank()) "New event type" else "Edit event type",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textColor
                )

                if (showDelete) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(22.dp))
                    }
                }
            }
        },
        text = {
            androidx.compose.foundation.rememberScrollState().let { scroll ->
                Column(
                    modifier            = Modifier.verticalScroll(scroll),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Name ──────────────────────────────────────
                    OutlinedTextField(
                        value         = label,
                        onValueChange = { if (it.length <= 16) label = it },
                        label         = { Text("Name") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor  = curAccent,
                            focusedLabelColor   = curAccent,
                            cursorColor         = curAccent,
                            unfocusedTextColor  = textColor,
                            focusedTextColor    = textColor,
                            unfocusedLabelColor = textColor.copy(alpha = 0.5f)
                        )
                    )

                    // ── Icon picker ───────────────────────────────
                    Text("Icon", fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))
                    allIcons.chunked(8).forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { key ->
                                val sel = iconKey == key
                                Surface(
                                    onClick  = { iconKey = key },
                                    shape    = RoundedCornerShape(10.dp),
                                    color    = if (sel) curAccent.copy(alpha = 0.15f) else iconUnselBg,
                                    border   = if (sel) BorderStroke(2.dp, curAccent) else null,
                                    modifier = Modifier.weight(1f).aspectRatio(1f)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(key.imageVector, key.label, tint = if (sel) curAccent else iconUnselTint, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            repeat(8 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    // ── Color / gradient picker ───────────────────
                    Text("Color", fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))
                    palette.chunked(6).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { pair ->
                                val idx   = palette.indexOf(pair)
                                val isSel = gradientIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(pair.first, pair.second)))
                                        .then(if (isSel) Modifier.border(2.5.dp, textColor, RoundedCornerShape(10.dp)) else Modifier)
                                        .clickable { gradientIndex = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSel) {
                                        Icon(Icons.Default.Check, null, tint = com.j4.eventify.components.textColorForGradient(pair.first), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // ── Live preview strip ────────────────────────
                    if (label.isNotBlank()) {
                        val pair = palette[gradientIndex]
                        val previewText = com.j4.eventify.components.textColorForGradient(pair.first)
                        Box(
                            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(pair.first, pair.second))).padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(iconKey.imageVector, null, tint = previewText, modifier = Modifier.size(20.dp))
                                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = previewText)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = { if (label.isNotBlank()) onConfirm(EditTypeResult(label.trim(), gradientIndex, iconKey)) },
                shape   = RoundedCornerShape(10.dp),
                color   = if (label.isNotBlank()) curAccent else textColor.copy(alpha = 0.2f)
            ) {
                Text("Apply", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (label.isNotBlank()) com.j4.eventify.components.textColorForGradient(curAccent) else textColor.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = textColor.copy(alpha = 0.6f)) }
        }
    )

    // ── THE CONFIRMATION POPUP ──
    if (showDeleteConfirm) {
        ModernDeleteCategoryDialog(
            categoryName = initialLabel,
            eventCount = associatedEventCount,
            moveToOther = moveToOther,
            onCheckedChange = { moveToOther = it },
            onConfirm = {
                showDeleteConfirm = false
                onDelete(moveToOther) // Pass the choice to HomeScreen!
            },
            onDismiss = { showDeleteConfirm = false },
            surfColor = surfColor,
            textColor = textColor
        )
    }
}

@Composable
fun ModernDeleteCategoryDialog(
    categoryName: String,
    eventCount: Int,
    moveToOther: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    surfColor: Color,
    textColor: Color
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (visible) 1f else 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "dialog_scale")
    LaunchedEffect(Unit) { visible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = surfColor,
        shape            = RoundedCornerShape(24.dp),
        modifier         = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        icon = {
            Surface(shape = CircleShape, color = Color(0xFFFF5252).copy(alpha = 0.1f), modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF5252), modifier = Modifier.size(32.dp)) }
            }
        },
        title = {
            Text("Delete Category?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Are you sure you want to delete \"$categoryName\"?",
                    fontSize  = 15.sp, color = textColor.copy(alpha = 0.7f), textAlign = TextAlign.Center
                )

                if (eventCount > 0) {
                    // ── THE DYNAMIC UI: Blue when checked (Safe), Red when UNCHECKED (Danger) ──
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (moveToOther) Color(0xFF2196F3).copy(alpha = 0.1f) else Color(0xFFFF5252).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, if (moveToOther) Color.Transparent else Color(0xFFFF5252).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onCheckedChange(!moveToOther) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Checkbox(
                                checked = moveToOther,
                                onCheckedChange = onCheckedChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2196F3),
                                    uncheckedColor = Color(0xFFFF5252)
                                )
                            )
                            Text(
                                text = if (moveToOther) "Keep the $eventCount event(s) and move them to 'Other'."
                                else "Permanently delete the $eventCount event(s).",
                                fontSize = 14.sp,
                                color = if (moveToOther) textColor else Color(0xFFFF5252),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Surface(onClick = onConfirm, shape = RoundedCornerShape(12.dp), color = Color(0xFFFF5252)) {
                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), fontSize = 15.sp)
            }
        },
        dismissButton = {
            Surface(onClick = onDismiss, shape = RoundedCornerShape(12.dp), color = textColor.copy(alpha = 0.05f)) {
                Text("Cancel", fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), fontSize = 15.sp)
            }
        }
    )
}