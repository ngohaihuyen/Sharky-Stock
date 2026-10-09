package com.example.sharkystocks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [StockListEntity::class, StockDetailEntity::class],
    version = 2,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockListDao(): StockListDao
    abstract fun stockDetailDao(): StockDetailDao
}