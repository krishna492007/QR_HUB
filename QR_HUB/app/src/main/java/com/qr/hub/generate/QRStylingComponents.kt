package com.qr.hub.generate

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interactive QR Code Customization UI Section with tabs for Presets, Shapes, Eyes, Frames, and Logos
 */
@Composable
fun QRCustomizationSection(
    styleConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Presets", "Dot Shape", "Corner Eyes", "Frames", "Center Logo")
    val context = LocalContext.current

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
                    onStyleChanged(styleConfig.copy(logoBitmap = bitmap, logoTag = "custom"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                    3 -> FramesTab(styleConfig, onStyleChanged)
                    4 -> CenterLogoTab(styleConfig, onStyleChanged, onPickGallery = { galleryLauncher.launch("image/*") })
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

@Composable
private fun FramesTab(
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QRFrameStyle.values().forEach { frame ->
            val isSelected = currentConfig.frameStyle == frame
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
                                frameStyle = frame,
                                frameText = when (frame) {
                                    QRFrameStyle.PAYMENT_BADGE -> "SCAN & PAY"
                                    else -> "SCAN ME"
                                }
                            )
                        )
                    }
                    .padding(14.dp)
            ) {
                Icon(
                    when (frame) {
                        QRFrameStyle.NONE -> Icons.Default.Block
                        QRFrameStyle.BOTTOM_BADGE -> Icons.Default.CropPortrait
                        QRFrameStyle.PAYMENT_BADGE -> Icons.Default.CurrencyRupee
                        QRFrameStyle.CARD_BORDER -> Icons.Default.Dashboard
                    },
                    null,
                    tint = if (isSelected) AmberPrimary else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        frame.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) AmberSoft else TextPrimary
                    )
                    Text(
                        when (frame) {
                            QRFrameStyle.NONE -> "Plain QR code without extra border"
                            QRFrameStyle.BOTTOM_BADGE -> "Adds bold 'SCAN ME' bottom banner"
                            QRFrameStyle.PAYMENT_BADGE -> "Adds 'SCAN & PAY' badge for UPI"
                            QRFrameStyle.CARD_BORDER -> "Adds sleek rounded card frame"
                        },
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

@Composable
private fun CenterLogoTab(
    currentConfig: QRStyleConfig,
    onStyleChanged: (QRStyleConfig) -> Unit,
    onPickGallery: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Option 1: None
        LogoOptionRow(
            title = "No Center Logo",
            subtitle = "Pure clean QR pattern",
            icon = Icons.Default.Close,
            isSelected = currentConfig.logoBitmap == null,
            onClick = { onStyleChanged(currentConfig.copy(logoBitmap = null, logoTag = "none")) }
        )

        // Option 2: QR Hub Official Logo
        LogoOptionRow(
            title = "QR Hub Official Logo",
            subtitle = "Branded Gold Crown Icon",
            icon = Icons.Default.QrCodeScanner,
            isSelected = currentConfig.logoTag == "app_logo",
            onClick = {
                val logo = try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
                onStyleChanged(currentConfig.copy(logoBitmap = logo, logoTag = "app_logo"))
            }
        )

        // Option 3: Custom Gallery Photo Pick
        LogoOptionRow(
            title = if (currentConfig.logoTag == "custom") "Custom Image Selected" else "Pick Logo from Gallery",
            subtitle = "Upload custom brand/business logo",
            icon = Icons.Default.AddPhotoAlternate,
            isSelected = currentConfig.logoTag == "custom",
            onClick = onPickGallery
        )
    }
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
