package com.example.sharkystocks.mvi

import com.example.sharkystocks.data.model.Stock

sealed class StockAction {
    data class SelectedStock(val stock: Stock?) : StockAction()
    object FetchListOfStock : StockAction()
    data class FetchSuccess(val stock: List<Stock>) : StockAction()
    data class FetchError(val error: String) : StockAction()
}