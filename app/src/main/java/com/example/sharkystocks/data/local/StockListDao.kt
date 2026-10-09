package com.example.sharkystocks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockListDao {
    @Query("SELECT * FROM stock_list WHERE id = 1")
    fun getStockList(): Flow<StockListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockList(stockList: StockListEntity)

}