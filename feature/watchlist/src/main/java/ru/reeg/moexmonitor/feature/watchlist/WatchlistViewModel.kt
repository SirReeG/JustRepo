package ru.reeg.moexmonitor.feature.watchlist

import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.model.SecurityQuote

class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val schedulers: SchedulerProvider,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val items: Flowable<List<SecurityQuote>> = watchlistRepository.observeWatchlist()
        .observeOn(schedulers.ui())
        .replay(1)
        .refCount()

    fun toggleFavorite(secId: String) {
        disposables.add(watchlistRepository.toggle(secId).subscribe({ }, { }))
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
