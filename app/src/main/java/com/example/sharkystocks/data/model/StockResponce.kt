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
    val volume: Double,
    val date: String? = null // e.g. "2025-10-17T00:00:00+0000", used by the chart
)