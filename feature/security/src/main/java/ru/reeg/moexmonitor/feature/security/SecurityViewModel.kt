package ru.reeg.moexmonitor.feature.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import java.util.Optional
import javax.inject.Inject
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider
import ru.reeg.moexmonitor.core.common.rx.orNull
import ru.reeg.moexmonitor.core.common.userMessage
import ru.reeg.moexmonitor.core.data.repository.MarketRepository
import ru.reeg.moexmonitor.core.data.repository.WatchlistRepository
import ru.reeg.moexmonitor.core.di.simpleViewModelFactory
import ru.reeg.moexmonitor.core.model.Candle
import ru.reeg.moexmonitor.core.model.CandleRange
import ru.reeg.moexmonitor.core.model.SecurityQuote

/**
 * secId известен только в рантайме, поэтому его нельзя получить через @Inject.
 * В Hilt это решал SavedStateHandle, здесь — своя маленькая фабрика:
 * зависимости в неё приносит Dagger, а параметр передаёт фрагмент.
 *
 * Это ручной аналог @AssistedInject, только без дополнительной библиотеки.
 */
class SecurityViewModelFactory @Inject constructor(
    private val marketRepository: MarketRepository,
    private val watchlistRepository: WatchlistRepository,
    private val schedulers: SchedulerProvider,
) {
    fun create(secId: String): ViewModelProvider.Factory = simpleViewModelFactory {
        SecurityViewModel(secId, marketRepository, watchlistRepository, schedulers)
    }
}

class SecurityViewModel(
    val secId: String,
    private val marketRepository: MarketRepository,
    private val watchlistRepository: WatchlistRepository,
    private val schedulers: SchedulerProvider,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val range = BehaviorSubject.createDefault(CandleRange.MONTH)
    private val isLoading = BehaviorSubject.createDefault(true)
    private val errorMessage = BehaviorSubject.createDefault(Optional.empty<String>())

    private val candles: Flowable<List<Candle>> = range
        .toFlowable(BackpressureStrategy.LATEST)
        .switchMap { marketRepository.observeCandles(secId, it) }

    /**
     * combineLatest в RxJava2 без RxKotlin требует явного BiFunction/Function3,
     * а вариантов с 5 аргументами лучше избегать — читать их невозможно.
     * Поэтому склеиваем данные (2) и состояние (3), а потом эти две группы.
     */
    private val data: Flowable<Pair<Optional<SecurityQuote>, List<Candle>>> =
        Flowable.combineLatest(
            marketRepository.observeSecurity(secId),
            candles,
            BiFunction<Optional<SecurityQuote>, List<Candle>, Pair<Optional<SecurityQuote>, List<Candle>>> {
                quote, candleList ->
                quote to candleList
            },
        )

    private val progress: Flowable<Triple<CandleRange, Boolean, Optional<String>>> =
        Flowable.combineLatest(
            range.toFlowable(BackpressureStrategy.LATEST),
            isLoading.toFlowable(BackpressureStrategy.LATEST),
            errorMessage.toFlowable(BackpressureStrategy.LATEST),
            Function3<CandleRange, Boolean, Optional<String>, Triple<CandleRange, Boolean, Optional<String>>> {
                selectedRange, loading, error ->
                Triple(selectedRange, loading, error)
            },
        )

    val uiState: Flowable<SecurityUiState> = Flowable.combineLatest(
        data,
        progress,
        BiFunction<
            Pair<Optional<SecurityQuote>, List<Candle>>,
            Triple<CandleRange, Boolean, Optional<String>>,
            SecurityUiState,
            > { (quote, candleList), (selectedRange, loading, error) ->
            SecurityUiState(
                secId = secId,
                quote = quote.orNull(),
                candles = candleList,
                range = selectedRange,
                isLoading = loading,
                errorMessage = error.orNull(),
            )
        },
    )
        .observeOn(schedulers.ui())
        .replay(1)
        .refCount()

    init {
        loadCandles(CandleRange.MONTH)
    }

    fun selectRange(newRange: CandleRange) {
        if (range.value == newRange) return
        range.onNext(newRange)
        loadCandles(newRange)
    }

    fun toggleFavorite() {
        disposables.add(
            watchlistRepository.toggle(secId).subscribe({ }, { error ->
                errorMessage.onNext(Optional.of(error.userMessage()))
            }),
        )
    }

    private fun loadCandles(newRange: CandleRange) {
        isLoading.onNext(true)
        errorMessage.onNext(Optional.empty())
        disposables.add(
            // Сегмент влияет на market/board в URL свечей, поэтому берём его
            // из кэша; если бумаги ещё нет в базе — считаем её акцией.
            marketRepository.segmentOf(secId)
                .flatMapCompletable { segment ->
                    marketRepository.refreshCandles(secId, segment, newRange)
                }
                .observeOn(schedulers.ui())
                .subscribe(
                    { isLoading.onNext(false) },
                    { error ->
                        errorMessage.onNext(Optional.of(error.userMessage()))
                        isLoading.onNext(false)
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

    companion object {
        const val ARG_SEC_ID = "secId"
    }
}
