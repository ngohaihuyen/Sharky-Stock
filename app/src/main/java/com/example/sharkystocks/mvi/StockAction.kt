package com.example.sharkystocks.mvi

import com.example.sharkystocks.data.model.ChartPoint
import com.example.sharkystocks.data.model.ChartRange
import com.example.sharkystocks.data.model.Stock

sealed class StockAction {
    data class SelectedStock(val stock: Stock?) : StockAction()
    object FetchListOfStock : StockAction()
    data class FetchSuccess(val stock: List<Stock>) : StockAction()
    data class FetchError(val error: String) : StockAction()

    // Chart
    object FetchChart : StockAction()
    data class FetchChartSuccess(val symbol: String, val points: List<ChartPoint>) : StockAction()
    data class FetchChartError(val error: String) : StockAction()
    data class SelectChartRange(val range: ChartRange) : StockAction()
}