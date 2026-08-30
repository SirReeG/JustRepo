package ru.reeg.moexmonitor.core.data.di

import dagger.Binds
import dagger.Module
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.data.repository.MarketRepository
import ru.reeg.moexmonitor.core.data.repository.MarketRepositoryImpl
import ru.reeg.moexmonitor.core.data.repository.NewsRepository
import ru.reeg.moexmonitor.core.data.repository.NewsRepositoryImpl
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepositoryImpl

/**
 * Фичи зависят только от интерфейсов репозиториев — реализацию подставляет Dagger.
 * Это и есть точка, где в тестах можно подменить всё на фейки.
 */
@Module
interface DataModule {

    @Binds
    @Singleton
    fun bindMarketRepository(impl: MarketRepositoryImpl): MarketRepository

    @Binds
    @Singleton
    fun bindWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository

    @Binds
    @Singleton
    fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository
}
