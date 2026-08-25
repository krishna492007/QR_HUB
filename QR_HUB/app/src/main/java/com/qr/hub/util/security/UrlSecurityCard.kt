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
            shieldBg = Color(0x1A10B981)
            shieldBorder = Color(0xFF10B981)
            shieldTint = Color(0xFF34D399)
        }
        SecurityLevel.CAUTION -> {
            shieldBg = Color(0x1AFFB300)
            shieldBorder = Color(0xFFFFB300)
            shieldTint = Color(0xFFFFC107)
        }
        SecurityLevel.SUSPICIOUS -> {
            shieldBg = Color(0x1AFF3D00)
            shieldBorder = Color(0xFFFF3D00)
            shieldTint = Color(0xFFFF5252)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Ink800)
            .border(1.dp, shieldBorder.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
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
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AmberSoft, strokeWidth = 2.dp)
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
                            color = TextPrimary
                        )
                        Text(
                            text = currentAnalysis.safetyDescription,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Protocol Pill (HTTPS / HTTP)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentAnalysis.isHttps) Color(0x2610B981) else Color(0x26FF5252))
                        .border(1.dp, if (currentAnalysis.isHttps) Color(0xFF10B981) else Color(0xFFFF5252), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentAnalysis.isHttps) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (currentAnalysis.isHttps) Color(0xFF34D399) else Color(0xFFFF5252),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentAnalysis.protocol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentAnalysis.isHttps) Color(0xFF34D399) else Color(0xFFFF5252)
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
                        .background(Ink750)
                        .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.UnfoldMore, null, tint = AmberSoft, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unmasked Destination URL", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AmberSoft)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentAnalysis.unmaskedUrl,
                            fontSize = 12.sp,
                            color = TextPrimary,
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
                    color = AmberSoft
                )
                Icon(
                    imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AmberSoft,
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
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(point, fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    // Risk Points (Red / Amber Warnings)
                    currentAnalysis.riskPoints.forEach { point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(point, fontSize = 11.sp, color = Color(0xFFFF8A80))
                        }
                    }
                }
            }
        }
    }
}
