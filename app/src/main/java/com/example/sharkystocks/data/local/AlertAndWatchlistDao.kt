package com.example.sharkystocks.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertAndWatchlistDao {
    // --- Watchlist ---
    @Query("SELECT symbol FROM watchlist")
    fun getAllWatchlistSymbols(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Delete
    suspend fun removeFromWatchlist(item: WatchlistEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    suspend fun isInWatchlist(symbol: String): Boolean

    // --- Price Alerts ---
    @Query("SELECT * FROM price_alerts WHERE isActive = 1")
    suspend fun getActiveAlerts(): List<AlertEntity>

    @Query("SELECT * FROM price_alerts WHERE symbol = :symbol")
    fun getAlertsForStock(symbol: String): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE price_alerts SET isActive = 0 WHERE id = :alertId")
    suspend fun markAlertTriggered(alertId: Int)

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)
}
