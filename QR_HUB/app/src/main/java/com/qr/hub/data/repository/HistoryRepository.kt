package com.qr.hub.data.repository

import android.content.Context
import com.qr.hub.data.dao.HistoryDao
import com.qr.hub.data.database.AppDatabase
import com.qr.hub.data.model.HistoryItem
import com.qr.hub.model.ScannedQR
import kotlinx.coroutines.flow.Flow

class HistoryRepository(context: Context) {
    private val historyDao: HistoryDao = AppDatabase.getDatabase(context).historyDao()

    val allHistory: Flow<List<HistoryItem>> = historyDao.getAll()
    val scannedHistory: Flow<List<HistoryItem>> = historyDao.getAllScanned()
    val generatedHistory: Flow<List<HistoryItem>> = historyDao.getAllGenerated()
    val favorites: Flow<List<HistoryItem>> = historyDao.getFavorites()

    suspend fun insert(item: HistoryItem): Long {
        return historyDao.insert(item)
    }

    suspend fun delete(item: HistoryItem) {
        historyDao.delete(item)
    }

    suspend fun deleteByIds(ids: List<Long>) {
        historyDao.deleteByIds(ids)
    }

    suspend fun toggleFavorite(id: Long, currentState: Boolean) {
        historyDao.updateFavorite(id, !currentState)
    }

    fun search(query: String): Flow<List<HistoryItem>> {
        return historyDao.search(query)
    }

    fun getByCategory(category: String): Flow<List<HistoryItem>> {
        return historyDao.getByCategory(category)
    }

    suspend fun getById(id: Long): HistoryItem? {
        return historyDao.getById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }

    // Helper to save scanned QR result
    suspend fun saveScan(rawValue: String, parsed: ScannedQR) {
        val type = when (parsed) {
            is ScannedQR.Text -> "TEXT"
            is ScannedQR.QRURL -> "URL"
            is ScannedQR.UPI -> "UPI"
            is ScannedQR.Phone -> "PHONE"
            is ScannedQR.Contact -> "CONTACT"
            is ScannedQR.SMS -> "SMS"
            is ScannedQR.QREmail -> "EMAIL"
            is ScannedQR.WiFi -> "WIFI"
            is ScannedQR.WhatsApp -> "WHATSAPP"
            is ScannedQR.Location -> "LOCATION"
            is ScannedQR.Event -> "EVENT"
            is ScannedQR.PlusCode -> "PLUS_CODE"
            is ScannedQR.GoogleMaps -> "GOOGLE_MAPS"
            is ScannedQR.Unknown -> "UNKNOWN"
            else -> "UNKNOWN"
        }

        val item = HistoryItem(
            rawValue = rawValue,
            type = type,
            category = type,
            isScanned = true,
            extraData = parsed.toString()
        )
        historyDao.insert(item)
    }

    // Helper to save generated QR
    suspend fun saveGenerate(rawValue: String, type: String, title: String = "") {
        val item = HistoryItem(
            rawValue = rawValue,
            type = type,
            category = type,
            isScanned = false,
            title = title
        )
        historyDao.insert(item)
    }
}
