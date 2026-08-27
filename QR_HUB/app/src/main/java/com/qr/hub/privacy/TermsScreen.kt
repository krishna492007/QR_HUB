package com.qr.hub.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.util.*

// ============================================
// TERMS & CONDITIONS SCREEN — In-App Luxury (Dark & Ceramic)
// ============================================

@Composable
fun TermsScreen(
    isDark: Boolean = true,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

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

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "Terms & Conditions",
                    color = appTextPrimary(isDark),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Header Card ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = appCardBg(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF2A1F0D) else Color(0xFFFAF0E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = appGoldPrimary(isDark),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "QR HUB TERMS OF SERVICE",
                                color = appGoldPrimary(isDark),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Last updated: August 2026",
                                color = appTextTertiary(isDark),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = appBorder(isDark), thickness = 0.6.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "Welcome to QR HUB! By installing, accessing, or using our mobile application, you agree to comply with and be bound by these Terms and Conditions. Please review them carefully.",
                        color = appTextSecondary(isDark),
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Section 1 ──
            TermsSectionCard(
                isDark = isDark,
                number = "01",
                title = "Acceptance of Terms",
                content = "By downloading, installing, or using QR HUB, you signify your agreement to these Terms and Conditions as well as our Privacy Policy. If you do not agree to these terms, please do not use the application."
            )

            // ── Section 2 ──
            TermsSectionCard(
                isDark = isDark,
                number = "02",
                title = "Permitted Use & Scanner Scope",
                content = "QR HUB is designed for personal, non-commercial, and commercial utility scanning, generating QR codes, and processing 1D retail barcodes. You agree not to use the app for illegal activities, transmitting malware, or creating fraudulent QR codes."
            )

            // ── Section 3 ──
            TermsSectionCard(
                isDark = isDark,
                number = "03",
                title = "UPI Payments & Third-Party Apps",
                content = "QR HUB facilitates quick UPI intent redirection to certified payment apps (such as Google Pay, PhonePe, Paytm, BHIM) installed on your device. QR HUB is not a payment gateway and does not process, hold, or guarantee financial transactions."
            )

            // ── Section 4 ──
            TermsSectionCard(
                isDark = isDark,
                number = "04",
                title = "Device Permissions & Privacy",
                content = "Camera permissions are utilized solely for on-device visual QR scanning. Storage permissions are used only when you choose to export history or save generated QR codes to your gallery. All scanning logic operates 100% on-device via Google ML Kit."
            )

            // ── Section 5 ──
            TermsSectionCard(
                isDark = isDark,
                number = "05",
                title = "Intellectual Property & Branding",
                content = "All interfaces, brand assets, animations, icons, and software designs created for QR HUB are the exclusive property of Krishna (KRISHNA / QR HUB). You may not reverse engineer, decompile, or copy the proprietary design patterns without explicit permission."
            )

            // ── Section 6 ──
            TermsSectionCard(
                isDark = isDark,
                number = "06",
                title = "Disclaimer & Limitation of Liability",
                content = "QR HUB is provided on an 'AS IS' and 'AS AVAILABLE' basis without warranties of any kind. The developer shall not be liable for any indirect, incidental, or consequential damages resulting from the use or inability to use the application."
            )

            // ── Contact Card ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = appCardBg(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Questions or Legal Inquiries?",
                        color = appGoldPrimary(isDark),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "For legal notices, rights inquiries, or support, reach out to:\nkrishnatechhub.contact@gmail.com",
                        color = appTextSecondary(isDark),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TermsSectionCard(
    isDark: Boolean,
    number: String,
    title: String,
    content: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = appCardBg(isDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = number,
                    color = appGoldPrimary(isDark),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = appTextPrimary(isDark),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = content,
                color = appTextSecondary(isDark),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
