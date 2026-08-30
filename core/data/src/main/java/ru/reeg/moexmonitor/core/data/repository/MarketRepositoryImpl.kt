package ru.reeg.moexmonitor.core.data.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.TimeProvider
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.toOptional
import ru.reeg.moexmonitor.core.data.mapper.toDomain
import ru.reeg.moexmonitor.core.data.mapper.toEntity
import ru.reeg.moexmonitor.core.database.dao.CandleDao
import ru.reeg.moexmonitor.core.database.dao.SecurityDao
import ru.reeg.moexmonitor.core.database.dao.WatchlistDao
import ru.reeg.moexmonitor.core.database.entity.SecurityEntity
import ru.reeg.moexmonitor.core.model.Candle
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.MarketList
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.model.SecurityQuote
import ru.reeg.moexmonitor.core.network.MoexRemoteDataSource

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val remote: MoexRemoteDataSource,
    private val securityDao: SecurityDao,
    private val candleDao: CandleDao,
    private val watchlistDao: WatchlistDao,
    private val timeProvider: TimeProvider,
    private val schedulers: SchedulerProvider,
) : MarketRepository {

    override fun observeList(list: MarketList): Flowable<List<SecurityQuote>> {
        val source = when (list) {
            MarketList.ALL -> securityDao.observeBySegment(MarketSegment.SHARES, LIST_LIMIT)
            MarketList.GAINERS -> securityDao.observeGainers(MarketSegment.SHARES, TOP_LIMIT)
            MarketList.LOSERS -> securityDao.observeLosers(MarketSegment.SHARES, TOP_LIMIT)
            MarketList.FUNDS -> securityDao.observeBySegment(MarketSegment.FUNDS, LIST_LIMIT)
        }
        return source.withFavorites()
    }

    override fun observeSecurity(secId: String): Flowable<Optional<SecurityQuote>> =
        Flowable.combineLatest(
            securityDao.observeBySecId(secId),
            watchlistDao.observeFavoriteCount(secId),
            BiFunction<List<SecurityEntity>, Int, Optional<SecurityQuote>> { entities, count ->
                entities.firstOrNull()?.toDomain(isFavorite = count > 0).toOptional()
            },
        ).subscribeOn(schedulers.io())

    override fun observeMainIndex(): Flowable<Optional<SecurityQuote>> =
        securityDao.observeBySecId(MarketSegment.MAIN_INDEX)
            .map { entities -> entities.firstOrNull()?.toDomain().toOptional() }
            .subscribeOn(schedulers.io())

    override fun search(query: String): Flowable<List<SecurityQuote>> =
        securityDao.search(query).withFavorites()

    override fun observeCandles(secId: String, range: CandleRange): Flowable<List<Candle>> =
        candleDao.observe(secId, range.name)
            .map { candles -> candles.map { it.toDomain() } }
            .subscribeOn(schedulers.io())

    override fun refreshSegment(segment: MarketSegment): Completable =
        remote.securities(segment).flatMapCompletable { networkItems ->
            Completable.fromAction {
                // Для акций и фондов приходит один и тот же ответ TQBR — сегмент
                // определяется по SECTYPE внутри маппера, поэтому одно обновление
                // наполняет сразу обе вкладки.
                val now = timeProvider.nowMillis()
                securityDao.upsertAll(networkItems.map { it.toEntity(segment, now) })
            }.subscribeOn(schedulers.io())
        }

    override fun refreshCandles(
        secId: String,
        segment: MarketSegment,
        range: CandleRange,
    ): Completable = remote.candles(secId = secId, segment = segment, range = range)
        .flatMapCompletable { networkCandles ->
            Completable.fromAction {
                candleDao.replace(
                    secId,
                    range.name,
                    networkCandles.map { it.toEntity(secId, range.name) },
                )
            }.subscribeOn(schedulers.io())
        }

    override fun segmentOf(secId: String): Single<MarketSegment> = Single
        .fromCallable { securityDao.segmentOf(secId) ?: MarketSegment.SHARES }
        .subscribeOn(schedulers.io())

    override fun isEmpty(segment: MarketSegment): Single<Boolean> = Single
        .fromCallable { securityDao.countBySegment(segment) == 0 }
        .subscribeOn(schedulers.io())

    /** Подмешивает флаг «в избранном» из отдельной таблицы. */
    private fun Flowable<List<SecurityEntity>>.withFavorites(): Flowable<List<SecurityQuote>> =
        Flowable.combineLatest(
            this,
            watchlistDao.observeIds(),
            BiFunction<List<SecurityEntity>, List<String>, List<SecurityQuote>> { items, ids ->
                val favorites = ids.toHashSet()
                items.map { entity -> entity.toDomain(isFavorite = entity.secId in favorites) }
            },
        ).subscribeOn(schedulers.io())

    private companion object {
        const val LIST_LIMIT = 300
        const val TOP_LIMIT = 30
    }
}
