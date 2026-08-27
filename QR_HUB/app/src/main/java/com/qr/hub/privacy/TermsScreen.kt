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
// TERMS & CONDITIONS SCREEN — In-App Luxury
// ============================================

@Composable
fun TermsScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                    "Terms & Conditions",
                    color = TextPrimary,
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
                color = Ink850,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLine)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2A1F0D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = AmberSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "LEGAL AGREEMENT",
                            color = AmberSoft,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "Terms of Service",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Please read these terms carefully before using QR HUB. By using the app, you accept all terms outlined here.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Sections ──
            val sections = listOf(
                Pair("01. Acceptance of Terms", "By downloading or using QR HUB, you agree to comply with these terms. If you do not agree, please discontinue using the app immediately."),
                Pair("02. Offline-First Privacy", "QR HUB processes all QR codes and barcodes 100% locally on your device. No camera feeds or scanned data are uploaded to external cloud servers."),
                Pair("03. Permitted Usage", "You may freely create, scan, export, and share barcodes and QR codes for personal and commercial activities. Malicious usage (phishing or malware links) is strictly prohibited."),
                Pair("04. Third-Party Services & Ads", "QR HUB incorporates Google AdMob and Start.io SDKs to support ongoing free development. Ad networks adhere to Google Play Developer Policies."),
                Pair("05. Disclaimer & Accuracy", "QR HUB is provided on an 'AS IS' basis. While our scanning engine provides high-precision recognition, the developer is not responsible for external third-party content accessed via scanned URLs or UPI payment flows.")
            )

            sections.forEach { (title, desc) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Ink850,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLine)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            title,
                            color = AmberSoft,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            desc,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Footer ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BUILT BY KRISHNA",
                    color = AmberSoft,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
