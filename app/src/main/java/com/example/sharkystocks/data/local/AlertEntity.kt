package com.example.sharkystocks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val targetPrice: Double,
    val isAbove: Boolean, // true: báo khi giá >= targetPrice; false: báo khi giá <= targetPrice
    val isActive: Boolean = true, // Sau khi đã bắn thông báo thì đổi thành false để không spam
    val createdAt: Long = System.currentTimeMillis()
)
