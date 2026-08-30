package ru.reeg.moexmonitor.feature.security.di

import dagger.Component
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.feature.security.SecurityFragment

/**
 * Здесь нет модуля с @IntoMap: SecurityViewModel создаётся через
 * SecurityViewModelFactory, потому что ему нужен параметр secId.
 */
@Component(dependencies = [CoreComponent::class])
interface SecurityComponent {

    fun inject(fragment: SecurityFragment)

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): SecurityComponent
    }
}
