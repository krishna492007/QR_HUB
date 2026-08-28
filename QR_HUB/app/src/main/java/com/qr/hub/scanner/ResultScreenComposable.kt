package com.qr.hub.scanner

import android.content.ContentValues
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import android.app.Activity
import com.qr.hub.generate.QRGenerator
import com.qr.hub.util.UpiPreferenceManager
import com.qr.hub.util.ads.AdManager
import com.qr.hub.util.ads.BannerAdView
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.model.ScannedQR
import com.qr.hub.util.*
import com.qr.hub.viewmodel.HistoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// ============================================
// REDESIGNED RESULT SCREEN COLORS — Dynamic Ink & Ceramic
// ============================================
private data class ResultColors(
    val isDark: Boolean,
    val bg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val elevatedBg: Color,
    val accent: Color,
    val accentPink: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

private val LocalResultColors = staticCompositionLocalOf {
    ResultColors(
        isDark = true,
        bg = Ink950,
        cardBg = Ink800,
        cardBorder = BorderLine,
        elevatedBg = Ink750,
        accent = AmberPrimary,
        accentPink = AmberSoft,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextTertiary
    )
}

private val ResultIsDark: Boolean @Composable get() = LocalResultColors.current.isDark
private val ResultElevatedBg: Color @Composable get() = LocalResultColors.current.elevatedBg

private val ResultBg: Color @Composable get() = LocalResultColors.current.bg
private val ResultCardBg: Color @Composable get() = LocalResultColors.current.cardBg
private val ResultCardBorder: Color @Composable get() = LocalResultColors.current.cardBorder
private val ResultAccent: Color @Composable get() = LocalResultColors.current.accent
private val ResultAccentPink: Color @Composable get() = LocalResultColors.current.accentPink
private val ResultTextPrimary: Color @Composable get() = LocalResultColors.current.textPrimary
private val ResultTextSecondary: Color @Composable get() = LocalResultColors.current.textSecondary
private val ResultTextMuted: Color @Composable get() = LocalResultColors.current.textMuted

// Type-specific colors
private val TypeUrlColor = Color(0xFF4E9EFF)
private val TypePhoneColor = Color(0xFF4CAF50)
private val TypeSmsColor = Color(0xFF66BB6A)
private val TypeEmailColor = Color(0xFF42A5F5)
private val TypeWifiColor = Color(0xFFAB47BC)
private val TypeWhatsAppColor = Color(0xFF25D366)
private val TypeLocationColor = Color(0xFFEF5350)
private val TypeUpiColor = AmberPrimary
private val TypeContactColor = Color(0xFF5C6BC0)
private val TypeTextColor = AmberSoft

@Composable
fun ResultScreen(
    result: ScannedQR.RawResult,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val resultColors = remember(isDark) {
        ResultColors(
            isDark = isDark,
            bg = appBg(isDark),
            cardBg = appCardBg(isDark),
            cardBorder = appBorder(isDark),
            elevatedBg = appElevatedBg(isDark),
            accent = appGoldPrimary(isDark),
            accentPink = appGoldSoft(isDark),
            textPrimary = appTextPrimary(isDark),
            textSecondary = appTextSecondary(isDark),
            textMuted = appTextTertiary(isDark)
        )
    }

    val context = LocalContext.current
    val parsed = remember(result.rawValue) { com.qr.hub.util.detectType(result.rawValue) }
    val historyViewModel: HistoryViewModel = viewModel()

    CompositionLocalProvider(LocalResultColors provides resultColors) {

    // Auto-save to history and auto-save QR to gallery for instant gallery scanning
    LaunchedEffect(result.rawValue) {
        historyViewModel.saveScan(result.rawValue, parsed)
        if (parsed is ScannedQR.UPI) {
            QrGallerySaver.saveOnce(context, result.rawValue)
        }
    }

    val badge = getBadge(parsed)
    val typeColor = getTypeAccentColor(parsed)

    var showMenu by remember { mutableStateOf(false) }
    var showDefaultAppDialog by remember { mutableStateOf(false) }
    var defaultPkg by remember { mutableStateOf(UpiPreferenceManager.getDefaultPackage(context)) }
    var defaultName by remember { mutableStateOf(UpiPreferenceManager.getDefaultName(context)) }
    var isQuickPay by remember { mutableStateOf(UpiPreferenceManager.isQuickPayEnabled(context)) }
    val installedUpiApps = remember { getInstalledUpiApps(context) }

    // Dialog for setting default UPI app
    if (showDefaultAppDialog) {
        SetDefaultUpiAppDialog(
            installedApps = installedUpiApps,
            currentDefaultPkg = defaultPkg,
            isDark = isDark,
            onDismiss = { showDefaultAppDialog = false },
            onSetDefault = { pkg, name ->
                defaultPkg = pkg
                defaultName = name
                UpiPreferenceManager.setDefaultApp(context, pkg, name)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ResultBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ============================================
            // HEADER WITH 3-DOT MENU
            // ============================================
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ResultCardBg)
                            .border(1.dp, ResultCardBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = ResultTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        "Scan Result",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ResultTextPrimary
                    )

                    // 3-dot More Menu
                    Box {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ResultCardBg)
                                .border(1.dp, ResultCardBorder, RoundedCornerShape(12.dp))
                                .clickable { showMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = ResultTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = if (ResultIsDark) Ink850 else CeramicSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (ResultIsDark) BorderLineStrong else CeramicBorder),
                            shadowElevation = 12.dp,
                            modifier = Modifier.width(250.dp)
                        ) {
                            // Default UPI App
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = androidx.compose.material3.ripple(color = AmberDim2)
                                    ) {
                                        showMenu = false
                                        showDefaultAppDialog = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Ink750),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmberSoft,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        "Default UPI app",
                                        color = ResultTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Text(
                                        text = if (!defaultName.isNullOrEmpty()) defaultName!! else "None (Always Ask)",
                                        color = ResultTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderLine, thickness = 0.8.dp)

                            // Quick auto-pay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = androidx.compose.material3.ripple(color = AmberDim2)
                                    ) {
                                        val next = !isQuickPay
                                        isQuickPay = next
                                        UpiPreferenceManager.setQuickPayEnabled(context, next)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Ink750),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = AmberSoft,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        "Quick auto-pay",
                                        color = ResultTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isQuickPay) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(CyanAccent)
                                            )
                                            Text(
                                                "Active on scan",
                                                color = CyanAccent,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                        } else {
                                            Text(
                                                "Disabled",
                                                color = ResultTextMuted,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ============================================
            // TYPE BADGE — Premium hero section
            // ============================================
            item {
                TypeHeroBadge(
                    badge = badge,
                    typeColor = typeColor,
                    parsed = parsed
                )
            }

            // ============================================
            // DETAIL CARD — Type-specific content
            // ============================================
            item {
                Spacer(Modifier.height(16.dp))
                ResultDetailCard(
                    parsed = parsed,
                    typeColor = typeColor
                )
            }

            // ============================================
            // RAW DATA — Collapsible
            // ============================================
            item {
                Spacer(Modifier.height(12.dp))
                RawDataCard(rawValue = result.rawValue)
            }

            // ============================================
            // QUICK ACTIONS — Copy & Share
            // ============================================
            item {
                Spacer(Modifier.height(16.dp))
                QuickActionRow(
                    parsed = parsed,
                    context = context,
                    typeColor = typeColor
                )
            }

            // ============================================
            // TYPE-SPECIFIC ACTIONS
            // ============================================
            item {
                Spacer(Modifier.height(8.dp))
                TypeSpecificActions(
                    parsed = parsed,
                    context = context,
                    typeColor = typeColor
                )
            }

            // ============================================
            // PRIMARY ACTION BUTTON
            // ============================================
            item {
                Spacer(Modifier.height(16.dp))
                PrimaryActionButton(
                    parsed = parsed,
                    typeColor = typeColor,
                    context = context,
                    onUpiPrimaryClick = {
                        if (parsed is ScannedQR.UPI) {
                            val currentDefaultPkg = UpiPreferenceManager.getDefaultPackage(context)
                            val currentDefaultName = UpiPreferenceManager.getDefaultName(context)
                            launchUpiScanFlow(context, parsed, currentDefaultPkg, currentDefaultName)
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            // ============================================
            // BANNER AD
            // ============================================
            item {
                BannerAdView()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
}

// ============================================
// TYPE HERO BADGE — Redesigned Ink & Amber
// ============================================
@Composable
private fun TypeHeroBadge(
    badge: BadgeInfo,
    typeColor: Color,
    parsed: ScannedQR
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ResultCardBg)
                .border(1.dp, ResultCardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (ResultIsDark) AmberDim else Color(0xFFFAF0E1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    badge.icon,
                    contentDescription = null,
                    tint = if (ResultIsDark) AmberSoft else CeramicGold,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(13.dp))

            Column {
                Text(
                    badge.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ResultTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    getTypeDescription(parsed),
                    fontSize = 12.5.sp,
                    color = ResultTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================
// RESULT DETAIL CARD — Per-type sections
// ============================================
@Composable
private fun ResultDetailCard(
    parsed: ScannedQR,
    typeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ResultCardBg)
            .border(1.dp, ResultCardBorder, RoundedCornerShape(16.dp))
    ) {
        when (parsed) {
            is ScannedQR.Text -> {
                DetailField(Icons.Default.TextFields, "Text", parsed.content, typeColor)
            }

            is ScannedQR.QRURL -> {
                DetailField(Icons.Default.Link, "URL", parsed.url, typeColor, isLink = true)
                Spacer(modifier = Modifier.height(10.dp))
                com.qr.hub.util.security.UrlSecurityCard(url = parsed.url, isDark = ResultIsDark)
            }

            is ScannedQR.UPI -> {
                if (parsed.name.isNotEmpty())
                    DetailField(Icons.Default.Person, "Name", parsed.name, typeColor)
                if (parsed.vpa.isNotEmpty())
                    DetailField(Icons.Default.AccountBalance, "UPI ID (VPA)", parsed.vpa, typeColor, highlight = true)
                if (parsed.amount.isNotEmpty())
                    DetailField(Icons.Default.AttachMoney, "Amount", "₹${parsed.amount}", typeColor, highlight = true)
                if (parsed.note.isNotEmpty())
                    DetailField(Icons.AutoMirrored.Filled.Notes, "Note", parsed.note, typeColor)
                DetailField(Icons.Default.Language, "Currency", parsed.currency, typeColor)
            }

            is ScannedQR.Phone -> {
                DetailField(Icons.Default.Phone, "Phone Number", parsed.number, typeColor, highlight = true)
            }

            is ScannedQR.Contact -> {
                if (parsed.name.isNotEmpty())
                    DetailField(Icons.Default.Person, "Name", parsed.name, typeColor, highlight = true)
                if (parsed.phone.isNotEmpty())
                    DetailField(Icons.Default.Phone, "Phone", parsed.phone, typeColor)
                if (parsed.email.isNotEmpty())
                    DetailField(Icons.Default.Email, "Email", parsed.email, typeColor)
                if (parsed.org.isNotEmpty())
                    DetailField(Icons.Default.Work, "Organization", parsed.org, typeColor)
                if (parsed.title.isNotEmpty())
                    DetailField(Icons.Default.Info, "Title", parsed.title, typeColor)
            }

            is ScannedQR.SMS -> {
                DetailField(Icons.Default.Phone, "Number", parsed.number, typeColor, highlight = true)
                if (parsed.message.isNotEmpty())
                    DetailField(Icons.AutoMirrored.Filled.Message, "Message", parsed.message, typeColor)
            }

            is ScannedQR.QREmail -> {
                DetailField(Icons.Default.Email, "Email", parsed.address, typeColor, highlight = true)
                if (parsed.subject.isNotEmpty())
                    DetailField(Icons.AutoMirrored.Filled.ShortText, "Subject", parsed.subject, typeColor)
                if (parsed.body.isNotEmpty())
                    DetailField(Icons.Default.Description, "Body", parsed.body, typeColor)
            }

            is ScannedQR.WiFi -> {
                DetailField(Icons.Default.Wifi, "Network (SSID)", parsed.ssid, typeColor, highlight = true)
                if (parsed.password.isNotEmpty())
                    DetailField(Icons.Default.VpnKey, "Password", parsed.password, typeColor, highlight = true)
                DetailField(Icons.Default.Lock, "Encryption", parsed.encryption, typeColor)
            }

            is ScannedQR.WhatsApp -> {
                if (parsed.qrLinkUrl != null) {
                    DetailField(Icons.Default.Link, "WhatsApp Link", "Tap 'Open WhatsApp' below", typeColor)
                } else if (parsed.groupId.isNotEmpty()) {
                    DetailField(Icons.Default.Group, "Group ID", parsed.groupId, typeColor, highlight = true)
                } else {
                    if (parsed.number.isNotEmpty())
                        DetailField(Icons.Default.Phone, "Phone", parsed.number, typeColor, highlight = true)
                }
                if (parsed.message.isNotEmpty())
                    DetailField(Icons.AutoMirrored.Filled.Message, "Message", parsed.message, typeColor)
            }

            is ScannedQR.PlusCode -> {
                DetailField(Icons.Default.Place, "Plus Code", parsed.code, typeColor, highlight = true)
                if (parsed.label.isNotEmpty())
                    DetailField(Icons.AutoMirrored.Filled.Label, "Label", parsed.label, typeColor)
            }

            is ScannedQR.GoogleMaps -> {
                DetailField(Icons.Default.Map, "Google Maps", parsed.url, typeColor, isLink = true)
            }

            is ScannedQR.Location -> {
                if (parsed.label.isNotEmpty())
                    DetailField(Icons.AutoMirrored.Filled.Label, "Label", parsed.label, typeColor)
                DetailField(Icons.Default.MyLocation, "Latitude", parsed.latitude.toString(), typeColor)
                DetailField(Icons.Default.Explore, "Longitude", parsed.longitude.toString(), typeColor)
                if (parsed.zoom.isNotEmpty())
                    DetailField(Icons.Default.Search, "Zoom Level", parsed.zoom, typeColor)
            }

            is ScannedQR.Event -> {
                if (parsed.summary.isNotEmpty())
                    DetailField(Icons.Default.Event, "Event Summary", parsed.summary, typeColor, highlight = true)
                if (parsed.location.isNotEmpty())
                    DetailField(Icons.Default.Place, "Location", parsed.location, typeColor)
                if (parsed.description.isNotEmpty())
                    DetailField(Icons.Default.Description, "Description", parsed.description, typeColor)
                if (parsed.startDate.isNotEmpty())
                    DetailField(Icons.Default.CalendarMonth, "Start Date", formatEventDate(parsed.startDate), typeColor)
                if (parsed.endDate.isNotEmpty())
                    DetailField(Icons.Default.CalendarMonth, "End Date", formatEventDate(parsed.endDate), typeColor)
            }

            is ScannedQR.Unknown -> {
                DetailField(Icons.Default.Code, "Raw Data", parsed.raw, typeColor)
            }
        }
    }
}

// ============================================
// DETAIL FIELD — Redesigned Row
// ============================================
@Composable
private fun DetailField(
    icon: ImageVector,
    label: String,
    value: String,
    typeColor: Color,
    highlight: Boolean = false,
    isLink: Boolean = false
) {
    val isDark = ResultIsDark
    val borderCol = ResultCardBorder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderCol,
                    start = Offset(54.dp.toPx(), size.height),
                    end = Offset(size.width - 16.dp.toPx(), size.height),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isDark) Ink750 else Color(0xFFF3EFE9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDark) AmberSoft else CeramicGold,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ResultTextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                value,
                fontSize = if (highlight) 15.5.sp else 14.5.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                color = if (isLink) CyanAccent else ResultTextPrimary,
                lineHeight = 21.sp
            )
        }
    }
}

// ============================================
// RAW DATA — Collapsible section
// ============================================
@Composable
private fun RawDataCard(rawValue: String) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = ResultIsDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ResultCardBg)
            .border(1.dp, ResultCardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                tint = ResultTextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Raw Data",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ResultTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = ResultTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Ink900 else CeramicSurface2)
                    .padding(12.dp)
            ) {
                Text(
                    rawValue,
                    fontSize = 12.sp,
                    color = ResultTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ============================================
// QUICK ACTION ROW — Copy + Share
// ============================================
@Composable
private fun QuickActionRow(
    parsed: ScannedQR,
    context: Context,
    typeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Copy Button
        QuickActionButton(
            icon = Icons.Outlined.ContentCopy,
            label = "Copy",
            modifier = Modifier.weight(1f)
        ) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("QR", parsed.toString()))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Share Button
        QuickActionButton(
            icon = Icons.Outlined.Share,
            label = "Share",
            modifier = Modifier.weight(1f)
        ) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, parsed.toString())
                }
                context.startActivity(Intent.createChooser(intent, "Share QR"))
            } catch (_: Exception) { }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ResultCardBg)
            .border(1.dp, ResultCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = ResultTextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ResultTextPrimary
            )
        }
    }
}

// ============================================
// TYPE-SPECIFIC ACTIONS — Extra buttons per type
// ============================================
@Composable
private fun TypeSpecificActions(
    parsed: ScannedQR,
    context: Context,
    typeColor: Color,
    onSelectUpiApp: ((UpiAppItem) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (parsed) {
            is ScannedQR.UPI -> {
                val installedApps = remember { getInstalledUpiApps(context) }
                val defaultName = remember { UpiPreferenceManager.getDefaultName(context) }
                val defaultPkg = remember { UpiPreferenceManager.getDefaultPackage(context) }

                val isDark = ResultIsDark
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ResultCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ResultCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Banner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(appTealDim(ResultIsDark)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = appTealAccent(ResultIsDark),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "100% SUCCESSFUL PAYMENT",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appTealAccent(ResultIsDark),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "QR saved to Gallery • Zero bank risk",
                                        fontSize = 11.sp,
                                        color = ResultTextSecondary
                                    )
                                }
                            }

                            // Default App Pill
                            if (!defaultName.isNullOrEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = appGoldDim(ResultIsDark),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, appGoldDim2(ResultIsDark))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("⭐", fontSize = 10.sp)
                                        Text(
                                            text = defaultName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = appGoldSoft(ResultIsDark)
                                        )
                                    }
                                }
                            }
                        }

                        // App List Row: 1-Tap Direct Launch
                        if (installedApps.isNotEmpty()) {
                            Text(
                                text = "TAP TO PAY DIRECTLY:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ResultTextMuted,
                                letterSpacing = 1.sp
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(installedApps) { app ->
                                    val isThisDefault = app.packageName == defaultPkg
                                    Card(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .clickable {
                                                launchUpiScanFlow(context, parsed, app.packageName, app.appName)
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isThisDefault) appGoldDim(ResultIsDark) else appElevatedBg(ResultIsDark)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isThisDefault) 1.5.dp else 1.dp,
                                            color = if (isThisDefault) appGoldPrimary(ResultIsDark) else appBorder(ResultIsDark)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(contentAlignment = Alignment.TopEnd) {
                                                if (app.icon != null) {
                                                    coil.compose.AsyncImage(
                                                        model = app.icon,
                                                        contentDescription = app.appName,
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .background(app.brandColor.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AccountBalance,
                                                            contentDescription = null,
                                                            tint = app.brandColor,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                if (isThisDefault) {
                                                    Text(
                                                        text = "⭐",
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = app.appName,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isThisDefault) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isThisDefault) appGoldSoft(ResultIsDark) else ResultTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (parsed.vpa.isNotEmpty()) {
                    ExtraActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy UPI ID",
                        subtitle = parsed.vpa,
                        color = AmberSoft
                    ) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", parsed.vpa))
                        Toast.makeText(context, "UPI ID copied: ${parsed.vpa}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is ScannedQR.WiFi -> {
                if (parsed.password.isNotEmpty()) {
                    ExtraActionButton(
                        icon = Icons.Default.VpnKey,
                        label = "Copy Password",
                        subtitle = parsed.password,
                        color = AmberSoft
                    ) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("WiFi Password", parsed.password))
                        Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is ScannedQR.Phone -> {
                ExtraActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Number",
                    subtitle = parsed.number,
                    color = AmberSoft
                ) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Phone", parsed.number))
                    Toast.makeText(context, "Number copied!", Toast.LENGTH_SHORT).show()
                }
                ExtraActionButton(
                    icon = Icons.AutoMirrored.Filled.Message,
                    label = "Send SMS",
                    subtitle = "Send text message to ${parsed.number}",
                    color = AmberSoft
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("smsto:${parsed.number}"))
                    context.startActivity(intent)
                }
            }

            is ScannedQR.QREmail -> {
                ExtraActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Email",
                    subtitle = parsed.address,
                    color = AmberSoft
                ) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Email", parsed.address))
                    Toast.makeText(context, "Email copied!", Toast.LENGTH_SHORT).show()
                }
            }

            is ScannedQR.Contact -> {
                if (parsed.phone.isNotEmpty()) {
                    ExtraActionButton(
                        icon = Icons.Default.Phone,
                        label = "Call ${parsed.name.ifEmpty { "Contact" }}",
                        subtitle = parsed.phone,
                        color = AmberSoft
                    ) {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parsed.phone}")))
                    }
                }
                if (parsed.email.isNotEmpty()) {
                    ExtraActionButton(
                        icon = Icons.Default.Email,
                        label = "Send Email",
                        subtitle = parsed.email,
                        color = AmberSoft
                    ) {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${parsed.email}")))
                    }
                }
            }

            is ScannedQR.SMS -> {
                if (parsed.number.isNotEmpty()) {
                    ExtraActionButton(
                        icon = Icons.Default.Phone,
                        label = "Call this number",
                        subtitle = parsed.number,
                        color = AmberSoft
                    ) {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parsed.number}")))
                    }
                }
            }

            else -> { /* No extra actions */ }
        }
    }
}

@Composable
private fun ExtraActionButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val isDark = ResultIsDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ResultCardBg)
            .border(1.dp, ResultCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDark) Ink750 else Color(0xFFF3EFE9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDark) AmberSoft else CeramicGold,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ResultTextPrimary
            )
            Text(
                subtitle,
                fontSize = 11.5.sp,
                color = ResultTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = ResultTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ============================================
// PRIMARY ACTION BUTTON — Amber CTA
// ============================================
@Composable
private fun PrimaryActionButton(
    parsed: ScannedQR,
    typeColor: Color,
    context: Context,
    onUpiPrimaryClick: (() -> Unit)? = null
) {
    val action = getPrimaryAction(parsed, onUpiPrimaryClick, context)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AmberSoft, AmberPrimary)
                )
            )
            .clickable { action.onClick(context) },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = Color(0xFF20140A),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                action.label,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF20140A)
            )
        }
    }
}

// ============================================
// HELPER: Get type accent color
// ============================================
private fun getTypeAccentColor(parsed: ScannedQR): Color = when (parsed) {
    is ScannedQR.Text -> TypeTextColor
    is ScannedQR.QRURL -> TypeUrlColor
    is ScannedQR.UPI -> TypeUpiColor
    is ScannedQR.Phone -> TypePhoneColor
    is ScannedQR.Contact -> TypeContactColor
    is ScannedQR.SMS -> TypeSmsColor
    is ScannedQR.QREmail -> TypeEmailColor
    is ScannedQR.WiFi -> TypeWifiColor
    is ScannedQR.WhatsApp -> TypeWhatsAppColor
    is ScannedQR.Location -> TypeLocationColor
    is ScannedQR.Event -> Color(0xFFF9A825) // Amber for Events
    is ScannedQR.PlusCode -> TypeLocationColor
    is ScannedQR.GoogleMaps -> TypeLocationColor
    is ScannedQR.Unknown -> AmberPrimary
}

// ============================================
// HELPER: Get type description
// ============================================
private fun getTypeDescription(parsed: ScannedQR): String = when (parsed) {
    is ScannedQR.Text -> "Plain text content detected"
    is ScannedQR.QRURL -> parsed.url.removePrefix("https://").removePrefix("http://").take(50)
    is ScannedQR.UPI -> if (parsed.vpa.isNotEmpty()) "UPI: ${parsed.vpa}" else "UPI Payment detected"
    is ScannedQR.Phone -> "Phone: ${parsed.number}"
    is ScannedQR.Contact -> if (parsed.name.isNotEmpty()) "Contact: ${parsed.name}" else "Contact card detected"
    is ScannedQR.SMS -> "SMS to: ${parsed.number}"
    is ScannedQR.QREmail -> "Email: ${parsed.address}"
    is ScannedQR.WiFi -> "Network: ${parsed.ssid}"
    is ScannedQR.WhatsApp -> when {
        parsed.qrLinkUrl != null -> "WhatsApp QR link"
        parsed.groupId.isNotEmpty() -> "WhatsApp group invite"
        else -> "WhatsApp: ${parsed.number}"
    }
    is ScannedQR.Location -> "Lat: ${parsed.latitude}, Lng: ${parsed.longitude}"
    is ScannedQR.Event -> "Calendar event detected"
    is ScannedQR.PlusCode -> "Plus Code: ${parsed.code}"
    is ScannedQR.GoogleMaps -> "Google Maps location"
    is ScannedQR.Unknown -> "Unknown QR format"
}

// ============================================
// PRIMARY ACTION CONFIG — Same logic as before
// ============================================
private data class PrimaryAction(
    val icon: ImageVector,
    val label: String,
    val onClick: (Context) -> Unit
)

private fun getPrimaryAction(
    parsed: ScannedQR,
    onUpiPrimaryClick: (() -> Unit)? = null,
    context: Context? = null
): PrimaryAction {
    return when (parsed) {
        is ScannedQR.QRURL -> PrimaryAction(Icons.AutoMirrored.Filled.OpenInNew, "Open URL") { ctx ->
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(parsed.url)))
        }
        is ScannedQR.GoogleMaps -> PrimaryAction(Icons.Default.LocationOn, "Open in Maps") { ctx ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsed.url))
                intent.`package` = "com.google.android.apps.maps"
                ctx.startActivity(intent)
            } catch (_: Exception) {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(parsed.url)))
            }
        }
        is ScannedQR.UPI -> {
            val defaultName = if (context != null) UpiPreferenceManager.getDefaultName(context) else null
            val btnLabel = if (!defaultName.isNullOrEmpty()) "Pay with $defaultName" else "Select UPI App & Pay"
            PrimaryAction(Icons.Default.Payment, btnLabel) { ctx ->
                if (onUpiPrimaryClick != null) {
                    onUpiPrimaryClick()
                } else {
                    val defaultPkg = UpiPreferenceManager.getDefaultPackage(ctx)
                    if (defaultPkg != null) {
                        launchUpiScanFlow(ctx, parsed, defaultPkg, defaultName)
                    } else {
                        val apps = getInstalledUpiApps(ctx)
                        if (apps.isNotEmpty()) {
                            launchUpiScanFlow(ctx, parsed, apps.first().packageName, apps.first().appName)
                        } else {
                            launchUpiScanFlow(ctx, parsed, null, null)
                        }
                    }
                }
            }
        }
        is ScannedQR.Phone -> PrimaryAction(Icons.Default.Phone, "Call Now") { ctx ->
            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parsed.number}")))
        }
        is ScannedQR.QREmail -> PrimaryAction(Icons.Default.Email, "Send Email") { ctx ->
            val uri = "mailto:${parsed.address}?subject=${parsed.subject}&body=${parsed.body}"
            ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(uri)))
        }
        is ScannedQR.SMS -> PrimaryAction(Icons.AutoMirrored.Filled.Message, "Send SMS") { ctx ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("smsto:${parsed.number}"))
            intent.putExtra("sms_body", parsed.message)
            ctx.startActivity(intent)
        }
        is ScannedQR.WhatsApp -> {
            if (parsed.qrLinkUrl != null) {
                PrimaryAction(Icons.AutoMirrored.Filled.Chat, "Open WhatsApp") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(parsed.qrLinkUrl)))
                }
            } else if (parsed.groupId.isNotEmpty()) {
                PrimaryAction(Icons.Default.Group, "Join Group") { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/${parsed.groupId}"))
                        intent.`package` = "com.whatsapp"
                        ctx.startActivity(intent)
                    } catch (_: Exception) {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/${parsed.groupId}")))
                    }
                }
            } else {
                PrimaryAction(Icons.AutoMirrored.Filled.Chat, "Open WhatsApp") { ctx ->
                    try {
                        val uri = Uri.Builder()
                            .scheme("https")
                            .authority("wa.me")
                            .appendPath(parsed.number)
                            .apply { if (parsed.message.isNotEmpty()) appendQueryParameter("text", parsed.message) }
                            .build()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.`package` = "com.whatsapp"
                        ctx.startActivity(intent)
                    } catch (_: Exception) {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${parsed.number}")))
                    }
                }
            }
        }
        is ScannedQR.WiFi -> PrimaryAction(Icons.Default.Wifi, "Connect to WiFi") { ctx ->
            connectWifi(ctx, parsed.ssid, parsed.password, parsed.encryption, showToast = true)
        }
        is ScannedQR.Location -> PrimaryAction(Icons.Default.LocationOn, "Open in Maps") { ctx ->
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${parsed.latitude},${parsed.longitude}")))
        }
        is ScannedQR.PlusCode -> PrimaryAction(Icons.Default.Search, "Open in Google Maps") { ctx ->
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(parsed.code)}")))
        }
        is ScannedQR.Contact -> PrimaryAction(Icons.Default.PersonAdd, "Add Contact") { ctx ->
            val intent = Intent(Intent.ACTION_INSERT, android.provider.ContactsContract.Contacts.CONTENT_URI).apply {
                putExtra(android.provider.ContactsContract.Intents.Insert.NAME, parsed.name)
                if (parsed.phone.isNotEmpty()) putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, parsed.phone)
                if (parsed.email.isNotEmpty()) putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, parsed.email)
            }
            ctx.startActivity(intent)
        }
        is ScannedQR.Event -> PrimaryAction(Icons.Default.CalendarMonth, "Add to Calendar") { ctx ->
            try {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, parsed.summary)
                    putExtra(CalendarContract.Events.EVENT_LOCATION, parsed.location)
                    putExtra(CalendarContract.Events.DESCRIPTION, parsed.description)
                    
                    // Parse start date - multiple format support
                    val startMillis = parseICalDate(parsed.startDate)
                    val endMillis = if (parsed.endDate.isNotEmpty()) {
                        parseICalDate(parsed.endDate)
                    } else {
                        startMillis + 3600000 // Default 1 hour duration
                    }
                    
                    putExtra(CalendarContract.Events.DTSTART, startMillis)
                    putExtra(CalendarContract.Events.DTEND, endMillis)
                }
                ctx.startActivity(intent)
            } catch (e: Exception) {
                Log.e("Calendar", "Error adding event: ${e.message}")
                Toast.makeText(ctx, "Could not open calendar", Toast.LENGTH_SHORT).show()
            }
        }
        is ScannedQR.Text -> PrimaryAction(Icons.Default.ContentCopy, "Copy to Clipboard") { ctx ->
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("QR", parsed.content))
            Toast.makeText(ctx, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        is ScannedQR.Unknown -> PrimaryAction(Icons.Default.ContentCopy, "Copy to Clipboard") { ctx ->
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("QR", parsed.raw))
            Toast.makeText(ctx, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}

// ============================================
// BADGE INFO — Used by hero section
// ============================================
data class BadgeInfo(val icon: ImageVector, val label: String, val tint: Color)

fun getBadge(parsed: ScannedQR): BadgeInfo {
    return when (parsed) {
        is ScannedQR.Text -> BadgeInfo(HtmlIcons.TextQr, "Text", TypeTextColor)
        is ScannedQR.QRURL -> BadgeInfo(HtmlIcons.UrlQr, "URL", TypeUrlColor)
        is ScannedQR.UPI -> BadgeInfo(HtmlIcons.UpiQr, "UPI Payment", TypeUpiColor)
        is ScannedQR.Phone -> BadgeInfo(HtmlIcons.PhoneQr, "Phone", TypePhoneColor)
        is ScannedQR.Contact -> BadgeInfo(HtmlIcons.ContactQr, "Contact", TypeContactColor)
        is ScannedQR.SMS -> BadgeInfo(HtmlIcons.SmsQr, "SMS", TypeSmsColor)
        is ScannedQR.QREmail -> BadgeInfo(HtmlIcons.EmailQr, "Email", TypeEmailColor)
        is ScannedQR.WiFi -> BadgeInfo(HtmlIcons.WiFiQr, "WiFi", TypeWifiColor)
        is ScannedQR.WhatsApp -> BadgeInfo(HtmlIcons.WhatsAppQr, "WhatsApp", TypeWhatsAppColor)
        is ScannedQR.Location, is ScannedQR.PlusCode, is ScannedQR.GoogleMaps -> BadgeInfo(HtmlIcons.LocationQr, "Location", TypeLocationColor)
        is ScannedQR.Event -> BadgeInfo(HtmlIcons.EventQr, "Event", Color(0xFFF9A825))
        is ScannedQR.Unknown -> BadgeInfo(HtmlIcons.TextQr, "Unknown", AmberPrimary)
    }
}

// ============================================
// WIFI CONNECT — Same logic as before
// ============================================
private fun connectWifi(
    context: Context,
    ssid: String,
    password: String,
    encryption: String,
    showToast: Boolean
) {
    com.qr.hub.util.WifiAutoConnector.connectToWifi(context, ssid, password, encryption)
}

// =====================================================
// HELPER: Format Event Date (YYYYMMDDTHHMMSS → human-readable)
// =====================================================

private fun formatEventDate(raw: String): String {
    return try {
        val clean = raw.trim()
        if (clean.length < 15) return clean
        val year = clean.substring(0, 4)
        val month = clean.substring(4, 6).toInt()
        val day = clean.substring(6, 8)
        val hour = clean.substring(9, 11)
        val minute = clean.substring(11, 13)
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthName = if (month in 1..12) monthNames[month - 1] else "?"
        
        // Convert 24h to 12h format
        var h = hour.toInt()
        val amPm = if (h >= 12) "PM" else "AM"
        h = if (h % 12 == 0) 12 else h % 12
        
        "$day $monthName $year, $h:$minute $amPm"
    } catch (_: Exception) { raw }
}

// ============================================
// HELPER: Parse iCalendar date to milliseconds
// ============================================
private fun parseICalDate(dateStr: String): Long {
    if (dateStr.isEmpty()) return System.currentTimeMillis()
    
    val formats = listOf(
        SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault()),
        SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyyMMdd'T'HHmm", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HHmmss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HHmmss'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    )
    
    for (format in formats) {
        try {
            val date = format.parse(dateStr)
            if (date != null && date.time > 0) {
                return date.time
            }
        } catch (_: Exception) { }
    }
    return System.currentTimeMillis()
}

// ============================================
// UPI HELPER UTILITIES & CUSTOM DIALOGS
// ============================================

data class UpiAppItem(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable? = null,
    val brandColor: Color = Color(0xFF6C63FF),
    val gradient: List<Color> = listOf(Color(0xFF6C63FF), Color(0xFFE94EFF))
)

object QrGallerySaver {
    private var lastSavedRaw: String? = null
    private var lastSavedTimestamp: Long = 0L

    @Synchronized
    fun saveOnce(context: Context, rawValue: String): Uri? {
        val now = System.currentTimeMillis()
        if (rawValue == lastSavedRaw && (now - lastSavedTimestamp) < 3500) {
            // Already saved within the last 3.5 seconds, prevent duplicate
            return null
        }
        lastSavedRaw = rawValue
        lastSavedTimestamp = now
        return saveQrBitmapToGallery(context, rawValue)
    }
}

fun saveQrBitmapToGallery(context: Context, rawValue: String): Uri? {
    return try {
        val qrContent = rawValue.ifBlank { "upi://pay" }
        val bitmap = QRGenerator.generateStandardQRBitmap(qrContent, 1024)
        val filename = "QR_HUB_UPI_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QR_HUB")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        uri
    } catch (e: Exception) {
        Log.e("SAVE_QR", "Failed to save QR to gallery: ${e.message}", e)
        null
    }
}

fun launchUpiScanFlow(
    context: Context,
    parsed: ScannedQR.UPI,
    targetPackage: String? = null,
    appName: String? = null
) {
    try {
        val raw = parsed.rawUri.ifBlank { "upi://pay?pa=${parsed.vpa}&pn=${parsed.name}&cu=INR" }
        QrGallerySaver.saveOnce(context, raw)

        if (parsed.vpa.isNotBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", parsed.vpa))
        }

        val pm = context.packageManager
        val effectivePackage = targetPackage ?: getInstalledUpiApps(context).firstOrNull()?.packageName

        if (!effectivePackage.isNullOrEmpty()) {
            val launchIntent = pm.getLaunchIntentForPackage(effectivePackage)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                return
            }
        }

        // Fallback to chooser
        val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay")), "Open UPI App")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

    } catch (e: Exception) {
        Log.e("UPI_SCAN_FLOW", "Error launching scan flow: ${e.message}", e)
        launchUpiPaymentIntent(context, parsed, targetPackage)
    }
}

fun getUpiPaymentUri(parsed: ScannedQR.UPI): Uri {
    val raw = parsed.rawUri.trim()
    return if (raw.isNotBlank() && (raw.startsWith("upi://", ignoreCase = true) || raw.startsWith("upi:"))) {
        Uri.parse(raw)
    } else {
        val vpa = parsed.vpa.trim()
        val builder = StringBuilder("upi://pay?pa=").append(URLEncoder.encode(vpa, "UTF-8"))
        if (parsed.name.isNotBlank()) {
            builder.append("&pn=").append(URLEncoder.encode(parsed.name.trim(), "UTF-8"))
        }
        if (parsed.amount.isNotBlank()) {
            val cleanAmount = parsed.amount.replace("[^0-9.]".toRegex(), "")
            if (cleanAmount.isNotBlank()) {
                builder.append("&am=").append(cleanAmount)
            }
        }
        if (parsed.note.isNotBlank()) {
            builder.append("&tn=").append(URLEncoder.encode(parsed.note.trim(), "UTF-8"))
        }
        builder.append("&cu=").append(parsed.currency.ifBlank { "INR" })
        Uri.parse(builder.toString())
    }
}

fun getInstalledUpiApps(context: Context): List<UpiAppItem> {
    val pm = context.packageManager
    val upiUri = Uri.parse("upi://pay")
    val intent = Intent(Intent.ACTION_VIEW, upiUri)
    val list = mutableListOf<UpiAppItem>()
    val seenPackages = mutableSetOf<String>()

    data class KnownApp(
        val pkg: String,
        val defaultName: String,
        val brandColor: Color,
        val gradient: List<Color>
    )

    val knownApps = listOf(
        KnownApp("com.phonepe.app", "PhonePe", Color(0xFF5F259F), listOf(Color(0xFF673AB7), Color(0xFF512DA8))),
        KnownApp("com.google.android.apps.nbu.paisa.user", "GPay", Color(0xFF1A73E8), listOf(Color(0xFF1A73E8), Color(0xFF34A853))),
        KnownApp("net.one97.paytm", "Paytm", Color(0xFF00BAF2), listOf(Color(0xFF00BAF2), Color(0xFF002970))),
        KnownApp("in.org.npci.upiapp", "BHIM", Color(0xFF00897B), listOf(Color(0xFF00897B), Color(0xFFE65100))),
        KnownApp("in.cred", "CRED", Color(0xFF1F2937), listOf(Color(0xFF374151), Color(0xFF111827))),
        KnownApp("in.amazon.mShop.android.shopping", "Amazon Pay", Color(0xFFFF9900), listOf(Color(0xFFFF9900), Color(0xFF146EB4))),
        KnownApp("com.navi.finance", "Navi", Color(0xFF00C853), listOf(Color(0xFF00C853), Color(0xFF1B5E20))),
        KnownApp("com.mobikwik_new", "Mobikwik", Color(0xFF0084FF), listOf(Color(0xFF0084FF), Color(0xFF0050C8))),
        KnownApp("com.freecharge.android", "Freecharge", Color(0xFFE65100), listOf(Color(0xFFFF9800), Color(0xFFE65100))),
        KnownApp("com.tatadigital.tcp", "Tata Neu", Color(0xFF8E24AA), listOf(Color(0xFF8E24AA), Color(0xFF4A148C))),
        KnownApp("app.jupiter.money", "Jupiter", Color(0xFFFF7043), listOf(Color(0xFFFF7043), Color(0xFFD84315))),
        KnownApp("money.fi.banking", "Fi Money", Color(0xFF00E676), listOf(Color(0xFF00E676), Color(0xFF00B0FF))),
        KnownApp("indwin.c3.shareany", "Slice", Color(0xFF7C4DFF), listOf(Color(0xFF7C4DFF), Color(0xFF536DFE))),
        KnownApp("com.fampay.in", "FamPay", Color(0xFFFFAB00), listOf(Color(0xFFFFAB00), Color(0xFFFF6D00))),
        KnownApp("money.super.app", "Super.money", Color(0xFF00E5FF), listOf(Color(0xFF00E5FF), Color(0xFF2979FF))),
        KnownApp("com.phonepe.app.business", "PhonePe Biz", Color(0xFF5F259F), listOf(Color(0xFF5F259F), Color(0xFF311B92))),
        KnownApp("com.paytm.business", "Paytm Biz", Color(0xFF00BAF2), listOf(Color(0xFF00BAF2), Color(0xFF001540))),
        KnownApp("com.msf.kbank.mobile", "Kotak 811", Color(0xFFD32F2F), listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))),
        KnownApp("com.enstage.wibmo.hdfc", "PayZapp", Color(0xFF004C8F), listOf(Color(0xFF004C8F), Color(0xFF002244))),
        KnownApp("com.csam.icici.bank.imobile", "iMobile", Color(0xFFE65100), listOf(Color(0xFFE65100), Color(0xFFBF360C))),
        KnownApp("com.sbi.lotusintouch", "YONO SBI", Color(0xFF00897B), listOf(Color(0xFF00897B), Color(0xFF004D40))),
        KnownApp("com.axis.mobile", "Axis Mobile", Color(0xFF880E4F), listOf(Color(0xFF880E4F), Color(0xFF4A148C)))
    )

    for (known in knownApps) {
        try {
            val appInfo = pm.getApplicationInfo(known.pkg, 0)
            val name = try {
                val label = pm.getApplicationLabel(appInfo).toString()
                if (label.contains("Google", ignoreCase = true)) "GPay" else known.defaultName
            } catch (_: Exception) { known.defaultName }
            val icon = try { pm.getApplicationIcon(appInfo) } catch (_: Exception) { null }
            list.add(UpiAppItem(known.pkg, name, icon, known.brandColor, known.gradient))
            seenPackages.add(known.pkg)
        } catch (_: Exception) {}
    }

    try {
        val resolveList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        for (info in resolveList) {
            val pkg = info.activityInfo.packageName
            if (!seenPackages.contains(pkg)) {
                val name = info.loadLabel(pm).toString()
                val icon = info.loadIcon(pm)
                list.add(UpiAppItem(pkg, name, icon, Color(0xFF6C63FF), listOf(Color(0xFF6C63FF), Color(0xFFE94EFF))))
                seenPackages.add(pkg)
            }
        }
    } catch (_: Exception) {}

    return list
}

fun launchUpiPaymentIntent(context: Context, parsed: ScannedQR.UPI, targetPackage: String? = null) {
    try {
        val upiUri = getUpiPaymentUri(parsed)

        // Always copy VPA to clipboard as a helpful fallback
        if (parsed.vpa.isNotBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", parsed.vpa))
        }

        if (!targetPackage.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                `package` = targetPackage
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Pay via UPI App")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        Log.e("UPI_PAY", "Error launching UPI payment: ${e.message}", e)
        try {
            val upiUri = getUpiPaymentUri(parsed)
            val fallback = Intent(Intent.ACTION_VIEW, upiUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(fallback, "Pay via UPI App")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}

// ============================================
// UPI DIALOGS & APP-SPECIFIC STEP GUIDANCE
// ============================================

fun getAppScanGuide(packageName: String, appName: String): List<String> {
    return when {
        packageName.contains("phonepe", ignoreCase = true) -> listOf(
            "PhonePe home screen par top-right me 📷 Scanner icon tap karein",
            "Scanner screen ke bottom-left me 🖼️ Gallery icon select karein",
            "Sabse pehli (auto-saved) QR photo choose karein aur PIN dalke payment karein"
        )
        packageName.contains("paisa", ignoreCase = true) || appName.contains("Google", ignoreCase = true) || appName.contains("GPay", ignoreCase = true) -> listOf(
            "Google Pay home screen par 📷 'Scan any QR code' tap karein",
            "Top-right corner me 🖼️ 'Upload from gallery' option select karein",
            "Gallery se sabse pehli QR photo choose karein aur payment complete karein"
        )
        packageName.contains("paytm", ignoreCase = true) -> listOf(
            "Paytm open karke bottom me 📷 'Scan & Pay' tap karein",
            "Scanner ke andar 🖼️ 'Scan from Gallery' button select karein",
            "Pehla QR select karein aur securely PIN dalke pay karein"
        )
        packageName.contains("upiapp", ignoreCase = true) || appName.contains("BHIM", ignoreCase = true) -> listOf(
            "BHIM App open karke 📷 'Scan' icon tap karein",
            "Scanner screen par 🖼️ 'Gallery' icon par click karein",
            "Auto-saved QR select karein aur payment complete karein"
        )
        packageName.contains("cred", ignoreCase = true) -> listOf(
            "CRED app open karke 📷 'Scan & Pay' tap karein",
            "Gallery icon select karke pehli QR image choose karein",
            "Payment amount check karke PIN enter karein"
        )
        packageName.contains("amazon", ignoreCase = true) -> listOf(
            "Amazon App me Amazon Pay section me 📷 'Scan QR' tap karein",
            "🖼️ 'Scan from Gallery' option select karein",
            "Latest QR select karke payment complete karein"
        )
        else -> listOf(
            "$appName open karke 📷 QR Scanner open karein",
            "Scanner me 🖼️ 'Gallery / Upload Photo' option select karein",
            "Sabse pehli auto-saved QR photo choose karein aur PIN dalke payment karein"
        )
    }
}

@Composable
fun SetDefaultUpiAppDialog(
    installedApps: List<UpiAppItem>,
    currentDefaultPkg: String?,
    isDark: Boolean = true,
    onDismiss: () -> Unit,
    onSetDefault: (packageName: String?, appName: String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", color = appTextSecondary(isDark), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = if (isDark) Ink850 else CeramicSurface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Column {
                Text(
                    text = "Default UPI App",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appTextPrimary(isDark)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Pick an app for instant 1-tap payments",
                    fontSize = 12.5.sp,
                    color = appTextSecondary(isDark)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: None (Always ask / show all)
                item {
                    val isSelected = currentDefaultPkg == null
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetDefault(null, null)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) appGoldDim(isDark) else appElevatedBg(isDark)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) appGoldPrimary(isDark) else appBorder(isDark)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Ink750 else CeramicCanvas),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = appTextPrimary(isDark),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No Default App",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) appGoldPrimary(isDark) else appTextPrimary(isDark)
                                )
                                Text(
                                    text = "Always show installed apps list",
                                    fontSize = 11.5.sp,
                                    color = appTextSecondary(isDark)
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onSetDefault(null, null)
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = appGoldPrimary(isDark),
                                    unselectedColor = appTextTertiary(isDark)
                                )
                            )
                        }
                    }
                }

                // Installed Apps options
                items(installedApps) { app ->
                    val isSelected = currentDefaultPkg == app.packageName
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetDefault(app.packageName, app.appName)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) appGoldDim(isDark) else appElevatedBg(isDark)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) appGoldPrimary(isDark) else appBorder(isDark)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (app.icon != null) {
                                coil.compose.AsyncImage(
                                    model = app.icon,
                                    contentDescription = app.appName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(app.brandColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = app.brandColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) appGoldPrimary(isDark) else appTextPrimary(isDark)
                                )
                                Text(
                                    text = "1-tap direct payment",
                                    fontSize = 11.5.sp,
                                    color = appTextSecondary(isDark)
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onSetDefault(app.packageName, app.appName)
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = appGoldPrimary(isDark),
                                    unselectedColor = appTextTertiary(isDark)
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}
