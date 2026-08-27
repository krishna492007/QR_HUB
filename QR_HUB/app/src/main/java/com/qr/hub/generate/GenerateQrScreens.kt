package com.qr.hub.generate

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.qr.hub.util.ads.AdManager
import com.qr.hub.util.ads.BannerAdView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.util.*
import com.qr.hub.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Calendar
import androidx.lifecycle.viewmodel.compose.viewModel

// =====================================================
// REDESIGNED FORM COLORS
// =====================================================
private val FormCardBg = Ink800
private val FormCardBorder = BorderLine
private val QrDisplayBg = Ink900
private val ButtonActive = AmberCtaGradient
private val AccentPurple = AmberSoft
private val AccentPink = AmberPrimary

sealed class QRType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Bulk : QRType("Bulk QR", Icons.Default.DynamicFeed)
    object Barcode : QRType("Barcode", Icons.Default.ViewWeek)
    object Text : QRType("Text", Icons.Default.TextFields)
    object URL : QRType("URL", Icons.Default.Link)
    object UPI : QRType("UPI", Icons.Default.AccountBalance)
    object WhatsApp : QRType("WhatsApp", Icons.AutoMirrored.Filled.Chat)
    object WAGroup : QRType("WA Group", Icons.Default.Group)
    object Phone : QRType("Phone", Icons.Default.Phone)
    object SMS : QRType("SMS", Icons.Default.Sms)
    object Email : QRType("Email", Icons.Default.Email)
    object Contact : QRType("Contact", Icons.Default.Person)
    object WiFi : QRType("WiFi", Icons.Default.Wifi)
    object Location : QRType("Location", Icons.Default.LocationOn)
    object Event : QRType("Event", Icons.Default.Event)

    companion object {
        val allTypes = listOf(Bulk, Barcode, Text, URL, UPI, WhatsApp, WAGroup, Phone, SMS, Email, Contact, WiFi, Location, Event)
    }
}

// =====================================================
// TYPE SELECTION SCREEN
// =====================================================

// =====================================================
// TYPE SELECTION SCREEN (Redesigned Ink & Amber)
// =====================================================

@Composable
fun GenerateQrTypeSelectionScreen(
    isDark: Boolean,
    onTypeSelected: (QRType) -> Unit,
    onPrivacyPolicyClick: (() -> Unit)? = null
) {
    var selectedType by remember { mutableStateOf<QRType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dotfieldBackground(isDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ============================================
            // EYEBROW & TITLE HEADER
            // ============================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 1.5.dp)
                        .background(appGoldPrimary(isDark))
                )
                Text(
                    text = "CREATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = appGoldSoft(isDark),
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Generate a QR",
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold,
                color = appTextPrimary(isDark),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Pick a type below to get started",
                fontSize = 13.5.sp,
                color = appTextSecondary(isDark),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ============================================
            // 3-COLUMN TYPE GRID + FULL-WIDTH BULK CARD
            // ============================================
            val standardTypes = remember {
                listOf(
                    QRType.Text, QRType.URL, QRType.UPI,
                    QRType.WhatsApp, QRType.WAGroup, QRType.Phone,
                    QRType.SMS, QRType.Email, QRType.Contact,
                    QRType.WiFi, QRType.Location, QRType.Event
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 12 Standard Types (3 columns each)
                items(standardTypes.size) { index ->
                    val type = standardTypes[index]
                    val isSelected = selectedType == type

                    GenerateTypeTile(
                        type = type,
                        isSelected = isSelected,
                        isDark = isDark,
                        onClick = {
                            selectedType = if (isSelected) null else type
                        }
                    )
                }

                // 2 Horizontally Aligned Wide Cards at Bottom (Bulk QR & Product Barcode)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        // Bulk QR Generator Tile
                        GenerateWideTile(
                            title = "Bulk QR",
                            subtitle = "Multi-batch mode",
                            icon = Icons.Default.Layers,
                            isSelected = selectedType == QRType.Bulk,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedType = if (selectedType == QRType.Bulk) null else QRType.Bulk
                            }
                        )

                        // Barcode Generator Tile
                        GenerateWideTile(
                            title = "Barcode",
                            subtitle = "1D Retail codes",
                            icon = Icons.Default.QrCode,
                            isSelected = selectedType == QRType.Barcode,
                            isDark = isDark,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedType = if (selectedType == QRType.Barcode) null else QRType.Barcode
                            }
                        )
                    }
                }
            }
        }

        // ============================================
        // FLOATING BOTTOM ACTION CTA (Transparent Backdrop)
        // ============================================
        val buttonEnabled = selectedType != null
        val ctaScale by animateFloatAsState(
            targetValue = if (buttonEnabled) 1.0f else 0.98f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "CtaScale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            appBg(isDark).copy(alpha = 0.4f),
                            appBg(isDark).copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(horizontal = 22.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(ctaScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (buttonEnabled) appCtaGradient(isDark)
                        else Brush.linearGradient(listOf(appCardBg(isDark).copy(alpha = 0.6f), appCardBg(isDark).copy(alpha = 0.6f)))
                    )
                    .border(
                        1.dp,
                        if (buttonEnabled) appGoldPrimary(isDark).copy(alpha = 0.8f) else appBorder(isDark).copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        enabled = buttonEnabled
                    ) {
                        selectedType?.let { onTypeSelected(it) }
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = selectedType?.let { "Continue with ${it.label}" } ?: "Select a type to continue",
                    transitionSpec = {
                        (fadeIn(tween(180)) + slideInVertically(tween(180)) { 20 }) togetherWith
                        (fadeOut(tween(140)) + slideOutVertically(tween(140)) { -20 })
                    },
                    label = "CtaTextAnimation"
                ) { targetLabel ->
                    Text(
                        text = targetLabel,
                        fontSize = 15.sp,
                        fontWeight = if (buttonEnabled) FontWeight.Bold else FontWeight.Medium,
                        color = if (buttonEnabled) Color(0xFF20140A) else appTextTertiary(isDark)
                    )
                }
            }
        }
    }
}

@Composable
private fun GenerateTypeTile(
    type: QRType,
    isSelected: Boolean,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) appGoldPrimary(isDark).copy(alpha = 0.5f) else appBorder(isDark),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "TileBorder"
    )
    val tileScale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "TileScale"
    )
    val iconBg by animateColorAsState(
        targetValue = if (isSelected) (if (isDark) Color(0xFF382A14) else Color(0xFFFAE8CD)) else appElevatedBg(isDark),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "TileIconBg"
    )
    val iconBorderColor by animateColorAsState(
        targetValue = if (isSelected) appGoldPrimary(isDark).copy(alpha = 0.35f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "TileIconBorder"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) appGoldSoft(isDark) else appTextPrimary(isDark),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "TileIconTint"
    )
    val bracketAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "BracketAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(tileScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    if (isDark) Brush.verticalGradient(listOf(Color(0xFF241C14), Ink800))
                    else Brush.verticalGradient(listOf(Color(0xFFFDF6EC), CeramicCard))
                } else {
                    if (isDark) Brush.linearGradient(listOf(Ink800, Ink800))
                    else Brush.linearGradient(listOf(CeramicCard, CeramicCard))
                }
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .drawWithContent {
                drawContent()
                if (bracketAlpha > 0f) {
                    val s = 2.5.dp.toPx()
                    val len = 12.dp.toPx()
                    val r = 5.dp.toPx()
                    val pad = 6.dp.toPx()
                    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = s,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                    val color = appGoldPrimary(isDark).copy(alpha = bracketAlpha)

                    // Top-Left (top: 6px, left: 6px)
                    val pathTL = androidx.compose.ui.graphics.Path().apply {
                        moveTo(pad, pad + len)
                        lineTo(pad, pad + r)
                        quadraticTo(pad, pad, pad + r, pad)
                        lineTo(pad + len, pad)
                    }
                    drawPath(pathTL, color, style = stroke)

                    // Top-Right (top: 6px, right: 6px)
                    val pathTR = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width - pad - len, pad)
                        lineTo(size.width - pad - r, pad)
                        quadraticTo(size.width - pad, pad, size.width - pad, pad + r)
                        lineTo(size.width - pad, pad + len)
                    }
                    drawPath(pathTR, color, style = stroke)

                    // Bottom-Left (bottom: 6px, left: 6px)
                    val pathBL = androidx.compose.ui.graphics.Path().apply {
                        moveTo(pad, size.height - pad - len)
                        lineTo(pad, size.height - pad - r)
                        quadraticTo(pad, size.height - pad, pad + r, size.height - pad)
                        lineTo(pad + len, size.height - pad)
                    }
                    drawPath(pathBL, color, style = stroke)

                    // Bottom-Right (bottom: 6px, right: 6px)
                    val pathBR = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width - pad - len, size.height - pad)
                        lineTo(size.width - pad - r, size.height - pad)
                        quadraticTo(size.width - pad, size.height - pad, size.width - pad, size.height - pad - r)
                        lineTo(size.width - pad, size.height - pad - len)
                    }
                    drawPath(pathBR, color, style = stroke)
                }
            }
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg)
                    .border(1.dp, iconBorderColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = type.label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Label
            Text(
                text = type.label,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) appGoldPrimary(isDark) else appTextPrimary(isDark),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GenerateWideTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isDark: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) appGoldPrimary(isDark).copy(alpha = 0.5f) else appBorder(isDark),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "WideTileBorder"
    )
    val tileScale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "WideTileScale"
    )
    val iconBg by animateColorAsState(
        targetValue = if (isSelected) (if (isDark) Color(0xFF382A14) else Color(0xFFFAE8CD)) else appElevatedBg(isDark),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "WideTileIconBg"
    )
    val iconBorderColor by animateColorAsState(
        targetValue = if (isSelected) appGoldPrimary(isDark).copy(alpha = 0.35f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "WideTileIconBorder"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) appGoldSoft(isDark) else appTextPrimary(isDark),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "WideTileIconTint"
    )
    val bracketAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "WideBracketAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(tileScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    if (isDark) Brush.verticalGradient(listOf(Color(0xFF241C14), Ink800))
                    else Brush.verticalGradient(listOf(Color(0xFFFDF6EC), CeramicCard))
                } else {
                    if (isDark) Brush.linearGradient(listOf(Ink800, Ink800))
                    else Brush.linearGradient(listOf(CeramicCard, CeramicCard))
                }
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .drawWithContent {
                drawContent()
                if (bracketAlpha > 0f) {
                    val s = 2.5.dp.toPx()
                    val len = 12.dp.toPx()
                    val r = 5.dp.toPx()
                    val pad = 6.dp.toPx()
                    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = s,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                    val color = appGoldPrimary(isDark).copy(alpha = bracketAlpha)

                    // Top-Left
                    val pathTL = androidx.compose.ui.graphics.Path().apply {
                        moveTo(pad, pad + len)
                        lineTo(pad, pad + r)
                        quadraticTo(pad, pad, pad + r, pad)
                        lineTo(pad + len, pad)
                    }
                    drawPath(pathTL, color, style = stroke)

                    // Top-Right
                    val pathTR = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width - pad - len, pad)
                        lineTo(size.width - pad - r, pad)
                        quadraticTo(size.width - pad, pad, size.width - pad, pad + r)
                        lineTo(size.width - pad, pad + len)
                    }
                    drawPath(pathTR, color, style = stroke)

                    // Bottom-Left
                    val pathBL = androidx.compose.ui.graphics.Path().apply {
                        moveTo(pad, size.height - pad - len)
                        lineTo(pad, size.height - pad - r)
                        quadraticTo(pad, size.height - pad, pad + r, size.height - pad)
                        lineTo(pad + len, size.height - pad)
                    }
                    drawPath(pathBL, color, style = stroke)

                    // Bottom-Right
                    val pathBR = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width - pad - len, size.height - pad)
                        lineTo(size.width - pad - r, size.height - pad)
                        quadraticTo(size.width - pad, size.height - pad, size.width - pad, size.height - pad - r)
                        lineTo(size.width - pad, size.height - pad - len)
                    }
                    drawPath(pathBR, color, style = stroke)
                }
            }
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg)
                    .border(1.dp, iconBorderColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = if (isSelected) AmberSoft.copy(alpha = 0.8f) else TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CornerBrackets(
    modifier: Modifier = Modifier,
    color: Color = AmberSoft,
    bracketSize: androidx.compose.ui.unit.Dp = 12.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 2.5.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 6.dp
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val bSize = bracketSize.toPx()
        val sWidth = strokeWidth.toPx()
        val r = cornerRadius.toPx()
        val half = sWidth / 2f
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
            width = sWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )

        // Top-Left ┌
        val pathTL = androidx.compose.ui.graphics.Path().apply {
            moveTo(half, bSize)
            lineTo(half, r)
            quadraticTo(half, half, r, half)
            lineTo(bSize, half)
        }
        drawPath(pathTL, color, style = stroke)

        // Top-Right ┐
        val pathTR = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width - bSize, half)
            lineTo(size.width - r, half)
            quadraticTo(size.width - half, half, size.width - half, r)
            lineTo(size.width - half, bSize)
        }
        drawPath(pathTR, color, style = stroke)

        // Bottom-Left └
        val pathBL = androidx.compose.ui.graphics.Path().apply {
            moveTo(half, size.height - bSize)
            lineTo(half, size.height - r)
            quadraticTo(half, size.height - half, r, size.height - half)
            lineTo(bSize, size.height - half)
        }
        drawPath(pathBL, color, style = stroke)

        // Bottom-Right ┘
        val pathBR = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width - bSize, size.height - half)
            lineTo(size.width - r, size.height - half)
            quadraticTo(size.width - half, size.height - half, size.width - half, size.height - r)
            lineTo(size.width - half, size.height - bSize)
        }
        drawPath(pathBR, color, style = stroke)
    }
}

private fun getTypeCardColor(type: QRType): Color = when (type) {
    QRType.Bulk -> AmberPrimary
    QRType.Barcode -> AmberPrimary
    QRType.Text -> Color(0xFFFFA726)
    QRType.URL -> Color(0xFF42A5F5)
    QRType.UPI -> Color(0xFF66BB6A)
    QRType.WhatsApp -> Color(0xFF4CAF50)
    QRType.WAGroup -> Color(0xFF00BFA5)
    QRType.Phone -> Color(0xFF29B6F6)
    QRType.SMS -> Color(0xFFAB47BC)
    QRType.Email -> Color(0xFFEF5350)
    QRType.Contact -> Color(0xFF5C6BC0)
    QRType.WiFi -> Color(0xFF26A69A)
    QRType.Location -> Color(0xFFFF7043)
    QRType.Event -> Color(0xFFEC407A)
}

// =====================================================
// VALIDATION
// =====================================================

private fun isValidPhone(number: String): Boolean {
    val digits = number.replace("[^0-9+]".toRegex(), "")
    return digits.length >= 10
}

private fun isValidEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".")
}

// =====================================================
// TEXT QR
// =====================================================

@Composable
fun GenerateTextQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "Text QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildTextContent(text))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = text.isNotEmpty(),
        generatedBitmap = generatedBitmap,
        qrType = "TEXT",
        getContent = { QRGenerator.buildTextContent(text) }
    ) {
        // Hint row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = AccentPurple.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enter any text to encode into a QR code", fontSize = 12.sp, color = TextSecondary)
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            placeholder = { Text("Type your message, notes, or any text...") },
            colors = premiumFieldColors()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("${text.length} characters", fontSize = 12.sp, color = TextTertiary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
    }
}

// =====================================================
// URL QR
// =====================================================

@Composable
fun GenerateUrlQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var url by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "URL QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildUrlContent(url))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = url.isNotEmpty(),
        generatedBitmap = generatedBitmap,
        qrType = "URL",
        getContent = { QRGenerator.buildUrlContent(url) }
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Enter URL (e.g., google.com)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Link, null, tint = AccentPurple) },
            colors = premiumFieldColors()
        )
    }
}

// =====================================================
// UPI QR
// =====================================================

@Composable
fun GenerateUpiQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var vpa by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "UPI Payment QR",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildUpiContent(vpa, name, amount, note))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = vpa.isNotEmpty() && vpa.contains("@"),
        generatedBitmap = generatedBitmap,
        qrType = "UPI",
        getContent = { QRGenerator.buildUpiContent(vpa, name, amount, note) }
    ) {
        OutlinedTextField(
            value = vpa,
            onValueChange = { vpa = it },
            label = { Text("UPI ID / VPA *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("example@upi") },
            isError = vpa.isNotEmpty() && !vpa.contains("@"),
            supportingText = if (vpa.isNotEmpty() && !vpa.contains("@")) {
                { Text("UPI ID must contain @", color = Color(0xFFEF5350)) }
            } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Payee Name") }, modifier = Modifier.fillMaxWidth(), colors = premiumFieldColors())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note / Remark") }, modifier = Modifier.fillMaxWidth(), colors = premiumFieldColors())
    }
}

// =====================================================
// PHONE QR
// =====================================================

@Composable
fun GeneratePhoneQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var number by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "Phone QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildPhoneContent(number))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = isValidPhone(number),
        generatedBitmap = generatedBitmap,
        qrType = "PHONE",
        getContent = { QRGenerator.buildPhoneContent(number) }
    ) {
        OutlinedTextField(
            value = number,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^[+\\d\\s()-]*$"))) number = it },
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("+91 98765 43210") },
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = AccentPurple) },
            isError = number.isNotEmpty() && !isValidPhone(number),
            supportingText = if (number.isNotEmpty() && !isValidPhone(number)) {
                { Text("Min 10 digits required", color = Color(0xFFEF5350)) }
            } else null,
            colors = premiumFieldColors()
        )
    }
}

// =====================================================
// SMS QR
// =====================================================

@Composable
fun GenerateSmsQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var number by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "SMS QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildSmsContent(number, message))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = isValidPhone(number),
        generatedBitmap = generatedBitmap,
        qrType = "SMS",
        getContent = { QRGenerator.buildSmsContent(number, message) }
    ) {
        OutlinedTextField(
            value = number,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^[+\\d\\s()-]*$"))) number = it },
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            isError = number.isNotEmpty() && !isValidPhone(number),
            supportingText = if (number.isNotEmpty() && !isValidPhone(number)) {
                { Text("Min 10 digits required", color = Color(0xFFEF5350)) }
            } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3, maxLines = 5,
            colors = premiumFieldColors()
        )
    }
}

// =====================================================
// EMAIL QR
// =====================================================

@Composable
fun GenerateEmailQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var address by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "Email QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildEmailContent(address, subject, body))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = isValidEmail(address),
        generatedBitmap = generatedBitmap,
        qrType = "EMAIL",
        getContent = { QRGenerator.buildEmailContent(address, subject, body) }
    ) {
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Email Address *") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null, tint = AccentPurple) },
            isError = address.isNotEmpty() && !isValidEmail(address),
            supportingText = if (address.isNotEmpty() && !isValidEmail(address)) {
                { Text("Enter a valid email address", color = Color(0xFFEF5350)) }
            } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), colors = premiumFieldColors())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5, colors = premiumFieldColors())
    }
}

// =====================================================
// WIFI QR
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateWifiQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var encryption by remember { mutableStateOf("WPA") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "WiFi QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildWifiContent(ssid, password, encryption))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = ssid.isNotEmpty(),
        generatedBitmap = generatedBitmap,
        qrType = "WIFI",
        getContent = { QRGenerator.buildWifiContent(ssid, password, encryption) }
    ) {
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text("Network Name (SSID) *") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Wifi, null, tint = AccentPurple) },
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), colors = premiumFieldColors())
        Spacer(modifier = Modifier.height(12.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = encryption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Security Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                colors = premiumFieldColors()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("WPA", "WEP", "None").forEach { displayEnc ->
                    val actualEnc = if (displayEnc == "None") "None" else displayEnc
                    DropdownMenuItem(text = { Text(displayEnc) }, onClick = { encryption = actualEnc; expanded = false })
                }
            }
        }
    }
}

// =====================================================
// CONTACT QR
// =====================================================

@Composable
fun GenerateContactQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var org by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val phoneValid = phone.isEmpty() || isValidPhone(phone)
    val emailValid = email.isEmpty() || isValidEmail(email)

    GenerateQrFormScreen(
        title = "Contact QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildContactContent(name, phone, email, org, title))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = name.isNotEmpty() && phoneValid && emailValid,
        generatedBitmap = generatedBitmap,
        qrType = "CONTACT",
        getContent = { QRGenerator.buildContactContent(name, phone, email, org, title) }
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null, tint = AccentPurple) }, colors = premiumFieldColors())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^[+\\d\\s()-]*$"))) phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            isError = !phoneValid,
            supportingText = if (!phoneValid) { { Text("Min 10 digits required", color = Color(0xFFEF5350)) } } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = !emailValid,
            supportingText = if (!emailValid) { { Text("Enter a valid email", color = Color(0xFFEF5350)) } } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = org, onValueChange = { org = it }, label = { Text("Organization") }, modifier = Modifier.fillMaxWidth(), colors = premiumFieldColors())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth(), colors = premiumFieldColors())
    }
}

// =====================================================
// WHATSAPP QR
// =====================================================

@Composable
fun GenerateWhatsAppQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var number by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "WhatsApp QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildWhatsAppContent(number, message))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = isValidPhone(number),
        generatedBitmap = generatedBitmap,
        qrType = "WHATSAPP",
        getContent = { QRGenerator.buildWhatsAppContent(number, message) }
    ) {
        OutlinedTextField(
            value = number,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^[+\\d\\s()-]*$"))) number = it },
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("With country code, e.g., 919876543210") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color(0xFF4CAF50)) },
            isError = number.isNotEmpty() && !isValidPhone(number),
            supportingText = if (number.isNotEmpty() && !isValidPhone(number)) {
                { Text("Min 10 digits required", color = Color(0xFFEF5350)) }
            } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Pre-filled Message") },
            modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5, colors = premiumFieldColors())
    }
}

// =====================================================
// WA GROUP QR
// =====================================================

@Composable
fun GenerateWAGroupQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var inviteCode by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    GenerateQrFormScreen(
        title = "WA Group QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildWhatsAppGroupContent(inviteCode))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = inviteCode.isNotEmpty(),
        generatedBitmap = generatedBitmap,
        qrType = "WHATSAPP",
        getContent = { QRGenerator.buildWhatsAppGroupContent(inviteCode) }
    ) {
        OutlinedTextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text("Group Invite Link / Code *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://chat.whatsapp.com/ABC123...") },
            leadingIcon = { Icon(Icons.Default.Group, null, tint = Color(0xFF00BFA5)) },
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Paste the full invite link or just the code", fontSize = 12.sp, color = TextSecondary)
    }
}

// =====================================================
// LOCATION QR
// =====================================================

@Composable
fun GenerateLocationQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val isValidCoords = try {
        latitude.isNotEmpty() && longitude.isNotEmpty() &&
                latitude.toDouble() in -90.0..90.0 && longitude.toDouble() in -180.0..180.0
    } catch (_: Exception) { false }
    val latError = latitude.isNotEmpty() && try { latitude.toDouble() !in -90.0..90.0 } catch (_: Exception) { true }
    val lngError = longitude.isNotEmpty() && try { longitude.toDouble() !in -180.0..180.0 } catch (_: Exception) { true }

    GenerateQrFormScreen(
        title = "Location QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildLocationContent(latitude, longitude, label))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = isValidCoords,
        generatedBitmap = generatedBitmap,
        qrType = "LOCATION",
        getContent = { QRGenerator.buildLocationContent(latitude, longitude, label) }
    ) {
        OutlinedTextField(
            value = latitude,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d*$"))) latitude = it },
            label = { Text("Latitude *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. 28.6139") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = AccentPurple) },
            isError = latError,
            supportingText = if (latError) { { Text("Must be -90 to 90", color = Color(0xFFEF5350)) } } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = longitude,
            onValueChange = { if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d*$"))) longitude = it },
            label = { Text("Longitude *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. 77.2090") },
            isError = lngError,
            supportingText = if (lngError) { { Text("Must be -180 to 180", color = Color(0xFFEF5350)) } } else null,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Place Name (optional)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. India Gate, New Delhi") },
            colors = premiumFieldColors()
        )
    }
}

// =====================================================
// EVENT QR (Date/Time Picker + VCALENDAR format)
// =====================================================

@Composable
fun GenerateEventQrScreen(isDark: Boolean, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var startDisplay by remember { mutableStateOf("") }
    var endDisplay by remember { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    fun showDateTimePicker(onResult: (formatted: String, display: String) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val formatted = String.format("%04d%02d%02dT%02d%02d00", year, month + 1, day, hour, minute)
                        val display = String.format("%02d %s %04d, %02d:%02d",
                            day,
                            listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[month],
                            year, hour, minute
                        )
                        onResult(formatted, display)
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    GenerateQrFormScreen(
        title = "Event QR Code",
        isDark = isDark,
        onBack = onBack,
        onGenerate = { logo ->
            val qr = QRGenerator.generateStandardQRBitmap(QRGenerator.buildEventContent(title, location, description, startDate, endDate))
            generatedBitmap = if (logo != null) QRGenerator.overlayLogoOnQR(qr, logo) else qr
        },
        isValid = title.isNotEmpty(),
        generatedBitmap = generatedBitmap,
        qrType = "EVENT",
        getContent = { QRGenerator.buildEventContent(title, location, description, startDate, endDate) }
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title *") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Team Meeting") },
            leadingIcon = { Icon(Icons.Default.Event, null, tint = AccentPurple) },
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Conference Room A") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = AccentPurple.copy(alpha = 0.7f)) },
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2, maxLines = 3,
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Start Date/Time — Tap to pick
        OutlinedTextField(
            value = startDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text("Start Date & Time") },
            modifier = Modifier.fillMaxWidth().clickable {
                showDateTimePicker { f, d -> startDate = f; startDisplay = d }
            },
            placeholder = { Text("Tap to select") },
            trailingIcon = {
                IconButton(onClick = { showDateTimePicker { f, d -> startDate = f; startDisplay = d } }) {
                    Icon(Icons.Default.CalendarMonth, "Pick date", tint = AccentPurple)
                }
            },
            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = AccentPurple.copy(alpha = 0.7f)) },
            colors = premiumFieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // End Date/Time — Tap to pick
        OutlinedTextField(
            value = endDisplay,
            onValueChange = {},
            readOnly = true,
            label = { Text("End Date & Time") },
            modifier = Modifier.fillMaxWidth().clickable {
                showDateTimePicker { f, d -> endDate = f; endDisplay = d }
            },
            placeholder = { Text("Tap to select") },
            trailingIcon = {
                IconButton(onClick = { showDateTimePicker { f, d -> endDate = f; endDisplay = d } }) {
                    Icon(Icons.Default.CalendarMonth, "Pick date", tint = AccentPurple)
                }
            },
            leadingIcon = { Icon(Icons.Default.Schedule, null, tint = AccentPurple.copy(alpha = 0.7f)) },
            colors = premiumFieldColors()
        )
    }
}

// =====================================================
// REDESIGNED FIELD COLORS — Dynamic Ink & Ceramic
// =====================================================

@Composable
private fun premiumFieldColors(isDark: Boolean = true) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = appGoldPrimary(isDark),
    unfocusedBorderColor = appBorder(isDark),
    cursorColor = appGoldPrimary(isDark),
    focusedLabelColor = appGoldSoft(isDark),
    unfocusedLabelColor = appTextSecondary(isDark),
    focusedTextColor = appTextPrimary(isDark),
    unfocusedTextColor = appTextPrimary(isDark),
    unfocusedPlaceholderColor = appTextTertiary(isDark),
    focusedPlaceholderColor = appTextTertiary(isDark),
    focusedContainerColor = appElevatedBg(isDark),
    unfocusedContainerColor = appElevatedBg(isDark)
)

// =====================================================
// SHARED REDESIGNED FORM SCREEN — Dynamic Ink & Ceramic
// =====================================================

@Composable
private fun GenerateQrFormScreen(
    title: String,
    isDark: Boolean,
    onBack: () -> Unit,
    onGenerate: (Bitmap?) -> Unit,
    isValid: Boolean,
    generatedBitmap: Bitmap?,
    qrType: String,
    getContent: () -> String,
    content: @Composable ColumnScope.() -> Unit
) {
    val historyViewModel: HistoryViewModel = viewModel()
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var lastSavedContent by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg(isDark))
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .imePadding()
    ) {
        // ── TOP BAR ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(appCardBg(isDark))
                    .border(1.dp, appBorder(isDark), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = appTextPrimary(isDark),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = appTextPrimary(isDark))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── FORM CARD ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(appCardBg(isDark))
                .border(1.dp, appBorder(isDark), RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column {
                content()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── GENERATE BUTTON ──
        val defaultAppLogo = remember {
            try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
        }

        var styleConfig by remember(qrType) {
            mutableStateOf(
                if (qrType.uppercase() == "UPI") {
                    QRStyleConfig(
                        moduleShape = QRModuleShape.ROUNDED,
                        eyeShape = QREyeShape.ROUNDED,
                        logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
                        frameStyle = QRFrameStyle.PAYMENT_BADGE,
                        frameText = "SCAN & PAY",
                        logoBitmap = defaultAppLogo,
                        logoTag = "app_logo"
                    )
                } else {
                    QRStyleConfig(
                        moduleShape = QRModuleShape.ROUNDED,
                        eyeShape = QREyeShape.ROUNDED,
                        logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
                        frameStyle = QRFrameStyle.CARD_BORDER,
                        frameText = "SCAN ME",
                        logoBitmap = defaultAppLogo,
                        logoTag = "app_logo"
                    )
                }
            )
        }
        var customStyledBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var liveRenderJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isValid || isGenerating) appCtaGradient(isDark)
                    else Brush.horizontalGradient(listOf(appCardBg(isDark), appCardBg(isDark)))
                )
                .then(
                    if (isValid || isGenerating) Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(16.dp))
                    else Modifier.border(1.dp, appBorder(isDark), RoundedCornerShape(16.dp))
                )
                .clickable(enabled = isValid && !isGenerating) {
                    val activity = context as? Activity
                    AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                        scope.launch {
                            isGenerating = true
                            val qrContent = getContent()
                            withContext(Dispatchers.Default) {
                                val bmp = QRStylingEngine.renderStyledQR(qrContent, styleConfig, 600)
                                withContext(Dispatchers.Main) {
                                    customStyledBitmap = bmp
                                }
                                onGenerate(styleConfig.logoBitmap)
                            }
                            isGenerating = false
                            if (qrContent.isNotEmpty() && qrContent != lastSavedContent) {
                                historyViewModel.saveGenerate(qrContent, qrType)
                                lastSavedContent = qrContent
                            }
                            kotlinx.coroutines.delay(100)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF20140A),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.QrCode,
                        null,
                        tint = if (isValid) Color(0xFF20140A) else appTextTertiary(isDark),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isGenerating) "Generating..." else "Generate QR Code",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isValid || isGenerating) Color(0xFF20140A) else appTextTertiary(isDark)
                )
            }
        }

        // ── BANNER AD (Bottom of Form) ──
        Spacer(modifier = Modifier.height(14.dp))
        BannerAdView(modifier = Modifier.padding(horizontal = 18.dp))

        // ── GENERATED QR DISPLAY ──
        val displayBitmap = customStyledBitmap ?: generatedBitmap
        displayBitmap?.let { bitmap ->
            Spacer(modifier = Modifier.height(20.dp))

            // QR Card with Dynamic styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(appCardBg(isDark))
                    .border(1.dp, appBorder(isDark), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // QR Label
                    Text(
                        "Your Styled QR Code",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appTextSecondary(isDark),
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // QR Image container
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(styleConfig.bgColor))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated Styled QR Code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── SHARE / DOWNLOAD BUTTONS ──
                    val downloadScope = rememberCoroutineScope()
                    var isDownloading by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(appElevatedBg(isDark))
                                .border(1.dp, appBorder(isDark), RoundedCornerShape(14.dp))
                                .clickable {
                                    val activity = context as? Activity
                                    AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                        downloadScope.launch(Dispatchers.Default) {
                                            val fullBmp = QRStylingEngine.renderStyledQR(getContent(), styleConfig, 1024)
                                            withContext(Dispatchers.Main) {
                                                shareBitmap(context, fullBmp, "qr_code.png")
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, null, tint = appTextPrimary(isDark), modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = appTextPrimary(isDark))
                            }
                        }

                        // Download
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(appCtaGradient(isDark))
                                .clickable(enabled = !isDownloading) {
                                    val activity = context as? Activity
                                    AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                        downloadScope.launch {
                                            isDownloading = true
                                            withContext(Dispatchers.Default) {
                                                val fullBmp = QRStylingEngine.renderStyledQR(getContent(), styleConfig, 1024)
                                                downloadBitmap(context, fullBmp, "qr_${System.currentTimeMillis()}.png")
                                            }
                                            isDownloading = false
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isDownloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF20140A), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Download, null, tint = Color(0xFF20140A), modifier = Modifier.size(17.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isDownloading) "Saving..." else "Download", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF20140A))
                            }
                        }
                    }
                }
            }

            // ── EXPANDABLE CUSTOMIZATION CONTROLS PANEL ──
            Spacer(modifier = Modifier.height(16.dp))

            var isCustomizeExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Ink800)
                    .border(1.dp, if (isCustomizeExpanded) AmberPrimary else BorderLine, RoundedCornerShape(18.dp))
            ) {
                Column {
                    // Tap Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCustomizeExpanded = !isCustomizeExpanded }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberDim2),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, null, tint = AmberSoft, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Customize QR Style",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                if (isCustomizeExpanded) "Tap to collapse customization panel" else "Tap to customize Colors, Shapes, Eyes & Logos",
                                fontSize = 11.5.sp,
                                color = TextTertiary
                            )
                        }
                        Icon(
                            if (isCustomizeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AmberSoft,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Expandable Customization Options
                    AnimatedVisibility(
                        visible = isCustomizeExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            HorizontalDivider(color = BorderLine, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            QRCustomizationSection(
                                qrType = qrType,
                                styleConfig = styleConfig,
                                onStyleChanged = { newConfig ->
                                    styleConfig = newConfig
                                    val raw = getContent()
                                    if (raw.isNotEmpty()) {
                                        liveRenderJob?.cancel()
                                        liveRenderJob = scope.launch(Dispatchers.Default) {
                                            val updatedBmp = QRStylingEngine.renderStyledQR(raw, newConfig, 600)
                                            withContext(Dispatchers.Main) {
                                                customStyledBitmap = updatedBmp
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// =====================================================
// HELPER FUNCTIONS
// =====================================================

private fun shareBitmap(context: android.content.Context, bitmap: Bitmap, filename: String) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, filename)
        FileOutputStream(file).use { fos -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) }

        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share QR Code"))
    } catch (e: Exception) { e.printStackTrace() }
}

private suspend fun downloadBitmap(context: android.content.Context, bitmap: Bitmap, filename: String) {
    withContext(Dispatchers.IO) {
        try {
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QR_HUB")
                }
            }
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val qrDir = File(dir, "QR_HUB"); qrDir.mkdirs()
                Uri.fromFile(File(qrDir, filename))
            }
            uri?.let { context.contentResolver.openOutputStream(it)?.use { os: OutputStream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, os) } }
            withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "✅ Saved to Downloads/QR_HUB", android.widget.Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "Failed to save", android.widget.Toast.LENGTH_SHORT).show() }
        }
    }
}
