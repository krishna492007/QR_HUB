package com.qr.hub.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * High-Efficiency Offline Compression for Ultra-Long QR Codes (1,000 - 2,000+ Words)
 */
object QRCompressor {

    const val GZIP_PREFIX = "QRHUB_GZ:"

    /**
     * Compress plain text into Base64 GZIP string with signature prefix
     */
    fun compress(rawText: String): String {
        return try {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).use { gzip ->
                gzip.write(rawText.toByteArray(StandardCharsets.UTF_8))
            }
            val compressedBytes = bos.toByteArray()
            val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            "$GZIP_PREFIX$base64"
        } catch (e: Exception) {
            rawText
        }
    }

    /**
     * Check if string is a compressed QR payload
     */
    fun isCompressed(text: String): Boolean {
        return text.startsWith(GZIP_PREFIX) || text.startsWith("QRHUB_GZIP:") || text.startsWith("GZIP:")
    }

    /**
     * Decompress Base64 GZIP string back to original plain text
     */
    fun decompress(compressedPayload: String): String? {
        return try {
            val base64Data = when {
                compressedPayload.startsWith(GZIP_PREFIX) -> compressedPayload.removePrefix(GZIP_PREFIX)
                compressedPayload.startsWith("QRHUB_GZIP:") -> compressedPayload.removePrefix("QRHUB_GZIP:")
                compressedPayload.startsWith("GZIP:") -> compressedPayload.removePrefix("GZIP:")
                else -> compressedPayload
            }

            val compressedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bis = ByteArrayInputStream(compressedBytes)
            GZIPInputStream(bis).use { gzip ->
                val buffer = ByteArray(1024)
                val out = ByteArrayOutputStream()
                var len: Int
                while (gzip.read(buffer).also { len = it } > 0) {
                    out.write(buffer, 0, len)
                }
                out.toString(StandardCharsets.UTF_8.name())
            }
        } catch (e: Exception) {
            null
        }
    }
}
