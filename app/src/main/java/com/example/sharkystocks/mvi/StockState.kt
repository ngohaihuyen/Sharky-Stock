package com.example.sharkystocks.mvi

import com.example.sharkystocks.StockListState
import com.example.sharkystocks.data.model.Stock

data class StockState(
    val selectedStock: Stock? = null,
    val error: Exception? = null,
    val stockListState: StockListState = StockListState.Loading,
    val stocks: List<Stock>? = null
)