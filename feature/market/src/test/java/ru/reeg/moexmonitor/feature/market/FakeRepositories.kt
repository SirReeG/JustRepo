package ru.reeg.moexmonitor.feature.market

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import java.util.Optional
import ru.reeg.moexmonitor.core.data.repository.MarketRepository
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.model.Candle
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.MarketList
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.model.Quote
import ru.reeg.moexmonitor.core.model.Security
import ru.reeg.moexmonitor.core.model.SecurityQuote

/**
 * Фейки вместо моков: без библиотек, зато видно, как ведёт себя источник данных.
 * BehaviorProcessor — Flowable-версия BehaviorSubject, умеет отдавать
 * текущее значение новому подписчику.
 */
class FakeMarketRepository : MarketRepository {

    val listItems = BehaviorProcessor.createDefault<List<SecurityQuote>>(emptyList())
    val searchItems = BehaviorProcessor.createDefault<List<SecurityQuote>>(emptyList())
    var refreshError: Throwable? = null
    var refreshCount = 0
    var lastSearchQuery: String? = null

    override fun observeList(list: MarketList): Flowable<List<SecurityQuote>> = listItems

    override fun observeSecurity(secId: String): Flowable<Optional<SecurityQuote>> =
        Flowable.just(Optional.empty())

    override fun observeMainIndex(): Flowable<Optional<SecurityQuote>> =
        Flowable.just(Optional.empty())

    override fun search(query: String): Flowable<List<SecurityQuote>> {
        lastSearchQuery = query
        return searchItems
    }

    override fun observeCandles(secId: String, range: CandleRange): Flowable<List<Candle>> =
        Flowable.just(emptyList())

    override fun refreshSegment(segment: MarketSegment): Completable {
        refreshCount++
        return refreshError?.let { Completable.error(it) } ?: Completable.complete()
    }

    override fun refreshCandles(
        secId: String,
        segment: MarketSegment,
        range: CandleRange,
    ): Completable = Completable.complete()

    override fun segmentOf(secId: String): Single<MarketSegment> =
        Single.just(MarketSegment.SHARES)

    override fun isEmpty(segment: MarketSegment): Single<Boolean> = Single.just(false)
}

class FakeWatchlistRepository : WatchlistRepository {

    val toggled = mutableListOf<String>()

    override fun observeWatchlist(): Flowable<List<SecurityQuote>> = Flowable.just(emptyList())

    override fun observeIsFavorite(secId: String): Flowable<Boolean> = Flowable.just(false)

    override fun toggle(secId: String): Completable = Completable.fromAction {
        toggled += secId
    }
}

fun securityQuote(secId: String, changePercent: Double = 1.0) = SecurityQuote(
    security = Security(
        secId = secId,
        shortName = secId,
        segment = MarketSegment.SHARES,
        board = MarketSegment.SHARES.board,
        lotSize = 10,
        prevPrice = 100.0,
    ),
    quote = Quote(
        last = 101.0,
        changePercent = changePercent,
        open = 100.0,
        high = 102.0,
        low = 99.0,
        valueToday = 1_000.0,
        updatedAtMillis = 0L,
    ),
)
