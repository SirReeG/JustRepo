package ru.reeg.moexmonitor.core.common.di

import dagger.Binds
import dagger.Module
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.SystemTimeProvider
import ru.reeg.moexmonitor.core.common.TimeProvider
import ru.reeg.moexmonitor.core.common.rx.AppSchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider

/**
 * Обычный Dagger-модуль: никаких @InstallIn, его подключает CoreComponent руками.
 */
@Module
interface CommonModule {

    @Binds
    @Singleton
    fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    @Singleton
    fun bindSchedulerProvider(impl: AppSchedulerProvider): SchedulerProvider
}
