package com.example.sharkystocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sharkystocks.data.repository.StockRepository
import com.example.sharkystocks.data.model.ChartPoint
import com.example.sharkystocks.data.model.ChartRange
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

    /**
     * Historical closing prices for mini charts on the Home Screen.
     * Key = stock symbol, value = closing prices ordered oldest -> newest.
     * This is kept separate from StockState so the existing MVI reducer
     * and the Details chart flow do not need to be changed.
     */
    private val _miniChartPrices = MutableStateFlow<Map<String, List<Float>>>(emptyMap())
    val miniChartPrices: StateFlow<Map<String, List<Float>>> =
        _miniChartPrices.asStateFlow()

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

    fun refreshChartScreen(selectedItem: String, forceRefresh: Boolean = false) {
        fireAction(StockAction.FetchChart)

        viewModelScope.launch {
            try {
                val history = repository.refreshChartScreen(selectedItem, forceRefresh)

                val points = history
                    .sortedBy { it.date }
                    .map {
                        ChartPoint(
                            date = it.date,
                            open = it.open,
                            high = it.high,
                            low = it.low,
                            close = it.close,
                            volume = it.volume
                        )
                    }

                fireAction(StockAction.FetchChartSuccess(selectedItem, points))

            } catch (e: Exception) {
                fireAction(StockAction.FetchChartError("Failed to load chart: ${e.message}"))
            }
        }
    }

    /**
     * Load historical prices for the supplied stock symbols.
     * The repository reuses its existing API and history cache.
     * Each symbol is handled independently so one failed request does not
     * prevent the remaining mini charts from loading.
     */

    fun refreshMiniCharts(
        symbols: List<String>,
        forceRefresh: Boolean = false
    ) {

        viewModelScope.launch {
            symbols.distinct().forEach { symbol ->
                try {
                    val history = repository.refreshMiniChartScreen(
                        symbol = symbol,
                        forceRefresh = forceRefresh
                    )

                    val prices = history
                        .sortedBy { it.date }
                        .map { it.close.toFloat() }
                        .filter { it.isFinite() && it > 0f }

                    android.util.Log.d(
                        "SharkyMiniChart",
                        "$symbol: received ${prices.size} prices; " +
                                "dates=${history.map { it.date }}"
                    )

                    if (prices.size >= 2) {
                        _miniChartPrices.value =
                            _miniChartPrices.value + (symbol to prices)
                    } else {
                        android.util.Log.w(
                            "SharkyMiniChart",
                            "$symbol: insufficient historical data"
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e(
                        "SharkyMiniChart",
                        "Failed to load history for $symbol",
                        e
                    )
                }
            }
        }
    }

    fun selectChartRange(range: ChartRange) {
        fireAction(StockAction.SelectChartRange(range))
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