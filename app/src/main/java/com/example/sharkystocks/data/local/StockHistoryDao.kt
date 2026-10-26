package com.example.sharkystocks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockHistoryDao {
    @Query("SELECT * FROM stock_history WHERE symbol = :symbol ORDER BY date ASC")
    suspend fun getStockHistory(symbol: String): List<StockHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(history: List<StockHistoryEntity>)

    @Query("DELETE FROM stock_history WHERE symbol = :symbol")
    suspend fun deleteStockHistory(symbol: String)
}
