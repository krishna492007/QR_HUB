package com.qr.hub.privacy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.scanner.SetDefaultUpiAppDialog
import com.qr.hub.scanner.getInstalledUpiApps
import com.qr.hub.util.*

// ============================================
// SETTINGS & ABOUT SCREEN — Ink & Ceramic Luxury
// ============================================

@Composable
fun AboutLegalScreen(
    isDark: Boolean = true,
    currentThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // UPI state
    var showDefaultAppDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var defaultPkg by remember { mutableStateOf(UpiPreferenceManager.getDefaultPackage(context)) }
    var defaultName by remember { mutableStateOf(UpiPreferenceManager.getDefaultName(context)) }
    var isQuickPay by remember { mutableStateOf(UpiPreferenceManager.isQuickPayEnabled(context)) }
    val installedUpiApps = remember { getInstalledUpiApps(context) }

    if (showDefaultAppDialog) {
        SetDefaultUpiAppDialog(
            installedApps = installedUpiApps,
            currentDefaultPkg = defaultPkg,
            onDismiss = { showDefaultAppDialog = false },
            onSetDefault = { pkg, name ->
                defaultPkg = pkg
                defaultName = name
                UpiPreferenceManager.setDefaultApp(context, pkg, name)
            }
        )
    }

    if (showThemeDialog) {
        ThemeChooserDialog(
            currentMode = currentThemeMode,
            isDark = isDark,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                onThemeModeChange(mode)
                showThemeDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg(isDark))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(appCardBg(isDark))
                        .border(1.dp, appBorder(isDark), CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = appTextPrimary(isDark),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "Settings & Info",
                        color = appTextPrimary(isDark),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        "Preferences, Legal & App Information",
                        color = appTextTertiary(isDark),
                        fontSize = 12.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 1: Appearance & Theme ──
            Text(
                "APPEARANCE",
                color = appGoldPrimary(isDark),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = appCardBg(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
            ) {
                Column {
                    // Theme Mode Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = appGoldDim2(isDark))
                            ) {
                                showThemeDialog = true
                            }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF2A1F0D) else Color(0xFFFAF0E2))
                                .border(0.8.dp, appGoldPrimary(isDark).copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = appGoldPrimary(isDark),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "App Theme",
                                color = appTextPrimary(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                currentThemeMode.label,
                                color = appGoldPrimary(isDark),
                                fontSize = 12.5.sp
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = appTextTertiary(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 2: Scanner & UPI Preferences ──
            Text(
                "SCANNER & PAYMENT PREFERENCES",
                color = appGoldPrimary(isDark),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = appCardBg(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
            ) {
                Column {
                    // Default UPI App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = appGoldDim2(isDark))
                            ) {
                                showDefaultAppDialog = true
                            }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF0D2A2A) else Color(0xFFE2F6F3))
                                .border(0.8.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (isDark) CyanAccent else Color(0xFF1E8E7E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Default UPI App",
                                color = appTextPrimary(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (!defaultName.isNullOrEmpty()) defaultName!! else "None (Always Ask)",
                                color = if (!defaultName.isNullOrEmpty()) (if (isDark) CyanAccent else Color(0xFF1E8E7E)) else appTextTertiary(isDark),
                                fontSize = 12.5.sp
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = appTextTertiary(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(color = appBorder(isDark), thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    // Quick Auto-Pay Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = appGoldDim2(isDark))
                            ) {
                                val next = !isQuickPay
                                isQuickPay = next
                                UpiPreferenceManager.setQuickPayEnabled(context, next)
                            }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF2A1F0D) else Color(0xFFFAF0E2))
                                .border(0.8.dp, appGoldPrimary(isDark).copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = appGoldPrimary(isDark),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Quick Auto-Pay",
                                color = appTextPrimary(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isQuickPay) "Active on scan (Direct pay)" else "Disabled (Show result first)",
                                color = if (isQuickPay) appGoldPrimary(isDark) else appTextTertiary(isDark),
                                fontSize = 12.5.sp
                            )
                        }
                        Switch(
                            checked = isQuickPay,
                            onCheckedChange = { next ->
                                isQuickPay = next
                                UpiPreferenceManager.setQuickPayEnabled(context, next)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDark) Color(0xFF20140A) else Color.White,
                                checkedTrackColor = appGoldPrimary(isDark),
                                uncheckedThumbColor = appTextTertiary(isDark),
                                uncheckedTrackColor = if (isDark) Ink750 else CeramicElevated
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 3: Legal & Privacy ──
            Text(
                "LEGAL & PRIVACY",
                color = appGoldPrimary(isDark),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = appCardBg(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
            ) {
                Column {
                    // Language
                    SettingsItemRow(
                        isDark = isDark,
                        icon = Icons.Default.Language,
                        iconTint = if (isDark) CyanAccent else Color(0xFF1E8E7E),
                        iconBg = if (isDark) Color(0xFF0D2A2A) else Color(0xFFE2F6F3),
                        title = "Language",
                        subtitle = "English",
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (_: Exception) {}
                        }
                    )

                    HorizontalDivider(color = appBorder(isDark), thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    // Privacy Policy
                    SettingsItemRow(
                        isDark = isDark,
                        icon = Icons.Default.PrivacyTip,
                        iconTint = Color(0xFF43A047),
                        iconBg = if (isDark) Color(0xFF0D2A14) else Color(0xFFE8F5E9),
                        title = "Privacy Policy",
                        subtitle = "How your data is protected offline",
                        onClick = onPrivacyPolicyClick
                    )

                    HorizontalDivider(color = appBorder(isDark), thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    // Terms & Conditions
                    SettingsItemRow(
                        isDark = isDark,
                        icon = Icons.Default.Description,
                        iconTint = appGoldPrimary(isDark),
                        iconBg = if (isDark) Color(0xFF2A1F0D) else Color(0xFFFAF0E2),
                        title = "Terms & Conditions",
                        subtitle = "Rules for using QR Hub",
                        onClick = onTermsClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 4: Support & Community ──
            Text(
                "SUPPORT & COMMUNITY",
                color = appGoldPrimary(isDark),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = appCardBg(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
            ) {
                Column {
                    // Contact Us
                    SettingsItemRow(
                        isDark = isDark,
                        icon = Icons.AutoMirrored.Filled.HelpCenter,
                        iconTint = Color(0xFF7E57C2),
                        iconBg = if (isDark) Color(0xFF1A0D2A) else Color(0xFFF3E5F5),
                        title = "Contact Us",
                        subtitle = "Get in touch: krishnatechhub.contact@gmail.com",
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:krishnatechhub.contact@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "QR HUB - Support & Feedback")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (_: Exception) {}
                        }
                    )

                    HorizontalDivider(color = appBorder(isDark), thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    // Rate Us
                    SettingsItemRow(
                        isDark = isDark,
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFF59E0B),
                        iconBg = if (isDark) Color(0xFF2A250D) else Color(0xFFFEF3C7),
                        title = "Rate Us",
                        subtitle = "Leave a review on Google Play Store",
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.qr.hub")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            } catch (_: Exception) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.qr.hub")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                } catch (_: Exception) {}
                            }
                        }
                    )

                    HorizontalDivider(color = appBorder(isDark), thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    // Share App
                    SettingsItemRow(
                        isDark = isDark,
                        icon = Icons.Default.Share,
                        iconTint = if (isDark) CyanAccent else Color(0xFF1E8E7E),
                        iconBg = if (isDark) Color(0xFF0D2A2A) else Color(0xFFE2F6F3),
                        title = "Share App",
                        subtitle = "Tell a friend about QR Hub",
                        onClick = {
                            try {
                                val shareText = "🚀 Check out QR HUB — ultra-fast 120 FPS QR Scanner, Custom Designer QR Maker & 1D Barcode Studio!\n\nDownload: https://play.google.com/store/apps/details?id=com.qr.hub"
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }, "Share QR HUB via"))
                            } catch (_: Exception) {}
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Premium Brand Footer ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qrhub_logo),
                    contentDescription = "QR HUB Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, appGoldDim2(isDark), RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "QR HUB",
                    color = appGoldPrimary(isDark),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "VERSION ${APP_VERSION}",
                    color = appTextTertiary(isDark),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gold gradient divider
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(1.5.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    appGoldDim2(isDark),
                                    appGoldSoft(isDark),
                                    appGoldDim2(isDark),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(
                            color = appTextTertiary(isDark),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 2.sp
                        )) { append("Built by ") }
                        withStyle(SpanStyle(
                            color = appGoldPrimary(isDark),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp
                        )) { append("Krishna") }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    isDark: Boolean,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = appGoldDim2(isDark))
            ) {
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg)
                .border(0.8.dp, iconTint.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = appTextPrimary(isDark),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
            Text(
                subtitle,
                color = appTextTertiary(isDark),
                fontSize = 12.sp,
                letterSpacing = 0.1.sp
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = appTextTertiary(isDark),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ThemeChooserDialog(
    currentMode: AppThemeMode,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelect: (AppThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appDialogBg(isDark),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Choose Theme",
                color = appTextPrimary(isDark),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.values().forEach { mode ->
                    val isSelected = mode == currentMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) appGoldDim(isDark) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) appGoldPrimary(isDark).copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = appGoldPrimary(isDark),
                                unselectedColor = appTextTertiary(isDark)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            mode.label,
                            color = if (isSelected) appGoldPrimary(isDark) else appTextPrimary(isDark),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = appGoldPrimary(isDark), fontWeight = FontWeight.Bold)
            }
        }
    )
}
