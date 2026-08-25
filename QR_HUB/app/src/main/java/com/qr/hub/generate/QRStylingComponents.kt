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
 * Interactive QR Code Customization UI Section with type-aware Frames, Logos, and In-App 1:1 Cropper
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

    // Show In-App Square Cropper Dialog
    if (showCropDialog && rawPickedBitmap != null) {
        ImageCropDialog(
            sourceBitmap = rawPickedBitmap!!,
            onDismiss = { showCropDialog = false },
            onCropApplied = { croppedBmp ->
                showCropDialog = false
                onStyleChanged(
                    styleConfig.copy(
                        logoBitmap = croppedBmp,
                        logoTag = "custom",
                        logoShape = QRLogoShape.ROUNDED_SQUIRCLE
                    )
                )
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Ink800)
            .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberDim2),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Palette, null, tint = AmberSoft, modifier = Modifier.size(17.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Customize QR Design",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Colors, Shapes, Eyes, Frames & Logos",
                        fontSize = 11.5.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AmberPrimary else Ink750)
                            .clickable { selectedTab = index }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF160E06) else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                label = "StylingTabAnimation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> PresetsTab(styleConfig, onStyleChanged)
                    1 -> DotShapesTab(styleConfig, onStyleChanged)
                    2 -> CornerEyesTab(styleConfig, onStyleChanged)
                    3 -> TypeAwareFramesTab(qrType, styleConfig, onStyleChanged)
                    4 -> TypeAwareCenterLogoTab(qrType, styleConfig, onStyleChanged, onPickGallery = { galleryLauncher.launch("image/*") })
                }
            }
        }
    }
}

@Composable
private fun PresetsTab(
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QRPreset.values().forEach { preset ->
            val isSelected = currentConfig.moduleShape == preset.config.moduleShape &&
                    currentConfig.fgColor == preset.config.fgColor &&
                    currentConfig.gradientType == preset.config.gradientType

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(12.dp)
            ) {
                // Color preview badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(preset.previewBg))
                        .border(1.5.dp, Color(preset.previewFg), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(preset.previewFg))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        preset.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) AmberSoft else TextPrimary
                    )
                    Text(
                        preset.subtitle,
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                }

                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(20.dp)
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) AmberDim else Ink750)
                    .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(14.dp))
                    .clickable { onStyleChanged(currentConfig.copy(moduleShape = shape)) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Preview Icon
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(
                                when (shape) {
                                    QRModuleShape.SQUARE -> RoundedCornerShape(0.dp)
                                    QRModuleShape.ROUNDED -> RoundedCornerShape(6.dp)
                                    QRModuleShape.CIRCLE -> CircleShape
                                    QRModuleShape.DIAMOND -> RoundedCornerShape(4.dp)
                                }
                            )
                            .background(if (isSelected) AmberPrimary else TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        shape.displayName,
                        fontSize = 11.5.sp,
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) AmberDim else Ink750)
                    .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(14.dp))
                    .clickable { onStyleChanged(currentConfig.copy(eyeShape = eye)) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Finder eye preview icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(
                                when (eye) {
                                    QREyeShape.SQUARE -> RoundedCornerShape(2.dp)
                                    QREyeShape.ROUNDED -> RoundedCornerShape(8.dp)
                                    QREyeShape.CIRCULAR -> CircleShape
                                }
                            )
                            .border(
                                3.dp,
                                if (isSelected) AmberPrimary else TextSecondary,
                                when (eye) {
                                    QREyeShape.SQUARE -> RoundedCornerShape(2.dp)
                                    QREyeShape.ROUNDED -> RoundedCornerShape(8.dp)
                                    QREyeShape.CIRCULAR -> CircleShape
                                }
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(
                                    when (eye) {
                                        QREyeShape.SQUARE -> RoundedCornerShape(1.dp)
                                        QREyeShape.ROUNDED -> RoundedCornerShape(3.dp)
                                        QREyeShape.CIRCULAR -> CircleShape
                                    }
                                )
                                .background(if (isSelected) AmberPrimary else TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        eye.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AmberSoft else TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Type-Aware Frame Options (adapts dynamically to UPI, WhatsApp, WiFi, URL, etc.)
 */
@Composable
private fun TypeAwareFramesTab(
    qrType: String,
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    val upperType = qrType.uppercase()

    val options = when (upperType) {
        "UPI" -> listOf(
            FrameOption(QRFrameStyle.PAYMENT_BADGE, "SCAN & PAY", "Recommended for UPI Payment QR", Icons.Default.CurrencyRupee),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "PAY VIA UPI", "Bold UPI action banner", Icons.Default.AccountBalance),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic scan banner", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Modern rounded border", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.NONE, "NONE", "No frame border", Icons.Default.Block)
        )
        "WHATSAPP", "WAGROUP" -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "CHAT ON WHATSAPP", "Direct chat action banner", Icons.AutoMirrored.Filled.Chat),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN TO CHAT", "Action banner for messaging", Icons.Default.Sms),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic scan banner", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Modern rounded border", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.NONE, "NONE", "No frame border", Icons.Default.Block)
        )
        "WIFI" -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "CONNECT TO WIFI", "Connect action banner", Icons.Default.Wifi),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN TO CONNECT", "Quick network connection", Icons.Default.SignalWifi4Bar),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic scan banner", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Modern rounded border", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.NONE, "NONE", "No frame border", Icons.Default.Block)
        )
        "URL" -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "VISIT WEBSITE", "Website open action banner", Icons.Default.Link),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN TO OPEN", "Link navigation banner", Icons.Default.OpenInBrowser),
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Classic scan banner", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Modern rounded border", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.NONE, "NONE", "No frame border", Icons.Default.Block)
        )
        else -> listOf(
            FrameOption(QRFrameStyle.BOTTOM_BADGE, "SCAN ME", "Bold bottom scan banner", Icons.Default.CropPortrait),
            FrameOption(QRFrameStyle.CARD_BORDER, "CARD BORDER", "Modern rounded card border", Icons.Default.Dashboard),
            FrameOption(QRFrameStyle.NONE, "NONE", "Plain QR code without frame", Icons.Default.Block)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { opt ->
            val isSelected = (opt.style == QRFrameStyle.NONE && currentConfig.frameStyle == QRFrameStyle.NONE) ||
                    (opt.style == QRFrameStyle.CARD_BORDER && currentConfig.frameStyle == QRFrameStyle.CARD_BORDER) ||
                    (opt.style != QRFrameStyle.NONE && opt.style != QRFrameStyle.CARD_BORDER && currentConfig.frameStyle != QRFrameStyle.NONE && currentConfig.frameStyle != QRFrameStyle.CARD_BORDER && currentConfig.frameText == opt.text)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(14.dp)
            ) {
                Icon(
                    opt.icon,
                    null,
                    tint = if (isSelected) AmberPrimary else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        opt.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) AmberSoft else TextPrimary
                    )
                    Text(
                        opt.subtitle,
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                }

                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
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
 * Type-Aware Center Logo Options with Logo Shape Selector (Squircle, Circle, Square)
 */
@Composable
private fun TypeAwareCenterLogoTab(
    qrType: String,
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit,
    onPickGallery: () -> Unit
) {
    val context = LocalContext.current
    val upperType = qrType.uppercase()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── 1. LOGO SHAPE SELECTOR (SQUIRCLE, CIRCLE, SQUARE) ──
        if (currentConfig.logoBitmap != null) {
            Column {
                Text(
                    "Logo Badge Shape",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QRLogoShape.values().forEach { shape ->
                        val isSelected = currentConfig.logoShape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AmberDim else Ink750)
                                .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(10.dp))
                                .clickable { onStyleChanged(currentConfig.copy(logoShape = shape)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                shape.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AmberSoft else TextPrimary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = BorderLine, modifier = Modifier.padding(vertical = 4.dp))
        }

        // ── 2. LOGO CHOICES ──

        // Option 1: Official App Logo (Default Branded Choice)
        LogoOptionRow(
            title = "QR Hub Official Logo (Default)",
            subtitle = "Gold Square App Crown Icon",
            icon = Icons.Default.QrCodeScanner,
            isSelected = currentConfig.logoTag == "app_logo",
            onClick = {
                val logo = try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
                onStyleChanged(
                    currentConfig.copy(
                        logoBitmap = logo,
                        logoTag = "app_logo",
                        logoShape = QRLogoShape.ROUNDED_SQUIRCLE
                    )
                )
            }
        )

        // Option 2: Custom Gallery Photo with In-App 1:1 Cropper
        LogoOptionRow(
            title = if (currentConfig.logoTag == "custom") "Custom Logo (Tap to Re-Crop)" else "Custom Logo (Pick & Crop 1:1)",
            subtitle = "Select any photo & crop in exact square",
            icon = Icons.Default.Crop,
            isSelected = currentConfig.logoTag == "custom",
            onClick = onPickGallery
        )

        // Type-Specific Branded Logo Badge
        when (upperType) {
            "UPI" -> {
                LogoOptionRow(
                    title = "UPI / BHIM Pay Badge",
                    subtitle = "Official UPI Payment Logo Badge",
                    icon = Icons.Default.CurrencyRupee,
                    isSelected = currentConfig.logoTag == "upi_badge",
                    onClick = {
                        val upiBmp = createTextBadgeBitmap("UPI", 0xFF00796B.toInt(), 0xFFFFFFFF.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = upiBmp, logoTag = "upi_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                    }
                )
            }
            "WHATSAPP", "WAGROUP" -> {
                LogoOptionRow(
                    title = "WhatsApp Logo Badge",
                    subtitle = "Official Green Chat Badge",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    isSelected = currentConfig.logoTag == "wa_badge",
                    onClick = {
                        val waBmp = createTextBadgeBitmap("WA", 0xFF25D366.toInt(), 0xFFFFFFFF.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = waBmp, logoTag = "wa_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                    }
                )
            }
            "WIFI" -> {
                LogoOptionRow(
                    title = "WiFi Signal Badge",
                    subtitle = "WiFi Network Connection Badge",
                    icon = Icons.Default.Wifi,
                    isSelected = currentConfig.logoTag == "wifi_badge",
                    onClick = {
                        val wifiBmp = createTextBadgeBitmap("WIFI", 0xFF0288D1.toInt(), 0xFFFFFFFF.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = wifiBmp, logoTag = "wifi_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                    }
                )
            }
            "URL" -> {
                LogoOptionRow(
                    title = "Web Link Badge",
                    subtitle = "Website Navigation Badge",
                    icon = Icons.Default.Link,
                    isSelected = currentConfig.logoTag == "web_badge",
                    onClick = {
                        val webBmp = createTextBadgeBitmap("WEB", 0xFF3F51B5.toInt(), 0xFFFFFFFF.toInt())
                        onStyleChanged(currentConfig.copy(logoBitmap = webBmp, logoTag = "web_badge", logoShape = QRLogoShape.ROUNDED_SQUIRCLE))
                    }
                )
            }
        }

        // Option 4: None
        LogoOptionRow(
            title = "No Center Logo",
            subtitle = "Pure clean QR pattern without logo",
            icon = Icons.Default.Close,
            isSelected = currentConfig.logoBitmap == null,
            onClick = { onStyleChanged(currentConfig.copy(logoBitmap = null, logoTag = "none")) }
        )
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

@Composable
private fun LogoOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) AmberDim else Ink750)
            .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) AmberPrimary else Ink800),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (isSelected) Color(0xFF160E06) else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) AmberSoft else TextPrimary
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = TextTertiary
            )
        }

        if (isSelected) {
            Icon(Icons.Default.CheckCircle, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
        }
    }
}
