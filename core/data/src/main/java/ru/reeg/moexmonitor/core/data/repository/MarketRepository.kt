package ru.reeg.moexmonitor.core.data.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.Optional
import ru.reeg.moexmonitor.core.model.Candle
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.MarketList
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.model.SecurityQuote

/**
 * Offline-first контракт: подписка всегда идёт на базу (Flowable), сеть только
 * обновляет кэш (Completable). UI за счёт этого сразу показывает последние
 * известные данные, а обновление — это отдельное «действие», а не источник данных.
 *
 * Optional нужен там, где значения может не быть: RxJava2 не пропускает null.
 */
interface MarketRepository {

    fun observeList(list: MarketList): Flowable<List<SecurityQuote>>

    fun observeSecurity(secId: String): Flowable<Optional<SecurityQuote>>

    fun observeMainIndex(): Flowable<Optional<SecurityQuote>>

    fun search(query: String): Flowable<List<SecurityQuote>>

    fun observeCandles(secId: String, range: CandleRange): Flowable<List<Candle>>

    /** Тянет свежие данные по сегменту и складывает их в базу. */
    fun refreshSegment(segment: MarketSegment): Completable

    fun refreshCandles(secId: String, segment: MarketSegment, range: CandleRange): Completable

    /** Сегмент бумаги из кэша: от него зависят market/board в URL свечей. */
    fun segmentOf(secId: String): Single<MarketSegment>

    /** true, если по сегменту вообще нет закэшированных данных. */
    fun isEmpty(segment: MarketSegment): Single<Boolean>
}
