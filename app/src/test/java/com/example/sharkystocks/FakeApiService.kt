package com.example.sharkystocks

import com.example.sharkystocks.data.model.MarketstackStock
import com.example.sharkystocks.data.model.StockResponse
import com.example.sharkystocks.data.network.ApiService

class FakeApiService : ApiService {

    var stocks: List<MarketstackStock> = emptyList()
    var shouldThrowError: Boolean = false

    override suspend fun getStocks(accessKey: String, symbols: String): StockResponse {
        if (shouldThrowError) throw Exception("Fake API error")
        return StockResponse(data = stocks)
    }

    var history: List<MarketstackStock> = emptyList()
    var historyCallCount: Int = 0

    override suspend fun getStockHistory(
        accessKey: String,
        symbols: String,
        limit: Int,
        sort: String
    ): StockResponse {
        historyCallCount++
        if (shouldThrowError) throw Exception("Fake API error")
        return StockResponse(data = history.filter { it.symbol == symbols }.take(limit))
    }
}
