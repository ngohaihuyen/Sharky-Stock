package com.example.sharkystocks

import com.example.sharkystocks.data.model.ChartPoint

sealed class StockChartState {
    object Idle : StockChartState()
    object Loading : StockChartState()
    data class Success(val symbol: String, val points: List<ChartPoint>) : StockChartState()
    data class Error(val message: String) : StockChartState()
    object Empty : StockChartState()
}
