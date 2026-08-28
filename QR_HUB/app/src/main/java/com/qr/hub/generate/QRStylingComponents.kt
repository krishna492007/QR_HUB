package com.qr.hub.generate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.util.*

/**
 * Compact, Low-Height QR Customization Panel with Dynamic Dark & Light Theme
 */
@Composable
fun QRCustomizationSection(
    qrType: String,
    styleConfig: QRStyleConfig,
    isDark: Boolean = true,
    onStyleChanged: (QRStyleConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Presets", "Colors", "Dot Shape", "Corner Eyes", "Frames", "Center Logo")
    val context = LocalContext.current

    // State for Image Crop Dialog
    var rawPickedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    // State for Color Picker Dialog
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf(ColorTarget.FOREGROUND) }

    // Gallery Picker for custom logo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    rawPickedBitmap = bitmap
                    showCropDialog = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Show In-App Shape-Aware Cropper Dialog
    if (showCropDialog && rawPickedBitmap != null) {
        ImageCropDialog(
            sourceBitmap = rawPickedBitmap!!,
            shape = styleConfig.logoShape,
            onDismiss = { showCropDialog = false },
            onCropApplied = { croppedBmp ->
                showCropDialog = false
                onStyleChanged(
                    styleConfig.copy(
                        logoBitmap = croppedBmp,
                        logoTag = "custom"
                    )
                )
            }
        )
    }

    // Show Custom Color Picker Dialog
    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            initialTarget = colorPickerTarget,
            styleConfig = styleConfig,
            isDark = isDark,
            onDismiss = { showColorPickerDialog = false },
            onColorApplied = { updatedConfig ->
                showColorPickerDialog = false
                onStyleChanged(updatedConfig)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Ink800 else CeramicSurface2)
            .border(1.dp, if (isDark) BorderLine else CeramicBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            // Scrollable Tab Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val activeBg = appGoldPrimary(isDark)
                    val inactiveBg = if (isDark) Ink750 else CeramicSurface
                    val activeTextColor = if (isDark) Color(0xFF160E06) else CeramicCtaInk
                    val inactiveTextColor = appTextSecondary(isDark)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeBg else inactiveBg)
                            .clickable { selectedTab = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activeTextColor else inactiveTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                label = "StylingTabAnimation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> CompactPresetsTab(styleConfig, isDark, onStyleChanged)
                    1 -> CompactColorsTab(styleConfig, isDark, onStyleChanged, onOpenPicker = { target ->
                        colorPickerTarget = target
                        showColorPickerDialog = true
                    })
                    2 -> DotShapesTab(styleConfig, isDark, onStyleChanged)
                    3 -> CornerEyesTab(styleConfig, isDark, onStyleChanged)
                    4 -> CompactFramesTab(qrType, styleConfig, isDark, onStyleChanged)
                    5 -> TypeAwareCenterLogoTab(qrType, styleConfig, isDark, onStyleChanged, onPickGallery = { galleryLauncher.launch("image/*") })
                }
            }
        }
    }
}

/**
 * Horizontal Scrollable Custom Brand Color Chips & Swatches (Compact ~84dp height)
 */
@Composable
private fun CompactColorsTab(
    currentConfig: QRStyleConfig,
    isDark: Boolean,
    onStyleChanged: (QRStyleConfig) -> Unit,
    onOpenPicker: (ColorTarget) -> Unit
) {
    val quickColors = listOf(
        0xFFFFB300.toInt(), // Royal Gold
        0xFF00E5FF.toInt(), // Cyber Cyan
        0xFF00E676.toInt(), // Neon Green
        0xFFFF4081.toInt(), // Hot Pink
        0xFF7C4DFF.toInt(), // Royal Purple
        0xFFFF1744.toInt(), // Crimson
        0xFFFF6D00.toInt(), // Orange
        0xFFFFFFFF.toInt()  // Pure White
    )

    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)
    val textTertiary = appTextTertiary(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Dots Color Chip
        Box(
            modifier = Modifier
                .width(115.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                .clickable { onOpenPicker(ColorTarget.FOREGROUND) }
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(currentConfig.fgColor))
                            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    Icon(Icons.Default.Edit, null, tint = accentSoft, modifier = Modifier.size(14.dp))
                }
                Column {
                    Text("Dots Color", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("#${String.format("%06X", 0xFFFFFF and currentConfig.fgColor)}", fontSize = 10.5.sp, color = accentSoft)
                }
            }
        }

        // 2. Gradient Accent Chip
        val hasGradient = currentConfig.gradientType != QRGradientType.NONE && currentConfig.fgGradientEnd != currentConfig.fgColor
        Box(
            modifier = Modifier
                .width(115.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (hasGradient) appGoldDim(isDark) else cardBg)
                .border(1.dp, if (hasGradient) appGoldPrimary(isDark) else cardBorder, RoundedCornerShape(14.dp))
                .clickable { onOpenPicker(ColorTarget.GRADIENT) }
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (hasGradient) {
                                    Brush.linearGradient(listOf(Color(currentConfig.fgColor), Color(currentConfig.fgGradientEnd)))
                                } else {
                                    Brush.linearGradient(listOf(Color(currentConfig.fgColor), Color(currentConfig.fgColor)))
                                }
                            )
                            .border(1.2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    )
                    Icon(Icons.Default.Palette, null, tint = if (hasGradient) accentSoft else textTertiary, modifier = Modifier.size(14.dp))
                }
                Column {
                    Text("Gradient", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text(if (hasGradient) "Active" else "Solid", fontSize = 10.5.sp, color = if (hasGradient) accentSoft else textTertiary)
                }
            }
        }

        // 3. Background Color Chip
        Box(
            modifier = Modifier
                .width(115.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                .clickable { onOpenPicker(ColorTarget.BACKGROUND) }
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(currentConfig.bgColor))
                            .border(1.5.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    )
                    Icon(Icons.Default.FormatColorFill, null, tint = accentSoft, modifier = Modifier.size(14.dp))
                }
                Column {
                    Text("Background", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("#${String.format("%06X", 0xFFFFFF and currentConfig.bgColor)}", fontSize = 10.5.sp, color = accentSoft)
                }
            }
        }

        // 4. Quick Swatches
        quickColors.forEach { colorInt ->
            val isSelected = currentConfig.fgColor == colorInt
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) appGoldDim(isDark) else cardBg)
                    .border(1.dp, if (isSelected) appGoldPrimary(isDark) else cardBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        onStyleChanged(
                            currentConfig.copy(
                                fgColor = colorInt,
                                fgGradientEnd = colorInt,
                                gradientType = QRGradientType.NONE
                            )
                        )
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(1.5.dp, if (isSelected) appGoldPrimary(isDark) else Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "#${String.format("%06X", 0xFFFFFF and colorInt).take(4)}",
                        fontSize = 10.sp,
                        color = if (isSelected) accentSoft else textTertiary
                    )
                }
            }
        }
    }
}

/**
 * Horizontal Scrollable Luxury Preset Cards (Compact ~84dp height)
 */
@Composable
private fun CompactPresetsTab(
    currentConfig: QRStyleConfig,
    isDark: Boolean,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)
    val textTertiary = appTextTertiary(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QRPreset.values().forEach { preset ->
            val isSelected = currentConfig.moduleShape == preset.config.moduleShape &&
                    currentConfig.fgColor == preset.config.fgColor &&
                    currentConfig.gradientType == preset.config.gradientType

            Box(
                modifier = Modifier
                    .width(135.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) appGoldDim(isDark) else cardBg)
                    .border(1.dp, if (isSelected) accent else cardBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        onStyleChanged(
                            preset.config.copy(
                                logoBitmap = currentConfig.logoBitmap,
                                logoTag = currentConfig.logoTag,
                                logoShape = currentConfig.logoShape,
                                frameStyle = currentConfig.frameStyle,
                                frameText = currentConfig.frameText
                            )
                        )
                    }
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color preview badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(preset.previewBg))
                                .border(1.2.dp, Color(preset.previewFg), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(preset.previewFg))
                            )
                        }

                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        preset.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentSoft else textPrimary,
                        maxLines = 1
                    )
                    Text(
                        preset.subtitle,
                        fontSize = 10.5.sp,
                        color = textTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Compact Dot Shapes Tab (Rounded, Square, Circle, Diamond)
 */
@Composable
private fun DotShapesTab(
    currentConfig: QRStyleConfig,
    isDark: Boolean,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QRModuleShape.values().forEach { shape ->
            val isSelected = currentConfig.moduleShape == shape
            Box(
                modifier = Modifier
                    .width(105.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) appGoldDim(isDark) else cardBg)
                    .border(1.dp, if (isSelected) accent else cardBorder, RoundedCornerShape(14.dp))
                    .clickable { onStyleChanged(currentConfig.copy(moduleShape = shape)) }
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (shape) {
                                QRModuleShape.ROUNDED -> Icons.Default.RoundedCorner
                                QRModuleShape.SQUARE -> Icons.Default.Square
                                QRModuleShape.CIRCLE -> Icons.Default.Circle
                                QRModuleShape.DIAMOND -> Icons.Default.Diamond
                            },
                            contentDescription = null,
                            tint = if (isSelected) accent else appTextSecondary(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        shape.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentSoft else textPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Compact Corner Eyes Tab
 */
@Composable
private fun CornerEyesTab(
    currentConfig: QRStyleConfig,
    isDark: Boolean,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QREyeShape.values().forEach { eye ->
            val isSelected = currentConfig.eyeShape == eye
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) appGoldDim(isDark) else cardBg)
                    .border(1.dp, if (isSelected) accent else cardBorder, RoundedCornerShape(14.dp))
                    .clickable { onStyleChanged(currentConfig.copy(eyeShape = eye)) }
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (eye) {
                                QREyeShape.ROUNDED -> Icons.Default.RoundedCorner
                                QREyeShape.SQUARE -> Icons.Default.CheckBoxOutlineBlank
                                QREyeShape.CIRCULAR -> Icons.Default.Lens
                            },
                            contentDescription = null,
                            tint = if (isSelected) accent else appTextSecondary(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        eye.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentSoft else textPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Compact Horizontal Scrollable Frames Tab (~84dp height)
 */
@Composable
private fun CompactFramesTab(
    qrType: String,
    currentConfig: QRStyleConfig,
    isDark: Boolean,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    val upperType = qrType.uppercase()
    val isPayment = upperType.contains("UPI")
    val isWifi = upperType.contains("WIFI")
    val isWhatsApp = upperType.contains("WHATSAPP")

    val dynamicPrimaryTitle = when {
        isPayment -> "SCAN & PAY (₹)"
        isWhatsApp -> "CHAT ON WA"
        isWifi -> "CONNECT WIFI"
        else -> "SCAN ME"
    }

    val dynamicPrimaryText = when {
        isPayment -> "SCAN & PAY"
        isWhatsApp -> "CHAT ON WHATSAPP"
        isWifi -> "CONNECT TO WIFI"
        else -> "SCAN ME"
    }

    val dynamicPrimaryDesc = when {
        isPayment -> "UPI Payment Badge"
        isWhatsApp -> "WhatsApp CTA"
        isWifi -> "WiFi Join Frame"
        else -> "Classic Scan Banner"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Frame 1: CARD BORDER
        CompactFrameCard(
            title = "CARD BORDER",
            subtitle = "Modern Card Frame",
            icon = Icons.Default.Dashboard,
            isSelected = currentConfig.frameStyle == QRFrameStyle.CARD_BORDER,
            isDark = isDark,
            onClick = {
                onStyleChanged(currentConfig.copy(frameStyle = QRFrameStyle.CARD_BORDER, frameText = ""))
            }
        )

        // Frame 2: DYNAMIC SMART BANNER
        CompactFrameCard(
            title = dynamicPrimaryTitle,
            subtitle = dynamicPrimaryDesc,
            icon = when {
                isPayment -> Icons.Default.CurrencyRupee
                isWhatsApp -> Icons.AutoMirrored.Filled.Chat
                isWifi -> Icons.Default.Wifi
                else -> Icons.Default.CropPortrait
            },
            isSelected = currentConfig.frameStyle == QRFrameStyle.BOTTOM_BADGE || currentConfig.frameStyle == QRFrameStyle.PAYMENT_BADGE,
            isDark = isDark,
            onClick = {
                val chosenStyle = if (isPayment) QRFrameStyle.PAYMENT_BADGE else QRFrameStyle.BOTTOM_BADGE
                onStyleChanged(currentConfig.copy(frameStyle = chosenStyle, frameText = dynamicPrimaryText))
            }
        )

        // Frame 3: NO FRAME
        CompactFrameCard(
            title = "NO FRAME",
            subtitle = "Plain Clean QR",
            icon = Icons.Default.Block,
            isSelected = currentConfig.frameStyle == QRFrameStyle.NONE,
            isDark = isDark,
            onClick = {
                onStyleChanged(currentConfig.copy(frameStyle = QRFrameStyle.NONE, frameText = ""))
            }
        )
    }
}

@Composable
private fun CompactFrameCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)
    val textTertiary = appTextTertiary(isDark)

    Box(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) appGoldDim(isDark) else cardBg)
            .border(1.dp, if (isSelected) accent else cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (isSelected) accent else appTextSecondary(isDark),
                    modifier = Modifier.size(18.dp)
                )
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentSoft else textPrimary,
                maxLines = 1
            )
            Text(
                subtitle,
                fontSize = 10.5.sp,
                color = textTertiary,
                maxLines = 1
            )
        }
    }
}

/**
 * Compact Center Logo Options as Horizontal Scrollable Mini-Cards
 */
@Composable
private fun TypeAwareCenterLogoTab(
    qrType: String,
    currentConfig: QRStyleConfig,
    isDark: Boolean,
    onStyleChanged: (QRStyleConfig) -> Unit,
    onPickGallery: () -> Unit
) {
    val context = LocalContext.current
    val upperType = qrType.uppercase()
    val isUPI = upperType.contains("UPI")
    val isWA = upperType.contains("WHATSAPP")
    val isWifi = upperType.contains("WIFI")

    val appLogoBitmap = remember {
        try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
    }

    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)

    Column {
        // Logo Shape Selection (Squircle, Circle, Square)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shape:", fontSize = 12.sp, color = appTextSecondary(isDark), modifier = Modifier.padding(end = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QRLogoShape.values().forEach { shape ->
                    val isShapeSelected = currentConfig.logoShape == shape
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isShapeSelected) appGoldDim(isDark) else cardBg)
                            .border(1.dp, if (isShapeSelected) accent else cardBorder, RoundedCornerShape(8.dp))
                            .clickable { onStyleChanged(currentConfig.copy(logoShape = shape)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            shape.displayName,
                            fontSize = 11.5.sp,
                            fontWeight = if (isShapeSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isShapeSelected) accentSoft else appTextSecondary(isDark)
                        )
                    }
                }
            }
        }

        // Horizontal Logo Mini-Cards (~84dp height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Official QR Hub Logo (Gold Icon)
            CompactLogoCard(
                title = "QR Hub Logo",
                subtitle = "Gold Brand Crown",
                isSelected = currentConfig.logoTag == "app_logo",
                isDark = isDark,
                iconContent = {
                    appLogoBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    } ?: Icon(Icons.Default.QrCodeScanner, null, tint = accent, modifier = Modifier.size(20.dp))
                },
                onClick = {
                    onStyleChanged(
                        currentConfig.copy(
                            logoBitmap = appLogoBitmap,
                            logoTag = "app_logo"
                        )
                    )
                }
            )

            // Option 2: Custom Gallery Logo (Pinch & Crop)
            val isCustom = currentConfig.logoTag == "custom"
            CompactLogoCard(
                title = "Custom Logo",
                subtitle = if (isCustom) "Image Loaded" else "Pick & Crop 1:1",
                isSelected = isCustom,
                isDark = isDark,
                iconContent = {
                    if (isCustom && currentConfig.logoBitmap != null) {
                        Image(
                            bitmap = currentConfig.logoBitmap.asImageBitmap(),
                            contentDescription = "Custom Logo",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    } else {
                        Icon(Icons.Default.Crop, null, tint = accentSoft, modifier = Modifier.size(20.dp))
                    }
                },
                onClick = onPickGallery
            )

            // Option 3: Type-Aware Smart Badge
            if (isUPI) {
                CompactLogoCard(
                    title = "UPI Badge",
                    subtitle = "₹ Rupee Icon",
                    isSelected = currentConfig.logoTag == "upi_badge",
                    isDark = isDark,
                    iconContent = {
                        Text("₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accent)
                    },
                    onClick = {
                        val upiBmp = createTextBadgeBitmap("₹", 0xFF0B0906.toInt(), 0xFFFFB300.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = upiBmp, logoTag = "upi_badge"))
                    }
                )
            } else if (isWA) {
                CompactLogoCard(
                    title = "WhatsApp",
                    subtitle = "Chat Icon",
                    isSelected = currentConfig.logoTag == "wa_badge",
                    isDark = isDark,
                    iconContent = {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        val waBmp = createTextBadgeBitmap("WA", 0xFF25D366.toInt(), 0xFFFFFFFF.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = waBmp, logoTag = "wa_badge"))
                    }
                )
            } else if (isWifi) {
                CompactLogoCard(
                    title = "WiFi Icon",
                    subtitle = "Network Badge",
                    isSelected = currentConfig.logoTag == "wifi_badge",
                    isDark = isDark,
                    iconContent = {
                        Icon(Icons.Default.Wifi, null, tint = accent, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        val wifiBmp = createTextBadgeBitmap("WiFi", 0xFF0B0906.toInt(), 0xFFFFB300.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = wifiBmp, logoTag = "wifi_badge"))
                    }
                )
            }

            // Option 4: No Logo
            CompactLogoCard(
                title = "No Logo",
                subtitle = "Plain Center",
                isSelected = currentConfig.logoBitmap == null,
                isDark = isDark,
                iconContent = {
                    Icon(Icons.Default.Close, null, tint = appTextTertiary(isDark), modifier = Modifier.size(20.dp))
                },
                onClick = {
                    onStyleChanged(currentConfig.copy(logoBitmap = null, logoTag = ""))
                }
            )
        }
    }
}

@Composable
private fun CompactLogoCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isDark: Boolean,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Ink750 else CeramicSurface
    val cardBorder = if (isDark) BorderLine else CeramicBorder
    val accent = appGoldPrimary(isDark)
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)
    val textTertiary = appTextTertiary(isDark)

    Box(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) appGoldDim(isDark) else cardBg)
            .border(1.dp, if (isSelected) accent else cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) accent.copy(alpha = 0.2f) else if (isDark) Ink800 else CeramicSurface2),
                    contentAlignment = Alignment.Center
                ) {
                    iconContent()
                }

                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentSoft else textPrimary,
                maxLines = 1
            )
            Text(
                subtitle,
                fontSize = 10.5.sp,
                color = textTertiary,
                maxLines = 1
            )
        }
    }
}

/**
 * Creates high-resolution centered text badge bitmap for type-specific logos
 */
private fun createTextBadgeBitmap(text: String, bgColor: Int, textColor: Int, sizePx: Int = 200): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    val rect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
    val cornerRadius = sizePx * 0.24f
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = sizePx * 0.38f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val fontMetrics = textPaint.fontMetrics
    val textY = (sizePx / 2f) - ((fontMetrics.ascent + fontMetrics.descent) / 2f)
    canvas.drawText(text, sizePx / 2f, textY, textPaint)

    return bitmap
}
