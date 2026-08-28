package com.qr.hub.generate

import android.graphics.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.abs

/**
 * High-Performance Canvas-based Custom QR Code Styling Engine
 */
object QRStylingEngine {

    /**
     * Render styled QR Bitmap with custom shapes, gradients, corner eyes, sleek square/squircle/circle logo badge, and frames.
     * Includes Adaptive Error Correction fallback (H -> Q -> M -> L) to encode large text seamlessly without crashes.
     */
    fun renderStyledQR(
        content: String,
        config: QRStyleConfig,
        baseSize: Int = 1024
    ): Bitmap {
        if (content.isEmpty()) {
            return Bitmap.createBitmap(baseSize, baseSize, Bitmap.Config.ARGB_8888)
        }

        // 1. Adaptive Error Correction Fallback: Try H (30% recovery) -> Q (25%) -> M (15%) -> L (7%)
        val eccLevels = listOf(
            ErrorCorrectionLevel.H,
            ErrorCorrectionLevel.Q,
            ErrorCorrectionLevel.M,
            ErrorCorrectionLevel.L
        )

        var bitMatrix: com.google.zxing.common.BitMatrix? = null
        var usedEccLevel = ErrorCorrectionLevel.H

        for (ecc in eccLevels) {
            try {
                val hints = hashMapOf<EncodeHintType, Any>().apply {
                    put(EncodeHintType.CHARACTER_SET, "UTF-8")
                    put(EncodeHintType.MARGIN, 0) // Zero ZXing margin, we control custom padding
                    put(EncodeHintType.ERROR_CORRECTION, ecc)
                }
                bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
                usedEccLevel = ecc
                break
            } catch (e: Exception) {
                // Try next lower ECC level to fit more data
                continue
            }
        }

        // Final fallback if data exceeds standard: truncate or generate with L at fixed size
        if (bitMatrix == null) {
            val safeContent = if (content.length > 2800) content.take(2800) else content
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 0)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
            }
            bitMatrix = try {
                QRCodeWriter().encode(safeContent, BarcodeFormat.QR_CODE, 0, 0, hints)
            } catch (e: Exception) {
                QRCodeWriter().encode(safeContent.take(1000), BarcodeFormat.QR_CODE, 25, 25, hints)
            }
            usedEccLevel = ErrorCorrectionLevel.L
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

        // ── 2. DRAW BACKGROUND ──
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

        // ── 3. SETUP FOREGROUND PAINT & SHADER ──
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

        // Center Logo Cutout Calculation (Matches exact squircle/circle curvature & scales safely for high-density QRs)
        val hasLogo = config.logoBitmap != null
        val matrixCenter = matrixSize / 2f
        val logoScaleRatio = when {
            usedEccLevel == ErrorCorrectionLevel.L -> 0.12f
            usedEccLevel == ErrorCorrectionLevel.M -> 0.16f
            matrixSize > 65 -> 0.18f
            else -> 0.22f
        }
        val logoSpanModules = if (hasLogo) (matrixSize * logoScaleRatio).toInt().coerceAtLeast(3) else 0
        val halfSpanM = logoSpanModules / 2f
        val cornerRadiusM = halfSpanM * 0.28f
        val circleCutoutRadius = logoSpanModules * 0.60f

        // ── 4. DRAW DATA MODULES ──
        for (x in 0 until matrixSize) {
            for (y in 0 until matrixSize) {
                // Skip Finder Patterns (drawn separately with eye geometry)
                if (isFinderPattern(x, y, matrixSize)) continue

                // Skip Center Logo Cutout matching the exact shape boundary
                if (hasLogo) {
                    when (config.logoShape) {
                        QRLogoShape.CIRCLE -> {
                            val distSq = (x + 0.5f - matrixCenter) * (x + 0.5f - matrixCenter) +
                                         (y + 0.5f - matrixCenter) * (y + 0.5f - matrixCenter)
                            if (distSq < (circleCutoutRadius * circleCutoutRadius)) continue
                        }
                        QRLogoShape.ROUNDED_SQUIRCLE -> {
                            val dx = abs(x + 0.5f - matrixCenter)
                            val dy = abs(y + 0.5f - matrixCenter)
                            if (dx <= halfSpanM && dy <= halfSpanM) {
                                if (dx > (halfSpanM - cornerRadiusM) && dy > (halfSpanM - cornerRadiusM)) {
                                    val cdx = dx - (halfSpanM - cornerRadiusM)
                                    val cdy = dy - (halfSpanM - cornerRadiusM)
                                    if ((cdx * cdx + cdy * cdy) <= (cornerRadiusM * cornerRadiusM)) continue
                                } else {
                                    continue
                                }
                            }
                        }
                        QRLogoShape.SQUARE -> {
                            val dx = abs(x + 0.5f - matrixCenter)
                            val dy = abs(y + 0.5f - matrixCenter)
                            if (dx <= halfSpanM && dy <= halfSpanM) continue
                        }
                    }
                }

                if (bitMatrix[x, y]) {
                    val left = offsetX + (x * moduleSize)
                    val top = offsetY + (y * moduleSize)
                    val right = left + moduleSize
                    val bottom = top + moduleSize

                    drawDataModule(canvas, left, top, right, bottom, moduleSize, config.moduleShape, fgPaint)
                }
            }
        }

        // ── 5. DRAW THE 3 FINDER PATTERN CORNER EYES ──
        drawFinderEye(canvas, offsetX, offsetY, moduleSize, config.eyeShape, fgPaint, bgPaint)
        drawFinderEye(canvas, offsetX + ((matrixSize - 7) * moduleSize), offsetY, moduleSize, config.eyeShape, fgPaint, bgPaint)
        drawFinderEye(canvas, offsetX, offsetY + ((matrixSize - 7) * moduleSize), moduleSize, config.eyeShape, fgPaint, bgPaint)

        // ── 6. DRAW SEAMLESS CENTER LOGO BADGE (WITH THIN SLEEK BORDER) ──
        config.logoBitmap?.let { logo ->
            drawSeamlessCenterLogo(
                canvas = canvas,
                offsetX = offsetX,
                offsetY = offsetY,
                matrixSize = matrixSize,
                moduleSize = moduleSize,
                logoSpanModules = logoSpanModules,
                logoShape = config.logoShape,
                logo = logo,
                bgColor = config.bgColor,
                fgColor = config.fgColor
            )
        }

        // ── 7. DRAW BOTTOM FRAME ("SCAN ME" / "SCAN & PAY") ──
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
     * Draw Seamless Center Logo Badge (with Thin Fine Border and Flush Edge Fitting)
     */
    private fun drawSeamlessCenterLogo(
        canvas: Canvas,
        offsetX: Float,
        offsetY: Float,
        matrixSize: Int,
        moduleSize: Float,
        logoSpanModules: Int,
        logoShape: QRLogoShape,
        logo: Bitmap,
        bgColor: Int,
        fgColor: Int
    ) {
        val qrTotalPx = matrixSize * moduleSize
        val cx = offsetX + (qrTotalPx / 2f)
        val cy = offsetY + (qrTotalPx / 2f)

        // Badge fills the exact cutout dimensions
        val badgeSize = (logoSpanModules * moduleSize * 0.98f).coerceAtLeast(160f)
        val halfSize = badgeSize / 2f
        val badgeRect = RectF(cx - halfSize, cy - halfSize, cx + halfSize, cy + halfSize)
        val squircleRadius = badgeSize * 0.20f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        // Thin, elegant, subtle hairline border outline (not thick and heavy)
        val fineRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.STROKE
            strokeWidth = moduleSize * 0.14f // Fine thin hairline outline
            alpha = 170
        }

        val clipPath = Path()
        when (logoShape) {
            QRLogoShape.CIRCLE -> {
                canvas.drawCircle(cx, cy, halfSize, bgPaint)
                clipPath.addCircle(cx, cy, halfSize * 0.98f, Path.Direction.CW)
            }
            QRLogoShape.ROUNDED_SQUIRCLE -> {
                canvas.drawRoundRect(badgeRect, squircleRadius, squircleRadius, bgPaint)
                clipPath.addRoundRect(badgeRect, squircleRadius * 0.98f, squircleRadius * 0.98f, Path.Direction.CW)
            }
            QRLogoShape.SQUARE -> {
                canvas.drawRect(badgeRect, bgPaint)
                clipPath.addRect(badgeRect, Path.Direction.CW)
            }
        }

        // Draw Logo filling 96% of the badge with clean rounded clip
        canvas.save()
        canvas.clipPath(clipPath)

        val logoFitDiameter = badgeSize * 0.96f
        val dstRect = RectF(
            cx - (logoFitDiameter / 2f),
            cy - (logoFitDiameter / 2f),
            cx + (logoFitDiameter / 2f),
            cy + (logoFitDiameter / 2f)
        )
        val srcRect = Rect(0, 0, logo.width, logo.height)

        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        canvas.drawBitmap(logo, srcRect, dstRect, logoPaint)
        canvas.restore()

        // Draw Single Thin Crisp Outer Ring
        when (logoShape) {
            QRLogoShape.CIRCLE -> canvas.drawCircle(cx, cy, halfSize, fineRingPaint)
            QRLogoShape.ROUNDED_SQUIRCLE -> canvas.drawRoundRect(badgeRect, squircleRadius, squircleRadius, fineRingPaint)
            QRLogoShape.SQUARE -> canvas.drawRect(badgeRect, fineRingPaint)
        }
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
