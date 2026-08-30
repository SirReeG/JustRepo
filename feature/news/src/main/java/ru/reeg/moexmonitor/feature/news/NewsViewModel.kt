package ru.reeg.moexmonitor.feature.news

import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import java.util.Optional
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.orNull
import ru.reeg.moexmonitor.core.common.userMessage
import ru.reeg.moexmonitor.core.data.repository.NewsRepository
import ru.reeg.moexmonitor.core.model.NewsItem
import ru.reeg.moexmonitor.core.ui.ext.ListUiState

class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val schedulers: SchedulerProvider,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val isRefreshing = BehaviorSubject.createDefault(true)
    private val errorMessage = BehaviorSubject.createDefault(Optional.empty<String>())

    val uiState: Flowable<ListUiState<NewsItem>> = Flowable.combineLatest(
        newsRepository.observeNews(),
        isRefreshing.toFlowable(BackpressureStrategy.LATEST),
        errorMessage.toFlowable(BackpressureStrategy.LATEST),
        Function3<List<NewsItem>, Boolean, Optional<String>, ListUiState<NewsItem>> {
            items, refreshing, error ->
            ListUiState(items = items, isRefreshing = refreshing, errorMessage = error.orNull())
        },
    )
        .observeOn(schedulers.ui())
        .replay(1)
        .refCount()

    init {
        refresh()
    }

    fun refresh() {
        isRefreshing.onNext(true)
        errorMessage.onNext(Optional.empty())
        disposables.add(
            newsRepository.refreshNews()
                .observeOn(schedulers.ui())
                .subscribe(
                    { isRefreshing.onNext(false) },
                    { error ->
                        errorMessage.onNext(Optional.of(error.userMessage()))
                        isRefreshing.onNext(false)
                    },
                ),
        )
    }

    fun consumeError() {
        errorMessage.onNext(Optional.empty())
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
