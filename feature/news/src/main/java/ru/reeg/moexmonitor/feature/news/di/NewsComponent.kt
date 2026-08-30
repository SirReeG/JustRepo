package ru.reeg.moexmonitor.feature.news.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoMap
import ru.reeg.moexmonitor.core.di.CoreComponent
import ru.reeg.moexmonitor.core.di.ViewModelKey
import ru.reeg.moexmonitor.feature.news.NewsDetailFragment
import ru.reeg.moexmonitor.feature.news.NewsFragment
import ru.reeg.moexmonitor.feature.news.NewsViewModel

/**
 * Один компонент на модуль обслуживает оба экрана фичи:
 * список новостей (ViewModel из карты) и деталь (ViewModel из своей фабрики).
 */
@Component(dependencies = [CoreComponent::class], modules = [NewsModule::class])
interface NewsComponent {

    fun inject(fragment: NewsFragment)

    fun inject(fragment: NewsDetailFragment)

    @Component.Factory
    interface Factory {
        fun create(coreComponent: CoreComponent): NewsComponent
    }
}

@Module
interface NewsModule {

    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    fun bindNewsViewModel(viewModel: NewsViewModel): ViewModel
}
