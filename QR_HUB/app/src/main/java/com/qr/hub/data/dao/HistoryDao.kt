package com.qr.hub.data.dao

import androidx.room.*
import com.qr.hub.data.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryItem): Long

    @Delete
    suspend fun delete(item: HistoryItem)

    @Query("DELETE FROM history_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE isScanned = 1 ORDER BY timestamp DESC")
    fun getAllScanned(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE isScanned = 0 ORDER BY timestamp DESC")
    fun getAllGenerated(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE rawValue LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<HistoryItem>>

    @Query("UPDATE history_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT * FROM history_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HistoryItem?

    @Query("DELETE FROM history_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM history_items")
    fun getCount(): Flow<Int>
}
