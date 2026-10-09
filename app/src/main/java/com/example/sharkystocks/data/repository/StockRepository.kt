package com.example.sharkystocks.data.repository

import android.content.Context
import androidx.room.Room
import com.example.sharkystocks.data.local.StockDatabase
import com.example.sharkystocks.data.local.StockDetailEntity
import com.example.sharkystocks.data.local.StockListEntity
import com.example.sharkystocks.data.network.ApiService
import com.example.sharkystocks.data.network.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

open class StockRepository(
    private val context: Context? = null,
    private val api: ApiService = apiService
) {

    private val db = context?.let {
        Room.databaseBuilder(it, StockDatabase::class.java, "stock-database")
            .fallbackToDestructiveMigration()
            .build()
    }
    private val stockListDao = db?.stockListDao()
    private val stockDetailDao = db?.stockDetailDao()


    private var cachedSymbols: List<String>? = null
    private val cachedDetails = mutableMapOf<String, StockDetailEntity>()


    open suspend fun refreshHomeScreen(): List<String> {

        if (context == null) {
            return try {
                cachedSymbols ?: fetchFromNetworkAndCacheInMemory()
            } catch (e: Exception) {
                fetchFromNetworkAndCacheInMemory()
            }
        }

        return withContext(Dispatchers.IO) {
            try {
                val cachedList = stockListDao!!.getStockList().first()
                cachedList?.symbols?.split(",") ?: fetchFromNetworkAndCacheRoom()
            } catch (e: Exception) {
                fetchFromNetworkAndCacheRoom()
            }
        }
    }

    private suspend fun fetchFromNetworkAndCacheInMemory(): List<String> {
        val response = api.getStocks()
        val marketStocks = response.data

        val symbols = marketStocks.map { it.symbol }
        cachedSymbols = symbols

        marketStocks.forEach { ms ->
            val change = ms.close - ms.open
            val changePercent = if (ms.open != 0.0) (change / ms.open) * 100 else 0.0

            cachedDetails[ms.symbol] = StockDetailEntity(
                symbol = ms.symbol,
                price = ms.close,
                change = change,
                changePercent = changePercent,
                open = ms.open,
                high = ms.high,
                low = ms.low,
                volume = ms.volume
            )
        }

        return symbols
    }

    private suspend fun fetchFromNetworkAndCacheRoom(): List<String> = withContext(Dispatchers.IO) {
        val response = api.getStocks()
        val marketStocks = response.data

        val symbols = marketStocks.map { it.symbol }

        stockListDao!!.insertStockList(
            StockListEntity(symbols = symbols.joinToString(","))
        )

        val stockDetailEntities = marketStocks.map { ms ->
            val change = ms.close - ms.open
            val changePercent = if (ms.open != 0.0) (change / ms.open) * 100 else 0.0

            StockDetailEntity(
                symbol = ms.symbol,
                price = ms.close,
                change = change,
                changePercent = changePercent,
                open = ms.open,
                high = ms.high,
                low = ms.low,
                volume = ms.volume
            )
        }
        stockDetailDao!!.insertAllStockDetails(stockDetailEntities)

        symbols
    }


    open suspend fun refreshDetailsScreen(symbol: String): StockDetailEntity? {

        if (context == null) {
            return try {
                cachedDetails[symbol] ?: run {
                    fetchFromNetworkAndCacheInMemory()
                    cachedDetails[symbol]
                }
            } catch (e: Exception) {
                fetchFromNetworkAndCacheInMemory()
                cachedDetails[symbol]
            }
        }


        return withContext(Dispatchers.IO) {
            try {
                val cached = stockDetailDao!!.getStockDetail(symbol).first()
                cached ?: fetchStockDetailFromNetworkRoom(symbol)
            } catch (e: Exception) {
                fetchStockDetailFromNetworkRoom(symbol)
            }
        }
    }

    private suspend fun fetchStockDetailFromNetworkRoom(symbol: String): StockDetailEntity? = withContext(Dispatchers.IO) {
        val response = api.getStocks()
        val ms = response.data.find { it.symbol == symbol } ?: return@withContext null

        val change = ms.close - ms.open
        val changePercent = if (ms.open != 0.0) (change / ms.open) * 100 else 0.0

        val detail = StockDetailEntity(
            symbol = ms.symbol,
            price = ms.close,
            change = change,
            changePercent = changePercent,
            open = ms.open,
            high = ms.high,
            low = ms.low,
            volume = ms.volume
        )

        stockDetailDao!!.insertStockDetail(detail)
        detail
    }


    open suspend fun getCachedStockDetails(symbols: List<String>): List<StockDetailEntity> {

        if (context == null) {
            return symbols.mapNotNull { cachedDetails[it] }
        }


        return withContext(Dispatchers.IO) {
            try {
                stockDetailDao!!.getStockDetails(symbols)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
