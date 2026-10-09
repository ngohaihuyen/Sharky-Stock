package com.example.sharkystocks

import com.example.sharkystocks.data.model.Stock
import com.example.sharkystocks.mvi.StockAction
import com.example.sharkystocks.mvi.StockState
import com.example.sharkystocks.mvi.stockReducer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReducerTest {

    @Test
    fun `FetchListOfStock sets state to Loading`() {
        // Arrange
        val oldState = StockState()
        val action = StockAction.FetchListOfStock

        // Act
        val newState = stockReducer(oldState, action)

        // Assert
        assertEquals(StockListState.Loading, newState.stockListState)
    }

    @Test
    fun `FetchSuccess updates state with stocks`() {
        // Arrange
        val oldState = StockState()
        val testStocks = listOf(
            Stock("AAPL", "$150.00", "5.00", "3.33%"),
            Stock("MSFT", "$300.00", "10.00", "3.33%")
        )
        val action = StockAction.FetchSuccess(testStocks)

        // Act
        val newState = stockReducer(oldState, action)

        // Assert
        assertTrue(newState.stockListState is StockListState.Success)
        assertEquals(testStocks, newState.stocks)
        assertEquals(2, (newState.stockListState as StockListState.Success).stocks.size)
    }

    @Test
    fun `FetchSuccess with empty list sets Empty state`() {
        // Arrange
        val oldState = StockState()
        val emptyStocks = emptyList<Stock>()
        val action = StockAction.FetchSuccess(emptyStocks)

        // Act
        val newState = stockReducer(oldState, action)

        // Assert
        assertEquals(StockListState.Empty, newState.stockListState)
        assertEquals(emptyStocks, newState.stocks)
    }

    @Test
    fun `FetchError sets Error state`() {
        // Arrange
        val oldState = StockState()
        val errorMessage = "Network error occurred"
        val action = StockAction.FetchError(errorMessage)

        // Act
        val newState = stockReducer(oldState, action)

        // Assert
        assertTrue(newState.stockListState is StockListState.Error)
        assertEquals(errorMessage, (newState.stockListState as StockListState.Error).message)
        assertNull(newState.stocks)
    }

    @Test
    fun `SelectedStock sets selected stock`() {
        // Arrange
        val oldState = StockState()
        val testStock = Stock("AAPL", "$150.00", "5.00", "3.33%")
        val action = StockAction.SelectedStock(testStock)

        // Act
        val newState = stockReducer(oldState, action)

        // Assert
        assertEquals(testStock, newState.selectedStock)
    }

    @Test
    fun `SelectedStock null clears selection`() {
        // Arrange
        val testStock = Stock("OLD", "$100.00", "1.00", "1.00%")
        val oldState = StockState(selectedStock = testStock)
        val action = StockAction.SelectedStock(null)

        // Act
        val newState = stockReducer(oldState, action)

        // Assert
        assertNull(newState.selectedStock)
    }
}