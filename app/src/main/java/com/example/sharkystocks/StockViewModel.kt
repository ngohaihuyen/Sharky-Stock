package com.example.sharkystocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sharkystocks.data.repository.StockRepository
import com.example.sharkystocks.data.model.Stock
import com.example.sharkystocks.mvi.StockAction
import com.example.sharkystocks.mvi.StockState
import com.example.sharkystocks.mvi.stockReducer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class StockViewModel(private val repository: StockRepository) : ViewModel() {

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()


    private fun fireAction(action: StockAction) {
        _state.value = stockReducer(_state.value, action)
    }

    fun refreshHomeScreen() {
        fireAction(StockAction.FetchListOfStock)

        viewModelScope.launch {
            try {
                val symbols = repository.refreshHomeScreen()
                val cachedDetails = repository.getCachedStockDetails(symbols)

                val uniqueStocks = cachedDetails
                    .groupBy { it.symbol }
                    .map { (_, details) ->

                        details.maxByOrNull { it.lastUpdated }
                    }
                    .filterNotNull()

                val stocks = uniqueStocks.map { stockDetail ->
                    stockDetail?.let {
                        Stock(
                            symbol = it.symbol,
                            price = String.format("$%.2f", it.price),
                            change = String.format("%.2f", it.change),
                            changePercent = String.format("%.2f%%", it.changePercent)
                        )
                    } ?: Stock("", "Loading...", "0.0", "0.0%")
                }

                fireAction(StockAction.FetchSuccess(stocks))

            } catch (e: Exception) {
                fireAction(StockAction.FetchError("Failed to load stocks: ${e.message}"))
            }
        }
    }

    fun refreshStocks() {
        fireAction(StockAction.FetchListOfStock)

        viewModelScope.launch {
            try {
                val symbols = repository.refreshHomeScreen()
                val cachedDetails = repository.getCachedStockDetails(symbols)


                val uniqueStocks = cachedDetails
                    .groupBy { it.symbol }
                    .map { (_, details) ->

                        details.maxByOrNull { it.lastUpdated }
                    }
                    .filterNotNull()

                val stocks = uniqueStocks.map { stockDetail ->
                    stockDetail?.let {
                        Stock(
                            symbol = it.symbol,
                            price = String.format("$%.2f", it.price),
                            change = String.format("%.2f", it.change),
                            changePercent = String.format("%.2f%%", it.changePercent)
                        )
                    } ?: Stock("", "Loading...", "0.0", "0.0%")
                }

                fireAction(StockAction.FetchSuccess(stocks))

            } catch (e: Exception) {
                fireAction(StockAction.FetchError("Failed to refresh stocks: ${e.message}"))
            }
        }
    }

    fun refreshDetailsScreen(selectedItem: String) {
        viewModelScope.launch {
            try {
                val stockDetail = repository.refreshDetailsScreen(selectedItem)
                val stock = stockDetail?.let {
                    Stock(
                        symbol = it.symbol,
                        price = String.format("$%.2f", it.price),
                        change = String.format("%.2f", it.change),
                        changePercent = String.format("%.2f%%", it.changePercent)
                    )
                }
                fireAction(StockAction.SelectedStock(stock))
            } catch (e: Exception) {

            }
        }
    }

    fun selectStock(stock: Stock) {
        fireAction(StockAction.SelectedStock(stock))
    }

    fun retryLoading() {
        refreshHomeScreen()
    }
}

class StockViewModelFactory(
    private val repository: StockRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            return StockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}