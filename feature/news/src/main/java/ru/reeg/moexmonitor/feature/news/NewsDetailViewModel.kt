package ru.reeg.moexmonitor.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.util.Optional
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.repository.NewsRepository
import ru.reeg.moexmonitor.core.di.simpleViewModelFactory
import ru.reeg.moexmonitor.core.model.NewsItem

/** Ручная assisted-фабрика: newsId известен только в рантайме. */
class NewsDetailViewModelFactory @Inject constructor(
    private val newsRepository: NewsRepository,
    private val schedulers: SchedulerProvider,
) {
    fun create(newsId: Long): ViewModelProvider.Factory = simpleViewModelFactory {
        NewsDetailViewModel(newsId, newsRepository, schedulers)
    }
}

class NewsDetailViewModel(
    private val newsId: Long,
    private val newsRepository: NewsRepository,
    private val schedulers: SchedulerProvider,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    val newsItem: Flowable<Optional<NewsItem>> = newsRepository.observeNewsItem(newsId)
        .observeOn(schedulers.ui())
        .replay(1)
        .refCount()

    init {
        // Заголовки уже в кэше, а текст догружаем только при открытии — экономим трафик.
        disposables.add(newsRepository.refreshNewsBody(newsId).subscribe({ }, { }))
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    companion object {
        const val ARG_NEWS_ID = "newsId"
    }
}
