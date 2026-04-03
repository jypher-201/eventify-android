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
    onConfirm: (Int) -> Unit  // kept for built-in compat — calls full version internally
) {
    EditTypeDialog(
        initialLabel       = state.label,
        initialGradient    = state.gradientIndex,
        initialIconKey     = state.iconKey,
        onDismiss          = onDismiss,
        onConfirm          = { result -> onConfirm(result.gradientIndex) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTypeDialog(
    initialLabel: String,
    initialGradient: Int,
    initialIconKey: BuiltInIcon,
    onDismiss: () -> Unit,
    onConfirm: (EditTypeResult) -> Unit
) {
    var label         by remember { mutableStateOf(initialLabel) }
    var gradientIndex by remember { mutableIntStateOf(initialGradient) }
    var iconKey       by remember { mutableStateOf(initialIconKey) }

    val allIcons = BuiltInIcon.entries.toList()
    val palette   = com.j4.eventify.components.gradientPalette

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = White,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Edit event type",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A1A)
            )
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
                            focusedBorderColor = palette[gradientIndex].first,
                            focusedLabelColor  = palette[gradientIndex].first,
                            cursorColor        = palette[gradientIndex].first
                        )
                    )

                    // ── Icon picker ───────────────────────────────
                    Text("Icon", fontSize = 12.sp, color = Color.Gray)
                    allIcons.chunked(8).forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { key ->
                                val sel = iconKey == key
                                Surface(
                                    onClick = { iconKey = key },
                                    shape   = RoundedCornerShape(10.dp),
                                    color   = if (sel) palette[gradientIndex].first.copy(alpha = 0.15f)
                                    else Color(0xFFF5F5F5),
                                    border  = if (sel) BorderStroke(2.dp, palette[gradientIndex].first) else null,
                                    modifier = Modifier.weight(1f).aspectRatio(1f)
                                ) {
                                    Box(
                                        modifier         = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            key.imageVector,
                                            key.label,
                                            tint     = if (sel) palette[gradientIndex].first else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            // fill remaining slots in last row
                            repeat(8 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    // ── Color / gradient picker ───────────────────
                    Text("Color", fontSize = 12.sp, color = Color.Gray)
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
                                            if (isSel) Modifier.border(2.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
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
                onClick = {
                    if (label.isNotBlank()) {
                        onConfirm(EditTypeResult(label.trim(), gradientIndex, iconKey))
                    }
                },
                shape = RoundedCornerShape(10.dp),
                color = if (label.isNotBlank()) palette[gradientIndex].first
                else Color(0xFFE0E0E0)
            ) {
                Text(
                    "Apply",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (label.isNotBlank())
                        com.j4.eventify.components.textColorForGradient(palette[gradientIndex].first)
                    else Color.Gray,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}