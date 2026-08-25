package com.qr.hub.generate

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qr.hub.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Sleek 1:1 Square Image Cropper Dialog for Custom QR Center Logos
 */
@Composable
fun ImageCropDialog(
    sourceBitmap: Bitmap,
    onDismiss: () -> Unit,
    onCropApplied: (Bitmap) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotationDegrees by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6000000))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Ink900)
                    .border(1.dp, BorderLine, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Crop, null, tint = AmberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Crop Square Logo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextTertiary)
                    }
                }

                Text(
                    "Pinch to zoom and drag to center your logo in 1:1 square",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp)
                )

                // ── 1:1 SQUARE CROPPING VIEWPORT ──
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(2.dp, AmberPrimary, RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                                offset += pan
                            }
                        }
                        .drawWithContent {
                            drawContent()
                            // Rule of thirds grid lines
                            val thirdW = size.width / 3f
                            val thirdH = size.height / 3f
                            val gridColor = Color.White.copy(alpha = 0.25f)
                            drawLine(gridColor, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth = 1f)
                            drawLine(gridColor, Offset(thirdW * 2, 0f), Offset(thirdW * 2, size.height), strokeWidth = 1f)
                            drawLine(gridColor, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth = 1f)
                            drawLine(gridColor, Offset(0f, thirdH * 2), Offset(size.width, thirdH * 2), strokeWidth = 1f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sourceBitmap.asImageBitmap(),
                        contentDescription = "Source to crop",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                                rotationZ = rotationDegrees.toFloat()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Zoom & Rotate Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Zoom", fontSize = 12.5.sp, color = TextSecondary)
                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 0.8f..4f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AmberPrimary,
                            activeTrackColor = AmberPrimary,
                            inactiveTrackColor = Ink750
                        )
                    )

                    IconButton(
                        onClick = { rotationDegrees = (rotationDegrees + 90) % 360 },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Ink750)
                    ) {
                        Icon(Icons.Default.RotateRight, "Rotate", tint = AmberSoft, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Ink750)
                            .border(1.dp, BorderLine, RoundedCornerShape(12.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    // Apply Crop
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberCtaGradient)
                            .clickable {
                                val cropped = performCrop(
                                    source = sourceBitmap,
                                    scale = scale,
                                    offsetX = offset.x,
                                    offsetY = offset.y,
                                    rotationDegrees = rotationDegrees,
                                    viewportSizePx = 260f
                                )
                                onCropApplied(cropped)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF20140A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Crop", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF20140A))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Crop the bitmap into an exact 512x512 1:1 square output bitmap matching user's transformation
 */
private fun performCrop(
    source: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotationDegrees: Int,
    viewportSizePx: Float,
    targetOutputSize: Int = 512
): Bitmap {
    val outBitmap = Bitmap.createBitmap(targetOutputSize, targetOutputSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outBitmap)

    val matrix = Matrix()

    // 1. Initial fit of source bitmap to viewport
    val minDim = min(source.width, source.height).toFloat()
    val initialScale = viewportSizePx / minDim

    val srcCenterX = source.width / 2f
    val srcCenterY = source.height / 2f

    // Scale mapping from viewport (e.g. 260px) to output resolution (512px)
    val outRatio = targetOutputSize / viewportSizePx

    matrix.postTranslate(-srcCenterX, -srcCenterY)
    matrix.postRotate(rotationDegrees.toFloat())
    matrix.postScale(initialScale * scale * outRatio, initialScale * scale * outRatio)
    matrix.postTranslate((targetOutputSize / 2f) + (offsetX * outRatio), (targetOutputSize / 2f) + (offsetY * outRatio))

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        isDither = true
    }

    canvas.drawBitmap(source, matrix, paint)
    return outBitmap
}
