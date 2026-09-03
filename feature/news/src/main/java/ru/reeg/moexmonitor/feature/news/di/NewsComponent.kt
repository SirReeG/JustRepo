package ru.reeg.moexmonitor.feature.news.di

import dagger.Component
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.FeatureScope
import ru.reeg.moexmonitor.feature.news.NewsDetailFragment
import ru.reeg.moexmonitor.feature.news.NewsFragment

@FeatureScope
@Component(
    dependencies = [CoreComponent::class],
    modules = [NewsModule::class]
)
interface NewsComponent {
    fun inject(fragment: NewsFragment)

    fun inject(fragment: NewsDetailFragment)

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): NewsComponent
    }
}