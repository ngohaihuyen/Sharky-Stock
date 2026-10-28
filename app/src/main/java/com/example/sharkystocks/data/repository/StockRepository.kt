package com.example.sharkystocks.data.repository

import android.content.Context
import androidx.room.Room
import com.example.sharkystocks.data.local.StockDatabase
import com.example.sharkystocks.data.local.StockDetailEntity
import com.example.sharkystocks.data.local.StockHistoryEntity
import com.example.sharkystocks.data.local.StockListEntity
import com.example.sharkystocks.data.model.ChartRange
import com.example.sharkystocks.data.model.MarketstackStock
import com.example.sharkystocks.data.network.ApiService
import com.example.sharkystocks.data.network.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

open class StockRepository(
    private val context: Context? = null,
    private val api: ApiService = apiService
) {

    private fun formatApiDate(calendar: Calendar): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(calendar.time)
    }

    private fun getHistoryDateRange(): Pair<String, String> {
        val to = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val from = to.clone() as Calendar
        from.add(Calendar.DAY_OF_YEAR, -14)

        return formatApiDate(from) to formatApiDate(to)
    }

    private val db = context?.let {
        Room.databaseBuilder(it, StockDatabase::class.java, "stock-database")
            .fallbackToDestructiveMigration()
            .build()
    }
    private val stockListDao = db?.stockListDao()
    private val stockDetailDao = db?.stockDetailDao()
    private val stockHistoryDao = db?.stockHistoryDao()


    private var cachedSymbols: List<String>? = null
    private val cachedDetails = mutableMapOf<String, StockDetailEntity>()
    private val cachedHistory = mutableMapOf<String, List<StockHistoryEntity>>()


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


    // ---------------- Chart (price history) ----------------

    open suspend fun refreshChartScreen(
        symbol: String,
        forceRefresh: Boolean = false
    ): List<StockHistoryEntity> {

        if (context == null) {
            val cached = cachedHistory[symbol]
            if (cached != null && !forceRefresh) return cached

            return try {
                fetchHistoryFromNetworkAndCacheInMemory(symbol)
            } catch (e: Exception) {
                cached ?: throw e
            }
        }

        return withContext(Dispatchers.IO) {
            val cached = try {
                stockHistoryDao!!.getStockHistory(symbol)
            } catch (e: Exception) {
                emptyList()
            }

            val isFresh = cached.isNotEmpty() &&
                    System.currentTimeMillis() - cached.maxOf { it.lastUpdated } < HISTORY_CACHE_DURATION_MS

            if (isFresh && !forceRefresh) {
                cached
            } else {
                try {
                    fetchHistoryFromNetworkAndCacheRoom(symbol)
                } catch (e: Exception) {
                    // Offline / rate-limited: fall back to old data if we have any
                    if (cached.isNotEmpty()) cached else throw e
                }
            }
        }
    }

    open suspend fun refreshMiniChartScreen(
        symbol: String,
        forceRefresh: Boolean = false
    ): List<StockHistoryEntity> {
        return refreshChartScreen(
            symbol = symbol,
            forceRefresh = forceRefresh
        )
            .sortedBy { it.date }
            .takeLast(MINI_CHART_POINTS)
    }
    private suspend fun fetchHistoryFromNetworkAndCacheInMemory(symbol: String): List<StockHistoryEntity> {

        val (dateFrom, dateTo) = getHistoryDateRange()

        val response = api.getStockHistory(
            symbols = symbol,
            dateFrom = dateFrom,
            dateTo = dateTo,
            sort = "ASC",
            limit = 100
        )

        val history = response.data.toHistoryEntities(symbol)

        cachedHistory[symbol] = history
        return history
    }

    private suspend fun fetchHistoryFromNetworkAndCacheRoom(symbol: String): List<StockHistoryEntity> = withContext(Dispatchers.IO) {

        val (dateFrom, dateTo) = getHistoryDateRange()

        val response = api.getStockHistory(
            symbols = symbol,
            dateFrom = dateFrom,
            dateTo = dateTo,
            sort = "ASC",
            limit = 100
        )

        val history = response.data.toHistoryEntities(symbol)

        if (history.isNotEmpty()) {
            stockHistoryDao!!.deleteStockHistory(symbol)
            stockHistoryDao!!.insertStockHistory(history)
        }

        history
    }

    private fun List<MarketstackStock>.toHistoryEntities(symbol: String): List<StockHistoryEntity> {
        return this
            .filter { it.symbol == symbol && !it.date.isNullOrBlank() }
            .map { ms ->
                StockHistoryEntity(
                    symbol = ms.symbol,
                    date = ms.date!!.take(10), // "2025-10-17T00:00:00+0000" -> "2025-10-17"
                    open = ms.open,
                    high = ms.high,
                    low = ms.low,
                    close = ms.close,
                    volume = ms.volume
                )
            }
            .distinctBy { it.date }
            .sortedBy { it.date }
    }

    companion object {
        // EOD data only changes once a day -> re-use cached history for 6 hours
        private const val HISTORY_CACHE_DURATION_MS = 6 * 60 * 60 * 1000L

        // Number of historical points displayed in each mini chart.
        private const val MINI_CHART_POINTS = 7
    }
}
