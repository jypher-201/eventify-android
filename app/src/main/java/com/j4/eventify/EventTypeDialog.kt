package com.j4.eventify

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4.eventify.components.EventTypeConfig
import com.j4.eventify.components.gradientPalette
import com.j4.eventify.components.textColorForGradient
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
    // ── THE FIX: Pass the full result, not just the Int! ──
    onConfirm: (EditTypeResult) -> Unit
) {
    EditTypeDialog(
        initialLabel       = state.label,
        initialGradient    = state.gradientIndex,
        initialIconKey     = state.iconKey,
        onDismiss          = onDismiss,
        onConfirm          = onConfirm,
        showDelete         = false // We hide the delete button for default built-in types
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
    onDelete: () -> Unit = {},
    surfColor: Color = White,
    textColor: Color = Color(0xFF1A1A1A)
) {
    var label         by remember { mutableStateOf(initialLabel) }
    var gradientIndex by remember { mutableIntStateOf(initialGradient) }
    var iconKey       by remember { mutableStateOf(initialIconKey) }

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
            // ── THE FIX: Use a Row to push the title left and the Trash icon right ──
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

                // ── THE NEW TOP-RIGHT TRASH ICON ──
                if (showDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(22.dp)
                        )
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
                                    Box(
                                        modifier         = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            key.imageVector, key.label,
                                            tint     = if (sel) curAccent else iconUnselTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            repeat(8 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    // ── Color / gradient picker ───────────────────
                    Text("Color", fontSize = 12.sp, color = textColor.copy(alpha = 0.5f))
                    palette.chunked(6).forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { pair ->
                                val idx   = palette.indexOf(pair)
                                val isSel = gradientIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(pair.first, pair.second)
                                            )
                                        )
                                        .then(
                                            if (isSel) Modifier.border(2.5.dp, textColor, RoundedCornerShape(10.dp))
                                            else Modifier
                                        )
                                        .clickable { gradientIndex = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSel) {
                                        Icon(
                                            Icons.Default.Check, null,
                                            tint     = com.j4.eventify.components.textColorForGradient(pair.first),
                                            modifier = Modifier.size(16.dp)
                                        )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(pair.first, pair.second)
                                    )
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
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
                Text(
                    "Apply",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (label.isNotBlank())
                        com.j4.eventify.components.textColorForGradient(curAccent)
                    else textColor.copy(alpha = 0.4f),
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            // ── Cleaned up bottom buttons ──
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textColor.copy(alpha = 0.6f))
            }
        }
    )
}