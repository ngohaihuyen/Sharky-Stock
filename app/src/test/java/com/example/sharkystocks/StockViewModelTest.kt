package com.example.sharkystocks

import com.example.sharkystocks.data.model.MarketstackStock
import com.example.sharkystocks.data.repository.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockViewModelTest {

    private lateinit var viewModel: StockViewModel
    private lateinit var repository: StockRepository
    private lateinit var fakeApiService: FakeApiService

    private val testDispatcher = StandardTestDispatcher()

    private val marketAapl = MarketstackStock(
        symbol = "AAPL",
        open = 150.0,
        high = 152.0,
        low = 149.0,
        close = 151.0,
        volume = 1000.0
    )

    private val marketMsft = MarketstackStock(
        symbol = "MSFT",
        open = 340.0,
        high = 345.0,
        low = 339.0,
        close = 341.0,
        volume = 2000.0
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeApiService = FakeApiService()
        fakeApiService.stocks = listOf(marketAapl, marketMsft)


        repository = StockRepository(context = null, api = fakeApiService)

        viewModel = StockViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshHomeScreen sets the list of items to what the API returns`() = runTest {
        // Act
        viewModel.refreshHomeScreen()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertTrue(state.stockListState is StockListState.Success)

        val success = state.stockListState as StockListState.Success
        assertEquals(2, success.stocks.size)

        assertEquals("AAPL", success.stocks[0].symbol)
        assertEquals("$151.00", success.stocks[0].price)
        assertEquals("1.00", success.stocks[0].change)
        assertEquals("0.67%", success.stocks[0].changePercent)

        assertEquals("MSFT", success.stocks[1].symbol)
        assertEquals("$341.00", success.stocks[1].price)
    }

    @Test
    fun `refreshHomeScreen sets Error state when API throws`() = runTest {
        // Arrange
        fakeApiService.shouldThrowError = true

        // Act
        viewModel.refreshHomeScreen()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertTrue(state.stockListState is StockListState.Error)

        val err = state.stockListState as StockListState.Error
        assertTrue(err.message.contains("Failed to load stocks"))
    }

    @Test
    fun `refreshDetailsScreen updates state with selected stock`() = runTest {
        // Arrange
        viewModel.refreshHomeScreen()
        advanceUntilIdle()

        // Act
        viewModel.refreshDetailsScreen("AAPL")
        advanceUntilIdle()

        // Assert
        val selected = viewModel.state.value.selectedStock
        assertEquals("AAPL", selected?.symbol)
        assertEquals("$151.00", selected?.price)
    }

    @Test
    fun `selectStock updates selectedStock in state`() = runTest {
        // Arrange
        viewModel.refreshHomeScreen()
        advanceUntilIdle()

        val success = viewModel.state.value.stockListState as StockListState.Success
        val chosen = success.stocks[0]

        // Act
        viewModel.selectStock(chosen)
        advanceUntilIdle()

        // Assert
        assertEquals("AAPL", viewModel.state.value.selectedStock?.symbol)
    }

    @Test
    fun `refreshStocks sets Success with items`() = runTest {
        // Act
        viewModel.refreshStocks()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertTrue(state.stockListState is StockListState.Success)

        val success = state.stockListState as StockListState.Success
        assertEquals(2, success.stocks.size)
    }

    @Test
    fun `refreshHomeScreen uses cached data on second call`() = runTest {
        viewModel.refreshHomeScreen()
        advanceUntilIdle()


        fakeApiService.stocks = listOf(
            MarketstackStock("TSLA", open = 200.0, high = 210.0, low = 195.0, close = 205.0, volume = 3000.0)
        )


        viewModel.refreshHomeScreen()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.stockListState is StockListState.Success)
    }
}
