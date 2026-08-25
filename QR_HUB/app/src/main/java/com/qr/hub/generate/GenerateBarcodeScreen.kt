package com.qr.hub.generate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class BarcodeType(val displayName: String, val format: BarcodeFormat, val hint: String, val sample: String) {
    CODE_128("Code-128", BarcodeFormat.CODE_128, "Text / Numbers for Products & Logistics", "PROD-2025-A1"),
    EAN_13("EAN-13", BarcodeFormat.EAN_13, "13 Digits Retail & Mart Barcode", "8901234567890"),
    UPC_A("UPC-A", BarcodeFormat.UPC_A, "12 Digits Global Retail Barcode", "012345678905"),
    CODE_39("Code-39", BarcodeFormat.CODE_39, "Alphanumeric Warehouse & Inventory", "ITEM-9988")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateBarcodeScreen(
    isDark: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBarcodeType by remember { mutableStateOf(BarcodeType.CODE_128) }
    var inputText by remember { mutableStateOf("") }
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }

    // Validation
    val isValidInput = when (selectedBarcodeType) {
        BarcodeType.EAN_13 -> inputText.length in 12..13 && inputText.all { it.isDigit() }
        BarcodeType.UPC_A -> inputText.length in 11..12 && inputText.all { it.isDigit() }
        BarcodeType.CODE_128, BarcodeType.CODE_39 -> inputText.trim().isNotEmpty()
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
                Text("Product Barcode Generator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Generate 1D Barcodes for Products & Marts", fontSize = 11.5.sp, color = TextSecondary)
            }
        }

        // ── SCROLLABLE CONTENT ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            // ── FORMAT SELECTOR ──
            Text("Select Barcode Standard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))

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
                                errorMessage = null
                                barcodeBitmap = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            type.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AmberSoft else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── INPUT CARD ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Ink800)
                    .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Product Code / Value", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        Text(
                            "Sample",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberPrimary,
                            modifier = Modifier.clickable {
                                inputText = selectedBarcodeType.sample
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(selectedBarcodeType.hint, fontSize = 11.5.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            errorMessage = null
                        },
                        placeholder = { Text(selectedBarcodeType.sample, color = TextTertiary, fontSize = 13.5.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = BorderLine,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Generate Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isValidInput) AmberCtaGradient else Brush.linearGradient(listOf(Ink750, Ink750)))
                            .clickable(enabled = isValidInput) {
                                scope.launch {
                                    val bmp = generateProductBarcode(inputText, selectedBarcodeType.format)
                                    if (bmp != null) {
                                        barcodeBitmap = bmp
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Invalid characters or length for ${selectedBarcodeType.displayName}"
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ViewWeek,
                                null,
                                tint = if (isValidInput) Color(0xFF20140A) else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Generate Barcode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isValidInput) Color(0xFF20140A) else TextTertiary
                            )
                        }
                    }
                }
            }

            // ── BARCODE PREVIEW CARD ──
            if (barcodeBitmap != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Ink800)
                        .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Generated Barcode Label",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        // Barcode Display Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = barcodeBitmap!!.asImageBitmap(),
                                contentDescription = "Barcode",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Share / Download
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
                                    .background(Ink750)
                                    .border(1.dp, BorderLine, RoundedCornerShape(14.dp))
                                    .clickable {
                                        val activity = context as? Activity
                                        AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                            shareBarcodeImage(context, barcodeBitmap!!)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, null, tint = TextPrimary, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                            }

                            // Download
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AmberCtaGradient)
                                    .clickable(enabled = !isDownloading) {
                                        val activity = context as? Activity
                                        AdManager.showInterstitialWithFrequency(activity, interval = 2) {
                                            scope.launch {
                                                isDownloading = true
                                                saveBarcodeToGallery(context, barcodeBitmap!!, "Barcode_${inputText}")
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
                                        Icon(Icons.Default.Download, null, tint = Color(0xFF20140A), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isDownloading) "Saving..." else "Download Label",
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

        // White Background
        canvas.drawColor(AndroidColor.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
        }

        // Draw Barcode Stripes
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixHeight) {
                if (bitMatrix.get(x, y)) {
                    canvas.drawPoint(x.toFloat(), y.toFloat() + 10f, paint)
                }
            }
        }

        // Alternative safe bitmap write
        val pixels = IntArray(matrixWidth * matrixHeight)
        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        val stripeBitmap = Bitmap.createBitmap(pixels, matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(stripeBitmap, null, Rect(0, 10, widthPx, 190), null)

        // Draw Human-Readable Numbers / Text underneath
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
            android.widget.Toast.makeText(context, "Saved Barcode to Gallery!", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
