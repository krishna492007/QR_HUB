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
import androidx.compose.ui.res.painterResource
import com.qr.hub.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.util.*

// ============================================
// ABOUT & LEGAL SCREEN — Luxury Obsidian & Gold
// ============================================

private data class AboutMenuItem(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val onClick: (Context, (() -> Unit)?) -> Unit
)

@Composable
fun AboutLegalScreen(
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val menuItems = listOf(
        AboutMenuItem(
            icon = Icons.Default.Language,
            iconTint = CyanAccent,
            iconBg = Color(0xFF0D2A2A),
            title = "Language",
            subtitle = "English",
            onClick = { ctx, _ ->
                try {
                    ctx.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {}
            }
        ),
        AboutMenuItem(
            icon = Icons.Default.PrivacyTip,
            iconTint = Color(0xFF66BB6A),
            iconBg = Color(0xFF0D2A14),
            title = "Privacy Policy",
            subtitle = "How your data is handled",
            onClick = { _, _ -> onPrivacyPolicyClick() }
        ),
        AboutMenuItem(
            icon = Icons.Default.Description,
            iconTint = AmberSoft,
            iconBg = Color(0xFF2A1F0D),
            title = "Terms & Conditions",
            subtitle = "Rules for using QR Hub",
            onClick = { _, _ -> onTermsClick() }
        ),
        AboutMenuItem(
            icon = Icons.AutoMirrored.Filled.HelpCenter,
            iconTint = Color(0xFF7E57C2),
            iconBg = Color(0xFF1A0D2A),
            title = "Contact Us",
            subtitle = "Get in touch with the team",
            onClick = { ctx, _ ->
                try {
                    ctx.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:krishnatechhub.contact@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "QR HUB - Feedback / Support")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {}
            }
        ),
        AboutMenuItem(
            icon = Icons.Default.Star,
            iconTint = Color(0xFFFFD54F),
            iconBg = Color(0xFF2A250D),
            title = "Rate Us",
            subtitle = "Leave a review on Play Store",
            onClick = { ctx, _ ->
                try {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.qr.hub")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (_: Exception) {
                    try {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.qr.hub")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } catch (_: Exception) {}
                }
            }
        ),
        AboutMenuItem(
            icon = Icons.Default.Share,
            iconTint = CyanAccent,
            iconBg = Color(0xFF0D2A2A),
            title = "Share App",
            subtitle = "Tell a friend about QR Hub",
            onClick = { ctx, _ ->
                try {
                    val shareText = "🚀 Check out QR HUB — ultra-fast 120 FPS QR Scanner, Custom Designer QR Maker & 1D Barcode Studio!\n\nDownload: https://play.google.com/store/apps/details?id=com.qr.hub"
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }, "Share QR HUB via"))
                } catch (_: Exception) {}
            }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink950)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Ink800)
                        .border(1.dp, BorderLine, CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "About & Legal",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Invisible spacer for centering title
                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Menu Card ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Ink850,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLine),
                shadowElevation = 8.dp
            ) {
                Column {
                    menuItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = AmberDim2)
                                ) {
                                    item.onClick(context, if (item.title == "Privacy Policy") onPrivacyPolicyClick else null)
                                }
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(item.iconBg)
                                    .border(0.8.dp, item.iconTint.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Title + Subtitle
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.title,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.3.sp
                                )
                                Text(
                                    item.subtitle,
                                    color = TextTertiary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.2.sp
                                )
                            }

                            // Chevron arrow
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Divider between items (not after last)
                        if (index < menuItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                color = BorderLine,
                                thickness = 0.6.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Premium Brand Footer ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon
                Image(
                    painter = painterResource(id = R.drawable.qrhub_logo),
                    contentDescription = "QR HUB Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, AmberDim2, RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "QR HUB",
                    color = AmberSoft,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "VERSION ${APP_VERSION}",
                    color = TextTertiary,
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
                                    AmberDim2,
                                    AmberSoft,
                                    AmberDim2,
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
                            color = TextTertiary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 2.sp
                        )) { append("Built by ") }
                        withStyle(SpanStyle(
                            color = AmberSoft,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp
                        )) { append("Krishna") }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
