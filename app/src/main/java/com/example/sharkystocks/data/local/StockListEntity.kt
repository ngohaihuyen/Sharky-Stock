package com.example.sharkystocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_list")
data class StockListEntity(
    @PrimaryKey
    val id: Int = 1,
    val symbols: String,
    val lastUpdated: Long = System.currentTimeMillis()
)