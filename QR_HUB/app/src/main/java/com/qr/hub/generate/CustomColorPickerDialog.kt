package com.qr.hub.generate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qr.hub.util.*

enum class ColorTarget(val title: String) {
    FOREGROUND("QR Dots Color"),
    GRADIENT("Gradient Accent"),
    BACKGROUND("Background Color")
}

// 12 Curated Vibrant Brand Colors
private val BrandPalette = listOf(
    0xFFFFB300.toInt(), // Royal Gold
    0xFF00E5FF.toInt(), // Cyber Cyan
    0xFF00E676.toInt(), // Neon Green
    0xFFFF4081.toInt(), // Hot Pink
    0xFF7C4DFF.toInt(), // Royal Purple
    0xFFFF1744.toInt(), // Deep Crimson
    0xFFFF6D00.toInt(), // Vivid Orange
    0xFF1DE9B6.toInt(), // Teal Mint
    0xFF2979FF.toInt(), // Electric Blue
    0xFFFFFFFF.toInt(), // Pure White
    0xFF1E293B.toInt(), // Slate Blue-Grey
    0xFF0B0906.toInt()  // Deep Ink Black
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomColorPickerDialog(
    initialTarget: ColorTarget = ColorTarget.FOREGROUND,
    styleConfig: QRStyleConfig,
    isDark: Boolean = true,
    onDismiss: () -> Unit,
    onColorApplied: (QRStyleConfig) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(initialTarget) }

    var currentFg by remember { mutableStateOf(styleConfig.fgColor) }
    var currentGrad by remember { mutableStateOf(if (styleConfig.gradientType != QRGradientType.NONE) styleConfig.fgGradientEnd else styleConfig.fgColor) }
    var currentBg by remember { mutableStateOf(styleConfig.bgColor) }
    var enableGradient by remember { mutableStateOf(styleConfig.gradientType != QRGradientType.NONE) }

    val activeColorInt = when (selectedTarget) {
        ColorTarget.FOREGROUND -> currentFg
        ColorTarget.GRADIENT -> currentGrad
        ColorTarget.BACKGROUND -> currentBg
    }

    var hexText by remember(activeColorInt) {
        mutableStateOf(String.format("%06X", 0xFFFFFF and activeColorInt))
    }

    val dialogBg = if (isDark) Ink900 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val elevatedBg = if (isDark) Ink750 else CeramicSurface2
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)
    val textSecondary = appTextSecondary(isDark)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(dialogBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, null, tint = accent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Custom Color Palette",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Selector Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorTarget.values().forEach { target ->
                        val isSelected = selectedTarget == target
                        val previewColor = when (target) {
                            ColorTarget.FOREGROUND -> Color(currentFg)
                            ColorTarget.GRADIENT -> Color(currentGrad)
                            ColorTarget.BACKGROUND -> Color(currentBg)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) appGoldDim(isDark) else elevatedBg)
                                .border(1.dp, if (isSelected) accent else cardBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedTarget = target }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(previewColor)
                                        .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    when (target) {
                                        ColorTarget.FOREGROUND -> "Dots"
                                        ColorTarget.GRADIENT -> "Gradient"
                                        ColorTarget.BACKGROUND -> "Background"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) accentSoft else textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Color Display & Hex Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview Swatch
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(activeColorInt))
                            .border(2.dp, cardBorder, RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Hex Input Field
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                            hexText = filtered
                            if (filtered.length == 6) {
                                try {
                                    val parsedColor = (0xFF000000 or filtered.toLong(16)).toInt()
                                    when (selectedTarget) {
                                        ColorTarget.FOREGROUND -> currentFg = parsedColor
                                        ColorTarget.GRADIENT -> {
                                            currentGrad = parsedColor
                                            enableGradient = true
                                        }
                                        ColorTarget.BACKGROUND -> currentBg = parsedColor
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        prefix = { Text("#", color = accent, fontWeight = FontWeight.Bold) },
                        label = { Text("Hex Code", fontSize = 11.sp, color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = elevatedBg,
                            unfocusedContainerColor = elevatedBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Palette Grid
                Text(
                    "Quick Brand Palettes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BrandPalette) { colorInt ->
                        val isPicked = activeColorInt == colorInt
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(colorInt))
                                .border(
                                    if (isPicked) 2.5.dp else 1.dp,
                                    if (isPicked) accent else Color.Black.copy(alpha = 0.15f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    when (selectedTarget) {
                                        ColorTarget.FOREGROUND -> currentFg = colorInt
                                        ColorTarget.GRADIENT -> {
                                            currentGrad = colorInt
                                            enableGradient = true
                                        }
                                        ColorTarget.BACKGROUND -> currentBg = colorInt
                                    }
                                    hexText = String.format("%06X", 0xFFFFFF and colorInt)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPicked) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reset / Clear Gradient
                    if (selectedTarget == ColorTarget.GRADIENT) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(elevatedBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    enableGradient = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Solid Color", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                        }
                    }

                    // Cancel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(elevatedBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    }

                    // Apply Color
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appCtaGradient(isDark))
                            .clickable {
                                val updatedConfig = styleConfig.copy(
                                    fgColor = currentFg,
                                    fgGradientEnd = if (enableGradient) currentGrad else currentFg,
                                    gradientType = if (enableGradient && currentGrad != currentFg) QRGradientType.LINEAR_DIAGONAL else QRGradientType.NONE,
                                    bgColor = currentBg
                                )
                                onColorApplied(updatedConfig)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = appCtaTextColor(isDark), modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Colors", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = appCtaTextColor(isDark))
                        }
                    }
                }
            }
        }
    }
}
