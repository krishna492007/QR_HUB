package com.qr.hub.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Pixel-Perfect Vector Icons from qr-hub-light-mode.html Design System
 */
object HtmlIcons {

    // ── Bottom Nav & System Icons ──
    val GenerateNav: ImageVector by lazy {
        ImageVector.Builder(
            name = "GenerateNav",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // 4 Grid Rectangles
                moveTo(4.5f, 3f)
                lineTo(8.5f, 3f)
                lineTo(8.5f, 7f)
                lineTo(4.5f, 7f)
                close()

                moveTo(15.5f, 3f)
                lineTo(19.5f, 3f)
                lineTo(19.5f, 7f)
                lineTo(15.5f, 7f)
                close()

                moveTo(4.5f, 14f)
                lineTo(8.5f, 14f)
                lineTo(8.5f, 18f)
                lineTo(4.5f, 18f)
                close()

                moveTo(15.5f, 14f)
                lineTo(19.5f, 14f)
                lineTo(19.5f, 18f)
                lineTo(15.5f, 18f)
                close()
            }
        }.build()
    }

    val ScanNav: ImageVector by lazy {
        ImageVector.Builder(
            name = "ScanNav",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Brackets
                moveTo(3f, 7f); lineTo(3f, 4f); lineTo(6f, 4f)
                moveTo(21f, 7f); lineTo(21f, 4f); lineTo(18f, 4f)
                moveTo(3f, 17f); lineTo(3f, 20f); lineTo(6f, 20f)
                moveTo(21f, 17f); lineTo(21f, 20f); lineTo(18f, 20f)
                // Center Box
                moveTo(9f, 8f); lineTo(15f, 8f); lineTo(15f, 14f); lineTo(9f, 14f); close()
            }
        }.build()
    }

    val HistoryNav: ImageVector by lazy {
        ImageVector.Builder(
            name = "HistoryNav",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Outer circle
                moveTo(12f, 3f)
                arcTo(9f, 9f, 0f, true, true, 12f, 21f)
                arcTo(9f, 9f, 0f, true, true, 12f, 3f)
                // Clock hands
                moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15.5f, 14f)
            }
        }.build()
    }

    val SettingsNav: ImageVector by lazy {
        ImageVector.Builder(
            name = "SettingsNav",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Inner circle
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                moveTo(12f, 9f)
                arcTo(3f, 3f, 0f, true, true, 12f, 15f)
                arcTo(3f, 3f, 0f, true, true, 12f, 9f)
            }
            // Cog path
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.4f, 15f)
                curveTo(19.7f, 15.6f, 19.8f, 16.3f, 19.73f, 16.82f)
                lineTo(19.79f, 16.88f)
                curveTo(20.57f, 17.66f, 20.57f, 18.93f, 19.79f, 19.71f)
                curveTo(19.01f, 20.49f, 17.74f, 20.49f, 16.96f, 19.71f)
                lineTo(16.9f, 19.65f)
                curveTo(16.4f, 19.5f, 15.6f, 19.7f, 15.1f, 20f)
                lineTo(15f, 21f)
                lineTo(9f, 21f)
                lineTo(8.9f, 20f)
                curveTo(8.4f, 19.7f, 7.6f, 19.5f, 7.1f, 19.65f)
                lineTo(7.04f, 19.71f)
                curveTo(6.26f, 20.49f, 4.99f, 20.49f, 4.21f, 19.71f)
                curveTo(3.43f, 18.93f, 3.43f, 17.66f, 4.21f, 16.88f)
                lineTo(4.27f, 16.82f)
                curveTo(4.2f, 16.3f, 4.3f, 15.6f, 4.6f, 15f)
                lineTo(3f, 15f)
                lineTo(3f, 9f)
                lineTo(4.6f, 9f)
                curveTo(4.3f, 8.4f, 4.2f, 7.7f, 4.27f, 7.18f)
                lineTo(4.21f, 7.12f)
                curveTo(3.43f, 6.34f, 3.43f, 5.07f, 4.21f, 4.29f)
                curveTo(4.99f, 3.51f, 6.26f, 3.51f, 7.04f, 4.29f)
                lineTo(7.1f, 4.35f)
                curveTo(7.6f, 4.5f, 8.4f, 4.3f, 8.9f, 4f)
                lineTo(9f, 3f)
                lineTo(15f, 3f)
                lineTo(15.1f, 4f)
                curveTo(15.6f, 4.3f, 16.4f, 4.5f, 16.9f, 4.35f)
                lineTo(16.96f, 4.29f)
                curveTo(17.74f, 3.51f, 19.01f, 3.51f, 19.79f, 4.29f)
                curveTo(20.57f, 5.07f, 20.57f, 6.34f, 19.79f, 7.12f)
                lineTo(19.73f, 7.18f)
                curveTo(19.8f, 7.7f, 19.7f, 8.4f, 19.4f, 9f)
                lineTo(21f, 9f)
                lineTo(21f, 15f)
                close()
            }
        }.build()
    }

    // ── Generate Types Icons ──
    val TextQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "TextQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(4f, 6f); lineTo(20f, 6f)
                moveTo(4f, 12f); lineTo(14f, 12f)
                moveTo(4f, 18f); lineTo(17f, 18f)
            }
        }.build()
    }

    val UrlQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "UrlQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 15f); lineTo(15f, 9f)
                moveTo(13f, 6f); lineTo(14f, 5f)
                curveTo(15.6f, 3.4f, 18.2f, 3.4f, 19.8f, 5f)
                curveTo(21.4f, 6.6f, 21.4f, 9.2f, 19.8f, 10.8f)
                lineTo(18.8f, 11.8f)
                moveTo(11f, 18f); lineTo(10f, 19f)
                curveTo(8.4f, 20.6f, 5.8f, 20.6f, 4.2f, 19f)
                curveTo(2.6f, 17.4f, 2.6f, 14.8f, 4.2f, 13.2f)
                lineTo(5.2f, 12.2f)
            }
        }.build()
    }

    val UpiQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "UpiQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 21f); lineTo(21f, 21f)
                moveTo(4f, 21f); lineTo(4f, 10f); lineTo(12f, 4f); lineTo(20f, 10f); lineTo(20f, 21f)
                moveTo(9f, 21f); lineTo(9f, 15f); lineTo(15f, 15f); lineTo(15f, 21f)
            }
        }.build()
    }

    val WhatsAppQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "WhatsAppQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21f, 11.5f)
                curveTo(21f, 16.14f, 17.24f, 19.9f, 12.6f, 19.9f)
                curveTo(11.2f, 19.9f, 9.8f, 19.55f, 8.6f, 18.9f)
                lineTo(3f, 20f)
                lineTo(4.2f, 14.5f)
                curveTo(3.45f, 13.3f, 3f, 11.9f, 3f, 10.5f)
                curveTo(3f, 5.86f, 6.76f, 2f, 11.6f, 2f)
                curveTo(16.4f, 2f, 21f, 6.7f, 21f, 11.5f)
                close()
                moveTo(8.5f, 9.5f)
                curveTo(8.8f, 12f, 11f, 14.2f, 13.5f, 14.5f)
            }
        }.build()
    }

    val WAGroupQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "WAGroupQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 6f); arcTo(3f, 3f, 0f, true, true, 9f, 12f); arcTo(3f, 3f, 0f, true, true, 9f, 6f)
                moveTo(17f, 7.6f); arcTo(2.4f, 2.4f, 0f, true, true, 17f, 12.4f); arcTo(2.4f, 2.4f, 0f, true, true, 17f, 7.6f)
                moveTo(3.5f, 20f); curveTo(4f, 16.8f, 6.3f, 15f, 9f, 15f); curveTo(11.7f, 15f, 14f, 16.8f, 14.5f, 20f)
                moveTo(15f, 15.3f); curveTo(17.2f, 15.5f, 19f, 17f, 19.4f, 19.7f)
            }
        }.build()
    }

    val PhoneQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "PhoneQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 3f); lineTo(9f, 3f); lineTo(11f, 8f); lineTo(8.5f, 9.5f)
                curveTo(9.5f, 12f, 12f, 14.5f, 14.5f, 15.5f)
                lineTo(16f, 13f); lineTo(21f, 15f); lineTo(21f, 18f)
                curveTo(21f, 19.1f, 20.1f, 20f, 18.8f, 20f)
                curveTo(10.2f, 20f, 4f, 13.8f, 4f, 5.2f)
                curveTo(4f, 3.9f, 4.9f, 3f, 6f, 3f)
                close()
            }
        }.build()
    }

    val SmsQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "SmsQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 4f); lineTo(20f, 4f); lineTo(20f, 16f); lineTo(8f, 16f); lineTo(4f, 20f); close()
                moveTo(8f, 9f); lineTo(16f, 9f)
                moveTo(8f, 12f); lineTo(13f, 12f)
            }
        }.build()
    }

    val EmailQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "EmailQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close()
                moveTo(3f, 7f); lineTo(12f, 13f); lineTo(21f, 7f)
            }
        }.build()
    }

    val ContactQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "ContactQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 4.5f)
                arcTo(3.5f, 3.5f, 0f, true, true, 12f, 11.5f)
                arcTo(3.5f, 3.5f, 0f, true, true, 12f, 4.5f)
                moveTo(5f, 20f)
                curveTo(6f, 16.5f, 8.8f, 14.5f, 12f, 14.5f)
                curveTo(15.2f, 14.5f, 18f, 16.5f, 19f, 20f)
            }
        }.build()
    }

    val WiFiQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "WiFiQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2f, 9f); curveTo(7.5f, 4f, 16.5f, 4f, 22f, 9f)
                moveTo(5.5f, 12.5f); curveTo(9.5f, 9f, 14.5f, 9f, 18.5f, 12.5f)
                moveTo(9f, 16f); curveTo(10.8f, 14.5f, 13.2f, 14.5f, 15f, 16f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 18.5f)
                arcTo(1.2f, 1.2f, 0f, true, true, 12f, 20.9f)
                arcTo(1.2f, 1.2f, 0f, true, true, 12f, 18.5f)
            }
        }.build()
    }

    val LocationQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "LocationQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 21f); curveTo(12f, 21f, 19f, 14.5f, 19f, 9.5f); arcTo(7f, 7f, 0f, true, false, 5f, 9.5f); curveTo(5f, 14.5f, 12f, 21f, 12f, 21f); close()
                moveTo(12f, 7.2f); arcTo(2.3f, 2.3f, 0f, true, true, 12f, 11.8f); arcTo(2.3f, 2.3f, 0f, true, true, 12f, 7.2f)
            }
        }.build()
    }

    val EventQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "EventQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.5f, 5f); lineTo(20.5f, 5f); lineTo(20.5f, 20.5f); lineTo(3.5f, 20.5f); close()
                moveTo(3.5f, 9.5f); lineTo(20.5f, 9.5f)
                moveTo(8f, 3f); lineTo(8f, 6.5f)
                moveTo(16f, 3f); lineTo(16f, 6.5f)
            }
        }.build()
    }

    val BulkQr: ImageVector by lazy {
        ImageVector.Builder(
            name = "BulkQr",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 6f); lineTo(20f, 6f); lineTo(20f, 10f); lineTo(4f, 10f); close()
                moveTo(4f, 13f); lineTo(20f, 13f); lineTo(20f, 17f); lineTo(4f, 17f); close()
                moveTo(7f, 20f); lineTo(17f, 20f)
            }
        }.build()
    }

    val BarcodeIcon: ImageVector by lazy {
        ImageVector.Builder(
            name = "BarcodeIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 5f); lineTo(3f, 19f)
                moveTo(6f, 5f); lineTo(6f, 19f)
                moveTo(8f, 5f); lineTo(8f, 19f)
                moveTo(12f, 5f); lineTo(12f, 19f)
                moveTo(15f, 5f); lineTo(15f, 19f)
                moveTo(17f, 5f); lineTo(17f, 19f)
                moveTo(21f, 5f); lineTo(21f, 19f)
            }
        }.build()
    }

    fun getTypeIcon(type: String): ImageVector = when (type.uppercase().trim()) {
        "URL", "QRURL", "HTTP", "HTTPS" -> UrlQr
        "PHONE", "TEL" -> PhoneQr
        "SMS", "SMSTO" -> SmsQr
        "EMAIL", "QREMAIL", "MAILTO" -> EmailQr
        "WIFI" -> WiFiQr
        "WHATSAPP" -> WhatsAppQr
        "WA_GROUP", "WAGROUP" -> WAGroupQr
        "TEXT", "PLAIN" -> TextQr
        "LOCATION", "PLUS_CODE", "GOOGLE_MAPS", "GEO" -> LocationQr
        "UPI", "PAYMENT" -> UpiQr
        "CONTACT", "VCARD", "MECARD" -> ContactQr
        "EVENT", "VEVENT", "CALENDAR" -> EventQr
        "BULK", "BULK_QR" -> BulkQr
        "BARCODE", "EAN_13", "UPC_A", "CODE_128", "QR_CODE" -> BarcodeIcon
        else -> TextQr
    }
}
