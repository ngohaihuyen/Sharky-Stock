package com.example.sharkystocks

import com.example.sharkystocks.data.model.MarketstackStock
import com.example.sharkystocks.data.repository.StockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryTest {

    private lateinit var repository: StockRepository
    private lateinit var fakeApiService: FakeApiService

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
        fakeApiService = FakeApiService()
        fakeApiService.stocks = listOf(marketAapl, marketMsft)

        repository = StockRepository(context = null, api = fakeApiService)
    }

    @Test
    fun `refreshHomeScreen returns symbols from API`() = runTest {
        val symbols = repository.refreshHomeScreen()
        Assert.assertEquals(listOf("AAPL", "MSFT"), symbols)
    }

    @Test
    fun `refreshHomeScreen uses cached data on second call`() = runTest {
        val first = repository.refreshHomeScreen()
        Assert.assertEquals(listOf("AAPL", "MSFT"), first)

        fakeApiService.stocks = listOf(
            MarketstackStock(
                "TSLA",
                open = 200.0,
                high = 210.0,
                low = 195.0,
                close = 205.0,
                volume = 3000.0
            )
        )

        val second = repository.refreshHomeScreen()


        Assert.assertEquals(listOf("AAPL", "MSFT"), second)
    }

    @Test
    fun `refreshDetailsScreen returns detail after home refresh`() = runTest {

        repository.refreshHomeScreen()

        val detail = repository.refreshDetailsScreen("AAPL")

        Assert.assertNotNull(detail)
        Assert.assertEquals("AAPL", detail!!.symbol)
        Assert.assertEquals(151.0, detail.price, 0.0001)
        Assert.assertEquals(1.0, detail.change, 0.0001)
    }

    @Test
    fun `refreshDetailsScreen returns null for unknown symbol`() = runTest {
        repository.refreshHomeScreen()

        val detail = repository.refreshDetailsScreen("UNKNOWN")
        Assert.assertNull(detail)
    }

    @Test
    fun `getCachedStockDetails returns cached entities for symbols`() = runTest {
        val symbols = repository.refreshHomeScreen()

        val cached = repository.getCachedStockDetails(symbols)

        Assert.assertEquals(2, cached.size)
        Assert.assertEquals("AAPL", cached[0].symbol)
        Assert.assertEquals("MSFT", cached[1].symbol)
    }



    @Test
    fun `refreshHomeScreen throws when API throws and no cache`() = runTest {
        fakeApiService.shouldThrowError = true

        try {
            repository.refreshHomeScreen()
        } catch (e: Exception) {

            Assert.assertNotNull(e.message)
            return@runTest
        }


        throw AssertionError("Expected exception but none was thrown")
    }
}