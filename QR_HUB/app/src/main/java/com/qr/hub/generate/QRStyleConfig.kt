package com.qr.hub.generate

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Dot/Module shapes for QR Code
 */
enum class QRModuleShape(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCLE("Dots"),
    DIAMOND("Diamond")
}

/**
 * Corner Eye (Finder Pattern) shapes
 */
enum class QREyeShape(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCULAR("Circle")
}

/**
 * Center Logo Badge shapes
 */
enum class QRLogoShape(val displayName: String) {
    ROUNDED_SQUIRCLE("Squircle"),
    CIRCLE("Circle"),
    SQUARE("Square")
}

/**
 * Gradient Type for QR Foreground
 */
enum class QRGradientType {
    NONE,
    LINEAR_HORIZONTAL,
    LINEAR_VERTICAL,
    LINEAR_DIAGONAL,
    RADIAL
}

/**
 * Frame / Border banner style
 */
enum class QRFrameStyle(val displayName: String) {
    NONE("None"),
    BOTTOM_BADGE("Scan Me Badge"),
    PAYMENT_BADGE("Scan & Pay"),
    CARD_BORDER("Modern Card")
}

/**
 * Predefined Branded Preset Styles
 */
enum class QRPreset(
    val title: String,
    val subtitle: String,
    val previewFg: Int,
    val previewBg: Int,
    val config: QRStyleConfig
) {
    CLASSIC_NOIR(
        title = "Noir Classic",
        subtitle = "Crisp Black & White",
        previewFg = Color.BLACK,
        previewBg = Color.WHITE,
        config = QRStyleConfig(
            moduleShape = QRModuleShape.ROUNDED,
            eyeShape = QREyeShape.ROUNDED,
            logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
            fgColor = Color.BLACK,
            bgColor = Color.WHITE,
            gradientType = QRGradientType.NONE,
            frameStyle = QRFrameStyle.CARD_BORDER
        )
    ),
    ROYAL_GOLD(
        title = "Royal Amber",
        subtitle = "Luxury Gold Gradient",
        previewFg = 0xFFFFB300.toInt(),
        previewBg = 0xFF0D0A05.toInt(),
        config = QRStyleConfig(
            moduleShape = QRModuleShape.ROUNDED,
            eyeShape = QREyeShape.ROUNDED,
            logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
            fgColor = 0xFFFFC837.toInt(),
            fgGradientEnd = 0xFFFF8008.toInt(),
            bgColor = 0xFF0B0906.toInt(),
            gradientType = QRGradientType.LINEAR_DIAGONAL,
            frameStyle = QRFrameStyle.CARD_BORDER
        )
    ),
    CYBERPUNK_NEON(
        title = "Neon Cyber",
        subtitle = "Electric Cyan Glow",
        previewFg = 0xFF00F2FE.toInt(),
        previewBg = 0xFF050B14.toInt(),
        config = QRStyleConfig(
            moduleShape = QRModuleShape.CIRCLE,
            eyeShape = QREyeShape.CIRCULAR,
            logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
            fgColor = 0xFF00F2FE.toInt(),
            fgGradientEnd = 0xFF4FACFE.toInt(),
            bgColor = 0xFF050B14.toInt(),
            gradientType = QRGradientType.LINEAR_VERTICAL,
            frameStyle = QRFrameStyle.CARD_BORDER
        )
    ),
    EMERALD_PRO(
        title = "Emerald Mint",
        subtitle = "Deep Forest Green",
        previewFg = 0xFF10B981.toInt(),
        previewBg = 0xFF05160E.toInt(),
        config = QRStyleConfig(
            moduleShape = QRModuleShape.ROUNDED,
            eyeShape = QREyeShape.ROUNDED,
            logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
            fgColor = 0xFF34D399.toInt(),
            fgGradientEnd = 0xFF059669.toInt(),
            bgColor = 0xFF06140D.toInt(),
            gradientType = QRGradientType.LINEAR_DIAGONAL,
            frameStyle = QRFrameStyle.CARD_BORDER
        )
    ),
    DEEP_VELVET(
        title = "Velvet Purple",
        subtitle = "Cosmic Violet Tone",
        previewFg = 0xFFA855F7.toInt(),
        previewBg = 0xFF0F0616.toInt(),
        config = QRStyleConfig(
            moduleShape = QRModuleShape.ROUNDED,
            eyeShape = QREyeShape.ROUNDED,
            logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
            fgColor = 0xFFC084FC.toInt(),
            fgGradientEnd = 0xFF7C3AED.toInt(),
            bgColor = 0xFF0E0514.toInt(),
            gradientType = QRGradientType.LINEAR_DIAGONAL,
            frameStyle = QRFrameStyle.CARD_BORDER
        )
    ),
    CRIMSON_RUBY(
        title = "Crimson Ruby",
        subtitle = "Fiery Coral Red",
        previewFg = 0xFFFF416C.toInt(),
        previewBg = 0xFF140507.toInt(),
        config = QRStyleConfig(
            moduleShape = QRModuleShape.CIRCLE,
            eyeShape = QREyeShape.CIRCULAR,
            logoShape = QRLogoShape.ROUNDED_SQUIRCLE,
            fgColor = 0xFFFF4B2B.toInt(),
            fgGradientEnd = 0xFFFF416C.toInt(),
            bgColor = 0xFF140507.toInt(),
            gradientType = QRGradientType.LINEAR_HORIZONTAL,
            frameStyle = QRFrameStyle.CARD_BORDER
        )
    )
}

/**
 * Comprehensive Styling Configuration for QR Codes
 */
data class QRStyleConfig(
    val moduleShape: QRModuleShape = QRModuleShape.ROUNDED,
    val eyeShape: QREyeShape = QREyeShape.ROUNDED,
    val logoShape: QRLogoShape = QRLogoShape.ROUNDED_SQUIRCLE,
    val fgColor: Int = Color.BLACK,
    val fgGradientEnd: Int = Color.BLACK,
    val bgColor: Int = Color.WHITE,
    val gradientType: QRGradientType = QRGradientType.NONE,
    val frameStyle: QRFrameStyle = QRFrameStyle.CARD_BORDER,
    val frameText: String = "SCAN ME",
    val logoBitmap: Bitmap? = null,
    val logoTag: String = "app_logo" // "none", "app_logo", "upi", "whatsapp", "custom"
)
