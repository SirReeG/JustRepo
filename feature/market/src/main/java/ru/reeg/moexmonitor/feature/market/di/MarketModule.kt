package ru.reeg.moexmonitor.feature.market.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import ru.reeg.moexmonitor.feature.market.MarketViewModel

@Module
interface MarketModule {

    @Binds
    fun bindMarketViewModel(viewModel: MarketViewModel): ViewModel
}
