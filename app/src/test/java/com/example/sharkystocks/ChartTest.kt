package com.example.sharkystocks

import com.example.sharkystocks.data.model.ChartPoint
import com.example.sharkystocks.data.model.ChartRange
import com.example.sharkystocks.data.model.MarketstackStock
import com.example.sharkystocks.data.repository.StockRepository
import com.example.sharkystocks.mvi.StockAction
import com.example.sharkystocks.mvi.StockState
import com.example.sharkystocks.mvi.stockReducer
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
class ChartTest {

    private lateinit var fakeApiService: FakeApiService
    private lateinit var repository: StockRepository
    private lateinit var viewModel: StockViewModel

    private val testDispatcher = StandardTestDispatcher()

    // API returns newest first (sort=DESC)
    private val aaplHistory = listOf(
        MarketstackStock("AAPL", 152.0, 155.0, 151.0, 154.0, 3000.0, "2025-10-17T00:00:00+0000"),
        MarketstackStock("AAPL", 151.0, 153.0, 150.0, 152.0, 2000.0, "2025-10-16T00:00:00+0000"),
        MarketstackStock("AAPL", 150.0, 152.0, 149.0, 151.0, 1000.0, "2025-10-15T00:00:00+0000")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeApiService = FakeApiService()
        fakeApiService.history = aaplHistory

        repository = StockRepository(context = null, api = fakeApiService)
        viewModel = StockViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- Repository ----------

    @Test
    fun `refreshChartScreen returns history sorted oldest first with short dates`() = runTest {
        val history = repository.refreshChartScreen("AAPL")

        assertEquals(listOf("2025-10-15", "2025-10-16", "2025-10-17"), history.map { it.date })
        assertEquals(154.0, history.last().close, 0.0)
    }

    @Test
    fun `refreshChartScreen uses cache on second call`() = runTest {
        repository.refreshChartScreen("AAPL")
        repository.refreshChartScreen("AAPL")

        assertEquals(1, fakeApiService.historyCallCount)
    }

    @Test
    fun `refreshChartScreen falls back to cache when API fails on force refresh`() = runTest {
        repository.refreshChartScreen("AAPL")
        fakeApiService.shouldThrowError = true

        val history = repository.refreshChartScreen("AAPL", forceRefresh = true)

        assertEquals(3, history.size)
    }

    // ---------- ViewModel ----------

    @Test
    fun `refreshChartScreen sets chart state to Success`() = runTest {
        viewModel.refreshChartScreen("AAPL")
        advanceUntilIdle()

        val chartState = viewModel.state.value.chartState
        assertTrue(chartState is StockChartState.Success)

        val success = chartState as StockChartState.Success
        assertEquals("AAPL", success.symbol)
        assertEquals(3, success.points.size)
        assertEquals("2025-10-15", success.points.first().date)
    }

    @Test
    fun `refreshChartScreen sets chart state to Error when API fails`() = runTest {
        fakeApiService.shouldThrowError = true

        viewModel.refreshChartScreen("AAPL")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.chartState is StockChartState.Error)
    }

    @Test
    fun `refreshChartScreen sets chart state to Empty when no history`() = runTest {
        viewModel.refreshChartScreen("MSFT")
        advanceUntilIdle()

        assertEquals(StockChartState.Empty, viewModel.state.value.chartState)
    }

    @Test
    fun `selectChartRange updates chart range`() {
        viewModel.selectChartRange(ChartRange.THREE_MONTHS)

        assertEquals(ChartRange.THREE_MONTHS, viewModel.state.value.chartRange)
    }

    // ---------- Reducer ----------

    @Test
    fun `FetchChart sets chart state to Loading`() {
        val newState = stockReducer(StockState(), StockAction.FetchChart)

        assertEquals(StockChartState.Loading, newState.chartState)
    }

    @Test
    fun `FetchChartSuccess with points sets Success`() {
        val points = listOf(ChartPoint("2025-10-17", 1.0, 2.0, 0.5, 1.5, 100.0))

        val newState = stockReducer(StockState(), StockAction.FetchChartSuccess("AAPL", points))

        assertEquals(StockChartState.Success("AAPL", points), newState.chartState)
    }

    @Test
    fun `FetchChartError sets Error`() {
        val newState = stockReducer(StockState(), StockAction.FetchChartError("boom"))

        assertEquals(StockChartState.Error("boom"), newState.chartState)
    }
}
