package ru.reeg.moexmonitor.core.common.di

import dagger.Binds
import dagger.Module
import javax.inject.Singleton
import ru.reeg.moexmonitor.core.common.SystemTimeProvider
import ru.reeg.moexmonitor.core.common.TimeProvider
import ru.reeg.moexmonitor.core.common.rx.AppSchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider

@Module
interface CommonModule {

    @Binds
    fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    fun bindSchedulerProvider(impl: AppSchedulerProvider): SchedulerProvider
}
