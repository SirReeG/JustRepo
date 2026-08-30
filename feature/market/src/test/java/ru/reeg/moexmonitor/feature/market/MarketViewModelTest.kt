package ru.reeg.moexmonitor.feature.market

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.reeg.moexmonitor.core.model.SecurityQuote
import ru.reeg.moexmonitor.core.ui.ext.ListUiState

/**
 * Тесты на ViewModel без Android: TestScheduler даёт полный контроль над временем,
 * TestObserver — доступ к истории всех эмиссий.
 */
class MarketViewModelTest {

    private lateinit var market: FakeMarketRepository
    private lateinit var watchlist: FakeWatchlistRepository
    private lateinit var schedulers: TestSchedulerProvider
    private lateinit var viewModel: MarketViewModel

    @Before
    fun setUp() {
        market = FakeMarketRepository()
        watchlist = FakeWatchlistRepository()
        schedulers = TestSchedulerProvider()
        viewModel = MarketViewModel(market, watchlist, schedulers)
    }

    private fun ListUiState<SecurityQuote>.itemIds(): List<String> = items.map { it.secId }

    @Test
    fun `refresh at init requests both segments`() {
        schedulers.scheduler.triggerActions()
        // Акции и индекс — два отдельных обновления.
        assertEquals(2, market.refreshCount)
    }

    @Test
    fun `list items are shown and refreshing turns off`() {
        val observer = viewModel.uiState.test()
        market.listItems.onNext(listOf(securityQuote("SBER"), securityQuote("GAZP")))
        schedulers.scheduler.triggerActions()

        val state = observer.values().last()
        assertEquals(listOf("SBER", "GAZP"), state.itemIds())
        assertFalse(state.isRefreshing)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `search switches source only after debounce`() {
        val observer = viewModel.uiState.test()
        market.listItems.onNext(listOf(securityQuote("SBER")))
        schedulers.scheduler.triggerActions()

        market.searchItems.onNext(listOf(securityQuote("LKOH")))
        viewModel.onSearchQueryChanged("лук")

        // Ещё не прошло 250 мс — запрос в репозиторий не ушёл.
        schedulers.scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)
        assertEquals(null, market.lastSearchQuery)

        schedulers.scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
        assertEquals("лук", market.lastSearchQuery)
        assertEquals(listOf("LKOH"), observer.values().last().itemIds())
    }

    @Test
    fun `refresh error is exposed and can be consumed`() {
        market.refreshError = IllegalStateException("нет сети")
        val observer = viewModel.uiState.test()
        market.listItems.onNext(emptyList())

        viewModel.refresh()
        schedulers.scheduler.triggerActions()
        assertEquals("нет сети", observer.values().last().errorMessage)

        viewModel.consumeError()
        schedulers.scheduler.triggerActions()
        assertEquals(null, observer.values().last().errorMessage)
    }

    @Test
    fun `toggle favorite goes to watchlist repository`() {
        viewModel.uiState.test()
        viewModel.toggleFavorite("SBER")
        schedulers.scheduler.triggerActions()
        assertTrue(watchlist.toggled.contains("SBER"))
    }
}
