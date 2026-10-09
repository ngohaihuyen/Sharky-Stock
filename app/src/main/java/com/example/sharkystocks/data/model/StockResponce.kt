package com.example.sharkystocks.data.model

data class StockResponse(
    val data: List<MarketstackStock>
)

data class MarketstackStock(
    val symbol: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)