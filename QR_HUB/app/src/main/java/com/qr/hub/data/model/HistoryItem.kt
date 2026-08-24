package com.qr.hub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawValue: String,
    val type: String, // "TEXT", "URL", "UPI", "PHONE", "SMS", "EMAIL", "WIFI", "WHATSAPP", "LOCATION", "CONTACT", "QR_CODE"
    val category: String, // For filtering: same as type but could be more granular
    val timestamp: Long = System.currentTimeMillis(),
    val isScanned: Boolean = true, // true = scanned, false = generated
    val isFavorite: Boolean = false,
    val title: String = "", // Optional title for generated QR codes
    val extraData: String = "" // JSON string for extra data if needed
) {
    fun getFormattedDate(): String {
        val date = Date(timestamp)
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000} min ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            diff < 604_800_000 -> "${diff / 86_400_000} days ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                sdf.format(date)
            }
        }
    }
}

enum class QRType {
    TEXT,
    URL,
    UPI,
    PHONE,
    SMS,
    EMAIL,
    WIFI,
    WHATSAPP,
    LOCATION,
    CONTACT,
    VCARD,
    PLUS_CODE,
    GOOGLE_MAPS,
    UNKNOWN
}
