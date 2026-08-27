package com.qr.hub.scanner

import androidx.compose.ui.text.withStyle
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.qr.hub.model.ScannedQR
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qr.hub.viewmodel.HistoryViewModel
import com.qr.hub.util.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

private enum class CameraState { LOADING, STARTED, DENIED }

// Premium accent colors for scanner
private val ScannerAccent = Color(0xFF6C63FF)
private val ScannerAccentGlow = Color(0xFF9D97FF)
private val ScannerLaserStart = Color(0xFF6C63FF)
private val ScannerLaserMid = Color(0xFFE94EFF)
private val ScannerLaserEnd = Color(0xFF6C63FF)
private val GlassBg = Color(0xFF0D1117)
private val GlassBorder = Color(0xFF2A2F3A)

@Composable
fun ScannerScreen(
    isDark: Boolean,
    onNavigateToResult: (ScannedQR.RawResult) -> Unit,
    onBack: (() -> Unit)? = null,
    onPrivacyPolicyClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val historyViewModel: HistoryViewModel = viewModel()
    var cameraState by remember { mutableStateOf(CameraState.LOADING) }
    var flashOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var scannedResult by remember { mutableStateOf<ScannedQR.RawResult?>(null) }
    var galleryUri by remember { mutableStateOf<Uri?>(null) }
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.getSystemService(context, VibratorManager::class.java)?.defaultVibrator
        } else {
            ContextCompat.getSystemService(context, Vibrator::class.java)
        }
    }
    val lastVibrateTime = remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    // Vibration helper function (for both camera and gallery)
    fun triggerVibrate() {
        scope.launch {
            val now = System.currentTimeMillis()
            if (now - lastVibrateTime.value > 500) {
                if (vibrator == null) {
                    android.util.Log.d("ScannerScreen", "Vibrator is null!")
                    return@launch
                }
                if (!vibrator!!.hasVibrator()) {
                    android.util.Log.d("ScannerScreen", "Device has no vibrator")
                    return@launch
                }
                android.util.Log.d("ScannerScreen", "Vibrating now (300ms)")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator!!.vibrate(
                        VibrationEffect.createOneShot(
                            300,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator!!.vibrate(300)
                }
                lastVibrateTime.value = now
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            cameraState = if (granted) CameraState.STARTED else CameraState.DENIED
        }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { galleryUri = it }
        }
    )

    val bg = if (isDark) DarkPrimary else LightPrimary
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val accent = if (isDark) DarkAccent else LightAccent

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraState = CameraState.STARTED
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Scan from gallery image
    LaunchedEffect(galleryUri) {
        galleryUri?.let { uri ->
            try {
                val bitmap = android.graphics.BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(uri)
                )
                if (bitmap != null) {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val scanner = BarcodeScanning.getClient()
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty() && barcodes[0].rawValue != null) {
                                triggerVibrate()
                                scannedResult = ScannedQR.RawResult(
                                    rawValue = barcodes[0].rawValue!!,
                                    format = barcodes[0].format,
                                    fromGallery = true
                                )
                            }
                        }
                }
            } catch (_: Exception) { }
            galleryUri = null
        }
    }

    // Navigate to result or execute Quick Auto-Pay
    LaunchedEffect(scannedResult) {
        scannedResult?.let { result ->
            val parsed = detectType(result.rawValue)
            historyViewModel.saveScan(result.rawValue, parsed)

            if (parsed is ScannedQR.UPI && UpiPreferenceManager.isQuickPayEnabled(context)) {
                val defaultPkg = UpiPreferenceManager.getDefaultPackage(context)
                if (!defaultPkg.isNullOrEmpty()) {
                    QrGallerySaver.saveOnce(context, result.rawValue)
                    if (parsed.vpa.isNotBlank()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", parsed.vpa))
                    }
                    val pm = context.packageManager
                    val launchIntent = pm.getLaunchIntentForPackage(defaultPkg)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                        scannedResult = null
                        return@LaunchedEffect
                    }
                }
            }
            cameraState = CameraState.LOADING
            onNavigateToResult(result)
            scannedResult = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        when (cameraState) {
            CameraState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accent)
                }
            }

            CameraState.DENIED -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = accent
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Camera Permission Required",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "QR scan karne ke liye camera access chahiye.",
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            cameraState = CameraState.LOADING
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) {
                        Text("Request Permission")
                    }
                }
            }

            CameraState.STARTED -> ScannerActiveView(
                isDark = isDark,
                accent = accent,
                lensFacing = lensFacing,
                onLensFacingChange = { lensFacing = it },
                flashOn = flashOn,
                onFlashToggle = { flashOn = !flashOn },
                onNavigateToResult = { value ->
                    scannedResult = value
                },
                onBack = onBack,
                onPrivacyPolicyClick = onPrivacyPolicyClick,
                photoPickerLauncher = photoPickerLauncher,
                onVibrate = { triggerVibrate() }
            )
        }
    }
}

@Composable
private fun ScannerActiveView(
    isDark: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    lensFacing: Int,
    onLensFacingChange: (Int) -> Unit,
    flashOn: Boolean,
    onFlashToggle: () -> Unit,
    onNavigateToResult: (ScannedQR.RawResult) -> Unit,
    onBack: (() -> Unit)?,
    onPrivacyPolicyClick: (() -> Unit)?,
    photoPickerLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    onVibrate: () -> Unit
) {
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT

    // Menu & UPI state
    var showMenu by remember { mutableStateOf(false) }
    var showDefaultAppDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var defaultPkg by remember { mutableStateOf(UpiPreferenceManager.getDefaultPackage(context)) }
    var defaultName by remember { mutableStateOf(UpiPreferenceManager.getDefaultName(context)) }
    var isQuickPay by remember { mutableStateOf(UpiPreferenceManager.isQuickPayEnabled(context)) }
    val installedUpiApps = remember { getInstalledUpiApps(context) }

    if (showDefaultAppDialog) {
        SetDefaultUpiAppDialog(
            installedApps = installedUpiApps,
            currentDefaultPkg = defaultPkg,
            onDismiss = { showDefaultAppDialog = false },
            onSetDefault = { pkg, name ->
                defaultPkg = pkg
                defaultName = name
                UpiPreferenceManager.setDefaultApp(context, pkg, name)
            }
        )
    }

    // ==========================================
    // ANIMATIONS
    // ==========================================

    // Corner pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")

    val cornerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerAlpha"
    )

    // Scanning laser line position (0f to 1f)
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    // Instruction text fade
    val instructionAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "instructionAlpha"
    )

    var hasDetected by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera preview
        val handleBarcodeDetected: (String) -> Unit = { value ->
            if (!hasDetected) {
                hasDetected = true
                onVibrate()
                onNavigateToResult(
                    ScannedQR.RawResult(
                        rawValue = value,
                        format = com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE,
                        fromGallery = false
                    )
                )
            }
        }
        CameraXPreview(
            modifier = Modifier.fillMaxSize(),
            onBarcodeDetected = handleBarcodeDetected,
            lensFacing = lensFacing,
            flashOn = flashOn
        )

        // ==========================================
        // DARK OVERLAY WITH CUTOUT FOR SCAN AREA
        // ==========================================
        ScannerOverlay(
            cornerAlpha = { cornerAlpha },
            laserProgress = { laserProgress }
        )

        // ==========================================
        // INSTRUCTION TEXT
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Position the instruction text below the scan area
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 170.dp)
                    .graphicsLayer { alpha = instructionAlpha },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Align the code inside the frame",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
                Text(
                    text = "// live",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyanAccent
                )
            }
        }

        // ==========================================
        // TOP CONTROLS — REDESIGNED INK & AMBER
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink800)
                    .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                    .clickable { onBack?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Title
            Text(
                "Scan",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            // More menu button
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Ink800)
                        .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Ink850,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLineStrong),
                    shadowElevation = 12.dp,
                    modifier = Modifier.width(250.dp)
                ) {
                    // Default UPI App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = AmberDim2)
                            ) {
                                showMenu = false
                                showDefaultAppDialog = true
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink750),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Default UPI app",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = if (!defaultName.isNullOrEmpty()) defaultName!! else "None (Always Ask)",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    HorizontalDivider(color = BorderLine, thickness = 0.8.dp)

                    // About & Legal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = AmberDim2)
                            ) {
                                showMenu = false
                                onPrivacyPolicyClick?.invoke()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink750),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AmberSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "About & Legal",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = BorderLine, thickness = 0.8.dp)

                    // Quick auto-pay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material3.ripple(color = AmberDim2)
                            ) {
                                val next = !isQuickPay
                                isQuickPay = next
                                UpiPreferenceManager.setQuickPayEnabled(context, next)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink750),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AmberSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "Quick auto-pay",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isQuickPay) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(CyanAccent)
                                    )
                                    Text(
                                        "Active on scan",
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        "Disabled",
                                        color = TextTertiary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }





                    // Premium Brand Footer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Gold accent divider line
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(1.5.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(
                                                androidx.compose.ui.graphics.Color.Transparent,
                                                AmberDim2,
                                                AmberSoft,
                                                AmberDim2,
                                                androidx.compose.ui.graphics.Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = androidx.compose.ui.text.buildAnnotatedString {
                                    withStyle(
                                        style = androidx.compose.ui.text.SpanStyle(
                                            color = TextTertiary,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Normal,
                                            letterSpacing = 2.sp
                                        )
                                    ) { append("BUILT BY ") }
                                    withStyle(
                                        style = androidx.compose.ui.text.SpanStyle(
                                            color = AmberSoft,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.5.sp
                                        )
                                    ) { append("KRISHNA") }
                                },
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // BOTTOM CONTROLS — REDESIGNED INK & AMBER
        // ==========================================
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 18.dp, end = 18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Ink850.copy(alpha = 0.92f))
                .border(1.dp, BorderLineStrong, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash toggle
            RedesignedScannerCtrl(
                icon = if (flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                label = "Flash",
                isOn = flashOn,
                onClick = { onFlashToggle() }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(BorderLineStrong)
            )

            // Gallery picker
            RedesignedScannerCtrl(
                icon = Icons.Default.Image,
                label = "Gallery",
                isOn = false,
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(BorderLineStrong)
            )

            // Camera switch
            RedesignedScannerCtrl(
                icon = Icons.Default.Cameraswitch,
                label = "Flip",
                isOn = isFrontCamera,
                onClick = {
                    val newLens = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
                    onLensFacingChange(newLens)
                }
            )
        }
    }
}

@Composable
private fun RedesignedScannerCtrl(
    icon: ImageVector,
    label: String,
    isOn: Boolean,
    onClick: () -> Unit
) {
    val bgAnim by animateColorAsState(
        targetValue = if (isOn) AmberDim2 else Ink750,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CtrlBg"
    )
    val tintAnim by animateColorAsState(
        targetValue = if (isOn) AmberSoft else TextPrimary,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CtrlTint"
    )
    val textAnim by animateColorAsState(
        targetValue = if (isOn) AmberSoft else TextSecondary,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "CtrlText"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isOn) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "CtrlScale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .scale(iconScale)
                .clip(RoundedCornerShape(12.dp))
                .background(bgAnim),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintAnim,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textAnim
        )
    }
}

// ==========================================
// SCANNER CONTROL BUTTON
// ==========================================
@Composable
private fun ScannerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) activeColor.copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.08f)
                )
                .then(
                    if (isActive) Modifier.border(1.5.dp, activeColor.copy(alpha = 0.5f), CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) activeColor else Color.White.copy(alpha = 0.5f),
            letterSpacing = 0.5.sp
        )
    }
}

// ==========================================
// SCANNER OVERLAY WITH ANIMATED CORNERS + LASER (0-Recomposition Draw-Phase Pipeline)
// ==========================================
@Composable
private fun ScannerOverlay(
    cornerAlpha: () -> Float,
    laserProgress: () -> Float
) {
    val density = LocalDensity.current
    val scanAreaPadding = 48.dp
    val cornerLength = 30.dp
    val cornerStrokeWidth = 2.5.dp
    val cornerRadius = 10.dp

    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentCornerAlpha = cornerAlpha()
        val currentLaserProgress = laserProgress()

        val canvasW = size.width
        val canvasH = size.height
        val paddingPx = with(density) { scanAreaPadding.toPx() }
        val cornerLenPx = with(density) { cornerLength.toPx() }
        val strokePx = with(density) { cornerStrokeWidth.toPx() }
        val radiusPx = with(density) { cornerRadius.toPx() }

        // Scan area rectangle
        val scanLeft = paddingPx
        val scanRight = canvasW - paddingPx
        val scanTop = (canvasH - (scanRight - scanLeft)) / 2f
        val scanBottom = scanTop + (scanRight - scanLeft)

        // ========================================
        // 1. SEMI-TRANSPARENT OVERLAY (darken outside scan area)
        // ========================================
        val overlayColor = Color.Black.copy(alpha = 0.55f)

        // Top region
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, 0f),
            size = Size(canvasW, scanTop)
        )
        // Bottom region
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, scanBottom),
            size = Size(canvasW, canvasH - scanBottom)
        )
        // Left region
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, scanTop),
            size = Size(scanLeft, scanBottom - scanTop)
        )
        // Right region
        drawRect(
            color = overlayColor,
            topLeft = Offset(scanRight, scanTop),
            size = Size(canvasW - scanRight, scanBottom - scanTop)
        )

        // ========================================
        // 2. ANIMATED ROUNDED CORNER BRACKETS (Amber Accent)
        // ========================================
        val accentColor = AmberPrimary.copy(alpha = currentCornerAlpha)
        val glowColor = AmberDim2.copy(alpha = currentCornerAlpha * 0.4f)
        val strokeStyle = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
        val glowStyle = Stroke(
            width = strokePx * 2.5f,
            cap = StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )

        // 1. Top-Left Corner
        val tlPath = Path().apply {
            moveTo(scanLeft + cornerLenPx, scanTop)
            lineTo(scanLeft + radiusPx, scanTop)
            quadraticTo(scanLeft, scanTop, scanLeft, scanTop + radiusPx)
            lineTo(scanLeft, scanTop + cornerLenPx)
        }
        drawPath(path = tlPath, color = glowColor, style = glowStyle)
        drawPath(path = tlPath, color = accentColor, style = strokeStyle)

        // 2. Top-Right Corner
        val trPath = Path().apply {
            moveTo(scanRight - cornerLenPx, scanTop)
            lineTo(scanRight - radiusPx, scanTop)
            quadraticTo(scanRight, scanTop, scanRight, scanTop + radiusPx)
            lineTo(scanRight, scanTop + cornerLenPx)
        }
        drawPath(path = trPath, color = glowColor, style = glowStyle)
        drawPath(path = trPath, color = accentColor, style = strokeStyle)

        // 3. Bottom-Left Corner
        val blPath = Path().apply {
            moveTo(scanLeft + cornerLenPx, scanBottom)
            lineTo(scanLeft + radiusPx, scanBottom)
            quadraticTo(scanLeft, scanBottom, scanLeft, scanBottom - radiusPx)
            lineTo(scanLeft, scanBottom - cornerLenPx)
        }
        drawPath(path = blPath, color = glowColor, style = glowStyle)
        drawPath(path = blPath, color = accentColor, style = strokeStyle)

        // 4. Bottom-Right Corner
        val brPath = Path().apply {
            moveTo(scanRight - cornerLenPx, scanBottom)
            lineTo(scanRight - radiusPx, scanBottom)
            quadraticTo(scanRight, scanBottom, scanRight, scanBottom - radiusPx)
            lineTo(scanRight, scanBottom - cornerLenPx)
        }
        drawPath(path = brPath, color = glowColor, style = glowStyle)
        drawPath(path = brPath, color = accentColor, style = strokeStyle)

        // ========================================
        // 3. SCANNING LASER LINE (Cyan Glow Sweep)
        // ========================================
        val laserY = scanTop + (scanBottom - scanTop) * currentLaserProgress
        val laserPadding = strokePx * 2

        // Laser glow (wider, lower alpha)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    CyanDim2.copy(alpha = 0.2f),
                    CyanAccent.copy(alpha = 0.5f),
                    CyanDim2.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                startX = scanLeft + laserPadding,
                endX = scanRight - laserPadding
            ),
            start = Offset(scanLeft + laserPadding, laserY),
            end = Offset(scanRight - laserPadding, laserY),
            strokeWidth = 24f,
            cap = StrokeCap.Round
        )

        // Laser core line (thin, bright Cyan)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    CyanAccent.copy(alpha = 0.8f),
                    Color.White,
                    CyanAccent.copy(alpha = 0.8f),
                    Color.Transparent
                ),
                startX = scanLeft + laserPadding,
                endX = scanRight - laserPadding
            ),
            start = Offset(scanLeft + laserPadding, laserY),
            end = Offset(scanRight - laserPadding, laserY),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}
