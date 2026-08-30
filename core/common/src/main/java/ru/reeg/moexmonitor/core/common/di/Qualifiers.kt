package ru.reeg.moexmonitor.core.common.di

import javax.inject.Qualifier

/**
 * В чистом Dagger нет готового @ApplicationContext из Hilt — квалификатор
 * объявляем сами и отдаём Context в граф через @BindsInstance в CoreComponent.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationContext
