package com.qr.hub.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraXPreview"

@Composable
fun CameraXPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    flashOn: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // remember the PreviewView so we can use it across composition
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val provider = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    // Get camera provider
    LaunchedEffect(Unit) {
        ProcessCameraProvider.getInstance(context).get().let { processCameraProvider ->
            provider.value = processCameraProvider
        }
    }

    // Bind camera use cases when previewView is ready
    LaunchedEffect(provider.value, previewView, lensFacing) {
        val p = provider.value ?: return@LaunchedEffect
        val pv = previewView ?: return@LaunchedEffect

        // Important: Unbind ALL use cases before switching cameras
        try {
            p.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding camera", e)
        }

        // Small delay to ensure camera is released before rebinding
        kotlinx.coroutines.delay(100)

        try {
            // Check if camera is available for this lens facing
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            // Verify camera exists (will throw if front camera not available)
            try {
                p.availableCameraInfos
            } catch (e: Exception) {
                Log.e(TAG, "Camera not available", e)
                return@LaunchedEffect
            }

            val previewUseCase = Preview.Builder().build().also {
                it.setSurfaceProvider(pv.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                BarcodeAnalyzer { value ->
                    onBarcodeDetected(value)
                }
            )

            camera = p.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase, imageAnalysis)
            camera?.cameraControl?.enableTorch(flashOn)
            Log.d(TAG, "Camera bound successfully with lensFacing=$lensFacing")
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding error", e)
        }
    }

    // Update torch mode when flashOn changes
    LaunchedEffect(flashOn) {
        camera?.cameraControl?.enableTorch(flashOn)
    }

    DisposableEffect(context, lifecycleOwner, lensFacing) {
        onDispose {
            try {
                provider.value?.unbindAll()
                camera = null
            } catch (_: Exception) { }
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }.also { previewView = it }
        },
        modifier = modifier.fillMaxSize()
    )
}

@SuppressLint("ViewConstructor")
private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

class BarcodeAnalyzer(
    private val onDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // For front camera, we need to handle the rotation correctly
        // The rotationDegrees already accounts for camera orientation
        val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
            mediaImage, imageProxy.imageInfo.rotationDegrees
        )

        barcodeDetector.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        onDetected(rawValue)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Barcode scanning error", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

private val barcodeDetector by lazy {
    com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
}
