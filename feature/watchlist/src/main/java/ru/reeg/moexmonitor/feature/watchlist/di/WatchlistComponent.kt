package ru.reeg.moexmonitor.feature.watchlist.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import jakarta.inject.Singleton
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.ViewModelKey
import ru.reeg.moexmonitor.feature.watchlist.WatchlistFragment
import ru.reeg.moexmonitor.feature.watchlist.WatchlistViewModel
@Singleton
@Component(dependencies = [CoreComponent::class], modules = [WatchlistModule::class])
interface WatchlistComponent {

    fun inject(fragment: WatchlistFragment)


    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): WatchlistComponent
    }
}

@Module
interface WatchlistModule {

    @Binds
    @IntoMap
    @ViewModelKey(WatchlistViewModel::class)
    fun bindWatchlistViewModel(viewModel: WatchlistViewModel): ViewModel
}
