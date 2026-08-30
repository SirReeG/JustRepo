package ru.reeg.moexmonitor.feature.market

import io.reactivex.Scheduler
import io.reactivex.schedulers.TestScheduler
import ru.reeg.moexmonitor.core.common.rx.SchedulerProvider

/**
 * Все планировщики — один TestScheduler, поэтому в тесте мы полностью
 * управляем временем через advanceTimeBy: debounce перестаёт быть проблемой.
 *
 * Это и есть главная причина, по которой SchedulerProvider вообще существует.
 */
class TestSchedulerProvider(
    val scheduler: TestScheduler = TestScheduler(),
) : SchedulerProvider {
    override fun io(): Scheduler = scheduler
    override fun computation(): Scheduler = scheduler
    override fun ui(): Scheduler = scheduler
}
