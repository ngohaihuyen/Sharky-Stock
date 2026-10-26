package com.example.sharkystocks.mvi

import com.example.sharkystocks.StockChartState
import com.example.sharkystocks.StockListState
import com.example.sharkystocks.data.model.ChartRange
import com.example.sharkystocks.data.model.Stock

data class StockState(
    val selectedStock: Stock? = null,
    val error: Exception? = null,
    val stockListState: StockListState = StockListState.Loading,
    val stocks: List<Stock>? = null,
    val chartState: StockChartState = StockChartState.Idle,
    val chartRange: ChartRange = ChartRange.ONE_MONTH
)