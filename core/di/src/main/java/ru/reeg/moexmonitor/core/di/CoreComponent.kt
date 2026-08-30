package ru.reeg.moexmonitor.core.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.di.ApplicationContext
import ru.reeg.moexmonitor.core.common.di.CommonModule
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.di.DataModule
import ru.reeg.moexmonitor.core.data.repository.MarketRepository
import ru.reeg.moexmonitor.core.data.repository.NewsRepository
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.database.di.DatabaseModule
import ru.reeg.moexmonitor.core.network.di.NetworkModule

/**
 * Корневой (application-scoped) компонент. Живёт столько же, сколько процесс,
 * и хранит синглтоны: Retrofit, Room, репозитории, планировщики.
 *
 * Наружу он отдаёт только то, что перечислено в provision-методах ниже.
 * Это осознанное ограничение: фича не может случайно дотянуться до DAO
 * или до OkHttp — только до репозиториев.
 *
 * Компоненты фич объявляют CoreComponent как dependency и получают
 * доступ ровно к этим типам.
 */
@Singleton
@Component(
    modules = [
        CommonModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        DataModule::class,
    ],
)
interface CoreComponent {

    fun marketRepository(): MarketRepository

    fun watchlistRepository(): WatchlistRepository

    fun newsRepository(): NewsRepository

    fun schedulers(): SchedulerProvider

    @Component.Factory
    interface Factory {
        /**
         * @BindsInstance — способ положить в граф уже готовый объект.
         * В Hilt это делал @ApplicationContext, здесь передаём Application руками.
         */
        fun create(@BindsInstance @ApplicationContext context: Context): CoreComponent
    }
}
