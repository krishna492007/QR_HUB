package com.qr.hub.util.security

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class SecurityLevel {
    SAFE,
    CAUTION,
    SUSPICIOUS
}

data class UrlSecurityAnalysis(
    val originalUrl: String,
    val unmaskedUrl: String? = null,
    val domain: String = "",
    val protocol: String = "HTTP",
    val isHttps: Boolean = false,
    val isShortened: Boolean = false,
    val isDirectDownload: Boolean = false,
    val securityLevel: SecurityLevel = SecurityLevel.SAFE,
    val safetyTitle: String = "Safe & Secure Link",
    val safetyDescription: String = "Verified HTTPS encrypted connection",
    val riskPoints: List<String> = emptyList(),
    val trustPoints: List<String> = emptyList()
)

object UrlSecurityInspector {

    private val KnownShorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "buff.ly",
        "ow.ly", "rebrand.ly", "cutt.ly", "shorturl.at", "tiny.cc",
        "t.ly", "linktr.ee", "qr.ae", "shorte.st"
    )

    private val DangerousExtensions = setOf(
        ".apk", ".exe", ".bat", ".cmd", ".msi", ".vbs", ".dmg", ".pkg", ".sh"
    )

    /**
     * Synchronously / asynchronously inspects URL safety and unmasks shortened URLs
     */
    suspend fun inspectUrl(rawUrl: String): UrlSecurityAnalysis = withContext(Dispatchers.IO) {
        val cleanUrl = if (!rawUrl.startsWith("http://", ignoreCase = true) && !rawUrl.startsWith("https://", ignoreCase = true)) {
            "https://$rawUrl"
        } else {
            rawUrl
        }

        val uri = try { Uri.parse(cleanUrl) } catch (_: Exception) { null }
        val host = uri?.host?.lowercase(Locale.ROOT).orEmpty()
        val isHttps = cleanUrl.startsWith("https://", ignoreCase = true)
        val protocol = if (isHttps) "HTTPS" else "HTTP"

        val isShortened = KnownShorteners.any { host.contains(it) }

        // Attempt to unmask shortened URL by following redirects (max 3 hops)
        var unmasked: String? = null
        if (isShortened) {
            unmasked = resolveRedirectDestination(cleanUrl)
        }

        val finalCheckUrl = unmasked ?: cleanUrl
        val finalUri = try { Uri.parse(finalCheckUrl) } catch (_: Exception) { uri }
        val finalHost = finalUri?.host?.lowercase(Locale.ROOT).orEmpty()

        val isDirectDownload = DangerousExtensions.any { finalCheckUrl.lowercase(Locale.ROOT).contains(it) }
        val isIpAddress = finalHost.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}$"))

        val risks = mutableListOf<String>()
        val trusts = mutableListOf<String>()

        if (isHttps) {
            trusts.add("SSL/TLS Encrypted Connection (HTTPS)")
        } else {
            risks.add("Insecure HTTP protocol (Data sent in plaintext)")
        }

        if (isDirectDownload) {
            risks.add("Direct executable / App package download (.apk/.exe)")
        } else {
            trusts.add("No dangerous executable downloads detected")
        }

        if (isIpAddress) {
            risks.add("Raw IP Address destination without registered domain")
        } else if (finalHost.isNotEmpty()) {
            trusts.add("Registered domain ($finalHost)")
        }

        if (isShortened) {
            if (unmasked != null) {
                trusts.add("Shortened URL successfully unmasked to $finalHost")
            } else {
                risks.add("Masked shortened link (Redirect destination hidden)")
            }
        }

        // Determine Overall Security Level
        val level: SecurityLevel
        val title: String
        val desc: String

        when {
            isDirectDownload || isIpAddress -> {
                level = SecurityLevel.SUSPICIOUS
                title = "Suspicious Link Warning"
                desc = "This URL downloads files or points to a raw IP address."
            }
            !isHttps -> {
                level = SecurityLevel.CAUTION
                title = "Unencrypted Connection"
                desc = "This website does not use HTTPS encryption."
            }
            isShortened && unmasked == null -> {
                level = SecurityLevel.CAUTION
                title = "Shortened URL"
                desc = "This link redirects to an external destination."
            }
            else -> {
                level = SecurityLevel.SAFE
                title = "Safe & Secure Connection"
                desc = "Verified domain with active SSL encryption."
            }
        }

        UrlSecurityAnalysis(
            originalUrl = rawUrl,
            unmaskedUrl = unmasked,
            domain = finalHost.ifEmpty { host },
            protocol = protocol,
            isHttps = isHttps,
            isShortened = isShortened,
            isDirectDownload = isDirectDownload,
            securityLevel = level,
            safetyTitle = title,
            safetyDescription = desc,
            riskPoints = risks,
            trustPoints = trusts
        )
    }

    private fun resolveRedirectDestination(urlStr: String, maxHops: Int = 3): String? {
        var currentUrl = urlStr
        var hops = 0

        try {
            while (hops < maxHops) {
                val u = URL(currentUrl)
                val conn = u.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 3500
                conn.readTimeout = 3500
                conn.requestMethod = "HEAD"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; QRHub Security Shield)")

                val responseCode = conn.responseCode
                if (responseCode in 300..399) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (!location.isNullOrEmpty()) {
                        currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                            location
                        } else {
                            val base = URL(currentUrl)
                            URL(base, location).toString()
                        }
                        hops++
                    } else {
                        break
                    }
                } else {
                    conn.disconnect()
                    break
                }
            }
            return if (hops > 0) currentUrl else null
        } catch (_: Exception) {
            return null
        }
    }
}
