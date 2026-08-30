package ru.reeg.moexmonitor.feature.market.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.ViewModelKey
import ru.reeg.moexmonitor.feature.market.MarketFragment
import ru.reeg.moexmonitor.feature.market.MarketViewModel

/**
 * Компонент фичи. Живёт ровно столько, сколько фрагмент, и зависит
 * от CoreComponent — то есть видит только те типы, которые тот отдаёт наружу.
 *
 * Так фича остаётся изолированной: она физически не может получить DAO
 * или Retrofit, только репозитории.
 */
@Component(dependencies = [CoreComponent::class], modules = [MarketModule::class])
interface MarketComponent {

    fun inject(fragment: MarketFragment)

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): MarketComponent
    }
}

@Module
interface MarketModule {

    @Binds
    @IntoMap
    @ViewModelKey(MarketViewModel::class)
    fun bindMarketViewModel(viewModel: MarketViewModel): ViewModel
}
