package ru.reeg.moexmonitor.feature.market

import androidx.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import java.util.Optional
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.orNull
import ru.reeg.moexmonitor.core.common.userMessage
import ru.reeg.moexmonitor.core.data.repository.MarketRepository
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.model.MarketList
import ru.reeg.moexmonitor.core.model.MarketSegment
import ru.reeg.moexmonitor.core.model.SecurityQuote
import ru.reeg.moexmonitor.core.ui.ext.ListUiState

/**
 * Состояние экрана держим в BehaviorSubject — это Rx-аналог StateFlow:
 * есть текущее значение, и новый подписчик сразу его получает.
 *
 * Наружу отдаём Flowable с .replay(1).refCount(): при повороте экрана
 * подписка пересоздаётся, но цепочка к базе не собирается заново с нуля.
 */
class MarketViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val watchlistRepository: WatchlistRepository,
    private val schedulers: SchedulerProvider,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val selectedList = BehaviorSubject.createDefault(MarketList.ALL)
    private val searchQuery = BehaviorSubject.createDefault("")
    private val isRefreshing = BehaviorSubject.createDefault(false)
    private val errorMessage = BehaviorSubject.createDefault(Optional.empty<String>())

    val currentList: Flowable<MarketList> = selectedList
        .toFlowable(BackpressureStrategy.LATEST)
        .observeOn(schedulers.ui())

    /** Значение главного индекса для шапки экрана. */
    val mainIndex: Flowable<Optional<SecurityQuote>> = marketRepository.observeMainIndex()
        .observeOn(schedulers.ui())
        .replay(1)
        .refCount()

    private val items: Flowable<List<SecurityQuote>> = Observable.combineLatest(
        selectedList,
        // debounce не даёт дёргать базу на каждое нажатие клавиши,
        // а startWith нужен, чтобы первый (пустой) запрос ушёл сразу,
        // не ожидая 250 мс.
        searchQuery
            .debounce(SEARCH_DEBOUNCE_MS, TimeUnit.MILLISECONDS, schedulers.computation())
            .startWith("")
            .map { it.trim() }
            .distinctUntilChanged(),
        BiFunction<MarketList, String, Pair<MarketList, String>> { list, query -> list to query },
    )
        .toFlowable(BackpressureStrategy.LATEST)
        // switchMap, а не flatMap: старая подписка на список отменяется,
        // иначе результаты предыдущего запроса могли бы прийти последними.
        .switchMap { (list, query) ->
            if (query.isEmpty()) {
                marketRepository.observeList(list)
            } else {
                marketRepository.search(query)
            }
        }

    val uiState: Flowable<ListUiState<SecurityQuote>> = Flowable.combineLatest(
        items,
        isRefreshing.toFlowable(BackpressureStrategy.LATEST),
        errorMessage.toFlowable(BackpressureStrategy.LATEST),
        Function3<List<SecurityQuote>, Boolean, Optional<String>, ListUiState<SecurityQuote>> {
            data, refreshing, error ->
            ListUiState(items = data, isRefreshing = refreshing, errorMessage = error.orNull())
        },
    )
        .observeOn(schedulers.ui())
        .replay(1)
        .refCount()

    init {
        refresh()
    }

    fun selectList(list: MarketList) {
        selectedList.onNext(list)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.onNext(query)
    }

    fun toggleFavorite(secId: String) {
        disposables.add(
            watchlistRepository.toggle(secId).subscribe({ }, { error ->
                errorMessage.onNext(Optional.of(error.userMessage()))
            }),
        )
    }

    fun refresh() {
        isRefreshing.onNext(true)
        errorMessage.onNext(Optional.empty())
        disposables.add(
            // mergeDelayError: если упал один запрос, второй всё равно доедет.
            // Акции и фонды приходят одним запросом, индекс — отдельным.
            Completable.mergeDelayError(
                listOf(
                    marketRepository.refreshSegment(MarketSegment.SHARES),
                    marketRepository.refreshSegment(MarketSegment.INDEX),
                ),
            )
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

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
