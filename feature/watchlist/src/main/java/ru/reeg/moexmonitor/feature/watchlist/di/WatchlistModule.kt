package ru.reeg.moexmonitor.feature.watchlist.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.di.FeatureScope
import ru.reeg.moexmonitor.feature.watchlist.WatchlistViewModel

@Module
abstract class WatchlistModule {

    @Binds
    abstract fun bindWatchlistViewModel(viewModel: WatchlistViewModel): ViewModel

    companion object {
        @FeatureScope
        @Provides
        fun provideWatchlistViewModel(
            watchlistRepository: WatchlistRepository,
            schedulers: SchedulerProvider,
        ): WatchlistViewModel =
             WatchlistViewModel(
                watchlistRepository = watchlistRepository,
                schedulers = schedulers,)
    }
}
