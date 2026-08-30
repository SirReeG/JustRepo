package ru.reeg.moexmonitor.core.common.rx

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Планировщики прячем за интерфейсом, а не вызываем Schedulers.io() напрямую.
 * В тестах вместо них подставляется Schedulers.trampoline() или TestScheduler —
 * иначе unit-тесты на ViewModel становятся асинхронными и flaky.
 */
interface SchedulerProvider {
    /** Сеть и база данных. */
    fun io(): Scheduler

    /** Тяжёлые вычисления, debounce, таймеры. */
    fun computation(): Scheduler

    /** Главный поток Android. */
    fun ui(): Scheduler
}

@Singleton
class AppSchedulerProvider @Inject constructor() : SchedulerProvider {
    override fun io(): Scheduler = Schedulers.io()
    override fun computation(): Scheduler = Schedulers.computation()
    override fun ui(): Scheduler = AndroidSchedulers.mainThread()
}
