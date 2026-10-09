package com.example.sharkystocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_details")
data class StockDetailEntity(
    @PrimaryKey
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)