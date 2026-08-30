package ru.reeg.moexmonitor.core.common

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Абстракция над «текущим временем». Нужна не ради красоты:
 * с ней репозитории и кэш-политики тестируются без ожиданий и flaky-тестов.
 */
interface TimeProvider {
    fun nowMillis(): Long
}

@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
