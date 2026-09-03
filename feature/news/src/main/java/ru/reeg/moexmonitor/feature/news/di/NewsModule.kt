package ru.reeg.moexmonitor.feature.news.di

import dagger.Module
import dagger.Provides
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.repository.NewsRepository
import ru.reeg.moexmonitor.feature.news.NewsDetailViewModel
import ru.reeg.moexmonitor.feature.news.NewsViewModel

@Module
object NewsModule {

    @Provides
    fun provideNewsViewModel(
        newsRepository: NewsRepository,
        schedulers: SchedulerProvider,
    ): NewsViewModel {
        return NewsViewModel(
            newsRepository = newsRepository,
            schedulers = schedulers,
        )
    }

    @Provides
    fun provideNewsDetailViewModel(
        newsRepository: NewsRepository,
        schedulers: SchedulerProvider,
    ): NewsDetailViewModel {
        return NewsDetailViewModel(
            newsRepository = newsRepository,
            schedulers = schedulers,
        )
    }
}