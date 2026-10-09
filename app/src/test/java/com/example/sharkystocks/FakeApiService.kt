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
}
