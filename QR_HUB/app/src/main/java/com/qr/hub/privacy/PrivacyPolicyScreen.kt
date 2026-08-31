package com.qr.hub.privacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.util.*

// Developer Info
const val DEVELOPER_NAME = "KRISHNA"
val APP_VERSION: String get() = com.qr.hub.BuildConfig.VERSION_NAME
const val DEVELOPER_EMAIL = "krishnatechhub.contact@gmail.com"

@Composable
fun PrivacyPolicyScreen(
    isDark: Boolean = true,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg(isDark))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Spacer(modifier = Modifier.statusBarsPadding())

            PrivacyHeader(isDark = isDark, onBackClick = onBackClick)

            // Content - Scrollable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Developer Info Card
                DeveloperInfoCard(isDark = isDark)

                // Privacy Policy Content
                PrivacyPolicyContent(isDark = isDark)

                // Bottom spacer
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PrivacyHeader(
    isDark: Boolean,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(appCardBg(isDark))
                .border(1.dp, appBorder(isDark), RoundedCornerShape(12.dp))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = appTextPrimary(isDark),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title
        Text(
            text = "Privacy Policy",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = appTextPrimary(isDark)
        )
    }
}

@Composable
private fun DeveloperInfoCard(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(appCardBg(isDark))
            .border(1.dp, appBorder(isDark), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            // App Icon & Name Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qrhub_logo),
                    contentDescription = "QR HUB Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, appGoldDim2(isDark), RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "QR Hub",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appTextPrimary(isDark)
                    )
                    Text(
                        text = "Version $APP_VERSION",
                        fontSize = 12.5.sp,
                        color = appTextSecondary(isDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(appBorder(isDark))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Developer Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(appElevatedBg(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = appGoldSoft(isDark),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Developer",
                        fontSize = 11.5.sp,
                        color = appTextTertiary(isDark)
                    )
                    Text(
                        text = DEVELOPER_NAME,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appTextPrimary(isDark)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent(isDark: Boolean) {
    // Introduction Card
    PolicySectionCard(
        isDark = isDark,
        icon = Icons.Default.PrivacyTip,
        title = "Your Privacy Matters",
        content = "QR Hub respects your privacy. This app does not collect, store, or transmit any personal data to external servers. All data is stored locally on your device."
    )

    // Data Collection Section
    PolicySectionCard(
        isDark = isDark,
        icon = Icons.Default.Security,
        title = "Data We Store",
        content = "The app only stores:\n\n" +
                "• QR Code scan history (stored locally on your device)\n" +
                "• Generated QR codes (stored locally on your device)\n" +
                "• App preferences (theme settings)\n\n" +
                "All data remains on your device and is never uploaded to any server."
    )

    // Camera Permission Section
    PolicySectionCard(
        isDark = isDark,
        icon = Icons.Default.Security,
        title = "Camera Permission",
        content = "QR Hub requires camera access to scan QR codes. The camera is used solely for scanning purposes and:\n\n" +
                "• No images or videos are stored\n" +
                "• No camera data leaves your device\n" +
                "• Camera is only active when scanning"
    )

    // Storage Permission Section
    PolicySectionCard(
        isDark = isDark,
        icon = Icons.Default.Security,
        title = "Storage Permissions",
        content = "Storage access is used to:\n\n" +
                "• Save generated QR codes to your gallery\n" +
                "• Read QR codes from gallery images\n" +
                "• Export your scan history\n\n" +
                "Files are only accessed when you explicitly choose to do so."
    )

    // Third Party Section
    PolicySectionCard(
        isDark = isDark,
        icon = Icons.Default.Security,
        title = "Third-Party Services & Advertising",
        content = "QR Hub uses the following trusted third-party SDKs:\n\n" +
                "• Google ML Kit (for on-device high-speed scanning)\n" +
                "• Google AdMob & Start.io (for delivering policy-compliant advertisements)\n\n" +
                "These ad networks may collect anonymized advertising identifiers in accordance with their privacy policies to display relevant ads."
    )

    // Contact Section
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) AmberDim else Color(0xFFFAF0E2))
            .border(1.dp, appGoldDim2(isDark), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "Contact Us",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = appGoldPrimary(isDark)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "If you have any questions or feedback about this Privacy Policy, please reach out directly:",
                fontSize = 13.sp,
                color = appTextSecondary(isDark),
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Clickable Email Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(appCardBg(isDark))
                    .border(1.dp, appGoldDim2(isDark), RoundedCornerShape(12.dp))
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$DEVELOPER_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "QR HUB - Privacy Policy Inquiry")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF2A1F0D) else Color(0xFFFAE8CD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = appGoldPrimary(isDark),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = DEVELOPER_EMAIL,
                    color = appGoldPrimary(isDark),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = appTextTertiary(isDark),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "This privacy policy was last updated on August 2026.",
                fontSize = 11.5.sp,
                color = appTextTertiary(isDark),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun PolicySectionCard(
    isDark: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(appCardBg(isDark))
            .border(1.dp, appBorder(isDark), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isDark) AmberDim else Color(0xFFFAF0E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = appGoldPrimary(isDark),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appTextPrimary(isDark)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                fontSize = 13.5.sp,
                color = appTextSecondary(isDark),
                lineHeight = 21.sp
            )
        }
    }
}
