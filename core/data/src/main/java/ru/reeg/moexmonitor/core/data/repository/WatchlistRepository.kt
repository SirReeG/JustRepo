package ru.reeg.moexmonitor.core.data.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import ru.reeg.moexmonitor.core.model.SecurityQuote

interface WatchlistRepository {
    fun observeWatchlist(): Flowable<List<SecurityQuote>>
    fun observeIsFavorite(secId: String): Flowable<Boolean>
    fun toggle(secId: String): Completable
}
