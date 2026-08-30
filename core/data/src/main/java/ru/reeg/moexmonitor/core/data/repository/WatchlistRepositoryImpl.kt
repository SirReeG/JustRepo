package ru.reeg.moexmonitor.core.data.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.TimeProvider
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.mapper.toDomain
import ru.reeg.moexmonitor.core.database.dao.WatchlistDao
import ru.reeg.moexmonitor.core.model.SecurityQuote

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val timeProvider: TimeProvider,
    private val schedulers: SchedulerProvider,
) : WatchlistRepository {

    override fun observeWatchlist(): Flowable<List<SecurityQuote>> =
        watchlistDao.observeSecurities()
            .map { items -> items.map { it.toDomain(isFavorite = true) } }
            .subscribeOn(schedulers.io())

    override fun observeIsFavorite(secId: String): Flowable<Boolean> =
        watchlistDao.observeFavoriteCount(secId)
            .map { count -> count > 0 }
            .subscribeOn(schedulers.io())

    override fun toggle(secId: String): Completable = Completable
        .fromAction { watchlistDao.toggle(secId, timeProvider.nowMillis()) }
        .subscribeOn(schedulers.io())
}
