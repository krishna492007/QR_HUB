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
 * Compact, Low-Height QR Customization Panel (keeps QR Code Preview visible at all times)
 */
@Composable
fun QRCustomizationSection(
    qrType: String,
    styleConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Presets", "Dot Shape", "Corner Eyes", "Frames", "Center Logo")
    val context = LocalContext.current

    // State for Image Crop Dialog
    var rawPickedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

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
            initialShape = styleConfig.logoShape,
            onDismiss = { showCropDialog = false },
            onCropApplied = { croppedBmp, appliedShape ->
                showCropDialog = false
                onStyleChanged(
                    styleConfig.copy(
                        logoBitmap = croppedBmp,
                        logoTag = "custom",
                        logoShape = appliedShape
                    )
                )
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Ink800)
            .border(1.dp, BorderLine, RoundedCornerShape(18.dp))
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AmberPrimary else Ink750)
                            .clickable { selectedTab = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF160E06) else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content (Compact height so QR preview stays visible)
            AnimatedContent(
                targetState = selectedTab,
                label = "StylingTabAnimation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> CompactPresetsTab(styleConfig, onStyleChanged)
                    1 -> DotShapesTab(styleConfig, onStyleChanged)
                    2 -> CornerEyesTab(styleConfig, onStyleChanged)
                    3 -> CompactFramesTab(qrType, styleConfig, onStyleChanged)
                    4 -> TypeAwareCenterLogoTab(qrType, styleConfig, onStyleChanged, onPickGallery = { galleryLauncher.launch("image/*") })
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
    onStyleChanged: (QRStyleConfig) -> Unit
) {
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
                    .background(if (isSelected) AmberDim else Ink750)
                    .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(14.dp))
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
                            Icon(Icons.Default.CheckCircle, null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        preset.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AmberSoft else TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        preset.subtitle,
                        fontSize = 10.5.sp,
                        color = TextTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DotShapesTab(
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QRModuleShape.values().forEach { shape ->
            val isSelected = currentConfig.moduleShape == shape
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) AmberDim else Ink750)
                    .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(12.dp))
                    .clickable { onStyleChanged(currentConfig.copy(moduleShape = shape)) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Preview Icon
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(
                                when (shape) {
                                    QRModuleShape.SQUARE -> RoundedCornerShape(0.dp)
                                    QRModuleShape.ROUNDED -> RoundedCornerShape(5.dp)
                                    QRModuleShape.CIRCLE -> CircleShape
                                    QRModuleShape.DIAMOND -> RoundedCornerShape(3.dp)
                                }
                            )
                            .background(if (isSelected) AmberPrimary else TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        shape.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AmberSoft else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CornerEyesTab(
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QREyeShape.values().forEach { eye ->
            val isSelected = currentConfig.eyeShape == eye
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) AmberDim else Ink750)
                    .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(12.dp))
                    .clickable { onStyleChanged(currentConfig.copy(eyeShape = eye)) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Finder eye preview icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(
                                when (eye) {
                                    QREyeShape.SQUARE -> RoundedCornerShape(2.dp)
                                    QREyeShape.ROUNDED -> RoundedCornerShape(6.dp)
                                    QREyeShape.CIRCULAR -> CircleShape
                                }
                            )
                            .border(
                                2.5.dp,
                                if (isSelected) AmberPrimary else TextSecondary,
                                when (eye) {
                                    QREyeShape.SQUARE -> RoundedCornerShape(2.dp)
                                    QREyeShape.ROUNDED -> RoundedCornerShape(6.dp)
                                    QREyeShape.CIRCULAR -> CircleShape
                                }
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(
                                    when (eye) {
                                        QREyeShape.SQUARE -> RoundedCornerShape(1.dp)
                                        QREyeShape.ROUNDED -> RoundedCornerShape(2.dp)
                                        QREyeShape.CIRCULAR -> CircleShape
                                    }
                                )
                                .background(if (isSelected) AmberPrimary else TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        eye.displayName,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AmberSoft else TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Compact Horizontal Scrollable Frames Tab (keeps height small)
 */
@Composable
private fun CompactFramesTab(
    qrType: String,
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    val upperType = qrType.uppercase()

    val options = when (upperType) {
        "UPI" -> listOf(
            FrameOption(QRFrameStyle.PAYMENT_BADGE, "SCAN & PAY", "UPI Payment", Icons.Default.CurrencyRupee),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "PAY VIA UPI", "UPI Banner", Icons.Default.AccountBalance),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Card Frame", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic Scan", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.NONE, "NONE", "No Frame", Icons.Default.Block)
        )
        "WHATSAPP", "WAGROUP" -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "CHAT ON WHATSAPP", "Direct Chat", Icons.AutoMirrored.Filled.Chat),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN TO CHAT", "Chat Action", Icons.Default.Sms),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Card Frame", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic Scan", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.NONE, "NONE", "No Frame", Icons.Default.Block)
        )
        "WIFI" -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "CONNECT TO WIFI", "Auto Connect", Icons.Default.Wifi),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN TO CONNECT", "Network Scan", Icons.Default.SignalWifi4Bar),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Card Frame", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic Scan", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.NONE, "NONE", "No Frame", Icons.Default.Block)
        )
        "URL" -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "VISIT WEBSITE", "Open Link", Icons.Default.Link),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN TO OPEN", "Browser Action", Icons.Default.OpenInBrowser),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Card Frame", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic Scan", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.NONE, "NONE", "No Frame", Icons.Default.Block)
        )
        else -> listOf(
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Modern Card Frame", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic Scan Banner", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.NONE, "NONE", "Plain QR Code", Icons.Default.Block)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { opt ->
            val isSelected = (opt.style == QRFrameStyle.NONE && currentConfig.frameStyle == QRFrameStyle.NONE) ||
                    (opt.style == QRFrameStyle.CARD_BORDER && currentConfig.frameStyle == QRFrameStyle.CARD_BORDER) ||
                    (opt.style != QRFrameStyle.NONE && opt.style != QRFrameStyle.CARD_BORDER && currentConfig.frameStyle != QRFrameStyle.NONE && currentConfig.frameStyle != QRFrameStyle.CARD_BORDER && currentConfig.frameText == opt.text)

            Box(
                modifier = Modifier
                    .width(135.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) AmberDim else Ink750)
                    .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(14.dp))
                    .clickable {
                        onStyleChanged(
                            currentConfig.copy(
                                frameStyle = opt.style,
                                frameText = opt.text
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
                        Icon(
                            opt.icon,
                            null,
                            tint = if (isSelected) AmberPrimary else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        opt.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AmberSoft else TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        opt.subtitle,
                        fontSize = 10.5.sp,
                        color = TextTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private data class FrameOption(
    val style: QRFrameStyle,
    val text: String,
    val subtitle: String,
    val icon: ImageVector
)

/**
 * Type-Aware Center Logo Options with Compact Horizontal Cards & Shape Selector
 */
@Composable
private fun TypeAwareCenterLogoTab(
    qrType: String,
    styleConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit,
    onPickGallery: () -> Unit
) {
    val context = LocalContext.current
    val upperType = qrType.uppercase()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // ── 1. LOGO SHAPE SELECTOR (SQUIRCLE, CIRCLE, SQUARE) ──
        if (styleConfig.logoBitmap != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Shape:", fontSize = 11.sp, color = TextSecondary)
                QRLogoShape.values().forEach { shape ->
                    val isSelected = styleConfig.logoShape == shape
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AmberDim else Ink750)
                            .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(8.dp))
                            .clickable { onStyleChanged(styleConfig.copy(logoShape = shape)) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            shape.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AmberSoft else TextPrimary
                        )
                    }
                }
            }
        }

        // ── 2. LOGO OPTIONS IN HORIZONTAL SCROLL ROW ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Official App Logo (Default)
            CompactLogoCard(
                title = "QR Hub",
                subtitle = "App Gold Logo",
                icon = Icons.Default.QrCodeScanner,
                isSelected = styleConfig.logoTag == "app_logo",
                onClick = {
                    val logo = try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
                    onStyleChanged(
                        styleConfig.copy(
                            logoBitmap = logo,
                            logoTag = "app_logo",
                            logoShape = QRLogoShape.ROUNDED_SQUIRCLE
                        )
                    )
                }
            )

            // Option 2: Custom Gallery Logo (Pick & Crop 1:1)
            CompactLogoCard(
                title = if (styleConfig.logoTag == "custom") "Custom (Re-crop)" else "Custom Logo",
                subtitle = "Pick & Crop 1:1",
                icon = Icons.Default.Crop,
                isSelected = styleConfig.logoTag == "custom",
                onClick = onPickGallery
            )

            // Type-Specific Badges
            when (upperType) {
                "UPI" -> {
                    CompactLogoCard(
                        title = "UPI / BHIM",
                        subtitle = "Payment Badge",
                        icon = Icons.Default.CurrencyRupee,
                        isSelected = styleConfig.logoTag == "upi_badge",
                        onClick = {
                            val upiBmp = createTextBadgeBitmap("UPI", 0xFF00796B.toInt(), 0xFFFFFFFF.toInt())
                            onStyleChanged(styleConfig.copy(logoBitmap = upiBmp, logoTag = "upi_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                        }
                    )
                }
                "WHATSAPP", "WAGROUP" -> {
                    CompactLogoCard(
                        title = "WhatsApp",
                        subtitle = "Chat Badge",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        isSelected = styleConfig.logoTag == "wa_badge",
                        onClick = {
                            val waBmp = createTextBadgeBitmap("WA", 0xFF25D366.toInt(), 0xFFFFFFFF.toInt())
                            onStyleChanged(styleConfig.copy(logoBitmap = waBmp, logoTag = "wa_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                        }
                    )
                }
                "WIFI" -> {
                    CompactLogoCard(
                        title = "WiFi Signal",
                        subtitle = "Network Badge",
                        icon = Icons.Default.Wifi,
                        isSelected = styleConfig.logoTag == "wifi_badge",
                        onClick = {
                            val wifiBmp = createTextBadgeBitmap("WIFI", 0xFF0288D1.toInt(), 0xFFFFFFFF.toInt())
                            onStyleChanged(styleConfig.copy(logoBitmap = wifiBmp, logoTag = "wifi_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                        }
                    )
                }
                "URL" -> {
                    CompactLogoCard(
                        title = "Website",
                        subtitle = "Web Link Badge",
                        icon = Icons.Default.Link,
                        isSelected = styleConfig.logoTag == "web_badge",
                        onClick = {
                            val webBmp = createTextBadgeBitmap("WEB", 0xFF3F51B5.toInt(), 0xFFFFFFFF.toInt())
                            onStyleChanged(styleConfig.copy(logoBitmap = webBmp, logoTag = "web_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                        }
                    )
                }
            }

            // Option 3: None (No Logo)
            CompactLogoCard(
                title = "No Logo",
                subtitle = "Clean Pattern",
                icon = Icons.Default.Close,
                isSelected = styleConfig.logoBitmap == null,
                onClick = { onStyleChanged(styleConfig.copy(logoBitmap = null, logoTag = "none")) }
            )
        }
    }
}

@Composable
private fun CompactLogoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) AmberDim else Ink750)
            .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(14.dp))
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
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AmberPrimary else Ink800),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (isSelected) Color(0xFF160E06) else TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = AmberPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AmberSoft else TextPrimary,
                maxLines = 1
            )
            Text(
                subtitle,
                fontSize = 10.5.sp,
                color = TextTertiary,
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
