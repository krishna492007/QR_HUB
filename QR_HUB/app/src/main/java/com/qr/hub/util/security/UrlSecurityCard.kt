package com.qr.hub.util.security

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.util.*

@Composable
fun UrlSecurityCard(
    url: String,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    var analysis by remember(url) { mutableStateOf<UrlSecurityAnalysis?>(null) }
    var isChecking by remember(url) { mutableStateOf(true) }
    var isDetailsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(url) {
        isChecking = true
        analysis = UrlSecurityInspector.inspectUrl(url)
        isChecking = false
    }

    val currentAnalysis = analysis ?: return

    val shieldBg: Color
    val shieldBorder: Color
    val shieldTint: Color

    when (currentAnalysis.securityLevel) {
        SecurityLevel.SAFE -> {
            shieldBg = if (isDark) Color(0x1A10B981) else Color(0xFFDCFCE7)
            shieldBorder = Color(0xFF10B981)
            shieldTint = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
        }
        SecurityLevel.CAUTION -> {
            shieldBg = if (isDark) Color(0x1AFFB300) else Color(0xFFFEF3C7)
            shieldBorder = Color(0xFFFFB300)
            shieldTint = if (isDark) Color(0xFFFFC107) else Color(0xFFD97706)
        }
        SecurityLevel.SUSPICIOUS -> {
            shieldBg = if (isDark) Color(0x1AFF3D00) else Color(0xFFFEE2E2)
            shieldBorder = Color(0xFFFF3D00)
            shieldTint = if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
        }
    }

    val cardBg = if (isDark) Ink800 else CeramicSurface
    val cardBorder = if (isDark) shieldBorder.copy(alpha = 0.4f) else (if (currentAnalysis.securityLevel == SecurityLevel.SAFE) Color(0xFF86EFAC) else shieldBorder.copy(alpha = 0.4f))
    val accentSoft = appGoldSoft(isDark)
    val textPrimary = appTextPrimary(isDark)
    val textSecondary = appTextSecondary(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row (Shield + Title + Protocol Badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(shieldBg)
                            .border(1.dp, shieldBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accentSoft, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = when (currentAnalysis.securityLevel) {
                                    SecurityLevel.SAFE -> Icons.Default.GppGood
                                    SecurityLevel.CAUTION -> Icons.Default.Shield
                                    SecurityLevel.SUSPICIOUS -> Icons.Default.GppMaybe
                                },
                                contentDescription = "Security Shield",
                                tint = shieldTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = currentAnalysis.safetyTitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = currentAnalysis.safetyDescription,
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Protocol Pill (HTTPS / HTTP)
                val isHttps = currentAnalysis.isHttps
                val protocolPillBg = if (isDark) {
                    if (isHttps) Color(0x2610B981) else Color(0x26FF5252)
                } else {
                    if (isHttps) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                }
                val protocolPillBorder = if (isHttps) Color(0xFF10B981) else Color(0xFFFF5252)
                val protocolPillTint = if (isDark) {
                    if (isHttps) Color(0xFF34D399) else Color(0xFFFF5252)
                } else {
                    if (isHttps) Color(0xFF059669) else Color(0xFFDC2626)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(protocolPillBg)
                        .border(1.dp, protocolPillBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = protocolPillTint,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentAnalysis.protocol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = protocolPillTint
                        )
                    }
                }
            }

            // Shortened URL Unmasker Card (if shortened)
            if (currentAnalysis.isShortened && currentAnalysis.unmaskedUrl != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Ink750 else CeramicSurface2)
                        .border(1.dp, if (isDark) BorderLine else CeramicBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.UnfoldMore, null, tint = accentSoft, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unmasked Destination URL", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = accentSoft)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentAnalysis.unmaskedUrl,
                            fontSize = 12.sp,
                            color = textPrimary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expandable Security Checklist
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDetailsExpanded = !isDetailsExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDetailsExpanded) "Hide Security Checklist" else "View Trust Checklist & Risks",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentSoft
                )
                Icon(
                    imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = accentSoft,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isDetailsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Trust Points (Green Checks)
                    currentAnalysis.trustPoints.forEach { point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = if (isDark) Color(0xFF10B981) else Color(0xFF059669), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(point, fontSize = 11.sp, color = textSecondary)
                        }
                    }

                    // Risk Points (Red / Amber Warnings)
                    currentAnalysis.riskPoints.forEach { point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(point, fontSize = 11.sp, color = if (isDark) Color(0xFFFF8A80) else Color(0xFFDC2626))
                        }
                    }
                }
            }
        }
    }
}
