package com.qr.hub.model

sealed class ScannedQR {
    data class Text(val content: String) : ScannedQR()
    data class QRURL(val url: String) : ScannedQR()
    data class UPI(
        val vpa: String,
        val name: String,
        val amount: String,
        val note: String,
        val currency: String = "INR",
        val rawUri: String = "" // Original UPI URI (preserved for payment intent)
    ) : ScannedQR() {
        override fun toString(): String = rawUri.ifEmpty { "upi://pay?pa=$vpa&pn=$name&cu=$currency" }
    }
    data class Phone(val number: String) : ScannedQR()
    data class Contact(val vCard: String, val name: String, val phone: String, val email: String, val org: String = "", val title: String = "") : ScannedQR()
    data class SMS(val number: String, val message: String) : ScannedQR()
    data class QREmail(val address: String, val subject: String, val body: String) : ScannedQR()
    data class WiFi(val ssid: String, val password: String, val encryption: String) : ScannedQR()
    data class WhatsApp(
        val number: String,
        val message: String = "",
        val groupId: String = "",
        val qrLinkUrl: String? = null // for wa.me/qr/ links, store the full URL to open in browser
    ) : ScannedQR()
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val label: String = "",
        val zoom: String = ""
    ) : ScannedQR()
    data class Event(
        val raw: String,
        val summary: String = "",
        val location: String = "",
        val description: String = "",
        val startDate: String = "",
        val endDate: String = ""
    ) : ScannedQR()
    data class PlusCode(val code: String, val label: String = "") : ScannedQR()
    data class GoogleMaps(val url: String) : ScannedQR()
    data class Unknown(val raw: String) : ScannedQR()

    data class RawResult(
        val rawValue: String,
        val format: Int,
        val fromGallery: Boolean
    )
}
