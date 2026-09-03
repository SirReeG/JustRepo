package ru.reeg.moexmonitor.feature.watchlist.di

import dagger.Component
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.FeatureScope
import ru.reeg.moexmonitor.feature.watchlist.WatchlistFragment
@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [WatchlistModule::class])
interface WatchlistComponent {

    fun inject(fragment: WatchlistFragment)

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): WatchlistComponent
    }
}
