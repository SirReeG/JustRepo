package ru.reeg.moexmonitor.feature.market.di

import dagger.Component
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.FeatureScope
import ru.reeg.moexmonitor.feature.market.MarketFragment

@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [MarketModule::class])
interface MarketComponent {

    fun inject(fragment: MarketFragment)

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): MarketComponent
    }
}
