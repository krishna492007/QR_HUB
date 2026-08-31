package com.qr.hub.generate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.qr.hub.util.ads.AdManager
import com.qr.hub.util.ads.BannerAdView
import com.qr.hub.util.ads.BannerAdType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.R
import com.qr.hub.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class BatchQRItem(
    val index: Int,
    val text: String,
    val bitmap: Bitmap
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchQrGeneratorScreen(
    isDark: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf("") }
    val itemsList = remember(textInput) {
        textInput.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    val defaultAppLogo = remember {
        try { BitmapFactory.decodeResource(context.resources, R.drawable.qrhub_logo) } catch (_: Exception) { null }
    }

    var styleConfig by remember {
        mutableStateOf(
            QRStyleConfig(
                moduleShape = QRModuleShape.ROUNDED,
                eyeShape = QREyeShape.ROUNDED,
                logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
                frameStyle = QRFrameStyle.CARD_BORDER,
                frameText = "SCAN ME",
                logoBitmap = defaultAppLogo,
                logoTag = "app_logo"
            )
        )
    }

    var isCustomizeExpanded by remember { mutableStateOf(false) }

    // Generation states
    var isGenerating by remember { mutableStateOf(false) }
    var generateProgress by remember { mutableStateOf(0f) }
    var generatedItems by remember { mutableStateOf<List<BatchQRItem>>(emptyList()) }

    // Export states
    var isExportingZip by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // CSV / TXT File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (content.isNotEmpty()) {
                    textInput = content
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg(isDark))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── TOP BAR WITH UNIFIED HTML ICON BADGE ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(appCardBg(isDark))
                    .border(1.dp, appBorder(isDark), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = appTextPrimary(isDark),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(appGoldDim2(isDark))
                    .border(1.dp, appGoldPrimary(isDark).copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HtmlIcons.BulkQr,
                    contentDescription = null,
                    tint = appGoldSoft(isDark),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Bulk QR Generator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appTextPrimary(isDark))
                Text("Generate 100+ QRs in Batch & Export", fontSize = 11.5.sp, color = appTextSecondary(isDark))
            }
        }

        // ── MAIN CONTENT ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            // ── INPUT CARD ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(appCardBg(isDark))
                    .border(1.dp, appBorder(isDark), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(HtmlIcons.BulkQr, null, tint = appGoldPrimary(isDark), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Items List (1 per line)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = appTextPrimary(isDark))
                        }

                        // CSV Import Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(appGoldDim(isDark))
                                .border(1.dp, appGoldPrimary(isDark), RoundedCornerShape(8.dp))
                                .clickable { filePickerLauncher.launch("*/*") }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.UploadFile, null, tint = appGoldSoft(isDark), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import CSV/TXT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = appGoldSoft(isDark))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                "Enter 1 text or URL per line:\nTable 1\nTable 2\nhttps://mybrand.com/menu\nUPI_ID@okaxis",
                                fontSize = 12.5.sp,
                                color = appTextTertiary(isDark)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appGoldPrimary(isDark),
                            unfocusedBorderColor = appBorder(isDark),
                            focusedTextColor = appTextPrimary(isDark),
                            unfocusedTextColor = appTextPrimary(isDark),
                            focusedContainerColor = appElevatedBg(isDark),
                            unfocusedContainerColor = appElevatedBg(isDark)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status & Sample data button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${itemsList.size} item(s) detected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (itemsList.isNotEmpty()) appGoldSoft(isDark) else appTextTertiary(isDark)
                        )

                        Text(
                            "Load Sample Data",
                            fontSize = 11.5.sp,
                            color = appGoldPrimary(isDark),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                textInput = "Product #101 - Smart Watch\nProduct #102 - Earbuds Pro\nProduct #103 - Power Bank\nProduct #104 - Wireless Charger\nProduct #105 - Bluetooth Speaker\nProduct #106 - USB-C Hub"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── EXPANDABLE STYLING PANEL ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(appCardBg(isDark))
                    .border(1.dp, if (isCustomizeExpanded) appGoldPrimary(isDark) else appBorder(isDark), RoundedCornerShape(18.dp))
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
                            Icon(Icons.Default.Palette, null, tint = appGoldSoft(isDark), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Batch QR Styling",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = appTextPrimary(isDark)
                            )
                            Text(
                                if (isCustomizeExpanded) "Tap to collapse styling panel" else "Customize Colors, Shapes, Eyes & Logos for batch",
                                fontSize = 11.5.sp,
                                color = appTextTertiary(isDark)
                            )
                        }
                        Icon(
                            if (isCustomizeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = appGoldSoft(isDark),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isCustomizeExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            HorizontalDivider(color = appBorder(isDark), modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            QRCustomizationSection(
                                qrType = "TEXT",
                                styleConfig = styleConfig,
                                isDark = isDark,
                                onStyleChanged = { newConfig -> styleConfig = newConfig },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── GENERATE BATCH CTA ──
            val canGenerate = itemsList.isNotEmpty() && !isGenerating
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (canGenerate) appCtaGradient(isDark)
                        else Brush.linearGradient(listOf(appCardBg(isDark), appCardBg(isDark)))
                    )
                    .then(
                        if (canGenerate) Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(16.dp))
                        else Modifier.border(1.dp, appBorder(isDark), RoundedCornerShape(16.dp))
                    )
                    .clickable(enabled = canGenerate) {
                        val activity = context as? Activity
                        AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                            scope.launch {
                                isGenerating = true
                                generateProgress = 0f
                                val list = mutableListOf<BatchQRItem>()

                                withContext(Dispatchers.Default) {
                                    itemsList.forEachIndexed { idx, rawText ->
                                        val qr = QRStylingEngine.renderStyledQR(rawText, styleConfig, 768)
                                        list.add(BatchQRItem(idx + 1, rawText, qr))
                                        generateProgress = (idx + 1).toFloat() / itemsList.size
                                    }
                                }

                                generatedItems = list
                                isGenerating = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = if (isDark) Color(0xFF20140A) else CeramicCtaInk, strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generating ${(generateProgress * 100).toInt()}%...",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF20140A) else CeramicCtaInk
                        )
                    } else {
                        Icon(Icons.Default.Bolt, null, tint = if (canGenerate) (if (isDark) Color(0xFF20140A) else CeramicCtaInk) else appTextTertiary(isDark), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (itemsList.isEmpty()) "Add Items to Generate" else "Generate All (${itemsList.size} QRs)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canGenerate) (if (isDark) Color(0xFF20140A) else CeramicCtaInk) else appTextTertiary(isDark)
                        )
                    }
                }
            }

            // ── GENERATED RESULTS & EXPORT OPTIONS ──
            if (generatedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                // Export Header & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Generated (${generatedItems.size} QRs)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = appTextPrimary(isDark)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Export ZIP Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(appGoldDim(isDark))
                                .border(1.dp, appGoldPrimary(isDark), RoundedCornerShape(10.dp))
                                .clickable(enabled = !isExportingZip) {
                                    scope.launch {
                                        isExportingZip = true
                                        exportBatchAsZip(context, generatedItems)
                                        isExportingZip = false
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isExportingZip) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = appGoldSoft(isDark), strokeWidth = 1.5.dp)
                                } else {
                                    Icon(Icons.Default.FolderZip, null, tint = appGoldSoft(isDark), modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ZIP Export", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = appGoldSoft(isDark))
                            }
                        }

                        // Export PDF Sheet Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(appCtaGradient(isDark))
                                .clickable(enabled = !isExportingPdf) {
                                    scope.launch {
                                        isExportingPdf = true
                                        exportBatchAsPrintablePdf(context, generatedItems)
                                        isExportingPdf = false
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isExportingPdf) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = if (isDark) Color(0xFF20140A) else CeramicCtaInk, strokeWidth = 1.5.dp)
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, null, tint = if (isDark) Color(0xFF20140A) else CeramicCtaInk, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF Sheet", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF20140A) else CeramicCtaInk)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2-Column Grid of Generated QRs
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunked = generatedItems.chunked(2)
                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(appCardBg(isDark))
                                        .border(1.dp, appBorder(isDark), RoundedCornerShape(16.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(styleConfig.bgColor))
                                                .border(1.dp, if (isDark) BorderLineStrong else appGoldPrimary(isDark).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                                .padding(6.dp)
                                        ) {
                                            Image(
                                                bitmap = item.bitmap.asImageBitmap(),
                                                contentDescription = item.text,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            "#${item.index} ${item.text}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = appTextPrimary(isDark),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Prominent Banner Ad
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BannerAdView(
                    type = if (generatedItems.isEmpty()) BannerAdType.MEDIUM_RECTANGLE else BannerAdType.ADAPTIVE,
                    showAdBadge = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Packages all generated QR bitmaps into a ZIP archive and triggers Share sheet
 */
private suspend fun exportBatchAsZip(context: Context, items: List<BatchQRItem>) = withContext(Dispatchers.IO) {
    try {
        val zipFile = File(context.cacheDir, "QR_Batch_${System.currentTimeMillis()}.zip")
        val zos = ZipOutputStream(FileOutputStream(zipFile))

        items.forEach { item ->
            val cleanName = item.text.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(20)
            val entryName = "QR_${item.index}_$cleanName.png"
            zos.putNextEntry(ZipEntry(entryName))
            val stream = ByteArrayOutputStream()
            item.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            zos.write(stream.toByteArray())
            zos.closeEntry()
        }
        zos.close()

        // Also save to Downloads if possible
        saveZipToDownloads(context, zipFile)

        // Share ZIP Intent
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Batch ZIP"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Creates multi-page A4 Printable PDF Sheet with 2x3 grid (6 QRs per page)
 */
private suspend fun exportBatchAsPrintablePdf(context: Context, items: List<BatchQRItem>) = withContext(Dispatchers.IO) {
    try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points
        val itemsPerPage = 6

        val pagesCount = (items.size + itemsPerPage - 1) / itemsPerPage

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textSize = 14f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        for (p in 0 until pagesCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Page Header
            canvas.drawText("QR HUB - Batch Printable Sheet", 40f, 45f, headerPaint)
            textPaint.textSize = 10f
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Page ${p + 1} of $pagesCount", (pageWidth - 40).toFloat(), 45f, textPaint)
            textPaint.textAlign = Paint.Align.CENTER

            val startIndex = p * itemsPerPage
            val pageItems = items.subList(startIndex, (startIndex + itemsPerPage).coerceAtMost(items.size))

            // 2 Columns x 3 Rows
            val colWidth = 230f
            val rowHeight = 220f
            val startX = 50f
            val startY = 70f

            pageItems.forEachIndexed { i, item ->
                val col = i % 2
                val row = i / 2

                val x = startX + (col * (colWidth + 35f))
                val y = startY + (row * (rowHeight + 25f))

                // Card outline with rounded corners
                val cardRect = RectF(x, y, x + colWidth, y + rowHeight)
                canvas.drawRoundRect(cardRect, 12f, 12f, borderPaint)

                // Draw QR Code
                val qrSize = 140f
                val qrX = x + ((colWidth - qrSize) / 2f)
                val qrY = y + 18f
                val destRect = android.graphics.Rect(qrX.toInt(), qrY.toInt(), (qrX + qrSize).toInt(), (qrY + qrSize).toInt())
                canvas.drawBitmap(item.bitmap, null, destRect, null)

                // Label Text
                val labelText = "#${item.index}  ${item.text}".take(30)
                textPaint.textSize = 10.5f
                canvas.drawText(labelText, x + (colWidth / 2f), y + qrSize + 36f, textPaint)
            }

            pdfDocument.finishPage(page)
        }

        val pdfFile = File(context.cacheDir, "QR_Sheet_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

        // Share PDF Intent
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Printable PDF Sheet"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun saveZipToDownloads(context: Context, zipFile: File) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, zipFile.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRHub")
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { os ->
                    zipFile.inputStream().copyTo(os)
                }
            }
        }
    } catch (_: Exception) {}
}
