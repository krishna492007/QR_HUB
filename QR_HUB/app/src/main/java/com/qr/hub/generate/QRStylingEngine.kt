package com.qr.hub.generate

import android.graphics.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * High-Performance Canvas-based Custom QR Code Styling Engine
 */
object QRStylingEngine {

    /**
     * Render styled QR Bitmap with custom shapes, gradients, corner eyes, lower-layer center logo, and frames.
     */
    fun renderStyledQR(
        content: String,
        config: QRStyleConfig,
        baseSize: Int = 1024
    ): Bitmap {
        // 1. Encode QR Matrix with High Error Correction (30% recovery for logo & custom shapes)
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 0) // Zero ZXing margin, we control custom padding
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        }

        val bitMatrix = try {
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        } catch (e: Exception) {
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 25, 25, hints)
        }

        val matrixSize = bitMatrix.width
        val quietZoneModules = 2
        val totalModules = matrixSize + (quietZoneModules * 2)

        val qrRenderSize = baseSize.toFloat()
        val moduleSize = qrRenderSize / totalModules

        // Calculate Canvas Dimensions (Extra height if bottom frame banner is active)
        val hasBottomFrame = config.frameStyle == QRFrameStyle.BOTTOM_BADGE || config.frameStyle == QRFrameStyle.PAYMENT_BADGE
        val hasCardBorder = config.frameStyle == QRFrameStyle.CARD_BORDER

        val bannerHeight = if (hasBottomFrame) (qrRenderSize * 0.18f) else 0f
        val cardBorderPadding = if (hasCardBorder) (qrRenderSize * 0.04f) else 0f

        val bitmapWidth = (qrRenderSize + (cardBorderPadding * 2)).toInt()
        val bitmapHeight = (qrRenderSize + bannerHeight + (cardBorderPadding * 2)).toInt()

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // ── 2. LAYER 1: DRAW BACKGROUND ──
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.bgColor
            style = Paint.Style.FILL
        }

        if (hasCardBorder) {
            val cardRect = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
            val cornerRadius = qrRenderSize * 0.05f
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

            // Outer subtle border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = config.fgColor
                alpha = 40
                style = Paint.Style.STROKE
                strokeWidth = moduleSize * 0.6f
            }
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)
        } else {
            canvas.drawRect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat(), bgPaint)
        }

        // QR Drawing Offset
        val offsetX = cardBorderPadding + (quietZoneModules * moduleSize)
        val offsetY = cardBorderPadding + (quietZoneModules * moduleSize)

        // ── 3. LAYER 2: DRAW CENTER LOGO IN LOWER BASE LAYER ──
        config.logoBitmap?.let { logo ->
            drawCenterLogoInLowerLayer(canvas, offsetX, offsetY, matrixSize, moduleSize, logo, config.bgColor, config.fgColor)
        }

        // ── 4. SETUP FOREGROUND PAINT & SHADER ──
        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        when (config.gradientType) {
            QRGradientType.LINEAR_HORIZONTAL -> {
                fgPaint.shader = LinearGradient(
                    offsetX, offsetY,
                    offsetX + (matrixSize * moduleSize), offsetY,
                    config.fgColor, config.fgGradientEnd,
                    Shader.TileMode.CLAMP
                )
            }
            QRGradientType.LINEAR_VERTICAL -> {
                fgPaint.shader = LinearGradient(
                    offsetX, offsetY,
                    offsetX, offsetY + (matrixSize * moduleSize),
                    config.fgColor, config.fgGradientEnd,
                    Shader.TileMode.CLAMP
                )
            }
            QRGradientType.LINEAR_DIAGONAL -> {
                fgPaint.shader = LinearGradient(
                    offsetX, offsetY,
                    offsetX + (matrixSize * moduleSize), offsetY + (matrixSize * moduleSize),
                    config.fgColor, config.fgGradientEnd,
                    Shader.TileMode.CLAMP
                )
            }
            QRGradientType.RADIAL -> {
                val cx = offsetX + (matrixSize * moduleSize / 2f)
                val cy = offsetY + (matrixSize * moduleSize / 2f)
                val radius = matrixSize * moduleSize * 0.7f
                fgPaint.shader = RadialGradient(
                    cx, cy, radius,
                    config.fgColor, config.fgGradientEnd,
                    Shader.TileMode.CLAMP
                )
            }
            QRGradientType.NONE -> {
                fgPaint.color = config.fgColor
            }
        }

        // ── 5. LAYER 3: DRAW DATA MODULES OVER THE LOWER LAYER ──
        for (x in 0 until matrixSize) {
            for (y in 0 until matrixSize) {
                // Skip Finder Patterns (drawn separately with eye geometry)
                if (isFinderPattern(x, y, matrixSize)) continue

                if (bitMatrix[x, y]) {
                    val left = offsetX + (x * moduleSize)
                    val top = offsetY + (y * moduleSize)
                    val right = left + moduleSize
                    val bottom = top + moduleSize

                    drawDataModule(canvas, left, top, right, bottom, moduleSize, config.moduleShape, fgPaint)
                }
            }
        }

        // ── 6. LAYER 4: DRAW THE 3 FINDER PATTERN CORNER EYES ──
        drawFinderEye(canvas, offsetX, offsetY, moduleSize, config.eyeShape, fgPaint, bgPaint)
        drawFinderEye(canvas, offsetX + ((matrixSize - 7) * moduleSize), offsetY, moduleSize, config.eyeShape, fgPaint, bgPaint)
        drawFinderEye(canvas, offsetX, offsetY + ((matrixSize - 7) * moduleSize), moduleSize, config.eyeShape, fgPaint, bgPaint)

        // ── 7. LAYER 5: DRAW BOTTOM FRAME ("SCAN ME" / "SCAN & PAY") ──
        if (hasBottomFrame) {
            val text = when (config.frameStyle) {
                QRFrameStyle.PAYMENT_BADGE -> config.frameText.ifEmpty { "SCAN & PAY" }
                else -> config.frameText.ifEmpty { "SCAN ME" }
            }
            drawBottomFrameBadge(canvas, bitmapWidth.toFloat(), bitmapHeight.toFloat(), bannerHeight, text, config.fgColor, config.bgColor)
        }

        return bitmap
    }

    /**
     * Check if module (x,y) belongs to one of the 3 7x7 corner finder patterns
     */
    private fun isFinderPattern(x: Int, y: Int, matrixSize: Int): Boolean {
        val inTopLeft = x < 7 && y < 7
        val inTopRight = x >= (matrixSize - 7) && y < 7
        val inBottomLeft = x < 7 && y >= (matrixSize - 7)
        return inTopLeft || inTopRight || inBottomLeft
    }

    /**
     * Draw individual data module based on chosen shape
     */
    private fun drawDataModule(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        moduleSize: Float,
        shape: QRModuleShape,
        paint: Paint
    ) {
        when (shape) {
            QRModuleShape.SQUARE -> {
                canvas.drawRect(left, top, right, bottom, paint)
            }
            QRModuleShape.ROUNDED -> {
                val rect = RectF(left + 0.5f, top + 0.5f, right - 0.5f, bottom - 0.5f)
                val radius = moduleSize * 0.35f
                canvas.drawRoundRect(rect, radius, radius, paint)
            }
            QRModuleShape.CIRCLE -> {
                val cx = left + (moduleSize / 2f)
                val cy = top + (moduleSize / 2f)
                val radius = moduleSize * 0.44f
                canvas.drawCircle(cx, cy, radius, paint)
            }
            QRModuleShape.DIAMOND -> {
                val cx = left + (moduleSize / 2f)
                val cy = top + (moduleSize / 2f)
                val path = Path().apply {
                    moveTo(cx, top + 0.5f)
                    lineTo(right - 0.5f, cy)
                    lineTo(cx, bottom - 0.5f)
                    lineTo(left + 0.5f, cy)
                    close()
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    /**
     * Draw 7x7 Finder Pattern Corner Eye with custom outer & inner geometry
     */
    private fun drawFinderEye(
        canvas: Canvas,
        left: Float,
        top: Float,
        moduleSize: Float,
        eyeShape: QREyeShape,
        fgPaint: Paint,
        bgPaint: Paint
    ) {
        val outerSize = 7 * moduleSize
        val middleSize = 5 * moduleSize
        val innerSize = 3 * moduleSize

        val middleOffset = 1 * moduleSize
        val innerOffset = 2 * moduleSize

        when (eyeShape) {
            QREyeShape.SQUARE -> {
                // 7x7 outer square
                canvas.drawRect(left, top, left + outerSize, top + outerSize, fgPaint)
                // 5x5 inner cutout
                canvas.drawRect(left + middleOffset, top + middleOffset, left + middleOffset + middleSize, top + middleOffset + middleSize, bgPaint)
                // 3x3 center eyeball
                canvas.drawRect(left + innerOffset, top + innerOffset, left + innerOffset + innerSize, top + innerOffset + innerSize, fgPaint)
            }
            QREyeShape.ROUNDED -> {
                val outerRadius = moduleSize * 1.8f
                val middleRadius = moduleSize * 1.2f
                val innerRadius = moduleSize * 0.8f

                // Outer 7x7 rounded rect
                val outerRect = RectF(left, top, left + outerSize, top + outerSize)
                canvas.drawRoundRect(outerRect, outerRadius, outerRadius, fgPaint)

                // Middle 5x5 rounded cutout
                val middleRect = RectF(left + middleOffset, top + middleOffset, left + middleOffset + middleSize, top + middleOffset + middleSize)
                canvas.drawRoundRect(middleRect, middleRadius, middleRadius, bgPaint)

                // Inner 3x3 rounded eyeball
                val innerRect = RectF(left + innerOffset, top + innerOffset, left + innerOffset + innerSize, top + innerOffset + innerSize)
                canvas.drawRoundRect(innerRect, innerRadius, innerRadius, fgPaint)
            }
            QREyeShape.CIRCULAR -> {
                val cx = left + (outerSize / 2f)
                val cy = top + (outerSize / 2f)

                // Outer circle
                canvas.drawCircle(cx, cy, outerSize / 2f, fgPaint)
                // Middle circle cutout
                canvas.drawCircle(cx, cy, middleSize / 2f, bgPaint)
                // Inner center eyeball
                canvas.drawCircle(cx, cy, innerSize / 2f, fgPaint)
            }
        }
    }

    /**
     * Draw Center Logo in Lower/Background Layer (beneath the QR modules)
     */
    private fun drawCenterLogoInLowerLayer(
        canvas: Canvas,
        offsetX: Float,
        offsetY: Float,
        matrixSize: Int,
        moduleSize: Float,
        logo: Bitmap,
        bgColor: Int,
        fgColor: Int
    ) {
        val qrTotalPx = matrixSize * moduleSize
        val cx = offsetX + (qrTotalPx / 2f)
        val cy = offsetY + (qrTotalPx / 2f)

        // Prominent 30% logo card size in lower layer
        val logoSize = qrTotalPx * 0.30f
        val logoRect = RectF(cx - (logoSize / 2f), cy - (logoSize / 2f), cx + (logoSize / 2f), cy + (logoSize / 2f))
        val cornerRadius = logoSize * 0.24f

        // 1. Lower Layer Background Card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(logoRect, cornerRadius, cornerRadius, cardPaint)

        // 2. Draw Logo in lower layer with rounded clip
        canvas.save()
        val clipPath = Path().apply {
            addRoundRect(logoRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        val srcRect = Rect(0, 0, logo.width, logo.height)
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        canvas.drawBitmap(logo, srcRect, logoRect, logoPaint)
        canvas.restore()

        // 3. Crisp Border Outline in lower layer
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            alpha = 90
            style = Paint.Style.STROKE
            strokeWidth = moduleSize * 0.4f
        }
        canvas.drawRoundRect(logoRect, cornerRadius, cornerRadius, borderPaint)
    }

    /**
     * Draw Bottom Pill Badge Banner ("SCAN ME" / "SCAN & PAY")
     */
    private fun drawBottomFrameBadge(
        canvas: Canvas,
        bitmapWidth: Float,
        bitmapHeight: Float,
        bannerHeight: Float,
        text: String,
        fgColor: Int,
        bgColor: Int
    ) {
        val pillWidth = (bitmapWidth * 0.72f).coerceAtLeast(280f)
        val pillHeight = bannerHeight * 0.65f
        val pillLeft = (bitmapWidth - pillWidth) / 2f
        val pillTop = bitmapHeight - bannerHeight + ((bannerHeight - pillHeight) / 2f) - 4f
        val pillRight = pillLeft + pillWidth
        val pillBottom = pillTop + pillHeight
        val pillRadius = pillHeight / 2f

        val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)

        // Pill background
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillPaint)

        // Text Paint
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            textSize = pillHeight * 0.46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }

        // Vertical Centering for text
        val fontMetrics = textPaint.fontMetrics
        val textY = pillTop + (pillHeight / 2f) - ((fontMetrics.ascent + fontMetrics.descent) / 2f)
        val textX = bitmapWidth / 2f

        canvas.drawText(text, textX, textY, textPaint)
    }
}
