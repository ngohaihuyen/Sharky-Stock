package com.example.sharkystocks

sealed class StockListState {
    object Loading : StockListState()
    data class Success(val stocks: List<com.example.sharkystocks.data.model.Stock>) : StockListState()
    data class Error(val message: String) : StockListState()
    object Empty : StockListState()
}