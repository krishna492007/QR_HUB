package com.qr.hub.generate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.qr.hub.util.*
import com.qr.hub.util.ads.AdManager
import com.qr.hub.util.ads.BannerAdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.min

enum class BarcodeType(val displayName: String, val format: BarcodeFormat, val hint: String, val sample: String) {
    CODE_128("Code-128", BarcodeFormat.CODE_128, "Text / Numbers for Products & Logistics", "PROD-2025-A1"),
    EAN_13("EAN-13", BarcodeFormat.EAN_13, "13 Digits Retail & Mart Barcode", "8901234567890"),
    UPC_A("UPC-A", BarcodeFormat.UPC_A, "12 Digits Global Retail Barcode", "012345678905"),
    CODE_39("Code-39", BarcodeFormat.CODE_39, "Alphanumeric Warehouse & Inventory", "ITEM-9988")
}

enum class SheetFormat(val countPerPage: Int, val cols: Int, val rows: Int, val title: String, val useCase: String) {
    LABELS_24(24, 3, 8, "24 Labels (3x8)", "Standard Retail & Marts (Best)"),
    LABELS_12(12, 2, 6, "12 Labels (2x6)", "Boxes & Shipping Parcels"),
    LABELS_40(40, 4, 10, "40 Labels (4x10)", "Medicines & Cosmetics"),
    LABELS_65(65, 5, 13, "65 Labels (5x13)", "Jewellery & Micro Tags")
}

data class BatchBarcodeItem(
    val index: Int,
    val text: String,
    val bitmap: Bitmap
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateBarcodeScreen(
    isDark: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0 = Single & Multi-Copy Sheet, 1 = Bulk CSV
    var selectedBarcodeType by remember { mutableStateOf(BarcodeType.CODE_128) }
    var selectedSheetFormat by remember { mutableStateOf(SheetFormat.LABELS_24) }

    // ── Single Mode States ──
    var singleInputText by remember { mutableStateOf("") }
    var singleBarcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var singleErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSavingSingle by remember { mutableStateOf(false) }

    // Copies state for Multi-Page Sticker Printing
    var copiesInputText by remember { mutableStateOf("24") }
    val totalCopies = copiesInputText.toIntOrNull() ?: 24
    val calculatedPages = remember(totalCopies, selectedSheetFormat) {
        if (totalCopies <= 0) 1
        else (totalCopies + selectedSheetFormat.countPerPage - 1) / selectedSheetFormat.countPerPage
    }
    var isGeneratingStickerSheet by remember { mutableStateOf(false) }

    // ── Bulk Mode States ──
    var bulkTextInput by remember { mutableStateOf("") }
    val bulkItemsList = remember(bulkTextInput) {
        bulkTextInput.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }
    var isGeneratingBulk by remember { mutableStateOf(false) }
    var bulkProgress by remember { mutableStateOf(0f) }
    var generatedBulkItems by remember { mutableStateOf<List<BatchBarcodeItem>>(emptyList()) }
    var isExportingBulkZip by remember { mutableStateOf(false) }
    var isExportingBulkPdf by remember { mutableStateOf(false) }

    // CSV / TXT File Picker for Bulk
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (content.isNotEmpty()) {
                    bulkTextInput = content
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Validation for single input
    val isSingleInputValid = when (selectedBarcodeType) {
        BarcodeType.EAN_13 -> singleInputText.length in 12..13 && singleInputText.all { it.isDigit() }
        BarcodeType.UPC_A -> singleInputText.length in 11..12 && singleInputText.all { it.isDigit() }
        BarcodeType.CODE_128, BarcodeType.CODE_39 -> singleInputText.trim().isNotEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── TOP BAR ──
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
                    .background(Ink800)
                    .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("Product Barcode Studio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Generate & Print A4 Sticker Sheets for Shops & Marts", fontSize = 11.5.sp, color = TextSecondary)
            }
        }

        // ── MODE TABS (Single & Sticker Sheet vs Bulk CSV) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Ink800)
                .border(1.dp, BorderLine, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            // Tab 0: Single & Sticker Sheet
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 0) AmberPrimary else Color.Transparent)
                    .clickable { activeTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.QrCode,
                        null,
                        tint = if (activeTab == 0) Color(0xFF160E06) else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Single & Stickers",
                        fontSize = 12.sp,
                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeTab == 0) Color(0xFF160E06) else TextSecondary
                    )
                }
            }

            // Tab 1: Bulk Barcodes / CSV
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 1) AmberPrimary else Color.Transparent)
                    .clickable { activeTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DynamicFeed,
                        null,
                        tint = if (activeTab == 1) Color(0xFF160E06) else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Bulk List (CSV)",
                        fontSize = 12.sp,
                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeTab == 1) Color(0xFF160E06) else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── SCROLLABLE CONTENT ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            // ── FORMAT SELECTOR ──
            Text("Select Barcode Standard", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BarcodeType.values().forEach { type ->
                    val isSelected = selectedBarcodeType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AmberDim else Ink800)
                            .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(12.dp))
                            .clickable {
                                selectedBarcodeType = type
                                singleErrorMessage = null
                                singleBarcodeBitmap = null
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            type.displayName,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AmberSoft else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =========================================================================
            // TAB 0: SINGLE BARCODE & MULTI-COPY STICKER SHEET
            // =========================================================================
            if (activeTab == 0) {
                // Input Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Ink800)
                        .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Product Code / Number", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                            Text(
                                "Sample",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberPrimary,
                                modifier = Modifier.clickable {
                                    singleInputText = selectedBarcodeType.sample
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedBarcodeType.hint, fontSize = 11.sp, color = TextTertiary)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = singleInputText,
                            onValueChange = {
                                singleInputText = it
                                singleErrorMessage = null
                            },
                            placeholder = { Text(selectedBarcodeType.sample, color = TextTertiary, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLine,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (singleErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(singleErrorMessage!!, color = Color(0xFFFF5252), fontSize = 11.5.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Generate Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSingleInputValid) AmberCtaGradient else Brush.linearGradient(listOf(Ink750, Ink750)))
                                .clickable(enabled = isSingleInputValid) {
                                    scope.launch {
                                        val bmp = generateProductBarcode(singleInputText, selectedBarcodeType.format)
                                        if (bmp != null) {
                                            singleBarcodeBitmap = bmp
                                            singleErrorMessage = null
                                        } else {
                                            singleErrorMessage = "Invalid format or length for ${selectedBarcodeType.displayName}"
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ViewWeek,
                                    null,
                                    tint = if (isSingleInputValid) Color(0xFF20140A) else TextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Generate Barcode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSingleInputValid) Color(0xFF20140A) else TextTertiary
                                )
                            }
                        }
                    }
                }

                // Barcode Preview & Multi-Page Sticker Sheet Exporter
                if (singleBarcodeBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Ink800)
                            .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Generated Barcode Label",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // White Barcode Display Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = singleBarcodeBitmap!!.asImageBitmap(),
                                    contentDescription = "Barcode",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Single Save & Share Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Ink750)
                                        .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                                        .clickable {
                                            val activity = context as? Activity
                                            AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                                shareBarcodeImage(context, singleBarcodeBitmap!!)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Share, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share Single", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AmberDim)
                                        .border(1.dp, AmberPrimary, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isSavingSingle) {
                                            val activity = context as? Activity
                                            AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                                scope.launch {
                                                    isSavingSingle = true
                                                    saveBarcodeToGallery(context, singleBarcodeBitmap!!, "Barcode_${singleInputText}")
                                                    isSavingSingle = false
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSavingSingle) {
                                            CircularProgressIndicator(modifier = Modifier.size(15.dp), color = AmberSoft, strokeWidth = 1.5.dp)
                                        } else {
                                            Icon(Icons.Default.Download, null, tint = AmberSoft, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Single", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = AmberSoft)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = BorderLine)
                            Spacer(modifier = Modifier.height(14.dp))

                            // ── MULTI-PAGE STICKER SHEET CONFIGURATION ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Print Sticker Sheet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("A4 Self-Adhesive Sticker Paper", fontSize = 11.sp, color = TextTertiary)
                                }

                                // Clean Pill Badge for A4 Page Count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AmberDim)
                                        .border(1.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Description, null, tint = AmberSoft, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "$calculatedPages A4 Page${if (calculatedPages > 1) "s" else ""}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberSoft
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 Standard Sheet Formats
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val chunked = SheetFormat.values().toList().chunked(2)
                                chunked.forEach { rowFormats ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowFormats.forEach { format ->
                                            val isPicked = selectedSheetFormat == format
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isPicked) AmberDim else Ink750)
                                                    .border(1.dp, if (isPicked) AmberPrimary else BorderLine, RoundedCornerShape(12.dp))
                                                    .clickable { selectedSheetFormat = format }
                                                    .padding(horizontal = 10.dp, vertical = 9.dp)
                                            ) {
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            format.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isPicked) AmberSoft else TextPrimary
                                                        )
                                                        if (isPicked) {
                                                            Icon(Icons.Default.CheckCircle, null, tint = AmberPrimary, modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        format.useCase,
                                                        fontSize = 10.sp,
                                                        color = if (isPicked) TextSecondary else TextTertiary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Section Title & Counter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Quantity / Copies", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                Text("$totalCopies Stickers", fontSize = 11.5.sp, color = AmberSoft, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quantity Stepper & Quick-Select Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Stepper [-]
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Ink750)
                                        .border(1.dp, BorderLine, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val cur = copiesInputText.toIntOrNull() ?: 24
                                            val next = (cur - 12).coerceAtLeast(1)
                                            copiesInputText = next.toString()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                }

                                // Direct Input Box
                                OutlinedTextField(
                                    value = copiesInputText,
                                    onValueChange = {
                                        val filtered = it.filter { ch -> ch.isDigit() }.take(4)
                                        copiesInputText = filtered
                                    },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier
                                        .width(76.dp)
                                        .height(48.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AmberPrimary,
                                        unfocusedBorderColor = BorderLine,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                // Stepper [+]
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Ink750)
                                        .border(1.dp, BorderLine, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val cur = copiesInputText.toIntOrNull() ?: 24
                                            val next = cur + 12
                                            copiesInputText = next.toString()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                }

                                // Quick Preset Pills
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("24", "48", "100", "200").forEach { quickCount ->
                                        val isSelected = copiesInputText == quickCount
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) AmberDim else Ink750)
                                                .border(1.dp, if (isSelected) AmberPrimary else BorderLine, RoundedCornerShape(8.dp))
                                            .clickable { copiesInputText = quickCount }
                                            .padding(horizontal = 9.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                "$quickCount pcs",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) AmberSoft else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Export Multi-Page Sticker Sheet CTA
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AmberCtaGradient)
                                    .clickable(enabled = !isGeneratingStickerSheet && totalCopies > 0) {
                                        val activity = context as? Activity
                                        AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                            scope.launch {
                                                isGeneratingStickerSheet = true
                                                exportMultiPageStickerSheetPdf(
                                                    context = context,
                                                    barcodeBitmap = singleBarcodeBitmap!!,
                                                    label = singleInputText,
                                                    totalCopies = totalCopies,
                                                    format = selectedSheetFormat
                                                )
                                                isGeneratingStickerSheet = false
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isGeneratingStickerSheet) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF20140A), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFF20140A), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (isGeneratingStickerSheet) "Generating PDF Sheet..." else "Export $totalCopies Stickers ($calculatedPages Page${if (calculatedPages > 1) "s" else ""} PDF)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF20140A)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 1: BULK BARCODES (CSV / MULTI-LINE LIST)
            // =========================================================================
            if (activeTab == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Ink800)
                        .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Barcodes List (1 per line)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberDim)
                                    .border(1.dp, AmberPrimary, RoundedCornerShape(8.dp))
                                    .clickable { filePickerLauncher.launch("*/*") }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.UploadFile, null, tint = AmberSoft, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AmberSoft)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bulkTextInput,
                            onValueChange = { bulkTextInput = it },
                            placeholder = {
                                Text(
                                    "Enter 1 code per line:\nPROD-001\nPROD-002\nPROD-003\n8901234567890",
                                    fontSize = 12.5.sp,
                                    color = TextTertiary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = BorderLine,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${bulkItemsList.size} barcode(s) detected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (bulkItemsList.isNotEmpty()) AmberSoft else TextTertiary
                            )

                            Text(
                                "Load 10 Sample Items",
                                fontSize = 11.5.sp,
                                color = AmberPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    bulkTextInput = "ITEM-101-WATCH\nITEM-102-EARBUD\nITEM-103-SPEAKER\nITEM-104-CHARGER\nITEM-105-POWERBANK\nITEM-106-ADAPTER\nITEM-107-MOUSE\nITEM-108-KEYBOARD\nITEM-109-STAND\nITEM-110-CABLE"
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Generate All Button
                        val canGenerateBulk = bulkItemsList.isNotEmpty() && !isGeneratingBulk
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (canGenerateBulk) AmberCtaGradient else Brush.linearGradient(listOf(Ink750, Ink750)))
                                .clickable(enabled = canGenerateBulk) {
                                    val activity = context as? Activity
                                    AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                        scope.launch {
                                            isGeneratingBulk = true
                                            bulkProgress = 0f
                                            val list = mutableListOf<BatchBarcodeItem>()

                                            withContext(Dispatchers.Default) {
                                                bulkItemsList.forEachIndexed { idx, rawText ->
                                                    val bmp = generateProductBarcode(rawText, selectedBarcodeType.format)
                                                    if (bmp != null) {
                                                        list.add(BatchBarcodeItem(idx + 1, rawText, bmp))
                                                    }
                                                    bulkProgress = (idx + 1).toFloat() / bulkItemsList.size
                                                }
                                            }

                                            generatedBulkItems = list
                                            isGeneratingBulk = false
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isGeneratingBulk) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF20140A), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating ${(bulkProgress * 100).toInt()}%...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF20140A))
                                } else {
                                    Icon(Icons.Default.Bolt, null, tint = if (canGenerateBulk) Color(0xFF20140A) else TextTertiary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (bulkItemsList.isEmpty()) "Add Items to Generate" else "Generate All (${bulkItemsList.size} Barcodes)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canGenerateBulk) Color(0xFF20140A) else TextTertiary
                                    )
                                }
                            }
                        }
                    }
                }

                // Bulk Results & Multi-Page Export Section
                if (generatedBulkItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))

                    val bulkPagesCount = (generatedBulkItems.size + selectedSheetFormat.countPerPage - 1) / selectedSheetFormat.countPerPage

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Generated (${generatedBulkItems.size} Barcodes)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text("$bulkPagesCount Page(s) A4 Sheet", fontSize = 11.sp, color = AmberSoft)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // ZIP Export
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmberDim)
                                    .border(1.dp, AmberPrimary, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isExportingBulkZip) {
                                        scope.launch {
                                            isExportingBulkZip = true
                                            exportBatchBarcodesAsZip(context, generatedBulkItems)
                                            isExportingBulkZip = false
                                        }
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isExportingBulkZip) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = AmberSoft, strokeWidth = 1.5.dp)
                                    } else {
                                        Icon(Icons.Default.FolderZip, null, tint = AmberSoft, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ZIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberSoft)
                                }
                            }

                            // PDF Sheet Export
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmberCtaGradient)
                                    .clickable(enabled = !isExportingBulkPdf) {
                                        scope.launch {
                                            isExportingBulkPdf = true
                                            exportBulkBarcodesAsPrintablePdf(context, generatedBulkItems, selectedSheetFormat)
                                            isExportingBulkPdf = false
                                        }
                                    }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isExportingBulkPdf) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color(0xFF20140A), strokeWidth = 1.5.dp)
                                    } else {
                                        Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFF20140A), modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF20140A))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2-Column Grid of Generated Barcodes
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val chunked = generatedBulkItems.chunked(2)
                        chunked.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Ink800)
                                            .border(1.dp, BorderLine, RoundedCornerShape(14.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(70.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
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
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
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
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Banner Ad
        BannerAdView(modifier = Modifier.fillMaxWidth())
    }
}

/**
 * Generate 1D Product Barcode Bitmap with Human-Readable Numbers below
 */
private suspend fun generateProductBarcode(
    content: String,
    format: BarcodeFormat,
    widthPx: Int = 800,
    heightPx: Int = 260
): Bitmap? = withContext(Dispatchers.Default) {
    try {
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(content, format, widthPx, 180)

        val outputBitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        canvas.drawColor(AndroidColor.WHITE)

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val pixels = IntArray(matrixWidth * matrixHeight)
        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        val stripeBitmap = Bitmap.createBitmap(pixels, matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(stripeBitmap, null, Rect(0, 10, widthPx, 190), null)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
            textSize = 28f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }

        canvas.drawText(content, widthPx / 2f, heightPx - 20f, textPaint)

        outputBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Multi-Page A4 Sticker Sheet PDF for Case 1: Same Barcode with N Copies (e.g. 100 copies = 5 pages)
 */
private suspend fun exportMultiPageStickerSheetPdf(
    context: Context,
    barcodeBitmap: Bitmap,
    label: String,
    totalCopies: Int,
    format: SheetFormat
) = withContext(Dispatchers.IO) {
    try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width points
        val pageHeight = 842 // A4 standard height points

        val cols = format.cols
        val rows = format.rows
        val perPage = format.countPerPage

        val totalPages = (totalCopies + perPage - 1) / perPage

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textSize = 11f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val pageNumPaint = Paint(headerPaint).apply {
            textAlign = Paint.Align.RIGHT
            textSize = 10f
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val marginX = 25f
        val marginY = 40f
        val availableWidth = pageWidth - (marginX * 2)
        val availableHeight = pageHeight - marginY - 25f

        val cellWidth = availableWidth / cols
        val cellHeight = availableHeight / rows

        var printedCount = 0

        for (p in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Page Header
            canvas.drawText("QR HUB - Barcode Sticker Sheet ($totalCopies Copies)", marginX, 26f, headerPaint)
            canvas.drawText("Page ${p + 1} of $totalPages", (pageWidth - marginX), 26f, pageNumPaint)

            val remainingForThisPage = min(perPage, totalCopies - printedCount)

            for (i in 0 until remainingForThisPage) {
                val c = i % cols
                val r = i / cols

                val x = marginX + (c * cellWidth)
                val y = marginY + (r * cellHeight)

                // Sticker Border
                val rect = RectF(x + 2f, y + 2f, x + cellWidth - 2f, y + cellHeight - 2f)
                canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

                // Draw Barcode inside sticker
                val paddingH = cellWidth * 0.08f
                val paddingV = cellHeight * 0.12f
                val destRect = Rect(
                    (x + paddingH).toInt(),
                    (y + paddingV).toInt(),
                    (x + cellWidth - paddingH).toInt(),
                    (y + cellHeight - paddingV).toInt()
                )
                canvas.drawBitmap(barcodeBitmap, null, destRect, null)
                printedCount++
            }

            pdfDocument.finishPage(page)
        }

        val pdfFile = File(context.cacheDir, "Barcode_Sheet_${totalCopies}Copies_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

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
        context.startActivity(Intent.createChooser(shareIntent, "Share Sticker Sheet PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Multi-Page A4 Printable PDF Sheet for Bulk Barcodes (Case 2: 100 Different Barcodes)
 */
private suspend fun exportBulkBarcodesAsPrintablePdf(
    context: Context,
    items: List<BatchBarcodeItem>,
    format: SheetFormat
) = withContext(Dispatchers.IO) {
    try {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val cols = format.cols
        val rows = format.rows
        val perPage = format.countPerPage

        val totalPages = (items.size + perPage - 1) / perPage

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textSize = 11f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val pageNumPaint = Paint(headerPaint).apply {
            textAlign = Paint.Align.RIGHT
            textSize = 10f
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val marginX = 25f
        val marginY = 40f
        val availableWidth = pageWidth - (marginX * 2)
        val availableHeight = pageHeight - marginY - 25f

        val cellWidth = availableWidth / cols
        val cellHeight = availableHeight / rows

        for (p in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawText("QR HUB - Bulk Barcode Catalog Sheet (${items.size} Items)", marginX, 26f, headerPaint)
            canvas.drawText("Page ${p + 1} of $totalPages", (pageWidth - marginX), 26f, pageNumPaint)

            val startIndex = p * perPage
            val pageItems = items.subList(startIndex, min(startIndex + perPage, items.size))

            pageItems.forEachIndexed { i, item ->
                val c = i % cols
                val r = i / cols

                val x = marginX + (c * cellWidth)
                val y = marginY + (r * cellHeight)

                val rect = RectF(x + 2f, y + 2f, x + cellWidth - 2f, y + cellHeight - 2f)
                canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

                val paddingH = cellWidth * 0.08f
                val paddingV = cellHeight * 0.12f
                val destRect = Rect(
                    (x + paddingH).toInt(),
                    (y + paddingV).toInt(),
                    (x + cellWidth - paddingH).toInt(),
                    (y + cellHeight - paddingV).toInt()
                )
                canvas.drawBitmap(item.bitmap, null, destRect, null)
            }

            pdfDocument.finishPage(page)
        }

        val pdfFile = File(context.cacheDir, "Bulk_Barcodes_${items.size}Items_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

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
        context.startActivity(Intent.createChooser(shareIntent, "Share Bulk Barcodes PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Packages all bulk barcode bitmaps into a ZIP archive
 */
private suspend fun exportBatchBarcodesAsZip(context: Context, items: List<BatchBarcodeItem>) = withContext(Dispatchers.IO) {
    try {
        val zipFile = File(context.cacheDir, "Barcodes_Batch_${System.currentTimeMillis()}.zip")
        val zos = ZipOutputStream(FileOutputStream(zipFile))

        items.forEach { item ->
            val cleanName = item.text.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(20)
            val entryName = "Barcode_${item.index}_$cleanName.png"
            zos.putNextEntry(ZipEntry(entryName))
            val stream = ByteArrayOutputStream()
            item.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            zos.write(stream.toByteArray())
            zos.closeEntry()
        }
        zos.close()

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
        context.startActivity(Intent.createChooser(shareIntent, "Share Barcodes ZIP Archive"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareBarcodeImage(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "barcode_${System.currentTimeMillis()}.png")
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
        context.startActivity(Intent.createChooser(shareIntent, "Share Barcode Label"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun saveBarcodeToGallery(context: Context, bitmap: Bitmap, name: String) {
    try {
        val filename = "${name}_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRHub")
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
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
            android.widget.Toast.makeText(context, "Saved Barcode to Gallery!", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
