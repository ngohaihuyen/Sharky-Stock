package com.example.sharkystocks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDetailDao {
    @Query("SELECT * FROM stock_details WHERE symbol = :symbol")
    fun getStockDetail(symbol: String): Flow<StockDetailEntity?>

    @Query("SELECT * FROM stock_details WHERE symbol IN (:symbols)")
    suspend fun getStockDetails(symbols: List<String>): List<StockDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockDetail(stockDetail: StockDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStockDetails(stockDetails: List<StockDetailEntity>)
}