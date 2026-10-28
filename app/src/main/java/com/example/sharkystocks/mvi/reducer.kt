package com.example.sharkystocks.mvi

import com.example.sharkystocks.StockChartState
import com.example.sharkystocks.StockListState


fun stockReducer(oldState: StockState, action: StockAction): StockState {
    return when (action) {
        is StockAction.FetchError -> {
            oldState.copy(
                stockListState = StockListState.Error(action.error),
                error = Exception(action.error),
                stocks = null
            )
        }
        is StockAction.FetchSuccess -> {
            oldState.copy(
                stockListState = if (action.stock.isEmpty()) StockListState.Empty
                else StockListState.Success(action.stock),
                stocks = action.stock,
                error = null
            )
        }
        is StockAction.FetchListOfStock -> {
            oldState.copy(
                stockListState = StockListState.Loading,
                error = null

            )
        }
        is StockAction.SelectedStock -> {
            oldState.copy(
                selectedStock = action.stock
            )
        }
        is StockAction.FetchChart -> {
            oldState.copy(
                chartState = StockChartState.Loading
            )
        }
        is StockAction.FetchChartSuccess -> {
            oldState.copy(
                chartState = if (action.points.isEmpty()) StockChartState.Empty
                else StockChartState.Success(action.symbol, action.points)
            )
        }
        is StockAction.FetchChartError -> {
            oldState.copy(
                chartState = StockChartState.Error(action.error)
            )
        }
        is StockAction.SelectChartRange -> {
            oldState.copy(
                chartRange = action.range
            )
        }
    }
}