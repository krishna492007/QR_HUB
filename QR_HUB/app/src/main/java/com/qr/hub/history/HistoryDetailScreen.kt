package com.qr.hub.history

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
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
import com.qr.hub.data.model.HistoryItem
import com.qr.hub.model.ScannedQR
import com.qr.hub.generate.QRGenerator
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URLDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.qr.hub.util.*

// Ink & Amber Design Tokens
private val DetailBg = Ink950
private val DetailCardBg = Ink800
private val DetailCardBorder = BorderLine
private val DetailAccent = AmberPrimary
private val DetailAccentSoft = AmberSoft
private val DetailCtaGradient = Brush.verticalGradient(listOf(AmberSoft, AmberPrimary))
private val DetailTextPrimary = TextPrimary
private val DetailTextSecondary = TextSecondary
private val DetailTextMuted = TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    item: HistoryItem,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val parsed = remember(item.rawValue, item.type) { parseHistoryDetailItem(item) }
    val qrBitmap by produceState<Bitmap?>(initialValue = null, item.rawValue) {
        value = withContext(Dispatchers.Default) {
            generateQRBitmap(context, item.rawValue, 1024)
        }
    }

    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

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

            // ── QR DISPLAY CARD ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DetailCardBg)
                    .border(1.dp, DetailCardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // QR Label
                    Text(
                        "Your QR Code",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailTextSecondary,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    qrBitmap?.let { bmp ->
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(12.dp),
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
                        modifier = Modifier.size(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DetailAccent, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Type Badge
                    val badge = getHistoryBadge(parsed)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberDim)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(badge.icon, null, tint = AmberSoft, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(badge.label, color = AmberSoft, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── DETAILS CARD ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DetailCardBg)
                    .border(1.dp, DetailCardBorder, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text("Details", color = DetailTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(16.dp))

                    val hideRawContent = when (parsed) {
                        is ScannedQR.UPI, is ScannedQR.WhatsApp, is ScannedQR.Phone,
                        is ScannedQR.SMS, is ScannedQR.WiFi, is ScannedQR.Event,
                        is ScannedQR.QREmail, is ScannedQR.Contact -> true
                        else -> false
                    }
                    if (!hideRawContent) {
                        DetailRow(Icons.Default.ContentCopy, "Content", item.rawValue)
                    }

                    when (parsed) {
                        is ScannedQR.UPI -> {
                            if (parsed.name.isNotEmpty()) DetailRow(Icons.Default.Person, "Name", parsed.name)
                            if (parsed.vpa.isNotEmpty()) DetailRow(Icons.Default.AccountBalance, "VPA", parsed.vpa)
                            if (parsed.amount.isNotEmpty()) DetailRow(Icons.Default.CurrencyRupee, "Amount", "₹${parsed.amount}")
                            if (parsed.note.isNotEmpty()) DetailRow(Icons.AutoMirrored.Filled.Notes, "Note", parsed.note)
                        }
                        is ScannedQR.Phone -> DetailRow(Icons.Default.Phone, "Phone", parsed.number)
                        is ScannedQR.SMS -> {
                            DetailRow(Icons.Default.Phone, "Number", parsed.number)
                            if (parsed.message.isNotEmpty()) DetailRow(Icons.Default.Sms, "Message", parsed.message)
                        }
                        is ScannedQR.QREmail -> {
                            DetailRow(Icons.Default.Email, "Email", parsed.address)
                            if (parsed.subject.isNotEmpty()) DetailRow(Icons.AutoMirrored.Filled.Subject, "Subject", parsed.subject)
                            if (parsed.body.isNotEmpty()) DetailRow(Icons.AutoMirrored.Filled.Notes, "Body", parsed.body)
                        }
                        is ScannedQR.WiFi -> {
                            DetailRow(Icons.Default.Wifi, "Network", parsed.ssid)
                            if (parsed.password.isNotEmpty()) DetailRow(Icons.Default.Lock, "Password", parsed.password)
                            val displayEncryption = when (parsed.encryption.lowercase()) {
                                "None" -> "None"
                                else -> parsed.encryption.uppercase()
                            }
                            DetailRow(Icons.Default.Security, "Encryption", displayEncryption)
                        }
                        is ScannedQR.WhatsApp -> {
                            if (parsed.number.isNotEmpty()) DetailRow(Icons.Default.Phone, "Phone", parsed.number)
                            if (parsed.groupId.isNotEmpty()) DetailRow(Icons.Default.Group, "Group ID", parsed.groupId)
                            if (parsed.message.isNotEmpty()) DetailRow(Icons.AutoMirrored.Filled.Chat, "Message", parsed.message)
                        }
                        is ScannedQR.Location -> {
                            DetailRow(Icons.Default.LocationOn, "Latitude", parsed.latitude.toString())
                            DetailRow(Icons.Default.LocationOn, "Longitude", parsed.longitude.toString())
                            if (parsed.label.isNotEmpty()) DetailRow(Icons.AutoMirrored.Filled.Label, "Label", parsed.label)
                        }
                        is ScannedQR.Contact -> {
                            if (parsed.name.isNotEmpty()) DetailRow(Icons.Default.Person, "Name", parsed.name)
                            if (parsed.phone.isNotEmpty()) DetailRow(Icons.Default.Phone, "Phone", parsed.phone)
                            if (parsed.email.isNotEmpty()) DetailRow(Icons.Default.Email, "Email", parsed.email)
                        }
                        is ScannedQR.Event -> {
                            if (parsed.summary.isNotEmpty()) DetailRow(Icons.Default.Event, "Title", parsed.summary)
                            if (parsed.location.isNotEmpty()) DetailRow(Icons.Default.LocationOn, "Location", parsed.location)
                            if (parsed.description.isNotEmpty()) DetailRow(Icons.AutoMirrored.Filled.Notes, "Description", parsed.description)
                            if (parsed.startDate.isNotEmpty()) DetailRow(Icons.Default.Schedule, "Start", formatEventDate(parsed.startDate))
                            if (parsed.endDate.isNotEmpty()) DetailRow(Icons.Default.Schedule, "End", formatEventDate(parsed.endDate))
                        }
                        else -> {}
                    }

                    // Divider
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderLine)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Timestamp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = DetailTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saved: ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))}",
                            color = DetailTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── BOTTOM ACTION BAR ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink900)
                .border(
                    width = 1.dp,
                    color = BorderLine,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ink750)
                        .border(1.dp, BorderLine, RoundedCornerShape(14.dp))
                        .clickable { shareQR(context, item.rawValue) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Share, "Share", tint = DetailTextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = DetailTextPrimary)
                    }
                }

                // Download
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DetailCtaGradient)
                        .clickable(enabled = !isDownloading && qrBitmap != null) {
                            qrBitmap?.let { bmp ->
                                scope.launch {
                                    isDownloading = true
                                    saveQRToGallery(context, bmp, "QR_${item.id}")
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
                            Icon(Icons.Default.Download, "Download", tint = Color(0xFF20140A), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isDownloading) "Saving..." else "Download",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF20140A)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// DETAIL ROW WITH ICON
// =====================================================

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Ink750),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AmberSoft, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = DetailTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(value, color = DetailTextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp)
        }
    }
}

// =====================================================
// FORMAT EVENT DATE (YYYYMMDDTHHMMSS → readable)
// =====================================================

private fun formatEventDate(raw: String): String {
    return try {
        // Format: 20260412T194500
        val clean = raw.trim()
        if (clean.length < 15) return clean
        val year = clean.substring(0, 4)
        val month = clean.substring(4, 6).toInt()
        val day = clean.substring(6, 8)
        val hour = clean.substring(9, 11)
        val minute = clean.substring(11, 13)
        val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val monthName = if (month in 1..12) monthNames[month - 1] else "?"
        "$day $monthName $year, $hour:$minute"
    } catch (_: Exception) { raw }
}

// =====================================================
// HELPER FUNCTIONS
// =====================================================

private fun generateQRBitmap(context: Context, content: String, size: Int): Bitmap? {
    return try {
        QRGenerator.generateStandardQRBitmap(content, size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun loadLogoFromDrawable(context: Context): Bitmap? {
    return try {
        android.graphics.BitmapFactory.decodeResource(context.resources, com.qr.hub.R.drawable.qrhub_logo)
    } catch (e: Exception) { null }
}

private fun shareQR(context: Context, content: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(intent, "Share QR"))
}

private suspend fun saveQRToGallery(context: Context, bitmap: Bitmap, name: String) {
    withContext(Dispatchers.IO) {
        try {
            val filename = "$name.png"
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

private data class HistoryBadge(
    val icon: ImageVector,
    val label: String,
    val tint: Color
)

private fun getHistoryBadge(parsed: ScannedQR): HistoryBadge = when (parsed) {
    is ScannedQR.Text -> HistoryBadge(Icons.Default.TextFields, "Text", AmberSoft)
    is ScannedQR.QRURL -> HistoryBadge(Icons.Default.Link, "URL", CyanAccent)
    is ScannedQR.UPI -> HistoryBadge(Icons.Default.AccountBalance, "UPI Payment", AmberSoft)
    is ScannedQR.Phone -> HistoryBadge(Icons.Default.Phone, "Phone", CyanAccent)
    is ScannedQR.Contact -> HistoryBadge(Icons.Default.Person, "Contact", AmberSoft)
    is ScannedQR.SMS -> HistoryBadge(Icons.Default.Sms, "SMS", CyanAccent)
    is ScannedQR.QREmail -> HistoryBadge(Icons.Default.Email, "Email", CyanAccent)
    is ScannedQR.WiFi -> HistoryBadge(Icons.Default.Wifi, "WiFi", CyanAccent)
    is ScannedQR.WhatsApp -> HistoryBadge(Icons.AutoMirrored.Filled.Chat, "WhatsApp", CyanAccent)
    is ScannedQR.Location -> HistoryBadge(Icons.Default.LocationOn, "Location", AmberSoft)
    is ScannedQR.Event -> HistoryBadge(Icons.Default.Event, "Event", AmberSoft)
    is ScannedQR.PlusCode -> HistoryBadge(Icons.Default.Place, "Plus Code", AmberSoft)
    is ScannedQR.GoogleMaps -> HistoryBadge(Icons.Default.Map, "Google Maps", CyanAccent)
    is ScannedQR.Unknown -> HistoryBadge(Icons.Default.QrCode, "Unknown", DetailTextSecondary)
}

private fun parseHistoryDetailItem(item: HistoryItem): ScannedQR {
    val raw = item.rawValue.trim()

    return when (item.type.uppercase()) {
        "URL", "QRURL" -> ScannedQR.QRURL(raw)
        "TEXT" -> ScannedQR.Text(raw)
        "UPI" -> ScannedQR.UPI(
            vpa = queryParam(raw.substringAfter("?", ""), "pa"),
            name = queryParam(raw.substringAfter("?", ""), "pn"),
            amount = queryParam(raw.substringAfter("?", ""), "am"),
            note = queryParam(raw.substringAfter("?", ""), "tn"),
            currency = queryParam(raw.substringAfter("?", ""), "cu").ifEmpty { "INR" },
            rawUri = raw
        )
        "PHONE" -> ScannedQR.Phone(raw.removePrefix("tel:").removePrefix("TEL:"))
        "SMS" -> parseSms(raw)
        "EMAIL", "QREMAIL" -> ScannedQR.QREmail(
            address = raw.removePrefix("mailto:").substringBefore("?"),
            subject = queryParam(raw.substringAfter("?", ""), "subject"),
            body = queryParam(raw.substringAfter("?", ""), "body")
        )
        "WIFI" -> ScannedQR.WiFi(
            ssid = wifiField(raw, "S"),
            password = wifiField(raw, "P"),
            encryption = wifiField(raw, "T").trim().ifEmpty { "None" }
        )
        "WHATSAPP" -> parseWhatsApp(raw)
        "LOCATION" -> parseGeo(raw)
        "PLUS_CODE" -> ScannedQR.PlusCode(raw, "")
        "GOOGLE_MAPS" -> ScannedQR.GoogleMaps(raw)
        "CONTACT", "VCARD" -> ScannedQR.Contact(
            vCard = raw,
            name = vCardField(raw, "FN").ifEmpty { vCardField(raw, "N").substringBefore(';') },
            phone = vCardField(raw, "TEL"),
            email = vCardField(raw, "EMAIL"),
            org = vCardField(raw, "ORG"),
            title = vCardField(raw, "TITLE")
        )
        "EVENT", "VEVENT" -> ScannedQR.Event(
            raw = raw,
            summary = icalField(raw, "SUMMARY"),
            location = icalField(raw, "LOCATION"),
            description = icalField(raw, "DESCRIPTION"),
            startDate = icalField(raw, "DTSTART"),
            endDate = icalField(raw, "DTEND")
        )
        else -> parseHistoryDetailFallback(raw)
    }
}

private fun parseHistoryDetailFallback(raw: String): ScannedQR {
    val lower = raw.lowercase()
    return when {
        lower.startsWith("http://") || lower.startsWith("https://") -> ScannedQR.QRURL(raw)
        lower.startsWith("mailto:") -> ScannedQR.QREmail(
            address = raw.removePrefix("mailto:").substringBefore("?"),
            subject = queryParam(raw.substringAfter("?", ""), "subject"),
            body = queryParam(raw.substringAfter("?", ""), "body")
        )
        lower.startsWith("tel:") -> ScannedQR.Phone(raw.substring(4))
        lower.startsWith("sms:") || lower.startsWith("smsto:") -> parseSms(raw)
        lower.startsWith("wifi:") -> ScannedQR.WiFi(
            ssid = wifiField(raw, "S"),
            password = wifiField(raw, "P"),
            encryption = wifiField(raw, "T").trim().ifEmpty { "None" }
        )
        lower.startsWith("upi://pay") -> ScannedQR.UPI(
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
