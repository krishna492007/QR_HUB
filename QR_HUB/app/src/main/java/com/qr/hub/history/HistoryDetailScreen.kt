package com.qr.hub.history

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.data.model.HistoryItem
import com.qr.hub.generate.*
import com.qr.hub.model.ScannedQR
import com.qr.hub.util.*
import com.qr.hub.util.ads.AdManager
import com.qr.hub.util.ads.BannerAdView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================
// REDESIGNED DETAIL SCREEN COLORS — Dynamic Ink & Ceramic
// ============================================
private data class DetailColors(
    val isDark: Boolean,
    val bg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val elevatedBg: Color,
    val accent: Color,
    val accentSoft: Color,
    val ctaGradient: Brush,
    val ctaTextColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

private val LocalDetailColors = staticCompositionLocalOf {
    DetailColors(
        isDark = true,
        bg = Ink950,
        cardBg = Ink800,
        cardBorder = BorderLine,
        elevatedBg = Ink750,
        accent = AmberPrimary,
        accentSoft = AmberSoft,
        ctaGradient = Brush.verticalGradient(listOf(AmberSoft, AmberPrimary)),
        ctaTextColor = Color(0xFF20140A),
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextTertiary
    )
}

private val DetailBg: Color @Composable get() = LocalDetailColors.current.bg
private val DetailCardBg: Color @Composable get() = LocalDetailColors.current.cardBg
private val DetailCardBorder: Color @Composable get() = LocalDetailColors.current.cardBorder
private val DetailElevatedBg: Color @Composable get() = LocalDetailColors.current.elevatedBg
private val DetailAccent: Color @Composable get() = LocalDetailColors.current.accent
private val DetailAccentSoft: Color @Composable get() = LocalDetailColors.current.accentSoft
private val DetailCtaGradient: Brush @Composable get() = LocalDetailColors.current.ctaGradient
private val DetailCtaTextColor: Color @Composable get() = LocalDetailColors.current.ctaTextColor
private val DetailTextPrimary: Color @Composable get() = LocalDetailColors.current.textPrimary
private val DetailTextSecondary: Color @Composable get() = LocalDetailColors.current.textSecondary
private val DetailTextMuted: Color @Composable get() = LocalDetailColors.current.textMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    item: HistoryItem,
    isDark: Boolean = true,
    onBackClick: () -> Unit = {}
) {
    val detailColors = remember(isDark) {
        DetailColors(
            isDark = isDark,
            bg = appBg(isDark),
            cardBg = appCardBg(isDark),
            cardBorder = appBorder(isDark),
            elevatedBg = appElevatedBg(isDark),
            accent = appGoldPrimary(isDark),
            accentSoft = appGoldSoft(isDark),
            ctaGradient = appCtaGradient(isDark),
            ctaTextColor = appCtaTextColor(isDark),
            textPrimary = appTextPrimary(isDark),
            textSecondary = appTextSecondary(isDark),
            textMuted = appTextTertiary(isDark)
        )
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parsed = remember(item.rawValue, item.type) { parseHistoryDetailItem(item) }

    CompositionLocalProvider(LocalDetailColors provides detailColors) {

    val defaultAppLogo = remember {
        try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
    }

    var styleConfig by remember(item.rawValue, item.type) {
        mutableStateOf(
            if (item.type.uppercase().contains("UPI") || item.rawValue.startsWith("upi://", ignoreCase = true)) {
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

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var isCustomizeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(item.rawValue, styleConfig) {
        withContext(Dispatchers.Default) {
            val styled = QRStylingEngine.renderStyledQR(item.rawValue, styleConfig, 1024)
            withContext(Dispatchers.Main) {
                qrBitmap = styled
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBg)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                    .background(DetailCardBg)
                    .border(1.dp, DetailCardBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = DetailTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text("QR Details", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DetailTextPrimary)
        }

        // ── SCROLLABLE CONTENT ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── QR DISPLAY CARD (EXACT MATCH WITH GENERATE SCREEN) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DetailCardBg)
                    .border(1.dp, DetailCardBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // QR Label
                    Text(
                        "Your Styled QR Code",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailTextSecondary,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    qrBitmap?.let { bmp ->
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(styleConfig.bgColor))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    } ?: Box(
                        modifier = Modifier.size(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DetailAccent, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── SHARE / DOWNLOAD ACTION BUTTONS ──
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
                                .background(DetailElevatedBg)
                                .border(1.dp, DetailCardBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    val activity = context as? Activity
                                    AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                        qrBitmap?.let { bmp ->
                                            shareQRBitmap(context, bmp)
                                        } ?: shareQR(context, item.rawValue)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, "Share", tint = DetailTextPrimary, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = DetailTextPrimary)
                            }
                        }

                        // Download
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DetailCtaGradient)
                                .clickable(enabled = !isDownloading && qrBitmap != null) {
                                    qrBitmap?.let { bmp ->
                                        val activity = context as? Activity
                                        AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                            scope.launch {
                                                isDownloading = true
                                                saveQRToGallery(context, bmp, "QR_${item.id}")
                                                isDownloading = false
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isDownloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DetailCtaTextColor, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Download, "Download", tint = DetailCtaTextColor, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isDownloading) "Saving..." else "Download",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DetailCtaTextColor
                                )
                            }
                        }
                    }
                }
            }

            // ── EXPANDABLE CUSTOMIZE QR STYLE SECTION ──
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DetailCardBg)
                    .border(1.dp, if (isCustomizeExpanded) DetailAccent else DetailCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column {
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
                                .background(appGoldDim2(isDark)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, null, tint = DetailAccentSoft, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Customize QR Style",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DetailTextPrimary
                            )
                            Text(
                                if (isCustomizeExpanded) "Tap to collapse styling panel" else "Tap to customize Colors, Shapes, Eyes & Logos",
                                fontSize = 11.5.sp,
                                color = DetailTextMuted
                            )
                        }
                        Icon(
                            if (isCustomizeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = DetailAccentSoft,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isCustomizeExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            HorizontalDivider(color = DetailCardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            QRCustomizationSection(
                                qrType = item.type,
                                styleConfig = styleConfig,
                                isDark = isDark,
                                onStyleChanged = { newConfig ->
                                    styleConfig = newConfig
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── DETAILS CARD ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DetailCardBg)
                    .border(1.dp, DetailCardBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = DetailAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Content Details",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DetailTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DetailItem(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        label = "Raw Data",
                        value = item.rawValue
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailItem(
                        icon = Icons.Default.AccessTime,
                        label = "Created",
                        value = formatDate(item.timestamp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailItem(
                        icon = Icons.Default.Category,
                        label = "QR Type",
                        value = item.type
                    )
                }
            }

            // ── URL SECURITY INSPECTOR CARD (IF URL) ──
            if (item.type.equals("URL", ignoreCase = true) || item.rawValue.startsWith("http://", ignoreCase = true) || item.rawValue.startsWith("https://", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(14.dp))
                com.qr.hub.util.security.UrlSecurityCard(url = item.rawValue)
            }

            // Bottom Spacing
            Spacer(modifier = Modifier.height(48.dp))
        }

        // ── BANNER AD (START.IO ZERO DELAY MONETIZATION) ──
        BannerAdView(modifier = Modifier.fillMaxWidth())
    }
}
}

/**
 * Share High-Resolution Styled QR Bitmap to external apps
 */
private fun shareQRBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "styled_qr_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        shareQR(context, "QR Code")
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = DetailTextSecondary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, color = DetailTextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 13.5.sp,
                color = DetailTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class HistoryBadge(val label: String, val icon: ImageVector)

private fun getHistoryBadge(item: ScannedQR): HistoryBadge = when (item) {
    is ScannedQR.QRURL -> HistoryBadge("Website Link", Icons.Default.Language)
    is ScannedQR.WiFi -> HistoryBadge("WiFi Network", Icons.Default.Wifi)
    is ScannedQR.UPI -> HistoryBadge("UPI Payment", Icons.Default.AccountBalance)
    is ScannedQR.Contact -> HistoryBadge("Contact Card", Icons.Default.Person)
    is ScannedQR.Phone -> HistoryBadge("Phone Number", Icons.Default.Phone)
    is ScannedQR.SMS -> HistoryBadge("SMS Message", Icons.Default.Sms)
    is ScannedQR.QREmail -> HistoryBadge("Email Address", Icons.Default.Email)
    is ScannedQR.Location -> HistoryBadge("Map Location", Icons.Default.Place)
    is ScannedQR.WhatsApp -> HistoryBadge("WhatsApp", Icons.AutoMirrored.Filled.Chat)
    is ScannedQR.Event -> HistoryBadge("Calendar Event", Icons.Default.Event)
    is ScannedQR.Text -> HistoryBadge("Plain Text", Icons.AutoMirrored.Filled.Subject)
    else -> HistoryBadge("QR Code", Icons.Default.QrCode)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun shareQR(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share QR Content"))
}

private fun saveQRToGallery(context: Context, bitmap: Bitmap, name: String) {
    try {
        val filename = "${name}_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var uri: Uri? = null

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRHub")
            }
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = uri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/QRHub"
            val file = File(imagesDir)
            if (!file.exists()) file.mkdirs()
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            android.widget.Toast.makeText(context, "Saved to Gallery!", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Failed to save: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun parseHistoryDetailItem(item: HistoryItem): ScannedQR {
    val raw = item.rawValue
    val lower = raw.lowercase()

    return when {
        lower.startsWith("http://") || lower.startsWith("https://") -> ScannedQR.QRURL(raw)
        lower.startsWith("wifi:") || lower.startsWith("WIFI:") -> ScannedQR.WiFi(
            ssid = wifiField(raw, "S"),
            password = wifiField(raw, "P"),
            encryption = wifiField(raw, "T").ifEmpty { "WPA" }
        )
        lower.startsWith("tel:") -> ScannedQR.Phone(raw.removePrefix("tel:").removePrefix("TEL:"))
        lower.startsWith("smsto:") || lower.startsWith("sms:") -> parseSms(raw)
        lower.startsWith("mailto:") -> ScannedQR.QREmail(
            address = raw.removePrefix("mailto:").removePrefix("MAILTO:").substringBefore("?"),
            subject = queryParam(raw.substringAfter("?", ""), "subject"),
            body = queryParam(raw.substringAfter("?", ""), "body")
        )
        lower.startsWith("upi://") -> ScannedQR.UPI(
            vpa = queryParam(raw.substringAfter("?", ""), "pa"),
            name = queryParam(raw.substringAfter("?", ""), "pn"),
            amount = queryParam(raw.substringAfter("?", ""), "am"),
            note = queryParam(raw.substringAfter("?", ""), "tn"),
            currency = queryParam(raw.substringAfter("?", ""), "cu").ifEmpty { "INR" },
            rawUri = raw
        )
        lower.contains("wa.me/") || lower.contains("chat.whatsapp.com/") -> parseWhatsApp(raw)
        lower.startsWith("geo:") -> parseGeo(raw)
        raw.contains("BEGIN:VCARD", ignoreCase = true) -> ScannedQR.Contact(
            vCard = raw,
            name = vCardField(raw, "FN").ifEmpty { vCardField(raw, "N").substringBefore(';') },
            phone = vCardField(raw, "TEL"),
            email = vCardField(raw, "EMAIL"),
            org = vCardField(raw, "ORG"),
            title = vCardField(raw, "TITLE")
        )
        raw.contains("BEGIN:VEVENT", ignoreCase = true) || raw.contains("BEGIN:VCALENDAR", ignoreCase = true) -> ScannedQR.Event(
            raw = raw,
            summary = icalField(raw, "SUMMARY"),
            location = icalField(raw, "LOCATION"),
            description = icalField(raw, "DESCRIPTION"),
            startDate = icalField(raw, "DTSTART"),
            endDate = icalField(raw, "DTEND")
        )
        else -> ScannedQR.Text(raw)
    }
}

private fun parseSms(raw: String): ScannedQR.SMS {
    val normalized = raw.removePrefix("smsto:").removePrefix("SMSTO:").removePrefix("sms:").removePrefix("SMS:")
    val number = when {
        normalized.contains(":") -> normalized.substringBefore(":")
        normalized.contains("?") -> normalized.substringBefore("?")
        else -> normalized
    }
    val message = when {
        normalized.contains("?") -> queryParam(normalized.substringAfter("?", ""), "body")
        normalized.contains(":") -> URLDecoder.decode(normalized.substringAfter(":"), "UTF-8")
        else -> ""
    }
    return ScannedQR.SMS(number = number, message = message)
}

private fun parseWhatsApp(raw: String): ScannedQR.WhatsApp {
    val lower = raw.lowercase()
    return when {
        lower.contains("chat.whatsapp.com/") -> {
            val groupId = raw.substringAfter("chat.whatsapp.com/").substringBefore("?").trim()
            ScannedQR.WhatsApp(number = "", groupId = groupId)
        }
        lower.contains("wa.me/qr/") -> ScannedQR.WhatsApp(number = "", qrLinkUrl = raw)
        lower.contains("wa.me/") -> {
            val path = raw.substringAfter("wa.me/").substringBefore("?")
            val message = queryParam(raw.substringAfter("?", ""), "text")
            ScannedQR.WhatsApp(number = path.filter(Char::isDigit), message = message)
        }
        else -> ScannedQR.WhatsApp(number = "")
    }
}

private fun parseGeo(raw: String): ScannedQR {
    val geoData = raw.removePrefix("geo:")
    val coordinates = geoData.substringBefore("?").split(",")
    return if (coordinates.size >= 2) {
        ScannedQR.Location(
            latitude = coordinates[0].toDoubleOrNull() ?: 0.0,
            longitude = coordinates[1].toDoubleOrNull() ?: 0.0,
            label = queryParam(geoData.substringAfter("?", ""), "q")
        )
    } else {
        ScannedQR.Text(raw)
    }
}

private fun queryParam(query: String, key: String): String {
    return query.split("&")
        .firstOrNull { it.substringBefore("=") == key }
        ?.substringAfter("=", "")
        ?.let { URLDecoder.decode(it, "UTF-8") }
        .orEmpty()
}

private fun wifiField(raw: String, key: String): String {
    return Regex("$key:([^;]*)").find(raw)?.groupValues?.getOrNull(1)?.trim().orEmpty()
}

private fun vCardField(raw: String, key: String): String {
    return Regex("(?im)^$key(?:;[^:]+)?:([^\\r\\n]+)").find(raw)?.groupValues?.getOrNull(1).orEmpty()
}

private fun icalField(raw: String, key: String): String {
    return Regex("(?im)^$key(?:;[^:]+)?:([^\\r\\n]+)").find(raw)?.groupValues?.getOrNull(1).orEmpty()
}
