package ru.reeg.moexmonitor

import android.app.Application
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.CoreComponentProvider
import ru.reeg.moexmonitor.core.di.DaggerCoreComponent

/**
 * Точка сборки графа. В чистом Dagger это делается руками: класс Application
 * создаёт корневой компонент и отдаёт его наружу через CoreComponentProvider.
 *
 * by lazy — чтобы Room и Retrofit создавались при первом обращении,
 * а не в onCreate: старт приложения от этого заметно быстрее.
 */
class App : Application(), CoreComponentProvider {

    override val coreComponent: CoreComponent by lazy {
        DaggerCoreComponent.factory().create(this)
    }
}
