package ru.reeg.moexmonitor.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.util.Optional
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.data.repository.NewsRepository
import ru.reeg.moexmonitor.core.model.NewsItem




/** Ручная assisted-фабрика: newsId известен только в рантайме. */
class NewsDetailViewModel(
    private val newsRepository: NewsRepository,
    private val schedulers: SchedulerProvider,
) : ViewModel() {

    private var newsId: Long = 0L
    private val disposables = CompositeDisposable()

    private val _newsItem = BehaviorSubject.create<Optional<NewsItem>>()
    val newsItem: Observable<Optional<NewsItem>> = _newsItem
        .hide()
        .observeOn(schedulers.ui())

    fun setNewsId(id: Long) {
        disposables.clear()

        this.newsId = id

        disposables.add(
            newsRepository.observeNewsItem(id) // Observable
                .observeOn(schedulers.ui())
                .subscribe(
                    { newsItem -> _newsItem.onNext(newsItem) },
                    { error -> /* Обработка ошибки */ }
                )
        )

        disposables.add(
            newsRepository.refreshNewsBody(id)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { /* success */ },
                    { error -> /* Обработка ошибки */ }
                )
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    companion object {
        const val ARG_NEWS_ID = "newsId"
    }
}
