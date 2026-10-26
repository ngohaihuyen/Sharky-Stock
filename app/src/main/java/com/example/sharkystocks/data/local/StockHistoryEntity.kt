package com.example.sharkystocks.data.local

import androidx.room.Entity

@Entity(
    tableName = "stock_history",
    primaryKeys = ["symbol", "date"]
)
data class StockHistoryEntity(
    val symbol: String,
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
