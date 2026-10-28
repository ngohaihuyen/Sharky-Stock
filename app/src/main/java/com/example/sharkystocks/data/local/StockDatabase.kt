package com.example.sharkystocks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [StockListEntity::class, StockDetailEntity::class, StockHistoryEntity::class, WatchlistEntity::class, AlertEntity::class],
    version = 4,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockListDao(): StockListDao
    abstract fun stockDetailDao(): StockDetailDao
    abstract fun stockHistoryDao(): StockHistoryDao
    abstract fun alertAndWatchlistDao(): AlertAndWatchlistDao
}