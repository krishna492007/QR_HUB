package com.qr.hub.util

import com.qr.hub.model.ScannedQR
import java.net.URLDecoder

private val PLUS_CODE_REGEX = Regex("(?i)[A-Z0-9]{4,}\\+[A-Z0-9]{2,}") // plus ke baad minimum 2 chars

// Helper function to detect domain-like patterns (e.g., google.com, example.co.in)
private fun isLikelyDomain(raw: String): Boolean {
    if (raw.contains(" ") || !raw.contains(".")) return false
    if (raw.startsWith(".") || raw.endsWith(".")) return false
    if (raw.contains("://")) return false // Already handled by HTTP/HTTPS check

    val parts = raw.split(".")
    if (parts.size < 2) return false
    if (parts.first().isEmpty()) return false

    // Check TLD length (2-10 chars for valid TLDs like .com, .org, .museum)
    val tld = parts.last()
    if (tld.length !in 2..10) return false

    // Make sure it doesn't look like a file name (e.g., "file.txt")
    // A domain should have at least 2 chars before the last dot
    val domainPart = parts.dropLast(1).joinToString(".")
    if (domainPart.length < 2) return false

    return true
}

fun detectType(value: String): ScannedQR {
    val raw = value.trim()
    return when {
        raw.lowercase().startsWith("mailto:") -> {
            val after = raw.removePrefix("mailto:").removePrefix("MAILTO:").removePrefix("Mailto:")
            val address = after.takeWhile { it != '?' && it != '&' }
            val query = after.substringAfter("?", "")
            ScannedQR.QREmail(
                address = address,
                subject = queryParam(query, "subject"),
                body = queryParam(query, "body")
            )
        }

        raw.lowercase().startsWith("tel:") -> {
            ScannedQR.Phone(raw.substring(4))
        }

        raw.lowercase().startsWith("sms:") || raw.lowercase().startsWith("smsto:") -> {
            val prefix = if (raw.lowercase().startsWith("smsto:")) 6 else 4
            val after = raw.substring(prefix)
            val number = when {
                after.contains(":") -> after.substringBefore(":")
                after.contains("?") -> after.substringBefore("?")
                else -> after
            }
            val message = when {
                after.contains("?") -> {
                    val query = after.substringAfter("?")
                    queryParam(query, "body")
                }
                after.contains(":") -> URLDecoder.decode(after.substringAfter(":"), "UTF-8")
                else -> ""
            }
            ScannedQR.SMS(number, message)
        }

        raw.lowercase().startsWith("geo:") -> {
            val after = raw.substring(4)
            val coordsPart = after.substringBefore("?")
            val query = after.substringAfter("?", "")
            val coords = coordsPart.takeWhile { it != '?' && it != '&' }
            val parts = coords.split(",")
            val label = queryParam(query, "q")
            val zoom = queryParam(query, "z")
            if (parts.size >= 2) {
                ScannedQR.Location(
                    latitude = parts[0].trim().toDoubleOrNull() ?: 0.0,
                    longitude = parts[1].trim().toDoubleOrNull() ?: 0.0,
                    label = label,
                    zoom = zoom
                )
            } else ScannedQR.Unknown(raw)
        }

        !raw.contains("://") && PLUS_CODE_REGEX.containsMatchIn(raw) -> {
            parsePlusCode(raw)
        }

        raw.startsWith("WIFI:") -> {
            parseWiFi(raw)
        }

        raw.lowercase().startsWith("begin:vcard") -> {
            parseVCard(raw)
        }

        raw.lowercase().startsWith("begin:vevent") -> {
            parseEvent(raw)
        }

        raw.lowercase().startsWith("upi://") || raw.lowercase().startsWith("upi:") -> {
            parseUPI(raw)
        }

        raw.contains("pa=") && raw.contains("@") -> {
            parseUPI(raw)
        }

        // Detect plain UPI ID (e.g. 6232659514-1@okbizaxis, user@paytm, name@ybl)
        raw.matches(Regex("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z0-9.\\-_]{2,64}$")) && !raw.contains(" ") && !raw.contains("://") -> {
            parseUPI("upi://pay?pa=$raw&cu=INR")
        }

        raw.contains("wa.me/") || raw.contains("wa.link/") || raw.contains("api.whatsapp.com")
            || raw.contains("chat.whatsapp.com") || raw.lowercase().startsWith("whatsapp://") -> {
            parseWhatsApp(raw)
        }

        // Plus codes: standalone (no ://) OR plus.codes URLs (case-insensitive)
        raw.lowercase().contains("plus.codes") || (PLUS_CODE_REGEX.containsMatchIn(raw) && !raw.contains("://")) -> {
            parsePlusCode(raw)
        }

        raw.lowercase().startsWith("http://") || raw.lowercase().startsWith("https://") -> {
            val lower = raw.lowercase()
            if (lower.contains("maps.app.goo.gl") || lower.contains("goo.gl/maps")) {
                ScannedQR.GoogleMaps(raw)
            } else {
                ScannedQR.QRURL(raw)
            }
        }

        // Detect URLs without protocol: www.domain.com or domain.com patterns
        raw.lowercase().startsWith("www.") -> {
            ScannedQR.QRURL(raw)
        }

        // Detect Compressed Ultra-Long QR Payloads
        QRCompressor.isCompressed(raw) -> {
            val decompressed = QRCompressor.decompress(raw) ?: raw
            // Re-detect on decompressed content in case it's a URL or plain text
            detectType(decompressed)
        }

        else -> ScannedQR.Text(raw)
    }
}

private fun queryParam(query: String, key: String): String {
    val match = Regex("(?:^|&)${Regex.escape(key)}=([^&]*)").find(query)
    return match?.groupValues?.getOrNull(1)?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
}

private fun parseWiFi(raw: String): ScannedQR {
    val ssid = Regex("S:([^;]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
    val pass = Regex("P:([^;]*)").find(raw)?.groupValues?.getOrNull(1) ?: ""
    val enc = Regex("T:([^;]*)").find(raw)?.groupValues?.getOrNull(1) ?: "None"
    return ScannedQR.WiFi(ssid, pass, if (enc.isEmpty()) "None" else enc)
}

private fun parseVCard(raw: String): ScannedQR {
    fun extract(key: String) = keyRegex(key).find(raw)?.groupValues?.getOrNull(1) ?: ""
    return ScannedQR.Contact(
        vCard = raw,
        name = extract("FN") ?: extract("N")?.let {
            it.split(";").filter { s -> s.isNotBlank() }.joinToString(" ").trim()
        } ?: "",
        phone = extract("TEL").substringAfterLast(":").trim(),
        email = extract("EMAIL"),
        org = extract("ORG"),
        title = extract("TITLE")
    )
}

private fun parseEvent(raw: String): ScannedQR {
    fun extract(key: String) = keyRegex(key).find(raw)?.groupValues?.getOrNull(1) ?: ""
    return ScannedQR.Event(
        raw = raw,
        summary = extract("SUMMARY"),
        location = extract("LOCATION"),
        description = extract("DESCRIPTION"),
        startDate = extract("DTSTART"),
        endDate = extract("DTEND")
    )
}

private fun cleanVCardPhone(value: String): String {
    if (value.isEmpty()) return ""
    return Regex("(?:;?\\w+=\\w+)+", RegexOption.IGNORE_CASE)
        .replace(value, "")
        .replace(";", "")
        .replace("^\\s*:\\s*".toRegex(), "")
        .trim()
}

private fun keyRegex(key: String) = Regex("${Regex.escape(key)}[:;]([^\\n]*)")

private fun parseUPI(raw: String): ScannedQR {
    // Extract query part after '?' and before any '#'
    val queryPart = raw.substringAfter("?", raw)
    val query = queryPart.substringBefore("#")
    val params = mutableMapOf<String, String>()

    query.split("&").forEach { pair ->
        val parts = pair.split("=", limit = 2)
        if (parts.size == 2) {
            val key = parts[0].lowercase()
            val value = parts[1]
            try {
                params[key] = URLDecoder.decode(value, "UTF-8")
            } catch (e: Exception) {
                params[key] = value
            }
        }
    }

    return ScannedQR.UPI(
        vpa = params["pa"] ?: "",
        name = params["pn"] ?: "",
        amount = params["am"] ?: "",
        note = params["tn"] ?: "",
        currency = params["cu"] ?: "INR",
        rawUri = raw  // Preserve original UPI URI for payment intent
    )
}

private fun parseWhatsApp(raw: String): ScannedQR {
    val lower = raw.lowercase()

    val phone: String
    val groupId: String
    var qrLinkUrl: String? = null

    when {
        lower.startsWith("whatsapp://") -> {
            val after = raw.substringAfter("whatsapp://")
            when {
                after.startsWith("send") -> {
                    phone = Regex("[?&]phone=([^&]*)", RegexOption.IGNORE_CASE).find(raw)
                        ?.groupValues?.getOrNull(1) ?: ""
                    groupId = ""
                }
                after.startsWith("chat") -> {
                    phone = Regex("[?&]phone=([^&]*)", RegexOption.IGNORE_CASE).find(raw)
                        ?.groupValues?.getOrNull(1) ?: ""
                    groupId = ""
                }
                else -> {
                    phone = Regex("phone=([^&]*)", RegexOption.IGNORE_CASE).find(raw)
                        ?.groupValues?.getOrNull(1) ?: ""
                    groupId = ""
                }
            }
        }
        lower.contains("wa.me/") -> {
            val idx = lower.indexOf("wa.me/")
            val after = raw.substring(idx + 6)
            if (after.startsWith("qr/", true)) {
                phone = ""
                qrLinkUrl = raw // store full URL to open in browser
            } else if (after.startsWith("business", true)) {
                phone = ""
            } else {
                phone = after.takeWhile { it != '/' && it != '?' && it != '&' }
            }
            groupId = ""
        }
        lower.contains("wa.link/") -> {
            val idx = lower.indexOf("wa.link/")
            phone = raw.substring(idx + 7).takeWhile { it != '/' && it != '?' && it != '&' }
            groupId = ""
        }
        lower.contains("chat.whatsapp.com") -> {
            val idx = lower.indexOf("chat.whatsapp.com/")
            phone = ""
            groupId = raw.substring(idx + 18).split(Regex("[?#]")).first()
        }
        lower.contains("api.whatsapp.com") -> {
            phone = Regex("[?&]phone=([^&]*)", RegexOption.IGNORE_CASE).find(raw)
                ?.groupValues?.getOrNull(1) ?: ""
            groupId = ""
        }
        else -> {
            phone = ""
            groupId = ""
        }
    }

    val message = Regex("[?&]text=([^&]*)", RegexOption.IGNORE_CASE).find(raw)
        ?.groupValues?.getOrNull(1)
        ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""

    return ScannedQR.WhatsApp(phone, message, groupId, qrLinkUrl)
}

private fun parsePlusCode(raw: String): ScannedQR {
    val match = PLUS_CODE_REGEX.find(raw) ?: return ScannedQR.Unknown(raw)
    val code = match.value
    val label = if (raw.contains("://")) {
        // plus.codes URL: extract label from query param 'q'
        val queryPart = raw.substringAfter("?", "")
        val query = queryPart.substringBefore("#")
        queryParam(query, "q") ?: ""
    } else {
        // Plain text: label after code (remove leading comma/space)
        raw.substringAfter(code, "").trim().removePrefix(",").trim()
    }
    return ScannedQR.PlusCode(code, label)
}
