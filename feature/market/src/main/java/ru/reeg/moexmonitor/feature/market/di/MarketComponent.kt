package ru.reeg.moexmonitor.feature.market.di

import dagger.BindsInstance
import dagger.Component
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.FeatureScope
import ru.reeg.moexmonitor.core.di.coreComponent
import ru.reeg.moexmonitor.feature.market.MarketFragment

@FeatureScope
@Component(dependencies = [CoreComponent::class], modules = [MarketModule::class])
interface MarketComponent {

    fun inject(fragment: MarketFragment)

    @Component.Factory
    interface Factory {
        fun create(
            coreComponent: CoreComponent,
            @BindsInstance
            fragment: MarketFragment
        ): MarketComponent
    }

    companion object {
        fun create(fragment: MarketFragment): MarketComponent =
            DaggerMarketComponent.factory().create(
                coreComponent = fragment.coreComponent(), fragment = fragment
            )
    }
}
